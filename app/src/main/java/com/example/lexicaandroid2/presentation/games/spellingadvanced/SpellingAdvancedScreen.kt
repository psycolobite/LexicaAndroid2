package com.example.lexicaandroid2.presentation.games.spellingadvanced

import android.app.Application
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
fun SpellingAdvancedScreen(
    repository: FlashcardRepository,
    onBack: () -> Unit,
    viewModel: SpellingAdvancedViewModel = run {
        val context = LocalContext.current
        viewModel(factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SpellingAdvancedViewModel(
                    context.applicationContext as Application,
                    repository
                ) as T
            }
        })
    }
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    Text(text = uiState.error ?: "Erreur", color = MaterialTheme.colorScheme.error)
                    GameButton(text = "Retour", onClick = onBack)
                }
            }

            uiState.gameOver -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Spelling avance termine", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Score: ${uiState.score}/${uiState.totalRounds}",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GameButton(text = "Rejouer", onClick = viewModel::restart)
                    GameButton(text = "Menu", onClick = onBack)
                }
            }

            else -> {
                val card = uiState.currentCard
                GameHeader(
                    title = "Spelling Avance",
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
                        text = "Round ${uiState.currentIndex + 1}/${uiState.totalRounds}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.replayWord(useJoker = false) },
                            enabled = uiState.ttsReady
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Ecouter")
                        }
                        Text(
                            text = if (uiState.isSpeaking) "Lecture..." else "Ecouter le mot",
                            color = if (uiState.isSpeaking) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    Surface(color = Color(0xFFF5F5F5), tonalElevation = 2.dp) {
                        Text(
                            text = card?.verso ?: "",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp
                        )
                    }

                    TextField(
                        value = uiState.userInput,
                        onValueChange = viewModel::onUserInputChanged,
                        enabled = !uiState.answerSubmitted,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Saisis le mot") }
                    )

                    Text(
                        text = "Jokers restants: ${uiState.jokersRemaining}",
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GameButton(
                            text = "Reveler",
                            onClick = viewModel::useRevealLetterJoker,
                            enabled = uiState.jokersRemaining > 0 && !uiState.answerSubmitted,
                            modifier = Modifier.weight(1f)
                        )
                        GameButton(
                            text = "Rejouer",
                            onClick = { viewModel.replayWord(useJoker = true) },
                            enabled = uiState.jokersRemaining > 0 && !uiState.answerSubmitted,
                            modifier = Modifier.weight(1f)
                        )
                        GameButton(
                            text = "Sauter",
                            onClick = viewModel::useSkipJoker,
                            enabled = uiState.jokersRemaining > 0 && !uiState.answerSubmitted,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (card != null) {
                        val hint = card.recto.mapIndexed { index, c ->
                            if (index in uiState.revealedIndices) c else '_'
                        }.joinToString(" ")
                        Text(text = "Indice: $hint", color = Color.Gray)
                    }

                    uiState.feedback?.let { message ->
                        val ok = uiState.isCorrect == true
                        Surface(color = if (ok) Color(0xFFD4EDDA) else Color(0xFFF8D7DA)) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(10.dp),
                                color = if (ok) Color(0xFF155724) else Color(0xFF721C24)
                            )
                        }
                    }

                    if (uiState.answerSubmitted) {
                        GameButton(text = "Suivant", onClick = viewModel::nextRound)
                    } else {
                        GameButton(
                            text = "Valider",
                            onClick = viewModel::submitAnswer,
                            enabled = uiState.userInput.isNotBlank()
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
}
