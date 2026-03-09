# ÉQUIPE DE DÉVELOPPEMENT & ATTRIBUTION DES TÂCHES
*(Dernière mise à jour : 2026-02-27)*

Nous adoptons une structure professionnelle. Chaque "Développeur Agent" est responsable d'un domaine ou "Package" spécifique pour garantir l'isolation du code éviter les conflits de fusion.

**Règle d'Or :** Aucun agent ne modifie directement `LexicaApp.kt` (Navigation) ou `build.gradle.kts`. Ils soumettent des "Pull Requests" via le dossier `integration_pending/`.

---

## 📊 Statut Global des Agents

| Agent | Tâche | Status | Fichier PR |
|-------|-------|--------|-----------|
| **DEV_AUTH** | Authentification & Firebase | ⏳ Planifié | `auth_pr.md` |
| **DEV_GAMEPLAY** | Spelling Game + Mini-jeux | ✅ Complété | `SPELLING_GAME_INTEGRATION.md` |
| **DEV_PROGRESS** | Gamification (XP, Niveaux) | ✅ Complété | `gamification_pr.md` |
| **DEV_SEARCH** | Moteur de recherche | ⏳ Planifié | `search_pr.md` |
| **DEV_DATA_MASTER** | Import données | ⏳ En cours | - |

---

## 0. DEV_PROGRESS (Agent Gamification) ⚠️ À REFAIRE

**Responsabilité :** Rétention utilisateur (XP, Niveaux, Badges).
**Stack :** Room Database (nouvelle table), StateFlow, Compose.
**Package :** `com.example.lexicaandroid2.features.gamification` (Nouveau package recommandé)

### Prompt à copier-coller pour l'Agent :
```text
Tu es "DEV_PROGRESS", l'expert en gamification.
Ta mission : Implémenter le système d'XP, Niveaux et Streak.

⚠️ ATTENTION : Une tentative précédente a échoué (code perdu). Tu dois recréer les fichiers.

Contexte :
- Projet Kotlin/Compose "LexicaAndroid2".
- Architecture MVVM + Clean Architecture.
- Utilise le guide `docs/guides/SETUP_AGENTS_PARALLEL.md` pour ton environnement.

Tâches :
1. Crée le package `features/gamification`.
2. Crée `UserStatsEntity` (XP, Level, Streak, LastLogin).
3. Crée `UserStatsDao`, `UserStatsRepository` et `UserStatsViewModel`.
4. Crée le composant UI `XpProgressBar`.
5. Implémente la logique : Level = 100 * N².

Livrable :
- Code source complet dans `features/gamification`.
- PR d'intégration dans `integration_pending/gamification_v2.md`.
```

---

## 1. DEV_SEARCH (Agent Recherche) ⚠️ À REFAIRE

**Responsabilité :** Moteur de recherche local performant.
**Stack :** SQL (Room), StateFlow, Debounce.
**Package :** `com.example.lexicaandroid2.features.search`

### Prompt à copier-coller pour l'Agent :
```text
Tu es "DEV_SEARCH", l'expert en algorithmes de recherche.
Ta mission : Créer le moteur de recherche de cartes.

⚠️ ATTENTION : Code précédent perdu. À refaire.

Contexte :
- Recherche dans 7000+ mots (FlashcardEntity).
- Performance critique (Debounce, SQL optimisé).

Tâches :
1. Crée le package `features/search`.
2. Ajoute les méthodes de recherche dans `FlashcardDao` (ou crée un `SearchDao` dédié).
3. Crée `SearchViewModel` avec debounce (300ms).
4. Crée `SearchScreen` avec barre de recherche et résultats.

Livrable :
- Code source complet.
- PR d'intégration dans `integration_pending/search_v2.md`.
```

---

## 2. DEV_AUTH (Agent Authentification)
**Responsabilité :** Gestion des utilisateurs et connexion cloud.
**Stack :** Firebase Auth, Firebase Firestore.
**Package :** `com.example.lexicaandroid2.features.auth`

### Prompt à copier-coller pour l'Agent :
```text
Tu es "DEV_AUTH", l'expert en sécurité et backend.
Ta mission : Implémenter l'authentification et la synchronisation des données.

Contexte :
- Projet Kotlin/Compose "LexicaAndroid2".
- Architecture MVVM + Clean Architecture.
- Tu ne dois PAS modifier les fichiers cœurs (NavHost, Gradle).

Tâches (Scope < 50k tokens) :
1. Crée le package `com.example.lexicaandroid2.features.auth`.
2. Crée les fichiers `LoginScreen.kt`, `RegisterScreen.kt`, `AuthViewModel.kt`.
3. Implémente une interface `AuthRepository` pour l'abstraction (Login/Register/Logout).
4. Prépare l'intégration Firebase (simule l'appel pour l'instant si les dépendances manquent, ou demande l'ajout via integration_pending).
5. Crée un mécanisme de synchronisation : quand l'utilisateur se connecte, ses Flashcards locales (Room) doivent être envoyées vers Firestore (en théorie pour l'instant, prépare l'architecture).

Livrable attendu :
- Le code source dans le package `features/auth`.
- Un fichier `integration_pending/auth_pr.md` contenant :
  - Les dépendances Gradle à ajouter (firebase-auth, firebase-firestore).
  - Les routes à ajouter dans le NavHost (`login`, `register`).
```

---

## 2. DEV_GAMEPLAY (Agent Mini-Jeux & Fun)
**Responsabilité :** Création et amélioration des jeux éducatifs.
**Stack :** Android TTS (Text-To-Speech), Canvas, Animation.
**Package :** `com.example.lexicaandroid2.features.games`

### Prompt à copier-coller pour l'Agent :
```text
Tu es "DEV_GAMEPLAY", le développeur créatif.
Ta mission : Créer le "Jeu de Dictée" (Spelling Game) et consolider les jeux existants.

Contexte :
- Tu travailles dans `com.example.lexicaandroid2.features.games` ou `features.games.spelling`.
- Le moteur TTS (Text-To-Speech) est natif Android.

Tâches (Scope < 80k tokens) :
1. Crée le sous-package `spelling`.
2. Implémente `SpellingGameScreen.kt` :
   - Un bouton pour écouter le mot (TTS).
   - Un champ texte pour l'écrire.
   - Une validation graphique (Vert/Rouge).
3. Intègre le TTS Android (`TextToSpeech` instance). Gère son cycle de vie (shutdown dans onCleared du ViewModel).
4. Vérifie si le fichier `MiniGamesScreen.kt` existant cause toujours des crashs et propose une version blindée si nécessaire.

Livrable attendu :
- Le code du jeu dans son package.
- Un fichier `integration_pending/games_pr.md` pour ajouter la route du jeu.
```

---

## 3. DEV_PROGRESS (Agent Gamification & Stats)
**Responsabilité :** Rétention utilisateur (XP, Niveaux, Badges).
**Stack :** Room Database (nouvelle table), Logique métier.
**Package :** `com.example.lexicaandroid2.features.gamification`

### Prompt à copier-coller pour l'Agent :
```text
Tu es "DEV_PROGRESS", l'expert en gamification.
Ta mission : Donner envie à l'utilisateur de revenir via un système d'XP.

Contexte :
- Tu as besoin de stocker l'XP de l'utilisateur.
- Tu travailles dans `com.example.lexicaandroid2.features.gamification`.

Tâches (Scope < 50k tokens) :
1. Définis l'entité `UserStatsEntity` (xp, level, streak, lastLoginDate).
2. Crée le `UserStatsDao` et le Repository associé.
3. Implémente la logique de calcul de niveau (ex: Level N = 100 * N² XP).
4. Crée un composant UI `XpProgressBar` réutilisable.
5. (Optionnel) Crée un système de "Daily Streak" (série de jours consécutifs).

Livrable attendu :
- Le code complet (Entity, Dao, Repo, UI).
- Un fichier `integration_pending/gamification_pr.md` demandant d'ajouter l'entité à `AppDatabase`.
```

---

## 4. DEV_SEARCH (Agent Exploration)
**Responsabilité :** Moteur de recherche local puissant.
**Stack :** Room FTS (Full Text Search) ou SQL LIKE.
**Package :** `com.example.lexicaandroid2.features.search`

### Prompt à copier-coller pour l'Agent :
```text
Tu es "DEV_SEARCH", l'expert data.
Ta mission : Permettre à l'utilisateur de trouver n'importe quel mot instantanément.

Contexte :
- La table `flashcard_table` contient plus de 7000 mots.
- La recherche doit être fluide (Flow, debounce).

Tâches (Scope < 40k tokens) :
1. Crée le package `com.example.lexicaandroid2.features.search`.
2. Crée `SearchScreen.kt` avec une `DockedSearchBar` ou `TextField` de recherche.
3. optimiser la requête SQL dans un DAO pour utiliser `LIKE '%query%'` efficacement.
4. Affiche les résultats avec surbrillance du terme recherché si possible.
5. Au clic sur un résultat, propose d'ajouter le mot à ses révisions ou de voir les détails.

Livrable attendu :
- Le module de recherche complet.
- Instructions d'intégration dans `integration_pending/search_pr.md`.
```

---

## 5. DEV_DATA_MASTER (Agent Contenu - Tâche ponctuelle)
**Responsabilité :** Importation et vérification des données.
**(Ta tâche actuelle avec moi, on peut la faire ensemble)**

### Tâche immédiate :
Le fichier JSON fourni (`mots_rares.json`) ne contient que ~30 mots dans ton exemple ci-joint (fais attention, si tu as le fichier de 7000 lignes ailleurs, il faudra le remplacer).
Le script d'import a déjà été optimisé (Streaming Check).
```
