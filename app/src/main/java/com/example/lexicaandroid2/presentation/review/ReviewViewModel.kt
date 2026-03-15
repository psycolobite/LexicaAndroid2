package com.example.lexicaandroid2.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.logic.Sm2Algorithm
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.Sm2Stats
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatHelper
import com.example.lexicaandroid2.presentation.admin.AdminPrefsRepository
import com.example.lexicaandroid2.presentation.admin.ReviewMode
import com.example.lexicaandroid2.presentation.review.challenge.ChallengeType
import com.example.lexicaandroid2.presentation.review.challenge.ValidationResult
import com.example.lexicaandroid2.presentation.review.challenge.SpellingValidator
import com.example.lexicaandroid2.presentation.review.challenge.JaccardSemanticValidator
import com.example.lexicaandroid2.presentation.review.challenge.SemanticValidator
import com.example.lexicaandroid2.presentation.review.challenge.SemanticValidatorFactory
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class ReviewViewModel(
    private val repository: FlashcardRepository,
    private val sm2Algorithm: Sm2Algorithm = Sm2Algorithm,
    private val dailyStatDao: DailyReviewStatDao? = null,
    private val context: android.content.Context? = null,
    private val adminPrefsRepository: AdminPrefsRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _snackbarEvents = Channel<String>(Channel.CONFLATED)
    val snackbarEvents = _snackbarEvents.receiveAsFlow()

    private var pending: ArrayDeque<Flashcard> = ArrayDeque()
    private var currentFaceIsMotVersDef = true  // true = showing recto (mot), false = showing verso (définition)
    private val spellingValidator = SpellingValidator()
    
    // Lazy initialization des validateurs sémantiques
    private val semanticValidator: SemanticValidator by lazy {
        if (context != null) {
            SemanticValidatorFactory.createSemanticValidator(context)
        } else {
            JaccardSemanticValidator()  // Fallback si context absent
        }
    }
    
    fun loadSession(limit: Int = DEFAULT_SESSION_SIZE) {
        // Lire la taille de session depuis les prefs admin si disponible
        val effectiveLimit = adminPrefsRepository?.sessionSize ?: limit
        _uiState.update { it.copy(isLoading = true, isSessionFinished = false) }
        viewModelScope.launch {
            val cards = repository.getCardsToReview(effectiveLimit)
            pending = ArrayDeque(cards)
            val nextCard = pending.removeFirstOrNull()
            currentFaceIsMotVersDef = true  // Reset to showing mot (recto) at start
            _uiState.value = ReviewUiState(
                currentCard = nextCard,
                isAnswerRevealed = false,
                scrum = pending.size + if (nextCard == null) 0 else 1,
                isLoading = false,
                isSessionFinished = nextCard == null,
                studiedCount = 0,
                totalInSession = cards.size
            )
        }
    }

    fun revealAnswer() {
        _uiState.update { it.copy(isAnswerRevealed = true) }
    }

    fun gradeCard(quality: Int) {
        val current = _uiState.value.currentCard ?: return
        viewModelScope.launch {
            // Déterminer quelle face a été révisée (avant de toggler)
            val wasMotVersDef = currentFaceIsMotVersDef
            
            // Mettre à jour la face qui vient d'être révisée
            val updatedStats = if (wasMotVersDef) {
                updateSm2(current.sm2MotVersDef, quality)
            } else {
                updateSm2(current.sm2DefVersMot, quality)
            }

            // Sauvegarder le progrès
            repository.updateCardProgress(
                cardId = current.id,
                motVersDef = if (wasMotVersDef) updatedStats else current.sm2MotVersDef,
                defVersMot = if (wasMotVersDef) current.sm2DefVersMot else updatedStats
            )

            // Incrémenter les stats quotidiennes
            dailyStatDao?.let { dao ->
                DailyReviewStatHelper.recordReview(dao, isCorrect = quality >= 3)
            }

            _snackbarEvents.trySend("Revue dans ${updatedStats.interval} jours")

            // Vérifier si un défi doit être déclenché (seulement si quality >= 3)
            if (quality >= 3) {
                val correctReviews = updatedStats.correctReviews
                if (correctReviews > 0 && correctReviews % 3 == 0) {
                    // Vérifier les préférences admin pour les défis
                    val orthoEnabled = adminPrefsRepository?.challengeOrthoEnabled ?: true
                    val semanticEnabled = adminPrefsRepository?.challengeSemanticEnabled ?: true

                    val challengeType = if (wasMotVersDef && orthoEnabled) {
                        ChallengeType.SPELLING
                    } else if (!wasMotVersDef && semanticEnabled) {
                        ChallengeType.SEMANTIC
                    } else null

                    if (challengeType != null) {
                        _uiState.update {
                            it.copy(
                                activeChallengeType = challengeType,
                                challengeInput = "",
                                challengeResult = null,
                                currentCard = current.copy(
                                    sm2MotVersDef = if (wasMotVersDef) updatedStats else current.sm2MotVersDef,
                                    sm2DefVersMot = if (wasMotVersDef) current.sm2DefVersMot else updatedStats
                                )
                            )
                        }
                        return@launch  // Attendre la validation du défi
                    }
                }
            }

            // Pas de défi, avancer à la prochaine carte
            advanceToNextCard(quality)
        }
    }

    fun onChallengeInputChanged(text: String) {
        _uiState.update { it.copy(challengeInput = text) }
    }

    fun validateChallenge() {
        val current = _uiState.value.currentCard ?: return
        val challengeType = _uiState.value.activeChallengeType ?: return
        val input = _uiState.value.challengeInput

        val validator = when (challengeType) {
            ChallengeType.SPELLING -> spellingValidator
            ChallengeType.SEMANTIC -> semanticValidator
        }

        val expectedAnswer = when (challengeType) {
            ChallengeType.SPELLING -> current.recto  // Doit taper le mot
            ChallengeType.SEMANTIC -> current.verso   // Doit taper la définition
        }

        val result = validator.validate(input, expectedAnswer)
        
        _uiState.update {
            it.copy(
                challengeResult = result,
                // Accorder les XP bonus au ViewModel parent (via onAwardXp callback)
            )
        }
    }

    fun dismissChallenge() {
        // Récupérer les XP bonus avant de passer à la suite
        val xpBonus = _uiState.value.challengeResult?.xpBonus ?: 0
        
        // Avancer à la prochaine carte (sans pénalité SM2, le défi n'affecte pas le score)
        _uiState.update {
            it.copy(
                activeChallengeType = null,
                challengeInput = "",
                challengeResult = null
            )
        }
        
        viewModelScope.launch {
            advanceToNextCard(qualityUsedForXp = 4, xpBonusFromChallenge = xpBonus)
        }
    }

    private suspend fun advanceToNextCard(qualityUsedForXp: Int = 0, xpBonusFromChallenge: Int = 0) {
        currentFaceIsMotVersDef = when (adminPrefsRepository?.reviewMode ?: ReviewMode.BOTH) {
            ReviewMode.VOCAB       -> true
            ReviewMode.DEFINITION  -> false
            ReviewMode.BOTH        -> !currentFaceIsMotVersDef
        }
        
        val nextCard = pending.removeFirstOrNull()
        val studied = _uiState.value.studiedCount + 1
        _uiState.value = ReviewUiState(
            currentCard = nextCard,
            isAnswerRevealed = false,
            scrum = pending.size + if (nextCard == null) 0 else 1,
            isLoading = false,
            isSessionFinished = nextCard == null,
            studiedCount = studied,
            totalInSession = _uiState.value.totalInSession,
            xpBonusAccumulated = _uiState.value.xpBonusAccumulated + xpBonusFromChallenge
        )
    }

    private fun updateSm2(stats: Sm2Stats, quality: Int): Sm2Stats {
        val now = Instant.now()
        val result = sm2Algorithm.calculate(
            Sm2Algorithm.Sm2State(
                interval = stats.interval,
                repetitions = stats.repetitions,
                easeFactor = stats.easeFactor
            ),
            quality = quality,
            now = now
        )
        return stats.copy(
            interval = result.interval,
            repetitions = result.repetitions,
            easeFactor = result.easeFactor,
            nextReviewDate = result.nextReviewDate.toEpochMilli(),
            lastReviewDate = now.toEpochMilli(),
            totalReviews = stats.totalReviews + 1,
            correctReviews = stats.correctReviews + if (quality >= 3) 1 else 0,
            lapses = stats.lapses + if (quality < 3) 1 else 0
        )
    }

    fun toggleFavorite() {
        val current = _uiState.value.currentCard ?: return
        val updated = current.copy(favori = !current.favori)
        _uiState.update { it.copy(currentCard = updated) }
        viewModelScope.launch {
            repository.setFavorite(current.id, updated.favori)
        }
    }

    fun deleteCurrentCard() {
        val current = _uiState.value.currentCard ?: return
        viewModelScope.launch {
            repository.deleteCard(current.id)
            val nextCard = pending.removeFirstOrNull()
            val totalLeft = (_uiState.value.totalInSession - 1).coerceAtLeast(0)
            _uiState.value = ReviewUiState(
                currentCard = nextCard,
                isAnswerRevealed = false,
                scrum = pending.size + if (nextCard == null) 0 else 1,
                isLoading = false,
                isSessionFinished = nextCard == null,
                studiedCount = _uiState.value.studiedCount,
                totalInSession = totalLeft
            )
        }
    }

    companion object {
        private const val DEFAULT_SESSION_SIZE = 20
    }
}

data class ReviewUiState(
    val currentCard: Flashcard? = null,
    val isAnswerRevealed: Boolean = false,
    val scrum: Int = 0,
    val isLoading: Boolean = false,
    val isSessionFinished: Boolean = false,
    val studiedCount: Int = 0,
    val totalInSession: Int = 0,
    val activeChallengeType: ChallengeType? = null,
    val challengeInput: String = "",
    val challengeResult: ValidationResult? = null,
    val xpBonusAccumulated: Int = 0
)

class ReviewViewModelFactory(
    private val repository: FlashcardRepository,
    private val sm2Algorithm: Sm2Algorithm = Sm2Algorithm,
    private val dailyStatDao: DailyReviewStatDao? = null,
    private val context: android.content.Context? = null,
    private val adminPrefsRepository: AdminPrefsRepository? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(repository, sm2Algorithm, dailyStatDao, context, adminPrefsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
