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
    entities = [FlashcardEntity::class, WordReserveEntity::class, UserStatsEntity::class, DailyReviewStat::class, ReviewQuestionProgressEntity::class, ReviewSessionSnapshotEntity::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LexicaDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
    abstract fun reviewQuestionDao(): ReviewQuestionDao
    @Suppress("unused")
    abstract fun reviewSessionSnapshotDao(): ReviewSessionSnapshotDao
    abstract fun wordReserveDao(): WordReserveDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun dailyReviewStatDao(): DailyReviewStatDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
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
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS review_question_progress (
                        questionId TEXT NOT NULL PRIMARY KEY,
                        cardId TEXT NOT NULL,
                        questionType TEXT NOT NULL,
                        globalOrder INTEGER NOT NULL,
                        level REAL NOT NULL DEFAULT 0,
                        intervalIndex INTEGER NOT NULL DEFAULT 0,
                        peakIntervalIndex INTEGER NOT NULL DEFAULT 0,
                        weightedSuccess REAL NOT NULL DEFAULT 0,
                        weightedFailure REAL NOT NULL DEFAULT 0,
                        recentStreak INTEGER NOT NULL DEFAULT 0,
                        recoveryReserve REAL NOT NULL DEFAULT 0,
                        currentIntervalDurationMs INTEGER NOT NULL,
                        nextDueAt INTEGER NOT NULL,
                        lastSessionFirstAnswerAt INTEGER,
                        lastAskedAt INTEGER,
                        firstAnsweredAt INTEGER
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_review_question_progress_cardId ON review_question_progress(cardId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_review_question_progress_nextDueAt ON review_question_progress(nextDueAt)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_review_question_progress_cardId_questionType ON review_question_progress(cardId, questionType)"
                )

                db.execSQL(
                    """
                    INSERT OR IGNORE INTO review_question_progress (
                        questionId,
                        cardId,
                        questionType,
                        globalOrder,
                        level,
                        intervalIndex,
                        peakIntervalIndex,
                        weightedSuccess,
                        weightedFailure,
                        recentStreak,
                        recoveryReserve,
                        currentIntervalDurationMs,
                        nextDueAt,
                        lastSessionFirstAnswerAt,
                        lastAskedAt,
                        firstAnsweredAt
                    )
                    SELECT
                        id || '::WORD_TO_DEFINITION',
                        id,
                        'WORD_TO_DEFINITION',
                        (dateAjout * 2),
                        CAST(
                            CASE
                                WHEN sm2_mot_vers_def_interval <= 0 THEN 0
                                WHEN sm2_mot_vers_def_interval < 7 THEN 2
                                WHEN sm2_mot_vers_def_interval < 30 THEN 3
                                WHEN sm2_mot_vers_def_interval < 90 THEN 4
                                WHEN sm2_mot_vers_def_interval < 180 THEN 5
                                WHEN sm2_mot_vers_def_interval < 365 THEN 6
                                WHEN sm2_mot_vers_def_interval < 730 THEN 7
                                ELSE 8
                            END AS REAL
                        ),
                        CASE
                            WHEN sm2_mot_vers_def_interval <= 0 THEN 0
                            WHEN sm2_mot_vers_def_interval < 7 THEN 2
                            WHEN sm2_mot_vers_def_interval < 30 THEN 3
                            WHEN sm2_mot_vers_def_interval < 90 THEN 4
                            WHEN sm2_mot_vers_def_interval < 180 THEN 5
                            WHEN sm2_mot_vers_def_interval < 365 THEN 6
                            WHEN sm2_mot_vers_def_interval < 730 THEN 7
                            ELSE 8
                        END,
                        CASE
                            WHEN sm2_mot_vers_def_interval <= 0 THEN 0
                            WHEN sm2_mot_vers_def_interval < 7 THEN 2
                            WHEN sm2_mot_vers_def_interval < 30 THEN 3
                            WHEN sm2_mot_vers_def_interval < 90 THEN 4
                            WHEN sm2_mot_vers_def_interval < 180 THEN 5
                            WHEN sm2_mot_vers_def_interval < 365 THEN 6
                            WHEN sm2_mot_vers_def_interval < 730 THEN 7
                            ELSE 8
                        END,
                        CAST(sm2_mot_vers_def_correctReviews AS REAL),
                        CAST(sm2_mot_vers_def_lapses AS REAL),
                        sm2_mot_vers_def_repetitions,
                        0,
                        CASE
                            WHEN sm2_mot_vers_def_lastReview IS NOT NULL AND sm2_mot_vers_def_nextReview > sm2_mot_vers_def_lastReview
                                THEN sm2_mot_vers_def_nextReview - sm2_mot_vers_def_lastReview
                            WHEN sm2_mot_vers_def_interval > 0
                                THEN sm2_mot_vers_def_interval * 86400000
                            ELSE 600000
                        END,
                        sm2_mot_vers_def_nextReview,
                        sm2_mot_vers_def_lastReview,
                        sm2_mot_vers_def_lastReview,
                        CASE
                            WHEN sm2_mot_vers_def_totalReviews > 0 OR sm2_mot_vers_def_repetitions > 0
                                THEN COALESCE(sm2_mot_vers_def_lastReview, dateAjout)
                            ELSE NULL
                        END
                    FROM flashcards
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT OR IGNORE INTO review_question_progress (
                        questionId,
                        cardId,
                        questionType,
                        globalOrder,
                        level,
                        intervalIndex,
                        peakIntervalIndex,
                        weightedSuccess,
                        weightedFailure,
                        recentStreak,
                        recoveryReserve,
                        currentIntervalDurationMs,
                        nextDueAt,
                        lastSessionFirstAnswerAt,
                        lastAskedAt,
                        firstAnsweredAt
                    )
                    SELECT
                        id || '::DEFINITION_TO_WORD',
                        id,
                        'DEFINITION_TO_WORD',
                        (dateAjout * 2) + 1,
                        CAST(
                            CASE
                                WHEN sm2_def_vers_mot_interval <= 0 THEN 0
                                WHEN sm2_def_vers_mot_interval < 7 THEN 2
                                WHEN sm2_def_vers_mot_interval < 30 THEN 3
                                WHEN sm2_def_vers_mot_interval < 90 THEN 4
                                WHEN sm2_def_vers_mot_interval < 180 THEN 5
                                WHEN sm2_def_vers_mot_interval < 365 THEN 6
                                WHEN sm2_def_vers_mot_interval < 730 THEN 7
                                ELSE 8
                            END AS REAL
                        ),
                        CASE
                            WHEN sm2_def_vers_mot_interval <= 0 THEN 0
                            WHEN sm2_def_vers_mot_interval < 7 THEN 2
                            WHEN sm2_def_vers_mot_interval < 30 THEN 3
                            WHEN sm2_def_vers_mot_interval < 90 THEN 4
                            WHEN sm2_def_vers_mot_interval < 180 THEN 5
                            WHEN sm2_def_vers_mot_interval < 365 THEN 6
                            WHEN sm2_def_vers_mot_interval < 730 THEN 7
                            ELSE 8
                        END,
                        CASE
                            WHEN sm2_def_vers_mot_interval <= 0 THEN 0
                            WHEN sm2_def_vers_mot_interval < 7 THEN 2
                            WHEN sm2_def_vers_mot_interval < 30 THEN 3
                            WHEN sm2_def_vers_mot_interval < 90 THEN 4
                            WHEN sm2_def_vers_mot_interval < 180 THEN 5
                            WHEN sm2_def_vers_mot_interval < 365 THEN 6
                            WHEN sm2_def_vers_mot_interval < 730 THEN 7
                            ELSE 8
                        END,
                        CAST(sm2_def_vers_mot_correctReviews AS REAL),
                        CAST(sm2_def_vers_mot_lapses AS REAL),
                        sm2_def_vers_mot_repetitions,
                        0,
                        CASE
                            WHEN sm2_def_vers_mot_lastReview IS NOT NULL AND sm2_def_vers_mot_nextReview > sm2_def_vers_mot_lastReview
                                THEN sm2_def_vers_mot_nextReview - sm2_def_vers_mot_lastReview
                            WHEN sm2_def_vers_mot_interval > 0
                                THEN sm2_def_vers_mot_interval * 86400000
                            ELSE 600000
                        END,
                        sm2_def_vers_mot_nextReview,
                        sm2_def_vers_mot_lastReview,
                        sm2_def_vers_mot_lastReview,
                        CASE
                            WHEN sm2_def_vers_mot_totalReviews > 0 OR sm2_def_vers_mot_repetitions > 0
                                THEN COALESCE(sm2_def_vers_mot_lastReview, dateAjout)
                            ELSE NULL
                        END
                    FROM flashcards
                    """.trimIndent()
                )
            }
        }

        @Suppress("unused")
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS review_session_snapshots (
                        sessionId TEXT NOT NULL PRIMARY KEY,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        payloadJson TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        @Suppress("unused")
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE review_question_progress ADD COLUMN pendingReplacementChallengeKind TEXT"
                )
            }
        }
    }
}
