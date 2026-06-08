package com.example.lexicaandroid2.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_reset_metadata")
data class SyncResetMetadataEntity(
    @PrimaryKey
    val userId: String = CURRENT_USER_ID,
    @ColumnInfo(defaultValue = "0")
    val resetAt: Long = 0L,
    @ColumnInfo(defaultValue = "0")
    val resetGeneration: Long = 0L
) {
    companion object {
        const val CURRENT_USER_ID = "currentUser"
    }
}