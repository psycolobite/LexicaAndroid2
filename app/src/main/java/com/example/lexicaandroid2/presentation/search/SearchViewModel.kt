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

                resultsFlow.collect { results ->
                    val count = if (query.isBlank()) {
                        repository.countSearchResults("")
                    } else {
                        results.size
                    }

                    _uiState.update {
                        it.copy(
                            results = results,
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

    fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _uiState.update { it.copy(query = "", isLoading = false) }
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
