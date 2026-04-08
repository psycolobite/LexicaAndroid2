package com.example.lexicaandroid2.presentation.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val normalPresentationEnabled: Boolean = true,
    val reviewWordToDefinitionEnabled: Boolean = true,
    val reviewDefinitionToWordEnabled: Boolean = true,
    val challengeSemanticEnabled: Boolean = true,
    val challengeUsageEnabled: Boolean = true,
    val challengeOrthoEnabled: Boolean = true,
    val extraSpellingEnabled: Boolean = true,
    val reviewQcmEnabled: Boolean = true,
    val reviewMatchingEnabled: Boolean = true,
    val sessionSize: Int = AdminPrefsRepository.DEFAULT_SESSION_SIZE,
    val qcmQuestionCount: Int = AdminPrefsRepository.DEFAULT_QCM_COUNT,
    val memoryGridSize: MemoryGridSize = MemoryGridSize.SIZE_4X4,
    val currentLevel: Int = 1,
    val pendingLevel: Int = 1,
    val snackbarMessage: String? = null
)

class AdminViewModel(
    private val adminPrefsRepository: AdminPrefsRepository,
    private val userStatsRepository: UserStatsRepository,
    private val dailyReviewStatDao: DailyReviewStatDao? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()
    private var hasPendingReviewSettingsChange = false

    init {
        loadPrefs()
        observeUserStats()
    }

    private fun loadPrefs() {
        _uiState.update {
            it.copy(
                normalPresentationEnabled = adminPrefsRepository.normalPresentationEnabled,
                reviewWordToDefinitionEnabled = adminPrefsRepository.reviewWordToDefinitionEnabled,
                reviewDefinitionToWordEnabled = adminPrefsRepository.reviewDefinitionToWordEnabled,
                challengeOrthoEnabled = adminPrefsRepository.challengeOrthoEnabled,
                challengeSemanticEnabled = adminPrefsRepository.challengeSemanticEnabled,
                challengeUsageEnabled = adminPrefsRepository.challengeUsageEnabled,
                extraSpellingEnabled = adminPrefsRepository.extraSpellingEnabled,
                reviewQcmEnabled = adminPrefsRepository.reviewQcmEnabled,
                reviewMatchingEnabled = adminPrefsRepository.reviewMatchingEnabled,
                sessionSize = adminPrefsRepository.sessionSize,
                qcmQuestionCount = adminPrefsRepository.qcmQuestionCount,
                memoryGridSize = adminPrefsRepository.memoryGridSize
            )
        }
    }

    private fun observeUserStats() {
        viewModelScope.launch {
            userStatsRepository.getUserStats().collectLatest { stats ->
                val level = stats?.level ?: 1
                _uiState.update {
                    it.copy(
                        currentLevel = level,
                        pendingLevel = level
                    )
                }
            }
        }
    }

    fun setReviewWordToDefinitionEnabled(enabled: Boolean) {
        if (_uiState.value.reviewWordToDefinitionEnabled != enabled) {
            hasPendingReviewSettingsChange = true
        }
        adminPrefsRepository.reviewWordToDefinitionEnabled = enabled
        _uiState.update { it.copy(reviewWordToDefinitionEnabled = enabled) }
    }

    fun setReviewDefinitionToWordEnabled(enabled: Boolean) {
        if (_uiState.value.reviewDefinitionToWordEnabled != enabled) {
            hasPendingReviewSettingsChange = true
        }
        adminPrefsRepository.reviewDefinitionToWordEnabled = enabled
        _uiState.update { it.copy(reviewDefinitionToWordEnabled = enabled) }
    }

    fun setChallengeOrtho(enabled: Boolean) {
        if (_uiState.value.challengeOrthoEnabled != enabled) {
            hasPendingReviewSettingsChange = true
        }
        adminPrefsRepository.challengeOrthoEnabled = enabled
        _uiState.update { it.copy(challengeOrthoEnabled = enabled) }
    }

    fun setChallengeSemanticEnabled(enabled: Boolean) {
        if (_uiState.value.challengeSemanticEnabled != enabled) {
            hasPendingReviewSettingsChange = true
        }
        adminPrefsRepository.challengeSemanticEnabled = enabled
        _uiState.update { it.copy(challengeSemanticEnabled = enabled) }
    }

    fun setChallengeUsageEnabled(enabled: Boolean) {
        if (_uiState.value.challengeUsageEnabled != enabled) {
            hasPendingReviewSettingsChange = true
        }
        adminPrefsRepository.challengeUsageEnabled = enabled
        _uiState.update { it.copy(challengeUsageEnabled = enabled) }
    }

    fun setExtraSpellingEnabled(enabled: Boolean) {
        if (_uiState.value.extraSpellingEnabled != enabled) {
            hasPendingReviewSettingsChange = true
        }
        adminPrefsRepository.extraSpellingEnabled = enabled
        _uiState.update { it.copy(extraSpellingEnabled = enabled) }
    }

    fun setReviewQcmEnabled(enabled: Boolean) {
        if (_uiState.value.reviewQcmEnabled != enabled) {
            hasPendingReviewSettingsChange = true
        }
        adminPrefsRepository.reviewQcmEnabled = enabled
        _uiState.update { it.copy(reviewQcmEnabled = enabled) }
    }

    fun setReviewMatchingEnabled(enabled: Boolean) {
        if (_uiState.value.reviewMatchingEnabled != enabled) {
            hasPendingReviewSettingsChange = true
        }
        adminPrefsRepository.reviewMatchingEnabled = enabled
        _uiState.update { it.copy(reviewMatchingEnabled = enabled) }
    }

    fun setNormalPresentationEnabled(enabled: Boolean) {
        if (_uiState.value.normalPresentationEnabled != enabled) {
            hasPendingReviewSettingsChange = true
        }
        adminPrefsRepository.normalPresentationEnabled = enabled
        _uiState.update { it.copy(normalPresentationEnabled = enabled) }
    }

    fun applyNormalPresentationPreset() {
        if (!adminPrefsRepository.normalPresentationEnabled) {
            hasPendingReviewSettingsChange = true
        }
        adminPrefsRepository.normalPresentationEnabled = true
        loadPrefs()
    }

    fun setSessionSize(size: Int) {
        if (_uiState.value.sessionSize != size.coerceIn(2, 50)) {
            hasPendingReviewSettingsChange = true
        }
        adminPrefsRepository.sessionSize = size
        _uiState.update { it.copy(sessionSize = adminPrefsRepository.sessionSize) }
    }

    fun consumePendingReviewSettingsChange(): Boolean {
        val hadPendingChange = hasPendingReviewSettingsChange
        hasPendingReviewSettingsChange = false
        return hadPendingChange
    }

    fun setQcmQuestionCount(count: Int) {
        adminPrefsRepository.qcmQuestionCount = count
        _uiState.update { it.copy(qcmQuestionCount = count) }
    }

    fun setMemoryGridSize(size: MemoryGridSize) {
        adminPrefsRepository.memoryGridSize = size
        _uiState.update { it.copy(memoryGridSize = size) }
    }

    fun setPendingLevel(level: Int) {
        _uiState.update { it.copy(pendingLevel = level.coerceIn(1, 20)) }
    }

    fun applyPendingLevel() {
        viewModelScope.launch {
            try {
                userStatsRepository.setLevel(_uiState.value.pendingLevel)
                _uiState.update {
                    it.copy(snackbarMessage = "Niveau mis à jour : ${it.pendingLevel} ✅")
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Erreur : ${e.message}") }
            }
        }
    }

    fun resetXpAndLevel() {
        viewModelScope.launch {
            try {
                userStatsRepository.resetStats()
                _uiState.update { it.copy(snackbarMessage = "XP et niveau réinitialisés ✅") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Erreur : ${e.message}") }
            }
        }
    }

    fun clearDailyStats() {
        viewModelScope.launch {
            try {
                dailyReviewStatDao?.clearAll()
                _uiState.update { it.copy(snackbarMessage = "Stats quotidiennes effacées ✅") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Erreur : ${e.message}") }
            }
        }
    }

    fun simulateStreak7Days() {
        viewModelScope.launch {
            try {
                userStatsRepository.simulateStreak(7)
                _uiState.update { it.copy(snackbarMessage = "Streak de 7 jours simulé ✅") }
            } catch (e: Exception) {
                _uiState.update { it.copy(snackbarMessage = "Erreur : ${e.message}") }
            }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

class AdminViewModelFactory(
    private val context: Context,
    private val userStatsRepository: UserStatsRepository,
    private val dailyReviewStatDao: DailyReviewStatDao? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            return AdminViewModel(
                adminPrefsRepository = AdminPrefsRepository(context),
                userStatsRepository = userStatsRepository,
                dailyReviewStatDao = dailyReviewStatDao
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

