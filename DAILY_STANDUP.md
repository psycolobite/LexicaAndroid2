# 📅 DAILY STANDUP - Journal Unique de Suivi

**Responsable:** Chef d'Orchestre  
**Fréquence:** Quotidienne  
**Format:** SEUL fichier de suivi du projet (remplace tous les "etat_*.md", "rapport_*.md", etc.)

---

## 📅 2026-04-14 — Ajustement Polo-1 : espacement minimal des occurrences d’une même carte

### ✅ Accompli
- [x] Renforcement de `Polo-1` pour éviter qu’une même carte réapparaisse avec moins de **2 autres questions** entre ses occurrences de révision, quand une alternative existe
- [x] Harmonisation des équivalences métier d’espacement
  - `définition → mot` ≈ `question orthographique` / `défi orthographique`
  - `mot → définition` ≈ `défi sémantique`
- [x] Prise en compte de **tous les items visibles** comme séparateurs valides
  - questions normales
  - événements intégrés (`QCM`, `matching`, orthographe, défis)
- [x] Ajout d’un repli contrôlé quand la session est trop petite pour respecter la contrainte
- [x] Couverture par tests unitaires sur le planner, le moteur et le `ReviewViewModel`

### 🔗 Fichiers modifiés
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewSessionSpacing.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/logic/ReviewSessionPlanner.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/logic/ReviewSessionEngine.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewSessionSnapshotState.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/review/ReviewViewModel.kt`
- `app/src/test/java/com/example/lexicaandroid2/domain/logic/ReviewSessionPlannerTest.kt`
- `app/src/test/java/com/example/lexicaandroid2/domain/logic/ReviewSessionEngineTest.kt`
- `app/src/test/java/com/example/lexicaandroid2/presentation/review/ReviewViewModelTest.kt`

---

 ## 📅 2026-04-14 — Bouton Modifier multi-écrans + éditeur prérempli + top bars sécurisées

### ✅ Accompli
- [x] Ajout d'un **flux d'édition dédié** avec écran `Modifier mon mot`
  - nouvelle route `edit_word/{cardId}`
  - écran prérempli depuis la carte existante
  - sauvegarde via mise à jour du contenu **sans écraser la progression de révision**
  - validation anti-doublon sur le mot modifié
- [x] Ajout du **bouton Modifier** dans les zones demandées
  - `Mes mots` : cartes inline + popup détail
  - `Ajouter des mots` : cartes vertes inline + popup de validation + popup mot déjà présent
  - `Apprendre mes mots` : sur le recto/verso de la carte + bouton centré dans la ligne retour / audio
- [x] Harmonisation du formulaire d'ajout manuel
  - formulaire partagé entre ajout manuel et édition
  - ajout des champs optionnels `registre` et `notes personnelles`
  - parsing plus souple des listes (`synonymes`, `exemples`) via virgules **ou** retours ligne
- [x] Amélioration de lisibilité des contenus longs
  - blocs scrollables pour les longues définitions / exemples / étymologies dans les popups et détails
- [x] Correctif top bars
  - suppression des hauteurs forcées trop basses
  - hauteur minimale augmentée sur les top bars principales / jeux / recherche / détail mot
- [x] Validation technique
  - tests unitaires ajoutés pour l'éditeur et le refresh après renommage côté `AddWordsViewModel`
  - `:app:testDebugUnitTest` ciblé **OK**
  - `:app:assembleDebug` **OK**

### 🔗 Fichiers modifiés
- `app/src/main/java/com/example/lexicaandroid2/presentation/common/WordEditComponents.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/editword/EditWordScreen.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/navigation/Screen.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/LexicaApp.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/addwords/AddWordsViewModel.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/addwords/AddWordsScreen.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/wordlist/WordListScreen.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/wordlist/WordDetailScreen.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/review/ReviewScreen.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/review/NormalQuestionContent.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/common/LexicaTopAppBar.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/games/common/GameComposables.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/search/SearchScreen.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/repository/FlashcardRepository.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/repository/FlashcardRepositoryImpl.kt`
- `app/src/test/java/com/example/lexicaandroid2/presentation/addwords/AddWordsViewModelTest.kt`
- `app/src/test/java/com/example/lexicaandroid2/presentation/editword/EditWordViewModelTest.kt`
- `app/src/test/java/com/example/lexicaandroid2/domain/logic/ReviewSessionPlannerTest.kt`

### 🔁 Ajustement complémentaire (retour QA)
- [x] **Seed initial réduit de 18 à 5 cartes** pour un premier lancement moins chargé
  - import initial désormais limité à 5 cartes depuis `local_storage.json`
  - la réserve de mots reste importée séparément, sans être affectée par cette limite
- [x] **Nettoyage des boutons dans l'entraînement**
  - suppression du bouton `Modifier` directement sur la carte
  - remplacement du bouton texte `Modifier` sur la ligne d'options par une simple icône centrée
  - boutons `favori` + `poubelle` repositionnés uniquement **en bas à droite** sur recto et verso
  - teintes restaurées : étoile jaune/grise, poubelle en couleur d'erreur

### 🔗 Fichiers modifiés (complément)
- `app/src/main/java/com/example/lexicaandroid2/data/importer/DataImporter.kt`
- `app/src/main/java/com/example/lexicaandroid2/MainActivity.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/review/NormalQuestionContent.kt`

### 🔁 Ajustement UX complémentaire (cartes inline `Mes mots`)
- [x] **Remontée de la ligne d'état sous la définition** sur les cartes inline de `Mes mots`
  - les infos `la définition • état` et `le mot • état` appartiennent maintenant à la colonne texte
  - elles ne se calent plus sous la colonne des boutons (`favori`, `poubelle`, `modifier`)
  - sur petite largeur / police agrandie, le retour à la ligne se fait dans la largeur disponible de la colonne texte sans chevauchement avec les actions

### 🔗 Fichiers modifiés (complément UX liste)
- `app/src/main/java/com/example/lexicaandroid2/presentation/wordlist/WordListScreen.kt`

### 🔁 Ajustement UX complémentaire (actions des pop-ups)
- [x] **Actions alignées sur une ligne** dans les pop-ups d'information ciblées
  - `Ajouter des mots` : popup d'une carte déjà ajoutée
  - `Mes mots` : popup détail d'une carte
  - les actions restent au même emplacement général, mais sont désormais horizontales pour un rendu plus propre

### 🔗 Fichiers modifiés (complément UX pop-ups)
- `app/src/main/java/com/example/lexicaandroid2/presentation/addwords/AddWordsScreen.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/wordlist/WordDetailScreen.kt`

### 🔁 Ajustement correctif (cible réelle des pop-ups)
- [x] **Correction du périmètre** pour l'alignement horizontal des actions
  - `Ajouter des mots` : cible confirmée = popup d'information des cartes **déjà ajoutées** ouverte au clic sur la carte
  - `Mes mots` : cible confirmée = popup détail d'une carte
  - footer d'actions désormais horizontal sur ces deux pop-ups ciblées

---

## 📅 2026-04-12 — Sélection multiple dans `Mes mots` + suppression groupée

### ✅ Accompli
- [x] Ajout d'un **mode sélection multiple par appui long** dans `presentation/wordlist/WordListScreen.kt`
  - appui long sur une carte => entrée en mode sélection et carte cochée
  - tap court en mode sélection => ajoute/retire la carte de la sélection
  - ouverture du détail désactivée pendant la sélection pour éviter les conflits d'action
- [x] Remplacement du **bandeau d'actions contextuelles** par un **menu `⋮` dans la top bar**
  - le bandeau `n carte(s) sélectionnée(s)` a été supprimé pour éviter les problèmes d'affichage
  - un bouton `⋮` apparaît à droite dans la **vraie top bar** (`Mes mots` + catégorie + retour) dès qu'au moins une carte est sélectionnée
  - le menu contient désormais : `Ajouter aux favoris`, `Supprimer`, `Réinitialiser la progression`
  - confirmations conservées pour les actions destructives (`Supprimer`, `Réinitialiser la progression`)
  - la barre de recherche conserve sa largeur normale (plus de rétrécissement lié au menu)
  - les boutons `favori` et `poubelle` des cartes restent visibles même pendant la sélection multiple
- [x] Ajout de la **logique de suppression groupée** dans `presentation/wordlist/WordListViewModel.kt`
  - état `selectedCardIds`
  - nettoyage automatique de la sélection quand la recherche/le filtre change
  - rechargement de la liste après suppression du lot
- [x] Ajout des **actions de lot complémentaires** dans `presentation/wordlist/WordListViewModel.kt`
  - ajout aux favoris pour toutes les cartes sélectionnées
  - réinitialisation de la progression pour toutes les cartes sélectionnées
- [x] Ajout d'une **hiérarchie de recherche** dans `Mes mots` / `À travailler` / `En cours` / `Connu`
  - priorité 1 : correspondance dans le **mot** (`recto`)
  - priorité 2 : correspondance dans la **définition** (`verso`)
  - priorité 3 : correspondance dans les **autres champs** (synonymes, exemples, catégorie grammaticale, registre, étymologie, notes)
  - à priorité égale, l'ordre reste stable et alphabétique par mot
  - le filtre de catégorie continue de s'appliquer normalement avant le classement par pertinence
- [x] Ajustement léger de libellés sur les cartes de listes
  - `Mot → Déf.` remplacé par `la définition`
  - `Déf. → Mot` remplacé par `le mot`
  - suppression de la puce d'état global redondante sur les cartes (l'information est déjà portée par la liste courante)
- [x] Correctif UX dashboard + popup détail
  - suppression du flash `Aucune carte pour le moment` au lancement tant que le premier chargement des stats n'est pas terminé
  - le popup détail d'un mot ouvert depuis les listes a maintenant une petite marge au-dessus de la ligne du haut
  - l'espace entre cette ligne du haut et les informations du popup a été réduit
- [x] Ajout de **tests unitaires ciblés** dans `presentation/wordlist/WordListViewModelTest.kt`
  - toggle de sélection
  - sélection de toutes les cartes visibles
  - conservation uniquement des sélections encore visibles après filtrage
  - suppression groupée et vidage de la sélection
  - ajout aux favoris en lot
  - reset de progression en lot
  - priorité mot > définition > autres champs
  - conservation de cette priorité à l'intérieur d'une liste filtrée

### 🔗 Fichiers modifiés
- `app/src/main/java/com/example/lexicaandroid2/presentation/wordlist/WordListScreen.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/wordlist/WordListViewModel.kt`
- `app/src/test/java/com/example/lexicaandroid2/presentation/wordlist/WordListViewModelTest.kt`

---

## 📅 2026-04-12 — Refonte UX "Ajouter des mots" : présentation unifiée + état ajouté + bug fix

### ✅ Accompli
- [x] **Correctif suggestions réaffichées à tort au retour sur `Ajouter des mots`**
  - les `mots suggérés pour toi` sont maintenant rechargés **après** rafraîchissement de la collection réelle de l'utilisateur
  - les suggestions sont filtrées contre les mots déjà présents dans `flashcards` (comparaison normalisée sur le mot)
  - les mots ajoutés pendant la session courante restent visibles en **fond vert** jusqu'à ce qu'on quitte l'écran
  - après sortie/retour sur l'écran, ces mots ne réapparaissent plus dans les suggestions
  - la recherche locale continue en revanche à remontrer un mot déjà possédé dans `localMatches` si l'utilisateur le cherche explicitement
- [x] **Correctif recherche AddWords : effacement propre + reset au retour écran**
  - si la barre de recherche est vidée, les anciens résultats API ne peuvent plus réapparaître après coup
  - les réponses asynchrones d'une ancienne requête sont désormais ignorées si la requête courante a changé ou a été effacée
  - à chaque retour sur `Ajouter des mots`, la requête précédente est remise à zéro (`searchQuery`, `localMatches`, `apiResults`, état de chargement)
- [x] **Optimisation légère de réactivité sur `Ajouter des mots`**
  - réduction prudente du debounce de recherche externe de `500 ms` à `300 ms`
  - aucun changement de logique métier ou de ranking, uniquement un délai artificiel raccourci pour améliorer la perception de vitesse
- [x] **Tests unitaires ajoutés** pour verrouiller le scénario de régression de `AddWordsViewModel`
  - mot suggéré ajouté → visible en vert pendant la session
  - même mot absent des suggestions après `onScreenEntered()`
  - recherche locale d'un mot déjà possédé toujours fonctionnelle
  - effacement de recherche qui bloque les résultats fantômes d'une ancienne requête
  - retour écran qui remet bien la recherche à l'état initial

### 🔗 Fichiers modifiés
- `app/src/main/java/com/example/lexicaandroid2/presentation/addwords/AddWordsViewModel.kt`
- `app/src/test/java/com/example/lexicaandroid2/presentation/addwords/AddWordsViewModelTest.kt`

### ✅ Accompli
- [x] **Présentation unifiée** : `ApiResultItem` et `ReserveWordItem` remplacés par un seul composable `WordCandidateItem`
  - Même layout pour tous : mot bold / définition grise 2 lignes / catégorie grammaticale texte plain violet
  - Plus de badge/rectangle pour la catégorie dans les résultats API → cohérence visuelle totale
- [x] **Comportement après ajout** : le mot ne disparaît plus de la liste immédiatement
  - Fond de la ligne passe en **vert clair** (`#E8F5E9`)
  - Icône ✓ apparaît à côté du mot
  - Bouton "Ajouter" remplacé par **⭐ favori** + **🗑️ supprimer**
  - L'étoile bascule entre plein/vide selon l'état favori (jaune si favori)
  - La poubelle supprime le mot de la collection et retire la ligne verte
- [x] **Reset à la re-navigation** : `LaunchedEffect(Unit) { viewModel.onScreenEntered() }`
  - À chaque retour sur l'écran : `addedInSession` remis à zéro, `proposedWords` rechargé depuis la DB (sans les mots déjà ajoutés), `allCards` rafraîchi
  - Les lignes vertes disparaissent → liste propre
- [x] **Bug fix : mots supprimés encore visibles comme "déjà ajouté"**
  - Cause : `allCards` était chargé une seule fois au `init {}` et jamais rafraîchi
  - Correction : `onScreenEntered()` appelle `loadAllCards()` → `allCards` toujours à jour par rapport à la DB réelle

### 🔗 Fichiers modifiés
- `presentation/addwords/AddWordsViewModel.kt` ← `addedInSession`, `onScreenEntered`, `deleteAddedWord`, `toggleFavoriteAddedWord`
- `presentation/addwords/AddWordsScreen.kt` ← `WordCandidateItem` unifié, `LaunchedEffect`

### ✅ Accompli
- [x] **Refill automatique `word_reserve`** : quand la réserve passe sous 100 mots et que l'appareil est connecté à internet, l'app va chercher des mots rares sur le Wiktionnaire pour revenir à 100
  - Déclenchement silencieux au démarrage de l'app (`MainActivity`, sur `Dispatchers.IO`)
  - Anti-doublon : les mots déjà dans `word_reserve` ou dans `flashcards` sont exclus
  - Gestion des erreurs mot par mot : un échec réseau sur un mot passe au suivant sans bloquer
  - Logs détaillés sous le tag `REFILL_RESERVE`
- [x] **Source dynamique (Option A) : API catégories Wiktionnaire** via `WiktionnaireCategorySource`
  - 10 catégories ciblées : `Registre soutenu en français`, `Vocabulaire de la philosophie`, `Rhétorique`, `Psychologie`, `Linguistique`, `Littérature`, `Droit`, `Politique`, `Médecine`, `Sociologie`
  - L'app tire des vraies listes de mots existants dans Wiktionnaire (jamais une liste figée codée en dur)
  - Filtre heuristique : mots simples (pas d'espace), minuscule, ≥ 5 caractères, sans chiffres
  - Catégories mélangées aléatoirement → variété à chaque refill
  - **Fallback statique** : si l'API catégories échoue → `RareWordsCandidates` (~200 mots) prend le relais
- [x] `RefillWordReserveUseCase` mis à jour pour utiliser `WiktionnaireCategorySource` en source primaire
- [x] **Option D ajoutée au backlog** dans `FEATURES.md` : refill intelligent ciblé selon le profil de l'utilisateur (catégories sous-représentées dans sa collection)

### 🔗 Fichiers modifiés/créés
- `app/src/main/java/…/data/remote/WiktionnaireCategorySource.kt` ← **nouveau**
- `app/src/main/java/…/domain/usecase/RefillWordReserveUseCase.kt` ← source catégories intégrée
- `FEATURES.md` ← Option A (✅) + Option D (backlog)

---

## 📅 2026-04-12 — Fallback recherche externe dans `Mes mots` + carte préremplie

### ✅ Complément — Recherche locale-only dans `Mes mots`
- [x] Retrait du fallback web de la barre de recherche de `presentation/wordlist/WordListViewModel.kt`
  - `onSearchQueryChanged(...)` ne filtre plus que la collection locale
  - suppression des états et méthodes liés à la recherche externe (`apiSearchResults`, `apiPreviewResult`, `apiError`, `isApiLoading`, etc.)
- [x] Allègement de `presentation/wordlist/WordListScreen.kt`
  - suppression des sections UI de résultats externes / popup d'aperçu externe
  - la barre de recherche de `Mes mots` sert désormais uniquement à chercher dans la liste courante
- [x] Simplification de l'instanciation `WordListViewModelFactory` dans `MainActivity.kt`
- [x] Réalignement des tests `WordListViewModelTest.kt` sur un comportement local-only

### ✅ Complément — Correctif crash molette / hover Compose sur émulateur
- [x] Diagnostic runtime récupéré via `adb logcat`
  - crash confirmé : `java.lang.IllegalStateException: The ACTION_HOVER_EXIT event was not cleared.` dans `AndroidComposeView`
- [x] Ajout d'un garde-fou ciblé dans `MainActivity.kt`
  - override de `dispatchGenericMotionEvent(...)`
  - interception uniquement du bug Compose connu sur les actions hover/molette (`ACTION_SCROLL`, `ACTION_HOVER_EXIT`, `ACTION_HOVER_MOVE`, `ACTION_HOVER_ENTER`)
  - les autres `IllegalStateException` continuent d'être relancées normalement
- [x] Ajout d'un test unitaire `MainActivityInputWorkaroundTest.kt` pour verrouiller la détection du crash contourné

### ✅ Complément — Refonte UX compacte des listes
- [x] Refonte de `presentation/wordlist/WordListScreen.kt`
  - structure des cartes `Mes mots` passée d'une `Row` rigide à un layout vertical compact
  - actions favori/suppression regroupées en haut à droite sans créer de grand vide horizontal
  - badges d'état déplacés dans un `FlowRow` pour éviter les retours à la ligne cassés et les espaces morts
  - libellés visuels raccourcis (`Mot → Déf.`, `Déf. → Mot`) pour mieux tenir sur petits écrans
- [x] Validation ciblée exécutée après refonte
  - `:app:compileDebugKotlin`
  - `:app:testDebugUnitTest --tests "com.example.lexicaandroid2.presentation.wordlist.WordListViewModelTest"`

### ✅ Accompli
- [x] Intégration des changements validés dans `main`
  - merge local de `integration/espace-de-travail-2026-04-08-suite` vers `main`
  - création d'une nouvelle branche de travail datée `integration/detail-ux-recherche-2026-04-12`
- [x] Activation d'un fallback vers la base de recherche externe dans `presentation/wordlist/WordListViewModel.kt`
  - recherche en ligne déclenchée quand aucun mot local ne correspond à la requête
  - filtrage des doublons déjà présents dans la collection
  - message d'erreur dédié si la base de recherche ne retourne rien ou n'est pas joignable
- [x] Enrichissement de `presentation/wordlist/WordListScreen.kt`
  - affichage d'un état de chargement pour la recherche externe
  - section "résultats proposés depuis la base de recherche"
  - ouverture d'une fiche d'aperçu avant ajout, avec définition, catégorie grammaticale, exemples, synonymes et source
  - ajout direct de la carte sans saisie manuelle
- [x] Sécurisation de l'ajout depuis un résultat externe
  - prévention des doublons accent/casse-insensibles
  - confirmation visuelle après ajout dans la collection
- [x] Retouches UX de la recherche de mots sur `Ajouter des mots` et `Mes mots`
  - clic sur toute la ligne d'un résultat web/suggéré pour ouvrir un popup détaillé
  - ajout possible directement depuis le popup, sans ressaisie
  - uniformisation du bouton `Ajouter` entre résultats web et suggestions locales
  - popup compacté (hauteur max réduite + scroll seulement si contenu long)
  - clic sur un mot déjà présent dans `Ajouter des mots` => popup d'information complet
- [x] Harmonisation partielle du popup local `WordDetailDialog`
  - hauteur max ramenée de `700.dp` à `620.dp` pour se rapprocher des nouveaux aperçus compacts
- [x] Ajustement UX `Ajouter des mots` : conserver les mots après ajout et les marquer visuellement
  - ajout depuis suggestions locales : les cartes restent visibles avec fond vert + état `Ajoute`
  - ajout depuis recherche API : même logique (pas de disparition immédiate, fond vert)
  - suppression du bandeau de succès global (feedback désormais porté par l'état de chaque carte)
- [x] Ajustement UX complémentaire `Ajouter des mots`
  - reset des états verts temporaires au retour sur l'écran (sortie/retour ou rafraîchissement)
  - ajout via popup synchronisé avec l'état vert de la liste
  - bouton popup vert quand le mot est déjà ajouté
  - bouton `Ajouter` bascule maintenant en mode toggle (re-clic = retrait + déverdissement)
- [x] Correctif `Ajouter des mots` sur les doublons multi-définitions et accents
  - le doublon est désormais détecté sur `mot + définition` (et non plus sur le mot seul)
  - plusieurs cartes avec le même mot mais des définitions différentes sont autorisées
  - la clé visuelle d'état vert est aussi basée sur `mot + définition`
  - la comparaison conserve les accents (plus de fusion indésirable de mots distincts accentués)
- [x] Ajout de tests unitaires ciblés dans `presentation/wordlist/WordListViewModelTest.kt`
  - fallback externe quand la recherche locale échoue
  - absence d'appel externe quand un mot local existe déjà
  - persistance correcte des champs préremplis
  - blocage de l'ajout d'un doublon

### 🔜 Vérifications
- [x] Validation compilateur ciblée via `:app:testDebugUnitTest --tests com.example.lexicaandroid2.presentation.wordlist.WordListViewModelTest`
  - `:app:compileDebugKotlin` exécuté avec succès dans le pipeline de test
  - `WordListViewModelTest` vert après ajout du fallback externe et de l'ajout prérempli
- [x] Validation compilation UI ciblée via `:app:compileDebugKotlin`
- [ ] Validation visuelle manuelle sur `Mes mots` et `Ajouter des mots`

---

## 📅 2026-04-08 — Vision future : thèmes, sources de contenu, nouveau type de question

### ✅ Accompli
- [x] Création de `docs/planning/VISION_FUTURE.md` : document de vision structuré daté 2026-04-08
- [x] Intégration des réponses Q1–Q8 + QC2 & QC3
- [x] Correction **SM2 → POLO-1** dans tous les `.md` de haut niveau (`VISION_FUTURE.md`, `DAILY_STANDUP.md`)
  - *(Les fichiers `integration_pending/polo_1_*` gardent "SM2" : contexte de migration historique volontaire)*
- [x] QC2 validée : validation hybride "Invente une phrase" (MiniLM local + HuggingFace si connecté / non proposé offline / futur premium)
- [x] QC3 validée : ordre auto par fréquence + override manuel curator, les deux coexistent
- [x] QC1 mise en attente (processus de création supervisé — itération dédiée à venir)
- [x] Refactor de `VISION_FUTURE.md` en **cadrage produit pré-lancement**, tout en conservant les annales en bas du fichier
  - définition précise du produit et du positionnement
  - réponse explicite à la question stratégique : **pas de lancement public large maintenant**, priorité à un **MVP** puis **alpha fermée**
  - ajout du **public cible initial**
  - ajout d'une **définition métier claire du thème**
  - ajout d'un **MVP strict** (1–2 thèmes, 3 niveaux, aperçu → validation → ajout deck, filtre de session)
  - ajout du **modèle métier minimal recommandé** (`Theme`, `ThemeLevel`, `ThemeWordCandidate`, `FlashcardThemeLink`)
  - ajout des **règles minimales de qualité des données**
  - ajout de la **hiérarchie des fonctionnalités** (`MVP` / `V2` / `Plus tard` / `Premium`)
  - ajout d'une **stratégie de mesure** et d'un **ordre recommandé des travaux**
  - transformation du plan global en séquence **Phase A → Phase F** orientée exécution
- [x] Découpage du plan de `VISION_FUTURE.md` en **tâches numérotées** au format inspiré de `docs/guides/CONSIGNES_TACHES.md`
  - `TACHE_VF_01` à `TACHE_VF_12`
  - scope, zone code/produit, livrables, contraintes, critère de sortie
  - vue synthétique des priorités : immédiat / MVP / extensions
- [x] Évolution du cadrage produit : nouvelle définition des thèmes centrée sur **l'usage visé par l'utilisateur**
  - `Discussion générale / intellectuelle`
  - `Littérature / écriture`
  - `Domaine particulier`
- [x] Ajout d'un nouvel axe produit : **Verbaliser les ressentis**
- [x] Intégration réelle d'un **Défi utilisation** dans l'entraînement courant
  - nouveau type `ReviewSessionChallengeKind.USAGE`
  - validation légère dédiée via `UsageChallengeValidator`
  - intégration dans l'entraînement en **mode non structurant** (sans impact sur `POLO-1`)
  - ajout du toggle admin `Défi utilisation` pour test isolé des questions
  - adaptation de l'UI review pour saisie multi-ligne et libellés dédiés
  - test unitaire ciblé `UsageChallengeValidatorTest` + compilation Kotlin OK
- [x] Mise en place d'un **banc d'essai data-driven** pour le défi utilisation
  - fichier `app/src/test/resources/usage_challenge_dataset.csv`
  - 20 cas initiaux issus de mots de `mots_rares.json`
  - verdicts attendus humains : `ACCEPT` / `BORDERLINE` / `REJECT`
  - test `UsageChallengeDatasetCompatibilityTest` pour mesurer la concordance app ↔ estimation humaine
  - recalibrage de `UsageChallengeValidator` jusqu'à obtenir une concordance satisfaisante sur le dataset de départ
- [x] Formalisation et amélioration de l'ordre de sélection des activités intégrées
  - `QCM` : distracteurs pris d'abord dans les cartes de session, puis dans le cache global si besoin
  - `Matching` : cartes de session d'abord, carte en cours si nécessaire, puis distracteur global si la session est trop courte
  - `Matching` peut maintenant se déclencher avec une session à 1 carte si une carte distractrice globale est disponible
  - test ajouté : `qcmCanUseGlobalDistractorsWhenSessionHasSingleQuestion`
  - test ajouté : `matchingCanUseGlobalDistractorWhenSessionHasSingleCard`

---

## 📅 2026-04-08 — Refonte complète UX Matching : drag & drop + cartes définition/drop

### ✅ Accompli
- [x] **Nouveau fichier `MatchingContent.kt`** : composant `MatchingDragDropContent` complet
  - Zone mots **sticky** en haut : chips dans un `FlowRow` adaptatif (s'élargit sur petits écrans)
  - Zone définitions **scrollable** : `LazyColumn` de cartes bi-zones (haut = définition / bas = drop)
  - **Long-press + drag** : ghost du mot sous le doigt via overlay `zIndex(10f)`, détection de drop par coordonnées fenêtre
  - **Tap court** : sélection du mot → tap sur zone de drop pour placer (compatible petits écrans + scroll)
  - Scroll du `LazyColumn` automatiquement désactivé pendant un drag (`userScrollEnabled = activeDrag == null`)
  - Retrait d'un mot depuis sa zone (tap quand pas de mot sélectionné) → reprend le mot
  - Highlights : hovered (drag survole), sélectionné, assigné, vide
- [x] **`ReviewViewModel.kt`** : ajout de `fun onMatchingDrop(wordId, definition)` pour le drop direct
- [x] **`ReviewScreen.kt`** : nouvelle branche `shouldShowMatchingLayout` (layout dédié `fillMaxSize` + `weight(1f)`) en dehors du bloc scrollable → instruction via `ReviewContextHint` en dehors de la carte
- [x] **`EventContent.kt`** : suppression de l'ancien `MatchingEventContent` (remplacé entièrement)

### 🗂 Fichiers modifiés
- `app/.../presentation/review/MatchingContent.kt` ← **nouveau**
- `app/.../presentation/review/ReviewViewModel.kt`
- `app/.../presentation/review/ReviewScreen.kt`
- `app/.../presentation/review/EventContent.kt`

---

## 📅 2026-04-08 — Fix UX QCM mode entraînement : bouton TTS + distinction visuelle instruction/options

### ✅ Accompli
- [x] **Bugfix `speakCurrentFace()` dans `ReviewViewModel.kt`** : en mode QCM/Matching, le bouton 🔊 lisait `eventInstruction` ("Choisis la bonne définition") au lieu du mot affiché → maintenant lit toujours `visibleFrontText` (le mot ou la définition selon le mode)
- [x] **Refonte visuelle de l'instruction QCM dans `EventContent.kt`** :
  - L'instruction ("Choisis la bonne définition") est maintenant encapsulée dans un badge arrondi (`Surface` + `RoundedCornerShape(50)`) avec fond `secondaryContainer`
  - Style `labelMedium` en italique pour la distinguer clairement des définitions proposées
  - Ajout d'un `HorizontalDivider` entre l'instruction et les options pour séparer les deux zones

### 🗂 Fichiers modifiés
- `app/src/main/java/.../presentation/review/ReviewViewModel.kt` (l.308)
- `app/src/main/java/.../presentation/review/EventContent.kt`

---

## 📅 2026-04-07 - Réparation tests planner + nettoyage warnings Kotlin ciblés

### ✅ Accompli
- [x] Réparation de la compilation des tests unitaires ciblés
  - ajout du stub `deleteAllCards()` dans `FakeFlashcardRepository` de `ReviewSessionPlannerTest.kt`
  - réalignement du fake de test sur l'interface `FlashcardRepository`
- [x] Nettoyage des warnings Kotlin/Compose les plus sûrs et concrets
  - suppression d'une condition toujours vraie dans `ReviewViewModel.kt`
  - migration de `LinearProgressIndicator` vers la surcharge non dépréciée dans `XpProgressBar.kt` et `ModelDownloadUI.kt`
  - remplacement de `Divider()` par `HorizontalDivider()` dans `GamificationDemoScreen.kt`
  - suppression d'une variable inutilisée dans `XpProgressBar.kt`
- [x] Deuxième passe ultra-conservative sur les warnings restants sans casser les API
  - suppressions localisées `UNUSED_PARAMETER` / `unused` sur `ProfileScreen.kt`, `WordDetailScreen.kt`, `SyncManager.kt`, `GameComposables.kt`, `ModelDownloadUI.kt`
  - aucune signature métier/navigation critique modifiée
- [x] Validation ciblée relancée avec succès
  - `:app:compileDebugKotlin`
  - `:app:testDebugUnitTest --tests com.example.lexicaandroid2.domain.logic.ReviewSessionPlannerTest`
  - `:app:testDebugUnitTest --tests com.example.lexicaandroid2.presentation.review.ReviewViewModelTest`
  - `:app:testDebugUnitTest --tests com.example.lexicaandroid2.presentation.admin.AdminViewModelTest`
  - `:app:testDebugUnitTest --tests com.example.lexicaandroid2.core.tts.TtsVoiceSupportTest`
- [x] Correctif du retour au flux métier normal après usage du filtre admin `Review`
  - suivi des changements de réglages admin liés à l'entraînement dans `AdminViewModel`
  - application automatique au moment de quitter `AdminScreen`, même via navigation retour
  - `ReviewViewModel` sait maintenant recharger explicitement une session après changement de configuration
  - ajout d'un test de non-régression : passage en filtrage admin puis retour à `Présentation normale` => session standard restaurée
- [x] Intégration correcte du téléchargement in-app du modèle sémantique
  - si un défi sémantique arrive sans modèle en cache, `Review` affiche maintenant un vrai dialog intégré à l'app
  - le téléchargement démarre dans l'interface avec progression visible
  - l'utilisateur peut continuer sans IA en fallback Jaccard, sans blocage
  - après téléchargement réussi, `Review` active le mode IA sémantique et l'indique visuellement dans l'UI du défi
  - ajout d'un test de non-régression pour le prompt de téléchargement puis la bascule en mode IA

### 🔴 Bloquants
- Aucun sur ce correctif ciblé
- Des dépréciations non critiques restent encore hors périmètre immédiat (notamment `GoogleSignIn`)

### 🔜 Prochaines actions
- Décider si on poursuit un nettoyage plus large des warnings non critiques (`ProfileScreen`, `WordDetailScreen`, `SyncManager`, `GameComposables`)
- Évaluer séparément la migration future du flux `GoogleSignIn` déprécié

### 📊 Statut Global
```
Tests planner:      ✅ Recompilent
Review warnings:    ✅ Nettoyage ciblé appliqué
Compose warnings:   ✅ Progress bars + dividers modernisés
Warnings safe:      ✅ Passe conservative supplémentaire validée
Admin review mode:  ✅ Retour fiable au flux métier normal restauré
Semantic model UX:  ✅ Téléchargement in-app branché + fallback Jaccard conservé
Validation ciblée:  ✅ BUILD SUCCESSFUL
```

### 🛍️ TODO pré-publication Google Play

> Plan déplacé dans `docs/planning/PLAN_INTEGRATION_GOOGLE_PLAY.md` pour sortir la checklist de publication du journal quotidien.

- [x] Checklist Google Play extraite de `DAILY_STANDUP.md`
- [ ] Suivre désormais les actions de publication dans `docs/planning/PLAN_INTEGRATION_GOOGLE_PLAY.md`


---

## 📅 2026-04-05 - Refonte UX question orthographique et défi orthographique

### ✅ Accompli
- [x] Refonte UX orthographique dans `ReviewScreen.kt`
  - layout dédié fixe en bas pour la saisie + validation + ligne retour / options son
  - carte orthographique alignée sur le style des cartes standards (`RoundedCornerShape(20dp)`, `lexicaPanelContainerColor()`, elevation 4dp)
  - typographie alignée via `rememberAdaptiveTextStyle` / `AudioTextLine`
  - **Question orthographique** : aide audio visible dans la carte (`Écouter le mot`)
  - **Défi orthographique** : aucune aide audio visible, l'utilisateur doit retrouver seul
  - validation avec flip vers le verso + message de feedback
  - **Défi orthographique réussi** : feedback spécial vert + confettis
  - échec : feedback standard, sans animation spéciale
- [x] Correctifs UX complémentaires appliqués
  - hauteur du bandeau bas réduite pour la ligne retour / options son
  - suppression du titre redondant sous la barre de progression pour les écrans orthographiques / défis
  - suppression du suffixe ` (mode test admin)` dans les feedbacks affichés
  - lecture vocale de la définition conservée dans le défi orthographique sans bouton dédié
  - célébration du défi resserrée pour éviter l’agrandissement inutile de la carte
- [x] Ajustements finaux
  - icône des options audio restaurée à sa taille normale
  - carte orthographique recentrée en hauteur dans l’espace disponible
  - verso du défi compacté pour éviter l’effet d’expansion verticale

### 🔴 Bloquants
- Aucun

### 🔜 Prochaines actions
- Vérifier visuellement sur émulateur le layout fixe bas d'écran et l'animation de flip
- Vérifier le feedback vert + confettis sur succès d’un défi orthographique
- Envisager la même refonte pour le défi sémantique si souhaité

### 📊 Statut Global
```
Orthographique UX:     ✅ Layout fixe + flip + feedback
Question ortho:        ✅ TTS mot disponible (section dédiée dans la carte)
Défi ortho:            ✅ TTS mot absent (retrouver seul)
Défi sémantique:       ✅ TTS mot inline, sans-serif
Validation:            ✅ Aucune erreur de compilation
```

---

## 📅 2026-04-05 - Option "Remettre la progression à zéro" dans le profil

### ✅ Accompli
- [x] Ajout de `deleteAll()` dans `FlashcardDao` (supprime toutes les flashcards)
- [x] Ajout de `deleteAll()` dans `ReviewQuestionDao` (supprime toute la progression POLO-1)
- [x] Ajout de `deleteAllCards()` dans l'interface `FlashcardRepository` + implémentation dans `FlashcardRepositoryImpl`
- [x] Création de `domain/usecase/ResetProgressUseCase.kt` — orchestre la suppression complète : flashcards, progression POLO-1, snapshots de session, stats XP/streak, stats journalières
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
Seuls **XP, niveau, streak, favoris** sont synchronisés dans le cloud. La progression POLO-1 par mot (date de prochaine révision, état d'apprentissage) reste **locale uniquement**. Sur un nouveau téléphone, l'utilisateur retrouve ses stats de gamification mais repart de zéro pour les révisions.

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
  - mise à jour POLO-1 d'une carte
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

---

## 2026-04-05 (suite) — Corrections UX orthographique + Refactoring ReviewScreen

### 🔧 Corrections de bugs (questions orthographiques)
- **Label de type disparu** : le hint contextuel ("Question orthographique" / "Défi orthographique" / "Défi sémantique") n'apparaissait plus au-dessus de la carte → ajout d'une branche dédiée dans le layout avec centrage vertical via `Box(weight(1f), contentAlignment = Center)`
- **Bouton audio lisait le mot au lieu de la définition** : `AudioTextLine` appelait `onSpeakWord` au lieu de `onSpeakDefinition` → corrigé
- **Auto-speak pour événements orthographiques** : `maybeAutoSpeakVisibleContent()` retournait immédiatement pour les types non-NORMAL_QUESTION → étendu avec règles : extra_spelling lit def puis mot ; défi ortho lit def seulement (JAMAIS le mot) ; défi sémantique lit le mot seulement
- **Bouton PASSER** : ajouté pour question ortho uniquement (EXTRA_SPELLING), les défis (CHALLENGE) n'ont que VALIDER pleine largeur

### 🏗️ Refactoring : décomposition de ReviewScreen.kt (1727 → 7 fichiers)
- `ReviewModels.kt` — data classes + fonctions utilitaires
- `ReviewSharedComponents.kt` — composables partagés (ReviewHeader, AudioTextLine, etc.)
- `NormalQuestionContent.kt` — contenu question normale + contrôles fixes
- `OrthographicContent.kt` — contenu orthographique + contrôles fixes
- `EventContent.kt` — QCM + Matching
- `SessionCelebration.kt` — célébration + confettis
- `ReviewScreen.kt` — orchestrateur lean (~300 lignes)

### 📊 Statut Global
```
Question ortho:     ✅ Label restauré + carte centrée + PASSER/VALIDER
Défi ortho/séma:    ✅ Label restauré + carte centrée + VALIDER seul (pas de PASSER)
Audio ortho:        ✅ Bouton son lit la définition (pas le mot)
Auto-speak ortho:   ✅ Règles spécifiques par type (défi ortho ≠ mot)
Refactoring Review: ✅ 1 fichier monolithique → 7 fichiers découpés
Build:              ✅ Compilation OK
```

---

## 2026-04-08 — Audit + refonte pipeline défi sémantique embeddings/TFLite

### ✅ Diagnostic confirmé
- L’URL `paraphrase-multilingual-MiniLM-L12-v2/resolve/main/model.tflite` utilisée par `TFLiteSemanticValidator.kt` retourne **404** : le fichier visé n’existe pas dans le dépôt Hugging Face ciblé.
- Le dépôt source publie bien `sentencepiece.bpe.model` + `tokenizer.json` + `1_Pooling/config.json`, donc le modèle exact nécessite une **vraie tokenization SentencePiece/BERT** et une étape de **pooling sentence-transformers**.
- L’ancienne implémentation injectait un **`FloatArray(384)` fabriqué par hashing de tokens** dans TFLite ; ce n’était **pas** un input valide de MiniLM/sentence-transformers.

### 🔧 Refonte livrée
- Pivot vers un bundle on-device **DistilUSE multilingue cased** réellement exploitable sur mobile :
  - modèle TFLite quantifié téléchargé et caché localement
  - `vocab.txt` WordPiece téléchargé avec le modèle
  - tokenizer WordPiece Kotlin embarqué
  - inférence TFLite réelle sur `input_ids`
  - mean pooling côté app puis cosine similarity
- `JaccardSemanticValidator` conservé comme fallback si modèle absent/refusé/en erreur.
- `ReviewViewModel.kt` : le feedback détaillé du validateur sémantique remonte désormais dans l’UI du défi.
- `ModelDownloadUI.kt` : texte et statut alignés avec le vrai bundle embeddings on-device.

### 🧪 Validations exécutées
- ✅ `:app:compileDebugKotlin`
- ✅ `:app:testDebugUnitTest --tests "com.example.lexicaandroid2.presentation.review.ReviewViewModelTest"`
- ✅ `:app:testDebugUnitTest --tests "com.example.lexicaandroid2.presentation.review.challenge.SpellingValidatorTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.JaccardSemanticValidatorTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.DistilUseWordPieceTokenizerTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.TFLiteSemanticValidatorTest"`

### ✅ Ajustement UX prompt téléchargement sémantique (suite)
- Le prompt de téléchargement n'attend plus l'ouverture d'un défi sémantique : il peut maintenant être proposé dès le lancement de l'app via `LexicaApp.kt`
- `ReviewScreen.kt` ne porte plus de dialog local pour ce téléchargement ; l'affichage est globalisé au niveau application
- Le wording visible a été réécrit côté utilisateur : on explique maintenant que cela aide l'app à mieux reconnaître les réponses libres, sans parler d'"embeddings" ou de "Jaccard"
- Les messages de confirmation/refus ont aussi été simplifiés (`ReviewViewModel.kt` + `ModelDownloadUI.kt`)
- Validation supplémentaire exécutée :
  - ✅ `:app:compileDebugKotlin`
  - ✅ `:app:testDebugUnitTest --tests "com.example.lexicaandroid2.presentation.review.ReviewViewModelTest"`

### ✅ Recalage métier de la sélection des mots-clés
- L'extraction ne vise plus une liste longue de 5+ mots-clés : la cible par défaut est maintenant **1 à 2 concepts vraiment centraux** par définition
- `KeywordExtractor.kt` ne trie plus seulement par longueur ; la sélection donne priorité au début de définition, au groupe nominal de tête et écarte certains termes génériques de définition
- `SemanticValidator.kt` et `TFLiteSemanticValidator.kt` utilisent désormais cette extraction resserrée sans `topN = 5` codé en dur
- Tests mis à jour et validés :
  - ✅ `:app:testDebugUnitTest --tests "com.example.lexicaandroid2.presentation.review.challenge.KeywordExtractorTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.JaccardSemanticValidatorTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.TFLiteSemanticValidatorTest"`

### ✅ Décision produit : les mots-clés ne décident plus du défi sémantique
- En mode modèle sémantique disponible, la validation repose désormais **uniquement** sur la similarité sémantique ; une reformulation proche n'est plus rejetée parce qu'elle ne contient pas les “bons mots”
- Le fallback sans modèle a été simplifié en similarité lexicale globale (`KeywordExtractor.lexicalFallbackScore`) au lieu d'une logique de mots-clés trouvés/manquants
- Les messages utilisateur ne parlent plus de mots-clés manquants pour juger une réponse libre
- Validation exécutée :
  - ✅ `:app:compileDebugKotlin`
  - ✅ `:app:testDebugUnitTest --tests "com.example.lexicaandroid2.presentation.review.challenge.KeywordExtractorTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.JaccardSemanticValidatorTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.TFLiteSemanticValidatorTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.SpellingValidatorTest"`

### ✅ Calibration de seuils (tests ciblés)
- Ajout de tests de frontière explicites sur le mode sémantique :
  - `0.62` = succès
  - `0.40` = réponse partielle
  - `< 0.40` = échec
- Ajustement léger des seuils modèle retenus dans `TFLiteSemanticValidator.kt` :
  - succès `0.65` → `0.62`
  - partiel `0.45` → `0.40`
- Mesure rapide du fallback lexical sur quelques reformulations françaises courtes, puis assouplissement du mode secours :
  - succès lexical `0.55` → `0.50`
  - partiel lexical `0.30` → `0.25`
- Cas validés par tests : reformulation courte acceptable, réponse limite, hors-sujet, fallback lexical proche, fallback lexical partiel
- Validation exécutée :
  - ✅ `:app:compileDebugKotlin`
  - ✅ `:app:testDebugUnitTest --tests "com.example.lexicaandroid2.presentation.review.challenge.KeywordExtractorTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.JaccardSemanticValidatorTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.TFLiteSemanticValidatorTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.SpellingValidatorTest"`

### ✅ Objectif Google Play début mai — lot du jour
- **Ajout de mots / API** : `DictionaryServiceImpl.kt` n’est plus limité au scraper HTML brut ; la recherche passe d’abord par un nouveau flux **MediaWiki API Wiktionnaire** (`WiktionnaireApiSource.kt`) avec :
  - lookup page exact via `action=parse`
  - recherche de titres candidats via `action=query&list=search`
  - parsing HTML mutualisé dans `WiktionnaireHtmlParser.kt`
  - fallback final sur `WiktionnaireScraper.kt`
  - déduplication des résultats cross-source
- **Prépa Google Play / conformité** :
  - suppression du point d’entrée public `GamificationDemoScreen` dans la navigation (`Screen.kt` + `LexicaApp.kt`)
  - ajout d’un vrai flux **supprimer mon compte** côté profil (`ProfileViewModel.kt`, `ProfileScreen.kt`)
  - suppression du document Firestore utilisateur avant suppression Firebase Auth (`FirestoreSyncRepository.kt`, `SyncManager.kt`, `FirebaseAuthRepository.kt`)
- **Tests exécutés aujourd’hui** :
  - ✅ `:app:compileDebugKotlin`
  - ✅ `:app:testDebugUnitTest --tests "com.example.lexicaandroid2.data.remote.DictionaryServiceImplTest" --tests "com.example.lexicaandroid2.data.repository.WordReserveRepositoryImplTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.SpellingValidatorTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.JaccardSemanticValidatorTest" --tests "com.example.lexicaandroid2.presentation.review.challenge.TFLiteSemanticValidatorTest"`
- **Audit défis / revue intégrée** : la suite `ReviewViewModelTest` n’est pas entièrement verte actuellement. Échecs relevés à traiter avant release :
  - `skipExtraSpellingReturnsToNormalQuestion`
  - `qcmCanUseGlobalDistractorsWhenSessionHasSingleQuestion`
  - `adminCanDisableIntegratedQcmInReview`
  - `sessionProgressAdvancesProportionallyWithGotItAnswers`
  - `invalidateSessionForSettingsChangePersistsValidatedQuestions`
  - `completingSessionShowsCelebrationAndAwardsSessionXp`

### ✅ Lot P0 review + backup stabilisé (suite)
- `ReviewViewModel.kt` réaligné pour les scénarios P0 :
  - pas d’`EXTRA_SPELLING` dans les **micro-sessions explicites à 1 carte**
  - `MATCHING` ne parasite plus les scénarios de progression/QCM ciblés : il reste déclenché sur des cas d’échec adaptés et hors mode admin filtré
  - le **défi utilisation** n’est plus injecté dans le flux utilisateur standard ; il reste réservé au mode admin avancé / calibration
- Résultat direct : la suite `ReviewViewModelTest` repasse au vert sur les 6 échecs P0 confirmés précédemment.
- Stratégie de backup P0 appliquée côté app :
  - `AndroidManifest.xml` → `allowBackup=false`
  - `backup_rules.xml` + `data_extraction_rules.xml` → exclusion explicite des données locales (`database`, `sharedpref`, `files`, etc.)
- `SettingsScreen.kt` : retrait du placeholder technique pour la politique de confidentialité ; l’UI attend maintenant une **vraie URL publique** au lieu d’un faux lien local.
- Blocages externes restants avant soumission Play :
  - URL publique réelle de politique de confidentialité
  - décision finale sur le package public (`applicationId` / Firebase / OAuth release)
  - signature release + SHA console + Play Console

### ▶️ Démarrage P0 externe — politique de confidentialité
- Priorité suivante validée : obtenir une **URL publique HTTPS stable** pour la politique de confidentialité avant la fiche Play.
- Recommandation retenue pour aller vite sans coût : **GitHub Pages** avec une page simple dédiée (slug type `/privacy-policy`).
- Le branchement côté app est déjà prêt à accepter une vraie URL via `privacyPolicyUrl` dans `SettingsScreen.kt` ; il reste à publier la page puis à injecter l’URL finale.

### ✅ Automatisation repo-side — politique de confidentialité
- Création d’une page statique prête à publier : `privacy-policy/index.html`
- Ajout d’un mini guide de déploiement : `privacy-policy/README.md`
- Ajout d’un workflow GitHub Actions pour GitHub Pages : `.github/workflows/privacy-policy-pages.yml`
- Ajustement du workflow pour autoriser aussi la publication depuis les branches `integration/**`, afin de sortir l’URL sans attendre un merge sur `main`
- Vérification Git locale effectuée : `origin` pointe bien sur `https://github.com/psycolobite/LexicaAndroid2.git`
- Vérification remote effectuée : la branche distante existante est `origin/integration/espace-de-travail-2026-04-08-suite` ; `main` n’a pas encore été poussée sur GitHub à ce stade
- Correction de la cible GitHub Pages documentée : le workflow publie le contenu de `privacy-policy/` à la **racine** du site Pages du dépôt, donc l’URL attendue est de type `https://psycolobite.github.io/LexicaAndroid2/` et non `/privacy-policy/`
- Commit/push documentaire effectué sur `integration/espace-de-travail-2026-04-08-suite` : `74c1757 docs: fix privacy policy pages url guidance`
- Test HTTP public effectué sur `https://psycolobite.github.io/LexicaAndroid2/` : **404** à ce stade, ce qui confirme que l’activation GitHub Pages côté dépôt reste nécessaire
- Activation GitHub Pages effectuée ensuite côté dépôt (source : **GitHub Actions**) ; un **nouveau déclenchement** du workflow est nécessaire après cette activation pour sortir du `404` initial
- URL publique GitHub Pages attendue déjà branchée côté app dans `LexicaApp.kt` : `https://psycolobite.github.io/LexicaAndroid2/`
- Vérification technique après branchement : `:app:compileDebugKotlin` → **BUILD SUCCESSFUL**
- Diagnostic complémentaire : la croix rouge visible dans GitHub Actions correspond au run ancien `docs: fix privacy policy pages url guidance` ; les commits suivants n’avaient pas relancé Pages car le workflow ne se déclenche que sur `privacy-policy/**` ou le workflow lui-même
- Nouveau déclenchement préparé via une micro-mise à jour de `privacy-policy/index.html` (date de mise à jour + notice contact) pour forcer un run Pages après activation
- Politique de confidentialité retravaillée ensuite pour la publication : suppression de la notice interne destinée au dépôt et renforcement des mentions attendues côté RGPD / Google Play (`base légale`, `durée de conservation`, `transferts`, `droits`, `CNIL`, reformulation de la `sécurité` autour de Firebase / Google Cloud)
- Audit complémentaire des flux de données réalisé pour préparer la fiche **Google Play Data safety** : confirmation que le cloud Firestore ne synchronise actuellement que `xp`, `level`, `streak`, `lastLoginDate` et `favoriteCardIds` ; la progression détaillée question par question reste locale
- Politique de confidentialité encore précisée sur deux points techniques réels : les requêtes de recherche externe envoyées au Wiktionnaire et la portée exacte des données synchronisées
- Préparation repo-side du verrouillage Firestore : ajout de `firestore.rules`, `firebase.json` et `.firebaserc` pour le projet `lexica-6d59a`
- Correction du flux de suppression de compte dans `ProfileViewModel.kt` : suppression du document Firestore **avant** la suppression du compte Firebase pour rester compatible avec des règles strictes basées sur `auth.uid == userId`
- Vérification technique après correction du flux : `:app:compileDebugKotlin` → **BUILD SUCCESSFUL**
- Retour utilisateur : priorité 1 côté Firebase considérée comme faite (`déploiement des règles Firestore` côté projet)
- Reste hors repo :
  - laisser le workflow GitHub Pages redéployer après activation
  - récupérer l’URL finale publique HTTPS
  - confirmer l’e-mail support public final avant publication


---

## 📅 2026-04-14 — Ajustement icône launcher + embellissement splash screen

### ✅ Accompli
- [x] **Icône launcher Android dézoomée d’environ 25%**
  - `app/src/main/res/drawable/ic_launcher_foreground.xml` encapsulé dans un `group` avec `scaleX/scaleY = 0.75`
  - réduction visuelle centrée sans changer la direction artistique de l’icône
- [x] **Splash screen de lancement rendu plus esthétique**
  - création d’un logo dédié `app/src/main/res/drawable/ic_splash_logo.xml`
  - palette splash dédiée ajoutée dans `app/src/main/res/values/colors.xml`
  - thèmes de démarrage clair/sombre modernisés dans `app/src/main/res/values/themes.xml` et `app/src/main/res/values-night/themes.xml`
  - usage de `Theme.SplashScreen.IconBackground` + fond d’icône pour un rendu plus premium au lancement
- [x] **Validation technique ciblée à exécuter après retouche ressources**
  - build debug relancé pour vérifier le merge des ressources et les thèmes
- [x] **Durcissement anti-crash Compose hover/molette**
  - `MainActivity.kt` : garde-fou étendu aux dispatchs `generic motion`, `hover` et `touch` pour limiter les plantages fréquents liés au bug Compose `ACTION_HOVER_EXIT`
  - `MainActivityInputWorkaroundTest.kt` complété
- [x] **Ajustement POLO-1 — question orthographique remplaçante**
  - la question orthographique remplace désormais certaines questions `Définition -> Mot`
  - condition d’éligibilité : question déjà validée au moins une fois lors d’une session précédente (`firstAnsweredAt != null`)
  - réussite : validation directe de la question pour la session
  - échec ou passage : vaut `À revoir`
- [x] **Texte de skip orthographique adouci**
  - `Vous avez passé la question`
- [x] **Taille de session minimale remontée à 4**
  - réglages utilisateur, réglages admin et résolution runtime de `Review` réalignés
- [x] **Fermeture globale du clavier au tap hors champ**
  - wrapper racine Compose ajouté dans `LexicaApp.kt` pour retirer le focus lors d’un tap dans l’app hors saisie active
- [x] **Refonte de la sync compte/cloud**
  - la progression cloud ne se limite plus à `xp/niveau/streak/favoris` : extension vers cartes, progression question par question et stats quotidiennes
  - `SyncManager.kt`, `SyncViewModel.kt`, `SyncConfirmDialog.kt` et `FirestoreSyncRepository.kt` réalignés pour gérer compte cloud vide, import cloud, écrasement local et envoi du local vers le compte
  - `MainActivity.kt` ajusté pour ne plus réinjecter silencieusement les données seed quand un compte authentifié doit charger sa propre progression
  - `firestore.rules` étendu aux sous-collections utilisateur nécessaires à la synchro complète
- [x] **Question orthographique POLO-1 non remplaçante**
  - abandon de l’activation systématiquement remplaçante en première position
  - planification initiale aléatoire, limitée à une session éligible sur deux et plafonnée à ~30% des questions
  - succès/échec appliqués à la question cible sans supposer que l’orthographique remplace la question courante
- [x] **UI de résultat orthographique allégée**
  - suppression de la ligne redondante du mot correct
  - affichage de `Vous avez écrit : ...` en cas d’échec avec saisie utilisateur présente
- [x] **Validation technique ciblée relancée**
  - `:app:compileDebugKotlin` → **BUILD SUCCESSFUL**
  - `:app:testDebugUnitTest --tests "com.example.lexicaandroid2.MainActivityInputWorkaroundTest" --tests "com.example.lexicaandroid2.domain.logic.ReviewSessionEngineTest" --tests "com.example.lexicaandroid2.presentation.review.ReviewViewModelTest" --tests "com.example.lexicaandroid2.presentation.admin.AdminViewModelTest"` → **BUILD SUCCESSFUL**

### 🔜 Vérifications
- [ ] Vérifier visuellement sur appareil/émulateur que l’icône paraît bien ~25% plus petite sur le launcher
- [ ] Vérifier le rendu du splash en mode clair et en mode sombre
- [ ] Vérifier sur appareil Samsung / émulateur que le scroll souris ne provoque plus de crash Compose remontant comme "défaillance fréquente"
- [ ] Vérifier sur appareil réel les nouveaux dialogues de choix compte/local (`compte vide`, `charger le compte`, `envoyer le local`) et le comportement après changement de compte sur le même téléphone
- [ ] Vérifier que le clavier se ferme bien sur les écrans de connexion, inscription, ajout de mots et question orthographique sans gêner la saisie
- [ ] Décider plus tard si les fallbacks launcher legacy API < 26 (`mipmap-*/ic_launcher.webp`) doivent aussi être régénérés pour cohérence totale

