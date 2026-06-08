package com.example.lexicaandroid2.domain.recommendation

import com.example.lexicaandroid2.data.corpus.ExtractCandidate
import com.example.lexicaandroid2.presentation.search.preferences.InterestTaxonomyProvider
import com.example.lexicaandroid2.presentation.search.preferences.UserObjective
import com.example.lexicaandroid2.presentation.search.preferences.UserPreferences

/**
 * Moteur de scoring combinant les signaux explicites (préférences déclarées)
 * et implicites (comportement observé) pour classer les extraits candidats.
 *
 * ## Principe
 *
 * Chaque extrait reçoit un score composite entre 0.0 et 1.0, calculé à partir
 * de 4 sous-scores :
 * - **preferenceScore** : correspondance avec les objectifs et domaines préférés
 * - **qualityScore** : qualité intrinsèque de l'extrait (contextQuality de R3)
 * - **diversityScore** : bonus de diversité pour éviter la redondance
 * - **explorationScore** : bonus pour découvrir de nouveaux domaines
 *
 * ## Formule V1
 *
 * ```
 * score = 0.4 * preferenceScore
 *       + 0.3 * qualityScore
 *       + 0.2 * diversityScore
 *       + 0.1 * explorationScore
 * ```
 *
 * Cette formule est volontairement simple et explicable. Elle pourra être
 * enrichie par itérations (notamment avec les signaux de R9).
 *
 * @property weights Poids de chaque sous-score dans le calcul final
 */
class ScoringEngine(
    private val weights: ScoreWeights = ScoreWeights()
) {

    /**
     * Calcule le score d'un seul extrait dans un contexte donné.
     *
     * @param extract L'extrait candidat à scorer
     * @param context Contexte utilisateur (préférences, historique, profil)
     * @return Résultat scoré avec détail des sous-scores
     */
    fun score(extract: ExtractCandidate, context: ScoreContext): ScoredExtract {
        val preferenceScore = computePreferenceScore(extract, context)
        val qualityScore = computeQualityScore(extract)
        val diversityScore = computeDiversityScore(extract, context)
        val explorationScore = computeExplorationScore(extract, context)

        val breakdown = ScoreBreakdown(
            preferenceScore = preferenceScore,
            qualityScore = qualityScore,
            diversityScore = diversityScore,
            explorationScore = explorationScore
        )

        val finalScore = weights.apply(breakdown)

        return ScoredExtract(
            extract = extract,
            score = finalScore.coerceIn(0.0f, 1.0f),
            breakdown = breakdown
        )
    }

    /**
     * Scorre un lot d'extraits dans le même contexte utilisateur.
     *
     * @param extracts Liste des extraits candidats
     * @param context Contexte utilisateur partagé
     * @return Liste des extraits scorés, triée par score décroissant
     */
    fun scoreBatch(
        extracts: List<ExtractCandidate>,
        context: ScoreContext
    ): List<ScoredExtract> {
        return extracts
            .map { score(it, context) }
            .sortedByDescending { it.score }
    }

    // ------------------------------------------------------------------
    // Sous-scores
    // ------------------------------------------------------------------

    /**
     * Score de correspondance avec les préférences utilisateur (0.0 – 1.0).
     *
     * Utilise le mapping objectifs → domaines de [InterestTaxonomyProvider]
     * pour mesurer le chevauchement entre les domaines de l'extrait et
     * les domaines préférés de l'utilisateur.
     */
    private fun computePreferenceScore(
        extract: ExtractCandidate,
        context: ScoreContext
    ): Float {
        val prefs = context.userPreferences ?: return 0.5f // valeur neutre

        // 1. Domaines dérivés des objectifs déclarés
        val objectiveDomains = InterestTaxonomyProvider.domainsForObjectives(prefs.objectives)

        // 2. Domaines explicitement préférés
        val allPreferredDomains = (objectiveDomains + prefs.preferredDomains).distinct()

        if (allPreferredDomains.isEmpty()) return 0.5f

        // 3. Chevauchement avec les domaines de l'extrait
        val extractDomains = extract.domainTags
        if (extractDomains.isEmpty()) return 0.3f

        val overlap = extractDomains.count { it in allPreferredDomains }
        val ratio = overlap.toFloat() / extractDomains.size.toFloat()

        // 4. Bonus si les registres correspondent aussi
        val registerBonus = if (prefs.preferredRegisters.isNotEmpty() &&
            extract.registerTags.any { it in prefs.preferredRegisters }
        ) 0.15f else 0.0f

        return (ratio + registerBonus).coerceIn(0.0f, 1.0f)
    }

    /**
     * Score de qualité intrinsèque de l'extrait (0.0 – 1.0).
     *
     * Reprend directement le [ExtractCandidate.contextQuality] calculé par R3.
     * Un extrait avec un contexte clair et bien formé est plus utile
     * pour l'apprentissage.
     */
    private fun computeQualityScore(extract: ExtractCandidate): Float {
        return extract.contextQuality.coerceIn(0.0f, 1.0f)
    }

    /**
     * Score de diversité (0.0 – 1.0).
     *
     * Bonus si l'extrait provient d'une source ou d'un domaine différent
     * des N derniers extraits déjà vus par l'utilisateur.
     * Évite la redondance et maintient l'intérêt.
     */
    private fun computeDiversityScore(
        extract: ExtractCandidate,
        context: ScoreContext
    ): Float {
        if (context.history.isEmpty()) return 1.0f // premier extrait → diversité max

        // Vérifier si la source a déjà été vue
        val sourceSeen = context.history.any { it == extract.sourceId }
        if (!sourceSeen) return 0.8f // nouvelle source → bon bonus

        // Vérifier si les domaines sont différents des préférences courantes
        val prefs = context.userPreferences
        if (prefs != null) {
            val preferredDomains = InterestTaxonomyProvider.domainsForObjectives(prefs.objectives)
            val hasNewDomain = extract.domainTags.any { it !in preferredDomains }
            if (hasNewDomain) return 0.5f // domaine nouveau dans une source connue
        }

        return 0.2f // source et domaines déjà vus → faible diversité
    }

    /**
     * Score d'exploration (0.0 – 1.0).
     *
     * Bonus si l'extrait touche un domaine que l'utilisateur n'a pas
     * encore exploré selon ses préférences déclarées.
     * Ce score est utilisé par [ExplorationPolicy] pour injecter
     * de la nouveauté dans les recommandations.
     */
    private fun computeExplorationScore(
        extract: ExtractCandidate,
        context: ScoreContext
    ): Float {
        val prefs = context.userPreferences ?: return 0.3f

        val knownDomains = InterestTaxonomyProvider.domainsForObjectives(prefs.objectives)
            .toSet()

        if (knownDomains.isEmpty()) return 0.5f

        val unknownDomains = extract.domainTags.filter { it !in knownDomains }
        if (unknownDomains.isEmpty()) return 0.0f

        // Plus il y a de domaines inconnus, plus le score d'exploration est élevé
        val ratio = unknownDomains.size.toFloat() / extract.domainTags.size.toFloat()
        return ratio.coerceIn(0.0f, 1.0f)
    }
}

// ---------------------------------------------------------------------------
// Modèles de données
// ---------------------------------------------------------------------------

/**
 * Contexte utilisateur pour le scoring d'un extrait.
 *
 * @property userPreferences Préférences déclarées (peut être null si pas encore renseignées)
 * @property interestProfile Profil d'intérêt dérivé (null en V1, sera alimenté par R9)
 * @property history Liste des IDs de sources déjà vues par l'utilisateur
 */
data class ScoreContext(
    val userPreferences: UserPreferences?,
    val interestProfile: InterestProfile? = null,
    val history: List<String> = emptyList()
)

/**
 * Résultat du scoring d'un extrait.
 *
 * @property extract L'extrait candidat scoré
 * @property score Score final combiné (0.0 – 1.0)
 * @property breakdown Détail des sous-scores pour débogage et transparence
 */
data class ScoredExtract(
    val extract: ExtractCandidate,
    val score: Float,
    val breakdown: ScoreBreakdown
)

/**
 * Détail des sous-scores ayant contribué au score final.
 *
 * Permet de comprendre pourquoi un extrait a été classé à une certaine
 * position, utile pour le débogage et l'affichage de transparence.
 */
data class ScoreBreakdown(
    val preferenceScore: Float,
    val qualityScore: Float,
    val diversityScore: Float,
    val explorationScore: Float
)

/**
 * Poids de chaque sous-score dans la formule de calcul finale.
 *
 * @property preferenceWeight Poids du score de préférence (défaut: 0.4)
 * @property qualityWeight Poids du score de qualité (défaut: 0.3)
 * @property diversityWeight Poids du score de diversité (défaut: 0.2)
 * @property explorationWeight Poids du score d'exploration (défaut: 0.1)
 */
data class ScoreWeights(
    val preferenceWeight: Float = 0.4f,
    val qualityWeight: Float = 0.3f,
    val diversityWeight: Float = 0.2f,
    val explorationWeight: Float = 0.1f
) {
    /**
     * Calcule le score final pondéré à partir d'un détail de sous-scores.
     */
    fun apply(breakdown: ScoreBreakdown): Float {
        return preferenceWeight * breakdown.preferenceScore +
                qualityWeight * breakdown.qualityScore +
                diversityWeight * breakdown.diversityScore +
                explorationWeight * breakdown.explorationScore
    }

    companion object {
        /**
         * Poids alternatifs qui donnent plus d'importance à l'exploration.
         * Utile pour les nouveaux utilisateurs sans historique.
         */
        val explorationFriendly = ScoreWeights(
            preferenceWeight = 0.3f,
            qualityWeight = 0.3f,
            diversityWeight = 0.2f,
            explorationWeight = 0.2f
        )

        /**
         * Poids alternatifs qui favorisent la qualité du contexte.
         */
        val qualityFocused = ScoreWeights(
            preferenceWeight = 0.3f,
            qualityWeight = 0.5f,
            diversityWeight = 0.1f,
            explorationWeight = 0.1f
        )
    }
}

/**
 * Profil d'intérêt utilisateur par domaine.
 *
 * En V1, ce profil est null et le scoring se base uniquement sur
 * les préférences déclarées. Dans une version future (R9), ce profil
 * sera alimenté par les événements utilisateur observés.
 *
 * @property domainScores Map domaine → score d'intérêt agrégé (0.0 – 1.0)
 * @property lastUpdated Timestamp de la dernière mise à jour du profil
 */
data class InterestProfile(
    val domainScores: Map<String, Float>,
    val lastUpdated: Long = System.currentTimeMillis()
)
