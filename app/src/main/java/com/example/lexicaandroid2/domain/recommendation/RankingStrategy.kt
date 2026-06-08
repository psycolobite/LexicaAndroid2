package com.example.lexicaandroid2.domain.recommendation

/**
 * Stratégie de classement des extraits candidats scorés.
 *
 * Le ranking détermine l'ordre final dans lequel les extraits sont
 * présentés à l'utilisateur. Il combine le score brut du moteur de
 * scoring avec des règles de reranking (ignorés, déjà vus, etc.)
 * et la politique d'exploration.
 */
interface RankingStrategy {

    /**
     * Classe une liste d'extraits scorés et retourne les N meilleurs.
     *
     * @param candidates Liste des extraits déjà scorés par [ScoringEngine]
     * @param limit Nombre maximal d'extraits à retourner
     * @return Les N meilleurs extraits dans l'ordre de présentation
     */
    fun rank(candidates: List<ScoredExtract>, limit: Int): List<ScoredExtract>
}

/**
 * Stratégie de classement par défaut.
 *
 * ## Règles de classement
 *
 * 1. **Tri par score décroissant** : les extraits les mieux scorés arrivent en premier
 * 2. **Reranking des ignorés** : les extraits que l'utilisateur a déjà ignorés
 *    (swipés sans ajout de mot) descendent dans le classement
 * 3. **Injection d'exploration** : la politique d'exploration peut remplacer
 *    un extrait du haut du classement par un extrait exploratoire
 * 4. **Déduplication** : on ne présente pas deux extraits de la même source
 *    consécutivement si possible
 *
 * @property explorationPolicy Politique d'exploration à appliquer
 * @property rerankingConfig Configuration du reranking
 */
class DefaultRankingStrategy(
    private val explorationPolicy: ExplorationPolicy = ExplorationPolicy(),
    private val rerankingConfig: RerankingConfig = RerankingConfig()
) : RankingStrategy {

    override fun rank(candidates: List<ScoredExtract>, limit: Int): List<ScoredExtract> {
        if (candidates.isEmpty()) return emptyList()

        // 1. Filtrer les extraits en dessous du seuil de qualité
        val filtered = candidates.filter { it.score >= rerankingConfig.minScoreThreshold }

        // 2. Trier par score décroissant
        val sorted = filtered.sortedByDescending { it.score }

        // 3. Appliquer le reranking (ignorés, déjà vus)
        val reranked = applyReranking(sorted)

        // 4. Déduplication des sources consécutives
        val deduplicated = deduplicateSources(reranked)

        // 5. Appliquer l'exploration policy
        val withExploration = applyExploration(deduplicated)

        // 6. Limiter le résultat
        return withExploration.take(limit)
    }

    // ------------------------------------------------------------------
    // Reranking
    // ------------------------------------------------------------------

    /**
     * Applique les pénalités de reranking.
     *
     * Les extraits ignorés (dans [RerankingConfig.ignoredExtractIds]) sont
     * déplacés en fin de liste. Les extraits déjà vus sont légèrement
     * descendus.
     */
    private fun applyReranking(candidates: List<ScoredExtract>): List<ScoredExtract> {
        if (rerankingConfig.ignoredExtractIds.isEmpty() &&
            rerankingConfig.seenSourceIds.isEmpty()
        ) return candidates

        val (penalized, normal) = candidates.partition { candidate ->
            candidate.extract.id in rerankingConfig.ignoredExtractIds ||
                    candidate.extract.sourceId in rerankingConfig.seenSourceIds
        }

        // Appliquer une pénalité de score aux extraits concernés
        val demoted = penalized.map { scored ->
            scored.copy(
                score = scored.score * rerankingConfig.penaltyFactor,
                breakdown = scored.breakdown.copy(
                    diversityScore = scored.breakdown.diversityScore * rerankingConfig.penaltyFactor
                )
            )
        }.sortedByDescending { it.score }

        return normal + demoted
    }

    // ------------------------------------------------------------------
    // Déduplication
    // ------------------------------------------------------------------

    /**
     * Évite de présenter deux extraits de la même source consécutivement.
     *
     * Si deux extraits consécutifs partagent la même source, on intercale
     * un extrait d'une autre source quand c'est possible.
     */
    private fun deduplicateSources(candidates: List<ScoredExtract>): List<ScoredExtract> {
        if (candidates.size < 2) return candidates

        val result = mutableListOf<ScoredExtract>()
        val remaining = candidates.toMutableList()

        var lastSourceId: String? = null

        while (remaining.isNotEmpty()) {
            val index = if (lastSourceId != null) {
                // Chercher un extrait d'une source différente
                val differentSource = remaining.indexOfFirst { it.extract.sourceId != lastSourceId }
                if (differentSource >= 0) differentSource else 0
            } else {
                0
            }

            val selected = remaining.removeAt(index)
            lastSourceId = selected.extract.sourceId
            result.add(selected)
        }

        return result
    }

    // ------------------------------------------------------------------
    // Exploration
    // ------------------------------------------------------------------

    /**
     * Injecte un extrait exploratoire si la politique d'exploration le décide.
     *
     * L'extrait exploratoire remplace le dernier élément de la liste
     * (le moins bien classé parmi les retenus).
     */
    private fun applyExploration(candidates: List<ScoredExtract>): List<ScoredExtract> {
        if (candidates.isEmpty()) return candidates

        if (!explorationPolicy.shouldExplore()) return candidates

        val exploratory = explorationPolicy.selectExploratoryExtract(candidates)
            ?: return candidates

        // Remplacer le dernier élément par l'extrait exploratoire
        val result = candidates.toMutableList()
        if (result.size > 1) {
            result[result.lastIndex] = exploratory
        }
        return result
    }
}

/**
 * Configuration du reranking.
 *
 * @property minScoreThreshold Score minimum pour qu'un extrait soit retenu (défaut: 0.1)
 * @property penaltyFactor Facteur multiplicatif de pénalité pour les extraits ignorés (défaut: 0.5)
 * @property ignoredExtractIds IDs des extraits que l'utilisateur a ignorés
 * @property seenSourceIds IDs des sources déjà vues par l'utilisateur
 */
data class RerankingConfig(
    val minScoreThreshold: Float = 0.1f,
    val penaltyFactor: Float = 0.5f,
    val ignoredExtractIds: Set<String> = emptySet(),
    val seenSourceIds: Set<String> = emptySet()
)

/**
 * Stratégie de classement qui alterne entre plusieurs domaines.
 *
 * Utile pour garantir une diversité thématique dans les résultats
 * présentés à l'utilisateur, même si un domaine domine le scoring.
 */
class InterleavedRankingStrategy(
    private val explorationPolicy: ExplorationPolicy = ExplorationPolicy(),
    private val rerankingConfig: RerankingConfig = RerankingConfig()
) : RankingStrategy {

    override fun rank(candidates: List<ScoredExtract>, limit: Int): List<ScoredExtract> {
        if (candidates.isEmpty()) return emptyList()

        // Filtrer les extraits sous le seuil
        val filtered = candidates.filter { it.score >= rerankingConfig.minScoreThreshold }

        // Grouper par domaine principal (premier domainTag)
        val byDomain = filtered.groupBy { it.extract.domainTags.firstOrNull() ?: "unknown" }

        // Trier chaque groupe par score
        val sortedGroups = byDomain.mapValues { (_, extracts) ->
            extracts.sortedByDescending { it.score }
        }

        // Interleaving : prendre un extrait de chaque groupe à tour de rôle
        val result = mutableListOf<ScoredExtract>()
        val iterators = sortedGroups.mapValues { (_, list) -> list.iterator() }
        var hasRemaining = true

        while (hasRemaining && result.size < limit) {
            hasRemaining = false
            for ((_, iterator) in iterators) {
                if (iterator.hasNext()) {
                    result.add(iterator.next())
                    hasRemaining = true
                    if (result.size >= limit) break
                }
            }
        }

        return result
    }
}
