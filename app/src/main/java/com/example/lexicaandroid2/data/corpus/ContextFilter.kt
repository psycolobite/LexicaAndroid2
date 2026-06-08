package com.example.lexicaandroid2.data.corpus

/**
 * Résultat du filtrage d'une occurrence : un extrait filtré avec un score de qualité.
 *
 * @property occurrence L'occurrence originale
 * @property qualityScore Score de qualité du contexte (0.0-1.0)
 * @property reason Explication du score (pour debug)
 */
data class FilteredExtract(
    val occurrence: Occurrence,
    val qualityScore: Float,
    val reason: String
)

/**
 * Filtre les occurrences de mots pour ne retenir que les contextes
 * de qualité suffisante pour l'apprentissage.
 *
 * Critères de qualité :
 * - Le mot est utilisé dans une phrase complète
 * - Le contexte permet de comprendre le sens du mot
 * - Pas de bruit (citations tronquées, listes, titres)
 * - Bonus si le mot apparaît avec d'autres mots intéressants à proximité
 */
object ContextFilter {

    /** Score minimum pour qu'un extrait soit retenu */
    private const val MINIMUM_QUALITY_SCORE = 0.4f

    /** Longueur minimale du contexte en caractères */
    private const val MIN_CONTEXT_LENGTH = 30

    /** Longueur maximale du contexte en caractères */
    private const val MAX_CONTEXT_LENGTH = 800

    /**
     * Filtre une liste d'occurrences et ne conserve que celles
     * dont la qualité de contexte est suffisante.
     *
     * @param occurrences Liste des occurrences à filtrer
     * @return Liste des extraits filtrés, triés par score décroissant
     */
    fun filterOccurrences(occurrences: List<Occurrence>): List<FilteredExtract> {
        return occurrences
            .map { evaluateQuality(it) }
            .filter { it.qualityScore >= MINIMUM_QUALITY_SCORE }
            .sortedByDescending { it.qualityScore }
    }

    /**
     * Évalue la qualité du contexte d'une occurrence.
     *
     * @param occurrence L'occurrence à évaluer
     * @return Un FilteredExtract avec le score calculé
     */
    private fun evaluateQuality(occurrence: Occurrence): FilteredExtract {
        val context = occurrence.context
        val word = occurrence.word
        val scores = mutableListOf<Pair<Float, String>>()

        // Critère 1 : Longueur du contexte (ni trop court, ni trop long)
        val lengthScore = when {
            context.length < MIN_CONTEXT_LENGTH -> 0.2f
            context.length > MAX_CONTEXT_LENGTH -> 0.5f
            context.length in 100..400 -> 1.0f
            else -> 0.8f
        }
        scores.add(lengthScore to "longueur_contexte")

        // Critère 2 : Phrase complète (commence par majuscule, finit par un point)
        val hasCompleteSentence = context.isNotEmpty() &&
                context.first().isUpperCase() &&
                context.last() in setOf('.', '!', '?')
        val sentenceScore = if (hasCompleteSentence) 1.0f else 0.3f
        scores.add(sentenceScore to "phrase_complete")

        // Critère 3 : Le mot est utilisé dans un contexte qui permet de comprendre son sens
        val clarityScore = evaluateContextClarity(context, word)
        scores.add(clarityScore to "clarte_contexte")

        // Critère 4 : Absence de bruit
        val noiseScore = evaluateNoise(context)
        scores.add(noiseScore to "absence_bruit")

        // Critère 5 : Bonus pour présence d'autres mots intéressants
        val bonusScore = evaluateInterestingWordsProximity(context, word)
        scores.add(bonusScore to "mots_interessants_proches")

        // Calcul du score final (moyenne pondérée)
        val weights = mapOf(
            "longueur_contexte" to 0.15f,
            "phrase_complete" to 0.25f,
            "clarte_contexte" to 0.30f,
            "absence_bruit" to 0.20f,
            "mots_interessants_proches" to 0.10f
        )

        val finalScore = scores.fold(0.0f) { acc, (score, reason) ->
            acc + score * (weights[reason] ?: 0.0f)
        }

        // Générer la raison du score
        val reasons = scores
            .filter { (score, _) -> score < 0.5f }
            .joinToString(", ") { (_, reason) -> reason }

        val reasonText = if (reasons.isEmpty()) {
            "contexte de bonne qualité"
        } else {
            "qualité réduite : $reasons"
        }

        return FilteredExtract(
            occurrence = occurrence,
            qualityScore = finalScore.coerceIn(0.0f, 1.0f),
            reason = reasonText
        )
    }

    /**
     * Évalue si le contexte permet de comprendre le sens du mot.
     *
     * Indices de clarté :
     * - Présence de définitions implicites ("c'est-à-dire", "autrement dit")
     * - Présence de synonymes à proximité
     * - Présence d'exemples ou d'illustrations
     * - Le mot est utilisé dans une phrase déclarative complète
     */
    private fun evaluateContextClarity(context: String, word: String): Float {
        var score = 0.5f // score de base

        // Indices de définition
        val definitionMarkers = listOf(
            "c'est-à-dire", "autrement dit", "c'est", "cela signifie",
            "désigne", "correspond à", "se définit comme", "s'entend de"
        )
        val hasDefinition = definitionMarkers.any { it in context.lowercase() }
        if (hasDefinition) score += 0.3f

        // Présence de synonymes (mots de même famille)
        val synonymIndicators = listOf("ou", "c'est-à-dire", "soit", "comme")
        val hasSynonym = synonymIndicators.any { it in context.lowercase() }
        if (hasSynonym) score += 0.1f

        // Le mot est utilisé dans une phrase qui l'explique
        val wordLower = word.lowercase()
        val wordIndex = context.lowercase().indexOf(wordLower)
        if (wordIndex >= 0) {
            // Vérifier que le mot n'est pas en début de phrase (moins de contexte)
            val beforeWord = context.substring(maxOf(0, wordIndex - 40), wordIndex)
            if (beforeWord.length > 10) score += 0.1f
        }

        return score.coerceIn(0.0f, 1.0f)
    }

    /**
     * Évalue le niveau de bruit dans le contexte.
     *
     * Indices de bruit :
     * - Présence de listes à puces ou numérotées
     * - Citations tronquées
     * - Texte en majuscules (titres)
     * - Caractères spéciaux excessifs
     */
    private fun evaluateNoise(context: String): Float {
        var score = 1.0f

        // Détection de listes
        val listPatterns = listOf(
            Regex("^\\s*[-•*]\\s"),           // listes à puces
            Regex("^\\s*\\d+\\.\\s"),          // listes numérotées
            Regex("^\\s*[a-z]\\)\\s")          // listes alphabétiques
        )
        val hasList = listPatterns.any { it.containsMatchIn(context) }
        if (hasList) score -= 0.3f

        // Détection de texte tout en majuscules (titres)
        val upperRatio = context.count { it.isUpperCase() }.toFloat() / context.length.coerceAtLeast(1)
        if (upperRatio > 0.5f) score -= 0.2f

        // Détection de caractères spéciaux excessifs
        val specialChars = context.count { it in setOf('@', '#', '$', '%', '^', '&', '*', '=', '+', '\\', '|', '~', '`') }
        if (specialChars > 3) score -= 0.2f

        // Détection de citations tronquées
        val quoteCount = context.count { it == '"' || it == '«' || it == '»' }
        if (quoteCount % 2 != 0) score -= 0.2f // nombre impair de guillemets

        return score.coerceIn(0.0f, 1.0f)
    }

    /**
     * Évalue la présence d'autres mots intéressants à proximité.
     *
     * Bonus si le contexte contient d'autres mots longs ou rares
     * qui pourraient également intéresser l'utilisateur.
     */
    private fun evaluateInterestingWordsProximity(context: String, word: String): Float {
        // Compter les mots longs (> 7 caractères) comme indicateurs
        // de vocabulaire intéressant
        val longWords = Regex("[a-zA-ZÀ-ÿ]{7,}")
            .findAll(context)
            .map { it.value.lowercase() }
            .filter { it != word.lowercase() }
            .toList()

        return when {
            longWords.size >= 5 -> 1.0f  // beaucoup de vocabulaire intéressant
            longWords.size >= 3 -> 0.7f  // quelques mots intéressants
            longWords.size >= 1 -> 0.4f  // au moins un mot intéressant
            else -> 0.0f                  // pas d'autre vocabulaire riche
        }
    }

    /**
     * Filtre les extraits pour n'avoir qu'un nombre limité par mot.
     * Évite la redondance quand un mot apparaît dans plusieurs contextes similaires.
     *
     * @param extracts Liste des extraits filtrés
     * @param maxPerWord Nombre maximum d'extraits par mot
     * @return Liste dédupliquée
     */
    fun deduplicateByWord(extracts: List<FilteredExtract>, maxPerWord: Int = 3): List<FilteredExtract> {
        val wordCount = mutableMapOf<String, Int>()
        val result = mutableListOf<FilteredExtract>()

        for (extract in extracts.sortedByDescending { it.qualityScore }) {
            val word = extract.occurrence.word.lowercase()
            val count = wordCount[word] ?: 0
            if (count < maxPerWord) {
                result.add(extract)
                wordCount[word] = count + 1
            }
        }

        return result
    }

    /**
     * Filtre les extraits pour n'avoir qu'un nombre limité par source.
     * Évite qu'une seule source ne domine les résultats.
     *
     * @param extracts Liste des extraits filtrés
     * @param maxPerSource Nombre maximum d'extraits par source
     * @return Liste diversifiée
     */
    fun diversifyBySource(extracts: List<FilteredExtract>, maxPerSource: Int = 5): List<FilteredExtract> {
        val sourceCount = mutableMapOf<String, Int>()
        val result = mutableListOf<FilteredExtract>()

        for (extract in extracts.sortedByDescending { it.qualityScore }) {
            val sourceId = extract.occurrence.sourceId
            val count = sourceCount[sourceId] ?: 0
            if (count < maxPerSource) {
                result.add(extract)
                sourceCount[sourceId] = count + 1
            }
        }

        return result
    }
}
