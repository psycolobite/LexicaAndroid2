package com.example.lexicaandroid2.features.gamification.domain

import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import kotlinx.coroutines.flow.Flow

interface UserStatsRepository {
    fun getUserStats(): Flow<UserStatsEntity?>
    suspend fun addXp(amount: Int)
    suspend fun updateStreak()
}

