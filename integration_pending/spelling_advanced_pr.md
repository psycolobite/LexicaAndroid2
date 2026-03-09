# TACHE_06 - Spelling Avance

Date: 2026-03-09
Statut: Code feature pret pour integration coeur

## Livrables ajoutes

Fichiers crees:
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/spellingadvanced/SpellingAdvancedViewModel.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/spellingadvanced/SpellingAdvancedScreen.kt`

## Fonctionnalites implementees

- Mini-jeu de dictee avance avec TTS et callbacks robustes (`onStart`/`onDone`)
- Tolerance orthographique:
  - accents/diacritiques via `Normalizer`
  - variantes singulier/pluriel (`s`, `x`, `es`)
- 3 jokers par partie:
  - reveler une lettre
  - rejouer l'audio
  - sauter le mot
- Feedback detaille en cas d'erreur (distance de Levenshtein + message contextuel)

## Changements coeur requis

### 1) Ajouter route dans `LexicaApp.kt`

- Ajouter dans `Screen`:
  - `data object SpellingAdvancedGame : Screen("game_spelling_advanced")`

- Ajouter import:
  - `import com.example.lexicaandroid2.presentation.games.spellingadvanced.SpellingAdvancedScreen`

- Ajouter composable:

```kotlin
composable(route = Screen.SpellingAdvancedGame.route) {
    SpellingAdvancedScreen(
        repository = repository,
        onBack = { navController.navigateUp() }
    )
}
```

- (Optionnel) titre top bar:
  - `Screen.SpellingAdvancedGame.route -> "Spelling Avance"`

- (Optionnel) ajouter au `canNavigateBack`.

### 2) Ajouter entree dans `MiniGamesScreen.kt`

```kotlin
GameButton(
    text = "Spelling Avance",
    onClick = { onGameSelected("game_spelling_advanced") }
)
```

## Notes

- Aucun fichier coeur modifie dans cette livraison.
- Le spelling existant n'a pas ete modifie.
