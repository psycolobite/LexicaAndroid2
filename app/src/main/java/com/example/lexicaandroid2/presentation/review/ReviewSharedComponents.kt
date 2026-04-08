package com.example.lexicaandroid2.presentation.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lexicaandroid2.presentation.common.lexicaPanelContainerColor

@Composable
internal fun ReviewHeader(
    uiState: ReviewUiState,
    showEventTitle: Boolean = true
) {
    val shouldShowHeaderDetails =
        (showEventTitle && uiState.eventTitle.isNotBlank()) ||
            uiState.isSpeaking ||
            !uiState.ttsStatusMessage.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReviewProgressOverview(uiState = uiState)

        if (shouldShowHeaderDetails) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (showEventTitle && uiState.eventTitle.isNotBlank()) {
                        Text(
                            text = uiState.eventTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (uiState.isSpeaking) {
                        Text(
                            text = "🔊 Lecture en cours...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    uiState.ttsStatusMessage?.takeIf { it.isNotBlank() }?.let { statusMessage ->
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.ttsReady) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
internal fun ReviewContextHint(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
internal fun ReviewProgressOverview(uiState: ReviewUiState) {
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.sessionProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 280),
        label = "reviewSessionProgress"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = lexicaPanelContainerColor()
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
internal fun AudioTextLine(
    text: String,
    onSpeak: () -> Unit,
    enabled: Boolean,
    textStyle: TextStyle,
    fontWeight: FontWeight? = null
) {
    val decodedText = decodeReviewText(text)
    val audioInlineContentId = "audio-inline"
    val inlineText = remember(decodedText) {
        buildAnnotatedString {
            append(decodedText)
            append(" ")
            appendInlineContent(audioInlineContentId, "🔊")
        }
    }
    val inlineContent = mapOf(
        audioInlineContentId to InlineTextContent(
            placeholder = Placeholder(
                width = 20.sp,
                height = 20.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
            )
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .semantics { contentDescription = "Lire l'audio" }
                    .clip(CircleShape)
                    .clickable(enabled = enabled, onClick = onSpeak),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = inlineText,
            inlineContent = inlineContent,
            style = textStyle,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun rememberAdaptiveTextStyle(
    text: String,
    availableHeight: Dp,
    preferDisplayStyle: Boolean,
    serif: Boolean
): TextStyle {
    val baseStyle = when {
        text.length > 260 || availableHeight < 300.dp -> MaterialTheme.typography.bodyMedium
        text.length > 160 -> MaterialTheme.typography.bodyLarge
        text.length > 90 -> MaterialTheme.typography.titleMedium
        text.length > 45 -> MaterialTheme.typography.headlineSmall
        preferDisplayStyle -> MaterialTheme.typography.headlineMedium
        else -> MaterialTheme.typography.headlineSmall
    }

    val compactStyle = baseStyle.copy(
        lineHeight = (baseStyle.fontSize.value * 1.15f).sp
    )

    return if (serif) compactStyle.copy(fontFamily = FontFamily.Serif) else compactStyle
}

@Composable
internal fun AudioSettingsButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Box(modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Options audio",
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(18.dp)
                    .offset(x = (-1).dp, y = 1.dp)
            )
            Box(
                modifier = Modifier
                    .size(13.dp)
                    .align(Alignment.TopEnd)
                    .background(color = MaterialTheme.colorScheme.surface, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(11.dp)
                )
            }
        }
    }
}

@Composable
internal fun GradeButton(
    modifier: Modifier,
    text: String,
    fontSize: TextUnit,
    containerColor: Color,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = Color.White),
        onClick = onClick
    ) {
        Text(
            text = text,
            maxLines = 2,
            textAlign = TextAlign.Center,
            fontSize = fontSize,
            lineHeight = (fontSize.value + 1).sp
        )
    }
}

@Composable
internal fun EventSurface(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = lexicaPanelContainerColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content
        )
    }
}

@Composable
internal fun EventResultCard(uiState: ReviewUiState, onContinue: () -> Unit) {
    val success = uiState.eventResultSuccessful == true
    val background = if (success) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val contentColor = if (success) Color(0xFF2E7D32) else Color(0xFFB3261E)

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = background) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = sanitizeAdminTestMessage(uiState.eventResultMessage).orEmpty(),
                color = contentColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            uiState.eventResultCorrectAnswer?.takeIf { it.isNotBlank() }?.let { answer ->
                Text(
                    text = "Réponse attendue : ${decodeReviewText(answer)}",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
                Text(text = "CONTINUER")
            }
        }
    }
}

@Composable
internal fun EmptyReviewState(onReturnToMenu: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Aucune carte à réviser", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onReturnToMenu) { Text(text = "Retour au Menu") }
    }
}

