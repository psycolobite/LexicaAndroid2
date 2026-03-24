# TACHE_23 — Synchronisation Firebase Firestore — PR d'intégration

**Statut :** ✅ Code livré — En attente d'intégration par le Chef d'Orchestre  
**Date de livraison :** 2026-03-12  
**Dépendances :** TACHE_18 (auth) ✅  
**Package isolé :** `features/sync/`

---

## 📦 Fichiers créés

| Fichier | Description |
|---------|-------------|
| `features/sync/FirestoreSyncRepository.kt` | CRUD Firestore : upload/download de `CloudProgress` |
| `features/sync/SyncManager.kt` | Orchestrateur : logique login/logout + conflict + sync périodique |
| `features/sync/SyncViewModel.kt` | ViewModel + `SyncViewModelFactory` — expose `SyncUiState` |
| `features/sync/SyncConfirmDialog.kt` | Dialog Compose pour la résolution de conflit |

---

## 🔧 Intégrations requises dans les fichiers coeur

### 1. `app/build.gradle.kts` — Ajouter la dépendance Firestore

```kotlin
// Dans le bloc dependencies {}, après firebase-auth-ktx :
implementation("com.google.firebase:firebase-firestore-ktx")
```

> ⚠️ Le BOM Firebase `32.8.1` est déjà présent — pas besoin de version explicite.

---

### 2. `AndroidManifest.xml` — Permission Internet (si absente)

Vérifier que la permission suivante est présente (normalement déjà là pour Firebase Auth) :
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

### 3. `MainActivity.kt` — Créer le `SyncViewModel`

**a) Ajouter les imports :**
```kotlin
import com.example.lexicaandroid2.features.sync.SyncViewModel
import com.example.lexicaandroid2.features.sync.SyncViewModelFactory
```

**b) Créer le ViewModel après les autres ViewModels (dans `onCreate`) :**
```kotlin
val syncViewModel = ViewModelProvider(
    this,
    SyncViewModelFactory(
        authRepository = authRepository,
        userStatsRepository = userStatsRepository,
        userStatsDao = userStatsDao,
        flashcardRepository = repository
    )
)[SyncViewModel::class.java]
```

**c) Passer `syncViewModel` à `LexicaApp` :**
```kotlin
LexicaApp(
    // ... paramètres existants ...
    syncViewModel = syncViewModel
)
```

---

### 4. `presentation/LexicaApp.kt` — Brancher le dialog et le ViewModel

**a) Ajouter `syncViewModel: SyncViewModel? = null` en paramètre de `LexicaApp` :**
```kotlin
@Composable
fun LexicaApp(
    // ... paramètres existants ...
    syncViewModel: SyncViewModel? = null
)
```

**b) Ajouter les imports :**
```kotlin
import com.example.lexicaandroid2.features.sync.SyncViewModel
import com.example.lexicaandroid2.features.sync.SyncUiState
import com.example.lexicaandroid2.features.sync.SyncConfirmDialog
```

**c) Collecter le state et afficher le dialog — dans le corps de `LexicaApp`, avant le `Scaffold` :**
```kotlin
// SyncConfirmDialog — s'affiche par-dessus tout l'écran si conflit détecté
val syncUiState by syncViewModel?.uiState?.collectAsState() 
    ?: MutableStateFlow(SyncUiState.Idle).collectAsState()

if (syncUiState is SyncUiState.PendingConflict) {
    val conflict = syncUiState as SyncUiState.PendingConflict
    SyncConfirmDialog(
        conflictState = conflict,
        onKeepLocal = { syncViewModel?.keepLocal(conflict.uid) },
        onReplaceLocal = {
            syncViewModel?.confirmReplaceWithCloud(conflict.uid, conflict.cloud)
        }
    )
}
```

> 💡 Ce dialog s'affiche par-dessus n'importe quel écran (dans le composable racine, avant le Scaffold) — c'est le comportement voulu pour garantir que l'utilisateur choisit avant de naviguer.

---

### 5. `presentation/profile/ProfileScreen.kt` — Déconnexion avec sync

Actuellement `ProfileScreen` appelle `authRepository.signOut()` directement. Il faut utiliser `syncViewModel.signOutWithSync()` pour sauvegarder avant de déconnecter.

**a) Ajouter `syncViewModel: SyncViewModel? = null` en paramètre de `ProfileScreen` :**
```kotlin
@Composable
fun ProfileScreen(
    // ... paramètres existants ...
    syncViewModel: SyncViewModel? = null
)
```

**b) Passer `syncViewModel` depuis `LexicaApp.kt` :**
```kotlin
composable(route = Screen.Profile.route) {
    ProfileScreen(
        // ... paramètres existants ...
        syncViewModel = syncViewModel
    )
}
```

**c) Remplacer l'appel `authRepository.signOut()` :**

Chercher dans `ProfileScreen.kt` l'endroit où `signOut` est appelé et remplacer :
```kotlin
// AVANT
onSignOut = { viewModel.signOut() }  // ou similar

// APRÈS — récupérer l'uid courant avant de déconnecter
val currentUid = /* uiState.currentUser?.uid */ ""
if (syncViewModel != null && currentUid.isNotBlank()) {
    syncViewModel.signOutWithSync(uid = currentUid, onComplete = { /* naviguer si besoin */ })
} else {
    viewModel.signOut()
}
```

> ⚠️ Lire `ProfileScreen.kt` entièrement pour trouver l'exact pattern de signOut avant de modifier.

---

## ✅ Checklist d'intégration

- [ ] `firebase-firestore-ktx` ajouté dans `app/build.gradle.kts`
- [ ] `SyncViewModelFactory` instancié dans `MainActivity.kt`
- [ ] `syncViewModel` passé à `LexicaApp`
- [ ] `syncViewModel: SyncViewModel? = null` ajouté en paramètre de `LexicaApp`
- [ ] `SyncConfirmDialog` rendu dans `LexicaApp` quand `syncUiState is PendingConflict`
- [ ] `syncViewModel` passé à `ProfileScreen`
- [ ] `ProfileScreen` utilise `syncViewModel.signOutWithSync()` pour la déconnexion
- [ ] Build : `./gradlew clean :app:assembleDebug`
- [ ] Test manuel : créer un compte, ajouter des XP, se déconnecter sur appareil 1, se reconnecter sur appareil 2 → vérifier dialog de conflit

---

## 🗺️ Architecture

```
features/sync/
├── FirestoreSyncRepository.kt   — Firestore CRUD (collection "users/{uid}")
│                                   upload/download CloudProgress
├── SyncManager.kt               — Orchestrateur pur (non-ViewModel)
│                                   checkOnLogin(), uploadLocalToCloud(),
│                                   replaceLocalWithCloud(), periodicPush()
├── SyncViewModel.kt             — ViewModel (observe authRepository.currentUser)
│                                   Expose SyncUiState (Idle/Loading/PendingConflict/Message)
│                                   Auto-trigger: checkAndSync() dès uid non-null
│                                   Sync périodique: toutes les 30 min
└── SyncConfirmDialog.kt         — Composable dialog affiché dans LexicaApp
```

### Modèle de données Firestore

```
Collection: users
Document:   {uid}
Fields:
  xp:              Long       (XP total)
  level:           Int        (niveau calculé)
  streak:          Int        (jours consécutifs)
  lastLoginDate:   Long       (timestamp ms)
  favoriteCardIds: List<String>
```

### Flux complet

```
Login réussi
    ↓
SyncViewModel observe authRepository.currentUser → uid non-null détecté
    ↓
SyncManager.checkOnLogin(uid)
    ├── NoCloudData       → uploadLocalToCloud() silencieux
    ├── EmptyLocalImport  → silentImportFromCloud() silencieux
    ├── UpToDate          → rien
    ├── Conflict          → SyncUiState.PendingConflict → SyncConfirmDialog affiché
    │       ├── "Conserver local" → keepLocal() → uploadLocalToCloud()
    │       └── "Remplacer"       → confirmReplaceWithCloud() → replaceLocalWithCloud()
    └── NetworkError      → Idle (log silencieux)

Déconnexion via ProfileScreen
    ↓
SyncViewModel.signOutWithSync(uid)
    ↓
SyncManager.saveBeforeLogout(uid) → uploadLocalToCloud()
    ↓
authRepository.signOut()
```

---

## 📝 Notes techniques

### Sécurité Firestore
Pour protéger les données, ajouter les règles suivantes dans la Firebase Console → Firestore → Rules :
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```
> Sans ces règles, les documents sont publics — **à configurer avant mise en production**.

### Comportement hors-ligne
Firestore SDK met en cache les écritures et les re-tente quand le réseau revient. `uploadProgress()` retourne donc `Result.success()` même hors-ligne.

### Sync périodique (WorkManager — Phase 2)
La sync toutes les 30 minutes est implémentée via un `delay()` dans une coroutine ViewModel. En production, il faudrait utiliser `WorkManager` pour les syncs en arrière-plan quand l'app est fermée. Documenter dans le backlog.

### Limitation connue : `UserStatsRepository`
`SyncManager.replaceLocalWithCloud()` utilise `UserStatsDao` directement pour écrire l'entité complète (XP + streak + lastLoginDate en une seule passe). Si une future refactorisation de `UserStatsRepositoryImpl` casse cette dépendance directe, ajouter `suspend fun setStats(entity: UserStatsEntity)` à l'interface `UserStatsRepository`.

---

## 💡 Sources d'inspiration

- **Now in Android** (Google) : `github.com/android/nowinandroid` → architecture offline-first + Firestore sync pattern
- **Firebase Firestore Android Codelab** : documentation officielle Google pour les règles Firestore + opérations CRUD
- **KotlinConf 2023** — "Offline-First avec Firestore" talk — pattern `runCatching` + cache automatique SDK
