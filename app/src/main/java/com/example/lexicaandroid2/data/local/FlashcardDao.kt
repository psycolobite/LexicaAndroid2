package com.example.lexicaandroid2.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(flashcard: FlashcardEntity)

    @Update
    suspend fun update(flashcard: FlashcardEntity)

    @Delete
    suspend fun delete(flashcard: FlashcardEntity)

    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun getById(id: String): FlashcardEntity?

    @Query("SELECT * FROM flashcards")
    suspend fun getAll(): List<FlashcardEntity>

    @Query(
        """
        SELECT * FROM flashcards
        WHERE sm2_mot_vers_def_nextReview <= :now
           OR sm2_def_vers_mot_nextReview <= :now
        ORDER BY CASE
            WHEN sm2_mot_vers_def_nextReview < sm2_def_vers_mot_nextReview
                THEN sm2_mot_vers_def_nextReview
            ELSE sm2_def_vers_mot_nextReview
        END ASC
        LIMIT :limit
        """
    )
    suspend fun getDue(now: Long, limit: Int): List<FlashcardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flashcards: List<FlashcardEntity>)

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun count(): Int

    @Query("SELECT state, COUNT(*) as count FROM flashcards GROUP BY state")
    suspend fun getStatsByState(): Map<@MapColumn(columnName = "state") String, @MapColumn(columnName = "count") Int>

    // ========== REQUÊTES DE RECHERCHE OPTIMISÉES ==========
    
    @Query("""
        SELECT * FROM flashcards 
        WHERE LOWER(mot) LIKE '%' || LOWER(:query) || '%'
        ORDER BY LENGTH(mot) ASC
        LIMIT :limit
    """)
    fun searchByWord(query: String, limit: Int = 50): Flow<List<FlashcardEntity>>

    @Query("""
        SELECT * FROM flashcards 
        WHERE LOWER(definition) LIKE '%' || LOWER(:query) || '%'
        ORDER BY LENGTH(mot) ASC
        LIMIT :limit
    """)
    fun searchByDefinition(query: String, limit: Int = 50): Flow<List<FlashcardEntity>>

    @Query("""
        SELECT * FROM flashcards 
        WHERE LOWER(mot) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(definition) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(synonymes) LIKE '%' || LOWER(:query) || '%'
        ORDER BY 
            CASE 
                WHEN LOWER(mot) = LOWER(:query) THEN 0
                WHEN LOWER(mot) LIKE LOWER(:query) || '%' THEN 1
                ELSE 2
            END,
            LENGTH(mot) ASC
        LIMIT :limit
    """)
    fun searchGlobal(query: String, limit: Int = 50): Flow<List<FlashcardEntity>>

    @Query("""
        SELECT * FROM flashcards 
        WHERE favori = 1 
          AND (LOWER(mot) LIKE '%' || LOWER(:query) || '%'
               OR LOWER(definition) LIKE '%' || LOWER(:query) || '%')
        ORDER BY LENGTH(mot) ASC
        LIMIT :limit
    """)
    fun searchFavorites(query: String, limit: Int = 50): Flow<List<FlashcardEntity>>

    @Query("""
        SELECT * FROM flashcards 
        ORDER BY dateAjout DESC
        LIMIT :limit OFFSET :offset
    """)
    fun getAllPaginated(limit: Int = 50, offset: Int = 0): Flow<List<FlashcardEntity>>

    @Query("""
        SELECT COUNT(*) FROM flashcards 
        WHERE LOWER(mot) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(definition) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(synonymes) LIKE '%' || LOWER(:query) || '%'
    """)
    suspend fun countSearchResults(query: String): Int

    /** Efface TOUS les flashcards (réinitialisation complète). */
    @Query("DELETE FROM flashcards")
    suspend fun deleteAll()

    /** Retourne uniquement les mots (colonne mot) — utilisé pour le check anti-doublon du refill. */
    @Query("SELECT mot FROM flashcards")
    suspend fun getAllMots(): List<String>
}
