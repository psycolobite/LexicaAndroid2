# ⚠️ Problème: Android Studio + Agents Parallèles = CONFLITS

## 🔴 Le Problème Identifié

Tous les agents travaillent sur **Android Studio ouvert à la même position du projet**.

### Pourquoi c'est problématique:

1. **Cache IDE en mémoire**
   - Android Studio maintient des caches de fichiers en mémoire
   - Modifications simultanées = Versions divergentes
   - Perte de données aléatoire

2. **Race conditions sur les fichiers**
   - Agent A modifie `FlashcardDao.kt` à 14:35
   - Agent B modifie `FlashcardDao.kt` à 14:35:30
   - Lequel gagne? → INDÉFINI

3. **Sync Project Files**
   - Android Studio essaie de "rafraîchir" les fichiers
   - Conflits de fusion automatique = corruption
   - Rebuild échoue pour des raisons incompréhensibles

4. **Gradle Locking**
   - `gradlew :app:assemble` verrouille les ressources
   - Agent A lance le build
   - Agent B tente de modifier → BLOCAGE ou ERREUR

5. **Index Corruption**
   - Index Kotlin peut se corrompre avec édits parallèles
   - Erreurs de compilation fantômes
   - Nettoyer le cache = perte de temps

---

## 📊 Comparaison: Android Studio vs Alternatives

| Critère | Android Studio | VS Code | Sublime Text | JetBrains Fleet |
|---------|---|---|---|---|
| **Cache en mémoire** | ❌ Énorme | ✅ Minimal | ✅ Aucun | ✅ Minimal |
| **Lightweight** | ❌ 2GB RAM+  | ✅ 500MB | ✅ 100MB | ✅ 400MB |
| **Multi-instance** | ❌ Risky | ✅ Safe | ✅ Safe | ✅ Safe |
| **Gradle build** | ✅ Intégré | ⚠️ Via terminal | ⚠️ Via terminal | ✅ Intégré |
| **Kotlin support** | ✅✅✅ | ✅ Good | ⚠️ Basic | ✅✅ Good |
| **Copilot GitHub** | ✅ Oui | ✅ Oui | ✅ Oui | ✅ Oui |
| **Prix** | Free | Free | $99 | Free (Beta) |
| **Pour agents** | ❌ Non | ✅ Idéal | ✅ Idéal | ✅ Idéal |

---

## ✅ SOLUTION RECOMMANDÉE

### **Chaque agent utilise SON propre éditeur**

```
Project LexicaAndroid2/
├── AndroidStudioProject/ (VERSION OFFICIELLE - BUILD SEULEMENT)
├── Agent1_VSCode/ (Copie locale - DEV_PROGRESS)
├── Agent2_Sublime/ (Copie locale - DEV_SEARCH)
├── Agent3_Fleet/ (Copie locale - DEV_AUTH)
└── Agent4_VSCode/ (Copie locale - DEV_GAMES)
```

### **Flux de travail professionnel:**

```
1. Chef d'Orchestre (TOI)
   └─ Clone principal dans AndroidStudio (BUILD SEULEMENT)

2. Chaque Agent
   ├─ Clone sa branche git locale
   ├─ Crée fichiers dans SON éditeur (VS Code, Sublime, etc)
   ├─ Valide avec `./gradlew :app:check` sur sa machine
   └─ Commit ses changements

3. Intégrateur (Toi ou Agent)
   ├─ Pull tous les commits
   ├─ Merge dans main
   ├─ Compile dans Android Studio
   ├─ Teste sur emulateur
   └─ Tag version
```

---

## 🎯 Instructions d'Installation

### **Pour TOI (Chef d'Orchestre) - Android Studio**

Garder 1 instance d'Android Studio pour:
- ✅ Lancer les builds
- ✅ Tester sur émulateur
- ✅ Voir les erreurs de compilation
- ❌ **NE PAS coder** si les agents travaillent

### **Pour Agent 1 (DEV_PROGRESS) - VS Code**

```bash
# 1. Installer VS Code
# https://code.visualstudio.com/download

# 2. Extensions Copilot
# - GitHub Copilot
# - Kotlin Language Server
# - Gradle Language Support

# 3. Clone du projet (branche ou sous-dossier)
git clone <repo> LexicaAndroid2_Dev1
cd LexicaAndroid2_Dev1

# 4. Ouvrir dans VS Code
code .

# 5. Valider la compilation (depuis le terminal VS Code)
./gradlew :app:check
```

### **Pour Agent 2 (DEV_SEARCH) - Sublime Text**

```bash
# 1. Installer Sublime Text 4
# https://www.sublimetext.com/download

# 2. Copier le projet
cp -r LexicaAndroid2 LexicaAndroid2_Dev2
cd LexicaAndroid2_Dev2

# 3. Ouvrir dans Sublime
subl .

# 4. Ouvrir le terminal intégré (Ctrl+` ou Ctrl+Shift+P > Terminal)
./gradlew :app:check
```

### **Pour Agent 3 (DEV_AUTH) - JetBrains Fleet**

```bash
# 1. Installer Fleet (beta)
# https://www.jetbrains.com/help/fleet/getting-started.html

# 2. Clone du projet
git clone <repo> LexicaAndroid2_Dev3
cd LexicaAndroid2_Dev3

# 3. Ouvrir dans Fleet
fleet .

# 4. Outils Kotlin natifs + Terminal intégré
./gradlew :app:check
```

---

## 🔄 Gestion des conflits Git

### **Stratégie 1: Feature Branches (RECOMMANDÉE)**

```bash
# Agent 1 crée sa branche
git checkout -b feature/gamification

# Agent 2 crée sa branche
git checkout -b feature/search

# Chacun travaille indépendamment
# Chef d'orchestre fusionne les branches propres
git merge feature/gamification --no-ff
git merge feature/search --no-ff
```

### **Stratégie 2: Dossiers séparés**

Chaque agent travaille sur des fichiers distincts:
- `Agent1` → `data/local/UserStats*.kt`
- `Agent2` → `presentation/search/*.kt`
- `Agent3` → `features/auth/*.kt`

Risque minimum de conflit.

### **Stratégie 3: PRs avec révision (IDÉAL)**

1. Agent crée un fichier ou branche
2. Crée une PR (Pull Request)
3. Chef d'orchestre revoit le code
4. Merge après validation
5. Merge automatise les conflits mineurs

---

## ⚠️ Règles Strictes à Suivre

### **À FAIRE:**
- ✅ Chaque agent a SON éditeur
- ✅ Chaque agent travaille sur SA branche
- ✅ Commiter régulièrement (1 commit = 1 feature)
- ✅ Chef d'orchestr valide avant merge
- ✅ Build testé après chaque merge

### **À NE PAS FAIRE:**
- ❌ **JAMAIS 2 agents sur Android Studio simultanément**
- ❌ **JAMAIS modifier les mêmes fichiers au même moment**
- ❌ **JAMAIS lancer gradle depuis plusieurs machines en // sur le même projet**
- ❌ **JAMAIS ignorer les conflits Git**
- ❌ **JAMAIS push sans avoir testé la compilation**

---

## 🧪 Validation Avant Merge

**Checklist obligatoire:**

```bash
# 1. Agent: Vérifier localement
cd ~/LexicaAndroid2_Dev1
./gradlew :app:check --refresh-dependencies
# ✅ Aucune erreur

# 2. Agent: Lancer sur émulateur/appareil
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 3. Agent: Vérifier fonctionnalités basiques
# - App se lance
# - Navigation marche
# - Pas de crash au démarrage
# - Feature implémentée fonctionne

# 4. Agent: Commit avec message clair
git add .
git commit -m "feat: Ajouter UserStats et Gamification System"

# 5. Chef d'orchestre: Pull, Merge, Retest
git pull origin main
git merge feature/gamification
./gradlew :app:assembleDebug
# Tester à nouveau
```

---

## 📋 Template pour les Agents

### **À chaque début de session:**

1. **Vérifier la version locale**
   ```bash
   git status
   git log -1 --oneline
   ```

2. **Mettre à jour la branche**
   ```bash
   git fetch origin
   git rebase origin/main
   # Ou si conflit:
   # Résoudre les conflits, puis:
   git rebase --continue
   ```

3. **Vérifier les dépendances**
   ```bash
   ./gradlew --refresh-dependencies
   ```

4. **Lancer le build**
   ```bash
   ./gradlew :app:clean :app:build
   ```

5. **Coder ta feature**
   ```bash
   # Créer les fichiers, éditer, tester
   ```

6. **Tester avant commit**
   ```bash
   ./gradlew :app:assembleDebug
   # Ou sur vraie machine:
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

7. **Commit & Push**
   ```bash
   git add .
   git commit -m "feat: [MON_FEATURE] Description courte"
   git push origin feature/mon-feature
   ```

---

## 🚀 Résumé: Ce qu'il faut faire MAINTENANT

1. **Installer éditeurs** (distribuer aux agents):
   - VS Code (gratuit)
   - Sublime Text (optionnel, $99)
   - JetBrains Fleet (beta gratuit)

2. **Configurer Git** pour chaque agent:
   - Clone locale du repo
   - Créer branches feature individuelles

3. **Tester la première intégration**:
   - Agent 1 fait PR gamification
   - Chef d'orchestre merge
   - Test compil + APK

4. **Documenter le flux**:
   - Ajouter ce guide au README
   - Checklist d'intégration
   - Réunions de sync (quotidienne?)

---

## 📞 Support des Agents

**Si un agent a un problème:**

1. **Erreur de compilation:**
   - Vérifier branche locale vs main
   - `git status`, `git diff main`
   - Rebase si retard: `git rebase origin/main`

2. **Conflit Git:**
   - Utiliser IDE/éditeur pour résoudre
   - Ou: `git mergetool`
   - Tester après résolution

3. **Cache dégradé:**
   - Nettoyer Gradle: `./gradlew clean --refresh-dependencies`
   - Supprimer `.gradle/`: `rm -rf .gradle/`
   - Rebuild complet

4. **Problème d'éditeur:**
   - VS Code: Redémarrer, supprimer `.vscode/`
   - Sublime: Redémarrer simplement
   - Fleet: Invalider cache dans settings

---

**Document créé:** 2026-02-27
**Responsable mise en place:** Chef d'Orchestre (TOI)
**Validation:** Avant de lancer chaque agent


