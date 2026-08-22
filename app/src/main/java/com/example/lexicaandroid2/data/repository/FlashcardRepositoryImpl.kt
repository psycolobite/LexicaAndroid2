package com.example.lexicaandroid2.data.repository

import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.ReviewQuestionDao
import com.example.lexicaandroid2.data.mapper.toEmbedded
import com.example.lexicaandroid2.data.mapper.toEntity
import com.example.lexicaandroid2.data.mapper.toReviewQuestionProgressEntities
import com.example.lexicaandroid2.data.mapper.toDomain
import com.example.lexicaandroid2.data.mapper.toDomain as toReviewQuestionDomain
import com.example.lexicaandroid2.domain.model.ReviewCardAggregateState
import com.example.lexicaandroid2.domain.model.ReviewCardProgressSummary
import com.example.lexicaandroid2.domain.model.Sm2Stats
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.repository.FlashcardRepository

class FlashcardRepositoryImpl(
    private val dao: FlashcardDao,
    private val reviewQuestionDao: ReviewQuestionDao
) : FlashcardRepository {
    override suspend fun getCardsToReview(limit: Int) =
        dao.getDue(System.currentTimeMillis(), limit).map { it.toDomain() }

    override suspend fun getQuestionProgress(questionId: String): ReviewQuestionProgress? =
        reviewQuestionDao.getByQuestionId(questionId)?.toReviewQuestionDomain()

    override suspend fun getQuestionProgressForCard(cardId: String): List<ReviewQuestionProgress> =
        reviewQuestionDao.getByCardId(cardId).map { it.toReviewQuestionDomain() }

    override suspend fun getAllQuestionProgress(): List<ReviewQuestionProgress> =
        reviewQuestionDao.getAll().map { it.toReviewQuestionDomain() }

    override suspend fun getStartedDueQuestionProgress(now: Long, limit: Int): List<ReviewQuestionProgress> =
        reviewQuestionDao.getStartedDueQuestions(now, limit).map { it.toReviewQuestionDomain() }

    override suspend fun countStartedDueQuestionProgress(now: Long): Int =
        reviewQuestionDao.countStartedDueQuestions(now)

    override suspend fun getNeverStartedQuestionProgress(limit: Int): List<ReviewQuestionProgress> =
        reviewQuestionDao.getNeverStartedQuestions(limit).map { it.toReviewQuestionDomain() }

    override suspend fun countNeverStartedQuestionProgress(): Int =
        reviewQuestionDao.countNeverStartedQuestions()

    override suspend fun saveQuestionProgress(progress: ReviewQuestionProgress) {
        reviewQuestionDao.insert(progress.toEntity())
    }

    override suspend fun saveQuestionProgress(progressList: List<ReviewQuestionProgress>) {
        reviewQuestionDao.insertAll(progressList.map { it.toEntity() })
    }

    override suspend fun saveCard(card: com.example.lexicaandroid2.domain.model.Flashcard) {
        val entity = card.toEntity()
        dao.insert(entity)
        reviewQuestionDao.insertAll(entity.toReviewQuestionProgressEntities())
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
        reviewQuestionDao.insertAll(updated.toReviewQuestionProgressEntities())
    }

    override suspend fun updateCardContent(card: com.example.lexicaandroid2.domain.model.Flashcard) {
        val existing = dao.getById(card.id) ?: return
        val updated = existing.copy(
            mot = card.recto,
            definition = card.verso,
            synonymes = card.synonymes,
            exemples = card.exemples,
            categorieGrammaticale = card.categorieGrammaticale,
            registre = card.registre,
            etymologie = card.etymologie,
            notesPersonnelles = card.notesPersonnelles
        )
        dao.update(updated)
        reviewQuestionDao.insertAll(updated.toReviewQuestionProgressEntities())
    }

    override suspend fun setFavorite(cardId: String, isFavorite: Boolean) {
        val existing = dao.getById(cardId) ?: return
        dao.update(existing.copy(favori = isFavorite))
    }

    override suspend fun deleteCard(cardId: String) {
        val existing = dao.getById(cardId) ?: return
        reviewQuestionDao.deleteByCardId(cardId)
        dao.delete(existing)
    }

    override suspend fun getStatsByState(): Map<String, Int> {
        val cards = dao.getAll().map { it.toDomain() }
        val summaries = ReviewCardProgressSummary.indexByCardId(
            cards = cards,
            questionProgress = reviewQuestionDao.getAll().map { it.toReviewQuestionDomain() }
        )

        val toWork = summaries.values.count { it.aggregateState == ReviewCardAggregateState.TO_WORK }
        val inProgress = summaries.values.count { it.aggregateState == ReviewCardAggregateState.IN_PROGRESS }
        val known = summaries.values.count { it.aggregateState == ReviewCardAggregateState.KNOWN }

        return mapOf(
            "TO_WORK" to toWork,
            "TO_LEARN" to toWork,
            "IN_PROGRESS" to inProgress,
            "LEARNING" to inProgress,
            "KNOWN" to known
        )
    }

    override suspend fun getAllCards(): List<com.example.lexicaandroid2.domain.model.Flashcard> {
        return dao.getAll().map { it.toDomain() }
    }

    override suspend fun deleteAllCards() {
        reviewQuestionDao.deleteAll()
        dao.deleteAll()
    }
}
