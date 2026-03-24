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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.presentation.games.common.GameButton
import com.example.lexicaandroid2.presentation.games.common.GameTopAppBar
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

    LaunchedEffect(uiState.gameOver, uiState.totalXpEarned) {
        if (uiState.gameOver && !xpSent) {
            onAwardXp(uiState.totalXpEarned)
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

    Scaffold(
        topBar = {
            GameTopAppBar(
                title = "QCM",
                score = uiState.score,
                current = uiState.currentIndex + 1,
                total = uiState.flashcards.size,
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
                    Text(text = uiState.error, color = Color.Red)
                    Button(onClick = onBack) {
                        Text("Retour")
                    }
                }
            } else if (uiState.gameOver) {
                GameOverQcmScreen(
                    score = uiState.score,
                    total = uiState.flashcards.size,
                    xpEarned = uiState.totalXpEarned,
                    onRestart = { viewModel.resetGame() },
                    onBack = onBack
                )
            } else {
                val displayAttempt = (uiState.attemptCount + 1).coerceAtMost(3)
                val canAdvance = uiState.questionState == QcmQuestionState.CORRECT || uiState.questionState == QcmQuestionState.REVEALED

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "XP question : ${uiState.currentQuestionXp} | Essai : $displayAttempt/3",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
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
                            text = "Choisis la bonne définition puis valide.",
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
                            val isResolved = uiState.questionState == QcmQuestionState.CORRECT || uiState.questionState == QcmQuestionState.REVEALED
                            val isWrong = answer == uiState.lastIncorrectAnswer &&
                                (uiState.questionState == QcmQuestionState.RETRY || uiState.questionState == QcmQuestionState.REVEALED)

                            SelectableButton(
                                text = answer,
                                isSelected = isSelected,
                                isFound = isResolved && isCorrect,
                                isWrong = isWrong,
                                onClick = { viewModel.selectAnswer(answer) },
                                enabled = !isResolved,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    when (uiState.questionState) {
                        QcmQuestionState.RETRY -> {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFFDECEA)
                            ) {
                                Text(
                                    text = "Mauvaise réponse. Réessaie : l'XP de cette question a été divisée par 2.",
                                    modifier = Modifier.padding(14.dp),
                                    color = Color(0xFF8A1C1C),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        QcmQuestionState.CORRECT -> {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFDFF6E3)
                            ) {
                                Text(
                                    text = "Bonne réponse ! +${uiState.currentQuestionXp} XP",
                                    modifier = Modifier.padding(14.dp),
                                    color = Color(0xFF1B5E20),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        QcmQuestionState.REVEALED -> {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFFFF3CD)
                            ) {
                                Text(
                                    text = "3 erreurs : la bonne réponse était « ${uiState.currentQuestion.definition} ». 0 XP pour cette question.",
                                    modifier = Modifier.padding(14.dp),
                                    color = Color(0xFF7A5A00),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        QcmQuestionState.ANSWERING -> Unit
                    }
                }

                GameButton(
                    text = if (canAdvance) {
                        if (uiState.currentIndex + 1 >= uiState.flashcards.size) "Terminer" else "Question suivante"
                    } else {
                        "Valider"
                    },
                    onClick = {
                        if (canAdvance) {
                            viewModel.nextQuestion()
                        } else {
                            viewModel.validateAnswer()
                        }
                    },
                    enabled = canAdvance || uiState.selectedAnswer != null,
                    modifier = Modifier.padding(top = 8.dp)
                )

                GameButton(
                    text = "Retour au menu",
                    onClick = onBack,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
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
