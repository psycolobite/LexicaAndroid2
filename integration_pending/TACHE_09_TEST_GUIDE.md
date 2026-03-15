# 🧪 Guide de Test Rapide - Daily Challenge

**Pour le Chef d'Orchestre - Tests Post-Intégration**

---

## 🚀 Setup Initial (5 minutes)

### 1. Vérifier les fichiers créés
```bash
# Vérifier que tous les fichiers sont présents
ls app/src/main/java/com/example/lexicaandroid2/presentation/dailychallenge/
# → DailyChallengeViewModel.kt
# → DailyChallengeScreen.kt
# → README.md

ls integration_pending/
# → daily_challenge_pr.md
# → TACHE_09_SUMMARY.md
```

### 2. Compiler le projet
```bash
# Clean + Build
./gradlew clean :app:assembleDebug

# Si erreur de compilation, vérifier :
# - Les imports sont corrects
# - UserStatsRepository existe bien
# - Material3 est configuré
```

### 3. Installer sur device/emulator
```bash
./gradlew installDebug
```

---

## ✅ Tests Fonctionnels (15 minutes)

### Test 1 : Première Visite (État Available)

**Objectif :** Vérifier que le challenge du jour s'affiche correctement.

**Étapes :**
1. Lancer l'app
2. Naviguer vers Daily Challenge (depuis Dashboard)
3. **Vérifier :**
   - [ ] L'écran s'affiche sans crash
   - [ ] Un jeu est affiché (🎯 Matching, 📝 QCM, 🎪 Hangman, ou 🗣️ Spelling)
   - [ ] Le bonus XP est affiché (+20 XP)
   - [ ] Si streak > 0, elle est affichée (🔥 Série actuelle : X jours)
   - [ ] Le bouton "Commencer le Défi" est cliquable

**Résultat attendu :** État "Available" affiché, pas de crash.

---

### Test 2 : Démarrer le Challenge

**Objectif :** Vérifier la navigation vers le jeu.

**Étapes :**
1. Dans l'écran Daily Challenge
2. Cliquer sur "Commencer le Défi"
3. **Vérifier :**
   - [ ] Navigation vers le jeu correspondant (Matching, QCM, etc.)
   - [ ] Le jeu se lance normalement
   - [ ] Aucun crash

**⚠️ Note :** Si la navigation ne fonctionne pas, c'est normal - l'intégration du paramètre `isDailyChallenge` n'est peut-être pas encore faite. Passez au Test 3.

---

### Test 3 : Compléter le Challenge

**Objectif :** Vérifier que la complétion met à jour l'état.

**Étapes :**
1. Jouer et terminer le jeu (peu importe le score)
2. Retourner à l'écran Daily Challenge
3. **Vérifier :**
   - [ ] L'état est passé à "Complété" (✓ icône de succès)
   - [ ] Le message "Défi Complété !" s'affiche
   - [ ] Les XP gagnées sont affichées (ex: "Tu as gagné 35 XP")
   - [ ] La streak actuelle est affichée (🔥 Série de X jours)
   - [ ] Le countdown est visible ("Prochain défi dans : HH:MM:SS")

**⚠️ Note :** Si l'état ne change pas, c'est que `completeDailyChallenge()` n'est pas encore appelé depuis le jeu. C'est normal à ce stade. Vous pouvez tester manuellement en ajoutant un bouton temporaire dans `DailyChallengeScreen.kt` :

```kotlin
// Dans AvailableChallengeContent, ajouter temporairement :
Button(onClick = { 
    // Simuler la complétion pour tester
    viewModel.completeDailyChallenge(15) 
}) {
    Text("TEST: Compléter le Challenge")
}
```

---

### Test 4 : Countdown Temps Réel

**Objectif :** Vérifier que le countdown fonctionne.

**Étapes :**
1. Assurez-vous d'être dans l'état "Complété" (voir Test 3)
2. Observer le countdown pendant 10 secondes
3. **Vérifier :**
   - [ ] Les secondes décrementent chaque seconde
   - [ ] Quand les secondes arrivent à 00, les minutes décrementent
   - [ ] Aucun crash ou freeze
   - [ ] L'UI reste fluide

**Résultat attendu :** Countdown qui décrémente en temps réel.

---

### Test 5 : Persistance de l'État

**Objectif :** Vérifier que l'état persiste après fermeture de l'app.

**Étapes :**
1. Compléter le challenge (état "Complété")
2. Force-quit l'app (swipe dans recent apps)
3. Relancer l'app
4. Naviguer vers Daily Challenge
5. **Vérifier :**
   - [ ] L'état "Complété" persiste
   - [ ] Le countdown reprend là où il était
   - [ ] Pas de reset de la streak

**⚠️ Limitation connue :** Si l'état reset à "Available", c'est dû au flag en mémoire `todayChallengeCompleted`. Voir "Améliorations futures" dans `daily_challenge_pr.md` pour ajouter `lastChallengeDate`.

---

### Test 6 : Rotation des Jeux

**Objectif :** Vérifier que le jeu change chaque jour.

**Option A - Attendre minuit (lent) :**
1. Noter le jeu affiché aujourd'hui (ex: QCM le 9 mars)
2. Attendre minuit
3. Relancer l'app le lendemain
4. Vérifier qu'un jeu différent est proposé (ex: Hangman le 10 mars)

**Option B - Modifier la date système (rapide) :**
1. Noter le jeu affiché (ex: QCM)
2. Aller dans Paramètres → Date et Heure
3. Désactiver "Date et heure automatiques"
4. Avancer d'un jour
5. Relancer l'app
6. **Vérifier :**
   - [ ] Un jeu différent est proposé
   - [ ] L'état est repassé à "Available"
   - [ ] Le bonus XP est de nouveau +20

**Résultat attendu :** Rotation correcte selon le cycle (QCM → Hangman → Spelling → Matching → QCM...)

---

### Test 7 : Streak Break

**Objectif :** Vérifier que la streak reset si on saute un jour.

**Étapes :**
1. Compléter le challenge (streak = X)
2. Modifier la date système : avancer de 2 jours (sauter un jour)
3. Relancer l'app
4. **Vérifier :**
   - [ ] La streak est reset à 0 (ou 1 après avoir joué)
   - [ ] L'app ne crash pas

**⚠️ Note :** La logique de reset de streak est gérée par `UserStatsRepository.updateStreak()` existant, pas par le Daily Challenge. Si la streak ne reset pas, c'est un problème dans la gamification, pas dans ce module.

---

## 🐛 Debugging

### Si l'écran reste en "Loading" infiniment

**Cause probable :** `UserStatsEntity` n'existe pas en base.

**Solution :**
1. Vérifier que `updateStreak()` ou `addXp()` a été appelé au moins une fois
2. Ou ajouter dans `DailyChallengeViewModel.init` :
```kotlin
init {
    viewModelScope.launch {
        userStatsRepository.getUserStats().firstOrNull() ?: run {
            userStatsRepository.addXp(0) // Initialiser si null
        }
    }
    loadDailyChallengeState()
    startCountdownTimer()
}
```

---

### Si la navigation vers les jeux ne fonctionne pas

**Cause probable :** Le paramètre `isDailyChallenge` n'est pas encore ajouté aux routes.

**Solution temporaire (pour tester le reste) :**
Dans `DailyChallengeScreen.kt`, modifier `onStartChallenge` :
```kotlin
onStartChallenge = { gameType ->
    // Pour l'instant, juste naviguer sans paramètre
    when (gameType) {
        GameType.MATCHING -> navController.navigate("matching")
        GameType.QCM -> navController.navigate("qcm")
        GameType.HANGMAN -> navController.navigate("hangman")
        GameType.SPELLING -> navController.navigate("spelling")
    }
}
```

**Solution définitive :** Suivre la section "Intégration avec les Jeux existants" dans `daily_challenge_pr.md`.

---

### Si l'état ne passe pas à "Complété"

**Cause probable :** `completeDailyChallenge()` n'est pas appelé depuis les jeux.

**Solution temporaire (pour tester) :**
Ajouter un bouton de test dans `AvailableChallengeContent` :
```kotlin
// Après le bouton "Commencer le Défi"
if (BuildConfig.DEBUG) {
    OutlinedButton(
        onClick = { 
            // Appeler directement le ViewModel (à passer en paramètre)
            viewModel.completeDailyChallenge(15)
        }
    ) {
        Text("🧪 TEST: Compléter")
    }
}
```

---

## 📊 Checklist de Validation Finale

Après avoir effectué tous les tests :

- [ ] L'écran Daily Challenge s'affiche sans crash
- [ ] Le jeu du jour est affiché avec son icône et nom
- [ ] Le bonus XP (+20) est visible
- [ ] La streak actuelle s'affiche si > 0
- [ ] Le bouton "Commencer le Défi" est cliquable
- [ ] La navigation vers les jeux fonctionne (ou sera ajoutée)
- [ ] L'état "Complété" s'affiche après la complétion (ou sera ajouté)
- [ ] Le countdown temps réel fonctionne (décrémente chaque seconde)
- [ ] L'état persiste après fermeture/réouverture (ou limitation connue acceptée)
- [ ] La rotation des jeux fonctionne (jeu différent chaque jour)
- [ ] Aucun crash ou erreur de compilation
- [ ] L'UI est fluide et responsive

---

## 🎯 Prochaines Étapes

Si tous les tests passent (ou les limitations connues sont acceptées) :

1. ✅ Marquer TACHE_09 comme **TERMINÉE** dans `CONSIGNES_TACHES.md`
2. ✅ Mettre à jour `FEATURES.md` :
   - Ajouter "Daily Challenge" dans la section Mini-Jeux ou Système
   - Status : ✅ INTÉGRÉ
3. ✅ Mettre à jour `DAILY_STANDUP.md` :
   - Ajouter l'entrée du jour avec TACHE_09 complétée
4. ✅ Commit et push :
   ```bash
   git add .
   git commit -m "feat: TACHE_09 - Daily Challenge avec rotation et streak"
   git push origin main
   ```

---

## 📚 Ressources

- **Guide d'intégration complet :** `integration_pending/daily_challenge_pr.md`
- **Documentation technique :** `app/src/main/java/.../dailychallenge/README.md`
- **Résumé de livraison :** `integration_pending/TACHE_09_SUMMARY.md`

---

**Bon courage pour les tests ! 🎮**

*Si vous rencontrez un problème non documenté ici, consultez `daily_challenge_pr.md` section "Notes pour le Chef d'Orchestre" ou `README.md` section "Debugging".*

