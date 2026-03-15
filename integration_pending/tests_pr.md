# PR TACHE_16 — Tests Unitaires

## Résumé

Ajout de 4 fichiers de tests unitaires couvrant les composants créés en TACHE_14/14b (défis révision) et TACHE_12 (liste de mots).

## Fichiers créés

```
app/src/test/java/com/example/lexicaandroid2/
├── presentation/
│   ├── review/
│   │   ├── challenge/
│   │   │   ├── KeywordExtractorTest.kt          (14 tests)
│   │   │   └── SemanticValidatorTest.kt         (21 tests)
│   │   └── ReviewViewModelTest.kt               (18 tests)
│   └── wordlist/
│       └── WordListViewModelTest.kt             (16 tests)
```

**Total : 69 tests unitaires**

---

## Détail des tests

### KeywordExtractorTest (14 tests)
Teste l'objet `KeywordExtractor` — aucun mock requis.
- `tokenize()` : normalisation diacritiques, minuscules, ponctuation, espaces multiples
- `extractKeywords()` : filtrage stopwords, mots < 4 chars, topN, dédoublonnage, tri longueur
- `jaccardScore()` : textes identiques (1.0), totalement différents (0.0), overlap partiel
- `analyzeKeywords()` : found/missing, input vide, match parfait

### SemanticValidatorTest (21 tests)
Teste `SpellingValidator` et `JaccardSemanticValidator` — aucun mock requis.

**SpellingValidator :**
- Exact match → `isValid=true`, `xpBonus=10`
- Case-insensitive, espaces ignorés
- Mauvaise orthographe → `isValid=false`, `xpBonus=0`
- Feedback message contient le mot correct ou le checkmark ✅

**JaccardSemanticValidator :**
- Couverture >60% → `isValid=true`, `xpBonus=15`
- Couverture partielle → `xpBonus < 15`
- Input complètement faux → `xpBonus=0`
- Found/missing keywords correctement calculés
- Feedback contient ✅, ❌ ou 💡 selon le cas

### ReviewViewModelTest (18 tests)
Teste `ReviewViewModel` avec mocks Mockito sur `FlashcardRepository` et `DailyReviewStatDao`.
- `loadSession()` : popule l'état, gère liste vide
- `revealAnswer()` : flag `isAnswerRevealed`
- `gradeCard()` : appel repository, déclenchement défi à `correctReviews % 3 == 0`, quality < 3 ne déclenche pas, avance à la carte suivante
- `gradeCard()` face initiale = mot → challenge = `SPELLING`
- `onChallengeInputChanged()` / `validateChallenge()` : état challenge
- `dismissChallenge()` : clear état, avance carte, accumule XP
- `toggleFavorite()` / `deleteCurrentCard()` : mutations état + appels repository

### WordListViewModelTest (16 tests)
Teste `WordListViewModel` avec mocks Mockito sur `FlashcardRepository` et `DictionaryService`.
- `loadWords()` : popule cards, tri alphabétique, liste vide
- `onSearchQueryChanged()` : filtre recto/verso, case-insensitive, query vide restaure tout
- `onFilterSelected()` : filtres KNOWN / TO_LEARN / null (clear)
- `toggleFavorite()` : appel repository avec valeur toggleée
- `deleteCard()` : appel repository + rechargement

---

## Dépendances utilisées (déjà disponibles)

```kotlin
// build.gradle.kts — déjà présentes
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
```

Aucune nouvelle dépendance requise. `mockk` et `turbine` (utilisés dans `SearchViewModelTest.kt` existant) **ne sont pas ajoutés** — ce test existant ne compile probablement pas actuellement. Pour l'activer, ajouter dans `libs.versions.toml` :

```toml
[versions]
mockk = "1.13.10"
turbine = "1.1.0"

[libraries]
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
cashapp-turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
```

Et dans `build.gradle.kts` :
```kotlin
testImplementation(libs.mockk)
testImplementation(libs.cashapp.turbine)
```

---

## Notes d'intégration

- Aucune modification de fichier existant
- Tous les tests utilisent `@ExperimentalCoroutinesApi` + `UnconfinedTestDispatcher` pour les ViewModels
- `Dispatchers.setMain` / `resetMain` dans `@Before` / `@After`
- Style de test aligné sur `Sm2AlgorithmTest.kt` (pas de backticks, noms descriptifs)
