package com.example.lexicaandroid2.presentation.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewCardProgressSummary
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
    val selectedFilter: String? = null
)

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
            _uiState.update {
                it.copy(
                    cards = allCards,
                    filteredCards = applyFilters(allCards, it.searchQuery, it.selectedFilter, progressByCardId),
                    progressByCardId = progressByCardId,
                    isLoading = false
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val filteredCards = applyFilters(
            cards = _uiState.value.cards,
            query = query,
            filter = _uiState.value.selectedFilter,
            progressByCardId = _uiState.value.progressByCardId
        )
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredCards = filteredCards
            )
        }
    }

    fun onFilterSelected(filter: String?) {
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                filteredCards = applyFilters(it.cards, it.searchQuery, filter, it.progressByCardId)
            )
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
