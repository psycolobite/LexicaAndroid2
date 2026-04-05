package com.example.lexicaandroid2.domain.model

data class ReviewQuestionProgress(
    val questionId: String,
    val cardId: String,
    val questionType: ReviewQuestionType,
    val globalOrder: Long,
    val level: Double = 0.0,
    val intervalIndex: Int = 0,
    val peakIntervalIndex: Int = 0,
    val weightedSuccess: Double = 0.0,
    val weightedFailure: Double = 0.0,
    val recentStreak: Int = 0,
    val recoveryReserve: Double = 0.0,
    val currentIntervalDurationMs: Long = DEFAULT_INTERVAL_DURATION_MS,
    val nextDueAt: Long = System.currentTimeMillis(),
    val lastSessionFirstAnswerAt: Long? = null,
    val lastAskedAt: Long? = null,
    val firstAnsweredAt: Long? = null,
    val pendingReplacementChallengeKind: ReviewSessionChallengeKind? = null
) {
    val isStarted: Boolean
        get() = firstAnsweredAt != null

    companion object {
        const val DEFAULT_INTERVAL_DURATION_MS: Long = 10L * 60L * 1000L
    }
}

