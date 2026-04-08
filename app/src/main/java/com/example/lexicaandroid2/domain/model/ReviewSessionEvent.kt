package com.example.lexicaandroid2.domain.model

enum class ReviewSessionEventType {
    QCM,
    MATCHING,
    EXTRA_SPELLING,
    CHALLENGE
}

enum class ReviewSessionChallengeKind {
    SPELLING,
    SEMANTIC,
    USAGE
}

data class ReviewSessionEvent(
    val eventId: String,
    val type: ReviewSessionEventType,
    val questionId: String? = null,
    val cardId: String? = null,
    val cardIds: List<String> = emptyList(),
    val prompt: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val countdownBeforeDisplay: Int = 0,
    val challengeKind: ReviewSessionChallengeKind? = null,
    val isSkippable: Boolean = false,
    val appliesSessionCredit: Boolean = true
)

