package com.example.lexicaandroid2.presentation.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.presentation.games.common.GameButton

@Composable
fun MiniGamesScreen(
    onGameSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mini-Jeux Lexica",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )

        Text(
            text = "Choisissez un jeu pour améliorer votre vocabulaire",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GameButton(
            text = "🎮 Jeu de Correspondance",
            onClick = { onGameSelected("game_matching") },
            modifier = Modifier.padding(top = 16.dp)
        )

        GameButton(
            text = "❓ QCM (Questions Choix Multiples)",
            onClick = { onGameSelected("game_qcm") }
        )

        GameButton(
            text = "🎯 Jeu du Pendu",
            onClick = { onGameSelected("game_hangman") }
        )

        GameButton(
            text = "✍️ Jeu de Dictée",
            onClick = { onGameSelected("game_spelling") }
        )

        Text(
            text = "Itération 2 à venir: Anagrammes, Chrono, Memory",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 16.dp)
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

        GameButton(
            text = "Retour",
            onClick = onBack,
            modifier = Modifier
                .padding(top = 32.dp)
        )
    }
}

