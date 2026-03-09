# TACHE_04 - Mini-jeu Chrono

Date: 2026-03-09
Statut: Code feature pret pour integration coeur

## Livrables ajoutes

Fichiers crees:
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/chrono/ChronoViewModel.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/chrono/ChronoScreen.kt`

## Fonctionnalites implementees

- Ecran de pre-start avec choix duree: `30s`, `60s`, `300s`
- Session chrono reactive via `StateFlow<Long>` (`timeRemainingSeconds`)
- Countdown gere par coroutine avec annulation propre (`timerJob`)
- Questions en chaine depuis `FlashcardRepository`
- Validation de reponse, score, compte des questions traitees
- XP gagnee:
  - +5 XP par bonne reponse
  - Bonus de completion via `XPCalculator.calculateXpForGame(...)`
- Ecran resultat de fin avec score et XP

## Changements coeur requis

### 1) Ajouter une route dans `LexicaApp.kt`

- Ajouter dans `Screen`:
  - `data object ChronoGame : Screen("game_chrono")`

- Ajouter import:
  - `import com.example.lexicaandroid2.presentation.games.chrono.ChronoScreen`

- Ajouter un `composable`:

```kotlin
composable(route = Screen.ChronoGame.route) {
    ChronoScreen(
        repository = repository,
        onBack = { navController.navigateUp() }
    )
}
```

- (Optionnel UI title) ajouter dans `topBarTitle`:
  - `Screen.ChronoGame.route -> "Chrono"`

- (Optionnel back nav) ajouter dans `canNavigateBack`.

### 2) Ajouter l'entree dans `MiniGamesScreen.kt`

Ajouter un bouton:

```kotlin
GameButton(
    text = "Mode Chrono",
    onClick = { onGameSelected("game_chrono") }
)
```

## Notes

- Aucun fichier coeur modifie dans cette livraison.
- Aucune dependance Gradle supplementaire requise.
