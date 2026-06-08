package com.example.lexicaandroid2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SyncResetMetadataDao {
    @Query("SELECT * FROM sync_reset_metadata WHERE userId = 'currentUser' LIMIT 1")
    suspend fun getCurrent(): SyncResetMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: SyncResetMetadataEntity)

    @Query("DELETE FROM sync_reset_metadata")
    suspend fun clearAll()
}