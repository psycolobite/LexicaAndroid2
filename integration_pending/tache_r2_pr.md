# TACHE_R2 — Préférences utilisateur et taxonomie des intérêts

## Résumé

Livraison du socle de personnalisation déclarée pour le nouveau module de recherche.

### Fichiers créés

| Fichier | Package | Description |
|---------|---------|-------------|
| `UserPreferences.kt` | `presentation/search/preferences/` | Modèle de données des préférences utilisateur + enum `UserObjective` + labels |
| `InterestTaxonomy.kt` | `presentation/search/preferences/` | Taxonomie V1 (8 thèmes, 38 domaines, 5 registres) + table de correspondance objectifs → dimensions |
| `UserPreferencesRepository.kt` | `presentation/search/preferences/` | Stockage SharedPreferences + Flow d'accès |

### Aucun fichier existant modifié

---

## Contenu détaillé

### 1. `UserPreferences.kt`

```kotlin
data class UserPreferences(
    val objectives: List<UserObjective>,
    val preferredDomains: List<String>,
    val preferredRegisters: List<String>,
    val lastUpdated: Long
)

enum class UserObjective {
    CONVERSATIONS_SOUTENUES,
    TEXTES_EXIGEANTS,
    DIALECTES_ARGOTS,
    DOMAINE_SPECIFIQUE
}
```

- `UserObjectiveLabels` fournit les libellés affichables pour chaque objectif
- Les 4 objectifs correspondent exactement aux formulations produit du document DESCRIPTION.md section 5

### 2. `InterestTaxonomy.kt`

**8 thèmes :** Sciences, Littérature, Sciences humaines, Vie quotidienne, Technique, Arts, Société, Argots et dialectes

**38 domaines** répartis dans ces thèmes (ex: biologie, philosophie, cuisine, droit, cinéma, politique…)

**5 registres :** Soutenu, Courant, Familier, Argotique, Technique

**Table de correspondance `objectiveMappings` :**
- `CONVERSATIONS_SOUTENUES` → littérature, sciences humaines, vie quotidienne → registres soutenu/courant
- `TEXTES_EXIGEANTS` → sciences, sciences humaines, littérature → registres soutenu/technique
- `DIALECTES_ARGOTS` → argot/dialecte, société, arts → registres familier/argotique/courant
- `DOMAINE_SPECIFIQUE` → technique, sciences, arts → registres technique/soutenu/courant

Fonctions utilitaires : `domainsForObjectives()`, `themesForObjectives()`, `registersForObjectives()`

### 3. `UserPreferencesRepository.kt`

- Backend : `SharedPreferences` (choix délibéré pour sa simplicité)
- `savePreferences(prefs: UserPreferences)` — sauvegarde + notifie le Flow
- `getPreferences(): Flow<UserPreferences?>` — Flow réactif
- `clearPreferences()` — efface tout
- `hasCompletedOnboarding(): Flow<Boolean>` — Flow pour l'UI
- `hasCompletedOnboardingSync(): Boolean` — version synchrone pour guards
- `markOnboardingCompleted()` — marque l'onboarding sans modifier les préférences

---

## Instructions pour le Chef d'Orchestre

### Copie des fichiers

Les 3 fichiers sont dans `presentation/search/preferences/`. Copier tels quels.

### Intégration DI

Si le projet utilise un conteneur manuel ou Hilt, ajouter :

```kotlin
// Dans AppContainer ou équivalent
val userPreferencesRepository = UserPreferencesRepository(context)
```

### Points d'attention

1. **SharedPreferences vs DataStore** : Le choix SharedPreferences est volontaire pour minimiser les dépendances. Si le projet utilise déjà DataStore ailleurs, migrer ce repository vers DataStore pour uniformité.
2. **Taxonomie V1** : La taxonomie est une première version. Elle est conçue pour être enrichie par itérations. Les IDs sont stables (ne pas les changer sans migration).
3. **Mapping objectifs → thèmes** : La table de correspondance dans `InterestTaxonomyProvider.objectiveMappings` est le point d'entrée principal pour le moteur de recommandation (TACHE_R5). Si les règles métier évoluent, c'est ici qu'il faut les modifier.
4. **Onboarding** : Le flag `onboarding_done` est séparé des préférences elles-mêmes. Cela permet de marquer l'onboarding complété même si l'utilisateur n'a sélectionné aucun objectif (cas "Je verrai plus tard").

### Dépendances futures

- **TACHE_R3/TACHE_R4** (pipelines corpus) : utiliseront la taxonomie pour taguer les extraits
- **TACHE_R5** (moteur de scoring) : utilisera `objectiveMappings` pour le score de base
- **TACHE_R9** (profil d'intérêt) : combinera les préférences déclarées avec les événements comportementaux
- **UI onboarding** : à créer dans une tâche ultérieure (hors périmètre R2)

---

## Questions ouvertes pour itération future

1. **Faut-il ajouter un objectif "Généraliste / Exploration"** pour les utilisateurs qui ne savent pas quoi choisir ?
2. **La taxonomie doit-elle être extensible par l'utilisateur** (ajout de domaines personnalisés) ?
3. **Faut-il prévoir un poids par objectif** (ex: "surtout des conversations soutenues, un peu de technique") plutôt qu'une simple liste ?
4. **SharedPreferences ou DataStore** ? Si DataStore est déjà utilisé ailleurs, uniformiser.
5. **Faut-il internationaliser les labels** (actuellement en français uniquement) ?

---

## Inspiration open source

- **Now in Android** (Google) : pattern de repository avec Flow + SharedPreferences pour les préférences utilisateur
- **AnkiDroid** : taxonomie de tags et mapping vers les decks d'étude
