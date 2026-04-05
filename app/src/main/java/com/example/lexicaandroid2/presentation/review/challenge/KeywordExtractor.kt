package com.example.lexicaandroid2.presentation.review.challenge

import java.text.Normalizer

object KeywordExtractor {
    private val STOPWORDS_FR = setOf(
        "le", "la", "les", "de", "du", "des", "un", "une", "et", "ou",
        "est", "qui", "que", "dans", "sur", "avec", "pour", "par", "au",
        "aux", "ce", "se", "sa", "son", "ses", "en", "il", "elle", "on",
        "ne", "pas", "plus", "très", "aussi", "dont", "où", "car", "ni",
        "est", "etre", "avoir", "faire", "aller", "venir", "pouvoir", "devoir",
        "vouloir", "savoir", "falloir", "sembler", "paraître", "rester", "demeurer"
    ).map(::normalizeToken).toSet()

    /**
     * Extrait les mots-clés importants du texte
     * Stratégie : filtre les stopwords et trie par longueur (les mots longs = plus spécifiques)
     */
    fun extractKeywords(text: String, topN: Int = 5): List<String> {
        val tokens = tokenize(text)
        return tokens
            .filter { it.length >= 4 && it !in STOPWORDS_FR }
            .sortedByDescending { it.length }
            .distinctBy { it }
            .take(topN)
    }

    /**
     * Tokenise le texte : normalise, supprime diacritiques, split sur les espaces
     */
    fun tokenize(text: String): List<String> =
        normalizeToken(text)
            .replace(Regex("[^a-z]+"), " ")
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }

    private fun normalizeToken(text: String): String =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

    /**
     * Calcule la similarité Jaccard entre deux textes
     * Retourne un score entre 0 et 1
     */
    fun jaccardScore(userInput: String, expectedDef: String): Float {
        val userTokens = tokenize(userInput).toSet()
        val defTokens = tokenize(expectedDef).filter { it !in STOPWORDS_FR }.toSet()
        
        if (defTokens.isEmpty()) {
            return if (userTokens.isNotEmpty()) 1f else 0f
        }
        
        val intersection = userTokens.intersect(defTokens).size
        val union = userTokens.union(defTokens).size
        
        return if (union == 0) 0f else intersection.toFloat() / union.toFloat()
    }

    /**
     * Calcule les mots-clés trouvés et manquants
     */
    fun analyzeKeywords(userInput: String, expectedDef: String, topN: Int = 5): Pair<List<String>, List<String>> {
        val expectedKeywords = extractKeywords(expectedDef, topN)
        val userTokens = tokenize(userInput).toSet()
        
        val found = expectedKeywords.filter { it in userTokens }
        val missing = expectedKeywords - found.toSet()
        
        return found to missing.toList()
    }
}
