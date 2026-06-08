package com.example.lexicaandroid2.data.repository

import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.FlashcardSyncStateDao
import com.example.lexicaandroid2.data.local.FlashcardSyncStateEntity
import com.example.lexicaandroid2.data.local.ReviewQuestionDao
import com.example.lexicaandroid2.data.local.ReviewAnswerSyncEventDao
import com.example.lexicaandroid2.data.local.ReviewAnswerSyncEventEntity
import com.example.lexicaandroid2.data.mapper.toEmbedded
import com.example.lexicaandroid2.data.mapper.toEntity
import com.example.lexicaandroid2.data.mapper.toReviewQuestionProgressEntities
import com.example.lexicaandroid2.data.mapper.toDomain
import com.example.lexicaandroid2.data.mapper.toDomain as toReviewQuestionDomain
import com.example.lexicaandroid2.domain.model.ReviewCardAggregateState
import com.example.lexicaandroid2.domain.model.ReviewCardProgressSummary
import com.example.lexicaandroid2.domain.model.ReviewAnswerSyncEvent
import com.example.lexicaandroid2.domain.model.Sm2Stats
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.repository.FlashcardRepository

class FlashcardRepositoryImpl(
    private val dao: FlashcardDao,
    private val reviewQuestionDao: ReviewQuestionDao,
    private val flashcardSyncStateDao: FlashcardSyncStateDao? = null,
    private val reviewAnswerSyncEventDao: ReviewAnswerSyncEventDao? = null,
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
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
        val now = nowProvider()
        flashcardSyncStateDao?.upsert(
            FlashcardSyncStateEntity(
                cardId = entity.id,
                lastModifiedAt = now,
                favoriteUpdatedAt = if (entity.favori) now else 0L,
                deletedAt = null
            )
        )
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
        touchCard(updated.id)
    }

    override suspend fun updateCardContent(card: com.example.lexicaandroid2.domain.model.Flashcard) {
        val existing = dao.getById(card.id) ?: return
        dao.update(
            card.copy(
                dateAjout = existing.dateAjout,
                sm2MotVersDef = existing.sm2MotVersDef.toDomain(),
                sm2DefVersMot = existing.sm2DefVersMot.toDomain(),
                favori = existing.favori
            ).toEntity()
        )
        touchCard(card.id)
    }

    override suspend fun setFavorite(cardId: String, isFavorite: Boolean) {
        val existing = dao.getById(cardId) ?: return
        dao.update(existing.copy(favori = isFavorite))
        val now = nowProvider()
        val state = flashcardSyncStateDao?.getByCardId(cardId)
        flashcardSyncStateDao?.upsert(
            FlashcardSyncStateEntity(
                cardId = cardId,
                lastModifiedAt = maxOf(state?.lastModifiedAt ?: existing.dateAjout, now),
                favoriteUpdatedAt = now,
                deletedAt = null
            )
        )
    }

    override suspend fun deleteCard(cardId: String) {
        val existing = dao.getById(cardId) ?: return
        val now = nowProvider()
        val state = flashcardSyncStateDao?.getByCardId(cardId)
        flashcardSyncStateDao?.upsert(
            FlashcardSyncStateEntity(
                cardId = cardId,
                lastModifiedAt = maxOf(state?.lastModifiedAt ?: existing.dateAjout, now),
                favoriteUpdatedAt = state?.favoriteUpdatedAt ?: if (existing.favori) existing.dateAjout else 0L,
                deletedAt = now
            )
        )
        reviewQuestionDao.deleteByCardId(cardId)
        reviewAnswerSyncEventDao?.deleteByCardId(cardId)
        dao.delete(existing)
    }

    override suspend fun appendReviewAnswerSyncEvent(event: ReviewAnswerSyncEvent) {
        reviewAnswerSyncEventDao?.insert(event.toEntity())
        compactReviewAnswerEventsIfNeeded()
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
        val deletedAt = nowProvider()
        flashcardSyncStateDao?.upsertAll(
            dao.getAll().map { existing ->
                val state = flashcardSyncStateDao.getByCardId(existing.id)
                FlashcardSyncStateEntity(
                    cardId = existing.id,
                    lastModifiedAt = maxOf(state?.lastModifiedAt ?: existing.dateAjout, deletedAt),
                    favoriteUpdatedAt = state?.favoriteUpdatedAt ?: if (existing.favori) existing.dateAjout else 0L,
                    deletedAt = deletedAt
                )
            }
        )
        reviewQuestionDao.deleteAll()
        reviewAnswerSyncEventDao?.clearAll()
        dao.deleteAll()
    }

    private suspend fun touchCard(cardId: String) {
        val now = nowProvider()
        val state = flashcardSyncStateDao?.getByCardId(cardId)
        flashcardSyncStateDao?.upsert(
            FlashcardSyncStateEntity(
                cardId = cardId,
                lastModifiedAt = maxOf(state?.lastModifiedAt ?: 0L, now),
                favoriteUpdatedAt = state?.favoriteUpdatedAt ?: 0L,
                deletedAt = null
            )
        )
    }

    private suspend fun compactReviewAnswerEventsIfNeeded() {
        val eventDao = reviewAnswerSyncEventDao ?: return
        val events = eventDao.getAll()
        if (events.size <= MAX_REVIEW_SYNC_EVENT_COUNT) return

        val compacted = (
            events.takeLast(REVIEW_RECENT_EVENTS_TO_KEEP) +
                events.groupBy(ReviewAnswerSyncEventEntity::questionId)
                    .values
                    .flatMap { questionEvents -> questionEvents.takeLast(REVIEW_EVENTS_PER_QUESTION_TO_KEEP) }
            )
            .distinctBy(ReviewAnswerSyncEventEntity::eventId)
            .sortedWith(compareBy<ReviewAnswerSyncEventEntity> { it.answeredAt }.thenBy { it.eventId })

        if (compacted.size >= events.size) return

        eventDao.clearAll()
        eventDao.insertAll(compacted)
    }
}

private const val MAX_REVIEW_SYNC_EVENT_COUNT = 400
private const val REVIEW_RECENT_EVENTS_TO_KEEP = 120
private const val REVIEW_EVENTS_PER_QUESTION_TO_KEEP = 2

private fun ReviewAnswerSyncEvent.toEntity(): ReviewAnswerSyncEventEntity = ReviewAnswerSyncEventEntity(
    eventId = eventId,
    sessionId = sessionId,
    questionId = questionId,
    cardId = cardId,
    questionType = questionType.name,
    answer = answer.name,
    answeredAt = answeredAt,
    challengeKind = challengeKind?.name
)
