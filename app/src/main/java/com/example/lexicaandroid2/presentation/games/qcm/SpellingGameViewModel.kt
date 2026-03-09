package com.example.lexicaandroid2.presentation.games.qcm

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
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
import java.util.Locale

data class SpellingGameUiState(
    val flashcards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val currentQuestion: Question = Question(),
    val userInput: String = "",
    val score: Int = 0,
    val totalWords: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val gameOver: Boolean = false,
    val answered: Boolean = false,
    val isCorrect: Boolean? = null,
    val isSpeaking: Boolean = false,
    val ttsReady: Boolean = false
)

class SpellingGameViewModel(
    private val application: Application,
    private val repository: FlashcardRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SpellingGameUiState())
    val uiState: StateFlow<SpellingGameUiState> = _uiState.asStateFlow()

    private var textToSpeech: TextToSpeech? = null

    init {
        initializeTextToSpeech()
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(application) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.FRENCH
                _uiState.update { it.copy(ttsReady = true) }
            } else {
                _uiState.update { it.copy(
                    error = "Erreur d'initialisation du TTS",
                    ttsReady = false
                )}
            }
        }
    }

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

        _uiState.update { it.copy(
            flashcards = flashcards,
            currentQuestion = question,
            userInput = "",
            isLoading = false,
            answered = false,
            isCorrect = null,
            totalWords = flashcards.size
        )}

        // Automatically speak the word when a new question loads
        speakWord(question.mot)
    }

    fun speakWord(word: String) {
        textToSpeech?.let { tts ->
            if (tts.isSpeaking) {
                tts.stop()
            }
            _uiState.update { it.copy(isSpeaking = true) }
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null)

            // Simulate speaking finished after 2 seconds
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                _uiState.update { it.copy(isSpeaking = false) }
            }
        }
    }

    fun updateUserInput(input: String) {
        _uiState.update { it.copy(userInput = input) }
    }

    fun validateAnswer() {
        val state = _uiState.value
        val userAnswer = state.userInput.trim()
        val correctAnswer = state.currentQuestion.mot.trim()

        // Case-insensitive comparison
        val isCorrect = userAnswer.equals(correctAnswer, ignoreCase = true)
        val newScore = if (isCorrect) state.score + 1 else state.score

        _uiState.update { it.copy(
            answered = true,
            isCorrect = isCorrect,
            score = newScore
        )}
    }

    fun nextQuestion() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            val newIndex = state.currentIndex + 1

            if (newIndex >= state.flashcards.size) {
                _uiState.update { it.copy(gameOver = true) }
            } else {
                _uiState.update { it.copy(currentIndex = newIndex) }
                loadQuestion(state.flashcards)
            }
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            _uiState.update { SpellingGameUiState(ttsReady = _uiState.value.ttsReady) }
            loadGame()
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.shutdown()
    }
}
