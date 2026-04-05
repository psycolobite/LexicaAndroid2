# Polo-1 1 — Persistance par question + migration Room

## Résumé
Implémentation de la source de vérité persistante par question pour `Review`, sans casser l’agrégat `Flashcard` existant.

Cette livraison introduit :
- un type explicite de question (`WORD_TO_DEFINITION`, `DEFINITION_TO_WORD`)
- une progression persistée par question
- une migration Room `5 -> 6` qui convertit les deux blocs SM2 legacy en 2 lignes `review_question_progress` par carte
- une synchronisation automatique entre écriture carte legacy et écriture question-level

## Fichiers principaux
### Domaine
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewQuestionType.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewQuestionProgress.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/repository/FlashcardRepository.kt`

### Data / Room
- `app/src/main/java/com/example/lexicaandroid2/data/local/ReviewQuestionProgressEntity.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/local/ReviewQuestionDao.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/local/LexicaDatabase.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/mapper/ReviewQuestionMapper.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/repository/FlashcardRepositoryImpl.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/repository/WordReserveRepositoryImpl.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/importer/DataImporter.kt`
- `app/src/main/java/com/example/lexicaandroid2/MainActivity.kt`

### Tests
- `app/src/test/java/com/example/lexicaandroid2/data/mapper/ReviewQuestionMapperTest.kt`
- `app/src/test/java/com/example/lexicaandroid2/data/repository/FlashcardRepositoryImplTest.kt`
- `app/src/test/java/com/example/lexicaandroid2/data/repository/WordReserveRepositoryImplTest.kt`

## Détails d’implémentation
### 1. Nouveau modèle question-level
Chaque carte produit 2 identifiants stables :
- `{cardId}::WORD_TO_DEFINITION`
- `{cardId}::DEFINITION_TO_WORD`

La progression persistée contient notamment :
- `cardId`
- `questionType`
- `globalOrder`
- `intervalIndex`
- `peakIntervalIndex`
- `weightedSuccess`
- `weightedFailure`
- `recentStreak`
- `currentIntervalDurationMs`
- `nextDueAt`
- `firstAnsweredAt`
- `lastAskedAt`
- `lastSessionFirstAnswerAt`

### 2. Migration Room `5 -> 6`
Ajout de la table `review_question_progress` + index :
- `cardId`
- `nextDueAt`
- unique `(cardId, questionType)`

Migration des données legacy :
- lecture de `sm2_mot_vers_def_*`
- lecture de `sm2_def_vers_mot_*`
- insertion de 2 lignes par carte existante

Règles appliquées lors de la migration :
- `questionId` dérivé du `cardId` + `questionType`
- `globalOrder` dérivé de `dateAjout`
- `intervalIndex` approximé à partir de l’intervalle legacy en jours
- `currentIntervalDurationMs` dérivé prioritairement de `lastReview -> nextReview`, sinon de `interval * 1 jour`, sinon valeur courte par défaut
- `firstAnsweredAt` positionné seulement si la question a déjà été commencée (`totalReviews > 0` ou `repetitions > 0`)

## Compatibilité / stratégie de transition
Pour éviter de casser l’existant :
- `Flashcard` reste l’agrégat de contenu principal
- les champs SM2 legacy dans `FlashcardEntity` sont conservés
- toute écriture carte persistée maintient aussi les 2 lignes question-level

Chemins synchronisés :
- `saveCard(...)`
- `updateCardProgress(...)`
- `deleteCard(...)`
- ajout depuis la réserve
- import initial de la base legacy

## Validation réalisée
Tests unitaires exécutés avec succès :
- `com.example.lexicaandroid2.data.mapper.ReviewQuestionMapperTest`
- `com.example.lexicaandroid2.data.repository.FlashcardRepositoryImplTest`
- `com.example.lexicaandroid2.data.repository.WordReserveRepositoryImplTest`

Commande exécutée :
```powershell
.\gradlew.bat --no-daemon testDebugUnitTest --tests "com.example.lexicaandroid2.data.mapper.ReviewQuestionMapperTest" --tests "com.example.lexicaandroid2.data.repository.FlashcardRepositoryImplTest" --tests "com.example.lexicaandroid2.data.repository.WordReserveRepositoryImplTest"
```

Résultat : `BUILD SUCCESSFUL`

## Limites connues / suite
Cette tâche prépare le socle mais ne remplace pas encore le moteur de sélection/session actuel :
- `getCardsToReview(...)` reste carte-level pour l’instant
- la planification question-level sera introduite dans `Polo-1 2` et `Polo-1 3`
- la projection produit complète (`à travailler` / `en cours` / `connu`) reste à réaligner ensuite

## Recommandation d’intégration
Intégrer cette base avant de démarrer :
- `Polo-1 2` — `ReviewIntervalEngine`
- `Polo-1 3` — `ReviewSessionPlanner`

Le socle persistant question-level est désormais prêt pour ces étapes.

