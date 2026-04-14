package com.example.lexicaandroid2.features.gamification.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyReviewStatDao {

    /** Retourne les 30 derniers jours triés du plus récent au plus ancien. */
    @Query("SELECT * FROM daily_review_stats ORDER BY dateKey DESC LIMIT 30")
    fun getLast30Days(): Flow<List<DailyReviewStat>>

    /** Insert ou remplace un enregistrement existant (upsert). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stat: DailyReviewStat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stats: List<DailyReviewStat>)

    /** Récupère les stats d'une date précise (ou null si aucune révision ce jour). */
    @Query("SELECT * FROM daily_review_stats WHERE dateKey = :date")
    suspend fun getByDate(date: String): DailyReviewStat?

    @Query("SELECT * FROM daily_review_stats ORDER BY dateKey ASC")
    suspend fun getAllOnce(): List<DailyReviewStat>

    /** Efface toutes les stats quotidiennes (usage admin uniquement). */
    @Query("DELETE FROM daily_review_stats")
    suspend fun clearAll()
}

