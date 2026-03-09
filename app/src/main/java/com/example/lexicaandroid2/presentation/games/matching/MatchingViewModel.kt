package com.example.lexicaandroid2.presentation.games.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.presentation.games.common.GameUtils
import com.example.lexicaandroid2.presentation.games.common.MatchingPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchingUiState(
    val pairs: List<MatchingPair> = emptyList(),
    val shuffledDefinitions: List<String> = emptyList(),
    val selectedWord: String? = null,
    val selectedDefinition: String? = null,
    val foundPairs: Set<String> = emptySet(),
    val score: Int = 0,
    val totalPairs: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val gameOver: Boolean = false
)

class MatchingViewModel(private val repository: FlashcardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MatchingUiState())
    val uiState: StateFlow<MatchingUiState> = _uiState.asStateFlow()

    fun loadGame() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val flashcards = repository.getAllCards()
                    .shuffled()
                    .take(8) // Limiter à 8 paires pour pas trop lourd

                if (flashcards.isEmpty()) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Aucune carte disponible"
                    )}
                    return@launch
                }

                val pairs = GameUtils.createMatchingPairs(flashcards)
                val definitions = pairs.map { it.definitionText }.shuffled()

                _uiState.update { it.copy(
                    pairs = pairs,
                    shuffledDefinitions = definitions,
                    totalPairs = pairs.size,
                    isLoading = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur inconnue"
                )}
            }
        }
    }

    fun selectWord(wordId: String) {
        _uiState.update { state ->
            if (state.foundPairs.contains(wordId)) {
                state
            } else {
                state.copy(selectedWord = wordId)
            }
        }
    }

    fun selectDefinition(definitionText: String) {
        _uiState.update { state ->
            val currentWord = state.selectedWord
            if (currentWord != null) {
                val pair = state.pairs.find { it.wordId == currentWord }
                if (pair != null && pair.definitionText == definitionText) {
                    // Paire correcte
                    val newFound = state.foundPairs + currentWord
                    val newScore = state.foundPairs.size + 1
                    state.copy(
                        foundPairs = newFound,
                        score = newScore,
                        selectedWord = null,
                        selectedDefinition = null,
                        gameOver = newFound.size == state.totalPairs
                    )
                } else {
                    // Mauvaise paire, juste reset la sélection
                    state.copy(
                        selectedWord = null,
                        selectedDefinition = definitionText
                    )
                }
            } else {
                state.copy(selectedDefinition = definitionText)
            }
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            _uiState.update { MatchingUiState() }
            loadGame()
        }
    }

    private fun <T> List<T>.shuffle(): List<T> = this.shuffled()
}

