package com.example.lexicaandroid2.features.gamification.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val userId: String = "currentUser",
    val xp: Long = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastLoginDate: Long = 0L // Timestamp in milliseconds
)

