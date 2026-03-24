package com.example.lexicaandroid2.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.SearchRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.Normalizer

enum class SearchType {
    GLOBAL, BY_WORD, BY_DEFINITION, FAVORITES
}

data class SearchUiState(
    val query: String = "",
    val searchType: SearchType = SearchType.GLOBAL,
    val results: List<Flashcard> = emptyList(),
    val isLoading: Boolean = false,
    val totalResults: Int = 0,
    val error: String? = null
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val repository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        // Debounce de 300ms pour la recherche
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    performSearch(query, _uiState.value.searchType)
                }
        }

        // Charger les résultats initiaux
        performSearch("", SearchType.GLOBAL)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(query = query, isLoading = true) }
        _searchQuery.value = query
    }

    fun onSearchTypeChanged(type: SearchType) {
        _uiState.update { it.copy(searchType = type) }
        performSearch(_uiState.value.query, type)
    }

    private fun performSearch(query: String, type: SearchType) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val resultsFlow = when (type) {
                    SearchType.GLOBAL -> repository.searchGlobal(query)
                    SearchType.BY_WORD -> repository.searchByWord(query)
                    SearchType.BY_DEFINITION -> repository.searchByDefinition(query)
                    SearchType.FAVORITES -> repository.searchFavorites(query)
                }

                resultsFlow.collect { localResults ->
                    val resolvedResults = resolveResults(query = query, type = type, localResults = localResults)
                    val count = if (query.isBlank()) repository.countSearchResults("") else resolvedResults.size

                    _uiState.update {
                        it.copy(
                            results = resolvedResults,
                            totalResults = count,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur lors de la recherche"
                    )
                }
            }
        }
    }

    private suspend fun resolveResults(
        query: String,
        type: SearchType,
        localResults: List<Flashcard>
    ): List<Flashcard> {
        if (query.isBlank()) return localResults
        if (localResults.isNotEmpty()) return localResults

        val accentInsensitive = accentInsensitiveFallback(query = query, type = type)
        if (accentInsensitive.isNotEmpty()) return accentInsensitive

        return if (type == SearchType.GLOBAL || type == SearchType.BY_WORD) {
            repository.searchExternal(query)
        } else {
            emptyList()
        }
    }

    private suspend fun accentInsensitiveFallback(query: String, type: SearchType): List<Flashcard> {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return emptyList()

        val allCards = repository.getAllPaginated(limit = 500, offset = 0).first()

        return allCards
            .asSequence()
            .filter { card ->
                when (type) {
                    SearchType.GLOBAL -> {
                        normalize(card.recto).contains(normalizedQuery) ||
                            normalize(card.verso).contains(normalizedQuery) ||
                            card.synonymes.any { normalize(it).contains(normalizedQuery) }
                    }
                    SearchType.BY_WORD -> normalize(card.recto).contains(normalizedQuery)
                    SearchType.BY_DEFINITION -> normalize(card.verso).contains(normalizedQuery)
                    SearchType.FAVORITES -> {
                        card.favori && (
                            normalize(card.recto).contains(normalizedQuery) ||
                                normalize(card.verso).contains(normalizedQuery)
                            )
                    }
                }
            }
            .take(50)
            .toList()
    }

    private fun normalize(value: String): String {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
            .trim()
    }

    fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _uiState.update { it.copy(query = "", results = emptyList(), totalResults = 0, isLoading = false) }
    }
}

class SearchViewModelFactory(
    private val repository: SearchRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
