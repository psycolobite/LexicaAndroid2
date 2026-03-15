# TACHE_12 PR — Liste de mots : supprimer, favoris, et détail complet

**Assigné à :** Agent Développeur (TACHE_12)  
**Date :** 2026-03-09  
**Status :** ✅ CODE LIVRÉ — Prêt pour intégration

---

## 📦 Livrables

### Fichiers créés
- ✅ `presentation/wordlist/WordDetailScreen.kt` — Nouvel écran détail complet

### Fichiers modifiés
- ✅ `presentation/wordlist/WordListScreen.kt` — Ajout icônes ⭐ et 🗑️
- ✅ `presentation/wordlist/WordListViewModel.kt` — Ajout méthodes `toggleFavorite()` et `deleteCard()`

---

## 🔧 Modifications requises dans les fichiers coeur

### 1. `presentation/LexicaApp.kt`

**Ajouter l'import :**
```kotlin
import com.example.lexicaandroid2.presentation.wordlist.WordDetailScreen
import com.example.lexicaandroid2.presentation.wordlist.WordDetailViewModel
import com.example.lexicaandroid2.presentation.wordlist.WordDetailViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
```

**Dans la classe `sealed class Screen`, ajouter :**
```kotlin
data class WordDetail(val cardId: String) : Screen("word/{cardId}") {
    fun createRoute(cardId: String) = "word/$cardId"
}
```

**Dans la fonction `LexicaApp()`, ajouter dans le `topBarTitle` :**
```kotlin
// Ajouter dans le `when (currentRoute)`:
Screen.WordDetail(cardId = "").route.substringBefore("{") -> "Détail du mot"
```

**Dans la `canNavigateBack` Boolean, ajouter :**
```kotlin
currentRoute?.startsWith("word/") == true
```

**Dans le `NavHost { }`, avant la dernière `composable()` de DailyChallenge, ajouter :**
```kotlin
composable(
    route = "word/{cardId}",
    arguments = listOf(
        navArgument("cardId") { type = NavType.StringType }
    ),
    enterTransition = {
        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
    },
    exitTransition = {
        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
    }
) { backStackEntry ->
    val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
    val factory = WordDetailViewModelFactory(cardId, repository)
    val viewModel: WordDetailViewModel = viewModel(factory = factory)
    
    WordDetailScreen(
        cardId = cardId,
        viewModel = viewModel,
        onBack = { navController.navigateUp() }
    )
}
```

---

## ✅ Vérifications

### Tests manuels à effectuer (après intégration)
- [ ] Cliquer sur un mot dans la liste → écran détail s'ouvre
- [ ] Bouton ⭐ dans le détail → toggle favori, icône change
- [ ] Bouton ⭐ dans la liste → toggle favori, icône change
- [ ] Cliquer 🗑️ dans la liste → AlertDialog apparaît
- [ ] Cliquer 🗑️ dans le détail → AlertDialog apparaît
- [ ] Confirmer suppression → mot disparaît et écran revient à la liste
- [ ] Bouton "← Retour" dans détail → retour à la liste

### Compilation
```bash
./gradlew clean :app:assembleDebug
```
Doit compiler sans erreurs.

---

## 🎨 UI/UX Checklist (TACHE_12)

✅ La zone de JEU (grille, cartes) utilise `Modifier.weight(1f)` → N/A (écran détail)  
✅ LazyColumn avec ScrollState propre → OK (WordDetailScreen)  
✅ Sélecteurs visibles et compacts → OK (favoris/suppression en icons)  
✅ Feedback immédiat (AlertDialog) → OK  
✅ Texte minimal 14.sp → OK  
✅ Contraste suffisant → OK  
✅ Pas de exceptions non gérées → OK  

---

## 📝 Notes d'intégration

- L'écran détail utilise `LazyColumn` pour supporter du contenu riche (définitions longues, nombreux synonymes)
- Les données sont chargées dans le ViewModel au init (pas de polling)
- Après suppression, l'écran navigue automatiquement vers la liste
- Le toggle favori recharge les données pour afficher l'icône mise à jour

---

## 🚀 Prêt pour intégration

Tous les fichiers sont dans `presentation/wordlist/` — prêts à être copiés.

**Chef d'Orchestre :** Applique les modifications dans `LexicaApp.kt` et relance la compilation.
