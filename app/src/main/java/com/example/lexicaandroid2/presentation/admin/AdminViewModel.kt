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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val reviewMode: ReviewMode = ReviewMode.BOTH,
    val challengeOrthoEnabled: Boolean = true,
    val challengeSemanticEnabled: Boolean = true,
    val sessionSize: Int = AdminPrefsRepository.DEFAULT_SESSION_SIZE,
    val qcmQuestionCount: Int = AdminPrefsRepository.DEFAULT_QCM_COUNT,
    val memoryGridSize: MemoryGridSize = MemoryGridSize.SIZE_4X4,
    val snackbarMessage: String? = null
)

class AdminViewModel(
    private val adminPrefsRepository: AdminPrefsRepository,
    private val userStatsRepository: UserStatsRepository,
    private val dailyReviewStatDao: DailyReviewStatDao? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadPrefs()
    }

    private fun loadPrefs() {
        _uiState.update {
            it.copy(
                reviewMode = adminPrefsRepository.reviewMode,
                challengeOrthoEnabled = adminPrefsRepository.challengeOrthoEnabled,
                challengeSemanticEnabled = adminPrefsRepository.challengeSemanticEnabled,
                sessionSize = adminPrefsRepository.sessionSize,
                qcmQuestionCount = adminPrefsRepository.qcmQuestionCount,
                memoryGridSize = adminPrefsRepository.memoryGridSize
            )
        }
    }

    fun setReviewMode(mode: ReviewMode) {
        adminPrefsRepository.reviewMode = mode
        _uiState.update { it.copy(reviewMode = mode) }
    }

    fun setChallengeOrtho(enabled: Boolean) {
        adminPrefsRepository.challengeOrthoEnabled = enabled
        _uiState.update { it.copy(challengeOrthoEnabled = enabled) }
    }

    fun setChallengeSemanticEnabled(enabled: Boolean) {
        adminPrefsRepository.challengeSemanticEnabled = enabled
        _uiState.update { it.copy(challengeSemanticEnabled = enabled) }
    }

    fun setSessionSize(size: Int) {
        adminPrefsRepository.sessionSize = size
        _uiState.update { it.copy(sessionSize = size) }
    }

    fun setQcmQuestionCount(count: Int) {
        adminPrefsRepository.qcmQuestionCount = count
        _uiState.update { it.copy(qcmQuestionCount = count) }
    }

    fun setMemoryGridSize(size: MemoryGridSize) {
        adminPrefsRepository.memoryGridSize = size
        _uiState.update { it.copy(memoryGridSize = size) }
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

