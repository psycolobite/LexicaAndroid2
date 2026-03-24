package com.example.lexicaandroid2.presentation.games.chrono

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.domain.XPCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChronoUiState(
    val allCards: List<Flashcard> = emptyList(),
    val currentQuestion: Flashcard? = null,
    val answers: List<String> = emptyList(),
    val selectedAnswer: String? = null,
    val totalAnswered: Int = 0,
    val score: Int = 0,
    val selectedDurationSeconds: Long = 60L,
    val timeRemainingSeconds: Long = 60L,
    val gameStarted: Boolean = false,
    val gameFinished: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val earnedXp: Int = 0,
    val lastAnswerCorrect: Boolean? = null
) {
    val progress: Float
        get() {
            if (!gameStarted || selectedDurationSeconds <= 0L) return 0f
            val elapsed = selectedDurationSeconds - timeRemainingSeconds
            return (elapsed.toFloat() / selectedDurationSeconds.toFloat()).coerceIn(0f, 1f)
        }
}

class ChronoViewModel(private val repository: FlashcardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ChronoUiState())
    val uiState: StateFlow<ChronoUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun loadGameData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val cards = repository.getAllCards()
                    .filter { it.recto.isNotBlank() && it.verso.isNotBlank() }
                    .distinctBy { it.id }
                    .shuffled()
                    .take(100) // Cap à 100 : évite de charger 7000+ cartes en mémoire

                if (cards.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No cards available for chrono mode"
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        allCards = cards,
                        isLoading = false,
                        gameStarted = false,
                        gameFinished = false,
                        selectedDurationSeconds = 60L,
                        timeRemainingSeconds = 60L
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun setDuration(durationSeconds: Long) {
        _uiState.update {
            if (it.gameStarted) it
            else it.copy(
                selectedDurationSeconds = durationSeconds,
                timeRemainingSeconds = durationSeconds
            )
        }
    }

    fun startGame() {
        val state = _uiState.value
        if (state.isLoading || state.allCards.isEmpty()) return

        val duration = state.selectedDurationSeconds
        _uiState.update {
            it.copy(
                gameStarted = true,
                gameFinished = false,
                score = 0,
                totalAnswered = 0,
                earnedXp = 0,
                selectedAnswer = null,
                lastAnswerCorrect = null,
                timeRemainingSeconds = duration,
                error = null
            )
        }

        generateQuestion()
        startTimer(duration)
    }

    fun selectAnswer(answer: String) {
        _uiState.update { state ->
            if (!state.gameStarted || state.gameFinished) state else state.copy(selectedAnswer = answer)
        }
    }

    fun submitAnswer() {
        val state = _uiState.value
        if (!state.gameStarted || state.gameFinished) return

        val current = state.currentQuestion ?: return
        val selected = state.selectedAnswer ?: return

        val correct = selected == current.verso
        val perAnswerXp = if (correct) XPCalculator.XP_WORD_REVIEWED else 0

        _uiState.update {
            it.copy(
                score = if (correct) it.score + 1 else it.score,
                totalAnswered = it.totalAnswered + 1,
                earnedXp = it.earnedXp + perAnswerXp,
                selectedAnswer = null,
                lastAnswerCorrect = correct
            )
        }

        generateQuestion()
    }

    fun restart() {
        stopTimer()
        _uiState.update { ChronoUiState() }
        loadGameData()
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }

    private fun startTimer(durationSeconds: Long) {
        stopTimer()
        timerJob = viewModelScope.launch {
            var remaining = durationSeconds
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
                _uiState.update { it.copy(timeRemainingSeconds = remaining) }
            }
            finishGame()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun finishGame() {
        val state = _uiState.value
        if (state.gameFinished) return

        stopTimer()
        val perfect = state.totalAnswered > 0 && state.score == state.totalAnswered
        val completionBonus = XPCalculator.calculateXpForGame(perfectScore = perfect)

        _uiState.update {
            it.copy(
                gameFinished = true,
                gameStarted = false,
                earnedXp = it.earnedXp + completionBonus
            )
        }
    }

    private fun generateQuestion() {
        val state = _uiState.value
        if (state.allCards.isEmpty()) return

        val question = state.allCards.random()
        val wrongAnswers = state.allCards
            .asSequence()
            .filter { it.id != question.id }
            .map { it.verso }
            .distinct()
            .shuffled()
            .take(3)
            .toList()

        val answers = (listOf(question.verso) + wrongAnswers)
            .distinct()
            .shuffled()

        _uiState.update {
            it.copy(
                currentQuestion = question,
                answers = answers,
                selectedAnswer = null
            )
        }
    }
}
