package com.example.lexicaandroid2.domain.usecase

import com.example.lexicaandroid2.data.local.ReviewSessionSnapshotDao
import com.example.lexicaandroid2.data.local.SyncResetMetadataDao
import com.example.lexicaandroid2.data.local.SyncResetMetadataEntity
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository

/**
 * Orchestrate la réinitialisation COMPLÈTE de la progression utilisateur :
 * - Suppression de tous les mots appris (flashcards + progression SM2)
 * - Suppression des snapshots de session de révision
 * - Remise à zéro des stats (XP, niveau, streak)
 * - Suppression des statistiques journalières
 */
class ResetProgressUseCase(
    private val flashcardRepository: FlashcardRepository,
    private val reviewSessionSnapshotDao: ReviewSessionSnapshotDao,
    private val userStatsRepository: UserStatsRepository,
    private val dailyReviewStatDao: DailyReviewStatDao? = null,
    private val syncResetMetadataDao: SyncResetMetadataDao? = null,
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) {
    suspend operator fun invoke() {
        val resetAt = nowProvider()
        // 1. Supprime tous les mots et la progression associée (review_question_progress)
        flashcardRepository.deleteAllCards()
        // 2. Supprime les snapshots de sessions en cours
        reviewSessionSnapshotDao.clearAll()
        // 3. Remet à zéro XP, niveau et streak
        userStatsRepository.resetStats()
        // 4. Efface les statistiques journalières (si disponibles)
        dailyReviewStatDao?.clearAll()
        val currentGeneration = syncResetMetadataDao?.getCurrent()?.resetGeneration ?: 0L
        syncResetMetadataDao?.upsert(
            SyncResetMetadataEntity(
                resetAt = resetAt,
                resetGeneration = currentGeneration + 1L
            )
        )
    }
}

