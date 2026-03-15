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

/** Résultat d'une tentative d'appariement, affiché brièvement à l'utilisateur. */
enum class MatchResult { SUCCESS, FAILURE, NONE }

data class MatchingUiState(
    val pairs: List<MatchingPair> = emptyList(),
    val shuffledDefinitions: List<String> = emptyList(),
    val selectedWord: String? = null,
    val selectedDefinition: String? = null,
    val foundPairs: Set<String> = emptySet(),       // wordId des paires correctement appariées
    val wrongPairs: Set<String> = emptySet(),        // wordId de la tentative incorrecte en cours
    val score: Int = 0,
    val totalPairs: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val gameOver: Boolean = false,
    val lastMatchResult: MatchResult = MatchResult.NONE  // feedback affiché après "Valider"
)

class MatchingViewModel(private val repository: FlashcardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MatchingUiState())
    val uiState: StateFlow<MatchingUiState> = _uiState.asStateFlow()

    fun loadGame() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val flashcards = repository.getAllCards()
                    .shuffled()
                    .take(4)

                if (flashcards.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "Aucune carte disponible") }
                    return@launch
                }

                val pairs = GameUtils.createMatchingPairs(flashcards)
                val definitions = pairs.map { it.definitionText }.shuffled()

                _uiState.update {
                    it.copy(
                        pairs = pairs,
                        shuffledDefinitions = definitions,
                        totalPairs = pairs.size,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Erreur inconnue") }
            }
        }
    }

    fun selectWord(wordId: String) {
        _uiState.update { state ->
            if (state.foundPairs.contains(wordId)) state
            else state.copy(selectedWord = wordId, selectedDefinition = null, wrongPairs = emptySet(), lastMatchResult = MatchResult.NONE)
        }
    }

    fun selectDefinition(definitionText: String) {
        _uiState.update { state ->
            if (state.selectedWord == null) state.copy(selectedDefinition = definitionText, lastMatchResult = MatchResult.NONE)
            else state.copy(selectedDefinition = definitionText, lastMatchResult = MatchResult.NONE, wrongPairs = emptySet())
        }
    }

    /** Appelé par le bouton "Valider" — vérifie la paire sélectionnée. */
    fun validateSelection() {
        val state = _uiState.value
        val wordId = state.selectedWord ?: return
        val definition = state.selectedDefinition ?: return
        val pair = state.pairs.find { it.wordId == wordId } ?: return

        if (pair.definitionText == definition) {
            // ✅ Bonne paire
            val newFound = state.foundPairs + wordId
            _uiState.update {
                it.copy(
                    foundPairs = newFound,
                    score = newFound.size,
                    selectedWord = null,
                    selectedDefinition = null,
                    wrongPairs = emptySet(),
                    lastMatchResult = MatchResult.SUCCESS,
                    gameOver = newFound.size == state.totalPairs
                )
            }
            // Effacer le feedback après 1 s
            viewModelScope.launch {
                delay(1000)
                _uiState.update { it.copy(lastMatchResult = MatchResult.NONE) }
            }
        } else {
            // ❌ Mauvaise paire — marquer en rouge, conserver la sélection pour réessai
            _uiState.update {
                it.copy(
                    wrongPairs = setOf(wordId),
                    lastMatchResult = MatchResult.FAILURE
                )
            }
            // Effacer la mise en rouge après 900 ms, reset sélection
            viewModelScope.launch {
                delay(900)
                _uiState.update {
                    it.copy(
                        selectedWord = null,
                        selectedDefinition = null,
                        wrongPairs = emptySet(),
                        lastMatchResult = MatchResult.NONE
                    )
                }
            }
        }
    }

    fun resetGame() {
        viewModelScope.launch {
            _uiState.update { MatchingUiState() }
            loadGame()
        }
    }
}
