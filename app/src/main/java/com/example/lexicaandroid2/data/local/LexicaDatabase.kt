package com.example.lexicaandroid2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lexicaandroid2.features.gamification.data.UserStatsDao
import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity

@Database(
    entities = [FlashcardEntity::class, WordReserveEntity::class, UserStatsEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LexicaDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
    abstract fun wordReserveDao(): WordReserveDao
    abstract fun userStatsDao(): UserStatsDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS user_stats (
                        userId TEXT PRIMARY KEY NOT NULL DEFAULT 'currentUser',
                        xp INTEGER NOT NULL DEFAULT 0,
                        level INTEGER NOT NULL DEFAULT 1,
                        streak INTEGER NOT NULL DEFAULT 0,
                        lastLoginDate INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
