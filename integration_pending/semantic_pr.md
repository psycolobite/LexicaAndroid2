# TACHE_06 - Associations Semantiques

Date: 2026-03-09
Statut: Code feature pret pour integration coeur

## Livrables ajoutes

Fichiers crees:
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/semantic/SemanticViewModel.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/semantic/SemanticScreen.kt`

## Fonctionnalites implementees

- Jeu a choix multiples avec 3 types de questions:
  - synonymes (si disponibles)
  - definition correspondante
  - relation grammaticale (meme categorie)
- Verification structure donnees faite:
  - `FlashcardEntity` contient `synonymes`
  - pas de champ `antonymes` trouve
- Distracteurs algorithmiques:
  - priorite aux mots proches en longueur (fenetre des 50 plus proches)
  - fallback sur pool global pour garantir 4 choix
- Feedback visuel correct/incorrect + score + progression

## Changements coeur requis

### 1) Ajouter route dans `LexicaApp.kt`

- Ajouter dans `Screen`:
  - `data object SemanticGame : Screen("game_semantic")`

- Ajouter import:
  - `import com.example.lexicaandroid2.presentation.games.semantic.SemanticScreen`

- Ajouter composable:

```kotlin
composable(route = Screen.SemanticGame.route) {
    SemanticScreen(
        repository = repository,
        onBack = { navController.navigateUp() }
    )
}
```

- (Optionnel) titre top bar:
  - `Screen.SemanticGame.route -> "Associations"`

- (Optionnel) ajouter au `canNavigateBack`.

### 2) Ajouter entree dans `MiniGamesScreen.kt`

```kotlin
GameButton(
    text = "Associations Semantiques",
    onClick = { onGameSelected("game_semantic") }
)
```

## Notes

- Aucun fichier coeur modifie dans cette livraison.
- En l'absence de champ `antonymes`, le jeu utilise `synonymes/definition/relation`.
