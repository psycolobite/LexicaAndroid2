package com.example.lexicaandroid2.presentation.games.matching

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.lexicaandroid2.presentation.games.common.SelectableButton

@Composable
fun MatchingScreen(
    repository: FlashcardRepository,
    onBack: () -> Unit,
    viewModel: MatchingViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MatchingViewModel(repository) as T
        }
    })
) {
    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(Unit) {
        if (uiState.isLoading && uiState.pairs.isEmpty()) {
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
        } else if (uiState.gameOver) {
            GameOverMatchingScreen(
                score = uiState.score,
                total = uiState.totalPairs,
                onRestart = { viewModel.resetGame() },
                onBack = onBack
            )
        } else {
            GameHeader(
                title = "Jeu de Correspondance",
                score = uiState.score,
                progress = uiState.score.toFloat() / maxOf(1, uiState.totalPairs)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Colonne gauche : mots
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF5F5F5))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.pairs) { pair ->
                        val isFound = uiState.foundPairs.contains(pair.wordId)
                        SelectableButton(
                            text = pair.wordText,
                            isSelected = uiState.selectedWord == pair.wordId,
                            onClick = { viewModel.selectWord(pair.wordId) },
                            enabled = !isFound,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Colonne droite : définitions
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF5F5F5))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.shuffledDefinitions) { def ->
                        val isUsed = uiState.foundPairs.any { id ->
                            uiState.pairs.find { it.wordId == id }?.definitionText == def
                        }
                        SelectableButton(
                            text = def,
                            isSelected = uiState.selectedDefinition == def,
                            onClick = { viewModel.selectDefinition(def) },
                            enabled = !isUsed,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
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
fun GameOverMatchingScreen(
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
            text = "Partie Terminée!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Score: $score / $total",
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

