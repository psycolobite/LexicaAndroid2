package com.example.lexicaandroid2.data.corpus

/**
 * Représente une source de corpus (livre, article, interview...)
 * à partir de laquelle des extraits peuvent être segmentés.
 */
data class CorpusSource(
    val id: String,
    val title: String,
    val author: String?,
    val year: Int?,
    val type: SourceType,
    val language: String = "fr",
    val license: String? = null,
    val contentUrl: String? = null,
    val domainTags: List<String> = emptyList(),
    val content: String = ""
) {
    init {
        require(id.isNotBlank()) { "id ne peut pas être vide" }
        require(title.isNotBlank()) { "title ne peut pas être vide" }
    }
}

enum class SourceType { BOOK, ARTICLE, INTERVIEW, TRANSCRIPT, SPEECH, OTHER }

/**
 * Catalogue de sources de corpus pilotes.
 */
object CorpusSources {
    val ALL: List<CorpusSource> = listOf(
        CorpusSource(
            id = "baudelaire-fleurs-du-mal",
            title = "Les Fleurs du Mal",
            author = "Charles Baudelaire",
            year = 1857,
            type = SourceType.BOOK,
            language = "fr",
            license = "PUBLIC_DOMAIN",
            contentUrl = "https://www.gutenberg.org/cache/epub/6099/pg6099.txt",
            domainTags = listOf("litterature_classique", "poesie")
        ),
        CorpusSource(
            id = "descartes-discours",
            title = "Discours de la méthode",
            author = "René Descartes",
            year = 1637,
            type = SourceType.BOOK,
            language = "fr",
            license = "PUBLIC_DOMAIN",
            contentUrl = "https://www.gutenberg.org/cache/epub/13846/pg13846.txt",
            domainTags = listOf("philosophie", "sciences_humaines")
        ),
        CorpusSource(
            id = "moliere-misanthrope",
            title = "Le Misanthrope",
            author = "Molière",
            year = 1666,
            type = SourceType.BOOK,
            language = "fr",
            license = "PUBLIC_DOMAIN",
            contentUrl = "https://www.gutenberg.org/cache/epub/6372/pg6372.txt",
            domainTags = listOf("litterature_classique", "theatre")
        )
    )

    fun byDomain(domainId: String): List<CorpusSource> =
        ALL.filter { domainId in it.domainTags }

    fun byId(id: String): CorpusSource? =
        ALL.firstOrNull { it.id == id }
}
