package com.example.lexicaandroid2.data.corpus

import android.util.Log
import java.text.Normalizer
import java.util.UUID

/**
 * Service de segmentation et d'analyse de corpus textuels en [ExtractCandidate].
 *
 * Découpe un texte brut en paragraphes, extrait les mots intéressants,
 * calcule le score de qualité du contexte et estime le registre et la difficulté.
 */
class CorpusParser(
    private val minContextLength: Int = 50,
    private val maxContextLength: Int = 1000,
    private val targetWordMinLength: Int = 7
) {

    companion object {
        private const val TAG = "CORPUS_PARSER"

        // Liste de mots outils français courants à exclure (stop list)
        private val STOP_WORDS = setOf(
            "le", "la", "les", "un", "une", "des", "ce", "cette", "ces", "mon", "ton", "son",
            "ma", "ta", "sa", "mes", "tes", "ses", "notre", "votre", "leur", "nos", "vos", "leurs",
            "qui", "que", "quoi", "dont", "où", "pour", "dans", "avec", "sans", "sous", "sur",
            "dans", "vers", "chez", "mais", "donc", "or", "ni", "car", "plus", "moins", "assez",
            "trop", "très", "tout", "tous", "toute", "toutes", "rien", "aucun", "aucune", "personne",
            "quelque", "quelques", "plusieurs", "certains", "certaines", "autre", "autres",
            "être", "avoir", "faire", "dire", "pouvoir", "vouloir", "devoir", "savoir", "venir",
            "aller", "suis", "es", "est", "sommes", "êtes", "sont", "étais", "était", "étions",
            "étiez", "étaient", "serai", "sera", "serons", "serez", "seront", "serais", "serait",
            "serions", "seriez", "seraient", "ai", "as", "a", "avons", "avez", "ont", "avais",
            "avait", "avions", "aviez", "avaient", "aurai", "aura", "aurons", "aurez", "auront",
            "aurais", "aurait", "aurions", "auriez", "auraient", "faire", "fait", "faisons",
            "faites", "font", "dis", "dit", "disons", "dites", "disent", "peux", "peut", "pouvons",
            "pouvez", "peuvent", "veux", "veut", "voulons", "voulez", "veulent", "dois", "doit",
            "devons", "devez", "doivent", "sais", "sait", "savons", "savez", "savent", "viens",
            "vient", "venons", "venez", "viennent", "vais", "vas", "va", "allons", "allez", "vont",
            "comme", "quand", "depuis", "pendant", "durant", "alors", "après", "avant", "ensuite"
        )
    }

    /**
     * Parse le texte brut en extraits pour une source donnée.
     */
    fun parse(source: CorpusSource, rawText: String): List<ExtractCandidate> {
        if (rawText.isBlank()) return emptyList()

        val candidates = mutableListOf<ExtractCandidate>()
        // Découpage par paragraphes (retours à la ligne multiples ou simples longs)
        val paragraphs = rawText.split(Regex("(\\r?\\n){2,}"))
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.length in minContextLength..maxContextLength }

        paragraphs.forEachIndexed { index, content ->
            val suggested = detectInterestingWords(content)
            if (suggested.isNotEmpty()) {
                val wordCount = content.split(Regex("\\s+")).size
                val difficulty = estimateDifficulty(content, wordCount)
                val registerTags = estimateRegisterTags(content)
                val quality = calculateContextQuality(content)

                if (quality >= ExtractCandidate.MIN_CONTEXT_QUALITY) {
                    candidates.add(
                        ExtractCandidate(
                            id = UUID.randomUUID().toString(),
                            sourceId = source.id,
                            content = content,
                            startPosition = 0, // Optionnel, mis à 0 par simplicité
                            endPosition = content.length,
                            wordCount = wordCount,
                            suggestedWords = suggested,
                            domainTags = source.domainTags,
                            registerTags = registerTags,
                            difficulty = difficulty,
                            contextQuality = quality,
                            hasCompleteSource = source.contentUrl != null
                        )
                    )
                }
            }
        }

        Log.d(TAG, "Parsed ${candidates.size} candidates from source ${source.title}")
        return candidates
    }

    /**
     * Détecte les mots intéressants à apprendre.
     * Exclut les mots outils et les mots trop courts.
     */
    private fun detectInterestingWords(text: String): List<String> {
        val cleanWords = text.split(Regex("[\\s,;:.!?«»()\\[\\]\"'\\-]+"))
            .map { it.lowercase().trim() }
            .filter { it.length >= targetWordMinLength && it !in STOP_WORDS }
            .distinct()

        // Filtrer par normalisation pour éviter les caractères non alphabétiques
        return cleanWords.filter { word ->
            val normalized = Normalizer.normalize(word, Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "")
            normalized.all { it.isLetter() }
        }.take(5) // Limiter à 5 mots suggérés par extrait
    }

    /**
     * Estime la difficulté à partir de la longueur moyenne des mots et du nombre total de mots.
     */
    private fun estimateDifficulty(text: String, wordCount: Int): String {
        val averageWordLength = text.length.toFloat() / wordCount.toFloat()
        return when {
            averageWordLength > 6.0f || wordCount > 40 -> "avancé"
            averageWordLength < 5.0f && wordCount < 20 -> "facile"
            else -> "moyen"
        }
    }

    /**
     * Estime le registre en recherchant des marqueurs lexicaux.
     */
    private fun estimateRegisterTags(text: String): List<String> {
        val textLower = text.lowercase()
        val tags = mutableListOf<String>()

        // Exemples de marqueurs
        val soutenuMarkers = listOf("certes", "néanmoins", "toutefois", "ainsi", "or", "conséquent", "jadis", "lorsque")
        val techniqueMarkers = listOf("système", "méthode", "structure", "analyse", "fonction", "concept", "théorie", "données")

        if (soutenuMarkers.any { it in textLower }) {
            tags.add("soutenu")
        }
        if (techniqueMarkers.any { it in textLower }) {
            tags.add("technique")
        }
        if (tags.isEmpty()) {
            tags.add("courant")
        }
        return tags
    }

    /**
     * Calcule la qualité du contexte d'usage.
     */
    private fun calculateContextQuality(text: String): Float {
        var score = 0.5f // Score de base

        // 1. Longueur optimale
        if (text.length in 150..500) {
            score += 0.2f
        }

        // 2. Ponctuation correcte (phrase complète)
        if (text.first().isUpperCase()) {
            score += 0.1f
        }
        if (text.last() in setOf('.', '!', '?')) {
            score += 0.2f
        }

        // 3. Pénalité pour bruits
        if (text.contains("[") || text.contains("]") || text.contains("*")) {
            score -= 0.2f
        }

        return score.coerceIn(0.0f, 1.0f)
    }
}
