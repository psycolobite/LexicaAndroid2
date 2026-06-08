package com.example.lexicaandroid2.domain.model

data class ReviewAnswerSyncEvent(
    val eventId: String,
    val sessionId: String,
    val questionId: String,
    val cardId: String,
    val questionType: ReviewQuestionType,
    val answer: ReviewAnswer,
    val answeredAt: Long,
    val challengeKind: ReviewSessionChallengeKind? = null
)