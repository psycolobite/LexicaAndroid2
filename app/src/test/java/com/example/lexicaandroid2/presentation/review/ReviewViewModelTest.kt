package com.example.lexicaandroid2.presentation.review

import com.example.lexicaandroid2.domain.logic.Sm2Algorithm
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.Sm2Stats
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStat
import com.example.lexicaandroid2.presentation.review.challenge.ChallengeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ReviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FlashcardRepository
    private lateinit var dailyStatDao: DailyReviewStatDao
    private lateinit var viewModel: ReviewViewModel

    // 0 correct reviews — grading with quality>=3 gives correctReviews=1 → no challenge
    private val cardFresh = Flashcard(
        id = "1",
        recto = "lacune",
        verso = "Manque important dans un ensemble",
        sm2MotVersDef = Sm2Stats(correctReviews = 0),
        sm2DefVersMot = Sm2Stats(correctReviews = 0)
    )

    // 2 correct reviews — grading with quality>=3 gives correctReviews=3 → 3%3==0 → challenge
    private val cardReadyForChallenge = Flashcard(
        id = "2",
        recto = "ephemere",
        verso = "Qui dure tres peu de temps",
        sm2MotVersDef = Sm2Stats(correctReviews = 2),
        sm2DefVersMot = Sm2Stats(correctReviews = 2)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        dailyStatDao = mock()
        viewModel = ReviewViewModel(repository, Sm2Algorithm, dailyStatDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region loadSession

    @Test
    fun loadSessionPopulatesCurrentCardAndCount() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardFresh))
        viewModel.loadSession()
        assertEquals(cardFresh, viewModel.uiState.value.currentCard)
        assertEquals(1, viewModel.uiState.value.totalInSession)
        assertFalse(viewModel.uiState.value.isSessionFinished)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadSessionEmptyListFinishesSession() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(emptyList())
        viewModel.loadSession()
        assertNull(viewModel.uiState.value.currentCard)
        assertTrue(viewModel.uiState.value.isSessionFinished)
    }

    // endregion

    // region revealAnswer

    @Test
    fun revealAnswerSetsIsAnswerRevealedTrue() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardFresh))
        viewModel.loadSession()
        viewModel.revealAnswer()
        assertTrue(viewModel.uiState.value.isAnswerRevealed)
    }

    // endregion

    // region gradeCard

    @Test
    fun gradeCardCallsUpdateCardProgressInRepository() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardFresh))
        viewModel.loadSession()
        viewModel.gradeCard(4)
        verify(repository).updateCardProgress(eq("1"), any(), any())
    }

    @Test
    fun gradeCardLowQualityDoesNotTriggerChallenge() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardReadyForChallenge))
        viewModel.loadSession()
        viewModel.gradeCard(2) // quality < 3 → never triggers challenge
        assertNull(viewModel.uiState.value.activeChallengeType)
    }

    @Test
    fun gradeCardTriggersChallengeWhenCorrectReviewsReachMultipleOf3() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardReadyForChallenge))
        viewModel.loadSession()
        viewModel.gradeCard(4) // correctReviews: 2 → 3, 3%3==0 → challenge triggered
        assertNotNull(viewModel.uiState.value.activeChallengeType)
    }

    @Test
    fun gradeCardFirstFaceIsMotSoChallengIsSpelling() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardReadyForChallenge))
        viewModel.loadSession() // initial face = mot (recto)
        viewModel.gradeCard(4)
        assertEquals(ChallengeType.SPELLING, viewModel.uiState.value.activeChallengeType)
    }

    @Test
    fun gradeCardNoChallengeWhenCorrectReviewsNotMultipleOf3() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardFresh))
        viewModel.loadSession()
        viewModel.gradeCard(4) // correctReviews: 0 → 1, not multiple of 3
        assertNull(viewModel.uiState.value.activeChallengeType)
    }

    @Test
    fun gradeCardAdvancesToNextCardWhenNoChallengeTriggered() = runTest {
        val secondCard = cardFresh.copy(id = "3", recto = "apogee")
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardFresh, secondCard))
        viewModel.loadSession()
        viewModel.gradeCard(4)
        assertEquals(secondCard, viewModel.uiState.value.currentCard)
        assertEquals(1, viewModel.uiState.value.studiedCount)
    }

    @Test
    fun gradeCardUpdatesStudiedCountWhenNoChallengeTriggered() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardFresh))
        viewModel.loadSession()
        viewModel.gradeCard(4)
        assertEquals(1, viewModel.uiState.value.studiedCount)
    }

    // endregion

    // region challenge input & validation

    @Test
    fun onChallengeInputChangedUpdatesChallengeInput() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardReadyForChallenge))
        viewModel.loadSession()
        viewModel.gradeCard(4)
        viewModel.onChallengeInputChanged("ephemere")
        assertEquals("ephemere", viewModel.uiState.value.challengeInput)
    }

    @Test
    fun validateChallengeSetsResultInState() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardReadyForChallenge))
        viewModel.loadSession()
        viewModel.gradeCard(4) // SPELLING challenge
        viewModel.onChallengeInputChanged("ephemere")
        viewModel.validateChallenge()
        assertNotNull(viewModel.uiState.value.challengeResult)
    }

    @Test
    fun validateChallengeSpellingCorrectAnswerIsValid() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardReadyForChallenge))
        viewModel.loadSession()
        viewModel.gradeCard(4) // SPELLING challenge — recto = "ephemere"
        viewModel.onChallengeInputChanged("ephemere")
        viewModel.validateChallenge()
        assertTrue(viewModel.uiState.value.challengeResult?.isValid == true)
    }

    @Test
    fun validateChallengeSpellingWrongAnswerIsInvalid() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardReadyForChallenge))
        viewModel.loadSession()
        viewModel.gradeCard(4) // SPELLING challenge — recto = "ephemere"
        viewModel.onChallengeInputChanged("ephamare")
        viewModel.validateChallenge()
        assertFalse(viewModel.uiState.value.challengeResult?.isValid == true)
    }

    // endregion

    // region dismissChallenge

    @Test
    fun dismissChallengeClearsActiveChallengeType() = runTest {
        val secondCard = cardFresh.copy(id = "3")
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardReadyForChallenge, secondCard))
        viewModel.loadSession()
        viewModel.gradeCard(4)
        viewModel.dismissChallenge()
        assertNull(viewModel.uiState.value.activeChallengeType)
    }

    @Test
    fun dismissChallengeClearsChallengeResultAndInput() = runTest {
        val secondCard = cardFresh.copy(id = "3")
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardReadyForChallenge, secondCard))
        viewModel.loadSession()
        viewModel.gradeCard(4)
        viewModel.onChallengeInputChanged("test")
        viewModel.validateChallenge()
        viewModel.dismissChallenge()
        assertNull(viewModel.uiState.value.challengeResult)
        assertEquals("", viewModel.uiState.value.challengeInput)
    }

    @Test
    fun dismissChallengeAdvancesToNextCard() = runTest {
        val secondCard = cardFresh.copy(id = "3", recto = "apogee")
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardReadyForChallenge, secondCard))
        viewModel.loadSession()
        viewModel.gradeCard(4)
        viewModel.dismissChallenge()
        assertEquals(secondCard, viewModel.uiState.value.currentCard)
    }

    @Test
    fun dismissChallengeAccumulatesXpBonus() = runTest {
        val secondCard = cardFresh.copy(id = "3")
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardReadyForChallenge, secondCard))
        viewModel.loadSession()
        viewModel.gradeCard(4)
        viewModel.onChallengeInputChanged("ephemere")
        viewModel.validateChallenge()
        val xpBefore = viewModel.uiState.value.xpBonusAccumulated
        val bonus = viewModel.uiState.value.challengeResult?.xpBonus ?: 0
        viewModel.dismissChallenge()
        assertEquals(xpBefore + bonus, viewModel.uiState.value.xpBonusAccumulated)
    }

    // endregion

    // region toggleFavorite / deleteCurrentCard

    @Test
    fun toggleFavoriteTogglesCardFavoriAndCallsRepository() = runTest {
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardFresh))
        viewModel.loadSession()
        viewModel.toggleFavorite()
        assertTrue(viewModel.uiState.value.currentCard?.favori == true)
        verify(repository).setFavorite("1", true)
    }

    @Test
    fun deleteCurrentCardRemovesCardAndCallsRepository() = runTest {
        val secondCard = cardFresh.copy(id = "3", recto = "apogee")
        whenever(repository.getCardsToReview(any())).thenReturn(listOf(cardFresh, secondCard))
        viewModel.loadSession()
        viewModel.deleteCurrentCard()
        verify(repository).deleteCard("1")
        assertEquals(secondCard, viewModel.uiState.value.currentCard)
    }

    // endregion
}
