package com.example.lexicaandroid2.presentation.search.preferences

import com.example.lexicaandroid2.presentation.search.preferences.UserObjective

/**
 * Taxonomie V1 des thèmes, domaines et registres de langue.
 *
 * Cette taxonomie sert à :
 * - catégoriser les sources et extraits candidats,
 * - mapper les objectifs utilisateur vers des dimensions internes,
 * - alimenter le moteur de scoring et de recommandation.
 *
 * Structure :
 * - **Thèmes** : grandes familles (Sciences, Littérature, Vie quotidienne…)
 * - **Domaines** : sous-catégories fines d'un thème
 * - **Registres** : niveau de langue (Soutenu, Courant, Familier…)
 *
 * @param themes Liste complète des thèmes disponibles
 * @param domains Liste complète des domaines disponibles
 * @param registers Liste complète des registres disponibles
 */
data class InterestTaxonomy(
    val themes: List<Theme>,
    val domains: List<Domain>,
    val registers: List<Register>
) {
    /**
     * Retourne les domaines appartenant à un thème donné.
     */
    fun domainsForTheme(themeId: String): List<Domain> =
        domains.filter { it.themeId == themeId }

    /**
     * Retourne le thème parent d'un domaine.
     */
    fun themeForDomain(domainId: String): Theme? {
        val domain = domains.find { it.id == domainId } ?: return null
        return themes.find { it.id == domain.themeId }
    }
}

/**
 * Un thème est une grande famille thématique.
 *
 * @param id Identifiant unique (ex: "sciences")
 * @param label Libellé affichable (ex: "Sciences")
 * @param domainIds Liste des identifiants de domaines rattachés
 */
data class Theme(
    val id: String,
    val label: String,
    val domainIds: List<String>
)

/**
 * Un domaine est une sous-catégorie fine d'un thème.
 *
 * @param id Identifiant unique (ex: "biologie")
 * @param label Libellé affichable (ex: "Biologie")
 * @param themeId Identifiant du thème parent
 */
data class Domain(
    val id: String,
    val label: String,
    val themeId: String
)

/**
 * Un registre de langue.
 *
 * @param id Identifiant unique (ex: "soutenu")
 * @param label Libellé affichable (ex: "Soutenu")
 */
data class Register(
    val id: String,
    val label: String
)

// ---------------------------------------------------------------------------
// Taxonomie V1 — données pré-remplies
// ---------------------------------------------------------------------------

/**
 * Fournisseur de la taxonomie V1.
 *
 * Cette taxonomie est une première version destinée à être enrichie
 * par itérations produit et retours utilisateurs.
 */
object InterestTaxonomyProvider {

    /** Thèmes V1 */
    val themes: List<Theme> = listOf(
        Theme(
            id = "sciences",
            label = "Sciences",
            domainIds = listOf("biologie", "physique", "mathematiques", "medecine", "informatique")
        ),
        Theme(
            id = "litterature",
            label = "Littérature",
            domainIds = listOf("litterature_classique", "litterature_moderne", "poesie", "theatre")
        ),
        Theme(
            id = "sciences_humaines",
            label = "Sciences humaines",
            domainIds = listOf("philosophie", "histoire", "sociologie", "psychologie", "linguistique")
        ),
        Theme(
            id = "vie_quotidienne",
            label = "Vie quotidienne",
            domainIds = listOf("cuisine", "voyage", "sports", "medias", "vie_professionnelle")
        ),
        Theme(
            id = "technique",
            label = "Technique et professionnel",
            domainIds = listOf("droit", "economie", "ingenierie", "architecture", "commerce")
        ),
        Theme(
            id = "arts",
            label = "Arts et culture",
            domainIds = listOf("arts_visuels", "musique", "cinema", "photographie", "design")
        ),
        Theme(
            id = "societe",
            label = "Société et actualité",
            domainIds = listOf("politique", "environnement", "education", "sante_publique")
        ),
        Theme(
            id = "argot_dialecte",
            label = "Argots et dialectes",
            domainIds = listOf("argot_general", "argot_jeunesse", "dialectes_regionaux", "francophonie")
        )
    )

    /** Domaines V1 */
    val domains: List<Domain> = listOf(
        // Sciences
        Domain("biologie", "Biologie", "sciences"),
        Domain("physique", "Physique", "sciences"),
        Domain("mathematiques", "Mathématiques", "sciences"),
        Domain("medecine", "Médecine", "sciences"),
        Domain("informatique", "Informatique", "sciences"),
        // Littérature
        Domain("litterature_classique", "Littérature classique", "litterature"),
        Domain("litterature_moderne", "Littérature moderne", "litterature"),
        Domain("poesie", "Poésie", "litterature"),
        Domain("theatre", "Théâtre", "litterature"),
        // Sciences humaines
        Domain("philosophie", "Philosophie", "sciences_humaines"),
        Domain("histoire", "Histoire", "sciences_humaines"),
        Domain("sociologie", "Sociologie", "sciences_humaines"),
        Domain("psychologie", "Psychologie", "sciences_humaines"),
        Domain("linguistique", "Linguistique", "sciences_humaines"),
        // Vie quotidienne
        Domain("cuisine", "Cuisine", "vie_quotidienne"),
        Domain("voyage", "Voyage", "vie_quotidienne"),
        Domain("sports", "Sports", "vie_quotidienne"),
        Domain("medias", "Médias", "vie_quotidienne"),
        Domain("vie_professionnelle", "Vie professionnelle", "vie_quotidienne"),
        // Technique
        Domain("droit", "Droit", "technique"),
        Domain("economie", "Économie", "technique"),
        Domain("ingenierie", "Ingénierie", "technique"),
        Domain("architecture", "Architecture", "technique"),
        Domain("commerce", "Commerce", "technique"),
        // Arts
        Domain("arts_visuels", "Arts visuels", "arts"),
        Domain("musique", "Musique", "arts"),
        Domain("cinema", "Cinéma", "arts"),
        Domain("photographie", "Photographie", "arts"),
        Domain("design", "Design", "arts"),
        // Société
        Domain("politique", "Politique", "societe"),
        Domain("environnement", "Environnement", "societe"),
        Domain("education", "Éducation", "societe"),
        Domain("sante_publique", "Santé publique", "societe"),
        // Argots et dialectes
        Domain("argot_general", "Argot général", "argot_dialecte"),
        Domain("argot_jeunesse", "Argot jeunesse", "argot_dialecte"),
        Domain("dialectes_regionaux", "Dialectes régionaux", "argot_dialecte"),
        Domain("francophonie", "Francophonie", "argot_dialecte")
    )

    /** Registres V1 */
    val registers: List<Register> = listOf(
        Register("soutenu", "Soutenu"),
        Register("courant", "Courant"),
        Register("familier", "Familier"),
        Register("argotique", "Argotique"),
        Register("technique", "Technique")
    )

    /** Instance complète de la taxonomie V1 */
    val defaultTaxonomy: InterestTaxonomy = InterestTaxonomy(
        themes = themes,
        domains = domains,
        registers = registers
    )

    // -----------------------------------------------------------------------
    // Table de correspondance : UserObjective → thèmes/domaines/registres
    // -----------------------------------------------------------------------

    /**
     * Mapping entre les objectifs utilisateur déclarés et les dimensions
     * internes de la taxonomie.
     *
     * Ce mapping permet au moteur de recommandation de traduire
     * un objectif simple en signaux exploitables pour le scoring.
     */
    data class ObjectiveMapping(
        val themeIds: List<String>,
        val domainIds: List<String>,
        val registerIds: List<String>
    )

    /**
     * Table de correspondance complète.
     *
     * Règles de conception :
     * - Chaque objectif cible 1-3 thèmes principaux
     * - Les domaines précisent les thèmes
     * - Les registres indiquent le niveau de langue prioritaire
     */
    val objectiveMappings: Map<UserObjective, ObjectiveMapping> = mapOf(
        UserObjective.CONVERSATIONS_SOUTENUES to ObjectiveMapping(
            themeIds = listOf("litterature", "sciences_humaines", "vie_quotidienne"),
            domainIds = listOf(
                "litterature_classique", "litterature_moderne",
                "philosophie", "psychologie",
                "vie_professionnelle", "medias"
            ),
            registerIds = listOf("soutenu", "courant")
        ),
        UserObjective.TEXTES_EXIGEANTS to ObjectiveMapping(
            themeIds = listOf("sciences", "sciences_humaines", "litterature"),
            domainIds = listOf(
                "philosophie", "histoire", "sociologie",
                "biologie", "physique", "medecine",
                "litterature_classique", "poesie"
            ),
            registerIds = listOf("soutenu", "technique")
        ),
        UserObjective.DIALECTES_ARGOTS to ObjectiveMapping(
            themeIds = listOf("argot_dialecte", "societe", "arts"),
            domainIds = listOf(
                "argot_general", "argot_jeunesse",
                "dialectes_regionaux", "francophonie",
                "cinema", "musique", "medias"
            ),
            registerIds = listOf("familier", "argotique", "courant")
        ),
        UserObjective.DOMAINE_SPECIFIQUE to ObjectiveMapping(
            themeIds = listOf("technique", "sciences", "arts"),
            domainIds = listOf(
                "droit", "economie", "ingenierie",
                "informatique", "medecine",
                "architecture", "design"
            ),
            registerIds = listOf("technique", "soutenu", "courant")
        )
    )

    /**
     * Calcule la liste des identifiants de domaines recommandés
     * à partir d'une liste d'objectifs utilisateur.
     *
     * @param objectives Liste des objectifs déclarés
     * @return Liste des identifiants de domaines (dédupliquée)
     */
    fun domainsForObjectives(objectives: List<UserObjective>): List<String> =
        objectives.flatMap { objectiveMappings[it]?.domainIds.orEmpty() }.distinct()

    /**
     * Calcule la liste des identifiants de thèmes recommandés
     * à partir d'une liste d'objectifs utilisateur.
     *
     * @param objectives Liste des objectifs déclarés
     * @return Liste des identifiants de thèmes (dédupliquée)
     */
    fun themesForObjectives(objectives: List<UserObjective>): List<String> =
        objectives.flatMap { objectiveMappings[it]?.themeIds.orEmpty() }.distinct()

    /**
     * Calcule la liste des identifiants de registres recommandés
     * à partir d'une liste d'objectifs utilisateur.
     *
     * @param objectives Liste des objectifs déclarés
     * @return Liste des identifiants de registres (dédupliquée)
     */
    fun registersForObjectives(objectives: List<UserObjective>): List<String> =
        objectives.flatMap { objectiveMappings[it]?.registerIds.orEmpty() }.distinct()
}
