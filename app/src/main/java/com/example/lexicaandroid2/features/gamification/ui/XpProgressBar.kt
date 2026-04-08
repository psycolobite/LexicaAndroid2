package com.example.lexicaandroid2.features.gamification.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import com.example.lexicaandroid2.features.gamification.domain.XPCalculator
import com.example.lexicaandroid2.presentation.common.lexicaPanelContainerColor

@Composable
fun XpProgressBar(
    userStats: UserStatsEntity,
    modifier: Modifier = Modifier
) {
    val currentLevel = userStats.level
    val totalXp = userStats.xp

    // Use XPCalculator to get progress information
    val progress = XPCalculator.calculateProgressToNextLevel(currentLevel, totalXp)
    val nextLevelXp = XPCalculator.calculateXpForLevel(currentLevel + 1)
    val xpToNext = XPCalculator.calculateXpToNextLevel(currentLevel, totalXp)

    Column(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Level $currentLevel",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "$xpToNext XP to Level ${currentLevel + 1}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = lexicaPanelContainerColor()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${totalXp} / ${nextLevelXp} XP",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.End),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

