package com.example.lexicaandroid2.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val fontSize: Float = UserPrefsRepository.DEFAULT_FONT_SIZE,
    val accentColor: AccentColor = AccentColor.VIOLET,
    val cardsPerSession: Int = UserPrefsRepository.DEFAULT_CARDS_PER_SESSION,
    val showDefinitionFirst: Boolean = false,
    val challengesEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = false,
    val reminderTime: String = UserPrefsRepository.DEFAULT_REMINDER_TIME
)

class SettingsViewModel(
    private val userPrefsRepository: UserPrefsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPrefs()
    }

    private fun loadPrefs() {
        _uiState.update {
            SettingsUiState(
                theme = userPrefsRepository.theme,
                fontSize = userPrefsRepository.fontSize,
                accentColor = userPrefsRepository.accentColor,
                cardsPerSession = userPrefsRepository.cardsPerSession,
                showDefinitionFirst = userPrefsRepository.showDefinitionFirst,
                challengesEnabled = userPrefsRepository.challengesEnabled,
                dailyReminderEnabled = userPrefsRepository.dailyReminderEnabled,
                reminderTime = userPrefsRepository.reminderTime
            )
        }
    }

    fun setTheme(theme: AppTheme) {
        userPrefsRepository.theme = theme
        _uiState.update { it.copy(theme = theme) }
    }

    fun setFontSize(size: Float) {
        userPrefsRepository.fontSize = size
        _uiState.update { it.copy(fontSize = size) }
    }

    fun setAccentColor(color: AccentColor) {
        userPrefsRepository.accentColor = color
        _uiState.update { it.copy(accentColor = color) }
    }

    fun setCardsPerSession(count: Int) {
        userPrefsRepository.cardsPerSession = count
        _uiState.update { it.copy(cardsPerSession = userPrefsRepository.cardsPerSession) }
    }

    fun setShowDefinitionFirst(show: Boolean) {
        userPrefsRepository.showDefinitionFirst = show
        _uiState.update { it.copy(showDefinitionFirst = show) }
    }

    fun setChallengesEnabled(enabled: Boolean) {
        userPrefsRepository.challengesEnabled = enabled
        _uiState.update { it.copy(challengesEnabled = enabled) }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        userPrefsRepository.dailyReminderEnabled = enabled
        _uiState.update { it.copy(dailyReminderEnabled = enabled) }
    }

    fun setReminderTime(time: String) {
        userPrefsRepository.reminderTime = time
        _uiState.update { it.copy(reminderTime = time) }
    }
}

class SettingsViewModelFactory(
    private val userPrefsRepository: UserPrefsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(userPrefsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
