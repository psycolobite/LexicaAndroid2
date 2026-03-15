package com.example.lexicaandroid2.presentation.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.data.remote.DictionaryService
import com.example.lexicaandroid2.data.remote.model.WordResult
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WordListUiState(
    val cards: List<Flashcard> = emptyList(),
    val filteredCards: List<Flashcard> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedFilter: String? = null, // "TO_LEARN", "LEARNING", "KNOWN"
    val apiSearchResults: List<WordResult> = emptyList(),
    val isApiLoading: Boolean = false,
    val apiError: String? = null
)

class WordListViewModel(
    private val repository: FlashcardRepository,
    private val dictionaryService: DictionaryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordListUiState())
    val uiState: StateFlow<WordListUiState> = _uiState.asStateFlow()

    fun loadWords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val allCards = repository.getAllCards()
            _uiState.update {
                it.copy(
                    cards = allCards,
                    filteredCards = applyFilters(allCards, it.searchQuery, it.selectedFilter),
                    isLoading = false
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredCards = applyFilters(it.cards, query, it.selectedFilter)
            )
        }
    }

    fun onFilterSelected(filter: String?) {
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                filteredCards = applyFilters(it.cards, it.searchQuery, filter)
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

    // API Search Logic
    fun searchOnline(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isApiLoading = true, apiError = null, apiSearchResults = emptyList()) }
            try {
                val results = dictionaryService.searchWord(query)
                _uiState.update {
                    it.copy(
                        isApiLoading = false,
                        apiSearchResults = results,
                        apiError = if (results.isEmpty()) "Aucun résultat trouvé pour '$query'" else null
                    )
                }
            } catch (e: Exception) {
                 _uiState.update { it.copy(isApiLoading = false, apiError = "Erreur: ${e.message}") }
            }
        }
    }

    fun clearApiResults() {
        _uiState.update { it.copy(apiSearchResults = emptyList(), apiError = null) }
    }

    // Add logic from API result
    fun addWordFromApi(result: WordResult) {
        viewModelScope.launch {
            val card = Flashcard(
                id = java.util.UUID.randomUUID().toString(),
                recto = result.mot,
                verso = result.definition,
                categorieGrammaticale = result.categorieGrammaticale,
                exemples = result.exemples,
                synonymes = result.synonymes,
                dateAjout = System.currentTimeMillis()
            )
            repository.saveCard(card)
            loadWords() // Refresh list
            clearApiResults()
        }
    }

    // Add new card logic will come later with API
    suspend fun addCard(card: Flashcard) {
         repository.saveCard(card)
         loadWords()
    }

    private fun applyFilters(cards: List<Flashcard>, query: String, filter: String?): List<Flashcard> {
        var result = cards

        if (query.isNotBlank()) {
            result = result.filter {
                it.recto.contains(query, ignoreCase = true) ||
                it.verso.contains(query, ignoreCase = true)
            }
        }

        if (filter != null) {
            // Need to reconstruct logic for state or add state to Domain model?
            // Domain model doesn't have 'state' field yet explicitly (it's calculated in Mapper).
            // Option 1: Add 'state' to Domain Flashcard.
            // Option 2: Re-calculate here or just accept it's a bit heavier.
            // Let's add 'state' to Domain Flashcard to be consistent (cleaner).

            // Wait, for now I will recalculate to avoid changing Domain model everywhere immediately if not strictly needed.
            result = result.filter {
                val isNew = it.sm2MotVersDef.repetitions == 0 && it.sm2DefVersMot.repetitions == 0
                val isKnown = it.sm2MotVersDef.interval > 20 && it.sm2DefVersMot.interval > 20
                val state = when {
                    isNew -> "TO_LEARN"
                    isKnown -> "KNOWN"
                    else -> "LEARNING"
                }
                state == filter
            }
        }

        return result.sortedBy { it.recto }
    }
}

class WordListViewModelFactory(
    private val repository: FlashcardRepository,
    private val dictionaryService: DictionaryService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WordListViewModel(repository, dictionaryService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
