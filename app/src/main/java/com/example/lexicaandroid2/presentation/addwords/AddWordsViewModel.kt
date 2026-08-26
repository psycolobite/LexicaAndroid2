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
    val isLoadingProposed: Boolean = false
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

    init {
        loadAllCardsAndProposed()
    }

    // ── Chargement initial et rafraîchissement ───────────────────────────────

    fun refreshProposedWords() {
        loadAllCardsAndProposed()
    }

    private fun loadAllCardsAndProposed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProposed = true) }
            runCatching {
                val cards = flashcardRepository.getAllCards()
                allCards = cards
                val existingWords = cards.map { it.recto.trim().lowercase() }.toSet()
                val availableWords = wordReserveRepository.getProposedWords(1000)
                    .filter { it.mot.trim().lowercase() !in existingWords }
                availableWords.shuffled().take(50)
            }.onSuccess { words ->
                _uiState.update { it.copy(proposedWords = words, isLoadingProposed = false) }
            }.onFailure {
                _uiState.update { it.copy(isLoadingProposed = false) }
            }
        }
    }

    // ── Barre de recherche ───────────────────────────────────────────────────

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, error = null, successMessage = null) }

        // Filtre local instantané (dès 2 caractères)
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            val matches = allCards.filter {
                it.recto.contains(trimmed, ignoreCase = true) ||
                it.verso.contains(trimmed, ignoreCase = true)
            }.take(5)
            _uiState.update { it.copy(localMatches = matches) }
        } else {
            _uiState.update { it.copy(localMatches = emptyList(), apiResults = emptyList()) }
            return
        }

        // Debounce 500ms → recherche API
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(500)
            searchApi(trimmed)
        }
    }

    private fun searchApi(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isApiLoading = true, apiResults = emptyList()) }
            try {
                val results = wordReserveRepository.searchOnline(query)
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
                _uiState.update {
                    it.copy(
                        isApiLoading = false,
                        error = "Pas de connexion — tu peux ajouter le mot manuellement"
                    )
                }
            }
        }
    }

    fun clearSearch() {
        debounceJob?.cancel()
        _uiState.update {
            it.copy(
                searchQuery = "",
                localMatches = emptyList(),
                apiResults = emptyList(),
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
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        selectedResult = null,
                        manualMode = false,
                        successMessage = "\"${word.mot}\" ajouté à ta liste !"
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
                    allCards = allCards + Flashcard(
                        id = word.id, recto = word.mot, verso = word.definition,
                        synonymes = word.synonymes, exemples = word.exemples,
                        categorieGrammaticale = word.categorieGrammaticale
                    )
                    _uiState.update { state ->
                        state.copy(
                            proposedWords = state.proposedWords.filter { it.id != word.id },
                            successMessage = "\"${word.mot}\" ajouté à ta liste !"
                        )
                    }
                    clearSuccessAfterDelay()
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

    fun closeManualMode() {
        _uiState.update { it.copy(manualMode = false) }
    }

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

