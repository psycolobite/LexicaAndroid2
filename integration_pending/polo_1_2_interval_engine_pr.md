# Polo-1 2 — ReviewIntervalEngine

## Résumé
Implémentation du moteur pur de calcul long terme pour la révision par question.

Cette livraison remplace le socle théorique de l'algorithme par une implémentation testable, indépendante de l'UI :
- `À revoir` => chute à `t0`
- `Je l'ai` => progression continue selon ratio de maîtrise, streak et récupération
- `Trop facile` => équivalent à 3 réussites condensées
- conversion centralisée `intervalIndex -> durée`

## Fichiers principaux
### Domaine
- `app/src/main/java/com/example/lexicaandroid2/domain/model/ReviewAnswer.kt`
- `app/src/main/java/com/example/lexicaandroid2/domain/logic/ReviewIntervalEngine.kt`

### Data
- `app/src/main/java/com/example/lexicaandroid2/data/mapper/ReviewQuestionMapper.kt`

### Tests
- `app/src/test/java/com/example/lexicaandroid2/domain/logic/ReviewIntervalEngineTest.kt`

## API livrée
### `ReviewAnswer`
Trois réponses métier sont formalisées :
- `AGAIN`
- `GOT_IT`
- `TOO_EASY`

### `ReviewIntervalEngine`
Point d'entrée principal :
- `applyFirstAnswer(progress, answer, nowMs)`

Le moteur produit un nouveau `ReviewQuestionProgress` avec mise à jour de :
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
- `firstAnsweredAt`

## Règles implémentées
### Ratio de maîtrise
```text
R = weightedSuccess / (weightedSuccess + weightedFailure + 1)
```

### Pondération avec décroissance
```text
weightedSuccess' = 0.85 * weightedSuccess + successWeight
weightedFailure' = 0.85 * weightedFailure + failureWeight
```

### Éligibilité temporelle
```text
E = min(1, elapsedSinceLastRealReview / currentIntervalDuration)
```

### `À revoir`
- retour à `t0`
- `recentStreak = 0`
- construction d'une `recoveryReserve` basée sur `peakIntervalIndex` et `R`

### `Je l'ai`
- gain continu basé sur `R`, `recentStreak`, `recoveryReserve` et `E`
- progression discrète via `floor(level)`

### `Trop facile`
- gain condensé plus fort
- `recentStreak += 3`
- consommation plus rapide de la `recoveryReserve`

## Conversion des intervalles
Échelle livrée :
- `t0 = 10 min`
- `t1 = 1 h`
- `t2 = 1 j`
- `t3 = 1 semaine`
- `t4 = 1 mois`
- `t5 = 3 mois`
- `t6 = 6 mois`
- `t7 = 1 an`
- `t8 = 2 ans`
- puis doublement (`4 ans`, `8 ans`, `16 ans`, ...)

Cette conversion est aussi réutilisée par `ReviewQuestionMapper.kt` pour éviter un drift entre migration legacy et nouveau moteur.

## Validation réalisée
### Tests ciblés verts
Commande :
```powershell
.\gradlew.bat --no-daemon testDebugUnitTest --tests "com.example.lexicaandroid2.domain.logic.ReviewIntervalEngineTest" --tests "com.example.lexicaandroid2.data.mapper.ReviewQuestionMapperTest" --tests "com.example.lexicaandroid2.data.repository.FlashcardRepositoryImplTest"
```

Résultat : `BUILD SUCCESSFUL`

### Non-régression complémentaire verte
Commande :
```powershell
.\gradlew.bat --no-daemon testDebugUnitTest --tests "com.example.lexicaandroid2.presentation.review.ReviewViewModelTest" --tests "com.example.lexicaandroid2.data.repository.WordReserveRepositoryImplTest" --tests "com.example.lexicaandroid2.data.local.FlashcardEntityTest"
```

Résultat : `BUILD SUCCESSFUL`

## Limites / suite
Cette tâche livre le moteur pur, mais ne rebranche pas encore le flux complet de session sur ce moteur.

Le branchement métier complet est attendu dans les tâches suivantes :
- `Polo-1 3` — `ReviewSessionPlanner`
- `Polo-1 4` — `ReviewSessionEngine`
- puis refonte `ReviewViewModel`/`ReviewScreen`

## Conclusion
Le moteur d'intervalle cible est maintenant disponible, pur, testé, et prêt à être consommé par la planification et le moteur de session `Polo-1`.

