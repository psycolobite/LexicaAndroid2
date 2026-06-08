package com.example.lexicaandroid2.data.corpus

/**
 * Candidat d'extrait — représente un passage textuel extrait d'un corpus,
 * avec ses métadonnées et indicateurs de qualité pour l'apprentissage du vocabulaire.
 */
data class ExtractCandidate(
    /** Identifiant unique de l'extrait */
    val id: String,

    /** Identifiant de la source d'origine */
    val sourceId: String,

    /** Contenu textuel de l'extrait */
    val content: String,

    /** Position de départ (en caractères) dans la source originale */
    val startPosition: Int,

    /** Position de fin (en caractères) dans la source originale */
    val endPosition: Int,

    /** Nombre de mots dans l'extrait */
    val wordCount: Int,

    /** Mots potentiellement intéressants identifiés dans le texte */
    val suggestedWords: List<String>,

    /** Identifiants des thèmes/domaines associés (tags issus de la taxonomie) */
    val domainTags: List<String>,

    /** Registres de langue détectés (ex: soutenu, technique, familier) */
    val registerTags: List<String>,

    /** Niveau de difficulté estimé (ex: facile, moyen, avancé) */
    val difficulty: String,

    /** Score de qualité du contexte (0.0 à 1.0) */
    val contextQuality: Float,

    /** Vrai si la source complète est accessible (ex: via un lien ou une lecture intégrée) */
    val hasCompleteSource: Boolean
) {
    init {
        require(id.isNotBlank()) { "id ne peut pas être vide" }
        require(sourceId.isNotBlank()) { "sourceId ne peut pas être vide" }
        require(content.isNotBlank()) { "content ne peut pas être vide" }
        require(contextQuality in 0.0f..1.0f) { "contextQuality doit être compris entre 0.0f et 1.0f" }
    }

    companion object {
        const val MIN_CONTEXT_QUALITY = 0.3f
    }
}
