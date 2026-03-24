package com.example.lexicaandroid2.features.gamification.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper pour incrémenter les statistiques de révision quotidiennes.
 *
 * Usage dans ReviewViewModel (à intégrer par le Chef d'Orchestre) :
 *
 * ```kotlin
 * // Dans gradeCard(), après repository.updateCardProgress(...) :
 * dailyStatDao?.let { dao ->
 *     DailyReviewStatHelper.recordReview(dao, isCorrect = quality >= 3)
 * }
 * ```
 *
 * NOTE : DailyReviewStatDao est injecté de façon nullable pour rester
 * rétro-compatible avant la migration DB (version 4 → 5).
 */
object DailyReviewStatHelper {

    /**
     * Incrémente les stats du jour pour une révision.
     *
     * @param dao      Le DAO Room (non null uniquement après migration DB v5)
     * @param isCorrect true si la carte a été correctement révisée (quality >= 3)
     */
    suspend fun recordReview(dao: DailyReviewStatDao, isCorrect: Boolean) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existing = dao.getByDate(today) ?: DailyReviewStat(dateKey = today)
        dao.upsert(
            existing.copy(
                cardsReviewed = existing.cardsReviewed + 1,
                correctAnswers = existing.correctAnswers + if (isCorrect) 1 else 0
            )
        )
    }
}


