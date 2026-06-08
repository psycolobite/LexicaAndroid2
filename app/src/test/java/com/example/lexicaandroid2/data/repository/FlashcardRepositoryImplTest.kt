package com.example.lexicaandroid2.data.repository

import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.FlashcardEntity
import com.example.lexicaandroid2.data.local.ReviewAnswerSyncEventDao
import com.example.lexicaandroid2.data.local.ReviewAnswerSyncEventEntity
import com.example.lexicaandroid2.data.local.ReviewQuestionDao
import com.example.lexicaandroid2.data.local.ReviewQuestionProgressEntity
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewAnswer
import com.example.lexicaandroid2.domain.model.ReviewAnswerSyncEvent
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.model.ReviewQuestionType
import com.example.lexicaandroid2.domain.model.Sm2Stats
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FlashcardRepositoryImplTest {
    private val flashcardDao: FlashcardDao = mock()
    private val reviewQuestionDao: ReviewQuestionDao = mock()
    private val reviewAnswerSyncEventDao: ReviewAnswerSyncEventDao = mock()
    private val repository = FlashcardRepositoryImpl(flashcardDao, reviewQuestionDao)
    private val compactingRepository = FlashcardRepositoryImpl(
        flashcardDao,
        reviewQuestionDao,
        reviewAnswerSyncEventDao = reviewAnswerSyncEventDao
    )

    @Test
    fun saveCardPersistsLegacyCardAndTwoQuestionRows() = runTest {
        val card = Flashcard(
            id = "card-1",
            recto = "lucide",
            verso = "Qui voit clairement.",
            dateAjout = 1_700_000_000_000L
        )

        repository.saveCard(card)

        verify(flashcardDao).insert(any())
        verify(reviewQuestionDao).insertAll(
            argThat {
                size == 2 &&
                    any { it.questionId == "card-1::WORD_TO_DEFINITION" } &&
                    any { it.questionId == "card-1::DEFINITION_TO_WORD" }
            }
        )
    }

    @Test
    fun updateCardProgressRefreshesTwoQuestionRows() = runTest {
        whenever(flashcardDao.getById("card-1")).thenReturn(
            FlashcardEntity(
                id = "card-1",
                mot = "lucide",
                definition = "Qui voit clairement.",
                dateAjout = 1_700_000_000_000L
            )
        )

        repository.updateCardProgress(
            cardId = "card-1",
            motVersDef = Sm2Stats(interval = 5, repetitions = 2, correctReviews = 2),
            defVersMot = Sm2Stats(interval = 0, repetitions = 0, correctReviews = 0)
        )

        verify(flashcardDao).update(any())
        verify(reviewQuestionDao).insertAll(
            argThat { size == 2 && any { it.cardId == "card-1" && it.intervalIndex >= 0 } }
        )
    }

    @Test
    fun getQuestionProgressMapsEntityToDomain() = runTest {
        whenever(reviewQuestionDao.getByQuestionId("card-2::WORD_TO_DEFINITION")).thenReturn(
            ReviewQuestionProgressEntity(
                questionId = "card-2::WORD_TO_DEFINITION",
                cardId = "card-2",
                questionType = ReviewQuestionType.WORD_TO_DEFINITION.name,
                globalOrder = 12L,
                currentIntervalDurationMs = 600_000L,
                nextDueAt = 123_456L
            )
        )

        val result = repository.getQuestionProgress("card-2::WORD_TO_DEFINITION")

        assertEquals("card-2", result?.cardId)
        assertEquals(ReviewQuestionType.WORD_TO_DEFINITION, result?.questionType)
    }

    @Test
    fun saveQuestionProgressDelegatesToDao() = runTest {
        val progress = ReviewQuestionProgress(
            questionId = "card-3::DEFINITION_TO_WORD",
            cardId = "card-3",
            questionType = ReviewQuestionType.DEFINITION_TO_WORD,
            globalOrder = 30L,
            currentIntervalDurationMs = 86_400_000L,
            nextDueAt = 999_999L
        )

        repository.saveQuestionProgress(progress)

        verify(reviewQuestionDao).insert(
            argThat {
                questionId == progress.questionId &&
                    questionType == ReviewQuestionType.DEFINITION_TO_WORD.name
            }
        )
    }

    @Test
    fun deleteCardRemovesQuestionRowsAndLegacyCard() = runTest {
        whenever(flashcardDao.getById("card-4")).thenReturn(
            FlashcardEntity(
                id = "card-4",
                mot = "probe",
                definition = "Qui vérifie avec soin."
            )
        )

        repository.deleteCard("card-4")

        verify(reviewQuestionDao).deleteByCardId("card-4")
        verify(flashcardDao).delete(any())
    }

    @Test
    fun getQuestionProgressReturnsNullWhenMissing() = runTest {
        whenever(reviewQuestionDao.getByQuestionId("missing")).thenReturn(null)

        val result = repository.getQuestionProgress("missing")

        assertNull(result)
    }

    @Test
    fun getAllQuestionProgressMapsList() = runTest {
        whenever(reviewQuestionDao.getAll()).thenReturn(
            listOf(
                ReviewQuestionProgressEntity(
                    questionId = "card-5::WORD_TO_DEFINITION",
                    cardId = "card-5",
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION.name,
                    globalOrder = 1L,
                    currentIntervalDurationMs = 600_000L,
                    nextDueAt = 1_000L
                ),
                ReviewQuestionProgressEntity(
                    questionId = "card-5::DEFINITION_TO_WORD",
                    cardId = "card-5",
                    questionType = ReviewQuestionType.DEFINITION_TO_WORD.name,
                    globalOrder = 2L,
                    currentIntervalDurationMs = 600_000L,
                    nextDueAt = 2_000L
                )
            )
        )

        val result = repository.getAllQuestionProgress()

        assertEquals(2, result.size)
        assertTrue(result.any { it.questionType == ReviewQuestionType.WORD_TO_DEFINITION })
        assertTrue(result.any { it.questionType == ReviewQuestionType.DEFINITION_TO_WORD })
    }

    @Test
    fun getStartedDueQuestionProgressMapsDaoResult() = runTest {
        whenever(reviewQuestionDao.getStartedDueQuestions(123L, 2)).thenReturn(
            listOf(
                ReviewQuestionProgressEntity(
                    questionId = "card-6::WORD_TO_DEFINITION",
                    cardId = "card-6",
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION.name,
                    globalOrder = 3L,
                    currentIntervalDurationMs = 600_000L,
                    nextDueAt = 100L,
                    firstAnsweredAt = 1L
                )
            )
        )

        val result = repository.getStartedDueQuestionProgress(123L, 2)

        assertEquals(1, result.size)
        assertEquals("card-6", result.first().cardId)
    }

    @Test
    fun countQuestionBucketsDelegatesToDao() = runTest {
        whenever(reviewQuestionDao.countStartedDueQuestions(123L)).thenReturn(4)
        whenever(reviewQuestionDao.countNeverStartedQuestions()).thenReturn(7)

        val dueCount = repository.countStartedDueQuestionProgress(123L)
        val newCount = repository.countNeverStartedQuestionProgress()

        assertEquals(4, dueCount)
        assertEquals(7, newCount)
    }

    @Test
    fun appendReviewAnswerSyncEventCompactsOversizedJournal() = runTest {
        val denseHistory = buildList {
            repeat(250) { index ->
                add(
                    ReviewAnswerSyncEventEntity(
                        eventId = "q1-$index",
                        sessionId = "s1",
                        questionId = "q1",
                        cardId = "c1",
                        questionType = ReviewQuestionType.WORD_TO_DEFINITION.name,
                        answer = ReviewAnswer.GOT_IT.name,
                        answeredAt = index.toLong()
                    )
                )
            }
            repeat(151) { index ->
                add(
                    ReviewAnswerSyncEventEntity(
                        eventId = "q2-$index",
                        sessionId = "s2",
                        questionId = "q2",
                        cardId = "c2",
                        questionType = ReviewQuestionType.DEFINITION_TO_WORD.name,
                        answer = ReviewAnswer.TOO_EASY.name,
                        answeredAt = 1_000L + index
                    )
                )
            }
        }
        whenever(reviewAnswerSyncEventDao.getAll()).thenReturn(denseHistory)

        compactingRepository.appendReviewAnswerSyncEvent(
            ReviewAnswerSyncEvent(
                eventId = "ignored-by-mock",
                sessionId = "s3",
                questionId = "q2",
                cardId = "c2",
                questionType = ReviewQuestionType.DEFINITION_TO_WORD,
                answer = ReviewAnswer.GOT_IT,
                answeredAt = 2_000L
            )
        )

        verify(reviewAnswerSyncEventDao).clearAll()
        verify(reviewAnswerSyncEventDao).insertAll(
            argThat { 
                size < denseHistory.size &&
                    any { it.eventId == "q1-249" } &&
                    any { it.eventId == "q2-150" } &&
                    none { it.eventId == "q1-0" }
            }
        )
    }
}

