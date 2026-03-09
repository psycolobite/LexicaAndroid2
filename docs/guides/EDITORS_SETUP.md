# 🖥️ Configuration des Éditeurs pour Agents Multiples

Ce document liste les éditeurs compatibles avec GitHub Copilot pour mettre en place une architecture "Multi-Agents" simulée.

L'objectif est d'avoir plusieurs fenêtres/éditeurs ouverts, chacun représentant un "Agent" spécialisé, pour éviter les conflits de contexte et paralléliser le travail mental.

---

## 🚀 ÉDITEURS RECOMMANDÉS

### 1. Android Studio (Agent Principal - DEV_ANDROID)
L'IDE officiel pour le développement Android. C'est ici que l'agent principal travaille sur le code de l'application.
- **Support Copilot:** Plugin "GitHub Copilot" officiel via Marketplace JetBrains.
- **Usage:** Développement UI (Compose), Logique métier, Gradle.
- **Configuration:** `Settings > Plugins > GitHub Copilot`.

### 2. IntelliJ IDEA (Agent Support - DEV_LIB / DEV_TESTS)
Peut être utilisé en parallèle sur le même projet ou sur des modules isolés.
- **Support Copilot:** Plugin "GitHub Copilot" (même que Android Studio).
- **Usage:** Tests unitaires purs, refactoring de classes logiques (Domain/Data layer), scripts Kotlin.
- **Avantage:** Permet de garder Android Studio pour l'UI et le device mirroring sans surcharge.

### 3. VS Code (Agent Documentation / Scripts - AGENT_ARCHITECT)
Léger et très performant pour tout ce qui n'est pas compilation Android directe.
- **Support Copilot:** Extension officielle très mature + Copilot Chat.
- **Usage:** 
  - Édition de documentation Markdown (`docs/`).
  - Gestion des fichiers `.json`, `.xml` (ressources brutes).
  - Scripts Python ou Bash pour l'automatisation.
- **Avantage:** Contexte de chat souvent plus rapide et interface épurée.

### 4. Fleet (Agent Expérimental - DEV_NEXT)
L'éditeur nouvelle génération de JetBrains.
- **Support Copilot:** Via AI Assistant (parfois différent) ou plugin (vérifier compatibilité actuelle).
- **Usage:** Édition rapide de fichiers distribués.

### 5. Neovim / Vim (Agent Ninja - DEV_OPS)
Pour les modifications rapides en terminal ou sur serveur.
- **Support Copilot:** Plugin `copilot.vim`.
- **Usage:** Corrections rapides, git rebase interactif, édition de fichiers de config.

---

## 🛠️ SETUP "MULTI-AGENTS" PROPOSÉ

Pour simuler une équipe de développement complète :

| Agent | Rôle | Éditeur Recommandé | Tâche Typique |
|-------|------|---------------------|---------------|
| **CHEF D'ORCHESTRE** | Coordination | **VS Code** (fenêtre 1) | Gestion du `TODO.md`, `MASTER_INDEX.md`, Prompts |
| **DEV_CORE** | Features Android | **Android Studio** | Création écrans, ViewModels, Navigation |
| **DEV_GAMES** | Mini-Jeux | **IntelliJ IDEA** (ou AS instance 2) | Logique pure des jeux (classes Kotlin) |
| **DEV_QA** | Tests & Review | **VS Code** (fenêtre 2) | Revue de code, écriture de tests (si possible), Docs |

---

## 🔗 LIENS D'INSTALLATION

- **Android Studio:** [Télécharger](https://developer.android.com/studio)
- **VS Code:** [Télécharger](https://code.visualstudio.com/)
- **IntelliJ IDEA:** [Télécharger](https://www.jetbrains.com/idea/)
- **GitHub Copilot:** [S'abonner](https://github.com/features/copilot)

---

## 📝 NOTES IMPORTANTES

- **Contexte:** Chaque éditeur a son propre contexte Copilot. Ce qui est ouvert dans VS Code n'est pas "vu" par le Copilot d'Android Studio. C'est un **avantage** pour isoler les tâches (separation of concerns).
- **Git:** Tous les agents travaillent sur le même repo Git local. Pensez à commiter ou stasher avant de changer d'agent si vous touchez aux mêmes fichiers (ce qui devrait être évité).

