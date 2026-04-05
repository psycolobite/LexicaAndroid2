package com.example.lexicaandroid2.domain.model

enum class ReviewAnswer(
    val successWeight: Double,
    val failureWeight: Double
) {
    AGAIN(
        successWeight = 0.0,
        failureWeight = 1.0
    ),
    GOT_IT(
        successWeight = 1.0,
        failureWeight = 0.0
    ),
    TOO_EASY(
        successWeight = 3.0,
        failureWeight = 0.0
    );

    companion object {
        fun fromLegacyQuality(quality: Int): ReviewAnswer = when {
            quality < 3 -> AGAIN
            quality >= 5 -> TOO_EASY
            else -> GOT_IT
        }
    }
}

