package com.example.lexicaandroid2.presentation.review.challenge

import java.text.Normalizer

object KeywordExtractor {
    private const val DEFAULT_KEYWORD_COUNT = 2

    private val STOPWORDS_FR = setOf(
        "le", "la", "les", "de", "du", "des", "un", "une", "et", "ou",
        "est", "qui", "que", "dans", "sur", "avec", "pour", "par", "au",
        "aux", "ce", "se", "sa", "son", "ses", "en", "il", "elle", "on",
        "ne", "pas", "plus", "très", "aussi", "dont", "où", "car", "ni",
        "est", "etre", "avoir", "faire", "aller", "venir", "pouvoir", "devoir",
        "vouloir", "savoir", "falloir", "sembler", "paraître", "rester", "demeurer"
    ).map(::normalizeToken).toSet()

    private val KEYWORD_BOUNDARY_MARKERS = setOf(
        "qui", "que", "dont", "avec", "sans", "pour", "par", "dans", "sur", "sous",
        "chez", "entre", "vers", "comme", "mais", "ou", "et"
    ).map(::normalizeToken).toSet()

    private val GENERIC_DEFINITION_WORDS = setOf(
        "chose", "choses", "fait", "faits", "action", "actions", "etat", "etat",
        "maniere", "type", "forme", "personne", "personnes", "objet", "objets",
        "partie", "parties", "ensemble", "element", "elements"
    ).map(::normalizeToken).toSet()

    private data class KeywordCandidate(
        val token: String,
        val firstIndex: Int,
        val occurrenceCount: Int,
        val score: Int
    )

    /**
     * Extrait les mots-clés importants du texte
     * Stratégie :
     * - peu de mots-clés (par défaut 2)
     * - priorité au groupe nominal de tête de définition
     * - bonus léger pour répétition et spécificité lexicale
     */
    fun extractKeywords(text: String, topN: Int = DEFAULT_KEYWORD_COUNT): List<String> {
        val tokens = tokenize(text)
        if (tokens.isEmpty()) return emptyList()

        val boundaryIndex = tokens.indexOfFirst { it in KEYWORD_BOUNDARY_MARKERS }
            .let { if (it == -1) tokens.size else it }

        val candidatesByToken = mutableMapOf<String, KeywordCandidate>()
        tokens.forEachIndexed { index, token ->
            if (!isKeywordCandidate(token)) return@forEachIndexed

            val existing = candidatesByToken[token]
            val occurrenceCount = (existing?.occurrenceCount ?: 0) + 1
            val firstIndex = existing?.firstIndex ?: index
            candidatesByToken[token] = KeywordCandidate(
                token = token,
                firstIndex = firstIndex,
                occurrenceCount = occurrenceCount,
                score = computeCandidateScore(
                    token = token,
                    firstIndex = firstIndex,
                    occurrenceCount = occurrenceCount,
                    boundaryIndex = boundaryIndex
                )
            )
        }

        return candidatesByToken.values
            .sortedWith(
                compareByDescending<KeywordCandidate> { it.score }
                    .thenBy { it.firstIndex }
                    .thenByDescending { it.occurrenceCount }
                    .thenByDescending { it.token.length }
                    .thenBy { it.token }
            )
            .map { it.token }
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
     * Score lexical de secours : combinaison d'un recouvrement tokenisé et
     * d'une similarité de trigrammes de caractères.
     */
    fun lexicalFallbackScore(userInput: String, expectedDef: String): Float {
        val normalizedUser = normalizeForFallback(userInput)
        val normalizedExpected = normalizeForFallback(expectedDef)
        if (normalizedUser.isBlank() || normalizedExpected.isBlank()) return 0f

        val tokenScore = jaccardScore(userInput, expectedDef)
        val trigramScore = charNGramDiceScore(normalizedUser, normalizedExpected, n = 3)
        return (tokenScore * 0.55f) + (trigramScore * 0.45f)
    }

    /**
     * Calcule les mots-clés trouvés et manquants
     */
    fun analyzeKeywords(userInput: String, expectedDef: String, topN: Int = DEFAULT_KEYWORD_COUNT): Pair<List<String>, List<String>> {
        val expectedKeywords = extractKeywords(expectedDef, topN)
        val userTokens = tokenize(userInput).toSet()
        
        val found = expectedKeywords.filter { it in userTokens }
        val missing = expectedKeywords - found.toSet()
        
        return found to missing.toList()
    }

    private fun isKeywordCandidate(token: String): Boolean {
        return token.length >= 4 && token !in STOPWORDS_FR && token !in GENERIC_DEFINITION_WORDS
    }

    private fun computeCandidateScore(
        token: String,
        firstIndex: Int,
        occurrenceCount: Int,
        boundaryIndex: Int
    ): Int {
        val headPhraseBonus = if (firstIndex < boundaryIndex) 50 else 0
        val earlyPositionBonus = (30 - (firstIndex * 3)).coerceAtLeast(0)
        val repetitionBonus = (occurrenceCount - 1).coerceAtLeast(0) * 12
        val lengthBonus = (token.length - 3).coerceIn(1, 4)
        return headPhraseBonus + earlyPositionBonus + repetitionBonus + lengthBonus
    }

    private fun normalizeForFallback(text: String): String {
        return normalizeToken(text)
            .replace(Regex("[^a-z]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    private fun charNGramDiceScore(a: String, b: String, n: Int): Float {
        val gramsA = charNGrams(a, n)
        val gramsB = charNGrams(b, n)
        if (gramsA.isEmpty() || gramsB.isEmpty()) return 0f

        val countsB = gramsB.groupingBy { it }.eachCount().toMutableMap()
        var overlap = 0
        for (gram in gramsA) {
            val count = countsB[gram] ?: 0
            if (count > 0) {
                overlap += 1
                countsB[gram] = count - 1
            }
        }

        return (2f * overlap) / (gramsA.size + gramsB.size).toFloat()
    }

    private fun charNGrams(text: String, n: Int): List<String> {
        val padded = "  $text  "
        if (padded.length < n) return emptyList()
        return (0..padded.length - n).map { index -> padded.substring(index, index + n) }
    }
}
