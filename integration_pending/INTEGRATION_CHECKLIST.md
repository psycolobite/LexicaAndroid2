# ✅ Checklist d'intégration du module de recherche

## 📋 Étapes à suivre

### Phase 1: Vérification (2 min)

- [ ] ✅ Tous les fichiers sont créés
  - [ ] `SearchRepository.kt` (domain)
  - [ ] `SearchRepositoryImpl.kt` (data)
  - [ ] `SearchViewModel.kt` (presentation)
  - [ ] `SearchScreen.kt` (presentation)
  - [ ] `FlashcardDao.kt` modifié avec 6 nouvelles requêtes
  - [ ] `SearchViewModelTest.kt` (tests)

- [ ] ✅ Documentation disponible
  - [ ] `SEARCH_MODULE_SUMMARY.md` lu
  - [ ] `search_integration_guide.md` compris
  - [ ] `search_sql_optimization.md` consulté

### Phase 2: Intégration dans MainActivity (5 min)

#### Étape 1: Créer SearchRepository

Dans `MainActivity.onCreate()`, après `reserveRepository`:

```kotlin
// ✨ AJOUTER
import com.example.lexicaandroid2.data.repository.SearchRepositoryImpl
import com.example.lexicaandroid2.presentation.search.SearchViewModel
import com.example.lexicaandroid2.presentation.search.SearchViewModelFactory

val searchRepository = SearchRepositoryImpl(dao)
```

- [ ] ✅ Import ajouté
- [ ] ✅ SearchRepository créé

#### Étape 2: Créer SearchViewModel

```kotlin
val searchFactory = SearchViewModelFactory(searchRepository)
val searchViewModel = ViewModelProvider(this, searchFactory)[SearchViewModel::class.java]
```

- [ ] ✅ Factory créée
- [ ] ✅ ViewModel créé

#### Étape 3: Passer à LexicaApp

Modifier l'appel à `LexicaApp()`:

```kotlin
LexicaApp(
    navController = navController,
    reviewViewModel = reviewViewModel,
    dashboardViewModel = dashboardViewModel,
    wordListViewModel = wordListViewModel,
    addWordsViewModel = addWordsViewModel,
    searchViewModel = searchViewModel,  // ✨ AJOUTER
    repository = repository
)
```

- [ ] ✅ Paramètre ajouté à LexicaApp

### Phase 3: Intégration dans LexicaApp (5 min)

#### Étape 1: Ajouter la route

Dans le sealed class `Screen`:

```kotlin
data object Search : Screen("search")
```

- [ ] ✅ Route Search ajoutée

#### Étape 2: Modifier signature de LexicaApp

```kotlin
@Composable
fun LexicaApp(
    reviewViewModel: ReviewViewModel,
    dashboardViewModel: DashboardViewModel,
    wordListViewModel: WordListViewModel,
    addWordsViewModel: AddWordsViewModel,
    searchViewModel: SearchViewModel,  // ✨ AJOUTER
    repository: FlashcardRepository,
    navController: NavHostController = rememberNavController()
) {
```

- [ ] ✅ Paramètre searchViewModel ajouté

#### Étape 3: Ajouter titre

Dans `topBarTitle`:

```kotlin
val topBarTitle = when (currentRoute) {
    Screen.Dashboard.route -> ""
    Screen.Review.route -> "Review (${reviewUiState.scrum})"
    Screen.WordList.route -> "Mes mots"
    Screen.Search.route -> "Recherche"  // ✨ AJOUTER
    Screen.MiniGames.route -> "Mini-Jeux"
    // ...
}
```

- [ ] ✅ Titre "Recherche" ajouté

#### Étape 4: Ajouter gestion du retour

Dans `canNavigateBack`:

```kotlin
val canNavigateBack = currentRoute == Screen.Review.route || 
                     currentRoute == Screen.WordList.route ||
                     currentRoute == Screen.Search.route ||  // ✨ AJOUTER
                     // ...
```

- [ ] ✅ Navigation retour ajoutée

#### Étape 5: Ajouter le composable

Dans le `NavHost`, ajouter:

```kotlin
composable(
    route = Screen.Search.route,
    enterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left, 
            tween(300)
        )
    },
    exitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right, 
            tween(300)
        )
    }
) {
    SearchScreen(
        viewModel = searchViewModel,
        onNavigateBack = { navController.navigateUp() },
        onFlashcardClick = { flashcardId ->
            // TODO: Implémenter navigation vers détail
            navController.navigateUp()  // Temporaire
        }
    )
}
```

- [ ] ✅ Composable Search ajouté au NavHost
- [ ] ✅ Imports nécessaires ajoutés

### Phase 4: Ajouter bouton de navigation (Optionnel, 3 min)

#### Option A: Depuis le Dashboard

Modifier `DashboardScreen.kt`:

1. Ajouter paramètre `onNavigateToSearch: () -> Unit`
2. Ajouter un bouton:

```kotlin
Button(
    onClick = onNavigateToSearch,
    modifier = Modifier.fillMaxWidth()
) {
    Icon(Icons.Default.Search, contentDescription = null)
    Spacer(modifier = Modifier.width(8.dp))
    Text("🔍 Rechercher")
}
```

3. Dans LexicaApp, passer:

```kotlin
DashboardScreen(
    // ... autres params
    onNavigateToSearch = { navController.navigate(Screen.Search.route) }
)
```

- [ ] ✅ Bouton ajouté au Dashboard
- [ ] ✅ Navigation depuis Dashboard fonctionne

#### Option B: Depuis la TopBar

Ajouter une icône de recherche dans `LexicaTopAppBar`.

- [ ] ✅ Icône ajoutée à la TopBar

### Phase 5: Tests (5 min)

#### Test de compilation

```bash
./gradlew assembleDebug
```

- [ ] ✅ Compilation réussie
- [ ] ✅ Aucune erreur

#### Test unitaire

```bash
./gradlew test --tests SearchViewModelTest
```

- [ ] ✅ Tests passent
- [ ] ⚠️ Si échec: Installer dépendances (voir ci-dessous)

#### Test manuel

1. Lancer l'app
2. Naviguer vers Search
3. Taper "test" dans la barre
4. Attendre 300ms
5. Vérifier résultats

- [ ] ✅ Écran de recherche s'affiche
- [ ] ✅ Barre de recherche fonctionne
- [ ] ✅ Debounce fonctionne (pas de requête avant 300ms)
- [ ] ✅ Résultats s'affichent
- [ ] ✅ Highlighting jaune visible
- [ ] ✅ Onglets fonctionnent
- [ ] ✅ Nombre de résultats affiché
- [ ] ✅ Bouton retour fonctionne
- [ ] ✅ État vide s'affiche correctement
- [ ] ✅ Favoris filtrent correctement

### Phase 6: Optimisation SQL (Optionnel, 10 min)

Suivre le guide `search_sql_optimization.md`:

#### Créer migration

```kotlin
// Dans data/local/migrations/Migrations.kt
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE INDEX idx_flashcards_mot ON flashcards(mot)")
        database.execSQL("CREATE INDEX idx_flashcards_definition ON flashcards(definition)")
        database.execSQL("CREATE INDEX idx_flashcards_favori ON flashcards(favori)")
    }
}
```

- [ ] ✅ Fichier de migration créé

#### Appliquer migration

Dans MainActivity:

```kotlin
val database = Room.databaseBuilder(...)
    .addMigrations(MIGRATION_1_2)  // ✨ AJOUTER
    .fallbackToDestructiveMigration()
    .build()
```

- [ ] ✅ Migration ajoutée
- [ ] ✅ App testée après migration
- [ ] ✅ Performances améliorées (~10x)

### Phase 7: Dépendances (Si tests échouent)

Vérifier `app/build.gradle`:

```kotlin
dependencies {
    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")  // ✨ Vérifier
}
```

- [ ] ✅ Dépendances vérifiées
- [ ] ✅ Gradle sync effectué

### Phase 8: Finalisation

- [ ] ✅ Code commité sur Git
- [ ] ✅ Documentation mise à jour (README)
- [ ] ✅ Screenshots pris (optionnel)
- [ ] ✅ Performance mesurée
- [ ] ✅ Module de recherche opérationnel 🎉

## 🚨 Troubleshooting

### Problème: Compilation échoue

**Solution:**
1. Vérifier tous les imports
2. Sync Gradle
3. Clean + Rebuild

```bash
./gradlew clean build
```

### Problème: Tests échouent

**Solution:**
1. Installer Turbine: `testImplementation("app.cash.turbine:turbine:1.0.0")`
2. Sync Gradle
3. Relancer tests

### Problème: SearchScreen ne s'affiche pas

**Solution:**
1. Vérifier que searchViewModel est passé à LexicaApp
2. Vérifier que la route "search" est bien dans NavHost
3. Vérifier les imports

### Problème: Debounce ne fonctionne pas

**Solution:**
1. Vérifier que FlowPreview est importé: `@OptIn(FlowPreview::class)`
2. Vérifier la version des coroutines: `1.7.3+`

### Problème: Requêtes SQL lentes

**Solution:**
1. Ajouter les index (voir Phase 6)
2. Vérifier LIMIT 50 dans les requêtes
3. Profiler avec Android Profiler

## 📞 Support

Référence documentation:
- 📊 `SEARCH_MODULE_SUMMARY.md` - Vue d'ensemble
- 🔧 `search_integration_guide.md` - Guide détaillé
- ⚡ `search_sql_optimization.md` - Optimisations
- 📁 `SEARCH_MODULE_STRUCTURE.md` - Architecture

## 🎯 Résultat attendu

Après intégration complète:

✅ Module de recherche fonctionnel
✅ Debounce de 300ms actif
✅ 4 types de recherche disponibles
✅ Highlighting des résultats
✅ Interface Material3 fluide
✅ Performances optimales

## ⏱️ Temps total estimé

- Phase 1: 2 min (Vérification)
- Phase 2: 5 min (MainActivity)
- Phase 3: 5 min (LexicaApp)
- Phase 4: 3 min (Bouton navigation - optionnel)
- Phase 5: 5 min (Tests)
- Phase 6: 10 min (Optimisation SQL - optionnel)

**Total: 15-30 minutes**

---

**Status:** 🚀 Ready for integration!

Bonne intégration! 🎉
