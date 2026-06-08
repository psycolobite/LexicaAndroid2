package com.example.lexicaandroid2.data.corpus

/**
 * Représente une source de corpus textuel (œuvre littéraire, article, chapitre, etc.)
 * à partir de laquelle des extraits contenant du vocabulaire cible peuvent être tirés.
 *
 * Une source est identifiée par un [id] unique et contient les métadonnées
 * nécessaires pour télécharger/parser son contenu texte brut.
 */
data class CorpusSource(
    /** Identifiant unique de la source (ex: "baudelaire-fleurs-du-mal") */
    val id: String,

    /** Titre de l'œuvre ou du document */
    val title: String,

    /** Auteur ou éditeur */
    val author: String,

    /** URL du texte brut (format .txt ou HTML à parser) */
    val rawUrl: String,

    /** Domaine(s) thématique(s) de la source (ex: "littérature", "philosophie") */
    val domains: List<String> = emptyList(),

    /** Niveau de difficulté estimé du vocabulaire (1 = courant, 3 = avancé) */
    val difficultyLevel: Int = 2,

    /** Langue du corpus (défaut: français) */
    val language: String = "fr",

    /** Année de publication ou période (ex: "1857", "XVIIe siècle") */
    val period: String = ""
) {
    init {
        require(id.isNotBlank()) { "CorpusSource.id ne peut pas être vide" }
        require(title.isNotBlank()) { "CorpusSource.title ne peut pas être vide" }
        require(rawUrl.isNotBlank()) { "CorpusSource.rawUrl ne peut pas être vide" }
        require(difficultyLevel in 1..3) { "CorpusSource.difficultyLevel doit être 1, 2 ou 3" }
    }
}

/**
 * Catalogue de sources de corpus prédéfinies pour la littérature française classique.
 *
 * Ces sources sont utilisées comme base pour l'extraction d'extraits
 * contextuels lors de l'apprentissage de vocabulaire avancé.
 */
object CorpusSources {

    /** Liste complète des sources disponibles */
    val ALL: List<CorpusSource> = listOf(
        // ── Poésie ────────────────────────────────────────────────────────
        CorpusSource(
            id = "baudelaire-fleurs-du-mal",
            title = "Les Fleurs du Mal",
            author = "Charles Baudelaire",
            rawUrl = "https://www.gutenberg.org/cache/epub/6099/pg6099.txt",
            domains = listOf("littérature", "poésie"),
            difficultyLevel = 3,
            period = "1857"
        ),
        CorpusSource(
            id = "rimbaud-poemes",
            title = "Poésies complètes",
            author = "Arthur Rimbaud",
            rawUrl = "https://www.gutenberg.org/cache/epub/15753/pg15753.txt",
            domains = listOf("littérature", "poésie"),
            difficultyLevel = 3,
            period = "XIXe siècle"
        ),
        CorpusSource(
            id = "verlaine-poemes",
            title = "Poèmes saturniens",
            author = "Paul Verlaine",
            rawUrl = "https://www.gutenberg.org/cache/epub/9049/pg9049.txt",
            domains = listOf("littérature", "poésie"),
            difficultyLevel = 2,
            period = "1866"
        ),

        // ── Roman XIXe ────────────────────────────────────────────────────
        CorpusSource(
            id = "flaubert-madame-bovary",
            title = "Madame Bovary",
            author = "Gustave Flaubert",
            rawUrl = "https://www.gutenberg.org/cache/epub/14155/pg14155.txt",
            domains = listOf("littérature", "roman"),
            difficultyLevel = 2,
            period = "1857"
        ),
        CorpusSource(
            id = "stendhal-rouge-et-noir",
            title = "Le Rouge et le Noir",
            author = "Stendhal",
            rawUrl = "https://www.gutenberg.org/cache/epub/798/pg798.txt",
            domains = listOf("littérature", "roman"),
            difficultyLevel = 2,
            period = "1830"
        ),
        CorpusSource(
            id = "balzac-eugenie-grandet",
            title = "Eugénie Grandet",
            author = "Honoré de Balzac",
            rawUrl = "https://www.gutenberg.org/cache/epub/1459/pg1459.txt",
            domains = listOf("littérature", "roman"),
            difficultyLevel = 2,
            period = "1834"
        ),

        // ── Philosophie ───────────────────────────────────────────────────
        CorpusSource(
            id = "descartes-discours",
            title = "Discours de la méthode",
            author = "René Descartes",
            rawUrl = "https://www.gutenberg.org/cache/epub/13846/pg13846.txt",
            domains = listOf("philosophie"),
            difficultyLevel = 3,
            period = "1637"
        ),
        CorpusSource(
            id = "rousseau-contrat-social",
            title = "Du contrat social",
            author = "Jean-Jacques Rousseau",
            rawUrl = "https://www.gutenberg.org/cache/epub/17984/pg17984.txt",
            domains = listOf("philosophie", "politique"),
            difficultyLevel = 3,
            period = "1762"
        ),
        CorpusSource(
            id = "montesquieu-esprit-des-lois",
            title = "De l'esprit des lois (extraits)",
            author = "Montesquieu",
            rawUrl = "https://www.gutenberg.org/cache/epub/27573/pg27573.txt",
            domains = listOf("philosophie", "politique"),
            difficultyLevel = 3,
            period = "1748"
        ),

        // ── Théâtre ───────────────────────────────────────────────────────
        CorpusSource(
            id = "moliere-misanthrope",
            title = "Le Misanthrope",
            author = "Molière",
            rawUrl = "https://www.gutenberg.org/cache/epub/6372/pg6372.txt",
            domains = listOf("littérature", "théâtre"),
            difficultyLevel = 2,
            period = "1666"
        )
    )

    /** Retourne les sources filtrées par domaine */
    fun byDomain(domain: String): List<CorpusSource> =
        ALL.filter { domain in it.domains }

    /** Retourne les sources filtrées par niveau de difficulté */
    fun byDifficulty(level: Int): List<CorpusSource> =
        ALL.filter { it.difficultyLevel == level }

    /** Retourne une source par son identifiant, ou null si introuvable */
    fun byId(id: String): CorpusSource? =
        ALL.firstOrNull { it.id == id }
}
