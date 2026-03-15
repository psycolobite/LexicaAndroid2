# ✅ TACHE_09 - Checklist d'Intégration Express

**Chef d'Orchestre - Guide Ultra-Rapide**

---

## 🎯 Objectif

Intégrer le module Daily Challenge dans le projet principal en **7 étapes**.

**Temps estimé :** 30-45 minutes

---

## 📋 Checklist (à cocher au fur et à mesure)

### 🔹 Étape 1 : Valider les Fichiers Créés
- [ ] Vérifier que `presentation/dailychallenge/DailyChallengeViewModel.kt` existe
- [ ] Vérifier que `presentation/dailychallenge/DailyChallengeScreen.kt` existe
- [ ] Vérifier que `integration_pending/daily_challenge_pr.md` existe

**Si fichiers manquants :** Arrêter et signaler à l'agent.

---

### 🔹 Étape 2 : Ajouter la Route dans LexicaApp.kt

**Fichier :** `app/src/main/java/com/example/lexicaandroid2/presentation/LexicaApp.kt`

**Action 1 - Ajouter l'import :**
```kotlin
import com.example.lexicaandroid2.presentation.dailychallenge.DailyChallengeScreen
import com.example.lexicaandroid2.presentation.dailychallenge.DailyChallengeViewModel
```

**Action 2 - Ajouter dans `sealed class Screen` :**
```kotlin
sealed class Screen(val route: String) {
    // ...existing screens...
    data object DailyChallenge : Screen("daily_challenge")
}
```

- [ ] Imports ajoutés
- [ ] Route ajoutée dans `sealed class Screen`

---

### 🔹 Étape 3 : Créer le ViewModel

**Fichier :** `app/src/main/java/com/example/lexicaandroid2/presentation/LexicaApp.kt`

**Dans la fonction `LexicaApp` (ou MainActivity), ajouter :**
```kotlin
@Composable
fun LexicaApp(
    // ...existing params...
    gamificationViewModel: GamificationViewModel,  // ← déjà existant
    modifier: Modifier = Modifier
) {
    // Créer le DailyChallengeViewModel
    val userStatsRepository = remember {
        // Utiliser le même repository que gamificationViewModel
        // OU créer ici si nécessaire
    }
    val dailyChallengeViewModel = remember {
        DailyChallengeViewModel(userStatsRepository)
    }
    
    // ...rest of code...
}
```

**⚠️ Alternative si repository pas accessible :**
Créer dans MainActivity et passer en paramètre à LexicaApp.

- [ ] `dailyChallengeViewModel` créé

---

### 🔹 Étape 4 : Ajouter le Composable dans le NavHost

**Fichier :** `app/src/main/java/com/example/lexicaandroid2/presentation/LexicaApp.kt`

**Dans le `NavHost`, ajouter :**
```kotlin
composable(
    route = Screen.DailyChallenge.route,
    enterTransition = {
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
    },
    exitTransition = {
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
    }
) {
    val uiState by dailyChallengeViewModel.uiState.collectAsState()
    
    DailyChallengeScreen(
        uiState = uiState,
        onStartChallenge = { gameType ->
            // Navigation vers le jeu correspondant
            when (gameType) {
                com.example.lexicaandroid2.presentation.dailychallenge.GameType.MATCHING -> 
                    navController.navigate(Screen.MatchingGame.route)
                com.example.lexicaandroid2.presentation.dailychallenge.GameType.QCM -> 
                    navController.navigate(Screen.QcmGame.route)
                com.example.lexicaandroid2.presentation.dailychallenge.GameType.HANGMAN -> 
                    navController.navigate(Screen.HangmanGame.route)
                com.example.lexicaandroid2.presentation.dailychallenge.GameType.SPELLING -> 
                    navController.navigate(Screen.SpellingGame.route)
            }
        }
    )
}
```

- [ ] Composable ajouté dans NavHost
- [ ] Navigation vers jeux configurée

---

### 🔹 Étape 5 : Ajouter Bouton dans Dashboard

**Fichier :** `app/src/main/java/com/example/lexicaandroid2/presentation/dashboard/DashboardScreen.kt`

**Action 1 - Modifier la signature :**
```kotlin
@Composable
fun DashboardScreen(
    // ...existing params...
    onNavigateToDailyChallenge: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Action 2 - Ajouter une Card cliquable (quelque part dans le layout) :**
```kotlin
// Après les autres cards du Dashboard
Card(
    modifier = Modifier
        .fillMaxWidth()
        .clickable { onNavigateToDailyChallenge() },
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
    )
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "⭐", fontSize = 32.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Défi Quotidien",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Complète ton défi du jour !",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
    }
}
```

**Action 3 - Dans LexicaApp.kt, passer le callback :**
```kotlin
composable(Screen.Dashboard.route) {
    DashboardScreen(
        // ...existing params...
        onNavigateToDailyChallenge = {
            navController.navigate(Screen.DailyChallenge.route)
        }
    )
}
```

- [ ] Signature `DashboardScreen` modifiée
- [ ] Card ajoutée
- [ ] Callback passé dans LexicaApp

---

### 🔹 Étape 6 : Compiler

**Commande :**
```bash
./gradlew clean :app:assembleDebug
```

**Vérifications :**
- [ ] Pas d'erreurs de compilation
- [ ] Warning "never used" OK (disparaîtront après intégration complète)

**Si erreurs :**
1. Vérifier les imports
2. Vérifier que `UserStatsRepository` existe
3. Consulter `integration_pending/daily_challenge_pr.md`

---

### 🔹 Étape 7 : Tester

**Installer sur device/emulator :**
```bash
./gradlew installDebug
```

**Tests rapides (5 min) :**
- [ ] Lancer l'app
- [ ] Cliquer sur "Défi Quotidien" dans Dashboard
- [ ] Vérifier que l'écran s'affiche (état Available)
- [ ] Vérifier que le jeu du jour est affiché (🎯📝🎪🗣️)
- [ ] Vérifier que le bonus XP s'affiche (+20)

**Si crash ou écran blanc :**
1. Vérifier les logs : `adb logcat | grep -i error`
2. Consulter `TACHE_09_TEST_GUIDE.md` section Debugging

---

## 🚀 Pour Aller Plus Loin (Optionnel)

### Étape 8 : Intégrer avec les Jeux (Optionnel mais Recommandé)

**Objectif :** Faire en sorte que les jeux appellent `completeDailyChallenge()` automatiquement.

**Voir :** `daily_challenge_pr.md` section "Intégration avec les Jeux existants"

**Résumé :**
- Ajouter un paramètre `isDailyChallenge: Boolean` aux routes des jeux
- Quand le jeu est fini ET `isDailyChallenge == true` → appeler `completeDailyChallenge()`

**Temps estimé :** +15 minutes

- [ ] Paramètre `isDailyChallenge` ajouté aux routes
- [ ] Appel à `completeDailyChallenge()` implémenté dans les jeux

---

## 📊 Post-Intégration

### Mettre à Jour les Documents

- [ ] **DAILY_STANDUP.md** : Ajouter entrée du jour (✅ déjà fait par l'agent)
- [ ] **FEATURES.md** : Ajouter "Daily Challenge" dans la section appropriée
- [ ] **CONSIGNES_TACHES.md** : Marquer TACHE_09 comme ✅ TERMINEE

### Commit et Push

```bash
git add .
git commit -m "feat: TACHE_09 - Daily Challenge avec rotation et streak"
git push origin main
```

- [ ] Documents mis à jour
- [ ] Commit effectué
- [ ] Push effectué

---

## ✅ Validation Finale

**Si tous les tests passent :**
- [ ] L'écran Daily Challenge s'affiche
- [ ] Pas de crash
- [ ] Navigation fonctionne
- [ ] Build successful

**→ TACHE_09 est INTÉGRÉE ! 🎉**

---

## 📚 Ressources

**Documentation complète :**
- 📘 `integration_pending/daily_challenge_pr.md` → Guide détaillé
- 🧪 `integration_pending/TACHE_09_TEST_GUIDE.md` → Tests
- 📖 `presentation/dailychallenge/README.md` → Doc technique

**Besoin d'aide ?**
→ Consulter `daily_challenge_pr.md` section "Notes pour le Chef d'Orchestre"

---

**Temps total estimé (Étapes 1-7) : 30-45 minutes**

**Bon courage ! 🚀**

