# TACHE_R5 — Moteur de scoring et ranking des propositions

## Résumé

Livraison du moteur de scoring et ranking pour le module de recommandation d'extraits. 3 fichiers créés dans le package `domain/recommendation/`.

## Fichiers livrés

| Fichier | Package | Rôle |
|---------|---------|------|
| `ScoringEngine.kt` | `domain/recommendation/` | Moteur de score combinant 4 sous-scores (préférence, qualité, diversité, exploration) |
| `RankingStrategy.kt` | `domain/recommendation/` | Stratégies de classement (DefaultRankingStrategy + InterleavedRankingStrategy) |
| `ExplorationPolicy.kt` | `domain/recommendation/` | Politique d'exploration vs exploitation avec taux et décroissance |

## Dépendances

Le moteur de scoring s'appuie sur les modèles suivants (déjà livrés par R2 et R3) :

- `data/corpus/ExtractCandidate.kt` (R3) — utilisé dans `ScoreContext`
- `data/corpus/CorpusIndex.kt` (R3) — pour récupérer les candidats
- `presentation/search/preferences/UserPreferences.kt` (R2) — préférences utilisateur
- `presentation/search/preferences/InterestTaxonomy.kt` (R2) — `InterestTaxonomyProvider.objectiveMappings` pour le mapping objectifs → domaines

## Architecture

```
domain/recommendation/
├── ScoringEngine.kt       ← Moteur de scoring
│   ├── ScoreContext        (userPreferences, interestProfile, history)
│   ├── ScoredExtract       (extract + score + breakdown)
│   ├── ScoreBreakdown      (preferenceScore, qualityScore, diversityScore, explorationScore)
│   ├── ScoreWeights        (poids configurables de la formule)
│   └── InterestProfile     (profil domaine → score, pour future intégration R9)
│
├── RankingStrategy.kt     ← Stratégies de classement
│   ├── RankingStrategy     (interface)
│   ├── DefaultRankingStrategy (tri + reranking + déduplication + exploration)
│   ├── RerankingConfig     (seuil, pénalité, IDs ignorés/vus)
│   └── InterleavedRankingStrategy (alternance par domaine)
│
└── ExplorationPolicy.kt   ← Politique d'exploration
    ├── ExplorationPolicy   (shouldExplore, selectExploratoryExtract, decayRate)
    └── ExplorationConfig   (explorationRate, decayFactor, minExplorationRate)
```

## Formule de score V1

```
score = 0.4 * preferenceScore
     + 0.3 * qualityScore
     + 0.2 * diversityScore
     + 0.1 * explorationScore
```

Les poids sont configurables via `ScoreWeights`. 3 variantes fournies :
- `ScoreWeights()` — défaut (exploitation modérée)
- `ScoreWeights.explorationFriendly` — pour nouveaux utilisateurs
- `ScoreWeights.qualityFocused` — priorité à la qualité du contexte

## Détail des sous-scores

### preferenceScore (0.0 – 1.0)
- Mesure le chevauchement entre les domaines de l'extrait et les domaines préférés
- Utilise `InterestTaxonomyProvider.domainsForObjectives()` pour dériver les domaines des objectifs
- Bonus si les registres correspondent aussi (+0.15)
- Valeur neutre à 0.5 si pas de préférences

### qualityScore (0.0 – 1.0)
- Reprend directement `ExtractCandidate.contextQuality` (calculé par R3)
- Pas de calcul supplémentaire

### diversityScore (0.0 – 1.0)
- 1.0 si premier extrait (pas d'historique)
- 0.8 si nouvelle source
- 0.5 si nouveau domaine dans une source connue
- 0.2 si source et domaines déjà vus

### explorationScore (0.0 – 1.0)
- Proportion de domaines inconnus dans l'extrait
- 0.0 si tous les domaines sont déjà dans les préférences
- > 0.0 si l'extrait contient des domaines non explorés

## Stratégies de ranking

### DefaultRankingStrategy
1. Filtre les extraits sous le seuil (`minScoreThreshold = 0.1`)
2. Trie par score décroissant
3. Reranking : les extraits ignorés descendent (pénalité ×0.5)
4. Déduplication : pas deux extraits de la même source consécutifs
5. Injection d'exploration : remplace le dernier élément si `shouldExplore()`

### InterleavedRankingStrategy
- Groupe les extraits par domaine principal
- Alterne entre les domaines pour garantir la diversité thématique
- Utile pour les écrans de type "découverte"

## Politique d'exploration

- Taux d'exploration par défaut : 10 % (configurable)
- Décroissance : ×0.95 après chaque session
- Minimum : 5 %
- 3 configurations prêtes à l'emploi : `highExploration`, `lowExploration`, `discoveryMode`

## Instructions pour le Chef d'Orchestre

### Copie des fichiers
Copier les 3 fichiers de `domain/recommendation/` vers le projet :
```
app/src/main/java/com/example/lexicaandroid2/domain/recommendation/
├── ScoringEngine.kt
├── RankingStrategy.kt
└── ExplorationPolicy.kt
```

### Intégration DI
Si un conteneur DI est en place, ajouter les dépendances suivantes :
```kotlin
// Dans AppContainer ou équivalent
val scoringEngine = ScoringEngine()
val explorationPolicy = ExplorationPolicy()
val rankingStrategy = DefaultRankingStrategy(explorationPolicy = explorationPolicy)
```

### Utilisation typique
```kotlin
// 1. Récupérer les candidats depuis CorpusIndex
val candidates = corpusIndex.getCandidates(domainIds = preferredDomains, limit = 50)

// 2. Scorer dans le contexte utilisateur
val context = ScoreContext(
    userPreferences = userPrefs,
    history = seenSourceIds
)
val scored = scoringEngine.scoreBatch(candidates, context)

// 3. Classer et prendre les N meilleurs
val ranked = rankingStrategy.rank(scored, limit = 10)
```

### Points d'attention
- Le `InterestProfile` est prévu pour la future intégration R9 — actuellement null
- L'`ExplorationPolicy` utilise `kotlin.random.Random` — pas de seed fixe
- Les poids de scoring sont modifiables sans changer le code grâce à `ScoreWeights`
- La déduplication des sources est O(n²) dans le pire cas — acceptable pour des listes < 100 extraits

### Tests recommandés
1. Scoring avec préférences null → score neutre
2. Scoring avec préférences alignées → preferenceScore élevé
3. Reranking avec extraits ignorés → pénalité appliquée
4. Exploration policy : `shouldExplore()` respecte le taux configuré
5. InterleavedRankingStrategy : alternance correcte des domaines
