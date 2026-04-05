package com.example.lexicaandroid2.domain.model

data class ReviewChallengeResultSnapshot(
    val isValid: Boolean,
    val keywordScore: Float = 0f,
    val semanticScore: Float = -1f,
    val foundKeywords: List<String> = emptyList(),
    val missingKeywords: List<String> = emptyList(),
    val xpBonus: Int = 0,
    val feedbackMessage: String = ""
)

