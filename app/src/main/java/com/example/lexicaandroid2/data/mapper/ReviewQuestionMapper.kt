package com.example.lexicaandroid2.data.mapper

import com.example.lexicaandroid2.data.local.FlashcardEntity
import com.example.lexicaandroid2.data.local.ReviewQuestionProgressEntity
import com.example.lexicaandroid2.data.local.Sm2DataEmbedded
import com.example.lexicaandroid2.domain.logic.ReviewIntervalEngine
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.model.ReviewSessionChallengeKind
import com.example.lexicaandroid2.domain.model.ReviewQuestionType
import kotlin.math.max

private const val ONE_DAY_MS = 24L * 60L * 60L * 1000L

fun ReviewQuestionProgressEntity.toDomain(): ReviewQuestionProgress = ReviewQuestionProgress(
    questionId = questionId,
    cardId = cardId,
    questionType = ReviewQuestionType.fromStorage(questionType),
    globalOrder = globalOrder,
    level = level,
    intervalIndex = intervalIndex,
    peakIntervalIndex = peakIntervalIndex,
    weightedSuccess = weightedSuccess,
    weightedFailure = weightedFailure,
    recentStreak = recentStreak,
    recoveryReserve = recoveryReserve,
    currentIntervalDurationMs = currentIntervalDurationMs,
    nextDueAt = nextDueAt,
    lastSessionFirstAnswerAt = lastSessionFirstAnswerAt,
    lastAskedAt = lastAskedAt,
    firstAnsweredAt = firstAnsweredAt,
    pendingReplacementChallengeKind = pendingReplacementChallengeKind?.let(ReviewSessionChallengeKind::valueOf)
)

fun ReviewQuestionProgress.toEntity(): ReviewQuestionProgressEntity = ReviewQuestionProgressEntity(
    questionId = questionId,
    cardId = cardId,
    questionType = questionType.name,
    globalOrder = globalOrder,
    level = level,
    intervalIndex = intervalIndex,
    peakIntervalIndex = peakIntervalIndex,
    weightedSuccess = weightedSuccess,
    weightedFailure = weightedFailure,
    recentStreak = recentStreak,
    recoveryReserve = recoveryReserve,
    currentIntervalDurationMs = currentIntervalDurationMs,
    nextDueAt = nextDueAt,
    lastSessionFirstAnswerAt = lastSessionFirstAnswerAt,
    lastAskedAt = lastAskedAt,
    firstAnsweredAt = firstAnsweredAt,
    pendingReplacementChallengeKind = pendingReplacementChallengeKind?.name
)

fun FlashcardEntity.toReviewQuestionProgressEntities(): List<ReviewQuestionProgressEntity> = listOf(
    sm2MotVersDef.toReviewQuestionProgressEntity(
        cardId = id,
        cardAddedAt = dateAjout,
        questionType = ReviewQuestionType.WORD_TO_DEFINITION
    ),
    sm2DefVersMot.toReviewQuestionProgressEntity(
        cardId = id,
        cardAddedAt = dateAjout,
        questionType = ReviewQuestionType.DEFINITION_TO_WORD
    )
)

private fun Sm2DataEmbedded.toReviewQuestionProgressEntity(
    cardId: String,
    cardAddedAt: Long,
    questionType: ReviewQuestionType
): ReviewQuestionProgressEntity {
    val intervalIndex = intervalDaysToIndex(interval)
    val startedAt = if (totalReviews > 0 || repetitions > 0) {
        lastReview ?: cardAddedAt
    } else {
        null
    }
    val computedIntervalDurationMs = inferIntervalDurationMs(
        storedIntervalDays = interval,
        lastReview = lastReview,
        nextReview = nextReview,
        intervalIndex = intervalIndex
    )

    return ReviewQuestionProgressEntity(
        questionId = questionType.questionId(cardId),
        cardId = cardId,
        questionType = questionType.name,
        globalOrder = (cardAddedAt * 2L) + questionType.globalOrderOffset,
        level = intervalIndex.toDouble(),
        intervalIndex = intervalIndex,
        peakIntervalIndex = intervalIndex,
        weightedSuccess = correctReviews.toDouble(),
        weightedFailure = lapses.toDouble(),
        recentStreak = repetitions,
        recoveryReserve = 0.0,
        currentIntervalDurationMs = computedIntervalDurationMs,
        nextDueAt = nextReview,
        lastSessionFirstAnswerAt = lastReview,
        lastAskedAt = lastReview,
        firstAnsweredAt = startedAt
    )
}

private fun inferIntervalDurationMs(
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

private fun intervalDaysToIndex(intervalDays: Int): Int = when {
    intervalDays <= 0 -> 0
    intervalDays < 7 -> 2
    intervalDays < 30 -> 3
    intervalDays < 90 -> 4
    intervalDays < 180 -> 5
    intervalDays < 365 -> 6
    intervalDays < 730 -> 7
    else -> 8 + max(0, floorLog2((intervalDays.toDouble() / 730.0)).toInt())
}


private fun floorLog2(value: Double): Double {
    if (value <= 1.0) return 0.0
    return kotlin.math.floor(kotlin.math.ln(value) / kotlin.math.ln(2.0))
}

