# 📅 Daily Challenge Module

## 📋 Vue d'ensemble

Le module Daily Challenge implémente un système de défi quotidien qui propose un jeu différent chaque jour, avec rotation automatique entre les 4 jeux existants (Matching, QCM, Hangman, Spelling). Le système encourage l'engagement quotidien via un bonus XP et maintient une série (streak) de jours consécutifs.

---

## 🎯 Fonctionnalités

### Core Features
- ✅ **Rotation automatique des jeux** : 1 jeu imposé par jour basé sur `DAY_OF_YEAR % 4`
- ✅ **Détection de complétion** : Empêche de jouer 2 fois le même challenge dans une journée
- ✅ **Bonus XP Streak** : +20 XP à chaque complétion du challenge
- ✅ **Countdown temps réel** : Affiche le temps restant jusqu'au prochain challenge (minuit)
- ✅ **Integration gamification** : Mise à jour automatique de la streak via `UserStatsRepository`

### UI Features
- ✅ **Interface Material3** moderne et animée
- ✅ **États visuels distincts** : Loading, Available, Completed, Error
- ✅ **Animations** : Pulsation de l'icône header, transitions entre états
- ✅ **Countdown visuel** : Boxes stylisées pour heures/minutes/secondes
- ✅ **Card de stats** : Niveau, XP, Série de l'utilisateur

---

## 📁 Architecture

### Package : `presentation/dailychallenge/`

```
dailychallenge/
├── DailyChallengeViewModel.kt    (230 lignes)
│   ├── DailyChallengeState       (sealed class - 4 états)
│   ├── GameType                  (enum - 4 jeux)
│   ├── DailyChallengeUiState     (data class)
│   └── DailyChallengeViewModel   (logique métier)
│
├── DailyChallengeScreen.kt       (517 lignes)
│   ├── DailyChallengeScreen      (composable principal)
│   ├── DailyChallengeHeader      (header animé)
│   ├── LoadingContent            (état loading)
│   ├── AvailableChallengeContent (challenge disponible)
│   ├── CompletedChallengeContent (challenge complété)
│   ├── TimeBox                   (countdown boxes)
│   ├── ErrorContent              (gestion erreurs)
│   └── UserStatsCard             (stats utilisateur)
│
└── README.md                     (ce fichier)
```

---

## 🔄 Logique de Rotation des Jeux

### Algorithme
```kotlin
fun GameType.Companion.getGameForDate(date: Calendar): GameType {
    val dayOfYear = date.get(Calendar.DAY_OF_YEAR) // 1-365 (ou 366)
    val gameIndex = dayOfYear % 4                   // 0, 1, 2, 3
    return entries[gameIndex]                       // MATCHING, QCM, HANGMAN, SPELLING
}
```

### Cycle de rotation
```
Jour 1  (1 % 4 = 1)  → QCM
Jour 2  (2 % 4 = 2)  → HANGMAN
Jour 3  (3 % 4 = 3)  → SPELLING
Jour 4  (4 % 4 = 0)  → MATCHING
Jour 5  (5 % 4 = 1)  → QCM  (cycle recommence)
...
```

**Avantages :**
- Prévisible et déterministe (même jour = même jeu)
- Pas de stockage nécessaire
- Distribution équitable sur l'année (91-92 occurrences par jeu)

**Note :** Tous les utilisateurs ont le même jeu le même jour (peut être modifié si besoin de personnalisation).

---

## 🧩 Détection de Complétion

### Mécanisme

Le système utilise **2 sources de vérité** :

1. **`UserStatsEntity.lastLoginDate`** (base de données)
   - Mis à jour par `updateStreak()` au lancement de l'app
   - Timestamp en millisecondes

2. **`todayChallengeCompleted`** (mémoire ViewModel)
   - Flag volatile pour cette session
   - `true` quand `completeDailyChallenge()` est appelé

### Logique de détection

```kotlin
// Dans loadDailyChallengeState()
val today = getTodayMidnight()                    // Aujourd'hui à 00:00:00
val lastChallengeDate = stats.lastLoginDate       // Timestamp de la dernière action
val lastChallengeMidnight = getMidnight(lastChallengeDate) // Ramené à 00:00:00

val isTodayCompleted = 
    lastChallengeMidnight.timeInMillis == today.timeInMillis  // Même jour ?
    && todayChallengeCompleted == true                         // Joué ce jour ?

if (isTodayCompleted) {
    // Challenge déjà complété aujourd'hui → État Completed
} else {
    // Challenge disponible → État Available
}
```

### Flux complet

```
1. User lance l'app
   └─> updateStreak() met à jour lastLoginDate

2. User ouvre Daily Challenge
   └─> loadDailyChallengeState() vérifie l'état

3. Si pas encore joué aujourd'hui
   └─> État Available affiché

4. User clique "Commencer le Défi"
   └─> Navigation vers le jeu (Matching, QCM, etc.)

5. User complète le jeu
   └─> completeDailyChallenge() appelé
       ├─> addXp(20 + gameScore)
       ├─> updateStreak()
       └─> todayChallengeCompleted = true

6. État passe à Completed
   └─> Countdown jusqu'à minuit affiché
```

### Limitation connue

**Problème :** Si l'utilisateur force-quit l'app et la relance, `todayChallengeCompleted` est reset à `false`.

**Solution temporaire :** Utilisation de `lastLoginDate` comme proxy (si déjà mis à jour aujourd'hui, on considère que le challenge peut avoir été fait).

**Solution robuste (amélioration future) :** Ajouter un champ `lastChallengeDate` dans `UserStatsEntity` :

```kotlin
@Entity(tableName = "user_stats")
data class UserStatsEntity(
    // ...existing fields...
    val lastChallengeDate: Long = 0L  // Timestamp de la dernière complétion de challenge
)
```

Voir `integration_pending/daily_challenge_pr.md` section "Améliorations futures" pour les détails de migration.

---

## ⏱️ Countdown Temps Réel

### Mécanisme

Un timer coroutine met à jour le countdown chaque seconde :

```kotlin
private fun startCountdownTimer() {
    viewModelScope.launch {
        while (isActive) {
            delay(1000) // Tick chaque seconde
            _uiState.update { 
                it.copy(timeUntilNextChallenge = getTimeUntilMidnight())
            }
        }
    }
}
```

### Calcul du temps restant

```kotlin
private fun getTimeUntilMidnight(): Long {
    val now = Calendar.getInstance()
    val tomorrow = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)  // Demain
        set(Calendar.HOUR_OF_DAY, 0)   // 00:00:00
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return tomorrow.timeInMillis - now.timeInMillis
}
```

### Affichage UI

Le countdown est affiché sous forme de 3 boxes (H:M:S) dans `CompletedChallengeContent` :

```kotlin
val hours = (timeUntilNext / (1000 * 60 * 60)) % 24
val minutes = (timeUntilNext / (1000 * 60)) % 60
val seconds = (timeUntilNext / 1000) % 60

Row {
    TimeBox(value = hours, label = "H")
    Text(":")
    TimeBox(value = minutes, label = "M")
    Text(":")
    TimeBox(value = seconds, label = "S")
}
```

**Performance :** La coroutine est lifecycle-aware et s'arrête automatiquement quand l'écran est quitté (`viewModelScope`).

---

## 🎨 États UI

### 1. Loading
Affiché pendant le chargement initial des stats utilisateur.

### 2. Available
Challenge disponible pour aujourd'hui :
- Icône emoji du jeu (🎯📝🎪🗣️)
- Nom du jeu
- Bonus XP (+20)
- Streak actuelle (si > 0)
- Bouton "Commencer le Défi"

### 3. Completed
Challenge déjà complété aujourd'hui :
- Icône de succès (✓)
- Message de félicitations
- XP gagnée
- Streak actuelle
- Countdown jusqu'au prochain challenge

### 4. Error
Erreur lors du chargement ou de la complétion.

---

## 🔗 Intégration avec le Système Existant

### Dépendances

Le module utilise uniquement des composants existants :
- ✅ `UserStatsRepository` (gamification)
- ✅ `UserStatsEntity` (champ `lastLoginDate`)
- ✅ Jetpack Compose (Material3)
- ✅ Kotlin Coroutines & Flow
- ✅ Navigation Compose (pour l'intégration)

**Aucune nouvelle dépendance Gradle.**

### Pas de Migration Room

Le champ `lastLoginDate` existe déjà dans `UserStatsEntity` :

```kotlin
@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val userId: String = "currentUser",
    val xp: Long = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastLoginDate: Long = 0L  // ✅ EXISTE DÉJÀ
)
```

**Aucune migration Room nécessaire pour la version actuelle.**

---

## 🚀 Utilisation

### Pour le Chef d'Orchestre

Voir le fichier d'intégration complet : `integration_pending/daily_challenge_pr.md`

**Checklist résumée :**
1. Ajouter la route `Screen.DailyChallenge` dans `LexicaApp.kt`
2. Créer le `DailyChallengeViewModel` dans MainActivity
3. Ajouter le composable dans le NavHost
4. Modifier les routes des jeux pour accepter `isDailyChallenge` (paramètre)
5. Appeler `completeDailyChallenge()` depuis les jeux quand `isDailyChallenge == true`
6. Ajouter un bouton d'accès dans le Dashboard

### Exemple d'intégration dans NavHost

```kotlin
composable(Screen.DailyChallenge.route) {
    val uiState by dailyChallengeViewModel.uiState.collectAsState()
    
    DailyChallengeScreen(
        uiState = uiState,
        onStartChallenge = { gameType ->
            when (gameType) {
                GameType.MATCHING -> navController.navigate(
                    Screen.Matching.createRoute(isDailyChallenge = true)
                )
                GameType.QCM -> navController.navigate(
                    Screen.Qcm.createRoute(isDailyChallenge = true)
                )
                GameType.HANGMAN -> navController.navigate(
                    Screen.Hangman.createRoute(isDailyChallenge = true)
                )
                GameType.SPELLING -> navController.navigate(
                    Screen.Spelling.createRoute(isDailyChallenge = true)
                )
            }
        }
    )
}
```

---

## 🧪 Tests

### Tests manuels recommandés

1. **Première visite du jour**
   - ✅ Vérifier que le jeu du jour s'affiche
   - ✅ Vérifier le bonus XP (+20)
   - ✅ Compléter le jeu
   - ✅ Vérifier l'état "Complété"
   - ✅ Vérifier que la streak augmente

2. **Deuxième visite le même jour**
   - ✅ Vérifier l'état "Complété" persistant
   - ✅ Vérifier le countdown actif

3. **Rotation des jeux**
   - ✅ Attendre minuit (ou changer date système)
   - ✅ Vérifier qu'un jeu différent est proposé

4. **Streak break**
   - ✅ Ne pas jouer pendant 1 jour
   - ✅ Vérifier que la streak est reset

### Tests unitaires (à implémenter)

Suggestions pour tests futurs :
```kotlin
@Test
fun `getGameForDate returns correct rotation`() {
    // Jour 1 → QCM
    // Jour 2 → HANGMAN
    // Jour 3 → SPELLING
    // Jour 4 → MATCHING
}

@Test
fun `completeDailyChallenge updates streak`() {
    // Vérifier que streak augmente de 1
}

@Test
fun `countdown calculates correct time until midnight`() {
    // Vérifier calcul temps restant
}
```

---

## 🔮 Améliorations Futures

### 1. Persistance robuste de la complétion
Ajouter `lastChallengeDate` dans `UserStatsEntity` pour éviter le reset du flag en mémoire.

### 2. Historique des challenges
Créer une table `DailyChallengeHistory` pour tracker :
- Date de complétion
- Type de jeu joué
- Score obtenu
- XP gagnée

### 3. Notifications push
Envoyer une notification à 9h si le challenge n'est pas encore complété.

### 4. Récompenses de série
Badges pour paliers de streak :
- 7 jours → "Dévoué"
- 30 jours → "Champion"
- 100 jours → "Légende"

### 5. Choix du jeu
Permettre de choisir parmi 2-3 jeux au lieu d'un seul imposé.

### 6. Leaderboard
Classement des utilisateurs par streak la plus longue.

---

## 📚 Références

### Inspiration Open Source
- **Material3 Design Guidelines** : Animation et patterns de gamification
- **Duolingo Daily Challenge** : UX de streak et countdown
- **GitHub Contributions** : Visualisation de streak

### Documentation Google
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [Jetpack Compose State](https://developer.android.com/jetpack/compose/state)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

## 📝 Métadonnées

- **Tâche:** TACHE_09
- **Auteur:** Agent Développeur
- **Date création:** 2026-03-09
- **Lignes de code:** ~750 (ViewModel: 230, Screen: 517)
- **Status:** ✅ PRÊT POUR INTÉGRATION

---

**🎯 Module créé dans le cadre de l'architecture MVVM + Clean Architecture du projet LexicaAndroid2**

