package com.example.lexicaandroid2.presentation.review.challenge

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lexicaandroid2.presentation.common.lexicaPanelContainerColor

enum class ChallengeType {
    SPELLING,
    SEMANTIC
}

@Composable
fun ChallengeOverlay(
    challengeType: ChallengeType,
    cardRecto: String,
    cardVerso: String,
    challengeInput: String,
    onInputChange: (String) -> Unit,
    onValidate: () -> Unit,
    onAbandon: () -> Unit,
    isLoading: Boolean = false
) {
    val (title, icon, contextText, placeholder) = when (challengeType) {
        ChallengeType.SPELLING -> {
            Quadruple(
                "🏆 Défi orthographique !",
                "Écris ce mot sans regarder :",
                cardVerso,  // définition affichée
                "Tape le mot..."
            )
        }
        ChallengeType.SEMANTIC -> {
            Quadruple(
                "🎯 Défi sémantique !",
                "Décris le sens de ce mot :",
                cardRecto,  // mot affiché
                "Tape la définition..."
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF8E1), shape = RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Titre du défi
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF57F17),
            textAlign = TextAlign.Center
        )

        // Instruction
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        // Contexte (définition ou mot)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Text(
                text = contextText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }

        // Zone de saisie
        if (challengeType == ChallengeType.SPELLING) {
            OutlinedTextField(
                value = challengeInput,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onValidate() }),
                enabled = !isLoading
            )
        } else {
            OutlinedTextField(
                value = challengeInput,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text(placeholder) },
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onValidate() }),
                enabled = !isLoading
            )
        }

        // Boutons action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAbandon,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = lexicaPanelContainerColor()
                ),
                enabled = !isLoading
            ) {
                Text("Abandonner")
            }

            Button(
                onClick = onValidate,
                modifier = Modifier.weight(1f),
                enabled = challengeInput.isNotBlank() && !isLoading
            ) {
                Text("Valider")
            }
        }
    }
}

@Composable
fun ChallengeResultOverlay(
    result: ValidationResult,
    onContinue: () -> Unit,
    correctAnswer: String = ""
) {
    val backgroundColor = if (result.isValid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val iconColor = if (result.isValid) Color(0xFF4CAF50) else Color(0xFFF44336)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, shape = RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Message feedback
        Text(
            text = result.feedbackMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = iconColor,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        // XP bonus
        if (result.xpBonus > 0) {
            Text(
                text = "+${result.xpBonus} XP",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFFFB800),
                fontWeight = FontWeight.Bold
            )
        }

        // Réponse correcte si échec
        if (!result.isValid && correctAnswer.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Bonne réponse :",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = correctAnswer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Bouton continuer
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continuer")
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
