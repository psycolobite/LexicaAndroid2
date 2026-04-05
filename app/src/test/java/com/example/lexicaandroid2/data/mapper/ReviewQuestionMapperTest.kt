package com.example.lexicaandroid2.data.mapper

import com.example.lexicaandroid2.data.local.FlashcardEntity
import com.example.lexicaandroid2.data.local.ReviewQuestionProgressEntity
import com.example.lexicaandroid2.data.local.Sm2DataEmbedded
import com.example.lexicaandroid2.domain.model.ReviewSessionChallengeKind
import com.example.lexicaandroid2.domain.model.ReviewQuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewQuestionMapperTest {
    @Test
    fun flashcardEntityCreatesTwoQuestionProgressRows() {
        val entity = FlashcardEntity(
            id = "card-1",
            mot = "abnégation",
            definition = "Action de se sacrifier volontairement.",
            dateAjout = 1_700_000_000_000L,
            sm2MotVersDef = Sm2DataEmbedded(
                interval = 3,
                repetitions = 2,
                nextReview = 1_700_259_200_000L,
                lastReview = 1_700_000_000_000L,
                totalReviews = 2,
                correctReviews = 2,
                lapses = 0
            ),
            sm2DefVersMot = Sm2DataEmbedded()
        )

        val progress = entity.toReviewQuestionProgressEntities()

        assertEquals(2, progress.size)
        assertEquals("card-1::WORD_TO_DEFINITION", progress[0].questionId)
        assertEquals("card-1::DEFINITION_TO_WORD", progress[1].questionId)
        assertEquals(ReviewQuestionType.WORD_TO_DEFINITION.name, progress[0].questionType)
        assertEquals(ReviewQuestionType.DEFINITION_TO_WORD.name, progress[1].questionType)
        assertEquals(entity.dateAjout * 2, progress[0].globalOrder)
        assertEquals((entity.dateAjout * 2) + 1, progress[1].globalOrder)
        assertEquals(259_200_000L, progress[0].currentIntervalDurationMs)
        assertEquals(entity.dateAjout, progress[0].firstAnsweredAt)
        assertNull(progress[1].firstAnsweredAt)
    }

    @Test
    fun reviewQuestionProgressRoundTripPreservesFields() {
        val entity = ReviewQuestionProgressEntity(
            questionId = "card-7::DEFINITION_TO_WORD",
            cardId = "card-7",
            questionType = ReviewQuestionType.DEFINITION_TO_WORD.name,
            globalOrder = 77L,
            level = 4.0,
            intervalIndex = 4,
            peakIntervalIndex = 5,
            weightedSuccess = 6.0,
            weightedFailure = 1.0,
            recentStreak = 3,
            recoveryReserve = 0.25,
            currentIntervalDurationMs = 86_400_000L,
            nextDueAt = 2_000_000L,
            lastSessionFirstAnswerAt = 1_900_000L,
            lastAskedAt = 1_950_000L,
            firstAnsweredAt = 1_000_000L,
            pendingReplacementChallengeKind = ReviewSessionChallengeKind.SPELLING.name
        )

        val domain = entity.toDomain()
        val roundTrip = domain.toEntity()

        assertEquals(entity, roundTrip)
        assertEquals(ReviewQuestionType.DEFINITION_TO_WORD, domain.questionType)
        assertEquals(ReviewSessionChallengeKind.SPELLING, domain.pendingReplacementChallengeKind)
        assertTrue(domain.isStarted)
    }
}

