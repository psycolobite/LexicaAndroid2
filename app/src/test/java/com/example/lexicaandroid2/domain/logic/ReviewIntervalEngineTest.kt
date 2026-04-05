package com.example.lexicaandroid2.domain.logic

import com.example.lexicaandroid2.domain.model.ReviewAnswer
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.model.ReviewQuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewIntervalEngineTest {
    @Test
    fun againResetsQuestionToT0AndBuildsRecoveryReserve() {
        val now = 2_000_000L
        val progress = baseProgress(
            level = 4.4,
            intervalIndex = 4,
            peakIntervalIndex = 5,
            weightedSuccess = 8.0,
            weightedFailure = 1.0,
            recentStreak = 3,
            recoveryReserve = 0.0,
            currentIntervalDurationMs = ReviewIntervalEngine.durationForIntervalIndex(4),
            lastSessionFirstAnswerAt = now - ReviewIntervalEngine.durationForIntervalIndex(4)
        )

        val result = ReviewIntervalEngine.applyFirstAnswer(progress, ReviewAnswer.AGAIN, now)

        assertEquals(0.0, result.level, 0.0001)
        assertEquals(0, result.intervalIndex)
        assertEquals(5, result.peakIntervalIndex)
        assertEquals(0, result.recentStreak)
        assertEquals(3.55, result.recoveryReserve, 0.0001)
        assertEquals(10L * 60L * 1000L, result.currentIntervalDurationMs)
        assertEquals(now + (10L * 60L * 1000L), result.nextDueAt)
        assertEquals(now, result.lastSessionFirstAnswerAt)
        assertEquals(now, result.lastAskedAt)
        assertNull(progress.firstAnsweredAt)
        assertEquals(now, result.firstAnsweredAt)
        assertEquals(6.8, result.weightedSuccess, 0.0001)
        assertEquals(1.85, result.weightedFailure, 0.0001)
    }

    @Test
    fun gotItProgressesAccordingToMasteryEligibilityStreakAndRecovery() {
        val now = 10_000_000L
        val progress = baseProgress(
            level = 2.0,
            intervalIndex = 2,
            peakIntervalIndex = 2,
            weightedSuccess = 6.0,
            weightedFailure = 1.0,
            recentStreak = 2,
            recoveryReserve = 1.0,
            currentIntervalDurationMs = ReviewIntervalEngine.durationForIntervalIndex(2),
            lastSessionFirstAnswerAt = now - ReviewIntervalEngine.durationForIntervalIndex(2),
            firstAnsweredAt = 1_000L
        )

        val result = ReviewIntervalEngine.applyFirstAnswer(progress, ReviewAnswer.GOT_IT, now)

        assertEquals(4.675, result.level, 0.0001)
        assertEquals(4, result.intervalIndex)
        assertEquals(4, result.peakIntervalIndex)
        assertEquals(3, result.recentStreak)
        assertEquals(0.0, result.recoveryReserve, 0.0001)
        assertEquals(30L * 24L * 60L * 60L * 1000L, result.currentIntervalDurationMs)
        assertEquals(now + (30L * 24L * 60L * 60L * 1000L), result.nextDueAt)
        assertEquals(6.1, result.weightedSuccess, 0.0001)
        assertEquals(0.85, result.weightedFailure, 0.0001)
        assertEquals(1_000L, result.firstAnsweredAt)
    }

    @Test
    fun tooEasyActsLikeThreeCondensedSuccesses() {
        val now = 5_000_000L
        val progress = baseProgress(
            level = 2.0,
            intervalIndex = 2,
            peakIntervalIndex = 2,
            weightedSuccess = 4.0,
            weightedFailure = 1.0,
            recentStreak = 1,
            recoveryReserve = 2.0,
            currentIntervalDurationMs = ReviewIntervalEngine.durationForIntervalIndex(2),
            lastSessionFirstAnswerAt = now - ReviewIntervalEngine.durationForIntervalIndex(2)
        )

        val gotIt = ReviewIntervalEngine.applyFirstAnswer(progress, ReviewAnswer.GOT_IT, now)
        val tooEasy = ReviewIntervalEngine.applyFirstAnswer(progress, ReviewAnswer.TOO_EASY, now)

        assertTrue(tooEasy.level > gotIt.level)
        assertTrue(tooEasy.intervalIndex > gotIt.intervalIndex)
        assertEquals(progress.recentStreak + 3, tooEasy.recentStreak)
        assertEquals(0.0, tooEasy.recoveryReserve, 0.0001)
        assertEquals(6.4, tooEasy.weightedSuccess, 0.0001)
        assertEquals(0.85, tooEasy.weightedFailure, 0.0001)
    }

    @Test
    fun strongerQuestionRecoversFasterAfterAFall() {
        val fallAt = 20_000_000L
        val recoverAt = fallAt + ReviewIntervalEngine.durationForIntervalIndex(0)

        val strongBeforeFall = baseProgress(
            level = 5.2,
            intervalIndex = 5,
            peakIntervalIndex = 5,
            weightedSuccess = 10.0,
            weightedFailure = 1.0,
            recentStreak = 4,
            currentIntervalDurationMs = ReviewIntervalEngine.durationForIntervalIndex(5),
            lastSessionFirstAnswerAt = fallAt - ReviewIntervalEngine.durationForIntervalIndex(5),
            firstAnsweredAt = 100L
        )
        val weakBeforeFall = baseProgress(
            level = 5.2,
            intervalIndex = 5,
            peakIntervalIndex = 5,
            weightedSuccess = 1.0,
            weightedFailure = 10.0,
            recentStreak = 4,
            currentIntervalDurationMs = ReviewIntervalEngine.durationForIntervalIndex(5),
            lastSessionFirstAnswerAt = fallAt - ReviewIntervalEngine.durationForIntervalIndex(5),
            firstAnsweredAt = 100L
        )

        val strongAfterFall = ReviewIntervalEngine.applyFirstAnswer(strongBeforeFall, ReviewAnswer.AGAIN, fallAt)
        val weakAfterFall = ReviewIntervalEngine.applyFirstAnswer(weakBeforeFall, ReviewAnswer.AGAIN, fallAt)
        val strongRecovered = ReviewIntervalEngine.applyFirstAnswer(strongAfterFall, ReviewAnswer.GOT_IT, recoverAt)
        val weakRecovered = ReviewIntervalEngine.applyFirstAnswer(weakAfterFall, ReviewAnswer.GOT_IT, recoverAt)

        assertTrue(strongAfterFall.recoveryReserve > weakAfterFall.recoveryReserve)
        assertTrue(strongRecovered.level > weakRecovered.level)
        assertTrue(strongRecovered.intervalIndex >= weakRecovered.intervalIndex)
    }

    @Test
    fun crossingT4UsesOneMonthDuration() {
        val now = 30_000_000L
        val progress = baseProgress(
            level = 3.1,
            intervalIndex = 3,
            peakIntervalIndex = 3,
            weightedSuccess = 1.0,
            weightedFailure = 2.0,
            recentStreak = 0,
            recoveryReserve = 0.0,
            currentIntervalDurationMs = ReviewIntervalEngine.durationForIntervalIndex(3),
            lastSessionFirstAnswerAt = now - ReviewIntervalEngine.durationForIntervalIndex(3)
        )

        val result = ReviewIntervalEngine.applyFirstAnswer(progress, ReviewAnswer.GOT_IT, now)

        assertTrue(result.level >= 4.0)
        assertEquals(4, result.intervalIndex)
        assertEquals(30L * 24L * 60L * 60L * 1000L, result.currentIntervalDurationMs)
    }

    @Test
    fun temporalEligibilityPreventsEarlyReviewFromOverBoostingProgress() {
        val now = 40_000_000L
        val currentInterval = ReviewIntervalEngine.durationForIntervalIndex(3)
        val template = baseProgress(
            level = 2.5,
            intervalIndex = 3,
            peakIntervalIndex = 3,
            weightedSuccess = 4.0,
            weightedFailure = 1.0,
            recentStreak = 1,
            recoveryReserve = 0.0,
            currentIntervalDurationMs = currentInterval
        )

        val early = ReviewIntervalEngine.applyFirstAnswer(
            template.copy(lastSessionFirstAnswerAt = now - (currentInterval / 4)),
            ReviewAnswer.GOT_IT,
            now
        )
        val onTime = ReviewIntervalEngine.applyFirstAnswer(
            template.copy(lastSessionFirstAnswerAt = now - currentInterval),
            ReviewAnswer.GOT_IT,
            now
        )

        assertTrue(onTime.level > early.level)
        assertTrue(onTime.intervalIndex >= early.intervalIndex)
    }

    @Test
    fun durationForIntervalIndexDoublesAfterT8() {
        val twoYears = 730L * 24L * 60L * 60L * 1000L

        assertEquals(10L * 60L * 1000L, ReviewIntervalEngine.durationForIntervalIndex(0))
        assertEquals(60L * 60L * 1000L, ReviewIntervalEngine.durationForIntervalIndex(1))
        assertEquals(twoYears, ReviewIntervalEngine.durationForIntervalIndex(8))
        assertEquals(twoYears * 2, ReviewIntervalEngine.durationForIntervalIndex(9))
        assertEquals(twoYears * 4, ReviewIntervalEngine.durationForIntervalIndex(10))
    }

    private fun baseProgress(
        level: Double = 0.0,
        intervalIndex: Int = 0,
        peakIntervalIndex: Int = intervalIndex,
        weightedSuccess: Double = 0.0,
        weightedFailure: Double = 0.0,
        recentStreak: Int = 0,
        recoveryReserve: Double = 0.0,
        currentIntervalDurationMs: Long = ReviewIntervalEngine.durationForIntervalIndex(intervalIndex),
        lastSessionFirstAnswerAt: Long? = null,
        firstAnsweredAt: Long? = null
    ): ReviewQuestionProgress = ReviewQuestionProgress(
        questionId = "card-1::WORD_TO_DEFINITION",
        cardId = "card-1",
        questionType = ReviewQuestionType.WORD_TO_DEFINITION,
        globalOrder = 1L,
        level = level,
        intervalIndex = intervalIndex,
        peakIntervalIndex = peakIntervalIndex,
        weightedSuccess = weightedSuccess,
        weightedFailure = weightedFailure,
        recentStreak = recentStreak,
        recoveryReserve = recoveryReserve,
        currentIntervalDurationMs = currentIntervalDurationMs,
        nextDueAt = 0L,
        lastSessionFirstAnswerAt = lastSessionFirstAnswerAt,
        lastAskedAt = lastSessionFirstAnswerAt,
        firstAnsweredAt = firstAnsweredAt
    )
}

