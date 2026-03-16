package com.example.lexicaandroid2.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.core.tts.LexicaTtsService
import com.example.lexicaandroid2.domain.logic.Sm2Algorithm
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.Sm2Stats
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatHelper
import com.example.lexicaandroid2.presentation.admin.AdminPrefsRepository
import com.example.lexicaandroid2.presentation.review.challenge.ChallengeType
import com.example.lexicaandroid2.presentation.review.challenge.JaccardSemanticValidator
import com.example.lexicaandroid2.presentation.review.challenge.SemanticValidator
import com.example.lexicaandroid2.presentation.review.challenge.SemanticValidatorFactory
import com.example.lexicaandroid2.presentation.review.challenge.SpellingValidator
import com.example.lexicaandroid2.presentation.review.challenge.ValidationResult
import java.time.Instant
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private enum class TrainingMode {
    WORD_TO_DEFINITION,
    DEFINITION_TO_WORD,
    SPELLING_CHALLENGE,
    SEMANTIC_CHALLENGE
}

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
    private var currentFaceIsMotVersDef = true
    private var trainingModeCursor = 0
    private val spellingValidator = SpellingValidator()
    private val ttsService = context?.applicationContext?.let { LexicaTtsService(it) }

    private val semanticValidator: SemanticValidator by lazy {
        if (context != null) {
            SemanticValidatorFactory.createSemanticValidator(context)
        } else {
            JaccardSemanticValidator()
        }
    }

    init {
        observeTtsState()
    }

    private fun observeTtsState() {
        val service = ttsService ?: return

        viewModelScope.launch {
            service.isReady.collect { ready ->
                _uiState.update { it.copy(ttsReady = ready) }
            }
        }

        viewModelScope.launch {
            service.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }
    }

    fun loadSession(limit: Int = DEFAULT_SESSION_SIZE) {
        stopSpeaking()
        val effectiveLimit = adminPrefsRepository?.sessionSize ?: limit
        _uiState.update { it.copy(isLoading = true, isSessionFinished = false) }
        viewModelScope.launch {
            val cards = repository.getCardsToReview(effectiveLimit)
            pending = ArrayDeque(cards)
            trainingModeCursor = 0
            val nextCard = pending.removeFirstOrNull()
            _uiState.value = if (nextCard == null) {
                buildSessionFinishedState(
                    studiedCount = 0,
                    totalInSession = 0,
                    xpBonusAccumulated = 0
                )
            } else {
                buildStateForCard(
                    nextCard = nextCard,
                    studiedCount = 0,
                    totalInSession = cards.size,
                    xpBonusAccumulated = 0
                )
            }
            maybeAutoSpeakVisibleContent()
        }
    }

    fun revealAnswer() {
        stopSpeaking()
        _uiState.update { it.copy(isAnswerRevealed = true) }
        maybeAutoSpeakVisibleContent()
    }

    fun toggleAnswerReveal() {
        stopSpeaking()
        _uiState.update { it.copy(isAnswerRevealed = !it.isAnswerRevealed) }
        maybeAutoSpeakVisibleContent()
    }

    fun speakCurrentFace() {
        val current = _uiState.value.currentCard ?: return
        val textToRead = if (_uiState.value.isAnswerRevealed) current.verso else current.recto
        ttsService?.speak(textToRead)
    }

    fun speakCurrentWord() {
        val current = _uiState.value.currentCard ?: return
        ttsService?.speak(current.recto)
    }

    fun speakCurrentDefinition() {
        val current = _uiState.value.currentCard ?: return
        ttsService?.speak(current.verso)
    }

    fun toggleAutoSpeakWord() {
        _uiState.update { it.copy(autoSpeakWord = !it.autoSpeakWord) }
        maybeAutoSpeakVisibleContent()
    }

    fun toggleAutoSpeakDefinition() {
        _uiState.update { it.copy(autoSpeakDefinition = !it.autoSpeakDefinition) }
        maybeAutoSpeakVisibleContent()
    }

    fun stopSpeaking() {
        ttsService?.stop()
    }

    private fun maybeAutoSpeakVisibleContent() {
        val state = _uiState.value
        val current = state.currentCard ?: return

        when {
            state.isAnswerRevealed && state.autoSpeakDefinition -> ttsService?.speak(current.verso)
            !state.isAnswerRevealed && state.autoSpeakWord -> ttsService?.speak(current.recto)
        }
    }

    fun gradeCard(quality: Int) {
        val current = _uiState.value.currentCard ?: return
        viewModelScope.launch {
            val wasMotVersDef = currentFaceIsMotVersDef
            val updatedStats = if (wasMotVersDef) {
                updateSm2(current.sm2MotVersDef, quality)
            } else {
                updateSm2(current.sm2DefVersMot, quality)
            }

            repository.updateCardProgress(
                cardId = current.id,
                motVersDef = if (wasMotVersDef) updatedStats else current.sm2MotVersDef,
                defVersMot = if (wasMotVersDef) current.sm2DefVersMot else updatedStats
            )

            dailyStatDao?.let { dao ->
                DailyReviewStatHelper.recordReview(dao, isCorrect = quality >= 3)
            }

            _snackbarEvents.trySend("Revue dans ${updatedStats.interval} jours")
            advanceToNextCard(xpBonusFromChallenge = 0)
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
            ChallengeType.SPELLING -> current.recto
            ChallengeType.SEMANTIC -> current.verso
        }

        val result = validator.validate(input, expectedAnswer)
        _uiState.update { it.copy(challengeResult = result) }
    }

    fun dismissChallenge() {
        val xpBonus = _uiState.value.challengeResult?.xpBonus ?: 0
        _uiState.update {
            it.copy(
                activeChallengeType = null,
                challengeInput = "",
                challengeResult = null
            )
        }

        viewModelScope.launch {
            advanceToNextCard(xpBonusFromChallenge = xpBonus)
        }
    }

    private suspend fun advanceToNextCard(xpBonusFromChallenge: Int = 0) {
        stopSpeaking()
        val nextCard = pending.removeFirstOrNull()
        val studied = _uiState.value.studiedCount + 1
        val xpBonusAccumulated = _uiState.value.xpBonusAccumulated + xpBonusFromChallenge
        _uiState.value = if (nextCard == null) {
            buildSessionFinishedState(
                studiedCount = studied,
                totalInSession = _uiState.value.totalInSession,
                xpBonusAccumulated = xpBonusAccumulated
            )
        } else {
            buildStateForCard(
                nextCard = nextCard,
                studiedCount = studied,
                totalInSession = _uiState.value.totalInSession,
                xpBonusAccumulated = xpBonusAccumulated
            )
        }
        maybeAutoSpeakVisibleContent()
    }

    private fun buildStateForCard(
        nextCard: Flashcard?,
        studiedCount: Int,
        totalInSession: Int,
        xpBonusAccumulated: Int
    ): ReviewUiState {
        if (nextCard == null) {
            return buildSessionFinishedState(
                studiedCount = studiedCount,
                totalInSession = totalInSession,
                xpBonusAccumulated = xpBonusAccumulated
            )
        }

        val mode = nextTrainingMode()
        val challengeType = when (mode) {
            TrainingMode.SPELLING_CHALLENGE -> ChallengeType.SPELLING
            TrainingMode.SEMANTIC_CHALLENGE -> ChallengeType.SEMANTIC
            else -> null
        }

        currentFaceIsMotVersDef = when (mode) {
            TrainingMode.WORD_TO_DEFINITION,
            TrainingMode.SPELLING_CHALLENGE -> true
            TrainingMode.DEFINITION_TO_WORD,
            TrainingMode.SEMANTIC_CHALLENGE -> false
        }

        return ReviewUiState(
            currentCard = nextCard,
            isAnswerRevealed = false,
            scrum = pending.size + 1,
            isLoading = false,
            isSpeaking = false,
            ttsReady = ttsService?.isReady?.value == true,
            isSessionFinished = false,
            studiedCount = studiedCount,
            totalInSession = totalInSession,
            autoSpeakWord = _uiState.value.autoSpeakWord,
            autoSpeakDefinition = _uiState.value.autoSpeakDefinition,
            activeChallengeType = challengeType,
            challengeInput = "",
            challengeResult = null,
            xpBonusAccumulated = xpBonusAccumulated
        )
    }

    private fun buildSessionFinishedState(
        studiedCount: Int,
        totalInSession: Int,
        xpBonusAccumulated: Int
    ): ReviewUiState {
        return ReviewUiState(
            currentCard = null,
            isAnswerRevealed = false,
            scrum = 0,
            isLoading = false,
            isSpeaking = false,
            ttsReady = ttsService?.isReady?.value == true,
            isSessionFinished = true,
            studiedCount = studiedCount,
            totalInSession = totalInSession,
            autoSpeakWord = _uiState.value.autoSpeakWord,
            autoSpeakDefinition = _uiState.value.autoSpeakDefinition,
            activeChallengeType = null,
            challengeInput = "",
            challengeResult = null,
            xpBonusAccumulated = xpBonusAccumulated
        )
    }

    private fun nextTrainingMode(): TrainingMode {
        val enabledModes = buildEnabledModes().ifEmpty {
            listOf(TrainingMode.WORD_TO_DEFINITION, TrainingMode.DEFINITION_TO_WORD)
        }
        val mode = enabledModes[trainingModeCursor % enabledModes.size]
        trainingModeCursor++
        return mode
    }

    private fun buildEnabledModes(): List<TrainingMode> {
        val prefs = adminPrefsRepository
        val modes = buildList {
            if (prefs?.reviewWordToDefinitionEnabled != false) add(TrainingMode.WORD_TO_DEFINITION)
            if (prefs?.reviewDefinitionToWordEnabled != false) add(TrainingMode.DEFINITION_TO_WORD)
            if (prefs?.challengeSemanticEnabled == true) add(TrainingMode.SEMANTIC_CHALLENGE)
            if (prefs?.challengeOrthoEnabled == true) add(TrainingMode.SPELLING_CHALLENGE)
        }
        return if (modes.isEmpty()) {
            listOf(TrainingMode.WORD_TO_DEFINITION, TrainingMode.DEFINITION_TO_WORD)
        } else {
            modes
        }
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
            stopSpeaking()
            repository.deleteCard(current.id)
            val nextCard = pending.removeFirstOrNull()
            val totalLeft = (_uiState.value.totalInSession - 1).coerceAtLeast(0)
            _uiState.value = buildStateForCard(
                nextCard = nextCard,
                studiedCount = _uiState.value.studiedCount,
                totalInSession = totalLeft,
                xpBonusAccumulated = _uiState.value.xpBonusAccumulated
            )
            maybeAutoSpeakVisibleContent()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsService?.shutdown()
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
    val isSpeaking: Boolean = false,
    val ttsReady: Boolean = false,
    val isSessionFinished: Boolean = false,
    val studiedCount: Int = 0,
    val totalInSession: Int = 0,
    val autoSpeakWord: Boolean = false,
    val autoSpeakDefinition: Boolean = false,
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
