# 🚀 Plan d'Action Immédiat - 2026-02-27

## 📌 STATUT ACTUEL (AVANT INTÉGRATION)

### ✅ Complété par les Agents
- **DEV_PROGRESS:** Gamification system (PR en `integration_pending/gamification_pr.md`)
- **DEV_SEARCH:** Module recherche (PR en `integration_pending/search_pr.md`)
- **DEV_AUTH:** À vérifier

### ❌ Non intégré au code
- 0 fichier `.kt` créé réellement
- Seulement des documentations + instructions

### 🔴 Problème critique
- **Tous les agents sur Android Studio** = Conflits probables
- Besoin de restructurer le workflow

---

## 📋 ACTIONS À FAIRE AUJOURD'HUI (Priorité décroissante)

### **ACTION 1: Arrêter les agents (15 min)** 🛑

```
Message à envoyer à chaque agent:

"STOP. Fermez Android Studio.
Ne tochez PLUS aux fichiers du projet jusqu'à nouvel ordre.
Nous restructurons le workflow.

Instructions complètes seront données demain matin.
Voir: /docs/SETUP_AGENTS_PARALLEL.md"
```

### **ACTION 2: Documenter l'état complet (30 min)** 📊

- ✅ Créé: `ETAT_AGENTS_2026-02-27.md` (Ce fichier est prêt)
- ✅ Créé: `MINI_GAMES_BACKLOG.md` (Liste complète des jeux)
- ✅ Créé: `SETUP_AGENTS_PARALLEL.md` (Nouveau workflow)
- ⏳ À faire: Mettre à jour `PROMPTS_AGENTS.md` avec nouveau workflow

### **ACTION 3: Corriger le problème Android Studio (1h)** 🔧

**Objectif:** Avoir une instance Android Studio propre pour les builds uniquement.

#### Étape 1: Nettoyer le projet
```bash
cd C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2

# Nettoyer les caches
./gradlew clean --refresh-dependencies
Remove-Item -Recurse -Force .gradle
Remove-Item -Recurse -Force app/build
Remove-Item -Recurse -Force build

# Vérifier l'absence d'erreurs
./gradlew :app:assembleDebug 2>&1 | Tee build_clean.log
```

#### Étape 2: Ouvrir Android Studio frais
```bash
# Fermer TOUTES les instances
# Supprimer cache Android Studio
Remove-Item -Recurse -Force "$env:USERPROFILE\.android"
Remove-Item -Recurse -Force "$env:USERPROFILE\.AndroidStudio*"

# Relancer
Start-Process "C:\Program Files\Android\Android Studio\bin\studio64.exe" -ArgumentList "C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2"
```

#### Étape 3: Vérifier le build
- File → Project Structure → Modules → app
- Vérifier SDK version (API 34+)
- Gradle: `./gradlew :app:check`

### **ACTION 4: Créer la structure de branches Git (30 min)** 🌳

```bash
cd C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2

# 1. Vérifier branche actuelle
git status
git log -1 --oneline

# 2. Créer branche main propre (si besoin)
git checkout -b main-clean
git pull origin main

# 3. Créer branche de dev pour l'intégration
git checkout -b develop

# 4. Créer branches pour chaque agent
git checkout -b feature/gamification-integration
git checkout -b feature/search-integration
git checkout -b feature/auth
git checkout -b feature/games

# 5. Vérifier les branches
git branch -a
```

### **ACTION 5: Télécharger et distribuer les éditeurs (30 min)** 💾

**Pour chaque agent, envoyer lien de téléchargement:**

Agent 1 (DEV_PROGRESS):
```
VS Code: https://code.visualstudio.com/download
Extensions: GitHub Copilot, Kotlin Language Server
```

Agent 2 (DEV_SEARCH):
```
Sublime Text 4: https://www.sublimetext.com/download
OU VS Code (même que Agent 1)
```

Agent 3 (DEV_AUTH):
```
JetBrains Fleet: https://www.jetbrains.com/help/fleet/getting-started.html
OU VS Code
```

---

## 🔄 DEMAIN MATIN: Réintégrer les Agents (2-3h)

### **ÉTAPE 1: Créer les fichiers de DEV_PROGRESS** ✅

**Source:** `integration_pending/gamification_pr.md`

**Fichiers à créer:**
```
app/src/main/java/com/example/lexicaandroid2/

data/local/
├── UserStatsEntity.kt          [50 lignes]
└── UserStatsDao.kt             [80 lignes]

data/repository/
├── UserStatsRepository.kt       [30 lignes - interface]
├── UserStatsRepositoryImpl.kt    [60 lignes - impl]
└── GamificationManager.kt       [100 lignes - logique]

presentation/review/
└── UserStatsViewModel.kt        [80 lignes]

presentation/common/
├── XpProgressBar.kt            [120 lignes]
└── XpProgressBarCompact.kt      [80 lignes]

data/local/LexicaDatabase.kt     [MODIFIER - Ajouter UserStats]
```

**Exécution:**
1. Copier le code depuis `gamification_pr.md`
2. Créer les fichiers
3. Modifier `LexicaDatabase` (version 4)
4. Compiler: `./gradlew :app:assembleDebug`
5. Vérifier: Pas d'erreur

**Temps estimé:** 30 min

### **ÉTAPE 2: Créer les fichiers de DEV_SEARCH** ✅

**Source:** `integration_pending/search_pr.md`

**Fichiers à créer:**
```
app/src/main/java/com/example/lexicaandroid2/

domain/repository/
└── SearchRepository.kt          [40 lignes]

data/repository/
└── SearchRepositoryImpl.kt       [100 lignes]

presentation/search/
├── SearchScreen.kt             [200 lignes]
├── SearchViewModel.kt          [120 lignes]
└── SearchNavigation.kt         [30 lignes - optionnel]

data/local/FlashcardDao.kt       [MODIFIER - Ajouter 6 requêtes]
```

**Exécution:**
1. Copier le code depuis `search_pr.md`
2. Créer les fichiers search
3. Ajouter les requêtes SQL à `FlashcardDao.kt`
4. Compiler: `./gradlew :app:assembleDebug`
5. Vérifier: Pas d'erreur

**Temps estimé:** 45 min

### **ÉTAPE 3: Intégrer à LexicaApp.kt** ⚙️

**Modifier:** `presentation/LexicaApp.kt`

```kotlin
// Ajouter au sealed class Screen
data object Search : Screen("search")

// Ajouter au topBarTitle when
Screen.Search.route -> "Recherche"

// Ajouter au canNavigateBack
currentRoute == Screen.Search.route

// Ajouter à NavHost
composable(Screen.Search.route) {
    SearchScreen(
        viewModel = searchViewModel,
        onCardClick = { /* TODO */ },
        onAddToReview = { /* TODO */ }
    )
}
```

**Temps estimé:** 15 min

### **ÉTAPE 4: Tester la compilation complète** 🧪

```bash
./gradlew clean build --refresh-dependencies
```

**Résultat attendu:**
```
BUILD SUCCESSFUL in Xs
```

**Si erreur:**
1. Lire le log d'erreur
2. Vérifier les imports manquants
3. Vérifier les syntaxes Kotlin
4. Recompiler

**Temps estimé:** 15-30 min (selon erreurs)

### **ÉTAPE 5: Tester sur émulateur/appareil** 📱

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Tests à faire:**
- [ ] App se lance
- [ ] Dashboard affiche l'XP bar
- [ ] Navigation vers Recherche marche
- [ ] Recherche fonctionne (chercher un mot)
- [ ] Pas de crash
- [ ] Pas d'exception logcat

**Temps estimé:** 15 min

### **ÉTAPE 6: Commit et Merge** 🔀

```bash
# Branch gamification
git checkout feature/gamification-integration
git add .
git commit -m "feat: Gamification system (XP, Niveaux, Streak)"
git push origin feature/gamification-integration

# Branch search
git checkout feature/search-integration
git add .
git commit -m "feat: Search module (global, by word, by definition)"
git push origin feature/search-integration

# Merge to main
git checkout main
git merge feature/gamification-integration
git merge feature/search-integration
git push origin main
```

**Temps estimé:** 10 min

---

## 🎯 PROCHAINS JOURS: Réorganiser les Agents

### **JOUR 2-3: Nouvelle organisation**

**Créer document:** `AGENT_ASSIGNMENTS_v2.md`

```
Agent 1: DEV_GAMES_ANAGRAMS
  Éditeur: VS Code
  Branche: feature/games-anagrams
  PR: integration_pending/games_anagrams_pr.md
  Token budget: 100k
  
Agent 2: DEV_GAMES_CHRONO
  Éditeur: Sublime Text
  Branche: feature/games-chrono
  PR: integration_pending/games_chrono_pr.md
  Token budget: 100k
  
Agent 3: DEV_GAMES_MEMORY
  Éditeur: JetBrains Fleet
  Branche: feature/games-memory
  PR: integration_pending/games_memory_pr.md
  Token budget: 100k
  
Agent 4: DEV_AUTH (Firebase)
  Éditeur: VS Code
  Branche: feature/auth-firebase
  PR: integration_pending/auth_firebase_pr.md
  Token budget: 150k
```

### **Distribuer à chaque agent:**

1. Clone du projet
2. Lien vers éditeur recommandé
3. Lien vers sa PR à lire (`integration_pending/[task]_pr.md`)
4. Instructions du document `SETUP_AGENTS_PARALLEL.md`
5. Son dossier de travail dédié

---

## 📊 Tableau Récapitulatif

| Tâche | Qui | Durée | Avant | Après |
|-------|-----|-------|-------|-------|
| **Arrêter agents** | Toi | 15 min | 3 agents actifs | 0 agents |
| **Nettoyer gradle** | Toi | 30 min | Cache corrompu | Cache clean |
| **Créer branches** | Toi | 15 min | Main uniquement | Branches feature |
| **Intégrer gamification** | Toi ou Bot | 30 min | 0 fichiers | 8 fichiers ✅ |
| **Intégrer search** | Toi ou Bot | 45 min | 0 fichiers | 5 fichiers ✅ |
| **Intégrer à LexicaApp** | Toi | 15 min | Routes manquantes | Routes + composables |
| **Compiler & tester** | Toi | 30 min | Incertain | ✅ BUILD SUCCESS |
| **Commit et merge** | Toi | 10 min | 2 branches | main mise à jour |
| **Total** | Toi | **3-4h** | État chaos | État professionnel |

---

## ⚠️ Risques et Mitigations

| Risque | Probabilité | Impact | Mitigation |
|--------|------------|--------|-----------|
| Erreurs syntax Kotlin | Moyenne | Compilation échoue | Vérifier avant commit |
| Imports manquants | Haute | `unresolved reference` | Utiliser IDE autocomplete |
| Conflits Git | Basse | Merge bloqué | Feature branches isolées |
| Cache Gradle dégradé | Moyenne | Build lent/échoue | `./gradlew clean` |
| Android Studio instable | Basse | IDE crash | Redémarrer frais |
| Test échoue sur device | Moyenne | Feature non testée | Test sur émulateur d'abord |

---

## ✅ Checklist Avant Lancer Dev Round 2

- [ ] Tous les agents ont compris le nouveau workflow
- [ ] Éditeurs installés et configurés (1 par agent)
- [ ] Branches Git créées et accessibles
- [ ] Build propre compilé (`BUILD SUCCESSFUL`)
- [ ] APK lancé sans crash sur émulateur
- [ ] Gamification + Search intégrés et testés
- [ ] Documentation mise à jour (`AGENT_ASSIGNMENTS_v2.md`)
- [ ] Commits poussés et mergés

---

## 📞 Support Immédiat

**Si problème:**
1. Lire le log d'erreur complètement
2. Chercher solution dans `docs/TROUBLESHOOTING.md` (à créer)
3. Demander aide avec:
   - Output complet de l'erreur
   - `git log -5 --oneline`
   - `./gradlew --version`
   - `java -version`

---

**Plan créé:** 2026-02-27 22:30
**Responsable:** Toi (Chef d'orchestre)
**Prochaine review:** Demain à 09:00
**Status:** 🟡 En attente d'exécution


