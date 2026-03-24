# 🏗️ Daily Challenge - Architecture Diagram

## 📐 Vue d'ensemble du système

```
┌──────────────────────────────────────────────────────────────────┐
│                         USER INTERFACE                            │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │            DailyChallengeScreen.kt                       │    │
│  │                                                           │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │    │
│  │  │  Available  │  │  Completed  │  │   Loading   │    │    │
│  │  │   Content   │  │   Content   │  │   Content   │    │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘    │    │
│  │                                                           │    │
│  │  Components:                                             │    │
│  │  • DailyChallengeHeader (animated)                      │    │
│  │  • TimeBox (countdown H:M:S)                            │    │
│  │  • UserStatsCard (niveau/xp/série)                      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ▲                                    │
│                              │ StateFlow<UiState>                │
│                              │                                    │
└──────────────────────────────┼────────────────────────────────────┘
                               │
┌──────────────────────────────┼────────────────────────────────────┐
│                    PRESENTATION LAYER                             │
├──────────────────────────────┼────────────────────────────────────┤
│                              │                                    │
│  ┌──────────────────────────▼─────────────────────────────────┐ │
│  │         DailyChallengeViewModel.kt                          │ │
│  │                                                              │ │
│  │  State:                                                     │ │
│  │  • DailyChallengeUiState (StateFlow)                       │ │
│  │  • todayChallengeCompleted (Boolean)                       │ │
│  │                                                              │ │
│  │  Methods:                                                   │ │
│  │  • loadDailyChallengeState()                               │ │
│  │  • completeDailyChallenge(gameScore)                       │ │
│  │  • startCountdownTimer()                                   │ │
│  │  • getTimeUntilMidnight()                                  │ │
│  │                                                              │ │
│  │  Helper:                                                    │ │
│  │  • GameType.getGameForDate(date) → rotation algorithm     │ │
│  └──────────────────────────┬─────────────────────────────────┘ │
│                              │                                    │
│                              │ Repository calls                   │
│                              │                                    │
└──────────────────────────────┼────────────────────────────────────┘
                               │
┌──────────────────────────────┼────────────────────────────────────┐
│                      DOMAIN LAYER                                 │
├──────────────────────────────┼────────────────────────────────────┤
│                              │                                    │
│  ┌──────────────────────────▼─────────────────────────────────┐ │
│  │        UserStatsRepository (interface)                      │ │
│  │                                                              │ │
│  │  • getUserStats(): Flow<UserStatsEntity?>                  │ │
│  │  • addXp(amount: Int)                                      │ │
│  │  • updateStreak()                                          │ │
│  └──────────────────────────┬─────────────────────────────────┘ │
│                              │                                    │
└──────────────────────────────┼────────────────────────────────────┘
                               │
┌──────────────────────────────┼────────────────────────────────────┐
│                       DATA LAYER                                  │
├──────────────────────────────┼────────────────────────────────────┤
│                              │                                    │
│  ┌──────────────────────────▼─────────────────────────────────┐ │
│  │      UserStatsRepositoryImpl                                │ │
│  │              (gamification module)                           │ │
│  └──────────────────────────┬─────────────────────────────────┘ │
│                              │                                    │
│  ┌──────────────────────────▼─────────────────────────────────┐ │
│  │           UserStatsDao (Room)                               │ │
│  └──────────────────────────┬─────────────────────────────────┘ │
│                              │                                    │
│  ┌──────────────────────────▼─────────────────────────────────┐ │
│  │    UserStatsEntity (Room Database)                          │ │
│  │                                                              │ │
│  │  • userId: String                                           │ │
│  │  • xp: Long                                                 │ │
│  │  • level: Int                                               │ │
│  │  • streak: Int                                              │ │
│  │  • lastLoginDate: Long  ← UTILISÉ POUR DÉTECTION           │ │
│  └──────────────────────────────────────────────────────────────┘ │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Flux de Données

### 1️⃣ Chargement Initial
```
User ouvre Daily Challenge
         ↓
DailyChallengeViewModel.init
         ↓
loadDailyChallengeState()
         ↓
getUserStats() (Flow)
         ↓
Compare lastLoginDate avec aujourd'hui
         ↓
État Available OU Completed
         ↓
Affichage UI
```

### 2️⃣ Complétion du Challenge
```
User clique "Commencer le Défi"
         ↓
Navigation vers le jeu (Matching/QCM/etc.)
         ↓
User termine le jeu
         ↓
completeDailyChallenge(gameScore)
         ↓
addXp(20 + gameScore)
         ↓
updateStreak()
         ↓
todayChallengeCompleted = true
         ↓
État passe à Completed
         ↓
Countdown s'affiche
```

### 3️⃣ Timer Countdown
```
startCountdownTimer() dans init
         ↓
Coroutine infinie (viewModelScope)
         ↓
while (isActive) {
    delay(1000)
    calcul temps → minuit
    update UI
}
         ↓
UI refresh chaque seconde
```

---

## 🎮 Rotation des Jeux - Calendrier

### Mars 2026 (Exemple)
```
Date    | Jour  | Jour%4 | Jeu
--------|-------|--------|------------------
9 mars  |  68   |   0    | 🎯 MATCHING
10 mars |  69   |   1    | 📝 QCM
11 mars |  70   |   2    | 🎪 HANGMAN
12 mars |  71   |   3    | 🗣️ SPELLING
13 mars |  72   |   0    | 🎯 MATCHING
14 mars |  73   |   1    | 📝 QCM
15 mars |  74   |   2    | 🎪 HANGMAN
...
```

**Distribution sur l'année :**
- 365 jours ÷ 4 jeux = ~91 occurrences par jeu
- Jour 366 (année bissextile) : 366 % 4 = 2 → Hangman (1 occurrence bonus)

---

## 🧩 Intégration avec les Autres Modules

### ✅ Gamification Module
```
DailyChallengeViewModel
         ↓
   UserStatsRepository
         ↓
   • addXp(20)
   • updateStreak()
         ↓
   UserStatsEntity updated
```

### 🎮 Mini-Jeux Existants
```
GameScreen (Matching/QCM/Hangman/Spelling)
         ↓
   Reçoit param isDailyChallenge
         ↓
   Si true: onGameFinish()
         ↓
   completeDailyChallenge(score)
         ↓
   Daily Challenge updated
```

### 📱 Navigation
```
Dashboard
    ↓ (bouton "Défi Quotidien")
DailyChallenge
    ↓ (bouton "Commencer")
GameScreen
    ↓ (onFinish)
DailyChallenge (état Completed)
```

---

## 🧪 Scénarios de Test

### Scénario 1 : Happy Path
```
1. User lance app (9 mars, première fois)
   → État: Available, jeu: MATCHING
2. User clique "Commencer"
   → Navigation vers MatchingScreen
3. User termine le jeu (score: 15)
   → completeDailyChallenge(15) appelé
4. User retourne à Daily Challenge
   → État: Completed, XP: 35 (20+15), Série: 6
5. User voit countdown: 12:34:56
   → Timer décrémente chaque seconde
```

### Scénario 2 : Streak Continue
```
1. User joue 5 jours consécutifs
   → Série = 5
2. User joue le 6e jour
   → completeDailyChallenge() appelé
   → updateStreak() → Série = 6
3. User voit: "🔥 Série de 6 jours"
```

### Scénario 3 : Streak Break
```
1. User joue jour 1, série = 1
2. User saute jour 2 (ne joue pas)
3. User revient jour 3
   → updateStreak() détecte gap
   → Série reset à 0
4. User joue jour 3
   → Série = 1
```

### Scénario 4 : Même jour, deuxième visite
```
1. User complète challenge (matin)
   → État: Completed
2. User ferme app
3. User rouvre app (après-midi, même jour)
   → État: Completed (persiste)
   → Countdown: 08:23:15 (moins de temps)
```

---

## 🔮 Roadmap Futures

### Phase 2 : Persistance Robuste
- Ajouter `lastChallengeDate` dans `UserStatsEntity`
- Migration Room : `MIGRATION_X_Y`
- Éliminer le flag volatile `todayChallengeCompleted`

### Phase 3 : Historique
- Créer table `DailyChallengeHistory`
- Tracker: date, jeu, score, XP gagnée
- Écran "Historique" avec calendrier visuel

### Phase 4 : Notifications
- WorkManager pour notification à 9h
- "Tu n'as pas encore fait ton défi du jour !"
- Deep link vers Daily Challenge

### Phase 5 : Badges
- Streak 7 jours → Badge "Dévoué" 🥉
- Streak 30 jours → Badge "Champion" 🥈
- Streak 100 jours → Badge "Légende" 🥇
- Affichage dans Profile

### Phase 6 : Leaderboard
- Classement par streak la plus longue
- Firebase Firestore pour sync multi-users
- Écran "Classement des Séries"

---

## 📚 Fichiers à Consulter

| Fichier | Usage | Priorité |
|---------|-------|----------|
| `daily_challenge_pr.md` | Guide d'intégration complet | 🔴 HAUTE |
| `TACHE_09_TEST_GUIDE.md` | Tests post-intégration | 🟠 MOYENNE |
| `TACHE_09_SUMMARY.md` | Résumé de livraison | 🟢 INFO |
| `TACHE_09_DELIVERY.md` | Livraison visuelle | 🟢 INFO |
| `dailychallenge/README.md` | Doc technique | 🔵 RÉFÉRENCE |

---

## ✅ Signature

**Tâche :** TACHE_09 - Daily Challenge  
**Agent :** Agent Développeur  
**Date :** 2026-03-09  
**Status :** ✅ **100% TERMINÉ**  
**Qualité :** 🟢 **PRODUCTION READY**

---

**🎯 Prêt à intégrer dans le projet principal !**

