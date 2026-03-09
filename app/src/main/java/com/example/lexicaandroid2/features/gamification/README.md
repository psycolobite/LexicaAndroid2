# 🎮 Module Gamification

Module de gamification pour LexicaAndroid2 - Système d'XP, de niveaux et de séries quotidiennes.

---

## 🎯 Vue d'Ensemble

Ce module implémente un système complet de gamification pour motiver les utilisateurs à apprendre du vocabulaire. Il comprend :

- **Système d'XP** : Gagnez de l'expérience pour chaque action
- **Niveaux** : Progression basée sur une formule mathématique
- **Séries (Streaks)** : Bonus pour les connexions quotidiennes
- **Composants UI** : Barres de progression et écrans de stats

---

## 📂 Structure

```
features/gamification/
├── data/                          # Couche Data
│   ├── UserStatsEntity.kt        # Entité Room
│   ├── UserStatsDao.kt           # DAO avec Flow
│   └── UserStatsRepositoryImpl.kt # Implémentation
│
├── domain/                        # Couche Domain
│   ├── UserStatsRepository.kt    # Interface
│   └── XPCalculator.kt           # Logique métier
│
└── ui/                           # Couche UI
    ├── XpProgressBar.kt          # Composant barre de progression
    ├── GamificationViewModel.kt  # ViewModel
    └── GamificationDemoScreen.kt # Écran de démonstration
```

---

## 🚀 Quick Start

### 1. Ajouter de l'XP

```kotlin
// Injecter le repository
class MyViewModel(
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {
    
    fun onActionCompleted() {
        viewModelScope.launch {
            userStatsRepository.addXp(10) // +10 XP
        }
    }
}
```

### 2. Afficher la Progression

```kotlin
@Composable
fun MyScreen(userStatsRepository: UserStatsRepository) {
    val userStats by userStatsRepository
        .getUserStats()
        .collectAsState(initial = null)
    
    userStats?.let {
        XpProgressBar(userStats = it)
    }
}
```

### 3. Utiliser XPCalculator

```kotlin
// Calculer l'XP pour une action
val xp = XPCalculator.calculateXpForLearning(5) // 5 mots = 50 XP

// Calculer le niveau
val level = XPCalculator.calculateLevel(totalXp = 450L) // Niveau 3

// Calculer la progression
val progress = XPCalculator.calculateProgressToNextLevel(
    currentLevel = 2, 
    currentXp = 250L
) // 0.5 (50%)
```

---

## 📊 Système d'XP

### Valeurs par Action

| Action | XP Gagné |
|--------|----------|
| 📖 Mot appris | **10 XP** |
| ✅ Révision réussie | **5 XP** |
| 🎮 Jeu complété | **15 XP** |
| ⭐ Score parfait | **+10 XP** (bonus) |
| 🔥 Série quotidienne | **20 XP/jour** |

### Formule des Niveaux

```
XP requis pour niveau N = 100 × (N-1)²
```

#### Exemples de Progression

| Niveau | XP Total Requis | XP Requis pour ce Niveau |
|--------|-----------------|--------------------------|
| 1      | 0               | 0                        |
| 2      | 100             | 100                      |
| 3      | 400             | 300                      |
| 4      | 900             | 500                      |
| 5      | 1,600           | 700                      |
| 10     | 8,100           | 1,700                    |
| 20     | 36,100          | 3,700                    |

---

## 🛠️ API

### XPCalculator

#### Calculs de Niveau

```kotlin
// Calculer le niveau basé sur l'XP total
XPCalculator.calculateLevel(totalXp: Long): Int

// XP requis pour atteindre un niveau donné
XPCalculator.calculateXpForLevel(level: Int): Long

// XP manquants pour le niveau suivant
XPCalculator.calculateXpToNextLevel(
    currentLevel: Int, 
    currentXp: Long
): Long

// Progression en pourcentage (0.0 à 1.0)
XPCalculator.calculateProgressToNextLevel(
    currentLevel: Int, 
    currentXp: Long
): Float
```

#### Calculs d'XP par Action

```kotlin
// XP pour apprentissage
XPCalculator.calculateXpForLearning(wordsLearned: Int): Int

// XP pour révisions
XPCalculator.calculateXpForReview(wordsReviewed: Int): Int

// XP pour jeu (avec/sans score parfait)
XPCalculator.calculateXpForGame(perfectScore: Boolean = false): Int

// Bonus de série
XPCalculator.calculateStreakBonus(streakDays: Int): Int
```

#### Constantes

```kotlin
XPCalculator.XP_WORD_LEARNED          // 10
XPCalculator.XP_WORD_REVIEWED         // 5
XPCalculator.XP_DAILY_STREAK_BONUS    // 20
XPCalculator.XP_GAME_COMPLETED        // 15
XPCalculator.XP_PERFECT_SCORE_BONUS   // 10
```

### UserStatsRepository

```kotlin
interface UserStatsRepository {
    // Observer les stats (Flow réactif)
    fun getUserStats(): Flow<UserStatsEntity?>
    
    // Ajouter de l'XP
    suspend fun addXp(amount: Int)
    
    // Mettre à jour la série quotidienne
    suspend fun updateStreak()
}
```

### GamificationViewModel

```kotlin
class GamificationViewModel(repository: UserStatsRepository) {
    // État des stats (StateFlow)
    val userStats: StateFlow<UserStatsEntity?>
    
    // Ajouter de l'XP
    fun addXp(amount: Int)
    
    // Mettre à jour la série
    fun updateStreak()
    
    // Initialiser les stats
    fun initializeStatsIfNeeded()
}
```

---

## 🎨 Composants UI

### XpProgressBar

Barre de progression Material 3 avec informations de niveau.

```kotlin
@Composable
fun XpProgressBar(
    userStats: UserStatsEntity,
    modifier: Modifier = Modifier
)
```

**Affiche:**
- Niveau actuel
- XP restants pour le niveau suivant
- Barre de progression visuelle
- XP total / XP requis

### GamificationDemoScreen

Écran complet de démonstration et de test.

```kotlin
@Composable
fun GamificationDemoScreen(
    userStats: UserStatsEntity?,
    onAddXp: (Int) -> Unit,
    modifier: Modifier = Modifier
)
```

**Fonctionnalités:**
- Affichage des stats en temps réel
- Boutons pour tester chaque type d'action
- Cartes d'informations détaillées
- Calculs de progression

---

## 🧪 Tests

### Tests Unitaires

**Fichier:** `XPCalculatorTest.kt`  
**Couverture:** 28 tests unitaires

```bash
# Lancer les tests
./gradlew test --tests XPCalculatorTest
```

**Tests couverts:**
- ✅ Calculs de niveau (tous les cas)
- ✅ Calculs d'XP requis
- ✅ Calculs de progression
- ✅ Calculs d'XP par action
- ✅ Cas limites et erreurs
- ✅ Validation des constantes

---

## 📖 Documentation

### Guides Disponibles

1. **[integration_pending/gamification.md](../../integration_pending/gamification.md)**
   - Guide d'intégration dans l'app
   - Modifications de database
   - Instructions de migration

2. **[docs/guides/XPCALCULATOR_USAGE.md](../guides/XPCALCULATOR_USAGE.md)**
   - Utilisation détaillée de XPCalculator
   - Exemples de code
   - Bonnes pratiques

3. **[docs/GAMIFICATION_SUMMARY.md](../GAMIFICATION_SUMMARY.md)**
   - Résumé complet du module
   - Fichiers créés
   - Checklist d'implémentation

---

## 💡 Exemples d'Utilisation

### Exemple 1: Mini-Jeu

```kotlin
class MatchingGameViewModel(
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {
    
    fun onGameFinished(score: Int, maxScore: Int) {
        viewModelScope.launch {
            val perfectScore = (score == maxScore)
            val xpGained = XPCalculator.calculateXpForGame(perfectScore)
            
            userStatsRepository.addXp(xpGained)
            
            _message.value = if (perfectScore) {
                "Parfait ! +$xpGained XP 🌟"
            } else {
                "Bien joué ! +$xpGained XP"
            }
        }
    }
}
```

### Exemple 2: Flashcards

```kotlin
class FlashcardViewModel(
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {
    
    fun onCardsReviewed(correctAnswers: Int) {
        viewModelScope.launch {
            val xpGained = XPCalculator.calculateXpForReview(correctAnswers)
            userStatsRepository.addXp(xpGained)
        }
    }
    
    fun onNewWordsLearned(count: Int) {
        viewModelScope.launch {
            val xpGained = XPCalculator.calculateXpForLearning(count)
            userStatsRepository.addXp(xpGained)
        }
    }
}
```

### Exemple 3: Dashboard avec Streak

```kotlin
class DashboardViewModel(
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {
    
    val userStats = userStatsRepository.getUserStats()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    init {
        checkDailyStreak()
    }
    
    private fun checkDailyStreak() {
        viewModelScope.launch {
            userStatsRepository.updateStreak()
            
            userStats.value?.let { stats ->
                if (stats.streak > 1) {
                    val bonus = XPCalculator.XP_DAILY_STREAK_BONUS
                    userStatsRepository.addXp(bonus)
                }
            }
        }
    }
}
```

---

## 🔄 Intégration

### Étape 1: Ajouter à la Database

Modifier `AppDatabase.kt`:

```kotlin
@Database(
    entities = [
        FlashcardEntity::class,
        UserStatsEntity::class  // Ajouter
    ],
    version = 2  // Incrémenter
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao
    abstract fun userStatsDao(): UserStatsDao  // Ajouter
}
```

### Étape 2: Migration

Ajouter la migration:

```kotlin
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS user_stats (
                userId TEXT PRIMARY KEY NOT NULL,
                xp INTEGER NOT NULL,
                level INTEGER NOT NULL,
                streak INTEGER NOT NULL,
                lastLoginDate INTEGER NOT NULL
            )
        """)
    }
}
```

### Étape 3: Instancier

Dans `MainActivity`:

```kotlin
val userStatsDao = database.userStatsDao()
val userStatsRepository = UserStatsRepositoryImpl(userStatsDao)
val gamificationViewModel = GamificationViewModel(userStatsRepository)
```

---

## ⚠️ Notes Importantes

1. **Thread Safety**: Toutes les opérations sont asynchrones (suspend functions)
2. **Flow**: Le repository utilise Flow pour la réactivité
3. **Default Values**: L'entité s'initialise avec des valeurs par défaut
4. **Singleton**: XPCalculator est un object Kotlin (singleton)

---

## 🎯 Prochaines Améliorations

- [ ] Système de badges/achievements
- [ ] Animations de level-up
- [ ] Leaderboard
- [ ] Notifications de progression
- [ ] Récompenses par niveau
- [ ] Statistiques détaillées

---

## 📞 Support

Pour toute question sur l'utilisation de ce module :
- Consulter la documentation dans `docs/guides/`
- Voir les exemples dans `GamificationDemoScreen.kt`
- Lancer les tests pour comprendre le comportement

---

**✨ Module développé par Agent 2 - Tâche Gamification**
