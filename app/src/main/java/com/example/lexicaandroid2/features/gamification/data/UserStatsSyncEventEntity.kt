package com.example.lexicaandroid2.features.gamification.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_stats_sync_events",
    indices = [Index(value = ["occurredAt"], name = "index_user_stats_sync_events_occurredAt")]
)
data class UserStatsSyncEventEntity(
    @PrimaryKey
    val eventId: String,
    val eventType: String,
    val occurredAt: Long,
    val xpDelta: Long? = null,
    val levelValue: Int? = null,
    val streakValue: Int? = null,
    val lastLoginDateValue: Long? = null
)