package com.example.lexicaandroid2.features.gamification.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserStatsSyncEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: UserStatsSyncEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<UserStatsSyncEventEntity>)

    @Query("SELECT * FROM user_stats_sync_events ORDER BY occurredAt ASC, eventId ASC")
    suspend fun getAll(): List<UserStatsSyncEventEntity>

    @Query("DELETE FROM user_stats_sync_events")
    suspend fun clearAll()
}