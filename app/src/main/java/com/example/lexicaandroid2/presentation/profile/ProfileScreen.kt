package com.example.lexicaandroid2.presentation.profile

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.auth.domain.repository.AuthRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import com.example.lexicaandroid2.features.gamification.domain.XPCalculator
import com.example.lexicaandroid2.features.sync.SyncViewModel
import com.example.lexicaandroid2.presentation.common.lexicaPanelContainerColor

// ==================== COLOR PALETTE ====================

private val AccentPurple = Color(0xFF7C4DFF)
private val AccentPurpleLight = Color(0xFFB388FF)
private val AccentTeal = Color(0xFF00BFA5)
private val AccentOrange = Color(0xFFFF6D00)
private val StreakGold = Color(0xFFFFAB00)
private val ChartGreen = Color(0xFF00E676)
private val ChartGreenDark = Color(0xFF1B5E20)
private val DangerRed = Color(0xFFFF5252)

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

    // Dialogs
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

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // ===== HERO HEADER =====
            HeroHeader(
                displayName = uiState.displayName,
                email = uiState.email,
                initials = uiState.initials,
                level = uiState.userStats.level,
                totalXp = uiState.userStats.xp,
                streak = uiState.userStats.streak,
                isDark = isDark
            )

            // ===== CONTENT =====
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // ===== STAT CARDS ROW =====
                QuickStatsRow(
                    totalWords = uiState.totalWordsLearned,
                    todayCards = uiState.todayCards,
                    successRate = uiState.successRate7Days,
                    bestStreak = uiState.bestStreak30Days,
                    isDark = isDark
                )

                // ===== WEEKLY ACTIVITY =====
                if (uiState.last7Days.isNotEmpty()) {
                    WeeklyActivityCard(
                        days = uiState.last7Days,
                        isDark = isDark
                    )
                }

                // ===== GAME STATS =====
                if (uiState.gameStats.any { it.value > 0 }) {
                    GameStatsCard(
                        gameStats = uiState.gameStats,
                        isDark = isDark
                    )
                }

                // ===== ERROR =====
                uiState.error?.let { message ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // ===== ACTIONS =====
                ActionsSection(
                    isAuthenticated = uiState.isAuthenticated,
                    isDeletingAccount = uiState.isDeletingAccount,
                    isResetting = uiState.isResetting,
                    uid = uiState.uid,
                    syncViewModel = syncViewModel,
                    viewModel = viewModel,
                    onSignInRequested = onSignInRequested,
                    onBack = onBack
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ==================== HERO HEADER ====================

@Composable
private fun HeroHeader(
    displayName: String,
    email: String?,
    initials: String,
    level: Int,
    totalXp: Long,
    streak: Int,
    isDark: Boolean
) {
    val gradientColors = if (isDark) {
        listOf(Color(0xFF1A237E), Color(0xFF4A148C), Color(0xFF311B92))
    } else {
        listOf(AccentPurple, Color(0xFF536DFE), AccentPurpleLight)
    }

    val xpProgress = XPCalculator.calculateProgressToNextLevel(level, totalXp)
    val xpToNext = XPCalculator.calculateXpToNextLevel(level, totalXp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(gradientColors))
            .padding(top = 32.dp, bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with XP ring
            Box(contentAlignment = Alignment.Center) {
                AnimatedXpRing(
                    progress = xpProgress,
                    modifier = Modifier.size(96.dp)
                )
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                }
                // Level badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(StreakGold)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$level",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White
            )
            if (!email.isNullOrBlank()) {
                Text(
                    text = email,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // XP info row
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalXp",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Text(
                        text = "XP total",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (streak > 0) "🔥 $streak" else "—",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (streak > 0) StreakGold else Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "jour${if (streak != 1) "s" else ""} de suite",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$xpToNext",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Text(
                        text = "XP → Niv.${level + 1}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ==================== ANIMATED XP RING ====================

@Composable
private fun AnimatedXpRing(progress: Float, modifier: Modifier = Modifier) {
    var animationTriggered by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTriggered) progress else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "xp_ring"
    )

    LaunchedEffect(progress) {
        animationTriggered = true
    }

    Canvas(modifier = modifier) {
        val strokeWidth = 6.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Track
        drawCircle(
            color = Color.White.copy(alpha = 0.15f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        if (animatedProgress > 0f) {
            drawArc(
                color = StreakGold,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

// ==================== QUICK STATS ROW ====================

@Composable
private fun QuickStatsRow(
    totalWords: Int,
    todayCards: Int,
    successRate: Float,
    bestStreak: Int,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MiniStatCard(
            emoji = "📚",
            value = "$totalWords",
            label = "Mots",
            accentColor = AccentPurple,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )
        MiniStatCard(
            emoji = "📝",
            value = "$todayCards",
            label = "Aujourd'hui",
            accentColor = AccentTeal,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )
        MiniStatCard(
            emoji = "🎯",
            value = "${successRate.toInt()}%",
            label = "Taux 7j",
            accentColor = AccentOrange,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )
        MiniStatCard(
            emoji = "🔥",
            value = "${bestStreak}j",
            label = "Record",
            accentColor = StreakGold,
            isDark = isDark,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MiniStatCard(
    emoji: String,
    value: String,
    label: String,
    accentColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = lexicaPanelContainerColor()),
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = accentColor
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// ==================== WEEKLY ACTIVITY CARD ====================

@Composable
private fun WeeklyActivityCard(
    days: List<com.example.lexicaandroid2.features.gamification.data.DailyReviewStat>,
    isDark: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = lexicaPanelContainerColor()),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Activité des 7 derniers jours",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val totalWeek = days.sumOf { it.cardsReviewed }
                Text(
                    text = "$totalWeek révisées",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ImprovedWeekBarChart(days = days, isDark = isDark)
        }
    }
}

@Composable
private fun ImprovedWeekBarChart(
    days: List<com.example.lexicaandroid2.features.gamification.data.DailyReviewStat>,
    isDark: Boolean
) {
    val maxCards = days.maxOfOrNull { it.cardsReviewed }?.coerceAtLeast(1) ?: 1
    val barColor = if (isDark) ChartGreen else Color(0xFF43A047)
    val barColorInactive = if (isDark) Color(0xFF2E7D32).copy(alpha = 0.3f) else Color(0xFFC8E6C9)
    val dayLabels = listOf("L", "M", "M", "J", "V", "S", "D")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEachIndexed { index, stat ->
            val fraction = stat.cardsReviewed.toFloat() / maxCards
            val hasActivity = stat.cardsReviewed > 0

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Count label on top of bar
                if (hasActivity) {
                    Text(
                        text = "${stat.cardsReviewed}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Bar
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .weight(1f)
                ) {
                    val barWidth = size.width
                    val maxBarHeight = size.height
                    val barHeight = if (hasActivity) {
                        (fraction * maxBarHeight).coerceIn(4.dp.toPx(), maxBarHeight)
                    } else {
                        4.dp.toPx()
                    }
                    val cornerRadius = CornerRadius(4.dp.toPx())

                    drawRoundRect(
                        color = if (hasActivity) barColor else barColorInactive,
                        topLeft = Offset(0f, maxBarHeight - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Day label
                val dayLabel = if (index < dayLabels.size) {
                    // Use calendar-based label from dateKey
                    val dateStr = stat.dateKey.takeLast(5).replace("-", "/")
                    dateStr
                } else {
                    ""
                }
                Text(
                    text = dayLabel,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// ==================== GAME STATS ====================

@Composable
private fun GameStatsCard(
    gameStats: Map<String, Int>,
    isDark: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = lexicaPanelContainerColor()),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎮 Mini-jeux",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            val activeGames = gameStats.filter { it.value > 0 }
            val emojiMap = mapOf(
                "Matching" to "🔗",
                "QCM" to "✅",
                "Pendu" to "🪓",
                "Dictée" to "✍️"
            )

            activeGames.entries.forEachIndexed { index, (game, played) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${emojiMap[game] ?: "🎲"} $game",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$played partie${if (played > 1) "s" else ""}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentPurple
                    )
                }
                if (index < activeGames.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

// ==================== ACTIONS SECTION ====================

@Composable
private fun ActionsSection(
    isAuthenticated: Boolean,
    isDeletingAccount: Boolean,
    isResetting: Boolean,
    uid: String?,
    syncViewModel: SyncViewModel?,
    viewModel: ProfileViewModel,
    onSignInRequested: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = lexicaPanelContainerColor()),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "⚙️ Compte",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Auth button
            Button(
                onClick = {
                    if (isAuthenticated && syncViewModel != null && !uid.isNullOrBlank()) {
                        syncViewModel.signOutWithSync(uid)
                    } else {
                        viewModel.onAuthAction(onSignInRequested)
                    }
                },
                enabled = !isDeletingAccount,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAuthenticated) MaterialTheme.colorScheme.surfaceVariant
                    else AccentPurple
                )
            ) {
                Icon(
                    imageVector = if (isAuthenticated) Icons.AutoMirrored.Filled.ExitToApp else Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAuthenticated) "Se déconnecter" else "Se connecter",
                    color = if (isAuthenticated) MaterialTheme.colorScheme.onSurface else Color.White
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                thickness = 0.5.dp
            )

            // Danger zone
            Text(
                text = "Zone sensible",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = DangerRed.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            OutlinedButton(
                onClick = { viewModel.onResetProgressClicked() },
                enabled = !isResetting,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isResetting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = DangerRed
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remettre la progression à zéro")
                }
            }

            if (isAuthenticated) {
                OutlinedButton(
                    onClick = { viewModel.onDeleteAccountClicked() },
                    enabled = !isDeletingAccount,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isDeletingAccount) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = DangerRed
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Supprimer mon compte")
                    }
                }
            }
        }
    }
}

// ==================== DIALOGS ====================

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
