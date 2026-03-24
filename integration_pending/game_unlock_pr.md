# PR TACHE_20 — Déverrouillage progressif des mini-jeux

## Résumé

Les mini-jeux se débloquent progressivement selon l'XP du joueur. Les jeux verrouillés s'affichent en grisé avec 🔒 et "Niveau X · Y XP". Animation spring au déverrouillage.

## Fichiers créés / modifiés

```
features/gamification/domain/
└── GameUnlockConfig.kt          ← CRÉÉ  : 10 jeux avec seuils XP/niveau

presentation/games/
├── MiniGamesViewModel.kt        ← CRÉÉ  : StateFlow<List<GameWithLockState>> via UserStatsRepository
└── MiniGamesScreen.kt           ← MODIFIÉ : cartes dynamiques lock/unlock + animateFloatAsState spring
```

---

## Modification requise dans `LexicaApp.kt`

### 1. Import à ajouter

```kotlin
import com.example.lexicaandroid2.presentation.games.MiniGamesViewModel
import com.example.lexicaandroid2.presentation.games.MiniGamesViewModelFactory
```

### 2. Modifier la route `Screen.MiniGames`

**Avant :**
```kotlin
composable(route = Screen.MiniGames.route, ...) {
    MiniGamesScreen(
        onGameSelected = { gameRoute -> navController.navigate(gameRoute) },
        onBack = { navController.navigateUp() }
    )
}
```

**Après :**
```kotlin
composable(route = Screen.MiniGames.route, ...) {
    val miniGamesViewModel: MiniGamesViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(
            factory = MiniGamesViewModelFactory(userStatsRepository)
        )
    MiniGamesScreen(
        viewModel = miniGamesViewModel,
        onGameSelected = { gameRoute -> navController.navigate(gameRoute) },
        onBack = { navController.navigateUp() }
    )
}
```

`userStatsRepository` est déjà un paramètre de `LexicaApp()` — aucun changement de signature requis.

---

## Tableau de déverrouillage

| Niveau | XP requis | Jeu |
|--------|-----------|-----|
| 1 | 0 XP | 🎮 Correspondance, ❓ QCM |
| 2 | 100 XP | 🎯 Pendu |
| 3 | 300 XP | ✍️ Dictée |
| 5 | 700 XP | 🔤 Anagrammes |
| 7 | 1 300 XP | ⏱️ Mode Chrono |
| 10 | 2 100 XP | 🃏 Memory |
| 13 | 3 300 XP | 📝 Définition à Compléter |
| 16 | 4 900 XP | 🔗 Associations Sémantiques |
| 20 | 7 100 XP | 🎓 Spelling Avancé |

---

## Architecture

- **`GameUnlockConfig`** : objet singleton — données statiques, testable sans Android
- **`MiniGamesViewModel`** : `getUserStats()` → `Flow<UserStatsEntity?>` → `map { xp }` → `StateFlow<List<GameWithLockState>>`
- **`MiniGamesScreen`** : reçoit le `ViewModel` comme paramètre (testable), itère `games.forEach { GameCard(...) }`
- **`GameCard`** : `Card(onClick, enabled = isUnlocked)` + `animateFloatAsState(spring)` pour le scale bounce

## Notes

- Les jeux verrouillés restent **visibles** (spec : "pas cachée, juste verrouillée visuellement")
- Animation : scale 0.96→1.0 avec `DampingRatioMediumBouncy` quand le XP franchit le seuil
- `initialValue` du `stateIn` déverrouille uniquement les jeux à 0 XP (Matching + QCM) avant le premier emit du Flow
- Aucun fichier de jeu individuel modifié
