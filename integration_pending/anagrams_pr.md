# TACHE_04 - Mini-jeu Anagrams

Date: 2026-03-09
Statut: Code feature pret pour integration coeur

## Livrables ajoutes

Fichiers crees:
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/anagrams/AnagramsViewModel.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/anagrams/AnagramsScreen.kt`

## Fonctionnalites implementees

- Chargement d'un set de mots depuis `FlashcardRepository`
- Mot nettoye et melange en tuiles de lettres
- Construction de la reponse par selection de tuiles
- Boutons `Undo`, `Clear`, `Validate`, puis `Next`
- Scoring par manche + total
- XP gagnee:
  - +5 XP par bonne reponse
  - Bonus de fin de partie via `XPCalculator.calculateXpForGame(...)`
- Ecran de fin avec score et XP

## Changements coeur requis

### 1) Ajouter une route dans `LexicaApp.kt`

- Ajouter dans `Screen`:
  - `data object AnagramsGame : Screen("game_anagrams")`

- Ajouter import:
  - `import com.example.lexicaandroid2.presentation.games.anagrams.AnagramsScreen`

- Ajouter un `composable`:

```kotlin
composable(route = Screen.AnagramsGame.route) {
    AnagramsScreen(
        repository = repository,
        onBack = { navController.navigateUp() }
    )
}
```

- (Optionnel UI title) ajouter dans `topBarTitle`:
  - `Screen.AnagramsGame.route -> "Anagrams"`

- (Optionnel back nav) ajouter dans `canNavigateBack`.

### 2) Ajouter l'entree dans `MiniGamesScreen.kt`

Ajouter un bouton:

```kotlin
GameButton(
    text = "Anagrams",
    onClick = { onGameSelected("game_anagrams") }
)
```

## Notes

- Aucun fichier coeur modifie dans cette livraison.
- Aucune dependance Gradle supplementaire requise.
