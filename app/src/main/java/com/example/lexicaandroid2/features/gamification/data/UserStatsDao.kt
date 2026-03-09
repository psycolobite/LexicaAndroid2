package com.example.lexicaandroid2.features.gamification.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {

    @Query("SELECT * FROM user_stats WHERE userId = :userId")
    fun getUserStats(userId: String = "currentUser"): Flow<UserStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStatsEntity)

    @Update
    suspend fun update(stats: UserStatsEntity)
}

