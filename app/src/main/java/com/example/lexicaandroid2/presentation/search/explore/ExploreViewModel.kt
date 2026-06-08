package com.example.lexicaandroid2.presentation.search.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.data.corpus.CorpusIndex
import com.example.lexicaandroid2.data.corpus.ExtractCandidate
import com.example.lexicaandroid2.presentation.search.preferences.UserPreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =============================================================================
// ÉTATS DE L'ÉCRAN (cf. ExploreScreenSpec.kt §2)
// =============================================================================

/**
 * État global de l'écran Explore.
 * Reprend la sealed class définie dans ExploreScreenSpec.kt.
 */
sealed class ExploreUiState {

    /** Chargement en cours — premier extrait pas encore prêt */
    data object Loading : ExploreUiState()

    /** État nominal : un extrait est affiché */
    data class ExtractDisplayed(
        val extract: ExtractUiModel,
        val wordStates: Map<String, WordStatus>,
        val interestRating: Int?,
        val isTransitioning: Boolean,
        val historyIndex: Int,
        val historySize: Int
    ) : ExploreUiState()

    /** Aucun extrait disponible */
    data class NoExtractAvailable(
        val reason: NoExtractReason,
        val suggestion: String?
    ) : ExploreUiState()

    /** Erreur */
    data class Error(
        val message: String,
        val isRetryable: Boolean
    ) : ExploreUiState()
}

/**
 * Modèle UI d'un extrait affiché à l'écran.
 */
data class ExtractUiModel(
    val id: String,
    val content: String,
    val highlightedWords: List<String>,
    val sourceTitle: String,
    val sourceAuthor: String,
    val sourceYear: String?,
    val sourceUrl: String?,
    val contentType: ExtractContentType,
    val videoUrl: String?,
    val domainTags: List<String>,
    val difficulty: String
)

enum class ExtractContentType { TEXT, VIDEO }

/**
 * Statut visuel d'un mot dans l'extrait.
 */
enum class WordStatus {
    NORMAL,
    SUGGESTED,
    ADDED,
    TRANSITIONING
}

/**
 * Raison pour laquelle aucun extrait n'est disponible.
 */
enum class NoExtractReason {
    CORPUS_EMPTY,
    PROFILE_TOO_NARROW,
    ALL_CONSUMED,
    PENDING_INDEX
}

// =============================================================================
// VIEWMODEL
// =============================================================================

/**
 * ViewModel de l'écran Explore.
 *
 * Gère le chargement des extraits, la navigation entre extraits,
 * l'ajout/retrait de mots, la notation et les états d'erreur.
 *
 * @param corpusIndex Index des extraits candidats (issu de TACHE_R3)
 * @param scoringEngine Moteur de scoring (issu de TACHE_R5, optionnel en V1)
 * @param rankingStrategy Stratégie de classement (issu de TACHE_R5, optionnel en V1)
 */
class ExploreViewModel(
    private val corpusIndex: CorpusIndex? = null,
    private val scoringEngine: Any? = null,    // ScoringEngine? — optionnel en V1
    private val rankingStrategy: Any? = null   // RankingStrategy? — optionnel en V1
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    /** Cache des extraits chargés pour la session courante */
    private val extractCache = mutableListOf<ExtractUiModel>()

    /** Index de l'extrait actuellement affiché dans le cache */
    private var currentIndex = 0

    /** Ensemble des IDs d'extraits déjà vus (pour éviter les doublons) */
    private val seenExtractIds = mutableSetOf<String>()

    /** Job de chargement pour annulation */
    private var loadJob: Job? = null

    /** Compteur de swipes rapides pour anti-brûlage */
    private var rapidSwipeCount = 0
    private var lastSwipeTime = 0L

    // =========================================================================
    // CHARGEMENT INITIAL
    // =========================================================================

    /**
     * Charge le premier extrait à l'ouverture de l'écran.
     * Utilise les préférences utilisateur si disponibles.
     */
    fun loadInitialExtract(preferences: UserPreferences? = null) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { ExploreUiState.Loading }
            try {
                // Simuler un délai de chargement pour l'UX
                delay(300)

                val candidates = getCandidates(preferences)
                if (candidates.isEmpty()) {
                    _uiState.update {
                        ExploreUiState.NoExtractAvailable(
                            reason = if (corpusIndex == null || corpusIndex.size() == 0)
                                NoExtractReason.CORPUS_EMPTY
                            else
                                NoExtractReason.PROFILE_TOO_NARROW,
                            suggestion = if (corpusIndex == null || corpusIndex.size() == 0)
                                "Aucun corpus chargé. Revenez plus tard."
                            else
                                "Essayez d'élargir vos centres d'intérêt dans les paramètres."
                        )
                    }
                    return@launch
                }

                // Construire le cache UI
                buildExtractCache(candidates)

                if (extractCache.isEmpty()) {
                    _uiState.update {
                        ExploreUiState.NoExtractAvailable(
                            reason = NoExtractReason.ALL_CONSUMED,
                            suggestion = "Vous avez vu tous les extraits disponibles. Revenez plus tard."
                        )
                    }
                    return@launch
                }

                currentIndex = 0
                showExtract(currentIndex)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    ExploreUiState.Error(
                        message = e.message ?: "Erreur inconnue",
                        isRetryable = true
                    )
                }
            }
        }
    }

    // =========================================================================
    // NAVIGATION ENTRE EXTRAITS
    // =========================================================================

    /**
     * Navigue vers l'extrait suivant.
     */
    fun navigateToNext() {
        val state = _uiState.value
        if (state !is ExploreUiState.ExtractDisplayed) return
        if (state.isTransitioning) return

        // Anti-brûlage : si l'utilisateur swipe trop vite
        if (isSwipingTooFast()) return

        if (currentIndex < extractCache.size - 1) {
            currentIndex++
            showExtract(currentIndex)
        } else {
            // Tenter de charger plus d'extraits
            viewModelScope.launch {
                val moreCandidates = loadMoreCandidates()
                if (moreCandidates.isEmpty()) {
                    _uiState.update {
                        ExploreUiState.NoExtractAvailable(
                            reason = NoExtractReason.ALL_CONSUMED,
                            suggestion = "Vous avez vu tous les extraits disponibles."
                        )
                    }
                } else {
                    buildExtractCache(moreCandidates)
                    currentIndex++
                    showExtract(currentIndex)
                }
            }
        }
    }

    /**
     * Navigue vers l'extrait précédent.
     */
    fun navigateToPrevious() {
        val state = _uiState.value
        if (state !is ExploreUiState.ExtractDisplayed) return
        if (state.isTransitioning) return
        if (currentIndex > 0) {
            currentIndex--
            showExtract(currentIndex)
        }
    }

    // =========================================================================
    // INTERACTIONS SUR LES MOTS
    // =========================================================================

    /**
     * Ajoute ou retire un mot du deck.
     * Tap court sur mot SUGGESTED → ajoute (passe en ADDED)
     * Tap court sur mot ADDED → retire (passe en SUGGESTED)
     */
    fun toggleWord(word: String) {
        _uiState.update { state ->
            if (state !is ExploreUiState.ExtractDisplayed) return@update state

            val currentStatus = state.wordStates[word] ?: WordStatus.NORMAL
            val newStatus = when (currentStatus) {
                WordStatus.SUGGESTED -> WordStatus.ADDED
                WordStatus.ADDED -> WordStatus.SUGGESTED
                else -> return@update state // NORMAL et TRANSITIONING ne sont pas toggleables
            }

            // Animation de transition
            val transitioningWords = state.wordStates.toMutableMap().apply {
                put(word, WordStatus.TRANSITIONING)
            }
            val transitionState = state.copy(wordStates = transitioningWords, isTransitioning = true)

            // Lancer l'animation puis appliquer le nouvel état
            viewModelScope.launch {
                delay(200) // durée de l'animation
                _uiState.update { s ->
                    if (s is ExploreUiState.ExtractDisplayed) {
                        val finalWords = s.wordStates.toMutableMap().apply {
                            put(word, newStatus)
                        }
                        s.copy(wordStates = finalWords, isTransitioning = false)
                    } else s
                }
            }

            state.copy(wordStates = transitioningWords, isTransitioning = true)
        }
    }

    /**
     * Affiche la définition d'un mot.
     * Actuellement stub — sera connecté à un dictionnaire dans une version future.
     */
    fun showDefinition(word: String) {
        // Stub : dans une version future, cela ouvrira un bottom sheet
        // avec la définition récupérée depuis une API dictionnaire.
        viewModelScope.launch {
            // Émettre un événement pour que l'UI affiche le bottom sheet
            _definitionEvent.emit(DefinitionEvent.ShowDefinition(word))
        }
    }

    // =========================================================================
    // NOTATION
    // =========================================================================

    /**
     * Note l'extrait courant (1-5).
     */
    fun rateExtract(rating: Int) {
        val clampedRating = rating.coerceIn(1, 5)
        _uiState.update { state ->
            if (state !is ExploreUiState.ExtractDisplayed) return@update state
            state.copy(interestRating = clampedRating)
        }
    }

    // =========================================================================
    // RETRY
    // =========================================================================

    /**
     * Recharge après une erreur.
     */
    fun retry() {
        loadInitialExtract()
    }

    // =========================================================================
    // ÉVÉNEMENTS DE DÉFINITION
    // =========================================================================

    private val _definitionEvent = MutableStateFlow<DefinitionEvent?>(null)
    val definitionEvent: StateFlow<DefinitionEvent?> = _definitionEvent.asStateFlow()

    /**
     * Événement de définition émis par le ViewModel.
     */
    sealed class DefinitionEvent {
        data class ShowDefinition(val word: String) : DefinitionEvent()
    }

    /**
     * Marque l'événement de définition comme consommé.
     */
    fun consumeDefinitionEvent() {
        _definitionEvent.update { null }
    }

    // =========================================================================
    // MÉTHODES PRIVÉES
    // =========================================================================

    /**
     * Récupère les candidats depuis l'index, filtrés par préférences si disponibles.
     */
    private fun getCandidates(preferences: UserPreferences?): List<ExtractCandidate> {
        if (corpusIndex == null) return emptyList()

        val domainIds = preferences?.preferredDomains
        return if (domainIds.isNullOrEmpty()) {
            corpusIndex.getAllCandidates(limit = 50)
        } else {
            corpusIndex.getCandidates(domainIds, limit = 50)
        }
    }

    /**
     * Construit le cache UI à partir des candidats.
     */
    private fun buildExtractCache(candidates: List<ExtractCandidate>) {
        for (candidate in candidates) {
            if (candidate.id !in seenExtractIds) {
                seenExtractIds.add(candidate.id)
                extractCache.add(candidate.toUiModel())
            }
        }
    }

    /**
     * Charge plus de candidats (quand le cache est épuisé).
     */
    private fun loadMoreCandidates(): List<ExtractCandidate> {
        if (corpusIndex == null) return emptyList()
        return corpusIndex.getAllCandidates(limit = 20)
            .filter { it.id !in seenExtractIds }
    }

    /**
     * Affiche l'extrait à l'index donné avec animation.
     */
    private fun showExtract(index: Int) {
        if (index < 0 || index >= extractCache.size) return

        val extract = extractCache[index]
        val wordStates = buildWordStates(extract)

        _uiState.update {
            ExploreUiState.ExtractDisplayed(
                extract = extract,
                wordStates = wordStates,
                interestRating = null,
                isTransitioning = true,
                historyIndex = index,
                historySize = extractCache.size
            )
        }

        // Fin de l'animation de transition
        viewModelScope.launch {
            delay(300)
            _uiState.update { state ->
                if (state is ExploreUiState.ExtractDisplayed) {
                    state.copy(isTransitioning = false)
                } else state
            }
        }
    }

    /**
     * Construit la map des statuts de mots pour un extrait.
     * Les mots suggérés sont en SUGGESTED, les autres en NORMAL.
     */
    private fun buildWordStates(extract: ExtractUiModel): Map<String, WordStatus> {
        val states = mutableMapOf<String, WordStatus>()
        val words = extract.content
            .split(Regex("[\\s,;:.!?«»()\\[\\]\"'\\-]+"))
            .filter { it.isNotBlank() }

        for (word in words) {
            val cleanWord = word.lowercase()
            if (cleanWord in extract.highlightedWords.map { it.lowercase() }) {
                states[word] = WordStatus.SUGGESTED
            } else {
                states[word] = WordStatus.NORMAL
            }
        }
        return states
    }

    /**
     * Détecte si l'utilisateur swipe trop rapidement.
     * Limite : 3 swipes en 5 secondes.
     */
    private fun isSwipingTooFast(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastSwipeTime < 1666) { // moins de 1.666s entre deux swipes
            rapidSwipeCount++
        } else {
            rapidSwipeCount = 1
        }
        lastSwipeTime = now
        return rapidSwipeCount > 3
    }
}

// =============================================================================
// EXTENSION : ExtractCandidate → ExtractUiModel
// =============================================================================

/**
 * Convertit un [ExtractCandidate] (modèle data) en [ExtractUiModel] (modèle UI).
 */
fun ExtractCandidate.toUiModel(): ExtractUiModel = ExtractUiModel(
    id = id,
    content = content,
    highlightedWords = suggestedWords,
    sourceTitle = sourceId, // sera enrichi avec le titre réel via CorpusSource
    sourceAuthor = "",
    sourceYear = null,
    sourceUrl = null,
    contentType = ExtractContentType.TEXT,
    videoUrl = null,
    domainTags = domainTags,
    difficulty = difficulty
)
