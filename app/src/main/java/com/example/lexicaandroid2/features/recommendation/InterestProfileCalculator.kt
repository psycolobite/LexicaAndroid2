package com.example.lexicaandroid2.features.recommendation

import com.example.lexicaandroid2.domain.recommendation.InterestProfile
import com.example.lexicaandroid2.presentation.search.preferences.InterestTaxonomyProvider
import com.example.lexicaandroid2.presentation.search.preferences.UserPreferences

object InterestProfileCalculator {

    /**
     * Calcule le profil d'intérêt dynamique à partir des préférences initiales et des événements.
     *
     * @param preferences Les préférences de l'utilisateur (onboarding)
     * @param events Liste des événements d'interaction observés
     * @return Le profil d'intérêt calculé contenant la map domaine -> score
     */
    fun calculateProfile(
        preferences: UserPreferences?,
        events: List<InterestEvent>
    ): InterestProfile {
        val scores = mutableMapOf<String, Float>()

        // 1. Initialisation avec les domaines préférés déclarés (score de 0.5f)
        if (preferences != null) {
            val objectiveDomains = InterestTaxonomyProvider.domainsForObjectives(preferences.objectives)
            val allPreferredDomains = (objectiveDomains + preferences.preferredDomains).distinct()
            for (domain in allPreferredDomains) {
                scores[domain] = 0.5f
            }
        }

        // 2. Tri chronologique et traitement des événements
        val sortedEvents = events.sortedBy { it.timestamp }
        for (event in sortedEvents) {
            for (domain in event.domainIds) {
                val currentScore = scores[domain] ?: 0.0f
                val adjustment = when (event.type) {
                    InterestEventType.ONBOARDING_OBJECTIVE -> {
                        // Réinitialise ou force à 0.5f
                        0.5f - currentScore
                    }
                    InterestEventType.WORD_ADDED -> 0.3f
                    InterestEventType.WORD_REMOVED -> -0.3f
                    InterestEventType.EXTRACT_RATED -> {
                        // event.value correspond à la note (1 à 5).
                        // Note 3 -> 0.0f
                        // Note 5 -> +0.2f
                        // Note 1 -> -0.2f
                        (event.value - 3.0f) * 0.1f
                    }
                    InterestEventType.SOURCE_OPENED -> 0.15f
                }
                scores[domain] = (currentScore + adjustment).coerceIn(0.0f, 1.0f)
            }
        }

        return InterestProfile(
            domainScores = scores,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
