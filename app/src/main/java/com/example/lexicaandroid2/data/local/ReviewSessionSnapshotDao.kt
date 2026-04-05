package com.example.lexicaandroid2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReviewSessionSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(snapshot: ReviewSessionSnapshotEntity)

    @Query("SELECT * FROM review_session_snapshots WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getById(sessionId: String): ReviewSessionSnapshotEntity?

    @Query("DELETE FROM review_session_snapshots WHERE sessionId = :sessionId")
    suspend fun deleteById(sessionId: String)

    @Query("DELETE FROM review_session_snapshots")
    suspend fun clearAll()
}

