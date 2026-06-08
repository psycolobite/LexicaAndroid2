package com.example.lexicaandroid2.presentation.search.preferences

/**
 * Préférences utilisateur pour la recherche et la personnalisation du contenu.
 *
 * @property selectedInterestIds Identifiants des catégories d'intérêt sélectionnées.
 * @property filterByInterests Si `true`, les résultats de recherche sont filtrés
 *           selon les centres d'intérêt sélectionnés.
 * @property maxResults Nombre maximum de résultats affichés par recherche.
 * @property searchLanguage Code langue pour la recherche (ex: "fr", "en").
 */
data class UserPreferences(
    val selectedInterestIds: Set<String> = InterestCategory.defaultSelection.map { it.id }.toSet(),
    val filterByInterests: Boolean = false,
    val maxResults: Int = DEFAULT_MAX_RESULTS,
    val searchLanguage: String = "fr"
) {
    companion object {
        const val DEFAULT_MAX_RESULTS = 50
        const val MIN_RESULTS = 10
        const val MAX_RESULTS = 100
    }

    /**
     * Retourne la liste des [InterestCategory] correspondant aux identifiants sélectionnés.
     */
    val selectedInterests: List<InterestCategory>
        get() = selectedInterestIds.mapNotNull { InterestCategory.fromId(it) }

    /**
     * Vrai si toutes les catégories sont sélectionnées (comportement par défaut).
     */
    val isAllSelected: Boolean
        get() = selectedInterestIds.size == InterestCategory.entries.size
}
