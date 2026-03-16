# 📅 DAILY STANDUP - Journal Unique de Suivi

**Responsable:** Chef d'Orchestre  
**Fréquence:** Quotidienne  
**Format:** SEUL fichier de suivi du projet (remplace tous les "etat_*.md", "rapport_*.md", etc.)

---

## ⚠️ RÈGLE FONDAMENTALE

**DAILY_STANDUP.md est le SEUL endroit pour documenter l'état du projet.**

- ✅ Ajouter une section ici chaque jour
- ✅ Mettre à jour FEATURES.md si status change
- ✅ Mettre à jour CONSIGNES_TACHES.md si nouvelle TACHE
- ❌ NE PAS créer d'autres fichiers de suivi
- ❌ NE PAS créer de fichiers "etat_DATE.md"
- ❌ NE PAS créer de fichiers "rapport_*.md"

---

## 🎯 Format Quotidien

Ajouter une nouvelle section chaque jour avec ce format:

```markdown
## 📅 YYYY-MM-DD (Jour X)

### ✅ Accompli
- TACHE_XX intégrée
- Modification LexicaApp.kt
- Build compiled

### 🔴 Bloquants
- Aucun / Description du bloquant

### 🔜 Demain
- TACHE_YY à intégrer
- Test sur émulateur

### 📊 Statut Global
Mini-jeux: 5/10
Build: ✅ OK
```

---

## 📅 2026-03-04 - Prise de Fonction Chef d'Orchestre

### ✅ Accompli
- [x] Lire documentation complète (START_HERE → INDEX_DOCUMENTS)
- [x] Audit complet du projet et identification des devoirs
- [x] **Nettoyage & Clarification de la doc:**
  - Suppression fichiers doc inutiles créés
  - Modification START_HERE.md → section Chef d'Orchestre claire
  - Modification CONSIGNES_TACHES.md → section Chef d'Orchestre + résumé au début
  - Modification DAILY_STANDUP.md → SEUL fichier de suivi
  - Modification INDEX_DOCUMENTS.md → pointage correct
- [x] **Fix Compatibilité Kotlin/Firebase:**
  - Firebase BOM downgraded: 33.10.0 → 32.8.1 (compatible avec Kotlin 1.9.22)
  - **BUILD SUCCESSFUL** ✅ (39 actionable tasks executed)

### 🔴 Bloquants
- Aucun

### 🔜 Demain
- Identifier agents disponibles
- Assigner TACHE_03 et TACHE_04-06

### 📊 Statut Global
```
Mini-jeux:      4/10 ████░░░░░░ 40%
Gamification:   ✅ Intégré
Search:         ✅ Intégré
Auth:           ✅ Firebase compatible (32.8.1)
Build:          ✅ SUCCESSFUL (0 errors)
Documentation:  ✅ NETTOYÉE & CLARIFIÉE
```

---

## 📅 2026-03-09 - Intégration 6 mini-jeux + nouvelles tâches

### ✅ Accompli
- [x] Prise de connaissance complète du projet
- [x] Diagnostique bug moteur de recherche (TACHE_S1 créée avec fix prescrit)
- [x] **Intégration 6 mini-jeux** dans `LexicaApp.kt` + `MiniGamesScreen.kt` :
  - Anagrammes, Mode Chrono, Memory, Définition à Compléter, Associations Sémantiques, Spelling Avancé
- [x] **BUILD SUCCESSFUL** ✅ (0 erreurs, 2026-03-09)
- [x] TACHE_S2 créée : recherche API externe + fix accents + fix espace
- [x] TACHE_11 créée : menu profil (icône top-right dashboard)
- [x] FEATURES.md mis à jour : 10/10 mini-jeux ✅

### 🔴 Bloquants
- TACHE_S1 + TACHE_S2 : moteur recherche bugué (agent à lancer)
- TACHE_S1 : ✅ fix bug Job annulé terminé
- TACHE_S2 : ✅ terminé — fallback accents (Kotlin normalize), fallback API (WiktionnaireScraper existant réutilisé)
- Git toujours non initialisé (TACHE_03)

### 🔜 Prochaines actions
- Lancer agent sur TACHE_S1 (fix bug Job) + TACHE_S2 (robustesse + API)
- Lancer agent sur TACHE_11 (profil)
- TACHE_03 (Git) quand disponible

### 📊 Statut Global
```
Mini-jeux:      10/10 ██████████ 100% (intégrés, non testés sur émulateur)
Gamification:   ✅ Intégré
Search:         🔴 BUG (TACHE_S1 + TACHE_S2 en attente d'agent)
Auth:           ✅ Firebase (32.8.1)
Profil:         ⏳ TACHE_11 à lancer
Git:            ❌ Non initialisé (TACHE_03)
Build:          ✅ SUCCESSFUL 2026-03-09
PRs intégrées:  6 (anagrams, chrono, memory, fillword, semantic, spelling_advanced)
```

---

## 📅 2026-03-09 - Session Intégration Sprint (TACHE_11 → TACHE_14b)

### ✅ Accompli

**Intégration complète de 6 tâches livrées par les agents :**

- [x] **TACHE_11 — Nom + Icône « Lexica »**
  - `strings.xml` → "Lexica" + icônes launcher déjà en place
  - Intégré et vérifié

- [x] **TACHE_12 — Actions liste de mots (supprimer / favoris / détail)**
  - `WordDetailScreen.kt` + `WordDetailViewModel` + `WordDetailViewModelFactory` livrés
  - `WordListScreen.kt` : star + delete icônes ajoutés
  - `WordListViewModel.kt` : `toggleFavorite()` + `deleteCard()`
  - Route `word/{cardId}` + `Screen.WordDetail` ajoutés dans `LexicaApp.kt`
  - `canNavigateBack` + titre « Détail du mot »

- [x] **TACHE_13 — Statistiques profil Anki-style**
  - `DailyReviewStat.kt` + `DailyReviewStatDao.kt` + `DailyReviewStatHelper.kt` livrés
  - `LexicaDatabase.kt` : v5, entité + DAO + `MIGRATION_4_5`
  - `ReviewViewModel.kt` : hook `DailyReviewStatHelper.recordReview()` dans `gradeCard()`
  - `ProfileScreen.kt` : section « Activité de révision » avec `StatBox` (Aujourd'hui / Taux 7j / Meilleure série) + `WeekBarChart`
  - `ProfileViewModel.kt` : `todayCards`, `last7Days`, `successRate7Days`, `bestStreak30Days`

- [x] **TACHE_14 — Défis intégrés dans la révision (Ortho + Sémantique)**
  - `ChallengeOverlay.kt` + `ChallengeResultOverlay` + `SemanticValidator.kt` + `KeywordExtractor.kt` livrés
  - `ReviewViewModel.kt` : `activeChallengeType`, `validateChallenge()`, `dismissChallenge()`, déclenchement toutes les 3 bonnes réponses
  - `ReviewScreen.kt` : affichage overlay challenge

- [x] **TACHE_14b — TFLite MiniLM + fallback Jaccard**
  - `TFLiteSemanticValidator.kt` + `ModelDownloadManager` + `ModelDownloadUI.kt` livrés
  - Dépendances `tensorflow-lite:2.14.0` + `tensorflow-lite-support:0.4.4` ajoutées
  - `SemanticValidatorFactory` : utilise TFLite si modèle caché, sinon Jaccard
  - `ReviewViewModel.kt` : lazy init via `SemanticValidatorFactory.createSemanticValidator(context)`
  - `ReviewViewModelFactory` + `MainActivity` : context passé

- [x] **TACHE_15 — Barre « Ajouter un mot » intelligente**
  - `AddWordsScreen.kt` + `AddWordsViewModel.kt` refactorés par l’agent
  - `AddWordsViewModelFactory` mis à jour (+ `flashcardRepository`)
  - `MainActivity.kt` : `AddWordsViewModelFactory(reserveRepository, repository)`

- [x] **Fixes de compilation**
  - `LexicaApp.kt` : accolade manquante `sealed class Screen` corrigée
  - `ReviewScreen.kt` : smart cast `challengeResult` corrigé
  - `ReviewViewModel.kt` : `ArrayDeque<Flashcard]` → `ArrayDeque<Flashcard>` corrigé
  - `ModelDownloadUI.kt` : `shape` invalide sur `LinearProgressIndicator` supprimé
  - Imports `SemanticValidator` + `SemanticValidatorFactory` ajoutés

- [x] **BUILD SUCCESSFUL** ✅ (warnings seulement, 0 erreurs)

### 🔴 Bloquants
- Aucun

### 🔜 Demain
- **TACHE_16** : Banner téléchargement modèle IA au premier lancement (non-bloquant)
- Tester sur émulateur : défis de révision, stats profil, détail carte
- Vérifier URL du modèle Hugging Face (modèle `.tflite` multilingual accessible ?)

### 📊 Statut Global
```
Mini-jeux:       10/10 ██████████ 100%
Gamification:    ✅ Intégré
Search:          ✅ Fixé (TACHE_S1 + TACHE_S2)
Auth:            ✅ Firebase (32.8.1)
Profil:          ✅ Stats Anki (TACHE_13)
Liste de mots:   ✅ Favoris + Suppression + Détail (TACHE_12)
Défis révision: ✅ Ortho + Sémantique Jaccard (TACHE_14)
TFLite MiniLM:   ✅ Intégré (déléchargement auto à faire — TACHE_16)
Ajout mot UX:    ✅ Barre intelligente (TACHE_15)
Git:             ❌ Non initialisé (TACHE_03)
Build:           ✅ SUCCESSFUL 2026-03-09 (0 erreurs)
Tâches actives:  TACHE_16 🔴
```

---


### ✅ Accompli

**TACHE_09 - Daily Challenge** ✅ **CODE TERMINÉ**
- ✅ `DailyChallengeViewModel.kt` créé (226 lignes)
  - Rotation automatique des 4 jeux (algorithme `DAY_OF_YEAR % 4`)
  - Détection de complétion via `lastLoginDate` existant
  - Timer countdown temps réel (coroutine avec delay(1000))
  - Bonus XP +20 à la complétion
  - États : Loading, Available, Completed, Error

- ✅ `DailyChallengeScreen.kt` créé (517 lignes)
  - UI Material3 moderne avec animations
  - 4 états visuels distincts
  - Countdown visuel (boxes H:M:S)
  - Animation pulsation sur header
  - Card de stats utilisateur (Niveau/XP/Série)

- ✅ Documentation complète (4 fichiers MD)
  - `daily_challenge_pr.md` : Guide d'intégration détaillé (650 lignes)
  - `TACHE_09_SUMMARY.md` : Résumé de livraison
  - `TACHE_09_TEST_GUIDE.md` : Guide de tests
  - `README.md` : Documentation technique du module

### 📊 Métriques
- **Code Kotlin :** 743 lignes (ViewModel + Screen)
- **Documentation :** 1550+ lignes (4 fichiers MD)
- **Total :** ~2300 lignes
- **Scope :** ~45 000 tokens (conforme à l'estimation)

### 🎯 Fonctionnalités Livrées
- Rotation automatique des jeux (1 jeu imposé par jour)
- Détection si déjà joué aujourd'hui
- Bonus XP streak (+20 XP)
- Countdown temps réel jusqu'à minuit
- Integration gamification (UserStatsRepository)
- UI/UX cohérente et animée

### ⚠️ Points Clés
- ✅ **Aucune migration Room nécessaire** (utilise `lastLoginDate` existant)
- ✅ **Aucune nouvelle dépendance** (tout existe déjà)
- ⚠️ **Limitation connue** : Flag `todayChallengeCompleted` en mémoire (amélioration future suggérée)

### 🔜 Prochaine Étape : Intégration par le Chef

**👉 Lire :** `integration_pending/daily_challenge_pr.md`

**Checklist d'intégration (résumée) :**
1. Ajouter `Screen.DailyChallenge` dans `LexicaApp.kt`
2. Créer `dailyChallengeViewModel` dans MainActivity
3. Ajouter composable dans NavHost
4. Modifier routes des jeux pour `isDailyChallenge` (paramètre)
5. Appeler `completeDailyChallenge()` depuis les jeux
6. Ajouter bouton dans Dashboard
7. Compiler et tester

**Temps estimé :** 30-45 minutes

### 🔴 Bloquants
- Aucun

### 📊 Statut Global Mis à Jour
```
Mini-jeux:       10/10 ██████████ 100% (intégrés)
Daily Challenge: ✅ CODE PRÊT (⏳ intégration requise)
Gamification:    ✅ Intégré
Search:          🔴 BUG (TACHE_S1 + TACHE_S2 en attente)
Auth:            ✅ Firebase (32.8.1)
Profil:          ⏳ TACHE_11 à lancer
Git:             ❌ Non initialisé (TACHE_03)
Build:           ✅ À RECOMPILER après intégration
PRs à intégrer:  1 (daily_challenge)
```

---

## 📅 2026-03-12 - Prise de fonction + intégration TACHE_09/10 + nouvelles tâches

### ✅ Accompli
- [x] Audit complet de l'état réel du code vs documentation (plusieurs incohérences corrigées)
- [x] **Correction doc Git** : TACHE_03 était déjà faite (3 commits, branches main+develop). Docs erronées.
- [x] **Correction doc Firebase** : Auth entièrement codée (`FirebaseAuthRepository`, `LoginScreen`, `RegisterScreen`, `AuthUser`, `AuthRepository`) + câblée dans `MainActivity`/`ProfileScreen`. Ce qui manque = routes NavHost Login/Register uniquement.
- [x] **TACHE_10 (XP jeux)** : Matching/QCM/Hangman déjà connectés. Ajout hook manquant `onAwardXp` pour `SpellingGameScreen` dans `LexicaApp.kt` ✅
- [x] **TACHE_09 (Daily Challenge)** : Déjà intégralement intégré (LexicaApp.kt + MainActivity). Rien à faire ✅
- [x] **TACHE_17 créée** : Mode Admin (bouton visible uniquement ton adresse e-mail, panneau de contrôle pour tester type révision/défis/session size)
- [x] **TACHE_18 créée** : Diagnostic Firebase puis activation routes Login/Register
- [x] **FEATURES.md** mis à jour : TACHE_10 ✅, Auth statut corrigé en ⚠️ partiel, TACHE_17/18 référencées
- [x] **TACHE_18 COMPLÉTÉE** : Routes `Screen.Login` + `Screen.Register` ajoutées, composables NavHost, `onSignInRequested` branché, `LoginViewModel`/`RegisterViewModel` instanciés dans `MainActivity`. `firebase_diagnostic_pr.md` livré ✅
- [x] **Bonus TACHE_15** : `AddWordsViewModelFactory` corrigée (2 args) dans `MainActivity`
- [x] **TACHE_17 COMPLÉTÉE** : Mode Admin complet — `AdminConfig`, `AdminPrefsRepository`, `AdminViewModel`, `AdminScreen`. Bouton admin conditionnel dans ProfileScreen. `ReviewViewModel` lit les prefs admin (sessionSize, défis). `UserStatsRepository` enrichi (`resetStats`, `simulateStreak`). `DailyReviewStatDao` enrichi (`clearAll`). Route `Screen.Admin` intégrée. `admin_mode_pr.md` livré ✅

### 🔴 Bloquants
- Vérifier manuellement dans Firebase Console : Email/Password activé + SHA-1 enregistré

### 🔜 Prochaine session
- Builder et tester auth + mode admin sur émulateur
- Choisir prochaine tâche parmi : TACHE_21 (Bottom Nav), TACHE_19 (Réglages), TACHE_20 (Déverrouillage XP)

### 📊 Statut Global
```
Mini-jeux:           10/10 ██████████ 100%
Déverrouillage XP:   🔴 TACHE_20
Daily Challenge:     ✅ TACHE_09
XP Jeux:             ✅ TACHE_10
Gamification:        ✅ Intégré
Search:              ✅ Fixé
Auth Firebase:       ✅ TACHE_18 — vérif Console Firebase requise
Sync Firebase:       🔴 TACHE_23
Modèle IA:           ✅ TACHE_16
Bottom Nav:          🔴 TACHE_21
Réglages:            🔴 TACHE_19
Icône moderne:       🔴 TACHE_22
Mode Admin:          ✅ TACHE_17
Icône moderne:       ✅ TACHE_22 — L+livre+particules, fond bleu marine
Profil:              ✅ TACHE_11/13
Git:                 ✅ main+develop
Build:               ✅ BUILD SUCCESSFUL (52s)
```


---

## 📅 2026-03-12 - Nouvelles tâches Sprint 3

### ✅ Accompli
- [x] **TACHE_16 marquée ✅** — Banner téléchargement modèle IA intégré
- [x] **TACHE_18 marquée ✅** — Firebase Auth routes Login/Register activées
- [x] **TACHE_19 créée** — Page Réglages + bouton ⚙️ TopBar (thème, police, notifications, préfs entrainement)
- [x] **TACHE_20 créée** — Déverrouillage progressif des 10 mini-jeux par paliers XP (niveau 1→20)
- [x] **TACHE_21 créée** — Barre de navigation inférieure 4 onglets + écran "Mode En Ligne" placeholder
- [x] **TACHE_22 créée** — Icône app moderne adaptive icon (SVG, concept L/livre sur fond bleu foncé)
- [x] **TACHE_23 créée** — Sync données Firebase : upload/download progression, dialog alerte si écrasement local
- [x] **FEATURES.md** mis à jour avec toutes les nouvelles tâches

### 🔴 Bloquants
- Aucun

### 🔜 Prochaines sessions — Ordre recommandé
1. **TACHE_21** (Bottom Nav) — impact fort sur l'UX globale, à faire tôt
2. **TACHE_19** (Réglages) — fonctionnalité visible et utile rapidement
3. **TACHE_20** (Déverrouillage jeux) — motivation utilisateur
4. **TACHE_22** (Icône) — rapide, impact visuel
5. **TACHE_17** (Admin) — pour toi uniquement
6. **TACHE_23** (Sync Firebase) — le plus lourd, après que l'auth soit validée sur émulateur
7. **TACHE_16** — déjà fait ✅

### 📊 Statut Global
```
Mini-jeux:           10/10 ██████████ 100%
Déverrouillage XP:   🔴 TACHE_20
Daily Challenge:     ✅ TACHE_09
XP Jeux:             ✅ TACHE_10
Gamification:        ✅ Intégré
Search:              ✅ Fixé
Auth Firebase:       ✅ TACHE_18 — vérif Console Firebase requise
Sync Firebase:       🔴 TACHE_23
Modèle IA:           ✅ TACHE_16
Bottom Nav:          🔴 TACHE_21
Réglages:            🔴 TACHE_19
Icône moderne:       🔴 TACHE_22
Mode Admin:          🔴 TACHE_17
Git:                 ✅ main+develop
Build:               🔄 À recompiler
```

---

## 📅 2026-03-12 - Finalisation intégrations pending + alignement doc

### ✅ Accompli
- [x] Vérification complète de l'état réel du code vs `integration_pending/`
- [x] **Daily Challenge finalisé** : les 4 jeux concernés notifient maintenant `DailyChallengeViewModel` à la fin de partie, ce qui ferme le vrai gap d'intégration restant
- [x] **Sync Firebase finalisé côté déconnexion** : `ProfileScreen` utilise désormais `SyncViewModel.signOutWithSync()` avant logout
- [x] **Mise à jour du suivi produit** : `FEATURES.md` réaligné sur les modules déjà présents dans le code (`TACHE_19` à `TACHE_23`, sync Firestore, bottom nav, réglages, admin, unlock XP)
- [x] **Mise à jour de la zone tampon** : `integration_pending/README.md` converti en état réel des PRs intégrées vs actions manuelles restantes
- [x] **Build encore vert** après corrections de flux

### 🔴 Bloquants
- Firebase Console : activer `Email/Password` si ce n'est pas déjà fait
- Firebase Console : ajouter le `SHA-1` du keystore debug pour les tests réels
- Tests runtime multi-appareils de la sync Firestore encore à exécuter

### 🔜 Suite logique
- Tester le cycle complet Daily Challenge sur émulateur
- Tester login → sync cloud → signout → relogin sur une seconde session
- Décider si les assets launcher legacy API < 26 doivent être régénérés

---

## 📅 2026-03-15 - Stabilisation UX, Git, profil, review et nouvelles entrées produit

### ✅ Accompli
- [x] **Workflow Git clarifié et simplifié**
  - création d'un snapshot Git de sécurité sur branche de travail
  - tag créé : `avant-que-jai-compris-git`
  - documentation réalignée sur le workflow réel : `main` stable + `develop` intégration + `integration_pending`
  - simplification de `START_HERE.md`, `GUIDE_CHEF_DORCHESTRE.md`, `CONSIGNES_TACHES.md`
- [x] **Catalogue des tâches réaligné sur l'état réel**
  - `TACHE_12` marquée terminée
  - `TACHE_13` marquée terminée
  - `TACHE_14` marquée terminée
  - `TACHE_10` marquée partiellement traitée (Matching refondu, QCM/Pendu à confirmer)
  - `TACHE_11` marquée partiellement traitée (nom + icône présents, refonte d'icône encore souhaitée)

- [x] **Mode Profil stabilisé**
  - correction du crash Compose à l'ouverture du profil
  - correction d'un second crash lié à la `TopAppBar` / navigation
  - retrait de l'accès admin depuis le profil
  - accès admin déplacé vers `Settings`

- [x] **Révision / Entraînement améliorés**
  - restauration du vrai `ReviewScreen.kt` après corruption partielle du fichier
  - retour du bouton `+` et des détails de carte
  - ajout d'un vrai flip recto/verso réversible pendant l'entraînement
  - renommage du CTA dashboard : `COMMENCER L'ENTRAÎNEMENT`
  - correction de l'affichage des boutons de notation (`A REVOIR`, `JE L'AI`, `TROP FACILE`) avec texte centré et multi-ligne
  - admin refondu avec **4 bascules indépendantes** :
    - `Définition → Mot`
    - `Mot → Définition`
    - `Défi sémantique`
    - `Défi orthographique`
  - logique `ReviewViewModel` réalignée sur ces 4 modes activables séparément

- [x] **Paramètres / Admin**
  - retrait des réglages trompeurs ou redondants (`accent color`, ancien switch `définition en premier`, anciens réglages entraînement non branchés)
  - retour d'un vrai réglage utile de texte sous forme de **modulateur global**
  - application réelle du modulateur à l'app via `fontScale` Compose dans `MainActivity`
  - ajout de l'entrée `Mode Admin` dans `Settings`
  - ajout du réglage manuel du niveau utilisateur dans l'admin
  - ajout d'une **déclaration de propriété intellectuelle** au nom de **Paul Mottet** dans les paramètres

- [x] **Liste de mots / Détail**
  - remplacement de l'ouverture plein écran du détail mot par une **popup**
  - conservation des infos détaillées existantes : définition, exemples, synonymes, étymologie, progression

- [x] **Mini-jeux / Daily Challenge**
  - ajout d'un bloc de proposition du **Défi du jour** directement en haut de l'écran `Mini-Jeux`
  - début de différenciation UX entre mini-jeux classiques et défi quotidien

- [x] **Jeu de correspondance refondu**
  - passage d'une validation paire par paire à une **validation globale**
  - l'utilisateur associe toutes les paires d'abord, puis valide une seule fois
  - succès : écran vert, félicitations, XP, passage automatique au lot suivant
  - échec : écran d'erreur avec nombre de fautes, `Recommencer`, `Accueil`
  - bouton `Accueil` relié au `dashboard`

- [x] **Navigation / Produit**
  - ajout d'une nouvelle page **`Utilisation`**
  - ajout de l'entrée `Utilisation` dans la barre du bas
  - ajout d'un bouton `Utilisation des mots` sur l'accueil
  - ajout d'un vrai bloc visuel `Lexica` en haut à gauche du dashboard pour casser le vide

- [x] **Synchronisation cloud / local**
  - changement du comportement du dialogue local/serveur :
    - plus de popup au redémarrage d'une session déjà connectée
    - synchro silencieuse privilégiée pour ces cas
    - conservation du cas d'arbitrage pour le vrai passage `invité -> connecté`

### 🔴 Bloquants
- **Crash encore présent en fin de session de révision** lorsque la série configurée (ex. 20 cartes) est terminée
- Build global complet non revalidé proprement sur tout l'ensemble après l'empilement des changements de session
- Plusieurs chantiers demandés restent encore ouverts (voir section suivante)

### 🔜 Fonctions restantes identifiées
- Corriger le crash de fin de session en révision
- Ajouter un **vrai service TTS central** réutilisable dans les jeux et l'entraînement
- Ajouter la **lecture vocale des faces de cartes** dans `Review`
- Ajouter un **mode spécial audio / voiture** avec lecture automatique des cartes
- Rendre le jeu de correspondance plus visuel (trait entre les paires ou repositionnement face à face)
- Enrichir réellement la page `Utilisation` avec des exercices d'emploi des mots
- Refaire l'icône de l'application avec une direction artistique choisie

### 📊 Statut Global
```
Git / workflow:        ✅ Clarifié et simplifié
Profil:                ✅ Stabilisé
Révision:              ⚠️ Améliorée mais crash fin de session à corriger
Admin:                 ✅ Refactoré en 4 modes indépendants
Settings:              ✅ Nettoyé + modulateur texte global
Word detail popup:     ✅ Intégré
Mini-jeux UX:          ✅ Défi du jour proposé
Matching:              ✅ Refonte logique validée
Utilisation:           ⚠️ Page créée, contenu avancé à faire
TTS / mode voiture:    🔴 À faire
Icône app:             🔴 À refaire
Build:                 🔄 À revalider globalement
```

### 📊 Statut Global
```
Mini-jeux:           10/10 ██████████ 100%
Daily Challenge:     ✅ Intégré et maintenant complété en fin de partie
XP Jeux:             ✅ Intégré
Search:              ✅ Intégré + corrigé
Auth Firebase:       ✅ Intégré (console à vérifier)
Sync Firebase:       ✅ Intégré (tests runtime à faire)
Mode Admin:          ✅ Intégré (email admin à renseigner)
Réglages:            ✅ Intégré
Bottom Nav:          ✅ Intégré
Déverrouillage XP:   ✅ Intégré
Icône moderne:       ✅ Intégrée
Build:               ✅ SUCCESSFUL
```

---

## 📅 2026-03-15 - Session UX / TTS / mode voiture / réalignement doc

### ✅ Accompli
- [x] **Réglages peaufinés**
  - ajout d'un vrai bouton `Valider` pour la taille de police
  - ajout d'un aperçu local dédié
  - correction de l'ambiguïté visuelle de l'aperçu après application globale
- [x] **Accueil / navigation / thème**
  - simplification du branding dashboard vers `Lexica` dans la top bar
  - fond central `Dashboard` / `Profil` aligné sur le thème sombre
  - ordre de la bottom bar ajusté avec `Usage` à droite de `Mini-jeux`
  - comportement des labels de bottom bar ajusté : 1 ligne à 100%, retour possible sur 2 lignes si la police augmente
- [x] **Audio / TTS avancés**
  - création d'un service central `LexicaTtsService`
  - migration du TTS dupliqué dans les modes dictée vers ce service partagé
  - ajout de commandes audio dans `Review` : lecture du mot, lecture de la définition, options audio dédiées
  - ajout des bascules `lecture auto du mot` / `lecture auto de la définition`
- [x] **Mode voiture (première version)**
  - création de `DrivingModeScreen.kt` + `DrivingModeViewModel.kt`
  - ajout d'un bouton `Mode voiture` sur l'écran d'accueil (temporaire)
  - lecture auto mot puis définition, avec `Pause`, `Reprendre`, `Répéter`, `Suivant`, `Retour`
- [x] **Icône launcher retravaillée**
  - nouvelle itération vectorielle plus simple / plus moderne dans `ic_launcher_foreground.xml`
- [x] **Build validée**
  - `:app:assembleDebug` ✅ SUCCESSFUL

### 🔴 Bloquants
- Le crash de fin de session `Review` reste volontairement mis de côté
- Les boutons audio grisés indiquent probablement un TTS non prêt / non disponible sur l'appareil ou l'émulateur ; diagnostic runtime encore à faire
- La bottom bar reste à peaufiner visuellement (espacements entre labels / taille perçue)

### 🔜 Prochaines tâches utiles
- Diagnostiquer clairement la disponibilité TTS sur appareil / émulateur et afficher un retour UX explicite
- Finaliser le `mode voiture` (options de délai, placement final du bouton, lecture configurable)
- Corriger le crash de fin de session `Review`
- Faire le rendu graphique avancé du `Matching`
- Enrichir réellement la page `Utilisation`

### 📊 Statut Global
```
Réglages UX:            ✅ Améliorés (aperçu + validation)
Bottom Nav:             ⚠️ Intégrée, peaufinage visuel restant
TTS central:            ✅ Intégré
Audio Review:           ⚠️ Intégré, diagnostic runtime restant
Mode voiture:           ⚠️ V1 intégrée
Icône launcher:         ⚠️ Nouvelle itération intégrée, DA encore perfectible
Review fin de session:  🔴 Crash toujours ouvert
Build:                  ✅ SUCCESSFUL (assembleDebug)
```


