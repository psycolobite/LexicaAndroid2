package com.example.lexicaandroid2.features.recommendation

enum class InterestEventType {
    ONBOARDING_OBJECTIVE,  // Intérêt initial déclaré
    WORD_ADDED,            // Mot ajouté depuis l'exploration
    WORD_REMOVED,          // Mot retiré du deck
    EXTRACT_RATED,         // Notation explicite de l'extrait
    SOURCE_OPENED          // Ouverture de l'ouvrage complet
}

data class InterestEvent(
    val id: String,
    val timestamp: Long,
    val type: InterestEventType,
    val domainIds: List<String>,
    val value: Float = 0f
)
