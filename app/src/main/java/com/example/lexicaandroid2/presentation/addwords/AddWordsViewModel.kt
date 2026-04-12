package com.example.lexicaandroid2.presentation.addwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.data.local.WordReserveEntity
import com.example.lexicaandroid2.data.remote.model.WordResult
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.domain.repository.WordReserveRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// ─── UI State ────────────────────────────────────────────────────────────────

data class AddWordsUiState(
    val searchQuery: String = "",
    val localMatches: List<Flashcard> = emptyList(),       // mots déjà dans la collection
    val apiResults: List<WordResult> = emptyList(),         // résultats API externe
    val isApiLoading: Boolean = false,
    val selectedResult: WordResult? = null,                 // fiche d'aperçu ouverte
    val previewDefinition: String = "",                     // définition éditable dans la fiche
    val previewSynonymes: String = "",                      // synonymes éditables (séparés par virgule)
    val manualMode: Boolean = false,                        // formulaire manuel ouvert
    val manualWord: String = "",
    val manualDefinition: String = "",
    val manualSynonymes: String = "",
    val manualCategorie: String = "",
    val manualEtymologie: String = "",
    val manualExemples: String = "",
    val manualExpanded: Boolean = false,                    // accordéon champs optionnels
    val duplicateCandidate: Flashcard? = null,              // doublon détecté → AlertDialog
    val pendingWord: WordResult? = null,                    // mot en attente après résolution doublon
    val successMessage: String? = null,
    val error: String? = null,
    // mots proposés (réserve locale, affichés quand pas de recherche)
    val proposedWords: List<WordReserveEntity> = emptyList(),
    val isLoadingProposed: Boolean = false,
    /**
     * Mots ajoutés pendant la session courante.
     * Clé : `mot.trim()` pour uniformiser les résultats API et réserve locale.
     * Valeur : la Flashcard créée (pour pouvoir la supprimer / mettre en favori).
     * Remis à zéro à chaque entrée sur l'écran.
     */
    val addedInSession: Map<String, Flashcard> = emptyMap()
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

class AddWordsViewModel(
    private val wordReserveRepository: WordReserveRepository,
    private val flashcardRepository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWordsUiState())
    val uiState: StateFlow<AddWordsUiState> = _uiState.asStateFlow()

    private var debounceJob: Job? = null
    private var allCards: List<Flashcard> = emptyList()
    private var latestSearchRequestId: Long = 0L

    init {
        refreshScreenData(resetSessionMarkers = false, resetSearchState = false)
    }

    // ── Entrée sur l'écran ────────────────────────────────────────────────────

    /**
     * À appeler via LaunchedEffect(Unit) dans le composable.
     * Recharge la réserve (sans les mots déjà ajoutés),
     * rafraîchit allCards (corrige le bug "mot supprimé encore visible"),
     * remet à zéro les marqueurs de session et nettoie l'état de recherche.
     */
    fun onScreenEntered() {
        refreshScreenData(resetSessionMarkers = true, resetSearchState = true)
    }

    // ── Chargement initial ────────────────────────────────────────────────────

    private fun refreshScreenData(
        resetSessionMarkers: Boolean,
        resetSearchState: Boolean
    ) {
        viewModelScope.launch {
            if (resetSearchState) {
                debounceJob?.cancel()
                latestSearchRequestId++
            }

            _uiState.update { state ->
                state.copy(
                    isLoadingProposed = true,
                    addedInSession = if (resetSessionMarkers) emptyMap() else state.addedInSession,
                    searchQuery = if (resetSearchState) "" else state.searchQuery,
                    localMatches = if (resetSearchState) emptyList() else state.localMatches,
                    apiResults = if (resetSearchState) emptyList() else state.apiResults,
                    isApiLoading = false,
                    error = null,
                    selectedResult = if (resetSearchState) null else state.selectedResult
                )
            }

            var latestCards = allCards
            runCatching { flashcardRepository.getAllCards() }
                .onSuccess { cards ->
                    allCards = cards
                    latestCards = cards
                }

            runCatching { wordReserveRepository.getProposedWords(50) }
                .onSuccess { words ->
                    _uiState.update { state ->
                        state.copy(
                            proposedWords = filterSuggestedWords(words, latestCards),
                            localMatches = computeLocalMatches(state.searchQuery, latestCards),
                            isLoadingProposed = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(
                            proposedWords = filterSuggestedWords(state.proposedWords, latestCards),
                            localMatches = computeLocalMatches(state.searchQuery, latestCards),
                            isLoadingProposed = false
                        )
                    }
                }
        }
    }

    // ── Barre de recherche ───────────────────────────────────────────────────

    fun onSearchQueryChanged(query: String) {
        debounceJob?.cancel()
        val requestId = ++latestSearchRequestId

        _uiState.update {
            it.copy(
                searchQuery = query,
                error = null,
                successMessage = null,
                isApiLoading = false
            )
        }

        // Filtre local instantané (dès 2 caractères)
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            val matches = computeLocalMatches(trimmed, allCards)
            _uiState.update { it.copy(localMatches = matches) }
        } else {
            _uiState.update {
                it.copy(
                    localMatches = emptyList(),
                    apiResults = emptyList(),
                    isApiLoading = false,
                    error = null
                )
            }
            return
        }

        // Debounce 500ms → recherche API
        debounceJob = viewModelScope.launch {
            delay(500)
            searchApi(trimmed, requestId)
        }
    }

    private suspend fun searchApi(query: String, requestId: Long) {
        if (!isSearchRequestStillCurrent(query, requestId)) return

        _uiState.update { it.copy(isApiLoading = true, apiResults = emptyList()) }

        try {
            val results = wordReserveRepository.searchOnline(query)
            if (!isSearchRequestStillCurrent(query, requestId)) return

            val wordResults = results.map { entity ->
                WordResult(
                    mot = entity.mot,
                    definition = entity.definition,
                    categorieGrammaticale = entity.categorieGrammaticale,
                    exemples = entity.exemples,
                    synonymes = entity.synonymes
                )
            }
            _uiState.update {
                it.copy(
                    apiResults = wordResults,
                    isApiLoading = false,
                    error = if (wordResults.isEmpty() && it.localMatches.isEmpty())
                        "Pas de résultat — tu peux ajouter le mot manuellement" else null
                )
            }
        } catch (e: Exception) {
            if (!isSearchRequestStillCurrent(query, requestId)) return

            _uiState.update {
                it.copy(
                    isApiLoading = false,
                    error = "Pas de connexion — tu peux ajouter le mot manuellement"
                )
            }
        }
    }

    fun clearSearch() {
        debounceJob?.cancel()
        latestSearchRequestId++
        _uiState.update {
            it.copy(
                searchQuery = "",
                localMatches = emptyList(),
                apiResults = emptyList(),
                isApiLoading = false,
                error = null,
                successMessage = null
            )
        }
    }

    // ── Fiche d'aperçu ───────────────────────────────────────────────────────

    fun openPreview(result: WordResult) {
        _uiState.update {
            it.copy(
                selectedResult = result,
                previewDefinition = result.definition,
                previewSynonymes = result.synonymes.joinToString(", ")
            )
        }
    }

    fun onPreviewDefinitionChanged(value: String) {
        _uiState.update { it.copy(previewDefinition = value) }
    }

    fun onPreviewSynonymesChanged(value: String) {
        _uiState.update { it.copy(previewSynonymes = value) }
    }

    fun closePreview() {
        _uiState.update { it.copy(selectedResult = null) }
    }

    fun addWordResult(result: WordResult) {
        checkDuplicateAndAdd(result)
    }

    /** Confirmer l'ajout depuis la fiche d'aperçu. */
    fun confirmAddFromPreview() {
        val result = _uiState.value.selectedResult ?: return
        val wordResult = result.copy(
            definition = _uiState.value.previewDefinition,
            synonymes = _uiState.value.previewSynonymes
                .split(",").map { it.trim() }.filter { it.isNotBlank() }
        )
        checkDuplicateAndAdd(wordResult)
    }

    // ── Gestion des doublons ─────────────────────────────────────────────────

    private fun checkDuplicateAndAdd(word: WordResult) {
        val duplicate = allCards.firstOrNull {
            it.recto.equals(word.mot.trim(), ignoreCase = true)
        }
        if (duplicate != null) {
            _uiState.update {
                it.copy(duplicateCandidate = duplicate, pendingWord = word)
            }
        } else {
            doAddWord(word)
        }
    }

    /** Remplacer la définition du doublon existant. */
    fun resolveDuplicateReplace() {
        val pending = _uiState.value.pendingWord ?: return
        val existing = _uiState.value.duplicateCandidate ?: return
        viewModelScope.launch {
            runCatching {
                flashcardRepository.saveCard(
                    existing.copy(verso = pending.definition, synonymes = pending.synonymes)
                )
            }
            allCards = allCards.map { if (it.id == existing.id) it.copy(verso = pending.definition) else it }
            _uiState.update {
                it.copy(
                    duplicateCandidate = null,
                    pendingWord = null,
                    selectedResult = null,
                    successMessage = "\"${pending.mot}\" mis à jour !"
                )
            }
            clearSuccessAfterDelay()
        }
    }

    /** Ignorer le doublon et annuler l'ajout. */
    fun resolveDuplicateCancel() {
        _uiState.update { it.copy(duplicateCandidate = null, pendingWord = null) }
    }

    private fun doAddWord(word: WordResult) {
        viewModelScope.launch {
            runCatching {
                val flashcard = Flashcard(
                    id = UUID.randomUUID().toString(),
                    recto = word.mot.trim(),
                    verso = word.definition.trim(),
                    synonymes = word.synonymes,
                    exemples = word.exemples,
                    categorieGrammaticale = word.categorieGrammaticale
                )
                flashcardRepository.saveCard(flashcard)
                allCards = allCards + flashcard
                flashcard
            }.onSuccess { flashcard ->
                val key = word.mot.trim()
                _uiState.update { state ->
                    state.copy(
                        selectedResult = null,
                        manualMode = false,
                        addedInSession = state.addedInSession + (key to flashcard)
                    )
                }
                clearSuccessAfterDelay()
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message ?: "Erreur lors de l'ajout") }
            }
        }
    }

    // ── Ajout depuis la réserve (mots proposés) ──────────────────────────────

    fun addWordFromReserve(word: WordReserveEntity) {
        viewModelScope.launch {
            runCatching { wordReserveRepository.addToCollection(word) }
                .onSuccess {
                    val flashcard = Flashcard(
                        id = word.id, recto = word.mot, verso = word.definition,
                        synonymes = word.synonymes, exemples = word.exemples,
                        categorieGrammaticale = word.categorieGrammaticale
                    )
                    allCards = allCards + flashcard
                    // Clé unifiée sur mot.trim() — même clé que doAddWord() pour que
                    // l'ajout via PreviewDialog et via bouton direct soient cohérents.
                    val key = word.mot.trim()
                    _uiState.update { state ->
                        state.copy(
                            addedInSession = state.addedInSession + (key to flashcard)
                        )
                    }
                    clearSuccessAfterDelay()
                }
        }
    }

    // ── Actions sur les mots ajoutés pendant la session ───────────────────────

    /** Supprime un mot ajouté cette session (par sa clé dans addedInSession). */
    fun deleteAddedWord(key: String) {
        val card = _uiState.value.addedInSession[key] ?: return
        viewModelScope.launch {
            runCatching { flashcardRepository.deleteCard(card.id) }
                .onSuccess {
                    allCards = allCards.filter { it.id != card.id }
                    _uiState.update { state ->
                        state.copy(addedInSession = state.addedInSession - key)
                    }
                }
        }
    }

    /** Bascule le favori d'un mot ajouté cette session. */
    fun toggleFavoriteAddedWord(key: String) {
        val card = _uiState.value.addedInSession[key] ?: return
        val newFavori = !card.favori
        viewModelScope.launch {
            runCatching { flashcardRepository.setFavorite(card.id, newFavori) }
                .onSuccess {
                    val updated = card.copy(favori = newFavori)
                    allCards = allCards.map { if (it.id == card.id) updated else it }
                    _uiState.update { state ->
                        state.copy(addedInSession = state.addedInSession + (key to updated))
                    }
                }
        }
    }

    // ── Mode manuel ──────────────────────────────────────────────────────────

    fun openManualMode() {
        _uiState.update {
            it.copy(
                manualMode = true,
                manualWord = _uiState.value.searchQuery,
                manualDefinition = "",
                manualSynonymes = "",
                manualCategorie = "",
                manualEtymologie = "",
                manualExemples = "",
                manualExpanded = false
            )
        }
    }

    fun closeManualMode() { _uiState.update { it.copy(manualMode = false) } }

    fun onManualWordChanged(v: String)       { _uiState.update { it.copy(manualWord = v) } }
    fun onManualDefinitionChanged(v: String) { _uiState.update { it.copy(manualDefinition = v) } }
    fun onManualSynonymesChanged(v: String)  { _uiState.update { it.copy(manualSynonymes = v) } }
    fun onManualCategorieChanged(v: String)  { _uiState.update { it.copy(manualCategorie = v) } }
    fun onManualEtymologieChanged(v: String) { _uiState.update { it.copy(manualEtymologie = v) } }
    fun onManualExemplesChanged(v: String)   { _uiState.update { it.copy(manualExemples = v) } }
    fun toggleManualExpanded()               { _uiState.update { it.copy(manualExpanded = !it.manualExpanded) } }

    fun confirmManualAdd() {
        val state = _uiState.value
        if (state.manualWord.isBlank() || state.manualDefinition.isBlank()) {
            _uiState.update { it.copy(error = "Le mot et la définition sont obligatoires") }
            return
        }
        val word = WordResult(
            mot = state.manualWord.trim(),
            definition = state.manualDefinition.trim(),
            synonymes = state.manualSynonymes.split(",").map { it.trim() }.filter { it.isNotBlank() },
            exemples = state.manualExemples.split(",").map { it.trim() }.filter { it.isNotBlank() },
            categorieGrammaticale = state.manualCategorie.trim()
        )
        checkDuplicateAndAdd(word)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun clearSuccessAfterDelay() {
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(successMessage = null) }
        }
    }

    private fun computeLocalMatches(query: String, cards: List<Flashcard>): List<Flashcard> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()

        return cards.filter {
            it.recto.contains(trimmed, ignoreCase = true) ||
                it.verso.contains(trimmed, ignoreCase = true)
        }.take(5)
    }

    private fun filterSuggestedWords(
        words: List<WordReserveEntity>,
        cards: List<Flashcard>
    ): List<WordReserveEntity> {
        val existingWords = cards.mapTo(mutableSetOf()) { it.recto.normalizedWordKey() }
        return words.filterNot { it.mot.normalizedWordKey() in existingWords }
    }

    private fun isSearchRequestStillCurrent(query: String, requestId: Long): Boolean {
        val currentState = _uiState.value
        return requestId == latestSearchRequestId && currentState.searchQuery.trim() == query
    }

    private fun String.normalizedWordKey(): String = trim().lowercase()
}

// ─── Factory ─────────────────────────────────────────────────────────────────

class AddWordsViewModelFactory(
    private val wordReserveRepository: WordReserveRepository,
    private val flashcardRepository: FlashcardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddWordsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddWordsViewModel(wordReserveRepository, flashcardRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

