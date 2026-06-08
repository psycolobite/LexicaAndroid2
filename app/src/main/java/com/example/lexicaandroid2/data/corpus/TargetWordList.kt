package com.example.lexicaandroid2.data.corpus

import com.example.lexicaandroid2.presentation.search.preferences.InterestTaxonomyProvider
import com.example.lexicaandroid2.presentation.search.preferences.UserObjective
import com.example.lexicaandroid2.presentation.search.preferences.UserPreferences

/**
 * Modèle représentant un mot cible pondéré.
 *
 * Un mot cible est un mot susceptible d'intéresser l'utilisateur,
 * généré à partir de ses préférences déclarées, de ses thèmes
 * et de ses comportements observés.
 *
 * @property word Le mot lui-même
 * @property weight Poids/importance relative (0.0-1.0)
 * @property source Origine de ce mot cible
 * @property domainIds Identifiants des domaines associés
 */
data class TargetWord(
    val word: String,
    val weight: Float,
    val source: TargetSource,
    val domainIds: List<String>
)

/**
 * Origine d'un mot cible.
 */
enum class TargetSource {
    /** Dérivé des préférences déclarées de l'utilisateur */
    USER_PREFERENCE,

    /** Basé sur la fréquence ou la rareté dans les corpus */
    FREQUENCY_BASED,

    /** Spécifique à un domaine thématique */
    DOMAIN_SPECIFIC,

    /** Mot exploratoire (hors zone de confort) */
    EXPLORATORY
}

/**
 * Liste de mots cibles générée pour un utilisateur.
 *
 * @property words Les mots cibles avec leur pondération
 * @property generatedAt Timestamp de génération
 * @property profileSnapshot Hash du profil utilisateur utilisé
 */
data class TargetWordList(
    val words: List<TargetWord>,
    val generatedAt: Long = System.currentTimeMillis(),
    val profileSnapshot: String = ""
)

/**
 * Générateur de listes de mots cibles à partir des préférences utilisateur.
 *
 * Utilise la taxonomie et le mapping objectifs → domaines de TACHE_R2
 * pour produire une liste pondérée de mots pertinents.
 */
object TargetWordGenerator {

    /**
     * Génère une liste de mots cibles à partir des préférences utilisateur.
     *
     * Stratégie de pondération :
     * - Mots directement liés aux domaines préférés → poids fort (0.8-1.0)
     * - Mots génériques transverses → poids moyen (0.5-0.7)
     * - Mots exploratoires (domaines voisins) → poids faible (0.2-0.4)
     *
     * @param preferences Préférences déclarées de l'utilisateur
     * @return Une TargetWordList avec les mots générés
     */
    fun generateTargetWords(preferences: UserPreferences): TargetWordList {
        val words = mutableListOf<TargetWord>()

        // 1. Récupérer les domaines cibles depuis les objectifs
        val targetDomainIds = InterestTaxonomyProvider.domainsForObjectives(preferences.objectives)

        // 2. Générer des mots pour chaque domaine prioritaire
        for (domainId in targetDomainIds) {
            val domainWords = generateWordsForDomain(domainId, weight = 0.9f)
            words.addAll(domainWords)
        }

        // 3. Ajouter des mots génériques transverses (poids moyen)
        val genericWords = generateGenericWords(preferences.objectives)
        words.addAll(genericWords)

        // 4. Ajouter des mots exploratoires (poids faible)
        val exploratoryWords = generateExploratoryWords(preferences.preferredDomains)
        words.addAll(exploratoryWords)

        // 5. Limiter le nombre total de mots et dédupliquer
        val deduplicated = words
            .groupBy { it.word.lowercase() }
            .map { (_, duplicates) ->
                duplicates.maxByOrNull { it.weight } ?: duplicates.first()
            }
            .sortedByDescending { it.weight }
            .take(200) // limite raisonnable pour une V1

        return TargetWordList(
            words = deduplicated,
            profileSnapshot = computeProfileSnapshot(preferences)
        )
    }

    /**
     * Génère des mots pour un domaine spécifique.
     *
     * @param domainId Identifiant du domaine
     * @param weight Poids à attribuer aux mots
     * @return Liste de mots cibles pour ce domaine
     */
    private fun generateWordsForDomain(domainId: String, weight: Float): List<TargetWord> {
        // Mots génériques par domaine — V1 statique, extensible plus tard
        return when (domainId) {
            "biologie" -> listOf(
                TargetWord("photosynthèse", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("homéostasie", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("mitose", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("symbiose", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("métabolisme", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId))
            )
            "physique" -> listOf(
                TargetWord("thermodynamique", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("électromagnétisme", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("quantique", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("entropie", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("réfraction", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId))
            )
            "philosophie" -> listOf(
                TargetWord("épistémologie", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("phénoménologie", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("dialectique", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("ontologie", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("herméneutique", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId))
            )
            "litterature_classique" -> listOf(
                TargetWord("alexandrin", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("périphrase", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("pléonasme", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("chiasme", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("énallage", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId))
            )
            "cuisine" -> listOf(
                TargetWord("cannelé", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("fumet", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("mijoter", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("émincer", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("déglacer", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId))
            )
            "droit" -> listOf(
                TargetWord("jurisprudence", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("prérogative", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("usufruit", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("caution", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("prescription", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId))
            )
            "medecine" -> listOf(
                TargetWord("diagnostic", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("prophylaxie", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("iatrogène", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("comorbidité", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("nosocomial", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId))
            )
            "informatique" -> listOf(
                TargetWord("polymorphisme", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("abstraction", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("récurrence", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("parallélisme", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("encapsulation", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId))
            )
            "argot_general" -> listOf(
                TargetWord("chelou", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("ouf", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("relou", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("kiffer", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("boloss", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId))
            )
            "environnement" -> listOf(
                TargetWord("anthropocène", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("biodiversité", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("résilience", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("écosystème", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId)),
                TargetWord("permaculture", weight, TargetSource.DOMAIN_SPECIFIC, listOf(domainId))
            )
            else -> emptyList()
        }
    }

    /**
     * Génère des mots génériques transverses selon les objectifs.
     *
     * @param objectives Liste des objectifs utilisateur
     * @return Liste de mots cibles génériques
     */
    private fun generateGenericWords(objectives: List<UserObjective>): List<TargetWord> {
        val words = mutableListOf<TargetWord>()
        val weight = 0.6f

        for (objective in objectives) {
            when (objective) {
                UserObjective.CONVERSATIONS_SOUTENUES -> words.addAll(
                    listOf(
                        TargetWord("néanmoins", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("cependant", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("subséquent", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("conséquent", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("ultérieurement", weight, TargetSource.USER_PREFERENCE, emptyList())
                    )
                )
                UserObjective.TEXTES_EXIGEANTS -> words.addAll(
                    listOf(
                        TargetWord("paradigme", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("heuristique", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("axiome", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("postulat", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("corollaire", weight, TargetSource.USER_PREFERENCE, emptyList())
                    )
                )
                UserObjective.DIALECTES_ARGOTS -> words.addAll(
                    listOf(
                        TargetWord("verlan", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("argot", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("jargon", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("dialecte", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("patois", weight, TargetSource.USER_PREFERENCE, emptyList())
                    )
                )
                UserObjective.DOMAINE_SPECIFIQUE -> words.addAll(
                    listOf(
                        TargetWord("spécialisation", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("expertise", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("compétence", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("maîtrise", weight, TargetSource.USER_PREFERENCE, emptyList()),
                        TargetWord("vocabulaire", weight, TargetSource.USER_PREFERENCE, emptyList())
                    )
                )
            }
        }

        return words
    }

    /**
     * Génère des mots exploratoires (domaines voisins non préférés).
     *
     * @param preferredDomains Domaines déjà préférés par l'utilisateur
     * @return Liste de mots exploratoires
     */
    private fun generateExploratoryWords(preferredDomains: List<String>): List<TargetWord> {
        // Mots exploratoires génériques — poids faible
        return listOf(
            TargetWord("anachronisme", 0.3f, TargetSource.EXPLORATORY, emptyList()),
            TargetWord("ubiquité", 0.3f, TargetSource.EXPLORATORY, emptyList()),
            TargetWord("pléthore", 0.3f, TargetSource.EXPLORATORY, emptyList()),
            TargetWord("dichotomie", 0.3f, TargetSource.EXPLORATORY, emptyList()),
            TargetWord("amalgame", 0.3f, TargetSource.EXPLORATORY, emptyList()),
            TargetWord("conjecture", 0.3f, TargetSource.EXPLORATORY, emptyList()),
            TargetWord("réminiscence", 0.3f, TargetSource.EXPLORATORY, emptyList()),
            TargetWord("superfétatoire", 0.2f, TargetSource.EXPLORATORY, emptyList()),
            TargetWord("rocambolesque", 0.2f, TargetSource.EXPLORATORY, emptyList()),
            TargetWord("rocailleux", 0.2f, TargetSource.EXPLORATORY, emptyList())
        )
    }

    /**
     * Calcule un hash du profil utilisateur pour identifier
     * la version du profil utilisé lors de la génération.
     */
    private fun computeProfileSnapshot(preferences: UserPreferences): String {
        val data = preferences.objectives.sortedBy { it.name }.joinToString(",") +
                preferences.preferredDomains.sorted().joinToString(",") +
                preferences.preferredRegisters.sorted().joinToString(",")
        return data.hashCode().toString()
    }
}
