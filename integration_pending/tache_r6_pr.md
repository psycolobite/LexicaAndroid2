# TACHE_R6 — UI ExploreScreen — PR d'intégration

## Résumé

Implémentation de l'écran principal d'exploration (ExploreScreen) en Jetpack Compose, conforme à la spécification UX `ExploreScreenSpec.kt` (TACHE_R1).

### Fichiers créés

| Fichier | Package | Description |
|---------|---------|-------------|
| `ExploreScreen.kt` | `presentation/search/explore/` | Écran principal Compose avec Scaffold, 4 états UI, swipe navigation, barre de notation, bottom bar |
| `ExploreViewModel.kt` | `presentation/search/explore/` | ViewModel avec chargement extraits, navigation, toggle mots, notation, anti-brûlage |
| `ExtractRenderer.kt` | `presentation/search/explore/` | Rendu des extraits avec mots surlignés (SUGGESTED/ADDED/TRANSITIONING/NORMAL), tap et appui long |

### Dépendances entre packages

- `ExploreViewModel` dépend de `data/corpus/CorpusIndex` et `data/corpus/ExtractCandidate` (TACHE_R3)
- `ExploreViewModel` dépend de `presentation/search/preferences/UserPreferences` (TACHE_R2)
- `ExploreScreen` dépend de `presentation/search/preferences/UserPreferences` (TACHE_R2)

### Ce qui fonctionne

- ✅ 4 états UI : Loading, ExtractDisplayed, NoExtractAvailable, Error
- ✅ Affichage d'un extrait avec mots surlignés (fond jaune = suggéré, vert = ajouté)
- ✅ Tap court sur mot SUGGESTED → ajoute (passe en ADDED)
- ✅ Tap court sur mot ADDED → retire (passe en SUGGESTED)
- ✅ Appui long sur tout mot → événement définition (stub)
- ✅ Animation de transition entre extraits (slide horizontal 300ms)
- ✅ Barre de note d'intérêt (1-5 étoiles)
- ✅ Barre basse avec boutons "Chercher des ouvrages" et "Rechercher des mots"
- ✅ Anti-brûlage : limite de 3 swipes en 5 secondes
- ✅ Cache session avec déduplication des extraits déjà vus
- ✅ Chargement de plus d'extraits quand le cache est épuisé
- ✅ Gestion des erreurs avec try-catch et retry

### Ce qui est en mode stub (à connecter plus tard)

- `showDefinition()` : émet un événement mais pas de bottom sheet UI (à connecter à une API dictionnaire)
- `scoringEngine` et `rankingStrategy` : paramètres optionnels du ViewModel, non utilisés en V1 (TACHE_R5)
- `ExtractCandidate.toUiModel()` : `sourceTitle` utilise `sourceId` comme fallback (sera enrichi avec `CorpusSource`)
- Swipe navigation : utilise des zones cliquables sur les bordures (24dp) plutôt qu'un vrai `HorizontalPager` (V1 pragmatique)

---

## Instructions pour le Chef d'Orchestre

### 1. Copier les fichiers

```bash
# Les fichiers sont déjà dans le bon package :
# app/src/main/java/com/example/lexicaandroid2/presentation/search/explore/
```

### 2. Ajouter la route dans `LexicaApp.kt`

Dans `presentation/navigation/Screen.kt`, ajouter :

```kotlin
object Explore : Screen("explore")
```

Dans `LexicaApp.kt`, ajouter le composable :

```kotlin
composable(Screen.Explore.route) {
    ExploreScreen(
        userPreferences = userPreferences, // à récupérer depuis UserPreferencesRepository
        onNavigateToCatalogue = { navController.navigate(Screen.Catalogue.route) },
        onNavigateToSearch = { navController.navigate(Screen.Search.route) },
        onBack = { navController.popBackStack() }
    )
}
```

### 3. Brancher le ViewModel dans le DI

Si vous utilisez un `ViewModelFactory`, ajouter :

```kotlin
ExploreViewModel(
    corpusIndex = corpusIndex,       // depuis TACHE_R3
    scoringEngine = null,            // TACHE_R5 optionnel
    rankingStrategy = null           // TACHE_R5 optionnel
)
```

### 4. Ajouter la navigation depuis le Dashboard

Dans `DashboardScreen.kt`, ajouter un bouton ou icône qui navigue vers `Screen.Explore`.

### 5. Vérifications

- [ ] `ExploreScreen` s'affiche correctement avec un `CorpusIndex` non null
- [ ] Les 4 états UI sont testables (Loading, ExtractDisplayed, NoExtractAvailable, Error)
- [ ] Le tap sur un mot surligné change son statut visuel
- [ ] La navigation entre extraits fonctionne (bordures cliquables)
- [ ] La barre de notation 1-5 est fonctionnelle
- [ ] Les boutons "Chercher des ouvrages" et "Rechercher des mots" naviguent correctement

### Points d'attention

1. **CorpusIndex requis** : le ViewModel nécessite un `CorpusIndex` non null pour fonctionner. Sans lui, l'état reste `NoExtractAvailable`.
2. **UserPreferences optionnel** : si null, les extraits sont chargés sans filtre de domaine.
3. **ScoringEngine/RankingStrategy** : paramètres optionnels pour compatibilité future avec TACHE_R5.
4. **Swipe navigation** : l'implémentation V1 utilise des zones cliquables sur les bordures. Une version future pourrait utiliser `HorizontalPager` pour un vrai geste de swipe.
5. **Définition** : le bottom sheet de définition n'est pas encore implémenté (stub événement).

### Conflits potentiels

- Aucun conflit avec les fichiers existants (package isolé `presentation/search/explore/`)
- Dépend des modèles de `data/corpus/` (TACHE_R3) et `presentation/search/preferences/` (TACHE_R2)
- Compatible avec TACHE_R5 (paramètres optionnels) et TACHE_R7 (bouton catalogue)
