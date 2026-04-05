package com.example.lexicaandroid2.presentation.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * Couleur de panneau neutre un peu plus claire que le `surfaceVariant` par défaut,
 * afin d'éviter des cartes trop sombres tout en gardant un contraste suffisant.
 */
@Composable
fun lexicaPanelContainerColor(): Color {
    val colorScheme = MaterialTheme.colorScheme
    val darkTheme = colorScheme.background.luminance() < 0.5f
    val target = if (darkTheme) Color.White else colorScheme.surface
    val blendRatio = if (darkTheme) 0.14f else 0.35f
    return lerp(colorScheme.surfaceVariant, target, blendRatio)
}

