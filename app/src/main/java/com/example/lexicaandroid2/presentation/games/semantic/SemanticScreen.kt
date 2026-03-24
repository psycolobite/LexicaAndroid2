package com.example.lexicaandroid2.presentation.games.semantic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.lexicaandroid2.presentation.games.common.SelectableButton

@Composable
fun SemanticScreen(
    repository: FlashcardRepository,
    onBack: () -> Unit,
    viewModel: SemanticViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SemanticViewModel(repository) as T
        }
    })
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.isLoading && uiState.cards.isEmpty()) {
            viewModel.loadGame()
        }
    }

    Scaffold(
        topBar = {
            GameTopAppBar(
                title = "Associations",
                score = uiState.score,
                current = uiState.currentIndex + 1,
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
                        Text(uiState.error ?: "Erreur", color = Color.Red)
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
                        Text("Associations terminees", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("Score: ${uiState.score}/${uiState.totalRounds}", fontSize = 20.sp)
                        GameButton(text = "Rejouer", onClick = viewModel::restart)
                        GameButton(text = "Menu", onClick = onBack)
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Question ${uiState.currentIndex + 1}/${uiState.totalRounds}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = uiState.prompt,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        uiState.details?.let {
                            Text(text = it, color = Color.Gray, fontSize = 12.sp)
                        }

                        uiState.choices.forEach { choice ->
                            val isSelected = uiState.selectedChoiceId == choice.id
                            SelectableButton(
                                text = choice.text,
                                isSelected = isSelected,
                                onClick = { viewModel.selectChoice(choice.id) },
                                enabled = !uiState.answered,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (uiState.answered) {
                            Text(
                                text = if (uiState.isCorrect == true) "Correct" else "Incorrect",
                                color = if (uiState.isCorrect == true) Color(0xFF155724) else Color(0xFF721C24),
                                fontWeight = FontWeight.Bold
                            )

                            GameButton(
                                text = "Suivant",
                                onClick = viewModel::nextQuestion
                            )
                        } else {
                            GameButton(
                                text = "Valider",
                                onClick = viewModel::validateAnswer,
                                enabled = uiState.selectedChoiceId != null
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
}
