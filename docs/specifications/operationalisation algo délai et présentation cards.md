# Opérationnalisation algo délai et présentation cards

**Statut :** Plan d’implémentation technique  
**Branche de travail :** `integration/Polo-1` *(nom Git équivalent à la demande utilisateur, car un nom de branche avec espace n’est pas valide)*  
**Référence source :** `docs/specifications/fonctionnement algo délai et présentation cards.md`

---

## 1. Objectif

Ce document traduit la spec produit en plan d’implémentation concret pour le code actuel.

Il ne remplace pas la spec fonctionnelle. Il répond à 4 questions :
- quels écarts existent entre la base actuelle et la cible,
- quels modèles persistants doivent être introduits,
- quels moteurs doivent être créés ou remplacés,
- dans quel ordre implémenter sans casser l’existant.

---

## 2. État actuel du code vs cible

## 2.1 Ce que fait actuellement `Review`

### `ReviewViewModel.kt`
Le moteur actuel :
- charge une liste de `Flashcard` via `repository.getCardsToReview(limit)`,
- la place dans `pending: ArrayDeque<Flashcard>`,
- présente les cartes une par une,
- applique le calcul long terme immédiatement dans `gradeCard()`,
- passe ensuite directement à la carte suivante.

Conséquences :
- il n’existe pas de lot fixe de session conservé jusqu’à validation,
- il n’existe pas de boucle locale sur les questions non validées,
- il n’existe pas de validation locale `2 x Je l’ai` / `1 x Trop facile`,
- la sortie de `à travailler` est actuellement immédiate, alors qu’elle doit devenir effective seulement en fin de session,
- il n’existe pas de persistance de session interrompue,
- il n’existe pas de bouton d’annulation de la dernière réponse,
- les défis actuels ne sont pas déclenchés selon la nouvelle logique de session.

### `FlashcardEntity.kt` / `FlashcardMapper.kt`
Le stockage actuel repose sur :
- un `state` unique au niveau carte,
- deux blocs `Sm2DataEmbedded` (`mot -> définition` et `définition -> mot`),
- un mapping de statut simplifié : `TO_LEARN` / `LEARNING` / `KNOWN`.

Conséquences :
- le statut produit n’est pas calculé correctement par face,
- `connu` n’est pas déterminé à partir de `t4` pour les 2 questions ensemble,
- il n’existe pas de projection regroupée par carte conforme à la spec,
- les données nécessaires à la nouvelle fonction de délai n’existent pas encore.

### `FlashcardDao.kt`
La requête `getDue(now, limit)` :
- travaille au niveau carte,
- considère due une carte si une des deux faces est due,
- ordonne selon le `nextReview` le plus faible,
- limite en nombre de cartes, pas en nombre de questions.

Conséquences :
- impossible de sélectionner les `N` premières questions priorisées,
- impossible de distinguer correctement « déjà commencée et due » vs « jamais commencée »,
- impossible de respecter l’ordre global de questions exigé par la spec.

### Réglages
Les valeurs actuelles sont incohérentes avec la spec :
- `ReviewViewModel.DEFAULT_SESSION_SIZE = 20`
- `AdminPrefsRepository.DEFAULT_SESSION_SIZE = 20`
- `UserPrefsRepository.DEFAULT_CARDS_PER_SESSION = 20`

La cible impose :
- valeur par défaut `10`,
- libellé UI non ambigu,
- paramètre pensé comme lot initial de travail, pas comme nombre total d’écrans.

---

## 3. Décision d’architecture

## 3.1 Garder la carte comme agrégat, ajouter une persistance par question

La solution cible la plus stable pour ce projet est :
- conserver `Flashcard` comme agrégat principal de contenu (`mot`, `définition`, métadonnées, favoris, etc.),
- ajouter une persistance explicite de progression **par question**,
- ajouter une persistance explicite de **session en cours**.

Cela évite de casser toute la base métier existante, tout en permettant :
- la sélection par question,
- le calcul de délai par question,
- le regroupement par carte pour l’UI produit.

---

## 3.2 Nouveaux concepts métier à introduire

### A. Type de question
Créer un type explicite :
- `WORD_TO_DEFINITION`
- `DEFINITION_TO_WORD`

### B. Identifiant de question
Chaque question doit être identifiable de manière stable.

Proposition :
- `questionId = "{cardId}::WORD_TO_DEFINITION"`
- `questionId = "{cardId}::DEFINITION_TO_WORD"`

### C. Progression persistante par question
Chaque question doit stocker au minimum :
- `cardId`
- `questionType`
- `globalOrder`
- `level`
- `intervalIndex`
- `peakIntervalIndex`
- `weightedSuccess`
- `weightedFailure`
- `recentStreak`
- `recoveryReserve`
- `currentIntervalDurationMs`
- `nextDueAt`
- `lastSessionFirstAnswerAt`
- `lastAskedAt`
- `firstAnsweredAt` *(pour savoir si la question est déjà commencée)*
- `statusOverride` éventuel seulement si nécessaire

### D. État local de session par question
La session doit stocker pour chaque question sélectionnée :
- sa position dans l’ordre de session,
- son nombre de `Je l’ai` dans la session,
- son nombre de `À revoir` dans la session,
- si elle est validée localement,
- sa première réponse de session,
- si un QCM est déjà planifié,
- si une question orthographique additionnelle est déjà planifiée,
- si un défi remplaçant est planifié,
- l’historique local utile au bouton retour.

### E. Session persistante
Une session persistante doit contenir :
- `sessionId`
- `createdAt`
- `sessionSize`
- le lot de questions sélectionnées
- leur ordre de session
- la position courante dans la boucle
- les insertions planifiées (`QCM`, `matching`, défi remplaçant, question orthographique)
- l’historique des réponses locales
- les mises à jour long terme encore non committées

---

## 4. Fichiers à créer / modifier

## 4.1 Domaine / moteur

### À créer
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewQuestionType.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewQuestionProgress.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewCardAggregateState.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewSessionSnapshot.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewSessionEvent.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/logic/ReviewIntervalEngine.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/logic/ReviewSessionPlanner.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/logic/ReviewSessionEngine.kt`

### À modifier
- `app/src/main/java/com/example/lexicaandroid2/domain/model/Flashcard.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/repository/FlashcardRepository.kt`

---

## 4.2 Data / Room

### À créer
- `app/src/main/java/com/example/lexicaandroid2/data/local/ReviewQuestionProgressEntity.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/local/ReviewSessionSnapshotEntity.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/local/ReviewSessionQuestionEntity.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/local/ReviewSessionEventEntity.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/local/ReviewQuestionDao.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/local/ReviewSessionDao.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/mapper/ReviewQuestionMapper.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/mapper/ReviewSessionMapper.kt`

### À modifier
- `app/src/main/java/com/example/lexicaandroid2/data/local/FlashcardDao.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/repository/FlashcardRepositoryImpl.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/local/FlashcardEntity.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/mapper/FlashcardMapper.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/local/LexicaDatabase.kt` *(ou fichier équivalent de base Room)*

### Migration Room à prévoir
- ajout des tables de progression par question,
- ajout des tables de session persistante,
- migration des 2 blocs `sm2MotVersDef` / `sm2DefVersMot` existants vers 2 lignes question par carte,
- conservation des cartes existantes.

---

## 4.3 Présentation / Review

### À modifier fortement
- `app/src/main/java/com/example/lexicaandroid2/presentation/review/ReviewViewModel.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/review/ReviewScreen.kt`

### À créer probablement
- `app/src/main/java/com/example/lexicaandroid2/presentation/review/ReviewSessionUiModels.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/review/ReviewUndoManager.kt` *(optionnel si intégré directement au moteur de session)*

### Zone défis / overlays à réaligner
- `app/src/main/java/com/example/lexicaandroid2/presentation/review/challenge/ChallengeOverlay.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/review/challenge/KeywordExtractor.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/review/challenge/SemanticValidator.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/review/challenge/TFLiteSemanticValidator.kt`

Objectif :
- garder les briques de validation existantes,
- mais faire dépendre leur déclenchement du nouveau moteur de session.

---

## 4.4 Réglages / produit

### À modifier
- `app/src/main/java/com/example/lexicaandroid2/presentation/settings/UserPrefsRepository.kt`
- `app/src/main/java/com/example/lexicaandroid2/presentation/admin/AdminPrefsRepository.kt`
- écrans / ViewModels de réglages et admin qui exposent ce paramètre

Décisions :
- valeur par défaut = `10`
- borne à confirmer, mais `5..50` peut être conservée
- libellé UI à reformuler autour du « lot initial de travail »

---

## 5. Cible fonctionnelle détaillée par couche

## 5.1 Couche persistance

### 5.1.1 Progression par question
Au lieu de recalculer l’état à partir de `Sm2Stats` simplifiés, la base doit stocker un enregistrement par question.

Une carte produit donc toujours 2 lignes persistantes :
- une ligne `WORD_TO_DEFINITION`
- une ligne `DEFINITION_TO_WORD`

### 5.1.2 Projection produit par carte
Les listes `à travailler`, `en cours`, `connu` ne doivent pas être stockées comme vérité brute au niveau carte.

La vérité persistante devient :
- la progression par question,
- les échéances par question.

Le statut produit devient une **projection calculée** :
- `à travailler` si au moins une question de la carte est due,
- `en cours` si aucune question n’est due et si les 2 questions ne sont pas encore `>= t4`,
- `connu` si les 2 questions sont `>= t4` et qu’aucune n’est expirée.

---

## 5.2 Couche sélection / planification

Le `ReviewSessionPlanner` doit faire 2 choses distinctes.

### Étape 1 — construire l’ordre global priorisé
Rassembler les questions éligibles puis trier selon :
1. questions déjà commencées et dues,
2. parmi elles, celles dues depuis le plus longtemps,
3. sinon questions jamais commencées,
4. parmi elles, cartes les plus anciennes d’abord,
5. dans cet ordre global, les 2 questions d’une carte se suivent.

### Étape 2 — construire l’ordre de session
Prendre les `N` premières questions de l’ordre global priorisé, puis :
- les mélanger,
- éviter si possible que les 2 questions d’une même carte se suivent,
- conserver cet ordre pendant toute la session.

Sortie attendue :
- `selectedQuestions`
- `sessionOrder`
- `remainingQuestionsCount`

---

## 5.3 Couche session

Le `ReviewSessionEngine` doit devenir la source de vérité locale pendant la session.

Il doit gérer :
- boucle sur lot fixe,
- validation locale par question,
- suppression progressive des questions validées,
- comptage des réponses locales,
- insertion d’événements annexes,
- annulation de la dernière réponse,
- reprise de session.

### Validation locale d’une question
Règles à implémenter :
- `2 x Je l’ai` dans la session,
- ou `1 x Trop facile`,
- ou `1 x Je l’ai` dès la première présentation si intervalle courant `> t2`,
- sauf si la première réponse de session est `À revoir`, auquel cas il faudra quand même `2 x Je l’ai`,
- ou sortie forcée après `5 x À revoir`.

### Calcul long terme
Le moteur doit mémoriser :
- la première réponse de session,
- son contexte temporel,
- la progression calculée cible,
- sans persister immédiatement sur la progression principale.

Le commit long terme doit être déclenché uniquement :
- en clôture effective de session.

---

## 5.4 Couche événements annexes

## 5.4.1 Matching intégré
Déclenchement :
- chaque fois que le volume de travail atteint la taille du lot initial.

Clarification :
- le volume de travail = nombre d'interactions normales auxquelles l'utilisateur a répondu dans la session
- les événements annexes (`QCM`, `matching`, défi, orthographe additionnelle) ne comptent pas dans ce compteur

Sélection :
- 5 cartes de la session,
- priorité aux cartes ayant accumulé le plus de `À revoir`.

Impact :
- aucun effet sur le calcul long terme,
- vaut `Je l’ai` carte par carte correctement matchée.

Clarification :
- si les 2 questions d'une même carte sont présentes dans la session, une bonne correspondance crédite localement les 2 questions
- si une seule question de la carte est présente dans la session, seule cette question est créditée

## 5.4.2 QCM intégré
Déclenchement :
- dès qu’une question atteint son `3e À revoir`.

Insertion :
- aléatoire entre le déclenchement et la prochaine occurrence de la même question.

Impact :
- aucun effet long terme,
- vaut `Je l’ai` pour l’avancement de session.

## 5.4.3 Question orthographique additionnelle
Déclenchement :
- pour chaque question `Mot -> Définition` présente dans la session.

Impact :
- aucun effet long terme,
- aucun effet sur la validation de session,
- peut être passée.

Clarification :
- si un défi orthographique remplaçant a déjà été armé de manière persistante pour la carte concernée, la question orthographique additionnelle n'est pas insérée

## 5.4.4 Défis remplaçants
Déclenchement :
- `3 x Je l’ai` d’affilée,
- ou `1 x Trop facile`,
- sans contrainte « première présentation de session ».

Effet :
- remplace une présentation normale,
- s’insère dans le flux piloté par le moteur de session.

Clarifications :
- le seuil `3 x Je l’ai` peut être atteint sur plusieurs sessions : il doit donc reposer sur un état persistant par question, pas seulement sur l'état local de session
- quand le seuil est atteint, on arme un défi remplaçant pour la **prochaine première présentation** de cette question dans une session future
- le défi est alors consommé à cette première présentation et ne vient jamais en plus d'une question normale
- comme il remplace cette première présentation, sa réponse devient la **première réponse de session** de la question et doit être utilisée pour le calcul long terme du délai
- en revanche, les autres événements annexes (`matching`, `QCM`, orthographe additionnelle) restent exclus du calcul long terme et ne servent qu'à l'avancement local de session

---

## 5.5 Couche UI

Le `ReviewUiState` doit cesser d’être centré uniquement sur une `Flashcard` courante.

Il devra exposer au minimum :
- l’item courant (`question normale`, `QCM`, `matching`, `défi`, `question orthographique`),
- le compteur de questions restantes à valider,
- l’état de reprise de session,
- la possibilité d’annuler la dernière réponse,
- les overlays et sous-flux annexes,
- l’état de persistance/restauration.

Conséquence UI :
- `ReviewScreen.kt` devra gérer un rendu polymorphe des items de session,
- pas seulement une carte recto/verso.

---

## 6. Séquence d’implémentation recommandée

## Phase 1 — Modèle persistant par question
Objectif : introduire la nouvelle source de vérité sans encore remplacer tout `Review`.

Livrables :
- entités Room question,
- DAO question,
- migration de données depuis les 2 stats SM2 actuelles,
- repository capable de lire / écrire la progression par question.

Critère de sortie :
- chaque carte existante possède 2 progressions question persistées.

## Phase 2 — Nouveau moteur de calcul du délai
Objectif : remplacer `Sm2Algorithm` comme moteur principal de révision pour `Review`.

Livrables :
- `ReviewIntervalEngine`
- fonction de calcul issue de la spec
- tests unitaires sur `À revoir`, `Je l’ai`, `Trop facile`, récupération rapide, montée lente, seuil `t4`.

Critère de sortie :
- les transitions d’intervalle sont testées indépendamment de l’UI.

## Phase 3 — Planificateur de session
Objectif : sélectionner correctement les questions et produire l’ordre de session.

Livrables :
- `ReviewSessionPlanner`
- requêtes repository pour questions dues / jamais commencées
- ordre global priorisé
- ordre de session mélangé avec contrainte anti-jumelles successives.

Critère de sortie :
- tests sur priorités, mélange et tolérance de la 10e question.

## Phase 4 — Moteur de session locale
Objectif : implémenter la boucle, les validations locales et le commit en fin de session.

Livrables :
- `ReviewSessionEngine`
- gestion `2 x Je l’ai`, `1 x Trop facile`, règle `> t2`, `5 x À revoir`
- stockage de la première réponse de session
- sortie de `à travailler` seulement en fin de session.

Critère de sortie :
- une session complète peut tourner sans persister le long terme avant la fin.

## Phase 5 — Persistance de session + undo
Objectif : rendre la session reprenable et annulable.

Livrables :
- tables snapshot session
- restauration automatique d’une session ouverte
- bouton retour / annulation
- historique local des réponses.

Critère de sortie :
- quitter l’écran puis revenir restaure l’état exact.

## Phase 6 — Événements annexes
Objectif : brancher `QCM`, `matching`, orthographe additionnelle et défis.

Livrables :
- insertions planifiées dans la file de session
- intégration au `ReviewUiState`
- raccord avec les validateurs déjà présents.

Critère de sortie :
- les activités annexes vivent dans le flux `Review`, sans impacter le calcul long terme.

## Phase 7 — Projection produit + réglages
Objectif : réaligner l’ensemble de l’app sur les nouveaux états.

Livrables :
- projection `à travailler` / `en cours` / `connu`
- regroupement visuel des 2 faces d’une même carte dans les listes
- réglage session par défaut = `10`
- libellés UI corrigés.

Critère de sortie :
- les compteurs et listes produit reflètent correctement la vérité par question.

---

## 7. Tests à écrire

## 7.1 Tests domaine
- calcul `À revoir -> t0`
- `Trop facile = 3 réussites`
- progression accélérée après forte maîtrise
- progression ralentie après historique difficile
- carte `connu` si 2 questions `>= t4`
- retour à `à travailler` quand une échéance expire

## 7.2 Tests session
- session initiale de 10 questions
- priorité aux questions déjà commencées et dues
- ordre stable pendant la session
- retrait progressif des questions validées
- règle `> t2`
- sortie forcée après `5 À revoir`
- cas particulier `Je l’ai` puis `5 À revoir` => calcul comme `À revoir`
- reprise de session après interruption
- undo de la dernière réponse

## 7.3 Tests UI / intégration
- compteur = nombre de questions restantes à valider
- session vide = fin correcte sans crash
- QCM inséré avant prochaine occurrence de la question fautive
- matching sur 5 cartes les plus fautives
- question orthographique skippable sans effet session

---

## 8. Risques techniques

### Risque 1 — Migration Room
Le passage carte -> question pour la progression est la partie la plus sensible.

Réduction du risque :
- introduire les nouvelles tables sans supprimer immédiatement l’ancien stockage,
- migrer d’abord en écriture doublée si besoin,
- supprimer l’ancien chemin seulement quand les tests sont verts.

### Risque 2 — Complexité du `ReviewViewModel`
Le ViewModel actuel est déjà chargé.

Réduction du risque :
- déplacer la logique métier dans `ReviewSessionPlanner` et `ReviewSessionEngine`,
- garder `ReviewViewModel` comme orchestrateur d’état UI.

### Risque 3 — Couplage avec les défis existants
Les défis sont déjà branchés à l’ancien moteur.

Réduction du risque :
- conserver les validateurs existants,
- remplacer seulement la logique de déclenchement et d’insertion.

---

## 9. Premier incrément recommandé

Le premier incrément à coder doit être :
1. nouvelles entités question + migration,
2. `ReviewIntervalEngine`,
3. `ReviewSessionPlanner`,
4. tests unitaires associés.

Raison :
- ce sont les fondations les moins visibles UI,
- elles verrouillent la vraie source de vérité avant la refonte complète de `ReviewScreen`.

---

## 10. Résultat attendu après implémentation complète

À la fin du chantier, `Review` devra fonctionner comme un moteur de session piloté par question avec :
- priorité correcte des questions dues,
- lot initial de `10` par défaut,
- validation locale indépendante du calcul long terme,
- calcul de délai continu puis discret par paliers `t0..t8`,
- statut `connu` au niveau carte entière,
- reprise de session,
- annulation de la dernière réponse,
- insertion contrôlée de `QCM`, `matching`, défis et question orthographique.

Ce document sert de feuille de route d’implémentation pour la branche `integration/Polo-1`.