package com.example.lexicaandroid2.presentation.common

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
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
    BottomNavItem(Screen.Explore.route, "Ajouter", Icons.Default.AddCircle, width = 68.dp),
    BottomNavItem(Screen.Review.route, "Entraînement", Icons.Default.School, width = 88.dp),
    BottomNavItem(Screen.MiniGames.route, "Mini-Jeux", Icons.Default.SportsEsports, width = 70.dp),
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
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val labelTextStyle = TextStyle(fontSize = 8.5.sp, lineHeight = 10.sp)
    val shouldWrapLabels = NAV_ITEMS.any { item ->
        val availableWidthPx = with(density) { (item.width - 8.dp).roundToPx() }.coerceAtLeast(1)
        textMeasurer.measure(
            text = item.label,
            style = labelTextStyle,
            maxLines = 2,
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
            constraints = androidx.compose.ui.unit.Constraints(maxWidth = availableWidthPx)
        ).lineCount > 1
    }
    val barHeight = if (shouldWrapLabels) 74.dp else 62.dp

    NavigationBar(
        modifier = Modifier.height(barHeight),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
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
                        fontSize = labelTextStyle.fontSize,
                        lineHeight = labelTextStyle.lineHeight,
                        textAlign = TextAlign.Center
                    )
                },
                colors = NavigationBarItemDefaults.colors(),
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
