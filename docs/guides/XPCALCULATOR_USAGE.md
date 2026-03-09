# 🎮 XPCalculator - Guide d'Utilisation

## 📖 Introduction

`XPCalculator` est une classe utilitaire (object Kotlin) qui centralise tous les calculs liés au système de gamification de LexicaAndroid2. Elle offre des méthodes pour calculer l'XP, les niveaux, et les progressions.

---

## 🎯 Utilisation dans les Mini-Jeux

### Exemple 1: Attribution d'XP après un jeu

```kotlin
import com.example.lexicaandroid2.features.gamification.domain.XPCalculator
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository

class GameViewModel(
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {
    
    fun onGameCompleted(perfectScore: Boolean) {
        viewModelScope.launch {
            // Calculer l'XP gagné
            val xpGained = XPCalculator.calculateXpForGame(perfectScore)
            
            // Mettre à jour les stats de l'utilisateur
            userStatsRepository.addXp(xpGained)
            
            // Afficher un message
            _message.value = "Bravo ! +$xpGained XP"
        }
    }
}
```

---

## 📚 Utilisation pour l'Apprentissage

### Exemple 2: Attribution d'XP pour des mots appris

```kotlin
class FlashcardViewModel(
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {
    
    fun onWordsLearned(count: Int) {
        viewModelScope.launch {
            // Calculer l'XP pour les mots appris
            val xpGained = XPCalculator.calculateXpForLearning(count)
            
            // Ajouter l'XP
            userStatsRepository.addXp(xpGained)
        }
    }
    
    fun onWordsReviewed(correctCount: Int) {
        viewModelScope.launch {
            // Calculer l'XP pour les révisions
            val xpGained = XPCalculator.calculateXpForReview(correctCount)
            
            // Ajouter l'XP
            userStatsRepository.addXp(xpGained)
        }
    }
}
```

---

## 🔥 Utilisation pour les Séries Quotidiennes

### Exemple 3: Afficher un bonus de série

```kotlin
class DashboardViewModel(
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {
    
    val userStats = userStatsRepository.getUserStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    fun onDailyLogin() {
        viewModelScope.launch {
            // Mettre à jour la série
            userStatsRepository.updateStreak()
            
            // Récupérer la série actuelle
            userStats.value?.let { stats ->
                if (stats.streak > 1) {
                    val bonus = XPCalculator.calculateStreakBonus(stats.streak)
                    userStatsRepository.addXp(bonus)
                    
                    _message.value = "Série de ${stats.streak} jours ! +$bonus XP"
                }
            }
        }
    }
}
```

---

## 📊 Affichage des Informations de Niveau

### Exemple 4: Calculer les informations de progression

```kotlin
@Composable
fun LevelInfoCard(userStats: UserStatsEntity) {
    val currentLevel = userStats.level
    val currentXp = userStats.xp
    
    // Calculer les informations
    val progress = XPCalculator.calculateProgressToNextLevel(currentLevel, currentXp)
    val xpToNext = XPCalculator.calculateXpToNextLevel(currentLevel, currentXp)
    val nextLevelXp = XPCalculator.calculateXpForLevel(currentLevel + 1)
    
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Niveau $currentLevel")
            LinearProgressIndicator(progress = progress)
            Text("$xpToNext XP restants pour atteindre le niveau ${currentLevel + 1}")
            Text("Total: ${currentXp} / ${nextLevelXp} XP")
        }
    }
}
```

---

## 🎨 Personnalisation des Valeurs d'XP

Si vous souhaitez ajuster les valeurs d'XP, modifiez les constantes dans `XPCalculator.kt`:

```kotlin
object XPCalculator {
    const val XP_WORD_LEARNED = 10        // Par défaut: 10 XP
    const val XP_WORD_REVIEWED = 5         // Par défaut: 5 XP
    const val XP_DAILY_STREAK_BONUS = 20   // Par défaut: 20 XP
    const val XP_GAME_COMPLETED = 15       // Par défaut: 15 XP
    const val XP_PERFECT_SCORE_BONUS = 10  // Par défaut: 10 XP
}
```

---

## 📐 Formule des Niveaux

La formule de progression est: **XP requis = 100 × (Niveau - 1)²**

### Tableau de référence

| Niveau | XP Requis | XP Total pour y arriver |
|--------|-----------|-------------------------|
| 1 | 0 | 0 |
| 2 | 100 | 100 |
| 3 | 400 | 400 |
| 4 | 900 | 900 |
| 5 | 1600 | 1600 |
| 10 | 8100 | 8100 |
| 20 | 36100 | 36100 |

### Pourquoi cette formule ?

- **Progression non-linéaire**: Plus on avance, plus c'est difficile
- **Équilibre**: Évite que les joueurs atteignent des niveaux trop élevés trop rapidement
- **Motivation**: Les premiers niveaux sont accessibles rapidement pour engager l'utilisateur

---

## 🧪 Tests

Des tests unitaires sont disponibles dans `XPCalculatorTest.kt` pour valider tous les calculs.

### Lancer les tests

```bash
./gradlew test --tests XPCalculatorTest
```

---

## 💡 Bonnes Pratiques

1. **Toujours utiliser XPCalculator**: Ne jamais calculer l'XP manuellement ailleurs
2. **Centralisation**: Toute modification des règles se fait dans `XPCalculator`
3. **Tests**: Ajouter des tests quand vous modifiez les formules
4. **Documentation**: Mettre à jour ce guide si vous ajoutez de nouvelles méthodes

---

## 🔗 Références

- `XPCalculator.kt` - Classe principale
- `UserStatsRepository.kt` - Interface du repository
- `UserStatsRepositoryImpl.kt` - Implémentation
- `XpProgressBar.kt` - Composant UI
- `XPCalculatorTest.kt` - Tests unitaires
