package com.example.lexicaandroid2.data.repository

import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.mapper.toDomain
import com.example.lexicaandroid2.data.mapper.toEmbedded
import com.example.lexicaandroid2.data.mapper.toEntity
import com.example.lexicaandroid2.domain.model.Sm2Stats
import com.example.lexicaandroid2.domain.repository.FlashcardRepository

class FlashcardRepositoryImpl(
    private val dao: FlashcardDao
) : FlashcardRepository {
    override suspend fun getCardsToReview(limit: Int) =
        dao.getDue(System.currentTimeMillis(), limit).map { it.toDomain() }

    override suspend fun saveCard(card: com.example.lexicaandroid2.domain.model.Flashcard) {
        dao.insert(card.toEntity())
    }

    override suspend fun updateCardProgress(
        cardId: String,
        motVersDef: Sm2Stats,
        defVersMot: Sm2Stats
    ) {
        val existing = dao.getById(cardId) ?: return
        val updated = existing.copy(
            sm2MotVersDef = motVersDef.toEmbedded(),
            sm2DefVersMot = defVersMot.toEmbedded()
        )
        dao.update(updated)
    }

    override suspend fun setFavorite(cardId: String, isFavorite: Boolean) {
        val existing = dao.getById(cardId) ?: return
        dao.update(existing.copy(favori = isFavorite))
    }

    override suspend fun deleteCard(cardId: String) {
        val existing = dao.getById(cardId) ?: return
        dao.delete(existing)
    }

    override suspend fun getStatsByState(): Map<String, Int> {
        val stats = dao.getStatsByState()
        // Ensure all keys exist with 0 if missing
        return mapOf(
            "TO_LEARN" to (stats["TO_LEARN"] ?: 0),
            "LEARNING" to (stats["LEARNING"] ?: 0),
            "KNOWN" to (stats["KNOWN"] ?: 0)
        )
    }

    override suspend fun getAllCards(): List<com.example.lexicaandroid2.domain.model.Flashcard> {
        return dao.getAll().map { it.toDomain() }
    }
}
