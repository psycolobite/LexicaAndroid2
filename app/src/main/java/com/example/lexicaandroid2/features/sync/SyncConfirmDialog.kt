package com.example.lexicaandroid2.features.sync

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dialog non-bloquant affiché quand Firestore détecte une progression cloud différente
 * de la progression locale.
 *
 * L'utilisateur choisit entre :
 * - "Conserver ma progression locale" → upload local → cloud
 * - "Remplacer par la progression cloud" → écrase local avec les données cloud
 *
 * @param conflictState  État PendingConflict portant les infos des deux progressions.
 * @param onKeepLocal    Callback quand l'utilisateur choisit de garder le local.
 * @param onReplaceLocal Callback quand l'utilisateur accepte l'écrasement.
 */
@Composable
fun SyncConfirmDialog(
    conflictState: SyncUiState.PendingConflict,
    onKeepLocal: () -> Unit,
    onReplaceLocal: () -> Unit
) {
    val isEmptyCloudAccount = conflictState.kind == SyncConflictKind.EMPTY_CLOUD_ACCOUNT
    val cloud = conflictState.cloud
    AlertDialog(
        onDismissRequest = onKeepLocal,
        title = {
            Text(
                text = if (isEmptyCloudAccount) {
                    "☁️ Ce compte n'a pas encore de progression"
                } else {
                    "⚠️ Progressions différentes détectées"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = if (isEmptyCloudAccount) {
                        "Ce compte est vide pour le moment. Tu peux repartir à zéro sur ce compte ou y envoyer ta progression locale actuelle."
                    } else {
                        "Ce compte a déjà une progression cloud différente de celle actuellement présente sur l'appareil. Choisis laquelle doit devenir la référence."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                if (!isEmptyCloudAccount && cloud != null) {
                    ProgressionRow(
                        label = "☁️ Compte",
                        level = cloud.level,
                        xp = cloud.xp,
                        streak = cloud.streak,
                        cardCount = cloud.flashcards.size,
                        startedQuestionCount = cloud.reviewQuestionProgress.count { it.firstAnsweredAt != null }
                    )

                    Spacer(Modifier.height(8.dp))
                }

                ProgressionRow(
                    label = "📱 Locale",
                    level = conflictState.local.level,
                    xp = conflictState.local.xp,
                    streak = conflictState.local.streak,
                    cardCount = conflictState.local.cardCount,
                    startedQuestionCount = conflictState.local.startedQuestionCount
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (isEmptyCloudAccount) {
                        "⚠️ Repartir à zéro supprimera la progression locale actuelle sur cet appareil pour aligner ce compte vide."
                    } else {
                        "⚠️ Charger la progression du compte écrasera la progression locale actuelle sur cet appareil."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onReplaceLocal,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    if (isEmptyCloudAccount) {
                        "Repartir à zéro sur ce compte"
                    } else {
                        "Charger la progression du compte"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepLocal) {
                Text(
                    if (isEmptyCloudAccount) {
                        "Envoyer ma progression locale"
                    } else {
                        "Envoyer ma progression locale"
                    }
                )
            }
        }
    )
}

@Composable
private fun ProgressionRow(
    label: String,
    level: Int,
    xp: Long,
    streak: Int?,
    cardCount: Int,
    startedQuestionCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(80.dp)
        )
        Column {
            Text(
                text = "Niveau $level — $xp XP",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$cardCount mot(s) — $startedQuestionCount question(s) déjà commencée(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (streak != null) {
                Text(
                    text = "Série : $streak jours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
