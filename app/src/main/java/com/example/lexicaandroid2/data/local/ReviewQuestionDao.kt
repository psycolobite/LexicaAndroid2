package com.example.lexicaandroid2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReviewQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: ReviewQuestionProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progresses: List<ReviewQuestionProgressEntity>)

    @Query("SELECT * FROM review_question_progress WHERE questionId = :questionId")
    suspend fun getByQuestionId(questionId: String): ReviewQuestionProgressEntity?

    @Query(
        """
        SELECT * FROM review_question_progress
        WHERE cardId = :cardId
        ORDER BY globalOrder ASC, questionType ASC
        """
    )
    suspend fun getByCardId(cardId: String): List<ReviewQuestionProgressEntity>

    @Query(
        """
        SELECT * FROM review_question_progress
        WHERE firstAnsweredAt IS NOT NULL
          AND nextDueAt <= :now
        ORDER BY nextDueAt ASC, globalOrder ASC, cardId ASC, questionType ASC
        LIMIT :limit
        """
    )
    suspend fun getStartedDueQuestions(now: Long, limit: Int): List<ReviewQuestionProgressEntity>

    @Query(
        """
        SELECT COUNT(*) FROM review_question_progress
        WHERE firstAnsweredAt IS NOT NULL
          AND nextDueAt <= :now
        """
    )
    suspend fun countStartedDueQuestions(now: Long): Int

    @Query(
        """
        SELECT * FROM review_question_progress
        WHERE firstAnsweredAt IS NULL
        ORDER BY globalOrder ASC, cardId ASC, questionType ASC
        LIMIT :limit
        """
    )
    suspend fun getNeverStartedQuestions(limit: Int): List<ReviewQuestionProgressEntity>

    @Query(
        """
        SELECT COUNT(*) FROM review_question_progress
        WHERE firstAnsweredAt IS NULL
        """
    )
    suspend fun countNeverStartedQuestions(): Int

    @Query("SELECT * FROM review_question_progress ORDER BY globalOrder ASC, cardId ASC, questionType ASC")
    suspend fun getAll(): List<ReviewQuestionProgressEntity>

    @Query("SELECT questionId FROM review_question_progress")
    suspend fun getAllQuestionIds(): List<String>

    @Query("DELETE FROM review_question_progress WHERE cardId = :cardId")
    suspend fun deleteByCardId(cardId: String)

    /** Efface TOUTE la progression des questions (réinitialisation complète). */
    @Query("DELETE FROM review_question_progress")
    suspend fun deleteAll()
}
