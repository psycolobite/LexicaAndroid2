package com.example.lexicaandroid2.presentation.review

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
internal fun SessionCelebrationView(
    studiedCount: Int,
    xpGained: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 360.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF1F8F4C))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        CelebrationConfettiOverlay()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "Félicitations !",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Session terminée",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.92f)
            )
            Text(
                text = "$studiedCount question(s) validée(s)",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center
            )
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color.White.copy(alpha = 0.18f)
            ) {
                Text(
                    text = "+$xpGained XP",
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
internal fun CelebrationConfettiOverlay() {
    val pieces = remember {
        listOf(
            ConfettiPiece(0.08f, 0.10f, 12.dp, Color(0xFFFFD54F), 12f),
            ConfettiPiece(0.18f, 0.24f, 10.dp, Color(0xFFFF8A65), -18f),
            ConfettiPiece(0.30f, 0.06f, 9.dp, Color(0xFF4DD0E1), 24f),
            ConfettiPiece(0.44f, 0.18f, 8.dp, Color(0xFF81C784), -12f),
            ConfettiPiece(0.58f, 0.09f, 12.dp, Color(0xFFBA68C8), 18f),
            ConfettiPiece(0.72f, 0.20f, 10.dp, Color(0xFFFFD54F), -26f),
            ConfettiPiece(0.85f, 0.11f, 9.dp, Color(0xFF4FC3F7), 14f),
            ConfettiPiece(0.12f, 0.62f, 11.dp, Color(0xFFCE93D8), 22f),
            ConfettiPiece(0.26f, 0.76f, 10.dp, Color(0xFFFFAB91), -20f),
            ConfettiPiece(0.73f, 0.68f, 11.dp, Color(0xFFA5D6A7), 16f),
            ConfettiPiece(0.88f, 0.58f, 8.dp, Color(0xFFFFF176), -10f)
        )
    }
    val transition = rememberInfiniteTransition(label = "celebrationConfetti")
    val fallProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiFall"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        pieces.forEachIndexed { index, piece ->
            val x = size.width * piece.xFraction
            val yBase = size.height * piece.yFraction
            val drift = if (index % 2 == 0) 1f else -1f
            val y = (yBase + size.height * 0.32f * fallProgress + index.toFloat() * 12f) % size.height
            rotate(piece.rotation + (fallProgress * 40f * drift), pivot = androidx.compose.ui.geometry.Offset(x, y)) {
                drawRoundRect(
                    color = piece.color,
                    topLeft = androidx.compose.ui.geometry.Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(piece.size.toPx(), piece.size.toPx() * 0.55f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }
        }
    }
}

