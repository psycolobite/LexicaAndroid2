/**
 * ExploreScreenSpec.kt — Spécification fonctionnelle et UX de l'écran de recherche principal
 *
 * TACHE_R1 — Architecture produit de l'écran recherche
 * Package : presentation/search/explore/
 *
 * Ce fichier est une maquette fonctionnelle + spécification.
 * Il n'est pas destiné à être compilé : il définit la structure, les états,
 * les interactions et les conventions de nommage pour le futur écran ExploreScreen.
 *
 * Destinataire : Chef d'Orchestre (validation UX) + TACHE_R6 (implémentation Compose)
 */

// =============================================================================
// 1. STRUCTURE DE L'ÉCRAN — Hiérarchie visuelle
// =============================================================================
//
// ┌──────────────────────────────────────────────────────┐
// │  TopAppBar                                           │
// │  [← Retour]  Explorer  [ℹ️ Info source]              │
// ├──────────────────────────────────────────────────────┤
// │                                                        │
// │  ┌──────────────────────────────────────────────────┐  │
// │  │              ZONE EXTRAT PRINCIPAL               │  │  ← weight(1f)
// │  │  (texte ou vidéo sous-titré)                     │  │
// │  │                                                  │  │
// │  │  "Le mot **suggestions** dans ce contexte        │  │
// │  │   montre bien que l'auteur cherchait à           │  │
// │  │   **explorer** de nouvelles **perspectives**     │  │
// │  │   sans jamais perdre de vue l'essentiel."        │  │
// │  │                                                  │  │
// │  │  [Source : Nom de l'ouvrage, Auteur, Année]      │  │
// │  └──────────────────────────────────────────────────┘  │
// │                                                        │
// │  ┌──────────────────────────────────────────────────┐  │
// │  │  ⭐ Signal d'intérêt (optionnel)   [1 2 3 4 5]  │  │  ← hauteur fixe ~48dp
// │  └──────────────────────────────────────────────────┘  │
// │                                                        │
// ├──────────────────────────────────────────────────────┤
// │  Barre basse secondaire                               │  ← hauteur fixe ~56dp
// │  [📚 Chercher des ouvrages]  [🔍 Rechercher des mots] │
// └──────────────────────────────────────────────────────┘
//
// Proportions recommandées :
// - TopAppBar : ~56dp
// - Zone extrait principal : weight(1f) — occupe tout l'espace restant
// - Signal d'intérêt : ~48dp (peut être masqué si non retenu)
// - Barre basse secondaire : ~56dp
//
// Comportement à l'ouverture :
// - En arrivant sur l'écran, un extrait est déjà affiché sans action préalable
// - Pas d'écran vide, pas de "Bienvenue" — on montre directement du contenu
// - Le premier extrait est choisi selon le profil utilisateur (ou aléatoire si inconnu)

// =============================================================================
// 2. ÉTATS DE L'ÉCRAN
// =============================================================================
//
// sealed class ExploreUiState {
//
//     /** Chargement en cours — premier extrait pas encore prêt */
//     object Loading : ExploreUiState()
//
//     /** État nominal : un extrait est affiché */
//     data class ExtractDisplayed(
//         val extract: ExtractUiModel,
//         val wordStates: Map<String, WordStatus>,  // mot -> statut visuel
//         val interestRating: Int?,                  // null = pas encore noté
//         val isTransitioning: Boolean               // true pendant swipe/scroll
//     ) : ExploreUiState()
//
//     /** Aucun extrait disponible (profil trop spécifique, corpus vide) */
//     data class NoExtractAvailable(
//         val reason: NoExtractReason,
//         val suggestion: String?    // suggestion pour débloquer
//     ) : ExploreUiState()
//
//     /** Erreur réseau / base de données */
//     data class Error(
//         val message: String,
//         val isRetryable: Boolean
//     ) : ExploreUiState()
// }
//
// enum class NoExtractReason {
//     CORPUS_EMPTY,           // pas encore de corpus chargé
//     PROFILE_TOO_NARROW,     // profil trop spécifique, aucun extrait match
//     ALL_CONSUMED,           // tous les extraits déjà vus
//     PENDING_INDEX           // indexation en cours
// }
//
// Transitions :
// - Loading → ExtractDisplayed (nominal)
// - Loading → NoExtractAvailable (si corpus vide)
// - Loading → Error (si erreur)
// - ExtractDisplayed → ExtractDisplayed (navigation entre extraits)
// - ExtractDisplayed → NoExtractAvailable (si plus d'extraits)
// - Error → Loading (retry)

// =============================================================================
// 3. INTERACTIONS UTILISATEUR
// =============================================================================
//
// ┌─────────────────────────────────────────────────────────────────────────┐
// │ Geste                  │ Zone              │ Effet                      │
// ├─────────────────────────────────────────────────────────────────────────┤
// │ Tap court              │ Mot surligné      │ Ajouter le mot au deck     │
// │                        │ (statut "suggéré")│ → passe en "ajouté"        │
// ├─────────────────────────────────────────────────────────────────────────┤
// │ Tap court              │ Mot surligné      │ Retirer le mot du deck     │
// │                        │ (statut "ajouté") │ → repasse en "suggéré"     │
// ├─────────────────────────────────────────────────────────────────────────┤
// │ Appui long             │ Mot surligné      │ Afficher définition        │
// │                        │                   │ (popup/bottom sheet)       │
// ├─────────────────────────────────────────────────────────────────────────┤
// │ Appui long             │ Mot non surligné  │ Afficher définition        │
// │                        │                   │ + proposition d'ajout      │
// ├─────────────────────────────────────────────────────────────────────────┤
// │ Swipe gauche/droite    │ Zone extrait      │ Extraits suivants/précéd.  │
// │ OU Scroll vertical     │                   │ (voir point ouvert §6.1)   │
// ├─────────────────────────────────────────────────────────────────────────┤
// │ Tap                    │ Note 1-5          │ Noter l'extrait            │
// ├─────────────────────────────────────────────────────────────────────────┤
// │ Tap                    │ "Chercher des     │ Naviguer vers Catalogue    │
// │                        │ ouvrages"         │                            │
// ├─────────────────────────────────────────────────────────────────────────┤
// │ Tap                    │ "Rechercher des   │ Naviguer vers SearchScreen │
// │                        │ mots"             │ existant                   │
// └─────────────────────────────────────────────────────────────────────────┘

// =============================================================================
// 4. ÉTATS VISUELS DES MOTS
// =============================================================================
//
// enum class WordStatus {
//
//     /** Mot non surligné — texte normal, pas d'interaction tap possible */
//     NORMAL,
//
//     /** Mot suggéré — surligné (ex: fond jaune clair #FFF8E1),
//      *  pas encore dans le deck de l'utilisateur.
//      *  Tap court → ajoute le mot. */
//     SUGGESTED,
//
//     /** Mot ajouté — surligné différemment (ex: fond vert clair #E8F5E9),
//      *  déjà dans le deck de l'utilisateur.
//      *  Tap court → retire le mot. */
//     ADDED,
//
//     /** Mot en cours de transition — animation avant changement d'état */
//     TRANSITIONING
// }
//
// Règles visuelles :
// - SUGGESTED : fond pastel jaune, texte normal, pas de bordure
// - ADDED : fond pastel vert, texte normal, icône check invisible
// - NORMAL : pas de fond, texte normal
// - Les mots SUGGESTED et ADDED sont cliquables (tap court)
// - Les mots NORMAL ne répondent qu'à l'appui long
// - Maximum ~5 mots surlignés par extrait pour éviter la surcharge visuelle

// =============================================================================
// 5. CONVENTIONS DE NOMMAGE
// =============================================================================
//
// Package : com.example.lexicaandroid2.presentation.search.explore
//
// --- Composables ---
// ExploreScreen              — écran principal (Scaffold + composition des zones)
// ExtractContent             — zone d'affichage de l'extrait (texte ou vidéo)
// TextExtractContent         — rendu texte avec mots surlignés
// VideoExtractContent        — rendu vidéo avec sous-titres
// HighlightedWord            — composable pour un mot unique (gère tap/longPress)
// InterestRatingBar          — barre de note 0-5
// ExploreBottomBar           — barre basse (ouvrages + recherche mots)
// ExtractSourceInfo          — affichage source (ouvrage, auteur, année)
// WordDefinitionSheet        — bottom sheet de définition
//
// --- ViewModel ---
// ExploreViewModel           — logique UI, état, navigation entre extraits
//
// --- États ViewModel ---
// ExploreUiState             — sealed class (Loading, ExtractDisplayed, etc.)
// ExtractUiModel             — données affichées d'un extrait
// WordStatus                 — enum (NORMAL, SUGGESTED, ADDED, TRANSITIONING)
// InterestRating             — Int (0-5) ou null
//
// --- Repository (futur) ---
// ExtractRepository          — interface pour obtenir les extraits
// (créé dans TACHE_R3 ou TACHE_R5)
//
// --- Structure de SearchUiState (pour le futur ViewModel) ---
//
// data class SearchUiState(
//     val currentExtract: ExtractUiModel?,
//     val wordStatuses: Map<String, WordStatus>,  // mot -> statut
//     val isLoading: Boolean,
//     val isTransitioning: Boolean,
//     val error: String?,
//     val noExtractReason: NoExtractReason?,
//     val interestRating: Int?,                    // null = pas noté
//     val historyIndex: Int,                       // position dans l'historique
//     val historySize: Int                         // nombre total d'extraits dispo
// )
//
// data class ExtractUiModel(
//     val id: String,
//     val content: String,                         // texte de l'extrait
//     val highlightedWords: List<String>,          // mots suggérés
//     val sourceTitle: String,
//     val sourceAuthor: String,
//     val sourceYear: String?,
//     val sourceUrl: String?,                      // lien vers source complète
//     val contentType: ExtractContentType,         // TEXT ou VIDEO
//     val videoUrl: String?,                       // si contentType == VIDEO
//     val domainTags: List<String>,                // thèmes/domaines
//     val difficulty: String                       // "facile", "moyen", "avancé"
// )
//
// enum class ExtractContentType { TEXT, VIDEO }

// =============================================================================
// 6. POINTS OUVERTS À ARBITRER PAR LE CHEF D'ORCHESTRE
// =============================================================================
//
// 6.1 Navigation entre extraits : swipe vs scroll
// - Option A : Swipe horizontal gauche/droite (type stories)
//   Avantage : geste naturel, pas de conflit avec scroll de lecture
//   Inconvénient : moins standard pour du texte long
// - Option B : Scroll vertical (type feed TikTok)
//   Avantage : standard mobile, infini
//   Inconvénient : conflit possible avec le scroll de l'extrait si texte long
// - Option C : Swipe vertical (type stories verticales)
//   Avantage : cohérent avec les réseaux sociaux
//   Inconvénient : moins familier pour du texte
//
// Recommandation : Option A (swipe horizontal) pour V1, car :
//   - Pas de conflit avec le scroll interne de l'extrait
//   - Gestuel clair et distinct
//   - Permet d'afficher un indicateur de progression (dots)
//
// 6.2 Signal d'intérêt explicite vs implicite seulement
// - Faut-il une barre de note 0-5 visible en permanence ?
// - Ou se contenter des signaux implicites (mots ajoutés, temps passé, swipe) ?
//
// Recommandation : barre de note légère (1-5) en bas de l'extrait, repliable,
// pour avoir un signal explicite dès la V1 sans alourdir l'interface.
//
// 6.3 Gestion des conflits de gestes
// - Tap sur mot vs swipe pour navigation
// - Appui long vs défilement
// - Solution proposée :
//   * La zone de texte gère les taps et appuis longs sur les mots
//   * Le swipe est capté en dehors des zones de texte (bordures)
//   * Un mot en cours d'interaction (tap) bloque le swipe le temps du geste
//
// 6.4 Extrait vidéo avec sous-titres
// - Comment afficher les mots surlignés dans une vidéo ?
// - Option A : sous-titres enrichis (les mots surlignés dans le sous-titre)
// - Option B : bandeau séparé sous la vidéo avec les mots clés
// - Option C : pas de vidéo en V1 (texte seulement)
//
// Recommandation : Option C pour la V1 (texte seulement), préparer l'architecture
// pour la vidéo en V2.
//
// 6.5 Nombre maximum de mots surlignés par extrait
// - Proposition : 3 à 5 mots par extrait
// - Au-delà, risque de surcharge cognitive
// - À ajuster selon les retours utilisateur
//
// 6.6 Comportement du "Mot ajouté" si déjà dans le deck
// - Si le mot est déjà dans le deck avant d'arriver sur l'extrait :
//   * Option A : afficher directement en statut ADDED
//   * Option B : afficher en SUGGESTED quand même (permet de le re-ajouter ?)
// - Recommandation : Option A — éviter la confusion, montrer l'état réel
//
// 6.7 Animation de transition entre extraits
// - Proposition : animation de slide horizontale (300ms, easing standard)
// - Les mots surlignés du nouvel extrait apparaissent avec un léger délai (50ms chacun)
// - Pas d'animation si l'utilisateur navigue rapidement (skip)

// =============================================================================
// 7. RÈGLES DE GESTION DES ERREURS ET CAS LIMITES
// =============================================================================
//
// - Si le chargement échoue → état Error avec bouton "Réessayer"
// - Si le corpus est vide → état NoExtractAvailable avec suggestion
//   "Ajoute des mots à ta liste pour recevoir des suggestions personnalisées"
// - Si l'extrait est trop court (< 50 caractères) → ne pas l'afficher, passer au suivant
// - Si un mot surligné est vide ou null → l'ignorer silencieusement
// - Si la source est inconnue → afficher "Source inconnue" au lieu de cacher l'info
// - Si l'utilisateur swipe rapidement (> 3 extraits en 5 secondes) →
//   ralentir le chargement pour éviter de brûler le pool de candidats
// - Si l'utilisateur revient en arrière (swipe précédent) →
//   restaurer l'état exact (mots ajoutés/retirés, note) depuis le cache local

// =============================================================================
// 8. DÉPENDANCES ET INTÉGRATION
// =============================================================================
//
// Dépendances pour l'implémentation (TACHE_R6) :
// - Jetpack Compose (déjà présent)
// - Material3 (déjà présent)
// - ViewModel + StateFlow (déjà présent)
// - Swipe détection : Modifier.pointerInput ou HorizontalPager (Pager Compose)
//
// Intégration dans LexicaApp.kt (par le Chef d'Orchestre) :
// - Ajouter Screen.Explore dans sealed class Screen
// - Ajouter composable("explore") { ExploreScreen(...) }
// - Ajouter navigation depuis le Dashboard
//
// Intégration dans Screen.kt (par le Chef d'Orchestre) :
// - Ajouter object Explore : Screen("explore")
