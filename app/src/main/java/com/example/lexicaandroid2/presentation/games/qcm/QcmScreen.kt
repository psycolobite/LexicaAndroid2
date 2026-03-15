package com.example.lexicaandroid2.presentation.games.qcm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.lexicaandroid2.presentation.games.common.SelectableButton

@Composable
fun QcmScreen(
    repository: FlashcardRepository,
    onBack: () -> Unit,
    onAwardXp: (Int) -> Unit = {},
    onGameCompleted: (Int) -> Unit = {},
    viewModel: QcmViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return QcmViewModel(repository) as T
        }
    })
) {
    val uiState = viewModel.uiState.collectAsState().value
    var xpSent by remember { mutableStateOf(false) }
    var completionSent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (uiState.isLoading && uiState.flashcards.isEmpty()) {
            viewModel.loadGame()
        }
    }

    LaunchedEffect(uiState.gameOver, uiState.score, uiState.flashcards.size) {
        if (uiState.gameOver && !xpSent) {
            onAwardXp(calculateGameXp(uiState.score, uiState.flashcards.size))
            xpSent = true
        }
        if (!uiState.gameOver) {
            xpSent = false
        }
    }

    LaunchedEffect(uiState.gameOver, uiState.score) {
        if (uiState.gameOver && !completionSent) {
            onGameCompleted(uiState.score)
            completionSent = true
        }
        if (!uiState.gameOver) {
            completionSent = false
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
                Text(text = uiState.error ?: "Erreur inconnue", color = Color.Red)
                Button(onClick = onBack) {
                    Text("Retour")
                }
            }
        } else if (uiState.gameOver) {
            GameOverQcmScreen(
                score = uiState.score,
                total = uiState.flashcards.size,
                xpEarned = calculateGameXp(uiState.score, uiState.flashcards.size),
                onRestart = { viewModel.resetGame() },
                onBack = onBack
            )
        } else {
            val progress = if (uiState.flashcards.isEmpty()) 0f
                          else (uiState.currentIndex.toFloat() / uiState.flashcards.size)

            GameHeader(
                title = "QCM - Lexica",
                score = uiState.score,
                progress = progress
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Question ${uiState.currentIndex + 1}/${uiState.flashcards.size}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Text(
                    text = "Quel est le mot?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.currentQuestion.mot,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Quelle est la définition?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.answers.forEach { answer ->
                        val isSelected = uiState.selectedAnswer == answer
                        val isCorrect = answer == uiState.currentQuestion.definition
                        val backgroundColor = when {
                            !uiState.answered -> Color.Transparent
                            isCorrect -> Color(0xFFD4EDDA) // Green
                            isSelected && !isCorrect -> Color(0xFFF8D7DA) // Red
                            else -> Color.Transparent
                        }

                        SelectableButton(
                            text = answer,
                            isSelected = isSelected,
                            onClick = { if (!uiState.answered) viewModel.selectAnswer(answer) },
                            enabled = !uiState.answered,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor)
                        )
                    }
                }
            }

            if (uiState.answered) {
                GameButton(
                    text = "Suivant",
                    onClick = { viewModel.validateAndNext() },
                    modifier = Modifier.padding(16.dp)
                )
            }

            GameButton(
                text = "Retour au menu",
                onClick = onBack,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

private fun calculateGameXp(score: Int, total: Int): Int {
    if (total <= 0) return 5
    val percent = (score * 100) / total
    return when {
        score == total -> 25
        percent >= 50 -> 15
        else -> 5
    }
}

@Composable
fun GameOverQcmScreen(
    score: Int,
    total: Int,
    xpEarned: Int,
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
            text = "Quiz Terminé!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Score: $score / $total",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "${(score * 100) / maxOf(1, total)}%",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "XP gagné: $xpEarned",
            fontSize = 16.sp,
            color = Color.Gray,
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

