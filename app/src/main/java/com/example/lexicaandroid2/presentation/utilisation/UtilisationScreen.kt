package com.example.lexicaandroid2.presentation.utilisation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lexicaandroid2.presentation.common.lexicaPanelContainerColor

private val PrimaryPurple = Color(0xFF6750A4)
private val PurpleLight = Color(0xFFF3EDF7)
private val AccentGold = Color(0xFFFFB800)

@Composable
fun UtilisationScreen(
    onNavigateToWordList: () -> Unit,
    onNavigateToReview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── En-tête "Bientôt disponible" ─────────────────────────────────────
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = PrimaryPurple.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Bientôt disponible",
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Mise en pratique & Transfert d'apprentissage",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Sur cet onglet, vous aurez différents défis vous poussant à utiliser activement vos mots afin de sortir de la simple mémorisation par cœur des flashcards et de les ancrer durablement dans votre langage quotidien.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }

        // ── Section : Aperçu des futurs modules ──────────────────────────────
        Text(
            text = "Ce qui vous attend prochainement :",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryPurple,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        FutureChallengeCard(
            icon = Icons.Default.AutoAwesome,
            title = "Focus « Mot du jour »",
            description = "Chaque jour, un mot de votre collection est mis à l'honneur avec des pistes de contextualisation pour stimuler son réemploi spontané."
        )

        FutureChallengeCard(
            icon = Icons.Default.Create,
            title = "Défi Rédaction contextuelle",
            description = "Rédigez une phrase ou un court paragraphe incorporant le mot dans une tournure élégante et naturelle."
        )

        FutureChallengeCard(
            icon = Icons.Default.ChatBubbleOutline,
            title = "Défi Conversation réelle",
            description = "Placez délibérément le mot appris lors de vos échanges et discussions orales de la journée pour valider votre maîtrise."
        )

        FutureChallengeCard(
            icon = Icons.Default.Psychology,
            title = "Défi Nuances & Reformulation",
            description = "Remplacez des mots courants ou génériques par vos termes enrichis pour affiner la précision de votre vocabulaire."
        )

        Spacer(modifier = Modifier.height(4.dp))

        // ── Actions d'entraînement en attendant ─────────────────────────────
        Button(
            onClick = onNavigateToReview,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("S'entraîner aux flashcards", fontWeight = FontWeight.Medium)
        }

        OutlinedButton(
            onClick = onNavigateToWordList,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Consulter ma liste de mots", fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FutureChallengeCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PurpleLight,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF616161),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
