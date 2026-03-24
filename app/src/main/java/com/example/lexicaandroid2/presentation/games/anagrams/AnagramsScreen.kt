package com.example.lexicaandroid2.presentation.games.anagrams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.lexicaandroid2.presentation.games.common.GameTopAppBar

@Composable
fun AnagramsScreen(
    repository: FlashcardRepository,
    onBack: () -> Unit,
    viewModel: AnagramsViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AnagramsViewModel(repository) as T
        }
    })
) {
    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(Unit) {
        if (uiState.isLoading && uiState.rounds.isEmpty()) {
            viewModel.loadGame()
        }
    }

    Scaffold(
        topBar = {
            GameTopAppBar(
                title = "Anagrams",
                score = uiState.score,
                current = uiState.currentRoundIndex + 1,
                total = uiState.totalRounds,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
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
                            Text("Back")
                        }
                    }
                }

                uiState.gameOver -> {
                    AnagramsGameOver(
                        score = uiState.score,
                        total = uiState.totalRounds,
                        earnedXp = uiState.earnedXp,
                        onRestart = viewModel::restartGame,
                        onBack = onBack
                    )
                }

                else -> {
                    val current = uiState.currentRound
                    val answer = uiState.answerTileIds
                        .mapNotNull { id -> uiState.tiles.find { it.id == id }?.letter }
                        .joinToString(separator = "")

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Round ${uiState.currentRoundIndex + 1}/${uiState.totalRounds}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "Definition",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Surface(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = current?.verso ?: "",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "Build the word",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFEFF7FF),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (answer.isEmpty()) "_".repeat((current?.recto?.length ?: 4)) else answer,
                                modifier = Modifier.padding(14.dp),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.tiles, key = { it.id }) { tile ->
                                val bg = if (tile.used) Color(0xFFEEEEEE) else Color(0xFFD8ECFF)
                                Surface(
                                    color = bg,
                                    shape = RoundedCornerShape(10.dp),
                                    onClick = { if (!tile.used) viewModel.selectTile(tile.id) }
                                ) {
                                    Box(
                                        modifier = Modifier.size(52.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tile.letter.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = if (tile.used) Color.Gray else Color(0xFF0B3A67)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GameButton(
                                text = "Undo",
                                onClick = viewModel::removeLastLetter,
                                enabled = !uiState.isRoundAnswered,
                                modifier = Modifier.weight(1f)
                            )
                            GameButton(
                                text = "Clear",
                                onClick = viewModel::clearAnswer,
                                enabled = !uiState.isRoundAnswered,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (uiState.isRoundAnswered) {
                            val isCorrect = uiState.isAnswerCorrect == true
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = if (isCorrect) Color(0xFFD4EDDA) else Color(0xFFF8D7DA),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isCorrect) {
                                        "+5 XP - Correct"
                                    } else {
                                        "Wrong. Word: ${current?.recto ?: ""}"
                                    },
                                    modifier = Modifier.padding(12.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) Color(0xFF155724) else Color(0xFF721C24)
                                )
                            }

                            GameButton(
                                text = "Next",
                                onClick = viewModel::nextRound
                            )
                        } else {
                            GameButton(
                                text = "Validate",
                                onClick = viewModel::submitAnswer,
                                enabled = answer.isNotEmpty()
                            )
                        }

                        Text(
                            text = "XP: ${uiState.earnedXp}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    GameButton(
                        text = "Back to menu",
                        onClick = onBack,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnagramsGameOver(
    score: Int,
    total: Int,
    earnedXp: Int,
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
            text = "Anagrams complete",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "Score: $score / $total",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "XP earned: $earnedXp",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        GameButton(
            text = "Play again",
            onClick = onRestart,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        GameButton(text = "Menu", onClick = onBack)
    }
}
