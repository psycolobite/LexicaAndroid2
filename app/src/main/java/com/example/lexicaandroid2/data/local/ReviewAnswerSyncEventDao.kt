package com.example.lexicaandroid2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReviewAnswerSyncEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ReviewAnswerSyncEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<ReviewAnswerSyncEventEntity>)

    @Query("SELECT * FROM review_answer_sync_events ORDER BY answeredAt ASC, eventId ASC")
    suspend fun getAll(): List<ReviewAnswerSyncEventEntity>

    @Query("DELETE FROM review_answer_sync_events WHERE cardId = :cardId")
    suspend fun deleteByCardId(cardId: String)

    @Query("DELETE FROM review_answer_sync_events")
    suspend fun clearAll()
}