package com.example.lexicaandroid2.features.gamification.domain

object GameUnlockConfig {

    data class GameEntry(
        val route: String,
        val name: String,
        val emoji: String,
        val requiredXp: Int
    )

    val ALL_GAMES: List<GameEntry> = listOf(
        GameEntry("game_matching",          "Jeu de Correspondance",    "🎮", requiredXp = 0),
        GameEntry("game_qcm",               "QCM",                      "❓", requiredXp = 0),
        GameEntry("game_hangman",           "Jeu du Pendu",             "🎯", requiredXp = 100),
        GameEntry("game_spelling",          "Jeu de Dictée",            "✍️", requiredXp = 300),
        GameEntry("game_anagrams",          "Anagrammes",               "🔤", requiredXp = 700),
        GameEntry("game_chrono",            "Mode Chrono",              "⏱️", requiredXp = 1300),
        GameEntry("game_memory",            "Memory",                   "🃏", requiredXp = 2100),
        GameEntry("game_fillword",          "Définition à Compléter",   "📝", requiredXp = 3300),
        GameEntry("game_semantic",          "Associations Sémantiques", "🔗", requiredXp = 4900),
        GameEntry("game_spelling_advanced", "Spelling Avancé",          "🎓", requiredXp = 7100)
    )

    /** Retourne le niveau requis correspondant au seuil XP. */
    fun requiredLevel(requiredXp: Int): Int = when {
        requiredXp <= 0    -> 1
        requiredXp <= 100  -> 2
        requiredXp <= 300  -> 3
        requiredXp <= 700  -> 5
        requiredXp <= 1300 -> 7
        requiredXp <= 2100 -> 10
        requiredXp <= 3300 -> 13
        requiredXp <= 4900 -> 16
        else               -> 20
    }
}
