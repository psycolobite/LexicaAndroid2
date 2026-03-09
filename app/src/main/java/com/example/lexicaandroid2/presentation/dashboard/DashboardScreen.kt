package com.example.lexicaandroid2.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToReview: () -> Unit,
    onNavigateToWordList: () -> Unit,
    onNavigateToWordListFiltered: (String) -> Unit,
    onNavigateToAddWords: () -> Unit,
    onNavigateToMiniGames: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStats()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA)) // Off-white background
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Chargement...")
            }
        } else if (uiState.totalCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .height(400.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune carte dans la base.\nImport automatique en cours si vide...",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            DashboardContent(
                uiState = uiState,
                onNavigateToReview = onNavigateToReview,
                onNavigateToWordList = onNavigateToWordList,
                onNavigateToWordListFiltered = onNavigateToWordListFiltered,
                onNavigateToAddWords = onNavigateToAddWords,
                onNavigateToMiniGames = onNavigateToMiniGames
            )
        }
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onNavigateToReview: () -> Unit,
    onNavigateToWordList: () -> Unit,
    onNavigateToWordListFiltered: (String) -> Unit,
    onNavigateToAddWords: () -> Unit,
    onNavigateToMiniGames: () -> Unit
) {
    Spacer(modifier = Modifier.height(32.dp))

    // Stat Cards
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "À apprendre",
            count = uiState.newCount,
            color = Color(0xFFA7C7E7), // Pastel Blue
            textColor = Color(0xFF424242), // Dark Gray
            onClick = { onNavigateToWordListFiltered("TO_LEARN") }
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "En Cours",
            count = uiState.learningCount,
            color = Color(0xFFFFB347), // Pastel Orange
            textColor = Color(0xFF424242),
            onClick = { onNavigateToWordListFiltered("LEARNING") }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Connus",
            count = uiState.knownCount,
            color = Color(0xFFB2D8B2), // Pastel Green
            textColor = Color(0xFF424242),
            onClick = { onNavigateToWordListFiltered("KNOWN") }
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Total",
            count = uiState.totalCount,
            color = MaterialTheme.colorScheme.surfaceVariant,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onNavigateToWordList
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Navigation Buttons
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onNavigateToAddWords,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
             colors = ButtonDefaults.buttonColors(
                 containerColor = Color(0xFF6750A4), // Primary purple
                 contentColor = Color.White
             )
        ) {
            Text(
                text = "AJOUTER DES MOTS",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Button(
            onClick = onNavigateToWordList,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
             colors = ButtonDefaults.buttonColors(
                 containerColor = MaterialTheme.colorScheme.secondaryContainer,
                 contentColor = MaterialTheme.colorScheme.onSecondaryContainer
             )
        ) {
            Text(
                text = "PARCOURIR MES MOTS",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Button(
            onClick = onNavigateToReview,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = uiState.totalCount > 0
        ) {
            Text(
                text = "COMMENCER RÉVISION",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Button(
            onClick = onNavigateToMiniGames,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6CA6CD), // Pastel Blue
                contentColor = Color.White
            )
        ) {
            Text(
                text = "🎮 MINI-JEUX",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    color: Color,
    textColor: Color = Color.Black,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

