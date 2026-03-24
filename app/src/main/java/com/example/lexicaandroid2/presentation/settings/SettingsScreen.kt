package com.example.lexicaandroid2.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Écran des réglages utilisateur.
 *
 * @param viewModel     SettingsViewModel contenant les préférences.
 * @param appVersion    Version de l'app à afficher (passée depuis LexicaApp/MainActivity).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    appVersion: String = "1.0",
    showAdminEntry: Boolean = false,
    onNavigateToAdmin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingFontSize by remember(uiState.fontSize) { mutableStateOf(uiState.fontSize) }

    if (showTimePicker) {
        ReminderTimePickerDialog(
            currentTime = uiState.reminderTime,
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                viewModel.setReminderTime(time)
                showTimePicker = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // ── Section Apparence ─────────────────────────────────────────────
        SettingsSectionTitle("🎨 Apparence")

        SettingsCard {
            SettingsLabel("Thème")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    FilterChip(
                        selected = uiState.theme == theme,
                        onClick = { viewModel.setTheme(theme) },
                        label = { Text(theme.label, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            val textScalePercent = ((pendingFontSize / UserPrefsRepository.DEFAULT_FONT_SIZE) * 100f).roundToInt()
            val appliedTextScalePercent = ((uiState.fontSize / UserPrefsRepository.DEFAULT_FONT_SIZE) * 100f).roundToInt()
            val previewScaleRatio = pendingFontSize / uiState.fontSize.coerceAtLeast(1f)
            SettingsLabel("Échelle du texte : $textScalePercent%")
            Slider(
                value = pendingFontSize,
                onValueChange = { pendingFontSize = it },
                valueRange = 12f..22f,
                steps = 9,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Ajuste la valeur ici, puis valide pour appliquer à toute l'application.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Aperçu local",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Lexica",
                        fontSize = (20f * previewScaleRatio).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Le changement global sera appliqué seulement après validation.",
                        fontSize = (14f * previewScaleRatio).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Taille appliquée actuellement : $appliedTextScalePercent%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Après validation, l’aperçu gardera exactement ce rendu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.setFontSize(pendingFontSize) },
                enabled = pendingFontSize != uiState.fontSize,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Valider")
            }
        }

        Spacer(Modifier.height(4.dp))

        // ── Section Notifications ─────────────────────────────────────────
        SettingsSectionTitle("🔔 Notifications")

        SettingsCard {
            SettingsToggleRow(
                label = "Rappel quotidien",
                description = "Notification pour réviser chaque jour",
                checked = uiState.dailyReminderEnabled,
                onCheckedChange = { viewModel.setDailyReminderEnabled(it) }
            )

            if (uiState.dailyReminderEnabled) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Heure du rappel",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Heure locale",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { showTimePicker = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = uiState.reminderTime,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        if (showAdminEntry) {
            SettingsSectionTitle("⚙️ Administration")

            SettingsCard {
                Text(
                    text = "Accéder au mode admin et aux réglages avancés d'entraînement.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onNavigateToAdmin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ouvrir le mode admin")
                }
            }

            Spacer(Modifier.height(4.dp))
        }

        // ── Section À propos ──────────────────────────────────────────────
        SettingsSectionTitle("ℹ️ À propos")

        SettingsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Version",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = appVersion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Politique de confidentialité",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { /* placeholder — ajouter URL quand disponible */ }
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Propriété intellectuelle",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Application, concept, contenus et identité du projet © Paul Mottet. Tous droits réservés.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Composables privés ─────────────────────────────────────────────────────

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    )
}

@Composable
private fun SettingsLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Dialog simple de sélection d'heure (HH:MM) sans dépendance externe.
 */
@Composable
private fun ReminderTimePickerDialog(
    currentTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parts = currentTime.split(":")
    var hour by remember { mutableIntStateOf(parts.getOrNull(0)?.toIntOrNull() ?: 20) }
    var minute by remember { mutableIntStateOf(parts.getOrNull(1)?.toIntOrNull() ?: 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Heure du rappel") },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Heure (0-23)", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = hour.toString().padStart(2, '0'),
                        onValueChange = { v ->
                            v.toIntOrNull()?.let { if (it in 0..23) hour = it }
                        },
                        singleLine = true,
                        modifier = Modifier.width(80.dp)
                    )
                }
                Text(":", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Minute (0-59)", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = minute.toString().padStart(2, '0'),
                        onValueChange = { v ->
                            v.toIntOrNull()?.let { if (it in 0..59) minute = it }
                        },
                        singleLine = true,
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                    )
                }
            ) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
