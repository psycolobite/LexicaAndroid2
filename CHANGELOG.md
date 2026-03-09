# Documentation fonctionnelle Lexica

## Suivi des fonctionnalites
- [x] Couche data Room (FlashcardEntity, DAO, Database)
- [x] Import legacy JSON (assets) et mapping vers Room
- [x] Algorithme SM-2 (logique pure) + tests unitaires
- [x] Repository (domain/data) + mappers
- [x] ViewModel de revue + UI Compose de base
- [x] UI Review: carte, notation, snackbar, fin de session
- [x] UI Dashboard: compteurs de cartes et bouton de lancement
- [x] Schema DB v2: ajout colonne 'state' et migration destructive
- [x] Sprint 3: TopAppBar globale, Navigation BackHandler, Transitions animees
- [x] Sprint 3 Edge Cases: Gestion base vide, retour automatique apres session
- [x] Sprint 4: Terminologie NEW -> TO_LEARN, ui dashboard v2, word list, api recherche
- [x] Sprint 4: Implementation complete du Screen WordList, ViewModel, et Service API (Wiktionnaire scraping).
- [x] Sprint 4: Integration du bouton "Parcourir" dans Dashboard et Navigation.
- [x] Sprint 4: Ajout Dialog recherche avec resultat API et ajout DB.
- [x] UI/UX Fixes: Palette pastel, fond blanc cassé, Snackbar rapide, Clic sur stats
- [x] Refactor Snackbar: Utilisation de Channel(CONFLATED) et dismiss() manuel pour actualisation immédiate.
- [x] Navigation Filtre: Redirection depuis Dashboard vers WordList avec filtre interactif.
- [x] UI Refonte ajout mots: Création `AddWordsScreen` avec liste de réserve et recherche.
- [x] Backend Reserve: Table `word_reserve`, DAO, Repository, et import `mots_rares.json`.
- [x] Navigation Dashboard: Boutons distincts pour "Ajouter des mots" et "Parcourir mes mots".
- [x] Cleanup: Nettoyage de `WordListScreen` (suppression dialog recherche obsolète).

## Problemes rencontres et resolutions
- Erreur Gradle plugin Kotlin en double: corrige en alignant les plugins via catalog et apply false.
- Avertissement compileSdk/AGP: compileSdk ajuste a 34 pour compatibilite.
- java.time sur minSdk 24: activation du coreLibraryDesugaring.
- Crash Compose (ArrayIndexOutOfBoundsException): ajustement des versions Kotlin/KSP/Compose compiler.
- Incompatibilite Kotlin/Compose compiler: versions realignees pour stabiliser Compose Runtime.
- Erreurs Compose AnimatedContent: utilisation correcte de targetState/targetCard.
- Blocage UI au demarrage: import JSON deplace sur Dispatchers.IO.
- Erreur Room @MapColumn: correction des queries GROUP BY avec annotation explicite.
- Crash Dashboard vide: gestion des cas avec 0 cartes.
- Navigation Refactor: Remplacement de NavGraph par LexicaApp avec Scaffold global.
- "Unresolved reference LexicaApp": Importation correcte dans MainActivity apres creation du fichier.
- Compilation Error DashboardScreen: Correction de "Expecting top level declaration" causée par des accolades déséquilibrées lors de l'édition. Fichier recréé proprement.
- Bug LexicaApp: Logique de titre et navigation arrière mise à jour pour inclure WordListScreen.
- Integration API: Implémentation d'un scraper Jsoup pour Wiktionnaire car l'API REST est moins riche en définitions structurées.
- Bug Import Asynchrone: L'UI démarrait avant la fin de l'import initial, affichant une base vide. Corrigé en déplaçant `setContent` dans le bloc `lifecycleScope.launch` après l'import (sur Dispatchers.Main).
- Bug Snackbar Lent: Les notifications de révision ne s'affichaient pas assez vite lors des clics rapides. Résolu en annulant manuellement le snackbar précedent avant d'afficher le nouveau.
- Compilation Error ReviewViewModel: `tryEmit` n'existe pas sur `Channel`, remplacé par `trySend`.
- Missing Imports: Restauration des imports Kotlin Flow/Coroutines perdus lors du refactoring de ReviewViewModel.
- Navigation Param: Ajout correct de la route `wordlist?filter={filter}` et gestion des arguments dans `LexicaApp`.
- Refactoring Import: Ajout de la gestion de `WordReserveEntity` dans `DataImporter`.
- Navigation Arguments: Gestion des filtres dans `LexicaApp` pour `WordListScreen`.
- Build Error: Corruption du fichier `AddWordsScreen.kt` et imports manquants. Corrigé en recréant le fichier stable.
- UI: Bouton "Ajouter des mots" manquant dans le code Compose initial du Dashboard. Ajouté explicitement avant "Parcourir mes mots".
- Bug Recherche AddWordsScreen: le scraper Wiktionnaire renvoyait 0 resultat (parsing fragile et absence de logs).
- Fix Recherche API: ajout de logs detailles, recuperation du statut HTTP et extraction plus robuste de la section Francais.
- Enrichissement Reserve: extension de `mots_rares.json` a 50 entrees pour la demo.

## État d'avancement pour reprise
- **Fonctionnalités terminées** :
    - Nouvelle architecture de données (Reserve vs Collection).
    - Nouvel écran `AddWordsScreen` fonctionnel (Affichage réserve + Recherche API + Ajout).
    - Navigation et Dashboard mis à jour avec les deux boutons.
    - Build stable et sans erreur.
- **À vérifier au prochain lancement** :
    - Confirmer visuellement la présence du bouton "AJOUTER DES MOTS" sur le Dashboard.
    - Tester le flux complet : Ajouter un mot de la réserve -> Vérifier qu'il apparaît dans "Mes mots".

## Point de vue utilisateur (APK FONCTIONNEL ACTUELLEMENT)
- Revision de cartes: affichage du mot, revelation de la definition, notation en 3 choix.
- Feedback immediat: message "Revue dans X jours" apres notation.
- Fin de session: ecran de fin avec recapitulatif et bouton de relance.
- Favoris: marquer/demarquer une carte via l'icone coeur.
- Suppression: supprimer une carte avec confirmation.
- Details enrichis: nature grammaticale, synonymes et exemple si disponibles.
- Navigation fluide: Animations de transition entre le Dashboard et la Revue.
- Interface unifiee: Barre de titre constante avec bouton retour contextuel.

## Tests
- Tests unitaires: .\gradlew :app:testDebugUnitTest
- Tests instrumentes: .\gradlew :app:connectedDebugAndroidTest
