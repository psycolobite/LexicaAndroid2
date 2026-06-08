package com.example.lexicaandroid2.presentation.search.preferences

/**
 * Taxonomie des centres d'intérêt pour les préférences utilisateur de recherche.
 *
 * Chaque catégorie représente un thème de vocabulaire que l'utilisateur peut
 * sélectionner pour personnaliser son apprentissage (filtrage, suggestions).
 */
enum class InterestCategory(
    val id: String,
    val label: String,
    val emoji: String,
    /** Mots-clés associés utilisés pour le matching avec les flashcards. */
    val keywords: List<String> = emptyList()
) {
    DAILY_LIFE(
        id = "daily_life",
        label = "Vie quotidienne",
        emoji = "🏠",
        keywords = listOf("maison", "famille", "courses", "vêtements", "cuisine", "ménage", "quotidien")
    ),
    WORK(
        id = "work",
        label = "Travail",
        emoji = "💼",
        keywords = listOf("emploi", "bureau", "entreprise", "réunion", "carrière", "métier", "professionnel")
    ),
    FOOD(
        id = "food",
        label = "Cuisine & Gastronomie",
        emoji = "🍳",
        keywords = listOf("repas", "restaurant", "recette", "ingrédient", "boisson", "nourriture", "plat")
    ),
    TECHNOLOGY(
        id = "tech",
        label = "Technologie",
        emoji = "💻",
        keywords = listOf("informatique", "internet", "ordinateur", "numérique", "logiciel", "réseau", "digital")
    ),
    TRAVEL(
        id = "travel",
        label = "Voyage",
        emoji = "✈️",
        keywords = listOf("transport", "tourisme", "hôtel", "aéroport", "vacances", "aventure", "découverte")
    ),
    HEALTH(
        id = "health",
        label = "Santé",
        emoji = "🏥",
        keywords = listOf("médecin", "hôpital", "bien-être", "maladie", "soin", "sport", "corps")
    ),
    NATURE(
        id = "nature",
        label = "Nature & Environnement",
        emoji = "🌿",
        keywords = listOf("animal", "plante", "écologie", "climat", "paysage", "forêt", "mer")
    ),
    CULTURE(
        id = "culture",
        label = "Culture & Arts",
        emoji = "🎨",
        keywords = listOf("musique", "peinture", "théâtre", "cinéma", "littérature", "exposition", "spectacle")
    ),
    SPORTS(
        id = "sports",
        label = "Sports",
        emoji = "⚽",
        keywords = listOf("football", "compétition", "entraînement", "match", "athlète", "jeu", "victoire")
    ),
    EDUCATION(
        id = "education",
        label = "Éducation",
        emoji = "📚",
        keywords = listOf("école", "étude", "cours", "apprentissage", "examen", "université", "savoir")
    ),
    SCIENCE(
        id = "science",
        label = "Science",
        emoji = "🔬",
        keywords = listOf("recherche", "expérience", "découverte", "théorie", "laboratoire", "physique", "chimie")
    ),
    BUSINESS(
        id = "business",
        label = "Business & Économie",
        emoji = "📈",
        keywords = listOf("finance", "marché", "commerce", "investissement", "économie", "entreprise", "stratégie")
    ),
    SOCIETY(
        id = "society",
        label = "Société & Politique",
        emoji = "🏛️",
        keywords = listOf("gouvernement", "loi", "citoyen", "démocratie", "social", "justice", "débat")
    ),
    EMOTIONS(
        id = "emotions",
        label = "Émotions & Relations",
        emoji = "❤️",
        keywords = listOf("amour", "amitié", "sentiment", "bonheur", "tristesse", "colère", "famille")
    );

    companion object {
        fun fromId(id: String): InterestCategory? =
            entries.find { it.id == id }

        val defaultSelection: Set<InterestCategory>
            get() = entries.toSet()
    }
}
