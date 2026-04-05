# 📅 DAILY STANDUP - Journal Unique de Suivi

**Responsable:** Chef d'Orchestre  
**Fréquence:** Quotidienne  
**Format:** SEUL fichier de suivi du projet (remplace tous les "etat_*.md", "rapport_*.md", etc.)

---

## 📅 2026-04-05 - Option "Remettre la progression à zéro" dans le profil

### ✅ Accompli
- [x] Ajout de `deleteAll()` dans `FlashcardDao` (supprime toutes les flashcards)
- [x] Ajout de `deleteAll()` dans `ReviewQuestionDao` (supprime toute la progression SM2)
- [x] Ajout de `deleteAllCards()` dans l'interface `FlashcardRepository` + implémentation dans `FlashcardRepositoryImpl`
- [x] Création de `domain/usecase/ResetProgressUseCase.kt` — orchestre la suppression complète : flashcards, progression SM2, snapshots de session, stats XP/streak, stats journalières
- [x] Mise à jour de `ProfileUiState` : nouveaux champs `resetDialogStep`, `isResetting`, `resetDoneMessage`
- [x] Ajout dans `ProfileViewModel` de 5 méthodes : `onResetProgressClicked`, `onResetStep1Confirmed`, `onResetDismissed`, `onResetConfirmedFinal`, `onResetMessageDismissed`
- [x] Mise à jour de `ProfileViewModelFactory` pour accepter `ResetProgressUseCase` + `SyncManager`
- [x] Mise à jour de `ProfileScreen` :
  - bouton rouge "🗑️ Remettre la progression à zéro" en bas de l'écran
  - **Dialog 1** : "🚨 Mais… VRAIMENT ?!" affiche les stats actuelles (mots, XP, streak) + boutons humoristiques
  - **Dialog 2** : "⚠️ DERNIÈRE CHANCE !" avec fond orange, texte rouge sang + bouton "💣 OUI, tout effacer !"
  - Dialog de succès post-reset : "🌱 C'est reparti de zéro !"
  - Spinner de chargement pendant la suppression
- [x] **Correctif sync Firestore post-reset** : après un reset, si l'utilisateur est connecté, la progression vide est uploadée vers Firestore pour éviter la ré-injection des anciennes données au prochain login
- [x] `SyncManager` sorti de `SyncViewModelFactory` et instancié dans `MainActivity` pour être partagé entre `SyncViewModel` et `ProfileViewModel`
- [x] `SyncViewModelFactory` simplifiée (accepte directement un `SyncManager`)
- [x] Nettoyage des imports inutilisés dans `SyncViewModel.kt`

### 📁 Fichiers modifiés
- `data/local/FlashcardDao.kt`
- `data/local/ReviewQuestionDao.kt`
- `domain/repository/FlashcardRepository.kt`
- `data/repository/FlashcardRepositoryImpl.kt`
- `domain/usecase/ResetProgressUseCase.kt` *(nouveau)*
- `presentation/profile/ProfileViewModel.kt`
- `presentation/profile/ProfileScreen.kt`
- `presentation/LexicaApp.kt`
- `features/sync/SyncViewModel.kt`
- `MainActivity.kt`

### ℹ️ Note sur la synchro Firestore
Seuls **XP, niveau, streak, favoris** sont synchronisés dans le cloud. La progression SM2 par mot (date de prochaine révision, état d'apprentissage) reste **locale uniquement**. Sur un nouveau téléphone, l'utilisateur retrouve ses stats de gamification mais repart de zéro pour les révisions.

---

## 📅 2026-04-05 - Correctif flash du verso sur carte suivante

### ✅ Accompli
- [x] Audit du flip Compose dans `ReviewScreen.kt` et du flux `publishUiState(...)` dans `ReviewViewModel.kt`
- [x] Ajout d'une clé d'instance de question normale pour distinguer chaque nouvelle présentation d'une carte
- [x] Correctif UI du flash entre deux cartes
  - le verso de la carte suivante ne montre plus brièvement sa réponse pendant l'animation de transition
  - tant que la présentation courante n'a pas été révélée au moins une fois, le verso reste visuellement vide
  - l'animation de flip recto/verso est conservée
- [x] Vérification statique ciblée des fichiers modifiés
- [x] Vérification Git : branche de travail active confirmée `integration/polo-1-2026-04-04`
- [x] Audit du réglage `Taille de la session` entre `Settings`, `UserPrefsRepository` et `ReviewViewModel`
- [x] Correctif du lot effectif de révision
  - un ancien snapshot de session à `10` questions n'est plus restauré si la taille effective configurée a changé
  - le contrôle de compatibilité snapshot compare maintenant toujours la taille effective, même hors mode admin
  - ajout d'un test de non-régression pour le cas `snapshot=10` + préférence utilisateur différente
- [x] Correctif mode admin `Review` pour tests isolés
  - si les deux types de questions normales sont désactivés mais qu'un sous-flux admin est sélectionné (`Défi sémantique`, `Défi orthographique`, `Question orthographique`, `QCM intégré`, `Correspondance`), une session de test forcée est maintenant créée
  - les événements de test admin se lancent directement sans dépendre de la planification naturelle de session
  - ces événements forcés sont marqués sans crédit de session pour éviter d'altérer la progression juste pour un test admin
  - ajout de tests ciblés pour `Défi sémantique` seul et `Correspondance` seule
- [x] Preset admin explicite `Présentation normale`
  - ajout d'un bouton dédié dans `AdminScreen.kt` pour revenir rapidement au flux standard des questions
  - ce preset active `Définition -> Mot` + `Mot -> Définition`
  - ce preset désactive les sous-flux de test forcé (`Question orthographique`, `QCM`, `Correspondance`, défis)
  - ajout d'un test Review qui verrouille le retour au flux `NORMAL_QUESTION`
- [x] Correctif du volume de tests en mode admin forcé
  - la `Taille de la session` admin pilote maintenant bien le nombre d'items de test enchaînés en mode forcé, au lieu d'un seul item par type
  - `Défi orthographique` seul / `Défi sémantique` seul peuvent maintenant s'enchaîner sur plusieurs cartes de test
  - `QCM` seul peut se répéter sur plusieurs cartes de test
  - `Correspondance` seule peut proposer plusieurs manches successives
  - quand plusieurs sous-flux sont cochés, ils sont maintenant distribués sur toute la longueur de la session de test admin
  - texte d'aide admin clarifié pour distinguer `flux normal` vs `mode test forcé`
  - tests ciblés ajoutés pour verrouiller que la taille de session est bien consommée sur plusieurs items forcés
- [x] Durcissement audio / TTS dans `Review`
  - `LexicaTtsService` accepte maintenant un fallback sur la voix système si une voix française n'est pas disponible
  - les boutons audio ne restent plus bloqués à cause de l'absence de voix FR stricte quand une autre voix TTS est disponible sur l'appareil
  - `ReviewViewModel` remonte désormais le message d'état TTS à l'UI
  - `ReviewScreen` affiche explicitement le statut audio (fallback voix système ou indisponibilité totale)
- [x] Réalignement du preset admin `Présentation normale`
  - le preset admin normal réactive désormais la révision normale complète
  - il active `Définition -> Mot`, `Mot -> Définition`, `Question orthographique`, `QCM intégré`, `Correspondance`, `Défi sémantique`, `Défi orthographique`
  - il ne sert plus à couper les sous-flux, mais à revenir à la vraie logique métier standard
  - l'état visuel de `AdminScreen.kt` et les textes d'aide ont été réalignés sur cette définition produit
- [x] Détection / installation TTS français au lancement de l'app
  - ajout d'un contrôle au démarrage via les intents système Android TTS
  - si aucune voix française n'est détectée, l'app ouvre automatiquement le flux système d'installation/configuration TTS
  - fallback vers les réglages système TTS si l'écran d'installation direct n'est pas disponible
  - ajout d'un helper testé pour détecter la présence d'une voix FR dans les voix annoncées par le système
  - ajout d'un test admin pour verrouiller le preset normal complet
- [x] Popup UX de configuration TTS au lancement
  - remplacement de l'ouverture système brute par un vrai dialogue utilisateur
  - popup avec explication claire, bouton `Installer / configurer` et bouton `Plus tard`
  - l'ouverture des écrans système TTS passe désormais par cette confirmation utilisateur
  - le test Review du flux normal a été réaligné sur la vraie logique métier (une session normale peut démarrer sur une annexe)
- [x] Refactor final du mode admin `Présentation normale`
  - `Présentation normale` est maintenant un vrai toggle persistant, pas seulement un bouton preset
  - quand il est activé, le panneau avancé de sélection des types de questions se replie visuellement
  - quand il est désactivé, le panneau avancé réapparaît avec les réglages précédemment mémorisés
  - en `Présentation normale`, `Review` se comporte maintenant réellement comme sans mode admin :
    - aucun type de question n'est forcé
    - aucun filtrage admin des types de questions n'est appliqué
    - l'override admin de taille de session n'est plus appliqué
  - compatibilité snapshot renforcée pour éviter de restaurer une session créée dans un mode admin incompatible
- [x] Validation ciblée après correctif
  - `:app:compileDebugKotlin`
  - `:app:testDebugUnitTest --tests com.example.lexicaandroid2.presentation.review.ReviewViewModelTest`
  - `:app:testDebugUnitTest --tests com.example.lexicaandroid2.presentation.admin.AdminViewModelTest`
  - `:app:testDebugUnitTest --tests com.example.lexicaandroid2.core.tts.TtsVoiceSupportTest`

### 🔴 Bloquants
- Aucun

### 🔜 Prochaines actions
- Vérifier sur appareil/émulateur que la transition `notation -> carte suivante` est désormais propre sur plusieurs enchaînements rapides
- Vérifier aussi le cas d'une même carte re-présentée plus tard dans la session après un `À revoir`
- Vérifier sur appareil qu'un changement `Réglages -> Taille de la session` modifie bien la prochaine session lancée depuis l'accueil
- Vérifier le même scénario pour un compte admin avec override de taille actif
- Vérifier sur appareil le mode admin en sélection isolée : `Défi sémantique` seul, `Défi orthographique` seul, `QCM` seul, `Correspondance` seule
- Vérifier sur appareil le preset `Présentation normale` et confirmer qu'il redonne bien le comportement standard sans forçage admin
- Vérifier sur appareil qu'une grande `Taille de la session` admin en mode forcé enchaîne bien plusieurs items, y compris quand plusieurs types sont cochés simultanément
- Vérifier sur appareil si l'audio fonctionne directement en voix FR, ou en fallback voix système selon la config TTS du téléphone
- Vérifier sur appareil que le flux de démarrage propose bien l'installation/configuration TTS quand seule une voix anglaise est disponible

### 📊 Statut Global
```
Review transition: ✅ verso suivant masqué tant que non révélé
Animation flip:    ✅ conservée
Branche Git:       ✅ integration/polo-1-2026-04-04 active
Session size:      ✅ snapshot incompatible rejeté, taille effective réappliquée
Admin review test: ✅ sous-flux forçables en isolation pour QA admin
Admin normal mode: ✅ preset explicite réaligné sur la vraie révision normale complète
Admin test volume: ✅ taille de session réappliquée sur plusieurs items forcés
Audio Review:      ✅ fallback voix système + statut TTS visible
TTS startup:       ✅ demande d'installation/configuration FR au lancement si absente
TTS popup:         ✅ confirmation utilisateur avant ouverture des écrans système
Admin normal toggle: ✅ panneau avancé rétractable + neutralisation réelle des forçages admin
Validation:        ✅ compilation + tests Review/Admin/TTS OK
```

---

## 📅 2026-04-04 - Correctif accueil vide

### ✅ Accompli
- [x] Audit de `DashboardScreen.kt` sur le cas `totalCount == 0`
- [x] Correctif UX de l'accueil quand la base est vide
  - l'écran d'accueil ne disparaît plus derrière un message centré plein écran
  - les sections et actions du dashboard restent visibles même avec `0` mot
  - ajout d'un bandeau d'état vide avec CTA immédiat `+ Ajouter mes premiers mots`
  - conservation du message d'import automatique sans bloquer l'accès aux fonctionnalités
- [x] Validation statique du fichier Compose modifié
- [x] Audit métier accueil vs entraînement
  - clarification validée côté produit : une carte reste `En cours` tant qu'au moins une face n'est pas `À travailler` et que les 2 faces ne sont pas encore `Connues`
  - l'incohérence observée côté entraînement provenait ici d'un filtrage admin actif, pas du sélecteur de session
- [x] Ajustement visuel de `ReviewScreen.kt`
  - suppression de la ligne vide sous la barre de progression quand aucun titre d'événement / état audio n'est affiché
  - la phrase de contexte revient désormais juste sous la barre de session
- [x] Compactage des cartes de révision
  - réduction légère des tailles de police adaptatives pour faire rentrer plus de contenu sans scroll
  - bouton audio déplacé inline à droite du texte avec repli automatique à la ligne si nécessaire
  - boutons favori / suppression déplacés du bandeau haut vers le bas du verso, sur la même ligne que `Plus d'infos`
  - suppression de l'inset haut réservé à l'ancien bandeau d'actions afin de faire commencer le contenu plus haut
- [x] Réagencement des contrôles fixes de révision
  - contenu du verso recentré verticalement quand la carte garde sa hauteur minimale et que peu d'informations sont affichées
  - barre `Annuler / options audio` déplacée hors du flux scrollable et fixée en bas de l'écran
  - barre primaire `Voir réponse` / `À revoir - Je l'ai - Trop facile` déplacée elle aussi en bas, sous la barre utilitaire
- [x] Ajustement final du verso et des contrôles fixes
  - suppression du fond gris derrière les lignes de boutons fixes du bas
  - verso structuré en 2 zones centrées dans la carte, chacune recentrée dans sa portion
  - répartition par défaut 50/50, avec extension progressive de la zone définition jusqu'à 80% pour les définitions longues
  - ligne `Plus d'infos / favori / suppression` conservée en bas de la carte
- [x] Centrage global du bloc de révision
  - le bloc `question de contexte + carte` est maintenant centré verticalement entre la barre de progression et la première barre fixe du bas
  - les autres états (`QCM`, `matching`, `challenge`, chargement, état vide) conservent leur layout dédié
- [x] Correctif anti-flash entre deux cartes
  - suppression de la transition de contenu entre cartes normales pour éviter l'apparition furtive du verso de la carte suivante après une notation
  - le flip recto/verso de la carte courante est conservé

### 🔴 Bloquants
- Aucun

### 🔜 Prochaines actions
- Vérifier sur émulateur le comportement du dashboard au tout premier lancement avec base vide
- Vérifier que l'ajout manuel depuis l'accueil est fluide avant et après l'import automatique

### 📊 Statut Global
```
Accueil vide:    ✅ actions conservées + CTA d'ajout visible
Navigation home: ✅ non bloquante quand la base est vide
UI review:       ✅ espacement header resserré
Cartes review:   ✅ plus compactes + audio inline + actions bas verso
Barres fixes:    ✅ contrôles de review fixés en bas
Verso review:    ✅ 2 zones centrées + fond fixe supprimé
Bloc review:     ✅ carte + question centrées dans l'espace utile
Transition carte: ✅ flash du verso suivant supprimé
Validation:      ✅ contrôle statique OK
```

---

## 📅 2026-04-03 - Correctifs révision + navigation recherche

### ✅ Accompli
- [x] Audit du flux de révision (`ReviewViewModel`, `ReviewSessionPlanner`, `ReviewSessionEngine`) et de la logique de classement (`ReviewCardAggregateState`)
- [x] Correctif du cas “aucune carte à réviser” quand des cartes existent mais que certaines lignes de `review_question_progress` sont manquantes
  - auto-réparation des progressions manquantes à l’ouverture d’une session
  - nouveau chemin de planification à partir des progressions déjà connues/réparées
- [x] Sauvegarde partielle implémentée lors d’une réinitialisation de session / changement de réglages
  - les questions déjà validées sont maintenant persistées
  - leur prochain délai de présentation est recalculé et sauvegardé
- [x] UI : message `Aucune carte à réviser` centré verticalement dans l’écran de révision
- [x] Navigation : renforcement du retour `Accueil` via la bottom bar depuis les écrans secondaires (liste/recherche)
- [x] Tests unitaires ajoutés
  - réparation automatique des progressions manquantes
  - persistance partielle sur reset de session
  - couverture du nouveau chemin `buildPlanFromProgress`
- [x] Correctif `KeywordExtractor`
  - la tokenisation sépare désormais correctement sur la ponctuation/apostrophe au lieu de fusionner les mots
  - normalisation des stopwords alignée avec la suppression des accents
- [x] Suite complète `testDebugUnitTest` repassée en vert
- [x] Optimisation du lancement de l'app
  - ajout d'un vrai thème de démarrage `SplashScreen` pour supprimer l'effet d'écran noir avant le premier frame
  - initialisation TTS différée : `ReviewViewModel` ne crée plus `TextToSpeech` au démarrage global
- [x] Bottom bar compactée
  - hauteur réduite en mode normal (~62dp)
  - hauteur augmentée uniquement si un label nécessite réellement 2 lignes
  - suppression des insets automatiques Material3 qui gonflaient la barre

### 🔴 Bloquants
- Aucun

### 🔜 Prochaines actions
- Vérifier sur émulateur le retour à l’accueil depuis la page de recherche/liste de mots
- Vérifier en condition réelle qu’un reset de session conserve bien les mots déjà validés
- Vérifier sur appareil réel la perception du splash et le confort tactile de la bottom bar compactée

### 📊 Statut Global
```
Révision:        ✅ auto-réparation + sauvegarde partielle
Navigation:      ✅ retour Accueil renforcé
UI Review:       ✅ état vide centré
Tests ciblés:    ✅ OK
Suite complète:  ✅ OK
Démarrage:       ✅ splash propre + TTS différé
Bottom bar:      ✅ compacte + adaptative
```

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
- [x] Features.md mis à jour : 10/10 mini-jeux ✅

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



## 📅 2026-03-16 - Optimisation Build & Refonte UI Mini-Jeux

### ✅ Accompli
- **Optimisation Build Gradle** : Activation exécution parallèle (`org.gradle.parallel=true`) et cache de construction.
- **Navigation Refactoring** : Extraction de la classe `Screen` dans un fichier dédié (`presentation/navigation/Screen.kt`).
- **Refonte UI Mini-Jeux** :
  - Création du composant `GameTopAppBar` unifié (titre, score, progression).
  - Suppression des anciens headers encombrants pour maximiser l'espace de jeu.
  - Mise à jour de TOUS les écrans de jeu : Matching, Anagrams, FillWord, Memory, Semantic, Chrono, SpellingAdvanced, QCM, Hangman, SpellingGame.
- **Règles "Correspondance" (Matching)** :
  - Implémentation logique "3 erreurs = solution + 0 XP".
  - Pénalité XP (division par 2) en cas de retry.
  - Feedback visuel clair (Succès, Échec, Solution).
- **Correctifs divers** :
  - Migration icônes Material (AutoMirrored).
  - Fix imports manquants et références (`currentIndex`).
  - Nettoyage code mort et corrections de compilation.

### 🔴 Bloquants
- Aucun. **BUILD SUCCESSFUL**.

### 🔜 Prochaines actions
- Vérification visuelle sur appareil/émulateur.
- Finalisation des tâches d'intégration restantes.

### 📊 Statut Global
- Mini-jeux:      10/10 (UI unifiée et optimisée)
- Build:          ✅ SUCCESSFUL (Optimisé)
- UX/UI:          ✅ Améliorée (TopBar compacte, espace utile maximisé)

---

## 📅 2026-03-24 - Refonte Auth UI + Google Sign-In

### ✅ Accompli
- [x] Relecture `START_HERE.md` avant intervention
- [x] Refonte visuelle de `LoginScreen.kt` et `RegisterScreen.kt` avec carte centrée, branding `Lexica` et hiérarchie plus propre
- [x] Suppression du doublon visuel `Connexion` / `Inscription` en retirant la top bar globale sur les routes auth
- [x] Remplacement du faux bouton Google par un vrai flux `GoogleSignInClient` → `idToken` → Firebase Auth
- [x] Ajout du helper partagé `GoogleSignInHelper.kt`
- [x] Ajout de `play-services-auth` dans `app/build.gradle.kts`
- [x] Gestion UX annulation / erreur Google dans `LoginViewModel.kt` et `RegisterViewModel.kt`
- [x] Intégration de la branche `espace-de-travail-2026-03-15` dans `main`
- [x] Création du tag `avant-algos-delais-approvisionnement-2026-03-24`
- [x] Création de la nouvelle branche de travail `espace-de-travail-2026-03-24`
- [x] **Passe UX Review / cartes d'entraînement**
  - bouton audio/paramètres rendu plus explicite avec icône hybride son + réglages
  - intitulé au-dessus de la carte désormais dynamique selon le mode (`mot -> définition`, `définition -> mot`, défi)
  - correction du vrai sens de présentation des cartes `Définition -> Mot`
  - carte agrandie avec hauteur mini ~50% écran et adaptation du texte à la taille disponible
  - verso refondu : séparation visuelle courte entre mot et définition + détails plus lisibles
  - correction affichage des entités HTML dans les détails (`&nbsp;` etc.)
  - couleurs des boutons de notation fixées pour ne plus dépendre du thème
  - menu audio réancré au bouton hybride son/réglages
  - flip corrigé : c'est bien toute la carte qui pivote
  - en mode `Définition -> Mot`, le verso affiche désormais la définition au-dessus puis le mot en dessous
  - status bar claire corrigée (heure / batterie / notifications visibles)
  - titre écran révision renommé en `Apprendre mes mots`
  - **BUILD SUCCESSFUL** ✅ (`:app:assembleDebug`)
- [x] **Documentation algo révision / délai / présentation**
  - nouvelle spec ajoutée : `docs/specifications/fonctionnement algo délai et présentation cards.md`
  - intégration des corrections métier : priorité des questions déjà commencées, ordre global vs ordre de session, état `à travailler`, persistance de session, matching/QCM/défis intégrés
  - proposition d'une fonction de calcul du délai avec niveau continu + paliers `t0..t8` + réserve de récupération après échec
  - `docs/INDEX_DOCUMENTS.md` mis à jour pour référencer cette nouvelle base de vérité
  - réalignement bref des docs actives qui résumaient encore l'ancien fonctionnement : `FEATURES.md`, `docs/guides/CONSIGNES_TACHES.md`, `docs/guides/GUIDE_CHEF_DORCHESTRE.md`

### 🔴 Bloquants
- Build et test runtime du flux Google à valider après intégration

### 🔜 Prochaines actions
- Recompiler le projet
- Tester `Continuer avec Google` sur connexion et inscription
- Vérifier le comportement premier accès vs compte déjà existant

### 📊 Statut Global
```
Auth UI:         ✅ Refonte visuelle
Google Sign-In:  ✅ Implémenté côté app
Build:           ✅ `:app:assembleDebug` SUCCESSFUL
Git:             ✅ `main` alignée + nouvelle branche `espace-de-travail-2026-03-24`
Review UX:       ✅ Cartes agrandies + libellés dynamiques + vrai double sens
Algo délai/doc:  ✅ Spec exhaustive ajoutée
```

---

## 📅 2026-03-25 - Opérationnalisation algo review / délais / sessions

### ✅ Accompli
- [x] Création de la branche de travail `integration/Polo-1` *(équivalent Git valide de la demande `integration Polo-1`)*
- [x] Vérification et correction de la spec source `docs/specifications/fonctionnement algo délai et présentation cards.md`
- [x] Création du document d'opérationnalisation : `docs/specifications/operationalisation algo délai et présentation cards.md`
- [x] Découpage du chantier dans `docs/guides/CONSIGNES_TACHES.md` en série : `TACHE Polo-1 1` à `TACHE Polo-1 8`
- [x] Traduction de la spec en plan technique concret :
  - nouveaux modèles persistants par question
  - persistance de session interrompue
  - moteur de calcul du délai dédié
  - planificateur de session
  - moteur de validation locale de session
  - mapping précis des fichiers à créer / modifier
  - séquence d'implémentation par phases

### 🔴 Bloquants
- La commande de build intégrée n'est pas disponible dans l'environnement actuel (`Build.BuildSolution` indisponible)
- Le chantier code n'est pas encore démarré : seul le cadrage technique a été produit à ce stade

### 🔜 Prochaines actions
- Implémenter la persistance par question et la migration Room
- Introduire `ReviewIntervalEngine` puis `ReviewSessionPlanner`
- Refondre `ReviewViewModel.kt` autour d'un vrai moteur de session locale
- Rebrancher QCM / matching / défis sur le nouveau flux

### 📊 Statut Global
```
Spec algo review:         ✅ Finalisée
Opérationnalisation:      ✅ Documentée
Découpage en tâches:      ✅ Créé dans `CONSIGNES_TACHES.md`
Branche de travail:       ✅ integration/Polo-1
Implémentation moteur:    🔴 À démarrer
Session persistante:      🔴 À implémenter
Réglage lot initial=10:   🔴 À réaligner dans le code
Build:                    ⚠️ Non exécutable via l'outil intégré actuel
```

---

## 📅 2026-03-25 - Implémentation `Polo-1 1` (persistance par question)

### ✅ Accompli
- [x] Ajout du modèle domaine `ReviewQuestionType` + `ReviewQuestionProgress`
- [x] Ajout de la persistance Room `review_question_progress`
  - entité `ReviewQuestionProgressEntity`
  - DAO `ReviewQuestionDao`
  - mapper `ReviewQuestionMapper`
- [x] Extension de `FlashcardRepository` / `FlashcardRepositoryImpl` pour lire/écrire la progression par question
- [x] Synchronisation automatique des 2 progressions question lors de :
  - sauvegarde d'une carte
  - mise à jour SM2 d'une carte
  - suppression d'une carte
  - ajout depuis la réserve
  - import initial des cartes legacy
- [x] Passage Room en version `6`
- [x] Ajout de la migration `MIGRATION_5_6`
  - création de la table `review_question_progress`
  - migration des blocs `sm2_mot_vers_def_*` et `sm2_def_vers_mot_*`
  - génération de 2 lignes question par carte existante
- [x] Ajout de tests unitaires ciblés :
  - `ReviewQuestionMapperTest`
  - `FlashcardRepositoryImplTest`
  - mise à jour `WordReserveRepositoryImplTest`
- [x] Validation Gradle ciblée : `BUILD SUCCESSFUL` sur les tests unitaires `Polo-1 1`

### 🔴 Bloquants
- Aucun bloquant sur `Polo-1 1` après validation ciblée
- Les warnings restants du build sont hors scope `Polo-1 1` (dépréciations UI / Firebase existantes)

### 🔜 Prochaines actions
- Implémenter `Polo-1 2` : `ReviewIntervalEngine`
- Brancher ensuite `Polo-1 3` : `ReviewSessionPlanner`
- Remplacer enfin la sélection carte-level par la sélection question-level dans `Review`

### 📊 Statut Global
```
Polo-1 1 persistance:     ✅ Implémenté
Migration Room 5 -> 6:    ✅ Ajoutée
Sync carte <-> question:  ✅ En place
Tests unitaires ciblés:   ✅ Verts
Polo-1 2 interval engine: 🔴 À démarrer
```

### 🔧 Complément - Correctif `ReviewViewModelTest`
- [x] Réalignement de `ReviewViewModel.kt` avec le contrat historique des défis intégrés
  - déclenchement du défi après `quality >= 3` et `correctReviews % 3 == 0`
  - pas d'avance immédiate à la carte suivante tant que `dismissChallenge()` n'est pas appelé
  - retrait des modes défi de la rotation normale des cartes
- [x] Vérification qu'il n'y a pas d'impact sur `Polo-1 1` (persistance par question inchangée)
- [x] Validation combinée réussie :
  - `ReviewViewModelTest`
  - `FlashcardEntityTest`
  - `ReviewQuestionMapperTest`
  - `FlashcardRepositoryImplTest`
  - `WordReserveRepositoryImplTest`

### ✅ Avancement - `Polo-1 2` (`ReviewIntervalEngine`)
- [x] Création de `ReviewAnswer` pour formaliser les premières réponses de session (`AGAIN`, `GOT_IT`, `TOO_EASY`)
- [x] Création du moteur pur `ReviewIntervalEngine`
  - ratio de maîtrise `R`
  - éligibilité temporelle `E`
  - gestion de `À revoir`, `Je l'ai`, `Trop facile`
  - conversion `intervalIndex -> durée`
- [x] Alignement de `ReviewQuestionMapper.kt` sur la même conversion de durée pour éviter les divergences avec `Polo-1 1`
- [x] Ajout de `ReviewIntervalEngineTest`
  - chute à `t0`
  - montée sur `Je l'ai`
  - effet `Trop facile`
  - récupération plus rapide d'une question forte
  - franchissement du seuil `t4`
  - pénalisation d'une revue trop précoce
- [x] Validation Gradle ciblée réussie sur :
  - `ReviewIntervalEngineTest`
  - `ReviewQuestionMapperTest`
  - `FlashcardRepositoryImplTest`
- [x] Non-régression ciblée réussie sur :
  - `ReviewViewModelTest`
  - `WordReserveRepositoryImplTest`
  - `FlashcardEntityTest`

### 📊 Statut Global
```
Polo-1 1 persistance:      ✅ Implémenté
Polo-1 2 interval engine:  ✅ Implémenté
Mapper durée unifié:       ✅ Aligné
Tests interval engine:     ✅ Verts
Polo-1 3 planner:          🔴 À démarrer
```

### ✅ Avancement - `Polo-1 3` (`ReviewSessionPlanner`)
- [x] Vérification qu'il n'y avait pas de reliquat bloquant à intégrer avant `Polo-1 3`
- [x] Extension du contrat data pour le planner :
  - lecture des questions déjà commencées et dues
  - lecture des questions jamais commencées
  - comptages exacts pour `remainingQuestionsCount`
- [x] Création du modèle `ReviewSessionPlan`
- [x] Création de `ReviewSessionPlanner`
  - construction de l'ordre global priorisé
  - priorité aux questions déjà commencées et dues
  - sous-priorité aux plus anciennes dues (`nextDueAt` le plus ancien)
  - complément par questions jamais commencées, plus anciennes d'abord (`globalOrder`)
  - maintien des jumelles consécutives à l'intérieur de chaque bucket de priorité
  - construction d'un ordre de session mélangé avec contrainte anti-jumelles successives si possible
- [x] Ajout de tests unitaires ciblés :
  - `ReviewSessionPlannerTest`
  - compléments `FlashcardRepositoryImplTest`
- [x] Validation Gradle ciblée réussie sur :
  - `ReviewSessionPlannerTest`
  - `FlashcardRepositoryImplTest`
  - `ReviewIntervalEngineTest`
- [x] Non-régression complémentaire réussie sur :
  - `ReviewViewModelTest`
  - `WordReserveRepositoryImplTest`
  - `ReviewQuestionMapperTest`
  - `FlashcardEntityTest`

### 📊 Statut Global
```
Polo-1 1 persistance:      ✅ Implémenté
Polo-1 2 interval engine:  ✅ Implémenté
Polo-1 3 planner:          ✅ Implémenté
Tests planner ciblés:      ✅ Verts
Polo-1 4 session engine:   🔴 À démarrer
```

### ✅ Avancement - `Polo-1 4` (`ReviewSessionEngine`)
- [x] Création des modèles de session locale :
  - `ReviewSessionValidationReason`
  - `ReviewSessionQuestionState`
  - `ReviewSessionState`
  - `ReviewSessionCompletion`
- [x] Création de `ReviewSessionEngine`
  - initialisation depuis `ReviewSessionPlan`
  - boucle locale sur lot fixe dans l'ordre de session
  - saut progressif des questions validées
  - finalisation long terme uniquement à la fin de session
- [x] Règles métier implémentées :
  - `2 x Je l'ai`
  - `1 x Trop facile`
  - `1 x Je l'ai` dès la première présentation si intervalle `> t2`
  - exception si première réponse = `À revoir`
  - sortie forcée après `5 x À revoir`
  - cas particulier `Je l'ai` non validant puis `5 x À revoir` => long terme = `À revoir`
- [x] Conservation de la première réponse de session comme vérité long terme locale, sans persistance immédiate
- [x] Ajout de `ReviewSessionEngineTest`
- [x] Validation Gradle ciblée réussie sur :
  - `ReviewSessionEngineTest`
  - `ReviewSessionPlannerTest`
  - `ReviewIntervalEngineTest`
- [x] Non-régression complémentaire réussie sur :
  - `ReviewViewModelTest`
  - `FlashcardRepositoryImplTest`
  - `WordReserveRepositoryImplTest`
  - `ReviewQuestionMapperTest`
  - `FlashcardEntityTest`

### 📊 Statut Global
```
Polo-1 1 persistance:      ✅ Implémenté
Polo-1 2 interval engine:  ✅ Implémenté
Polo-1 3 planner:          ✅ Implémenté
Polo-1 4 session engine:   ✅ Implémenté
Tests session engine:      ✅ Verts
Polo-1 5 persistance:      🔴 À démarrer
```

### ✅ Avancement - `Polo-1 5` (session persistante + reprise + annulation)
- [x] Ajout du stockage Room de snapshot de session :
  - `ReviewSessionSnapshotEntity`
  - `ReviewSessionSnapshotDao`
  - `ReviewSessionSnapshotRepository`
  - `ReviewSessionSnapshotRepositoryImpl`
- [x] Passage Room en version `7` avec migration `6 -> 7` pour `review_session_snapshots`
- [x] Ajout des modèles domaine de snapshot :
  - `ReviewSessionSnapshot`
  - `ReviewSessionSnapshotState`
  - `ReviewChallengeResultSnapshot`
- [x] Intégration dans `ReviewViewModel` :
  - restauration automatique d'une session active dans `loadSession()`
  - persistance du snapshot après chargement/reveal/réponse/challenge/navigation de session
  - conservation de l'état local exact (carte courante, pile pending, mode, challenge, XP, reveal, etc.)
  - support d'annulation de la dernière réponse via snapshot `undoState`
  - support d'annulation de session avec purge du snapshot
- [x] Intégration dans `ReviewScreen` :
  - bouton `ANNULER DERNIERE REPONSE`
  - bouton `ANNULER SESSION`
- [x] Correction du câblage `MainActivity` / `ReviewViewModelFactory` pour injecter le repository de snapshot
- [x] Tests ciblés ajoutés / validés :
  - `ReviewSessionSnapshotRepositoryImplTest`
  - compléments `ReviewViewModelTest`
- [x] Validation Gradle ciblée réussie sur :
  - `ReviewViewModelTest`
  - `ReviewSessionSnapshotRepositoryImplTest`
- [x] Non-régression complémentaire réussie sur :
  - `ReviewIntervalEngineTest`
  - `ReviewSessionPlannerTest`
  - `ReviewSessionEngineTest`
  - `FlashcardRepositoryImplTest`
  - `WordReserveRepositoryImplTest`
  - `ReviewQuestionMapperTest`
  - `FlashcardEntityTest`

### 📊 Statut Global
```
Polo-1 1 persistance:      ✅ Implémenté
Polo-1 2 interval engine:  ✅ Implémenté
Polo-1 3 planner:          ✅ Implémenté
Polo-1 4 session engine:   ✅ Implémenté
Polo-1 5 persistance:      ✅ Implémenté
Tests reprise/undo:        ✅ Verts
Polo-1 6 review flow:      🔴 À démarrer
```

### 🧹 Clôture de journée - nettoyage warnings
- [x] Nettoyage des derniers warnings locaux encore remontés par l'analyse du workspace
- [x] Correction du faux problème `BoxWithConstraints scope is not used` dans `ReviewScreen.kt`
- [x] Nettoyage des faux positifs restants sur le dialogue de suppression et la migration Room `6 -> 7`
- [x] Validation finale par `:app:compileDebugKotlin` => `BUILD SUCCESSFUL`

---

## 📅 2026-03-25 - Reprise Polo-1 6 (`Review` piloté par session)

### ✅ Accompli
- [x] Relecture de `START_HERE.md`, des specs Polo-1 et du journal existant avant reprise du chantier
- [x] Refonte de `ReviewViewModel.kt` pour piloter le flux via `ReviewSessionPlanner` + `ReviewSessionEngine`
- [x] Passage du chargement `Review` d'une file FIFO de cartes à une session par **questions**
- [x] Compteur UI réaligné sur le **nombre de questions restantes à valider**
- [x] Commit long terme déplacé en **fin de session** via `repository.saveQuestionProgress(...)`
- [x] Reprise/undo conservés via snapshot enrichi (`sessionCards`, `sessionPlan`, `sessionState`)
- [x] `ReviewScreen.kt` mis à jour pour afficher explicitement le compteur restant et une fin de session orientée questions validées
- [x] `ReviewViewModelTest.kt` réécrit pour couvrir le nouveau flux Polo-1
- [x] Validations Gradle ciblées réussies :
  - `ReviewViewModelTest`
  - `ReviewSessionSnapshotRepositoryImplTest`
  - `ReviewSessionEngineTest`
  - `ReviewSessionPlannerTest`
  - `ReviewIntervalEngineTest`
- [x] Non-régression complémentaire verte sur :
  - `FlashcardRepositoryImplTest`
  - `ReviewQuestionMapperTest`
  - `FlashcardEntityTest`
  - `WordReserveRepositoryImplTest`

### 🔴 Bloquants
- Aucun bloquant fonctionnel détecté sur Polo-1 6
- Reste hors scope de cette étape : `Polo-1 7` (QCM, matching, orthographe additionnelle, défis remplaçants)

### 🔜 Demain
- Attaquer `Polo-1 7` pour injecter les activités annexes dans le flux `Review`
- Décider si les anciens snapshots pré-Polo-1 6 doivent être migrés ou simplement purgés à l'ouverture

### 📊 Statut Global
```
Polo-1 1 persistance:      ✅ Implémenté
Polo-1 2 interval engine:  ✅ Implémenté
Polo-1 3 planner:          ✅ Implémenté
Polo-1 4 session engine:   ✅ Implémenté
Polo-1 5 persistance:      ✅ Implémenté
Polo-1 6 review flow:      ✅ Implémenté
Polo-1 7 side events:      🔴 À démarrer
Tests Review/session:      ✅ Verts
```

---

## 📅 2026-03-25 - Polo-1 7 (`Review` + événements annexes)

### ✅ Accompli
- [x] Intégration des événements annexes dans `ReviewViewModel.kt` :
  - QCM inséré au `3e À revoir`
  - matching intégré quand le volume de travail atteint la taille du lot initial
  - question orthographique additionnelle pour les questions `WORD_TO_DEFINITION`
  - défi remplaçant branché dans le flux de session
- [x] Extension du moteur local `ReviewSessionEngine` avec crédits d’événements (`applyEventGotIt`, `applyEventAgain`) et drapeaux de planification
- [x] Ajout des modèles domaine d’événements de session :
  - `ReviewSessionEvent`
  - `ReviewSessionEventType`
  - `ReviewSessionChallengeKind`
- [x] Enrichissement du snapshot de session pour reprise/undo exacts avec événements actifs et file d’insertions
- [x] Refonte de `ReviewScreen.kt` pour rendre les items polymorphes :
  - question normale
  - QCM
  - matching
  - orthographe additionnelle
  - défi
- [x] Réécriture des tests `ReviewViewModelTest.kt` pour couvrir les branches Polo-1 7
- [x] Extension de `ReviewSessionEngineTest.kt` pour les crédits locaux d’événements et les flags de planification

### 🔴 Bloquants
- Aucun bloquant technique détecté après validation
- Point fonctionnel à garder en tête : le cas "3 × Je l’ai d’affilée" reste structurellement rare avec la validation locale en `2 × Je l’ai`

### 🔜 Demain
- Vérifier sur appareil/émulateur le confort UX des sous-flux QCM/matching/défi dans `Review`
- Éventuellement raffiner le positionnement aléatoire ou l’habillage visuel des événements annexes

### 📊 Statut Global
```
Polo-1 1 persistance:      ✅ Implémenté
Polo-1 2 interval engine:  ✅ Implémenté
Polo-1 3 planner:          ✅ Implémenté
Polo-1 4 session engine:   ✅ Implémenté
Polo-1 5 persistance:      ✅ Implémenté
Polo-1 6 review flow:      ✅ Implémenté
Polo-1 7 side events:      ✅ Implémenté
Tests Review/session:      ✅ Verts
```

### ✅ Correctif métier complémentaire - Défis remplaçants persistants
- [x] Réalignement des défis remplaçants sur la clarification produit :
  - déclenchement armé en persistant par question
  - consommation à la **première présentation d'une session ultérieure**
  - le défi **remplace** cette première présentation au lieu de venir en plus
  - la réponse au défi compte désormais comme **première réponse de session** pour le calcul du délai
- [x] Ajout d'un champ persistant `pendingReplacementChallengeKind` sur `ReviewQuestionProgress`
- [x] Passage Room en version `8` + migration `7 -> 8`
- [x] `ReviewViewModel` corrigé pour :
  - armer le défi au commit de fin de session si le seuil persistant est atteint
  - consommer le défi au début d'une session future
  - annuler la question orthographique additionnelle si un défi orthographique persistant est déjà prévu pour la carte
- [x] Validation ciblée réussie sur :
  - `ReviewViewModelTest`
  - `ReviewSessionEngineTest`
  - `ReviewQuestionMapperTest`
  - `ReviewSessionSnapshotRepositoryImplTest`
- [x] Non-régression complémentaire réussie sur :
  - `ReviewSessionPlannerTest`
  - `ReviewIntervalEngineTest`
  - `FlashcardRepositoryImplTest`
  - `WordReserveRepositoryImplTest`
  - `FlashcardEntityTest`
- [x] Mise à jour des documents de référence :
  - `docs/specifications/fonctionnement algo délai et présentation cards.md`
  - `docs/specifications/operationalisation algo délai et présentation cards.md`
  - clarifications intégrées sur défis remplaçants persistants, impact délai, matching et orthographe additionnelle

---

## 📅 2026-03-25 - Polo-1 8 (projections produit + réglages + listes)

### ✅ Accompli
- [x] Ajout du modèle de projection produit par carte :
  - `ReviewCardAggregateState`
  - `ReviewCardProgressSummary`
- [x] Recalcul des états produit à partir de la progression **par question** au lieu du vieux `state` carte-level
- [x] Réalignement du dashboard sur la nouvelle terminologie :
  - `À apprendre` → `À travailler`
  - filtres `TO_WORK` / `IN_PROGRESS` / `KNOWN`
- [x] Refonte de `WordListViewModel.kt` pour filtrer la liste selon la vérité par question
- [x] Refonte de `WordListScreen.kt` pour afficher, dans une même carte visuelle :
  - l'état agrégé de la carte
  - le statut `Mot -> Définition`
  - le statut `Définition -> Mot`
- [x] Réalignement de `WordDetailScreen.kt` sur la même projection produit
- [x] Réglages session réalignés sur la spec :
  - `UserPrefsRepository.DEFAULT_CARDS_PER_SESSION = 10`
  - `AdminPrefsRepository.DEFAULT_SESSION_SIZE = 10`
  - nouveau libellé UI orienté **lot initial de travail**
- [x] `SettingsScreen.kt` expose désormais le réglage utilisateur du lot initial avec texte explicatif non ambigu
- [x] `ReviewViewModel.kt` utilise maintenant :
  - le réglage utilisateur pour les comptes non-admin
  - l'override admin seulement pour les comptes admin
  - tout en conservant les petits overrides explicites internes utiles aux tests
- [x] Tests ajoutés / mis à jour :
  - `ReviewCardProgressSummaryTest`
  - `WordListViewModelTest`
  - ajustements `ReviewViewModelTest`
- [x] Validation Gradle ciblée réussie sur :
  - `ReviewCardProgressSummaryTest`
  - `WordListViewModelTest`
  - `ReviewViewModelTest`
  - `ReviewSessionEngineTest`
  - `ReviewSessionPlannerTest`
  - `ReviewIntervalEngineTest`
  - `FlashcardRepositoryImplTest`
  - `ReviewQuestionMapperTest`

### 🔴 Bloquants
- Aucun bloquant technique sur cette étape après validation ciblée
- Point de doc à clarifier plus tard : la section `TACHE Polo-1 8` de `CONSIGNES_TACHES.md` contient un paragraphe sur la récupération de progression cloud qui semble hors périmètre de la projection produit / listes

### 🔜 Demain
- Vérifier visuellement sur appareil/émulateur le rendu des doubles statuts dans `Mes mots`
- Décider si `CONSIGNES_TACHES.md` doit être nettoyé / scindé pour retirer le paragraphe sync parasite de `Polo-1 8`
- Si tout est validé visuellement, marquer `Polo-1 8` comme clôturée dans la doc de tâches

### 📊 Statut Global
```
Polo-1 1 persistance:      ✅ Implémenté
Polo-1 2 interval engine:  ✅ Implémenté
Polo-1 3 planner:          ✅ Implémenté
Polo-1 4 session engine:   ✅ Implémenté
Polo-1 5 persistance:      ✅ Implémenté
Polo-1 6 review flow:      ✅ Implémenté
Polo-1 7 side events:      ✅ Implémenté
Polo-1 8 projections/listes: ✅ Implémentation principale + tests verts
```

### 🎨 Correctif UI complémentaire - écran `Review`
- [x] Barre de navigation basse masquée sur `Screen.Review`
- [x] Layout `ReviewScreen.kt` rendu scrollable pour garantir l'accès manuel aux boutons d'action
- [x] Réduction des paddings internes des cartes (`ReviewFrontFace`, `ReviewBackFace`, `AudioTextLine`, `EventSurface`)
- [x] Réduction de la hauteur mini/maxi de la carte pour éviter qu'elle n'écrase les actions
- [x] Correction du reveal : la **réponse** est maintenant en gras selon le type de question, plus systématiquement le mot
- [x] Compteur retravaillé en tuiles compactes (`Restantes` / `Validées`)
- [x] Libellé de type de question raccourci et rendu discret (non gras) : `Mot -> Définition` / `Définition -> Mot`
- [x] Validation ciblée par `:app:compileDebugKotlin` => `BUILD SUCCESSFUL`

### 🎯 Ajustements utilisateur complémentaires - `Review`
- [x] Top bar globale restaurée sur `Review` avec retour standard, bottom bar seule masquée
- [x] Action `ANNULER SESSION` retirée de l'écran
- [x] Compteurs replacés au-dessus des actions son / favori / suppression
- [x] Phrases longues de guidage restaurées (`Quelle est la définition du mot ?`, etc.) et déplacées en aide discrète sous l'en-tête
- [x] Carte remontée à un minimum de `50%` de la hauteur écran, tout en gardant l'accès aux boutons via scroll
- [x] Logique de mise en gras recto/verso réalignée sur la consigne produit :
  - `Mot -> Définition` : recto mot gras centré, verso mot gras en haut + définition dessous
  - `Définition -> Mot` : recto définition grasse centrée, verso définition grasse en haut + mot dessous
- [x] Correction TTS : fallback sur plusieurs locales françaises (`Locale.FRANCE`, `Locale.FRENCH`, `fr_FR`, `fr`) pour limiter les cas `Langue TTS non disponible`
- [x] Feedback `Question additionnelle passée` rendu visuellement négatif (fond rouge)
- [x] Validation ciblée par `:app:compileDebugKotlin` => `BUILD SUCCESSFUL`

### 🔌 Rebranchement des paramètres admin Review
- [x] Clarification du rôle des paramètres admin : ils servent de **sélecteur dev** pour isoler les mécaniques d'entraînement sans toucher à l'UX normale
- [x] Rebranchement des types principaux de question dans `ReviewViewModel.kt` :
  - `Mot -> Définition`
  - `Définition -> Mot`
- [x] Ajout + branchement des toggles admin manquants pour les annexes Review :
  - `Question orthographique`
  - `QCM intégré`
  - `Correspondance`
- [x] Rebranchement des défis remplaçants existants :
  - `Défi orthographique`
  - `Défi sémantique`
- [x] Les toggles admin n'impactent Review que si l'utilisateur courant est admin
- [x] Déplacement des boutons favori / suppression directement sur la carte Review
- [x] Top bar `Review` resserrée visuellement à ~60% de largeur
- [x] Validation ciblée :
  - `:app:compileDebugKotlin`
  - `:app:testDebugUnitTest --tests com.example.lexicaandroid2.presentation.review.ReviewViewModelTest`
  - résultat : `BUILD SUCCESSFUL`

## 📅 2026-04-03 - Ajustement UI `Review`

### ✅ Accompli
- [x] `LexicaApp.kt` : suppression de l'exception qui limitait la top bar de `Screen.Review` à `60%` de largeur
- [x] Top bar de l'écran d'apprentissage réalignée sur le comportement standard pleine largeur
- [x] Validation ciblée par `:app:compileDebugKotlin` => `BUILD SUCCESSFUL`
- [x] Éclaircissement global des fonds gris neutres via un token partagé `lexicaPanelContainerColor()`
- [x] Cartes/panneaux principaux réalignés sur ce fond plus clair : `SettingsScreen`, `ProfileScreen`, `UtilisationScreen`, `ReviewScreen`, `MiniGamesScreen`, `MatchingScreen`, `DailyChallengeScreen`, `XpProgressBar`, `ChallengeOverlay`, `GameComposables`
- [x] Réglage du lot initial de travail abaissé à un minimum de `2` cartes côté utilisateur
- [x] Alignement anti-conflit côté runtime/admin : `ReviewViewModel`, `AdminPrefsRepository`, `AdminScreen`, `AdminViewModel`
- [x] Validation ciblée complémentaire :
  - `:app:compileDebugKotlin`
  - `:app:testDebugUnitTest --tests com.example.lexicaandroid2.presentation.review.ReviewViewModelTest`
  - `:app:testDebugUnitTest --tests com.example.lexicaandroid2.domain.logic.ReviewSessionPlannerTest`
  - résultat : `BUILD SUCCESSFUL`
- [x] Branche de travail créée depuis `integration/Polo-1` : `integration/polo-1-2026-04-03`
- [x] `ReviewScreen.kt` : remplacement des tuiles `Restantes / Validées` par une barre de progression horizontale compacte avec compteur discret
- [x] `ReviewViewModel.kt` : invalidation des snapshots incompatibles avec les filtres admin (types de questions / annexes / taille de lot) pour que les réglages admin prennent effet au prochain chargement de session
- [x] `ReviewViewModelTest.kt` : ajout d'un test qui vérifie qu'un snapshot incompatible est ignoré puis recréé avec les filtres admin actifs
- [x] Validation ciblée Review/admin :
  - `:app:compileDebugKotlin`
  - `:app:testDebugUnitTest --tests com.example.lexicaandroid2.presentation.review.ReviewViewModelTest`
  - résultat : `BUILD SUCCESSFUL`
- [x] `ReviewScreen.kt` : bouton `Annuler la dernière réponse` remplacé par une flèche retour alignée à gauche sous la carte
- [x] `ReviewScreen.kt` : bouton d'options audio déplacé en bas à droite, sur la même ligne que la flèche retour
- [x] `ReviewScreen.kt` : réserve haute ajoutée au contenu de la carte pour éviter le chevauchement avec les actions favori / suppression
- [x] `ReviewViewModel.kt` : ajout d'une progression de session fractionnaire basée sur l'état réel de chaque question
  - `Je l'ai` = progression vers le seuil `2`
  - `À revoir` = progression vers le seuil `5`
  - `Trop facile` / question validée = progression immédiate à `100%` pour la question
- [x] `ReviewScreen.kt` : suppression du compteur numérique `x / y` au profit d'une barre de progression seule
- [x] `SettingsScreen.kt` : clarification du `lot initial de travail` comme **nombre de questions**, pas nombre de cartes
- [x] `SettingsScreen.kt` + `LexicaApp.kt` + `ReviewViewModel.kt` : à la sortie des réglages, si le lot initial a changé, la session `Review` est invalidée automatiquement pour appliquer le nouveau lot au prochain démarrage
- [x] `ReviewViewModelTest.kt` : ajout de tests pour
  - la progression fractionnaire après réponses `Je l'ai`
  - l'invalidation de session suite à un changement de réglage
- [x] Renommage UI : `Lot initial de travail` → `Taille de la session` dans `SettingsScreen.kt` et `AdminScreen.kt`
- [x] `ReviewScreen.kt` : suppression de l'écran bloquant `Session terminée / Retour au Menu`
- [x] Fin de session `Review` remplacée par un écran vert temporaire avec confettis + XP pendant ~2 secondes
- [x] `ReviewViewModel.kt` + `MainActivity.kt` : gain d'XP de session branché sur la taille de la session validée
- [x] Enchaînement automatique vers la session suivante après la célébration
- [x] `ReviewViewModelTest.kt` : test ajouté pour verrouiller la célébration de fin et le gain d'XP
- [x] Top bars compactées globalement (~40%) sur les composants partagés + écrans locaux restants (`LexicaTopAppBar`, `GameTopAppBar`, `WordDetail`, `Search`)
- [x] `LexicaApp.kt` : route `AddWords` rebranchée dans le titre de top bar et dans la logique de retour (`canNavigateBack`)
- [x] `LexicaApp.kt` : navigation de la bottom bar sécurisée via `popUpTo(Screen.Dashboard.route)` pour éviter le non-effet depuis `AddWords`
- [x] `ReviewScreen.kt` : fin du faux spinner infini quand aucune session/question n'est disponible
- [x] `ReviewViewModel.kt` : garde-fou ajouté dans `loadSession()` pour sortir proprement du chargement en cas d'erreur et afficher un état vide plutôt qu'un chargement bloqué

### 🔴 Bloquants
- Aucun sur ce correctif UI ciblé

### 🔜 Demain
- Vérifier visuellement en émulateur le rendu final de `Review` et l'alignement du titre avec les autres écrans
- Vérifier visuellement que les fonds éclaircis gardent un bon contraste dans `Réglages`, `Profil` et `Review`
- Valider en session courte que le lot initial à `2` cartes reste fluide avec les annexes / défis
- Vérifier en scénario admin réel que la désactivation de `Mot -> Définition`, `QCM` ou `Correspondance` relance bien une session compatible après retour dans `Review`
- Vérifier sur appareil que la nouvelle ligne d'actions sous carte reste propre pour les questions normales et les événements intégrés
- Vérifier sur appareil le ressenti de la nouvelle barre de progression sans compteur numérique
- Vérifier en réglant le lot initial à `2` puis en quittant `Réglages` que la session suivante `Review` repart bien sur un lot réduit
- Vérifier sur appareil la transition visuelle `célébration -> session suivante` quand il ne reste plus beaucoup de questions disponibles
- Vérifier sur appareil `AddWords -> retour haut-gauche` puis `AddWords -> Accueil` via bottom bar
- Vérifier sur appareil que `Review` affiche bien un état vide au lieu d'un spinner si aucune question n'est réellement disponible

### 📊 Statut Global
```
Review UI:       ✅ Top bar pleine largeur rétablie
Surfaces grises: ✅ Éclaircies globalement
Session min:     ✅ Réglable à 2 cartes sans conflit user/admin/runtime
Review header:   ✅ Barre de progression compacte à la place des deux tuiles
Progression:     ✅ Barre basée sur l'avancement réel des questions
Filtres admin:   ✅ Réappliqués en présence d'un snapshot incompatible
Actions Review:  ✅ Undo à gauche + audio à droite sous la carte
Lot initial:     ✅ Réglage clarifié (questions) + réappliqué à la sortie des réglages
Fin de session:  ✅ Célébration temporaire + auto-relance + XP de session
Top bars:        ✅ Compactées globalement
AddWords nav:    ✅ Retour haut-gauche + accueil bottom bar rebranchés
Review loading:  ✅ Fallback vide au lieu d'un spinner bloqué
Navigation:      ✅ Inchangée
Build:           ✅ `:app:compileDebugKotlin` OK
Tests ciblés:    ✅ `ReviewViewModelTest` + `ReviewSessionPlannerTest`
```

