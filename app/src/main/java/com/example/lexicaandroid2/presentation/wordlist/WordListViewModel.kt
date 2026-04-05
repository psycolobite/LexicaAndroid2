package com.example.lexicaandroid2.presentation.wordlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.data.remote.DictionaryService
import com.example.lexicaandroid2.data.remote.model.WordResult
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
    val selectedFilter: String? = null,
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
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredCards = applyFilters(it.cards, query, it.selectedFilter, it.progressByCardId)
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
