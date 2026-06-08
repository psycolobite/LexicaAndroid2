package com.example.lexicaandroid2.presentation.search.catalogue

import com.example.lexicaandroid2.data.corpus.CorpusIndex
import com.example.lexicaandroid2.data.corpus.CorpusSource
import com.example.lexicaandroid2.data.corpus.CorpusSources
import com.example.lexicaandroid2.data.corpus.ExtractCandidate

/**
 * Repository gérant l'accès aux sources d'ouvrages complets
 * et à leurs extraits associés indexés dans [CorpusIndex].
 */
class CatalogueRepository(private val corpusIndex: CorpusIndex) {

    /**
     * Récupère la liste des sources associées à une liste de domaines.
     * Si la liste est vide, retourne toutes les sources disponibles.
     */
    fun getSourcesByDomain(domainIds: List<String>): List<CorpusSource> {
        if (domainIds.isEmpty()) {
            return CorpusSources.ALL
        }
        return CorpusSources.ALL.filter { source ->
            source.domainTags.any { it in domainIds }
        }
    }

    /**
     * Récupère une source par son identifiant unique.
     */
    fun getSourceById(sourceId: String): CorpusSource? {
        return CorpusSources.byId(sourceId)
    }

    /**
     * Récupère les extraits candidats associés à une source spécifique.
     */
    fun getExtractsBySource(sourceId: String): List<ExtractCandidate> {
        return corpusIndex.getAllCandidates(limit = 10000).filter { it.sourceId == sourceId }
    }

    /**
     * Récupère les sources similaires partageant des domaines avec la source donnée.
     */
    fun getRelatedSources(sourceId: String, maxResults: Int = 3): List<CorpusSource> {
        val source = getSourceById(sourceId) ?: return emptyList()
        if (source.domainTags.isEmpty()) return emptyList()
        return CorpusSources.ALL
            .filter { it.id != sourceId && it.domainTags.any { tag -> tag in source.domainTags } }
            .take(maxResults)
    }
}
