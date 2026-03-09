package com.example.lexicaandroid2.presentation.games.qcm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.presentation.games.common.GameUtils
import com.example.lexicaandroid2.presentation.games.common.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QcmUiState(
    val flashcards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val currentQuestion: Question = Question(),
    val answers: List<String> = emptyList(),
    val selectedAnswer: String? = null,
    val score: Int = 0,
    val totalWords: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val gameOver: Boolean = false,
    val answered: Boolean = false
)

class QcmViewModel(private val repository: FlashcardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(QcmUiState())
    val uiState: StateFlow<QcmUiState> = _uiState.asStateFlow()

    fun loadGame() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val flashcards = repository.getAllCards().shuffled().take(10)

                if (flashcards.isEmpty()) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Aucune carte disponible"
                    )}
                    return@launch
                }

                loadQuestion(flashcards)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur inconnue"
                )}
            }
        }
    }

    private suspend fun loadQuestion(flashcards: List<Flashcard>) {
        val state = _uiState.value

        if (state.currentIndex >= flashcards.size) {
            _uiState.update { it.copy(
                gameOver = true,
                isLoading = false
            )}
            return
        }

        val current = flashcards[state.currentIndex]
        val question = GameUtils.flashcardToQuestion(current)
        val wrongCards = flashcards.filter { it.id != current.id }
        val answers = GameUtils.shuffleAnswers(current.verso, wrongCards)

        _uiState.update { it.copy(
            flashcards = flashcards,
            currentQuestion = question,
            answers = answers,
            selectedAnswer = null,
            isLoading = false,
            answered = false,
            totalWords = flashcards.size
        )}
    }

    fun selectAnswer(answer: String) {
        _uiState.update { state ->
            state.copy(selectedAnswer = answer)
        }
    }

    fun validateAndNext() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val isCorrect = state.selectedAnswer == state.currentQuestion.definition
            val newScore = if (isCorrect) state.score + 1 else state.score
            val newIndex = state.currentIndex + 1

            if (newIndex >= state.flashcards.size) {
                _uiState.update { it.copy(
                    score = newScore,
                    gameOver = true,
                    answered = true
                )}
            } else {
                _uiState.update { it.copy(
                    score = newScore,
                    currentIndex = newIndex,
                    answered = true
                )}
                loadQuestion(state.flashcards)
            }
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            _uiState.update { QcmUiState() }
            loadGame()
        }
    }
}

