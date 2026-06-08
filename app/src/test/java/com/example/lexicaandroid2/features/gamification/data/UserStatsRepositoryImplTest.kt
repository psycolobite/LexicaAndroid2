package com.example.lexicaandroid2.features.gamification.data

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserStatsRepositoryImplTest {

    @Test
    fun resetStatsClearsPreviousEventsAndWritesResetBoundary() = runTest {
        val userStatsDao = mock<UserStatsDao>()
        val syncEventDao = mock<UserStatsSyncEventDao>()
        whenever(userStatsDao.getUserStats(eq("currentUser"))).thenReturn(
            flowOf(UserStatsEntity(xp = 120, level = 2, streak = 5, lastLoginDate = 999L))
        )

        val repository = UserStatsRepositoryImpl(
            dao = userStatsDao,
            userStatsSyncEventDao = syncEventDao,
            nowProvider = { 1234L }
        )

        repository.resetStats()

        verify(syncEventDao).clearAll()
        verify(syncEventDao).insert(
            argThat {
                eventType == UserStatsEventType.STATS_RESET.name &&
                    occurredAt == 1234L &&
                    levelValue == 1 &&
                    streakValue == 0 &&
                    lastLoginDateValue == 0L
            }
        )
    }

    @Test
    fun setLevelCompactsAdminOverrideIntoSnapshotEvents() = runTest {
        val userStatsDao = mock<UserStatsDao>()
        val syncEventDao = mock<UserStatsSyncEventDao>()
        whenever(userStatsDao.getUserStats(eq("currentUser"))).thenReturn(
            flowOf(UserStatsEntity(xp = 420, level = 3, streak = 7, lastLoginDate = 2000L))
        )

        val repository = UserStatsRepositoryImpl(
            dao = userStatsDao,
            userStatsSyncEventDao = syncEventDao,
            nowProvider = { 5000L }
        )

        repository.setLevel(4)

        verify(syncEventDao).clearAll()
        verify(syncEventDao).insertAll(
            argThat { any { it.eventType == UserStatsEventType.LEVEL_SET.name && it.levelValue == 4 } }
        )
    }
}