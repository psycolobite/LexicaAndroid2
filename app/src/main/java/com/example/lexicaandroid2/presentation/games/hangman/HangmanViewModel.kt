package com.example.lexicaandroid2.presentation.games.hangman

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HangmanUiState(
    val currentWord: String = "",
    val guessedLetters: Set<Char> = emptySet(),
    val wrongLetters: Set<Char> = emptySet(),
    val livesRemaining: Int = 6,
    val score: Int = 0,
    val totalWords: Int = 0,
    val currentWordIndex: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val gameOver: Boolean = false,
    val won: Boolean = false
)

class HangmanViewModel(private val repository: FlashcardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HangmanUiState())
    val uiState: StateFlow<HangmanUiState> = _uiState.asStateFlow()

    private var wordsList: List<String> = emptyList()

    fun loadGame() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val flashcards = repository.getAllCards().shuffled().take(5)

                if (flashcards.isEmpty()) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Aucune carte disponible"
                    )}
                    return@launch
                }

                wordsList = flashcards.map { it.recto.uppercase() }
                loadNextWord()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur inconnue"
                )}
            }
        }
    }

    private fun loadNextWord() {
        val state = _uiState.value
        
        if (state.currentWordIndex >= wordsList.size) {
            _uiState.update { it.copy(gameOver = true, isLoading = false) }
            return
        }

        val word = wordsList[state.currentWordIndex]
        _uiState.update { it.copy(
            currentWord = word,
            guessedLetters = emptySet(),
            wrongLetters = emptySet(),
            livesRemaining = 6,
            isLoading = false,
            won = false
        )}
    }

    fun guessLetter(letter: Char) {
        val state = _uiState.value
        
        if (state.guessedLetters.contains(letter) || state.wrongLetters.contains(letter)) {
            return // Lettre déjà essayée
        }

        val word = state.currentWord
        val isCorrect = word.contains(letter)

        if (isCorrect) {
            val newGuessed = state.guessedLetters + letter
            val won = word.all { newGuessed.contains(it) }
            
            _uiState.update { it.copy(
                guessedLetters = newGuessed,
                won = won,
                score = if (won) it.score + 1 else it.score
            )}
            
            if (won) {
                // Auto-load next word after a short delay
                viewModelScope.launch {
                    kotlinx.coroutines.delay(1500)
                    val newIndex = state.currentWordIndex + 1
                    _uiState.update { it.copy(currentWordIndex = newIndex) }
                    loadNextWord()
                }
            }
        } else {
            val newWrong = state.wrongLetters + letter
            val newLives = state.livesRemaining - 1
            val lost = newLives <= 0

            _uiState.update { it.copy(
                wrongLetters = newWrong,
                livesRemaining = newLives,
                gameOver = lost
            )}

            if (lost) {
                _uiState.update { it.copy(gameOver = true) }
            }
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            _uiState.update { HangmanUiState() }
            loadGame()
        }
    }

    fun nextWord() {
        val state = _uiState.value
        val newIndex = state.currentWordIndex + 1
        _uiState.update { it.copy(currentWordIndex = newIndex) }
        loadNextWord()
    }
}

