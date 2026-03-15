package com.example.lexicaandroid2.presentation.games.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.presentation.games.common.GameButton
import com.example.lexicaandroid2.presentation.games.common.GameHeader

@Composable
fun MemoryScreen(
    repository: FlashcardRepository,
    onBack: () -> Unit,
    viewModel: MemoryViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MemoryViewModel(repository) as T
        }
    })
) {
    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(Unit) {
        if (uiState.isLoading && uiState.cards.isEmpty()) {
            viewModel.loadGame()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.error, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onBack) {
                        Text("Retour")
                    }
                }
            }

            uiState.gameOver -> {
                MemoryGameOver(
                    score = uiState.score,
                    attempts = uiState.attempts,
                    bestScore = uiState.bestScore,
                    onRestart = viewModel::restart,
                    onBack = onBack
                )
            }

            else -> {
                GameHeader(
                    title = "Memory",
                    score = uiState.score,
                    progress = uiState.progress
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.availableConfigs.forEach { config ->
                            val selected = config == uiState.selectedConfig
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.loadGame(config) },
                                label = { Text("${config.columns}×${config.rows}", fontSize = 12.sp) },
                                enabled = !uiState.isCheckingPair,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Paires: ${uiState.matchedPairs}/${uiState.totalPairs}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Essais: ${uiState.attempts}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(uiState.selectedConfig.columns),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.cards, key = { it.id }) { card ->
                            val showFront = card.isFaceUp || card.isMatched
                            val background = when {
                                card.isMatched -> Color(0xFFD4EDDA)
                                showFront -> Color(0xFFE3F2FD)
                                else -> Color(0xFFB0BEC5)
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = background,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onCardClicked(card.id)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (showFront) card.text else "?",
                                        fontSize = 12.sp,
                                        fontWeight = if (showFront) FontWeight.Medium else FontWeight.Bold,
                                        color = if (showFront) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Meilleur score (session): ${uiState.bestScore}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                GameButton(
                    text = "Retour au menu",
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MemoryGameOver(
    score: Int,
    attempts: Int,
    bestScore: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Partie terminée",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Score: $score",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Tentatives: $attempts",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Meilleur score: $bestScore",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        GameButton(
            text = "Recommencer",
            onClick = onRestart,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        GameButton(text = "Menu", onClick = onBack)
    }
}
