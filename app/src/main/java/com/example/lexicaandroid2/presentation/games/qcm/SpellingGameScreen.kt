package com.example.lexicaandroid2.presentation.games.qcm

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.presentation.games.common.GameButton
import com.example.lexicaandroid2.presentation.games.common.GameTopAppBar

@Composable
fun SpellingGameScreen(
    repository: FlashcardRepository,
    onBack: () -> Unit,
    onAwardXp: (Int) -> Unit = {},
    onGameCompleted: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: SpellingGameViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SpellingGameViewModel(
                    context.applicationContext as Application,
                    repository
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var xpSent by remember { mutableStateOf(false) }
    var completionSent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (uiState.isLoading && uiState.flashcards.isEmpty()) {
            viewModel.loadGame()
        }
    }

    LaunchedEffect(uiState.gameOver, uiState.score, uiState.totalWords) {
        if (uiState.gameOver && !xpSent) {
            onAwardXp(calculateGameXp(uiState.score, uiState.totalWords))
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
                title = "Dictée",
                score = uiState.score,
                current = uiState.currentIndex + 1,
                total = uiState.totalWords,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
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
                    Text(text = uiState.error ?: "Erreur inconnue", color = Color.Red, fontSize = 16.sp)
                    GameButton(
                        text = "Retour",
                        onClick = onBack,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else if (uiState.gameOver) {
                GameOverSpellingScreen(
                    score = uiState.score,
                    total = uiState.totalWords,
                    xpEarned = calculateGameXp(uiState.score, uiState.totalWords),
                    onRestart = { viewModel.resetGame() },
                    onBack = onBack
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Mot ${uiState.currentIndex + 1}/${uiState.totalWords}",
                        fontSize = 12.sp,
                    )

                    // Listen Button
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(
                                color = if (uiState.isSpeaking)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { viewModel.speakWord(uiState.currentQuestion.mot) },
                            enabled = !uiState.isSpeaking && uiState.ttsReady,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Écouter le mot",
                                tint = Color.White,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    if (uiState.isSpeaking) {
                        Text(
                            text = "🔊 En cours de lecture...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Input Field
                    TextField(
                        value = uiState.userInput,
                        onValueChange = { viewModel.updateUserInput(it) },
                        label = { Text("Tapez le mot...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = when {
                                    !uiState.answered -> MaterialTheme.colorScheme.outline
                                    uiState.isCorrect == true -> Color(0xFF28A745) // Green
                                    else -> Color(0xFFDC3545) // Red
                                },
                                shape = RoundedCornerShape(8.dp)
                            ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (!uiState.answered && uiState.userInput.isNotEmpty()) {
                                    viewModel.validateAnswer()
                            }
                        }
                    ),
                    enabled = !uiState.answered,
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                // Feedback
                if (uiState.answered) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (uiState.isCorrect == true)
                                        Color(0xFFD4EDDA) else Color(0xFFF8D7DA),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.isCorrect == true) "✅ Correct !" else "❌ Incorrect",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isCorrect == true)
                                    Color(0xFF155724) else Color(0xFF721C24)
                            )
                        }

                        if (uiState.isCorrect == false) {
                            Text(
                                text = "Réponse correcte: ${uiState.currentQuestion.mot}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            if (uiState.answered) {
                GameButton(
                    text = "Suivant",
                    onClick = { viewModel.nextQuestion() },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else if (uiState.userInput.isNotEmpty()) {
                GameButton(
                    text = "Valider",
                    onClick = { viewModel.validateAnswer() },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            GameButton(
                text = "Retour au menu",
                onClick = onBack,
                modifier = Modifier.padding(16.dp),
                backgroundColor = MaterialTheme.colorScheme.secondary
            )
        }
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
fun GameOverSpellingScreen(
    score: Int,
    total: Int,
    xpEarned: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Dictée Terminée!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Score: $score / $total",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "${(score * 100) / maxOf(1, total)}%",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "XP gagné: $xpEarned",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.outline,
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
