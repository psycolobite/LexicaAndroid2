# 🎮 Mini-Jeux à Implémenter - Backlog Détaillé

## 📊 État actuel

### ✅ Jeux Implémentés (Kotlin Android 2026)
1. **Jeu de Correspondance** (Matching Game)
   - Status: ✅ Complété
   - Package: `presentation.games.matching`
   - ViewModel: `MatchingViewModel.kt`
   - Screen: `MatchingScreen.kt`
   - Description: Associer des mots à leurs définitions

2. **QCM (Questions à Choix Multiples)**
   - Status: ✅ Complété
   - Package: `presentation.games.qcm`
   - ViewModel: `QcmViewModel.kt`
   - Screen: `QcmScreen.kt`
   - Description: Sélectionner la bonne réponse parmi 4 options

3. **Jeu du Pendu** (Hangman)
   - Status: ✅ Complété
   - Package: `presentation.games.hangman`
   - ViewModel: `HangmanViewModel.kt`
   - Screen: `HangmanScreen.kt`
   - Description: Deviner les lettres d'un mot secret

4. **Jeu de Dictée** (Spelling Game avec TTS)
   - Status: ✅ Complété
   - Package: `presentation.games.spelling`
   - ViewModel: `SpellingGameViewModel.kt`
   - Screen: `SpellingGameScreen.kt`
   - Features: TextToSpeech natif Android, Écoute du mot, Saisie manuelle
   - Documentation: `docs/SPELLING_GAME_INTEGRATION.md`

---

## 📋 Jeux Planifiés (Version 2 - À implémenter)

### 1. **Anagrammes** 🔤
**Priorité:** Haute | **Complexité:** Moyenne

**Description:**
Un mot est donné avec ses lettres mélangées. L'utilisateur doit retrouver le mot original.

**Mécanique:**
- Afficher les lettres du mot mélangées
- Champ de saisie pour l'utilisateur
- Validation: Vérifier si l'arrangement est correct
- Score: Basé sur le temps et le nombre de tentatives

**Fichiers à créer:**
```
presentation/games/anagrams/
├── AnagramsViewModel.kt
├── AnagramsScreen.kt
└── AnagramsUiState.kt
```

**Données requises:**
- Liste de mots avec leurs définitions
- Fonction de mélange de lettres

---

### 2. **Chrono (Speed Mode)** ⏱️
**Priorité:** Haute | **Complexité:** Moyenne

**Description:**
Mode chronométré: Répondre à un maximum de questions en un temps limité (30s, 1min, 5min).

**Mécanique:**
- Sélectionner durée: 30s / 1min / 5min
- Afficher question (définition → mot ou vice-versa)
- Chronomètre dégressif visible
- Boutons réponse rapides (A/B/C/D ou Text)
- Compteur: Réponses correctes / Total
- Écran de résultats: Score final, statistiques, XP gagné

**Fichiers à créer:**
```
presentation/games/chrono/
├── ChronoViewModel.kt
├── ChronoScreen.kt
├── ChronoUiState.kt
└── TimerCountdown.kt (composable réutilisable)
```

**Features:**
- Timer Animation (couleur change proche du timeout)
- Auto-avanç après 2s si réponse correcte
- Pause/Reprendre
- Vibration feedback (optionnel)

---

### 3. **Memory (Jeu de Mémorisation)** 🧠
**Priorité:** Moyenne | **Complexité:** Moyenne

**Description:**
Grille de cartes retournées: Trouver les paires de mots ↔ définitions.

**Mécanique:**
- Grille NxM de cartes (ex: 4x4 = 8 paires)
- Au clic: Affiche mot ou définition
- Trouver 2 cartes correspondantes
- Cartes correctes restent découvertes
- Compteur: Paires trouvées / Total
- Mode temps optionnel

**Fichiers à créer:**
```
presentation/games/memory/
├── MemoryViewModel.kt
├── MemoryScreen.kt
├── MemoryCard.kt (composable)
├── MemoryUiState.kt
└── MemoryUtils.kt
```

**Features:**
- Animations de flip
- Support multi-niveaux (4x4, 5x4, 6x4)
- Compte erreurs/tentatives
- Best score sauvegardé

---

### 4. **Orthographe Avancée (Phonétique)** 🎤
**Priorité:** Haute | **Complexité:** Haute | **Status:** Partiellement complété

**Description:**
Évolution du Spelling Game - Entendre le mot ET la définition, puis l'écrire.

**Mécanique:**
- Afficher définition en texte
- Bouton "🔊 Écouter le mot" (TTS)
- Champ de saisie
- Vérification phonétique (corriger accents/pluriels mineurs)
- Feedback détaillé en cas d'erreur

**Fichiers à créer/modifier:**
```
presentation/games/spelling/
├── SpellingGameScreen.kt (DÉJÀ EXISTANT - À AMÉLIORER)
├── SpellingGameViewModel.kt (DÉJÀ EXISTANT - À AMÉLIORER)
├── SpellingAdvancedScreen.kt (NOUVEAU)
└── PhoneticValidator.kt (NOUVEAU - Utilitaire)
```

**Features:**
- Joker: Voir une lettre, indice sur le genre/pluriel
- Afficher mot après 3 erreurs
- Points bonus si pas d'erreur
- Distinction des petites fautes (accents, majuscules)

---

### 5. **Associations Sémantiques** 🔗
**Priorité:** Moyenne | **Complexité:** Haute

**Description:**
Donner un mot, trouver ses synonymes/antonymes/relations sémantiques.

**Mécanique:**
- Afficher mot cible
- 4 choix: 1 correct + 3 distracteurs sémantiquement proches
- Exemple:
  - Mot: "Bonheur"
  - Options: [Joie ✓], [Tristesse], [Indifférence], [Nostalgie]

**Fichiers à créer:**
```
presentation/games/semantic/
├── SemanticViewModel.kt
├── SemanticScreen.kt
├── SemanticUiState.kt
└── SemanticRelationUtils.kt
```

**Données requises:**
- Base de synonymes
- Base d'antonymes
- Hiérarchies sémantiques

---

### 6. **Définition à Compléter** ✍️
**Priorité:** Basse | **Complexité:** Basse

**Description:**
Une définition est présentée avec un mot manquant: compléter le texte.

**Mécanique:**
- Afficher définition avec trou: "Un _____ est un animal domestique"
- 4 choix de mots
- Sélectionner le bon
- Ou: Mode libre (suggérer des mots)

**Fichiers à créer:**
```
presentation/games/fillword/
├── FillWordViewModel.kt
├── FillWordScreen.kt
└── FillWordUiState.kt
```

---

## 📊 Tableau d'Implémentation

| Jeu | Priorité | Complexité | Status | Package | Tokens |
|-----|----------|-----------|--------|---------|--------|
| **Matching** | Haute | Moyenne | ✅ Complété | `games.matching` | ~50k |
| **QCM** | Haute | Moyenne | ✅ Complété | `games.qcm` | ~50k |
| **Hangman** | Haute | Moyenne | ✅ Complété | `games.hangman` | ~50k |
| **Spelling** | Haute | Moyenne | ✅ Complété | `games.spelling` | ~60k |
| **Anagrammes** | Haute | Moyenne | ⏳ À faire | `games.anagrams` | ~50k |
| **Chrono** | Haute | Moyenne | ⏳ À faire | `games.chrono` | ~60k |
| **Memory** | Moyenne | Moyenne | ⏳ À faire | `games.memory` | ~55k |
| **Spelling Avancé** | Haute | Haute | ⏳ À faire | `games.spelling` | ~40k |
| **Associations** | Moyenne | Haute | ⏳ À faire | `games.semantic` | ~70k |
| **Définition Complète** | Basse | Basse | ⏳ À faire | `games.fillword` | ~30k |

---

## 🔄 Flux d'Intégration

### Pour chaque jeu à implémenter:

1. **Créer la structure:**
   ```
   presentation/games/{game_name}/
   ├── {GameName}Screen.kt
   ├── {GameName}ViewModel.kt
   ├── {GameName}UiState.kt
   └── {GameName}Utils.kt (si nécessaire)
   ```

2. **Ajouter à LexicaApp.kt:**
   ```kotlin
   // Écran
   data object {GameName}Game : Screen("game_{name}")
   
   // Navigation
   composable(Screen.{GameName}Game.route) {
       {GameName}Screen(
           repository = repository,
           onBack = { navController.navigateUp() }
       )
   }
   ```

3. **Ajouter à MiniGamesScreen.kt:**
   ```kotlin
   GameButton(
       text = "🎮 Nom du Jeu",
       onClick = { onGameSelected("game_{name}") }
   )
   ```

4. **Tester:**
   - Compilation: `./gradlew :app:assembleDebug`
   - Navigation fonctionnelle
   - Pas de crash au lancement
   - Sauvegarde du score

---

## 🎯 Phase Recommandée d'Implémentation

### **Phase 1 (Semaine 1):** Jeux rapides
- [ ] Anagrammes
- [ ] Chrono Mode (QCM chronométré)
- [ ] Memory

### **Phase 2 (Semaine 2):** Jeux avancés
- [ ] Spelling Avancé (avec phonétique)
- [ ] Associations Sémantiques
- [ ] Définition à Compléter

### **Phase 3 (Backlog):** Améliorations
- [ ] Modes multiplayers (futur)
- [ ] Daily Challenges (lié aux streaks)
- [ ] Statistiques avancées par jeu

---

## 💾 Points de données requises

Les jeux ont besoin de:
1. **Base de données existante** (7000+ mots) ✅ À importer
2. **Synonymes/Antonymes** ⏳ À ajouter
3. **Relations sémantiques** ⏳ À enrichir
4. **Exemples d'utilisation** ⏳ À extraire de Wiktionnaire

---

## ⚠️ Notes d'implémentation

- **Réutilisabilité:** Créer des composables partagées (`TimerCountdown`, `ScoreDisplay`, `GameButton`)
- **Animations:** Utiliser `androidx.compose.animation`
- **Persistance:** Chaque jeu doit enregistrer son score dans `UserStatsEntity`
- **TTS:** Réutiliser `TextToSpeech` depuis Spelling Game pour tous les jeux

---

**Document créé:** 2026-02-27
**Basé sur:** Version Python + État Kotlin actuel
**À mettre à jour:** Lors de chaque nouvel jeu implémenté


