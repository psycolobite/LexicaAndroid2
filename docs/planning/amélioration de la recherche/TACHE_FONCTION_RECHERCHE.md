# TACHE_FONCTION_RECHERCHE — Decoupage Agents IA

## 1. Role du document

Ce document sert de base de decoupage du chantier "amelioration de la recherche" en lots confiables pour agents IA.

Il complete :
- `DESCRIPTION.md` pour la vision produit,
- `PLAN.md` pour la logique de mise en route,
- `docs/guides/CONSIGNES_TACHES.md` pour les regles globales d'execution.

Chaque lot ci-dessous doit rester dans un ordre de grandeur compatible avec un agent IA autonome, vise ici autour de `200 000` a `300 000` tokens maximum.

---

## 2. Regles de decoupage

Chaque tache doit autant que possible :
- avoir une responsabilite claire,
- modifier un sous-ensemble limite de fichiers,
- produire une sortie testable ou relisable,
- eviter de melanger moteur de contenu, moteur de ranking et UI dans un seul lot,
- laisser les integrations globales au Chef d'Orchestre quand elles touchent les fichiers coeur.

Le Chef d'Orchestre doit verifier avant envoi :
- que la tache n'exige pas plusieurs architectures lourdes a la fois,
- que le perimetre est suffisamment autonome,
- que les livrables attendus sont explicites,
- que la tache ne suppose pas un build local si elle est destinee a un agent developpeur ordinaire.

---

## 3. Ordre recommande d'activation

Ordre recommande :
1. `TACHE_R1` — cadrage fonctionnel de l'ecran recherche
2. `TACHE_R2` — preferences utilisateur et taxonomie thematique
3. `TACHE_R3` — pipeline extraits a partir des corpus
4. `TACHE_R4` — pipeline mots cibles vers extraits
5. `TACHE_R5` — moteur de scoring et ranking
6. `TACHE_R6` — UI interactive de la page extrait
7. `TACHE_R7` — catalogue ouvrages et acces source complete
8. `TACHE_R8` — integration du mode secondaire "recherche de mots"
9. `TACHE_R9` — profil d'interet par domaine et evenements utilisateur

Remarque : `TACHE_R3` et `TACHE_R4` peuvent avancer en parallele si les interfaces de donnees sont definies assez tot.

---

## 4. Lots proposes

## TACHE_R1 — Architecture produit de l'ecran recherche

### 📦 Périmètre de livraison
- **Package cible :** `presentation/search/explore/` (à créer)
- **Fichiers à créer :** `ExploreScreenSpec.kt` (maquette fonctionnelle + spécifications)
- **Fichiers existants à modifier :** aucun
- **Fichier d'intégration :** `integration_pending/tache_r1_pr.md`
- **Actions Chef d'Orchestre :** Lire la spec, valider les choix UX, arbitrer les points ouverts

### Objectif
Definir de maniere exploitable la structure de l'ecran recherche principal et ses etats UX, sans encore traiter la complexite du moteur de contenu.

### Perimetre
- formaliser les sections de l'ecran,
- decrire les etats principaux de la page extrait,
- fixer la hiérarchie entre extrait principal et acces secondaires,
- definir les gestes principaux et leurs conflits possibles,
- proposer une maquette fonctionnelle ou pseudo-maquette detaillee.

### Sorties attendues
- specification UX exploitable,
- liste des etats ecran,
- liste des interactions utilisateur,
- conventions d'etat visuel pour les mots,
- decisions ouvertes a arbitrer par le Chef d'Orchestre.

### Hors perimetre
- implementation finale du ranking,
- ingestion de corpus,
- integration juridique des sources.

### Complexite cible
- moyenne a forte
- compatible avec un agent unique si la mission reste documentaire/UX et peu codee.

---

## TACHE_R2 — Preferences utilisateur et taxonomie des interets

### 📦 Périmètre de livraison
- **Package cible :** `presentation/search/preferences/` (à créer)
- **Fichiers à créer :** `UserPreferences.kt` (modèle de données), `InterestTaxonomy.kt` (taxonomie), `UserPreferencesRepository.kt` (stockage)
- **Fichiers existants à modifier :** aucun
- **Fichier d'intégration :** `integration_pending/tache_r2_pr.md`
- **Actions Chef d'Orchestre :** Copier les fichiers, ajouter le repository dans le DI si nécessaire

### Objectif
Construire le socle de personnalisation declaree au premier lancement.

### Perimetre
- definir la liste initiale des objectifs utilisateur,
- proposer une taxonomie simple themes/domaines/registres,
- definir la structure de donnees minimale pour stocker ces preferences,
- preparer le mapping entre objectifs declares et dimensions internes du moteur de recommandation.

### Sorties attendues
- modele de preferences utilisateur,
- taxonomie V1 exploitable,
- table de correspondance entre objectifs utilisateur et themes internes,
- liste des questions ouvertes pour itération future.

### Hors perimetre
- scoring dynamique en production,
- analyse des comportements utilisateur a grande echelle.

### Complexite cible
- moyenne
- bon candidat pour un agent orienté produit/data model.

---

## TACHE_R3 — Pipeline corpus vers extraits candidats

### 📦 Périmètre de livraison
- **Package cible :** `data/corpus/` (à créer)
- **Fichiers à créer :** `CorpusSource.kt` (modèle), `ExtractCandidate.kt` (schéma extrait), `CorpusParser.kt` (segmentation), `CorpusIndex.kt` (indexation)
- **Fichiers existants à modifier :** aucun
- **Fichier d'intégration :** `integration_pending/tache_r3_pr.md`
- **Actions Chef d'Orchestre :** Copier les fichiers, intégrer dans le DI si nécessaire

### Objectif
Mettre en place la variante `extraits d'abord` : partir des corpus, en extraire des passages candidats et leur associer des metadonnees exploitables sans IA temps reel.

### Perimetre
- definir le format d'entree des corpus,
- proposer la logique de decoupage en extraits,
- definir les metadonnees minimales par extrait,
- proposer les criteres automatiques de qualite de contexte,
- preparer une petite chaine de traitement V1 sur corpus pilote.

### Sorties attendues
- schema d'extrait candidat,
- regles de segmentation,
- regles de filtrage de qualite,
- sortie testable sur un mini-corpus.

### Hors perimetre
- UI Compose,
- moteur final de ranking personnalise,
- gestion complete des videos.

### Dependances
- depend legerement de `TACHE_R2` pour le vocabulaire thematique,
- peut demarrer avec une taxonomie provisoire.

### Complexite cible
- forte
- bon lot technique autonome pour un agent data/pipeline.

---

## TACHE_R4 — Pipeline mots cibles vers extraits d'usage

### 📦 Périmètre de livraison
- **Package cible :** `data/corpus/` (dans le même package que R3)
- **Fichiers à créer :** `TargetWordList.kt` (modèle mots cibles), `OccurrenceSearcher.kt` (recherche occurrences), `ContextFilter.kt` (filtrage qualité contexte)
- **Fichiers existants à modifier :** aucun
- **Fichier d'intégration :** `integration_pending/tache_r4_pr.md`
- **Actions Chef d'Orchestre :** Copier les fichiers, vérifier la cohérence avec R3

### Objectif
Mettre en place la variante `mots cibles d'abord` : produire une liste de mots susceptibles d'interesser l'utilisateur, puis rechercher des usages pertinents dans les corpus.

### Perimetre
- definir la structure d'une liste de mots cibles ponderes,
- proposer comment generer cette liste a partir des preferences et themes,
- definir la recherche d'occurrences dans les corpus,
- filtrer les passages pour ne retenir que les usages clairs et utiles,
- comparer les avantages et limites de cette variante pour une V1.

### Sorties attendues
- schema des mots cibles,
- logique de ponderation initiale,
- logique de recherche d'occurrences,
- regles de selection de contexte,
- mini-demonstration sur un jeu de donnees reduit si possible.

### Hors perimetre
- UI de la page extrait,
- politique juridique complete des sources,
- scoring comportemental avance.

### Dependances
- depend de `TACHE_R2`,
- peut s'executer en parallele avec `TACHE_R3`.

### Complexite cible
- forte
- bon lot pour un agent concentre sur recherche lexicale/indexation.

---

## TACHE_R5 — Moteur de scoring et ranking des propositions

### 📦 Périmètre de livraison
- **Package cible :** `domain/recommendation/` (à créer)
- **Fichiers à créer :** `ScoringEngine.kt` (moteur de score), `RankingStrategy.kt` (stratégie de ranking), `ExplorationPolicy.kt` (exploration/exploitation)
- **Fichiers existants à modifier :** aucun
- **Fichier d'intégration :** `integration_pending/tache_r5_pr.md`
- **Actions Chef d'Orchestre :** Copier les fichiers, intégrer dans le DI

### Objectif
Concevoir puis prototyper le moteur qui choisit quel extrait montrer et dans quel ordre.

### Perimetre
- definir les signaux explicites et implicites utilisateur,
- proposer une formule V1 de scoring explicable,
- integrer l'exploration/exploitation,
- definir le reranking en fonction des interactions,
- proposer une strategie simple de pool de candidats.

### Sorties attendues
- formule de score V1,
- schema des evenements utilisateur utiles,
- regles de reranking,
- strategie d'exploration V1,
- criteres de debugabilite du moteur.

### Hors perimetre
- implementation UI finale,
- extraction brute des corpus,
- police juridique des sources.

### Dependances
- s'appuie sur `TACHE_R2` et au moins une des deux voies `TACHE_R3` ou `TACHE_R4`.

### Complexite cible
- forte
- bon lot pour un agent orienté algorithmes/recommandation.

---

## TACHE_R6 — UI interactive de la page extrait

### 📦 Périmètre de livraison
- **Package cible :** `presentation/search/explore/` (dans le même package que R1)
- **Fichiers à créer :** `ExploreScreen.kt` (écran principal), `ExploreViewModel.kt` (logique UI), `ExtractRenderer.kt` (rendu des extraits avec mots surlignés)
- **Fichiers existants à modifier :** `presentation/search/SearchScreen.kt` (aucun — l'écran existant est conservé tel quel)
- **Fichier d'intégration :** `integration_pending/tache_r6_pr.md`
- **Actions Chef d'Orchestre :** Copier les fichiers, ajouter la route dans `LexicaApp.kt` et `Screen.kt`, brancher le ViewModel dans le DI

### Objectif
Construire l'experience principale cote interface : affichage de l'extrait, mots surlignes, interactions mot par mot, navigation entre extraits.

### Perimetre
- ecran principal d'extrait,
- rendu des mots surlignes,
- etat visuel `mot suggere` / `mot ajoute`,
- tap d'ajout/retrait,
- appui long pour definition,
- prototype de navigation entre extraits,
- zone basse de signal d'interet si retenue.

### Sorties attendues
- implementation UI ou prototype Compose exploitable,
- etats UI clairs,
- modeles d'interaction raccord avec les specs,
- liste des arbitrages UX restants.

### Hors perimetre
- moteur complet de recommandation,
- catalogue d'ouvrages complet,
- sourcing juridique.

### Dependances
- depend fortement de `TACHE_R1`,
- peut travailler avec un faux flux d'extraits avant l'integration du vrai moteur.

### Complexite cible
- forte
- bon lot agent frontend/Compose.

---

## TACHE_R7 — Catalogue d'ouvrages et acces a la source complete

### 📦 Périmètre de livraison
- **Package cible :** `presentation/search/catalogue/` (à créer)
- **Fichiers à créer :** `CatalogueScreen.kt`, `CatalogueViewModel.kt`, `CatalogueRepository.kt`
- **Fichiers existants à modifier :** aucun
- **Fichier d'intégration :** `integration_pending/tache_r7_pr.md`
- **Actions Chef d'Orchestre :** Copier les fichiers, ajouter la route dans `LexicaApp.kt`

### Objectif
Creer le sous-espace `Chercher des ouvrages` et le mecanisme de transition entre extrait recommande et contenu complet.

### Perimetre
- structure du catalogue de contenus complets,
- logique de navigation depuis l'extrait vers la source,
- priorisation de contenus compatibles avec les preferences utilisateur,
- prise en compte des cas `contenu complet disponible` vs `simple lien externe`.

### Sorties attendues
- specification du catalogue,
- flux de navigation vers la source complete,
- distinction claire entre lecture integree et redirection,
- points juridiques a valider avant integration large.

### Hors perimetre
- moteur global d'extraction de corpus,
- ranking feed principal complet,
- remplacement du moteur de recherche de mots existant.

### Dependances
- depend de la definition des sources et contraintes legales,
- peut commencer en mode catalogue pilote.

### Complexite cible
- moyenne a forte.

---

## TACHE_R8 — Integration du mode secondaire recherche de mots

### 📦 Périmètre de livraison
- **Package cible :** `presentation/search/` (fichiers existants)
- **Fichiers à créer :** aucun
- **Fichiers existants à modifier :** `presentation/search/SearchScreen.kt` (ajouter un mode "compact" ou "intégré"), `presentation/search/SearchViewModel.kt` (adaptation mineure)
- **Fichier d'intégration :** `integration_pending/tache_r8_pr.md`
- **Actions Chef d'Orchestre :** 
  - Modifier `LexicaApp.kt` pour ajouter la navigation ExploreScreen → SearchScreen
  - Modifier `presentation/navigation/Screen.kt` si nouvelle route nécessaire
  - Vérifier la cohabitation des deux flux

### Objectif
Reconnecter proprement la page de recherche actuelle comme fonctionnalite secondaire accessible depuis la nouvelle page recherche principale.

### Perimetre
- definir le point d'entree `Rechercher des mots`,
- conserver la page existante telle que possible,
- verifier la clarte de navigation entre page extrait principale et page recherche de mots,
- reduire les duplications ou ambiguïtés d'usage entre les deux parcours.

### Sorties attendues
- spec d'integration du flux secondaire,
- points de navigation a modifier,
- regles de cohabitation entre recherche contextuelle et ajout direct de mots,
- liste de regressions UX a surveiller.

### Hors perimetre
- reimplementation totale du moteur actuel de recherche de mots,
- nouveau ranking d'extraits.

### Dependances
- depend de `TACHE_R1`,
- peut s'integrer apres `TACHE_R6`.

### Complexite cible
- moyenne.

---

## TACHE_R9 — Profil d'interet par domaine et evenements utilisateur

### 📦 Périmètre de livraison
- **Package cible :** `features/recommendation/` (à créer)
- **Fichiers à créer :** `InterestEvent.kt` (modèle événements), `InterestProfile.kt` (profil dérivé), `InterestProfileCalculator.kt` (calcul du score agrégé)
- **Fichiers existants à modifier :** aucun
- **Fichier d'intégration :** `integration_pending/tache_r9_pr.md`
- **Actions Chef d'Orchestre :** Copier les fichiers, intégrer dans le DI, éventuellement ajouter une table Room

### Objectif
Construire le socle qui permettra au moteur de recherche de comprendre durablement les domaines qui interessent l'utilisateur.

### Perimetre
- definir les evenements utilisateur qui influencent l'interet,
- definir le schema de stockage des signaux d'interet,
- proposer le calcul d'un score agrege par domaine,
- definir comment les choix initiaux et les comportements reels se combinent,
- preparer la compatibilite avec la future synchronisation multi-appareils.

### Sorties attendues
- liste des `InterestEvent` utiles,
- schema d'un `InterestProfile` derive,
- formule V1 de calcul ou de ponderation,
- regles de decroissance ou d'actualisation dans le temps,
- points de raccord avec le moteur de recommandation.

### Hors perimetre
- synchronisation cloud complete,
- implementation finale de la recommandation,
- interface finale de consultation du profil.

### Dependances
- depend fortement de `TACHE_R2`,
- nourrit ensuite `TACHE_R5`.

### Complexite cible
- moyenne a forte.

---

## 5. Conseils de mission au Chef d'Orchestre

Quand une tache est envoyee a un agent IA, le prompt devrait toujours preciser :
- l'objectif fonctionnel exact,
- le perimetre autorise,
- les fichiers ou packages cibles,
- ce qui est explicitement hors scope,
- le format de livraison attendu,
- les tests ou validations minimales attendues,
- la contrainte de taille raisonnable du chantier.

Formulation utile :
- "Tu traites uniquement `TACHE_Rx`. Si une integration globale est necessaire, note-la dans un document d'integration au lieu d'etendre ton perimetre."

---

## 6. Lots prioritaires pour une premiere vague

Si le Chef d'Orchestre doit lancer seulement 3 ou 4 agents au debut, l'ordre le plus robuste est :
1. `TACHE_R1`
2. `TACHE_R2`
3. `TACHE_R3`
4. `TACHE_R6`

Puis dans une deuxieme vague :
1. `TACHE_R4`
2. `TACHE_R5`
3. `TACHE_R7`
4. `TACHE_R8`

`TACHE_R9` peut etre lancee en parallele de `TACHE_R5` si la taxonomie thematique est deja suffisamment stable.

---

## 7. Resultat attendu

Une fois ce document complete et utilise, le prochain Chef d'Orchestre doit pouvoir :
- distribuer le travail sans recroiser en permanence les scopes,
- lancer plusieurs agents en parallele sans collision majeure,
- garder la vision produit intacte,
- faire converger progressivement vers une V1 realiste de la nouvelle fonction de recherche.

---

## 8. Récapitulatif des intégrations Chef d'Orchestre

Ce tableau récapitule toutes les actions que le Chef d'Orchestre devra effectuer après réception des livrables agents.

| Tâche | Fichier intégration | Packages créés | Actions Chef |
|-------|---------------------|----------------|--------------|
| R1 | `integration_pending/tache_r1_pr.md` | `presentation/search/explore/` | Lire et valider la spec UX |
| R2 | `integration_pending/tache_r2_pr.md` | `presentation/search/preferences/` | Copier fichiers, intégrer DI |
| R3 | `integration_pending/tache_r3_pr.md` | `data/corpus/` | Copier fichiers, intégrer DI |
| R4 | `integration_pending/tache_r4_pr.md` | `data/corpus/` | Copier fichiers, vérifier cohérence R3 |
| R5 | `integration_pending/tache_r5_pr.md` | `domain/recommendation/` | Copier fichiers, intégrer DI |
| R6 | `integration_pending/tache_r6_pr.md` | `presentation/search/explore/` | Copier fichiers, ajouter route dans `LexicaApp.kt` + `Screen.kt`, brancher ViewModel |
| R7 | `integration_pending/tache_r7_pr.md` | `presentation/search/catalogue/` | Copier fichiers, ajouter route dans `LexicaApp.kt` |
| R8 | `integration_pending/tache_r8_pr.md` | `presentation/search/` (existants) | Modifier `LexicaApp.kt` pour navigation Explore → Search, modifier `Screen.kt` si besoin |
| R9 | `integration_pending/tache_r9_pr.md` | `features/recommendation/` | Copier fichiers, intégrer DI, éventuelle table Room |

### Fichiers coeur impactés (à intégrer par le Chef d'Orchestre uniquement)

| Fichier | Modifications attendues | Par qui |
|---------|------------------------|---------|
| `LexicaApp.kt` | Ajout route `ExploreScreen`, route `CatalogueScreen`, navigation Explore → Search | Chef d'Orchestre |
| `presentation/navigation/Screen.kt` | Ajout `Screen.Explore`, `Screen.Catalogue` si nécessaire | Chef d'Orchestre |
| `app/build.gradle.kts` | Éventuelles dépendances (corpus, parsing) | Chef d'Orchestre (via PR agent) |
| `AppDatabase.kt` | Éventuelle table `InterestEvent` (R9) | Chef d'Orchestre (via PR agent) |

---

## 9. Prompts agents prêts à lancer

### PROMPT R1 — Architecture produit de l'écran recherche

**Message de démarrage pour l'agent :**
```
Tu es développeur dans un projet Android/Kotlin.
Commence par lire START_HERE.md.
Ta tâche attribuée est la TACHE_R1.
Le prompt détaillé est dans la section 9 de docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md.
```

**Mission :** Définir l'architecture produit et UX du nouvel écran de recherche de l'application Lexica.

**Documents à lire :**
1. `START_HERE.md`
2. `docs/planning/amélioration de la recherche/DESCRIPTION.md`
3. `docs/planning/amélioration de la recherche/PLAN.md`
4. `docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md`
5. `docs/guides/CONSIGNES_TACHES.md`

**Package cible :** `presentation/search/explore/` (à créer)

**Fichier à créer :** `ExploreScreenSpec.kt` (maquette fonctionnelle + spécifications)

**Fichiers existants à modifier :** aucun

**Fichier d'intégration à livrer :** `integration_pending/tache_r1_pr.md`

**Ce que tu dois produire dans `ExploreScreenSpec.kt` :**

1. **Structure de l'écran** — Hiérarchie visuelle complète (zones, proportions, priorités). Comportement à l'ouverture (extrait déjà affiché sans action préalable).
2. **États de l'écran** — Chargement, extrait affiché (nominal), pas d'extrait disponible, erreur, navigation entre extraits (transition).
3. **Interactions utilisateur** — Tap court sur mot surligné → ajouter le mot. Second tap → retirer le mot. Appui long sur mot surligné → définition. Appui long sur mot non surligné → définition + proposition d'ajout. Navigation entre extraits (swipe ou scroll). Signal d'intérêt (note 0-5 ou équivalent).
4. **États visuels des mots** — "Mot suggéré" (surligné, pas encore ajouté), "Mot ajouté" (surligné différemment, déjà dans le deck), "Mot normal" (non surligné, mais accessible via appui long).
5. **Conventions de nommage** — Proposer des noms de composables, ViewModel, états. Proposer une structure de `SearchUiState` pour le futur ViewModel.
6. **Points ouverts à arbitrer** — Swipe vs scroll pour la navigation entre extraits. Signal d'intérêt explicite vs implicite seulement. Gestion des conflits de gestes (tap vs swipe).

**Hors périmètre :** Implémentation du moteur de ranking, ingestion de corpus, intégration juridique des sources, code Compose fonctionnel (c'est une spec, pas une implémentation).

**Format de livraison :**
1. Créer `presentation/search/explore/ExploreScreenSpec.kt`
2. Créer `integration_pending/tache_r1_pr.md` avec résumé + points d'attention + questions ouvertes

---

### PROMPT R2 — Préférences utilisateur et taxonomie des intérêts

**Message de démarrage pour l'agent :**
```
Tu es développeur dans un projet Android/Kotlin.
Commence par lire START_HERE.md.
Ta tâche attribuée est la TACHE_R2.
Le prompt détaillé est dans la section 9 de docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md.
```

**Mission :** Construire le socle de personnalisation déclarée de l'application Lexica : le modèle de préférences utilisateur et la taxonomie thématique.

**Documents à lire :**
1. `START_HERE.md`
2. `docs/planning/amélioration de la recherche/DESCRIPTION.md` (sections 5 et 10 notamment)
3. `docs/planning/amélioration de la recherche/PLAN.md` (section 5.5 notamment)
4. `docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md`
5. `docs/guides/CONSIGNES_TACHES.md`

**Package cible :** `presentation/search/preferences/` (à créer)

**Fichiers à créer :**
- `UserPreferences.kt` — modèle de données des préférences
- `InterestTaxonomy.kt` — taxonomie des thèmes/domaines/registres
- `UserPreferencesRepository.kt` — stockage et accès aux préférences

**Fichiers existants à modifier :** aucun

**Fichier d'intégration à livrer :** `integration_pending/tache_r2_pr.md`

**Ce que tu dois produire :**

**1. `UserPreferences.kt`** — Modèle représentant les choix initiaux de l'utilisateur :
```kotlin
data class UserPreferences(
    val objectives: List<UserObjective>,
    val preferredDomains: List<String>,
    val preferredRegisters: List<String>,
    val lastUpdated: Long
)

enum class UserObjective {
    CONVERSATIONS_SOUTENUES,
    TEXTES_EXIGEANTS,
    DIALECTES_ARGOTS,
    DOMAINE_SPECIFIQUE
}
```

**2. `InterestTaxonomy.kt`** — Taxonomie V1 avec :
- **Thèmes** (ex: Sciences, Littérature, Vie quotidienne, Technique, Arts...)
- **Domaines** (sous-catégories des thèmes)
- **Registres** (Soutenu, Courant, Familier, Argotique, Technique)
- Table de correspondance entre `UserObjective` et thèmes/domaines par défaut

```kotlin
data class InterestTaxonomy(
    val themes: List<Theme>,
    val domains: List<Domain>,
    val registers: List<Register>
)
data class Theme(val id: String, val label: String, val domains: List<String>)
data class Domain(val id: String, val label: String, val themeId: String)
data class Register(val id: String, val label: String)
```

**3. `UserPreferencesRepository.kt`** — Stockage (SharedPreferences ou DataStore) :
- `suspend fun savePreferences(prefs: UserPreferences)`
- `fun getPreferences(): Flow<UserPreferences?>`
- `suspend fun clearPreferences()`
- `suspend fun hasCompletedOnboarding(): Boolean`

**4. Mapping objectifs → thèmes** — Table de correspondance. Exemple :
- `CONVERSATIONS_SOUTENUES` → thèmes: ["littérature", "sciences_humaines"], registres: ["soutenu", "courant"]
- `TEXTES_EXIGEANTS` → thèmes: ["philosophie", "sciences", "littérature_classique"], registres: ["soutenu", "technique"]

**Hors périmètre :** Scoring dynamique en production, analyse des comportements utilisateur à grande échelle, UI de l'écran de préférences, base de données Room (préférer DataStore ou SharedPreferences).

**Format de livraison :**
1. Créer les 3 fichiers dans `presentation/search/preferences/`
2. Créer `integration_pending/tache_r2_pr.md` avec résumé + instructions Chef d'Orchestre + taxonomie complète documentée

---

### PROMPT R3 — Pipeline corpus vers extraits candidats

**Message de démarrage pour l'agent :**
```
Tu es développeur dans un projet Android/Kotlin.
Commence par lire START_HERE.md.
Ta tâche attribuée est la TACHE_R3.
Le prompt détaillé est dans la section 9 de docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md.
```

**Mission :** Mettre en place la variante "extraits d'abord" : partir des corpus, en extraire des passages candidats et leur associer des métadonnées exploitables sans IA temps réel.

**Documents à lire :**
1. `START_HERE.md`
2. `docs/planning/amélioration de la recherche/DESCRIPTION.md` (sections 4, 6, 7)
3. `docs/planning/amélioration de la recherche/PLAN.md` (sections 4.1, 4.2, 4.3)
4. `docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md`
5. `docs/guides/CONSIGNES_TACHES.md`

**Prérequis :** TACHE_R2 est livrée. La taxonomie est dans `presentation/search/preferences/InterestTaxonomy.kt`. Tu peux t'en servir pour taguer les extraits.

**Package cible :** `data/corpus/` (à créer)

**Fichiers à créer :**
- `CorpusSource.kt` — modèle représentant une source de corpus (livre, article, interview...)
- `ExtractCandidate.kt` — schéma d'un extrait candidat avec métadonnées
- `CorpusParser.kt` — logique de segmentation des corpus en passages
- `CorpusIndex.kt` — indexation légère des extraits par thème/domaine

**Fichiers existants à modifier :** aucun

**Fichier d'intégration à livrer :** `integration_pending/tache_r3_pr.md`

**Ce que tu dois produire :**

**1. `CorpusSource.kt`** — Modèle de source :
```kotlin
data class CorpusSource(
    val id: String,
    val title: String,
    val author: String?,
    val year: Int?,
    val type: SourceType,        // BOOK, ARTICLE, INTERVIEW, TRANSCRIPT...
    val language: String,        // "fr" par défaut
    val license: String?,        // "PUBLIC_DOMAIN", "CC_BY", "UNKNOWN"...
    val contentUrl: String?,     // lien vers la source complète
    val domainTags: List<String> // IDs de domaines de la taxonomie
)
enum class SourceType { BOOK, ARTICLE, INTERVIEW, TRANSCRIPT, SPEECH, OTHER }
```

**2. `ExtractCandidate.kt`** — Schéma d'extrait :
```kotlin
data class ExtractCandidate(
    val id: String,
    val sourceId: String,
    val content: String,              // texte de l'extrait
    val startPosition: Int,           // position dans la source
    val endPosition: Int,
    val wordCount: Int,
    val suggestedWords: List<String>, // mots potentiellement intéressants détectés
    val domainTags: List<String>,     // IDs de domaines
    val registerTags: List<String>,   // IDs de registres
    val difficulty: String,           // "facile", "moyen", "avancé"
    val contextQuality: Float,        // score de qualité du contexte (0.0-1.0)
    val hasCompleteSource: Boolean    // true si la source complète est accessible
)
```

**3. `CorpusParser.kt`** — Segmentation :
- Fonction `fun parse(source: CorpusSource): List<ExtractCandidate>`
- Règles de segmentation : découpage par paragraphes (~100-300 mots par extrait)
- Détection basique des mots intéressants (rareté relative, longueur > 6 lettres, mots composés)
- Filtrage : ignorer les extraits < 50 caractères, ceux sans mots intéressants détectés
- Calcul du `contextQuality` basé sur : présence du mot dans un contexte clair (phrase complète), proximité d'autres mots intéressants, absence de bruit (citations tronquées)

**4. `CorpusIndex.kt`** — Indexation :
- Structure en mémoire : `Map<String, List<ExtractCandidate>>` (domaine → extraits)
- Fonction `fun index(candidates: List<ExtractCandidate>): CorpusIndex`
- Fonction `fun getCandidates(domainIds: List<String>, limit: Int): List<ExtractCandidate>`
- Fonction `fun getCandidatesByDifficulty(difficulty: String, limit: Int): List<ExtractCandidate>`

**Hors périmètre :** UI Compose, moteur de ranking personnalisé, gestion des vidéos, intégration réseau (API).

**Format de livraison :**
1. Créer les 4 fichiers dans `data/corpus/`
2. Créer `integration_pending/tache_r3_pr.md` avec résumé + instructions Chef d'Orchestre

---

### PROMPT R4 — Pipeline mots cibles vers extraits d'usage

**Message de démarrage pour l'agent :**
```
Tu es développeur dans un projet Android/Kotlin.
Commence par lire START_HERE.md.
Ta tâche attribuée est la TACHE_R4.
Le prompt détaillé est dans la section 9 de docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md.
```

**Mission :** Mettre en place la variante "mots cibles d'abord" : produire une liste de mots susceptibles d'intéresser l'utilisateur, puis rechercher des usages pertinents dans les corpus.

**Documents à lire :**
1. `START_HERE.md`
2. `docs/planning/amélioration de la recherche/DESCRIPTION.md` (sections 6, 7)
3. `docs/planning/amélioration de la recherche/PLAN.md` (sections 4.1, 4.2, 4.3)
4. `docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md`
5. `docs/guides/CONSIGNES_TACHES.md`

**Prérequis :** TACHE_R2 est livrée. La taxonomie et le mapping objectifs → thèmes sont dans `presentation/search/preferences/`.

**Package cible :** `data/corpus/` (même package que R3)

**Fichiers à créer :**
- `TargetWordList.kt` — modèle de liste de mots cibles pondérés
- `OccurrenceSearcher.kt` — recherche d'occurrences dans les corpus
- `ContextFilter.kt` — filtrage des passages pour qualité de contexte

**Fichiers existants à modifier :** aucun (mais doit être cohérent avec les modèles de R3)

**Fichier d'intégration à livrer :** `integration_pending/tache_r4_pr.md`

**Ce que tu dois produire :**

**1. `TargetWordList.kt`** — Mots cibles pondérés :
```kotlin
data class TargetWord(
    val word: String,
    val weight: Float,           // 0.0-1.0, importance relative
    val source: TargetSource,    // d'où vient ce mot cible
    val domainIds: List<String>  // domaines associés
)
enum class TargetSource { USER_PREFERENCE, FREQUENCY_BASED, DOMAIN_SPECIFIC, EXPLORATORY }

data class TargetWordList(
    val words: List<TargetWord>,
    val generatedAt: Long,
    val profileSnapshot: String  // hash du profil utilisateur utilisé
)
```

Fonction `fun generateTargetWords(preferences: UserPreferences, taxonomy: InterestTaxonomy): TargetWordList` :
- Utilise le mapping objectifs → domaines de la taxonomie R2
- Génère une liste de mots cibles à partir des domaines préférés
- Pondération : mots directement liés aux domaines = poids fort, mots génériques = poids moyen, mots exploratoires = poids faible

**2. `OccurrenceSearcher.kt`** — Recherche d'occurrences :
```kotlin
data class Occurrence(
    val word: String,
    val sourceId: String,
    val context: String,         // phrase ou paragraphe contenant le mot
    val position: Int,           // position dans la source
    val sentenceCount: Int       // nombre de phrases autour du mot
)

interface OccurrenceSearcher {
    suspend fun search(words: List<TargetWord>, corpus: List<CorpusSource>): List<Occurrence>
    suspend fun searchByDomain(domainIds: List<String>, corpus: List<CorpusSource>): List<Occurrence>
}
```

Implémentation simple :
- Recherche textuelle (indexation naive ou `String.contains` insensible à la casse)
- Retourne le contexte : la phrase contenant le mot + une phrase avant/après
- Limite : max 5 occurrences par mot pour éviter la redondance

**3. `ContextFilter.kt`** — Filtrage qualité :
```kotlin
data class FilteredExtract(
    val occurrence: Occurrence,
    val qualityScore: Float,     // 0.0-1.0
    val reason: String           // pourquoi ce score
)

fun filterOccurrences(occurrences: List<Occurrence>): List<FilteredExtract>
```

Critères de qualité :
- Le mot est utilisé dans une phrase complète (commence par majuscule, finit par un point)
- Le contexte permet de comprendre le sens du mot (présence de mots définissant ou illustrant)
- Pas de bruit (citations tronquées, listes, titres)
- Bonus si le mot apparaît avec d'autres mots intéressants à proximité
- Score final = moyenne pondérée des critères

**Hors périmètre :** UI de la page extrait, politique juridique complète des sources, scoring comportemental avancé.

**Format de livraison :**
1. Créer les 3 fichiers dans `data/corpus/`
2. Créer `integration_pending/tache_r4_pr.md` avec résumé + instructions Chef d'Orchestre

---

### PROMPT R5 — Moteur de scoring et ranking des propositions

**Message de démarrage pour l'agent :**
```
Tu es développeur dans un projet Android/Kotlin.
Commence par lire START_HERE.md.
Ta tâche attribuée est la TACHE_R5.
Le prompt détaillé est dans la section 9 de docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md.
```

**Mission :** Concevoir et implémenter le moteur de scoring qui choisit quel extrait montrer à l'utilisateur et dans quel ordre, en combinant les signaux explicites (préférences) et implicites (comportement).

**Documents à lire :**
1. `START_HERE.md`
2. `docs/planning/amélioration de la recherche/DESCRIPTION.md` (sections 5, 8, 9)
3. `docs/planning/amélioration de la recherche/PLAN.md` (sections 4.4, 5.5)
4. `docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md`
5. `docs/guides/CONSIGNES_TACHES.md`

**Prérequis :** TACHE_R2 (taxonomie), TACHE_R3 (CorpusIndex, ExtractCandidate) et/ou TACHE_R4 (TargetWordList, FilteredExtract) sont livrés. Les modèles sont dans `data/corpus/` et `presentation/search/preferences/`.

**Package cible :** `domain/recommendation/` (à créer)

**Fichiers à créer :**
- `ScoringEngine.kt` — moteur de score combinant signaux explicites et implicites
- `RankingStrategy.kt` — stratégie de classement des extraits candidats
- `ExplorationPolicy.kt` — politique d'exploration vs exploitation

**Fichiers existants à modifier :** aucun

**Fichier d'intégration à livrer :** `integration_pending/tache_r5_pr.md`

**Ce que tu dois produire :**

**1. `ScoringEngine.kt`** — Moteur de score :
```kotlin
data class ScoreContext(
    val extract: ExtractCandidate,
    val userPreferences: UserPreferences?,
    val interestProfile: InterestProfile?,  // null en V1, alimenté par R9 plus tard
    val history: List<String>               // IDs des extraits déjà vus
)

data class ScoredExtract(
    val extract: ExtractCandidate,
    val score: Float,           // 0.0-1.0, score final combiné
    val breakdown: ScoreBreakdown  // détail des sous-scores pour debug
)

data class ScoreBreakdown(
    val preferenceScore: Float,   // correspondance avec les préférences utilisateur
    val qualityScore: Float,      // qualité intrinsèque de l'extrait (contextQuality)
    val diversityScore: Float,    // bonus de diversité (pas vu récemment)
    val explorationScore: Float   // bonus d'exploration (nouveaux domaines)
)
```

Fonctions :
- `fun score(extract: ExtractCandidate, context: ScoreContext): ScoredExtract`
- `fun scoreBatch(extracts: List<ExtractCandidate>, context: ScoreContext): List<ScoredExtract>`

Règles de scoring :
- **preferenceScore** (0.0-1.0) : basé sur le chevauchement entre les domaines de l'extrait et les domaines préférés de l'utilisateur. Utilise `InterestTaxonomyProvider.objectiveMappings` de R2.
- **qualityScore** (0.0-1.0) : directement `extract.contextQuality` (déjà calculé par R3)
- **diversityScore** (0.0-1.0) : bonus si l'extrait vient d'une source ou d'un domaine différent des N derniers extraits vus
- **explorationScore** (0.0-1.0) : bonus si l'extrait touche un domaine que l'utilisateur n'a pas encore exploré

Formule V1 : `score = 0.4 * preferenceScore + 0.3 * qualityScore + 0.2 * diversityScore + 0.1 * explorationScore`

**2. `RankingStrategy.kt`** — Stratégie de classement :
```kotlin
interface RankingStrategy {
    fun rank(candidates: List<ScoredExtract>, limit: Int): List<ScoredExtract>
}

class DefaultRankingStrategy : RankingStrategy {
    // Trie par score décroissant, puis applique l'exploration policy
}
```

- `fun rank(candidates, limit)` : trie les extraits par score, applique l'exploration policy, retourne les N meilleurs
- Supporte le reranking : si l'utilisateur a ignoré certains extraits, les descendre dans le classement

**3. `ExplorationPolicy.kt`** — Politique d'exploration :
```kotlin
data class ExplorationConfig(
    val explorationRate: Float = 0.1f,  // 10% de chances de montrer un extrait exploratoire
    val decayFactor: Float = 0.95f,     // décroissance du taux après chaque session
    val minExplorationRate: Float = 0.05f
)

class ExplorationPolicy(private val config: ExplorationConfig) {
    fun shouldExplore(): Boolean  // true si on doit montrer un extrait exploratoire
    fun selectExploratoryExtract(candidates: List<ScoredExtract>): ScoredExtract?
    fun decayRate(): ExplorationConfig  // réduit le taux après usage
}
```

**Hors périmètre :** UI Compose, stockage persistant des scores, synchronisation cloud, analyse comportementale avancée (R9).

**Format de livraison :**
1. Créer les 3 fichiers dans `domain/recommendation/`
2. Créer `integration_pending/tache_r5_pr.md` avec résumé + instructions Chef d'Orchestre

---

### PROMPT R6 — UI interactive de la page extrait

**Message de démarrage pour l'agent :**
```
Tu es développeur dans un projet Android/Kotlin.
Commence par lire START_HERE.md.
Ta tâche attribuée est la TACHE_R6.
Le prompt détaillé est dans la section 9 de docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md.
```

**Mission :** Implémenter l'écran principal d'exploration (ExploreScreen) en Jetpack Compose, avec affichage des extraits, mots surlignés, interactions tap/appui long, navigation entre extraits, et signal d'intérêt.

**Documents à lire :**
1. `START_HERE.md`
2. `docs/planning/amélioration de la recherche/DESCRIPTION.md`
3. `docs/planning/amélioration de la recherche/PLAN.md`
4. `docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md`
5. `docs/guides/CONSIGNES_TACHES.md`

**Prérequis :** TACHE_R1 (spec UX dans `presentation/search/explore/ExploreScreenSpec.kt`), TACHE_R3 (modèles dans `data/corpus/`).

**Package cible :** `presentation/search/explore/` (déjà créé par R1)

**Fichiers à créer :**
- `ExploreScreen.kt` — écran principal Compose
- `ExploreViewModel.kt` — ViewModel avec états et logique UI
- `ExtractRenderer.kt` — rendu des extraits avec mots surlignés et gestes

**Fichiers existants à modifier :** aucun (les modifications de navigation seront faites par le Chef d'Orchestre)

**Fichier d'intégration à livrer :** `integration_pending/tache_r6_pr.md`

**Ce que tu dois produire :**

**1. `ExploreScreen.kt`** — Écran principal :
- Scaffold avec TopAppBar (titre "Explorer", icône info source)
- Zone extrait principale (weight 1f) avec `ExtractRenderer`
- Barre de note d'intérêt (1-5) repliable en bas de l'extrait
- Barre basse secondaire avec boutons "Chercher des ouvrages" et "Rechercher des mots"
- Gestion des 4 états : Loading, ExtractDisplayed, NoExtractAvailable, Error
- Navigation entre extraits par swipe horizontal (HorizontalPager ou équivalent)
- Animation de transition entre extraits (slide, 300ms)

**2. `ExploreViewModel.kt`** — ViewModel :
```kotlin
class ExploreViewModel(
    private val corpusIndex: CorpusIndex,
    private val scoringEngine: ScoringEngine,
    private val rankingStrategy: RankingStrategy
) : ViewModel() {
    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()
    
    fun loadInitialExtract(preferences: UserPreferences?)
    fun navigateToNext()
    fun navigateToPrevious()
    fun toggleWord(word: String)       // ajouter/retirer du deck
    fun showDefinition(word: String)   // afficher définition
    fun rateExtract(rating: Int)       // noter l'extrait (1-5)
    fun retry()                        // recharger après erreur
}
```

**3. `ExtractRenderer.kt`** — Rendu des extraits :
- Affiche le texte de l'extrait avec les mots surlignés
- `WordStatus.NORMAL` : texte normal, pas de fond
- `WordStatus.SUGGESTED` : fond jaune clair (#FFF8E1), tap court → ajoute
- `WordStatus.ADDED` : fond vert clair (#E8F5E9), tap court → retire
- `WordStatus.TRANSITIONING` : animation avant changement d'état
- Appui long sur mot surligné → `showDefinition()`
- Appui long sur mot non surligné → définition + proposition d'ajout
- Utiliser `ClickableText` ou `AnnotatedString` pour les mots cliquables

**Conventions de nommage (cf. R1) :**
- `ExploreScreen`, `ExtractContent`, `TextExtractContent`, `HighlightedWord`
- `InterestRatingBar`, `ExploreBottomBar`, `ExtractSourceInfo`, `WordDefinitionSheet`

**Hors périmètre :** Moteur de scoring temps réel, catalogue d'ouvrages complet, intégration dans la navigation globale de l'app.

**Format de livraison :**
1. Créer les 3 fichiers dans `presentation/search/explore/`
2. Créer `integration_pending/tache_r6_pr.md` avec résumé + instructions Chef d'Orchestre (routes à ajouter dans `LexicaApp.kt` et `Screen.kt`)

---

### PROMPT R7 — Catalogue d'ouvrages et accès à la source complète

**Message de démarrage pour l'agent :**
```
Tu es développeur dans un projet Android/Kotlin.
Commence par lire START_HERE.md.
Ta tâche attribuée est la TACHE_R7.
Le prompt détaillé est dans la section 9 de docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md.
```

**Mission :** Créer le sous-espace "Chercher des ouvrages" qui permet de naviguer depuis un extrait vers la source complète, et de découvrir d'autres ouvrages du même domaine.

**Documents à lire :**
1. `START_HERE.md`
2. `docs/planning/amélioration de la recherche/DESCRIPTION.md` (section 4.3)
3. `docs/planning/amélioration de la recherche/PLAN.md` (section 4.5)
4. `docs/planning/amélioration de la recherche/TACHE_FONCTION_RECHERCHE.md`
5. `docs/guides/CONSIGNES_TACHES.md`

**Prérequis :** TACHE_R3 (CorpusSource, CorpusIndex dans `data/corpus/`).

**Package cible :** `presentation/search/catalogue/` (à créer)

**Fichiers à créer :**
- `CatalogueScreen.kt` — écran catalogue Compose
- `CatalogueViewModel.kt` — ViewModel du catalogue
- `CatalogueRepository.kt` — accès aux sources et extraits

**Fichiers existants à modifier :** aucun

**Fichier d'intégration à livrer :** `integration_pending/tache_r7_pr.md`

**Ce que tu dois produire :**

**1. `CatalogueRepository.kt`** — Accès aux données :
```kotlin
class CatalogueRepository(private val corpusIndex: CorpusIndex) {
    fun getSourcesByDomain(domainIds: List<String>): List<CorpusSource>
    fun getSourceById(sourceId: String): CorpusSource?
    fun getExtractsBySource(sourceId: String): List<ExtractCandidate>
    fun getRelatedSources(sourceId: String, maxResults: Int): List<CorpusSource>
}
```

**2. `CatalogueViewModel.kt`** — ViewModel :
```kotlin
data class CatalogueUiState(
    val sources: List<CorpusSource>,
    val selectedSource: CorpusSource?,
    val extracts: List<ExtractCandidate>,
    val isLoading: Boolean,
    val error: String?
)

class CatalogueViewModel(
    private val repository: CatalogueRepository
) : ViewModel() {
    val uiState: StateFlow<CatalogueUiState>
    fun loadSources(domainIds: List<String>)
    fun selectSource(sourceId: String)
    fun openSourceUrl(url: String)  // ouvre dans le navigateur
}
```

**3. `CatalogueScreen.kt`** — Écran Compose :
- Liste des sources disponibles (titre, auteur, année, type, licence)
- Vue détaillée d'une source : métadonnées complètes + liste des extraits
- Indicateur "contenu complet disponible" vs "lien externe"
- Bouton "Ouvrir la source" → navigateur ou lecteur intégré
- Filtrage par domaine (via chips ou dropdown)
- Tri : par date, par pertinence, par nombre d'extraits
- États : Loading, SourcesDisplayed, SourceDetail, Error, Empty

**Hors périmètre :** Lecteur intégré de contenu complet, téléchargement hors-ligne, synchronisation cloud.

**Format de livraison :**
1. Créer les 3 fichiers dans `presentation/search/catalogue/`
2. Créer `integration_pending/tache_r7_pr.md` avec résumé + instructions Chef d'Orchestre (route à ajouter dans `LexicaApp.kt`)
