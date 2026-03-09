# État du projet (Mis à jour le 2026-02-27)

## Statut Global
- **Architecture** : Clean Architecture (MVVM) en place.
- **Base de données** : Room fonctionnel.
- **UI** : Jetpack Compose fonctionnel.
- **ACCEPTE** : Structure de synchronisation via `integration_pending`.
- **EN ATTENTE** : Fichier JSON complet (7000 mots) pour lister complètement les données.

## Structure des Dossiers
- `/docs` : Documentation, Backlog, Guidelines.
- `/integration_pending` : Zone tampon pour les modifications "Cœur" demandées par les agents.
- `/app/src/main/assets` : Fichiers de données (JSON).

## Tâches en Cours / À Assigner

### 1. Authentification & Cloud (Firebase) -> **Agent 1**
-   **Objectif** : Permettre la sauvegarde de la progression.
-   **Status** : À démarrer.
-   **Contrainte** : Ne pas casser l'utilisation "Hors ligne".

### 2. Gamification (XP & Niveaux) -> **Agent 2**
-   **Objectif** : Calculer l'XP et gérer les niveaux.
-   **Status** : À démarrer.

### 3. Mini-Jeu Orthographe (Vocal) -> **Agent 3**
-   **Objectif** : Un jeu où l'on entend le mot et on doit l'écrire.
-   **Status** : À démarrer.

### 4. Recherche & Exploration -> **Agent 4**
-   **Objectif** : Écran de recherche avec autocomplétion.
-   **Status** : À démarrer.

