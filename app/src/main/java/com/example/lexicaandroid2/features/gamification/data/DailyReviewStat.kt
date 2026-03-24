package com.example.lexicaandroid2.features.gamification.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Enregistre les statistiques de révision par jour (style Anki).
 * La clé primaire est la date au format "YYYY-MM-DD".
 */
@Entity(tableName = "daily_review_stats")
data class DailyReviewStat(
    @PrimaryKey val dateKey: String,          // Format "YYYY-MM-DD"
    val cardsReviewed: Int = 0,               // Total de cartes révisées dans la journée
    val correctAnswers: Int = 0,              // Réponses correctes (quality >= 3)
    val totalTimeSeconds: Int = 0             // Temps total passé à réviser (extensible)
)

