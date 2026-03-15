package com.example.lexicaandroid2.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToReview: () -> Unit,
    onNavigateToWordList: () -> Unit,
    onNavigateToWordListFiltered: (String) -> Unit,
    onNavigateToAddWords: () -> Unit,
    onNavigateToMiniGames: () -> Unit,
    onNavigateToDailyChallenge: () -> Unit = {}
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
                onNavigateToMiniGames = onNavigateToMiniGames,
                onNavigateToDailyChallenge = onNavigateToDailyChallenge
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
    onNavigateToMiniGames: () -> Unit,
    onNavigateToDailyChallenge: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))

    // Section titre : mes mots
    Text(
        text = "Mes mots",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Cartes stats — les 3 catégories filtrent, "Tous" ouvre la liste complète
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "À apprendre",
            count = uiState.newCount,
            color = Color(0xFFA7C7E7),
            textColor = Color(0xFF424242),
            onClick = { onNavigateToWordListFiltered("TO_LEARN") }
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "En cours",
            count = uiState.learningCount,
            color = Color(0xFFFFB347),
            textColor = Color(0xFF424242),
            onClick = { onNavigateToWordListFiltered("LEARNING") }
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Connus",
            count = uiState.knownCount,
            color = Color(0xFFB2D8B2),
            textColor = Color(0xFF424242),
            onClick = { onNavigateToWordListFiltered("KNOWN") }
        )
    }
    // Ligne "Tous les mots" sous les cartes — évite le doublon avec le bouton
    Text(
        text = "Voir tous les mots (${uiState.totalCount})",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToWordList() }
            .padding(top = 6.dp, bottom = 4.dp),
        textAlign = TextAlign.End
    )

    Spacer(modifier = Modifier.height(24.dp))

    // CTA principal — Révision
    Button(
        onClick = onNavigateToReview,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        enabled = uiState.totalCount > 0,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6750A4),
            contentColor = Color.White
        )
    ) {
        Text(
            text = "🚀  COMMENCER LA RÉVISION",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Actions secondaires : Mini-Jeux + Défi du Jour côte à côte
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onNavigateToMiniGames,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6CA6CD),
                contentColor = Color.White
            )
        ) {
            Text(text = "🎮 Mini-Jeux", fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onNavigateToDailyChallenge,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB347),
                contentColor = Color.White
            )
        ) {
            Text(text = "📅 Défi du Jour", fontWeight = FontWeight.SemiBold)
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Action tertiaire — Ajouter des mots (outlined, moins imposant)
    OutlinedButton(
        onClick = onNavigateToAddWords,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(text = "+ Ajouter des mots", fontWeight = FontWeight.Medium)
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

