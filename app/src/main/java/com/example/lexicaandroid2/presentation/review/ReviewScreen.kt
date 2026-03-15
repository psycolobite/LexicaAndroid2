package com.example.lexicaandroid2.presentation.review

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavController
import com.example.lexicaandroid2.presentation.review.challenge.ChallengeOverlay
import com.example.lexicaandroid2.presentation.review.challenge.ChallengeResultOverlay
import com.example.lexicaandroid2.presentation.review.challenge.ChallengeType
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSession()
    }

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            val isLandscape = maxWidth > maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if (uiState.isSessionFinished) {
                    SessionCompleteView(
                        studiedCount = uiState.studiedCount,
                        onReturnToMenu = { navController.popBackStack() }
                    )
                    return@Column
                }

                val current = uiState.currentCard
                AnimatedContent(targetState = uiState.currentCard, label = "cardTransition") { targetCard ->
                    if (targetCard == null) {
                        Text(
                            text = "Aucune carte a reviser",
                            style = MaterialTheme.typography.titleMedium
                        )
                        return@AnimatedContent
                    }

                    var showDetails by rememberSaveable(targetCard.id) { mutableStateOf(false) }
                    val rotation by animateFloatAsState(
                        targetValue = if (uiState.isAnswerRevealed) 180f else 0f,
                        animationSpec = tween(durationMillis = 420),
                        label = "flip"
                    )
                    val isFront = rotation <= 90f
                    val density = LocalDensity.current.density
                    val activeChallengeType = uiState.activeChallengeType

                    if (activeChallengeType != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (val result = uiState.challengeResult) {
                                null -> ChallengeOverlay(
                                    challengeType = activeChallengeType,
                                    cardRecto = targetCard.recto,
                                    cardVerso = targetCard.verso,
                                    challengeInput = uiState.challengeInput,
                                    onInputChange = { viewModel.onChallengeInputChanged(it) },
                                    onValidate = { viewModel.validateChallenge() },
                                    onAbandon = { viewModel.dismissChallenge() }
                                )

                                else -> ChallengeResultOverlay(
                                    result = result,
                                    onContinue = { viewModel.dismissChallenge() },
                                    correctAnswer = when (activeChallengeType) {
                                        ChallengeType.SPELLING -> targetCard.recto
                                        ChallengeType.SEMANTIC -> targetCard.verso
                                        null -> ""
                                    }
                                )
                            }
                        }
                        return@AnimatedContent
                    }

                    // Layout normal de révision (sans défi)
                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Carte à gauche
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            rotationY = rotation
                                            cameraDistance = 12 * density
                                        }
                                ) {
                                    if (isFront) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = targetCard.recto,
                                                style = MaterialTheme.typography.displaySmall.copy(
                                                    fontFamily = FontFamily.Serif
                                                ),
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .padding(24.dp)
                                                .graphicsLayer { rotationY = 180f },
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = targetCard.recto,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            HorizontalDivider()
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = targetCard.verso,
                                                style = MaterialTheme.typography.bodyLarge,
                                                textAlign = TextAlign.Center
                                            )
                                            // ...existing code...
                                        }
                                    }
                                }
                            }
                            // Boutons à droite
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(24.dp))
                                if (!uiState.isAnswerRevealed) {
                                    Button(
                                        onClick = { viewModel.revealAnswer() },
                                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                    ) {
                                        Text(text = "VOIR REPONSE")
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // ...existing code...
                                    }
                                }
                            }
                        }
                    } else {
                        // Portrait : disposition classique
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // ...existing code...
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            rotationY = rotation
                                            cameraDistance = 12 * density
                                        }
                                ) {
                                    if (isFront) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = targetCard.recto,
                                                style = MaterialTheme.typography.displaySmall.copy(
                                                    fontFamily = FontFamily.Serif
                                                ),
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .padding(24.dp)
                                                .graphicsLayer { rotationY = 180f },
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = targetCard.recto,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Medium,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            HorizontalDivider()
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = targetCard.verso,
                                                style = MaterialTheme.typography.bodyLarge,
                                                textAlign = TextAlign.Center
                                            )
                                            // ...existing code...
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            if (!uiState.isAnswerRevealed) {
                                Button(
                                    onClick = { viewModel.revealAnswer() },
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Text(text = "VOIR REPONSE")
                                }
                            } else {
                                BoxWithConstraints(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val buttonTextSize =
                                        (maxWidth.value / 16f).coerceIn(10f, 13f).sp
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // ...existing code...
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Supprimer la carte ?") },
            text = { Text(text = "Cette action est definitive.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCurrentCard()
                    }
                ) {
                    Text(text = "Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "Annuler")
                }
            }
        )
    }
}

@Composable
private fun SessionCompleteView(
    studiedCount: Int,
    onReturnToMenu: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉 Bravo !",
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Vous avez révisé $studiedCount cartes",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onReturnToMenu) {
            Text("Retour au menu")
        }
    }
}
