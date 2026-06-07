package com.example.lexicaandroid2.domain.logic

import com.example.lexicaandroid2.domain.model.MIN_INTERVENING_PRESENTATIONS_FOR_SAME_CARD_FAMILY
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.model.ReviewSessionPlan
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlin.random.Random

class ReviewSessionPlanner(
    private val repository: FlashcardRepository,
    private val random: Random = Random.Default
) {
    suspend fun buildPlan(
        sessionSize: Int,
        nowMs: Long = System.currentTimeMillis()
    ): ReviewSessionPlan {
        require(sessionSize > 0) { "sessionSize must be > 0" }

        val startedDueCount = repository.countStartedDueQuestionProgress(nowMs)
        val neverStartedCount = repository.countNeverStartedQuestionProgress()
        val startedDue = repository.getStartedDueQuestionProgress(
            now = nowMs,
            limit = startedDueCount.coerceAtLeast(1)
        )
        val neverStarted = repository.getNeverStartedQuestionProgress(
            limit = neverStartedCount.coerceAtLeast(1)
        )

        val prioritizedGlobalOrder = buildPrioritizedGlobalOrder(startedDue, neverStarted)
        val selectedQuestions = prioritizedGlobalOrder.take(sessionSize)
        val sessionOrder = buildSessionOrder(selectedQuestions)

        return ReviewSessionPlan(
            selectedQuestions = selectedQuestions,
            sessionOrder = sessionOrder,
            remainingQuestionsCount = (prioritizedGlobalOrder.size - selectedQuestions.size).coerceAtLeast(0)
        )
    }

    fun buildPlanFromProgress(
        sessionSize: Int,
        questionProgress: List<ReviewQuestionProgress>,
        nowMs: Long = System.currentTimeMillis()
    ): ReviewSessionPlan {
        require(sessionSize > 0) { "sessionSize must be > 0" }

        val startedDue = questionProgress.filter { it.firstAnsweredAt != null && it.nextDueAt <= nowMs }
        val neverStarted = questionProgress.filter { it.firstAnsweredAt == null }
        val prioritizedGlobalOrder = buildPrioritizedGlobalOrder(startedDue, neverStarted)
        val selectedQuestions = prioritizedGlobalOrder.take(sessionSize)
        val sessionOrder = buildSessionOrder(selectedQuestions)

        return ReviewSessionPlan(
            selectedQuestions = selectedQuestions,
            sessionOrder = sessionOrder,
            remainingQuestionsCount = (prioritizedGlobalOrder.size - selectedQuestions.size).coerceAtLeast(0)
        )
    }

    internal fun buildPrioritizedGlobalOrder(
        startedDue: List<ReviewQuestionProgress>,
        neverStarted: List<ReviewQuestionProgress>
    ): List<ReviewQuestionProgress> {
        val duePart = buildPriorityBucket(startedDue) { group ->
            group.minOf { it.nextDueAt }
        }
        val neverStartedPart = buildPriorityBucket(neverStarted) { group ->
            group.minOf { it.globalOrder }
        }
        return duePart + neverStartedPart
    }

    internal fun buildSessionOrder(selectedQuestions: List<ReviewQuestionProgress>): List<ReviewQuestionProgress> {
        val remaining = selectedQuestions.shuffled(random).toMutableList()
        val order = mutableListOf<ReviewQuestionProgress>()

        while (remaining.isNotEmpty()) {
            val recentCardIds = order
                .takeLast(MIN_INTERVENING_PRESENTATIONS_FOR_SAME_CARD_FAMILY)
                .map { it.cardId }
                .toSet()
            val candidates = remaining.filter { it.cardId !in recentCardIds }
            val pool = if (candidates.isNotEmpty()) candidates else remaining
            val chosen = pool[random.nextInt(pool.size)]
            order += chosen
            remaining.remove(chosen)
        }

        return order
    }

    private fun buildPriorityBucket(
        questions: List<ReviewQuestionProgress>,
        groupKey: (List<ReviewQuestionProgress>) -> Long
    ): List<ReviewQuestionProgress> {
        return questions
            .groupBy { it.cardId }
            .values
            .sortedWith(
                compareBy<List<ReviewQuestionProgress>>(
                    { groupKey(it) },
                    { it.minOf { question -> question.globalOrder } }
                )
            )
            .flatMap { group -> group.sortedBy { it.globalOrder } }
    }
}

