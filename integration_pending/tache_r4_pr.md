# TACHE_R4 — Pipeline mots cibles vers extraits d'usage

## Résumé

Livraison du pipeline "mots cibles d'abord" : produire une liste de mots susceptibles d'intéresser l'utilisateur, puis rechercher des usages pertinents dans les corpus.

### Fichiers créés

| Fichier | Package | Description |
|---------|---------|-------------|
| `TargetWordList.kt` | `data/corpus/` | Modèle de mots cibles pondérés + générateur `TargetWordGenerator` |
| `OccurrenceSearcher.kt` | `data/corpus/` | Interface `OccurrenceSearcher` + implémentation `SimpleOccurrenceSearcher` |
| `ContextFilter.kt` | `data/corpus/` | Filtrage qualité des contextes + déduplication + diversification |

### Aucun fichier existant modifié

---

## Contenu détaillé

### 1. `TargetWordList.kt`

**Modèles :**
```kotlin
data class TargetWord(word: String, weight: Float, source: TargetSource, domainIds: List<String>)
enum class TargetSource { USER_PREFERENCE, FREQUENCY_BASED, DOMAIN_SPECIFIC, EXPLORATORY }
data class TargetWordList(words: List<TargetWord>, generatedAt: Long, profileSnapshot: String)
```

**Générateur `TargetWordGenerator` :**
- `generateTargetWords(preferences: UserPreferences): TargetWordList`
- Utilise `InterestTaxonomyProvider.domainsForObjectives()` de TACHE_R2
- 3 niveaux de pondération : domaine (0.9), générique transverse (0.6), exploratoire (0.2-0.3)
- Mots pré-définis pour 10 domaines (biologie, physique, philosophie, littérature, cuisine, droit, médecine, informatique, argot, environnement)
- Mots génériques par objectif utilisateur (4 objectifs × 5 mots)
- Mots exploratoires génériques (10 mots)
- Déduplication + limite à 200 mots maximum

### 2. `OccurrenceSearcher.kt`

**Modèle :**
```kotlin
data class Occurrence(word: String, sourceId: String, context: String, position: Int, sentenceCount: Int)
```

**Interface :**
```kotlin
interface OccurrenceSearcher {
    suspend fun search(words: List<TargetWord>, corpus: List<CorpusSource>): List<Occurrence>
    suspend fun searchByDomain(domainIds: List<String>, corpus: List<CorpusSource>): List<Occurrence>
}
```

**Implémentation `SimpleOccurrenceSearcher` :**
- Recherche textuelle insensible à la casse
- Extraction de contexte : phrase contenant le mot + 1 phrase avant/après
- Limite : max 5 occurrences par mot
- Filtrage des mots outils français (stop words)
- `searchByDomain()` : filtre le corpus par domaine, extrait les mots longs (> 6 car.) comme candidats

### 3. `ContextFilter.kt`

**Modèle :**
```kotlin
data class FilteredExtract(occurrence: Occurrence, qualityScore: Float, reason: String)
```

**Filtre `ContextFilter` :**
- `filterOccurrences(occurrences): List<FilteredExtract>` — filtre et trie par score
- 5 critères de qualité pondérés :
  - Longueur du contexte (15%) — ni trop court, ni trop long
  - Phrase complète (25%) — majuscule début, point fin
  - Clarté du contexte (30%) — présence de définitions, synonymes
  - Absence de bruit (20%) — pas de listes, titres, caractères spéciaux
  - Mots intéressants proches (10%) — bonus pour vocabulaire riche
- Score minimum : 0.4/1.0
- `deduplicateByWord(extracts, maxPerWord=3)` — évite la redondance
- `diversifyBySource(extracts, maxPerSource=5)` — diversifie les sources

---

## Instructions pour le Chef d'Orchestre

### Copie des fichiers

Les 3 fichiers sont dans `data/corpus/`. Copier tels quels.

### Vérification de cohérence avec R3

Si TACHE_R3 a été livrée, vérifier que :
- Les modèles `CorpusSource` et `ExtractCandidate` de R3 sont cohérents avec `Occurrence` et `FilteredExtract` de R4
- Le package `data/corpus/` est bien partagé entre R3 et R4
- Pas de conflit de nommage entre les classes

### Intégration DI

Si le projet utilise un conteneur manuel ou Hilt, ajouter :

```kotlin
// Dans AppContainer ou équivalent
val targetWordGenerator = TargetWordGenerator
val occurrenceSearcher: OccurrenceSearcher = SimpleOccurrenceSearcher()
val contextFilter = ContextFilter
```

### Points d'attention

1. **Mots pré-définis par domaine** : La liste actuelle est statique (10 domaines × 5 mots). Pour une V2, il faudrait soit :
   - Charger les mots depuis une ressource JSON
   - Les générer dynamiquement depuis un lexique
   - Les enrichir via une API externe

2. **Performance de la recherche** : `SimpleOccurrenceSearcher` fait une recherche textuelle naïve (`indexOf`). Pour des corpus volumineux, il faudra une indexation plus sophistiquée (inverted index, Lucene, etc.).

3. **Stop words** : La liste actuelle est basique. Pour une meilleure qualité, utiliser une liste plus complète (ex: https://github.com/stopwords-iso/stopwords-fr).

4. **ContextFilter** : Les poids des critères (0.15, 0.25, 0.30, 0.20, 0.10) sont empiriques. À ajuster selon les retours utilisateur.

### Dépendances futures

- **TACHE_R5** (moteur de scoring) : utilisera `TargetWordList` et `FilteredExtract` comme entrées du scoring
- **TACHE_R6** (UI interactive) : utilisera `FilteredExtract` pour afficher les extraits
- **TACHE_R9** (profil d'intérêt) : les mots ajoutés/ignorés par l'utilisateur pourront ajuster les poids dans `TargetWordGenerator`

---

## Questions ouvertes pour itération future

1. **Faut-il rendre les mots par domaine configurables** (JSON externe plutôt que hard-codés) ?
2. **Faut-il un mode "recherche floue"** pour les fautes d'orthographe ou les variations ?
3. **Comment gérer les mots composés** (ex: "pomme de terre", "chef-d'œuvre") ?
4. **Faut-il une indexation persistante** (Room) pour éviter de re-scanner les corpus à chaque fois ?
5. **Les poids du ContextFilter doivent-ils être ajustables par l'utilisateur** (préférence pour des contextes plus longs/courts) ?

---

## Statut

✅ Livré — En attente d'intégration par le Chef d'Orchestre.
