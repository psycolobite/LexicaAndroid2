package com.example.lexicaandroid2.presentation.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Review : Screen("review")
    data object WordList : Screen("wordlist?filter={filter}") {
        fun createRoute(filter: String? = null) = "wordlist?filter=${filter ?: ""}"
    }
    data object AddWords : Screen("add_words")
    data object MiniGames : Screen("mini_games")
    data object MatchingGame : Screen("game_matching")
    data object QcmGame : Screen("game_qcm")
    data object HangmanGame : Screen("game_hangman")
    data object SpellingGame : Screen("game_spelling")
    data object AnagramsGame : Screen("game_anagrams")
    data object ChronoGame : Screen("game_chrono")
    data object MemoryGame : Screen("game_memory")
    data object FillWordGame : Screen("game_fillword")
    data object SemanticGame : Screen("game_semantic")
    data object SpellingAdvancedGame : Screen("game_spelling_advanced")
    data object Profile : Screen("profile")
    data object DailyChallenge : Screen("daily_challenge")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Admin : Screen("admin")
    data object Settings : Screen("settings")
    data object Utilisation : Screen("utilisation")
    data object Online : Screen("online")
    data object DrivingMode : Screen("driving_mode")
    data object Explore : Screen("explore")
    data class WordDetail(val cardId: String = "") : Screen("word/{cardId}") {
        fun createRoute(cardId: String) = "word/$cardId"
    }
    data class EditWord(val cardId: String = "") : Screen("edit_word/{cardId}") {
        fun createRoute(cardId: String) = "edit_word/$cardId"
    }
}

/** Routes de jeux individuels sur lesquelles la barre doit être masquée. */
val GAME_ROUTES = setOf(
    Screen.MatchingGame.route,
    Screen.QcmGame.route,
    Screen.HangmanGame.route,
    Screen.SpellingGame.route,
    Screen.AnagramsGame.route,
    Screen.ChronoGame.route,
    Screen.MemoryGame.route,
    Screen.FillWordGame.route,
    Screen.SemanticGame.route,
    Screen.SpellingAdvancedGame.route,
    Screen.DrivingMode.route
)

private val BOTTOM_BAR_HIDDEN_ROUTES = GAME_ROUTES + setOf(
    Screen.Review.route,
    Screen.EditWord().route
)

/**
 * Retourne `true` si la barre de navigation inférieure doit être affichée
 * pour la route courante.
 */
fun shouldShowBottomBar(currentRoute: String?): Boolean {
    if (currentRoute == null) return false
    return currentRoute !in BOTTOM_BAR_HIDDEN_ROUTES &&
        !currentRoute.startsWith("word/") &&
        !currentRoute.startsWith("edit_word/")
}

