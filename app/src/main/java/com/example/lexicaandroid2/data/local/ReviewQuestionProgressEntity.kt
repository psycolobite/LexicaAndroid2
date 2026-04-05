package com.example.lexicaandroid2.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_question_progress",
    indices = [
        Index(value = ["cardId"]),
        Index(value = ["nextDueAt"]),
        Index(value = ["cardId", "questionType"], unique = true)
    ]
)
data class ReviewQuestionProgressEntity(
    @PrimaryKey
    val questionId: String,
    val cardId: String,
    val questionType: String,
    val globalOrder: Long,
    val level: Double = 0.0,
    val intervalIndex: Int = 0,
    val peakIntervalIndex: Int = 0,
    val weightedSuccess: Double = 0.0,
    val weightedFailure: Double = 0.0,
    val recentStreak: Int = 0,
    val recoveryReserve: Double = 0.0,
    val currentIntervalDurationMs: Long,
    val nextDueAt: Long,
    val lastSessionFirstAnswerAt: Long? = null,
    val lastAskedAt: Long? = null,
    val firstAnsweredAt: Long? = null,
    val pendingReplacementChallengeKind: String? = null
)

