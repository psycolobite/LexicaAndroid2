package com.example.lexicaandroid2.presentation.addwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.data.local.WordReserveEntity
import com.example.lexicaandroid2.domain.repository.WordReserveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddWordsUiState(
    val proposedWords: List<WordReserveEntity> = emptyList(),
    val searchResults: List<WordReserveEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class AddWordsViewModel(
    private val repository: WordReserveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWordsUiState())
    val uiState: StateFlow<AddWordsUiState> = _uiState.asStateFlow()

    init {
        loadProposedWords()
    }

    fun loadProposedWords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val words = repository.getProposedWords()
            _uiState.update { it.copy(proposedWords = words, isLoading = false) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun performSearch() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, searchResults = emptyList()) }
            try {
                val results = repository.searchOnline(query)
                _uiState.update {
                    it.copy(
                        searchResults = results,
                        isLoading = false,
                        error = if (results.isEmpty()) "Aucun résultat trouvé" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Erreur de recherche") }
            }
        }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), error = null) }
        loadProposedWords()
    }

    fun addWordToCollection(word: WordReserveEntity) {
        viewModelScope.launch {
            repository.addToCollection(word)
            // Remove locally to update UI immediately
            _uiState.update { state ->
                state.copy(
                    proposedWords = state.proposedWords.filter { it.id != word.id },
                    searchResults = state.searchResults.filter { it.id != word.id }
                )
            }
            // Ideally re-fetch if reserve is low, but for now simple removal is enough
        }
    }
}

class AddWordsViewModelFactory(
    private val repository: WordReserveRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddWordsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddWordsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

