package com.example.lexicaandroid2.presentation.dailychallenge

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Écran du Daily Challenge
 *
 * Affiche le défi quotidien avec :
 * - Le jeu imposé du jour
 * - Le bonus XP disponible
 * - La streak actuelle
 * - Le countdown jusqu'au prochain défi
 */
@Composable
fun DailyChallengeScreen(
    uiState: DailyChallengeUiState,
    onStartChallenge: (GameType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            DailyChallengeHeader()

            Spacer(modifier = Modifier.height(8.dp))

            // Contenu principal selon l'état
            when (val state = uiState.state) {
                is DailyChallengeState.Loading -> {
                    LoadingContent()
                }
                is DailyChallengeState.Available -> {
                    AvailableChallengeContent(
                        gameType = state.gameType,
                        bonusXp = state.bonusXp,
                        currentStreak = uiState.currentStreak,
                        onStartChallenge = onStartChallenge
                    )
                }
                is DailyChallengeState.Completed -> {
                    CompletedChallengeContent(
                        gameType = state.gameType,
                        xpEarned = state.xpEarned,
                        currentStreak = uiState.currentStreak,
                        timeUntilNext = uiState.timeUntilNextChallenge
                    )
                }
                is DailyChallengeState.Error -> {
                    ErrorContent(message = state.message)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Stats de l'utilisateur
            uiState.userStats?.let { stats ->
                UserStatsCard(stats)
            }
        }
    }
}

/**
 * Header avec titre et icône animée
 */
@Composable
private fun DailyChallengeHeader() {
    // Animation de pulsation pour l'icône
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "⭐",
            fontSize = 48.sp,
            modifier = Modifier.scale(scale)
        )
        Text(
            text = "Défi Quotidien",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Complète un jeu par jour et maintiens ta série !",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Contenu de chargement
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Contenu du challenge disponible
 */
@Composable
private fun AvailableChallengeContent(
    gameType: GameType,
    bonusXp: Int,
    currentStreak: Int,
    onStartChallenge: (GameType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icône du jeu
            Text(
                text = gameType.icon,
                fontSize = 64.sp
            )

            // Nom du jeu
            Text(
                text = gameType.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Bonus XP
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "+$bonusXp XP Bonus",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Streak actuelle
            if (currentStreak > 0) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF6B35)
                    )
                    Text(
                        text = "Série actuelle : $currentStreak jours",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bouton pour démarrer
            Button(
                onClick = { onStartChallenge(gameType) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Commencer le Défi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Contenu du challenge complété
 */
@Composable
private fun CompletedChallengeContent(
    gameType: GameType,
    xpEarned: Int,
    currentStreak: Int,
    timeUntilNext: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icône de succès
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            // Message de félicitations
            Text(
                text = "Défi Complété !",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            // Afficher le jeu complété
            Text(
                text = "${gameType.icon} ${gameType.displayName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )

            Text(
                text = "Tu as gagné $xpEarned XP",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )

            // Streak
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFFF6B35)
                    )
                    Column {
                        Text(
                            text = "Série de $currentStreak jours",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Continue demain pour prolonger !",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            HorizontalDivider()

            // Countdown
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Prochain défi dans :",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )

                val hours = (timeUntilNext / (1000 * 60 * 60)) % 24
                val minutes = (timeUntilNext / (1000 * 60)) % 60
                val seconds = (timeUntilNext / 1000) % 60

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeBox(value = hours, label = "H")
                    Text(":", style = MaterialTheme.typography.headlineSmall)
                    TimeBox(value = minutes, label = "M")
                    Text(":", style = MaterialTheme.typography.headlineSmall)
                    TimeBox(value = seconds, label = "S")
                }
            }
        }
    }
}

/**
 * Box pour afficher une valeur de temps
 */
@Composable
private fun TimeBox(value: Long, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(50.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%02d", value),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Contenu d'erreur
 */
@Composable
private fun ErrorContent(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Erreur",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Card affichant les stats de l'utilisateur
 */
@Composable
private fun UserStatsCard(stats: com.example.lexicaandroid2.features.gamification.data.UserStatsEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                icon = Icons.Default.Star,
                label = "Niveau",
                value = stats.level.toString()
            )
            StatItem(
                icon = Icons.Default.EmojiEvents,
                label = "XP",
                value = stats.xp.toString()
            )
            StatItem(
                icon = Icons.Default.LocalFireDepartment,
                label = "Série",
                value = stats.streak.toString()
            )
        }
    }
}

/**
 * Item de stat individuel
 */
@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

