package com.example.lexicaandroid2.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.logic.Sm2Algorithm
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.Sm2Stats
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class ReviewViewModel(
    private val repository: FlashcardRepository,
    private val sm2Algorithm: Sm2Algorithm = Sm2Algorithm
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _snackbarEvents = Channel<String>(Channel.CONFLATED)
    val snackbarEvents = _snackbarEvents.receiveAsFlow()

    private var pending: ArrayDeque<Flashcard> = ArrayDeque()

    fun loadSession(limit: Int = DEFAULT_SESSION_SIZE) {
        _uiState.update { it.copy(isLoading = true, isSessionFinished = false) }
        viewModelScope.launch {
            val cards = repository.getCardsToReview(limit)
            pending = ArrayDeque(cards)
            val nextCard = pending.removeFirstOrNull()
            _uiState.value = ReviewUiState(
                currentCard = nextCard,
                isAnswerRevealed = false,
                scrum = pending.size + if (nextCard == null) 0 else 1,
                isLoading = false,
                isSessionFinished = nextCard == null,
                studiedCount = 0,
                totalInSession = cards.size
            )
        }
    }

    fun revealAnswer() {
        _uiState.update { it.copy(isAnswerRevealed = true) }
    }

    fun gradeCard(quality: Int) {
        val current = _uiState.value.currentCard ?: return
        viewModelScope.launch {
            val updatedMotVersDef = updateSm2(current.sm2MotVersDef, quality)
            repository.updateCardProgress(
                cardId = current.id,
                motVersDef = updatedMotVersDef,
                defVersMot = current.sm2DefVersMot
            )
            _snackbarEvents.trySend("Revue dans ${updatedMotVersDef.interval} jours")

            val nextCard = pending.removeFirstOrNull()
            val studied = _uiState.value.studiedCount + 1
            _uiState.value = ReviewUiState(
                currentCard = nextCard,
                isAnswerRevealed = false,
                scrum = pending.size + if (nextCard == null) 0 else 1,
                isLoading = false,
                isSessionFinished = nextCard == null,
                studiedCount = studied,
                totalInSession = _uiState.value.totalInSession
            )
        }
    }

    private fun updateSm2(stats: Sm2Stats, quality: Int): Sm2Stats {
        val now = Instant.now()
        val result = sm2Algorithm.calculate(
            Sm2Algorithm.Sm2State(
                interval = stats.interval,
                repetitions = stats.repetitions,
                easeFactor = stats.easeFactor
            ),
            quality = quality,
            now = now
        )
        return stats.copy(
            interval = result.interval,
            repetitions = result.repetitions,
            easeFactor = result.easeFactor,
            nextReviewDate = result.nextReviewDate.toEpochMilli(),
            lastReviewDate = now.toEpochMilli(),
            totalReviews = stats.totalReviews + 1,
            correctReviews = stats.correctReviews + if (quality >= 3) 1 else 0,
            lapses = stats.lapses + if (quality < 3) 1 else 0
        )
    }

    fun toggleFavorite() {
        val current = _uiState.value.currentCard ?: return
        val updated = current.copy(favori = !current.favori)
        _uiState.update { it.copy(currentCard = updated) }
        viewModelScope.launch {
            repository.setFavorite(current.id, updated.favori)
        }
    }

    fun deleteCurrentCard() {
        val current = _uiState.value.currentCard ?: return
        viewModelScope.launch {
            repository.deleteCard(current.id)
            val nextCard = pending.removeFirstOrNull()
            val totalLeft = (_uiState.value.totalInSession - 1).coerceAtLeast(0)
            _uiState.value = ReviewUiState(
                currentCard = nextCard,
                isAnswerRevealed = false,
                scrum = pending.size + if (nextCard == null) 0 else 1,
                isLoading = false,
                isSessionFinished = nextCard == null,
                studiedCount = _uiState.value.studiedCount,
                totalInSession = totalLeft
            )
        }
    }

    companion object {
        private const val DEFAULT_SESSION_SIZE = 20
    }
}

data class ReviewUiState(
    val currentCard: Flashcard? = null,
    val isAnswerRevealed: Boolean = false,
    val scrum: Int = 0,
    val isLoading: Boolean = false,
    val isSessionFinished: Boolean = false,
    val studiedCount: Int = 0,
    val totalInSession: Int = 0
)

class ReviewViewModelFactory(
    private val repository: FlashcardRepository,
    private val sm2Algorithm: Sm2Algorithm = Sm2Algorithm
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(repository, sm2Algorithm) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
