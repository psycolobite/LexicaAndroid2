# 🔧 Guide d'intégration du module de recherche

## Étapes d'intégration dans MainActivity et LexicaApp

### 1️⃣ Mise à jour de MainActivity.kt

Ajouter la création du `SearchRepository` et `SearchViewModel` :

```kotlin
// Dans MainActivity.onCreate(), après la création des autres repositories

// Ajouter SearchRepository
val searchRepository = SearchRepositoryImpl(dao)

// Créer SearchViewModel avec factory
val searchFactory = SearchViewModelFactory(searchRepository)
val searchViewModel = ViewModelProvider(this, searchFactory)[SearchViewModel::class.java]
```

Puis passer le `searchViewModel` à `LexicaApp` :

```kotlin
LexicaApp(
    navController = navController,
    reviewViewModel = reviewViewModel,
    dashboardViewModel = dashboardViewModel,
    wordListViewModel = wordListViewModel,
    addWordsViewModel = addWordsViewModel,
    searchViewModel = searchViewModel,  // ← AJOUTER
    repository = repository
)
```

### 2️⃣ Mise à jour de LexicaApp.kt

#### A. Ajouter la route Search dans le sealed class Screen

```kotlin
sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Review : Screen("review")
    data object WordList : Screen("wordlist?filter={filter}") {
        fun createRoute(filter: String? = null) = "wordlist?filter=${filter ?: ""}"
    }
    data object Search : Screen("search")  // ← AJOUTER
    data object AddWords : Screen("add_words")
    data object MiniGames : Screen("mini_games")
    data object MatchingGame : Screen("game_matching")
    data object QcmGame : Screen("game_qcm")
    data object HangmanGame : Screen("game_hangman")
}
```

#### B. Ajouter le paramètre searchViewModel dans LexicaApp

```kotlin
@Composable
fun LexicaApp(
    reviewViewModel: ReviewViewModel,
    dashboardViewModel: DashboardViewModel,
    wordListViewModel: WordListViewModel,
    addWordsViewModel: AddWordsViewModel,
    searchViewModel: SearchViewModel,  // ← AJOUTER
    repository: FlashcardRepository,
    navController: NavHostController = rememberNavController()
) {
    // ... reste du code
}
```

#### C. Ajouter le titre dans topBarTitle

```kotlin
val topBarTitle = when (currentRoute) {
    Screen.Dashboard.route -> ""
    Screen.Review.route -> "Review (${reviewUiState.scrum})"
    Screen.WordList.route -> "Mes mots"
    Screen.Search.route -> "Recherche"  // ← AJOUTER
    Screen.MiniGames.route -> "Mini-Jeux"
    // ... autres routes
    else -> "Lexica"
}
```

#### D. Ajouter la gestion du bouton retour

```kotlin
val canNavigateBack = currentRoute == Screen.Review.route || 
                     currentRoute == Screen.WordList.route ||
                     currentRoute == Screen.Search.route ||  // ← AJOUTER
                     currentRoute == Screen.MiniGames.route || 
                     // ... autres routes
```

#### E. Ajouter la route dans le NavHost

Dans le `NavHost`, ajouter après le composable `Dashboard` ou `WordList` :

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
            // Navigation vers le détail d'une flashcard
            // À adapter selon votre système de navigation
            navController.navigate("flashcard_detail/$flashcardId")
        }
    )
}
```

### 3️⃣ Ajouter un bouton de navigation vers Search

#### Dans DashboardScreen.kt

Ajouter un bouton pour accéder à la recherche :

```kotlin
// Exemple d'ajout d'un bouton dans DashboardContent
Button(
    onClick = { onNavigateToSearch() },
    modifier = Modifier.fillMaxWidth(),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
) {
    Icon(Icons.Default.Search, contentDescription = null)
    Spacer(modifier = Modifier.width(8.dp))
    Text("Rechercher")
}
```

Et modifier la signature de `DashboardScreen` :

```kotlin
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToReview: () -> Unit,
    onNavigateToWordList: () -> Unit,
    onNavigateToWordListFiltered: (String) -> Unit,
    onNavigateToAddWords: () -> Unit,
    onNavigateToMiniGames: () -> Unit,
    onNavigateToSearch: () -> Unit  // ← AJOUTER
) {
    // ... reste du code
}
```

Et dans LexicaApp, mettre à jour l'appel :

```kotlin
composable(route = Screen.Dashboard.route) {
    DashboardScreen(
        viewModel = dashboardViewModel,
        onNavigateToReview = { navController.navigate(Screen.Review.route) },
        onNavigateToWordList = { navController.navigate(Screen.WordList.createRoute()) },
        onNavigateToWordListFiltered = { filter ->
            navController.navigate(Screen.WordList.createRoute(filter))
        },
        onNavigateToAddWords = { navController.navigate(Screen.AddWords.route) },
        onNavigateToMiniGames = { navController.navigate(Screen.MiniGames.route) },
        onNavigateToSearch = { navController.navigate(Screen.Search.route) }  // ← AJOUTER
    )
}
```

### 4️⃣ Imports nécessaires

Ajouter dans vos fichiers :

```kotlin
// MainActivity.kt
import com.example.lexicaandroid2.data.repository.SearchRepositoryImpl
import com.example.lexicaandroid2.presentation.search.SearchViewModel
import com.example.lexicaandroid2.presentation.search.SearchViewModelFactory

// LexicaApp.kt
import com.example.lexicaandroid2.presentation.search.SearchScreen
import com.example.lexicaandroid2.presentation.search.SearchViewModel
```

## ✅ Checklist d'intégration

- [ ] Créer `SearchRepositoryImpl` dans MainActivity
- [ ] Créer `SearchViewModel` avec factory
- [ ] Passer `searchViewModel` à LexicaApp
- [ ] Ajouter route `Search` dans sealed class
- [ ] Ajouter paramètre `searchViewModel` dans LexicaApp signature
- [ ] Ajouter titre "Recherche" dans topBarTitle
- [ ] Ajouter Search dans canNavigateBack
- [ ] Ajouter composable Search dans NavHost
- [ ] Ajouter bouton de navigation vers Search (optionnel)
- [ ] Vérifier les imports

## 🧪 Test de l'intégration

1. Lancer l'application
2. Cliquer sur le bouton "Rechercher" (si ajouté)
3. Ou naviguer directement vers `Screen.Search.route`
4. Vérifier que :
   - La barre de recherche s'affiche
   - Le debounce fonctionne (attendre 300ms après frappe)
   - Les onglets Global/Mots/Définitions/Favoris fonctionnent
   - Le highlighting jaune apparaît sur les résultats
   - Le clic sur une carte fonctionne (si onFlashcardClick est implémenté)

## 🎯 Navigation vers le détail d'une flashcard

Si vous n'avez pas encore d'écran de détail, vous pouvez temporairement :

```kotlin
onFlashcardClick = { flashcardId ->
    // Temporaire : retour à la liste avec filtre
    navController.navigate(Screen.WordList.createRoute())
}
```

Ou créer un écran de détail ultérieurement.

---

**Status** : ✅ Guide complet pour l'intégration
**Difficulté** : ⭐⭐☆☆☆ (Facile - modifications mineures)
**Temps estimé** : 10-15 minutes
