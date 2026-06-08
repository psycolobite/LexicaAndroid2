package com.example.lexicaandroid2.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcard_sync_state",
    indices = [Index(value = ["deletedAt"], name = "index_flashcard_sync_state_deletedAt")]
)
data class FlashcardSyncStateEntity(
    @PrimaryKey
    val cardId: String,
    @ColumnInfo(defaultValue = "0")
    val lastModifiedAt: Long = 0L,
    @ColumnInfo(defaultValue = "0")
    val favoriteUpdatedAt: Long = 0L,
    val deletedAt: Long? = null
)