# PR — Refonte UX Jeu de Correspondance (Matching)
*Livré le : 2026-03-12*

---

## Problèmes corrigés

### 1. Double header supprimé
**Avant :** La `TopAppBar` affichait "Correspondance" ET en dessous la barre violette (`GameHeader`) affichait "Jeu de Correspondance + score + %" → deux barres pour la même info, ~100dp perdus.

**Après :** Le `GameHeader` n'affiche plus qu'une barre compacte (score + barre de progression) sur ~40dp. Le titre est uniquement dans la `TopAppBar`. Gain d'espace énorme.

### 2. Cartes appariées illisibles
**Avant :** Les paires trouvées devenaient `enabled = false`, ce qui les grisait légèrement — quasi-invisible.

**Après :** Paires trouvées = **fond vert vif** `#2E7D32` + **texte blanc** + **icône ✓** à droite. Tentative incorrecte = **fond rouge** `#C62828` + **icône ✗** pendant 900 ms puis reset.

### 3. Bouton "Retour au menu" → "Valider"
**Avant :** Validation automatique au clic sur la définition. Bouton "Retour au menu" en bas.

**Après :** Validation globale via un bouton "VALIDER" en bas, activé uniquement quand toutes les paires sont reliées. Plus d'erreur immédiate frustrante.

## Mise à jour v2 (2026-03-16)

### 4. Consolidation finale de l'interface
**Avant :** Encore deux barres distinctes (TopBar + GameHeader).

**Après :** Intégration totale dans une `GameTopAppBar` unique. Gain maximal d'espace vertical.

### 5. Règles de jeu strictes
- **3 erreurs maximum** par planche : au-delà, la solution est dévoilée et l'XP est annulée (0 XP).
- **Pénalité XP** : Si le joueur recommence après un échec (moins de 3 erreurs), le gain d'XP est divisé par 2.
- **Feedback visuel** : Nouveaux écrans intermédiaires clairs pour "Succès", "Échec", et "Solution".

---

## Fichiers modifiés

| Fichier | Changements |
|---------|-------------|
| `presentation/games/common/GameComposables.kt` | `GameHeader` compact (1 ligne), `SelectableButton` refonte (4 états + icônes), nouveau `FeedbackBanner` |
| `presentation/games/matching/MatchingViewModel.kt` | Ajout `MatchResult`, `wrongPairs`, `validateSelection()`, `lastMatchResult` |
| `presentation/games/matching/MatchingScreen.kt` | Intégration nouvelle logique + `FeedbackBanner` + bouton Valider conditionnel |

---

## Impact sur les autres jeux

`GameHeader` et `SelectableButton` sont dans `GameComposables.kt` — **fichier partagé par tous les jeux**.

### `GameHeader` — Impact immédiat sur tous les jeux utilisant ce composant :
Les jeux suivants bénéficient **automatiquement** du header compact (plus de double barre) :
- `MatchingScreen` ✅ (modifié directement)
- `QcmScreen`, `HangmanScreen`, `SpellingGameScreen`, `AnagramsScreen`, `ChronoScreen`, `SemanticGameScreen` → si ils appellent `GameHeader()`, la barre est maintenant compacte automatiquement

### `SelectableButton` — Paramètres nouveaux optionnels :
Les nouveaux paramètres `isFound` et `isWrong` ont une **valeur par défaut `false`** → **aucune régression** sur les autres jeux qui utilisent `SelectableButton` sans ces paramètres.

---

## Aucune intégration supplémentaire requise
- Pas de nouvelle dépendance
- Pas de changement de navigation
- Pas de migration Room
- Build : ✅ **BUILD SUCCESSFUL**

---

## Checklist

- [x] Double header supprimé (`GameHeader` compact)
- [x] `SelectableButton` : 4 états visuels (normal/sélectionné/trouvé/erreur)
- [x] Paires trouvées : fond vert + icône ✓ (clairement lisible)
- [x] Tentative incorrecte : flash rouge 900ms + icône ✗
- [x] Bouton "Valider" remplace "Retour au menu"
- [x] Validation manuelle (non automatique)
- [x] `FeedbackBanner` animée (fade in/out)
- [x] `MatchingViewModel.validateSelection()` avec délais coroutines
- [x] Build réussi sans erreur ni warning Kotlin
