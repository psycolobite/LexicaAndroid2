package com.example.lexicaandroid2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lexicaandroid2.features.gamification.data.UserStatsDao
import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStat
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao

@Database(
    entities = [FlashcardEntity::class, WordReserveEntity::class, UserStatsEntity::class, DailyReviewStat::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LexicaDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
    abstract fun wordReserveDao(): WordReserveDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun dailyReviewStatDao(): DailyReviewStatDao

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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_review_stats (
                        dateKey TEXT PRIMARY KEY NOT NULL,
                        cardsReviewed INTEGER NOT NULL DEFAULT 0,
                        correctAnswers INTEGER NOT NULL DEFAULT 0,
                        totalTimeSeconds INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
