# Directives de l'Equipe & Workflow

## Roles
- **Chef de Projet (Toi)** : Fournit la vision, les ressources (fichiers, cles API) et valide les fonctionnalites.
- **Architecte/Lead (Moi/Copilot)** : Orchestre le projet, decoupe les taches, gere le backlog, effectue les fusions critiques (Merge) et garantit la stabilite.
- **Developpeurs (Agents Subordonnes - Gemini, Claude, GPT)** : Executent des taches specifiques et isolees (codage, tests unitaires) sans build.

## Gouvernance (mode open source)
- Le Chef d'Orchestre joue le role de mainteneur.
- Les agents jouent le role de contributeurs.
- Les integrations globales passent par demande d'integration puis revue.

## Workflow de Synchronisation (CRITIQUE)
Pour eviter les conflits de fichiers (ex: plusieurs agents modifiant `LexicaApp.kt` ou `AndroidManifest.xml` en meme temps) :

1. **Decoupage par taches** : Le Chef d'Orchestre attribue des taches `TACHE_XX` (scope cible ~100 000 tokens).
2. **Isolation des Packages** : Chaque agent travaille dans un package dedie a sa tache.
3. **Interdiction de modifier le Coeur** : Les agents **NE DOIVENT PAS** modifier directement :
   - `LexicaApp.kt` (Navigation globale)
   - `AndroidManifest.xml`
   - `build.gradle.kts`
   - `AppDatabase.kt`
4. **Interdiction sur les fichiers `.txt`** : Les agents ne modifient aucun fichier `.txt` (reserves au responsable du projet).
5. **Interdiction de build pour les agents developpeurs** : seul le Chef d'Orchestre (ou son aide) lance les builds de verification.
6. **Demandes d'Integration (Pull Request Manuelle)** :
   - Si un agent doit declarer un nouvel ecran, une dependance ou une table, il cree un fichier Markdown dans `integration_pending/`.
   - Ce fichier contient les changements globaux proposes.
   - **L'Architecte** applique ces modifications globales de maniere sure.

## Role de surveillance (Chef d'Orchestre / Architecte)
- Verifier qu'un build est lance apres chaque modification significative.
- Verifier que la documentation est a jour apres chaque integration.
- Verifier la coherence interne de la documentation (liens, index, statut, doublons).

## Standards de Code
- **Langage** : Kotlin.
- **UI** : Jetpack Compose (Material3).
- **Architecture** : MVVM + Clean Architecture (Domain / Data / Presentation).
- **Injection** : Hilt (si configure) ou Manuelle (Koin/Singleton).
- **Async** : Coroutines & Flow.

## Gestion des Tokens
- Les taches sont dimensionnees autour de ~100 000 tokens.
- Les prompts restent courts, avec contexte minimal utile.
