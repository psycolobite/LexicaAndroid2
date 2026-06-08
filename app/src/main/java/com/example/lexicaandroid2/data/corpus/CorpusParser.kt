package com.example.lexicaandroid2.data.corpus

import android.util.Log
import java.text.Normalizer
import java.util.UUID

/**
 * Service de parsing de corpus textuels bruts en une liste d'[ExtractCandidate].
 *
 * Le [CorpusParser] prend un texte brut (ex: une œuvre littéraire téléchargée) et
 * une liste de mots cibles. Il découpe le texte en phrases, puis identifie chaque
 * phrase contenant un mot cible pour en faire un candidat d'extrait.
 *
 * Usage typique :
 * ```
 * val parser = CorpusParser()
 * val candidates = parser.parse(
 *     rawText = "...",
 *     targetWords = listOf("abnégation", "acrimonie"),
 *     source = corpusSource
 * )
 * ```
 */
class CorpusParser(
    /** Longueur maximale souhaitée pour une phrase extraite (en caractères) */
    private val maxSentenceLength: Int = 500,

    /** Score de pertinence minimal pour conserver un candidat */
    private val minRelevanceScore: Float = 0.3f
) {
    /**
     * Parse le texte brut [rawText] à la recherche de [targetWords],
     * en associant les métadonnées de la [source].
     *
     * @return Une liste d'[ExtractCandidate] triés par score de pertinence décroissant.
     */
    fun parse(
        rawText: String,
        targetWords: List<String>,
        source: CorpusSource
    ): List<ExtractCandidate> {
        if (rawText.isBlank() || targetWords.isEmpty()) return emptyList()

        val normalizedWords = targetWords.map { normalize(it) }.toSet()
        val sentences = splitSentences(rawText)

        Log.d(TAG, "Parsing ${sentences.size} phrases pour ${normalizedWords.size} mots cibles (source: ${source.id})")

        val candidates = mutableListOf<ExtractCandidate>()

        sentences.forEachIndexed { index, sentence ->
            if (sentence.isBlank()) return@forEachIndexed

            val sentenceNormalized = normalize(sentence.lowercase())
            val words = sentenceNormalized.split(Regex("\\s+"))

            for (word in words) {
                val normalized = normalize(word)
                if (normalized in normalizedWords) {
                    val candidate = buildCandidate(
                        targetWord = word,
                        sentence = sentence,
                        position = index,
                        source = source
                    )
                    if (candidate.relevanceScore >= minRelevanceScore) {
                        candidates.add(candidate)
                    }
                    break // une seule correspondance par phrase
                }
            }
        }

        Log.d(TAG, "${candidates.size} candidats extraits de ${source.id}")

        return candidates.sortedByDescending { it.relevanceScore }
    }


    // ── Construction du candidat ─────────────────────────────────────────────

    private fun buildCandidate(
        targetWord: String,
        sentence: String,
        position: Int,
        source: CorpusSource
    ): ExtractCandidate {
        val cleaned = cleanSentence(sentence)
        val relevance = computeRelevance(targetWord, cleaned)

        val truncated = if (cleaned.length > maxSentenceLength) {
            truncateAroundWord(cleaned, targetWord, maxSentenceLength)
        } else {
            null
        }

        return ExtractCandidate(
            id = UUID.randomUUID().toString(),
            targetWord = targetWord,
            sentence = cleaned,
            sourceId = source.id,
            sourceTitle = source.title,
            sourceAuthor = source.author,
            domains = source.domains,
            positionInSource = position,
            relevanceScore = relevance,
            truncatedContext = truncated,
            sentenceLength = cleaned.length
        )
    }

    // ── Découpage en phrases ─────────────────────────────────────────────────

    private fun splitSentences(text: String): List<String> {
        val raw = text.split(Regex("(?<=[.!?])\\s+(?=[A-ZÀ-Ö])"))
        return raw
            .map { it.trim() }
            .filter { sentence ->
                val wordCount = sentence.split(Regex("\\s+")).size
                wordCount in 5..120
            }
    }

    // ── Nettoyage ────────────────────────────────────────────────────────────

    private fun cleanSentence(sentence: String): String {
        return sentence
            .replace('\n', ' ')
            .replace('\r', ' ')
            .replace(Regex("\\s{2,}"), " ")
            .trim()
    }

    // ── Score de pertinence ──────────────────────────────────────────────────

    private fun computeRelevance(targetWord: String, sentence: String): Float {
        var score = 0.5f
        val len = sentence.length
        if (len in 30..200) score += 0.2f
        else if (len in 200..350) score += 0.1f
        if (sentence.contains('[') || sentence.contains(']')) score -= 0.1f
        if (sentence.contains('*') || sentence.contains('_')) score -= 0.1f
        if (sentence.contains("***")) score -= 0.2f
        val targetIndex = sentence.indexOf(targetWord, ignoreCase = true)
        if (targetIndex >= 0) {
            val ratio = targetIndex.toFloat() / len
            if (ratio in 0.1f..0.7f) score += 0.1f
        }
        return score.coerceIn(0f, 1f)
    }

    // ── Troncation contextuelle ──────────────────────────────────────────────

    private fun truncateAroundWord(
        sentence: String,
        targetWord: String,
        maxLen: Int
    ): String {
        val index = sentence.indexOf(targetWord, ignoreCase = true)
        if (index < 0) return sentence.take(maxLen) + "…"
        val half = maxLen / 2
        val start = (index - half).coerceAtLeast(0)
        val end = (index + targetWord.length + half).coerceAtMost(sentence.length)
        val prefix = if (start > 0) "…" else ""
        val suffix = if (end < sentence.length) "…" else ""
        return prefix + sentence.substring(start, end).trim() + suffix
    }

    // ── Normalisation ────────────────────────────────────────────────────────

    private fun normalize(value: String): String =
        Normalizer.normalize(value.lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace(Regex("[^a-z]"), "")

    companion object {
        private const val TAG = "CORPUS_PARSER"
    }
}
