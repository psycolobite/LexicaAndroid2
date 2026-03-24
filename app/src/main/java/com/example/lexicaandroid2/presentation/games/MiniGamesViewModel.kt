package com.example.lexicaandroid2.presentation.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.features.gamification.domain.GameUnlockConfig
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class GameWithLockState(
    val entry: GameUnlockConfig.GameEntry,
    val isUnlocked: Boolean
)

class MiniGamesViewModel(
    userStatsRepository: UserStatsRepository
) : ViewModel() {

    val gamesWithLockState: StateFlow<List<GameWithLockState>> =
        userStatsRepository.getUserStats()
            .map { stats ->
                val currentXp = stats?.xp ?: 0L
                GameUnlockConfig.ALL_GAMES.map { game ->
                    GameWithLockState(
                        entry = game,
                        isUnlocked = currentXp >= game.requiredXp
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = GameUnlockConfig.ALL_GAMES.map { game ->
                    GameWithLockState(game, isUnlocked = game.requiredXp == 0)
                }
            )
}

class MiniGamesViewModelFactory(
    private val userStatsRepository: UserStatsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MiniGamesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MiniGamesViewModel(userStatsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
