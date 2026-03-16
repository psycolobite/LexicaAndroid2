package com.example.lexicaandroid2.features.gamification.data

import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import com.example.lexicaandroid2.features.gamification.domain.XPCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class UserStatsRepositoryImpl(
    private val dao: UserStatsDao
) : UserStatsRepository {

    override fun getUserStats(): Flow<UserStatsEntity?> = dao.getUserStats()

    override suspend fun addXp(amount: Int) {
        val currentStats = dao.getUserStats().firstOrNull() ?: UserStatsEntity()
        val newXp = currentStats.xp + amount

        // Use XPCalculator to determine the new level
        val newLevel = XPCalculator.calculateLevel(newXp)

        dao.insertOrUpdate(currentStats.copy(xp = newXp, level = newLevel))
    }

    override suspend fun setLevel(level: Int) {
        val currentStats = dao.getUserStats().firstOrNull() ?: UserStatsEntity()
        val safeLevel = level.coerceAtLeast(1)
        val xpForLevel = XPCalculator.calculateXpForLevel(safeLevel)
        dao.insertOrUpdate(currentStats.copy(xp = xpForLevel, level = safeLevel))
    }

    override suspend fun updateStreak() {
        val currentStats = dao.getUserStats().firstOrNull() ?: UserStatsEntity()
        val now = System.currentTimeMillis()
        val lastDate = currentStats.lastLoginDate

        val calNow = Calendar.getInstance().apply { timeInMillis = now }
        val calLast = Calendar.getInstance().apply { timeInMillis = lastDate }

        val isSameDay = calNow.get(Calendar.YEAR) == calLast.get(Calendar.YEAR) &&
                        calNow.get(Calendar.DAY_OF_YEAR) == calLast.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) return // Already logged in today

        // Check if yesterday
        calLast.add(Calendar.DAY_OF_YEAR, 1)
        val wasYesterday = calNow.get(Calendar.YEAR) == calLast.get(Calendar.YEAR) &&
                           calNow.get(Calendar.DAY_OF_YEAR) == calLast.get(Calendar.DAY_OF_YEAR)

        val newStreak = if (wasYesterday) currentStats.streak + 1 else 1

        dao.insertOrUpdate(currentStats.copy(lastLoginDate = now, streak = newStreak))
    }

    override suspend fun resetStats() {
        dao.insertOrUpdate(UserStatsEntity(xp = 0, level = 1, streak = 0, lastLoginDate = 0L))
    }

    override suspend fun simulateStreak(days: Int) {
        val currentStats = dao.getUserStats().firstOrNull() ?: UserStatsEntity()
        // Fixer lastLoginDate à hier pour que updateStreak() l'incrémente au prochain appel
        val yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        dao.insertOrUpdate(currentStats.copy(streak = days, lastLoginDate = yesterday))
    }
}

