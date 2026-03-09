# TACHE_01 - Module Auth Firebase (DEV_AUTH)

Date: 2026-03-04
Statut: ✅ Code feature prêt pour intégration cœur

## 1) Livrables ajoutés (sans modification fichiers cœur)

Nouveaux fichiers créés sous `app/src/main/java/com/example/lexicaandroid2/features/auth/`:

- `domain/model/AuthUser.kt`
- `domain/repository/AuthRepository.kt`
- `data/FirebaseAuthRepository.kt`
- `presentation/login/LoginViewModel.kt`
- `presentation/login/LoginScreen.kt`
- `presentation/register/RegisterViewModel.kt`
- `presentation/register/RegisterScreen.kt`

## 2) Ce que fait le module

- Authentification Email/Password via Firebase Auth
- Point d’entrée Google Sign-In via `idToken` (méthode repository incluse)
- État de session en `Flow` (`currentUser`) pour connecté/déconnecté
- Validation des inputs (email, mot de passe, confirmation)
- Écrans Compose Material 3: connexion + inscription

## 3) Changements cœur requis (à faire par mainteneur)

### A. Dépendances Gradle

Dans `gradle/libs.versions.toml`, ajouter les entrées Firebase (exemple):

- `firebase-bom = "<version stable actuelle>"`
- `firebase-auth-ktx`
- `kotlinx-coroutines-play-services`

Dans `app/build.gradle.kts`, ajouter:

- `implementation(platform(libs.firebase.bom))`
- `implementation(libs.firebase.auth.ktx)`
- `implementation(libs.kotlinx.coroutines.play.services)`

Si Google Sign-In natif est activé côté UI:

- ajouter la dépendance Google Identity (Credential Manager/GoogleId)

### B. Plugins Firebase

- Activer plugin Google Services si non activé:
  - projet root: plugin `com.google.gms.google-services` (apply false)
  - module app: apply plugin Google Services

### C. Configuration Firebase

- Placer `google-services.json` dans `app/`
- Vérifier package Android dans la console Firebase

### D. Navigation globale (`LexicaApp.kt`)

Ajouter routes minimales:

- `login`
- `register`

Injecter `AuthRepository` (construction manuelle ou DI existant), puis:

- `LoginScreen(..., onNavigateToRegister = { navController.navigate("register") }, onLoginSuccess = { navController.navigate("dashboard") { popUpTo("login") { inclusive = true } } })`
- `RegisterScreen(..., onNavigateToLogin = { navController.popBackStack() }, onRegisterSuccess = { navController.navigate("dashboard") { popUpTo("login") { inclusive = true } } })`

## 4) Notes techniques

- `FirebaseAuthRepository` utilise:
  - `FirebaseAuth.AuthStateListener` + `callbackFlow`
  - `kotlinx.coroutines.tasks.await()`
- Tant que les dépendances Firebase ne sont pas intégrées, ce module ne compile pas en isolation.

## 5) Incohérence documentaire détectée

- `docs/guides/CONSIGNES_TACHES.md` indique "Aucune tâche active" dans la section catalogue.
- Plusieurs documents de statut indiquent pourtant l’existence de `TACHE_01` (Auth Firebase).

Action recommandée mainteneur:
- Ajouter officiellement `TACHE_01 - Module Auth Firebase` dans le catalogue de `CONSIGNES_TACHES.md`.

## 6) Périmètre respecté

- Aucun fichier cœur modifié (`LexicaApp.kt`, `AndroidManifest.xml`, `build.gradle.kts`, `AppDatabase.kt`)
- Aucun fichier `.txt` modifié
- Aucun build lancé
