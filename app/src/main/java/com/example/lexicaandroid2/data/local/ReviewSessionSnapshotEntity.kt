package com.example.lexicaandroid2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_session_snapshots")
data class ReviewSessionSnapshotEntity(
    @PrimaryKey
    val sessionId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val payloadJson: String
)

