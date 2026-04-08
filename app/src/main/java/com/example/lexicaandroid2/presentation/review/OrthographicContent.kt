package com.example.lexicaandroid2.presentation.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewSessionChallengeKind
import com.example.lexicaandroid2.presentation.common.lexicaPanelContainerColor
import kotlinx.coroutines.delay

@Composable
internal fun OrthographicEventContent(
    uiState: ReviewUiState,
    card: Flashcard?,
    minCardHeight: Dp,
    maxCardHeight: Dp,
    onSpeakWord: () -> Unit,
    onSpeakDefinition: () -> Unit,
    showContextHint: Boolean = true
) {
    val isExtraSpelling = uiState.currentItemType == ReviewCurrentItemType.EXTRA_SPELLING
    val isSpellingChallenge = uiState.currentItemType == ReviewCurrentItemType.CHALLENGE &&
        (uiState.activeChallengeKind == ReviewSessionChallengeKind.SPELLING || uiState.activeChallengeKind == null)
    val isSemanticChallenge = uiState.currentItemType == ReviewCurrentItemType.CHALLENGE &&
        uiState.activeChallengeKind == ReviewSessionChallengeKind.SEMANTIC
    val resultVisible = uiState.eventResultMessage != null || uiState.eventResultSuccessful != null
    val challengeSuccessVisible = uiState.currentItemType == ReviewCurrentItemType.CHALLENGE && uiState.eventResultSuccessful == true
    val density = LocalDensity.current.density
    val orthographicInstanceKey = buildString {
        append(card?.id.orEmpty())
        append(':')
        append(uiState.currentItemType.name)
        append(':')
        append(uiState.activeChallengeKind?.name.orEmpty())
    }
    var isBackContentUnlocked by rememberSaveable(orthographicInstanceKey) { mutableStateOf(false) }

    LaunchedEffect(orthographicInstanceKey, resultVisible) {
        if (resultVisible) {
            isBackContentUnlocked = false
            delay(180)
            isBackContentUnlocked = true
        } else {
            isBackContentUnlocked = false
        }
    }

    val contextHintText = when {
        isExtraSpelling -> "Question orthographique"
        isSpellingChallenge -> "Défi orthographique"
        isSemanticChallenge -> "Défi sémantique"
        else -> uiState.eventInstruction
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showContextHint) {
            ReviewContextHint(text = contextHintText)
        }

        val frontText = when {
            isSemanticChallenge -> card?.recto.orEmpty()
            else -> card?.verso.orEmpty()
        }
        val answerText = when {
            isSemanticChallenge -> card?.verso.orEmpty()
            else -> card?.recto.orEmpty()
        }

        val frontTextStyle = rememberAdaptiveTextStyle(
            text = decodeReviewText(frontText),
            availableHeight = minCardHeight,
            preferDisplayStyle = decodeReviewText(frontText).length < 40,
            serif = isSemanticChallenge
        )
        val answerTextStyle = rememberAdaptiveTextStyle(
            text = decodeReviewText(answerText),
            availableHeight = minCardHeight,
            preferDisplayStyle = decodeReviewText(answerText).length < 40,
            serif = true
        )

        val rotation by animateFloatAsState(
            targetValue = if (resultVisible) 180f else 0f,
            animationSpec = tween(durationMillis = 420),
            label = "orthographicFlip"
        )
        val isFront = rotation <= 90f

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minCardHeight, max = if (resultVisible) minCardHeight else maxCardHeight)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12 * density
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (challengeSuccessVisible) Color(0xFFE8F5E9) else lexicaPanelContainerColor()
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isFront) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = minCardHeight)
                            .padding(start = 10.dp, top = 12.dp, end = 10.dp, bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isExtraSpelling) {
                            AudioTextLine(
                                text = frontText,
                                onSpeak = onSpeakDefinition,
                                enabled = uiState.ttsReady,
                                textStyle = frontTextStyle,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (isSpellingChallenge) {
                            AudioTextLine(
                                text = frontText,
                                onSpeak = onSpeakDefinition,
                                enabled = uiState.ttsReady,
                                textStyle = frontTextStyle,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = decodeReviewText(frontText),
                                style = frontTextStyle,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (isExtraSpelling) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(0.52f),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onSpeakWord, enabled = uiState.ttsReady) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Écouter le mot",
                                        tint = if (uiState.ttsReady) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        }
                                    )
                                }
                                Text(
                                    text = "Écouter le mot",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (uiState.ttsReady) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (challengeSuccessVisible) {
                            Box(modifier = Modifier.matchParentSize().background(Color(0xFF1F8F4C)))
                            CelebrationConfettiOverlay()
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = minCardHeight, max = minCardHeight)
                                .padding(start = 10.dp, top = 12.dp, end = 10.dp, bottom = 12.dp)
                                .graphicsLayer { rotationY = 180f },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (!isBackContentUnlocked) {
                                ReviewBackFacePlaceholder(minHeight = minCardHeight)
                            } else {
                                Text(
                                    text = decodeReviewText(answerText),
                                    style = answerTextStyle,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = if (challengeSuccessVisible) 2 else 3,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (challengeSuccessVisible) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = sanitizeAdminTestMessage(uiState.eventResultMessage).orEmpty(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (challengeSuccessVisible) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                if (!challengeSuccessVisible) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (isExtraSpelling) {
                                            "Mot correct : ${decodeReviewText(answerText)}"
                                        } else {
                                            "Réponse attendue : ${decodeReviewText(answerText)}"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
internal fun OrthographicFixedBottomControls(
    uiState: ReviewUiState,
    showAudioOptions: Boolean,
    onShowAudioOptionsChange: (Boolean) -> Unit,
    onInputChanged: (String) -> Unit,
    onToggleAutoSpeakWord: () -> Unit,
    onToggleAutoSpeakDefinition: () -> Unit,
    onValidate: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val showResult = uiState.eventResultMessage != null || uiState.eventResultSuccessful != null

    Column(modifier = Modifier.fillMaxWidth()) {
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
                    onClick = onBack,
                    enabled = uiState.canSkipCurrentEvent
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour"
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

            if (!showResult) {
                OutlinedTextField(
                    value = uiState.eventInput,
                    onValueChange = onInputChanged,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onValidate() }),
                    placeholder = {
                        Text(
                            text = if (uiState.activeChallengeKind == ReviewSessionChallengeKind.SEMANTIC) {
                                "Décris le sens..."
                            } else {
                                "Tape le mot..."
                            }
                        )
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onBack,
                        enabled = uiState.canSkipCurrentEvent,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(text = "PASSER")
                    }
                    Button(
                        onClick = onValidate,
                        enabled = uiState.eventInput.isNotBlank(),
                        modifier = Modifier.weight(2f)
                    ) {
                        Text(text = "VALIDER")
                    }
                }
            } else {
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "CONTINUER")
                }
            }
        }
    }
}

