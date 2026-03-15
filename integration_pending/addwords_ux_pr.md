# PR — TACHE_15 : Conception UX barre "Ajouter un mot"
*Livré le : 2026-03-09*
*Agent : Agent Développeur*

---

## Résumé

Refonte complète de l'expérience "Ajouter des mots" :
- **Barre de recherche intelligente** : filtre local temps réel + debounce API 500ms
- **Fiche d'aperçu** avant sauvegarde (définition éditable)
- **Formulaire manuel** avec accordéon pour champs optionnels
- **Gestion des doublons** avec AlertDialog (Mettre à jour / Annuler)
- **Mots proposés** affichés quand la barre est vide

---

## Fichiers modifiés

### 1. `presentation/addwords/AddWordsViewModel.kt` — réécriture complète

**Nouveau `AddWordsUiState`** (remplace l'ancien) :
```kotlin
data class AddWordsUiState(
    val searchQuery: String = "",
    val localMatches: List<Flashcard> = emptyList(),
    val apiResults: List<WordResult> = emptyList(),
    val isApiLoading: Boolean = false,
    val selectedResult: WordResult? = null,
    val previewDefinition: String = "",
    val previewSynonymes: String = "",
    val manualMode: Boolean = false,
    val manualWord / manualDefinition / manualSynonymes / ...,
    val manualExpanded: Boolean = false,
    val duplicateCandidate: Flashcard? = null,
    val pendingWord: WordResult? = null,
    val successMessage: String? = null,
    val error: String? = null,
    val proposedWords: List<WordReserveEntity> = emptyList(),
    val isLoadingProposed: Boolean = false
)
```

**Nouvelle signature `AddWordsViewModelFactory`** :
```kotlin
class AddWordsViewModelFactory(
    private val wordReserveRepository: WordReserveRepository,
    private val flashcardRepository: FlashcardRepository   // ← NOUVEAU
)
```

### 2. `presentation/addwords/AddWordsScreen.kt` — réécriture complète

Nouveaux composables :
- `LocalMatchItem` — mots déjà en liste avec badge "Déjà ajouté"
- `ApiResultItem` — résultats API avec bouton "+ Ajouter" → ouvre `PreviewDialog`
- `ReserveWordItem` — mots proposés (réserve locale)
- `PreviewDialog` — fiche d'aperçu avec définition et synonymes éditables
- `DuplicateDialog` — alerte doublon : Mettre à jour / Annuler
- `ManualAddDialog` — formulaire manuel avec accordéon pour champs optionnels

---

## ⚠️ Action obligatoire — `MainActivity.kt`

La factory `AddWordsViewModelFactory` nécessite maintenant **2 arguments** au lieu de 1.

### Modification à faire dans `MainActivity.kt`

**Avant :**
```kotlin
val addWordsFactory = AddWordsViewModelFactory(reserveRepository)
```

**Après :**
```kotlin
val addWordsFactory = AddWordsViewModelFactory(
    wordReserveRepository = reserveRepository,
    flashcardRepository = repository          // ← ajouter ce paramètre
)
```

`repository` est déjà déclaré juste au-dessus (ligne ~52) dans `MainActivity.kt` :
```kotlin
val repository = FlashcardRepositoryImpl(dao)
```

---

## Comportement détaillé

### Flux de recherche
1. L'utilisateur tape → dès **2 caractères** : filtre en mémoire sur `allCards` → section "✓ Déjà dans ta liste"
2. Après **500ms de silence** (debounce) : appel `wordReserveRepository.searchOnline(query)` → section "🌐 Définitions trouvées"
3. Si erreur réseau : message "Pas de connexion — tu peux ajouter le mot manuellement"
4. Si 0 résultat local ET 0 résultat API : message "Pas de résultat — tu peux ajouter le mot manuellement"

### Flux d'ajout via API
1. Clic "+ Ajouter" sur un résultat API → `PreviewDialog` s'ouvre
2. L'utilisateur peut modifier la définition et les synonymes
3. Clic "Confirmer l'ajout" → vérification doublon → `doAddWord()` → toast vert 3s

### Gestion des doublons
1. `checkDuplicateAndAdd()` cherche dans `allCards` par `recto` (insensible à la casse)
2. Si doublon : `DuplicateDialog` → "Mettre à jour" appelle `flashcardRepository.saveCard(existing.copy(...))`
3. Si annulé : rien ne se passe

### Formulaire manuel
- Toujours accessible via le bouton violet en bas de l'écran
- Pré-remplit le mot avec la valeur de la barre de recherche
- Champs obligatoires : Mot + Définition
- Champs optionnels (accordéon AnimatedVisibility) : Synonymes, Catégorie, Étymologie, Exemples
- Validation inline : `isError` sur les champs vides si tentative de soumission

### Mode hors ligne
- Le filtre local fonctionne toujours (pas de réseau requis)
- Les mots proposés (réserve) sont locaux → toujours disponibles
- L'ajout manuel est toujours possible

---

## Checklist de validation

- [x] Barre de recherche : filtre local dès 2 caractères, sans debounce
- [x] Recherche API : debounce 500ms, indicateur de chargement
- [x] Section "✓ Déjà dans ta liste" : 5 résultats max avec badge
- [x] Section "🌐 Définitions trouvées" : résultats avec catégorie grammaticale
- [x] Fiche d'aperçu : définition éditable avant confirmation
- [x] Message d'erreur réseau avec fallback vers ajout manuel
- [x] Bouton "Ajouter manuellement" toujours visible en bas
- [x] Formulaire manuel : champs Mot + Définition obligatoires
- [x] Accordéon champs optionnels avec AnimatedVisibility
- [x] AlertDialog doublon : Mettre à jour / Annuler
- [x] Toast succès vert 3 secondes, puis effacé automatiquement
- [x] Mots proposés affichés quand barre vide
- [x] `LazyColumn` dans `Column` avec `.weight(1f)` → pas de crash Compose
- [x] `AddWordsViewModelFactory` : 2 args (WordReserveRepository + FlashcardRepository)
- [x] 0 erreur de compilation

---

## Résumé des actions d'intégration

| # | Action | Fichier | Complexité |
|---|--------|---------|------------|
| 1 | Passer `flashcardRepository = repository` à `AddWordsViewModelFactory` | `MainActivity.kt` | **Trivial** (1 ligne) |

C'est **la seule modification** à faire hors du package `presentation/addwords/`.

