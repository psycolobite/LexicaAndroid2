package com.example.lexicaandroid2.data.repository

import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.ReviewQuestionDao
import com.example.lexicaandroid2.data.local.WordReserveDao
import com.example.lexicaandroid2.data.local.WordReserveEntity
import com.example.lexicaandroid2.data.mapper.toEntity
import com.example.lexicaandroid2.data.mapper.toReviewQuestionProgressEntities
import com.example.lexicaandroid2.data.remote.DictionaryService
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.WordReserveRepository
import java.util.UUID

class WordReserveRepositoryImpl(
    private val reserveDao: WordReserveDao,
    private val flashcardDao: FlashcardDao,
    private val reviewQuestionDao: ReviewQuestionDao,
    private val dictionaryService: DictionaryService
) : WordReserveRepository {

    override suspend fun getProposedWords(limit: Int): List<WordReserveEntity> {
        return reserveDao.getAvailableWords(limit)
    }

    override suspend fun countAvailable(): Int {
        return reserveDao.count()
    }

    override suspend fun addToCollection(word: WordReserveEntity) {
        // Create FlashcardEntity from ReserveEntity
        val flashcard = com.example.lexicaandroid2.data.local.FlashcardEntity(
            id = word.id,
            mot = word.mot,
            definition = word.definition,
            synonymes = word.synonymes,
            exemples = word.exemples,
            categorieGrammaticale = word.categorieGrammaticale,
            dateAjout = System.currentTimeMillis(),
            state = "TO_LEARN" // Start as new
        )
        flashcardDao.insert(flashcard)
        reviewQuestionDao.insertAll(flashcard.toReviewQuestionProgressEntities())
        reserveDao.delete(word) // Remove from reserve
    }

    override suspend fun searchOnline(query: String): List<WordReserveEntity> {
        // Call API
        try {
            val results = dictionaryService.searchWord(query)
            return results.map { res ->
                WordReserveEntity(
                    id = UUID.randomUUID().toString(),
                    mot = res.mot,
                    definition = res.definition,
                    synonymes = res.synonymes,
                    exemples = res.exemples,
                    categorieGrammaticale = res.categorieGrammaticale,
                    fromApi = true
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }
    }

    override suspend fun addCustomWordToCollection(flashcard: Flashcard) {
        val entity = flashcard.toEntity()
        flashcardDao.insert(entity)
        reviewQuestionDao.insertAll(entity.toReviewQuestionProgressEntities())
    }
}

