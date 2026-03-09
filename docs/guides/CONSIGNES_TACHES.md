# Consignes pour les Taches a Effectuer
*(Derniere mise a jour : 2026-03-04)*

Ce fichier remplace l'ancien `PROMPTS_AGENTS.md`.
Il est organise par taches (`TACHE_XX`) et non plus par agent.

---

## 🎼 TES DEVOIRS (Chef d'Orchestre) - RÉSUMÉ

**Si tu es le Chef d'Orchestre, voici tes 3 devoirs:**

1. **INTÉGRER** les PRs que les agents livrent dans `integration_pending/`
   - Copier les fichiers `.kt` dans les bons packages
   - Modifier `LexicaApp.kt` si intégration globale
   - Compiler: `./gradlew clean :app:assembleDebug`
   - Commit et push

2. **DÉCOUPER** le travail en `TACHE_XX` (ce fichier)
   - Créer une nouvelle section TACHE_XX ci-dessous
   - Assigner à un agent avec un message clair
   - Agent travaille dans SON package isolé uniquement

3. **DOCUMENTER** quotidiennement dans DAILY_STANDUP.md
   - ✅ SEUL fichier de suivi à tenir à jour
   - ❌ NE PAS créer d'autres fichiers de doc

**→ Voir section "Consignes pour le Chef d'Orchestre" ci-dessous pour tes devoirs complets**

---

## Objectif
- Le Chef d'Orchestre decoupe le travail en taches.
- Chaque tache a un scope estime autour de 100 000 tokens.
- Les prompts sont courts et operationnels.

## Lancement standard d'un agent
Message de demarrage recommande:

```text
Tu es developpeur dans un projet parmi une equipe de developpeurs supervises.
Commence par lire START_HERE.md.
Ta tache attribuee est la numero X.
```

## Regles globales (obligatoires pour tous)
1. Lire les documents dans l'ordre indique par `START_HERE.md`.
2. Travailler dans un package dedie a la tache.
3. Ne pas modifier les fichiers coeur:
   - `LexicaApp.kt`
   - `AndroidManifest.xml`
   - `build.gradle.kts`
   - `AppDatabase.kt`
4. Ne pas modifier les fichiers `.txt` (reserves au responsable du projet).
5. Les agents developpeurs n'ont pas le droit de lancer un build.
6. Si une integration globale est necessaire, creer une demande dans `integration_pending/`.
7. Mettre a jour la documentation liee a la tache (etat, decisions, integration).
8. Signaler toute incoherence detectee dans la documentation (liens casses, sections contradictoires, statuts obsoletes) dans un fichier Markdown de suivi sous `integration_pending/`.
9. (Optionnel) Proposer des ameliorations:
   - Les agents peuvent proposer de nouvelles fonctionnalites ou optimisations.
   - Ces propositions doivent etre documentees dans un fichier Markdown sous `integration_pending/` (ex: `integration_pending/proposition_feature_x.md`).
   - Inclure au minimum: contexte, valeur utilisateur, impact technique, risques, estimation.

10. Recherche et inspiration open source (cible) :
   - **Fortement recommande** pour : UI/UX nouvelle, algorithmes complexes (ex: moteur de recherche), intégrations API/SDK difficiles (ex: barre de recherche avec API).
   - **Obligatoire** en cas de probleme rencontre : si un bug/erreur de build survient, consulter 2-3 projets open source similaires pour trouver la solution eprouvee et remplacer le code problematique.
   - Verifier les licences (privilegier MIT, Apache 2.0, BSD).
   - S'inspirer de l'architecture et des patterns, pas de copier-coller aveugle.
   - **Obligatoire si utilise** : documenter les sources d'inspiration dans une section dediee du fichier `integration_pending/[tache]_pr.md`.
   - Exemples de ressources : GitHub (topics Android/Kotlin/Compose), awesome-lists, samples officiels Google/JetBrains.

## Format de livraison attendu (pour les agents)
- Code dans les packages de la tache.
- Fichier d'integration dans `integration_pending/` si necessaire.
- Notes de doc (changements + impact) dans les documents concernes.

---

## Consignes pour le Chef d'Orchestre (Aide)

### ✅ TES DEVOIRS (EN 3 POINTS)

1. **INTÉGRER les PRs**
   - Lire le fichier markdown dans `integration_pending/[tache]_pr.md`
   - Créer les fichiers `.kt` dans les bons packages
   - Modifier `LexicaApp.kt` si intégration globale nécessaire
   - Compiler: `./gradlew clean :app:assembleDebug`
   - Commit et push

2. **DÉCOUPER le travail**
   - Créer des `TACHE_XX` dans ce fichier (voir format au bas)
   - Assigner aux agents avec un message clair
   - Agenter travaille dans SON package isolé (ex: `presentation/games/anagrams/`)
   - Agent ne touche JAMAIS aux fichiers coeur

3. **DOCUMENTER quotidiennement**
   - Mettre à jour `DAILY_STANDUP.md` (SEUL fichier de suivi)
   - Pas de nouveaux fichiers doc (pollution = interdit)
   - Mettre à jour `FEATURES.md` quand une TACHE est finie
   - Mettre à jour ce fichier si nouvelles tâches

### 🚫 INTERDICTIONS STRICTES

- ❌ **NE PAS créer de nouveaux fichiers Markdown** (pollution workspace)
- ❌ **NE PAS créer de fichiers "etat_*.md"** (tout va dans DAILY_STANDUP.md)
- ❌ **NE PAS créer de fichiers de "rapport", "coordination", "quickstart"** (les docs existantes suffisent)
- ❌ **NE PAS modifier les fichiers agents directement** (c'est leur travail)

### 📋 WORKFLOW STANDARD

**1. Agent livre une PR** → `integration_pending/tache-XX-nom_pr.md`

**2. Toi, tu intègres:**
```bash
# Créer les fichiers .kt
# Modifier LexicaApp.kt si nécessaire
./gradlew clean :app:assembleDebug     # Compiler
git add .
git commit -m "feat: TACHE_XX - [description]"
git push origin main
```

**3. Mettre à jour la doc:**
- `DAILY_STANDUP.md` → Ajouter la tâche complétée
- `FEATURES.md` → Mettre à jour statut
- Ce fichier → Marquer TACHE comme ✅ TERMINEE

### 📚 SEULS DOCUMENTS À MODIFIER

| Document | Quand | Pourquoi |
|----------|-------|---------|
| `DAILY_STANDUP.md` | Chaque jour | Suivi quotidien |
| `FEATURES.md` | Après intégration | Mettre à jour statut |
| `CONSIGNES_TACHES.md` | Quand nouvelle TACHE | Ajouter tâche ou marquer finie |
| `LexicaApp.kt` | Quand intégration globale | Ajouter routes/connexions |
| `build.gradle.kts` | Quand dépendances | Ajouter libs si besoin |

## Catalogue des taches
Derniere mise a jour : 2026-03-04

---

## TACHE_01 - Module Auth UI ✅ TERMINEE
- Code livré dans `features/auth/` — intégré par le Chef d'Orchestre.
- Firebase stub en place. Brancher quand `google-services.json` disponible.

---

## TACHE_02 - Corriger Warnings Build ✅ TERMINEE
- 10 warnings corrigés. Build propre confirmé.

---

## TACHE_03 - Initialiser Git
- **Scope estime:** ~5 000 tokens
- **Zone code:** Racine du projet (pas de fichier Kotlin)
- **Livrables:**
  - `.gitignore` Android valide a la racine
  - Repo Git initialise (`git init`)
  - Branches `main` et `develop` creees
  - Commit initial : `chore: baseline stable state`
- **Contraintes:** Aucun fichier de code a modifier. Uniquement commandes Git.
- **Fichier integration attendu:** `integration_pending/git_init_pr.md`

---

## TACHE_S1 - DIAGNOSTIC & FIX — Moteur de Recherche 🔴 PRIORITAIRE
- **Scope estime:** ~25 000 tokens
- **Package isole:** `presentation/search/` — fichiers `SearchViewModel.kt` et `SearchRepositoryImpl.kt` uniquement
- **Symptome rapporte:** Ecran blanc ~0.2s puis reaffichage des memes mots, la recherche ne filtre pas.

### Cause racine identifiee (a confirmer et corriger)
Dans `SearchViewModel.kt`, la methode `performSearch()` lance un `viewModelScope.launch` a chaque appel **sans annuler le Job precedent**. Resultat : chaque frappe utilisateur + l'appel initial de `init` accumulent des coroutines actives qui collectent toutes le meme Room Flow. Quand Room emet une update, TOUS les collecteurs se declenchent en cascade → ecran blanc (`isLoading = true`) puis resultats anciens re-injectes.

### Fix prescrit
**Option A — Job explicite (simple, recommandee) :**
```kotlin
private var searchJob: Job? = null

private fun performSearch(query: String, type: SearchType) {
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val flow = when (type) { ... }
            flow.collect { results ->
                _uiState.update { it.copy(results = results, isLoading = false) }
            }
        } catch (e: CancellationException) { throw e }
          catch (e: Exception) { _uiState.update { it.copy(isLoading = false, error = e.message) } }
    }
}
```
**Option B — Pipeline reactif avec `flatMapLatest` (plus idiomatique Kotlin/Flow) :**
Fusionner `_searchQuery` + `searchType` dans un seul `StateFlow<Pair>` puis `.debounce(300).flatMapLatest { }` — un seul collecteur actif garanti.

### Livrables
- `SearchViewModel.kt` corrige (annulation Job ou flatMapLatest)
- Verifier que `clearSearch()` annule aussi le Job en cours
- Test : taper "chat" → resultat filtre en <400ms, pas de reaffichage, pas d'ecran blanc
- `integration_pending/search_fix_pr.md` si modification de `LexicaApp.kt` necessaire

### Inspiration open source
- **Now in Android** (Google) : `github.com/android/nowinandroid` — `SearchViewModel` avec `flatMapLatest` + debounce, pattern de reference officiel Google.
- **Tivi** (Chris Banes) : `github.com/chrisbanes/tivi` — gestion avancee des flows de recherche avec cancellation propre.

### Contraintes
- Ne pas modifier `FlashcardDao.kt` (les queries SQL sont correctes)
- Ne pas modifier `SearchRepositoryImpl.kt` sauf si necessaire apres diagnostic
- Ne pas modifier `SearchScreen.kt` (UI correcte)
- Fichier integration attendu : `integration_pending/search_fix_pr.md`

---

## TACHE_04 - Mini-jeux Groupe A : Anagrammes + Mode Chrono
- **Scope estime:** ~100 000 tokens
- **Packages isoles:**
  - `presentation/games/anagrams/` (Anagrammes)
  - `presentation/games/chrono/` (Mode Chrono)
- **Ces 2 jeux peuvent etre developpes sequentiellement dans la meme session agent**

### Jeu 1 — Anagrammes
- **Livrables :** `AnagramsViewModel.kt`, `AnagramsScreen.kt`
- **Logique :** mot pioche depuis `FlashcardRepository`, lettres melangees presentees sous forme de tuiles draggables ou boutons, validation de la solution, score + XP
- **UI inspiration :**
  - **Google Unscramble Codelab** (`github.com/google-developer-training/basic-android-kotlin-compose-training-unscramble`) — reference officielle Google pour ce type exact de jeu en Compose, logique ViewModel claire
  - **WordScramble-Android** (`github.com/ShreyashKore/wonderwords`) — tuiles de lettres avec animations Compose

### Jeu 2 — Mode Chrono (Speed Challenge)
- **Livrables :** `ChronoViewModel.kt`, `ChronoScreen.kt`
- **Logique :** selection duree (30s / 60s / 300s), enchaınement questions depuis `FlashcardRepository`, timer countdown reactif avec `StateFlow<Long>`, ecran resultat score
- **UI inspiration :**
  - **Trivia-App-Compose** (`github.com/CuriousNikhil/androidx-compose-trivia`) — timer + QCM en Compose, architecture MVVM propre
  - **QuizApp** (`github.com/AhmedAbdelaal2001/Trivia-Quiz-App`) — gestion countdown + transitions entre questions

### Contraintes communes
- Ne pas modifier `LexicaApp.kt`, `MiniGamesScreen.kt`, `build.gradle.kts`
- Fichiers integration attendus : `integration_pending/anagrams_pr.md` et `integration_pending/chrono_pr.md`

---

## TACHE_05 - Mini-jeux Groupe B : Memory + Definition a Completer
- **Scope estime:** ~90 000 tokens
- **Packages isoles:**
  - `presentation/games/memory/` (Memory)
  - `presentation/games/fillword/` (Definition a Completer)

### Jeu 1 — Memory (Jeu de paires)
- **Livrables :** `MemoryViewModel.kt`, `MemoryScreen.kt`
- **Logique :** grille de cartes (4x4 / 5x4 / 6x4), appairage mot ↔ definition, animation flip (utilisez `animateFloatAsState` + `rotationY`), best score sauvegarde via PR Room
- **UI inspiration :**
  - **memory-game-compose** (`github.com/alexjlockwood/android-jetpack-compose-samples`) — reference pour animation flip de cartes en Compose avec `graphicsLayer`
  - **Compose Memory Game** (`github.com/philipplackner/MemoryGame`) — grid layout + flip animation complete, code tres lisible, licence MIT
  - **FlipCard Compose** : pattern `Card { GraphicsLayer { rotationY = ... } }` documente dans la galerie d'exemples JetBrains

### Jeu 2 — Definition a Completer (Fill-in-the-blank)
- **Livrables :** `FillWordViewModel.kt`, `FillWordScreen.kt`
- **Logique :** affiche une definition avec le mot masque (remplace par `_____`), 4 boutons choix, validation, score
- **UI inspiration :**
  - **AnkiDroid** (`github.com/ankidroid/Anki-Android`) — pattern de presentation de flashcards avec choix multiples, architecture mature
  - Simple QCM : s'inspirer du `QcmScreen.kt` existant dans le projet (meme pattern, adapter pour les definitions)

### Contraintes communes
- Best score Memory → passer par `integration_pending/memory_pr.md` pour ajout entite Room
- Ne pas modifier `LexicaApp.kt`, `MiniGamesScreen.kt`, `build.gradle.kts`
- Fichiers integration attendus : `integration_pending/memory_pr.md` et `integration_pending/fillword_pr.md`

---

## TACHE_06 - Mini-jeux Groupe C : Spelling Avance + Associations Semantiques
- **Scope estime:** ~100 000 tokens
- **Packages isoles:**
  - `presentation/games/spellingadvanced/` (Spelling Avance)
  - `presentation/games/semantic/` (Associations Semantiques)

### Jeu 1 — Spelling Avance (TTS + saisie tolerante)
- **Prerequis :** `SpellingGameScreen.kt` existant dans `presentation/games/spelling/` — s'en inspirer, ne pas le modifier
- **Livrables :** `SpellingAdvancedViewModel.kt`, `SpellingAdvancedScreen.kt`
- **Logique :** TTS lit le mot, l'utilisateur saisit, tolerance accents/pluriels (`normalize()` via `java.text.Normalizer`), systeme de jokers (3 par partie — reveler une lettre, rejouer, sauter), feedback detaille sur erreur
- **UI inspiration :**
  - **SpellingBee-Android** (`github.com/queencodemonkey/spelling-bee`) — UI epuree pour saisie de mots avec feedback, animations d'erreur
  - **Android TTS Reference** : `github.com/googlesamples/android-text-to-speech` — integration TTS robuste avec callbacks `onStart`/`onDone`
  - Pour la tolerance d'accents : pattern `Normalizer.normalize(str, NFD).replace(Regex("\\p{M}"), "")` — documente dans Kotlin stdlib

### Jeu 2 — Associations Semantiques
- **Livrables :** `SemanticViewModel.kt`, `SemanticScreen.kt`
- **Logique :** affiche un mot, 4 choix (synonyme/antonyme/definition/relation), verifier d'abord si `FlashcardEntity` a des champs `synonymes`/`antonymes`; sinon generer des distracteurs plausibles depuis les 50 mots les plus proches par longueur
- **UI inspiration :**
  - **Duolingo-clone-compose** (`github.com/yojan/duolingo-compose-clone`) — UX de choix multiples avec feedback visuel (vert/rouge), animations de validation identiques au vrai Duolingo
  - **Open Flashcard** (`github.com/nicktindall/cyclic-flashcards`) — gestion de distracteurs algorithmiques propre

### Contraintes communes
- Verifier les champs de `FlashcardEntity.kt` avant de coder les distracteurs
- Ne pas modifier le Spelling Game existant ni les fichiers coeur
- Fichiers integration attendus : `integration_pending/spelling_advanced_pr.md` et `integration_pending/semantic_pr.md`

---

## TACHE_10 - Connecter XP aux Mini-jeux existants
- **Scope estime:** ~30 000 tokens
- **Package isole:** Modifications dans `presentation/games/matching/`, `presentation/games/qcm/`, `presentation/games/hangman/` uniquement
- **Livrables:**
  - Appel `GamificationViewModel.addXp(amount)` en fin de partie dans chaque jeu existant (Matching, QCM, Hangman, Spelling)
  - Montants XP selon le resultat (parfait = 25 XP, reussi = 15 XP, echec = 5 XP)
  - `GamificationViewModel` passe en parametre depuis `LexicaApp.kt` (demander l'integration via PR)
- **Contraintes:** Ne pas modifier `LexicaApp.kt` directement. Passer par `integration_pending/xp_games_pr.md` pour le branchement.
- **Fichier integration attendu:** `integration_pending/xp_games_pr.md`

---

## TACHE_11 - Ecran Profil Utilisateur
- **Scope estime:** ~40 000 tokens
- **Package isole:** `presentation/profile/` ← agent travaille UNIQUEMENT ici
- **Livrables:**
  - `ProfileViewModel.kt`
  - `ProfileScreen.kt`
  - Affichage : niveau actuel, XP (barre de progression), streak, nombre de mots appris, scores par jeu
  - Donnees issues de `GamificationViewModel` (UserStatsRepository) et `FlashcardRepository`
- **Contraintes:** Ne pas modifier les fichiers coeur. Passer par `integration_pending/profile_pr.md` pour la route.
- **Fichier integration attendu:** `integration_pending/profile_pr.md`

---

## TACHE_12 - Daily Challenge
- **Scope estime:** ~45 000 tokens
- **Package isole:** `presentation/dailychallenge/` ← agent travaille UNIQUEMENT ici
- **Livrables:**
  - `DailyChallengeViewModel.kt`
  - `DailyChallengeScreen.kt`
  - Logique : 1 jeu impose par jour (rotation des jeux existants), detection si deja joue aujourd'hui (timestamp Room), bonus XP streak (+20 XP), affichage du prochain challenge avec countdown
- **Contraintes:** Necessite un champ `lastChallengeDate` — passer par `integration_pending/` pour l'ajout Room. Ne pas modifier les fichiers coeur.
- **Fichier integration attendu:** `integration_pending/daily_challenge_pr.md`

---

## TACHE_S2 - Ameliorer le Moteur de Recherche (API + robustesse)
- **Scope estime:** ~80 000 tokens
- **Packages touches:**
  - `presentation/search/` — `SearchViewModel.kt` (bugfix + logique API)
  - `data/repository/SearchRepositoryImpl.kt` (trim + normalisation)
  - `data/local/FlashcardDao.kt` (normalisation SQL si necessaire)
  - Nouveau fichier : `data/remote/WiktionnaireApiService.kt` ou `data/remote/DictionaryApiService.kt`
- **Prerequis :** TACHE_S1 terminee (bug Job non annule corrige)

### Problemes a corriger (3 bugs urgents dans les fichiers existants)

**Bug 1 — Espace(s) en fin de saisie → 0 resultat**
Dans `SearchRepositoryImpl.kt`, toutes les methodes `searchBy*` doivent appeler `.trim()` sur `query` avant de passer au DAO :
```kotlin
// AVANT
dao.searchByWord(query, limit)

// APRES
dao.searchByWord(query.trim(), limit)
```
Faire de meme dans `SearchViewModel.kt` dans `onSearchQueryChanged` : nettoyer avant de mettre dans `_searchQuery`.

**Bug 2 — Accents / diacritiques**
Les requetes SQL LIKE ne sont pas insensibles aux accents sur Android/SQLite (ex: "etude" ne trouve pas "étude").
Solution : ajouter une colonne `mot_normalized` (sans accents, en minuscules) dans `FlashcardEntity` et indexer dessus, OU utiliser une fonction de normalisation Kotlin avant la requete et matcher sur la colonne normalisee.
- Pattern recommande : `java.text.Normalizer.normalize(str, NFD).replace(Regex("\\p{M}"), "").lowercase()`
- Passer par `integration_pending/search_s2_pr.md` pour la migration Room (si ajout colonne)

**Bug 3 — Casse (majuscule/minuscule)**
SQL LIKE avec `LOWER()` est deja present dans les requetes DAO → verifier que la normalisation Kotlin en amont utilise aussi `.lowercase()` avant envoi

### Nouvelle fonctionnalite : Recherche externe si mot absent de la liste

Quand la recherche locale retourne 0 resultats (apres debounce + trim), lancer automatiquement une requete vers une API de dictionnaire et afficher les resultats dans une section separee "🌐 Resultats du dictionnaire".

**API recommandee (gratuite, sans cle) :**
- **Free Dictionary API** : `https://api.dictionaryapi.dev/api/v2/entries/en/<word>`
  - Pas de cle API
  - Retourne definitions, phonetique, exemples
  - Deja utilisee dans de nombreux projets open source Android
  - Limite : anglais uniquement → voir alternative pour le francais

- **Alternative francais — Wiktionnaire MediaWiki API** :
  - `https://fr.wiktionary.org/w/api.php?action=query&titles=<mot>&prop=extracts&format=json`
  - Gratuite, pas de cle, couvre le francais complet
  - Parsing JSON manuel necessaire (extraire le premier paragraphe)

**Architecture recommandee :**
1. `data/remote/DictionaryApiService.kt` — interface Retrofit avec 1 methode `getDefinition(word: String)`
2. `data/remote/DictionaryApiServiceImpl.kt` — configure OkHttp + Gson/Moshi
3. `domain/repository/SearchRepository.kt` — ajouter `suspend fun searchExternal(word: String): ExternalSearchResult?`
4. `SearchRepositoryImpl.kt` — implementer avec fallback gracieux (catch IOException)
5. `SearchViewModel.kt` — si `results.isEmpty()` apres 300ms debounce → appeler `searchExternal(query.trim())`
6. `SearchScreen.kt` — section separee "🌐 Dictionnaire" en bas des resultats locaux

**Dependances Gradle a ajouter (via PR):**
```
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```
Deja peut-etre presents — verifier `build.gradle.kts` avant d'ajouter.

**Permission Internet dans Manifest (via PR):**
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Inspiration open source
- **Now in Android** (Google) : `github.com/android/nowinandroid` — architecture repository offline-first + remote fallback propre
- **Dictionary App Compose** : `github.com/philipplackner/DictionaryApp` — exemple complet Retrofit + Clean Architecture + Compose pour une appli de dictionnaire, ARCHITECTURE IDENTIQUE AU BESOIN, licence MIT. **Lire ce projet en entier avant de coder.**
- **Free Dictionary API client** : `github.com/yamin8000/freeDictionary` — client Kotlin pour cette meme API, peut servir de base
- **Retrofit Android Guide** : `github.com/square/retrofit` — reference officielle

### Livrables
- `SearchViewModel.kt` bugfixe (trim + normalisation + fallback API)
- `SearchRepositoryImpl.kt` avec trim et normalisation
- `data/remote/DictionaryApiService.kt` + impl
- `integration_pending/search_s2_pr.md` avec : ajout dependances Gradle, permission Manifest, migration Room si colonne normalisee ajoutee

### Contraintes
- Resultats externes NE DOIVENT PAS etre sauvegardes automatiquement dans Room (affichage only)
- Ajouter un bouton "Ajouter a ma liste" sur le resultat externe qui, lui, sauvegarde
- Ne pas modifier `LexicaApp.kt` directement
- Fichier integration attendu : `integration_pending/search_s2_pr.md`

---

## TACHE_11 - Menu Profil Utilisateur (icone top-right dashboard)
- **Scope estime:** ~50 000 tokens
- **Packages isoles:**
  - `presentation/profile/` ← agent cree les fichiers ici
  - `presentation/common/LexicaTopAppBar.kt` ← NE PAS MODIFIER, passer par PR
- **Objectif :** Un icone de profil (👤) dans le coin superieur droit du Dashboard ouvre un ecran de profil complet

### Comportement attendu
1. Sur l'ecran Dashboard uniquement, afficher un `IconButton` avec `Icons.Default.AccountCircle` dans la `TopAppBar` (action de droite)
2. Cliquer sur l'icone navigue vers `Screen.Profile` (route `"profile"`)
3. L'ecran Profil affiche :
   - Avatar / initiales de l'utilisateur (si auth Firebase connecte, sinon "Invit\u00e9")
   - Niveau actuel + XP courante (barre de progression `XpProgressBar` existante)
   - Streak (serie de jours consecutifs)
   - Statistiques : total mots appris, nombre de parties jouees par type de jeu
   - Bouton "Se connecter / Se deconnecter" (hook vers `AuthRepository`)

### Architecture
```
ProfileViewModel.kt  — collecte UserStatsRepository + AuthRepository
ProfileScreen.kt     — ecran Compose avec les sections ci-dessus
```

### Integration cœur requise (via PR)
- `LexicaApp.kt` :
  - Ajouter `Screen.Profile` dans `sealed class Screen`
  - Ajouter `composable("profile") { ProfileScreen(...) }`
  - Modifier la `Scaffold.topBar` pour passer `onProfileClick` au `DashboardScreen`
  - OU ajouter directement une action dans la `LexicaTopAppBar` quand `currentRoute == Dashboard`
- `DashboardScreen.kt` : recevoir `onNavigateToProfile: () -> Unit` et l'appeler depuis le bouton profil

### Inspiration open source
- **Now in Android** (Google) : `github.com/android/nowinandroid` → ecran Settings avec profil utilisateur en top bar, pattern exact recherche
- **Tivi** (Chris Banes) : `github.com/chrisbanes/tivi` → `AccountUiScreen.kt` — gestion connexion/deconnexion dans un profil lateral, UI tres propre Material3
- **Jetpack Compose Samples** : `github.com/android/compose-samples` → `Jetchat` — icone de profil circulaire dans la TopAppBar avec navigation
- Pour l'avatar initiales : `github.com/IlyaPavlovskii/whatsapp-android-compose` — composable `AvatarImage` avec fallback initiales

### Contraintes
- Ne pas modifier `LexicaApp.kt`, `LexicaTopAppBar.kt`, `DashboardScreen.kt` directement
- Tout passer par `integration_pending/profile_pr.md`
- Utiliser `XpProgressBar.kt` et `GamificationViewModel` existants (ne pas recoder)
- Fichier integration attendu : `integration_pending/profile_pr.md`

---

Quand une tache est ajoutee, utiliser ce format:
```markdown
## TACHE_XX - Titre
- Scope estime: ~100 000 tokens
- Package isole: `presentation/xxx/` <- agent travaille UNIQUEMENT ici
- Livrables: ...
- Contraintes: ...
- Fichier integration attendu: integration_pending/....md
```
## Gouvernance type open source
- Le Chef d'Orchestre joue le role de mainteneur (triage, validation, fusion).
- Les agents jouent le role de contributeurs (implementation isolee + proposition d'integration).
- Les changements globaux passent par revue et integration manuelle.