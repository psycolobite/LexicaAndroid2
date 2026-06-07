package com.example.lexicaandroid2.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val newCount: Int = 0,
    val learningCount: Int = 0,
    val knownCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
    val hasLoadedOnce: Boolean = false
)

class DashboardViewModel(
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val stats = repository.getStatsByState()
                val newC = stats["TO_WORK"] ?: stats["TO_LEARN"] ?: 0
                val learningC = stats["IN_PROGRESS"] ?: stats["LEARNING"] ?: 0
                val knownC = stats["KNOWN"] ?: 0

                val total = newC + learningC + knownC

                _uiState.update {
                    it.copy(
                        newCount = newC,
                        learningCount = learningC,
                        knownCount = knownC,
                        totalCount = total,
                        isLoading = false,
                        hasLoadedOnce = true
                    )
                }
            } catch (e: Exception) {
                // In real app, handle error
                _uiState.update { it.copy(isLoading = false, hasLoadedOnce = true) }
            }
        }
    }
}

class DashboardViewModelFactory(
    private val repository: FlashcardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

