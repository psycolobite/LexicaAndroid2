package com.example.lexicaandroid2.presentation.review

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showAudioOptions by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSession()
    }

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val current = uiState.currentCard
            when {
                uiState.isSessionFinished -> {
                    SessionCompleteView(
                        studiedCount = uiState.studiedCount,
                        onReturnToMenu = { navController.popBackStack() }
                    )
                }

                uiState.isLoading || (current == null && uiState.studiedCount == 0 && uiState.totalInSession == 0) -> {
                    CircularProgressIndicator()
                }

                current == null -> {
                    EmptyReviewState(
                        onReturnToMenu = { navController.popBackStack() }
                    )
                }

                else -> {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = { showAudioOptions = true },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Options audio"
                            )
                        }

                        DropdownMenu(
                            expanded = showAudioOptions,
                            onDismissRequest = { showAudioOptions = false },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Lire le mot automatiquement") },
                                trailingIcon = {
                                    Checkbox(
                                        checked = uiState.autoSpeakWord,
                                        onCheckedChange = { viewModel.toggleAutoSpeakWord() }
                                    )
                                },
                                onClick = { viewModel.toggleAutoSpeakWord() }
                            )
                            DropdownMenuItem(
                                text = { Text("Lire la définition automatiquement") },
                                trailingIcon = {
                                    Checkbox(
                                        checked = uiState.autoSpeakDefinition,
                                        onCheckedChange = { viewModel.toggleAutoSpeakDefinition() }
                                    )
                                },
                                onClick = { viewModel.toggleAutoSpeakDefinition() }
                            )
                        }
                    }

                    AnimatedContent(targetState = current, label = "cardTransition") { targetCard ->
                        var showDetails by rememberSaveable(targetCard.id) { mutableStateOf(false) }
                        val rotation by animateFloatAsState(
                            targetValue = if (uiState.isAnswerRevealed) 180f else 0f,
                            animationSpec = tween(durationMillis = 420),
                            label = "flip"
                        )
                        val isFront = rotation <= 90f
                        val density = LocalDensity.current.density

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Carte",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Row {
                                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                                        if (targetCard.favori) {
                                            Icon(
                                                imageVector = Icons.Filled.Favorite,
                                                contentDescription = "Retirer des favoris"
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Outlined.FavoriteBorder,
                                                contentDescription = "Ajouter aux favoris"
                                            )
                                        }
                                    }
                                    IconButton(onClick = { showDeleteDialog = true }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Supprimer la carte"
                                        )
                                    }
                                }
                            }

                            if (uiState.isSpeaking) {
                                Text(
                                    text = "🔊 Lecture en cours...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

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
                                        .clickable {
                                            if (uiState.isAnswerRevealed) {
                                                showDetails = false
                                            }
                                            viewModel.toggleAnswerReveal()
                                        }
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
                                            AudioTextLine(
                                                text = targetCard.recto,
                                                onSpeak = { viewModel.speakCurrentWord() },
                                                enabled = uiState.ttsReady,
                                                textStyle = MaterialTheme.typography.displaySmall.copy(
                                                    fontFamily = FontFamily.Serif
                                                ),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier
                                                .padding(24.dp)
                                                .graphicsLayer { rotationY = 180f },
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            AudioTextLine(
                                                text = targetCard.recto,
                                                onSpeak = { viewModel.speakCurrentWord() },
                                                enabled = uiState.ttsReady,
                                                textStyle = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            HorizontalDivider()
                                            Spacer(modifier = Modifier.height(12.dp))
                                            AudioTextLine(
                                                text = targetCard.verso,
                                                onSpeak = { viewModel.speakCurrentDefinition() },
                                                enabled = uiState.ttsReady,
                                                textStyle = MaterialTheme.typography.bodyLarge
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            TextButton(onClick = { showDetails = !showDetails }) {
                                                Text(text = if (showDetails) "-" else "+")
                                            }
                                            if (showDetails) {
                                                val detailsColor =
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                                if (targetCard.categorieGrammaticale.isNotBlank()) {
                                                    Text(
                                                        text = "Nature: ${targetCard.categorieGrammaticale}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = detailsColor
                                                    )
                                                }
                                                if (targetCard.synonymes.isNotEmpty()) {
                                                    Text(
                                                        text = "Synonymes: ${targetCard.synonymes.joinToString(", ")}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontStyle = FontStyle.Italic,
                                                        color = detailsColor
                                                    )
                                                }
                                                val exemple = targetCard.exemples.firstOrNull()
                                                if (!exemple.isNullOrBlank()) {
                                                    Text(
                                                        text = "Exemple: $exemple",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = detailsColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            if (!uiState.isAnswerRevealed) {
                                Button(
                                    onClick = { viewModel.toggleAnswerReveal() },
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Text(text = "VOIR REPONSE")
                                }
                            } else {
                                BoxWithConstraints(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val buttonTextSize =
                                        (maxWidth.value / 18f).coerceIn(9f, 12f).sp
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val errorColor = MaterialTheme.colorScheme.errorContainer
                                        val okColor = MaterialTheme.colorScheme.tertiaryContainer
                                        val easyColor = MaterialTheme.colorScheme.primaryContainer

                                        Button(
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = errorColor,
                                                contentColor = contentColorFor(errorColor)
                                            ),
                                            onClick = { viewModel.gradeCard(0) }
                                        ) {
                                            Text(
                                                text = "A REVOIR",
                                                maxLines = 2,
                                                textAlign = TextAlign.Center,
                                                fontSize = buttonTextSize,
                                                lineHeight = (buttonTextSize.value + 1).sp
                                            )
                                        }
                                        Button(
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = okColor,
                                                contentColor = contentColorFor(okColor)
                                            ),
                                            onClick = { viewModel.gradeCard(4) }
                                        ) {
                                            Text(
                                                text = "JE L'AI",
                                                maxLines = 2,
                                                textAlign = TextAlign.Center,
                                                fontSize = buttonTextSize,
                                                lineHeight = (buttonTextSize.value + 1).sp
                                            )
                                        }
                                        Button(
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = easyColor,
                                                contentColor = contentColorFor(easyColor)
                                            ),
                                            onClick = { viewModel.gradeCard(5) }
                                        ) {
                                            Text(
                                                text = "TROP\nFACILE",
                                                maxLines = 2,
                                                textAlign = TextAlign.Center,
                                                fontSize = buttonTextSize,
                                                lineHeight = (buttonTextSize.value + 1).sp
                                            )
                                        }
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
private fun AudioTextLine(
    text: String,
    onSpeak: () -> Unit,
    enabled: Boolean,
    textStyle: androidx.compose.ui.text.TextStyle,
    fontWeight: FontWeight? = null
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = textStyle,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 48.dp)
        )
        IconButton(
            onClick = onSpeak,
            enabled = enabled,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Lire l'audio"
            )
        }
    }
}

@Composable
private fun EmptyReviewState(
    onReturnToMenu: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Aucune carte a reviser",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onReturnToMenu) {
            Text(text = "Retour au Menu")
        }
    }
}

@Composable
private fun SessionCompleteView(
    studiedCount: Int,
    onReturnToMenu: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✓",
            style = MaterialTheme.typography.displayLarge,
            color = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Session terminee !",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$studiedCount cartes etudiees",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onReturnToMenu) {
            Text(text = "Retour au Menu")
        }
    }
}
