package com.example.lexicaandroid2.presentation.search.catalogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory pour instancier [CatalogueViewModel] avec le [CatalogueRepository] requis.
 */
class CatalogueViewModelFactory(
    private val repository: CatalogueRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CatalogueViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CatalogueViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
