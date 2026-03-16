# ✅ FEATURES - Lexica Android 2 - Master Checklist

**Dernière mise à jour:** 2026-03-15  
**Statut global:** 🟢 En bonne santé — TACHE_09 ✅ TACHE_10 ✅ TACHE_11 ✅ TACHE_12 ✅ TACHE_13 ✅ TACHE_14 ✅ TACHE_14b ✅ TACHE_15 ✅ TACHE_17 ✅ TACHE_18 ✅ TACHE_19 ✅ TACHE_20 ✅ TACHE_21 ✅ TACHE_22 ✅ TACHE_23 ✅

---

## 🏗️ INFRASTRUCTURE & PROCESS

### 🔴 PRIORITÉ IMMÉDIATE
- [x] **Réorganiser la documentation** (Structure `docs/planning`, `docs/archive`, etc.)
- [ ] **Exécuter le script de migration** (`organize.bat` à la racine)
- [ ] **Configurer les éditeurs pour Agents** (VS Code, Android Studio, etc.)
- [ ] **Intégrer Github Copilot** sur chaque instance d'éditeur

---

## 🎮 MINI-JEUX (10 TOTAL)

### ✅ TERMINÉS (10/10)

- [x] **Jeu de Correspondance (Matching)**
  - [x] MatchingViewModel.kt
  - [x] MatchingScreen.kt
  - [x] Logique d'appairage
  - [x] Scoring system
  - [x] Tests fonctionnels
  - 📁 Location: `presentation/games/matching/`

- [x] **QCM (Questions à Choix Multiples)**
  - [x] QcmViewModel.kt
  - [x] QcmScreen.kt
  - [x] Génération questions
  - [x] Validation réponses
  - [x] Tests fonctionnels
  - 📁 Location: `presentation/games/qcm/`

- [x] **Jeu du Pendu (Hangman)**
  - [x] HangmanViewModel.kt
  - [x] HangmanScreen.kt
  - [x] Logique lettres
  - [x] Animation pendu
  - [x] Tests fonctionnels
  - 📁 Location: `presentation/games/hangman/`

- [x] **Jeu de Dictée (Spelling Game avec TTS)**
  - ⚠️ **Moteur de recherche** : bug actif (TACHE_S1 — écran blanc + reaffichage)
  - [x] SpellingGameViewModel.kt
  - [x] SpellingGameScreen.kt
  - [x] TextToSpeech natif Android
  - [x] Écoute du mot
  - [x] Validation saisie
  - [x] Tests fonctionnels
  - 📁 Location: `presentation/games/spelling/`
  - 📄 Documentation: `docs/SPELLING_GAME_INTEGRATION.md`

---

### ⏳ EN PRIORITÉ 1 (En cours)

- [x] **Fix Recherche** — TACHE_S1 + TACHE_S2 : bug écran blanc, accents et fallback API ✅
- [x] **Menu Profil** 👤 — TACHE_11 : icône + nom app "Lexica" ✅
- [x] **Actions liste de mots** — TACHE_12 : supprimer / favoris / détail carte ✅
- [x] **Statistiques Profil (Anki-style)** — TACHE_13 : `DailyReviewStat` + graphe 7 jours ✅
- [x] **Défis intégrés dans la révision** — TACHE_14 : défi ortho + sémantique (Jaccard) ✅
- [x] **Validation sémantique TFLite MiniLM** — TACHE_14b : `TFLiteSemanticValidator` + fallback Jaccard ✅
- [x] **Barre "Ajouter un mot" intelligente** — TACHE_15 : UX recherche locale + API + apercu ✅
- [ ] **Crash fin de session Review** — session de 20 cartes / fin de lot
- ⚠️ **Audio / TTS global** — service central intégré + premiers branchements, validation runtime restante
- ⚠️ **Mode voiture** — première version intégrée, options avancées et emplacement final à décider
- ⚠️ **Bottom Nav / Réglages UX** — intégrés mais peaufinage visuel encore en cours
- [ ] **Matching rendu graphique avancé**
- [ ] **Utilisation : vrais exercices d'emploi**

---

### ⏳ EN PRIORITÉ 2 (Backlog)

- [x] **Connexion XP aux mini-jeux** — TACHE_10 ✅
- [x] **Daily Challenge** 📅 — TACHE_09 ✅
- [x] **Déverrouillage progressif des jeux par XP** — TACHE_20 ✅
- [ ] **Spelling avancé (phonetique)** — voir TACHE_06 (Spelling Avance)

---

### ⏳ BACKLOG FUTUR (À faire Sprint 4+)

- [x] **Barre de navigation inférieure (Bottom Nav)** — TACHE_21 ✅
- [x] **Mode En Ligne (placeholder + UI)** — TACHE_21 ✅
- [x] **Page Réglages (thème, police, notifications)** — TACHE_19 ✅
- [x] **Icône app moderne** — TACHE_22 ✅
- [x] **Sync données Firebase (progression cloud)** — TACHE_23 ✅
- [x] **Mode Administrateur** — TACHE_17 ✅

- [ ] **Jeu de Prononciation** 🗣️
  - Speech Recognition Android
  - Validation prononciation
  - Feedback audio

- [ ] **Mode Multiplayer** 👥
  - Fire base Realtime
  - Duels temps réel
  - Leaderboard

---

## 🔧 MODULES SYSTÈME

### ✅ COMPLÉTÉS (2/3)

- [x] **Gamification (XP, Niveaux, Streak)** 🎯
  - [x] UserStatsEntity (xp, level, streak, lastLoginDate)
  - [x] UserStatsDao (8 opérations Room)
  - [x] UserStatsRepository + Implementation
  - [x] Logique métier : Level N = 100*N² XP
  - [x] XpProgressBar UI composable
  - [x] Daily Streak system
  - 📁 Location: `features/gamification/`
  - 📄 Documentation: `docs/status/RAPPORT_AUDIT_2026-03-04.md`
  - **Status:** ✅ INTÉGRÉ (Audit 2026-03-04)

- [x] **Moteur de Recherche** 🔍
  - [x] SearchViewModel avec debounce
  - [x] SearchScreen UI Material 3
  - [x] Requêtes SQL optimisées
  - [x] Tests unitaires (SearchViewModelTest)
  - [x] Affichage résultats réactif
  - 📁 Location: `presentation/search/`
  - 📄 Documentation: `integration_pending/search_pr.md` (archives)
  - **Status:** ✅ INTÉGRÉ (Audit 2026-03-04)

### ✅ COMPLÉTÉS (3/3)

- [x] **Authentification Firebase** 🔐
  - [x] LoginScreen + RegisterScreen
  - [x] AuthRepository interface
  - [x] Firebase Auth SDK integration
  - [x] Firestore sync users
  - [x] Session management (Flow currentUser)
  - [x] Navigation Login/Register activée
  - **Status:** ✅ INTÉGRÉ (TACHE_18 + TACHE_23) — ⚠️ Vérifier Email/Password, SHA-1/SHA-256 et règles Firestore côté Firebase Console
  - 📁 Location: `features/auth/`

- [x] **Mode Administrateur** ⚙️
  - [x] `AdminConfig.kt` — détection email admin
  - [x] `AdminPrefsRepository.kt` — SharedPreferences (sessionSize, reviewMode, défis, jeux)
  - [x] `AdminViewModel.kt` + `AdminScreen.kt` — panneau de contrôle complet
  - [x] Bouton admin conditionnel dans ProfileScreen (invisible pour utilisateurs normaux)
  - [x] ReviewViewModel lit prefs admin (taille session, ON/OFF défis)
  - [x] `UserStatsRepository.resetStats()` + `simulateStreak()`
  - [x] `DailyReviewStatDao.clearAll()`
  - **Status:** ✅ INTÉGRÉ (TACHE_17)
  - 📁 Location: `presentation/admin/`
  - 📄 Documentation: `integration_pending/admin_mode_pr.md`

- [ ] **Mini-Jeux Mixtes** 🎯
  - Combinaison plusieurs modes
  - Scores composites

---

## 🎯 SYSTÈMES DE JEUX

### ✅ GAMIFICATION (EN INTÉGRATION - 2026-02-28)

- [x] **Architecture**
  - [x] UserStatsEntity (xp, level, streak)
  - [x] UserStatsDao (8 opérations)
  - [x] UserStatsRepository
  - [x] GamificationManager (logique)

- [x] **UI Components**
  - [x] XpProgressBar
  - [x] XpProgressBarCompact
  - [x] Affichage en Dashboard

- [x] **Logique**
  - [x] Calcul niveaux (Level N = 100*N²)
  - [x] Daily Streak system
  - [x] Gain XP per action

- [ ] **Intégration finale**
  - [ ] Ajouter ReviewScreen (gain XP quand reviewer)
  - [ ] Ajouter scores jeux (xp per game)
  - [ ] Sauvegarder dans BD
  - [ ] Afficher dans UI

**Statut:** ✅ Code completo, ⏳ À intégrer  
**Location:** `integration_pending/gamification_pr.md`

---

### ✅ RECHERCHE (EN INTÉGRATION - 2026-02-28)

- [x] **Architecture Data**
  - [x] SearchRepository interface
  - [x] SearchRepositoryImpl
  - [x] 6 requêtes SQL optimisées

- [x] **ViewModel**
  - [x] SearchViewModel avec debounce
  - [x] StateFlow réactif
  - [x] 4 modes recherche

- [x] **UI**
  - [x] SearchScreen
  - [x] SearchBar Material3
  - [x] Tabs de filtrage
  - [x] Highlighting texte
  - [x] States (Loading, Empty, Results)

- [ ] **Intégration finale**
  - [ ] Ajouter route à LexicaApp.kt
  - [ ] Bouton navigation depuis Dashboard
  - [ ] Tests complets
  - [ ] Vérifier performances

**Statut:** ✅ Code completo, ⏳ À intégrer  
**Location:** `integration_pending/search_pr.md`

---

### ✅ AUTHENTIFICATION FIREBASE (INTÉGRÉE)

- [x] **Setup Firebase**
  - [x] Projet Firebase configuré
  - [x] `google-services.json` ajouté
  - [x] Dépendances Firebase Auth + Firestore ajoutées

- [x] **Firebase Auth**
  - [x] Email/Password signup
  - [x] Email/Password login
  - [ ] Google Sign-In
  - [x] Logout
  - [x] Session persistence

- [x] **Firebase Firestore**
  - [x] Schéma utilisateur
  - [x] Sync progression utilisateur
  - [ ] Sync scores jeux
  - [x] Conflict resolution (local vs cloud)

- [x] **UI**
  - [x] Auth screen (signup/login)
  - [x] Profile screen
  - [x] Settings sync de déconnexion via `SyncViewModel`

- [ ] **Tests**
  - [ ] Tests unitaires ciblés
  - [ ] Tests intégration Firebase sur device/emulator
  - [ ] Tests synchronisation multi-session

**Statut:** ✅ Code intégré — validations console/runtime restantes

---

## 📊 DONNÉES

### ✅ CONTENU EXISTANT

- [x] ~7000 mots flashcards
- [x] Définitions Wiktionnaire
- [x] Structure Room Database

### ⏳ À ENRICHIR

- [ ] **Synonymes/Antonymes**
  - [ ] Récupérer depuis Wiktionnaire
  - [ ] Stocker dans BD
  - [ ] Indexer pour recherche sémantique
  - **Estimation:** 2000 mots avec synonymes

- [ ] **Exemples d'utilisation**
  - [ ] Récupérer depuis Wiktionnaire
  - [ ] Ajouter à définitions
  - [ ] Afficher dans cartes

- [ ] **Catégories grammaticales**
  - [ ] Verbe, Nom, Adjectif, etc.
  - [ ] Utiliser pour filtrage
  - [ ] Afficher dans UI

- [ ] **Phonétique**
  - [ ] Récupérer IPA (International Phonetic Alphabet)
  - [ ] Utiliser pour validation Spelling Game
  - [ ] Afficher pour mots difficiles

**Responsable:** DEV_DATA  
**Priority:** 🟡 MOYENNE

---

## 🏗️ INFRASTRUCTURE

### ✅ FAIT

- [x] Architecture Clean (MVVM)
- [x] Room Database v5
- [x] Jetpack Compose UI
- [x] Navigation Compose
- [x] Material Design 3
- [x] StateFlow réactif
- [x] TextToSpeech natif

### ⏳ À FAIRE

- [ ] **Performance**
  - [ ] Optimiser requêtes SQL
  - [ ] Pagination pour listes
  - [ ] Image lazy loading
  - [ ] Memory profiling

- [ ] **Tests**
  - [ ] Unit tests (ViewModel, Repository)
  - [ ] Integration tests (Database)
  - [ ] UI tests (Compose)
  - [ ] Coverage: Minimum 70%

- [ ] **Monitoring**
  - [ ] Crash logging (Firebase Crashlytics)
  - [ ] Analytics événements
  - [ ] Performance tracking

- [ ] **Offline Support**
  - [ ] Sync queue si offline
  - [ ] Restart sync au reconnect
  - [ ] Conflict resolution

---

## 🔧 BUILD & RELEASE

### ✅ FAIT

- [x] Gradle build system
- [x] Debug APK généré
- [x] App se lance sans crash

### ⏳ À FAIRE

- [ ] **APK Release**
  - [ ] Config signing key
  - [ ] ProGuard obfuscation
  - [ ] Générer release APK
  - [ ] Version numbering (1.0.0)

- [ ] **Distribution**
  - [ ] Google Play Store setup
  - [ ] Internal testing track
  - [ ] Beta release
  - [ ] Production release

- [ ] **CI/CD**
  - [ ] GitHub Actions
  - [ ] Auto build on push
  - [ ] Auto tests
  - [ ] Auto APK generation

---

## 📱 USER EXPERIENCE

### ✅ FAIT

- [x] Dashboard principal
- [x] Navigation structure
- [x] 4 mini-jeux jouables

### ⏳ À FAIRE

- [ ] **Onboarding**
  - [ ] Welcome screen
  - [ ] Tutorial mini-jeux
  - [ ] Permission requests

- [ ] **UX Polish**
  - [ ] Animations transitions
  - [ ] Haptic feedback (vibration)
  - [ ] Sound effects
  - [ ] Dark mode support

- [ ] **Accessibilité**
  - [ ] Contentdescriptions
  - [ ] TalkBack support
  - [ ] Font size adjustable
  - [ ] Color contrast

- [ ] **Notifications**
  - [ ] Daily reminder notifications
  - [ ] Achievement notifications
  - [ ] Personalised content

---

## 📋 CHECKLIST PAR SPRINT

### **SPRINT 1 (ACTUEL - 2026-02-27 à 28)**
- [ ] Arrêter agents parallèles
- [ ] Nettoyer Gradle
- [x] Créer documentation
- [ ] Intégrer Gamification PR
- [ ] Intégrer Search PR
- [ ] Compiler et tester
- [ ] Merge to main

**Statut:** 🟡 En cours (50% fait)

---

### **SPRINT 2 (Semaine 2 - 2026-03-03 à 07)**
- [ ] Lancer agents mini-jeux Phase 1
  - [ ] Anagrammes
  - [ ] Chrono
  - [ ] Memory
- [ ] Intégrer 3 nouveaux jeux
- [ ] Évaluer performances
- [ ] Refactor si nécessaire

**Statut:** ⏳ Planifié

---

### **SPRINT 3 (Semaine 3 - 2026-03-10 à 14)**
- [ ] Lancer agents mini-jeux Phase 2
  - [ ] Spelling Avancé
  - [ ] Associations Sémantiques
  - [ ] Définition Complète
- [ ] Lancer DEV_AUTH (Firebase)
- [ ] Intégrer 3 nouveaux jeux
- [ ] Intégrer auth system

**Statut:** ⏳ Planifié

---

### **SPRINT 4 (Semaine 4 - 2026-03-17 à 21)**
- [ ] Polish et optimisation
- [ ] Tests complets (unit + integration + UI)
- [ ] Audit accessibilité
- [ ] Préparation release

**Statut:** ⏳ Planifié

---

### **SPRINT 5 (Semaine 5 - 2026-03-24 à 28)**
- [ ] Release v1.0.0
- [ ] Google Play Store publication
- [ ] User feedback collection
- [ ] Hotfixes si nécessaire

**Statut:** ⏳ Planifié

---

## 🐛 BUGS CONNUS

### 🔴 CRITIQUES

- [ ] Mini-jeux crashing at runtime (ancien problème - à vérifier après intégration)
  - Location: `presentation/games/`
  - Debug: Vérifier logcat pour `LinearProgressIndicator`
  - Status: À vérifier

### 🟡 MAJEURS

- [ ] Wiktionnaire API retourne 0 résultats
  - Location: `data/importer/WiktionnaireParser.kt`
  - Cause: Parsing HTML header "Français" défaillant
  - Fix: Refondre parser avec regex robustes
  - Priority: 🟡 MOYENNE (après jeux Phase 1)

### 🟢 MINEURS

- (Aucun signalé pour l'instant)

---

## 📚 DOCUMENTATION À CRÉER

- [ ] README.md main (mise à jour avec gamification + search)
- [ ] ARCHITECTURE.md (Clean Architecture expliquée)
- [ ] API_REFERENCE.md (docs APIs publiques)
- [ ] CONTRIBUTING.md (guide contribution agents)
- [ ] CHANGELOG.md (historique versions)

---

## 🎯 KPIs & MÉTRIQUES

### Build Quality
- [ ] Build time: < 2 min
- [ ] APK size: < 50MB
- [ ] Crash rate: < 0.1%

### Gameplay
- [ ] 10 mini-jeux implémentés ✅ (4/10 done, 6 planned)
- [ ] XP system working ⏳ (à intégrer)
- [ ] Search performant ⏳ (à tester)
- [ ] Daily activity tracking ⏳

### User Engagement
- [ ] Session duration: > 5 min
- [ ] Daily active users (DAU): à mesurer
- [ ] Game completion rate: > 80%

---

## 📞 CONTACTS & RESPONSABLES

| Rôle | Personne | Email | Status |
|------|----------|-------|--------|
| Chef d'Orchestre | Toi | - | Actif |
| DEV_PROGRESS | Agent 1 | - | ✅ Livré |
| DEV_SEARCH | Agent 2 | - | ✅ Livré |
| DEV_AUTH | Agent 3 | - | ⏳ En cours |
| DEV_GAMES | À assigner | - | ⏳ Planifié |

---

## 🔄 Mise à Jour du TODO

**Fréquence:** Quotidienne après standup  
**Responsable:** Chef d'Orchestre  
**Format:** Utiliser checkbox `[x]` pour complété, `[ ]` pour à faire  

**Exemple d'update:**
```
- [x] Intégrer gamification (fait 2026-02-28 14:00)
- [x] Tests gamification (fait 2026-02-28 14:30)
- [ ] Intégrer search (à faire 2026-02-28 15:00)
```

---

**Dernière mise à jour:** 2026-02-27 23:00  
**Prochaine review:** 2026-02-28 09:00  
**Status:** 🟡 EN CONSTRUCTION


