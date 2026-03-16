package com.example.lexicaandroid2.presentation.games.matching

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    onGoHome: () -> Unit = onBack,
    onAwardXp: (Int) -> Unit = {},
    onGameCompleted: (Int) -> Unit = {},
    viewModel: MatchingViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MatchingViewModel(repository) as T
        }
    })
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.isLoading && uiState.pairs.isEmpty()) {
            viewModel.loadGame()
        }
    }

    LaunchedEffect(uiState.completedRounds) {
        if (uiState.completedRounds > 0) {
            onAwardXp(uiState.roundXpEarned)
            onGameCompleted(uiState.totalPairs)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            uiState.error != null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                GameButton("Retour", onBack)
            }

            uiState.roundState == MatchingRoundState.SUCCESS -> MatchingSuccessScreen(
                xpEarned = uiState.roundXpEarned
            )

            uiState.roundState == MatchingRoundState.FAILURE -> MatchingFailureScreen(
                mistakeCount = uiState.validationMistakeCount,
                onRestart = { viewModel.restartCurrentRound() },
                onBack = onGoHome
            )

            else -> {
                val assignedCount = uiState.assignments.size
                val canValidate = assignedCount == uiState.totalPairs && uiState.totalPairs > 0

                GameHeader(
                    title = "Correspondance",
                    score = assignedCount,
                    progress = assignedCount.toFloat() / maxOf(1, uiState.totalPairs)
                )

                Text(
                    text = "Associe toutes les paires mot / définition, puis valide l'ensemble.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(uiState.pairs) { index, pair ->
                            val badge = uiState.assignmentBadgeForWord(pair.wordId, index)
                            SelectableButton(
                                text = formatAssociationLabel(badge, pair.wordText),
                                isSelected = uiState.selectedWord == pair.wordId || badge != null,
                                isFound = false,
                                isWrong = false,
                                onClick = { viewModel.selectWord(pair.wordId) },
                                enabled = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(uiState.shuffledDefinitions) { _, definition ->
                            val badge = uiState.assignmentBadgeForDefinition(definition)
                            SelectableButton(
                                text = formatAssociationLabel(badge, definition),
                                isSelected = uiState.selectedDefinition == definition || badge != null,
                                isFound = false,
                                isWrong = false,
                                onClick = { viewModel.selectDefinition(definition) },
                                enabled = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Button(
                    onClick = { viewModel.validateAllPairs() },
                    enabled = canValidate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color(0xFFCCCCCC)
                    )
                ) {
                    Text(
                        text = if (canValidate) "✅ Valider les associations" else "Associe d'abord toutes les paires",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun MatchingUiState.assignmentBadgeForWord(wordId: String, index: Int): Int? {
    return if (assignments.containsKey(wordId)) index + 1 else null
}

private fun MatchingUiState.assignmentBadgeForDefinition(definition: String): Int? {
    val matchingEntry = assignments.entries.firstOrNull { it.value == definition } ?: return null
    val pairIndex = pairs.indexOfFirst { it.wordId == matchingEntry.key }
    return if (pairIndex >= 0) pairIndex + 1 else null
}

private fun formatAssociationLabel(badge: Int?, text: String): String {
    return if (badge == null) text else "[$badge] $text"
}

@Composable
private fun MatchingSuccessScreen(
    xpEarned: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFDFF6E3))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✨ Félicitations !",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Toutes les paires sont correctes",
            fontSize = 18.sp,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "+$xpEarned XP",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Passage automatique au lot suivant...",
            fontSize = 14.sp,
            color = Color(0xFF2E7D32)
        )
    }
}

@Composable
private fun MatchingFailureScreen(
    mistakeCount: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "❌ Ce n'est pas correct",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "$mistakeCount erreur${if (mistakeCount > 1) "s" else ""}",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))
        GameButton("🔄 Recommencer", onRestart)
        GameButton("← Accueil", onBack)
    }
}
