# TACHE_08 - Ecran Profil Utilisateur

Date: 2026-03-09
Statut: Code feature pret pour integration coeur

## Livrables ajoutes

Fichiers crees:
- `app/src/main/java/com/example/lexicaandroid2/presentation/profile/ProfileViewModel.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/profile/ProfileScreen.kt`

## Fonctionnalites implementees

- Ecran profil complet avec:
  - Avatar/initiales utilisateur
  - Nom + email (ou `Invité`)
  - Bloc progression avec `XpProgressBar` existante
  - Affichage streak courant
  - Statistiques (`mots appris` + compteur parties par type, actuellement initialise)
  - Bouton auth `Se connecter` / `Se déconnecter` (hook `AuthRepository`)
- `ProfileViewModel` combine:
  - `AuthRepository.currentUser`
  - `UserStatsRepository.getUserStats()`
  - `FlashcardRepository.getAllCards()` pour total mots

## Integration coeur requise

### 1) `LexicaApp.kt`

- Ajouter dans `sealed class Screen`:
  - `data object Profile : Screen("profile")`

- Ajouter `composable`:

```kotlin
composable(route = Screen.Profile.route) {
    ProfileScreen(
        flashcardRepository = repository,
        userStatsRepository = userStatsRepository,
        authRepository = authRepository,
        onBack = { navController.navigateUp() },
        onSignInRequested = { navController.navigate("login") }
    )
}
```

### 2) `LexicaTopAppBar.kt`

- Ajouter un `IconButton` (action de droite) avec `Icons.Default.AccountCircle`
- L'afficher uniquement quand `currentRoute == Screen.Dashboard.route`
- Action: navigation vers `Screen.Profile.route`

### 3) `DashboardScreen.kt`

- Ajouter parametre `onNavigateToProfile: () -> Unit`
- Relayer l'action de top bar vers la navigation profil

## Notes

- Aucun fichier coeur modifie dans cette livraison.
- `gameStats` est prepare dans l'UI state; branchement des compteurs reels a connecter quand les donnees de sessions jeux seront stockees en repository.
