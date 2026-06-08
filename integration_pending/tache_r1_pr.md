# TACHE_R1 — Architecture produit de l'écran recherche

## Résumé

Livraison de la spécification fonctionnelle et UX du nouvel écran de recherche principal (ExploreScreen).

**Fichier livré :**
- `presentation/search/explore/ExploreScreenSpec.kt` — maquette fonctionnelle complète

**Package créé :** `presentation/search/explore/`

**Aucun fichier existant modifié.**

---

## Contenu de la spec

Le fichier `ExploreScreenSpec.kt` couvre les 8 sections suivantes :

1. **Structure de l'écran** — Hiérarchie visuelle complète avec proportions (TopAppBar, zone extrait en weight(1f), signal d'intérêt, barre basse secondaire). Comportement à l'ouverture : extrait déjà affiché sans action préalable.

2. **États de l'écran** — `ExploreUiState` sealed class avec 4 états : `Loading`, `ExtractDisplayed`, `NoExtractAvailable`, `Error`. Transitions documentées.

3. **Interactions utilisateur** — Tableau complet des 8 gestes : tap court sur mot surligné (ajout/retrait), appui long (définition), swipe navigation, note d'intérêt, navigation vers catalogue/recherche mots.

4. **États visuels des mots** — `WordStatus` enum : `NORMAL`, `SUGGESTED` (jaune), `ADDED` (vert), `TRANSITIONING`. Règles visuelles et comportementales.

5. **Conventions de nommage** — Liste complète des composables, ViewModel, états, et structure de `SearchUiState` + `ExtractUiModel`.

6. **Points ouverts à arbitrer** — 7 points documentés avec recommandations : swipe vs scroll, signal explicite vs implicite, conflits de gestes, vidéo V1/V2, nombre max de mots surlignés, comportement mot déjà ajouté, animations.

7. **Règles de gestion des erreurs et cas limites** — 8 règles pour les cas edge (corpus vide, extrait trop court, swipe rapide, restauration d'état).

8. **Dépendances et intégration** — Ce qui est nécessaire pour l'implémentation (TACHE_R6) et les actions Chef d'Orchestre.

---

## Points d'attention pour le Chef d'Orchestre

### Décisions à arbitrer

Les 7 points ouverts dans la section 6 de la spec nécessitent une validation avant de lancer TACHE_R6. Les plus importants :

1. **Navigation entre extraits** — Ma recommandation : swipe horizontal (type stories). Pas de conflit avec le scroll interne de l'extrait. À valider.

2. **Signal d'intérêt explicite** — Ma recommandation : barre de note 1-5 légère et repliable. Permet un signal explicite dès la V1.

3. **Extrait vidéo** — Ma recommandation : texte seulement en V1, architecture préparée pour la vidéo en V2.

### Dépendances avec les autres tâches

- **TACHE_R6** (UI interactive) dépend fortement de cette spec — attendre la validation du Chef d'Orchestre avant de lancer R6.
- **TACHE_R3/R4** (pipelines corpus) peuvent avancer en parallèle — la spec définit le format d'extrait attendu (`ExtractUiModel`).
- **TACHE_R2** (préférences utilisateur) peut aussi avancer en parallèle — le profil utilisateur alimentera le choix du premier extrait.

### Intégration future dans les fichiers coeur

Quand TACHE_R6 sera livrée, le Chef d'Orchestre devra :
1. Ajouter `Screen.Explore` dans `presentation/navigation/Screen.kt`
2. Ajouter la route `composable("explore")` dans `LexicaApp.kt`
3. Brancher la navigation depuis le Dashboard

---

## Questions ouvertes pour le Chef d'Orchestre

1. **Swipe horizontal ou scroll vertical ?** — Voir section 6.1 de la spec. Impact fort sur l'UX et l'implémentation.

2. **Barre de note 1-5 ou signaux implicites seulement ?** — Voir section 6.2. Impact sur la richesse des données utilisateur.

3. **Faut-il un écran de préférences au premier lancement avant d'accéder à ExploreScreen ?** — Non spécifié dans R1, mais lié à TACHE_R2. À arbitrer si l'ordre d'intégration change.

4. **Quelle priorité pour le support vidéo ?** — Voir section 6.4. La spec recommande V1 texte only.

---

## Statut

✅ Livré — En attente de validation par le Chef d'Orchestre avant de lancer TACHE_R6.
