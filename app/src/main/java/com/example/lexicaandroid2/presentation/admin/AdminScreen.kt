package com.example.lexicaandroid2.presentation.admin

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
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
                text = "Ces paramètres affectent uniquement votre session.",
                fontSize = 13.sp,
                color = Color.Gray
            )

            HorizontalDivider()

            // ── Section Révision ─────────────────────────────────────────────
            AdminSection(title = "📚 Entraînement (Révision)") {

                AdminLabel("Type de carte affiché")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReviewMode.entries.forEach { mode ->
                        FilterChip(
                            selected = uiState.reviewMode == mode,
                            onClick = { viewModel.setReviewMode(mode) },
                            label = { Text(mode.label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                AdminLabel("Taille de session : ${uiState.sessionSize} cartes")
                Slider(
                    value = uiState.sessionSize.toFloat(),
                    onValueChange = { viewModel.setSessionSize(it.toInt()) },
                    valueRange = 5f..50f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                AdminToggleRow(
                    label = "Défis orthographiques",
                    checked = uiState.challengeOrthoEnabled,
                    onCheckedChange = { viewModel.setChallengeOrtho(it) }
                )
                AdminToggleRow(
                    label = "Défis sémantiques",
                    checked = uiState.challengeSemanticEnabled,
                    onCheckedChange = { viewModel.setChallengeSemanticEnabled(it) }
                )
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

