package com.example.lexicaandroid2.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewCardProgressSummaryTest {

    private val now = 1_000_000L

    @Test
    fun fromProgressMarksCardToWorkWhenOneQuestionIsDue() {
        val summary = ReviewCardProgressSummary.fromProgress(
            cardId = "card-1",
            progress = listOf(
                question(
                    cardId = "card-1",
                    type = ReviewQuestionType.WORD_TO_DEFINITION,
                    intervalIndex = 4,
                    nextDueAt = now + 60_000L,
                    firstAnsweredAt = now - 120_000L
                ),
                question(
                    cardId = "card-1",
                    type = ReviewQuestionType.DEFINITION_TO_WORD,
                    intervalIndex = 2,
                    nextDueAt = now - 1L,
                    firstAnsweredAt = now - 120_000L
                )
            ),
            now = now
        )

        assertEquals(ReviewCardAggregateState.TO_WORK, summary.aggregateState)
    }

    @Test
    fun fromProgressMarksCardKnownOnlyWhenBothQuestionsReachedT4AndAreNotDue() {
        val summary = ReviewCardProgressSummary.fromProgress(
            cardId = "card-2",
            progress = listOf(
                question(
                    cardId = "card-2",
                    type = ReviewQuestionType.WORD_TO_DEFINITION,
                    intervalIndex = 4,
                    nextDueAt = now + 60_000L,
                    firstAnsweredAt = now - 120_000L
                ),
                question(
                    cardId = "card-2",
                    type = ReviewQuestionType.DEFINITION_TO_WORD,
                    intervalIndex = 5,
                    nextDueAt = now + 60_000L,
                    firstAnsweredAt = now - 120_000L
                )
            ),
            now = now
        )

        assertEquals(ReviewCardAggregateState.KNOWN, summary.aggregateState)
    }

    @Test
    fun fromProgressMarksCardInProgressWhenNothingIsDueButMasteryIsIncomplete() {
        val summary = ReviewCardProgressSummary.fromProgress(
            cardId = "card-3",
            progress = listOf(
                question(
                    cardId = "card-3",
                    type = ReviewQuestionType.WORD_TO_DEFINITION,
                    intervalIndex = 3,
                    nextDueAt = now + 60_000L,
                    firstAnsweredAt = now - 120_000L
                ),
                question(
                    cardId = "card-3",
                    type = ReviewQuestionType.DEFINITION_TO_WORD,
                    intervalIndex = 2,
                    nextDueAt = now + 60_000L,
                    firstAnsweredAt = now - 120_000L
                )
            ),
            now = now
        )

        assertEquals(ReviewCardAggregateState.IN_PROGRESS, summary.aggregateState)
    }

    @Test
    fun matchesFilterAcceptsLegacyAndNewKeys() {
        val summary = ReviewCardProgressSummary(
            cardId = "card-4",
            aggregateState = ReviewCardAggregateState.TO_WORK,
            wordToDefinitionState = ReviewCardAggregateState.TO_WORK,
            definitionToWordState = ReviewCardAggregateState.IN_PROGRESS
        )

        assertTrue(summary.matchesFilter("TO_WORK"))
        assertTrue(summary.matchesFilter("TO_LEARN"))
    }

    private fun question(
        cardId: String,
        type: ReviewQuestionType,
        intervalIndex: Int,
        nextDueAt: Long,
        firstAnsweredAt: Long?
    ) = ReviewQuestionProgress(
        questionId = type.questionId(cardId),
        cardId = cardId,
        questionType = type,
        globalOrder = if (type == ReviewQuestionType.WORD_TO_DEFINITION) 0 else 1,
        intervalIndex = intervalIndex,
        nextDueAt = nextDueAt,
        firstAnsweredAt = firstAnsweredAt
    )
}

