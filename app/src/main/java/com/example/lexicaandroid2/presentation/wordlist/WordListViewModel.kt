package com.example.lexicaandroid2.presentation.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewCardProgressSummary
import com.example.lexicaandroid2.domain.model.Sm2Stats
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WordListUiState(
    val cards: List<Flashcard> = emptyList(),
    val filteredCards: List<Flashcard> = emptyList(),
    val progressByCardId: Map<String, ReviewCardProgressSummary> = emptyMap(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedFilter: String? = null,
    val selectedCardIds: Set<String> = emptySet()
) {
    val isSelectionMode: Boolean get() = selectedCardIds.isNotEmpty()
    val selectedCount: Int get() = selectedCardIds.size
}

class WordListViewModel(
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordListUiState())
    val uiState: StateFlow<WordListUiState> = _uiState.asStateFlow()

    fun loadWords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val allCards = repository.getAllCards()
            val progressByCardId = ReviewCardProgressSummary.indexByCardId(
                cards = allCards,
                questionProgress = repository.getAllQuestionProgress()
            )
            _uiState.update { state ->
                state.withCards(
                    cards = allCards,
                    progressByCardId = progressByCardId
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.withFilters(
                query = query,
                filter = state.selectedFilter
            )
        }
    }

    fun onFilterSelected(filter: String?) {
        _uiState.update { state ->
            state.withFilters(
                query = state.searchQuery,
                filter = filter
            )
        }
    }

    fun toggleCardSelection(cardId: String) {
        _uiState.update { state ->
            val updatedSelection = state.selectedCardIds.toMutableSet().apply {
                if (!add(cardId)) {
                    remove(cardId)
                }
            }
            state.copy(selectedCardIds = updatedSelection)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedCardIds = emptySet()) }
    }

    fun selectAllVisible() {
        _uiState.update { state ->
            state.copy(selectedCardIds = state.filteredCards.mapTo(linkedSetOf()) { it.id })
        }
    }

    fun toggleFavorite(card: Flashcard) {
        viewModelScope.launch {
            repository.setFavorite(card.id, !card.favori)
            loadWords()
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            repository.deleteCard(cardId)
            loadWords()
        }
    }

    fun deleteSelectedCards() {
        val cardIds = _uiState.value.selectedCardIds.toList()
        if (cardIds.isEmpty()) return

        viewModelScope.launch {
            cardIds.forEach { repository.deleteCard(it) }
            _uiState.update { it.copy(selectedCardIds = emptySet()) }
            loadWords()
        }
    }

    fun favoriteSelectedCards() {
        val selectedCards = _uiState.value.cards.filter { it.id in _uiState.value.selectedCardIds }
        if (selectedCards.isEmpty()) return

        viewModelScope.launch {
            selectedCards.forEach { card ->
                if (!card.favori) {
                    repository.setFavorite(card.id, true)
                }
            }
            _uiState.update { it.copy(selectedCardIds = emptySet()) }
            loadWords()
        }
    }

    fun resetProgressForSelectedCards() {
        val cardIds = _uiState.value.selectedCardIds.toList()
        if (cardIds.isEmpty()) return

        viewModelScope.launch {
            cardIds.forEach { cardId ->
                repository.updateCardProgress(
                    cardId = cardId,
                    motVersDef = Sm2Stats(),
                    defVersMot = Sm2Stats()
                )
            }
            _uiState.update { it.copy(selectedCardIds = emptySet()) }
            loadWords()
        }
    }

    private fun applyFilters(
        cards: List<Flashcard>,
        query: String,
        filter: String?,
        progressByCardId: Map<String, ReviewCardProgressSummary>
    ): List<Flashcard> {
        var result = cards

        if (query.isNotBlank()) {
            result = result.filter {
                it.recto.contains(query, ignoreCase = true) ||
                it.verso.contains(query, ignoreCase = true)
            }
        }

        if (filter != null) {
            result = result.filter {
                progressByCardId[it.id]?.matchesFilter(filter) == true
            }
        }

        return result.sortedBy { it.recto }
    }

    private fun WordListUiState.withCards(
        cards: List<Flashcard>,
        progressByCardId: Map<String, ReviewCardProgressSummary>
    ): WordListUiState {
        val filteredCards = applyFilters(cards, searchQuery, selectedFilter, progressByCardId)
        return copy(
            cards = cards,
            filteredCards = filteredCards,
            progressByCardId = progressByCardId,
            isLoading = false,
            selectedCardIds = selectedCardIds.intersect(filteredCards.mapTo(linkedSetOf()) { it.id })
        )
    }

    private fun WordListUiState.withFilters(
        query: String,
        filter: String?
    ): WordListUiState {
        val filteredCards = applyFilters(cards, query, filter, progressByCardId)
        return copy(
            searchQuery = query,
            selectedFilter = filter,
            filteredCards = filteredCards,
            selectedCardIds = selectedCardIds.intersect(filteredCards.mapTo(linkedSetOf()) { it.id })
        )
    }
}

class WordListViewModelFactory(
    private val repository: FlashcardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WordListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
