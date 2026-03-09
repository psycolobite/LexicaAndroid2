package com.example.lexicaandroid2.presentation.games.chrono

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
import com.example.lexicaandroid2.presentation.games.common.SelectableButton

@Composable
fun ChronoScreen(
    repository: FlashcardRepository,
    onBack: () -> Unit,
    viewModel: ChronoViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChronoViewModel(repository) as T
        }
    })
) {
    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(Unit) {
        if (uiState.isLoading && uiState.allCards.isEmpty()) {
            viewModel.loadGameData()
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
                        Text("Back")
                    }
                }
            }

            uiState.gameFinished -> {
                ChronoGameOver(
                    score = uiState.score,
                    totalAnswered = uiState.totalAnswered,
                    earnedXp = uiState.earnedXp,
                    onRestart = viewModel::restart,
                    onBack = onBack
                )
            }

            !uiState.gameStarted -> {
                ChronoStart(
                    selectedDuration = uiState.selectedDurationSeconds,
                    onSelectDuration = viewModel::setDuration,
                    onStart = viewModel::startGame,
                    onBack = onBack
                )
            }

            else -> {
                GameHeader(
                    title = "Chrono",
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
                    Text(
                        text = "Time left: ${uiState.timeRemainingSeconds}s",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.timeRemainingSeconds <= 10L) Color(0xFFB71C1C) else Color(0xFF1B5E20)
                    )

                    Text(
                        text = "Answered: ${uiState.totalAnswered}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF5F5F5),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Word",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = uiState.currentQuestion?.recto ?: "",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Choose the definition",
                        fontWeight = FontWeight.Bold
                    )

                    uiState.answers.forEach { answer ->
                        SelectableButton(
                            text = answer,
                            isSelected = uiState.selectedAnswer == answer,
                            onClick = { viewModel.selectAnswer(answer) }
                        )
                    }

                    GameButton(
                        text = "Validate",
                        onClick = viewModel::submitAnswer,
                        enabled = uiState.selectedAnswer != null
                    )

                    val last = uiState.lastAnswerCorrect
                    if (last != null) {
                        Text(
                            text = if (last) "Correct (+5 XP)" else "Wrong",
                            color = if (last) Color(0xFF155724) else Color(0xFF721C24),
                            fontWeight = FontWeight.Bold
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

@Composable
private fun ChronoStart(
    selectedDuration: Long,
    onSelectDuration: (Long) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Chrono mode",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp)
        )

        Text(
            text = "Pick a duration then answer as many words as possible.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        listOf(30L, 60L, 300L).forEach { duration ->
            val isSelected = selectedDuration == duration
            SelectableButton(
                text = "${duration}s",
                isSelected = isSelected,
                onClick = { onSelectDuration(duration) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        GameButton(
            text = "Start",
            onClick = onStart,
            modifier = Modifier.padding(top = 8.dp)
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

        GameButton(text = "Back", onClick = onBack)
    }
}

@Composable
private fun ChronoGameOver(
    score: Int,
    totalAnswered: Int,
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
            text = "Time is up",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Correct answers: $score / $totalAnswered",
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
