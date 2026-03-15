# TACHE_17 - Mode Administrateur
*Livré le : 2026-03-12*
*Agent : Agent Développeur*

---

## Résumé

Mode admin complet permettant au propriétaire du projet (identifié par son e-mail Firebase) de contrôler les paramètres de l'appli en production sans passer par un build debug.

---

## Fichiers créés

| Fichier | Description |
|---------|-------------|
| `presentation/admin/AdminConfig.kt` | Constante email(s) admin — **à renseigner avec votre email** |
| `presentation/admin/AdminPrefsRepository.kt` | SharedPreferences léger (pas Room) + enums `ReviewMode`, `MemoryGridSize` |
| `presentation/admin/AdminViewModel.kt` | ViewModel + Factory avec toutes les actions admin |
| `presentation/admin/AdminScreen.kt` | Écran Compose complet avec 3 sections |

---

## Fichiers modifiés

| Fichier | Modification |
|---------|-------------|
| `features/gamification/domain/UserStatsRepository.kt` | +`resetStats()` +`simulateStreak(days)` |
| `features/gamification/data/UserStatsRepositoryImpl.kt` | Implémentations de `resetStats()` et `simulateStreak()` |
| `features/gamification/data/DailyReviewStatDao.kt` | +`clearAll()` avec `@Query("DELETE FROM daily_review_stats")` |
| `presentation/profile/ProfileViewModel.kt` | +`isAdmin` dans `ProfileUiState`, alimenté par `AdminConfig.isAdmin(email)` |
| `presentation/profile/ProfileScreen.kt` | +`onNavigateToAdmin: () -> Unit`, bouton `⚙️ Mode Admin` conditionnel (`if (uiState.isAdmin)`) |
| `presentation/review/ReviewViewModel.kt` | +`adminPrefsRepository: AdminPrefsRepository?`, `loadSession` lit `sessionSize`, `gradeCard` conditionne les défis, `advanceToNextCard` applique `ReviewMode` (VOCAB→true / DEFINITION→false / BOTH→toggle) |
| `presentation/review/ReviewViewModelFactory` | +`adminPrefsRepository` paramètre |
| `presentation/LexicaApp.kt` | +`Screen.Admin`, +`adminViewModel` paramètre, composable `AdminScreen`, `onNavigateToAdmin` branché |
| `MainActivity.kt` | Instanciation `AdminPrefsRepository`, `AdminViewModel`, passage à `LexicaApp` et `ReviewViewModelFactory` |

---

## Architecture implémentée

```
AdminConfig.kt             → ADMIN_EMAILS (à renseigner)
AdminPrefsRepository.kt    → SharedPreferences (reviewMode, sessionSize, challenges, jeux)
AdminViewModel.kt          → collecte + mutations + actions système
AdminScreen.kt             → UI Compose 3 sections
```

## Flux de navigation

```
ProfileScreen (connecté en tant qu'admin)
  → bouton ⚙️ Mode Admin (visible UNIQUEMENT si email ∈ AdminConfig.ADMIN_EMAILS)
    → AdminScreen (route "admin")
```

---

## Sections de l'AdminScreen

### 📚 Entraînement (Révision)
- **Type de carte** : `FilterChip` horizontal → Mot→Déf / Déf→Mot / Les deux
- **Taille de session** : Slider 5–50 cartes (défaut 20)
- **Défis orthographiques** : Toggle ON/OFF
- **Défis sémantiques** : Toggle ON/OFF

### 🎮 Jeux
- **QCM** : Slider 3–20 questions
- **Memory** : Sélecteur grille (2×2 / 2×3 / 4×4 / 4×6)
- **Bouton** : 🔄 Réinitialiser XP & Niveau

### 🔧 Système
- **Bouton** : 🗑️ Effacer stats quotidiennes (graphe profil)
- **Bouton** : 🔥 Simuler streak 7 jours

---

## ⚠️ Action requise par le Chef d'Orchestre

**Dans `AdminConfig.kt`**, remplacer la valeur placeholder :
```kotlin
val ADMIN_EMAILS: Set<String> = setOf(
    "admin@example.com"  // ← Remplacer par votre email Firebase
)
```

---

## Persistance

Les préférences admin sont dans `SharedPreferences` clé `"admin_prefs"` :

| Clé | Type | Défaut |
|-----|------|--------|
| `admin_review_mode` | String | `"both"` |
| `admin_challenge_ortho_enabled` | Boolean | `true` |
| `admin_challenge_semantic_enabled` | Boolean | `true` |
| `admin_session_size` | Int | `20` |
| `admin_qcm_count` | Int | `10` |
| `admin_memory_grid` | String | `"4x4"` |

---

## Impact sur les autres modules

### ReviewViewModel
- `loadSession()` : utilise `adminPrefsRepository?.sessionSize ?: limit` 
- `gradeCard()` : les défis ne se déclenchent que si `orthoEnabled` / `semanticEnabled` est `true`
- Si `adminPrefsRepository == null` (legacy) → comportement identique à avant

### ProfileViewModel / ProfileScreen
- `isAdmin = false` par défaut (utilisateurs normaux ne voient rien)
- Le bouton admin n'apparaît QUE si `currentUser?.email ∈ AdminConfig.ADMIN_EMAILS`

---

## Checklist

- [x] `AdminConfig.kt` — constante emails (placeholder à remplacer)
- [x] `AdminPrefsRepository.kt` — SharedPreferences + enums
- [x] `AdminViewModel.kt` — toutes les actions + factory
- [x] `AdminScreen.kt` — UI complète 3 sections (layout UX conforme CONSIGNES_TACHES)
- [x] `UserStatsRepository` — `resetStats()` + `simulateStreak()`
- [x] `DailyReviewStatDao` — `clearAll()`
- [x] `ProfileUiState` — `isAdmin` exposé
- [x] `ProfileScreen` — bouton admin conditionnel
- [x] `ReviewViewModel` — lit sessionSize + conditionne défis
- [x] `LexicaApp.kt` — route `Screen.Admin` + composable
- [x] `MainActivity.kt` — instanciation complète
- [ ] Renseigner votre email dans `AdminConfig.kt`

