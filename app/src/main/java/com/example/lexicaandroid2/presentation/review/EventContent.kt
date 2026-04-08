package com.example.lexicaandroid2.presentation.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.presentation.common.lexicaPanelContainerColor

@Composable
internal fun MultipleChoiceEventContent(
    uiState: ReviewUiState,
    card: Flashcard?,
    onSpeakPrompt: () -> Unit,
    onSelectChoice: (String) -> Unit,
    onSubmit: () -> Unit,
    onContinue: () -> Unit
) {
    EventSurface {
        if (uiState.eventResultMessage != null || uiState.eventResultSuccessful != null) {
            EventResultCard(uiState, onContinue)
            return@EventSurface
        }

        if (card != null) {
            Text(
                text = if (uiState.presentationMode == ReviewPresentationMode.WORD_TO_DEFINITION) decodeReviewText(card.recto) else decodeReviewText(card.verso),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            IconButton(onClick = onSpeakPrompt, enabled = uiState.ttsReady) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Lire")
            }
        }

        Text(uiState.eventInstruction, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))

        uiState.eventOptions.forEach { option ->
            val selected = option == uiState.selectedChoice
            Button(
                onClick = { onSelectChoice(option) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else lexicaPanelContainerColor(),
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(text = decodeReviewText(option), textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(onClick = onSubmit, enabled = uiState.selectedChoice != null, modifier = Modifier.fillMaxWidth()) {
            Text(text = "VALIDER LE QCM")
        }
    }
}

@Composable
internal fun MatchingEventContent(
    uiState: ReviewUiState,
    onWordSelected: (String) -> Unit,
    onDefinitionSelected: (String) -> Unit,
    onSubmit: () -> Unit,
    onContinue: () -> Unit
) {
    EventSurface {
        if (uiState.eventResultMessage != null || uiState.eventResultSuccessful != null) {
            EventResultCard(uiState, onContinue)
            return@EventSurface
        }

        Text(uiState.eventInstruction, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.eventCards.forEach { card ->
                    val selected = uiState.matchingSelectedWordId == card.id
                    val assignment = uiState.matchingAssignments[card.id]
                    Button(
                        onClick = { onWordSelected(card.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                assignment != null -> MaterialTheme.colorScheme.tertiaryContainer
                                selected -> MaterialTheme.colorScheme.primary
                                else -> lexicaPanelContainerColor()
                            }
                        )
                    ) {
                        Text(text = decodeReviewText(card.recto), textAlign = TextAlign.Center)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.eventOptions.forEach { definition ->
                    val selected = uiState.matchingSelectedDefinition == definition
                    val assigned = uiState.matchingAssignments.values.contains(definition)
                    Button(
                        onClick = { onDefinitionSelected(definition) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                assigned -> MaterialTheme.colorScheme.tertiaryContainer
                                selected -> MaterialTheme.colorScheme.primary
                                else -> lexicaPanelContainerColor()
                            }
                        )
                    ) {
                        Text(text = decodeReviewText(definition), textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onSubmit,
            enabled = uiState.matchingAssignments.size == uiState.eventCards.size && uiState.eventCards.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "VALIDER LES ASSOCIATIONS")
        }
    }
}

