# 🔧 Troubleshooting Guide - Erreurs Courantes

## 🔴 Erreurs de Compilation

### 1. **ERROR: Failed to resolve: androidx.xxx**

```
Compilation error:
e: Could not find androidx.lifecycle:lifecycle-viewmodel:2.6.1
```

**Cause:** Dépendance manquante ou version incompatible

**Solution:**
```bash
# 1. Nettoyer et rafraîchir
./gradlew clean --refresh-dependencies

# 2. Invalider caches Android Studio
File → Invalidate Caches / Restart

# 3. Vérifier versions dans build.gradle.kts
# Doit avoir: 
# - androidx.lifecycle:lifecycle-viewmodel:2.6.1+
# - androidx.compose.material3:material3:1.1.0+

# 4. Recompiler
./gradlew :app:assembleDebug
```

---

### 2. **ERROR: unresolved reference: SearchRepository**

```
e: file:///.../SearchViewModel.kt:5:1
e: unresolved reference: SearchRepository
```

**Cause:** Import manquant ou classe non créée

**Solution:**
```kotlin
// Vérifier que le fichier existe:
// app/src/main/java/com/example/lexicaandroid2/domain/repository/SearchRepository.kt

// Ajouter l'import:
import com.example.lexicaandroid2.domain.repository.SearchRepository

// Si fichier n'existe pas, le créer:
// File → New → Kotlin Class → Package: domain.repository
```

---

### 3. **ERROR: Type mismatch: inferred type is String but FlowString was expected**

```
e: file:///.../SearchViewModel.kt:42:5
Type mismatch: inferred type is 'String'
but 'Flow<String>' was expected
```

**Cause:** StateFlow vs Flow confusion

**Solution:**
```kotlin
// MAUVAIS:
val searchQuery: Flow<String> = MutableStateFlow("")

// BON:
val searchQuery: MutableStateFlow<String> = MutableStateFlow("")
// Ou:
val searchQuery: StateFlow<String> = MutableStateFlow("").asStateFlow()
```

---

### 4. **ERROR: 'update' is not defined**

```
e: file:///.../SearchViewModel.kt:15:5
Unresolved reference 'update'
```

**Cause:** Extension manquante pour StateFlow

**Solution:**
```kotlin
// Ajouter import:
import kotlinx.coroutines.flow.update

// Puis utiliser:
_uiState.update { currentState ->
    currentState.copy(query = "nouveau")
}
```

---

### 5. **ERROR: Conflicting tasks with the same type**

```
e: [gradle-1] FAILURE: Build failed with an exception.
Task 'assembleDebug' has multiple workers
```

**Cause:** Plusieurs instances gradle tournent, ou cache en conflit

**Solution:**
```bash
# 1. Tuer tous les processus gradle
pkill -f gradlew
pkill -f gradle

# 2. Supprimer .gradle
Remove-Item -Recurse -Force .gradle

# 3. Clean complet
./gradlew clean --refresh-dependencies

# 4. Recompiler
./gradlew :app:assembleDebug
```

---

### 6. **ERROR: Gradle sync failed**

```
Gradle files have errors
The IDE cannot continue
```

**Cause:** Fichier build.gradle.kts mal formaté

**Solution:**
```bash
# 1. Vérifier syntaxe Kotlin
# app/build.gradle.kts ligne par ligne

# 2. Vérifier accolades {} matching
# Utiliser IDE: Code → Reformat Code

# 3. Checker dépendances:
# Versions correctes? Repos configurés?

# 4. Invalider cache
File → Invalidate Caches / Restart
```

---

## 🟡 Erreurs de Runtime

### 1. **java.lang.NullPointerException in DAO**

```
java.lang.NullPointerException: Attempt to invoke virtual method on null object reference
    at com.example...FlashcardDao.getAll()
```

**Cause:** Database non initialisée

**Solution:**
```kotlin
// Vérifier que la database est crée dans MainActivity:
val database = LexicaDatabase.getDatabase(this)
val dao = database.flashcardDao()

// Vérifier Room migration:
// Si version changé (3→4), ajouter migration:
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE UserStats (id INTEGER PRIMARY KEY, xp INTEGER, level INTEGER)"
        )
    }
}
```

---

### 2. **IllegalArgumentException: No route matches**

```
java.lang.IllegalArgumentException: 
No route matched for com.example.lexicaandroid2.presentation.Screen$Search@...
```

**Cause:** Route non ajoutée à LexicaApp.kt

**Solution:**
```kotlin
// 1. Vérifier écran dans sealed class:
sealed class Screen(val route: String) {
    data object Search : Screen("search")  // ✅ Doit exister
}

// 2. Vérifier dans NavHost:
composable(Screen.Search.route) {
    SearchScreen(...)  // ✅ Doit exister
}

// 3. Vérifier navigation call:
navController.navigate(Screen.Search.route)  // ✅ Correct format
```

---

### 3. **StateFlow not collecting updates**

**Cause:** Scope coroutine fermé ou StateFlow mal initialisé

**Solution:**
```kotlin
// MAUVAIS:
val _uiState = MutableStateFlow(SearchUiState())
val uiState: StateFlow<SearchUiState> = _uiState
// Ne pas exposer _uiState directement

// BON:
private val _uiState = MutableStateFlow(SearchUiState())
val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

// Ou:
val uiState = MutableStateFlow(SearchUiState())
// Mais alors utiliser directement

// Vérifier le scope coroutine:
viewModelScope.launch {
    _uiState.update { it.copy(...) }  // ✅ Bon scope
}

// Pas en UI:
// ❌ MAUVAIS:
_uiState.update { it.copy(...) }  // Sans scope
// ✅ BON:
val state by uiState.collectAsState()
```

---

### 4. **Cannot create extension on null**

```
com.example.SearchViewModel: 
Cannot create extension on null
```

**Cause:** ViewModel factory défaillante

**Solution:**
```kotlin
// Créer factory:
class SearchViewModelFactory(
    private val repository: SearchRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(repository) as T
    }
}

// Utiliser dans Composable:
val viewModel = viewModel(
    factory = SearchViewModelFactory(searchRepository)
)
```

---

### 5. **Database file is locked**

```
database file is locked (code 5 SQLITE_BUSY)
```

**Cause:** Plusieurs accès simultanés à la BD

**Solution:**
```bash
# 1. Fermer l'app
adb shell am force-stop com.example.lexicaandroid2

# 2. Redémarrer l'émulateur
adb emu kill
adb start-server

# 3. Nettoyer la BD
rm ~/.android/avd/*/storage.img

# 4. Relancer
adb start-server
./gradlew :app:assembleDebug
```

---

## ⚠️ Erreurs de Git

### 1. **Merge conflict**

```
<<<<<<< HEAD
    val name: String = "Agent1"
=======
    val name: String = "Agent2"
>>>>>>> feature/search
```

**Solution:**
```bash
# 1. Ouvrir le fichier
# 2. Choisir la version correcte
# 3. Supprimer les marqueurs <<<<<<, ======, >>>>>>

# 4. Marquer comme résolu
git add fichier_resolu.kt

# 5. Finir le merge
git merge --continue
```

---

### 2. **Your branch is behind origin/main**

```
Your branch is behind 'origin/main' by 5 commits
```

**Solution:**
```bash
# Option 1: Rebase (linéaire)
git fetch origin
git rebase origin/main

# Option 2: Merge (crée commit merge)
git fetch origin
git merge origin/main
```

---

## 🧪 Erreurs de Test

### 1. **Emulator not detecting**

```
adb: device unauthorized
```

**Solution:**
```bash
# 1. Redémarrer adb
adb kill-server
adb start-server

# 2. Vérifier appareils
adb devices

# 3. Si "offline":
adb reboot

# 4. Si toujours pas:
# Vérifier émulateur lancé dans Android Studio
# ou command-line:
emulator -avd Pixel_5_API_34 -no-snapshot-load
```

---

### 2. **APK installation fails**

```
adb: error: cannot stat '...apk': No such file or directory
```

**Solution:**
```bash
# 1. Vérifier fichier existe:
dir app/build/outputs/apk/debug/app-debug.apk

# 2. Compiler d'abord:
./gradlew :app:assembleDebug

# 3. Puis installer:
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 Erreurs d'Exécution sur Device

### 1. **App crashes at startup**

```
Process com.example.lexicaandroid2 died
```

**Vérifier logcat:**
```bash
adb logcat | grep "com.example.lexicaandroid2"
# Chercher "Exception" ou "Error"
```

**Solutions courantes:**
- [ ] Database pas initialisée
- [ ] ViewModel factory invalide
- [ ] Import ou package manquant
- [ ] Permission non déclarée dans AndroidManifest

---

### 2. **App crashes au clic sur "Search"**

**Solution:**
```bash
# 1. Vérifier logcat complet:
adb logcat -c
adb logcat | grep CRASH

# 2. Chercher:
# - NullPointerException
# - ClassNotFoundException
# - IllegalStateException

# 3. Vérifier SearchScreen existe:
# File exists: presentation/search/SearchScreen.kt

# 4. Vérifier composable ajouté à LexicaApp.kt:
composable(Screen.Search.route) { 
    SearchScreen(...)  // ✅
}
```

---

## 🎯 Checklist de Debug

Quand une erreur survient:

- [ ] Lire le message d'erreur COMPLET
- [ ] Noter le fichier et ligne du problème
- [ ] Reproduire le problème
- [ ] Isoler: Est-ce compilatioN ou runtime?
- [ ] Chercher solution dans ce guide
- [ ] Google: "[Error message] kotlin android"
- [ ] Demander aide avec output complet

---

## 📋 Logs Utiles

```bash
# Compilation + Logcat
./gradlew :app:assembleDebug 2>&1 | Tee build.log
adb logcat > logcat_$(date +%s).log

# Vérifier gradle daemon
./gradlew --status

# Kill daemon
./gradlew --stop

# Debug mode
./gradlew --debug :app:assembleDebug 2>&1 | Tee debug.log

# Vérifier dépendances
./gradlew :app:dependencies
```

---

## ☎️ Si rien ne marche

1. **Nettoyer complet:**
   ```bash
   ./gradlew clean
   rm -rf .gradle
   rm -rf app/build
   rm -rf build
   ```

2. **Invalidier cache IDE:**
   ```
   Android Studio → File → Invalidate Caches / Restart
   ```

3. **Redémarrer tout:**
   ```bash
   pkill -f gradlew
   pkill -f studio64
   # Attendre 30s
   code .  # ou: studio .
   ```

4. **Demander aide:**
   ```
   Joindre:
   - build.log complet
   - logcat complet
   - git status
   - java -version
   - ./gradlew --version
   ```

---

**Guide créé:** 2026-02-27
**Mise à jour:** À chaque nouvelle erreur rencontrée
**Contributeurs:** Agents + Chef d'orchestre


