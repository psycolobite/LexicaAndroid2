# Polo-1 4 — ReviewSessionEngine

## Résumé
Implémentation du moteur de session locale `Polo-1`, chargé de piloter le lot fixe de questions pendant la session sans persister immédiatement le long terme.

Cette livraison couvre :
- la boucle locale sur ordre de session fixe
- la validation locale des questions
- le retrait progressif des questions validées
- la conservation de la première réponse de session comme vérité long terme locale
- la production des progressions long terme seulement à la clôture de session

## Fichiers principaux
### Domaine
- `app/src/main/java/com/example/lexicaandroid2/domain/logic/ReviewSessionEngine.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewSessionValidationReason.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewSessionQuestionState.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewSessionState.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewSessionCompletion.kt`

### Tests
- `app/src/test/java/com/example/lexicaandroid2/domain/logic/ReviewSessionEngineTest.kt`

## Contrat livré
### `ReviewSessionEngine.start(plan)`
Construit un état local à partir de `ReviewSessionPlan` :
- état local par question
- ordre de session fixe
- compteur de questions restantes à valider
- question courante

### `ReviewSessionEngine.answerCurrentQuestion(state, answer, answeredAt)`
Applique une réponse locale à la question courante, met à jour les compteurs locaux et avance à la prochaine question non validée.

### `ReviewSessionEngine.completeSession(state, finishedAt)`
Autorisé uniquement quand la session est terminée.
Produit :
- `finalQuestionStates`
- `finalQuestionProgress`

Le calcul long terme est déclenché à `finishedAt`, pas au moment de la première réponse.

## Règles métier implémentées
### Validation locale
- `2 x Je l'ai` => validation
- `1 x Trop facile` => validation immédiate
- `1 x Je l'ai` à la première présentation si intervalle courant `> t2` => validation immédiate
- si la première réponse est `À revoir`, la règle spéciale `> t2` ne s'applique plus et il faut revenir à `2 x Je l'ai`
- `5 x À revoir` => sortie forcée de la session

### Long terme
- la première réponse de session est mémorisée comme vérité long terme locale
- **exception** : si la question sort via `5 x À revoir`, l'effet long terme devient `À revoir`
- le moteur n'écrit rien en persistance
- le moteur ne calcule la progression finale qu'à la clôture de session via `ReviewIntervalEngine`

## Modèles locaux introduits
### `ReviewSessionQuestionState`
Contient notamment :
- `firstAnswer`
- `firstAnswerAt`
- `lastAnswer`
- `lastAnsweredAt`
- `presentationCount`
- `gotItCount`
- `againCount`
- `isValidated`
- `validationReason`
- `effectiveLongTermAnswer`

### `ReviewSessionState`
Contient notamment :
- `sessionOrderQuestionIds`
- `questionStates`
- `currentQuestionId`
- `currentOrderIndex`
- `remainingQuestionsToValidate`
- `isFinished`

## Validation réalisée
### Tests ciblés verts
Commande :
```powershell
.\gradlew.bat --no-daemon testDebugUnitTest --tests "com.example.lexicaandroid2.domain.logic.ReviewSessionEngineTest" --tests "com.example.lexicaandroid2.domain.logic.ReviewSessionPlannerTest" --tests "com.example.lexicaandroid2.domain.logic.ReviewIntervalEngineTest"
```

Résultat : `BUILD SUCCESSFUL`

### Non-régression complémentaire verte
Commande :
```powershell
.\gradlew.bat --no-daemon testDebugUnitTest --tests "com.example.lexicaandroid2.presentation.review.ReviewViewModelTest" --tests "com.example.lexicaandroid2.data.repository.FlashcardRepositoryImplTest" --tests "com.example.lexicaandroid2.data.repository.WordReserveRepositoryImplTest" --tests "com.example.lexicaandroid2.data.mapper.ReviewQuestionMapperTest" --tests "com.example.lexicaandroid2.data.local.FlashcardEntityTest"
```

Résultat : `BUILD SUCCESSFUL`

## Limites / suite
Cette tâche ne couvre pas encore :
- la persistance de session interrompue
- la reprise exacte au retour
- l'annulation / undo
- l'insertion des événements annexes (QCM, matching, défis remplaçants, orthographe additionnelle)
- le branchement complet de `ReviewViewModel` / `ReviewScreen`

Ces sujets relèvent des tâches suivantes :
- `Polo-1 5` — session persistante + reprise + annulation
- `Polo-1 6` — refonte `ReviewViewModel` / `ReviewScreen`
- `Polo-1 7` — activités annexes dans la session

## Conclusion
Le socle métier `Polo-1` dispose maintenant :
- de la persistance par question
- du moteur d'intervalle
- du planificateur de session
- du moteur de session locale

Le prochain incrément naturel est `Polo-1 5` pour rendre la session reprenable et annulable.

