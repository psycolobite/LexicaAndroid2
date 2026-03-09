# 🔍 Module de Recherche - LexicaAndroid2

## 📋 Vue d'ensemble

Module de recherche complet implémenté avec debounce de 300ms, requêtes SQL optimisées avec LIKE, et interface Material3.

## ✅ Fichiers créés

### 1. **Couche Données (Data Layer)**

#### `FlashcardDao.kt` - Mise à jour
Ajout de 6 requêtes SQL optimisées :

- ✅ `searchByWord(query, limit)` - Recherche par mot avec LIKE
- ✅ `searchByDefinition(query, limit)` - Recherche par définition
- ✅ `searchGlobal(query, limit)` - Recherche globale (mot + définition + synonymes)
- ✅ `searchFavorites(query, limit)` - Recherche dans les favoris
- ✅ `getAllPaginated(limit, offset)` - Pagination pour recherche vide
- ✅ `countSearchResults(query)` - Comptage des résultats

**Optimisations SQL :**
- ✅ Utilisation de `LOWER()` pour recherche insensible à la casse
- ✅ `LIKE '%query%'` avec `LIMIT` pour performances
- ✅ Tri par `LENGTH(mot)` (mots courts en priorité)
- ✅ Support des synonymes dans recherche globale
- ✅ Priorité aux correspondances exactes dans l'ORDER BY

#### `SearchRepositoryImpl.kt` - NOUVEAU
```kotlin
package: com.example.lexicaandroid2.data.repository
```
- Implémentation du repository de recherche
- Gestion des recherches vides (retourne résultats paginés)
- Mapping Entity → Domain avec `toDomain()`
- Support de tous les types de recherche

### 2. **Couche Domaine (Domain Layer)**

#### `SearchRepository.kt` - NOUVEAU
```kotlin
package: com.example.lexicaandroid2.domain.repository
```
Interface définissant les contrats :
- `searchByWord()` - Flow<List<Flashcard>>
- `searchByDefinition()` - Flow<List<Flashcard>>
- `searchGlobal()` - Flow<List<Flashcard>>
- `searchFavorites()` - Flow<List<Flashcard>>
- `getAllPaginated()` - Flow<List<Flashcard>>
- `countSearchResults()` - Int

### 3. **Couche Présentation (Presentation Layer)**

#### `SearchViewModel.kt` - NOUVEAU
```kotlin
package: com.example.lexicaandroid2.presentation.search
```

**Fonctionnalités :**
- ✅ **Debounce de 300ms** avec `Flow.debounce(300)`
- ✅ StateFlow pour gestion d'état réactive
- ✅ 4 types de recherche (SearchType enum)
- ✅ Gestion du loading, erreurs, et résultats vides
- ✅ Comptage des résultats
- ✅ ViewModelFactory pour injection de dépendances

**État UI :**
```kotlin
data class SearchUiState(
    val query: String,
    val searchType: SearchType,
    val results: List<Flashcard>,
    val isLoading: Boolean,
    val totalResults: Int,
    val error: String?
)
```

#### `SearchScreen.kt` - NOUVEAU
```kotlin
package: com.example.lexicaandroid2.presentation.search
```

**Composables Material3 :**
- ✅ `SearchScreen` - Écran principal avec Scaffold
- ✅ `SearchTopBar` - Barre de recherche avec TextField
- ✅ `SearchTypeTabs` - ScrollableTabRow pour filtres
- ✅ `SearchResults` - LazyColumn avec résultats
- ✅ `FlashcardResultCard` - Card pour chaque flashcard
- ✅ `highlightQuery()` - Highlighting du terme recherché en jaune
- ✅ `EmptyState` - État vide avec icône
- ✅ `ErrorMessage` - Affichage des erreurs

**Fonctionnalités UI :**
- ✅ Highlighting automatique des termes recherchés (fond jaune)
- ✅ Affichage du nombre de résultats
- ✅ Icône favori sur les cartes
- ✅ Affichage des synonymes
- ✅ Gestion des états loading/error/empty
- ✅ Scroll infini des résultats

## 🎯 Utilisation

### Intégration dans le code

```kotlin
// 1. Créer l'instance du repository
val searchRepository = SearchRepositoryImpl(flashcardDao)

// 2. Créer le ViewModel avec factory
val viewModel = ViewModelProvider(
    this,
    SearchViewModelFactory(searchRepository)
)[SearchViewModel::class.java]

// 3. Afficher l'écran
SearchScreen(
    viewModel = viewModel,
    onNavigateBack = { navController.popBackStack() },
    onFlashcardClick = { flashcardId ->
        navController.navigate("flashcard_detail/$flashcardId")
    }
)
```

### Navigation

Ajouter la route dans votre NavHost :
```kotlin
composable("search") {
    val viewModel = viewModel<SearchViewModel>(
        factory = SearchViewModelFactory(searchRepository)
    )
    SearchScreen(
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() },
        onFlashcardClick = { id -> 
            navController.navigate("flashcard/$id") 
        }
    )
}
```

## 🚀 Performances

### Optimisations SQL
- **LOWER()** : Recherche insensible à la casse
- **LIMIT 50** : Par défaut, évite de charger trop de résultats
- **ORDER BY LENGTH()** : Mots courts en priorité
- **Indexation** : Créer index sur `mot` et `definition` pour meilleures perfs

```sql
-- À ajouter dans les migrations Room :
CREATE INDEX idx_flashcards_mot ON flashcards(mot);
CREATE INDEX idx_flashcards_definition ON flashcards(definition);
```

### Optimisations UI
- **Debounce 300ms** : Évite les requêtes trop fréquentes pendant la frappe
- **Flow** : Réactivité automatique aux changements de données
- **LazyColumn** : Affichage efficace de grandes listes
- **Key sur items** : Optimise le recomposition Compose

## 📊 Types de recherche

| Type | Description | Champs recherchés |
|------|-------------|-------------------|
| **GLOBAL** | Recherche dans tous les champs | mot, definition, synonymes |
| **BY_WORD** | Recherche uniquement dans les mots | mot |
| **BY_DEFINITION** | Recherche dans les définitions | definition |
| **FAVORITES** | Recherche dans les favoris | mot, definition (+ favori=1) |

## 🎨 Design

- **Material3** : Design moderne et cohérent
- **Couleurs** : Primary pour appbar, jaune pour highlighting
- **Icônes** : Material Icons (Search, Favorite, ArrowBack, Clear)
- **Typographie** : Hiérarchie claire (titleMedium pour mots, bodyMedium pour définitions)
- **Spacing** : 8dp/16dp pour cohérence visuelle

## 🧪 Tests suggérés

```kotlin
// Test du debounce
@Test
fun `search with debounce delays queries`() = runTest {
    viewModel.onSearchQueryChanged("test")
    advanceTimeBy(200) // Avant 300ms
    // Vérifier qu'aucune requête n'est lancée
    advanceTimeBy(150) // Total 350ms
    // Vérifier que la requête est lancée
}

// Test de recherche vide
@Test
fun `empty search returns all paginated results`() = runTest {
    viewModel.onSearchQueryChanged("")
    val state = viewModel.uiState.first()
    assertTrue(state.results.isNotEmpty())
}
```

## 📝 Notes importantes

1. **Injection de dépendances** : Créer SearchRepository dans le module DI
2. **Navigation** : Ajouter la route "search" dans NavHost
3. **Permissions** : Aucune permission nécessaire (recherche locale)
4. **Base de données** : Les requêtes utilisent Room Flow (réactivité automatique)

## 🔄 Prochaines améliorations possibles

- [ ] Historique de recherche
- [ ] Filtres avancés (catégorie grammaticale, registre)
- [ ] Recherche vocale
- [ ] Export des résultats
- [ ] Recherche par regex
- [ ] Suggestions de recherche

---

**Status** : ✅ Module complet et prêt à l'utilisation
**Performance** : ✅ Optimisé avec debounce et requêtes SQL LIKE limitées
**UI** : ✅ Material3 avec highlighting et gestion des états
