package com.example.lexicaandroid2.domain.recommendation

import kotlin.random.Random

/**
 * Politique d'exploration vs exploitation pour le moteur de recommandation.
 *
 * ## Principe
 *
 * Le système doit équilibrer deux objectifs contradictoires :
 * - **Exploitation** : montrer des extraits fortement compatibles avec le profil
 *   utilisateur (maximise la satisfaction immédiate)
 * - **Exploration** : tester de nouveaux domaines, sources ou registres pour
 *   découvrir de nouveaux intérêts potentiels (maximise la satisfaction long terme)
 *
 * ## Fonctionnement
 *
 * La politique utilise un taux d'exploration ([ExplorationConfig.explorationRate])
 * qui détermine la probabilité d'injecter un extrait exploratoire dans le classement.
 * Ce taux décroît progressivement avec l'usage ([ExplorationConfig.decayFactor])
 * jusqu'à un minimum ([ExplorationConfig.minExplorationRate]).
 *
 * ## Répartition cible (V1)
 *
 * - 70 % d'extraits fortement compatibles (exploitation)
 * - 20 % d'extraits voisins (exploration modérée)
 * - 10 % d'extraits exploratoires (nouveaux domaines)
 *
 * @property config Configuration actuelle de l'exploration
 */
class ExplorationPolicy(
    private var config: ExplorationConfig = ExplorationConfig()
) {

    /**
     * Détermine si l'exploration doit être activée pour la requête courante.
     *
     * @return true si on doit injecter un extrait exploratoire
     */
    fun shouldExplore(): Boolean {
        return Random.nextFloat() < config.explorationRate
    }

    /**
     * Sélectionne un extrait exploratoire parmi les candidats.
     *
     * L'extrait exploratoire est celui qui a le meilleur score d'exploration
     * ([ScoreBreakdown.explorationScore]) parmi les candidats, indépendamment
     * de son score total.
     *
     * @param candidates Liste des extraits scorés disponibles
     * @return L'extrait le plus exploratoire, ou null si la liste est vide
     */
    fun selectExploratoryExtract(candidates: List<ScoredExtract>): ScoredExtract? {
        if (candidates.isEmpty()) return null

        // Sélectionner l'extrait avec le meilleur score d'exploration
        return candidates.maxByOrNull { it.breakdown.explorationScore }
    }

    /**
     * Sélectionne un extrait aléatoire pour une exploration pure.
     *
     * Contrairement à [selectExploratoryExtract] qui choisit l'extrait
     * le plus exploratoire, cette méthode fait un tirage aléatoire
     * pour une exploration plus large.
     *
     * @param candidates Liste des extraits scorés disponibles
     * @return Un extrait choisi aléatoirement, ou null si la liste est vide
     */
    fun selectRandomExtract(candidates: List<ScoredExtract>): ScoredExtract? {
        if (candidates.isEmpty()) return null
        return candidates[Random.nextInt(candidates.size)]
    }

    /**
     * Réduit le taux d'exploration après une session.
     *
     * À mesure que l'utilisateur accumule des sessions, le système
     * a de plus en plus confiance dans son profil et peut réduire
     * la part d'exploration.
     *
     * @return La nouvelle configuration après décroissance
     */
    fun decayRate(): ExplorationConfig {
        val newRate = (config.explorationRate * config.decayFactor)
            .coerceAtLeast(config.minExplorationRate)

        config = config.copy(explorationRate = newRate)
        return config
    }

    /**
     * Réinitialise le taux d'exploration à sa valeur initiale.
     *
     * Utile si l'utilisateur change ses préférences ou si on détecte
     * un changement significatif de comportement.
     */
    fun resetRate(): ExplorationConfig {
        config = ExplorationConfig()
        return config
    }

    /**
     * Retourne la configuration actuelle.
     */
    fun getConfig(): ExplorationConfig = config

    /**
     * Met à jour la configuration.
     */
    fun updateConfig(newConfig: ExplorationConfig) {
        this.config = newConfig
    }
}

/**
 * Configuration de la politique d'exploration.
 *
 * @property explorationRate Probabilité d'exploration (0.0 – 1.0, défaut: 0.1 soit 10 %)
 * @property decayFactor Facteur de décroissance après chaque session (défaut: 0.95)
 * @property minExplorationRate Taux d'exploration minimum (défaut: 0.05 soit 5 %)
 */
data class ExplorationConfig(
    val explorationRate: Float = 0.1f,
    val decayFactor: Float = 0.95f,
    val minExplorationRate: Float = 0.05f
) {
    companion object {
        /**
         * Configuration agressive pour un nouvel utilisateur sans historique.
         * Explore davantage pour construire le profil rapidement.
         */
        val highExploration = ExplorationConfig(
            explorationRate = 0.3f,
            decayFactor = 0.9f,
            minExplorationRate = 0.1f
        )

        /**
         * Configuration pour un utilisateur établi avec un profil stable.
         * Explore très peu, exploite au maximum.
         */
        val lowExploration = ExplorationConfig(
            explorationRate = 0.05f,
            decayFactor = 0.98f,
            minExplorationRate = 0.02f
        )

        /**
         * Configuration pour une session de "découverte" explicite.
         * L'utilisateur a choisi de explorer de nouveaux contenus.
         */
        val discoveryMode = ExplorationConfig(
            explorationRate = 0.5f,
            decayFactor = 0.95f,
            minExplorationRate = 0.2f
        )
    }
}
