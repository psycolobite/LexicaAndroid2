package com.example.lexicaandroid2.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.core.tts.LexicaTtsService
import com.example.lexicaandroid2.domain.logic.ReviewSessionEngine
import com.example.lexicaandroid2.domain.logic.ReviewSessionPlanner
import com.example.lexicaandroid2.domain.logic.ReviewIntervalEngine
import com.example.lexicaandroid2.domain.logic.Sm2Algorithm
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewAnswer
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.model.ReviewQuestionType
import com.example.lexicaandroid2.domain.model.ReviewSessionChallengeKind
import com.example.lexicaandroid2.domain.model.ReviewSessionEvent
import com.example.lexicaandroid2.domain.model.ReviewSessionEventType
import com.example.lexicaandroid2.domain.model.ReviewSessionPlan
import com.example.lexicaandroid2.domain.model.ReviewSessionQuestionState
import com.example.lexicaandroid2.domain.model.ReviewSessionSnapshot
import com.example.lexicaandroid2.domain.model.ReviewSessionSnapshotState
import com.example.lexicaandroid2.domain.model.ReviewSessionState
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.domain.repository.ReviewSessionSnapshotRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatHelper
import com.example.lexicaandroid2.presentation.admin.AdminPrefsRepository
import com.example.lexicaandroid2.presentation.settings.UserPrefsRepository
import com.example.lexicaandroid2.presentation.review.challenge.JaccardSemanticValidator
import com.example.lexicaandroid2.presentation.review.challenge.SemanticValidator
import com.example.lexicaandroid2.presentation.review.challenge.SemanticValidatorFactory
import com.example.lexicaandroid2.presentation.review.challenge.SpellingValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

@Suppress("UNUSED_PARAMETER")
class ReviewViewModel(
    private val repository: FlashcardRepository,
    private val sm2Algorithm: Sm2Algorithm = Sm2Algorithm,
    private val dailyStatDao: DailyReviewStatDao? = null,
    context: android.content.Context? = null,
    private val userPrefsRepository: UserPrefsRepository? = null,
    private val adminPrefsRepository: AdminPrefsRepository? = null,
    private val reviewSessionSnapshotRepository: ReviewSessionSnapshotRepository? = null,
    private val isAdminUserProvider: (() -> Boolean)? = null,
    private val onSessionXpAwarded: (Int) -> Unit = {},
    private val random: Random = Random.Default
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _snackbarEvents = Channel<String>(Channel.CONFLATED)
    val snackbarEvents = _snackbarEvents.receiveAsFlow()

    private var activeSessionCreatedAt: Long? = null
    private var currentUndoSnapshotState: ReviewSessionSnapshotState? = null
    private var sessionPlan: ReviewSessionPlan? = null
    private var sessionState: ReviewSessionState? = null
    private var sessionCardsById: Map<String, Flashcard> = emptyMap()
    private var allCardsCache: List<Flashcard> = emptyList()
    private var pendingSessionEvents: List<ReviewSessionEvent> = emptyList()
    private var activeSessionEvent: ReviewSessionEvent? = null
    private var eventInput: String = ""
    private var selectedChoice: String? = null
    private var matchingAssignments: Map<String, String> = emptyMap()
    private var matchingSelectedWordId: String? = null
    private var matchingSelectedDefinition: String? = null
    private var eventResultSuccessful: Boolean? = null
    private var eventResultMessage: String? = null
    private var eventResultCorrectAnswer: String? = null
    private var normalAnswersSinceLastMatching: Int = 0
    private var sessionSizeLimit: Int = DEFAULT_SESSION_SIZE
    private val appContext = context?.applicationContext

    private var ttsService: LexicaTtsService? = null
    private var isTtsObservationStarted: Boolean = false
    private val reviewSessionPlanner by lazy { ReviewSessionPlanner(repository, random) }
    private val spellingValidator = SpellingValidator()
    private val semanticValidator: SemanticValidator by lazy {
        if (context != null) {
            SemanticValidatorFactory.createSemanticValidator(context)
        } else {
            JaccardSemanticValidator()
        }
    }

    private fun ensureTtsServiceInitialized() {
        val service = ttsService ?: appContext?.let { LexicaTtsService(it) } ?: return
        ttsService = service
        if (isTtsObservationStarted) return
        isTtsObservationStarted = true

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

        viewModelScope.launch {
            service.errorMessage.collect { error ->
                _uiState.update { it.copy(ttsStatusMessage = error) }
            }
        }
    }

    fun loadSession(limit: Int = DEFAULT_SESSION_SIZE) {
        ensureTtsServiceInitialized()
        stopSpeaking()
        val effectiveLimit = resolveSessionSize(limit)
        _uiState.update { it.copy(isLoading = true, isSessionFinished = false) }

        viewModelScope.launch {
            runCatching {
                val restoredSnapshot = reviewSessionSnapshotRepository?.getActiveSession()
                allCardsCache = repository.getAllCards()
                val repairedQuestionProgress = repairMissingQuestionProgressIfNeeded(allCardsCache)

                if (shouldUseForcedAdminEventSession()) {
                    if (restoredSnapshot != null) {
                        clearPersistedSessionSnapshot()
                    }
                    startForcedAdminEventSession(effectiveLimit)
                    return@runCatching
                }

                if (
                    restoredSnapshot?.state?.sessionPlan != null &&
                    restoredSnapshot.state.sessionState != null &&
                    !shouldDiscardSnapshotForAdminFilters(restoredSnapshot, effectiveLimit)
                ) {
                    restoreFromSnapshot(restoredSnapshot)
                    maybeActivateDueEvent()
                    publishUiState(isAnswerRevealed = restoredSnapshot.state.isAnswerRevealed)
                    maybeAutoSpeakVisibleContent()
                    return@runCatching
                }

                if (restoredSnapshot != null) {
                    clearPersistedSessionSnapshot()
                }

                startNewSession(effectiveLimit, repairedQuestionProgress)
            }.onFailure {
                resetSessionInternals()
                _uiState.value = buildSessionFinishedState(
                    studiedCount = 0,
                    totalInSession = 0,
                    xpBonusAccumulated = 0,
                    autoSpeakWord = _uiState.value.autoSpeakWord,
                    autoSpeakDefinition = _uiState.value.autoSpeakDefinition,
                    presentationMode = _uiState.value.presentationMode
                )
                _snackbarEvents.trySend("Impossible de lancer l'entraînement pour le moment")
            }
        }
    }

    private fun resolveSessionSize(fallbackLimit: Int): Int {
        if (fallbackLimit != DEFAULT_SESSION_SIZE) {
            return fallbackLimit.coerceAtLeast(1)
        }

        val userSessionSize = userPrefsRepository?.cardsPerSession
        val adminSessionSize = adminPrefsRepository?.sessionSize
        val effective = if (isAdminAdvancedReviewModeEnabled()) {
            adminSessionSize ?: userSessionSize ?: fallbackLimit
        } else {
            userSessionSize ?: fallbackLimit
        }
        return effective.coerceIn(2, 50)
    }

    private suspend fun startNewSession(
        limit: Int,
        repairedQuestionProgress: List<ReviewQuestionProgress>? = null
    ) {
        sessionSizeLimit = limit
        val rawPlan = repairedQuestionProgress?.let {
            reviewSessionPlanner.buildPlanFromProgress(
                sessionSize = limit,
                questionProgress = it
            )
        } ?: reviewSessionPlanner.buildPlan(limit)
        val filteredPlan = applyAdminQuestionFilters(rawPlan)
        val cardsById = loadCardsForPlan(filteredPlan)
        val sanitizedPlan = sanitizePlan(filteredPlan, cardsById.keys)

        sessionPlan = sanitizedPlan
        sessionState = ReviewSessionEngine.start(sanitizedPlan)
        sessionCardsById = cardsById.filterKeys { cardId ->
            sanitizedPlan.selectedQuestions.any { it.cardId == cardId }
        }
        activeSessionCreatedAt = System.currentTimeMillis()
        currentUndoSnapshotState = null
        normalAnswersSinceLastMatching = 0
        pendingSessionEvents = buildInitialExtraSpellingEvents(sanitizedPlan)
        activeSessionEvent = null
        resetEventInteraction()
        maybeActivateDueEvent()

        publishUiState(isAnswerRevealed = false)
        persistSessionSnapshot()
        maybeAutoSpeakVisibleContent()
    }

    private fun startForcedAdminEventSession(limit: Int) {
        val loadedCards = allCardsCache
        resetSessionInternals()
        allCardsCache = loadedCards
        sessionSizeLimit = limit

        val forcedEvents = buildForcedAdminTestEvents(limit)
        if (forcedEvents.isEmpty()) {
            _uiState.value = buildSessionFinishedState(
                studiedCount = 0,
                totalInSession = 0,
                xpBonusAccumulated = _uiState.value.xpBonusAccumulated,
                autoSpeakWord = _uiState.value.autoSpeakWord,
                autoSpeakDefinition = _uiState.value.autoSpeakDefinition,
                presentationMode = _uiState.value.presentationMode
            )
            _snackbarEvents.trySend("Aucune carte compatible pour le test admin sélectionné")
            return
        }

        val relevantCardIds = forcedEvents
            .flatMap { event ->
                buildList {
                    event.cardId?.let(::add)
                    addAll(event.cardIds)
                }
            }
            .toSet()

        sessionCardsById = allCardsCache
            .filter { it.id in relevantCardIds }
            .associateBy { it.id }
        pendingSessionEvents = forcedEvents.drop(1)
        activeSessionEvent = forcedEvents.firstOrNull()
        resetEventInteraction()
        publishUiState(isAnswerRevealed = false)
    }

    fun revealAnswer() {
        if (_uiState.value.currentItemType != ReviewCurrentItemType.NORMAL_QUESTION) return
        stopSpeaking()
        _uiState.update { it.copy(isAnswerRevealed = true) }
        viewModelScope.launch { persistSessionSnapshot() }
        maybeAutoSpeakVisibleContent()
    }

    fun toggleAnswerReveal() {
        if (_uiState.value.currentItemType != ReviewCurrentItemType.NORMAL_QUESTION) return
        stopSpeaking()
        _uiState.update { it.copy(isAnswerRevealed = !it.isAnswerRevealed) }
        viewModelScope.launch { persistSessionSnapshot() }
        maybeAutoSpeakVisibleContent()
    }

    fun speakCurrentFace() {
        ensureTtsServiceInitialized()
        val current = _uiState.value.currentCard ?: return
        val textToRead = if (_uiState.value.currentItemType == ReviewCurrentItemType.NORMAL_QUESTION) {
            if (_uiState.value.isAnswerRevealed) {
                visibleAnswerText(current, _uiState.value.presentationMode)
            } else {
                visibleFrontText(current, _uiState.value.presentationMode)
            }
        } else {
            _uiState.value.eventInstruction.ifBlank { visibleFrontText(current, _uiState.value.presentationMode) }
        }
        ttsService?.speak(textToRead)
    }

    fun speakCurrentWord() {
        ensureTtsServiceInitialized()
        val current = _uiState.value.currentCard ?: return
        ttsService?.speak(current.recto)
    }

    fun speakCurrentDefinition() {
        ensureTtsServiceInitialized()
        val current = _uiState.value.currentCard ?: return
        ttsService?.speak(current.verso)
    }

    fun toggleAutoSpeakWord() {
        _uiState.update { it.copy(autoSpeakWord = !it.autoSpeakWord) }
        viewModelScope.launch { persistSessionSnapshot() }
        maybeAutoSpeakVisibleContent()
    }

    fun toggleAutoSpeakDefinition() {
        _uiState.update { it.copy(autoSpeakDefinition = !it.autoSpeakDefinition) }
        viewModelScope.launch { persistSessionSnapshot() }
        maybeAutoSpeakVisibleContent()
    }

    fun stopSpeaking() {
        ttsService?.stop()
    }

    private fun maybeAutoSpeakVisibleContent() {
        val state = _uiState.value
        val current = state.currentCard ?: return
        if (state.currentItemType != ReviewCurrentItemType.NORMAL_QUESTION) return

        val textToSpeak = when {
            state.isAnswerRevealed && state.autoSpeakDefinition -> visibleAnswerText(current, state.presentationMode)
            !state.isAnswerRevealed && state.presentationMode == ReviewPresentationMode.WORD_TO_DEFINITION && state.autoSpeakWord -> current.recto
            !state.isAnswerRevealed && state.presentationMode == ReviewPresentationMode.DEFINITION_TO_WORD && state.autoSpeakDefinition -> current.verso
            else -> null
        }

        textToSpeak?.let { text ->
            ttsService?.speak(text)
        }
    }

    fun gradeCard(quality: Int) {
        val activeState = sessionState ?: return
        if (activeState.isFinished || activeSessionEvent != null) return

        val currentQuestionId = activeState.currentQuestionId ?: return
        val beforeQuestionState = activeState.questionStates.getValue(currentQuestionId)
        val answer = quality.toReviewAnswer()
        val answeredAt = System.currentTimeMillis()
        currentUndoSnapshotState = currentSnapshotState()

        viewModelScope.launch {
            dailyStatDao?.let { dao ->
                DailyReviewStatHelper.recordReview(dao, isCorrect = answer != ReviewAnswer.AGAIN)
            }

            decrementPendingEventCountdowns()
            var updatedState = ReviewSessionEngine.answerCurrentQuestion(
                state = activeState,
                answer = answer,
                answeredAt = answeredAt
            )
            val updatedQuestionState = updatedState.questionStates.getValue(currentQuestionId)
            normalAnswersSinceLastMatching += 1

            updatedState = maybeScheduleQcm(updatedState, beforeQuestionState, updatedQuestionState)
            maybeScheduleMatching(updatedState)

            sessionState = updatedState
            maybeActivateDueEvent()

            if (activeSessionEvent == null && updatedState.isFinished) {
                finishAndCommitSession(answeredAt)
            } else {
                publishUiState(isAnswerRevealed = false)
                persistSessionSnapshot()
                maybeAutoSpeakVisibleContent()
            }
        }
    }

    fun onChoiceSelected(choice: String) {
        selectedChoice = choice
        publishUiState(isAnswerRevealed = false)
        viewModelScope.launch { persistSessionSnapshot() }
    }

    fun onEventInputChanged(text: String) {
        eventInput = text
        publishUiState(isAnswerRevealed = false)
        viewModelScope.launch { persistSessionSnapshot() }
    }

    fun onMatchingWordSelected(wordId: String) {
        val event = activeSessionEvent ?: return
        if (event.type != ReviewSessionEventType.MATCHING || eventResultSuccessful != null) return

        if (matchingSelectedDefinition != null) {
            assignMatching(wordId, matchingSelectedDefinition!!)
        } else {
            matchingSelectedWordId = if (matchingSelectedWordId == wordId) null else wordId
        }
        publishUiState(isAnswerRevealed = false)
        viewModelScope.launch { persistSessionSnapshot() }
    }

    fun onMatchingDefinitionSelected(definition: String) {
        val event = activeSessionEvent ?: return
        if (event.type != ReviewSessionEventType.MATCHING || eventResultSuccessful != null) return

        if (matchingSelectedWordId != null) {
            assignMatching(matchingSelectedWordId!!, definition)
        } else {
            matchingSelectedDefinition = if (matchingSelectedDefinition == definition) null else definition
        }
        publishUiState(isAnswerRevealed = false)
        viewModelScope.launch { persistSessionSnapshot() }
    }

    private fun assignMatching(wordId: String, definition: String) {
        matchingAssignments = matchingAssignments
            .filterKeys { it != wordId }
            .filterValues { it != definition } + (wordId to definition)
        matchingSelectedWordId = null
        matchingSelectedDefinition = null
    }

    fun submitActiveEvent() {
        val event = activeSessionEvent ?: return
        currentUndoSnapshotState = currentSnapshotState()

        viewModelScope.launch {
            when (event.type) {
                ReviewSessionEventType.QCM -> validateQcmEvent(event)
                ReviewSessionEventType.MATCHING -> validateMatchingEvent(event)
                ReviewSessionEventType.EXTRA_SPELLING -> validateExtraSpellingEvent(event)
                ReviewSessionEventType.CHALLENGE -> validateChallengeEvent(event)
            }
            persistSessionSnapshot()
        }
    }

    fun skipActiveEvent() {
        val event = activeSessionEvent ?: return
        if (!event.isSkippable) return
        currentUndoSnapshotState = currentSnapshotState()

        viewModelScope.launch {
            eventResultSuccessful = false
            eventResultMessage = "Question additionnelle passée"
            eventResultCorrectAnswer = event.correctAnswer.ifBlank { null }
            publishUiState(isAnswerRevealed = false)
            persistSessionSnapshot()
        }
    }

    fun continueAfterEventResult() {
        if (eventResultSuccessful == null && eventResultMessage == null) return

        viewModelScope.launch {
            activeSessionEvent = null
            resetEventInteraction()
            maybeActivateDueEvent()

            val activeState = sessionState
            if (activeSessionEvent == null && activeState?.isFinished == true) {
                finishAndCommitSession(System.currentTimeMillis())
            } else {
                publishUiState(isAnswerRevealed = false)
                persistSessionSnapshot()
                maybeAutoSpeakVisibleContent()
            }
        }
    }

    private suspend fun validateQcmEvent(event: ReviewSessionEvent) {
        val selected = selectedChoice ?: return
        val isCorrect = selected == event.correctAnswer
        if (event.appliesSessionCredit && isCorrect && event.questionId != null) {
            sessionState = sessionState?.let { ReviewSessionEngine.applyEventGotIt(it, event.questionId, System.currentTimeMillis()) }
        }

        eventResultSuccessful = isCorrect
        eventResultMessage = if (event.appliesSessionCredit) {
            if (isCorrect) {
                "QCM réussi — crédit local appliqué"
            } else {
                "QCM incorrect — pas de crédit local"
            }
        } else {
            if (isCorrect) "QCM réussi (mode test admin)" else "QCM incorrect (mode test admin)"
        }
        eventResultCorrectAnswer = event.correctAnswer
        publishUiState(isAnswerRevealed = false)
    }

    private suspend fun validateMatchingEvent(event: ReviewSessionEvent) {
        val cards = event.cardIds.mapNotNull(sessionCardsById::get)
        if (cards.isEmpty()) return
        if (matchingAssignments.size < cards.size) return

        var updatedState = sessionState
        val correctlyMatchedCards = cards.filter { card ->
            matchingAssignments[card.id] == card.verso
        }

        if (event.appliesSessionCredit) {
            correctlyMatchedCards.forEach { card ->
                questionIdsForCard(card.id).forEach { questionId ->
                    updatedState = updatedState?.let { ReviewSessionEngine.applyEventGotIt(it, questionId, System.currentTimeMillis()) }
                }
            }
        }
        sessionState = updatedState

        eventResultSuccessful = correctlyMatchedCards.isNotEmpty()
        eventResultMessage = if (event.appliesSessionCredit) {
            "${correctlyMatchedCards.size}/${cards.size} carte(s) ont gagné un crédit local"
        } else {
            "${correctlyMatchedCards.size}/${cards.size} association(s) correcte(s) (mode test admin)"
        }
        eventResultCorrectAnswer = null
        publishUiState(isAnswerRevealed = false)
    }

    private suspend fun validateExtraSpellingEvent(event: ReviewSessionEvent) {
        val card = event.cardId?.let(sessionCardsById::get) ?: return
        val result = spellingValidator.validate(eventInput, card.recto)
        eventResultSuccessful = result.isValid
        eventResultMessage = if (result.isValid) {
            "Orthographe correcte"
        } else {
            result.feedbackMessage
        }
        eventResultCorrectAnswer = card.recto
        publishUiState(isAnswerRevealed = false)
    }

    private suspend fun validateChallengeEvent(event: ReviewSessionEvent) {
        val card = event.cardId?.let(sessionCardsById::get) ?: return
        val isCorrect = when (event.challengeKind) {
            ReviewSessionChallengeKind.SPELLING -> spellingValidator.validate(eventInput, card.recto).isValid
            ReviewSessionChallengeKind.SEMANTIC -> semanticValidator.validate(eventInput, card.verso).isValid
            null -> false
        }

        if (event.appliesSessionCredit && event.questionId != null) {
            sessionState = sessionState?.let { ReviewSessionEngine.clearPendingReplacementChallenge(it, event.questionId) }
            sessionState = sessionState?.let {
                ReviewSessionEngine.answerCurrentQuestion(
                    state = it,
                    answer = if (isCorrect) ReviewAnswer.GOT_IT else ReviewAnswer.AGAIN,
                    answeredAt = System.currentTimeMillis()
                )
            }
        }

        eventResultSuccessful = isCorrect
        eventResultMessage = if (event.appliesSessionCredit) {
            if (isCorrect) {
                "Défi réussi"
            } else {
                "Défi manqué"
            }
        } else {
            if (isCorrect) "Défi réussi (mode test admin)" else "Défi manqué (mode test admin)"
        }
        eventResultCorrectAnswer = when (event.challengeKind) {
            ReviewSessionChallengeKind.SPELLING -> card.recto
            ReviewSessionChallengeKind.SEMANTIC -> card.verso
            null -> null
        }
        publishUiState(isAnswerRevealed = false)
    }

    private fun maybeScheduleQcm(
        state: ReviewSessionState,
        previousQuestionState: ReviewSessionQuestionState,
        updatedQuestionState: ReviewSessionQuestionState
    ): ReviewSessionState {
        if (!isIntegratedQcmEnabled()) return state
        if (updatedQuestionState.isValidated) return state
        if (previousQuestionState.againCount >= 3 || updatedQuestionState.againCount < 3 || updatedQuestionState.qcmAlreadyScheduled) {
            return state
        }

        val card = sessionCardsById[updatedQuestionState.progress.cardId] ?: return state
        val correctAnswer = if (updatedQuestionState.progress.questionType == ReviewQuestionType.WORD_TO_DEFINITION) {
            card.verso
        } else {
            card.recto
        }
        val options = buildQcmOptions(card, updatedQuestionState.progress.questionType, correctAnswer)
        if (options.size < 4) return state

        pendingSessionEvents = pendingSessionEvents + ReviewSessionEvent(
            eventId = "qcm-${updatedQuestionState.progress.questionId}-${updatedQuestionState.againCount}",
            type = ReviewSessionEventType.QCM,
            questionId = updatedQuestionState.progress.questionId,
            cardId = card.id,
            prompt = if (updatedQuestionState.progress.questionType == ReviewQuestionType.WORD_TO_DEFINITION) card.recto else card.verso,
            options = options,
            correctAnswer = correctAnswer,
            countdownBeforeDisplay = computeCountdownBeforeNextOccurrence(updatedQuestionState.progress.questionId)
        )
        return ReviewSessionEngine.markQcmScheduled(state, updatedQuestionState.progress.questionId)
    }

    private fun maybeScheduleMatching(state: ReviewSessionState) {
        if (!isIntegratedMatchingEnabled()) return
        if (sessionSizeLimit <= 0 || normalAnswersSinceLastMatching < sessionSizeLimit) return
        normalAnswersSinceLastMatching = 0

        val candidateCardIds = state.questionStates.values
            .groupBy { it.progress.cardId }
            .values
            .sortedWith(
                compareByDescending<List<ReviewSessionQuestionState>> { group -> group.sumOf { it.againCount } }
                    .thenByDescending { group -> group.sumOf { it.presentationCount } }
                    .thenBy { group -> group.minOf { it.progress.globalOrder } }
            )
            .map { group -> group.first().progress.cardId }
            .take(5)

        if (candidateCardIds.size < 2) return

        val shuffledDefinitions = candidateCardIds
            .mapNotNull(sessionCardsById::get)
            .map { it.verso }
            .shuffled(random)

        pendingSessionEvents = pendingSessionEvents + ReviewSessionEvent(
            eventId = "matching-${System.currentTimeMillis()}",
            type = ReviewSessionEventType.MATCHING,
            cardIds = candidateCardIds,
            options = shuffledDefinitions,
            countdownBeforeDisplay = 0
        )
    }

    private fun buildInitialExtraSpellingEvents(plan: ReviewSessionPlan): List<ReviewSessionEvent> {
        if (!isExtraSpellingEnabled()) return emptyList()
        var updatedState = sessionState
        val events = mutableListOf<ReviewSessionEvent>()

        plan.selectedQuestions
            .filter { it.questionType == ReviewQuestionType.WORD_TO_DEFINITION }
            .forEach { question ->
                val card = sessionCardsById[question.cardId] ?: return@forEach
                val hasPendingSpellingChallengeForCard = plan.selectedQuestions
                    .filter { it.cardId == question.cardId }
                    .any { it.pendingReplacementChallengeKind == ReviewSessionChallengeKind.SPELLING }
                if (hasPendingSpellingChallengeForCard) {
                    return@forEach
                }
                events += ReviewSessionEvent(
                    eventId = "extra-${question.questionId}",
                    type = ReviewSessionEventType.EXTRA_SPELLING,
                    questionId = question.questionId,
                    cardId = question.cardId,
                    correctAnswer = card.recto,
                    countdownBeforeDisplay = if (plan.selectedQuestions.size <= 1) 0 else random.nextInt(plan.selectedQuestions.size),
                    isSkippable = true,
                    appliesSessionCredit = false
                )
                updatedState = updatedState?.let { ReviewSessionEngine.markExtraSpellingScheduled(it, question.questionId) }
            }

        sessionState = updatedState
        return events
    }

    private fun decrementPendingEventCountdowns() {
        pendingSessionEvents = pendingSessionEvents.map { event ->
            if (event.countdownBeforeDisplay > 0) {
                event.copy(countdownBeforeDisplay = event.countdownBeforeDisplay - 1)
            } else {
                event
            }
        }
    }

    private fun maybeActivateDueEvent() {
        if (activeSessionEvent != null) return
        val currentQuestionState = sessionState?.currentQuestionState
        if (
            currentQuestionState != null &&
            currentQuestionState.presentationCount == 0 &&
            currentQuestionState.progress.pendingReplacementChallengeKind != null
        ) {
            val challengeKind = currentQuestionState.progress.pendingReplacementChallengeKind
            if (challengeKind != null && !isReplacementChallengeEnabled(challengeKind)) {
                sessionState = sessionState?.let {
                    ReviewSessionEngine.clearPendingReplacementChallenge(it, currentQuestionState.progress.questionId)
                }
            } else {
                activeSessionEvent = buildReplacementChallengeEvent(currentQuestionState)
            }
            resetEventInteraction(keepResult = false)
            if (activeSessionEvent != null) return
        }
        val dueEvent = pendingSessionEvents.firstOrNull { it.countdownBeforeDisplay <= 0 }
            ?: return

        activeSessionEvent = dueEvent
        pendingSessionEvents = pendingSessionEvents.filterNot { it.eventId == dueEvent.eventId }
        resetEventInteraction(keepResult = false)
    }

    private fun computeCountdownBeforeNextOccurrence(questionId: String): Int {
        val state = sessionState ?: return 0
        if (state.isFinished || state.currentQuestionId == null) return 0
        if (state.questionStates[questionId]?.isValidated != false) return 0

        var stepsBeforeTarget = 0
        val order = state.sessionOrderQuestionIds
        for (offset in 0 until order.size) {
            val candidateIndex = (state.currentOrderIndex + offset) % order.size
            val candidateId = order[candidateIndex]
            val candidateState = state.questionStates[candidateId] ?: continue
            if (candidateState.isValidated) continue
            if (candidateId == questionId) break
            stepsBeforeTarget += 1
        }

        return if (stepsBeforeTarget <= 0) 0 else random.nextInt(stepsBeforeTarget)
    }

    private fun buildQcmOptions(
        targetCard: Flashcard,
        questionType: ReviewQuestionType,
        correctAnswer: String
    ): List<String> {
        val distractors = allCardsCache
            .asSequence()
            .filter { it.id != targetCard.id }
            .map {
                when (questionType) {
                    ReviewQuestionType.WORD_TO_DEFINITION -> it.verso.trim()
                    ReviewQuestionType.DEFINITION_TO_WORD -> it.recto.trim()
                }
            }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .filter { it.lowercase() != correctAnswer.trim().lowercase() }
            .shuffled(random)
            .take(3)
            .toList()

        return (listOf(correctAnswer) + distractors)
            .distinctBy { it.lowercase() }
            .shuffled(random)
    }

    private fun questionIdsForCard(cardId: String): List<String> {
        return sessionState?.questionStates
            ?.values
            ?.filter { it.progress.cardId == cardId && !it.isValidated }
            ?.map { it.progress.questionId }
            .orEmpty()
    }

    private suspend fun finishAndCommitSession(finishedAt: Long) {
        val activeState = sessionState ?: return
        val completion = ReviewSessionEngine.completeSession(activeState, finishedAt)
        val finalProgressToPersist = completion.finalQuestionStates.zip(completion.finalQuestionProgress).map { (questionState, progress) ->
            applyReplacementChallengePolicy(questionState, progress)
        }
        repository.saveQuestionProgress(finalProgressToPersist)

        val totalInSession = sessionPlan?.selectedQuestions?.size ?: completion.finalQuestionStates.size
        val sessionXp = totalInSession.coerceAtLeast(0)
        activeSessionEvent = null
        pendingSessionEvents = emptyList()
        resetEventInteraction()
        if (sessionXp > 0) {
            onSessionXpAwarded(sessionXp)
        }
        _uiState.value = buildSessionFinishedState(
            studiedCount = totalInSession,
            totalInSession = totalInSession,
            xpBonusAccumulated = _uiState.value.xpBonusAccumulated,
            autoSpeakWord = _uiState.value.autoSpeakWord,
            autoSpeakDefinition = _uiState.value.autoSpeakDefinition,
            presentationMode = _uiState.value.presentationMode,
            showSessionCelebration = sessionXp > 0,
            sessionCompletionXp = sessionXp,
            sessionCompletionToken = finishedAt
        )
        clearPersistedSessionSnapshot()
        _snackbarEvents.trySend("Session terminée")
    }

    fun undoLastAnswer() {
        val snapshotState = currentUndoSnapshotState ?: return
        viewModelScope.launch {
            currentUndoSnapshotState = null
            applySnapshotState(snapshotState)
            publishUiState(isAnswerRevealed = snapshotState.isAnswerRevealed)
            persistSessionSnapshot()
            maybeAutoSpeakVisibleContent()
        }
    }

    fun cancelSession() {
        stopSpeaking()
        viewModelScope.launch {
            persistValidatedProgressBeforeReset()
            resetSessionInternals()
            clearPersistedSessionSnapshot()
            _uiState.value = buildSessionFinishedState(
                studiedCount = 0,
                totalInSession = 0,
                xpBonusAccumulated = 0,
                autoSpeakWord = _uiState.value.autoSpeakWord,
                autoSpeakDefinition = _uiState.value.autoSpeakDefinition,
                presentationMode = _uiState.value.presentationMode
            )
        }
    }

    fun invalidateSessionForSettingsChange() {
        stopSpeaking()
        viewModelScope.launch {
            persistValidatedProgressBeforeReset()
            resetSessionInternals()
            clearPersistedSessionSnapshot()
            _uiState.value = buildSessionFinishedState(
                studiedCount = 0,
                totalInSession = 0,
                xpBonusAccumulated = 0,
                autoSpeakWord = _uiState.value.autoSpeakWord,
                autoSpeakDefinition = _uiState.value.autoSpeakDefinition,
                presentationMode = _uiState.value.presentationMode
            )
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value.currentCard ?: return
        val updated = current.copy(favori = !current.favori)
        sessionCardsById = sessionCardsById + (current.id to updated)
        _uiState.update { it.copy(currentCard = updated) }
        viewModelScope.launch {
            repository.setFavorite(current.id, updated.favori)
            persistSessionSnapshot()
        }
    }

    fun deleteCurrentCard() {
        val current = _uiState.value.currentCard ?: return
        viewModelScope.launch {
            stopSpeaking()
            repository.deleteCard(current.id)
            removeCardFromActiveSession(current.id)
            pendingSessionEvents = pendingSessionEvents.filterNot { event ->
                event.cardId == current.id || event.cardIds.contains(current.id)
            }
            if (activeSessionEvent?.cardId == current.id || activeSessionEvent?.cardIds?.contains(current.id) == true) {
                activeSessionEvent = null
                resetEventInteraction()
                maybeActivateDueEvent()
            }

            if (activeSessionEvent == null && sessionState?.isFinished == true) {
                finishAndCommitSession(System.currentTimeMillis())
            } else {
                publishUiState(isAnswerRevealed = false)
                if (_uiState.value.isSessionFinished) {
                    clearPersistedSessionSnapshot()
                } else {
                    persistSessionSnapshot()
                    maybeAutoSpeakVisibleContent()
                }
            }
        }
    }

    private suspend fun loadCardsForPlan(plan: ReviewSessionPlan): Map<String, Flashcard> {
        if (plan.selectedQuestions.isEmpty()) return emptyMap()
        val cardIds = plan.selectedQuestions.map { it.cardId }.toSet()
        return allCardsCache
            .filter { it.id in cardIds }
            .associateBy { it.id }
    }

    private fun sanitizePlan(
        plan: ReviewSessionPlan,
        availableCardIds: Set<String>
    ): ReviewSessionPlan {
        if (availableCardIds.isEmpty()) {
            return plan.copy(selectedQuestions = emptyList(), sessionOrder = emptyList(), remainingQuestionsCount = 0)
        }

        return plan.copy(
            selectedQuestions = plan.selectedQuestions.filter { it.cardId in availableCardIds },
            sessionOrder = plan.sessionOrder.filter { it.cardId in availableCardIds }
        )
    }

    private fun publishUiState(isAnswerRevealed: Boolean) {
        _uiState.value = buildUiState(isAnswerRevealed)
    }

    private fun buildUiState(isAnswerRevealed: Boolean): ReviewUiState {
        val activeEvent = activeSessionEvent
        return if (activeEvent != null) {
            buildEventUiState(activeEvent)
        } else {
            buildNormalUiState(isAnswerRevealed)
        }
    }

    private fun buildNormalUiState(isAnswerRevealed: Boolean): ReviewUiState {
        val activePlan = sessionPlan
        val activeState = sessionState
        if (activePlan == null || activeState == null || activePlan.selectedQuestions.isEmpty()) {
            return buildSessionFinishedState(
                studiedCount = 0,
                totalInSession = 0,
                xpBonusAccumulated = _uiState.value.xpBonusAccumulated,
                autoSpeakWord = _uiState.value.autoSpeakWord,
                autoSpeakDefinition = _uiState.value.autoSpeakDefinition,
                presentationMode = _uiState.value.presentationMode
            )
        }

        val totalInSession = activePlan.selectedQuestions.size
        val studiedCount = totalInSession - activeState.remainingQuestionsToValidate
        val currentQuestionState = activeState.currentQuestionState
        val currentCard = currentQuestionState?.let { sessionCardsById[it.progress.cardId] }

        if (activeState.isFinished || currentQuestionState == null || currentCard == null) {
            return buildSessionFinishedState(
                studiedCount = studiedCount,
                totalInSession = totalInSession,
                xpBonusAccumulated = _uiState.value.xpBonusAccumulated,
                autoSpeakWord = _uiState.value.autoSpeakWord,
                autoSpeakDefinition = _uiState.value.autoSpeakDefinition,
                presentationMode = _uiState.value.presentationMode
            )
        }

        return ReviewUiState(
            currentCard = currentCard,
            currentItemType = ReviewCurrentItemType.NORMAL_QUESTION,
            isAnswerRevealed = isAnswerRevealed,
            remainingQuestionsToValidate = activeState.remainingQuestionsToValidate,
            isLoading = false,
            isSpeaking = false,
            ttsReady = ttsService?.isReady?.value == true,
            isSessionFinished = false,
            studiedCount = studiedCount,
            totalInSession = totalInSession,
            sessionProgress = calculateSessionProgress(activeState),
            autoSpeakWord = _uiState.value.autoSpeakWord,
            autoSpeakDefinition = _uiState.value.autoSpeakDefinition,
            presentationMode = currentQuestionState.progress.questionType.toPresentationMode(),
            xpBonusAccumulated = _uiState.value.xpBonusAccumulated,
            canUndo = currentUndoSnapshotState != null,
            normalQuestionInstanceKey = "${currentQuestionState.progress.questionId}:${currentQuestionState.presentationCount}",
            ttsStatusMessage = _uiState.value.ttsStatusMessage
        )
    }

    private fun buildEventUiState(event: ReviewSessionEvent): ReviewUiState {
        val activePlan = sessionPlan
        val activeState = sessionState
        val totalInSession = activePlan?.selectedQuestions?.size ?: 0
        val studiedCount = if (activeState == null) 0 else totalInSession - activeState.remainingQuestionsToValidate
        val eventCards = when {
            event.cardIds.isNotEmpty() -> event.cardIds.mapNotNull(sessionCardsById::get)
            event.cardId != null -> listOfNotNull(sessionCardsById[event.cardId])
            else -> emptyList()
        }
        val primaryCard = event.cardId?.let(sessionCardsById::get) ?: eventCards.firstOrNull()
        val questionType = event.questionId?.let { sessionState?.questionStates?.get(it)?.progress?.questionType }
            ?: ReviewQuestionType.WORD_TO_DEFINITION

        val (title, instruction, canSkip, challengeKind) = when (event.type) {
            ReviewSessionEventType.QCM -> {
                val prompt = if (questionType == ReviewQuestionType.WORD_TO_DEFINITION) {
                    "Choisis la bonne définition"
                } else {
                    "Choisis le bon mot"
                }
                QuadrupleUi("QCM intégré", prompt, false, null)
            }
            ReviewSessionEventType.MATCHING -> QuadrupleUi("Correspondance intégrée", "Associe chaque mot à sa définition", false, null)
            ReviewSessionEventType.EXTRA_SPELLING -> QuadrupleUi("Question orthographique", "Écris le mot entendu ou deviné à partir de la définition", true, null)
            ReviewSessionEventType.CHALLENGE -> {
                val challengeTitle = when (event.challengeKind) {
                    ReviewSessionChallengeKind.SPELLING -> "Défi orthographique"
                    ReviewSessionChallengeKind.SEMANTIC -> "Défi sémantique"
                    null -> "Défi"
                }
                val challengeInstruction = when (event.challengeKind) {
                    ReviewSessionChallengeKind.SPELLING -> "Écris le mot correspondant à la définition"
                    ReviewSessionChallengeKind.SEMANTIC -> "Décris le sens du mot avec tes propres mots"
                    null -> "Résous le défi"
                }
                QuadrupleUi(challengeTitle, challengeInstruction, false, event.challengeKind)
            }
        }

        return ReviewUiState(
            currentCard = primaryCard,
            currentItemType = when (event.type) {
                ReviewSessionEventType.QCM -> ReviewCurrentItemType.QCM
                ReviewSessionEventType.MATCHING -> ReviewCurrentItemType.MATCHING
                ReviewSessionEventType.EXTRA_SPELLING -> ReviewCurrentItemType.EXTRA_SPELLING
                ReviewSessionEventType.CHALLENGE -> ReviewCurrentItemType.CHALLENGE
            },
            isAnswerRevealed = false,
            remainingQuestionsToValidate = activeState?.remainingQuestionsToValidate ?: 0,
            isLoading = false,
            isSpeaking = false,
            ttsReady = ttsService?.isReady?.value == true,
            isSessionFinished = false,
            studiedCount = studiedCount,
            totalInSession = totalInSession,
            sessionProgress = calculateSessionProgress(activeState),
            autoSpeakWord = _uiState.value.autoSpeakWord,
            autoSpeakDefinition = _uiState.value.autoSpeakDefinition,
            presentationMode = questionType.toPresentationMode(),
            xpBonusAccumulated = _uiState.value.xpBonusAccumulated,
            canUndo = currentUndoSnapshotState != null,
            normalQuestionInstanceKey = "",
            ttsStatusMessage = _uiState.value.ttsStatusMessage,
            eventTitle = title,
            eventInstruction = instruction,
            eventOptions = event.options,
            selectedChoice = selectedChoice,
            eventInput = eventInput,
            eventResultSuccessful = eventResultSuccessful,
            eventResultMessage = eventResultMessage,
            eventResultCorrectAnswer = eventResultCorrectAnswer,
            eventCards = eventCards,
            matchingAssignments = matchingAssignments,
            matchingSelectedWordId = matchingSelectedWordId,
            matchingSelectedDefinition = matchingSelectedDefinition,
            canSkipCurrentEvent = canSkip || event.isSkippable,
            activeChallengeKind = challengeKind
        )
    }

    private fun buildSessionFinishedState(
        studiedCount: Int,
        totalInSession: Int,
        xpBonusAccumulated: Int,
        autoSpeakWord: Boolean,
        autoSpeakDefinition: Boolean,
        presentationMode: ReviewPresentationMode,
        showSessionCelebration: Boolean = false,
        sessionCompletionXp: Int = 0,
        sessionCompletionToken: Long = 0L
    ): ReviewUiState {
        return ReviewUiState(
            currentCard = null,
            currentItemType = ReviewCurrentItemType.NORMAL_QUESTION,
            isAnswerRevealed = false,
            remainingQuestionsToValidate = 0,
            isLoading = false,
            isSpeaking = false,
            ttsReady = ttsService?.isReady?.value == true,
            isSessionFinished = true,
            studiedCount = studiedCount,
            totalInSession = totalInSession,
            sessionProgress = if (totalInSession > 0) 1f else 0f,
            autoSpeakWord = autoSpeakWord,
            autoSpeakDefinition = autoSpeakDefinition,
            presentationMode = presentationMode,
            xpBonusAccumulated = xpBonusAccumulated,
            canUndo = currentUndoSnapshotState != null,
            normalQuestionInstanceKey = "",
            ttsStatusMessage = _uiState.value.ttsStatusMessage,
            showSessionCelebration = showSessionCelebration,
            sessionCompletionXp = sessionCompletionXp,
            sessionCompletionToken = sessionCompletionToken
        )
    }

    private fun calculateSessionProgress(state: ReviewSessionState?): Float {
        val questionStates = state?.questionStates?.values.orEmpty()
        if (questionStates.isEmpty()) return 0f

        val accumulatedProgress = questionStates.sumOf { questionStateProgress(it).toDouble() }
        return (accumulatedProgress / questionStates.size).toFloat().coerceIn(0f, 1f)
    }

    private fun questionStateProgress(questionState: ReviewSessionQuestionState): Float {
        if (questionState.isValidated) return 1f

        val gotItProgress = (questionState.gotItCount / 2f).coerceIn(0f, 1f)
        val againProgress = (questionState.againCount / 5f).coerceIn(0f, 1f)
        return max(gotItProgress, againProgress)
    }

    private fun removeCardFromActiveSession(cardId: String) {
        val activePlan = sessionPlan
        val activeState = sessionState
        sessionCardsById = sessionCardsById - cardId

        sessionPlan = activePlan?.copy(
            selectedQuestions = activePlan.selectedQuestions.filterNot { it.cardId == cardId },
            sessionOrder = activePlan.sessionOrder.filterNot { it.cardId == cardId }
        )

        if (activeState == null) {
            return
        }

        val updatedQuestionStates = activeState.questionStates.filterValues { it.progress.cardId != cardId }
        val updatedOrderIds = activeState.sessionOrderQuestionIds.filter { updatedQuestionStates.containsKey(it) }
        val remainingQuestions = updatedQuestionStates.values.count { !it.isValidated }
        val nextOrderIndex = findNextUnvalidatedIndex(
            sessionOrderQuestionIds = updatedOrderIds,
            currentOrderIndex = activeState.currentOrderIndex,
            questionStates = updatedQuestionStates
        )
        val nextQuestionId = if (nextOrderIndex == -1) null else updatedOrderIds[nextOrderIndex]

        sessionState = ReviewSessionState(
            sessionOrderQuestionIds = updatedOrderIds,
            questionStates = updatedQuestionStates,
            currentQuestionId = nextQuestionId,
            currentOrderIndex = nextOrderIndex,
            remainingQuestionsToValidate = remainingQuestions,
            isFinished = remainingQuestions == 0 || updatedOrderIds.isEmpty()
        )
    }

    private fun findNextUnvalidatedIndex(
        sessionOrderQuestionIds: List<String>,
        currentOrderIndex: Int,
        questionStates: Map<String, ReviewSessionQuestionState>
    ): Int {
        if (sessionOrderQuestionIds.isEmpty()) return -1
        val normalizedIndex = currentOrderIndex.coerceAtLeast(-1)

        for (offset in 0 until sessionOrderQuestionIds.size) {
            val candidateIndex = Math.floorMod(normalizedIndex + offset, sessionOrderQuestionIds.size)
            val candidateId = sessionOrderQuestionIds[candidateIndex]
            val candidateState = questionStates.getValue(candidateId)
            if (!candidateState.isValidated) {
                return candidateIndex
            }
        }

        return -1
    }

    private suspend fun repairMissingQuestionProgressIfNeeded(cards: List<Flashcard>): List<ReviewQuestionProgress>? {
        if (cards.isEmpty()) return null

        val existingProgress = repository.getAllQuestionProgress()
        val existingQuestionIds = existingProgress.mapTo(mutableSetOf()) { it.questionId }
        val missingProgress = cards.flatMap { card ->
            ReviewQuestionType.entries.mapNotNull { questionType ->
                val candidate = card.toReviewQuestionProgress(questionType)
                candidate.takeIf { it.questionId !in existingQuestionIds }
            }
        }

        if (missingProgress.isEmpty()) return null

        repository.saveQuestionProgress(missingProgress)
        return existingProgress + missingProgress
    }

    private suspend fun persistValidatedProgressBeforeReset() {
        val activeState = sessionState ?: return
        val committedAt = System.currentTimeMillis()
        val validatedProgress = activeState.questionStates.values.mapNotNull { questionState ->
            val effectiveAnswer = questionState.effectiveLongTermAnswer ?: return@mapNotNull null
            if (!questionState.isValidated) return@mapNotNull null

            val updatedProgress = ReviewIntervalEngine.applyFirstAnswer(
                progress = questionState.progress,
                answer = effectiveAnswer,
                nowMs = committedAt
            )
            applyReplacementChallengePolicy(questionState, updatedProgress)
        }

        if (validatedProgress.isNotEmpty()) {
            repository.saveQuestionProgress(validatedProgress)
        }
    }

    private fun applyReplacementChallengePolicy(
        questionState: ReviewSessionQuestionState,
        progress: ReviewQuestionProgress
    ): ReviewQuestionProgress {
        val replacementChallengeKind = questionState.progress.questionType.toReplacementChallengeKind()
        val shouldArmReplacementChallenge = when (questionState.firstAnswer) {
            ReviewAnswer.TOO_EASY -> true
            ReviewAnswer.GOT_IT -> questionState.progress.recentStreak < 3 && progress.recentStreak >= 3
            else -> false
        }

        return progress.copy(
            pendingReplacementChallengeKind = when {
                shouldArmReplacementChallenge && isReplacementChallengeEnabled(replacementChallengeKind) -> replacementChallengeKind
                progress.pendingReplacementChallengeKind != null && !isReplacementChallengeEnabled(progress.pendingReplacementChallengeKind) -> null
                else -> progress.pendingReplacementChallengeKind
            }
        )
    }

    private suspend fun persistSessionSnapshot() {
        val repository = reviewSessionSnapshotRepository ?: return
        val activePlan = sessionPlan
        val activeState = sessionState
        if (_uiState.value.isSessionFinished || activePlan == null || activeState == null) {
            clearPersistedSessionSnapshot()
            return
        }

        val createdAt = activeSessionCreatedAt ?: System.currentTimeMillis().also { activeSessionCreatedAt = it }
        repository.saveActiveSession(
            ReviewSessionSnapshot(
                createdAt = createdAt,
                updatedAt = System.currentTimeMillis(),
                state = currentSnapshotState(),
                undoState = currentUndoSnapshotState,
                plannedInsertions = pendingSessionEvents.map { "${it.type}:${it.eventId}" }
            )
        )
    }

    private suspend fun clearPersistedSessionSnapshot() {
        reviewSessionSnapshotRepository?.clearActiveSession()
        activeSessionCreatedAt = null
        currentUndoSnapshotState = null
    }

    private fun restoreFromSnapshot(snapshot: ReviewSessionSnapshot) {
        activeSessionCreatedAt = snapshot.createdAt
        currentUndoSnapshotState = snapshot.undoState
        applySnapshotState(snapshot.state)
    }

    private fun applySnapshotState(snapshotState: ReviewSessionSnapshotState) {
        sessionPlan = snapshotState.sessionPlan
        sessionState = snapshotState.sessionState
        sessionCardsById = snapshotState.sessionCards.orEmpty().associateBy { it.id }
        pendingSessionEvents = snapshotState.pendingSessionEvents.orEmpty()
        activeSessionEvent = snapshotState.activeSessionEvent
        eventInput = snapshotState.eventInput
        selectedChoice = snapshotState.selectedChoice
        matchingAssignments = snapshotState.matchingAssignments.orEmpty()
        matchingSelectedWordId = snapshotState.matchingSelectedWordId
        matchingSelectedDefinition = snapshotState.matchingSelectedDefinition
        eventResultSuccessful = snapshotState.eventResultSuccessful
        eventResultMessage = snapshotState.eventResultMessage
        eventResultCorrectAnswer = snapshotState.eventResultCorrectAnswer
        normalAnswersSinceLastMatching = snapshotState.normalAnswersSinceLastMatching
        sessionSizeLimit = snapshotState.sessionSizeLimit.takeIf { it > 0 } ?: DEFAULT_SESSION_SIZE
    }

    private fun currentSnapshotState(): ReviewSessionSnapshotState = ReviewSessionSnapshotState(
        currentCard = _uiState.value.currentCard,
        pendingCards = emptyList(),
        currentFaceIsMotVersDef = _uiState.value.presentationMode == ReviewPresentationMode.WORD_TO_DEFINITION,
        trainingModeCursor = 0,
        sessionCards = sessionCardsById.values.toList(),
        sessionPlan = sessionPlan,
        sessionState = sessionState,
        pendingSessionEvents = pendingSessionEvents,
        activeSessionEvent = activeSessionEvent,
        eventInput = eventInput,
        selectedChoice = selectedChoice,
        matchingAssignments = matchingAssignments,
        matchingSelectedWordId = matchingSelectedWordId,
        matchingSelectedDefinition = matchingSelectedDefinition,
        eventResultSuccessful = eventResultSuccessful,
        eventResultMessage = eventResultMessage,
        eventResultCorrectAnswer = eventResultCorrectAnswer,
        normalAnswersSinceLastMatching = normalAnswersSinceLastMatching,
        sessionSizeLimit = sessionSizeLimit,
        adminNormalPresentationEnabled = isAdminNormalPresentationEnabled(),
        isAnswerRevealed = _uiState.value.isAnswerRevealed,
        isSessionFinished = _uiState.value.isSessionFinished,
        studiedCount = _uiState.value.studiedCount,
        totalInSession = _uiState.value.totalInSession,
        xpBonusAccumulated = _uiState.value.xpBonusAccumulated,
        autoSpeakWord = _uiState.value.autoSpeakWord,
        autoSpeakDefinition = _uiState.value.autoSpeakDefinition,
        presentationModeName = _uiState.value.presentationMode.name
    )

    private fun resetEventInteraction(keepResult: Boolean = false) {
        eventInput = ""
        selectedChoice = null
        matchingAssignments = emptyMap()
        matchingSelectedWordId = null
        matchingSelectedDefinition = null
        if (!keepResult) {
            eventResultSuccessful = null
            eventResultMessage = null
            eventResultCorrectAnswer = null
        }
    }

    private fun resetSessionInternals() {
        sessionPlan = null
        sessionState = null
        sessionCardsById = emptyMap()
        allCardsCache = emptyList()
        pendingSessionEvents = emptyList()
        activeSessionEvent = null
        resetEventInteraction()
        activeSessionCreatedAt = null
        currentUndoSnapshotState = null
        normalAnswersSinceLastMatching = 0
        sessionSizeLimit = DEFAULT_SESSION_SIZE
    }

    private fun applyAdminQuestionFilters(plan: ReviewSessionPlan): ReviewSessionPlan {
        if (!isAdminReviewFilteringEnabled()) return plan

        val allowedQuestionIds = plan.selectedQuestions
            .filter { isQuestionTypeEnabled(it.questionType) }
            .map { it.questionId }
            .toSet()

        return plan.copy(
            selectedQuestions = plan.selectedQuestions.filter { it.questionId in allowedQuestionIds },
            sessionOrder = plan.sessionOrder.filter { it.questionId in allowedQuestionIds },
            remainingQuestionsCount = allowedQuestionIds.size
        )
    }

    private fun isAdminReviewModeAvailable(): Boolean =
        isAdminUserProvider?.invoke() == true && adminPrefsRepository != null

    private fun isAdminNormalPresentationEnabled(): Boolean =
        isAdminReviewModeAvailable() && adminPrefsRepository?.normalPresentationEnabled != false

    private fun isAdminAdvancedReviewModeEnabled(): Boolean =
        isAdminReviewModeAvailable() && !isAdminNormalPresentationEnabled()

    private fun isAdminReviewFilteringEnabled(): Boolean =
        isAdminAdvancedReviewModeEnabled()

    private fun isQuestionTypeEnabled(questionType: ReviewQuestionType): Boolean {
        if (!isAdminReviewFilteringEnabled()) return true
        return when (questionType) {
            ReviewQuestionType.WORD_TO_DEFINITION -> adminPrefsRepository?.reviewWordToDefinitionEnabled != false
            ReviewQuestionType.DEFINITION_TO_WORD -> adminPrefsRepository?.reviewDefinitionToWordEnabled != false
        }
    }

    private fun isExtraSpellingEnabled(): Boolean =
        !isAdminReviewFilteringEnabled() || adminPrefsRepository?.extraSpellingEnabled != false

    private fun isIntegratedQcmEnabled(): Boolean =
        !isAdminReviewFilteringEnabled() || adminPrefsRepository?.reviewQcmEnabled != false

    private fun isIntegratedMatchingEnabled(): Boolean =
        !isAdminReviewFilteringEnabled() || adminPrefsRepository?.reviewMatchingEnabled != false

    private fun shouldUseForcedAdminEventSession(): Boolean {
        if (!isAdminReviewFilteringEnabled()) return false
        if (adminPrefsRepository?.reviewWordToDefinitionEnabled == true) return false
        if (adminPrefsRepository?.reviewDefinitionToWordEnabled == true) return false

        return isForcedAdminExtraSpellingEnabled() ||
            isForcedAdminQcmEnabled() ||
            isForcedAdminMatchingEnabled() ||
            isForcedAdminSemanticChallengeEnabled() ||
            isForcedAdminSpellingChallengeEnabled()
    }

    private fun buildForcedAdminTestEvents(limit: Int): List<ReviewSessionEvent> {
        val usableCards = allCardsCache.filter { it.recto.isNotBlank() && it.verso.isNotBlank() }
        if (usableCards.isEmpty() || limit <= 0) return emptyList()

        val qcmCapableCards = usableCards.mapNotNull { card ->
            val options = buildQcmOptions(
                targetCard = card,
                questionType = ReviewQuestionType.WORD_TO_DEFINITION,
                correctAnswer = card.verso
            )
            options.takeIf { it.size >= 4 }?.let { card to it }
        }

        val builders = buildList<(Int) -> ReviewSessionEvent?> {
            if (isForcedAdminSemanticChallengeEnabled()) {
                add { index ->
                    val card = usableCards[index % usableCards.size]
                    ReviewSessionEvent(
                        eventId = "admin-semantic-$index-${card.id}",
                        type = ReviewSessionEventType.CHALLENGE,
                        cardId = card.id,
                        correctAnswer = card.verso,
                        challengeKind = ReviewSessionChallengeKind.SEMANTIC,
                        appliesSessionCredit = false
                    )
                }
            }
            if (isForcedAdminSpellingChallengeEnabled()) {
                add { index ->
                    val card = usableCards[index % usableCards.size]
                    ReviewSessionEvent(
                        eventId = "admin-spelling-$index-${card.id}",
                        type = ReviewSessionEventType.CHALLENGE,
                        cardId = card.id,
                        correctAnswer = card.recto,
                        challengeKind = ReviewSessionChallengeKind.SPELLING,
                        appliesSessionCredit = false
                    )
                }
            }
            if (isForcedAdminExtraSpellingEnabled()) {
                add { index ->
                    val card = usableCards[index % usableCards.size]
                    ReviewSessionEvent(
                        eventId = "admin-extra-$index-${card.id}",
                        type = ReviewSessionEventType.EXTRA_SPELLING,
                        cardId = card.id,
                        correctAnswer = card.recto,
                        isSkippable = true,
                        appliesSessionCredit = false
                    )
                }
            }
            if (isForcedAdminQcmEnabled() && qcmCapableCards.isNotEmpty()) {
                add { index ->
                    val (card, options) = qcmCapableCards[index % qcmCapableCards.size]
                    ReviewSessionEvent(
                        eventId = "admin-qcm-$index-${card.id}",
                        type = ReviewSessionEventType.QCM,
                        cardId = card.id,
                        options = options,
                        correctAnswer = card.verso,
                        appliesSessionCredit = false
                    )
                }
            }
            if (isForcedAdminMatchingEnabled() && usableCards.size >= 2) {
                add { index ->
                    val groupSize = usableCards.size.coerceAtMost(limit.coerceIn(2, 5))
                    val matchingCards = buildList {
                        repeat(groupSize) { offset ->
                            add(usableCards[(index + offset) % usableCards.size])
                        }
                    }.distinctBy { it.id }.let { cards ->
                        if (cards.size >= 2) cards else usableCards.take(groupSize)
                    }

                    ReviewSessionEvent(
                        eventId = "admin-matching-$index-${matchingCards.joinToString("-") { it.id }}",
                        type = ReviewSessionEventType.MATCHING,
                        cardIds = matchingCards.map { it.id },
                        options = matchingCards.map { it.verso }.shuffled(random),
                        appliesSessionCredit = false
                    )
                }
            }
        }

        if (builders.isEmpty()) return emptyList()

        return List(limit) { index ->
            builders[index % builders.size].invoke(index)
        }.filterNotNull()
    }

    private fun isForcedAdminSemanticChallengeEnabled(): Boolean =
        isAdminReviewFilteringEnabled() && adminPrefsRepository?.challengeSemanticEnabled == true

    private fun isForcedAdminSpellingChallengeEnabled(): Boolean =
        isAdminReviewFilteringEnabled() && adminPrefsRepository?.challengeOrthoEnabled == true

    private fun isForcedAdminExtraSpellingEnabled(): Boolean =
        isAdminReviewFilteringEnabled() && adminPrefsRepository?.extraSpellingEnabled == true

    private fun isForcedAdminQcmEnabled(): Boolean =
        isAdminReviewFilteringEnabled() && adminPrefsRepository?.reviewQcmEnabled == true

    private fun isForcedAdminMatchingEnabled(): Boolean =
        isAdminReviewFilteringEnabled() && adminPrefsRepository?.reviewMatchingEnabled == true

    private fun shouldDiscardSnapshotForAdminFilters(
        snapshot: ReviewSessionSnapshot,
        effectiveLimit: Int
    ): Boolean {
        val restoredSessionSize = snapshot.state.sessionSizeLimit.takeIf { it > 0 } ?: effectiveLimit
        val snapshotNormalPresentationEnabled = snapshot.state.adminNormalPresentationEnabled == true
        val currentNormalPresentationEnabled = isAdminNormalPresentationEnabled()

        if (restoredSessionSize != effectiveLimit) {
            return true
        }

        if (isAdminReviewModeAvailable() && snapshotNormalPresentationEnabled != currentNormalPresentationEnabled) {
            return true
        }

        if (!isAdminReviewFilteringEnabled()) return false

        val snapshotPlan = snapshot.state.sessionPlan ?: return false

        return snapshotPlan.selectedQuestions.any { !isQuestionTypeEnabled(it.questionType) } ||
            snapshot.state.activeSessionEvent?.let { !isEventEnabledByAdminFilters(it) } == true ||
            snapshot.state.pendingSessionEvents.orEmpty().any { !isEventEnabledByAdminFilters(it) }
    }

    private fun isEventEnabledByAdminFilters(event: ReviewSessionEvent): Boolean = when (event.type) {
        ReviewSessionEventType.QCM -> isIntegratedQcmEnabled()
        ReviewSessionEventType.MATCHING -> isIntegratedMatchingEnabled()
        ReviewSessionEventType.EXTRA_SPELLING -> isExtraSpellingEnabled()
        ReviewSessionEventType.CHALLENGE -> isReplacementChallengeEnabled(event.challengeKind)
    }

    private fun isReplacementChallengeEnabled(challengeKind: ReviewSessionChallengeKind?): Boolean {
        if (challengeKind == null) return false
        if (!isAdminReviewFilteringEnabled()) return true
        return when (challengeKind) {
            ReviewSessionChallengeKind.SPELLING -> adminPrefsRepository?.challengeOrthoEnabled != false
            ReviewSessionChallengeKind.SEMANTIC -> adminPrefsRepository?.challengeSemanticEnabled != false
        }
    }

    private fun buildReplacementChallengeEvent(questionState: ReviewSessionQuestionState): ReviewSessionEvent {
        val card = sessionCardsById.getValue(questionState.progress.cardId)
        val challengeKind = questionState.progress.pendingReplacementChallengeKind
            ?: questionState.progress.questionType.toReplacementChallengeKind()
        return ReviewSessionEvent(
            eventId = "replacement-${questionState.progress.questionId}",
            type = ReviewSessionEventType.CHALLENGE,
            questionId = questionState.progress.questionId,
            cardId = questionState.progress.cardId,
            correctAnswer = when (challengeKind) {
                ReviewSessionChallengeKind.SPELLING -> card.recto
                ReviewSessionChallengeKind.SEMANTIC -> card.verso
            },
            challengeKind = challengeKind,
            appliesSessionCredit = true
        )
    }

    override fun onCleared() {
        super.onCleared()
        ttsService?.shutdown()
    }

    companion object {
        private const val DEFAULT_SESSION_SIZE = 10
    }
}

private data class QuadrupleUi(
    val title: String,
    val instruction: String,
    val canSkip: Boolean,
    val challengeKind: ReviewSessionChallengeKind?
)

private fun ReviewQuestionType.toPresentationMode(): ReviewPresentationMode = when (this) {
    ReviewQuestionType.WORD_TO_DEFINITION -> ReviewPresentationMode.WORD_TO_DEFINITION
    ReviewQuestionType.DEFINITION_TO_WORD -> ReviewPresentationMode.DEFINITION_TO_WORD
}

private fun ReviewQuestionType.toReplacementChallengeKind(): ReviewSessionChallengeKind = when (this) {
    ReviewQuestionType.WORD_TO_DEFINITION -> ReviewSessionChallengeKind.SEMANTIC
    ReviewQuestionType.DEFINITION_TO_WORD -> ReviewSessionChallengeKind.SPELLING
}

private fun Int.toReviewAnswer(): ReviewAnswer = when {
    this >= 5 -> ReviewAnswer.TOO_EASY
    this >= 3 -> ReviewAnswer.GOT_IT
    else -> ReviewAnswer.AGAIN
}

private fun visibleFrontText(card: Flashcard, mode: ReviewPresentationMode): String = when (mode) {
    ReviewPresentationMode.WORD_TO_DEFINITION,
    ReviewPresentationMode.SPELLING_CHALLENGE -> card.recto
    ReviewPresentationMode.DEFINITION_TO_WORD,
    ReviewPresentationMode.SEMANTIC_CHALLENGE -> card.verso
}

private fun visibleAnswerText(card: Flashcard, mode: ReviewPresentationMode): String = when (mode) {
    ReviewPresentationMode.WORD_TO_DEFINITION,
    ReviewPresentationMode.SPELLING_CHALLENGE -> card.verso
    ReviewPresentationMode.DEFINITION_TO_WORD,
    ReviewPresentationMode.SEMANTIC_CHALLENGE -> card.recto
}

private fun Flashcard.toReviewQuestionProgress(questionType: ReviewQuestionType): ReviewQuestionProgress {
    val sm2 = when (questionType) {
        ReviewQuestionType.WORD_TO_DEFINITION -> sm2MotVersDef
        ReviewQuestionType.DEFINITION_TO_WORD -> sm2DefVersMot
    }
    val intervalIndex = intervalDaysToReviewIntervalIndex(sm2.interval)
    val startedAt = if (sm2.totalReviews > 0 || sm2.repetitions > 0) {
        sm2.lastReviewDate ?: dateAjout
    } else {
        null
    }

    return ReviewQuestionProgress(
        questionId = questionType.questionId(id),
        cardId = id,
        questionType = questionType,
        globalOrder = (dateAjout * 2L) + questionType.globalOrderOffset,
        level = intervalIndex.toDouble(),
        intervalIndex = intervalIndex,
        peakIntervalIndex = intervalIndex,
        weightedSuccess = sm2.correctReviews.toDouble(),
        weightedFailure = sm2.lapses.toDouble(),
        recentStreak = sm2.repetitions,
        recoveryReserve = 0.0,
        currentIntervalDurationMs = inferReviewIntervalDurationMs(
            storedIntervalDays = sm2.interval,
            lastReview = sm2.lastReviewDate,
            nextReview = sm2.nextReviewDate,
            intervalIndex = intervalIndex
        ),
        nextDueAt = sm2.nextReviewDate,
        lastSessionFirstAnswerAt = sm2.lastReviewDate,
        lastAskedAt = sm2.lastReviewDate,
        firstAnsweredAt = startedAt
    )
}

private fun inferReviewIntervalDurationMs(
    storedIntervalDays: Int,
    lastReview: Long?,
    nextReview: Long,
    intervalIndex: Int
): Long {
    val legacyDurationMs = if (lastReview != null && nextReview > lastReview) {
        nextReview - lastReview
    } else {
        0L
    }

    if (legacyDurationMs > 0L) {
        return legacyDurationMs
    }

    return if (storedIntervalDays > 0) {
        storedIntervalDays * ONE_DAY_MS
    } else {
        ReviewIntervalEngine.durationForIntervalIndex(intervalIndex)
    }
}

private fun intervalDaysToReviewIntervalIndex(intervalDays: Int): Int = when {
    intervalDays <= 0 -> 0
    intervalDays < 7 -> 2
    intervalDays < 30 -> 3
    intervalDays < 90 -> 4
    intervalDays < 180 -> 5
    intervalDays < 365 -> 6
    intervalDays < 730 -> 7
    else -> 8 + max(0, kotlin.math.floor(kotlin.math.ln(intervalDays.toDouble() / 730.0) / kotlin.math.ln(2.0)).toInt())
}

private const val ONE_DAY_MS = 24L * 60L * 60L * 1000L

enum class ReviewPresentationMode {
    WORD_TO_DEFINITION,
    DEFINITION_TO_WORD,
    SPELLING_CHALLENGE,
    SEMANTIC_CHALLENGE
}

enum class ReviewCurrentItemType {
    NORMAL_QUESTION,
    QCM,
    MATCHING,
    EXTRA_SPELLING,
    CHALLENGE
}

data class ReviewUiState(
    val currentCard: Flashcard? = null,
    val currentItemType: ReviewCurrentItemType = ReviewCurrentItemType.NORMAL_QUESTION,
    val isAnswerRevealed: Boolean = false,
    val normalQuestionInstanceKey: String = "",
    val ttsStatusMessage: String? = null,
    val remainingQuestionsToValidate: Int = 0,
    val isLoading: Boolean = false,
    val isSpeaking: Boolean = false,
    val ttsReady: Boolean = false,
    val isSessionFinished: Boolean = false,
    val studiedCount: Int = 0,
    val totalInSession: Int = 0,
    val sessionProgress: Float = 0f,
    val autoSpeakWord: Boolean = false,
    val autoSpeakDefinition: Boolean = false,
    val presentationMode: ReviewPresentationMode = ReviewPresentationMode.WORD_TO_DEFINITION,
    val xpBonusAccumulated: Int = 0,
    val canUndo: Boolean = false,
    val showSessionCelebration: Boolean = false,
    val sessionCompletionXp: Int = 0,
    val sessionCompletionToken: Long = 0L,
    val eventTitle: String = "",
    val eventInstruction: String = "",
    val eventOptions: List<String> = emptyList(),
    val selectedChoice: String? = null,
    val eventInput: String = "",
    val eventResultSuccessful: Boolean? = null,
    val eventResultMessage: String? = null,
    val eventResultCorrectAnswer: String? = null,
    val eventCards: List<Flashcard> = emptyList(),
    val matchingAssignments: Map<String, String> = emptyMap(),
    val matchingSelectedWordId: String? = null,
    val matchingSelectedDefinition: String? = null,
    val canSkipCurrentEvent: Boolean = false,
    val activeChallengeKind: ReviewSessionChallengeKind? = null
)

class ReviewViewModelFactory(
    private val repository: FlashcardRepository,
    private val sm2Algorithm: Sm2Algorithm = Sm2Algorithm,
    private val dailyStatDao: DailyReviewStatDao? = null,
    private val context: android.content.Context? = null,
    private val userPrefsRepository: UserPrefsRepository? = null,
    private val adminPrefsRepository: AdminPrefsRepository? = null,
    private val reviewSessionSnapshotRepository: ReviewSessionSnapshotRepository? = null,
    private val isAdminUserProvider: (() -> Boolean)? = null,
    private val onSessionXpAwarded: (Int) -> Unit = {}
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(
                repository = repository,
                sm2Algorithm = sm2Algorithm,
                dailyStatDao = dailyStatDao,
                context = context,
                userPrefsRepository = userPrefsRepository,
                adminPrefsRepository = adminPrefsRepository,
                reviewSessionSnapshotRepository = reviewSessionSnapshotRepository,
                isAdminUserProvider = isAdminUserProvider,
                onSessionXpAwarded = onSessionXpAwarded
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
