package com.example.lexicaandroid2.presentation.search.catalogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.data.corpus.CorpusSource
import com.example.lexicaandroid2.data.corpus.ExtractCandidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Options de tri pour le catalogue d'ouvrages.
 */
enum class CatalogueSortOption(val label: String) {
    TITLE("Titre"),
    DATE("Année de publication"),
    PERTINENCE("Pertinence thématique"),
    EXTRACT_COUNT("Nombre d'extraits")
}

/**
 * État de l'interface graphique pour le catalogue d'ouvrages.
 */
data class CatalogueUiState(
    val sources: List<CorpusSource> = emptyList(),
    val selectedSource: CorpusSource? = null,
    val extracts: List<ExtractCandidate> = emptyList(),
    val relatedSources: List<CorpusSource> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDomains: List<String> = emptyList(),
    val sortBy: CatalogueSortOption = CatalogueSortOption.TITLE
)

/**
 * ViewModel du catalogue d'ouvrages.
 */
class CatalogueViewModel(
    private val repository: CatalogueRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogueUiState())
    val uiState: StateFlow<CatalogueUiState> = _uiState.asStateFlow()

    private val _openUrlEvent = MutableStateFlow<String?>(null)
    val openUrlEvent: StateFlow<String?> = _openUrlEvent.asStateFlow()

    /**
     * Charge les sources disponibles. Si [domainIds] est fourni, filtre par ces domaines.
     */
    fun loadSources(domainIds: List<String> = emptyList()) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Simuler un léger délai pour l'UX
                kotlinx.coroutines.delay(100)
                _uiState.update {
                    it.copy(
                        selectedDomains = domainIds,
                        isLoading = false
                    )
                }
                applySortAndFilter()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur de chargement des sources"
                    )
                }
            }
        }
    }

    /**
     * Sélectionne une source pour afficher ses détails et extraits.
     * Si `sourceId` est null, désélectionne l'ouvrage courant.
     */
    fun selectSource(sourceId: String?) {
        if (sourceId.isNullOrBlank()) {
            _uiState.update { it.copy(selectedSource = null, extracts = emptyList(), relatedSources = emptyList()) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val source = repository.getSourceById(sourceId)
                if (source == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Ouvrage introuvable : $sourceId"
                        )
                    }
                    return@launch
                }
                val extracts = repository.getExtractsBySource(sourceId)
                val related = repository.getRelatedSources(sourceId)
                _uiState.update {
                    it.copy(
                        selectedSource = source,
                        extracts = extracts,
                        relatedSources = related,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur lors de la sélection de l'ouvrage"
                    )
                }
            }
        }
    }

    /**
     * Ajoute ou retire un filtre par domaine.
     */
    fun toggleDomainFilter(domainId: String) {
        val currentDomains = _uiState.value.selectedDomains
        val newDomains = if (domainId in currentDomains) {
            currentDomains - domainId
        } else {
            currentDomains + domainId
        }
        loadSources(newDomains)
    }

    /**
     * Réinitialise les filtres de domaine.
     */
    fun clearDomainFilters() {
        loadSources(emptyList())
    }

    /**
     * Modifie l'option de tri du catalogue.
     */
    fun changeSortOption(sortOption: CatalogueSortOption) {
        _uiState.update { it.copy(sortBy = sortOption) }
        applySortAndFilter()
    }

    /**
     * Déclenche l'événement d'ouverture de l'URL dans le navigateur.
     */
    fun openSourceUrl(url: String) {
        _openUrlEvent.value = url
    }

    /**
     * Consomme l'événement d'ouverture d'URL.
     */
    fun consumeOpenUrlEvent() {
        _openUrlEvent.value = null
    }

    private fun applySortAndFilter() {
        val state = _uiState.value
        var list = repository.getSourcesByDomain(state.selectedDomains)

        list = when (state.sortBy) {
            CatalogueSortOption.TITLE -> list.sortedBy { it.title.lowercase() }
            CatalogueSortOption.DATE -> list.sortedByDescending { it.year ?: 0 }
            CatalogueSortOption.PERTINENCE -> {
                // Trie par nombre de tags de domaines correspondants (ceux sélectionnés)
                if (state.selectedDomains.isEmpty()) {
                    list.sortedBy { it.title.lowercase() }
                } else {
                    list.sortedByDescending { source ->
                        source.domainTags.count { it in state.selectedDomains }
                    }
                }
            }
            CatalogueSortOption.EXTRACT_COUNT -> {
                list.sortedByDescending { source ->
                    repository.getExtractsBySource(source.id).size
                }
            }
        }

        _uiState.update { it.copy(sources = list) }
    }
}
