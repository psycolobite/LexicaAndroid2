package com.example.lexicaandroid2.presentation.review

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

        // Instruction gérée en dehors via ReviewContextHint dans ReviewScreen

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

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
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

