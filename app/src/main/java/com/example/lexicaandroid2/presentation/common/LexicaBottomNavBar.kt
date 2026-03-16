package com.example.lexicaandroid2.presentation.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lexicaandroid2.presentation.navigation.Screen

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val width: Dp = 72.dp
)

private val NAV_ITEMS = listOf(
    BottomNavItem(Screen.Dashboard.route, "Accueil", Icons.Default.Home),
    BottomNavItem(Screen.Review.route, "Entraînement", Icons.Default.School, width = 88.dp),
    BottomNavItem(Screen.MiniGames.route, "Mini-Jeux", Icons.Default.SportsEsports, width = 70.dp),
    BottomNavItem(Screen.Utilisation.route, "Usage", Icons.AutoMirrored.Filled.MenuBook, width = 68.dp),
    BottomNavItem(Screen.Online.route, "En Ligne", Icons.Default.Wifi, width = 70.dp)
)

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
    val shouldWrapLabels = LocalDensity.current.fontScale > 1f

    NavigationBar {
        NAV_ITEMS.forEach { item ->
            NavigationBarItem(
                modifier = Modifier.width(item.width),
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = {
                    Text(
                        text = item.label,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = if (shouldWrapLabels) 2 else 1,
                        softWrap = shouldWrapLabels,
                        overflow = if (shouldWrapLabels) TextOverflow.Ellipsis else TextOverflow.Clip,
                        fontSize = 8.5.sp,
                        textAlign = TextAlign.Center
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                alwaysShowLabel = true
            )
        }
    }
}
