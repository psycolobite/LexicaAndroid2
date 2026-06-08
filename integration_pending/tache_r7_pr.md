# Tâche R7 — Catalogue d'ouvrages et accès à la source complète

Ce document résume l'implémentation de la tâche **TACHE_R7** visant à créer le catalogue d'ouvrages et à l'intégrer au flux de navigation de l'application Lexica.

## Résumé des modifications

1. **`CatalogueRepository`** ([CatalogueRepository.kt](file:///c:/Users/r0xef/AndroidStudioProjects/LexicaAndroid2/app/src/main/java/com/example/lexicaandroid2/presentation/search/catalogue/CatalogueRepository.kt))
   - Récupération des sources depuis `CorpusSources`.
   - Filtrage par domaines et extraction des candidats associés depuis `CorpusIndex`.
   - Recherche d'ouvrages similaires basés sur les tags de domaines partagés.

2. **`CatalogueViewModel`** & **`CatalogueViewModelFactory`** ([CatalogueViewModel.kt](file:///c:/Users/r0xef/AndroidStudioProjects/LexicaAndroid2/app/src/main/java/com/example/lexicaandroid2/presentation/search/catalogue/CatalogueViewModel.kt))
   - Gestion de l'état `CatalogueUiState` (liste des sources, source sélectionnée, extraits, ouvrages reliés, états de chargement/erreur).
   - Gestion du filtrage multi-domaines, du tri (par date, pertinence, titre, et nombre d'extraits).
   - Publication d'événements à sens unique pour l'ouverture d'URLs externes dans le navigateur.

3. **`CatalogueScreen`** ([CatalogueScreen.kt](file:///c:/Users/r0xef/AndroidStudioProjects/LexicaAndroid2/app/src/main/java/com/example/lexicaandroid2/presentation/search/catalogue/CatalogueScreen.kt))
   - Écran de liste principal avec filtres thématiques (chips horizontales) et menu déroulant de tri.
   - Vue détaillée d'un ouvrage avec métadonnées enrichies, boutons d'action (accès à la source intégrale externe), liste des extraits recommandés et ouvrages similaires.
   - Transitions et micro-animations fluides entre la vue liste et la vue détaillée.

4. **Modifications de Navigation et Raccordement :**
   - **`Screen.kt`** : Déclaration de la route `catalogue?sourceId={sourceId}`.
   - **`ExploreScreen.kt`** : Raccordement de l'action `onInfoClick` de la TopAppBar pour naviguer vers le catalogue pré-ciblé sur la source de l'extrait en cours.
   - **`LexicaApp.kt`** : Déclaration de la route dans le `NavHost`, instantiation locale du `CatalogueRepository`, configuration du titre et comportement du bouton retour de la TopAppBar.

## Tests et validations effectués

- **Tests unitaires locaux** ([CatalogueTest.kt](file:///c:/Users/r0xef/AndroidStudioProjects/LexicaAndroid2/app/src/test/java/com/example/lexicaandroid2/presentation/search/catalogue/CatalogueTest.kt)) couvrant les cas :
  - Filtrage des sources par domaines dans le Repository.
  - Recherche d'extraits et de sources reliées.
  - Chargement, filtrage, tri, et gestion de la sélection d'ouvrage dans le ViewModel.
  - Événements d'ouverture d'URL.
