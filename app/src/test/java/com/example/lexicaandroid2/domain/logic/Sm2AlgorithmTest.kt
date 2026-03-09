package com.example.lexicaandroid2.domain.logic

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class Sm2AlgorithmTest {
    @Test
    fun gradeBelowThreeResetsIntervalAndRepetitions() {
        val now = Instant.parse("2026-02-09T00:00:00Z")
        val state = Sm2Algorithm.Sm2State(
            interval = 5,
            repetitions = 3,
            easeFactor = 2.5
        )

        val result = Sm2Algorithm.calculate(state, quality = 2, now = now)

        assertEquals(0, result.interval)
        assertEquals(0, result.repetitions)
        assertEquals(now, result.nextReviewDate)
        assertEquals(2.18, result.easeFactor, 0.0001)
    }

    @Test
    fun gradeThreeOrMoreAdvancesSchedule() {
        val now = Instant.parse("2026-02-09T00:00:00Z")
        val state = Sm2Algorithm.Sm2State(
            interval = 6,
            repetitions = 2,
            easeFactor = 2.5
        )

        val result = Sm2Algorithm.calculate(state, quality = 4, now = now)

        assertEquals(3, result.repetitions)
        assertEquals(15, result.interval)
        assertEquals(2.5, result.easeFactor, 0.0001)
        assertEquals(now.plusSeconds(15 * 86_400L), result.nextReviewDate)
    }
}
