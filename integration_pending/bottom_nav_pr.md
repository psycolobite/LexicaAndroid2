# PR TACHE_21 — Barre de navigation inférieure + Mode En Ligne

## Résumé

Ajout d'une `NavigationBar` Material3 permanente (masquée sur les jeux) avec 4 onglets : Accueil, Entraînement, Mini-Jeux, En Ligne. Ajout d'un écran placeholder "Mode En Ligne".

## Fichiers créés

```
presentation/
├── common/
│   └── LexicaBottomNavBar.kt     ← composable NavigationBar + GAME_ROUTES + shouldShowBottomBar()
└── online/
    ├── OnlineScreen.kt           ← écran placeholder "Bientôt disponible"
    └── OnlineModeViewModel.kt    ← ViewModel placeholder vide
```

---

## Modifications requises dans `LexicaApp.kt`

### 1. Imports à ajouter

```kotlin
import com.example.lexicaandroid2.presentation.common.LexicaBottomNavBar
import com.example.lexicaandroid2.presentation.common.shouldShowBottomBar
import com.example.lexicaandroid2.presentation.online.OnlineScreen
```

---

### 2. Ajouter `Screen.Online` dans la sealed class

**Avant :**
```kotlin
    data object Admin : Screen("admin")
    data class WordDetail(val cardId: String = "") : Screen("word/{cardId}") {
```

**Après :**
```kotlin
    data object Admin : Screen("admin")
    data object Online : Screen("online")
    data class WordDetail(val cardId: String = "") : Screen("word/{cardId}") {
```

---

### 3. Modifier le `Scaffold` pour ajouter la `bottomBar`

**Avant :**
```kotlin
    Scaffold(
        topBar = {
            LexicaTopAppBar(
                title = topBarTitle,
                canNavigateBack = canNavigateBack,
                navigateUp = { navController.navigateUp() },
                onProfileClick = if (currentRoute == Screen.Dashboard.route) {
                    { navController.navigate(Screen.Profile.route) }
                } else null
            )
        }
    ) { innerPadding ->
```

**Après :**
```kotlin
    Scaffold(
        topBar = {
            LexicaTopAppBar(
                title = topBarTitle,
                canNavigateBack = canNavigateBack,
                navigateUp = { navController.navigateUp() },
                onProfileClick = if (currentRoute == Screen.Dashboard.route) {
                    { navController.navigate(Screen.Profile.route) }
                } else null
            )
        },
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                LexicaBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
```

---

### 4. Ajouter le titre "En Ligne" dans `topBarTitle`

**Avant :**
```kotlin
        Screen.Login.route -> "Connexion"
        Screen.Register.route -> "Inscription"
        Screen.Admin.route -> "⚙️ Mode Admin"
        else -> if (currentRoute?.startsWith("word/") == true) "Détail du mot" else "Lexica"
```

**Après :**
```kotlin
        Screen.Login.route -> "Connexion"
        Screen.Register.route -> "Inscription"
        Screen.Admin.route -> "⚙️ Mode Admin"
        Screen.Online.route -> "Mode En Ligne"
        else -> if (currentRoute?.startsWith("word/") == true) "Détail du mot" else "Lexica"
```

---

### 5. Ajouter la route `composable` pour `Screen.Online`

Ajouter dans le `NavHost`, après la route `Screen.DailyChallenge` et avant la fermeture `}` :

```kotlin
            composable(route = Screen.Online.route) {
                OnlineScreen()
            }
```

---

## Comportement de la barre

| Écran | Barre visible |
|-------|--------------|
| Dashboard, Review, WordList, MiniGames, AddWords | ✅ |
| Online, Profile, DailyChallenge, Gamification | ✅ |
| Login, Register, Admin, WordDetail | ✅ |
| game_matching, game_qcm, game_hangman, game_spelling, game_anagrams, game_chrono, game_memory, game_fillword, game_semantic, game_spelling_advanced | ❌ (masquée) |

La logique de masquage est centralisée dans `shouldShowBottomBar(currentRoute)` dans `LexicaBottomNavBar.kt`.

---

## Navigation bottom bar

- Utilise `popUpTo(startDestination) { saveState = true }` + `launchSingleTop = true` + `restoreState = true` → comportement standard Material3 (pas d'accumulation de back stack)
- Tab sélectionné = `currentRoute == item.route` (comparaison exacte)
- Si déjà sur la route → tap ignoré (pas de re-navigation)

---

## Notes

- `OnlineModeViewModel` est un placeholder vide — prêt pour TACHE_23 (sync Firebase)
- `GAME_ROUTES` set dans `LexicaBottomNavBar.kt` peut être réutilisé si besoin ailleurs
- Aucune modification des écrans de jeux existants
