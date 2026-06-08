package com.example.lexicaandroid2.data.corpus

import android.util.Log

/**
 * Index en mémoire des extraits de corpus.
 * Permet la recherche réactive par domaines thématiques et difficulté.
 */
class CorpusIndex {

    private val candidates = mutableListOf<ExtractCandidate>()

    companion object {
        private const val TAG = "CORPUS_INDEX"

        /**
         * Construit et peuple un [CorpusIndex] à partir d'une liste d'extraits.
         */
        fun index(extracts: List<ExtractCandidate>): CorpusIndex {
            val index = CorpusIndex()
            index.addAll(extracts)
            return index
        }
    }

    /**
     * Ajoute un candidat à l'index.
     */
    fun add(candidate: ExtractCandidate) {
        candidates.add(candidate)
    }

    /**
     * Ajoute une liste de candidats à l'index.
     */
    fun addAll(newCandidates: List<ExtractCandidate>) {
        candidates.addAll(newCandidates)
        Log.d(TAG, "Indexed ${newCandidates.size} candidates. Total: ${candidates.size}")
    }

    /**
     * Retourne tous les candidats avec une limite de résultats.
     */
    fun getAllCandidates(limit: Int = 50): List<ExtractCandidate> {
        return candidates.take(limit)
    }

    /**
     * Recherche les candidats correspondant à une liste d'identifiants de domaines.
     */
    fun getCandidates(domainIds: List<String>, limit: Int = 50): List<ExtractCandidate> {
        if (domainIds.isEmpty()) return getAllCandidates(limit)
        return candidates
            .filter { candidate -> candidate.domainTags.any { it in domainIds } }
            .take(limit)
    }

    /**
     * Recherche les candidats correspondant à une difficulté donnée.
     */
    fun getCandidatesByDifficulty(difficulty: String, limit: Int = 50): List<ExtractCandidate> {
        return candidates
            .filter { it.difficulty.lowercase() == difficulty.lowercase() }
            .take(limit)
    }

    /**
     * Liste tous les domaines disponibles dans l'index.
     */
    fun availableDomains(): List<String> {
        return candidates.flatMap { it.domainTags }.distinct()
    }

    /**
     * Nombre total d'extraits indexés.
     */
    fun size(): Int = candidates.size

    /**
     * Nombre total d'extraits indexés (alias pour compatibilité).
     */
    fun count(): Int = candidates.size

    /**
     * Vide l'index.
     */
    fun clear() {
        candidates.clear()
    }
}
