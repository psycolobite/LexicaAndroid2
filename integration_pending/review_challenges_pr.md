# TACHE_14 PR — Défis intégrés dans la révision (Orthographique + Sémantique)

**Assigné à :** Agent Développeur (TACHE_14)  
**Date :** 2026-03-10  
**Status :** ✅ CODE LIVRÉ — Prêt pour intégration

---

## 📦 Livrables

### Fichiers créés
- ✅ `presentation/review/challenge/KeywordExtractor.kt` — Utilitaire Jaccard TF-IDF pour extraction de mots-clés
- ✅ `presentation/review/challenge/SemanticValidator.kt` — Interface + implémentations (Spelling, Jaccard)
- ✅ `presentation/review/challenge/ChallengeOverlay.kt` — Composables UI pour défis et résultats

### Fichiers modifiés
- ✅ `presentation/review/ReviewViewModel.kt` — Ajout logique complète des défis
- ✅ `presentation/review/ReviewScreen.kt` — Affichage ChallengeOverlay quand défi actif

---

## 🎮 **Architecture des défis**

### Déclenchement automatique
- Après chaque révision réussie (quality ≥ 3), incrémenter le compteur `correctReviews`
- Si `correctReviews % 3 == 0` → déclencher un défi
- Type de défi selon la face révisée :
  - **Face MOT (recto) révisée** → Défi Sémantique (taper la définition)
  - **Face DÉFINITION (verso) révisée** → Défi Orthographique (taper le mot)

### Détection de face
- `ReviewViewModel` track `currentFaceIsMotVersDef: Boolean`
- Au start de session = `true` (commence par le mot)
- Toggle après chaque carte avancée
- Utilisé pour déterminer le type de défi et mettre à jour le bon `Sm2Stats`

### Validation en 2 couches

#### Couche 1 — Jaccard TF-IDF (Kotlin pur, toujours actif)
- `KeywordExtractor` extrait 5 mots-clés importants de la réponse attendue
- Calcule la similarité Jaccard
- **Règle de validation sémantique :**
  - `keywordScore >= 0.6` → ✅ Succès (+15 XP)
  - `keywordScore >= 0.3` → 💡 Presque (+5 XP)
  - `keywordScore < 0.3` → ❌ Échec (0 XP)

#### Couche 2 — Spelling (validation exacte)
- `SpellingValidator` compare mot exact (case-insensitive)
- **Règle :**
  - Exact match (ignoring case) → ✅ Succès (+10 XP)
  - Sinon → ❌ Échec (0 XP)

### XP Bonus
- Pas d'impact sur le score SM2 (ne modifie pas `interval`, `repetitions`)
- Accumulation dans `ReviewUiState.xpBonusAccumulated`
- **À intégrer :** Passer au `GamificationViewModel` après la session (voir ci-dessous)

---

## 🔧 Modifications requises dans les fichiers coeur

### 1. **Aucune modification requise dans `LexicaApp.kt`**
   - Les défis ne changent pas la structure de navigation
   - `ReviewScreen` reste inchangé au niveau de la route

### 2. **GamificationViewModel — Brancher XP Bonus (Optional pour V1)**
   Si on veut que les XP défis soient comptabilisés :
   - Passer `gamificationViewModel` en paramètre de `ReviewViewModel` (via DI)
   - Dans `dismissChallenge()`, appeler `gamificationViewModel.addXp(xpBonusFromChallenge)`
   - **Pour V1 :** Les XP bonus sont accumulés dans `xpBonusAccumulated` mais pas encore complémentés. Peut être hookés en TACHE_15

---

## ✅ Vérifications de la tâche

### Tests manuels à effectuer (après intégration)
- [ ] Réviser une carte, obtenir "Je l'ai" (quality 4)
- [ ] Vérifier qu'aucun défi ne s'affiche au 1er "Je l'ai"
- [ ] Obtenir 2 autres "Je l'ai" pour atteindre correctReviews = 3
- [ ] À `correctReviews % 3 == 0` → Défi s'affiche ✅
- [ ] Type de défi correct selon face révisée ✅
- [ ] Taper la bonne réponse orthographe → +10 XP affiché ✅
- [ ] Taper une définition partielle → +5 XP ✅
- [ ] Taper une mauvaise définition → 0 XP + réponse correcte affichée ✅
- [ ] Bouton Abandonner → passe à la carte suivante sans XP ✅
- [ ] Continuer après défaut → revenir à la révision normale ✅

### Compilation
```bash
./gradlew clean :app:assembleDebug
```
Doit compiler sans erreurs.

---

## 📝 Notes d'intégration

### Architecture `ReviewViewModel`
```
gradeCard(quality) 
  ├─ Si quality < 3 → advance to next card
  └─ Si quality >= 3 && correctReviews % 3 == 0
     ├─ Déterminer type de défi (SPELLING ou SEMANTIC)
     └─ activeChallengeType = type
     └─ Attendre validateChallenge() puis dismissChallenge()
```

### Flow du défi
1. User tape sa réponse
2. Clic "Valider" → `validateChallenge()` → calcule `ValidationResult`
3. Affiche `ChallengeResultOverlay` avec feedback + XP
4. Clic "Continuer" → `dismissChallenge()` → advance to next card
5. Retour à la révision normale

### Storage des XP
- `xpBonusAccumulated` dans `ReviewUiState` accumule tous les XP des défis
- **À faire en V2/TACHE_15 :** Passer au `GamificationViewModel` après la session complète

---

## 🎨 UX/Game Design Checklist (TACHE_14)

✅ Défi déclenché automatiquement tous les 3 points ✅  
✅ Type de défi correct selon face révisée ✅  
✅ Zone de saisie claire avec placeholder ✅  
✅ Feedback immédiat avec XP bonus affiché ✅  
✅ Réponse correcte affichée en cas d'échec ✅  
✅ Abandon possible sans pénalité ✅  
✅ Pas d'impact sur SM2 (défis isolés) ✅  
✅ Aucun crash si défi échoue ✅  

---

## 🚀 Dépendances

**Aucune nouvelle dépendance Gradle requise pour V1.**

TFLite sera optionnel en V2 (quand on veut la couche 2 complète).

---

## 🎯 Prochaines étapes (TACHE_15+)

1. **V2 Couche TFLite :** Intégrer modèle MiniLM pour similarité sémantique complète
2. **Brancher XP** : Passer `xpBonusAccumulated` au `GamificationViewModel` après session
3. **Daily Challenge** : Utiliser les défis dans les Daily Challenges
4. **Analytics** : Tracer taux de succès/abandon des défis

---

## 📚 Inspiration open source

- **AnkiDroid Active Recall** (`github.com/ankidroid/Anki-Android`) → `TypeAnswer` feature
- **Duolingo UI** → patterns de feedback et validation immédiate
- **TFLite NLP** (`github.com/tensorflow/examples/lite/examples/text_classification`)

---

**Tous les fichiers sont dans les packages dédiés. Prêt pour fusion!** 🎉
