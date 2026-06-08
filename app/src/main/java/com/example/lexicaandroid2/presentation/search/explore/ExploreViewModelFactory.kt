package com.example.lexicaandroid2.presentation.search.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lexicaandroid2.data.corpus.CorpusIndex

/**
 * Factory pour instancier [ExploreViewModel] avec le [CorpusIndex] requis.
 */
class ExploreViewModelFactory(
    private val corpusIndex: CorpusIndex,
    private val scoringEngine: Any? = null,
    private val rankingStrategy: Any? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExploreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExploreViewModel(
                corpusIndex = corpusIndex,
                scoringEngine = scoringEngine,
                rankingStrategy = rankingStrategy
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
