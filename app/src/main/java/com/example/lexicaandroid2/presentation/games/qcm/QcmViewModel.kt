package com.example.lexicaandroid2.presentation.games.qcm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.domain.XPCalculator
import com.example.lexicaandroid2.presentation.games.common.GameUtils
import com.example.lexicaandroid2.presentation.games.common.Question
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class QcmQuestionState {
    ANSWERING,
    RETRY,
    CORRECT,
    REVEALED
}

data class QcmUiState(
    val flashcards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val currentQuestion: Question = Question(),
    val answers: List<String> = emptyList(),
    val selectedAnswer: String? = null,
    val lastIncorrectAnswer: String? = null,
    val score: Int = 0,
    val totalWords: Int = 0,
    val attemptCount: Int = 0,
    val currentQuestionXp: Int = XPCalculator.XP_WORD_REVIEWED,
    val totalXpEarned: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val gameOver: Boolean = false,
    val questionState: QcmQuestionState = QcmQuestionState.ANSWERING
)

class QcmViewModel(private val repository: FlashcardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(QcmUiState())
    val uiState: StateFlow<QcmUiState> = _uiState.asStateFlow()

    private var answerPool: List<Flashcard> = emptyList()

    fun loadGame() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val allCards = repository.getAllCards()
                    .filter { it.recto.isNotBlank() && it.verso.isNotBlank() }
                    .distinctBy { "${it.recto.trim().lowercase()}::${it.verso.trim().lowercase()}" }

                if (allCards.size < MINIMUM_DISTINCT_ANSWERS) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Il faut au moins 4 cartes distinctes pour jouer au QCM"
                    )}
                    return@launch
                }

                answerPool = allCards
                val flashcards = allCards.shuffled().take(MAX_QUESTIONS)

                _uiState.update {
                    it.copy(
                        flashcards = flashcards,
                        currentIndex = 0,
                        score = 0,
                        totalWords = flashcards.size,
                        attemptCount = 0,
                        currentQuestionXp = XPCalculator.XP_WORD_REVIEWED,
                        totalXpEarned = 0,
                        selectedAnswer = null,
                        lastIncorrectAnswer = null,
                        gameOver = false,
                        questionState = QcmQuestionState.ANSWERING,
                        isLoading = false,
                        error = null
                    )
                }

                buildQuestion(flashcards, 0)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Erreur inconnue"
                )}
            }
        }
    }

    private fun buildQuestion(flashcards: List<Flashcard>, index: Int) {
        if (index >= flashcards.size) {
            _uiState.update {
                it.copy(
                    gameOver = true,
                    isLoading = false
                )
            }
            return
        }

        val current = flashcards[index]
        val question = GameUtils.flashcardToQuestion(current)
        val wrongCards = answerPool.filter { it.id != current.id }
        val answers = GameUtils.shuffleAnswers(current.verso, wrongCards)

        if (answers.size < MINIMUM_DISTINCT_ANSWERS) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Impossible de générer 4 propositions distinctes pour cette question"
                )
            }
            return
        }

        _uiState.update { it.copy(
            flashcards = flashcards,
            currentIndex = index,
            currentQuestion = question,
            answers = answers,
            selectedAnswer = null,
            lastIncorrectAnswer = null,
            isLoading = false,
            attemptCount = 0,
            currentQuestionXp = XPCalculator.XP_WORD_REVIEWED,
            totalWords = flashcards.size,
            questionState = QcmQuestionState.ANSWERING
        )}
    }

    fun selectAnswer(answer: String) {
        _uiState.update { state ->
            if (state.gameOver || state.questionState == QcmQuestionState.CORRECT || state.questionState == QcmQuestionState.REVEALED) {
                return@update state
            }

            state.copy(
                selectedAnswer = answer,
                lastIncorrectAnswer = null,
                questionState = if (state.questionState == QcmQuestionState.RETRY) {
                    QcmQuestionState.ANSWERING
                } else {
                    state.questionState
                }
            )
        }
    }

    fun validateAnswer() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _uiState.value
            if (state.gameOver || state.selectedAnswer == null) return@launch
            if (state.questionState == QcmQuestionState.CORRECT || state.questionState == QcmQuestionState.REVEALED) return@launch

            val isCorrect = state.selectedAnswer == state.currentQuestion.definition

            if (isCorrect) {
                _uiState.update {
                    it.copy(
                        score = it.score + 1,
                        totalXpEarned = it.totalXpEarned + it.currentQuestionXp,
                        questionState = QcmQuestionState.CORRECT,
                        lastIncorrectAnswer = null
                    )
                }
            } else {
                val nextAttemptCount = state.attemptCount + 1
                if (nextAttemptCount >= MAX_ATTEMPTS_PER_QUESTION) {
                    _uiState.update {
                        it.copy(
                            attemptCount = nextAttemptCount,
                            currentQuestionXp = 0,
                            questionState = QcmQuestionState.REVEALED,
                            lastIncorrectAnswer = state.selectedAnswer
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            attemptCount = nextAttemptCount,
                            currentQuestionXp = (it.currentQuestionXp / 2).coerceAtLeast(1),
                            questionState = QcmQuestionState.RETRY,
                            lastIncorrectAnswer = state.selectedAnswer
                        )
                    }
                }
            }
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.questionState != QcmQuestionState.CORRECT && state.questionState != QcmQuestionState.REVEALED) return

        val newIndex = state.currentIndex + 1
        if (newIndex >= state.flashcards.size) {
            _uiState.update {
                it.copy(gameOver = true)
            }
            return
        }

        buildQuestion(state.flashcards, newIndex)
    }

    fun resetGame() {
        viewModelScope.launch {
            _uiState.update { QcmUiState() }
            loadGame()
        }
    }

    companion object {
        private const val MAX_QUESTIONS = 10
        private const val MINIMUM_DISTINCT_ANSWERS = 4
        private const val MAX_ATTEMPTS_PER_QUESTION = 3
    }
}

