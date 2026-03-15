# TACHE_19 — Page Réglages + bouton TopBar — PR d'intégration

**Statut :** ✅ Code livré — En attente d'intégration par le Chef d'Orchestre  
**Date de livraison :** 2026-03-12  
**Package isolé :** `presentation/settings/`

---

## 📦 Fichiers créés

| Fichier | Description |
|---------|-------------|
| `presentation/settings/UserPrefsRepository.kt` | Préférences utilisateur via SharedPreferences |
| `presentation/settings/SettingsViewModel.kt` | ViewModel + `SettingsUiState` + `SettingsViewModelFactory` |
| `presentation/settings/SettingsScreen.kt` | Écran Compose complet (4 sections) |

---

## 🔧 Intégrations requises dans les fichiers coeur

### 1. `presentation/LexicaApp.kt`

**a) Ajouter `Screen.Settings` dans `sealed class Screen` :**
```kotlin
data object Settings : Screen("settings")
```

**b) Ajouter `settingsViewModel: SettingsViewModel` en paramètre de `LexicaApp` :**
```kotlin
@Composable
fun LexicaApp(
    // ... paramètres existants ...
    settingsViewModel: SettingsViewModel,
    // ...
)
```

**c) Ajouter le title dans `topBarTitle` :**
```kotlin
Screen.Settings.route -> "Réglages"
```

**d) Ajouter `Screen.Settings.route` dans `canNavigateBack` :**
```kotlin
val canNavigateBack = /* ... liste existante ... */ ||
    currentRoute == Screen.Settings.route
```

**e) Ajouter `onSettingsClick` dans l'appel de `LexicaTopAppBar` :**
```kotlin
LexicaTopAppBar(
    title = topBarTitle,
    canNavigateBack = canNavigateBack,
    navigateUp = { navController.navigateUp() },
    onProfileClick = if (currentRoute == Screen.Dashboard.route) {
        { navController.navigate(Screen.Profile.route) }
    } else null,
    onSettingsClick = if (currentRoute == Screen.Dashboard.route) {   // ← AJOUTER
        { navController.navigate(Screen.Settings.route) }
    } else null
)
```

**f) Ajouter le composable dans le `NavHost` :**
```kotlin
composable(route = Screen.Settings.route) {
    SettingsScreen(
        viewModel = settingsViewModel,
        appVersion = appVersion   // voir §3 ci-dessous
    )
}
```

> ⚠️ Importer : `com.example.lexicaandroid2.presentation.settings.SettingsScreen`  
> ⚠️ Importer : `com.example.lexicaandroid2.presentation.settings.SettingsViewModel`

---

### 2. `presentation/common/LexicaTopAppBar.kt`

Ajouter le paramètre `onSettingsClick` et l'icône associée dans les `actions` :

```kotlin
import androidx.compose.material.icons.filled.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LexicaTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null   // ← AJOUTER
) {
    CenterAlignedTopAppBar(
        // ... titre et navigationIcon inchangés ...
        actions = {
            if (onSettingsClick != null) {            // ← AJOUTER
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Réglages"
                    )
                }
            }
            if (onProfileClick != null) {
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Mon profil"
                    )
                }
            }
        }
    )
}
```

---

### 3. `MainActivity.kt`

**a) Lire la version de l'app depuis le PackageManager (pas besoin d'activer BuildConfig) :**
```kotlin
val appVersion = try {
    packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
} catch (e: Exception) { "1.0" }
```

**b) Créer `UserPrefsRepository` et `SettingsViewModel` :**
```kotlin
import com.example.lexicaandroid2.presentation.settings.UserPrefsRepository
import com.example.lexicaandroid2.presentation.settings.SettingsViewModelFactory
import com.example.lexicaandroid2.presentation.settings.SettingsViewModel

val userPrefsRepository = UserPrefsRepository(applicationContext)
val settingsViewModel = ViewModelProvider(
    this, SettingsViewModelFactory(userPrefsRepository)
)[SettingsViewModel::class.java]
```

**c) Passer `settingsViewModel` et `appVersion` à `LexicaApp` :**
```kotlin
LexicaApp(
    // ... paramètres existants ...
    settingsViewModel = settingsViewModel,
    appVersion = appVersion    // ← passer appVersion à LexicaApp puis à SettingsScreen
)
```

> 💡 Alternative pour `appVersion` : ajouter `appVersion: String` en paramètre de `LexicaApp`
> et le passer directement à `SettingsScreen` dans le composable.

---

### 4. Application du thème dynamique dans `MainActivity.kt`

Pour que le changement de thème s'applique sans redémarrer l'app :

```kotlin
// Dans setContent { ... }
setContent {
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val darkTheme = when (settingsUiState.theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()  // import androidx.compose.foundation.isSystemInDarkTheme
    }
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    ) {
        val navController = rememberNavController()
        LexicaApp(
            navController = navController,
            // ... autres paramètres ...
            settingsViewModel = settingsViewModel,
            appVersion = appVersion
        )
    }
}
```

> ⚠️ Imports nécessaires :
> ```kotlin
> import androidx.compose.foundation.isSystemInDarkTheme
> import androidx.compose.material3.darkColorScheme
> import androidx.compose.material3.lightColorScheme
> import com.example.lexicaandroid2.presentation.settings.AppTheme
> ```

---

## ✅ Checklist d'intégration

- [ ] `Screen.Settings` ajouté dans `sealed class Screen`
- [ ] `settingsViewModel: SettingsViewModel` ajouté en paramètre de `LexicaApp`
- [ ] `topBarTitle` mis à jour avec `"Réglages"` pour `Screen.Settings.route`
- [ ] `canNavigateBack` mis à jour pour inclure `Screen.Settings.route`
- [ ] `onSettingsClick` ajouté dans l'appel `LexicaTopAppBar` (visible sur Dashboard uniquement)
- [ ] `composable("settings")` ajouté dans le NavHost
- [ ] `LexicaTopAppBar.kt` : paramètre `onSettingsClick` + icône `Icons.Default.Settings`
- [ ] `MainActivity.kt` : `UserPrefsRepository` + `SettingsViewModel` créés
- [ ] `MainActivity.kt` : `appVersion` lu depuis `PackageManager`
- [ ] `MainActivity.kt` : thème dynamique appliqué via `settingsUiState.theme`
- [ ] Build : `./gradlew clean :app:assembleDebug`

---

## 📐 Fonctionnalités implémentées

### Section Apparence
- **Thème** : `FilterChip` 3 options (Clair / Sombre / Système)
- **Taille de police** : `Slider` 12sp–20sp avec aperçu live
- **Couleur d'accent** : 5 swatches circulaires cliquables (Bleu, Vert, Violet, Orange, Rose)

### Section Entraînement
- **Cartes par session** : `Slider` 5–50 (affichage valeur en temps réel)
- **Afficher définition en premier** : `Switch` toggle
- **Activer les défis** : `Switch` toggle (désactive ortho + sémantique d'un coup)

### Section Notifications
- **Rappel quotidien** : `Switch` toggle
- **Heure du rappel** : `OutlinedButton` avec icône horloge → ouvre dialog `AlertDialog` custom (visible uniquement si toggle ON)

### Section À propos
- **Version** : passée depuis `MainActivity` via `PackageManager` (pas besoin d'activer `buildConfig`)
- **Politique de confidentialité** : `Text` cliquable (placeholder — brancher URL quand disponible)

---

## 🗺️ Architecture

```
presentation/settings/
├── UserPrefsRepository.kt   — SharedPreferences "user_prefs"
│                              Enums : AppTheme, AccentColor
├── SettingsViewModel.kt     — SettingsUiState + SettingsViewModelFactory
└── SettingsScreen.kt        — UI Compose (SettingsScreen + composables privés)
```

### Enums exportés
- `AppTheme` (LIGHT, DARK, SYSTEM) — utilisé par `MainActivity` pour le thème
- `AccentColor` (BLUE, GREEN, VIOLET, ORANGE, PINK) — couleurs prédéfinies

---

## 📝 Notes

- **SharedPreferences name :** `"user_prefs"` — distinct de `"admin_prefs"` (AdminPrefsRepository).
- **Sync avec AdminPrefsRepository :** le champ `cardsPerSession` peut rester indépendant. Si un admin veut les syncer, il suffit d'utiliser la même clé dans le même fichier SharedPreferences, ou de déléguer depuis `UserPrefsRepository` vers `AdminPrefsRepository.sessionSize`.
- **Dialog heure :** implémenté sans lib externe (`AlertDialog` + deux `OutlinedTextField`). Pour une meilleure UX, le Chef d'Orchestre peut remplacer par le `TimePicker` Material3 (`androidx.compose.material3.TimePicker`) quand disponible (nécessite Material3 1.2+).
- **Couleur d'accent :** l'application réelle de la couleur d'accent au thème Material3 nécessite de modifier le `ColorScheme` dans `MainActivity` (ex : remplacer `primary` par `Color(accentColor.colorHex)`). Ceci peut être fait en Phase 2 — la sélection est déjà persistée.

---

## 💡 Sources d'inspiration

- **Now in Android** (Google) : `github.com/android/nowinandroid` → `SettingsScreen` Material3 complet avec thème dynamique, pattern de référence.
- **Jetpack Compose Samples** : `github.com/android/compose-samples` → Jetchat Settings pour toggling thème sans redémarrage.
