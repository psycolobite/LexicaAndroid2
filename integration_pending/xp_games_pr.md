# TACHE_10 - Connexion XP aux mini-jeux existants

Date: 2026-03-09
Statut: Code feature pret pour integration coeur

## Fichiers modifies

- `app/src/main/java/com/example/lexicaandroid2/presentation/games/matching/MatchingScreen.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/qcm/QcmScreen.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/hangman/HangmanScreen.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/hangman/HangmanViewModel.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/qcm/SpellingGameScreen.kt`

## Changements implementes

### 1) Hook XP dans les jeux

Chaque écran expose maintenant:
- `onAwardXp: (Int) -> Unit = {}`

Un envoi XP est déclenché **une seule fois** quand `gameOver == true`.

### 2) Règle XP appliquée

- parfait (`score == total`) -> `25 XP`
- réussi (`>= 50%`) -> `15 XP`
- échec (`< 50%`) -> `5 XP`

### 3) Correctif support Hangman

- `HangmanViewModel` renseigne `totalWords` au chargement pour avoir un calcul XP/progression correct.
- `HangmanScreen` affiche l'écran de fin dès que `gameOver == true`.

## Integration coeur requise

### `LexicaApp.kt`

Brancher `GamificationViewModel.addXp(amount)` lors de l’appel des écrans:

```kotlin
MatchingScreen(
    repository = repository,
    onBack = { navController.navigateUp() },
    onAwardXp = { amount -> gamificationViewModel.addXp(amount) }
)

QcmScreen(
    repository = repository,
    onBack = { navController.navigateUp() },
    onAwardXp = { amount -> gamificationViewModel.addXp(amount) }
)

HangmanScreen(
    repository = repository,
    onBack = { navController.navigateUp() },
    onAwardXp = { amount -> gamificationViewModel.addXp(amount) }
)

SpellingGameScreen(
    repository = repository,
    onBack = { navController.navigateUp() },
    onAwardXp = { amount -> gamificationViewModel.addXp(amount) }
)
```

## Notes

- Aucun fichier coeur modifié dans cette livraison.
- Le hook est backward-compatible (`onAwardXp` optionnel avec no-op).
