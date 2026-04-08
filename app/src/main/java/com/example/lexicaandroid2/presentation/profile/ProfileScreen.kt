package com.example.lexicaandroid2.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.auth.domain.repository.AuthRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import com.example.lexicaandroid2.features.gamification.ui.XpProgressBar
import com.example.lexicaandroid2.features.sync.SyncViewModel
import com.example.lexicaandroid2.presentation.common.lexicaPanelContainerColor

@Suppress("UNUSED_PARAMETER")
@Composable
fun ProfileScreen(
    flashcardRepository: FlashcardRepository,
    userStatsRepository: UserStatsRepository,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onSignInRequested: () -> Unit,
    syncViewModel: SyncViewModel? = null,
    dailyReviewStatDao: DailyReviewStatDao? = null,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // Dialogs de confirmation de reset
    if (uiState.resetDialogStep == 1) {
        ResetConfirmDialog1(
            wordsCount = uiState.totalWordsLearned,
            xp = uiState.userStats.xp,
            streak = uiState.userStats.streak,
            onConfirm = { viewModel.onResetStep1Confirmed() },
            onDismiss = { viewModel.onResetDismissed() }
        )
    }
    if (uiState.resetDialogStep == 2) {
        ResetConfirmDialog2(
            onConfirm = { viewModel.onResetConfirmedFinal() },
            onDismiss = { viewModel.onResetDismissed() }
        )
    }
    if (uiState.showDeleteAccountDialog) {
        DeleteAccountDialog(
            isDeleting = uiState.isDeletingAccount,
            onConfirm = { viewModel.onDeleteAccountConfirmed() },
            onDismiss = { viewModel.onDeleteAccountDismissed() }
        )
    }
    // Message de succès après reset
    uiState.resetDoneMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.onResetMessageDismissed() },
            title = { Text("🌱 C'est reparti de zéro !") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.onResetMessageDismissed() }) {
                    Text("OK, courage !")
                }
            }
        )
    }
    uiState.deleteAccountMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.onDeleteAccountMessageDismissed() },
            title = { Text("Compte supprimé") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.onDeleteAccountMessageDismissed() }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ProfileHeader(
                displayName = uiState.displayName,
                email = uiState.email,
                initials = uiState.initials
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = lexicaPanelContainerColor()),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Progression",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    XpProgressBar(userStats = uiState.userStats)
                    Text(
                        text = "Streak: ${uiState.userStats.streak} jours",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = lexicaPanelContainerColor()),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Statistiques",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text("Mots appris: ${uiState.totalWordsLearned}")
                    uiState.gameStats.forEach { (game, played) ->
                        Text("$game: $played parties")
                    }
                }
            }

            if (uiState.todayCards > 0 || uiState.last7Days.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Activité de révision",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatBox(
                                label = "Aujourd'hui",
                                value = "${uiState.todayCards}",
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                label = "Taux 7 jours",
                                value = "${uiState.successRate7Days.toInt()}%",
                                modifier = Modifier.weight(1f)
                            )
                            StatBox(
                                label = "Meilleure série",
                                value = "${uiState.bestStreak30Days}j",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (uiState.last7Days.isNotEmpty()) {
                            Text(
                                text = "7 derniers jours",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            WeekBarChart(days = uiState.last7Days)
                        }
                    }
                }
            }

            uiState.error?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (uiState.isAuthenticated && syncViewModel != null && !uiState.uid.isNullOrBlank()) {
                        syncViewModel.signOutWithSync(uiState.uid!!)
                    } else {
                        viewModel.onAuthAction(onSignInRequested)
                    }
                },
                enabled = !uiState.isDeletingAccount,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isAuthenticated) "Se déconnecter" else "Se connecter")
            }

            if (uiState.isAuthenticated) {
                OutlinedButton(
                    onClick = { viewModel.onDeleteAccountClicked() },
                    enabled = !uiState.isDeletingAccount,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isDeletingAccount) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("Supprimer mon compte")
                    }
                }
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retour")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ===== Bouton de réinitialisation =====
            OutlinedButton(
                onClick = { viewModel.onResetProgressClicked() },
                enabled = !uiState.isResetting,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isResetting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text("🗑️ Remettre la progression à zéro")
                }
            }
        }
    }
}

// ==================== DIALOGS HUMORISTIQUES ====================

@Composable
private fun ResetConfirmDialog1(
    wordsCount: Int,
    xp: Long,
    streak: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🚨 Mais… VRAIMENT ?!",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Vous êtes sur le point de supprimer :",
                    fontWeight = FontWeight.SemiBold
                )
                Text("📚 $wordsCount mot${if (wordsCount > 1) "s" else ""} appris avec tant d'efforts...")
                Text("⭐ $xp XP durement gagnés...")
                Text("🔥 Une série de $streak jour${if (streak > 1) "s" else ""} consécutifs...")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "C'est une décision grave. Voulez-vous vraiment continuer ?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
            ) {
                Text("Oui, il le faut")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Euuuh... non, finalement 😅")
            }
        }
    )
}

@Composable
private fun DeleteAccountDialog(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Supprimer définitivement le compte ?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                "Cette action supprime le compte connecté ainsi que sa progression synchronisée. " +
                    "Elle est irréversible."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isDeleting) {
                Text("Oui, supprimer", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun ResetConfirmDialog2(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFFF3E0),
        title = {
            Text(
                text = "⚠️ Dernière confirmation",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFFB71C1C),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Cette action est irréversible. Toute votre progression sera définitivement supprimée : mots appris, XP, streak et historique de révisions.",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFB71C1C)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Confirmez-vous la suppression ?",
                    fontSize = 13.sp,
                    color = Color(0xFF5D4037)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))
            ) {
                Text("Supprimer définitivement", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = Color(0xFF5D4037))
            }
        }
    )

}

// ==================== COMPOSANTS EXISTANTS ====================

@Composable
private fun ProfileHeader(
    displayName: String,
    email: String?,
    initials: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(0.18f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            if (!email.isNullOrBlank()) {
                Text(
                    text = email,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun WeekBarChart(
    days: List<com.example.lexicaandroid2.features.gamification.data.DailyReviewStat>
) {
    val maxCards = days.maxOfOrNull { it.cardsReviewed }?.coerceAtLeast(1) ?: 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { stat ->
            val fraction = stat.cardsReviewed.toFloat() / maxCards
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction.coerceIn(0.05f, 1f))
                        .background(Color(0xFF22C55E), shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stat.dateKey.takeLast(5).replace("-", "/"),
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
