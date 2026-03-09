package com.example.lexicaandroid2.features.gamification.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran de démonstration de la gamification
 */
class GamificationViewModel(
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {
    
    /**
     * État des statistiques de l'utilisateur
     */
    val userStats: StateFlow<UserStatsEntity?> = userStatsRepository.getUserStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    /**
     * Ajoute de l'XP à l'utilisateur
     * 
     * @param amount Quantité d'XP à ajouter
     */
    fun addXp(amount: Int) {
        viewModelScope.launch {
            userStatsRepository.addXp(amount)
        }
    }
    
    /**
     * Met à jour la série quotidienne
     */
    fun updateStreak() {
        viewModelScope.launch {
            userStatsRepository.updateStreak()
        }
    }
    
    /**
     * Initialise les stats si elles n'existent pas
     */
    fun initializeStatsIfNeeded() {
        viewModelScope.launch {
            if (userStats.value == null) {
                // Initialize with default values by adding 0 XP
                userStatsRepository.addXp(0)
            }
        }
    }
}
