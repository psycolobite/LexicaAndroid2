package com.example.lexicaandroid2.presentation.games.matching

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.presentation.games.common.GameButton
import com.example.lexicaandroid2.presentation.games.common.GameTopAppBar
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
    val wordCenters = remember { mutableStateMapOf<String, Float>() }
    val definitionCenters = remember { mutableStateMapOf<String, Float>() }

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

    Scaffold(
        topBar = {
            GameTopAppBar(
                title = "Correspondance",
                score = uiState.potentialXp,
                current = uiState.completedRounds + 1,
                total = 0, // Endless
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
                    failureCount = uiState.failureCount,
                    potentialXp = uiState.potentialXp,
                    onRestart = { viewModel.restartCurrentRound() },
                    onBack = onGoHome
                )

                uiState.roundState == MatchingRoundState.SHOW_SOLUTION -> MatchingShowSolutionScreen(
                    pairs = uiState.pairs,
                    onContinue = {
                        // 0 XP award, proceed to next round
                        viewModel.loadGame()
                    },
                    onBack = onGoHome
                )

                else -> {
                    val assignedCount = uiState.assignments.size
                    val canValidate = assignedCount == uiState.totalPairs && uiState.totalPairs > 0
                    var boardSize by remember { mutableStateOf(IntSize.Zero) }
                    var boardTopInRoot by remember { mutableStateOf(0f) }

                    Text(
                        text = "XP actuel: ${uiState.potentialXp} | Essai: ${uiState.failureCount + 1}/3",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Associe toutes les paires puis valide.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )

                    MatchingAssignmentsLegend(
                        assignedCount = assignedCount,
                        totalPairs = uiState.totalPairs,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .onGloballyPositioned { coordinates ->
                                boardSize = coordinates.size
                                boardTopInRoot = coordinates.positionInRoot().y
                            }
                    ) {
                        MatchingConnectionsCanvas(
                            uiState = uiState,
                            wordCenters = wordCenters,
                            definitionCenters = definitionCenters,
                            boardSize = boardSize,
                            modifier = Modifier.matchParentSize()
                        )

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                uiState.pairs.forEachIndexed { index, pair ->
                                    val badge = uiState.assignmentBadgeForWord(pair.wordId, index)
                                    SelectableButton(
                                        text = formatAssociationLabel(badge, pair.wordText),
                                        isSelected = uiState.selectedWord == pair.wordId || badge != null,
                                        isFound = false,
                                        isWrong = false,
                                        onClick = { viewModel.selectWord(pair.wordId) },
                                        enabled = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned { coordinates ->
                                                wordCenters[pair.wordId] =
                                                    coordinates.positionInRoot().y - boardTopInRoot + coordinates.size.height / 2f
                                            }
                                    )
                                }
                            }

                            MatchingConnectorColumn(
                                uiState = uiState,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                uiState.shuffledDefinitions.forEach { definition ->
                                    val badge = uiState.assignmentBadgeForDefinition(definition)
                                    SelectableButton(
                                        text = formatAssociationLabel(badge, definition),
                                        isSelected = uiState.selectedDefinition == definition || badge != null,
                                        isFound = false,
                                        isWrong = false,
                                        onClick = { viewModel.selectDefinition(definition) },
                                        enabled = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned { coordinates ->
                                                definitionCenters[definition] =
                                                    coordinates.positionInRoot().y - boardTopInRoot + coordinates.size.height / 2f
                                            }
                                    )
                                }
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
                            text = if (canValidate) "✅ Valider" else "Associer tout",
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
private fun MatchingAssignmentsLegend(
    assignedCount: Int,
    totalPairs: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = if (totalPairs > 0) {
                "Liaisons visibles au centre : $assignedCount / $totalPairs"
            } else {
                "Prépare les liaisons avant validation"
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MatchingConnectorColumn(
    uiState: MatchingUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        uiState.pairs.forEachIndexed { index, pair ->
            val badge = uiState.assignmentBadgeForWord(pair.wordId, index)
            val color = if (badge == null) Color(0xFFCFD8DC) else relationColor(index)
            Card(
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = if (badge == null) 0.35f else 0.9f))
            ) {
                Text(
                    text = badge?.toString() ?: "·",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                    fontWeight = FontWeight.Bold,
                    color = if (badge == null) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MatchingConnectionsCanvas(
    uiState: MatchingUiState,
    wordCenters: Map<String, Float>,
    definitionCenters: Map<String, Float>,
    boardSize: IntSize,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (boardSize.width == 0 || boardSize.height == 0) return@Canvas

        val startX = boardSize.width * 0.34f
        val endX = boardSize.width * 0.66f

        uiState.pairs.forEachIndexed { index, pair ->
            val definition = uiState.assignments[pair.wordId] ?: return@forEachIndexed
            val startY = wordCenters[pair.wordId] ?: return@forEachIndexed
            val endY = definitionCenters[definition] ?: return@forEachIndexed
            val color = relationColor(index)

            val path = Path().apply {
                moveTo(startX, startY)
                cubicTo(
                    startX + 40f,
                    startY,
                    endX - 40f,
                    endY,
                    endX,
                    endY
                )
            }

            drawPath(
                path = path,
                color = color,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f, cap = StrokeCap.Round)
            )

            drawCircle(
                color = color,
                radius = 8f,
                center = Offset(startX, startY)
            )
            drawCircle(
                color = color,
                radius = 8f,
                center = Offset(endX, endY)
            )
        }
    }
}

private fun relationColor(index: Int): Color {
    val palette = listOf(
        Color(0xFF1976D2),
        Color(0xFF7B1FA2),
        Color(0xFF00897B),
        Color(0xFFF57C00)
    )
    return palette[index % palette.size]
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
    failureCount: Int,
    potentialXp: Int,
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
            text = "❌ Pas tout à fait...",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "$mistakeCount erreur${if (mistakeCount > 1) "s" else ""}",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Essai $failureCount/3",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "XP restante si tu réussis : $potentialXp",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        GameButton("🔄 Réessayer", onRestart)
        GameButton("← Abandonner", onBack)
    }
}

@Composable
private fun MatchingShowSolutionScreen(
    pairs: List<com.example.lexicaandroid2.presentation.games.common.MatchingPair>,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Solution",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Dommage ! Voici les bonnes réponses :",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (pair in pairs) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F0F0), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(text = pair.wordText, fontWeight = FontWeight.Bold)
                    Text(text = "➜ ${pair.definitionText}", color = Color(0xFF2E7D32))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        GameButton("Continuer (0 XP)", onContinue)
        GameButton("Quitter", onBack)
    }
}
