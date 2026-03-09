# Pull Request: Module de Recherche

Date: 2026-02-27
Agent: DEV_SEARCH

## 📋 Description

Implémentation complète du module de recherche pour LexicaAndroid2. Ce module permet aux utilisateurs de trouver n'importe quel mot parmi les 7000+ flashcards instantanément avec une interface fluide et réactive.

## ✅ Tâches réalisées

### 1. **Couche Données (Data)**
- ✅ Extension du `FlashcardDao` avec 6 requêtes SQL optimisées
  - `searchByWord(query, limit)` - Recherche par mot (LIKE)
  - `searchByDefinition(query, limit)` - Recherche par définition
  - `searchGlobal(query, limit)` - Recherche globale (mot + définition + synonymes)
  - `searchFavorites(query, limit)` - Recherche dans les favoris
  - `getAllPaginated(limit, offset)` - Résultats paginés (pour recherche vide)
  - `countSearchResults(query)` - Compte les résultats

**Optimisations SQL :**
- Utilisation de `LOWER()` pour recherche insensible à la casse
- `LIKE '%query%'` avec LIMIT pour éviter de charger trop de résultats
- Tri par longueur du mot (les mots courts en priorité)
- Support des synonymes

### 2. **Couche Domaine (Domain)**
- ✅ Création de l'interface `SearchRepository`
  - Contrats pour tous les types de recherche
  - Support des Flow pour la réactivité

### 3. **Couche Données - Implémentation (Data Repository)**
- ✅ `SearchRepositoryImpl` 
  - Gestion des recherches vides
  - Délégage au DAO avec limite de 50 résultats par défaut

### 4. **Couche Présentation (UI Layer)**

#### ViewModel
- ✅ `SearchViewModel` avec :
  - Gestion de l'état UI via `StateFlow`
  - **Debounce de 300ms** pour éviter les requêtes trop fréquentes
  - Support de 4 types de recherche (GLOBAL, BY_WORD, BY_DEFINITION, FAVORITES)
  - Highlighting automatique du terme de recherche
  - Comptage des résultats

#### Écran
- ✅ `SearchScreen.kt` avec composables :
  - **SearchBar Material3** avec search actif/inactif
  - **Tabs de filtrage** (Global, Mots, Définitions, Favoris)
  - **Résultats avec highlighting** du terme recherché
  - **SearchResultCard** avec :
    - Mot souligné
    - Catégorie grammaticale
    - Définition (avec highlighting)
    - Synonymes
    - Exemples (max 2)
    - Boutons d'actions (Ajouter à révision, Détails)
  - **Gestion des états** :
    - Loading state (spinner)
    - Empty state (aucun résultat)
    - Default state (commencez à taper)
  - **Composable HighlightedText** pour le highlighting du texte

## 📊 Architecture

```
presentation/search/
├── SearchScreen.kt          # Écran principal + composables
├── SearchViewModel.kt       # État et logique
└── (future) SearchNavigation.kt

data/repository/
└── SearchRepositoryImpl.kt   # Implémentation du repository

domain/repository/
└── SearchRepository.kt      # Interface du repository

data/local/
└── FlashcardDao.kt         # Requêtes SQL optimisées (étendues)
```

## 🔄 Intégration requise

### 1. **Ajouter la route à LexicaApp.kt**

```kotlin
// Dans sealed class Screen
data object Search : Screen("search")

// Ajouter à topBarTitle
Screen.Search.route -> "Recherche"

// Ajouter à canNavigateBack
currentRoute == Screen.Search.route

// Ajouter composable dans NavHost
composable(
    route = Screen.Search.route,
    enterTransition = {
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
    },
    exitTransition = {
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
    }
) {
    SearchScreen(
        viewModel = searchViewModel,
        onCardClick = { card ->
            // TODO: Implémenter navigation vers détails
        },
        onAddToReview = { card ->
            // TODO: Ajouter la carte aux révisions
        }
    )
}

// Importer
import com.example.lexicaandroid2.presentation.search.SearchScreen
import com.example.lexicaandroid2.presentation.search.SearchViewModel
```

### 2. **Injecter le ViewModel dans MainActivity/App**

```kotlin
// Créer une instance du SearchViewModel (via DI ou Factory)
val searchRepository = SearchRepositoryImpl(database.flashcardDao())
val searchViewModel = SearchViewModel(searchRepository)

// Passer à LexicaApp
LexicaApp(
    searchViewModel = searchViewModel,
    // ... autres ViewModels
)
```

### 3. **Ajouter un bouton de navigation vers la recherche**

Depuis le DashboardScreen ou la TopAppBar :
```kotlin
navController.navigate(Screen.Search.route)
```

## 🎯 Fonctionnalités principales

1. **Recherche fluide avec debounce** : 300ms sans bloquer l'UI
2. **Highlighting du texte** : Surbrillance du terme recherché
3. **4 types de recherche** : Global, par mot, par définition, favoris
4. **Résultats détaillés** : Définition, synonymes, exemples
5. **Actions rapides** : Ajouter à révision ou voir détails
6. **Optimisation SQL** : LIMIT 50 pour performance
7. **Gestion d'état complète** : Loading, empty, error states

## 📝 Notes de performance

- Debounce : 300ms (configurable dans SearchViewModel)
- Limite des résultats : 50 par défaut (configurable)
- Requêtes SQL avec index sur `mot` et `definition`
- Flow reactif sans blocking

## 🚀 Prochaines étapes optionnelles

1. **Full-Text Search (FTS)** : Si les 7000 mots deviennent trop nombreux
2. **Historique de recherche** : Stocker les dernières recherches
3. **Suggestions autocomplete** : Basées sur les mots fréquents
4. **Filters avancés** : Par catégorie grammaticale, registre, etc.
5. **Integration avec la page détail** : Afficher les informations complètes d'un mot

## ✨ Livrables

- ✅ SearchDao : 6 requêtes optimisées
- ✅ SearchRepository (interface + impl)
- ✅ SearchViewModel avec debounce et highlighting
- ✅ SearchScreen avec UI complète
- ✅ Composables réutilisables (SearchResultItem, SearchResultCard, HighlightedText)
- ✅ Fichier d'intégration (ce fichier)

---

**Status**: ✅ **PRÊT POUR INTÉGRATION**

