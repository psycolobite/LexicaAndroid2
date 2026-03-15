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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.lexicaandroid2.presentation.games.common.FeedbackBanner
import com.example.lexicaandroid2.presentation.games.common.GameButton
import com.example.lexicaandroid2.presentation.games.common.GameHeader
import com.example.lexicaandroid2.presentation.games.common.SelectableButton

@Composable
fun MatchingScreen(
    repository: FlashcardRepository,
    onBack: () -> Unit,
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
    var xpSent by remember { mutableStateOf(false) }
    var completionSent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (uiState.isLoading && uiState.pairs.isEmpty()) viewModel.loadGame()
    }

    LaunchedEffect(uiState.gameOver) {
        if (uiState.gameOver && !xpSent) {
            onAwardXp(calculateGameXp(uiState.score, uiState.totalPairs))
            xpSent = true
        }
        if (!uiState.gameOver) xpSent = false
    }

    LaunchedEffect(uiState.gameOver, uiState.score) {
        if (uiState.gameOver && !completionSent) {
            onGameCompleted(uiState.score)
            completionSent = true
        }
        if (!uiState.gameOver) completionSent = false
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
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                GameButton("Retour", onBack)
            }

            uiState.gameOver -> GameOverMatchingScreen(
                score = uiState.score,
                total = uiState.totalPairs,
                xpEarned = calculateGameXp(uiState.score, uiState.totalPairs),
                onRestart = { viewModel.resetGame() },
                onBack = onBack
            )

            else -> {
                // ── Barre compacte score/progression ──────────────────────────
                GameHeader(
                    title = "Correspondance",
                    score = uiState.score,
                    progress = uiState.score.toFloat() / maxOf(1, uiState.totalPairs)
                )

                // ── Feedback banner ───────────────────────────────────────────
                FeedbackBanner(
                    show = uiState.lastMatchResult != MatchResult.NONE,
                    isSuccess = uiState.lastMatchResult == MatchResult.SUCCESS,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                // ── Instruction ───────────────────────────────────────────────
                Text(
                    text = "Sélectionne un mot et sa définition, puis appuie sur Valider",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // ── Deux colonnes ─────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Colonne gauche : MOTS
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.pairs) { pair ->
                            val isFound  = uiState.foundPairs.contains(pair.wordId)
                            val isWrong  = uiState.wrongPairs.contains(pair.wordId)
                            val isSelected = uiState.selectedWord == pair.wordId
                            SelectableButton(
                                text = pair.wordText,
                                isSelected = isSelected,
                                isFound = isFound,
                                isWrong = isWrong,
                                onClick = { viewModel.selectWord(pair.wordId) },
                                enabled = !isFound,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Colonne droite : DÉFINITIONS
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.shuffledDefinitions) { def ->
                            val matchingWordId = uiState.pairs.find { it.definitionText == def }?.wordId
                            val isFound   = matchingWordId != null && uiState.foundPairs.contains(matchingWordId)
                            val isWrong   = matchingWordId != null && uiState.wrongPairs.contains(matchingWordId)
                            val isSelected = uiState.selectedDefinition == def
                            SelectableButton(
                                text = def,
                                isSelected = isSelected,
                                isFound = isFound,
                                isWrong = isWrong,
                                onClick = { viewModel.selectDefinition(def) },
                                enabled = !isFound,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // ── Bouton Valider ────────────────────────────────────────────
                val canValidate = uiState.selectedWord != null && uiState.selectedDefinition != null
                Button(
                    onClick = { viewModel.validateSelection() },
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
                        text = if (canValidate) "✅ Valider" else "Sélectionne une paire…",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Calcul XP ─────────────────────────────────────────────────────────────────
private fun calculateGameXp(score: Int, total: Int): Int {
    if (total <= 0) return 5
    return when {
        score == total -> 25
        (score * 100) / total >= 50 -> 15
        else -> 5
    }
}

// ── Écran Game Over ───────────────────────────────────────────────────────────
@Composable
fun GameOverMatchingScreen(
    score: Int,
    total: Int,
    xpEarned: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    val isPerfect = score == total
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isPerfect) "🏆 Parfait !" else "Partie terminée !",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "$score / $total paires",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "+$xpEarned XP",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        GameButton("🔄 Rejouer", onRestart)
        GameButton("← Menu", onBack)
    }
}
