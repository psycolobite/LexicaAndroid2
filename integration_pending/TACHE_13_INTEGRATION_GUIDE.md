# Guide d'intégration TACHE_13 — Différences et décisions

*Rédigé par l'agent développeur — 2026-03-09*

Ce fichier documente uniquement **ce qui diffère** de la description originale de la TACHE_13 ou ce qui **n'était pas précisé** et a nécessité une décision de conception.

---

## 1. Hook ReviewViewModel → extracté dans un Helper (non prévu)

### Ce qui était demandé
La tâche demandait d'ajouter directement dans `ReviewViewModel.gradeCard()` :
```kotlin
val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
val existing = dailyStatDao.getByDate(today) ?: DailyReviewStat(today)
dailyStatDao.upsert(existing.copy(...))
```
Et d'injecter `DailyReviewStatDao` directement dans `ReviewViewModel`.

### Ce qui a été fait à la place
La logique a été encapsulée dans un objet singleton :
```
features/gamification/data/DailyReviewStatHelper.kt
```
```kotlin
object DailyReviewStatHelper {
    suspend fun recordReview(dao: DailyReviewStatDao, isCorrect: Boolean) { ... }
}
```

### Pourquoi
- La contrainte de la tâche interdit de **modifier `ReviewViewModel.kt` directement** (tout via PR).
- Extraire la logique dans un helper permet au Chef d'Orchestre de coller **une seule ligne** dans `ReviewViewModel.gradeCard()` sans risque de régression :
  ```kotlin
  dailyReviewStatDao?.let { DailyReviewStatHelper.recordReview(it, quality >= 3) }
  ```
- La PR fournit les instructions exactes de câblage.

---

## 2. DailyReviewStatDao nullable dans ProfileViewModel (non prévu)

### Ce qui était demandé
La tâche supposait que le DAO serait disponible directement.

### Ce qui a été fait
`DailyReviewStatDao?` est **nullable** dans `ProfileViewModel` et `ProfileViewModelFactory` :
```kotlin
class ProfileViewModel(
    ...
    private val dailyReviewStatDao: DailyReviewStatDao? = null  // ← nullable
)
```

### Pourquoi
La migration Room `4 → 5` doit être faite par le Chef d'Orchestre **avant** de passer le DAO.
Rendre le DAO nullable permet à l'appli de compiler et fonctionner **sans crash** avant la migration :
- Si `dao == null` → `observeDailyStats()` retourne immédiatement → tous les compteurs restent à `0`
- Aucun écran blanc, aucune exception

---

## 3. `buildLast7Days()` — Jours vides comblés (non prévu explicitement)

### Ce qui était demandé
```kotlin
getLast30Days().take(7)
```
Simple prise des 7 premiers enregistrements.

### Ce qui a été fait
Une fonction `buildLast7Days()` reconstruit une liste de **7 entrées exactes** couvrant les 7 derniers jours calendaires, en comblant les jours sans révision avec `DailyReviewStat(dateKey = "...", cardsReviewed = 0)`.

### Pourquoi
- `getLast30Days().take(7)` ne retourne que les jours *où il y a eu une révision*, pas les jours vides.
- Le graphique barres aurait des trous ou un nombre de barres variable (3 barres un jour, 7 un autre).
- Avec `buildLast7Days()`, le graphique affiche **toujours 7 barres** avec les bonnes dates, y compris les jours d'inactivité (barre grise à hauteur minimale).

---

## 4. `computeBestStreak()` — Meilleure série 30j (non prévu explicitement)

### Ce qui était demandé
Le tableau des stats listait "Meilleure série — Calculé depuis les 30 derniers jours" sans préciser l'algorithme.

### Ce qui a été fait
Algorithme de série de jours consécutifs :
```kotlin
private fun computeBestStreak(last30: List<DailyReviewStat>): Int {
    // Trie les dates avec au moins 1 révision, calcule les écarts en jours
    // Compte la plus longue suite de jours consécutifs (écart = 1 jour)
}
```

### Pourquoi
C'est la définition naturelle de "meilleure série" pour un outil de révision (identique à Anki).

---

## 5. `StatBox` — 3 boîtes compactes au lieu d'un "grand chiffre centré" (légère variation)

### Ce qui était demandé
> "Cartes vues aujourd'hui → Grand chiffre centré"

### Ce qui a été fait
Un composable `StatBox` réutilisable avec 3 instances en `Row` :
```
[Aujourd'hui] [Réussite 7j] [Meilleure série]
```
Chaque box : label petite police + valeur bold + sous-label.

### Pourquoi
Mettre un seul "grand chiffre centré" gaspille l'espace vertical sur mobile. Grouper 3 stats en une ligne respecte la règle UX de la checklist :
> "Les stats/score sont sur UNE SEULE ligne (Row) avec SpaceBetween, pas en colonne"

---

## 6. `WeekBarChart` — `fillMaxHeight(fraction)` dans un `Row(Alignment.Bottom)` au lieu du snippet de la tâche

### Ce qui était demandé
```kotlin
Row(modifier = Modifier.fillMaxWidth().height(80.dp)) {
    last7Days.forEach { day ->
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxHeight(heightFraction)
            .background(...)
            .align(Alignment.Bottom))
    }
}
```

### Problème du snippet original
`.align(Alignment.Bottom)` n'est pas disponible directement sur `Modifier` dans un `Row` — il faut `Modifier.align()` dans le scope du `RowScope`. Le snippet de la tâche produirait une erreur de compilation.

### Ce qui a été fait
```kotlin
Row(
    modifier = Modifier.fillMaxWidth().height(80.dp),
    verticalAlignment = Alignment.Bottom   // ← alignement sur le Row
) {
    days.forEach { day ->
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(fraction.coerceAtLeast(0.04f))  // hauteur min 4%
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                .background(...)
        )
    }
}
```

### Ajout : hauteur minimale visible
`coerceAtLeast(0.04f)` assure qu'une barre à 0 révision est quand même visible (4% = ~3dp) en gris, pour que l'utilisateur voit bien les jours vides vs les jours actifs.

---

## 7. `ProfileScreen` — `verticalScroll` ajouté (non prévu)

### Ce qui était demandé
Rien de spécifié sur le scroll.

### Ce qui a été fait
`.verticalScroll(rememberScrollState())` ajouté sur le `Column` principal.

### Pourquoi
L'ajout de la section stats alourdit l'écran. Sur petits écrans (< 5.5"), le contenu dépasse la hauteur disponible. Sans scroll, les boutons "Se connecter" et "Retour" seraient hors écran.

---

## Résumé des actions d'intégration requises

| # | Action | Fichier concerné | Complexité |
|---|--------|-----------------|------------|
| 1 | Ajouter `DailyReviewStat` aux entités + version 5 | `LexicaDatabase.kt` | Faible |
| 2 | Ajouter `MIGRATION_4_5` (CREATE TABLE) | `LexicaDatabase.kt` | Faible |
| 3 | Ajouter `abstract fun dailyReviewStatDao()` | `LexicaDatabase.kt` | Trivial |
| 4 | Injecter `dailyReviewStatDao?` dans `ReviewViewModel` + factory | `ReviewViewModel.kt` | Faible |
| 5 | Appeler `DailyReviewStatHelper.recordReview(it, quality >= 3)` dans `gradeCard()` | `ReviewViewModel.kt` | Trivial |
| 6 | Passer `dailyReviewStatDao = database.dailyReviewStatDao()` à `ProfileViewModelFactory` | `LexicaApp.kt` | Trivial |

**Toutes les instructions détaillées avec le code exact sont dans `integration_pending/profile_stats_pr.md`.**

