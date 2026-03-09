package com.example.lexicaandroid2.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WordReserveDao {
    // Get words that are NOT marked as added to collection.
    // In strict architectural separation, we might check validation against flashcards table,
    // but here we use a flag or just deleting them from reserve when added.
    // The requirement says: "getAvailableWords (exclut mots déjà dans collection)".
    // Ideally this implies a LEFT JOIN or NOT EXISTS, but since they are in different tables,
    // we can manage it by deleting from reserve when moving to flashcards, OR keep them and check IDs.
    // Simpler approach: Delete from reserve when added.

    @Query("SELECT * FROM word_reserve LIMIT :limit")
    suspend fun getAvailableWords(limit: Int = 100): List<WordReserveEntity>

    @Query("SELECT COUNT(*) FROM word_reserve")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordReserveEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordReserveEntity)

    @Delete
    suspend fun delete(word: WordReserveEntity)

    @Query("DELETE FROM word_reserve WHERE id = :id")
    suspend fun deleteById(id: String)
}

