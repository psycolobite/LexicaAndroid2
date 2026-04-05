package com.example.lexicaandroid2.presentation.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun FrenchTtsSetupDialog(
    onDismiss: () -> Unit,
    onOpenTtsSetup: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Voix française requise",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = "Lexica a détecté qu'aucune voix TTS française n'est disponible sur cet appareil. " +
                    "Pour activer correctement la lecture audio des mots et définitions, l'application va ouvrir l'installation ou les réglages système du moteur vocal. " +
                    "Choisis de configurer maintenant ou de remettre à plus tard.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenTtsSetup) {
                Text("Installer / configurer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Plus tard")
            }
        }
    )
}

