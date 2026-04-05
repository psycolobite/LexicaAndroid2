package com.example.lexicaandroid2.presentation.review

import com.example.lexicaandroid2.domain.logic.ReviewIntervalEngine
import com.example.lexicaandroid2.domain.logic.ReviewSessionEngine
import com.example.lexicaandroid2.domain.logic.Sm2Algorithm
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.model.ReviewQuestionType
import com.example.lexicaandroid2.domain.model.ReviewSessionChallengeKind
import com.example.lexicaandroid2.domain.model.ReviewSessionEvent
import com.example.lexicaandroid2.domain.model.ReviewSessionEventType
import com.example.lexicaandroid2.domain.model.ReviewSessionPlan
import com.example.lexicaandroid2.domain.model.ReviewSessionSnapshot
import com.example.lexicaandroid2.domain.model.ReviewSessionSnapshotState
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.domain.repository.ReviewSessionSnapshotRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.presentation.admin.AdminPrefsRepository
import com.example.lexicaandroid2.presentation.settings.UserPrefsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ReviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FlashcardRepository
    private lateinit var snapshotRepository: ReviewSessionSnapshotRepository
    private lateinit var dailyStatDao: DailyReviewStatDao
    private lateinit var viewModel: ReviewViewModel

    private val cardWord = Flashcard(
        id = "1",
        recto = "lacune",
        verso = "Manque important dans un ensemble"
    )
    private val cardDef = Flashcard(
        id = "2",
        recto = "éphémère",
        verso = "Qui dure très peu de temps"
    )
    private val cardDef2 = Flashcard(
        id = "3",
        recto = "turpitude",
        verso = "Faute morale grave"
    )
    private val cardDef3 = Flashcard(
        id = "4",
        recto = "sagace",
        verso = "Qui comprend vite"
    )

    private val questionWord = question(cardWord.id, ReviewQuestionType.WORD_TO_DEFINITION, 0)
    private val questionDef = question(cardDef.id, ReviewQuestionType.DEFINITION_TO_WORD, 1)
    private val questionDef2 = question(cardDef2.id, ReviewQuestionType.DEFINITION_TO_WORD, 2)
    private val questionDef3 = question(cardDef3.id, ReviewQuestionType.DEFINITION_TO_WORD, 3)
    private val fullQuestionCoverage = listOf(
        question(cardWord.id, ReviewQuestionType.WORD_TO_DEFINITION, 0, firstAnsweredAt = null),
        question(cardWord.id, ReviewQuestionType.DEFINITION_TO_WORD, 1, firstAnsweredAt = null),
        question(cardDef.id, ReviewQuestionType.WORD_TO_DEFINITION, 2, firstAnsweredAt = null),
        question(cardDef.id, ReviewQuestionType.DEFINITION_TO_WORD, 3, firstAnsweredAt = null),
        question(cardDef2.id, ReviewQuestionType.WORD_TO_DEFINITION, 4, firstAnsweredAt = null),
        question(cardDef2.id, ReviewQuestionType.DEFINITION_TO_WORD, 5, firstAnsweredAt = null),
        question(cardDef3.id, ReviewQuestionType.WORD_TO_DEFINITION, 6, firstAnsweredAt = null),
        question(cardDef3.id, ReviewQuestionType.DEFINITION_TO_WORD, 7, firstAnsweredAt = null)
    )

    @Before
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        snapshotRepository = mock()
        dailyStatDao = mock()
        viewModel = ReviewViewModel(
            repository = repository,
            sm2Algorithm = Sm2Algorithm,
            dailyStatDao = dailyStatDao,
            reviewSessionSnapshotRepository = snapshotRepository
        )

        whenever(snapshotRepository.getActiveSession()).thenReturn(null)
        whenever(repository.countStartedDueQuestionProgress(any())).thenReturn(0)
        whenever(repository.getStartedDueQuestionProgress(any(), any())).thenReturn(emptyList())
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(0)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(emptyList())
        whenever(repository.getAllQuestionProgress()).thenReturn(fullQuestionCoverage)
        whenever(repository.getAllCards()).thenReturn(listOf(cardWord, cardDef, cardDef2, cardDef3))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadSessionWithWordToDefinitionCanStartOnExtraSpellingEvent() = runTest {
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(1)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionWord))

        viewModel.loadSession()

        assertEquals(ReviewCurrentItemType.EXTRA_SPELLING, viewModel.uiState.value.currentItemType)
        assertTrue(viewModel.uiState.value.canSkipCurrentEvent)
        assertEquals(cardWord, viewModel.uiState.value.currentCard)
    }

    @Test
    fun skipExtraSpellingReturnsToNormalQuestion() = runTest {
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(1)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionWord))

        viewModel.loadSession()
        viewModel.skipActiveEvent()
        assertNotNull(viewModel.uiState.value.eventResultMessage)

        viewModel.continueAfterEventResult()

        assertEquals(ReviewCurrentItemType.NORMAL_QUESTION, viewModel.uiState.value.currentItemType)
        assertEquals(cardWord, viewModel.uiState.value.currentCard)
    }

    @Test
    fun thirdAgainSchedulesQcmBeforeNextOccurrence() = runTest {
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(1)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionDef))

        viewModel.loadSession()
        repeat(3) { viewModel.gradeCard(0) }

        assertEquals(ReviewCurrentItemType.QCM, viewModel.uiState.value.currentItemType)
        assertEquals(cardDef, viewModel.uiState.value.currentCard)
        assertEquals(4, viewModel.uiState.value.eventOptions.size)
    }

    @Test
    fun correctQcmAppliesLocalCreditAndReturnsToNormalFlow() = runTest {
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(1)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionDef))

        viewModel.loadSession()
        repeat(3) { viewModel.gradeCard(0) }
        val correctAnswer = cardDef.recto

        viewModel.onChoiceSelected(correctAnswer)
        viewModel.submitActiveEvent()
        assertTrue(viewModel.uiState.value.eventResultSuccessful == true)

        viewModel.continueAfterEventResult()

        assertEquals(ReviewCurrentItemType.NORMAL_QUESTION, viewModel.uiState.value.currentItemType)
        assertEquals(1, viewModel.uiState.value.remainingQuestionsToValidate)
    }

    @Test
    fun tooEasySchedulesImmediateChallenge() = runTest {
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(1)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionDef))

        viewModel.loadSession()
        viewModel.gradeCard(5)

        assertTrue(viewModel.uiState.value.isSessionFinished)
        verify(repository).saveQuestionProgress(
            argThat<List<ReviewQuestionProgress>> {
                size == 1 &&
                    first().questionId == questionDef.questionId &&
                    first().pendingReplacementChallengeKind == ReviewSessionChallengeKind.SPELLING
            }
        )
    }

    @Test
    fun pendingReplacementChallengeStartsNextSessionOnFirstPresentation() = runTest {
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(1)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(
            listOf(
                question(
                    cardId = cardDef.id,
                    type = ReviewQuestionType.DEFINITION_TO_WORD,
                    globalOrder = 1,
                    firstAnsweredAt = 1L,
                    pendingReplacementChallengeKind = ReviewSessionChallengeKind.SPELLING
                )
            )
        )

        viewModel.loadSession()

        assertEquals(ReviewCurrentItemType.CHALLENGE, viewModel.uiState.value.currentItemType)
        assertEquals(ReviewSessionChallengeKind.SPELLING, viewModel.uiState.value.activeChallengeKind)
    }

    @Test
    fun matchingIsScheduledAfterInitialWorkVolume() = runTest {
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(2)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionDef, questionDef2))

        viewModel.loadSession(limit = 2)
        advanceUntilIdle()
        viewModel.gradeCard(0)
        advanceUntilIdle()
        viewModel.gradeCard(0)
        advanceUntilIdle()

        assertEquals(ReviewCurrentItemType.MATCHING, viewModel.uiState.value.currentItemType)
        assertEquals(2, viewModel.uiState.value.eventCards.size)
    }

    @Test
    fun matchingValidationShowsResultAndReturnsToNormalFlow() = runTest {
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(2)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionDef, questionDef2))

        viewModel.loadSession(limit = 2)
        advanceUntilIdle()
        viewModel.gradeCard(0)
        advanceUntilIdle()
        viewModel.gradeCard(0)
        advanceUntilIdle()

        val cards = viewModel.uiState.value.eventCards
        cards.forEach { card ->
            viewModel.onMatchingWordSelected(card.id)
            viewModel.onMatchingDefinitionSelected(card.verso)
        }
        viewModel.submitActiveEvent()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.eventResultMessage)
        viewModel.continueAfterEventResult()
        advanceUntilIdle()

        assertEquals(ReviewCurrentItemType.NORMAL_QUESTION, viewModel.uiState.value.currentItemType)
    }

    @Test
    fun adminCanDisableWordToDefinitionQuestionsInReview() = runTest {
        val adminPrefs = mock<AdminPrefsRepository>()
        whenever(adminPrefs.reviewWordToDefinitionEnabled).thenReturn(false)
        whenever(adminPrefs.reviewDefinitionToWordEnabled).thenReturn(true)
        whenever(adminPrefs.extraSpellingEnabled).thenReturn(true)
        whenever(adminPrefs.reviewQcmEnabled).thenReturn(true)
        whenever(adminPrefs.reviewMatchingEnabled).thenReturn(true)
        whenever(adminPrefs.challengeOrthoEnabled).thenReturn(true)
        whenever(adminPrefs.challengeSemanticEnabled).thenReturn(true)

        viewModel = ReviewViewModel(
            repository = repository,
            sm2Algorithm = Sm2Algorithm,
            dailyStatDao = dailyStatDao,
            reviewSessionSnapshotRepository = snapshotRepository,
            adminPrefsRepository = adminPrefs,
            isAdminUserProvider = { true }
        )

        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(2)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionWord, questionDef))

        viewModel.loadSession(limit = 2)
        advanceUntilIdle()

        assertEquals(cardDef, viewModel.uiState.value.currentCard)
        assertEquals(ReviewPresentationMode.DEFINITION_TO_WORD, viewModel.uiState.value.presentationMode)
        assertEquals(1, viewModel.uiState.value.totalInSession)
    }

    @Test
    fun adminCanDisableIntegratedQcmInReview() = runTest {
        val adminPrefs = mock<AdminPrefsRepository>()
        whenever(adminPrefs.reviewWordToDefinitionEnabled).thenReturn(true)
        whenever(adminPrefs.reviewDefinitionToWordEnabled).thenReturn(true)
        whenever(adminPrefs.extraSpellingEnabled).thenReturn(true)
        whenever(adminPrefs.reviewQcmEnabled).thenReturn(false)
        whenever(adminPrefs.reviewMatchingEnabled).thenReturn(true)
        whenever(adminPrefs.challengeOrthoEnabled).thenReturn(true)
        whenever(adminPrefs.challengeSemanticEnabled).thenReturn(true)

        viewModel = ReviewViewModel(
            repository = repository,
            sm2Algorithm = Sm2Algorithm,
            dailyStatDao = dailyStatDao,
            reviewSessionSnapshotRepository = snapshotRepository,
            adminPrefsRepository = adminPrefs,
            isAdminUserProvider = { true }
        )

        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(1)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionDef))

        viewModel.loadSession(limit = 1)
        advanceUntilIdle()
        viewModel.gradeCard(0)
        advanceUntilIdle()
        viewModel.gradeCard(0)
        advanceUntilIdle()
        viewModel.gradeCard(0)
        advanceUntilIdle()

        assertEquals(ReviewCurrentItemType.NORMAL_QUESTION, viewModel.uiState.value.currentItemType)
    }

    @Test
    fun adminCanForceSemanticChallengeWithoutNormalQuestions() = runTest {
        val adminPrefs = mock<AdminPrefsRepository>()
        whenever(adminPrefs.reviewWordToDefinitionEnabled).thenReturn(false)
        whenever(adminPrefs.reviewDefinitionToWordEnabled).thenReturn(false)
        whenever(adminPrefs.extraSpellingEnabled).thenReturn(false)
        whenever(adminPrefs.reviewQcmEnabled).thenReturn(false)
        whenever(adminPrefs.reviewMatchingEnabled).thenReturn(false)
        whenever(adminPrefs.challengeOrthoEnabled).thenReturn(false)
        whenever(adminPrefs.challengeSemanticEnabled).thenReturn(true)

        viewModel = ReviewViewModel(
            repository = repository,
            sm2Algorithm = Sm2Algorithm,
            dailyStatDao = dailyStatDao,
            reviewSessionSnapshotRepository = snapshotRepository,
            adminPrefsRepository = adminPrefs,
            isAdminUserProvider = { true }
        )

        viewModel.loadSession()
        advanceUntilIdle()

        assertEquals(ReviewCurrentItemType.CHALLENGE, viewModel.uiState.value.currentItemType)
        assertEquals(ReviewSessionChallengeKind.SEMANTIC, viewModel.uiState.value.activeChallengeKind)
        assertEquals(cardWord, viewModel.uiState.value.currentCard)
    }

    @Test
    fun adminCanForceMatchingWithoutNormalQuestions() = runTest {
        val adminPrefs = mock<AdminPrefsRepository>()
        whenever(adminPrefs.reviewWordToDefinitionEnabled).thenReturn(false)
        whenever(adminPrefs.reviewDefinitionToWordEnabled).thenReturn(false)
        whenever(adminPrefs.extraSpellingEnabled).thenReturn(false)
        whenever(adminPrefs.reviewQcmEnabled).thenReturn(false)
        whenever(adminPrefs.reviewMatchingEnabled).thenReturn(true)
        whenever(adminPrefs.challengeOrthoEnabled).thenReturn(false)
        whenever(adminPrefs.challengeSemanticEnabled).thenReturn(false)

        viewModel = ReviewViewModel(
            repository = repository,
            sm2Algorithm = Sm2Algorithm,
            dailyStatDao = dailyStatDao,
            reviewSessionSnapshotRepository = snapshotRepository,
            adminPrefsRepository = adminPrefs,
            isAdminUserProvider = { true }
        )

        viewModel.loadSession(limit = 3)
        advanceUntilIdle()

        assertEquals(ReviewCurrentItemType.MATCHING, viewModel.uiState.value.currentItemType)
        assertEquals(3, viewModel.uiState.value.eventCards.size)
    }

    @Test
    fun forcedSemanticChallengeRespectsAdminSessionSize() = runTest {
        val adminPrefs = mock<AdminPrefsRepository>()
        whenever(adminPrefs.reviewWordToDefinitionEnabled).thenReturn(false)
        whenever(adminPrefs.reviewDefinitionToWordEnabled).thenReturn(false)
        whenever(adminPrefs.extraSpellingEnabled).thenReturn(false)
        whenever(adminPrefs.reviewQcmEnabled).thenReturn(false)
        whenever(adminPrefs.reviewMatchingEnabled).thenReturn(false)
        whenever(adminPrefs.challengeOrthoEnabled).thenReturn(false)
        whenever(adminPrefs.challengeSemanticEnabled).thenReturn(true)

        viewModel = ReviewViewModel(
            repository = repository,
            sm2Algorithm = Sm2Algorithm,
            dailyStatDao = dailyStatDao,
            reviewSessionSnapshotRepository = snapshotRepository,
            adminPrefsRepository = adminPrefs,
            isAdminUserProvider = { true }
        )

        viewModel.loadSession(limit = 3)
        advanceUntilIdle()

        repeat(2) {
            assertEquals(ReviewCurrentItemType.CHALLENGE, viewModel.uiState.value.currentItemType)
            viewModel.onEventInputChanged("test admin")
            viewModel.submitActiveEvent()
            advanceUntilIdle()
            viewModel.continueAfterEventResult()
            advanceUntilIdle()
        }

        assertEquals(ReviewCurrentItemType.CHALLENGE, viewModel.uiState.value.currentItemType)
        viewModel.onEventInputChanged("test admin")
        viewModel.submitActiveEvent()
        advanceUntilIdle()
        viewModel.continueAfterEventResult()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSessionFinished)
    }

    @Test
    fun forcedMatchingRespectsAdminSessionSize() = runTest {
        val adminPrefs = mock<AdminPrefsRepository>()
        whenever(adminPrefs.reviewWordToDefinitionEnabled).thenReturn(false)
        whenever(adminPrefs.reviewDefinitionToWordEnabled).thenReturn(false)
        whenever(adminPrefs.extraSpellingEnabled).thenReturn(false)
        whenever(adminPrefs.reviewQcmEnabled).thenReturn(false)
        whenever(adminPrefs.reviewMatchingEnabled).thenReturn(true)
        whenever(adminPrefs.challengeOrthoEnabled).thenReturn(false)
        whenever(adminPrefs.challengeSemanticEnabled).thenReturn(false)

        viewModel = ReviewViewModel(
            repository = repository,
            sm2Algorithm = Sm2Algorithm,
            dailyStatDao = dailyStatDao,
            reviewSessionSnapshotRepository = snapshotRepository,
            adminPrefsRepository = adminPrefs,
            isAdminUserProvider = { true }
        )

        viewModel.loadSession(limit = 2)
        advanceUntilIdle()

        repeat(2) { index ->
            assertEquals(ReviewCurrentItemType.MATCHING, viewModel.uiState.value.currentItemType)
            val currentCards = viewModel.uiState.value.eventCards
            currentCards.forEach { card ->
                viewModel.onMatchingWordSelected(card.id)
                viewModel.onMatchingDefinitionSelected(card.verso)
            }
            viewModel.submitActiveEvent()
            advanceUntilIdle()
            if (index == 0) {
                viewModel.continueAfterEventResult()
                advanceUntilIdle()
            }
        }

        viewModel.continueAfterEventResult()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSessionFinished)
    }

    @Test
    fun adminNormalReviewConfigurationKeepsStandardQuestionFlow() = runTest {
        val adminPrefs = mock<AdminPrefsRepository>()
        whenever(adminPrefs.normalPresentationEnabled).thenReturn(true)
        whenever(adminPrefs.reviewWordToDefinitionEnabled).thenReturn(true)
        whenever(adminPrefs.reviewDefinitionToWordEnabled).thenReturn(true)
        whenever(adminPrefs.extraSpellingEnabled).thenReturn(true)
        whenever(adminPrefs.reviewQcmEnabled).thenReturn(true)
        whenever(adminPrefs.reviewMatchingEnabled).thenReturn(true)
        whenever(adminPrefs.challengeOrthoEnabled).thenReturn(true)
        whenever(adminPrefs.challengeSemanticEnabled).thenReturn(true)

        viewModel = ReviewViewModel(
            repository = repository,
            sm2Algorithm = Sm2Algorithm,
            dailyStatDao = dailyStatDao,
            reviewSessionSnapshotRepository = snapshotRepository,
            adminPrefsRepository = adminPrefs,
            isAdminUserProvider = { true }
        )

        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(2)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionWord, questionDef))

        viewModel.loadSession(limit = 2)
        advanceUntilIdle()

        assertTrue(
            viewModel.uiState.value.currentItemType == ReviewCurrentItemType.NORMAL_QUESTION ||
                viewModel.uiState.value.currentItemType == ReviewCurrentItemType.EXTRA_SPELLING
        )
        assertEquals(2, viewModel.uiState.value.totalInSession)
        assertTrue(viewModel.uiState.value.currentCard in listOf(cardWord, cardDef))
        assertTrue(
            viewModel.uiState.value.presentationMode == ReviewPresentationMode.WORD_TO_DEFINITION ||
                viewModel.uiState.value.presentationMode == ReviewPresentationMode.DEFINITION_TO_WORD
        )
    }

    @Test
    fun sessionProgressAdvancesProportionallyWithGotItAnswers() = runTest {
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(1)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionDef))

        viewModel.loadSession(limit = 1)
        advanceUntilIdle()

        assertEquals(0f, viewModel.uiState.value.sessionProgress, 0.0001f)

        viewModel.gradeCard(4)
        advanceUntilIdle()

        assertEquals(0.5f, viewModel.uiState.value.sessionProgress, 0.0001f)

        viewModel.gradeCard(4)
        advanceUntilIdle()

        assertEquals(1f, viewModel.uiState.value.sessionProgress, 0.0001f)
    }

    @Test
    fun invalidateSessionForSettingsChangeClearsActiveSession() = runTest {
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(1)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionDef))

        viewModel.loadSession(limit = 1)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.totalInSession)

        viewModel.invalidateSessionForSettingsChange()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.totalInSession)
        assertTrue(viewModel.uiState.value.isSessionFinished)
        verify(snapshotRepository, atLeastOnce()).clearActiveSession()
    }

    @Test
    fun invalidateSessionForSettingsChangePersistsValidatedQuestions() = runTest {
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(1)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionDef))

        viewModel.loadSession(limit = 1)
        advanceUntilIdle()
        viewModel.gradeCard(4)
        advanceUntilIdle()
        viewModel.gradeCard(4)
        advanceUntilIdle()

        viewModel.invalidateSessionForSettingsChange()
        advanceUntilIdle()

        verify(repository, atLeastOnce()).saveQuestionProgress(
            argThat<List<ReviewQuestionProgress>> {
                any { it.questionId == questionDef.questionId && it.firstAnsweredAt != null && it.nextDueAt > 0L }
            }
        )
        verify(snapshotRepository, atLeastOnce()).clearActiveSession()
    }

    @Test
    fun loadSessionRepairsMissingQuestionProgressBeforeBuildingPlan() = runTest {
        whenever(repository.getAllQuestionProgress()).thenReturn(emptyList())

        viewModel.loadSession(limit = 2)
        advanceUntilIdle()

        verify(repository).saveQuestionProgress(
            argThat<List<ReviewQuestionProgress>> {
                size == 8 &&
                    any { it.questionId == ReviewQuestionType.WORD_TO_DEFINITION.questionId(cardWord.id) } &&
                    any { it.questionId == ReviewQuestionType.DEFINITION_TO_WORD.questionId(cardDef3.id) }
            }
        )
        assertFalse(viewModel.uiState.value.isSessionFinished)
        assertEquals(2, viewModel.uiState.value.totalInSession)
        assertNotNull(viewModel.uiState.value.currentCard)
    }

    @Test
    fun completingSessionShowsCelebrationAndAwardsSessionXp() = runTest {
        var awardedXp = 0
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(1)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionDef))

        viewModel = ReviewViewModel(
            repository = repository,
            sm2Algorithm = Sm2Algorithm,
            dailyStatDao = dailyStatDao,
            reviewSessionSnapshotRepository = snapshotRepository,
            onSessionXpAwarded = { amount -> awardedXp += amount }
        )

        viewModel.loadSession(limit = 1)
        advanceUntilIdle()
        viewModel.gradeCard(5)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSessionFinished)
        assertTrue(viewModel.uiState.value.showSessionCelebration)
        assertEquals(1, viewModel.uiState.value.sessionCompletionXp)
        assertEquals(1, awardedXp)
    }

    @Test
    fun loadSessionRestoresSnapshotWithActiveEvent() = runTest {
        val plan = ReviewSessionPlan(
            selectedQuestions = listOf(questionDef),
            sessionOrder = listOf(questionDef),
            remainingQuestionsCount = 0
        )
        val state = ReviewSessionEngine.start(plan)
        whenever(snapshotRepository.getActiveSession()).thenReturn(
            ReviewSessionSnapshot(
                createdAt = 1L,
                updatedAt = 2L,
                state = ReviewSessionSnapshotState(
                    sessionCards = listOf(cardDef),
                    sessionPlan = plan,
                    sessionState = state,
                    activeSessionEvent = ReviewSessionEvent(
                        eventId = "qcm-restore",
                        type = ReviewSessionEventType.QCM,
                        questionId = questionDef.questionId,
                        cardId = cardDef.id,
                        options = listOf(cardDef.recto, cardWord.recto, cardDef2.recto, cardDef3.recto),
                        correctAnswer = cardDef.recto
                    ),
                    presentationModeName = ReviewPresentationMode.DEFINITION_TO_WORD.name
                )
            )
        )

        viewModel.loadSession()

        assertEquals(ReviewCurrentItemType.QCM, viewModel.uiState.value.currentItemType)
        assertEquals(cardDef, viewModel.uiState.value.currentCard)
    }

    @Test
    fun loadSessionDiscardsSnapshotWhenAdminQuestionFiltersChanged() = runTest {
        val adminPrefs = mock<AdminPrefsRepository>()
        whenever(adminPrefs.reviewWordToDefinitionEnabled).thenReturn(false)
        whenever(adminPrefs.reviewDefinitionToWordEnabled).thenReturn(true)
        whenever(adminPrefs.extraSpellingEnabled).thenReturn(true)
        whenever(adminPrefs.reviewQcmEnabled).thenReturn(true)
        whenever(adminPrefs.reviewMatchingEnabled).thenReturn(true)
        whenever(adminPrefs.challengeOrthoEnabled).thenReturn(true)
        whenever(adminPrefs.challengeSemanticEnabled).thenReturn(true)
        whenever(adminPrefs.sessionSize).thenReturn(10)

        val incompatiblePlan = ReviewSessionPlan(
            selectedQuestions = listOf(questionWord),
            sessionOrder = listOf(questionWord),
            remainingQuestionsCount = 1
        )

        whenever(snapshotRepository.getActiveSession()).thenReturn(
            ReviewSessionSnapshot(
                createdAt = 10L,
                updatedAt = 11L,
                state = ReviewSessionSnapshotState(
                    sessionCards = listOf(cardWord),
                    sessionPlan = incompatiblePlan,
                    sessionState = ReviewSessionEngine.start(incompatiblePlan),
                    sessionSizeLimit = 10
                )
            )
        )
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(2)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionWord, questionDef))

        viewModel = ReviewViewModel(
            repository = repository,
            sm2Algorithm = Sm2Algorithm,
            dailyStatDao = dailyStatDao,
            reviewSessionSnapshotRepository = snapshotRepository,
            adminPrefsRepository = adminPrefs,
            isAdminUserProvider = { true }
        )

        viewModel.loadSession()
        advanceUntilIdle()

        assertEquals(cardDef, viewModel.uiState.value.currentCard)
        assertEquals(ReviewPresentationMode.DEFINITION_TO_WORD, viewModel.uiState.value.presentationMode)
        assertEquals(1, viewModel.uiState.value.totalInSession)
        verify(snapshotRepository).clearActiveSession()
    }

    @Test
    fun loadSessionDiscardsSnapshotWhenUserSessionSizeChanged() = runTest {
        val userPrefs = mock<UserPrefsRepository>()
        whenever(userPrefs.cardsPerSession).thenReturn(2)

        val snapshotPlan = ReviewSessionPlan(
            selectedQuestions = listOf(questionWord),
            sessionOrder = listOf(questionWord),
            remainingQuestionsCount = 1
        )

        whenever(snapshotRepository.getActiveSession()).thenReturn(
            ReviewSessionSnapshot(
                createdAt = 20L,
                updatedAt = 21L,
                state = ReviewSessionSnapshotState(
                    sessionCards = listOf(cardWord),
                    sessionPlan = snapshotPlan,
                    sessionState = ReviewSessionEngine.start(snapshotPlan),
                    sessionSizeLimit = 10
                )
            )
        )
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(2)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(listOf(questionWord, questionDef))

        viewModel = ReviewViewModel(
            repository = repository,
            sm2Algorithm = Sm2Algorithm,
            dailyStatDao = dailyStatDao,
            userPrefsRepository = userPrefs,
            reviewSessionSnapshotRepository = snapshotRepository
        )

        viewModel.loadSession()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.totalInSession)
        verify(snapshotRepository).clearActiveSession()
    }

    @Test
    fun finishingChallengeThenContinuingCommitsSession() = runTest {
        whenever(repository.countNeverStartedQuestionProgress()).thenReturn(1)
        whenever(repository.getNeverStartedQuestionProgress(any())).thenReturn(
            listOf(
                question(
                    cardId = cardDef.id,
                    type = ReviewQuestionType.DEFINITION_TO_WORD,
                    globalOrder = 1,
                    currentIntervalDurationMs = ReviewIntervalEngine.durationForIntervalIndex(3),
                    firstAnsweredAt = 1L,
                    pendingReplacementChallengeKind = ReviewSessionChallengeKind.SPELLING
                )
            )
        )

        viewModel.loadSession()
        viewModel.onEventInputChanged(cardDef.recto)
        viewModel.submitActiveEvent()
        viewModel.continueAfterEventResult()

        assertTrue(viewModel.uiState.value.isSessionFinished)
        verify(repository).saveQuestionProgress(
            argThat<List<ReviewQuestionProgress>> {
                size == 1 &&
                    first().questionId == questionDef.questionId &&
                    first().pendingReplacementChallengeKind == null
            }
        )
        verify(snapshotRepository, atLeastOnce()).clearActiveSession()
    }

    private fun question(
        cardId: String,
        type: ReviewQuestionType,
        globalOrder: Long,
        currentIntervalDurationMs: Long = ReviewIntervalEngine.durationForIntervalIndex(0),
        firstAnsweredAt: Long? = null,
        pendingReplacementChallengeKind: ReviewSessionChallengeKind? = null
    ): ReviewQuestionProgress = ReviewQuestionProgress(
        questionId = type.questionId(cardId),
        cardId = cardId,
        questionType = type,
        globalOrder = globalOrder,
        level = 0.0,
        intervalIndex = 0,
        peakIntervalIndex = 0,
        weightedSuccess = 0.0,
        weightedFailure = 0.0,
        recentStreak = 0,
        recoveryReserve = 0.0,
        currentIntervalDurationMs = currentIntervalDurationMs,
        nextDueAt = 0L,
        lastSessionFirstAnswerAt = null,
        lastAskedAt = null,
        firstAnsweredAt = firstAnsweredAt,
        pendingReplacementChallengeKind = pendingReplacementChallengeKind
    )
}
