package com.example.lexicaandroid2.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.domain.usecase.ResetProgressUseCase
import com.example.lexicaandroid2.features.auth.domain.model.AuthUser
import com.example.lexicaandroid2.features.auth.domain.repository.AuthRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStat
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import com.example.lexicaandroid2.features.sync.SyncManager
import com.example.lexicaandroid2.presentation.admin.AdminConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val uid: String? = null,
    val displayName: String = "Invité",
    val email: String? = null,
    val initials: String = "I",
    val isAuthenticated: Boolean = false,
    val isAdmin: Boolean = false,
    val userStats: UserStatsEntity = UserStatsEntity(),
    val totalWordsLearned: Int = 0,
    val gameStats: Map<String, Int> = mapOf(
        "Matching" to 0,
        "QCM" to 0,
        "Pendu" to 0,
        "Dictée" to 0
    ),
    // Statistiques Anki-like
    val todayCards: Int = 0,
    val todayCorrect: Int = 0,
    val last7Days: List<DailyReviewStat> = emptyList(),
    val successRate7Days: Float = 0f,   // Pourcentage (0..100)
    val bestStreak30Days: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Reset progression
    val resetDialogStep: Int = 0,        // 0=caché, 1=1er dialog, 2=2ème dialog
    val isResetting: Boolean = false,
    val resetDoneMessage: String? = null,
    // Suppression de compte
    val showDeleteAccountDialog: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val deleteAccountMessage: String? = null
)

class ProfileViewModel(
    private val userStatsRepository: UserStatsRepository,
    private val authRepository: AuthRepository,
    private val flashcardRepository: FlashcardRepository,
    private val dailyReviewStatDao: DailyReviewStatDao? = null,
    private val resetProgressUseCase: ResetProgressUseCase? = null,
    private val syncManager: SyncManager? = null       // pour invalider le cloud après reset
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeAuthAndStats()
        loadWordStats()
        observeDailyStats()
    }

    fun onAuthAction(onSignInRequested: () -> Unit) {
        viewModelScope.launch {
            if (_uiState.value.isAuthenticated) {
                runCatching { authRepository.signOut() }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(error = e.message ?: "Erreur de déconnexion")
                        }
                    }
            } else {
                onSignInRequested()
            }
        }
    }

    fun onDeleteAccountClicked() {
        if (!_uiState.value.isAuthenticated) return
        _uiState.update { it.copy(showDeleteAccountDialog = true, error = null, deleteAccountMessage = null) }
    }

    fun onDeleteAccountDismissed() {
        _uiState.update { it.copy(showDeleteAccountDialog = false) }
    }

    fun onDeleteAccountMessageDismissed() {
        _uiState.update { it.copy(deleteAccountMessage = null) }
    }

    fun onDeleteAccountConfirmed() {
        val uid = _uiState.value.uid
        if (uid.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    showDeleteAccountDialog = false,
                    error = "Aucun compte connecté à supprimer"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showDeleteAccountDialog = false,
                    isDeletingAccount = true,
                    error = null,
                    deleteAccountMessage = null
                )
            }

            runCatching {
                authRepository.deleteAccount().getOrThrow()
                syncManager?.deleteCloudAccountData(uid)
                resetProgressUseCase?.invoke()
                runCatching { authRepository.signOut() }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        totalWordsLearned = 0,
                        deleteAccountMessage = "✅ Ton compte et les données synchronisées associées ont été supprimés.",
                        error = null
                    )
                }
            }.onFailure { error ->
                val message = when {
                    error.message?.contains("recent login", ignoreCase = true) == true ->
                        "Pour supprimer ton compte, reconnecte-toi puis réessaie."
                    else -> error.message ?: "Erreur lors de la suppression du compte"
                }
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        error = message
                    )
                }
            }
        }
    }

    // ==================== RESET PROGRESSION ====================

    /** Étape 1 : l'utilisateur clique sur le bouton — ouvre le 1er dialog. */
    fun onResetProgressClicked() {
        _uiState.update { it.copy(resetDialogStep = 1) }
    }

    /** Étape 1 confirmée — ouvre le 2ème dialog (vraiment la dernière chance !). */
    fun onResetStep1Confirmed() {
        _uiState.update { it.copy(resetDialogStep = 2) }
    }

    /** L'utilisateur abandonne à n'importe quelle étape — ferme tout. */
    fun onResetDismissed() {
        _uiState.update { it.copy(resetDialogStep = 0) }
    }

    /** Étape 2 confirmée — on efface vraiment tout. Adieu les données 👋 */
    fun onResetConfirmedFinal() {
        val useCase = resetProgressUseCase ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isResetting = true, resetDialogStep = 0) }
            runCatching { useCase() }
                .onSuccess {
                    // Si l'utilisateur est connecté, on uploade la progression vide vers Firestore
                    // pour éviter qu'un re-login réimporte les anciennes données
                    val uid = _uiState.value.uid
                    if (uid != null && syncManager != null) {
                        runCatching { syncManager.uploadLocalToCloud(uid) }
                    }
                    _uiState.update {
                        it.copy(
                            isResetting = false,
                            totalWordsLearned = 0,
                            resetDoneMessage = "✅ Tabula rasa ! Tout est effacé. Bonne chance pour la suite 🌱"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isResetting = false,
                            error = e.message ?: "Erreur lors de la réinitialisation"
                        )
                    }
                }
        }
    }

    /** Dismiss le message de confirmation de reset. */
    fun onResetMessageDismissed() {
        _uiState.update { it.copy(resetDoneMessage = null) }
    }

    // ===========================================================

    private fun observeAuthAndStats() {
        viewModelScope.launch {
            try {
                combine(
                    authRepository.currentUser,
                    userStatsRepository.getUserStats()
                ) { user, stats -> user to stats }
                    .collect { (user, stats) ->
                        _uiState.update {
                            it.copy(
                                uid = user?.uid,
                                displayName = resolveDisplayName(user),
                                email = user?.email,
                                initials = resolveInitials(user),
                                isAuthenticated = user != null,
                                isAdmin = AdminConfig.isAdmin(user?.email),
                                userStats = stats ?: UserStatsEntity(),
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erreur de chargement") }
            }
        }
    }

    private fun loadWordStats() {
        viewModelScope.launch {
            runCatching {
                flashcardRepository.getAllCards().size
            }.onSuccess { total ->
                _uiState.update { it.copy(totalWordsLearned = total) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(error = e.message ?: "Impossible de charger les statistiques")
                }
            }
        }
    }

    /** Observe les statistiques daily depuis DailyReviewStatDao (si disponible). */
    private fun observeDailyStats() {
        val dao = dailyReviewStatDao ?: return
        viewModelScope.launch {
            try {
                dao.getLast30Days().collect { last30 ->
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    val todayStat = last30.firstOrNull { it.dateKey == today }

                    // 7 derniers jours : combler les jours sans données
                    val last7 = buildLast7Days(last30, today)

                    // Taux de réussite sur 7 jours
                    val totalReviewed7 = last7.sumOf { it.cardsReviewed }
                    val totalCorrect7 = last7.sumOf { it.correctAnswers }
                    val rate = if (totalReviewed7 > 0)
                        (totalCorrect7.toFloat() / totalReviewed7.toFloat()) * 100f
                    else 0f

                    // Meilleure série sur 30 jours : nb de jours consécutifs avec au moins 1 révision
                    val best = computeBestStreak(last30)

                    _uiState.update {
                        it.copy(
                            todayCards = todayStat?.cardsReviewed ?: 0,
                            todayCorrect = todayStat?.correctAnswers ?: 0,
                            last7Days = last7,
                            successRate7Days = rate,
                            bestStreak30Days = best
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Stats non critiques — ne pas bloquer l'UI
            }
        }
    }

    /**
     * Construit une liste de 7 DailyReviewStat pour les 7 derniers jours.
     * Les jours sans données sont remplis avec cardsReviewed = 0.
     */
    private fun buildLast7Days(last30: List<DailyReviewStat>, today: String): List<DailyReviewStat> {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        cal.time = sdf.parse(today) ?: java.util.Date()
        val map = last30.associateBy { it.dateKey }
        return (6 downTo 0).map { offset ->
            val c = java.util.Calendar.getInstance()
            c.time = cal.time
            c.add(java.util.Calendar.DAY_OF_YEAR, -offset)
            val key = sdf.format(c.time)
            map[key] ?: DailyReviewStat(dateKey = key)
        }
    }

    /**
     * Calcule la meilleure série de jours consécutifs avec au moins 1 révision
     * parmi les 30 derniers jours.
     */
    private fun computeBestStreak(last30: List<DailyReviewStat>): Int {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val sortedDates = last30
            .filter { it.cardsReviewed > 0 }
            .mapNotNull { runCatching { sdf.parse(it.dateKey) }.getOrNull() }
            .sorted()

        if (sortedDates.isEmpty()) return 0

        var best = 1
        var current = 1
        for (i in 1 until sortedDates.size) {
            val diffMs = sortedDates[i].time - sortedDates[i - 1].time
            val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()
            current = if (diffDays == 1) current + 1 else 1
            if (current > best) best = current
        }
        return best
    }

    private fun resolveDisplayName(user: AuthUser?): String {
        return when {
            user == null -> "Invité"
            !user.displayName.isNullOrBlank() -> user.displayName
            !user.email.isNullOrBlank() -> user.email.substringBefore('@')
            else -> "Utilisateur"
        }
    }

    private fun resolveInitials(user: AuthUser?): String {
        val base = resolveDisplayName(user)
        return base
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString(separator = "") { it.first().uppercase() }
            .ifBlank { "I" }
    }
}

class ProfileViewModelFactory(
    private val userStatsRepository: UserStatsRepository,
    private val authRepository: AuthRepository,
    private val flashcardRepository: FlashcardRepository,
    private val dailyReviewStatDao: DailyReviewStatDao? = null,
    private val resetProgressUseCase: ResetProgressUseCase? = null,
    private val syncManager: SyncManager? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                userStatsRepository = userStatsRepository,
                authRepository = authRepository,
                flashcardRepository = flashcardRepository,
                dailyReviewStatDao = dailyReviewStatDao,
                resetProgressUseCase = resetProgressUseCase,
                syncManager = syncManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
