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
import java.text.Normalizer
import java.util.UUID

data class WordListUiState(
    val cards: List<Flashcard> = emptyList(),
    val filteredCards: List<Flashcard> = emptyList(),
    val progressByCardId: Map<String, ReviewCardProgressSummary> = emptyMap(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedFilter: String? = null,
    val apiSearchResults: List<WordResult> = emptyList(),
    val isApiLoading: Boolean = false,
    val apiError: String? = null,
    val apiPreviewResult: WordResult? = null,
    val successMessage: String? = null
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
        val filteredCards = applyFilters(
            cards = _uiState.value.cards,
            query = query,
            filter = _uiState.value.selectedFilter,
            progressByCardId = _uiState.value.progressByCardId
        )
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredCards = filteredCards,
                successMessage = null
            )
        }

        val trimmed = query.trim()
        if (trimmed.length < 2) {
            clearApiResults()
            return
        }

        if (hasCollectionMatch(trimmed)) {
            clearApiResults()
            return
        }

        searchOnline(trimmed)
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
                    .filterNot { isAlreadyInCollection(it.mot) }
                _uiState.update {
                    it.copy(
                        isApiLoading = false,
                        apiSearchResults = results,
                        apiError = if (results.isEmpty()) {
                            "Aucun mot trouvé dans la base de recherche pour \"$query\""
                        } else {
                            null
                        }
                    )
                }
            } catch (e: Exception) {
                 _uiState.update {
                     it.copy(
                         isApiLoading = false,
                         apiError = "Impossible d'interroger la base de recherche${e.message?.let { message -> " : $message" } ?: ""}"
                     )
                 }
            }
        }
    }

    fun clearApiResults() {
        _uiState.update {
            it.copy(
                apiSearchResults = emptyList(),
                apiError = null,
                isApiLoading = false,
                apiPreviewResult = null
            )
        }
    }

    fun openApiPreview(result: WordResult) {
        _uiState.update { it.copy(apiPreviewResult = result) }
    }

    fun closeApiPreview() {
        _uiState.update { it.copy(apiPreviewResult = null) }
    }

    // Add logic from API result
    fun addWordFromApi(result: WordResult) {
        viewModelScope.launch {
            if (isAlreadyInCollection(result.mot)) {
                _uiState.update {
                    it.copy(
                        apiPreviewResult = null,
                        apiError = "\"${result.mot}\" est déjà présent dans ta liste"
                    )
                }
                return@launch
            }

            val card = Flashcard(
                id = UUID.randomUUID().toString(),
                recto = result.mot,
                verso = result.definition,
                categorieGrammaticale = result.categorieGrammaticale,
                exemples = result.exemples,
                synonymes = result.synonymes,
                dateAjout = System.currentTimeMillis()
            )
            repository.saveCard(card)
            loadWords() // Refresh list
            _uiState.update {
                it.copy(
                    apiSearchResults = emptyList(),
                    apiError = null,
                    apiPreviewResult = null,
                    successMessage = "\"${result.mot}\" a été ajouté à tes mots"
                )
            }
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

    private fun hasCollectionMatch(query: String): Boolean {
        return _uiState.value.cards.any {
            it.recto.contains(query, ignoreCase = true) ||
                it.verso.contains(query, ignoreCase = true)
        }
    }

    private fun isAlreadyInCollection(word: String): Boolean {
        val normalizedWord = normalize(word)
        return _uiState.value.cards.any { normalize(it.recto) == normalizedWord }
    }

    private fun normalize(value: String): String {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
            .trim()
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
