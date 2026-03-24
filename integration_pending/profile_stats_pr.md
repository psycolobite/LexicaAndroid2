# PR — TACHE_13 : Statistiques Anki-like dans l'onglet Profil
*Livré le : 2026-03-09*
*Agent : Agent Développeur*

---

## Résumé

Cette PR ajoute des statistiques de révision type Anki dans l'onglet Profil :
- Nouvelle entité Room `DailyReviewStat` + DAO
- `ProfileViewModel` étendu avec collecte des 30 derniers jours
- `ProfileScreen` avec section "📊 Mes statistiques" : compteurs + graphique barres 7j (sans lib externe)
- Helper `DailyReviewStatHelper` pour incrémenter les stats depuis `ReviewViewModel`

---

## Fichiers créés

### 1. `features/gamification/data/DailyReviewStat.kt`
Entité Room `daily_review_stats` :
- `dateKey: String` (PK, format `"YYYY-MM-DD"`)
- `cardsReviewed: Int`
- `correctAnswers: Int`
- `totalTimeSeconds: Int`

### 2. `features/gamification/data/DailyReviewStatDao.kt`
DAO avec :
- `getLast30Days(): Flow<List<DailyReviewStat>>`
- `upsert(stat: DailyReviewStat)`
- `getByDate(date: String): DailyReviewStat?`

### 3. `features/gamification/data/DailyReviewStatHelper.kt`
Objet singleton `DailyReviewStatHelper.recordReview(dao, isCorrect)` — à appeler dans `ReviewViewModel.gradeCard()`.

---

## Fichiers modifiés

### 4. `presentation/profile/ProfileViewModel.kt`
- `ProfileUiState` étendu : `todayCards`, `todayCorrect`, `last7Days`, `successRate7Days`, `bestStreak30Days`
- `ProfileViewModel` accepte `DailyReviewStatDao?` (nullable → rétro-compat avant migration)
- `observeDailyStats()` : collecte `getLast30Days()` et calcule toutes les métriques
- `buildLast7Days()` : comble les jours sans données avec `cardsReviewed = 0`
- `computeBestStreak()` : algorithme jours consécutifs sur 30 jours

### 5. `presentation/profile/ProfileScreen.kt`
Section **"📊 Mes statistiques"** ajoutée :
- `StatBox` : 3 boîtes côte à côte (aujourd'hui / réussite 7j / meilleure série)
- `WeekBarChart` : graphique barres 7 jours avec `Box + fillMaxHeight(fraction)` — **sans lib externe**
- Message motivant si `todayCards == 0` : "Aucune révision aujourd'hui — c'est le moment ! 🚀"
- Couleur du taux de réussite : vert ≥ 80%, orange ≥ 50%, rouge < 50%
- `verticalScroll` ajouté sur l'écran complet

---

## Instructions d'intégration pour le Chef d'Orchestre

### ⚠️ Migration Room obligatoire (version 4 → 5)

Dans `LexicaDatabase.kt`, ajouter :

```kotlin
@Database(
    entities = [FlashcardEntity::class, WordReserveEntity::class, UserStatsEntity::class, DailyReviewStat::class],
    version = 5,      // ← était 4
    exportSchema = false
)
abstract class LexicaDatabase : RoomDatabase() {
    // ...existing code...
    abstract fun dailyReviewStatDao(): DailyReviewStatDao   // ← ajouter

    companion object {
        // ...existing migrations...
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_review_stats (
                        dateKey TEXT PRIMARY KEY NOT NULL,
                        cardsReviewed INTEGER NOT NULL DEFAULT 0,
                        correctAnswers INTEGER NOT NULL DEFAULT 0,
                        totalTimeSeconds INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
    }
}
```

### Hook ReviewViewModel

Dans `ReviewViewModel.gradeCard()`, après `repository.updateCardProgress(...)` :

```kotlin
// Incrémenter stats daily (si DAO disponible après migration)
dailyReviewStatDao?.let { dao ->
    viewModelScope.launch {
        DailyReviewStatHelper.recordReview(dao, isCorrect = quality >= 3)
    }
}
```

Injecter `dailyReviewStatDao: DailyReviewStatDao?` dans `ReviewViewModel` et sa factory.

### ProfileScreen dans LexicaApp.kt

Passer le DAO si disponible :

```kotlin
composable(route = Screen.Profile.route) {
    ProfileScreen(
        flashcardRepository = repository,
        userStatsRepository = userStatsRepository,
        authRepository = authRepository,
        onBack = { navController.navigateUp() },
        onSignInRequested = {},
        viewModel = viewModel(
            factory = ProfileViewModelFactory(
                userStatsRepository = userStatsRepository,
                authRepository = authRepository,
                flashcardRepository = flashcardRepository,
                dailyReviewStatDao = database.dailyReviewStatDao()  // ← après migration
            )
        )
    )
}
```

### Build & test

```bash
./gradlew clean :app:assembleDebug
```

---

## Checklist de validation

- [x] `DailyReviewStat` : entité Room avec PK `dateKey` format `"YYYY-MM-DD"`
- [x] `DailyReviewStatDao` : `getLast30Days()` / `upsert()` / `getByDate()`
- [x] `ProfileUiState` : 5 nouveaux champs stats
- [x] `observeDailyStats()` : collecte Flow, calcul rate 7j + best streak 30j
- [x] `buildLast7Days()` : 7 entrées garanties même si pas de données
- [x] Si aucune donnée → affiche `—` et "Aucune révision aujourd'hui — c'est le moment ! 🚀"
- [x] `WeekBarChart` : barres Compose natif `Box + fillMaxHeight`, sans lib externe
- [x] `StatBox` : 3 boîtes en `Row`, hauteur max ~40dp chacune
- [x] `verticalScroll` : l'écran scroll si contenu > hauteur écran
- [x] `DailyReviewStatHelper.recordReview()` : helper prêt pour injection dans `ReviewViewModel`
- [x] `DailyReviewStatDao` nullable dans `ProfileViewModel` → pas de crash avant migration DB
- [x] Aucun fichier Kotlin core modifié (`LexicaDatabase`, `ReviewViewModel`, `LexicaApp`)
- [x] 0 erreur de compilation

---

## Aperçu visuel

```
┌─────────────────────────────────────────┐
│  📊 Mes statistiques                    │
│                                         │
│  ┌──────────┬──────────┬──────────┐     │
│  │Aujourd'  │ Réussite │Meilleure │     │
│  │   hui    │   7j     │  série   │     │
│  │   —      │   82%    │   5j     │     │
│  │Aucune    │bonne rép.│30 derniers│    │
│  └──────────┴──────────┴──────────┘     │
│                                         │
│  Activité des 7 derniers jours          │
│  ┌─────────────────────────────────┐    │
│  │    █         █    █             │    │
│  │    █    █    █    █    █        │    │
│  │    █    █    █    █    █    █   │    │
│  └─────────────────────────────────┘    │
│   J-6  J-5  J-4  J-3  J-2  J-1  Auj   │
│                                         │
│  ┌──────────────┬──────────────┐        │
│  │ Mots appris  │   XP total   │        │
│  │    7024      │    1250      │        │
│  │dans collect. │  niveau 5    │        │
│  └──────────────┴──────────────┘        │
└─────────────────────────────────────────┘
```

