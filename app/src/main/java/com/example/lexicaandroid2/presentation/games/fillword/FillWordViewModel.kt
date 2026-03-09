package com.example.lexicaandroid2.presentation.games.fillword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FillWordQuestion(
    val cardId: String = "",
    val maskedDefinition: String = "",
    val correctWord: String = "",
    val options: List<String> = emptyList()
)

data class FillWordUiState(
    val rounds: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val question: FillWordQuestion = FillWordQuestion(),
    val selectedOption: String? = null,
    val score: Int = 0,
    val totalRounds: Int = 0,
    val isLoading: Boolean = true,
    val gameOver: Boolean = false,
    val answered: Boolean = false,
    val isCorrect: Boolean? = null,
    val error: String? = null
) {
    val progress: Float
        get() = if (totalRounds == 0) 0f else currentIndex.toFloat() / totalRounds.toFloat()
}

class FillWordViewModel(private val repository: FlashcardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FillWordUiState())
    val uiState: StateFlow<FillWordUiState> = _uiState.asStateFlow()

    fun loadGame() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val rounds = repository.getAllCards()
                    .filter { it.recto.isNotBlank() && it.verso.isNotBlank() }
                    .shuffled()
                    .take(10)

                if (rounds.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Aucune carte disponible"
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        rounds = rounds,
                        totalRounds = rounds.size,
                        currentIndex = 0,
                        score = 0,
                        gameOver = false,
                        answered = false,
                        isCorrect = null,
                        isLoading = false
                    )
                }

                buildQuestion()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur lors du chargement"
                    )
                }
            }
        }
    }

    fun selectOption(option: String) {
        _uiState.update { state ->
            if (state.answered || state.gameOver) state else state.copy(selectedOption = option)
        }
    }

    fun validateAnswer() {
        _uiState.update { state ->
            if (state.answered || state.gameOver || state.selectedOption == null) return@update state

            val correct = state.selectedOption == state.question.correctWord
            state.copy(
                answered = true,
                isCorrect = correct,
                score = if (correct) state.score + 1 else state.score
            )
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (!state.answered) return

        val next = state.currentIndex + 1
        if (next >= state.totalRounds) {
            _uiState.update { it.copy(gameOver = true) }
            return
        }

        _uiState.update {
            it.copy(
                currentIndex = next,
                answered = false,
                isCorrect = null,
                selectedOption = null
            )
        }
        buildQuestion()
    }

    fun restart() {
        _uiState.update { FillWordUiState() }
        loadGame()
    }

    private fun buildQuestion() {
        val state = _uiState.value
        val current = state.rounds.getOrNull(state.currentIndex) ?: return

        val maskedDefinition = maskWord(current.verso, current.recto)
        val distractors = state.rounds
            .asSequence()
            .filter { it.id != current.id }
            .map { it.recto }
            .filter { it.isNotBlank() }
            .distinct()
            .shuffled()
            .take(3)
            .toList()

        val options = (listOf(current.recto) + distractors)
            .distinct()
            .shuffled()

        _uiState.update {
            it.copy(
                question = FillWordQuestion(
                    cardId = current.id,
                    maskedDefinition = maskedDefinition,
                    correctWord = current.recto,
                    options = options
                ),
                selectedOption = null,
                answered = false,
                isCorrect = null
            )
        }
    }

    private fun maskWord(definition: String, word: String): String {
        if (definition.isBlank()) return "_____"
        if (word.isBlank()) return definition

        val regex = Regex("\\b${Regex.escape(word)}\\b", RegexOption.IGNORE_CASE)
        return if (regex.containsMatchIn(definition)) {
            regex.replace(definition, "_____")
        } else {
            "$definition (_____)"
        }
    }
}
