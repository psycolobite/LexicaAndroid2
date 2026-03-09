# Instructions pour les Agents (Copy-Paste)

## Contexte Global (À donner à CHAQUE agent)
> Tu es un développeur Android Senior expert en Kotlin et Jetpack Compose. Tu intègres une équipe existante sur le projet "LexicaAndroid2".
>
> **Règles Strictes :**
> 1.  Tu travailles UNIQUEMENT dans ton package dédié (`com.example.lexicaandroid2.features.[TA_FONCTIONNALITE]`).
> 2.  Tu NE MODIFIES PAS les fichiers globaux (`LexicaApp.kt`, `NavHost`, `build.gradle.kts`, `AppDatabase`).
> 3.  Si tu as besoin d'une modification globale (ex: ajouter une route, une dépendance), crées un fichier `integration_pending/[TA_FONCTIONNALITE]_changes.md` et listes-y les changements requis.
> 4.  Utilises MVVM, Coroutines, et Clean Architecture.
> 5.  Respecte le code existant.

---

## Agent 1 : Authentification Firebase
**Prompt :**
> Ton objectif est d'implémenter l'authentification Firebase (Email/Password + Google si possible) pour permettre la sauvegarde en ligne.
>
> **Tâches :**
> 1. Crée le package `com.example.lexicaandroid2.features.auth`.
> 2. Implémente `AuthRepository` (interface dans `domain`, impl dans `data`).
> 3. Crée `LoginScreen` et `RegisterScreen` avec VueModel.
> 4. Gère l'état de connexion (connecté/déconnecté) via un `Flow`.
> 5. Ne touche pas au `MainActivity`. Crée un fichier `integration_pending/auth.md` pour demander l'ajout des dépendances Firebase et des routes de navigation.

---

## Agent 2 : Gamification (XP & Niveaux)
**Prompt :**
> Ton objectif est de créer le moteur de gamification (XP, Niveaux, Badges).
>
> **Tâches :**
> 1. Crée le package `com.example.lexicaandroid2.features.gamification`.
> 2. Crée une entité Room `UserStatsEntity` (XP total, niveau actuel, série de jours).
> 3. Implémente une classe `XPCalculator` (ex: 10 XP par mot appris, 5 XP par révision).
> 4. Crée un composant UI `LevelProgressBar` pour afficher la progression.
> 5. Crée un fichier `integration_pending/gamification.md` pour demander l'ajout de la table dans `AppDatabase`.

---

## Agent 3 : Jeu Orthographe (Vocal)
**Prompt :**
> Ton objectif est de créer un mini-jeu de dictée. Le téléphone prononce un mot (TTS), l'utilisateur l'écrit.
>
> **Tâches :**
> 1. Crée le package `com.example.lexicaandroid2.features.games.spelling`.
> 2. Utilise `Android TextToSpeech` (TTS) pour lire le mot.
> 3. Crée l'écran `SpellingGameScreen` : bouton "Écouter", champ texte, validation.
> 4. Gère le feedback visuel (vert = correct, rouge = incorrect + correction).
> 5. Crée un fichier `integration_pending/spelling_game.md` pour demander l'ajout de la route navigation.

---

## Agent 4 : Recherche Avancée
**Prompt :**
> Ton objectif est de créer un écran de recherche performant pour explorer le dictionnaire local.
>
> **Tâches :**
> 1. Crée le package `com.example.lexicaandroid2.features.search`.
> 2. Implémente une recherche avec `Flow` qui filtre la base de données Room (opérateur `LIKE`).
> 3. Crée `SearchScreen` avec une `SearchBar` Material 3.
> 4. Affiche les résultats dans une liste lazy. Au clic, ouvre le détail du mot.
> 5. Crée un fichier `integration_pending/search.md` pour demander l'ajout de la route.

