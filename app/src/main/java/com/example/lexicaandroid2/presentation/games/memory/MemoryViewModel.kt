package com.example.lexicaandroid2.presentation.games.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemoryCard(
    val id: String,
    val pairId: String,
    val text: String,
    val isWord: Boolean,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

data class GridConfig(
    val columns: Int,
    val rows: Int
) {
    val totalCards: Int
        get() = columns * rows
}

data class MemoryUiState(
    val cards: List<MemoryCard> = emptyList(),
    val score: Int = 0,
    val attempts: Int = 0,
    val matchedPairs: Int = 0,
    val totalPairs: Int = 0,
    val selectedConfig: GridConfig = GridConfig(columns = 4, rows = 4),
    val availableConfigs: List<GridConfig> = listOf(
        GridConfig(4, 4),
        GridConfig(5, 4),
        GridConfig(6, 4)
    ),
    val firstSelectedCardId: String? = null,
    val secondSelectedCardId: String? = null,
    val isCheckingPair: Boolean = false,
    val gameOver: Boolean = false,
    val bestScore: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val progress: Float
        get() = if (totalPairs == 0) 0f else matchedPairs.toFloat() / totalPairs.toFloat()
}

class MemoryViewModel(private val repository: FlashcardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    fun loadGame(config: GridConfig = _uiState.value.selectedConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    selectedConfig = config,
                    gameOver = false,
                    score = 0,
                    attempts = 0,
                    matchedPairs = 0,
                    firstSelectedCardId = null,
                    secondSelectedCardId = null,
                    isCheckingPair = false
                )
            }

            try {
                val allCards = repository.getAllCards()
                    .filter { it.recto.isNotBlank() && it.verso.isNotBlank() }
                    .distinctBy { it.id }

                val pairCount = config.totalCards / 2
                val selected = allCards.shuffled().take(pairCount)

                if (selected.size < pairCount) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Pas assez de cartes pour cette grille"
                        )
                    }
                    return@launch
                }

                val deck = buildDeck(selected)

                _uiState.update {
                    it.copy(
                        cards = deck,
                        totalPairs = pairCount,
                        isLoading = false
                    )
                }
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

    fun onCardClicked(cardId: String) {
        val state = _uiState.value
        if (state.isCheckingPair || state.gameOver) return

        val card = state.cards.find { it.id == cardId } ?: return
        if (card.isMatched || card.isFaceUp) return

        val updatedCards = state.cards.map {
            if (it.id == cardId) it.copy(isFaceUp = true) else it
        }

        if (state.firstSelectedCardId == null) {
            _uiState.update {
                it.copy(
                    cards = updatedCards,
                    firstSelectedCardId = cardId
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                cards = updatedCards,
                secondSelectedCardId = cardId,
                isCheckingPair = true,
                attempts = it.attempts + 1
            )
        }

        evaluateSelectedPair()
    }

    fun restart() {
        loadGame(_uiState.value.selectedConfig)
    }

    private fun evaluateSelectedPair() {
        viewModelScope.launch {
            val state = _uiState.value
            val first = state.cards.find { it.id == state.firstSelectedCardId } ?: return@launch
            val second = state.cards.find { it.id == state.secondSelectedCardId } ?: return@launch

            val isMatch = first.pairId == second.pairId && first.isWord != second.isWord

            if (isMatch) {
                val matched = state.cards.map {
                    if (it.id == first.id || it.id == second.id) it.copy(isMatched = true) else it
                }
                val newMatchedPairs = state.matchedPairs + 1
                val gainedScore = 10
                val newScore = state.score + gainedScore
                val finished = newMatchedPairs >= state.totalPairs
                val newBest = maxOf(state.bestScore, newScore)

                _uiState.update {
                    it.copy(
                        cards = matched,
                        score = newScore,
                        bestScore = newBest,
                        matchedPairs = newMatchedPairs,
                        firstSelectedCardId = null,
                        secondSelectedCardId = null,
                        isCheckingPair = false,
                        gameOver = finished
                    )
                }
            } else {
                delay(600)
                val hidden = _uiState.value.cards.map {
                    if (it.id == first.id || it.id == second.id) it.copy(isFaceUp = false) else it
                }

                _uiState.update {
                    it.copy(
                        cards = hidden,
                        firstSelectedCardId = null,
                        secondSelectedCardId = null,
                        isCheckingPair = false
                    )
                }
            }
        }
    }

    private fun buildDeck(selected: List<Flashcard>): List<MemoryCard> {
        return selected.flatMapIndexed { index, flashcard ->
            val pairId = flashcard.id.ifBlank { "pair_$index" }
            listOf(
                MemoryCard(
                    id = "${pairId}_word",
                    pairId = pairId,
                    text = flashcard.recto,
                    isWord = true
                ),
                MemoryCard(
                    id = "${pairId}_definition",
                    pairId = pairId,
                    text = flashcard.verso,
                    isWord = false
                )
            )
        }.shuffled()
    }
}
