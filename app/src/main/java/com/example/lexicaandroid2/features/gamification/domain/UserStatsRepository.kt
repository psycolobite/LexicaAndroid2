package com.example.lexicaandroid2.features.gamification.domain

import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import kotlinx.coroutines.flow.Flow

interface UserStatsRepository {
    fun getUserStats(): Flow<UserStatsEntity?>
    suspend fun addXp(amount: Int)
    suspend fun setLevel(level: Int)
    suspend fun updateStreak()
    /** Réinitialise XP et niveau à 0/1 (usage admin uniquement). */
    suspend fun resetStats()
    /** Simule un streak de N jours consécutifs (usage admin uniquement). */
    suspend fun simulateStreak(days: Int)
}

