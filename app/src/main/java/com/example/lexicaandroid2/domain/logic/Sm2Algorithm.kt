package com.example.lexicaandroid2.domain.logic

import java.time.Instant
import kotlin.math.roundToInt

object Sm2Algorithm {
    data class Sm2State(
        val interval: Int,
        val repetitions: Int,
        val easeFactor: Double
    )

    data class Sm2Result(
        val interval: Int,
        val repetitions: Int,
        val easeFactor: Double,
        val nextReviewDate: Instant
    )

    fun calculate(state: Sm2State, quality: Int, now: Instant = Instant.now()): Sm2Result {
        require(quality in 0..5) { "quality must be in range 0..5" }

        val adjustedEaseFactor = adjustEaseFactor(state.easeFactor, quality)

        if (quality < 3) {
            return Sm2Result(
                interval = 0,
                repetitions = 0,
                easeFactor = adjustedEaseFactor,
                nextReviewDate = now
            )
        }

        val repetitions = state.repetitions + 1
        val interval = when (repetitions) {
            1 -> 1
            2 -> 6
            else -> (state.interval * adjustedEaseFactor).roundToInt().coerceAtLeast(1)
        }

        return Sm2Result(
            interval = interval,
            repetitions = repetitions,
            easeFactor = adjustedEaseFactor,
            nextReviewDate = now.plusSeconds(interval * 86_400L)
        )
    }

    private fun adjustEaseFactor(current: Double, quality: Int): Double {
        val q = quality.toDouble()
        val updated = current + (0.1 - (5.0 - q) * (0.08 + (5.0 - q) * 0.02))
        return if (updated < 1.3) 1.3 else updated
    }
}

