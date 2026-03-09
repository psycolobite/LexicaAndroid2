package com.example.lexicaandroid2.presentation.games.hangman

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.presentation.games.common.GameButton
import com.example.lexicaandroid2.presentation.games.common.GameHeader

@Composable
fun HangmanScreen(
    repository: FlashcardRepository,
    onBack: () -> Unit,
    viewModel: HangmanViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return HangmanViewModel(repository) as T
        }
    })
) {
    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(Unit) {
        if (uiState.isLoading && uiState.currentWord.isEmpty()) {
            viewModel.loadGame()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = uiState.error!!, color = Color.Red)
                Button(onClick = onBack) {
                    Text("Retour")
                }
            }
        } else if (uiState.gameOver && !uiState.won) {
            GameOverHangmanScreen(
                score = uiState.score,
                word = uiState.currentWord,
                onRestart = { viewModel.resetGame() },
                onBack = onBack
            )
        } else {
            GameHeader(
                title = "Pendu - Lexica",
                score = uiState.score,
                progress = uiState.score.toFloat() / maxOf(1, uiState.totalWords)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vies: ${uiState.livesRemaining}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    HangmanDrawing(uiState.livesRemaining)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.currentWord.forEach { letter ->
                            Text(
                                text = if (uiState.guessedLetters.contains(letter)) letter.toString() else "_",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Lettres essayées: ${uiState.wrongLetters.joinToString(", ")}",
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)
                )

                Text(
                    text = "Choisissez une lettre:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(('A'..'Z').toList()) { letter ->
                        val isGuessed = uiState.guessedLetters.contains(letter)
                        val isWrong = uiState.wrongLetters.contains(letter)
                        val isUsed = isGuessed || isWrong

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = when {
                                        isGuessed -> Color(0xFFD4EDDA)
                                        isWrong -> Color(0xFFF8D7DA)
                                        else -> Color.White
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                .clickable(enabled = !isUsed) { viewModel.guessLetter(letter) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUsed) Color.Gray else Color.Black
                            )
                        }
                    }
                }

                if (uiState.won) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFD4EDDA))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mot trouvé! ${uiState.currentWord}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF155724)
                        )
                    }
                    GameButton(
                        text = "Mot suivant",
                        onClick = { viewModel.nextWord() }
                    )
                }
            }

            GameButton(
                text = "Retour au menu",
                onClick = onBack,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun HangmanDrawing(livesRemaining: Int) {
    Box(modifier = Modifier.size(60.dp), contentAlignment = Alignment.Center) {
        Text(
            text = when (livesRemaining) {
                6 -> "┌─┐\n│ \n│\n│\n├─┘\n─"
                5 -> "┌─┐\n│ O\n│\n│\n├─┘\n─"
                4 -> "┌─┐\n│ O\n│ |\n│\n├─┘\n─"
                3 -> "┌─┐\n│ O\n│/|\n│\n├─┘\n─"
                2 -> "┌─┐\n│ O\n│/|\\\n│\n├─┘\n─"
                1 -> "┌─┐\n│ O\n│/|\\\n│ /\n├─┘\n─"
                else -> "┌─┐\n│ O\n│/|\\\n│/ \\\n├─┘\n─"
            },
            fontSize = 8.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

@Composable
fun GameOverHangmanScreen(
    score: Int,
    word: String,
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
            text = "Partie Terminée!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Le mot était: $word",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Score: $score",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        GameButton(
            text = "Recommencer",
            onClick = onRestart,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        GameButton(
            text = "Menu",
            onClick = onBack
        )
    }
}

