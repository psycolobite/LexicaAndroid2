package com.example.lexicaandroid2.presentation.review

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lexicaandroid2.domain.repository.FlashcardRepository

@Composable
fun DrivingModeScreen(
    repository: FlashcardRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DrivingModeViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return DrivingModeViewModel(
                    context.applicationContext as Application,
                    repository
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.isLoading && uiState.cards.isEmpty()) {
            viewModel.loadSession()
        }
    }

    val currentCard = uiState.currentCard

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            uiState.error != null -> {
                Text(
                    text = uiState.error ?: "Erreur inconnue",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onBack) {
                    Text("Retour")
                }
            }

            uiState.isFinished -> {
                Text(
                    text = "🚗 Mode voiture terminé",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Toutes les cartes de la session ont été lues.",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { viewModel.loadSession() }) {
                    Text("Recommencer")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onBack) {
                    Text("Retour")
                }
            }

            currentCard != null -> {
                Text(
                    text = "Carte ${uiState.currentIndex + 1}/${uiState.cards.size}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = currentCard.recto,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = currentCard.verso,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.currentPhaseLabel,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { if (uiState.isPaused || !uiState.isPlaying) viewModel.resume() else viewModel.pause() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (uiState.isPaused || !uiState.isPlaying) "Reprendre" else "Pause")
                    }
                    Button(
                        onClick = { viewModel.repeatCurrentCard() },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.ttsReady
                    ) {
                        Text("Répéter")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.nextCard() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Suivant")
                    }
                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Retour")
                    }
                }
            }
        }
    }
}
