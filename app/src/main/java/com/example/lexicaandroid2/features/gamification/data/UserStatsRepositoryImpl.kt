package com.example.lexicaandroid2.features.gamification.data

import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import com.example.lexicaandroid2.features.gamification.domain.XPCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.UUID

class UserStatsRepositoryImpl(
    private val dao: UserStatsDao,
    private val userStatsSyncEventDao: UserStatsSyncEventDao? = null,
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) : UserStatsRepository {

    override fun getUserStats(): Flow<UserStatsEntity?> = dao.getUserStats()

    override suspend fun addXp(amount: Int) {
        val currentStats = dao.getUserStats().firstOrNull() ?: UserStatsEntity()
        val newXp = currentStats.xp + amount

        // Use XPCalculator to determine the new level
        val newLevel = XPCalculator.calculateLevel(newXp)

        dao.insertOrUpdate(currentStats.copy(xp = newXp, level = newLevel))
        userStatsSyncEventDao?.insert(
            UserStatsSyncEventEntity(
                eventId = UUID.randomUUID().toString(),
                eventType = UserStatsEventType.XP_AWARDED.name,
                occurredAt = nowProvider(),
                xpDelta = amount.toLong()
            )
        )
        compactUserStatsEventsIfNeeded(currentStats.copy(xp = newXp, level = newLevel))
    }

    override suspend fun setLevel(level: Int) {
        val currentStats = dao.getUserStats().firstOrNull() ?: UserStatsEntity()
        val safeLevel = level.coerceAtLeast(1)
        val xpForLevel = XPCalculator.calculateXpForLevel(safeLevel)
        val updatedStats = currentStats.copy(xp = xpForLevel, level = safeLevel)
        dao.insertOrUpdate(updatedStats)
        compactUserStatsEventsToSnapshot(updatedStats)
    }

    override suspend fun updateStreak() {
        val currentStats = dao.getUserStats().firstOrNull() ?: UserStatsEntity()
        val now = nowProvider()
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

        val updatedStats = currentStats.copy(lastLoginDate = now, streak = newStreak)
        dao.insertOrUpdate(updatedStats)
        userStatsSyncEventDao?.insert(
            UserStatsSyncEventEntity(
                eventId = UUID.randomUUID().toString(),
                eventType = UserStatsEventType.STREAK_UPDATED.name,
                occurredAt = now,
                streakValue = newStreak,
                lastLoginDateValue = now
            )
        )
        compactUserStatsEventsIfNeeded(updatedStats)
    }

    override suspend fun resetStats() {
        val resetAt = nowProvider()
        val resetStats = UserStatsEntity(xp = 0, level = 1, streak = 0, lastLoginDate = 0L)
        dao.insertOrUpdate(resetStats)
        userStatsSyncEventDao?.clearAll()
        userStatsSyncEventDao?.insert(
            UserStatsSyncEventEntity(
                eventId = UUID.randomUUID().toString(),
                eventType = UserStatsEventType.STATS_RESET.name,
                occurredAt = resetAt,
                levelValue = 1,
                streakValue = 0,
                lastLoginDateValue = 0L
            )
        )
    }

    override suspend fun simulateStreak(days: Int) {
        val currentStats = dao.getUserStats().firstOrNull() ?: UserStatsEntity()
        // Fixer lastLoginDate à hier pour que updateStreak() l'incrémente au prochain appel
        val yesterday = nowProvider() - 24 * 60 * 60 * 1000L
        val updatedStats = currentStats.copy(streak = days, lastLoginDate = yesterday)
        dao.insertOrUpdate(updatedStats)
        compactUserStatsEventsToSnapshot(updatedStats)
    }

    private suspend fun compactUserStatsEventsIfNeeded(currentStats: UserStatsEntity) {
        val eventDao = userStatsSyncEventDao ?: return
        if (eventDao.getAll().size <= MAX_USER_STATS_EVENT_COUNT) return
        compactUserStatsEventsToSnapshot(currentStats)
    }

    private suspend fun compactUserStatsEventsToSnapshot(currentStats: UserStatsEntity) {
        val eventDao = userStatsSyncEventDao ?: return
        eventDao.clearAll()
        eventDao.insertAll(snapshotToEvents(currentStats, nowProvider()))
    }
}

private fun snapshotToEvents(stats: UserStatsEntity, now: Long): List<UserStatsSyncEventEntity> {
    return buildList {
        add(
            UserStatsSyncEventEntity(
                eventId = UUID.randomUUID().toString(),
                eventType = UserStatsEventType.LEVEL_SET.name,
                occurredAt = now,
                levelValue = stats.level.coerceAtLeast(1)
            )
        )

        val levelXp = XPCalculator.calculateXpForLevel(stats.level.coerceAtLeast(1))
        val xpDelta = (stats.xp - levelXp).coerceAtLeast(0L)
        if (xpDelta > 0L) {
            add(
                UserStatsSyncEventEntity(
                    eventId = UUID.randomUUID().toString(),
                    eventType = UserStatsEventType.XP_AWARDED.name,
                    occurredAt = now + 1L,
                    xpDelta = xpDelta
                )
            )
        }

        if (stats.streak > 0 || stats.lastLoginDate > 0L) {
            add(
                UserStatsSyncEventEntity(
                    eventId = UUID.randomUUID().toString(),
                    eventType = UserStatsEventType.STREAK_SIMULATED.name,
                    occurredAt = now + 2L,
                    streakValue = stats.streak,
                    lastLoginDateValue = stats.lastLoginDate
                )
            )
        }
    }
}

enum class UserStatsEventType {
    XP_AWARDED,
    LEVEL_SET,
    STREAK_UPDATED,
    STREAK_SIMULATED,
    STATS_RESET
}

private const val MAX_USER_STATS_EVENT_COUNT = 200

