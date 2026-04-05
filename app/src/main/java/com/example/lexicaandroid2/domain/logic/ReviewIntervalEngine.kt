package com.example.lexicaandroid2.domain.logic

import com.example.lexicaandroid2.domain.model.ReviewAnswer
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object ReviewIntervalEngine {
    private const val DECAY_FACTOR = 0.85
    private const val TEN_MINUTES_MS = 10L * 60L * 1000L
    private const val ONE_HOUR_MS = 60L * 60L * 1000L
    private const val ONE_DAY_MS = 24L * 60L * 60L * 1000L
    private const val ONE_WEEK_MS = 7L * ONE_DAY_MS
    private const val THIRTY_DAYS_MS = 30L * ONE_DAY_MS
    private const val NINETY_DAYS_MS = 90L * ONE_DAY_MS
    private const val ONE_HUNDRED_EIGHTY_DAYS_MS = 180L * ONE_DAY_MS
    private const val THREE_HUNDRED_SIXTY_FIVE_DAYS_MS = 365L * ONE_DAY_MS
    private const val TWO_YEARS_MS = 730L * ONE_DAY_MS

    fun applyFirstAnswer(
        progress: ReviewQuestionProgress,
        answer: ReviewAnswer,
        nowMs: Long = System.currentTimeMillis()
    ): ReviewQuestionProgress {
        val masteryRatio = masteryRatio(progress)
        val eligibility = temporalEligibility(progress, nowMs)
        val updatedWeightedSuccess = (DECAY_FACTOR * progress.weightedSuccess) + answer.successWeight
        val updatedWeightedFailure = (DECAY_FACTOR * progress.weightedFailure) + answer.failureWeight

        val nextProgress = when (answer) {
            ReviewAnswer.AGAIN -> applyAgain(progress, masteryRatio)
            ReviewAnswer.GOT_IT -> applyGotIt(progress, masteryRatio, eligibility)
            ReviewAnswer.TOO_EASY -> applyTooEasy(progress, masteryRatio, eligibility)
        }

        val nextDuration = durationForIntervalIndex(nextProgress.intervalIndex)
        return nextProgress.copy(
            weightedSuccess = updatedWeightedSuccess,
            weightedFailure = updatedWeightedFailure,
            currentIntervalDurationMs = nextDuration,
            nextDueAt = nowMs + nextDuration,
            lastSessionFirstAnswerAt = nowMs,
            lastAskedAt = nowMs,
            firstAnsweredAt = progress.firstAnsweredAt ?: nowMs
        )
    }

    fun durationForIntervalIndex(intervalIndex: Int): Long = when {
        intervalIndex <= 0 -> TEN_MINUTES_MS
        intervalIndex == 1 -> ONE_HOUR_MS
        intervalIndex == 2 -> ONE_DAY_MS
        intervalIndex == 3 -> ONE_WEEK_MS
        intervalIndex == 4 -> THIRTY_DAYS_MS
        intervalIndex == 5 -> NINETY_DAYS_MS
        intervalIndex == 6 -> ONE_HUNDRED_EIGHTY_DAYS_MS
        intervalIndex == 7 -> THREE_HUNDRED_SIXTY_FIVE_DAYS_MS
        intervalIndex == 8 -> TWO_YEARS_MS
        else -> TWO_YEARS_MS * (1L shl (intervalIndex - 8))
    }

    fun masteryRatio(progress: ReviewQuestionProgress): Double {
        val denominator = progress.weightedSuccess + progress.weightedFailure + 1.0
        return progress.weightedSuccess / denominator
    }

    fun temporalEligibility(progress: ReviewQuestionProgress, nowMs: Long): Double {
        val duration = progress.currentIntervalDurationMs
        if (duration <= 0L) return 1.0
        val lastRealReviewAt = progress.lastSessionFirstAnswerAt ?: return 1.0
        val elapsed = (nowMs - lastRealReviewAt).coerceAtLeast(0L)
        return min(1.0, elapsed.toDouble() / duration.toDouble())
    }

    private fun applyAgain(
        progress: ReviewQuestionProgress,
        masteryRatio: Double
    ): ReviewQuestionProgress {
        val peakIntervalIndex = max(progress.peakIntervalIndex, progress.intervalIndex)
        val recoveryReserve = peakIntervalIndex * (0.35 + (0.45 * masteryRatio))
        return progress.copy(
            level = 0.0,
            intervalIndex = 0,
            peakIntervalIndex = peakIntervalIndex,
            recentStreak = 0,
            recoveryReserve = recoveryReserve
        )
    }

    private fun applyGotIt(
        progress: ReviewQuestionProgress,
        masteryRatio: Double,
        eligibility: Double
    ): ReviewQuestionProgress {
        val gain = eligibility * (
            1.0 +
                (1.10 * masteryRatio) +
                (0.25 * min(progress.recentStreak, 4)) +
                (0.35 * min(progress.recoveryReserve, 3.0))
            )
        val nextLevel = progress.level + gain
        val nextIntervalIndex = floor(nextLevel).toInt().coerceAtLeast(0)
        return progress.copy(
            level = nextLevel,
            intervalIndex = nextIntervalIndex,
            recentStreak = progress.recentStreak + 1,
            recoveryReserve = max(0.0, progress.recoveryReserve - 1.0),
            peakIntervalIndex = max(progress.peakIntervalIndex, nextIntervalIndex)
        )
    }

    private fun applyTooEasy(
        progress: ReviewQuestionProgress,
        masteryRatio: Double,
        eligibility: Double
    ): ReviewQuestionProgress {
        val gain = eligibility * (
            3.0 +
                (1.30 * masteryRatio) +
                (0.40 * min(progress.recentStreak, 4)) +
                (0.40 * min(progress.recoveryReserve, 3.0))
            )
        val nextLevel = progress.level + gain
        val nextIntervalIndex = floor(nextLevel).toInt().coerceAtLeast(0)
        return progress.copy(
            level = nextLevel,
            intervalIndex = nextIntervalIndex,
            recentStreak = progress.recentStreak + 3,
            recoveryReserve = max(0.0, progress.recoveryReserve - 2.0),
            peakIntervalIndex = max(progress.peakIntervalIndex, nextIntervalIndex)
        )
    }
}

