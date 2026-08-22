package com.example.lexicaandroid2.domain.repository

import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewAnswerSyncEvent
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.model.Sm2Stats

interface FlashcardRepository {
    suspend fun getCardsToReview(limit: Int): List<Flashcard>
    suspend fun getQuestionProgress(questionId: String): ReviewQuestionProgress?
    suspend fun getQuestionProgressForCard(cardId: String): List<ReviewQuestionProgress>
    suspend fun getAllQuestionProgress(): List<ReviewQuestionProgress>
    suspend fun getStartedDueQuestionProgress(now: Long, limit: Int): List<ReviewQuestionProgress>
    suspend fun countStartedDueQuestionProgress(now: Long): Int
    suspend fun getNeverStartedQuestionProgress(limit: Int): List<ReviewQuestionProgress>
    suspend fun countNeverStartedQuestionProgress(): Int
    suspend fun saveQuestionProgress(progress: ReviewQuestionProgress)
    suspend fun saveQuestionProgress(progressList: List<ReviewQuestionProgress>)
    suspend fun saveCard(card: Flashcard)
    suspend fun updateCardProgress(
        cardId: String,
        motVersDef: Sm2Stats,
        defVersMot: Sm2Stats
    )
    suspend fun updateCardContent(card: Flashcard)
    suspend fun setFavorite(cardId: String, isFavorite: Boolean)
    suspend fun deleteCard(cardId: String)
    suspend fun appendReviewAnswerSyncEvent(event: ReviewAnswerSyncEvent)
    suspend fun getStatsByState(): Map<String, Int>
    suspend fun getAllCards(): List<Flashcard>

    /** Efface TOUS les mots et toute leur progression (réinitialisation complète). */
    suspend fun deleteAllCards()
}
