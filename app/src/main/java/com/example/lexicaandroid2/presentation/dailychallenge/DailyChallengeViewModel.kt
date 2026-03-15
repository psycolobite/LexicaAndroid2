package com.example.lexicaandroid2.presentation.dailychallenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*

/**
 * États possibles du Daily Challenge
 */
sealed class DailyChallengeState {
    object Loading : DailyChallengeState()
    data class Available(
        val gameType: GameType,
        val bonusXp: Int = 20
    ) : DailyChallengeState()
    data class Completed(
        val gameType: GameType,
        val xpEarned: Int,
        val timeUntilNext: Long // en millisecondes
    ) : DailyChallengeState()
    data class Error(val message: String) : DailyChallengeState()
}

/**
 * Types de jeux disponibles pour le Daily Challenge
 */
enum class GameType(val displayName: String, val icon: String) {
    MATCHING("Jeu de Correspondance", "🎯"),
    QCM("Questions à Choix Multiples", "📝"),
    HANGMAN("Jeu du Pendu", "🎪"),
    SPELLING("Jeu de Dictée", "🗣️");

    companion object {
        /**
         * Détermine le jeu du jour en fonction de la date
         * Rotation de 4 jours pour couvrir tous les jeux
         */
        fun getGameForDate(date: Calendar): GameType {
            val dayOfYear = date.get(Calendar.DAY_OF_YEAR)
            val gameIndex = dayOfYear % 4
            return values()[gameIndex]
        }
    }
}

/**
 * État UI du Daily Challenge
 */
data class DailyChallengeUiState(
    val state: DailyChallengeState = DailyChallengeState.Loading,
    val userStats: UserStatsEntity? = null,
    val currentStreak: Int = 0,
    val timeUntilNextChallenge: Long = 0L, // en millisecondes
    val isCountdownActive: Boolean = false
)

/**
 * ViewModel pour le Daily Challenge
 *
 * Gère la logique de rotation quotidienne des jeux, la détection de complétion,
 * et le bonus XP streak.
 */
class DailyChallengeViewModel(
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyChallengeUiState())
    val uiState: StateFlow<DailyChallengeUiState> = _uiState.asStateFlow()

    // Flag pour savoir si le challenge d'aujourd'hui a été complété
    // Stocké en mémoire pour cette session
    private var todayChallengeCompleted = false
    private var launchedChallengeGame: GameType? = null

    init {
        loadDailyChallengeState()
        startCountdownTimer()
    }

    /**
     * Charge l'état du Daily Challenge
     */
    private fun loadDailyChallengeState() {
        viewModelScope.launch {
            try {
                userStatsRepository.getUserStats().collect { stats ->
                    if (stats != null) {
                        val today = getTodayMidnight()
                        val lastChallengeDate = Calendar.getInstance().apply {
                            timeInMillis = stats.lastLoginDate
                        }
                        val lastChallengeMidnight = getMidnight(lastChallengeDate)

                        // Vérifier si le challenge d'aujourd'hui a déjà été joué
                        val isTodayCompleted = lastChallengeMidnight.timeInMillis == today.timeInMillis
                                && todayChallengeCompleted

                        val currentGameType = GameType.getGameForDate(Calendar.getInstance())

                        val state = if (isTodayCompleted) {
                            DailyChallengeState.Completed(
                                gameType = currentGameType,
                                xpEarned = 20, // Bonus XP
                                timeUntilNext = getTimeUntilMidnight()
                            )
                        } else {
                            DailyChallengeState.Available(
                                gameType = currentGameType,
                                bonusXp = 20
                            )
                        }

                        _uiState.update { currentState ->
                            currentState.copy(
                                state = state,
                                userStats = stats,
                                currentStreak = stats.streak,
                                timeUntilNextChallenge = getTimeUntilMidnight()
                            )
                        }
                    } else {
                        // Initialiser les stats si elles n'existent pas
                        userStatsRepository.addXp(0)
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(state = DailyChallengeState.Error(e.message ?: "Erreur inconnue"))
                }
            }
        }
    }

    /**
     * Marque le challenge du jour comme complété
     * Ajoute le bonus XP et met à jour la streak
     */
    fun completeDailyChallenge(gameScore: Int = 0) {
        viewModelScope.launch {
            try {
                // Ajouter le bonus XP streak
                val bonusXp = 20
                userStatsRepository.addXp(bonusXp + gameScore)

                // Mettre à jour la streak
                userStatsRepository.updateStreak()

                // Marquer comme complété
                todayChallengeCompleted = true

                // Mettre à jour l'état
                val currentGameType = GameType.getGameForDate(Calendar.getInstance())
                _uiState.update { currentState ->
                    currentState.copy(
                        state = DailyChallengeState.Completed(
                            gameType = currentGameType,
                            xpEarned = bonusXp + gameScore,
                            timeUntilNext = getTimeUntilMidnight()
                        ),
                        currentStreak = (currentState.currentStreak + 1)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(state = DailyChallengeState.Error(e.message ?: "Erreur lors de la complétion"))
                }
            }
        }
    }

    /**
     * Marque le jeu du jour comme lancé depuis l'écran Daily Challenge.
     */
    fun markChallengeStarted(gameType: GameType) {
        launchedChallengeGame = gameType
    }

    /**
     * Complète le challenge uniquement si la partie en cours a bien été lancée
     * depuis l'écran Daily Challenge et correspond au jeu attendu du jour.
     */
    fun tryCompleteFromGame(gameType: GameType, gameScore: Int) {
        if (todayChallengeCompleted) return

        val expectedGame = GameType.getGameForDate(Calendar.getInstance())
        if (launchedChallengeGame != gameType || expectedGame != gameType) return

        launchedChallengeGame = null
        completeDailyChallenge(gameScore)
    }

    /**
     * Démarre un timer pour mettre à jour le countdown
     */
    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000) // Mise à jour toutes les secondes
                _uiState.update { currentState ->
                    currentState.copy(
                        timeUntilNextChallenge = getTimeUntilMidnight(),
                        isCountdownActive = true
                    )
                }
            }
        }
    }

    /**
     * Retourne le temps restant jusqu'à minuit en millisecondes
     */
    private fun getTimeUntilMidnight(): Long {
        val now = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return tomorrow.timeInMillis - now.timeInMillis
    }

    /**
     * Retourne la date d'aujourd'hui à minuit
     */
    private fun getTodayMidnight(): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    /**
     * Retourne la date à minuit pour un Calendar donné
     */
    private fun getMidnight(calendar: Calendar): Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = calendar.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}

