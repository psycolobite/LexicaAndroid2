package com.example.lexicaandroid2.data.corpus

/**
 * Modèle représentant une occurrence d'un mot dans une source.
 *
 * @property word Le mot recherché
 * @property sourceId Identifiant de la source contenant le mot
 * @property context Contexte textuel (phrase ou paragraphe contenant le mot)
 * @property position Position du mot dans la source (en caractères)
 * @property sentenceCount Nombre de phrases autour du mot
 */
data class Occurrence(
    val word: String,
    val sourceId: String,
    val context: String,
    val position: Int,
    val sentenceCount: Int
)

/**
 * Interface de recherche d'occurrences de mots cibles dans les corpus.
 *
 * Deux modes de recherche :
 * - Par liste de mots cibles (recherche directe)
 * - Par domaine (recherche de tous les mots pertinents pour un domaine)
 */
interface OccurrenceSearcher {

    /**
     * Recherche les occurrences d'une liste de mots cibles dans un corpus.
     *
     * @param words Liste des mots cibles à rechercher
     * @param corpus Liste des sources de corpus à parcourir
     * @return Liste des occurrences trouvées
     */
    suspend fun search(words: List<TargetWord>, corpus: List<CorpusSource>): List<Occurrence>

    /**
     * Recherche les occurrences par domaine thématique.
     *
     * @param domainIds Identifiants des domaines à explorer
     * @param corpus Liste des sources de corpus à parcourir
     * @return Liste des occurrences trouvées
     */
    suspend fun searchByDomain(domainIds: List<String>, corpus: List<CorpusSource>): List<Occurrence>
}

/**
 * Implémentation simple de [OccurrenceSearcher] basée sur une recherche textuelle.
 *
 * Stratégie :
 * - Recherche textuelle insensible à la casse
 * - Retourne le contexte : phrase contenant le mot + une phrase avant/après
 * - Limite : max 5 occurrences par mot pour éviter la redondance
 * - Ignore les mots trop courts (< 3 caractères) ou trop fréquents (mots outils)
 */
class SimpleOccurrenceSearcher : OccurrenceSearcher {

    /** Mots outils français à ignorer */
    private val stopWords = setOf(
        "le", "la", "les", "de", "des", "du", "un", "une", "et", "est",
        "a", "à", "dans", "par", "pour", "sur", "avec", "sans", "que",
        "qui", "quoi", "dont", "où", "il", "elle", "on", "nous", "vous",
        "ils", "elles", "ce", "cet", "cette", "ces", "mon", "ton", "son",
        "ma", "ta", "sa", "mes", "tes", "ses", "notre", "votre", "leur",
        "au", "aux", "en", "y", "ne", "pas", "plus", "très", "bien",
        "fait", "faire", "être", "avoir", "dire", "voir", "savoir",
        "pouvoir", "vouloir", "devoir", "falloir", "aller", "venir",
        "se", "me", "te", "lui", "leur", "eux", "elles"
    )

    companion object {
        private const val MAX_OCCURRENCES_PER_WORD = 5
        private const val MIN_WORD_LENGTH = 3
    }

    override suspend fun search(
        words: List<TargetWord>,
        corpus: List<CorpusSource>
    ): List<Occurrence> {
        val results = mutableListOf<Occurrence>()
        val wordCount = mutableMapOf<String, Int>()

        for (targetWord in words) {
            val word = targetWord.word.lowercase()

            // Ignorer les mots trop courts ou outils
            if (word.length < MIN_WORD_LENGTH || word in stopWords) continue

            // Vérifier la limite par mot
            if ((wordCount[word] ?: 0) >= MAX_OCCURRENCES_PER_WORD) continue

            for (source in corpus) {
                if ((wordCount[word] ?: 0) >= MAX_OCCURRENCES_PER_WORD) break

                val occurrences = findOccurrencesInSource(word, source)
                for (occ in occurrences) {
                    if ((wordCount[word] ?: 0) >= MAX_OCCURRENCES_PER_WORD) break
                    results.add(occ)
                    wordCount[word] = (wordCount[word] ?: 0) + 1
                }
            }
        }

        return results
    }

    override suspend fun searchByDomain(
        domainIds: List<String>,
        corpus: List<CorpusSource>
    ): List<Occurrence> {
        // Filtrer le corpus par domaines
        val filteredCorpus = corpus.filter { source ->
            source.domainTags.any { it in domainIds }
        }

        // Extraire tous les mots intéressants des sources filtrées
        val allWords = mutableSetOf<String>()
        for (source in filteredCorpus) {
            // Extraire les mots longs (> 6 caractères) comme candidats
            val words = extractLongWords(source.content)
            allWords.addAll(words)
        }

        // Rechercher les occurrences des mots extraits
        val results = mutableListOf<Occurrence>()
        val wordCount = mutableMapOf<String, Int>()

        for (word in allWords.take(100)) { // limite de 100 mots pour la performance
            if ((wordCount[word] ?: 0) >= MAX_OCCURRENCES_PER_WORD) continue

            for (source in filteredCorpus) {
                if ((wordCount[word] ?: 0) >= MAX_OCCURRENCES_PER_WORD) break

                val occurrences = findOccurrencesInSource(word, source)
                for (occ in occurrences) {
                    if ((wordCount[word] ?: 0) >= MAX_OCCURRENCES_PER_WORD) break
                    results.add(occ)
                    wordCount[word] = (wordCount[word] ?: 0) + 1
                }
            }
        }

        return results
    }

    /**
     * Recherche les occurrences d'un mot dans une source spécifique.
     */
    private fun findOccurrencesInSource(word: String, source: CorpusSource): List<Occurrence> {
        val occurrences = mutableListOf<Occurrence>()
        val content = source.content
        val lowerContent = content.lowercase()
        val lowerWord = word.lowercase()

        var searchFrom = 0
        while (true) {
            val index = lowerContent.indexOf(lowerWord, searchFrom)
            if (index == -1) break

            // Extraire le contexte : phrase contenant le mot
            val context = extractContext(content, index)

            occurrences.add(
                Occurrence(
                    word = word,
                    sourceId = source.id,
                    context = context,
                    position = index,
                    sentenceCount = countSentences(context)
                )
            )

            searchFrom = index + lowerWord.length
        }

        return occurrences
    }

    /**
     * Extrait le contexte autour d'une position donnée.
     * Retourne la phrase contenant le mot + une phrase avant/après si disponible.
     */
    private fun extractContext(content: String, position: Int): String {
        val sentenceEndings = setOf('.', '!', '?', '\n')

        // Trouver le début de la phrase courante
        var start = position
        while (start > 0) {
            val c = content[start - 1]
            if (c in sentenceEndings && start > 1 && content[start - 2] != '-') {
                // Vérifier que ce n'est pas une abréviation (M., Dr., etc.)
                if (start < 2 || !content.substring(start - 2, start).any { it.isUpperCase() }) {
                    break
                }
            }
            start--
        }

        // Inclure la phrase précédente si proche
        var contextStart = start
        if (start > 0) {
            val prevEnd = findPreviousSentenceEnd(content, start)
            if (start - prevEnd < 200) { // moins de 200 caractères entre les phrases
                contextStart = prevEnd
            }
        }

        // Trouver la fin de la phrase courante
        var end = position
        while (end < content.length) {
            val c = content[end]
            if (c in sentenceEndings) {
                end++
                break
            }
            end++
        }

        // Inclure la phrase suivante si proche
        var contextEnd = end
        if (end < content.length) {
            val nextEnd = findNextSentenceEnd(content, end)
            if (nextEnd - end < 200) {
                contextEnd = nextEnd
            }
        }

        // Ajuster les limites
        contextStart = maxOf(0, contextStart)
        contextEnd = minOf(content.length, contextEnd)

        return content.substring(contextStart, contextEnd).trim()
    }

    /**
     * Trouve la fin de la phrase précédente.
     */
    private fun findPreviousSentenceEnd(content: String, from: Int): Int {
        val sentenceEndings = setOf('.', '!', '?', '\n')
        var pos = from - 1
        while (pos > 0) {
            if (content[pos] in sentenceEndings) {
                return pos + 1
            }
            pos--
        }
        return 0
    }

    /**
     * Trouve la fin de la phrase suivante.
     */
    private fun findNextSentenceEnd(content: String, from: Int): Int {
        val sentenceEndings = setOf('.', '!', '?', '\n')
        var pos = from
        while (pos < content.length) {
            if (content[pos] in sentenceEndings) {
                return pos + 1
            }
            pos++
        }
        return content.length
    }

    /**
     * Compte le nombre de phrases dans un texte.
     */
    private fun countSentences(text: String): Int {
        return text.count { it in setOf('.', '!', '?') }
    }

    /**
     * Extrait les mots longs (> 6 caractères) d'un texte.
     * Utile pour la recherche par domaine.
     */
    private fun extractLongWords(text: String): Set<String> {
        val words = mutableSetOf<String>()
        val regex = Regex("[a-zA-ZÀ-ÿ]{7,}") // mots de 7+ caractères
        for (match in regex.findAll(text)) {
            val word = match.value.lowercase()
            if (word !in stopWords) {
                words.add(word)
            }
        }
        return words
    }
}
