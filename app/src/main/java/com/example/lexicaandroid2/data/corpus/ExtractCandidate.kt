package com.example.lexicaandroid2.data.corpus

/**
 * Candidat d'extrait — une phrase ou un passage de texte contenant un mot cible,
 * accompagné de métadonnées contextuelles pour l'apprentissage du vocabulaire.
 *
 * Un [ExtractCandidate] est le produit intermédiaire entre le parsing brut du corpus
 * et l'indexation finale. Il peut être filtré, trié ou classé avant d'être stocké.
 */
data class ExtractCandidate(
    /** Identifiant unique du candidat */
    val id: String,

    /** Le mot cible présent dans la phrase */
    val targetWord: String,

    /** La phrase ou le passage contenant le mot cible */
    val sentence: String,

    /** Source dont est issu l'extrait (référence au CorpusSource.id) */
    val sourceId: String,

    /** Titre de la source (dénormalisé pour accès rapide) */
    val sourceTitle: String,

    /** Auteur de la source (dénormalisé) */
    val sourceAuthor: String,

    /** Domaine(s) thématique(s) de la source */
    val domains: List<String> = emptyList(),

    /** Position de la phrase dans le texte source (indice pour référence) */
    val positionInSource: Int = 0,

    /** Score de pertinence de l'extrait (0.0 à 1.0) pour le contexte d'apprentissage */
    val relevanceScore: Float = 0.5f,

    /** Si la phrase est trop longue, un extrait tronqué autour du mot cible */
    val truncatedContext: String? = null,

    /** Longueur de la phrase en caractères */
    val sentenceLength: Int = sentence.length
) {
    init {
        require(id.isNotBlank()) { "ExtractCandidate.id ne peut pas être vide" }
        require(targetWord.isNotBlank()) { "ExtractCandidate.targetWord ne peut pas être vide" }
        require(sentence.isNotBlank()) { "ExtractCandidate.sentence ne peut pas être vide" }
        require(sourceId.isNotBlank()) { "ExtractCandidate.sourceId ne peut pas être vide" }
        require(relevanceScore in 0f..1f) { "relevanceScore doit être entre 0.0 et 1.0" }
    }
}
