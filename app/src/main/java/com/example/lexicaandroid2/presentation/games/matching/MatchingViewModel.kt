package com.example.lexicaandroid2.presentation.games.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.presentation.games.common.GameUtils
import com.example.lexicaandroid2.presentation.games.common.MatchingPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MatchingRoundState {
    PLAYING,
    SUCCESS,
    FAILURE
}

data class MatchingUiState(
    val pairs: List<MatchingPair> = emptyList(),
    val shuffledDefinitions: List<String> = emptyList(),
    val selectedWord: String? = null,
    val selectedDefinition: String? = null,
    val assignments: Map<String, String> = emptyMap(),
    val totalPairs: Int = 0,
    val validationMistakeCount: Int = 0,
    val completedRounds: Int = 0,
    val roundXpEarned: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val roundState: MatchingRoundState = MatchingRoundState.PLAYING
)

class MatchingViewModel(private val repository: FlashcardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MatchingUiState())
    val uiState: StateFlow<MatchingUiState> = _uiState.asStateFlow()

    fun loadGame() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    selectedWord = null,
                    selectedDefinition = null,
                    assignments = emptyMap(),
                    validationMistakeCount = 0,
                    roundXpEarned = 0,
                    roundState = MatchingRoundState.PLAYING
                )
            }

            try {
                val flashcards = repository.getAllCards()
                    .filter { it.recto.isNotBlank() && it.verso.isNotBlank() }
                    .shuffled()
                    .take(4)

                if (flashcards.size < 4) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Il faut au moins 4 cartes valides pour jouer à Correspondance"
                        )
                    }
                    return@launch
                }

                val pairs = GameUtils.createMatchingPairs(flashcards)
                _uiState.update {
                    it.copy(
                        pairs = pairs,
                        shuffledDefinitions = pairs.map { pair -> pair.definitionText }.shuffled(),
                        totalPairs = pairs.size,
                        isLoading = false,
                        error = null,
                        selectedWord = null,
                        selectedDefinition = null,
                        assignments = emptyMap(),
                        validationMistakeCount = 0,
                        roundXpEarned = 0,
                        roundState = MatchingRoundState.PLAYING
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur inconnue"
                    )
                }
            }
        }
    }

    fun selectWord(wordId: String) {
        _uiState.update { state ->
            if (state.roundState != MatchingRoundState.PLAYING) return@update state

            state.selectedDefinition?.let { selectedDefinition ->
                assignPair(state, wordId, selectedDefinition)
            } ?: state.copy(
                selectedWord = if (state.selectedWord == wordId) null else wordId,
                selectedDefinition = null
            )
        }
    }

    fun selectDefinition(definitionText: String) {
        _uiState.update { state ->
            if (state.roundState != MatchingRoundState.PLAYING) return@update state

            state.selectedWord?.let { selectedWord ->
                assignPair(state, selectedWord, definitionText)
            } ?: state.copy(
                selectedDefinition = if (state.selectedDefinition == definitionText) null else definitionText,
                selectedWord = null
            )
        }
    }

    fun validateAllPairs() {
        val state = _uiState.value
        if (state.roundState != MatchingRoundState.PLAYING) return
        if (state.assignments.size != state.totalPairs) return

        val mistakes = state.pairs.count { pair ->
            state.assignments[pair.wordId] != pair.definitionText
        }

        if (mistakes == 0) {
            _uiState.update {
                it.copy(
                    roundState = MatchingRoundState.SUCCESS,
                    validationMistakeCount = 0,
                    roundXpEarned = SUCCESS_XP,
                    completedRounds = it.completedRounds + 1
                )
            }

            viewModelScope.launch {
                delay(1400)
                loadGame()
            }
        } else {
            _uiState.update {
                it.copy(
                    roundState = MatchingRoundState.FAILURE,
                    validationMistakeCount = mistakes,
                    roundXpEarned = 0
                )
            }
        }
    }

    fun restartCurrentRound() {
        _uiState.update {
            it.copy(
                selectedWord = null,
                selectedDefinition = null,
                assignments = emptyMap(),
                validationMistakeCount = 0,
                roundXpEarned = 0,
                roundState = MatchingRoundState.PLAYING
            )
        }
    }

    private fun assignPair(
        state: MatchingUiState,
        wordId: String,
        definitionText: String
    ): MatchingUiState {
        val cleanedAssignments = state.assignments
            .filterKeys { it != wordId }
            .filterValues { it != definitionText }

        return state.copy(
            assignments = cleanedAssignments + (wordId to definitionText),
            selectedWord = null,
            selectedDefinition = null
        )
    }

    companion object {
        private const val SUCCESS_XP = 25
    }
}
