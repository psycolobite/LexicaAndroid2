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
    AlertDialog(
        onDismissRequest = onKeepLocal, // Tap en dehors = conserver local (sécurité)
        title = {
            Text(
                text = "⚠️ Progression existante détectée",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Ce compte a une progression sauvegardée dans le cloud. " +
                            "Elle diffère de votre progression locale.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                // Progression cloud
                ProgressionRow(
                    label = "☁️ Cloud",
                    level = conflictState.cloud.level,
                    xp = conflictState.cloud.xp,
                    streak = conflictState.cloud.streak
                )

                Spacer(Modifier.height(8.dp))

                // Progression locale
                ProgressionRow(
                    label = "📱 Locale",
                    level = conflictState.localLevel,
                    xp = conflictState.localXp,
                    streak = null // Non connue sans nouvelle DB call — omis pour simplicité
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "⚠️ Remplacer écrasera définitivement votre progression locale.",
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
                Text("Remplacer ma progression locale")
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepLocal) {
                Text("Conserver ma progression locale")
            }
        }
    )
}

@Composable
private fun ProgressionRow(
    label: String,
    level: Int,
    xp: Long,
    streak: Int?
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
