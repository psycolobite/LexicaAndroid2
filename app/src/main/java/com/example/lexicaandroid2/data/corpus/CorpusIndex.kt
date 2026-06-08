package com.example.lexicaandroid2.data.corpus

import android.util.Log
import java.text.Normalizer

/**
 * Index en mémoire des extraits de corpus, optimisé pour la recherche rapide
 * par mot cible.
 *
 * [CorpusIndex] stocke les [ExtractCandidate] dans un index inversé : chaque mot
 * normalisé pointe vers la liste d'extraits où il apparaît comme mot cible.
 *
 * Usage typique :
 * ```
 * val index = CorpusIndex()
 * index.addAll(candidates)
 * val extraits = index.find("abnégation")   // List<ExtractCandidate>
 * val nb = index.count()                     // Int
 * ```
 */
class CorpusIndex {

    /** Index inversé : mot normalisé → liste d'extraits */
    private val index = mutableMapOf<String, MutableList<ExtractCandidate>>()

    /** Nombre total d'extraits indexés */
    fun count(): Int = index.values.sumOf { it.size }

    /** Nombre de mots distincts indexés */
    fun distinctWordCount(): Int = index.size

    /**
     * Ajoute un [candidate] à l'index.
     * Le mot cible est normalisé (minuscules, sans accents) avant indexation.
     */
    fun add(candidate: ExtractCandidate) {
        val key = normalizeKey(candidate.targetWord)
        index.getOrPut(key) { mutableListOf() }.add(candidate)
    }

    /**
     * Ajoute une liste de [candidates] à l'index.
     */
    fun addAll(candidates: List<ExtractCandidate>) {
        candidates.forEach { add(it) }
        Log.d(TAG, "Index: ${candidates.size} candidats ajoutés. Total: ${count()} extraits, ${distinctWordCount()} mots distincts")
    }

    /**
     * Recherche les extraits pour un mot donné (normalisé automatiquement).
     *
     * @return Liste d'[ExtractCandidate] correspondants, ou liste vide si aucun.
     */
    fun find(word: String): List<ExtractCandidate> {
        val key = normalizeKey(word)
        return index[key]?.toList() ?: emptyList()
    }

    /**
     * Recherche les extraits pour un mot donné, avec une limite optionnelle.
     *
     * @param maxResults Nombre maximum d'extraits à retourner.
     */
    fun find(word: String, maxResults: Int): List<ExtractCandidate> {
        return find(word).take(maxResults)
    }

    /**
     * Recherche les extraits pour un mot donné, triés par pertinence décroissante.
     */
    fun findBest(word: String, maxResults: Int = 10): List<ExtractCandidate> {
        return find(word)
            .sortedByDescending { it.relevanceScore }
            .take(maxResults)
    }

    /**
     * Vérifie si un mot est présent dans l'index.
     */
    fun contains(word: String): Boolean {
        return normalizeKey(word) in index
    }

    /**
     * Retourne tous les mots indexés.
     */
    fun allWords(): Set<String> = index.keys.toSet()

    /**
     * Vide complètement l'index.
     */
    fun clear() {
        index.clear()
        Log.d(TAG, "Index vidé")
    }

    /**
     * Construit l'index à partir d'une map [candidatesByWord] (mot → extraits).
     * Utile pour le chargement initial ou après désérialisation.
     */
    fun buildFrom(candidatesByWord: Map<String, List<ExtractCandidate>>) {
        clear()
        candidatesByWord.forEach { (word, candidates) ->
            val key = normalizeKey(word)
            index.getOrPut(key) { mutableListOf() }.addAll(candidates)
        }
        Log.d(TAG, "Index construit depuis map: ${count()} extraits, ${distinctWordCount()} mots")
    }

    private fun normalizeKey(value: String): String =
        Normalizer.normalize(value.lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

    companion object {
        private const val TAG = "CORPUS_INDEX"
    }
}
