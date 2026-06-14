package com.example.lexicaandroid2.presentation.search.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.data.corpus.CorpusIndex
import com.example.lexicaandroid2.data.corpus.CorpusSource
import com.example.lexicaandroid2.data.corpus.CorpusSources
import com.example.lexicaandroid2.data.corpus.ExtractCandidate
import com.example.lexicaandroid2.presentation.search.preferences.UserPreferences
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.data.remote.DictionaryService
import com.example.lexicaandroid2.data.remote.DictionaryServiceImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// =============================================================================
// MODES DU HUB DE RECHERCHE
// =============================================================================
enum class ExploreHubMode {
    TEXT_EXTRACTS,
    VIDEO_EXTRACTS,
    LIBRARY,
    DICTIONARY
}

// =============================================================================
// ÉTATS DE L'ÉCRAN (cf. ExploreScreenSpec.kt §2)
// =============================================================================

/**
 * État global de l'écran Explore.
 */
sealed class ExploreUiState {
    abstract val activeMode: ExploreHubMode
    abstract val selectedBook: CorpusSource?
    abstract val currentBookPage: Int
    abstract val bookPages: List<String>

    data class Loading(
        override val activeMode: ExploreHubMode = ExploreHubMode.TEXT_EXTRACTS,
        override val selectedBook: CorpusSource? = null,
        override val currentBookPage: Int = 0,
        override val bookPages: List<String> = emptyList()
    ) : ExploreUiState()

    data class ExtractDisplayed(
        val extract: ExtractUiModel,
        val wordStates: Map<String, WordStatus>,
        val interestRating: Int?,
        val isTransitioning: Boolean,
        val historyIndex: Int,
        val historySize: Int,
        override val activeMode: ExploreHubMode = ExploreHubMode.TEXT_EXTRACTS,
        
        // États de simulation vidéo
        val isVideoPlaying: Boolean = false,
        val videoProgress: Float = 0f,
        val currentVideoTime: String = "0:00",
        val totalVideoTime: String = "2:15",
        val videoExtracts: List<ExtractUiModel> = emptyList(),
        val currentVideoIndex: Int = 0,
        
        // États de la bibliothèque
        override val selectedBook: CorpusSource? = null,
        override val currentBookPage: Int = 0,
        override val bookPages: List<String> = emptyList()
    ) : ExploreUiState()

    data class NoExtractAvailable(
        val reason: NoExtractReason,
        val suggestion: String?,
        override val activeMode: ExploreHubMode = ExploreHubMode.TEXT_EXTRACTS,
        override val selectedBook: CorpusSource? = null,
        override val currentBookPage: Int = 0,
        override val bookPages: List<String> = emptyList()
    ) : ExploreUiState()

    data class Error(
        val message: String,
        val isRetryable: Boolean,
        override val activeMode: ExploreHubMode = ExploreHubMode.TEXT_EXTRACTS,
        override val selectedBook: CorpusSource? = null,
        override val currentBookPage: Int = 0,
        override val bookPages: List<String> = emptyList()
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
 * ViewModel de l'écran Explore (devenu Hub de Recherche & Découverte).
 */
class ExploreViewModel(
    private val corpusIndex: CorpusIndex? = null,
    private val flashcardRepository: FlashcardRepository? = null,
    private val scoringEngine: Any? = null,
    private val rankingStrategy: Any? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val dictionaryService: DictionaryService = DictionaryServiceImpl()

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

    // États du mot sélectionné dans la bibliothèque
    val selectedLibraryWord = MutableStateFlow<String?>(null)
    val libraryWordDefinition = MutableStateFlow<String?>(null)
    val libraryWordCategory = MutableStateFlow<String?>(null)
    val libraryWordExemples = MutableStateFlow<List<String>>(emptyList())
    val libraryWordSynonymes = MutableStateFlow<List<String>>(emptyList())
    val libraryWordIsAdded = MutableStateFlow(false)

    // Job vidéo pour simulation
    private var videoJob: Job? = null

    // Extraits vidéo maquettés (INA / Conférences)
    private val videoExtractsList = listOf(
        ExtractUiModel(
            id = "video-ina-1",
            content = "Je pense que la liberté est l'essence même de l'homme, sa marque indélébile.",
            highlightedWords = listOf("liberté", "essence", "indélébile"),
            sourceTitle = "Entretien avec Jean-Paul Sartre (1968)",
            sourceAuthor = "Archives INA",
            sourceYear = "1968",
            sourceUrl = "https://www.youtube.com/watch?v=sartre-1968",
            contentType = ExtractContentType.VIDEO,
            videoUrl = "https://www.youtube.com/watch?v=sartre-1968",
            domainTags = listOf("philosophie"),
            difficulty = "avancé"
        ),
        ExtractUiModel(
            id = "video-ina-2",
            content = "La structure de cette méthode scientifique requiert une rigueur absolue.",
            highlightedWords = listOf("structure", "méthode", "rigueur", "absolue"),
            sourceTitle = "Conférence sur la méthode cartésienne",
            sourceAuthor = "Collège de France",
            sourceYear = "1975",
            sourceUrl = "https://www.youtube.com/watch?v=descartes-conf",
            contentType = ExtractContentType.VIDEO,
            videoUrl = "https://www.youtube.com/watch?v=descartes-conf",
            domainTags = listOf("philosophie", "sciences_humaines"),
            difficulty = "avancé"
        ),
        ExtractUiModel(
            id = "video-ina-3",
            content = "L'éloquence de sa rhétorique subjugue littéralement l'auditoire.",
            highlightedWords = listOf("éloquence", "rhétorique", "subjugue", "auditoire"),
            sourceTitle = "Débat littéraire sur le classicisme",
            sourceAuthor = "ORTF / INA",
            sourceYear = "1964",
            sourceUrl = "https://www.youtube.com/watch?v=deb-class",
            contentType = ExtractContentType.VIDEO,
            videoUrl = "https://www.youtube.com/watch?v=deb-class",
            domainTags = listOf("litterature_classique"),
            difficulty = "moyen"
        )
    )

    // Contenus enrichis pour le mode Bibliothèque
    private val bookContents = mapOf(
        "baudelaire-fleurs-du-mal" to listOf(
            "Sois sage, ô ma Douleur, et tiens-toi plus tranquille. Tu réclamais le Soir ; il descend ; le voici : Un atmosphère obscure enveloppe la ville, Aux uns portant la paix, aux autres le souci.",
            "Pendant que des mortels la multitude vile, Sous le fouet du Plaisir, ce bourreau sans merci, Va cueillir des remords dans la fête servile, Ma Douleur, donne-moi la main ; viens par ici.",
            "Vois se pencher les défuntes Années, Sur les balcons du ciel, en robes surannées ; Surgir du fond des eaux le Regret souriant ; Le Soleil moribond s'endormir sous une arche, Et, comme un long linceul traînant à l'Orient, Entends, ma chère, entends la douce Nuit qui marche."
        ),
        "descartes-discours" to listOf(
            "Le bon sens est la chose du monde la mieux partagée : car chacun pense en être si bien pourvu, que ceux même qui sont les plus difficiles à contenter en toute autre chose n'ont point coutume d'en désirer plus qu'ils en ont.",
            "En quoi il n'est pas vraisemblable que tous se trompent ; mais plutôt cela témoigne que la puissance de bien juger et distinguer le vrai d'avec le faux, qui est proprement ce qu'on nomme le bon sens ou la raison, est naturellement égale en tous les hommes.",
            "Et ainsi que la diversité de nos opinions ne vient pas de ce que les uns sont plus raisonnables que les autres, mais seulement de ce que nous conduisons nos pensées par diverses voies, et ne considérons pas les mêmes choses.",
            "Car ce n'est pas assez d'avoir l'esprit bon, mais le principal est de l'appliquer bien. Les plus grandes âmes sont capables des plus grands vices aussi bien que des plus grandes vertus ; et ceux qui ne marchent que fort lentement peuvent avancer beaucoup davantage, s'ils suivent toujours le droit chemin, que ne font ceux qui courent et qui s'en éloignent."
        ),
        "moliere-misanthrope" to listOf(
            "Mes yeux sont trop blessés, et la cour et la ville Ne m'offrent rien de bon qui ne me mette en bile ; J'entre en une humeur noire, en un chagrin profond, Quand je vois vivre entre eux les hommes comme ils font ; Je ne trouve partout que lâche flatterie, Qu'injustice, intérêt, trahison, fourberie ; Je n'y puis plus tenir, j'enrage, et mon dessein Est de rompre en visière à tout le genre humain.",
            "Je veux qu'on soit sincère, et qu'en homme d'honneur, On ne lâche aucun mot qui ne parte du cœur ; Qu'on ne donne jamais de promesses frivoles, Et que dans nos transports nos serments soient des règles.",
            "C'est une folie à nulle autre seconde, De vouloir se mêler de corriger le monde. J'observe comme vous cent choses tous les jours, Qui pourraient mieux aller en prenant un autre cours ; Mais quoi qu'à chaque pas mon esprit envisage, On ne me voit point, moi, courir dans le paysage."
        )
    )

    // =========================================================================
    // CHARGEMENT INITIAL
    // =========================================================================

    fun loadInitialExtract(preferences: UserPreferences? = null) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val currentMode = _uiState.value.activeMode
            val selectedBook = _uiState.value.selectedBook
            val currentBookPage = _uiState.value.currentBookPage
            val bookPages = _uiState.value.bookPages

            _uiState.update { ExploreUiState.Loading(activeMode = currentMode, selectedBook = selectedBook, currentBookPage = currentBookPage, bookPages = bookPages) }
            try {
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
                                "Essayez d'élargir vos centres d'intérêt dans les paramètres.",
                            activeMode = currentMode,
                            selectedBook = selectedBook,
                            currentBookPage = currentBookPage,
                            bookPages = bookPages
                        )
                    }
                    return@launch
                }

                buildExtractCache(candidates)

                if (extractCache.isEmpty()) {
                    _uiState.update {
                        ExploreUiState.NoExtractAvailable(
                            reason = NoExtractReason.ALL_CONSUMED,
                            suggestion = "Vous avez vu tous les extraits disponibles. Revenez plus tard.",
                            activeMode = currentMode,
                            selectedBook = selectedBook,
                            currentBookPage = currentBookPage,
                            bookPages = bookPages
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
                        isRetryable = true,
                        activeMode = currentMode,
                        selectedBook = selectedBook,
                        currentBookPage = currentBookPage,
                        bookPages = bookPages
                    )
                }
            }
        }
    }

    // =========================================================================
    // HUB MODES SWITCHER
    // =========================================================================

    fun setHubMode(mode: ExploreHubMode) {
        _uiState.update { state ->
            when (state) {
                is ExploreUiState.Loading -> state.copy(activeMode = mode)
                is ExploreUiState.ExtractDisplayed -> state.copy(activeMode = mode)
                is ExploreUiState.NoExtractAvailable -> state.copy(activeMode = mode)
                is ExploreUiState.Error -> state.copy(activeMode = mode)
            }
        }

        if (mode == ExploreHubMode.VIDEO_EXTRACTS) {
            val state = _uiState.value
            if (state is ExploreUiState.ExtractDisplayed) {
                if (state.extract.contentType != ExtractContentType.VIDEO) {
                    showVideoExtract(0)
                }
            } else {
                showVideoExtract(0)
            }
        }
    }

    // =========================================================================
    // VIDEO EXTRACTS LOGIC (Simulated Player)
    // =========================================================================

    fun showVideoExtract(index: Int) {
        if (index < 0 || index >= videoExtractsList.size) return
        val extract = videoExtractsList[index]
        val wordStates = buildWordStates(extract)
        
        videoJob?.cancel()

        _uiState.update { state ->
            ExploreUiState.ExtractDisplayed(
                extract = extract,
                wordStates = wordStates,
                interestRating = null,
                isTransitioning = false,
                historyIndex = index,
                historySize = videoExtractsList.size,
                activeMode = ExploreHubMode.VIDEO_EXTRACTS,
                isVideoPlaying = false,
                videoProgress = 0f,
                currentVideoTime = "0:00",
                totalVideoTime = "2:15",
                videoExtracts = videoExtractsList,
                currentVideoIndex = index,
                selectedBook = state.selectedBook,
                currentBookPage = state.currentBookPage,
                bookPages = state.bookPages
            )
        }
    }

    fun toggleVideoPlay() {
        val state = _uiState.value as? ExploreUiState.ExtractDisplayed ?: return
        if (state.activeMode != ExploreHubMode.VIDEO_EXTRACTS) return
        
        if (state.isVideoPlaying) {
            videoJob?.cancel()
            _uiState.update { s ->
                if (s is ExploreUiState.ExtractDisplayed) {
                    s.copy(isVideoPlaying = false)
                } else s
            }
        } else {
            _uiState.update { s ->
                if (s is ExploreUiState.ExtractDisplayed) {
                    s.copy(isVideoPlaying = true)
                } else s
            }
            videoJob = viewModelScope.launch {
                var progress = state.videoProgress
                val totalSeconds = 135 // 2:15
                while (progress < 1.0f) {
                    delay(200)
                    progress += 0.015f
                    if (progress > 1.0f) progress = 1.0f
                    val currentSeconds = (progress * totalSeconds).toInt()
                    val minutes = currentSeconds / 60
                    val seconds = currentSeconds % 60
                    val timeStr = String.format("%d:%02d", minutes, seconds)
                    _uiState.update { s ->
                        if (s is ExploreUiState.ExtractDisplayed) {
                            s.copy(
                                videoProgress = progress,
                                currentVideoTime = timeStr
                            )
                        } else s
                    }
                }
                _uiState.update { s ->
                    if (s is ExploreUiState.ExtractDisplayed) {
                        s.copy(isVideoPlaying = false, videoProgress = 0f, currentVideoTime = "0:00")
                    } else s
                }
            }
        }
    }

    fun seekVideo(progress: Float) {
        _uiState.update { s ->
            if (s is ExploreUiState.ExtractDisplayed) {
                val totalSeconds = 135
                val currentSeconds = (progress * totalSeconds).toInt()
                val minutes = currentSeconds / 60
                val seconds = currentSeconds % 60
                val timeStr = String.format("%d:%02d", minutes, seconds)
                s.copy(videoProgress = progress, currentVideoTime = timeStr)
            } else s
        }
    }

    fun navigateToNextVideo() {
        val state = _uiState.value as? ExploreUiState.ExtractDisplayed ?: return
        val nextIdx = (state.currentVideoIndex + 1) % videoExtractsList.size
        showVideoExtract(nextIdx)
    }

    fun navigateToPreviousVideo() {
        val state = _uiState.value as? ExploreUiState.ExtractDisplayed ?: return
        val prevIdx = if (state.currentVideoIndex - 1 < 0) videoExtractsList.size - 1 else state.currentVideoIndex - 1
        showVideoExtract(prevIdx)
    }

    // =========================================================================
    // LIBRARY READER LOGIC
    // =========================================================================

    fun selectBook(book: CorpusSource?) {
        val pages = if (book != null) bookContents[book.id] ?: emptyList() else emptyList()
        _uiState.update { state ->
            when (state) {
                is ExploreUiState.Loading -> state.copy(selectedBook = book, currentBookPage = 0, bookPages = pages)
                is ExploreUiState.ExtractDisplayed -> state.copy(selectedBook = book, currentBookPage = 0, bookPages = pages)
                is ExploreUiState.NoExtractAvailable -> state.copy(selectedBook = book, currentBookPage = 0, bookPages = pages)
                is ExploreUiState.Error -> state.copy(selectedBook = book, currentBookPage = 0, bookPages = pages)
            }
        }
    }

    fun nextBookPage() {
        _uiState.update { state ->
            val nextPage = (state.currentBookPage + 1).coerceAtMost(state.bookPages.size - 1)
            when (state) {
                is ExploreUiState.Loading -> state.copy(currentBookPage = nextPage)
                is ExploreUiState.ExtractDisplayed -> state.copy(currentBookPage = nextPage)
                is ExploreUiState.NoExtractAvailable -> state.copy(currentBookPage = nextPage)
                is ExploreUiState.Error -> state.copy(currentBookPage = nextPage)
            }
        }
    }

    fun previousBookPage() {
        _uiState.update { state ->
            val prevPage = (state.currentBookPage - 1).coerceAtLeast(0)
            when (state) {
                is ExploreUiState.Loading -> state.copy(currentBookPage = prevPage)
                is ExploreUiState.ExtractDisplayed -> state.copy(currentBookPage = prevPage)
                is ExploreUiState.NoExtractAvailable -> state.copy(currentBookPage = prevPage)
                is ExploreUiState.Error -> state.copy(currentBookPage = prevPage)
            }
        }
    }

    fun lookupLibraryWord(word: String) {
        val cleanWord = word.trim().replace(Regex("[.,;:!?()\"'«»]"), "")
        if (cleanWord.isBlank()) return
        
        selectedLibraryWord.value = cleanWord
        libraryWordDefinition.value = "Recherche de la définition..."
        libraryWordCategory.value = ""
        libraryWordExemples.value = emptyList()
        libraryWordSynonymes.value = emptyList()
        libraryWordIsAdded.value = false
        
        viewModelScope.launch {
            if (flashcardRepository != null) {
                val existing = flashcardRepository.getAllCards().firstOrNull {
                    it.recto.equals(cleanWord, ignoreCase = true)
                }
                if (existing != null) {
                    libraryWordDefinition.value = existing.verso
                    libraryWordCategory.value = existing.categorieGrammaticale
                    libraryWordExemples.value = existing.exemples
                    libraryWordSynonymes.value = existing.synonymes
                    libraryWordIsAdded.value = true
                    return@launch
                }
            }

            try {
                val results = dictionaryService.searchWord(cleanWord)
                if (results.isNotEmpty()) {
                    val result = results.first()
                    libraryWordDefinition.value = result.definition
                    libraryWordCategory.value = result.categorieGrammaticale
                    libraryWordExemples.value = result.exemples
                    libraryWordSynonymes.value = result.synonymes
                } else {
                    libraryWordDefinition.value = "Aucune définition trouvée pour ce mot. Vous pouvez l'ajouter avec une définition personnalisée."
                }
            } catch (e: Exception) {
                libraryWordDefinition.value = "Erreur de connexion. Vous pouvez tout de même ajouter ce mot manuellement."
            }
        }
    }

    fun saveLibraryWord(definition: String) {
        val word = selectedLibraryWord.value ?: return
        if (flashcardRepository == null) return
        
        viewModelScope.launch {
            val card = Flashcard(
                id = UUID.randomUUID().toString(),
                recto = word,
                verso = definition.trim().ifBlank { "Définition à compléter" },
                categorieGrammaticale = libraryWordCategory.value ?: "",
                exemples = libraryWordExemples.value,
                synonymes = libraryWordSynonymes.value
            )
            flashcardRepository.saveCard(card)
            libraryWordIsAdded.value = true
            
            // Re-sync local states to update color codes of words
            refreshLocalWordStates()
        }
    }

    fun deleteLibraryWord() {
        val word = selectedLibraryWord.value ?: return
        if (flashcardRepository == null) return
        
        viewModelScope.launch {
            val existing = flashcardRepository.getAllCards().firstOrNull {
                it.recto.equals(word, ignoreCase = true)
            }
            if (existing != null) {
                flashcardRepository.deleteCard(existing.id)
                libraryWordIsAdded.value = false
                
                refreshLocalWordStates()
            }
        }
    }

    fun closeLibraryWordDialog() {
        selectedLibraryWord.value = null
    }

    private fun refreshLocalWordStates() {
        val state = _uiState.value
        if (state is ExploreUiState.ExtractDisplayed) {
            val updatedStates = buildWordStates(state.extract)
            _uiState.update { state.copy(wordStates = updatedStates) }
        }
    }

    // =========================================================================
    // NAVIGATION ENTRE EXTRAITS TEXTUELS
    // =========================================================================

    fun navigateToNext() {
        val state = _uiState.value
        if (state !is ExploreUiState.ExtractDisplayed) return
        if (state.isTransitioning) return

        if (isSwipingTooFast()) return

        if (currentIndex < extractCache.size - 1) {
            currentIndex++
            showExtract(currentIndex)
        } else {
            viewModelScope.launch {
                val moreCandidates = loadMoreCandidates()
                if (moreCandidates.isEmpty()) {
                    _uiState.update {
                        ExploreUiState.NoExtractAvailable(
                            reason = NoExtractReason.ALL_CONSUMED,
                            suggestion = "Vous avez vu tous les extraits disponibles.",
                            activeMode = state.activeMode,
                            selectedBook = state.selectedBook,
                            currentBookPage = state.currentBookPage,
                            bookPages = state.bookPages
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
    // INTERACTIONS SUR LES MOTS D'EXTRAIT
    // =========================================================================

    fun toggleWord(word: String) {
        _uiState.update { state ->
            if (state !is ExploreUiState.ExtractDisplayed) return@update state

            val currentStatus = state.wordStates[word] ?: WordStatus.NORMAL
            val newStatus = when (currentStatus) {
                WordStatus.SUGGESTED -> WordStatus.ADDED
                WordStatus.ADDED -> WordStatus.SUGGESTED
                else -> return@update state
            }

            // Gérer l'ajout/retrait réel dans la base de données
            if (flashcardRepository != null) {
                viewModelScope.launch {
                    val cleanWord = word.lowercase().trim().replace(Regex("[.,;:!?()\"'«»]"), "")
                    if (newStatus == WordStatus.ADDED) {
                        // Ajouter le mot
                        val existing = flashcardRepository.getAllCards().firstOrNull { it.recto.equals(cleanWord, ignoreCase = true) }
                        if (existing == null) {
                            var def = "Définition à compléter"
                            var cat = ""
                            var syn = emptyList<String>()
                            var ex = emptyList<String>()
                            try {
                                val defs = dictionaryService.searchWord(cleanWord)
                                if (defs.isNotEmpty()) {
                                    def = defs.first().definition
                                    cat = defs.first().categorieGrammaticale
                                    syn = defs.first().synonymes
                                    ex = defs.first().exemples
                                }
                            } catch (_: Exception) {}

                            flashcardRepository.saveCard(
                                Flashcard(
                                    id = UUID.randomUUID().toString(),
                                    recto = cleanWord,
                                    verso = def,
                                    categorieGrammaticale = cat,
                                    synonymes = syn,
                                    exemples = ex
                                )
                            )
                        }
                    } else {
                        // Retirer le mot
                        val existing = flashcardRepository.getAllCards().firstOrNull { it.recto.equals(cleanWord, ignoreCase = true) }
                        if (existing != null) {
                            flashcardRepository.deleteCard(existing.id)
                        }
                    }
                }
            }

            val transitioningWords = state.wordStates.toMutableMap().apply {
                put(word, WordStatus.TRANSITIONING)
            }
            val transitionState = state.copy(wordStates = transitioningWords, isTransitioning = true)

            viewModelScope.launch {
                delay(200)
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

    fun showDefinition(word: String) {
        viewModelScope.launch {
            _definitionEvent.emit(DefinitionEvent.ShowDefinition(word))
        }
    }

    // =========================================================================
    // NOTATION
    // =========================================================================

    fun rateExtract(rating: Int) {
        val clampedRating = rating.coerceIn(1, 5)
        _uiState.update { state ->
            if (state !is ExploreUiState.ExtractDisplayed) return@update state
            state.copy(interestRating = clampedRating)
        }
    }

    // =========================================================================
    // RETRY / EVENT CONSUMPTION
    // =========================================================================

    fun retry() {
        loadInitialExtract()
    }

    private val _definitionEvent = MutableStateFlow<DefinitionEvent?>(null)
    val definitionEvent: StateFlow<DefinitionEvent?> = _definitionEvent.asStateFlow()

    sealed class DefinitionEvent {
        data class ShowDefinition(val word: String) : DefinitionEvent()
    }

    fun consumeDefinitionEvent() {
        _definitionEvent.update { null }
    }

    // =========================================================================
    // MÉTHODES PRIVÉES
    // =========================================================================

    private fun getCandidates(preferences: UserPreferences?): List<ExtractCandidate> {
        if (corpusIndex == null) return emptyList()

        val domainIds = preferences?.preferredDomains
        return if (domainIds.isNullOrEmpty()) {
            corpusIndex.getAllCandidates(limit = 50)
        } else {
            corpusIndex.getCandidates(domainIds, limit = 50)
        }
    }

    private fun buildExtractCache(candidates: List<ExtractCandidate>) {
        for (candidate in candidates) {
            if (candidate.id !in seenExtractIds) {
                seenExtractIds.add(candidate.id)
                extractCache.add(candidate.toUiModel())
            }
        }
    }

    private fun loadMoreCandidates(): List<ExtractCandidate> {
        if (corpusIndex == null) return emptyList()
        return corpusIndex.getAllCandidates(limit = 20)
            .filter { it.id !in seenExtractIds }
    }

    private fun showExtract(index: Int) {
        if (index < 0 || index >= extractCache.size) return

        val extract = extractCache[index]
        val wordStates = buildWordStates(extract)

        _uiState.update { state ->
            ExploreUiState.ExtractDisplayed(
                extract = extract,
                wordStates = wordStates,
                interestRating = null,
                isTransitioning = true,
                historyIndex = index,
                historySize = extractCache.size,
                activeMode = state.activeMode,
                selectedBook = state.selectedBook,
                currentBookPage = state.currentBookPage,
                bookPages = state.bookPages
            )
        }

        viewModelScope.launch {
            delay(300)
            _uiState.update { state ->
                if (state is ExploreUiState.ExtractDisplayed) {
                    state.copy(isTransitioning = false)
                } else state
            }
        }
    }

    private fun buildWordStates(extract: ExtractUiModel): Map<String, WordStatus> {
        val states = mutableMapOf<String, WordStatus>()
        val words = extract.content
            .split(Regex("[\\s,;:.!?«»()\\[\\]\"'\\-]+"))
            .filter { it.isNotBlank() }

        // Récupérer la liste des mots déjà enregistrés pour colorer correctement
        val existingWords = mutableSetOf<String>()
        if (flashcardRepository != null) {
            viewModelScope.launch {
                runCatching {
                    flashcardRepository.getAllCards().mapTo(existingWords) { it.recto.lowercase().trim() }
                }
            }
        }

        for (word in words) {
            val cleanWord = word.lowercase().trim().replace(Regex("[.,;:!?()\"'«»]"), "")
            if (cleanWord in existingWords) {
                states[word] = WordStatus.ADDED
            } else if (cleanWord in extract.highlightedWords.map { it.lowercase().trim() }) {
                states[word] = WordStatus.SUGGESTED
            } else {
                states[word] = WordStatus.NORMAL
            }
        }
        return states
    }

    private fun isSwipingTooFast(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastSwipeTime < 1666) {
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

fun ExtractCandidate.toUiModel(): ExtractUiModel = ExtractUiModel(
    id = id,
    content = content,
    highlightedWords = suggestedWords,
    sourceTitle = sourceId,
    sourceAuthor = "",
    sourceYear = null,
    sourceUrl = null,
    contentType = ExtractContentType.TEXT,
    videoUrl = null,
    domainTags = domainTags,
    difficulty = difficulty
)
