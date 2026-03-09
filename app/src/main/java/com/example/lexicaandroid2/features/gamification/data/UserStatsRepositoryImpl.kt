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
}

