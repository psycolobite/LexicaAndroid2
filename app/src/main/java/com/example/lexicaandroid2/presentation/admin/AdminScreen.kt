package com.example.lexicaandroid2.presentation.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    onReviewSettingsChanged: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    DisposableEffect(Unit) {
        onDispose {
            if (viewModel.consumePendingReviewSettingsChange()) {
                onReviewSettingsChanged()
            }
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Titre ────────────────────────────────────────────────────────
            Text(
                text = "⚙️ Mode Administrateur",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Active uniquement les modes d'entraînement souhaités.",
                fontSize = 13.sp,
                color = Color.Gray
            )

            HorizontalDivider()

            // ── Section Révision ─────────────────────────────────────────────
            AdminSection(title = "📚 Entraînement (Révision)") {
                AdminToggleRow(
                    label = "Présentation normale",
                    checked = uiState.normalPresentationEnabled,
                    onCheckedChange = { viewModel.setNormalPresentationEnabled(it) }
                )

                Text(
                    text = if (uiState.normalPresentationEnabled) {
                        "Mode actif : la révision se comporte comme sans mode admin. Rien n'est forcé, et questions, annexes et défis apparaissent selon la logique métier standard."
                    } else {
                        "Mode avancé admin actif : tu peux choisir et forcer des types de questions précis pour tester les sous-flux."
                    },
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                AnimatedVisibility(visible = !uiState.normalPresentationEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Spacer(modifier = Modifier.height(2.dp))
                        AdminLabel("Modes actifs")

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.reviewDefinitionToWordEnabled,
                                onClick = { viewModel.setReviewDefinitionToWordEnabled(!uiState.reviewDefinitionToWordEnabled) },
                                label = { Text("Définition → Mot", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.reviewWordToDefinitionEnabled,
                                onClick = { viewModel.setReviewWordToDefinitionEnabled(!uiState.reviewWordToDefinitionEnabled) },
                                label = { Text("Mot → Définition", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.extraSpellingEnabled,
                                onClick = { viewModel.setExtraSpellingEnabled(!uiState.extraSpellingEnabled) },
                                label = { Text("Question orthographique", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.reviewQcmEnabled,
                                onClick = { viewModel.setReviewQcmEnabled(!uiState.reviewQcmEnabled) },
                                label = { Text("QCM intégré", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.reviewMatchingEnabled,
                                onClick = { viewModel.setReviewMatchingEnabled(!uiState.reviewMatchingEnabled) },
                                label = { Text("Correspondance", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.challengeSemanticEnabled,
                                onClick = { viewModel.setChallengeSemanticEnabled(!uiState.challengeSemanticEnabled) },
                                label = { Text("Défi sémantique", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.challengeOrthoEnabled,
                                onClick = { viewModel.setChallengeOrtho(!uiState.challengeOrthoEnabled) },
                                label = { Text("Défi orthographique", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.challengeUsageEnabled,
                                onClick = { viewModel.setChallengeUsageEnabled(!uiState.challengeUsageEnabled) },
                                label = { Text("Défi utilisation", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Ces réglages servent à tester isolément les sous-flux de l'entraînement. Si les 2 questions normales sont désactivées, les items cochés ci-dessous peuvent être forcés en mode test admin.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        AdminLabel("Taille de la session : ${uiState.sessionSize}")
                        Slider(
                            value = uiState.sessionSize.toFloat(),
                            onValueChange = { viewModel.setSessionSize(it.toInt()) },
                            valueRange = 2f..50f,
                            steps = 47,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Réglage avancé admin : en mode test forcé, cela pilote le nombre d'items de test enchaînés (défis, QCM, correspondances, etc.).",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // ── Section Jeux ─────────────────────────────────────────────────
            AdminSection(title = "🎮 Jeux") {

                AdminLabel("QCM — Nombre de questions : ${uiState.qcmQuestionCount}")
                Slider(
                    value = uiState.qcmQuestionCount.toFloat(),
                    onValueChange = { viewModel.setQcmQuestionCount(it.toInt()) },
                    valueRange = 3f..20f,
                    steps = 16,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                AdminLabel("Memory — Taille de la grille")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MemoryGridSize.entries.forEach { size ->
                        FilterChip(
                            selected = uiState.memoryGridSize == size,
                            onClick = { viewModel.setMemoryGridSize(size) },
                            label = { Text(size.label, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.resetXpAndLevel() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("🔄 Réinitialiser XP & Niveau")
                }
            }

            AdminSection(title = "⭐ Progression") {
                AdminLabel("Niveau actuel : ${uiState.currentLevel}")
                AdminLabel("Niveau cible : ${uiState.pendingLevel}")
                Slider(
                    value = uiState.pendingLevel.toFloat(),
                    onValueChange = { viewModel.setPendingLevel(it.toInt()) },
                    valueRange = 1f..20f,
                    steps = 18,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { viewModel.applyPendingLevel() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Appliquer le niveau")
                }
            }

            // ── Section Système ──────────────────────────────────────────────
            AdminSection(title = "🔧 Système") {

                OutlinedButton(
                    onClick = { viewModel.clearDailyStats() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🗑️ Effacer stats quotidiennes (graphe)")
                }

                OutlinedButton(
                    onClick = { viewModel.simulateStreak7Days() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔥 Simuler streak 7 jours")
                }
            }

            HorizontalDivider()

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("← Retour")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Composables helper ──────────────────────────────────────────────────────

@Composable
private fun AdminSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            content()
        }
    }
}

@Composable
private fun AdminLabel(text: String) {
    Text(text = text, fontSize = 14.sp, color = Color.DarkGray)
}

@Composable
private fun AdminToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

