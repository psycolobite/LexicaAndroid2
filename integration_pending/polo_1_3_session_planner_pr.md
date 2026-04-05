# Polo-1 3 — ReviewSessionPlanner

## Résumé
Implémentation du planificateur de session `Polo-1`, chargé de :
- sélectionner les questions selon les priorités produit
- construire l'ordre global priorisé
- produire un ordre de session mélangé en évitant si possible deux faces d'une même carte à la suite

Aucun point bloquant supplémentaire n'était à intégrer avant cette étape :
- la persistance question-level (`Polo-1 1`) était prête
- le moteur d'intervalle (`Polo-1 2`) était prêt

## Fichiers principaux
### Domaine
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewSessionPlan.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/logic/ReviewSessionPlanner.kt`

### Repository / Data
- `app/src/main/java/com/example/lexicaandroid2/domain/repository/FlashcardRepository.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/repository/FlashcardRepositoryImpl.kt`
- `app/src/main/java/com/example/lexicaandroid2/data/local/ReviewQuestionDao.kt`

### Tests
- `app/src/test/java/com/example/lexicaandroid2/domain/logic/ReviewSessionPlannerTest.kt`
- `app/src/test/java/com/example/lexicaandroid2/data/repository/FlashcardRepositoryImplTest.kt`

## Contrat livré
### `ReviewSessionPlan`
Expose :
- `selectedQuestions`
- `sessionOrder`
- `remainingQuestionsCount`

### `ReviewSessionPlanner`
Point d'entrée principal :
- `buildPlan(sessionSize, nowMs)`

Le planner ne persiste rien lui-même.

## Règles implémentées
### 1. Bucket prioritaire `déjà commencées et dues`
Le planner récupère les questions :
- avec `firstAnsweredAt != null`
- et `nextDueAt <= now`

Ordre interne :
- priorité aux cartes/questions dues depuis le plus longtemps (`nextDueAt` le plus ancien)
- à égalité, ordre croissant de `globalOrder`

### 2. Bucket complément `jamais commencées`
Le planner complète ensuite avec :
- les questions `firstAnsweredAt == null`
- triées par ancienneté (`globalOrder` croissant)

### 3. Jumelles consécutives dans l'ordre global
À l'intérieur d'un même bucket de priorité, les questions d'une même carte sont regroupées et restituées dans l'ordre `globalOrder`.

Cela permet de conserver l'idée de jumelles consécutives sans casser la règle de priorité due > never-started.

### 4. Ordre de session mélangé
Le planner prend ensuite les `N` premières questions de l'ordre global priorisé puis construit `sessionOrder` :
- mélange déterminé par une source `Random` injectable
- évite si possible deux questions de même `cardId` à la suite
- accepte une adjacency jumelle seulement si aucune alternative n'existe

### 5. Compteur restant
`remainingQuestionsCount` correspond au nombre de questions éligibles non retenues après sélection des `N` premières.

Pour cela, le repository expose aussi les comptages exacts :
- `countStartedDueQuestionProgress(now)`
- `countNeverStartedQuestionProgress()`

## Validation réalisée
### Tests ciblés verts
Commande :
```powershell
.\gradlew.bat --no-daemon testDebugUnitTest --tests "com.example.lexicaandroid2.domain.logic.ReviewSessionPlannerTest" --tests "com.example.lexicaandroid2.data.repository.FlashcardRepositoryImplTest" --tests "com.example.lexicaandroid2.domain.logic.ReviewIntervalEngineTest"
```

Résultat : `BUILD SUCCESSFUL`

### Non-régression complémentaire verte
Commande :
```powershell
.\gradlew.bat --no-daemon testDebugUnitTest --tests "com.example.lexicaandroid2.presentation.review.ReviewViewModelTest" --tests "com.example.lexicaandroid2.data.repository.WordReserveRepositoryImplTest" --tests "com.example.lexicaandroid2.data.mapper.ReviewQuestionMapperTest" --tests "com.example.lexicaandroid2.data.local.FlashcardEntityTest"
```

Résultat : `BUILD SUCCESSFUL`

## Limites / suite
Cette tâche livre uniquement le planificateur de session.

Le branchement complet dans le flux de révision reste à faire dans :
- `Polo-1 4` — `ReviewSessionEngine`
- puis persistance/reprise/undo (`Polo-1 5`)
- puis refonte `ReviewViewModel` / `ReviewScreen`

## Conclusion
Le projet dispose maintenant :
- d'une persistance question-level (`Polo-1 1`)
- d'un moteur de calcul du délai (`Polo-1 2`)
- d'un planificateur de session priorisé (`Polo-1 3`)

Le socle est prêt pour attaquer le moteur de session locale `Polo-1 4`.

