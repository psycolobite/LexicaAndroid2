package com.example.lexicaandroid2.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_answer_sync_events",
    indices = [
        Index(value = ["answeredAt"], name = "index_review_answer_sync_events_answeredAt"),
        Index(value = ["cardId"], name = "index_review_answer_sync_events_cardId")
    ]
)
data class ReviewAnswerSyncEventEntity(
    @PrimaryKey
    val eventId: String,
    val sessionId: String,
    val questionId: String,
    val cardId: String,
    val questionType: String,
    val answer: String,
    val answeredAt: Long,
    val challengeKind: String? = null
)