# TACHE_18 - Diagnostic Firebase Auth & Activation Navigation
*Livré le : 2026-03-12*
*Agent : Agent Développeur*

---

## Phase 1 — Résultats du Diagnostic

### 1. `google-services.json`
- ✅ Présent dans `app/`
- ✅ Projet Firebase : `lexica-6d59a` (project_number: 1000182679544)
- ✅ Package Android déclaré : `com.example.lexicaandroid2` — **correspond** au `applicationId` du projet

### 2. `FirebaseAuthRepository.kt`
- ✅ Utilise `FirebaseAuth.getInstance()` — pas de crash attendu
- ✅ `currentUser` implémenté via `callbackFlow` avec `AuthStateListener` — pattern correct
- ✅ `signInWithEmail`, `registerWithEmail`, `signInWithGoogleIdToken`, `signOut` — tous implémentés

### 3. Dépendances Firebase
- ✅ `firebase-auth-ktx` présent via BOM `32.8.1` dans `build.gradle.kts`
- ✅ Plugin `google-services` déclaré

### 4. Points à vérifier manuellement dans la Firebase Console
> Ces étapes ne peuvent pas être vérifiées depuis le code — à faire par le Chef d'Orchestre :

- [ ] **Authentication → Sign-in method → Email/Password** doit être **Activé** dans la console Firebase du projet `lexica-6d59a`
- [ ] **SHA-1 du keystore debug** enregistré dans Firebase (pour éviter les erreurs auth sur device réel)
  - Obtenir via : `./gradlew signingReport` ou `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
- [ ] Google Sign-In : non requis pour l'instant (le token Google dans `LoginScreen` est en `TODO`)

---

## Phase 2 — Activation Navigation (COMPLÉTÉE ✅)

### Modifications apportées dans `LexicaApp.kt`

**1. Imports ajoutés :**
```kotlin
import com.example.lexicaandroid2.features.auth.presentation.login.LoginScreen
import com.example.lexicaandroid2.features.auth.presentation.login.LoginViewModel
import com.example.lexicaandroid2.features.auth.presentation.register.RegisterScreen
import com.example.lexicaandroid2.features.auth.presentation.register.RegisterViewModel
```

**2. Routes ajoutées dans `sealed class Screen` :**
```kotlin
data object Login : Screen("login")
data object Register : Screen("register")
```

**3. Paramètres ajoutés à `LexicaApp()` :**
```kotlin
loginViewModel: LoginViewModel,
registerViewModel: RegisterViewModel,
```

**4. Titres ajoutés dans `topBarTitle` :**
```kotlin
Screen.Login.route -> "Connexion"
Screen.Register.route -> "Inscription"
```

**5. Routes ajoutées dans `canNavigateBack` :**
```kotlin
currentRoute == Screen.Login.route || currentRoute == Screen.Register.route
```

**6. `onSignInRequested` branché dans `ProfileScreen` :**
```kotlin
onSignInRequested = { navController.navigate(Screen.Login.route) }
```

**7. Composables ajoutés dans le NavHost :**
```kotlin
composable(route = Screen.Login.route) {
    LoginScreen(
        viewModel = loginViewModel,
        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
        onLoginSuccess = {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    )
}
composable(route = Screen.Register.route) {
    RegisterScreen(
        viewModel = registerViewModel,
        onNavigateToLogin = { navController.popBackStack() },
        onRegisterSuccess = {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    )
}
```

### Modifications apportées dans `MainActivity.kt`

**Instanciation des ViewModels auth :**
```kotlin
val loginViewModel = ViewModelProvider(
    this, LoginViewModelFactory(authRepository)
)[LoginViewModel::class.java]
val registerViewModel = ViewModelProvider(
    this, RegisterViewModelFactory(authRepository)
)[RegisterViewModel::class.java]
```

**Passage à `LexicaApp()` :**
```kotlin
loginViewModel = loginViewModel,
registerViewModel = registerViewModel,
```

**Bonus — `AddWordsViewModelFactory` corrigée (TACHE_15) :**
```kotlin
val addWordsFactory = AddWordsViewModelFactory(
    wordReserveRepository = reserveRepository,
    flashcardRepository = repository
)
```

---

## Flux utilisateur activé

```
ProfileScreen (non connecté)
  → bouton "Se connecter"
    → LoginScreen (route "login")
      → "Créer un compte" → RegisterScreen (route "register")
      → connexion réussie → Dashboard (login retiré de la back stack)
```

---

## Checklist

- [x] `Screen.Login` et `Screen.Register` dans `sealed class Screen`
- [x] `composable("login")` et `composable("register")` dans NavHost
- [x] `onSignInRequested` branché dans ProfileScreen (navigue vers Login)
- [x] `loginViewModel` et `registerViewModel` instanciés dans `MainActivity`
- [x] Back stack propre après login/register (popUpTo inclusive)
- [x] Aucune erreur de compilation
- [ ] Vérifier Email/Password activé dans Firebase Console (manuel)
- [ ] Vérifier SHA-1 enregistré dans Firebase Console (si test sur device réel)

