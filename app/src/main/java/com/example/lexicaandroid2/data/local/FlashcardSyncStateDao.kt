package com.example.lexicaandroid2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FlashcardSyncStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: FlashcardSyncStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(states: List<FlashcardSyncStateEntity>)

    @Query("SELECT * FROM flashcard_sync_state WHERE cardId = :cardId")
    suspend fun getByCardId(cardId: String): FlashcardSyncStateEntity?

    @Query("SELECT * FROM flashcard_sync_state")
    suspend fun getAll(): List<FlashcardSyncStateEntity>

    @Query("DELETE FROM flashcard_sync_state")
    suspend fun clearAll()
}