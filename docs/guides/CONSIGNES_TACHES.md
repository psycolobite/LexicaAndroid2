# Consignes pour les Taches a Effectuer
*(Derniere mise a jour : 2026-03-15)*

Ce fichier remplace l'ancien `PROMPTS_AGENTS.md`.
Il est organise par taches (`TACHE_XX`) et non plus par agent.

---

## 📱 CHECKLIST UX OBLIGATOIRE (avant toute livraison)

> **Pourquoi ?** Les agents n'ont pas accès à un émulateur. Ces règles compensent l'absence de "vue utilisateur" et évitent les bugs visuels courants détectés sur l'appli réelle.

### 🔴 RÈGLES CRITIQUES — toute violation bloque la livraison

**Layout**
- [ ] La zone de JEU (grille, cartes, question) utilise `Modifier.weight(1f)` pour occuper TOUT l'espace disponible — jamais de hauteur fixe
- [ ] Un `LazyVerticalGrid` ou `LazyColumn` N'EST JAMAIS dans un `Column` sans contrainte de hauteur (sinon = crash Compose) → utiliser `.weight(1f)` ou `.fillMaxSize()`
- [ ] Les écrans utilisent `Scaffold` ou `Column(fillMaxSize)` — pas de layout qui se coupe hors écran
- [ ] Les éléments de CONFIGURATION du jeu (sélecteurs de niveau, taille de grille, durée) sont placés EN HAUT en format compact, jamais en plein milieu de l'écran de jeu

**Sélecteurs et contrôles**
- [ ] Max 3 choix → utiliser `Row` avec `FilterChip` (pas des `Button` pleine largeur en colonne)
- [ ] Les boutons de sélection de configuration SONT TOUJOURS VISIBLES mais n'écrasent pas la zone de jeu (hauteur max : 40dp each)
- [ ] Pendant une partie en cours, les contrôles de config sont désactivés (`enabled = !gameInProgress`) mais restent visibles

**Texte et feedback**
- [ ] Les stats/score (paires trouvées, tentatives, timer) sont sur UNE SEULE ligne (`Row`) avec `SpaceBetween`, pas en colonne
- [ ] Le feedback de bonne/mauvaise réponse dure 600ms–1000ms avant de passer à la suite
- [ ] L'écran de fin de partie affiche : score final, XP gagnée, bouton Rejouer, bouton Menu

**Navigation**
- [ ] Le bouton "Retour" est en BAS de l'écran, jamais en haut (la TopAppBar s'en charge)
- [ ] Chaque jeu accepte le paramètre `onBack: () -> Unit` et l'utilise sur le bouton Retour

### 🟡 RÈGLES DE QUALITÉ — fortement recommandées

**Accessibilité**
- [ ] Taille de police minimale 14.sp pour le texte de jeu, 12.sp pour les labels secondaires
- [ ] Les cartes/tuiles ont un `contentDescription` (ex: `contentDescription = if (showFront) card.text else "Carte retournée"`)
- [ ] Contraste suffisant : texte foncé sur fond clair, ou texte clair sur fond foncé

**Performance Compose**
- [ ] Les lambdas `onClick` ne capturent pas de `State` directement → passer par ViewModel
- [ ] `remember { }` utilisé sur les calculs coûteux dans les composables
- [ ] Pas d'appels `Flow.collect` ni de coroutines dans les composables — tout passe par ViewModel + `collectAsState()`

**Gestion d'erreurs**
- [ ] Les blocs `viewModelScope.launch { }` contiennent un `try-catch` — jamais d'exception non gérée
- [ ] Si un chargement échoue → afficher un message d'erreur + bouton Retour (pas un écran blanc)
- [ ] Les `Flow.collect { }` dans les ViewModels wrappent avec `catch { e -> uiState.copy(error = e.message) }`

### 📐 TEMPLATE DE LAYOUT STANDARD pour un mini-jeu

```kotlin
// Structure de base recommandée pour TOUT mini-jeu
Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

    // 1. Header de jeu (score + progression) — hauteur fixe ~60dp
    GameHeader(title = "...", score = uiState.score, progress = uiState.progress)

    // 2. Configuration compacte (si applicable) — hauteur fixe ~40dp
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        configs.forEach { config ->
            FilterChip(selected = ..., onClick = { ... }, label = { Text("...") },
                modifier = Modifier.weight(1f))
        }
    }

    // 3. ZONE DE JEU — prend TOUT l'espace restant
    Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp)) {
        // Grille, cartes, question, etc.
    }

    // 4. Bouton action (Valider, Suivant, etc.) — hauteur fixe ~56dp
    GameButton(text = "Valider", onClick = { ... },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
}
```

---

---

## � RÈGLES DE GAME DESIGN (obligatoires pour tout mini-jeu)

> **Pourquoi ?** Les agents codent sans jouer au jeu. Ces règles définissent les paramètres de gameplay testés sur l'appli réelle, à respecter à la lettre pour chaque nouveau jeu ou modification.

### Paramètres officiels de chaque mini-jeu

| Jeu | Nb éléments/manche | Durée estimée | Rationale |
|-----|-------------------|---------------|-----------|
| **Correspondance (Matching)** | **4 paires** | ~2 min | 8 paires = écran surchargé, illisible sur mobile. 4 = lisible, satisfaisant à compléter |
| **QCM** | **10 questions** | ~3 min | Standard quizz mobile. 4 choix par question |
| **Pendu (Hangman)** | **5 mots, 6 vies/mot** | ~4 min | Passer au mot suivant si perdu (0 pts) — pas de game-over total |
| **Dictée (Spelling)** | **10 mots** | ~4 min | Avec TTS, 10 est le maximum avant fatigue d'écoute |
| **Anagrammes** | **8 mots** | ~4 min | Filtrer les mots < 4 lettres (trop faciles) et > 12 lettres (trop longs) |
| **Mode Chrono** | **Pool de 100 cartes max** | 30s/60s/300s | Tirage aléatoire dans le pool. Ne jamais charger > 100 cartes en mémoire |
| **Memory** | **4×4 par défaut (8 paires)** | ~4 min | Choix utilisateur : 4×4, 5×4, 6×4. Sélection via FilterChip compact en haut |
| **Définition à Compléter** | **10 questions** | ~3 min | 4 choix. S'inspirer du QCM existant |
| **Associations Sémantiques** | **10 questions** | ~4 min | 4 choix. Minimum 4 cartes dispo sinon message d'erreur |
| **Spelling Avancé** | **10 mots** | ~5 min | TTS + 3 jokers. Filtrer les mots > 15 lettres |
| **Daily Challenge** | Délégué au jeu du jour | variable | Rotation sur les 4 jeux de base |

### 🔴 Règles critiques de game design

**Volume de données**
- [ ] `repository.getAllCards()` est TOUJOURS suivi d'un `.take(N)` ou `.shuffled().take(N)` — jamais de liste illimitée chargée en mémoire
- [ ] Le pool de cartes pour les distracteurs (mauvaises réponses) est tiré de la **même liste déjà chargée**, pas d'un second appel à getAllCards()
- [ ] Les jeux à questions successives (QCM, FillWord, Semantic) utilisent `.shuffled().take(N)` **une seule fois** en `loadGame()`, puis itèrent sur la liste — pas de re-tirage à chaque question

**Progressivité et feedback**
- [ ] Après chaque bonne/mauvaise réponse : afficher le feedback ≥ 600ms avant de passer à la suite (utiliser `delay(800)` dans le ViewModel, PAS dans le composable)
- [ ] L'écran de fin récapitule : score X/N, XP gagnée, bouton Rejouer et bouton Menu
- [ ] Un jeu avec timer (Chrono) continue de montrer la dernière question pendant le décompte final — pas de coupure brutale

**Accessibilité des contrôles**
- [ ] Jeux à choix multiples : 4 options maximum (QCM, FillWord, Semantic, Chrono)
- [ ] Jeu de Correspondance : 4 paires côte à côte sur écran, jamais plus
- [ ] Jeux avec saisie clavier : afficher le clavier virtuel automatiquement (`KeyboardOptions(imeAction = ImeAction.Done)` + `FocusRequester`)

**Difficulté**
- [ ] Les distracteurs (mauvaises réponses) **ne doivent pas** être trivialement différents (ex: ne pas mélanger des mots de 2 lettres avec des définitions de 10 mots)
- [ ] Filtrer les cartes avec `recto.isBlank() || verso.isBlank()` avant de les utiliser
- [ ] Anagrammes : filtrer `word.length < 4 || word.length > 12`

---

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
2. Workflow Git du projet :
   - `main` = stable
   - `develop` = branche de travail et d'integration
   - les agents ne creent pas de branche dediee par defaut
   - les integrations passent par `integration_pending/`
3. Travailler dans un package dedie a la tache.
4. Ne pas modifier les fichiers coeur:
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
   - Commit et push sur `develop`

2. **DÉCOUPER le travail**
   - Créer des `TACHE_XX` dans ce fichier (voir format au bas)
   - Assigner aux agents avec un message clair
   - L'agent travaille dans SON package isolé (ex: `presentation/games/anagrams/`)
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

**1. Agent livre une PR locale** → `integration_pending/tache-XX-nom_pr.md`
- l'agent travaille dans son package isole
- pas de branche dediee par agent par defaut

**2. Toi, tu intègres:**
```bash
git switch develop || git switch -c develop

# Créer les fichiers .kt
# Modifier LexicaApp.kt si nécessaire
./gradlew clean :app:assembleDebug     # Compiler
git add .
git commit -m "feat: TACHE_XX - [description]"
git push origin develop
```

**3. Quand develop est satisfaisante, fusionner dans main:**
```bash
git switch main
git merge develop
git push origin main
```

**4. Mettre à jour la doc:**
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

## TACHE_03 - Initialiser Git ✅ TERMINEE
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

## TACHE_S1 - DIAGNOSTIC & FIX — Moteur de Recherche ✅ TERMINEE
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

## TACHE_07 - Connecter XP aux Mini-jeux existants ✅ INTEGREE
- **Scope estime:** ~30 000 tokens
- **Package isole:** Modifications dans `presentation/games/matching/`, `presentation/games/qcm/`, `presentation/games/hangman/` uniquement
- **Livrables:**
  - Appel `GamificationViewModel.addXp(amount)` en fin de partie dans chaque jeu existant (Matching, QCM, Hangman, Spelling)
  - Montants XP selon le resultat (parfait = 25 XP, reussi = 15 XP, echec = 5 XP)
  - `GamificationViewModel` passe en parametre depuis `LexicaApp.kt` (demander l'integration via PR)
- **Contraintes:** Ne pas modifier `LexicaApp.kt` directement. Passer par `integration_pending/xp_games_pr.md` pour le branchement.
- **Fichier integration attendu:** `integration_pending/xp_games_pr.md`

---

## TACHE_08 - Ecran Profil Utilisateur (icone top-right dashboard) ✅ INTEGREE
- **Scope estime:** ~50 000 tokens
- **Packages isoles:**
  - `presentation/profile/` ← agent cree les fichiers ici
  - `presentation/common/LexicaTopAppBar.kt` ← NE PAS MODIFIER, passer par PR
- **Objectif :** Un icone de profil (👤) dans le coin superieur droit du Dashboard ouvre un ecran de profil complet

### Comportement attendu
1. Sur l'ecran Dashboard uniquement, afficher un `IconButton` avec `Icons.Default.AccountCircle` dans la `TopAppBar` (action de droite)
2. Cliquer sur l'icone navigue vers `Screen.Profile` (route `"profile"`)
3. L'ecran Profil affiche :
   - Avatar / initiales de l'utilisateur (si auth Firebase connecte, sinon "Invite")
   - Niveau actuel + XP courante (barre de progression `XpProgressBar` existante)
   - Streak (serie de jours consecutifs)
   - Statistiques : total mots appris, nombre de parties jouees par type de jeu
   - Bouton "Se connecter / Se deconnecter" (hook vers `AuthRepository`)

### Architecture
```
ProfileViewModel.kt  — collecte UserStatsRepository + AuthRepository
ProfileScreen.kt     — ecran Compose avec les sections ci-dessus
```

### Integration coeur requise (via PR)
- `LexicaApp.kt` :
  - Ajouter `Screen.Profile` dans `sealed class Screen`
  - Ajouter `composable("profile") { ProfileScreen(...) }`
  - Ajouter action droite dans `LexicaTopAppBar` quand `currentRoute == Dashboard`
- `DashboardScreen.kt` : recevoir `onNavigateToProfile: () -> Unit`

### Inspiration open source
- **Now in Android** (Google) : `github.com/android/nowinandroid` → ecran Settings avec profil en top bar
- **Tivi** (Chris Banes) : `github.com/chrisbanes/tivi` → `AccountUiScreen.kt` — profil lateral Material3
- **Jetpack Compose Samples** : `github.com/android/compose-samples` → `Jetchat` — icone circulaire dans TopAppBar

### Contraintes
- Ne pas modifier `LexicaApp.kt`, `LexicaTopAppBar.kt`, `DashboardScreen.kt` directement
- Tout passer par `integration_pending/profile_pr.md`
- Utiliser `XpProgressBar.kt` et `GamificationViewModel` existants (ne pas recoder)
- **Fichier integration attendu:** `integration_pending/profile_pr.md`

---

## TACHE_09 - Daily Challenge ✅ INTEGREE
- **Scope estime:** ~45 000 tokens
- **Package isole:** `presentation/dailychallenge/` ← agent travaille UNIQUEMENT ici
- **Livrables:**
  - `DailyChallengeViewModel.kt`
  - `DailyChallengeScreen.kt`
  - Logique : 1 jeu impose par jour (rotation des jeux existants), detection si deja joue aujourd'hui (timestamp Room), bonus XP streak (+20 XP), affichage du prochain challenge avec countdown
- **Contraintes:** Necessite un champ `lastChallengeDate` — passer par `integration_pending/` pour l'ajout Room. Ne pas modifier les fichiers coeur.
- **Fichier integration attendu:** `integration_pending/daily_challenge_pr.md`

---

## TACHE_O1 - Alléger `MainActivity` avec un AppContainer manuel
- **Scope estime:** ~55 000 tokens
- **Packages isoles:**
  - `app/src/main/java/com/example/lexicaandroid2/di/`
  - factories/ViewModels existants uniquement si nécessaire
- **Objectif :** réduire le rôle de `MainActivity.kt` en extrayant l'assemblage des dépendances vers un conteneur manuel simple, sans introduire Hilt pour l'instant.

### Livrables
- Créer `AppContainer` ou `DefaultAppContainer` qui centralise :
  - création de `LexicaDatabase`
  - création des repositories
  - exposition des dépendances nécessaires aux `ViewModelFactory`
- `MainActivity.kt` ne doit plus contenir toute la logique de création de la base, des repos et des services
- Conserver le comportement actuel strictement identique
- Préparer le terrain pour une migration future vers Hilt sans casser l'existant

### Contraintes
- Ne pas introduire de nouvelle librairie DI
- Ne pas changer les routes/navigation
- Si une adaptation globale de factories est requise, la documenter dans `integration_pending/app_container_pr.md`
- **Fichier integration attendu:** `integration_pending/app_container_pr.md`

---

## TACHE_O2 - Réduire la taille de l'application en release
- **Scope estime:** ~40 000 tokens
- **Zones code:**
  - `app/build.gradle.kts`
  - `app/proguard-rules.pro`
  - audit ciblé des dépendances et ressources utilisées
- **Objectif :** analyser puis réduire la taille de l'APK/AAB de release sans régression fonctionnelle.

### Diagnostic attendu
- Mesurer la taille `debug` vs `release`
- Vérifier l'impact de :
  - `isMinifyEnabled = false`
  - absence de `isShrinkResources = true`
  - dépendances lourdes (`tensorflow-lite`, Firebase, icônes étendues Compose)
- Identifier ce qui est réellement utilisé vs embarqué inutilement

### Livrables
- Activer et stabiliser l'optimisation `release` si compatible
- Ajouter ou ajuster les règles ProGuard/R8 minimales nécessaires
- Produire une recommandation claire : garder / retirer / rendre optionnelles certaines dépendances lourdes
- Vérifier que la build `release` assemble correctement

### Contraintes
- Ne retirer aucune dépendance métier sans preuve qu'elle est inutilisée ou remplaçable
- Priorité à la sécurité du build, pas à l'optimisation agressive
- **Fichier integration attendu:** `integration_pending/release_size_pr.md`

---

## TACHE_O3 - Fiabiliser la couche data et la gestion d'erreurs
- **Scope estime:** ~60 000 tokens
- **Packages isoles:**
  - `data/`
  - `domain/`
  - ViewModels ciblés si nécessaire
- **Objectif :** uniformiser la remontée d'erreurs et éviter les crashes silencieux ou états incohérents.

### Livrables
- Audit des `viewModelScope.launch` sans `try/catch`
- Définir un pattern simple et réutilisable pour les erreurs (`Result`, `sealed class UiResult`, ou équivalent léger)
- Appliquer ce pattern au minimum sur les flux les plus critiques : review, auth, sync, search
- Les écrans concernés doivent pouvoir afficher un état erreur explicite ou un snackbar cohérent

### Contraintes
- Changements minimaux, incrémentaux
- Ne pas réécrire toute l'architecture
- Si une adaptation transversale est requise, la documenter dans `integration_pending/error_handling_pr.md`
- **Fichier integration attendu:** `integration_pending/error_handling_pr.md`

---

## TACHE_S2 - Ameliorer le Moteur de Recherche (API + robustesse) ✅ TERMINEE
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

## TACHE_10 - Recoder QCM + Correspondance + Pendu (bugs critiques) ⚠️ PARTIELLEMENT TRAITÉE
- **Scope estime:** ~60 000 tokens
- **Packages touches:**
  - `presentation/games/qcm/` — `QcmViewModel.kt` + `QcmScreen.kt`
  - `presentation/games/matching/` — `MatchingViewModel.kt` + `MatchingScreen.kt`
  - `presentation/games/hangman/` — `HangmanViewModel.kt` + `HangmanScreen.kt`
- **Fichier integration attendu:** `integration_pending/games_fix_pr.md`

### Audit des bugs (detectes le 2026-03-09)

---

#### QCM — BUG BLOQUANT (jeu completement fige)

Le jeu est injouable tel quel. Voici le Catch-22 :
- Le bouton "Suivant" n'apparait que si `uiState.answered == true`
- `answered` n'est mis a `true` que via `validateAndNext()`
- `validateAndNext()` n'est accessible que via le bouton "Suivant"
- → **Le bouton "Suivant" n'apparait jamais. Le joueur ne peut jamais avancer.**

**Correction attendue :**
1. Ajouter un `fun validateAnswer()` dans `QcmViewModel` qui :
   - Verifie que `selectedAnswer != null`
   - Calcule `isCorrect = selectedAnswer == currentQuestion.definition`
   - Met a jour `score` et `answered = true`
   - N'avance PAS encore a la question suivante
2. Modifier `validateAndNext()` pour qu'il ne fasse que charger la question suivante (sans recalculer le score)
3. Dans `QcmScreen` :
   - Quand `!answered` et `selectedAnswer != null` → afficher bouton **"Valider"** → appelle `validateAnswer()`
   - Quand `answered` → afficher le feedback couleur (vert/rouge) + bouton **"Suivant"** → appelle `validateAndNext()`
4. Bonus UX : afficher distinctement la bonne reponse en vert meme si le joueur a repondu faux

---

#### Correspondance (Matching) — BUGS UX

**Bug 1 — Ordre de selection non indique :**
- `selectDefinition()` ignore les clics si `selectedWord == null`
- Aucun message n'indique a l'utilisateur qu'il doit cliquer MOT en premier
- Correction : ajouter un texte indicatif ("Selectionnez d'abord un mot", "Maintenant choisissez la definition") qui change dynamiquement selon `selectedWord`

**Bug 2 — selectedDefinition non remis a null lors de la selection d'un mot :**
```kotlin
// BUG ACTUEL dans selectWord()
state.copy(selectedWord = wordId)
// manque : selectedDefinition = null
```
- Si un joueur clique definitionA (mauvais appariement) puis motB → la definitionA reste visuellement "selectionnee" alors que la nouvelle selection est motB
- Correction : dans `selectWord()`, ajouter `selectedDefinition = null`

**Bug 3 — Pas de feedback sur mauvais appariement :**
- Un mauvais appariement reset silencieusement les deux selections
- Correction : ajouter `wrongAttempt: Boolean = false` dans l'UiState + animation/couleur rouge breve (1 seconde) sur la mauvaise selection avant reset

---

#### Pendu — 3 BUGS CRITIQUES (dont 2 rendent le jeu ingagnable)

**Bug 1 — Lettres accentuees dans les mots (INGAGNABLE) :**
```kotlin
// CODE ACTUEL
wordsList = flashcards.map { it.recto.uppercase() }
// → "école" devient "ÉCOLE" avec la lettre 'É'
// Le clavier ne propose que 'A'..'Z' sans accents
// → les mots avec accents ne peuvent JAMAIS etre gagnes
```
Correction : normaliser les mots avant de les afficher au pendu :
```kotlin
import java.text.Normalizer
fun normalizeForHangman(word: String): String =
    Normalizer.normalize(word.uppercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "")   // supprime les diacritiques
        .filter { it in 'A'..'Z' || it == ' ' || it == '-' }
wordsList = flashcards.map { normalizeForHangman(it.recto) }
```

**Bug 2 — Espaces et tirets dans les mots (INGAGNABLE) :**
```kotlin
// CODE ACTUEL
val won = word.all { newGuessed.contains(it) }
// → si word = "HORS JEU", le caractere ' ' ne sera jamais dans guessedLetters
// → word.all { ... } retourne toujours false → jeu ingagnable
```
Correction : ignorer les caracteres non-lettres dans le test de victoire :
```kotlin
val won = word.filter { it in 'A'..'Z' }.all { newGuessed.contains(it) }
```
Et afficher les espaces/tirets directement dans le mot sans les cacher :
```kotlin
text = if (letter in 'A'..'Z') {
    if (uiState.guessedLetters.contains(letter)) letter.toString() else "_"
} else {
    letter.toString() // espaces et tirets toujours visibles
}
```

**Bug 3 — Perdre 1 mot = game over total :**
- Si le joueur n'a plus de vies sur le premier mot, `gameOver = true` et toute la partie se termine
- Le design prevu est : 5 mots, le joueur passe au suivant meme s'il perd (et perd 0 point pour ce mot)
- Correction : quand `lives <= 0`, ne pas mettre `gameOver = true` directement — incrementer `currentWordIndex` et appeler `loadNextWord()`. Mettre `gameOver = true` seulement quand tous les mots sont epuises.
- Ajouter `wordsLost: Int` dans l'UiState pour tracker les echecs et les afficher en fin de partie

---

### Livrables attendus dans integration_pending/games_fix_pr.md
- `QcmViewModel.kt` rececode (scission validate / next)
- `QcmScreen.kt` recoded (bouton Valider + feedback visuel)
- `MatchingViewModel.kt` recoded (reset selectedDefinition + wrongAttempt)
- `MatchingScreen.kt` recoded (texte indicatif + feedback erreur)
- `HangmanViewModel.kt` recoded (normalisation accents + espaces + mort partielle)
- `HangmanScreen.kt` recoded (affichage espaces/tirets)

### Inspiration open source
- **Quizlet-like Flashcard apps** : `github.com/nicholaschiang/quizlet` — patterns QCM, feedback vert/rouge apres validation
- **Hangman Compose** : `github.com/yusufcakal/HangmanGame` — gestion alphabet complet + normalisation, licence MIT
- **Word Game Android** : `github.com/SimformSolutionsPvtLtd/SSComposeCookBook` — mini-jeux Compose avec animations de feedback

### Contraintes
- Travailler dans les fichiers existants (ne pas creer de nouveaux packages)
- Ne pas modifier `LexicaApp.kt`, `MiniGamesScreen.kt`, ni aucun fichier hors de `presentation/games/`
- Garder la signature des composables `(repository, onBack, onAwardXp)` intacte
- Tous les changements passent par `integration_pending/games_fix_pr.md`

### État réel constaté
- ✅ **Matching partiellement refondu** pendant les sessions récentes :
  - passage à une validation globale des associations
  - écrans succès / échec dédiés
  - bouton `Accueil` relié au dashboard
- ❌ **Rendu graphique avancé du matching** encore à faire (liaison visuelle par trait ou repositionnement)
- ❌ **QCM** pas confirmé comme totalement recodé selon ce cahier de correction
- ❌ **Pendu** pas confirmé comme totalement recodé selon ce cahier de correction

---

---

## TACHE_11 - Nom et icône de l'application (Lexica) ⚠️ PARTIELLEMENT TRAITÉE
- **Scope estimé :** ~10 000 tokens
- **Package isolé :** `res/values/`, `res/drawable/`, `res/mipmap-*` — aucun fichier Kotlin à modifier
- **Fichier intégration attendu :** `integration_pending/app_icon_pr.md`

### Objectif
Changer le nom affiché de l'app (actuellement "LexicaAndroid2") en **"Lexica"**, et créer une icône d'application soignée au format Adaptive Icon Android.

### Changement du nom
Dans `app/src/main/res/values/strings.xml` :
```xml
<string name="app_name">Lexica</string>
```
Vérifier que `AndroidManifest.xml` utilise bien `@string/app_name` (ne pas modifier le manifest directement — passer par PR si nécessaire).

### Icône — contraintes techniques
- Android Adaptive Icon = 2 layers : `ic_launcher_background.xml` (couleur/fond) + `ic_launcher_foreground.xml` (motif SVG)
- Fichiers à créer/remplacer :
  - `res/drawable/ic_launcher_background.xml` — fond uni couleur primaire `#6750A4`
  - `res/drawable/ic_launcher_foreground.xml` — motif SVG vectoriel
  - `res/mipmap-anydpi-v26/ic_launcher.xml` — déclaration adaptive
  - `res/mipmap-anydpi-v26/ic_launcher_round.xml` — déclaration adaptive ronde
- Le motif foreground doit être blanc sur fond transparent, taille safe zone 66dp centré dans 108dp

### Design de l'icône (à implémenter)
Concept : **Lettre "L" stylisée formant une page de livre ouverte**
- Lettre "L" en blanc, police bold arrondie, légèrement inclinée à -5°
- Un petit trait fin sous la lettre simulant une ligne de texte
- Fond : dégradé violet `#6750A4` → `#9C72F0`
- Alternative : les lettres "Lx" en monogramme blanc sur fond violet

Implémenter en SVG vectoriel Android (`<vector>` XML) — pas de PNG, pas de bitmap.

### Inspiration open source
- **Jetpack Compose Adaptive Icons** : `github.com/nickbutcher/plaid` — exemples `ic_launcher_*` vectoriels Material3
- **Android Asset Studio** patterns : foreground centré à 66% de la taille totale
- Outil de référence pour les proportions : `romannurik.github.io/AndroidAssetStudio/icons-launcher.html`

### Contraintes
- ❌ Ne pas modifier `AndroidManifest.xml` ni `build.gradle.kts` directement — passer par PR
- ❌ Ne pas créer de PNG (bitmaps) — uniquement SVG vectoriels XML
- ✅ Tester visuellement : le foreground doit être lisible en rond ET en carré

### État réel constaté
- ✅ Nom affiché de l'application aligné sur **Lexica**
- ✅ Icône adaptive déjà présente et intégrée
- ⚠️ Une **refonte graphique supplémentaire** de l'icône est encore souhaitée / demandée

---

## TACHE_12 - Liste de mots : supprimer, favoris, et détail complet ✅ TERMINÉE
- **Scope estimé :** ~50 000 tokens
- **Packages touchés :**
  - `presentation/wordlist/` — `WordListScreen.kt` + `WordListViewModel.kt` (modification)
  - `presentation/wordlist/WordDetailScreen.kt` — nuevo fichier à créer
- **Fichier intégration attendu :** `integration_pending/wordlist_actions_pr.md`

### Contexte (lire avant de coder)
**Tout est déjà en place côté données :**
- `FlashcardEntity` a les champs : `mot`, `definition`, `synonymes` (List), `exemples` (List), `categorieGrammaticale`, `registre`, `etymologie`, `notesPersonnelles`, `favori` (Boolean)
- `FlashcardRepositoryImpl` a déjà : `deleteCard(cardId: String)` et `setFavorite(cardId, isFavorite)`
- Il manque **uniquement** le câblage UI → ViewModel → Repository

### Sous-tâche 1 — Actions rapides sur chaque item de la liste

Dans `WordListScreen.kt`, chaque `WordItem` doit afficher à droite deux icônes :
```
[mot - définition courte]    [⭐ favori]  [🗑 supprimer]
```
- **Icône favori** : `Icons.Default.Star` (rempli si `card.favori`, outline sinon) — couleur `Color(0xFFFFB800)` si actif
- **Icône supprimer** : `Icons.Default.Delete` — couleur `MaterialTheme.colorScheme.error` — déclenche une **AlertDialog de confirmation** ("Supprimer ce mot ? Cette action est irréversible.")
- Ne jamais supprimer sans confirmation utilisateur

Dans `WordListViewModel.kt`, ajouter :
```kotlin
fun toggleFavorite(card: Flashcard) {
    viewModelScope.launch {
        repository.setFavorite(card.id, !card.favori)
        loadWords() // recharger après modification
    }
}

fun deleteCard(cardId: String) {
    viewModelScope.launch {
        repository.deleteCard(cardId)
        loadWords()
    }
}
```
Vérifier la signature exacte de `deleteCard` et `setFavorite` dans `FlashcardRepositoryImpl.kt` avant de coder.

### Sous-tâche 2 — Cliquer sur un mot → écran détail complet

Quand l'utilisateur clique sur la zone principale d'un item (hors icônes), naviguer vers `WordDetailScreen` avec l'ID de la carte.

**Créer `WordDetailScreen.kt`** dans `presentation/wordlist/` :
- Reçoit `cardId: String` + `repository: FlashcardRepository` + `onBack: () -> Unit`
- Layout vertical scrollable avec sections :

```
┌─────────────────────────────────────────┐
│  ← Retour                               │  ← TopAppBar
├─────────────────────────────────────────┤
│  MOT               [⭐ Favori toggle]   │  ← Grande police, titre
│  Catégorie : nom   Registre : courant   │  ← Chips ou petits labels
├─────────────────────────────────────────┤
│  📖 Définition                          │
│  Texte complet de la définition         │
├─────────────────────────────────────────┤
│  💡 Exemples d'usage                    │
│  • exemple 1                            │
│  • exemple 2                            │
├─────────────────────────────────────────┤
│  🔗 Synonymes                           │
│  [syn1]  [syn2]  [syn3]   ← FilterChip │
├─────────────────────────────────────────┤
│  🌿 Étymologie                          │
│  Texte étymologie                       │
├─────────────────────────────────────────┤
│  📊 Progression                         │
│  État : En cours  Révisions : 12        │
│  Prochaine révision : dans 3 jours      │
└─────────────────────────────────────────┘
```

- Si un champ est vide (`""`), masquer la section entière (`if (card.etymologie.isNotBlank()) { ... }`)
- Ne pas afficher une section vide avec juste le titre

### Intégration globale (via PR)
Dans `LexicaApp.kt` :
- Ajouter `Screen.WordDetail(cardId: String)` dans `sealed class Screen`
- Ajouter `composable("word/{cardId}") { ... }` avec `backStackEntry.arguments?.getString("cardId")`
- `WordListScreen` passe `onNavigateToWordDetail: (cardId: String) -> Unit` en paramètre

→ Documenter tout dans `integration_pending/wordlist_actions_pr.md`

### Inspiration open source
- **AnkiDroid** : `github.com/ankidroid/Anki-Android` → écran de détail de carte, layout propre pour contenu riche
- **Duolingo Compose clone** : `github.com/yojan/duolingo-compose-clone` → affichage de définitions avec chips de synonymes

### Contraintes
- ❌ Ne PAS modifier `LexicaApp.kt` directement — passer par `integration_pending/wordlist_actions_pr.md`
- ✅ Le toggle favori dans WordDetailScreen doit aussi fonctionner (pas seulement dans la liste)
- ✅ La suppression depuis WordDetailScreen doit naviguer automatiquement vers la liste après confirmation

### État réel constaté
- ✅ Actions rapides favoris / suppression intégrées dans la liste
- ✅ Détail de mot complet implémenté
- ✅ Évolution UX réalisée : ouverture du détail en **popup** au lieu d'un écran séparé

---

## TACHE_13 - Statistiques Anki-like dans l'onglet Profil ✅ TERMINÉE
- **Scope estimé :** ~55 000 tokens
- **Packages touchés :**
  - `features/gamification/data/` — nouvelle entité Room `DailyReviewStat`
  - `presentation/profile/` — `ProfileViewModel.kt` + `ProfileScreen.kt` (modifications)
  - `presentation/review/ReviewViewModel.kt` — hook pour incrémenter les stats daily
- **Fichier intégration attendu :** `integration_pending/profile_stats_pr.md`

### Contexte
`UserStatsEntity` actuel ne stocke que : `xp, level, streak, lastLoginDate`.
Il faut tracer les révisions quotidiennes pour afficher des statistiques type Anki.

### Sous-tâche 1 — Nouvelle entité Room : DailyReviewStat

Créer `features/gamification/data/DailyReviewStat.kt` :
```kotlin
@Entity(tableName = "daily_review_stats")
data class DailyReviewStat(
    @PrimaryKey val dateKey: String, // Format "YYYY-MM-DD"
    val cardsReviewed: Int = 0,
    val correctAnswers: Int = 0,
    val totalTimeSeconds: Int = 0
)
```

Créer `features/gamification/data/DailyReviewStatDao.kt` :
```kotlin
@Dao
interface DailyReviewStatDao {
    @Query("SELECT * FROM daily_review_stats ORDER BY dateKey DESC LIMIT 30")
    fun getLast30Days(): Flow<List<DailyReviewStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stat: DailyReviewStat)

    @Query("SELECT * FROM daily_review_stats WHERE dateKey = :date")
    suspend fun getByDate(date: String): DailyReviewStat?
}
```

Passer par PR pour l'ajout dans `LexicaDatabase.kt` (migration Room version +1).

### Sous-tâche 2 — Hook dans ReviewViewModel

Dans `ReviewViewModel.kt`, après chaque réponse validée, appeler :
```kotlin
// Incrémenter DailyReviewStat pour la date du jour
val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
val existing = dailyStatDao.getByDate(today) ?: DailyReviewStat(today)
dailyStatDao.upsert(existing.copy(
    cardsReviewed = existing.cardsReviewed + 1,
    correctAnswers = existing.correctAnswers + (if (isCorrect) 1 else 0)
))
```
Injecter `DailyReviewStatDao` dans `ReviewViewModel` via PR (`integration_pending/profile_stats_pr.md`).

### Sous-tâche 3 — Affichage dans ProfileScreen

Ajouter dans `ProfileViewModel.kt` la collecte des stats des 7 derniers jours.

Ajouter dans `ProfileScreen.kt` une section **"📊 Mes statistiques"** avec :

| Stat | Source | Affichage |
|------|--------|-----------|
| Cartes vues aujourd'hui | `DailyReviewStat` du jour | Grand chiffre centré |
| Série actuelle | `userStats.streak` | Déjà présent |
| Taux de réussite (7j) | Somme correct/total sur 7j | Pourcentage avec couleur |
| Graphique 7 jours | `getLast30Days().take(7)` | Barres verticales simples (pas de lib externe) |
| Total cartes apprises | `userStats.xp`-based ou count KNOWN | Chiffre |
| Meilleure série | Calculé depuis les 30 derniers jours | Chiffre |

**Graphique barres (implémenter sans lib externe) :**
```kotlin
// Barres verticales simples avec Canvas ou Box + height proportionnel
Row(modifier = Modifier.fillMaxWidth().height(80.dp)) {
    last7Days.forEach { day ->
        val heightFraction = day.cardsReviewed.toFloat() / maxCards
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxHeight(heightFraction)
            .background(Color(0xFF6750A4), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            .align(Alignment.Bottom))
    }
}
```

### Inspiration open source
- **AnkiDroid stats** : `github.com/ankidroid/Anki-Android` → `statistics/` package — heatmap et graphes de révision
- **Habitica** : `github.com/HabitRPG/habitica-android` → graphe barres simple sans lib externe, code clair
- **Now in Android** : `github.com/android/nowinandroid` → pattern de collecte de stats quotidiennes dans ViewModel

### Contraintes
- ❌ Ne PAS utiliser de lib de graphes externe (MPAndroidChart, etc.) — implémenter les barres avec `Box` ou `Canvas` Compose natif
- ❌ Ne PAS modifier `LexicaDatabase.kt`, `ReviewViewModel.kt`, `LexicaApp.kt` directement — tout via `integration_pending/profile_stats_pr.md`
- ✅ Si aucune donnée dans `DailyReviewStat` → afficher 0 partout sans crash
- ✅ Utiliser `today.cardsReviewed == 0` → afficher "Aucune révision aujourd'hui — c'est le moment ! 🚀"

### État réel constaté
- ✅ Entité `DailyReviewStat` + DAO intégrés
- ✅ Hook review → stats quotidiennes intégré
- ✅ Affichage profil type Anki présent avec barres / série / taux

---

## TACHE_14 - Défis intégrés dans la révision (Orthographique + Sémantique) ✅ TERMINÉE
- **Scope estimé :** ~80 000 tokens
- **Packages touchés :**
  - `presentation/review/` — `ReviewViewModel.kt` + `ReviewScreen.kt` (modifications)
  - `presentation/review/challenge/` — nouveaux fichiers à créer ici
- **Fichier intégration attendu :** `integration_pending/review_challenges_pr.md`

### ⚠️ Ce n'est PAS la même chose que les mini-jeux existants
- `SemanticScreen.kt` = mini-jeu standalone "Associations Sémantiques" (QCM 4 choix) — **à ne pas toucher**
- `SpellingAdvancedScreen.kt` = mini-jeu standalone avec TTS — **à ne pas toucher**
- Ce qui est demandé ici = **défis déclenchés automatiquement DANS le flux de révision normal**

---

### Mécanique de déclenchement

Dans le flux de révision (`ReviewViewModel.gradeCard()`), après chaque "je l'ai" (quality >= 3) :
- Incrémenter un compteur `challengeStreak` par face de carte (`sm2MotVersDef.correctReviews` est déjà disponible)
- Si `correctReviews % 3 == 0 && correctReviews > 0` → déclencher un défi sur cette face

**Type de défi selon la face révisée :**
| Face affichée | Réponse normale | Défi déclenché |
|--------------|-----------------|----------------|
| Définition → trouver le mot | "Je l'ai / Pas encore" | **Défi Orthographique** (taper le mot) |
| Mot → trouver la définition | "Je l'ai / Pas encore" | **Défi Sémantique** (taper la définition) |

Le challenge remplace TEMPORAIREMENT les boutons habituels pour cette seule carte. Une fois validé ou abandonné, le flux reprend normalement.

---

### Défi Orthographique (ChallengeType.SPELLING)

**Contexte affiché :** la définition de la carte (face que l'utilisateur vient de retourner)
**Action demandée :** l'utilisateur tape le mot (le `recto` de la carte)

**UI :**
```
┌────────────────────────────────────────┐
│  🏆 Défi orthographique !              │
│  Écris ce mot sans regarder :          │
│                                        │
│  [Définition complète affichée ici]    │
│                                        │
│  ┌──────────────────────────────────┐  │
│  │  Tape le mot...                  │  │
│  └──────────────────────────────────┘  │
│                                        │
│  [✅ Valider]      [🚪 Abandonner]     │
└────────────────────────────────────────┘
```

**Validation :**
```kotlin
fun validateSpellingChallenge(userInput: String, expectedWord: String): Boolean {
    return userInput.trim().lowercase() == expectedWord.trim().lowercase()
}
```
- Insensible à la casse uniquement
- Accents comptent (si l'utilisateur rate l'accent, c'est faux — c'est le but du défi)

**Résultat :**
- Succès : +10 XP bonus, message de félicitations, puis passer à la carte suivante
- Échec : afficher la bonne orthographe en rouge, puis passer à la carte suivante (pas de pénalité SM2)
- Abandon : passer à la carte suivante sans XP ni pénalité

---

### Défi Sémantique (ChallengeType.SEMANTIC)

**Contexte affiché :** le mot (le `recto` de la carte)
**Action demandée :** l'utilisateur tape la définition librement

**UI :** même layout que le défi orthographique, mais zone de texte multiligne (`maxLines = 5`)

**Validation sémantique — architecture en 2 couches combinées (OBLIGATOIRE) :**

La validation combine **deux algorithmes complémentaires** : Jaccard pour les mots-clés + TFLite MiniLM pour le sens global. Les deux doivent être implémentés.

---

#### Couche 1 — Détection des mots-clés (Jaccard TF-IDF simplifié) — TOUJOURS ACTIF

L'idée : extraire les mots "importants" de la définition attendue (ceux qui portent le sens), puis vérifier combien l'utilisateur en a mentionnés.

```kotlin
object KeywordExtractor {
    private val STOPWORDS_FR = setOf(
        "le", "la", "les", "de", "du", "des", "un", "une", "et", "ou",
        "est", "qui", "que", "dans", "sur", "avec", "pour", "par", "au",
        "aux", "ce", "se", "sa", "son", "ses", "en", "il", "elle", "on",
        "ne", "pas", "plus", "très", "aussi", "dont", "où", "car", "ni"
    )

    fun extractKeywords(text: String, topN: Int = 5): List<String> {
        val tokens = tokenize(text)
        // Trier par longueur décroissante (heuristique : les mots longs = plus spécifiques = plus importants)
        return tokens
            .filter { it.length >= 4 && it !in STOPWORDS_FR }
            .sortedByDescending { it.length }
            .take(topN)
    }

    fun tokenize(text: String): List<String> =
        Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .replace(Regex("[^a-z\\s]"), "")
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }

    fun jaccardScore(userInput: String, expectedDef: String): Float {
        val userTokens = tokenize(userInput).toSet()
        val defTokens = tokenize(expectedDef).filter { it !in STOPWORDS_FR }.toSet()
        if (defTokens.isEmpty()) return if (userTokens.isNotEmpty()) 1f else 0f
        val intersection = userTokens.intersect(defTokens).size
        val union = userTokens.union(defTokens).size
        return intersection.toFloat() / union.toFloat()
    }
}
```

**Résultat de la couche 1 :**
- `foundKeywords` : mots-clés importants présents dans la réponse utilisateur
- `missingKeywords` : mots-clés importants absents
- `keywordScore` : `foundKeywords.size / totalKeywords.size`

---

#### Couche 2 — Similarité sémantique (TFLite MiniLM) — TÉLÉCHARGÉ APRÈS INSTALLATION

Modèle : `paraphrase-multilingual-MiniLM-L12-v2` au format `.tflite` (~25MB)

**Téléchargement conditionnel au premier lancement du défi sémantique :**
```kotlin
// Dans SemanticValidator, avant la première inférence :
if (!modelFile.exists()) {
    showDownloadDialog() // "🧠 Téléchargement du modèle de compréhension (~25MB) pour activer cette fonctionnalité"
    downloadModel() // via URL directe ou Firebase ML Kit
}
```

**Architecture TFLite :**
```kotlin
class TFLiteSemanticValidator(context: Context) : SemanticValidator {
    private var interpreter: Interpreter? = null
    private val modelFile = File(context.filesDir, "minilm_multilingual.tflite")

    fun isModelReady(): Boolean = modelFile.exists()

    // Encode une phrase → vecteur Float32[384]
    private fun encode(sentence: String): FloatArray { /* tokenize + infer */ }

    // Cosine similarity entre deux vecteurs
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        val dot = a.zip(b).sumOf { (x, y) -> (x * y).toDouble() }
        val normA = sqrt(a.sumOf { (it * it).toDouble() })
        val normB = sqrt(b.sumOf { (it * it).toDouble() })
        return (dot / (normA * normB)).toFloat()
    }

    override fun validate(userInput: String, expected: String): ValidationResult {
        val similarity = cosineSimilarity(encode(userInput), encode(expected))
        return ValidationResult(isValid = similarity >= 0.65f, score = similarity, missingKeywords = emptyList())
    }
}
```

Dépendances à ajouter via PR dans `build.gradle.kts` :
```kotlin
implementation("org.tensorflow:tensorflow-lite:2.14.0")
implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
```

**Ressource modèle :** `https://huggingface.co/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2` → fichier `model.tflite` disponible directement.

---

#### Décision finale de validation (combinaison des 2 couches)

```kotlin
data class ValidationResult(
    val isValid: Boolean,
    val keywordScore: Float,          // score Jaccard mots-clés (0..1)
    val semanticScore: Float,         // cosine similarity TFLite (0..1, -1 si modèle absent)
    val foundKeywords: List<String>,
    val missingKeywords: List<String>
)

fun validate(userInput: String, expected: String): ValidationResult {
    val keywords = KeywordExtractor.extractKeywords(expected, topN = 5)
    val userTokens = KeywordExtractor.tokenize(userInput).toSet()
    val found = keywords.filter { it in userTokens }
    val missing = keywords - found.toSet()
    val keywordScore = if (keywords.isEmpty()) 1f else found.size.toFloat() / keywords.size

    val semanticScore = if (tfliteValidator.isModelReady())
        tfliteValidator.cosineSimilarity(encode(userInput), encode(expected))
    else -1f // modèle non disponible → fallback sur Jaccard seul

    // Règle de validation :
    // Si modèle dispo : (semanticScore >= 0.65) OU (keywordScore >= 0.6)
    // Si modèle absent : keywordScore >= 0.5
    val isValid = if (semanticScore >= 0f)
        (semanticScore >= 0.65f || keywordScore >= 0.6f)
    else
        keywordScore >= 0.5f

    return ValidationResult(isValid, keywordScore, semanticScore, found, missing)
}
```

**Feedback affiché selon le résultat :**
- Succès : `"✅ Bonne définition ! Mots-clés trouvés : ${found.joinToString(", ")}"` → +15 XP
- Échec proche (`keywordScore > 0.3`) : `"💡 Presque ! Il manquait : ${missing.joinToString(", ")}"` → +5 XP encouragement
- Échec loin : `"❌ Mots-clés manquants : ${missing.joinToString(", ")}"` + définition correcte → 0 XP
- Abandon : 0 XP, aucune pénalité

---

#### Interface commune (obligatoire pour la modularité)

```kotlin
interface SemanticValidator {
    fun validate(userInput: String, expected: String): ValidationResult
    fun isModelReady(): Boolean
}
// Deux implémentations : JaccardOnlyValidator (fallback) et TFLiteSemanticValidator (full)
```

**Résultat :**
- Succès : +15 XP bonus, feedback avec mots-clés trouvés
- Échec proche : +5 XP, mots-clés manquants indiqués
- Échec total : 0 XP, définition correcte affichée
- Abandon : 0 XP, pas de pénalité

---

### Architecture dans ReviewViewModel

Ajouter dans `ReviewUiState` :
```kotlin
val activeChallengeType: ChallengeType? = null, // null = pas de défi en cours
val challengeInput: String = "",
val challengeResult: ChallengeResult? = null   // null = pas encore validé
```

```kotlin
enum class ChallengeType { SPELLING, SEMANTIC }
data class ChallengeResult(val isSuccess: Boolean, val xpBonus: Int, val feedbackMessage: String, val correctAnswer: String)
```

Nouvelles fonctions dans `ReviewViewModel` :
- `fun onChallengeInputChanged(text: String)` — met à jour `challengeInput`
- `fun validateChallenge()` — lance la validation selon le type, met à jour `challengeResult`
- `fun dismissChallenge()` — abandon, passe à la carte suivante

**Déclenchement dans `gradeCard()` :**
```kotlin
// Après avoir mis à jour SM2 :
val newCorrectReviews = updatedMotVersDef.correctReviews
if (newCorrectReviews > 0 && newCorrectReviews % 3 == 0) {
    val challengeType = if (currentFace == CardFace.DEFINITION_TO_WORD) ChallengeType.SPELLING
                        else ChallengeType.SEMANTIC
    _uiState.update { it.copy(activeChallengeType = challengeType) }
    return@launch // ne pas avancer à la prochaine carte — attendre le défi
}
// ... sinon avancer normalement
```

### Intégration globale (via PR)
- `ReviewViewModel.kt` : ajouter les champs UiState + fonctions challenge
- `ReviewScreen.kt` : si `uiState.activeChallengeType != null` → afficher `ChallengeOverlay` au lieu des boutons normaux
- `ChallengeOverlay.kt` : nouveau composable dans `presentation/review/challenge/`

### Contraintes
- ❌ Ne PAS modifier `SemanticScreen.kt` ou `SpellingAdvancedScreen.kt` (ce sont des mini-jeux indépendants)
- ❌ Ne PAS modifier `LexicaApp.kt` directement — passer par `integration_pending/review_challenges_pr.md`
- ✅ V1 = validation Kotlin pur (Niveau 1) — l'interface `SemanticValidator` doit permettre un swap en V2 sans refactoring
- ✅ Le challenge n'affecte PAS le score SM2 de la carte — seul un XP bonus est accordé en cas de succès
- ✅ Un abandon ou une erreur ne pénalise PAS la carte

### Inspiration open source
- **AnkiDroid Active Recall** : `github.com/ankidroid/Anki-Android` → `TypeAnswer` feature — saisie ouverte dans la révision, UX de référence
- **Brainscape** patterns : input inline dans une flashcard, feedback immédiat
- **TFLite sentence similarity Android** : `github.com/tensorflow/examples/tree/master/lite/examples/text_classification` — intégration TFLite pour NLP sur Android (pour future V2)

---

## TACHE_14b - Intégration TFLite MiniLM pour validation sémantique
- **Scope estimé :** ~60 000 tokens
- **Statut :** ✅ Terminée — intégrée
- **Packages touchés :**
  - `presentation/review/challenge/` — `SemanticValidator.kt` (modification), nouveau `TFLiteSemanticValidator.kt`
  - `app/src/main/assets/` — modèle `.tflite` à placer ici
  - `app/build.gradle.kts` — dépendances TensorFlow Lite à ajouter
- **Fichier intégration attendu :** `integration_pending/tflite_semantic_pr.md`

### Contexte
TACHE_14 est intégrée avec `JaccardSemanticValidator` (score Jaccard + mots-clés). La spécification initiale exigeait **Jaccard + TFLite MiniLM obligatoires**. Cette tâche complète cet aspect.

### Ce qui existe déjà
- `SemanticValidator.kt` — interface `SemanticValidator` + `JaccardSemanticValidator` (100% fonctionnel, produit des scores Jaccard)
- `KeywordExtractor.kt` — extraction de mots-clés FR avec stopwords
- `ReviewViewModel.kt` — utilise déjà `JaccardSemanticValidator`, structure en place pour plugger un second validateur

### Lire d'abord
- `presentation/review/challenge/SemanticValidator.kt` — interface `SemanticValidator` et implémentation Jaccard existante
- `presentation/review/challenge/KeywordExtractor.kt` — `jaccardScore()`, `analyzeKeywords()`
- `app/build.gradle.kts` — voir les dépendances actuelles avant d'ajouter

### Modèle à utiliser
**MiniLM-L6-v2** converti en TFLite (sentence-transformers) :
- Modèle source : `sentence-transformers/all-MiniLM-L6-v2` (Hugging Face)
- Taille cible après quantification int8 : ~6 MB
- Fichier à placer : `app/src/main/assets/minilm_l6_v2.tflite`
- Tokenizer : utiliser le vocabulaire BERT pré-tokenisé (fichier `vocab.txt` aussi dans assets)

### Dépendances à ajouter dans `app/build.gradle.kts`
```kotlin
implementation("org.tensorflow:tensorflow-lite:2.14.0")
implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
abiFilters += listOf("arm64-v8a", "x86_64") // dans android > defaultConfig > ndk
```

### Architecture cible

```kotlin
// Nouveau fichier : presentation/review/challenge/TFLiteSemanticValidator.kt
class TFLiteSemanticValidator(context: Context) : SemanticValidator {
    private var interpreter: Interpreter? = null
    private var isReady = false

    init {
        try {
            val model = FileUtil.loadMappedFile(context, "minilm_l6_v2.tflite")
            interpreter = Interpreter(model)
            isReady = true
        } catch (e: Exception) {
            // fallback silencieux sur Jaccard
        }
    }

    override fun isModelReady() = isReady

    override fun validate(userInput: String, expected: String): ValidationResult {
        if (!isReady) return JaccardSemanticValidator().validate(userInput, expected)
        // 1. Tokenizer BERT minimal (CharPiece)
        // 2. Inférence TFLite → embeddings 384-dim
        // 3. Cosine similarity entre les deux embeddings
        // 4. Fusionner score cosinus + score Jaccard existant
        //    combinedScore = 0.6 * cosineSimilarity + 0.4 * jaccardScore
        // 5. Seuils : >= 0.65 → +15 XP, >= 0.45 → +5 XP, sinon 0 XP
        ...
    }
}
```

### Modification de `ReviewViewModel.kt`
- Ajouter `tfliteValidator: SemanticValidator` en paramètre du constructeur (avec fallback `JaccardSemanticValidator()`)
- Dans `validateChallenge()`, pour `SEMANTIC` → utiliser `tfliteValidator.validate()` à la place de `JaccardSemanticValidator()`
- Passer le validator depuis `MainActivity` : `TFLiteSemanticValidator(applicationContext)`

### Initialisation dans `MainActivity.kt`
```kotlin
val semanticValidator = TFLiteSemanticValidator(applicationContext)
val reviewFactory = ReviewViewModelFactory(
    repository = repository,
    dailyStatDao = dailyReviewStatDao,
    semanticValidator = semanticValidator
)
```

### Contraintes
- Chargement du modèle sur un thread background (IO) — ne jamais faire d'I/O sur le main thread
- Si le modèle n'est pas disponible ou échoue → fallback transparent sur `JaccardSemanticValidator` (ne jamais crasher)
- Taille APK : model en `assets/` est inclus dans l'APK — vérifier la taille finale
- Pas de connexion réseau requise : le modèle est embarqué localement

---

- **Scope estimé :** ~40 000 tokens
- **Statut :** ✅ Terminée — intégrée
- **Package isolé :** `presentation/addwords/` — `AddWordsScreen.kt` + `AddWordsViewModel.kt` uniquement
- **Fichier intégration attendu :** `integration_pending/addwords_ux_pr.md`

### Objectif
Repenser l'expérience "Ajouter des mots" : aujourd'hui c'est probablement un formulaire simple. Il faut une barre de recherche intelligente qui guide l'utilisateur de la recherche jusqu'à l'enregistrement.

### Lire d'abord
Lire **intégralement** `presentation/addwords/AddWordsScreen.kt` et `AddWordsViewModel.kt` avant de coder quoi que ce soit — comprendre l'état actuel.

### Flux UX cible

```
[Barre de recherche : "Chercher un mot à ajouter..."]
         ↓ frappe "arbre"
[Résultats locaux : mots déjà dans ma liste qui contiennent "arbre"]
   → Si aucun résultat local : chercher automatiquement dans l'API externe
         ↓
[Résultats de l'API — section "🌐 Définitions trouvées"]
   [arbre (nom masculin) — Végétal ligneux...] [+ Ajouter]
   [arbre de vie (expr.) — Symbole de...]      [+ Ajouter]
         ↓ clic "+ Ajouter" sur un résultat
[Fiche d'aperçu avant sauvegarde]
   Mot : arbre
   Définition : Végétal ligneux...
   [Modifier la définition (optionnel)]
   [Ajouter des synonymes (optionnel)]
   [✅ Confirmer l'ajout]  [❌ Annuler]
         ↓ confirme
[Toast : "arbre ajouté à ta liste !"] → retour à la barre de recherche
```

### Comportement de la barre de recherche

**Étape 1 — Filtre local en temps réel**
- Dès 2 caractères saisis → montrer les mots déjà dans la liste qui matchent (pour éviter les doublons)
- Si un mot est déjà dans la liste → afficher un badge "✓ Déjà dans ta liste" avec lien vers le détail
- Pas de debounce ici car c'est juste un filtre en mémoire

**Étape 2 — Recherche API externe (si 0 résultat local après 500ms)**
- Déclencher la recherche API automatiquement (debounce 500ms)
- Indicateur de chargement pendant la recherche réseau
- En cas d'erreur réseau → message "Pas de connexion — tu peux ajouter le mot manuellement"
- API utilisée : déjà implémentée dans `DictionaryService` (vérifier avant de recoder)

**Étape 3 — Bouton "Ajouter manuellement"**
- Toujours visible en bas de page
- Ouvre un formulaire simple : Mot + Définition (champs minimaux obligatoires)
- Champs optionnels (accordéon déroulant) : Synonymes, Étymologie, Exemples, Catégorie grammaticale

### Gestion des doublons
- Avant tout ajout → vérifier `repository.getCardByWord(word)` (à créer si absent)
- Si doublon détecté → AlertDialog : "Ce mot est déjà dans ta liste. Mettre à jour la définition ?"
- Option : remplacer / fusionner / annuler

### État du ViewModel

```kotlin
data class AddWordsUiState(
    val searchQuery: String = "",
    val localMatches: List<Flashcard> = emptyList(),      // mots déjà en liste
    val apiResults: List<ExternalWord> = emptyList(),      // résultats API
    val isApiLoading: Boolean = false,
    val selectedResult: ExternalWord? = null,              // fiche d'aperçu ouverte
    val manualMode: Boolean = false,                       // formulaire manuel
    val successMessage: String? = null,
    val error: String? = null
)
```

### Contraintes
- ❌ Ne PAS modifier `LexicaApp.kt` directement — passer par `integration_pending/addwords_ux_pr.md`
- ❌ Ne PAS recoder `DictionaryService` s'il existe déjà — réutiliser
- ✅ La barre de recherche doit fonctionner **offline** (filtre local) même sans connexion
- ✅ Toujours proposer l'ajout manuel en fallback
- ✅ Texte de la définition API = pré-rempli mais éditable par l'utilisateur avant sauvegarde

### Inspiration open source
- **Google Keep "Add note"** flow — recherche + création en une seule vue
- **Merriam-Webster Android App** — UX de recherche de mot + fiche détaillée
- **AnkiDroid Add Note** : `github.com/ankidroid/Anki-Android` → `NoteEditor` — formulaire d'ajout de carte avec validation

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

---

## TACHE_16 - Banner téléchargement modèle IA au premier lancement ✅ TERMINÉE
- **Scope estimé :** ~15 000 tokens
- **Statut :** ✅ INTÉGRÉE
- **Packages touchés :**
  - `presentation/review/challenge/ModelDownloadUI.kt` (modification)
  - `presentation/LexicaApp.kt` (ajout banner + ModelDownloadViewModel)
  - `MainActivity.kt` (création ModelDownloadViewModel)
- **Fichier intégration attendu :** `integration_pending/model_download_banner_pr.md`

### Objectif
Au premier lancement (ou quand le modèle TFLite est absent), déclencher automatiquement le téléchargement en arrière-plan avec un banner non-bloquant. L’utilisateur peut utiliser l’app normalement pendant ce temps.

### Ce qui existe déjà
- `ModelDownloadManager` — vérifie le cache, télécharge avec progress callback
- `ModelDownloadViewModel` — `downloadModel()`, `uiState` avec `isDownloading`, `downloadProgress`, `errorMessage`
- `ModelDownloadDialog` — dialog modal (ne pas utiliser — trop bloquant)

### Ce qu’il faut faire

**1. Ajouter `checkAndAutoDownload()` dans `ModelDownloadViewModel`**
```kotlin
// Appelé au démarrage : vérifie si modèle absent, lance téléchargement silent
fun checkAndAutoDownload() {
    if (!modelManager.isModelCached()) downloadModel()
}
```

**2. Créer `ModelDownloadBanner` composable (non-bloquant)**
```kotlin
// Visible en bas du Scaffold seulement si isDownloading == true ou errorMessage != null
// Disparaît automatiquement quand isDownloadSuccess == true
@Composable
fun ModelDownloadBanner(uiState: ModelDownloadUiState, onDismissError: () -> Unit)
```
UI : fond sombre semi-transparent, icône 🧠, texte « Téléchargement IA... X% », `LinearProgressIndicator`, bouton « Ignorer » en cas d’erreur seulement.

**3. Intégrer dans `LexicaApp.kt`**
- Ajouter `modelDownloadViewModel: ModelDownloadViewModel? = null` en paramètre
- Collecter `uiState` depuis le ViewModel
- Dans le `Scaffold`, ajouter `bottomBar` conditionnel qui affiche `ModelDownloadBanner`
- `LaunchedEffect(Unit)` → appeler `modelDownloadViewModel?.checkAndAutoDownload()`

**4. Créer le ViewModel dans `MainActivity.kt`**
```kotlin
val modelManager = ModelDownloadManager(applicationContext)
val modelDownloadViewModel = ModelDownloadViewModel(modelManager)
```
Passer à `LexicaApp(..., modelDownloadViewModel = modelDownloadViewModel)`

### Comportement attendu
- Premier lancement, modèle absent → banner apparaît automatiquement en bas, téléchargement démarre
- L’utilisateur navigue librement (dashboard, révision, jeux)
- Téléchargement terminé → banner disparaît silencieusement
- Erreur réseau → banner affiche le message + bouton « Ignorer » (Mode Jaccard actif)
- Lancements suivants, modèle en cache → rien ne s’affiche

### Contraintes
- Le banner ne bloque JAMAIS la navigation
- Pas de dialog modal, pas de splash screen
- Toast ou Snackbar interdit (trop ephimère pour un téléchargement)
- Le mode Jaccard reste actif pendant tout le téléchargement — zéro dégradation UX

- Le Chef d'Orchestre joue le role de mainteneur (triage, validation, fusion).
- Les agents jouent le role de contributeurs (implementation isolee + proposition d'integration).
- Les changements globaux passent par revue et integration manuelle.

---

## TACHE_17 - Mode Administrateur (debug & tests en production) ✅ INTÉGRÉE
- **Scope estimé :** ~40 000 tokens
- **Statut :** ✅ INTÉGRÉE
- **Packages touchés :**
  - `presentation/admin/` ← créer ici : `AdminScreen.kt`, `AdminViewModel.kt`
  - `presentation/profile/ProfileScreen.kt` (ajout bouton admin conditionnel)
  - `presentation/review/ReviewViewModel.kt` (ajout paramètres de session overridables)
- **Fichier intégration attendu :** `integration_pending/admin_mode_pr.md`

### Contexte
Le propriétaire du projet (une adresse e-mail spécifique) doit pouvoir activer un mode admin depuis son profil pour tester les fonctionnalités sans les mêmes contraintes qu'un utilisateur normal.

### Comportement attendu

**1. Détection admin**
L'adresse e-mail de l'utilisateur connecté (via `AuthRepository.currentUser`) est comparée à une constante hardcodée :
```kotlin
object AdminConfig {
    val ADMIN_EMAILS = setOf("TON_EMAIL@gmail.com") // à renseigner
}
```
Si `currentUser?.email in AdminConfig.ADMIN_EMAILS` → l'utilisateur est admin.

**2. Bouton admin dans ProfileScreen**
Sur `ProfileScreen`, si l'utilisateur est admin, afficher un bouton `⚙️ Mode Admin` visible uniquement pour lui. Ce bouton navigue vers `Screen.Admin`.

**3. AdminScreen : panneaux de contrôle**

Section "Entrainement (Révision)" :
- Sélecteur de **type de carte** : `[✓] Vocabulaire (recto→verso)` / `[✓] Définition (verso→recto)` / `[✓] Les deux` — via `FilterChip` horizontal
- Sélecteur d'**activation des défis** : toggle pour défis orthographiques ON/OFF, toggle pour défis sémantiques ON/OFF
- Nombre de cartes par session : slider 5–50 (défaut 20)

Section "Jeux" :
- Override du nombre de questions par jeu (ex: QCM : slider 3–20, Memory : sélecteur grille 2×2 à 6×4)
- Bouton "Réinitialiser XP & Niveau" (pour tests de progression)

Section "Système" :
- Bouton "Effacer le cache des stats (DailyReviewStat)" — pour tester le graphe vide
- Bouton "Simuler un streak de 7 jours" — pour tester les achievements
- Bouton "Forcer téléchargement du modèle TFLite" — relance `ModelDownloadManager`

**4. Persistance des préférences admin**
Les préférences admin sont stockées dans `SharedPreferences` (pas Room — légèreté) avec des clés simples :
- `admin_review_mode` : `"both"` / `"vocab"` / `"definition"`
- `admin_challenge_ortho_enabled` : `Boolean`
- `admin_challenge_semantic_enabled` : `Boolean`
- `admin_session_size` : `Int`

**5. Impact sur ReviewViewModel**
`ReviewViewModel` lit les préférences admin (via un `AdminPrefsRepository` simple) au démarrage de session et filtre les cartes / active/désactive les défis en conséquence.

### Architecture
```
AdminConfig.kt          — constante email(s) admin
AdminPrefsRepository.kt — SharedPreferences wrappé
AdminViewModel.kt       — collecte + met à jour les prefs
AdminScreen.kt          — écran de contrôle complet
```

### Contraintes
- Le bouton admin est **invisible** pour tout utilisateur non admin (vérification côté client suffisante — pas de données sensibles)
- Ne pas modifier `ReviewScreen.kt` directement — tout passe par `ReviewViewModel`
- Ne pas stocker l'email en clair dans les fichiers commités → utiliser une constante dans `AdminConfig.kt` que le dev renseigne localement
- Fichier intégration attendu : `integration_pending/admin_mode_pr.md`

---

## TACHE_18 - Diagnostic & Activation Firebase Auth ✅ TERMINÉE
- **Scope estimé :** ~20 000 tokens
- **Statut :** ✅ INTÉGRÉE — routes Login/Register activées
- **Agent concerné :** DEV_AUTH

### Ce qui est déjà en place (NE PAS RECODER)
- `FirebaseAuthRepository.kt` — implémentation complète (`signIn`, `signUp`, `signOut`, `currentUser`)
- `AuthRepository.kt` — interface domain
- `AuthUser.kt` — modèle
- `LoginScreen.kt` + `LoginViewModel.kt` — écran de connexion codé
- `RegisterScreen.kt` + `RegisterViewModel.kt` — écran d'inscription codé
- `MainActivity.kt` — instancie `FirebaseAuthRepository()` et le passe à `LexicaApp`
- `ProfileScreen.kt` — utilise déjà `authRepository` (sign out, affichage user)
- `google-services.json` — présent dans `app/`
- `firebase-auth-ktx` — dans `build.gradle.kts` (BOM 32.8.1)

### Ce qui manque (identifié par le Chef d'Orchestre)
- `Screen.Login` et `Screen.Register` absents de `sealed class Screen` dans `LexicaApp.kt`
- Aucun `composable("login")` ni `composable("register")` dans le NavHost
- `onSignInRequested` dans `ProfileScreen` est un lambda vide `{}` (ne navigue nulle part)
- Pas de gestion de l'état "non connecté" au démarrage (l'app démarre toujours sur Dashboard)

### Travail demandé

**Phase 1 — Diagnostic (OBLIGATOIRE en premier)**
1. Vérifier que `google-services.json` correspond bien à un projet Firebase Console actif
2. Vérifier que Firebase Authentication est activé dans la console (méthode Email/Password)
3. Vérifier que les SHA-1/SHA-256 du keystore debug Android sont enregistrés dans Firebase
4. Tenter un `FirebaseAuth.getInstance().currentUser` et vérifier qu'il ne crash pas
5. Documenter les résultats dans `integration_pending/firebase_diagnostic_pr.md`

**Phase 2 — Activation navigation (si diagnostic OK)**
1. Ajouter `Screen.Login` et `Screen.Register` dans `sealed class Screen`
2. Ajouter les composables dans le NavHost de `LexicaApp.kt`
3. Brancher `onSignInRequested = { navController.navigate(Screen.Login.route) }` dans ProfileScreen
4. Optionnel : rediriger vers Login si non connecté au démarrage (configurable)

### Contraintes
- Ne pas modifier `FirebaseAuthRepository.kt` — il est correct
- Ne pas recoder `LoginScreen.kt` + `RegisterScreen.kt` — ils existent déjà
- Fichier intégration attendu : `integration_pending/firebase_diagnostic_pr.md`

---

## TACHE_19 - Page Réglages + bouton TopBar ✅ INTÉGRÉE
- **Scope estimé :** ~35 000 tokens
- **Statut :** ✅ INTÉGRÉE
- **Packages touchés :**
  - `presentation/settings/` ← créer : `SettingsScreen.kt`, `SettingsViewModel.kt`, `UserPrefsRepository.kt`
  - `presentation/common/LexicaTopAppBar.kt` ← ne pas modifier directement, passer par PR
  - `presentation/LexicaApp.kt` ← ajout route + icône TopBar (via PR)
- **Fichier intégration attendu :** `integration_pending/settings_pr.md`

### Objectif
Un bouton ⚙️ dans la TopAppBar (à gauche du bouton profil 👤, visible sur le Dashboard uniquement) ouvre une page de réglages.

### Comportement attendu

**Bouton TopBar**
- Icône `Icons.Default.Settings` affiché uniquement sur `Screen.Dashboard`, à côté de l'icône profil
- Navigation vers `Screen.Settings`

**SettingsScreen — Sections**

Section "Apparence"
- **Thème** : `FilterChip` radio — `Clair` / `Sombre` / `Système` (défaut : Système)
- **Taille police** : slider 12sp–20sp (défaut 16sp)
- **Couleur d'accent** : 5 swatches prédéfinies (bleu/vert/violet/orange/rose)

Section "Entraînement"
- **Cartes par session** : slider 5–50 (défaut 20) — sync avec `AdminPrefsRepository` si admin
- **Afficher définition en premier** : toggle (inverse recto/verso)
- **Activer les défis** : toggle global ON/OFF (désactive ortho + sémantique d'un coup)

Section "Notifications"
- **Rappel quotidien** : toggle ON/OFF
- **Heure du rappel** : `TimePicker` Material3 (visible uniquement si toggle ON)

Section "À propos"
- Version de l'app (lue depuis `BuildConfig.VERSION_NAME`)
- Lien Politique de confidentialité (placeholder)

### Architecture
```
UserPrefsRepository.kt  — SharedPreferences wrappé (thème, taille police, etc.)
SettingsViewModel.kt    — collecte + met à jour les préférences
SettingsScreen.kt       — écran Compose avec les sections ci-dessus
```

### Persistance
Toutes les préférences stockées dans `SharedPreferences` via `UserPrefsRepository`. Le thème sombre/clair est appliqué via `MaterialTheme` dans `MainActivity` (lire la pref, passer à `darkTheme = ...`).

### Contraintes
- Ne pas modifier `LexicaTopAppBar.kt` directement
- Ne pas recréer ce qui existe dans `AdminPrefsRepository` — référencer les mêmes SharedPreferences
- Le changement de thème doit s'appliquer sans redémarrer l'app (`remember { mutableStateOf(...) }` dans MainActivity)
- Fichier intégration attendu : `integration_pending/settings_pr.md`

### État réel constaté
- ✅ Écran `Settings` intégré avec thème, taille de texte, notifications et entrée admin
- ✅ Application réelle du modulateur de texte via `fontScale` Compose dans `MainActivity`
- ✅ Ajout d'un flux plus propre pour la taille de police : slider local + bouton `Valider`
- ✅ Ajout d'un aperçu local dédié
- ⚠️ Peaufinage UX encore à confirmer sur appareil réel (ressenti de l'aperçu et ergonomie finale)

---

## TACHE_20 - Déverrouillage progressif des mini-jeux par XP ✅ INTÉGRÉE
- **Scope estimé :** ~30 000 tokens
- **Statut :** ✅ INTÉGRÉE
- **Packages touchés :**
  - `presentation/games/MiniGamesScreen.kt` ← modifier affichage (verrouillé/déverrouillé)
  - `presentation/games/MiniGamesViewModel.kt` ← créer
  - `features/gamification/domain/` ← ajouter `GameUnlockConfig.kt`
- **Fichier intégration attendu :** `integration_pending/game_unlock_pr.md`

### Objectif
Les mini-jeux se débloquent progressivement selon le niveau XP du joueur, pour créer une courbe de progression motivante.

### Ordre de déverrouillage (à implémenter exactement)

| Niveau | XP requis | Jeux débloqués |
|--------|-----------|----------------|
| 1 | 0 XP | Correspondance (Matching), QCM |
| 2 | 100 XP | Pendu (Hangman) |
| 3 | 300 XP | Dictée (Spelling) |
| 5 | 700 XP | Anagrammes |
| 7 | 1300 XP | Mode Chrono |
| 10 | 2100 XP | Memory |
| 13 | 3300 XP | Définition à Compléter |
| 16 | 4900 XP | Associations Sémantiques |
| 20 | 7100 XP | Spelling Avancé |

*Rationale : commencer par les jeux les plus simples (reconnaissance), progresser vers les jeux de production (saisie, mémoire). Level N = 100×N² XP selon formule existante.*

### Comportement attendu

**Dans MiniGamesScreen**
- Les jeux verrouillés affichent une carte grisée avec une icône 🔒 et le texte "Niveau X requis"
- Les jeux déverrouillés sont normalement cliquables
- Pas de message d'erreur — simplement visuellement distinct
- Ajouter un micro-effet d'animation (scale bounce) lors du déverrouillage d'un nouveau jeu

**GameUnlockConfig.kt**
```kotlin
object GameUnlockConfig {
    data class GameLock(val route: String, val name: String, val requiredXp: Int)
    val ALL_GAMES = listOf(
        GameLock(Screen.MatchingGame.route, "Correspondance", 0),
        GameLock(Screen.QcmGame.route, "QCM", 0),
        GameLock(Screen.HangmanGame.route, "Pendu", 100),
        // ... etc
    )
}
```

**MiniGamesViewModel.kt**
```kotlin
// Combine la liste des jeux avec le XP courant de l'utilisateur
val gamesWithLockState: StateFlow<List<GameWithLockState>>
```

### Contraintes
- La liste complète des jeux reste visible (pas cachée) — juste verrouillée visuellement
- `UserStatsRepository` existe déjà — récupérer le XP via `userStats.xp`
- Ne pas modifier les fichiers de jeux individuels
- Fichier intégration attendu : `integration_pending/game_unlock_pr.md`

---

## TACHE_21 - Barre de navigation inférieure + Mode En Ligne ✅ INTÉGRÉE
- **Scope estimé :** ~50 000 tokens
- **Statut :** ✅ INTÉGRÉE
- **Packages touchés :**
  - `presentation/LexicaApp.kt` ← ajout `BottomNavigationBar` dans le `Scaffold` (via PR)
  - `presentation/online/` ← créer : `OnlineScreen.kt`, `OnlineModeViewModel.kt` (placeholder)
  - `presentation/common/` ← créer : `LexicaBottomNavBar.kt`
  - `MainAcitivity.kt` ← toucher si nécessaire (via PR)
- **Fichier intégration attendu :** `integration_pending/bottom_nav_pr.md`

### Objectif
Remplacer la navigation actuelle (boutons Dashboard) par une barre de navigation inférieure permanente avec 4 onglets. Ajouter un bouton/onglet "Mode En Ligne" qui pointe vers un écran placeholder.

### Onglets de la barre (dans l'ordre)

| # | Icône | Label | Destination |
|---|-------|-------|-------------|
| 1 | `Home` | Accueil | `Screen.Dashboard` |
| 2 | `School` | Entraînement | `Screen.Review` |
| 3 | `SportsEsports` | Mini-Jeux | `Screen.MiniGames` |
| 4 | `Wifi` (ou `Language`) | En Ligne | `Screen.Online` (nouveau) |

### Comportement attendu

**Barre de navigation**
- `NavigationBar` Material3 avec 4 `NavigationBarItem`
- L'onglet actif est mis en surbrillance selon `currentRoute`
- La barre est visible sur TOUS les écrans SAUF les jeux individuels (masquée quand on joue)
- Sur les jeux (`Screen.MatchingGame`, `Screen.QcmGame`, etc.) — barre masquée, TopAppBar avec back button suffit

**Screen.Online (placeholder)**
- Titre "Mode En Ligne"
- Illustration centrale + texte "Bientôt disponible — Défis, duels et classements arrivent !"
- Bouton "Être notifié" (désactivé, juste visuel pour l'instant)
- Conçu pour accueillir le mode multijoueur futur

### Migration de la navigation actuelle
- Les boutons "Commencer l'entraînement", "Mini-Jeux" etc. dans `DashboardScreen` peuvent rester — ils font doublon mais c'est un avantage UX (accès rapide depuis l'accueil)
- Supprimer la navigation vers Review/MiniGames depuis la TopAppBar si elle y est

### Contraintes
- La barre doit être masquée sur les écrans de jeux (liste exhaustive dans `LexicaApp.kt` : toutes les routes `game_*`)
- Ne pas casser la navigation back existante (les jeux naviguent déjà avec `onBack`)
- `Screen.Online` est un simple placeholder — ne pas coder de logique réseau maintenant
- Fichier intégration attendu : `integration_pending/bottom_nav_pr.md`

### État réel constaté
- ✅ Bottom bar intégrée dans `LexicaApp.kt`
- ✅ Écran `Online` placeholder intégré
- ✅ Nouvelle entrée produit `Usage` aussi intégrée dans la navigation basse
- ✅ Ordre des onglets réajusté côté produit (`Accueil`, `Entraînement`, `Mini-Jeux`, `Usage`, `En Ligne`)
- ⚠️ Réglages fins d'espacement / taille perçue / wrapping des labels encore à peaufiner

---

## TACHE_22 - Icône d'application moderne ✅ INTÉGRÉE
- **Scope estimé :** ~15 000 tokens
- **Statut :** ✅ INTÉGRÉE
- **Packages touchés :**
  - `app/src/main/res/mipmap-*/` ← remplacer les icônes launcher
  - `app/src/main/res/drawable/` ← ajouter `ic_launcher_foreground.xml` vectoriel
  - `app/src/main/AndroidManifest.xml` ← vérifier `android:icon` et `android:roundIcon`
- **Fichier intégration attendu :** `integration_pending/app_icon_v2_pr.md`

### Objectif
Concevoir et intégrer une icône launcher moderne, reconnaissable et "marketing" pour l'app Lexica.

### Concept recommandé
- **Forme** : Adaptive icon (foreground + background séparés) pour s'adapter à tous les launchers Android
- **Symbole** : Lettre stylisée "L" ou livre ouvert avec effet lumière/gradient, sur fond uni profond (bleu marine `#0D1B2A` ou violet `#2D1B69`)
- **Style** : Flat design avec légère ombre portée — pas de skeuomorphisme. S'inspirer de Duolingo (figure ronde friendly) ou Google Translate (flat géométrique propre)
- **Couleur d'accent** : Dégradé bleu→violet (`#4F8EF7` → `#7C3AED`) sur le symbole principal
- **Lisibilité** : Icône reconnaissable en 48×48dp — tester mentalement si visible sur fond blanc ET fond sombre

### Livrables attendus
1. `ic_launcher_foreground.xml` — vecteur AnimatedVectorDrawable ou VectorDrawable (foreground)
2. `ic_launcher_background.xml` — fond uni ou dégradé simple
3. `ic_launcher.xml` dans `drawable/` — adaptive icon wrapper
4. Remplacer `mipmap-mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi` : `ic_launcher.png` et `ic_launcher_round.png`
5. Vérifier dans `AndroidManifest.xml` : `android:icon="@mipmap/ic_launcher"` et `android:roundIcon="@mipmap/ic_launcher_round"`

### Ressources et inspiration
- **Android Asset Studio** (outil Google pour générer les mipmap) : `romannurik.github.io/AndroidAssetStudio/icons-launcher.html`
- **Material Symbols** : icônes vectoriels Google libres de droits pour s'inspirer
- **Adaptive Icons guide** : `developer.android.com/develop/ui/views/launch/icon_design_adaptive`

### Contraintes
- Pas de dépendances externes pour l'icône — SVG/XML uniquement
- Respecter les zones safe area des adaptive icons (foreground centré dans 66% du canvas)
- Ne pas modifier les icônes dans `app/src/debug/` si elles existent

### État réel constaté
- ✅ Icône adaptive déjà intégrée dans le projet
- ✅ Nouvelle itération launcher vectorielle retravaillée récemment (`ic_launcher_foreground.xml`)
- ⚠️ Une direction artistique finale peut encore être affinée si besoin produit

---

## TACHE_23 - Synchronisation des données utilisateur via Firebase ✅ INTÉGRÉE
- **Scope estimé :** ~70 000 tokens
- **Statut :** ✅ INTÉGRÉE — validations runtime restantes
- **Packages touchés :**
  - `features/sync/` ← créer : `FirestoreSyncRepository.kt`, `SyncViewModel.kt`, `SyncManager.kt`
  - `features/auth/` ← modifier `FirebaseAuthRepository.kt` (hook post-login)
  - `presentation/auth/` ← ajouter dialog de confirmation avant merge
  - `data/local/LexicaDatabase.kt` ← ne pas modifier (lecture seule via repos existants)
- **Fichier intégration attendu :** `integration_pending/firebase_sync_pr.md`

### Objectif

---

## TACHE_34 - Crash de fin de session Review
- **Scope estimé :** ~20 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/review/ReviewViewModel.kt`
  - `presentation/review/ReviewScreen.kt`
  - tests ciblés `presentation/review/ReviewViewModelTest.kt`
- **Fichier intégration attendu :** `integration_pending/review_session_end_fix_pr.md`

### Objectif
Supprimer le crash encore présent quand la session de révision atteint sa fin (ex. 20 cartes), notamment avec `currentCard = null`, `isSessionFinished`, suppression de carte en fin de lot, et combinaisons de modes admin.

---

## TACHE_35 - Diagnostic disponibilité TTS + fallback UX
- **Scope estimé :** ~18 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `core/tts/`
  - `presentation/review/`
  - écrans dictée si nécessaire
- **Fichier intégration attendu :** `integration_pending/tts_runtime_diagnostic_pr.md`

### Objectif
Rendre explicite la disponibilité réelle du moteur TTS sur appareil / émulateur et éviter l'impression de boutons audio "gris sans raison".

### Attendu
- détection claire `TTS prêt / non prêt / langue indisponible`
- message UI explicite quand l'audio n'est pas disponible
- comportement cohérent des boutons audio selon l'état réel

---

## TACHE_36 - Mode voiture V2 (finalisation produit)
- **Scope estimé :** ~35 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/review/DrivingModeScreen.kt`
  - `presentation/review/DrivingModeViewModel.kt`
  - `presentation/dashboard/` ou `presentation/review/` selon arbitrage placement bouton
- **Fichier intégration attendu :** `integration_pending/driving_mode_v2_pr.md`

### Objectif
Transformer la première version du mode voiture en vraie fonctionnalité produit.

### Attendu
- options de délai recto → verso
- délai entre deux cartes
- répétition configurable
- choix `mot seulement` / `mot + définition`
- arbitrage final sur l'emplacement du bouton d'entrée

---

## TACHE_37 - Matching visuel avancé
- **Scope estimé :** ~30 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/games/matching/`
- **Fichier intégration attendu :** `integration_pending/matching_visual_pr.md`

### Objectif
Remplacer le système de badges du matching par une liaison visuelle plus claire : traits, repositionnement, ou autre rendu mobile lisible, tout en conservant la validation globale du lot.

---

## TACHE_38 - Utilisation : exercices d'emploi réels
- **Scope estimé :** ~45 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/utilisation/`
- **Fichier intégration attendu :** `integration_pending/utilisation_real_exercises_pr.md`

### Objectif
Faire évoluer la page `Utilisation` d'un placeholder vers de vrais modules d'emploi des mots : reformulation, phrase à écrire, choix du bon mot, correction de phrase, remplacement lexical.

---

## TACHE_39 - Bottom bar : peaufinage spacing / labels
- **Scope estimé :** ~12 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/common/LexicaBottomNavBar.kt`
- **Fichier intégration attendu :** `integration_pending/bottom_nav_polish_pr.md`

### Objectif
Réduire légèrement les espaces visuels entre les labels d'onglets pour permettre une police un peu plus lisible à 100%, tout en acceptant un passage sur 2 lignes si la taille utilisateur augmente.
Quand un utilisateur se connecte avec un compte existant, récupérer sa progression cloud (XP, streak, favoris) et proposer de remplacer la progression locale — avec une alerte claire avant d'écraser.

### Comportement attendu

**Connexion — flux complet**
1. Utilisateur se connecte via `LoginScreen`
2. `SyncManager.onLoginSuccess(uid)` est appelé juste après `FirebaseAuth.signIn()` réussi
3. `SyncManager` vérifie Firestore : existe-t-il un document `users/{uid}` ?
   - **Pas de doc cloud** → sauvegarder la progression locale dans Firestore silencieusement (premier appareil)
   - **Doc cloud trouvé ET progression locale non vide** → afficher le dialog de choix :
     ```
     ⚠️ Progression existante détectée
     "Ce compte a une progression sauvegardée (Niveau X, Y mots).
      Votre progression locale sera remplacée.
      Continuer ?"
     [Annuler]  [Remplacer ma progression locale]
     ```
   - **Doc cloud trouvé ET progression locale vide** → import silencieux, aucun dialog

**Données synchronisées (Firestore `users/{uid}`)**
```
{
  xp: Int,
  level: Int,
  streak: Int,
  lastLoginDate: String,
  favoriteCardIds: List<String>,
  reviewStats: Map<String, Int>   // optionnel
}
```

**Déconnexion**
- À la déconnexion (`SignOut`) : sauvegarder la progression locale dans Firestore avant de vider la session locale

**Sync périodique (optionnel)**
- Toutes les 30 minutes si connecté : push silencieux de `xp + streak` vers Firestore

### Architecture
```
FirestoreSyncRepository.kt  — opérations CRUD Firestore (uploadProgress, downloadProgress)
SyncManager.kt              — orchestrateur : appelé après login/logout, gère la logique de merge
SyncConfirmDialog.kt        — composable dialog "tes données locales vont être remplacées"
SyncViewModel.kt            — expose SyncState, appelé depuis MainActivity au démarrage si connecté
```

### Contraintes
- Ne JAMAIS écraser la progression locale sans confirmation explicite de l'utilisateur
- La synchro ne doit pas bloquer l'UI (tout en coroutine background)
- Si Firestore échoue (réseau absent) → continuer en mode local, logger silencieusement
- Ajouter `implementation("com.google.firebase:firebase-firestore-ktx")` dans `build.gradle.kts` via la PR
- Fichier intégration attendu : `integration_pending/firebase_sync_pr.md`

---

## TACHE_24 - Validation console Firebase + règles de sécurité
- **Scope estimé :** ~8 000 tokens
- **Statut :** 🔴 À faire
- **Type :** Tâche manuelle guidée (Chef d'Orchestre)
- **Zone touchée :** Firebase Console uniquement + mise à jour doc si validé

### Objectif
Finaliser la configuration Firebase qui ne peut pas être garantie depuis le code source.

### Checklist attendue
- Vérifier `Authentication` → `Sign-in method` → `Email/Password` = **Enabled**
- Vérifier que le projet Firebase est bien `lexica-6d59a`
- Ajouter le **SHA-1** du keystore debug Android dans `Project settings` → `Your apps` → Android app
- Ajouter aussi le **SHA-256** si disponible
- Vérifier que Firestore est créé dans le bon projet
- Appliquer des règles minimales de sécurité Firestore pour `users/{uid}`

### Livrables
- Capture ou confirmation humaine des points ci-dessus
- Mise à jour `DAILY_STANDUP.md` et `FEATURES.md` une fois validé

---

## TACHE_25 - Tests runtime Firebase Auth + Sync multi-session
- **Scope estimé :** ~15 000 tokens
- **Statut :** 🔴 À faire
- **Type :** Validation fonctionnelle manuelle
- **Zone touchée :** émulateur/appareil + éventuellement `DAILY_STANDUP.md`

### Objectif
Valider que l'authentification et la synchronisation cloud fonctionnent réellement en conditions d'usage.

### Parcours de test
1. Créer un compte Firebase depuis l'app
2. Se connecter, générer un peu de progression locale (XP, favoris)
3. Se déconnecter pour déclencher la sauvegarde cloud
4. Se reconnecter sur une seconde session ou après réinstallation
5. Vérifier l'import cloud ou le dialog de conflit
6. Vérifier que le sign-out ne perd pas les données

### Critères d'acceptation
- Login/register OK
- Sign-out avec sauvegarde OK
- Import cloud OK si local vide
- Dialog de conflit OK si local et cloud diffèrent
- Aucune régression de navigation

---

## TACHE_26 - Google Sign-In Firebase
- **Scope estimé :** ~35 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `features/auth/`
  - `presentation/login/` ou package auth existant
  - `MainActivity.kt` / `LexicaApp.kt` via PR si branchement global requis
- **Fichier intégration attendu :** `integration_pending/google_signin_pr.md`

### Objectif
Compléter le bouton Google déjà prévu côté auth pour permettre une connexion OAuth Google réelle.

### Attendu
- Configuration client OAuth Android/Web dans Firebase Console
- Récupération du token Google côté Android
- Appel de `signInWithGoogleIdToken()` réellement branché
- Gestion erreur annulation / compte invalide / absence réseau

---

## TACHE_27 - Synchroniser les scores de jeux vers Firestore
- **Scope estimé :** ~30 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `features/sync/`
  - `features/gamification/`
  - écrans de jeux via PR si nécessaire
- **Fichier intégration attendu :** `integration_pending/firebase_game_scores_pr.md`

### Objectif
Étendre la synchro Firestore actuelle pour inclure les statistiques de mini-jeux et pas seulement la progression globale.

### Données candidates
- nombre de parties par jeu
- meilleur score par jeu
- dernière date de jeu
- XP gagnée par jeu

### Contraintes
- Ne pas casser le schéma Firestore actuel
- Prévoir migration douce / champs optionnels

---

## TACHE_28 - Sync hors-ligne robuste via WorkManager
- **Scope estimé :** ~45 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `features/sync/`
  - `AndroidManifest.xml` ou config worker via PR si nécessaire
- **Fichier intégration attendu :** `integration_pending/firebase_offline_sync_pr.md`

### Objectif
Remplacer la sync périodique en coroutine tant que l'app est ouverte par une vraie stratégie Android persistante avec WorkManager.

### Attendu
- Worker périodique
- relance au retour réseau
- politique de retry
- limitation batterie raisonnable

---

## TACHE_29 - Jeu de Prononciation
- **Scope estimé :** ~55 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/games/pronunciation/`
  - éventuels helpers audio/permissions
- **Fichier intégration attendu :** `integration_pending/pronunciation_pr.md`

### Objectif
Ajouter le mini-jeu prononciation mentionné dans le backlog UX.

### Attendu
- reconnaissance vocale Android
- comparaison simple mot attendu / transcription
- gestion permission micro
- score + écran de fin + XP

---

## TACHE_30 - Mode Multijoueur / En Ligne réel
- **Scope estimé :** ~90 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/online/`
  - `features/multiplayer/` ou `features/online/`
  - `features/sync/` ou backend Firebase selon design
- **Fichier intégration attendu :** `integration_pending/multiplayer_pr.md`

### Objectif
Transformer le placeholder `Mode En Ligne` en vraie fonctionnalité : salons, duels, ou leaderboard selon arbitrage produit.

### Phase 1 recommandée
- écran leaderboard simple
- profils cloud minimaux
- classement par XP / streak

### Phase 2 possible
- matchmaking ou défis asynchrones
- duels temps réel

---

## TACHE_31 - Mode entraînement responsive en paysage
- **Scope estimé :** ~25 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/review/ReviewScreen.kt`
  - `presentation/review/` si un petit ajustement ViewModel est nécessaire
- **Fichier intégration attendu :** `integration_pending/review_landscape_pr.md`

### Objectif
Corriger l'écran d'entraînement/révision en mode paysage pour qu'il reste entièrement utilisable sur téléphone et tablette.

### Problèmes signalés
- le bouton pour retourner la carte disparaît en paysage
- il n'est pas possible de faire défiler l'écran quand la hauteur manque
- certains éléments sortent de la zone visible

### Attendu
- layout compatible portrait **et** paysage
- bouton de retournement toujours visible ou accessible
- contenu scrollable si l'espace vertical est insuffisant
- pas de blocage UX en orientation horizontale

### Contraintes
- privilégier une correction Compose simple (`verticalScroll`, `weight`, réorganisation portrait/paysage)
- ne pas casser le flux actuel de révision ni les défis intégrés

---

## TACHE_32 - Boutons de notation Review : libellés lisibles et layout compact
- **Scope estimé :** ~15 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/review/ReviewScreen.kt`
  - `presentation/common/` si un composable partagé est extrait
- **Fichier intégration attendu :** `integration_pending/review_grade_buttons_pr.md`

### Objectif
Rendre les boutons de notation de la révision lisibles, sans texte tronqué, surtout pour `À revoir`, `Je l'ai` et `Trop facile`.

### Problèmes signalés
- les libellés ne s'affichent pas en entier dans leurs rectangles
- les marges internes sont trop grandes
- une variante en disposition pyramidale est souhaitée si nécessaire, avec `Trop facile` en bas

### Attendu
- texte complet visible sur tous les boutons
- padding/marges ajustés pour mobile
- disposition compacte et stable en portrait comme en paysage
- si besoin, tester une disposition pyramidale plus lisible que la ligne actuelle

### Contraintes
- conserver la signification fonctionnelle des actions de notation
- ne pas dégrader l'accessibilité tactile des boutons

---

## TACHE_33 - Liste de mots : sélection multiple et actions groupées
- **Scope estimé :** ~40 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/wordlist/WordListScreen.kt`
  - `presentation/wordlist/WordListViewModel.kt`
  - éventuellement `presentation/wordlist/WordDetailScreen.kt` si besoin d'harmonisation navigation
- **Fichier intégration attendu :** `integration_pending/wordlist_bulk_actions_pr.md`

### Objectif
Permettre la sélection multiple de mots dans les listes pour appliquer des actions en lot.

### Actions attendues
- sélectionner plusieurs mots
- ajouter plusieurs mots aux favoris en une seule action
- supprimer plusieurs mots en une seule action

### Attendu
- mode sélection multiple clair visuellement
- état sélectionné/non sélectionné identifiable immédiatement
- barre d'actions ou actions contextuelles pour `Favoris` et `Supprimer`
- confirmation avant suppression multiple

### Contraintes
- conserver les actions unitaires existantes si elles sont déjà présentes
- éviter toute suppression en lot sans confirmation explicite

---

## TACHE_34 - Crash de fin de session Review
- **Scope estimé :** ~20 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/review/ReviewViewModel.kt`
  - `presentation/review/ReviewScreen.kt`
  - tests ciblés `presentation/review/ReviewViewModelTest.kt`
- **Fichier intégration attendu :** `integration_pending/review_session_end_fix_pr.md`

### Objectif
Supprimer le crash encore présent quand la session de révision atteint sa fin (ex. 20 cartes), notamment avec `currentCard = null`, `isSessionFinished`, suppression de carte en fin de lot, et combinaisons de modes admin.

---

## TACHE_35 - Diagnostic disponibilité TTS + fallback UX
- **Scope estimé :** ~18 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `core/tts/`
  - `presentation/review/`
  - écrans dictée si nécessaire
- **Fichier intégration attendu :** `integration_pending/tts_runtime_diagnostic_pr.md`

### Objectif
Rendre explicite la disponibilité réelle du moteur TTS sur appareil / émulateur et éviter l'impression de boutons audio grisés sans raison visible.

### Attendu
- détection claire `TTS prêt / non prêt / langue indisponible`
- message UI explicite quand l'audio n'est pas disponible
- comportement cohérent des boutons audio selon l'état réel

---

## TACHE_36 - Mode voiture V2 (finalisation produit)
- **Scope estimé :** ~35 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/review/DrivingModeScreen.kt`
  - `presentation/review/DrivingModeViewModel.kt`
  - `presentation/dashboard/` ou `presentation/review/` selon arbitrage placement bouton
- **Fichier intégration attendu :** `integration_pending/driving_mode_v2_pr.md`

### Objectif
Transformer la première version du mode voiture en vraie fonctionnalité produit.

### Attendu
- options de délai recto → verso
- délai entre deux cartes
- répétition configurable
- choix `mot seulement` / `mot + définition`
- arbitrage final sur l'emplacement du bouton d'entrée

---

## TACHE_37 - Matching visuel avancé
- **Scope estimé :** ~30 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/games/matching/`
- **Fichier intégration attendu :** `integration_pending/matching_visual_pr.md`

### Objectif
Remplacer le système de badges du matching par une liaison visuelle plus claire : traits, repositionnement, ou autre rendu mobile lisible, tout en conservant la validation globale du lot.

---

## TACHE_38 - Utilisation : exercices d'emploi réels
- **Scope estimé :** ~45 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/utilisation/`
- **Fichier intégration attendu :** `integration_pending/utilisation_real_exercises_pr.md`

### Objectif
Faire évoluer la page `Utilisation` d'un placeholder vers de vrais modules d'emploi des mots : reformulation, phrase à écrire, choix du bon mot, correction de phrase, remplacement lexical.

---

## TACHE_39 - Bottom bar : peaufinage spacing / labels
- **Scope estimé :** ~12 000 tokens
- **Statut :** 🔴 À faire
- **Packages touchés :**
  - `presentation/common/LexicaBottomNavBar.kt`
- **Fichier intégration attendu :** `integration_pending/bottom_nav_polish_pr.md`

### Objectif
Réduire légèrement les espaces visuels entre les labels d'onglets pour permettre une police un peu plus lisible à 100%, tout en acceptant un passage sur 2 lignes si la taille utilisateur augmente.

