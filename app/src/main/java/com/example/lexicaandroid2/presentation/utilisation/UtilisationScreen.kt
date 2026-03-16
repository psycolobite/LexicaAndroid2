package com.example.lexicaandroid2.presentation.utilisation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun UtilisationScreen(
    onNavigateToWordList: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Apprendre à utiliser les mots",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Cette page sert à transformer les mots appris en usage réel : reformulation, production de phrases et réemploi actif.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        UsageCard(
            title = "1. Reformuler",
            body = "Prends un mot appris et essaie de l'expliquer avec tes propres mots sans relire sa définition."
        )
        UsageCard(
            title = "2. Produire une phrase",
            body = "Invente une phrase courte, crédible et naturelle avec le mot. Si possible, change ensuite le registre ou le contexte."
        )
        UsageCard(
            title = "3. Réutiliser plus tard",
            body = "Reviens sur les mots récemment appris et vérifie si tu peux encore les employer sans aide."
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onNavigateToWordList,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voir mes mots")
        }

        Button(
            onClick = onNavigateToReview,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Commencer l'entraînement")
        }
    }
}

@Composable
private fun UsageCard(
    title: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
