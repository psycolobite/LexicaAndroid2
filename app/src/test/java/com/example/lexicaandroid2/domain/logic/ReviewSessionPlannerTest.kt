package com.example.lexicaandroid2.domain.logic

import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.model.ReviewQuestionType
import com.example.lexicaandroid2.domain.model.Sm2Stats
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlin.random.Random
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewSessionPlannerTest {
    @Test
    fun buildPrioritizedGlobalOrderPutsStartedDueFirstThenNeverStarted() {
        val planner = ReviewSessionPlanner(repository = FakeFlashcardRepository(), random = Random(0))
        val startedDue = listOf(
            question(cardId = "card-b", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 4, nextDueAt = 200L, firstAnsweredAt = 1L),
            question(cardId = "card-a", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0, nextDueAt = 100L, firstAnsweredAt = 1L),
            question(cardId = "card-a", type = ReviewQuestionType.DEFINITION_TO_WORD, globalOrder = 1, nextDueAt = 120L, firstAnsweredAt = 1L),
            question(cardId = "card-b", type = ReviewQuestionType.DEFINITION_TO_WORD, globalOrder = 5, nextDueAt = 220L, firstAnsweredAt = 1L)
        )
        val neverStarted = listOf(
            question(cardId = "card-c", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 6, firstAnsweredAt = null),
            question(cardId = "card-c", type = ReviewQuestionType.DEFINITION_TO_WORD, globalOrder = 7, firstAnsweredAt = null),
            question(cardId = "card-d", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 8, firstAnsweredAt = null)
        )

        val result = planner.buildPrioritizedGlobalOrder(startedDue, neverStarted)

        assertEquals(
            listOf(
                "card-a::WORD_TO_DEFINITION",
                "card-a::DEFINITION_TO_WORD",
                "card-b::WORD_TO_DEFINITION",
                "card-b::DEFINITION_TO_WORD",
                "card-c::WORD_TO_DEFINITION",
                "card-c::DEFINITION_TO_WORD",
                "card-d::WORD_TO_DEFINITION"
            ),
            result.map { it.questionId }
        )
    }

    @Test
    fun buildPlanUsesSessionSizeAndComputesRemainingQuestionsCount() = runTest {
        val startedDue = listOf(
            question(cardId = "card-a", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0, nextDueAt = 100L, firstAnsweredAt = 1L),
            question(cardId = "card-a", type = ReviewQuestionType.DEFINITION_TO_WORD, globalOrder = 1, nextDueAt = 110L, firstAnsweredAt = 1L),
            question(cardId = "card-b", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 2, nextDueAt = 120L, firstAnsweredAt = 1L)
        )
        val neverStarted = listOf(
            question(cardId = "card-c", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 4, firstAnsweredAt = null),
            question(cardId = "card-c", type = ReviewQuestionType.DEFINITION_TO_WORD, globalOrder = 5, firstAnsweredAt = null)
        )
        val repository = FakeFlashcardRepository(startedDue, neverStarted)
        val planner = ReviewSessionPlanner(repository, Random(1))

        val plan = planner.buildPlan(sessionSize = 4, nowMs = 500L)

        assertEquals(4, plan.selectedQuestions.size)
        assertEquals(1, plan.remainingQuestionsCount)
        assertEquals(
            listOf(
                "card-a::WORD_TO_DEFINITION",
                "card-a::DEFINITION_TO_WORD",
                "card-b::WORD_TO_DEFINITION",
                "card-c::WORD_TO_DEFINITION"
            ),
            plan.selectedQuestions.map { it.questionId }
        )
    }

    @Test
    fun buildSessionOrderAvoidsAdjacentTwinQuestionsWhenPossible() {
        val repository = FakeFlashcardRepository()
        val planner = ReviewSessionPlanner(repository, Random(7))
        val selected = listOf(
            question(cardId = "card-a", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0),
            question(cardId = "card-a", type = ReviewQuestionType.DEFINITION_TO_WORD, globalOrder = 1),
            question(cardId = "card-b", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 2),
            question(cardId = "card-c", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 4)
        )

        val sessionOrder = planner.buildSessionOrder(selected)

        assertEquals(selected.map { it.questionId }.sorted(), sessionOrder.map { it.questionId }.sorted())
        assertTrue(sessionOrder.zipWithNext().none { (left, right) -> left.cardId == right.cardId })
    }

    @Test
    fun buildSessionOrderAcceptsTwinAdjacencyWhenUnavoidable() {
        val planner = ReviewSessionPlanner(FakeFlashcardRepository(), Random(0))
        val selected = listOf(
            question(cardId = "card-a", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0),
            question(cardId = "card-a", type = ReviewQuestionType.DEFINITION_TO_WORD, globalOrder = 1)
        )

        val sessionOrder = planner.buildSessionOrder(selected)

        assertEquals(2, sessionOrder.size)
        assertEquals("card-a", sessionOrder[0].cardId)
        assertEquals("card-a", sessionOrder[1].cardId)
    }

    @Test
    fun buildPlanPassesNowToStartedDueQuery() = runTest {
        val repository = FakeFlashcardRepository(
            startedDue = listOf(question(cardId = "card-a", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0, nextDueAt = 10L, firstAnsweredAt = 1L)),
            neverStarted = emptyList()
        )
        val planner = ReviewSessionPlanner(repository, Random(0))

        planner.buildPlan(sessionSize = 1, nowMs = 123_456L)

        assertEquals(123_456L, repository.lastStartedDueNow)
    }

    @Test
    fun buildPlanFromProgressUsesDueAndNeverStartedBuckets() {
        val planner = ReviewSessionPlanner(FakeFlashcardRepository(), Random(0))
        val progress = listOf(
            question(cardId = "card-a", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0, nextDueAt = 10L, firstAnsweredAt = 1L),
            question(cardId = "card-a", type = ReviewQuestionType.DEFINITION_TO_WORD, globalOrder = 1, nextDueAt = 20L, firstAnsweredAt = 1L),
            question(cardId = "card-b", type = ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 2, nextDueAt = 999_999L, firstAnsweredAt = null),
            question(cardId = "card-b", type = ReviewQuestionType.DEFINITION_TO_WORD, globalOrder = 3, nextDueAt = 999_999L, firstAnsweredAt = null)
        )

        val plan = planner.buildPlanFromProgress(
            sessionSize = 3,
            questionProgress = progress,
            nowMs = 100L
        )

        assertEquals(
            listOf(
                "card-a::WORD_TO_DEFINITION",
                "card-a::DEFINITION_TO_WORD",
                "card-b::WORD_TO_DEFINITION"
            ),
            plan.selectedQuestions.map { it.questionId }
        )
        assertEquals(1, plan.remainingQuestionsCount)
    }

    private fun question(
        cardId: String,
        type: ReviewQuestionType,
        globalOrder: Long,
        nextDueAt: Long = 0L,
        firstAnsweredAt: Long? = 1L
    ): ReviewQuestionProgress = ReviewQuestionProgress(
        questionId = type.questionId(cardId),
        cardId = cardId,
        questionType = type,
        globalOrder = globalOrder,
        intervalIndex = 0,
        peakIntervalIndex = 0,
        currentIntervalDurationMs = 10L * 60L * 1000L,
        nextDueAt = nextDueAt,
        firstAnsweredAt = firstAnsweredAt
    )

    private class FakeFlashcardRepository(
        private val startedDue: List<ReviewQuestionProgress> = emptyList(),
        private val neverStarted: List<ReviewQuestionProgress> = emptyList()
    ) : FlashcardRepository {
        var lastStartedDueNow: Long? = null

        override suspend fun getCardsToReview(limit: Int): List<Flashcard> = emptyList()
        override suspend fun getQuestionProgress(questionId: String): ReviewQuestionProgress? = null
        override suspend fun getQuestionProgressForCard(cardId: String): List<ReviewQuestionProgress> = emptyList()
        override suspend fun getAllQuestionProgress(): List<ReviewQuestionProgress> = startedDue + neverStarted
        override suspend fun getStartedDueQuestionProgress(now: Long, limit: Int): List<ReviewQuestionProgress> {
            lastStartedDueNow = now
            return startedDue.take(limit)
        }
        override suspend fun countStartedDueQuestionProgress(now: Long): Int {
            lastStartedDueNow = now
            return startedDue.size
        }
        override suspend fun getNeverStartedQuestionProgress(limit: Int): List<ReviewQuestionProgress> = neverStarted.take(limit)
        override suspend fun countNeverStartedQuestionProgress(): Int = neverStarted.size
        override suspend fun saveQuestionProgress(progress: ReviewQuestionProgress) = Unit
        override suspend fun saveQuestionProgress(progressList: List<ReviewQuestionProgress>) = Unit
        override suspend fun saveCard(card: Flashcard) = Unit
        override suspend fun updateCardProgress(cardId: String, motVersDef: Sm2Stats, defVersMot: Sm2Stats) = Unit
        override suspend fun setFavorite(cardId: String, isFavorite: Boolean) = Unit
        override suspend fun deleteCard(cardId: String) = Unit
        override suspend fun getStatsByState(): Map<String, Int> = emptyMap()
        override suspend fun getAllCards(): List<Flashcard> = emptyList()
    }
}

