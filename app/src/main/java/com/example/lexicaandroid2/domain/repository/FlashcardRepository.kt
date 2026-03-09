package com.example.lexicaandroid2.domain.repository

import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.Sm2Stats

interface FlashcardRepository {
    suspend fun getCardsToReview(limit: Int): List<Flashcard>
    suspend fun saveCard(card: Flashcard)
    suspend fun updateCardProgress(
        cardId: String,
        motVersDef: Sm2Stats,
        defVersMot: Sm2Stats
    )
    suspend fun setFavorite(cardId: String, isFavorite: Boolean)
    suspend fun deleteCard(cardId: String)
    suspend fun getStatsByState(): Map<String, Int>
    suspend fun getAllCards(): List<Flashcard>
}
