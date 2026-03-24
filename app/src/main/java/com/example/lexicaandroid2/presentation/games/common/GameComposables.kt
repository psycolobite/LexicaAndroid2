package com.example.lexicaandroid2.presentation.games.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// GameTopAppBar — Nouvelle barre compacte avec tout dedans
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTopAppBar(
    title: String,
    score: Int,
    current: Int = 0,
    total: Int = 0,
    onBack: () -> Unit
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour"
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (total > 0) {
                    Text(
                        text = "$current/$total • Score: $score",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = "Score: $score",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// GameHeader COMPACT — remplace l'ancienne barre violette volumineuse.
// N'affiche QUE score + progress bar sur une seule ligne, pas de titre
// (la TopAppBar du Scaffold s'en charge déjà).
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GameHeader(
    title: String,          // conservé pour compatibilité mais ignoré ici
    score: Int,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Score : $score",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GameButton — inchangé
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = Color.Gray
        )
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ScoreBoard — inchangé
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ScoreBoard(
    correct: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val percentage = if (total == 0) 0 else (correct * 100) / total
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$correct / $total",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$percentage%",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SelectableButton — REFONTE COMPLÈTE
//
// États visuels :
//   • Normal        → fond blanc, bordure grise légère
//   • Sélectionné   → fond primary-container, bordure primary épaisse
//   • Trouvé (✓)    → fond vert #2E7D32, texte blanc, icône ✓, non-cliquable
//   • Erreur (✗)    → fond rouge #C62828, texte blanc, icône ✗, non-cliquable
//
// Taille : padding vertical 16dp, texte 15sp → bien lisible même en 2 colonnes
// ─────────────────────────────────────────────────────────────────────────────
private val FoundGreen  = Color(0xFF2E7D32)
private val ErrorRed    = Color(0xFFC62828)
private val FoundText   = Color.White
private val NormalBg    = Color.White
private val NormalBorder = Color(0xFFBBBBBB)

@Composable
fun SelectableButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isFound: Boolean = false,       // paire correctement validée
    isWrong: Boolean = false        // tentative incorrecte (flash rouge)
) {
    val bgColor = when {
        isFound -> FoundGreen
        isWrong -> ErrorRed
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> NormalBg
    }
    val borderColor = when {
        isFound -> FoundGreen
        isWrong -> ErrorRed
        isSelected -> MaterialTheme.colorScheme.primary
        else -> NormalBorder
    }
    val borderWidth = if (isSelected && !isFound && !isWrong) 2.dp else 1.dp
    val textColor = when {
        isFound || isWrong -> FoundText
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> Color(0xFF1A1A1A)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled && !isFound) { onClick() }
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            fontWeight = if (isFound || isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            lineHeight = 20.sp
        )
        if (isFound) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Trouvé",
                tint = FoundText,
                modifier = Modifier.size(18.dp)
            )
        } else if (isWrong) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Erreur",
                tint = FoundText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FeedbackBanner — bannière animée "Bravo !" / "Essaie encore"
// Affiché pendant ~1s après validation
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FeedbackBanner(
    show: Boolean,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSuccess) FoundGreen else ErrorRed,
                    RoundedCornerShape(10.dp)
                )
                .padding(vertical = 10.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isSuccess) "Bravo ! Bonne paire ✨" else "Essaie encore…",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
