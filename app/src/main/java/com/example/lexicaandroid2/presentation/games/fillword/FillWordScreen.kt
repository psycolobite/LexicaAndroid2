package com.example.lexicaandroid2.presentation.games.fillword

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
fun FillWordScreen(
    repository: FlashcardRepository,
    onBack: () -> Unit,
    viewModel: FillWordViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FillWordViewModel(repository) as T
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
                title = "Définition à compléter",
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
                        Text(text = uiState.error, color = MaterialTheme.colorScheme.error)
                        Button(onClick = onBack) {
                            Text("Retour")
                        }
                    }
                }

                uiState.gameOver -> {
                    FillWordGameOver(
                        score = uiState.score,
                        total = uiState.totalRounds,
                        onRestart = viewModel::restart,
                        onBack = onBack
                    )
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
                            text = "Complète la définition:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF5F5F5),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = uiState.question.maskedDefinition,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 16.sp
                            )
                        }

                        uiState.question.options.forEach { option ->
                            val selected = uiState.selectedOption == option
                            val isCorrectOption = option == uiState.question.correctWord
                            val bg = when {
                                !uiState.answered -> Color.Transparent
                                isCorrectOption -> Color(0xFFD4EDDA)
                                selected && !isCorrectOption -> Color(0xFFF8D7DA)
                                else -> Color.Transparent
                            }

                            SelectableButton(
                                text = option,
                                isSelected = selected,
                                onClick = { viewModel.selectOption(option) },
                                enabled = !uiState.answered,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bg)
                            )
                        }

                        if (uiState.answered) {
                            Text(
                                text = if (uiState.isCorrect == true) {
                                    "Bonne réponse"
                                } else {
                                    "Mauvaise réponse. Mot attendu: ${uiState.question.correctWord}"
                                },
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
                                enabled = uiState.selectedOption != null
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

@Composable
private fun FillWordGameOver(
    score: Int,
    total: Int,
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
            text = "Score: $score / $total",
            fontSize = 20.sp,
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
