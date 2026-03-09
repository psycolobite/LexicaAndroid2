# 📁 Structure du Module de Recherche

```
LexicaAndroid2/
│
├── app/
│   └── src/
│       ├── main/
│       │   └── java/com/example/lexicaandroid2/
│       │       │
│       │       ├── data/
│       │       │   ├── local/
│       │       │   │   └── FlashcardDao.kt ✏️ (MODIFIÉ)
│       │       │   │       ├── searchByWord()
│       │       │   │       ├── searchByDefinition()
│       │       │   │       ├── searchGlobal()
│       │       │   │       ├── searchFavorites()
│       │       │   │       ├── getAllPaginated()
│       │       │   │       └── countSearchResults()
│       │       │   │
│       │       │   └── repository/
│       │       │       └── SearchRepositoryImpl.kt ✨ (NOUVEAU)
│       │       │           └── Implémente SearchRepository
│       │       │
│       │       ├── domain/
│       │       │   └── repository/
│       │       │       └── SearchRepository.kt ✨ (NOUVEAU)
│       │       │           └── Interface avec 6 méthodes
│       │       │
│       │       └── presentation/
│       │           └── search/ ✨ (NOUVEAU PACKAGE)
│       │               ├── SearchViewModel.kt
│       │               │   ├── SearchUiState
│       │               │   ├── SearchType enum
│       │               │   ├── Debounce 300ms ⏱️
│       │               │   └── SearchViewModelFactory
│       │               │
│       │               └── SearchScreen.kt
│       │                   ├── SearchScreen()
│       │                   ├── SearchTopBar()
│       │                   ├── SearchTypeTabs()
│       │                   ├── SearchResults()
│       │                   ├── FlashcardResultCard()
│       │                   ├── highlightQuery() 🟡
│       │                   ├── EmptyState()
│       │                   └── ErrorMessage()
│       │
│       └── test/
│           └── java/com/example/lexicaandroid2/
│               └── presentation/
│                   └── search/
│                       └── SearchViewModelTest.kt ✨ (NOUVEAU)
│                           └── 11 tests unitaires
│
└── integration_pending/ ✨ (NOUVEAU DOSSIER)
    ├── SEARCH_MODULE_SUMMARY.md         📊 Vue d'ensemble
    ├── search_module_implementation.md  📖 Documentation technique
    ├── search_integration_guide.md      🔧 Guide d'intégration
    ├── search_sql_optimization.md       ⚡ Guide optimisation
    └── search_pr.md                     📝 PR originale
```

## 🎯 Fichiers par catégorie

### Data Layer (2 fichiers)
```
✏️ FlashcardDao.kt               - 6 requêtes SQL avec LIKE + LIMIT
✨ SearchRepositoryImpl.kt       - Implémentation du repository
```

### Domain Layer (1 fichier)
```
✨ SearchRepository.kt           - Interface de contrat
```

### Presentation Layer (2 fichiers)
```
✨ SearchViewModel.kt            - ViewModel avec debounce 300ms
✨ SearchScreen.kt               - UI Material3 complète
```

### Tests (1 fichier)
```
✨ SearchViewModelTest.kt        - 11 tests unitaires
```

### Documentation (4 fichiers)
```
📊 SEARCH_MODULE_SUMMARY.md      - Résumé complet du module
📖 search_module_implementation.md - Documentation technique
🔧 search_integration_guide.md    - Guide d'intégration
⚡ search_sql_optimization.md     - Guide performances SQL
```

## 🔗 Flux de données

```
┌─────────────────────────────────────────────────────────┐
│                      SearchScreen.kt                     │
│  ┌────────────────────────────────────────────────────┐ │
│  │ SearchTopBar (TextField avec SearchIcon)           │ │
│  └────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────┐ │
│  │ SearchTypeTabs (Global, Mot, Def, Favoris)        │ │
│  └────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────┐ │
│  │ LazyColumn (FlashcardResultCard avec highlighting)│ │
│  └────────────────────────────────────────────────────┘ │
└───────────────────┬─────────────────────────────────────┘
                    │ User input
                    ↓
┌─────────────────────────────────────────────────────────┐
│                   SearchViewModel.kt                     │
│  ┌────────────────────────────────────────────────────┐ │
│  │ onSearchQueryChanged(query)                        │ │
│  │         ↓                                          │ │
│  │ MutableStateFlow<String> (_searchQuery)            │ │
│  │         ↓                                          │ │
│  │ debounce(300ms)    ⏱️                              │ │
│  │         ↓                                          │ │
│  │ distinctUntilChanged()                             │ │
│  │         ↓                                          │ │
│  │ performSearch(query, searchType)                   │ │
│  └────────────────────────────────────────────────────┘ │
└───────────────────┬─────────────────────────────────────┘
                    │ Repository call
                    ↓
┌─────────────────────────────────────────────────────────┐
│                SearchRepositoryImpl.kt                   │
│  ┌────────────────────────────────────────────────────┐ │
│  │ searchGlobal(query, limit) → Flow<List<Flashcard>>│ │
│  │         ↓                                          │ │
│  │ dao.searchGlobal(query, limit)                     │ │
│  │         ↓                                          │ │
│  │ entities.map { it.toDomain() }                     │ │
│  └────────────────────────────────────────────────────┘ │
└───────────────────┬─────────────────────────────────────┘
                    │ SQL query
                    ↓
┌─────────────────────────────────────────────────────────┐
│                     FlashcardDao.kt                      │
│  ┌────────────────────────────────────────────────────┐ │
│  │ @Query("""                                         │ │
│  │   SELECT * FROM flashcards                         │ │
│  │   WHERE LOWER(mot) LIKE '%' || LOWER(:query) || '%'│ │
│  │      OR LOWER(definition) LIKE '%' || :query || '%'│ │
│  │      OR LOWER(synonymes) LIKE '%' || :query || '%' │ │
│  │   ORDER BY LENGTH(mot) ASC                         │ │
│  │   LIMIT 50                                         │ │
│  │ """)                                               │ │
│  └────────────────────────────────────────────────────┘ │
└───────────────────┬─────────────────────────────────────┘
                    │ Results
                    ↓
┌─────────────────────────────────────────────────────────┐
│                    SQLite Database                       │
│              flashcards table (7000+ rows)               │
└─────────────────────────────────────────────────────────┘
```

## ⚡ Optimisations appliquées

### 1. Debounce (300ms)
```kotlin
_searchQuery
    .debounce(300)  // ⏱️ Attend 300ms après dernière frappe
    .distinctUntilChanged()  // 🔄 Évite doublons
    .collectLatest { query ->
        performSearch(query, searchType)
    }
```

### 2. Requêtes SQL optimisées
```sql
-- LOWER() pour insensibilité casse
LOWER(mot) LIKE '%' || LOWER(:query) || '%'

-- LIMIT pour éviter surcharge
LIMIT 50

-- ORDER BY intelligent
ORDER BY LENGTH(mot) ASC
```

### 3. Flow pour réactivité
```kotlin
fun searchGlobal(query: String): Flow<List<FlashcardEntity>>
```

## 🎨 UI Components

```
SearchScreen
├── Scaffold
│   ├── TopAppBar (SearchTopBar)
│   │   ├── NavigationIcon (ArrowBack)
│   │   └── TextField
│   │       ├── LeadingIcon (Search)
│   │       └── TrailingIcon (Clear)
│   └── Content
│       ├── ScrollableTabRow (SearchTypeTabs)
│       │   ├── Tab "Global"
│       │   ├── Tab "Mots"
│       │   ├── Tab "Définitions"
│       │   └── Tab "Favoris" ⭐
│       ├── Text (Résultats count)
│       └── LazyColumn (SearchResults)
│           └── Card (FlashcardResultCard) × N
│               ├── Text (mot) - avec highlighting 🟡
│               ├── Text (définition) - avec highlighting 🟡
│               ├── Icon (Favorite) si favori
│               └── Text (synonymes)
```

## 📊 États de l'application

```
SearchUiState
├── query: String                 // Texte recherché
├── searchType: SearchType        // GLOBAL | BY_WORD | BY_DEFINITION | FAVORITES
├── results: List<Flashcard>      // Résultats
├── isLoading: Boolean            // État chargement
├── totalResults: Int             // Nombre de résultats
└── error: String?                // Message d'erreur
```

## 🔄 Cycle de vie d'une recherche

```
1. User types "test" → onSearchQueryChanged("test")
2. UiState.query = "test", isLoading = true
3. _searchQuery.value = "test"
4. Wait 300ms (debounce) ⏱️
5. If query changed → restart timer
6. If 300ms passed → performSearch("test", GLOBAL)
7. repository.searchGlobal("test", 50)
8. dao.searchGlobal("test", 50) → SQL query
9. Results Flow<List<FlashcardEntity>>
10. Map to List<Flashcard> (toDomain)
11. Update UiState: results, isLoading = false
12. UI recomposes with highlighted results 🟡
```

## 🏗️ Architecture layers

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (SearchScreen + SearchViewModel)       │
│                                         │
│  - UI Logic                             │
│  - State Management                     │
│  - Debounce                            │
└────────────┬────────────────────────────┘
             │ SearchRepository interface
             ↓
┌─────────────────────────────────────────┐
│          Domain Layer                   │
│     (SearchRepository interface)        │
│                                         │
│  - Business contracts                   │
│  - Domain models (Flashcard)            │
└────────────┬────────────────────────────┘
             │ Implementation
             ↓
┌─────────────────────────────────────────┐
│           Data Layer                    │
│  (SearchRepositoryImpl + FlashcardDao)  │
│                                         │
│  - Data sources                         │
│  - SQL queries                          │
│  - Entity mapping                       │
└────────────┬────────────────────────────┘
             │ Room Database
             ↓
┌─────────────────────────────────────────┐
│          SQLite Database                │
│        (flashcards table)               │
└─────────────────────────────────────────┘
```

---

**Légende:**
- ✨ Nouveau fichier créé
- ✏️ Fichier modifié
- ⏱️ Debounce implémenté
- 🟡 Highlighting activé
- ⚡ Optimisé pour performance
- 🔒 Type-safe avec Kotlin
- 📊 Tests inclus
