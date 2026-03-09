package com.example.lexicaandroid2.features.gamification.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import com.example.lexicaandroid2.features.gamification.domain.XPCalculator

/**
 * Écran de démonstration du système de gamification
 * 
 * Affiche les statistiques de l'utilisateur et permet de tester
 * l'ajout d'XP et les calculs de niveau.
 */
@Composable
fun GamificationDemoScreen(
    userStats: UserStatsEntity?,
    onAddXp: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "🎮 Gamification Demo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Divider()
        
        // Progress Bar
        userStats?.let { stats ->
            XpProgressBar(userStats = stats)
            
            // Stats Cards
            StatsCardsRow(stats)
            
            Divider()
            
            // XP Actions
            XpActionsSection(onAddXp)
            
            Divider()
            
            // Level Information
            LevelInformationCard(stats)
        } ?: run {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun StatsCardsRow(stats: UserStatsEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Level Card
        StatCard(
            icon = Icons.Default.Star,
            title = "Niveau",
            value = stats.level.toString(),
            modifier = Modifier.weight(1f)
        )
        
        // XP Card
        StatCard(
            icon = Icons.Default.EmojiEvents,
            title = "XP Total",
            value = stats.xp.toString(),
            modifier = Modifier.weight(1f)
        )
        
        // Streak Card
        StatCard(
            icon = Icons.Default.LocalFireDepartment,
            title = "Série",
            value = "${stats.streak} jours",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun XpActionsSection(onAddXp: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Actions XP",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                text = "Mot appris\n+${XPCalculator.XP_WORD_LEARNED} XP",
                onClick = { onAddXp(XPCalculator.XP_WORD_LEARNED) },
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = "Révision\n+${XPCalculator.XP_WORD_REVIEWED} XP",
                onClick = { onAddXp(XPCalculator.XP_WORD_REVIEWED) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                text = "Jeu complété\n+${XPCalculator.XP_GAME_COMPLETED} XP",
                onClick = { onAddXp(XPCalculator.XP_GAME_COMPLETED) },
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = "Score parfait\n+${XPCalculator.calculateXpForGame(true)} XP",
                onClick = { onAddXp(XPCalculator.calculateXpForGame(true)) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                text = "Bonus série\n+${XPCalculator.XP_DAILY_STREAK_BONUS} XP",
                onClick = { onAddXp(XPCalculator.XP_DAILY_STREAK_BONUS) },
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = "Custom\n+100 XP",
                onClick = { onAddXp(100) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun LevelInformationCard(stats: UserStatsEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Informations de Niveau",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Divider()
            
            InfoRow(
                label = "Niveau actuel",
                value = stats.level.toString()
            )
            InfoRow(
                label = "XP total",
                value = stats.xp.toString()
            )
            InfoRow(
                label = "XP pour niveau actuel",
                value = XPCalculator.calculateXpForLevel(stats.level).toString()
            )
            InfoRow(
                label = "XP pour niveau suivant",
                value = XPCalculator.calculateXpForLevel(stats.level + 1).toString()
            )
            InfoRow(
                label = "XP restants",
                value = XPCalculator.calculateXpToNextLevel(stats.level, stats.xp).toString()
            )
            InfoRow(
                label = "Progression",
                value = "${(XPCalculator.calculateProgressToNextLevel(stats.level, stats.xp) * 100).toInt()}%"
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
