package com.example.lexicaandroid2.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val NAV_ITEMS = listOf(
    BottomNavItem("dashboard", "Accueil", Icons.Default.Home),
    BottomNavItem("review", "Entraînement", Icons.Default.School),
    BottomNavItem("mini_games", "Mini-Jeux", Icons.Default.SportsEsports),
    BottomNavItem("online", "En Ligne", Icons.Default.Wifi)
)

/** Routes de jeux individuels sur lesquelles la barre doit être masquée. */
val GAME_ROUTES = setOf(
    "game_matching",
    "game_qcm",
    "game_hangman",
    "game_spelling",
    "game_anagrams",
    "game_chrono",
    "game_memory",
    "game_fillword",
    "game_semantic",
    "game_spelling_advanced"
)

/**
 * Retourne `true` si la barre de navigation inférieure doit être affichée
 * pour la route courante.
 */
fun shouldShowBottomBar(currentRoute: String?): Boolean {
    if (currentRoute == null) return false
    return currentRoute !in GAME_ROUTES && !currentRoute.startsWith("word/")
}

/**
 * Barre de navigation inférieure Material3 avec 4 onglets principaux :
 * Accueil, Entraînement, Mini-Jeux, En Ligne.
 *
 * @param currentRoute Route actuelle (depuis `navController.currentBackStackEntryAsState`).
 * @param onNavigate   Callback appelé avec la route destination lors d'un tap.
 */
@Composable
fun LexicaBottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        NAV_ITEMS.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                }
            )
        }
    }
}
