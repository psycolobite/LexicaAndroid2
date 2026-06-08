# TACHE_R3 — Pipeline corpus vers extraits candidats

## Résumé

Cette tâche met en place la variante "extraits d'abord" du pipeline de contenu pour le nouveau module de recherche Lexica. Elle permet de partir de corpus sources, d'en extraire des passages candidats, de les annoter avec des métadonnées exploitables (mots intéressants, difficulté, registre, qualité de contexte), et de les indexer par domaine et difficulté.

## Fichiers créés

### Package : `data/corpus/`

| Fichier | Rôle |
|---------|------|
| `CorpusSource.kt` | Modèle représentant une source de corpus (livre, article, interview...) avec métadonnées (auteur, année, licence, type, domaines) |
| `ExtractCandidate.kt` | Schéma d'un extrait candidat : contenu textuel, position, mots suggérés, tags domaine/registre, difficulté, score de qualité |
| `CorpusParser.kt` | Logique de segmentation : découpage en paragraphes, tokenisation, détection de mots intéressants (stop list française + critères de longueur), calcul de qualité de contexte, estimation de difficulté et registre |
| `CorpusIndex.kt` | Indexation légère en mémoire : consultation par domaine, par difficulté, ou combinaison des deux |

## Architecture

```
data/corpus/
├── CorpusSource.kt       # Modèle source
├── ExtractCandidate.kt   # Modèle extrait candidat
├── CorpusParser.kt       # Segmentation + détection + scoring
└── CorpusIndex.kt        # Indexation en mémoire
```

## Dépendances

- **Aucune dépendance externe** — tout est en Kotlin standard (stdlib, `java.text.Normalizer`)
- Dépend conceptuellement de `TACHE_R2` pour la taxonomie (`InterestTaxonomy.kt` dans `presentation/search/preferences/`) — les `domainTags` et `registerTags` des extraits sont conçus pour être compatibles avec les IDs de la taxonomie R2

## Instructions pour le Chef d'Orchestre

### 1. Copie des fichiers

Copier les 4 fichiers du package `data/corpus/` vers le projet cible :
```
app/src/main/java/com/example/lexicaandroid2/data/corpus/
```

### 2. Intégration DI (si nécessaire)

Si un conteneur DI est utilisé, ajouter les dépendances suivantes :

```kotlin
// Dans le module DI ou AppContainer
val corpusParser = CorpusParser()
// Le CorpusIndex est construit via CorpusIndex.index(listeDExtraits)
```

### 3. Vérification de cohérence avec TACHE_R2

- Les `domainTags` dans `CorpusSource` et `ExtractCandidate` sont des `List<String>` correspondant aux IDs de domaines de `InterestTaxonomy.kt`
- Les `registerTags` dans `ExtractCandidate` sont des `List<String>` correspondant aux IDs de registres
- Le parser (`CorpusParser.estimateRegisterTags`) produit des tags comme `"soutenu"`, `"courant"`, `"technique"` — à aligner avec les IDs exacts de la taxonomie R2 si nécessaire

### 4. Tests minimaux recommandés

Vérifier le pipeline avec un mini-corpus de test :

```kotlin
val source = CorpusSource(
    id = "test_001",
    title = "Test",
    author = "Auteur",
    year = 2024,
    type = SourceType.BOOK,
    license = "PUBLIC_DOMAIN",
    contentUrl = null,
    domainTags = listOf("litterature")
)

val texte = """
    La littérature française du XIXe siècle a profondément transformé les 
    structures narratives traditionnelles. Les écrivains de cette période 
    ont exploré des thématiques jusqu'alors inédites, notamment la 
    psychologie des personnages et les mécanismes sociaux sous-jacents.
    
    Victor Hugo, figure emblématique de ce mouvement, a su incarner 
    cette révolution stylistique avec une puissance rare. Son oeuvre 
    monumentale témoigne d'une compréhension exceptionnelle des 
    contradictions humaines.
""".trimIndent()

val parser = CorpusParser()
val extraits = parser.parse(source, texte)
val index = CorpusIndex.index(extraits)

// Vérifications
assert(extraits.isNotEmpty())
assert(extraits.all { it.contextQuality >= ExtractCandidate.MIN_CONTEXT_QUALITY })
assert(index.availableDomains().contains("litterature"))
```

### 5. Points d'attention

- **Stop list** : la liste des mots fréquents exclus (`CorpusParser.detectInterestingWords`) est en français et couvre ~150 mots. Elle peut être enrichie si des faux positifs sont détectés.
- **Qualité de contexte** : le score `contextQuality` est un indicateur heuristique (0.0-1.0). Le seuil `MIN_CONTEXT_QUALITY = 0.3` est conservateur — à ajuster selon les retours.
- **Registres** : la détection des registres est basée sur des marqueurs lexicaux simples. Elle peut être améliorée dans une version ultérieure.
- **Performance** : l'indexation est en mémoire (`Map`). Pour des corpus volumineux (>10 000 extraits), une solution persistante (Room) pourrait être nécessaire à terme.

## Questions ouvertes

1. Faut-il ajouter un champ `sourcePreview` (titre + auteur) directement dans `ExtractCandidate` pour éviter une jointure ultérieure ?
2. Le seuil `MIN_CONTEXT_QUALITY = 0.3` est-il adapté ou faut-il le remonter à 0.5 pour une V1 plus qualitative ?
3. Faut-il prévoir une version `suspend` des méthodes de `CorpusParser.parse()` pour les très gros corpus (exécution sur un dispatcher IO) ?
