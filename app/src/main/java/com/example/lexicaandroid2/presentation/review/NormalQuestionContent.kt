package com.example.lexicaandroid2.presentation.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.presentation.common.lexicaPanelContainerColor

@Composable
internal fun NormalQuestionContent(
    uiState: ReviewUiState,
    card: Flashcard,
    canEditCard: Boolean,
    isFavorite: Boolean,
    minCardHeight: Dp,
    maxCardHeight: Dp,
    onToggleReveal: () -> Unit,
    onSpeakCurrentFace: () -> Unit,
    onSpeakWord: () -> Unit,
    onSpeakDefinition: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val questionInstanceKey = uiState.normalQuestionInstanceKey.ifBlank {
        "${card.id}:${uiState.presentationMode}"
    }
    var showDetails by rememberSaveable(questionInstanceKey) { mutableStateOf(false) }
    var isBackContentUnlocked by rememberSaveable(questionInstanceKey) {
        mutableStateOf(uiState.isAnswerRevealed)
    }
    LaunchedEffect(questionInstanceKey, uiState.isAnswerRevealed) {
        if (uiState.isAnswerRevealed) {
            isBackContentUnlocked = true
        }
    }
    val rotation by animateFloatAsState(
        targetValue = if (uiState.isAnswerRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "flip"
    )
    val isFront = rotation <= 90f
    val density = LocalDensity.current.density
    val cardDisplay = remember(card, uiState.presentationMode) {
        buildReviewCardDisplay(card, uiState.presentationMode)
    }
    val frontTextStyle = rememberAdaptiveTextStyle(
        text = cardDisplay.frontText,
        availableHeight = minCardHeight,
        preferDisplayStyle = cardDisplay.frontText.length < 40,
        serif = cardDisplay.frontUsesSerif
    )
    val answerTextStyle = rememberAdaptiveTextStyle(
        text = cardDisplay.answerText,
        availableHeight = minCardHeight,
        preferDisplayStyle = cardDisplay.answerText.length < 40,
        serif = true
    )
    val supportTextStyle = rememberAdaptiveTextStyle(
        text = cardDisplay.supportingText,
        availableHeight = minCardHeight,
        preferDisplayStyle = false,
        serif = false
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minCardHeight, max = maxCardHeight)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12 * density
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = lexicaPanelContainerColor()),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (uiState.isAnswerRevealed) showDetails = false
                        onToggleReveal()
                    }
            ) {
                if (isFront) {
                    ReviewFrontFace(
                        text = cardDisplay.frontText,
                        onSpeak = onSpeakCurrentFace,
                        enabled = uiState.ttsReady,
                        textStyle = frontTextStyle,
                        minHeight = minCardHeight
                    )
                } else if (!isBackContentUnlocked) {
                    ReviewBackFacePlaceholder(minHeight = minCardHeight)
                } else {
                    val speakPrimaryBack = if (uiState.presentationMode == ReviewPresentationMode.WORD_TO_DEFINITION ||
                        uiState.presentationMode == ReviewPresentationMode.SPELLING_CHALLENGE
                    ) {
                        onSpeakWord
                    } else {
                        onSpeakDefinition
                    }
                    val speakSecondaryBack = if (uiState.presentationMode == ReviewPresentationMode.WORD_TO_DEFINITION ||
                        uiState.presentationMode == ReviewPresentationMode.SPELLING_CHALLENGE
                    ) {
                        onSpeakDefinition
                    } else {
                        onSpeakWord
                    }
                    ReviewBackFace(
                        answerText = cardDisplay.answerText,
                        supportingText = cardDisplay.supportingText,
                        onSpeakAnswer = speakPrimaryBack,
                        onSpeakSupporting = speakSecondaryBack,
                        enabled = uiState.ttsReady,
                        answerTextStyle = answerTextStyle,
                        supportingTextStyle = supportTextStyle,
                        minHeight = minCardHeight,
                        showSupportingFirst = cardDisplay.showSupportingFirst,
                        definitionOnTop = uiState.presentationMode == ReviewPresentationMode.DEFINITION_TO_WORD ||
                            uiState.presentationMode == ReviewPresentationMode.SEMANTIC_CHALLENGE,
                        showDetails = showDetails,
                        onToggleDetails = { showDetails = !showDetails },
                        card = card,
                        canEditCard = canEditCard,
                        isFavorite = isFavorite,
                        onToggleFavorite = onToggleFavorite,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
internal fun ReviewBackFacePlaceholder(minHeight: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .graphicsLayer { rotationY = 180f }
    )
}

@Composable
internal fun ReviewFrontFace(
    text: String,
    onSpeak: () -> Unit,
    enabled: Boolean,
    textStyle: androidx.compose.ui.text.TextStyle,
    minHeight: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .verticalScroll(rememberScrollState())
            .padding(start = 10.dp, top = 12.dp, end = 10.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AudioTextLine(text = text, onSpeak = onSpeak, enabled = enabled, textStyle = textStyle, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun ReviewBackFace(
    answerText: String,
    supportingText: String,
    onSpeakAnswer: () -> Unit,
    onSpeakSupporting: () -> Unit,
    enabled: Boolean,
    answerTextStyle: androidx.compose.ui.text.TextStyle,
    supportingTextStyle: androidx.compose.ui.text.TextStyle,
    minHeight: Dp,
    showSupportingFirst: Boolean,
    definitionOnTop: Boolean,
    showDetails: Boolean,
    onToggleDetails: () -> Unit,
    card: Flashcard,
    canEditCard: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val topText = if (showSupportingFirst) supportingText else answerText
    val topSpeak = if (showSupportingFirst) onSpeakSupporting else onSpeakAnswer
    val topStyle = if (showSupportingFirst) supportingTextStyle else answerTextStyle
    val topFontWeight = if (showSupportingFirst) null else FontWeight.Bold

    val bottomText = if (showSupportingFirst) answerText else supportingText
    val bottomSpeak = if (showSupportingFirst) onSpeakAnswer else onSpeakSupporting
    val bottomStyle = if (showSupportingFirst) answerTextStyle else supportingTextStyle
    val bottomFontWeight = if (showSupportingFirst) FontWeight.Bold else null

    val definitionText = if (definitionOnTop) topText else bottomText
    val definitionShare = when {
        definitionText.length > 240 -> 0.8f
        definitionText.length > 160 -> 0.7f
        definitionText.length > 90 -> 0.6f
        else -> 0.5f
    }
    val topWeight = if (definitionOnTop) definitionShare else 1f - definitionShare
    val bottomWeight = 1f - topWeight
    val actionRowEstimatedHeight = 56.dp
    val contentMinHeight = (minHeight - actionRowEstimatedHeight).coerceAtLeast(160.dp)
    val topSectionMinHeight = contentMinHeight * topWeight
    val bottomSectionMinHeight = contentMinHeight * bottomWeight
    val sectionModifier = if (showDetails) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .heightIn(min = contentMinHeight)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .verticalScroll(rememberScrollState())
            .padding(start = 10.dp, top = 12.dp, end = 10.dp, bottom = 12.dp)
            .graphicsLayer { rotationY = 180f },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(
            modifier = sectionModifier,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = topSectionMinHeight),
                contentAlignment = Alignment.Center
            ) {
                AudioTextLine(
                    text = topText,
                    onSpeak = topSpeak,
                    enabled = enabled,
                    textStyle = topStyle,
                    fontWeight = topFontWeight
                )
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.52f).align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = bottomSectionMinHeight),
                contentAlignment = Alignment.Center
            ) {
                AudioTextLine(
                    text = bottomText,
                    onSpeak = bottomSpeak,
                    enabled = enabled,
                    textStyle = bottomStyle,
                    fontWeight = bottomFontWeight
                )
            }
        }

        if (showDetails) {
            ReviewCardDetails(card = card)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onToggleDetails) {
                Text(text = if (showDetails) "Masquer les détails" else "Plus d'infos")
            }

            if (canEditCard) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite) {
                        if (isFavorite) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Retirer des favoris")
                        } else {
                            Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Ajouter aux favoris")
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Supprimer la carte")
                    }
                }
            }
        }
    }
}

@Composable
internal fun ReviewFixedBottomControls(
    uiState: ReviewUiState,
    showAudioOptions: Boolean,
    onShowAudioOptionsChange: (Boolean) -> Unit,
    onUndo: () -> Unit,
    onToggleAutoSpeakWord: () -> Unit,
    onToggleAutoSpeakDefinition: () -> Unit,
    onReveal: () -> Unit,
    onGrade: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onUndo,
                    enabled = uiState.canUndo
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Annuler la dernière réponse"
                    )
                }

                Box {
                    AudioSettingsButton(onClick = { onShowAudioOptionsChange(true) })
                    DropdownMenu(
                        expanded = showAudioOptions,
                        onDismissRequest = { onShowAudioOptionsChange(false) }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Lire le mot automatiquement") },
                            trailingIcon = {
                                Checkbox(
                                    checked = uiState.autoSpeakWord,
                                    onCheckedChange = { onToggleAutoSpeakWord() }
                                )
                            },
                            onClick = { onToggleAutoSpeakWord() }
                        )
                        DropdownMenuItem(
                            text = { Text("Lire la définition automatiquement") },
                            trailingIcon = {
                                Checkbox(
                                    checked = uiState.autoSpeakDefinition,
                                    onCheckedChange = { onToggleAutoSpeakDefinition() }
                                )
                            },
                            onClick = { onToggleAutoSpeakDefinition() }
                        )
                    }
                }
            }

            ReviewPrimaryActionBar(
                isAnswerRevealed = uiState.isAnswerRevealed,
                onReveal = onReveal,
                onGrade = onGrade
            )
        }
    }
}

@Composable
private fun ReviewPrimaryActionBar(
    isAnswerRevealed: Boolean,
    onReveal: () -> Unit,
    onGrade: (Int) -> Unit
) {
    if (!isAnswerRevealed) {
        Button(
            onClick = onReveal,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(text = "VOIR REPONSE")
        }
    } else {
        val buttonTextSize = 11.sp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GradeButton(Modifier.weight(1f), "A REVOIR", buttonTextSize, ReviewButtonColors.Error) { onGrade(0) }
            GradeButton(Modifier.weight(1f), "JE L'AI", buttonTextSize, ReviewButtonColors.Ok) { onGrade(4) }
            GradeButton(Modifier.weight(1f), "TROP\nFACILE", buttonTextSize, ReviewButtonColors.Easy) { onGrade(5) }
        }
    }
}

@Composable
private fun ReviewCardDetails(card: Flashcard) {
    val detailsColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
    val firstExample = card.exemples.firstOrNull()?.let(::decodeReviewText)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (card.categorieGrammaticale.isNotBlank()) {
            Text(
                text = "Nature : ${decodeReviewText(card.categorieGrammaticale)}",
                style = MaterialTheme.typography.bodySmall,
                color = detailsColor
            )
        }
        if (card.synonymes.isNotEmpty()) {
            Text(
                text = "Synonymes : ${card.synonymes.joinToString(", ") { decodeReviewText(it) }}",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = detailsColor
            )
        }
        if (!firstExample.isNullOrBlank()) {
            Text(
                text = "Exemple : $firstExample",
                style = MaterialTheme.typography.bodySmall,
                color = detailsColor
            )
        }
    }
}

