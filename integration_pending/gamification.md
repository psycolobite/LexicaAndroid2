# 🎮 Gamification Module - Integration Guide

## 📋 Résumé du Module

Le module de gamification ajoute un système d'XP, de niveaux et de séries (streaks) pour motiver les utilisateurs.

### Fonctionnalités implémentées

- ✅ Entité Room `UserStatsEntity` pour stocker les statistiques utilisateur
- ✅ DAO `UserStatsDao` avec requêtes Flow
- ✅ Repository pattern avec interface et implémentation
- ✅ `XPCalculator` - Classe utilitaire pour calculer l'XP et les niveaux
- ✅ Composant UI `XpProgressBar` pour afficher la progression
- ✅ `GamificationViewModel` - ViewModel pour gérer l'état
- ✅ `GamificationDemoScreen` - Écran de démonstration complet
- ✅ Tests unitaires complets pour `XPCalculator`

---

## 🔧 Changements Globaux Requis

### 1. Ajouter la table dans `AppDatabase.kt`

**Fichier:** `app/src/main/java/com/example/lexicaandroid2/data/AppDatabase.kt`

**Action:** Ajouter `UserStatsEntity` dans la liste des entités et le DAO correspondant.

```kotlin
@Database(
    entities = [
        FlashcardEntity::class,
        UserStatsEntity::class  // ✨ AJOUTER
    ],
    version = 2,  // ⚠️ INCRÉMENTER LA VERSION
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
    abstract fun userStatsDao(): UserStatsDao  // ✨ AJOUTER
}
```

**Imports à ajouter:**
```kotlin
import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import com.example.lexicaandroid2.features.gamification.data.UserStatsDao
```

---

### 2. Gérer la migration de base de données

**Fichier:** `app/src/main/java/com/example/lexicaandroid2/data/AppDatabase.kt`

**Action:** Ajouter une migration de la version 1 à la version 2

```kotlin
companion object {
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_stats (
                    userId TEXT PRIMARY KEY NOT NULL DEFAULT 'currentUser',
                    xp INTEGER NOT NULL DEFAULT 0,
                    level INTEGER NOT NULL DEFAULT 1,
                    streak INTEGER NOT NULL DEFAULT 0,
                    lastLoginDate INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
        }
    }
    
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "lexica_database"
            )
            .addMigrations(MIGRATION_1_2)  // ✨ AJOUTER
            .build()
            INSTANCE = instance
            instance
        }
    }
}
```

**Import à ajouter:**
```kotlin
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
```

---

### 3. Créer le Repository dans MainActivity (optionnel)

**Fichier:** `app/src/main/java/com/example/lexicaandroid2/MainActivity.kt`

**Action:** Instancier le `UserStatsRepository` et `GamificationViewModel` pour pouvoir l'utiliser dans l'app

```kotlin
// Après la création de la database
val userStatsDao = database.userStatsDao()
val userStatsRepository = UserStatsRepositoryImpl(userStatsDao)
val gamificationViewModel = GamificationViewModel(userStatsRepository)
```

**Imports à ajouter:**
```kotlin
import com.example.lexicaandroid2.features.gamification.data.UserStatsRepositoryImpl
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import com.example.lexicaandroid2.features.gamification.ui.GamificationViewModel
```

---

### 4. Ajouter une route pour l'écran de démonstration (optionnel)

**Fichier:** `app/src/main/java/com/example/lexicaandroid2/LexicaApp.kt`

**Action:** Ajouter une route pour l'écran de démonstration

```kotlin
// Dans le sealed class Screen
data object Gamification : Screen("gamification")

// Dans le NavHost
composable(Screen.Gamification.route) {
    val userStats by gamificationViewModel.userStats.collectAsState()
    GamificationDemoScreen(
        userStats = userStats,
        onAddXp = { amount -> gamificationViewModel.addXp(amount) }
    )
}
```

**Import à ajouter:**
```kotlin
import com.example.lexicaandroid2.features.gamification.ui.GamificationDemoScreen
```

---

### 5. Utilisation du XpProgressBar (optionnel)

Le composant `XpProgressBar` peut être ajouté à n'importe quel écran pour afficher la progression de l'utilisateur.

**Exemple d'utilisation dans un écran:**

```kotlin
@Composable
fun SomeScreen(userStatsRepository: UserStatsRepository) {
    val userStats by userStatsRepository.getUserStats().collectAsState(initial = null)
    
    userStats?.let { stats ->
        XpProgressBar(userStats = stats)
    }
}
```

---

## 📊 Calculs d'XP

### Règles de gamification

Le `XPCalculator` définit les règles suivantes:

| Action | XP gagné |
|--------|----------|
| Mot appris | 10 XP |
| Révision réussie | 5 XP |
| Jeu complété | 15 XP |
| Score parfait (bonus) | +10 XP |
| Série quotidienne | 20 XP/jour |

### Formule des niveaux

Pour atteindre le niveau N: **100 × (N-1)²** XP

Exemples:
- Niveau 1 → 2: 100 XP
- Niveau 2 → 3: 400 XP  
- Niveau 3 → 4: 900 XP
- Niveau 4 → 5: 1600 XP

---

## 🧪 Tests

### Tests unitaires

Les tests unitaires pour `XPCalculator` sont disponibles dans:
- `app/src/test/java/com/example/lexicaandroid2/features/gamification/domain/XPCalculatorTest.kt`

Pour lancer les tests:
```bash
./gradlew test --tests XPCalculatorTest
```

### Test manuel avec l'écran de démonstration

L'écran `GamificationDemoScreen` permet de tester facilement le système:
- Affiche les stats en temps réel
- Boutons pour ajouter différents montants d'XP
- Calculs de progression affichés

---

## 📁 Structure des fichiers

```
features/gamification/
├── data/
│   ├── UserStatsEntity.kt         ✅ Entité Room
│   ├── UserStatsDao.kt             ✅ DAO avec Flow
│   └── UserStatsRepositoryImpl.kt  ✅ Implémentation du repository
├── domain/
│   ├── UserStatsRepository.kt      ✅ Interface du repository
│   └── XPCalculator.kt             ✅ Calculateur d'XP et niveaux
└── ui/
    ├── XpProgressBar.kt            ✅ Composant de progression
    ├── GamificationViewModel.kt    ✅ ViewModel
    └── GamificationDemoScreen.kt   ✅ Écran de démonstration

tests/
└── features/gamification/domain/
    └── XPCalculatorTest.kt         ✅ Tests unitaires (28 tests)

docs/guides/
└── XPCALCULATOR_USAGE.md           ✅ Documentation d'utilisation
```

---

## ⚠️ Notes Importantes

1. **Version de la base de données**: Penser à incrémenter la version dans `@Database` annotation
2. **Migration**: Ajouter la migration pour éviter de perdre les données existantes
3. **Initialisation**: Le `UserStatsEntity` s'initialise automatiquement avec des valeurs par défaut
4. **Thread Safety**: Toutes les opérations Room sont suspend functions, à appeler dans un coroutine scope
5. **Tests**: 28 tests unitaires couvrent tous les cas d'usage de `XPCalculator`

---

## ✅ Checklist d'intégration

### Obligatoire
- [ ] Ajouter `UserStatsEntity` dans `@Database(entities = [...])`
- [ ] Ajouter `abstract fun userStatsDao()` dans `AppDatabase`
- [ ] Incrémenter la version de la database
- [ ] Ajouter la migration MIGRATION_1_2
- [ ] Créer `userStatsRepository` dans MainActivity

### Pour tester
- [ ] Créer `gamificationViewModel` dans MainActivity
- [ ] Ajouter la route `Screen.Gamification` dans `LexicaApp`
- [ ] Ajouter le composable pour `GamificationDemoScreen` dans le NavHost
- [ ] Tester l'ajout d'XP
- [ ] Tester le calcul de niveau
- [ ] Tester le système de streak

### Tests automatisés
- [ ] Lancer `./gradlew test --tests XPCalculatorTest`
- [ ] Vérifier que les 28 tests passent

---

## 🎯 Prochaines étapes possibles

- Créer un écran de profil utilisateur affichant les stats
- Ajouter des badges/achievements
- Créer un système de récompenses
- Intégrer l'XP dans les mini-jeux existants
- Ajouter des animations lors du level up
- Créer un leaderboard
- Ajouter des notifications de progression

---

## 📚 Documentation complémentaire

Pour plus d'informations sur l'utilisation de `XPCalculator` dans votre code:
→ Voir **[docs/guides/XPCALCULATOR_USAGE.md](docs/guides/XPCALCULATOR_USAGE.md)**
