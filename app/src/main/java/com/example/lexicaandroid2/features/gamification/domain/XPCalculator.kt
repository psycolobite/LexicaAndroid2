package com.example.lexicaandroid2.features.gamification.domain

/**
 * XPCalculator - Calcule les points d'expérience selon différentes actions
 * 
 * Règles de gamification:
 * - Apprentissage d'un nouveau mot: 10 XP
 * - Révision réussie: 5 XP
 * - Série quotidienne (streak bonus): 20 XP par jour
 * - Complétion d'un mini-jeu: 15 XP
 * - Niveau parfait dans un jeu: +10 XP bonus
 */
object XPCalculator {
    
    /**
     * XP pour l'apprentissage d'un nouveau mot
     */
    const val XP_WORD_LEARNED = 10
    
    /**
     * XP pour une révision réussie
     */
    const val XP_WORD_REVIEWED = 5
    
    /**
     * XP bonus pour la série quotidienne
     */
    const val XP_DAILY_STREAK_BONUS = 20
    
    /**
     * XP pour la complétion d'un mini-jeu
     */
    const val XP_GAME_COMPLETED = 15
    
    /**
     * XP bonus pour un score parfait dans un jeu
     */
    const val XP_PERFECT_SCORE_BONUS = 10
    
    /**
     * Calcule le niveau basé sur l'XP total
     * Formule: Pour atteindre le niveau N, il faut 100 * (N-1)^2 XP
     * 
     * @param totalXp Le total d'XP accumulé
     * @return Le niveau actuel
     */
    fun calculateLevel(totalXp: Long): Int {
        if (totalXp < 0) return 1
        
        var level = 1
        while (totalXp >= calculateXpForLevel(level + 1)) {
            level++
        }
        return level
    }
    
    /**
     * Calcule l'XP requis pour atteindre un niveau donné
     * Formule: 100 * (level-1)^2
     * 
     * @param level Le niveau cible
     * @return L'XP requis pour atteindre ce niveau
     */
    fun calculateXpForLevel(level: Int): Long {
        if (level <= 1) return 0L
        val previousLevel = level - 1
        return 100L * previousLevel * previousLevel
    }
    
    /**
     * Calcule l'XP requis pour passer au niveau suivant
     * 
     * @param currentLevel Le niveau actuel
     * @param currentXp L'XP actuel
     * @return L'XP manquant pour le niveau suivant
     */
    fun calculateXpToNextLevel(currentLevel: Int, currentXp: Long): Long {
        val nextLevelXp = calculateXpForLevel(currentLevel + 1)
        return (nextLevelXp - currentXp).coerceAtLeast(0L)
    }
    
    /**
     * Calcule le pourcentage de progression vers le niveau suivant
     * 
     * @param currentLevel Le niveau actuel
     * @param currentXp L'XP actuel
     * @return Le pourcentage de progression (0.0 à 1.0)
     */
    fun calculateProgressToNextLevel(currentLevel: Int, currentXp: Long): Float {
        val currentLevelXp = calculateXpForLevel(currentLevel)
        val nextLevelXp = calculateXpForLevel(currentLevel + 1)
        
        val xpInCurrentLevel = currentXp - currentLevelXp
        val xpNeededForLevel = nextLevelXp - currentLevelXp
        
        return if (xpNeededForLevel > 0) {
            (xpInCurrentLevel.toFloat() / xpNeededForLevel).coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    
    /**
     * Calcule l'XP total pour une action d'apprentissage
     * 
     * @param wordsLearned Nombre de mots appris
     * @return L'XP total gagné
     */
    fun calculateXpForLearning(wordsLearned: Int): Int {
        return wordsLearned * XP_WORD_LEARNED
    }
    
    /**
     * Calcule l'XP total pour des révisions
     * 
     * @param wordsReviewed Nombre de mots révisés avec succès
     * @return L'XP total gagné
     */
    fun calculateXpForReview(wordsReviewed: Int): Int {
        return wordsReviewed * XP_WORD_REVIEWED
    }
    
    /**
     * Calcule l'XP pour un jeu complété
     * 
     * @param perfectScore Si le score est parfait
     * @return L'XP total gagné
     */
    fun calculateXpForGame(perfectScore: Boolean = false): Int {
        var xp = XP_GAME_COMPLETED
        if (perfectScore) {
            xp += XP_PERFECT_SCORE_BONUS
        }
        return xp
    }
    
    /**
     * Calcule l'XP bonus pour une série quotidienne
     * 
     * @param streakDays Nombre de jours consécutifs
     * @return L'XP bonus gagné
     */
    fun calculateStreakBonus(streakDays: Int): Int {
        return streakDays * XP_DAILY_STREAK_BONUS
    }
}
