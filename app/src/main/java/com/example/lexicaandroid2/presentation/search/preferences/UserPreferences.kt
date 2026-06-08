package com.example.lexicaandroid2.presentation.search.preferences

/**
 * Modèle représentant les préférences déclarées de l'utilisateur.
 *
 * Ces préférences sont collectées lors du premier lancement (onboarding)
 * et peuvent être modifiées ultérieurement dans les paramètres.
 *
 * Elles servent de point de départ pour orienter les recommandations
 * d'extraits et de mots cibles avant que le comportement observé
 * ne prenne le relais.
 *
 * @property objectives Liste des objectifs déclarés par l'utilisateur
 * @property preferredDomains Identifiants des domaines préférés
 * @property preferredRegisters Identifiants des registres de langue préférés
 * @property lastUpdated Timestamp Unix (millis) de la dernière modification
 */
data class UserPreferences(
    val objectives: List<UserObjective>,
    val preferredDomains: List<String>,
    val preferredRegisters: List<String>,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Objectifs utilisateur disponibles au premier lancement.
 *
 * Chaque objectif correspond à un besoin utilisateur exprimé simplement,
 * et se traduit en une combinaison de thèmes/domaines/registres
 * dans la table de correspondance (voir [InterestTaxonomy.objectiveToThemes]).
 */
enum class UserObjective {
    /** "Je veux mieux m'exprimer dans des conversations soutenues" */
    CONVERSATIONS_SOUTENUES,

    /** "Je veux mieux comprendre des textes exigeants" */
    TEXTES_EXIGEANTS,

    /** "Je veux découvrir des façons de parler particulières" */
    DIALECTES_ARGOTS,

    /** "Je veux apprendre le vocabulaire d'un domaine précis" */
    DOMAINE_SPECIFIQUE
}

/**
 * Représentation lisible des objectifs pour l'interface utilisateur.
 */
object UserObjectiveLabels {
    fun labelFor(objective: UserObjective): String = when (objective) {
        UserObjective.CONVERSATIONS_SOUTENUES ->
            "Mieux m'exprimer dans des conversations soutenues"
        UserObjective.TEXTES_EXIGEANTS ->
            "Mieux comprendre des textes exigeants"
        UserObjective.DIALECTES_ARGOTS ->
            "Découvrir des façons de parler particulières"
        UserObjective.DOMAINE_SPECIFIQUE ->
            "Apprendre le vocabulaire d'un domaine précis"
    }

    val allLabels: List<Pair<UserObjective, String>> =
        UserObjective.entries.map { it to labelFor(it) }
}
