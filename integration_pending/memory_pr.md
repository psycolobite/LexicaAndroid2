# TACHE_05 - Mini-jeu Memory

Date: 2026-03-09
Statut: Code feature pret pour integration coeur

## Livrables ajoutes

Fichiers crees:
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/memory/MemoryViewModel.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/memory/MemoryScreen.kt`

## Fonctionnalites implementees

- Grilles disponibles: `4x4`, `5x4`, `6x4`
- Generation d'un deck de paires `mot ↔ definition`
- Flip de cartes et verification de paires
- Score, tentatives, progression et ecran de fin
- `bestScore` de session (memoire runtime)

## Changements coeur requis

### 1) Ajouter une route dans `LexicaApp.kt`

- Ajouter dans `Screen`:
  - `data object MemoryGame : Screen("game_memory")`

- Ajouter import:
  - `import com.example.lexicaandroid2.presentation.games.memory.MemoryScreen`

- Ajouter un `composable`:

```kotlin
composable(route = Screen.MemoryGame.route) {
    MemoryScreen(
        repository = repository,
        onBack = { navController.navigateUp() }
    )
}
```

### 2) Ajouter l'entree dans `MiniGamesScreen.kt`

Ajouter un bouton:

```kotlin
GameButton(
    text = "🧠 Memory",
    onClick = { onGameSelected("game_memory") }
)
```

### 3) Best score persistant (demande Room)

Le meilleur score est actuellement en memoire (session). Pour persister:

- Ajouter une entite Room dediee (ex: `MemoryBestScoreEntity`)
- Ajouter DAO + repository
- Brancher lecture/ecriture dans `MemoryViewModel`

## Notes

- Aucun fichier coeur modifie dans cette livraison.
- Aucune dependance Gradle supplementaire requise.
