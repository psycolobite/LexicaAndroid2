package com.example.lexicaandroid2.presentation.games.anagrams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.domain.XPCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LetterTile(
    val id: Int,
    val letter: Char,
    val used: Boolean = false
)

data class AnagramsUiState(
    val rounds: List<Flashcard> = emptyList(),
    val currentRoundIndex: Int = 0,
    val tiles: List<LetterTile> = emptyList(),
    val answerTileIds: List<Int> = emptyList(),
    val score: Int = 0,
    val totalRounds: Int = 0,
    val earnedXp: Int = 0,
    val isLoading: Boolean = true,
    val isRoundAnswered: Boolean = false,
    val isAnswerCorrect: Boolean? = null,
    val gameOver: Boolean = false,
    val error: String? = null
) {
    val currentRound: Flashcard?
        get() = rounds.getOrNull(currentRoundIndex)

    val currentAnswer: String
        get() = answerTileIds.joinToString(separator = "")

    val progress: Float
        get() = if (totalRounds == 0) 0f else currentRoundIndex.toFloat() / totalRounds.toFloat()
}

class AnagramsViewModel(private val repository: FlashcardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AnagramsUiState())
    val uiState: StateFlow<AnagramsUiState> = _uiState.asStateFlow()

    fun loadGame() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val rounds = repository
                    .getAllCards()
                    .mapNotNull { card ->
                        val cleaned = normalizeWord(card.recto)
                        if (cleaned.length >= 3) card.copy(recto = cleaned) else null
                    }
                    .distinctBy { it.recto }
                    .shuffled()
                    .take(10)

                if (rounds.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No words available for anagrams"
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        rounds = rounds,
                        totalRounds = rounds.size,
                        currentRoundIndex = 0,
                        score = 0,
                        earnedXp = 0,
                        isLoading = false,
                        gameOver = false,
                        isRoundAnswered = false,
                        isAnswerCorrect = null
                    )
                }
                prepareRound(rounds[0].recto)
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

    fun selectTile(tileId: Int) {
        _uiState.update { state ->
            if (state.isRoundAnswered || state.gameOver) return@update state

            val tile = state.tiles.find { it.id == tileId } ?: return@update state
            if (tile.used) return@update state

            state.copy(
                tiles = state.tiles.map {
                    if (it.id == tileId) it.copy(used = true) else it
                },
                answerTileIds = state.answerTileIds + tileId
            )
        }
    }

    fun removeLastLetter() {
        _uiState.update { state ->
            if (state.isRoundAnswered || state.answerTileIds.isEmpty()) return@update state
            val tileId = state.answerTileIds.last()
            state.copy(
                answerTileIds = state.answerTileIds.dropLast(1),
                tiles = state.tiles.map {
                    if (it.id == tileId) it.copy(used = false) else it
                }
            )
        }
    }

    fun clearAnswer() {
        _uiState.update { state ->
            if (state.isRoundAnswered) return@update state
            state.copy(
                answerTileIds = emptyList(),
                tiles = state.tiles.map { it.copy(used = false) }
            )
        }
    }

    fun submitAnswer() {
        _uiState.update { state ->
            val target = state.currentRound?.recto ?: return@update state
            if (state.isRoundAnswered || state.answerTileIds.isEmpty()) return@update state

            val built = buildAnswer(state.tiles, state.answerTileIds)
            val isCorrect = built == target

            val roundXp = if (isCorrect) XPCalculator.XP_WORD_REVIEWED else 0
            state.copy(
                isRoundAnswered = true,
                isAnswerCorrect = isCorrect,
                score = if (isCorrect) state.score + 1 else state.score,
                earnedXp = state.earnedXp + roundXp
            )
        }
    }

    fun nextRound() {
        _uiState.update { state ->
            if (!state.isRoundAnswered) return@update state

            val nextIndex = state.currentRoundIndex + 1
            if (nextIndex >= state.totalRounds) {
                val perfect = state.score == state.totalRounds
                val bonus = XPCalculator.calculateXpForGame(perfectScore = perfect)
                return@update state.copy(
                    gameOver = true,
                    earnedXp = state.earnedXp + bonus
                )
            }

            val nextRound = state.rounds[nextIndex]
            val newTiles = scrambleWord(nextRound.recto)
                .mapIndexed { index, c -> LetterTile(id = index, letter = c) }

            state.copy(
                currentRoundIndex = nextIndex,
                tiles = newTiles,
                answerTileIds = emptyList(),
                isRoundAnswered = false,
                isAnswerCorrect = null
            )
        }
    }

    fun restartGame() {
        _uiState.update { AnagramsUiState() }
        loadGame()
    }

    private fun prepareRound(word: String) {
        val tiles = scrambleWord(word).mapIndexed { index, c -> LetterTile(id = index, letter = c) }
        _uiState.update {
            it.copy(
                tiles = tiles,
                answerTileIds = emptyList(),
                isRoundAnswered = false,
                isAnswerCorrect = null
            )
        }
    }

    private fun buildAnswer(tiles: List<LetterTile>, answerTileIds: List<Int>): String {
        val byId = tiles.associateBy { it.id }
        return answerTileIds.mapNotNull { byId[it]?.letter }.joinToString(separator = "")
    }

    private fun normalizeWord(raw: String): String {
        return raw.uppercase().filter { it.isLetter() }
    }

    private fun scrambleWord(word: String): List<Char> {
        if (word.length <= 1) return word.toList()

        val original = word.toList()
        repeat(8) {
            val shuffled = original.shuffled()
            if (shuffled != original) {
                return shuffled
            }
        }
        return original
    }
}
