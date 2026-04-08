# 📚 Index Complet de la Documentation

**Derniere mise a jour :** 2026-04-08  
**Statut :** 🟢 Actif & Complet

Ce fichier fusionne l'ancien `README_NAVIGATION.md` et `INDEX_DOCUMENTS.md` pour eviter les doublons.

---

## 🗺️ Navigation Rapide par Role

### 👨‍💼 **Chef d'Orchestre**

**Lecture prioritaire (dans cet ordre) :**

1. **[../START_HERE.md](../START_HERE.md)** (5 min) - Point de départ + tes devoirs résumés
2. **[../FEATURES.md](../FEATURES.md)** (5 min) - État du projet
3. **[specifications/BRIEF_EXECUTIF.md](specifications/BRIEF_EXECUTIF.md)** (5 min) - Vision
4. **[planning/PLAN_ACTION_IMMEDIATE.md](planning/PLAN_ACTION_IMMEDIATE.md)** (10 min) - Actions à faire
5. **[../docs/guides/CONSIGNES_TACHES.md](../docs/guides/CONSIGNES_TACHES.md)** (15 min) - Section "Consignes pour le Chef d'Orchestre" = TES DEVOIRS COMPLETS

**Suivi quotidien :**
- **[../DAILY_STANDUP.md](../DAILY_STANDUP.md)** - Journal unique de suivi (c'est le SEUL fichier à tenir à jour chaque jour!)

**Si problème :**
- **[guides/TROUBLESHOOTING.md](guides/TROUBLESHOOTING.md)** (à garder en favoris)

---

### 👨‍💻 **Agent Developpeur**

**Lecture obligatoire (dans cet ordre) :**

1. **[../START_HERE.md](../START_HERE.md)** (3 min) - Point de depart
2. **[guides/SETUP_AGENTS_PARALLEL.md](guides/SETUP_AGENTS_PARALLEL.md)** (30 min) ⭐ **CRITIQUE**
3. **[guides/GUIDELINES.md](guides/GUIDELINES.md)** (15 min) - Regles globales, workflow, interdictions
4. **[guides/CONSIGNES_TACHES.md](guides/CONSIGNES_TACHES.md)** (5 min) - Trouver ta tache `TACHE_XX`
5. **[guides/TROUBLESHOOTING.md](guides/TROUBLESHOOTING.md)** (a garder en favoris) - Solutions erreurs

⚠️ **Rappel agents :**
- Pas de build local
- Pas de modification des fichiers coeur (`LexicaApp.kt`, `build.gradle.kts`, `AndroidManifest.xml`, `AppDatabase.kt`)
- Pas de modification des fichiers `.txt`

**Livraison :**
- Format : voir `../integration_pending/README.md`
- Standards code : voir `guides/GUIDELINES.md`

---

### 🎮 **Mini-Jeux**

- **[planning/MINI_GAMES_BACKLOG.md](planning/MINI_GAMES_BACKLOG.md)** - Liste complete des jeux (10 total)
- **[../FEATURES.md](../FEATURES.md)** - Section MINI-JEUX

---

## 📂 Structure des Documents

### 📁 `/docs/specifications/`
Definition du projet.
- `BRIEF_EXECUTIF.md` - Vue executif (5 min)
- `SYNTHESE_COMPLETE_2026-02-27.md` - Vue complete (25 min)
- `fonctionnement algo délai et présentation cards.md` - Référence complète pour l'algo de délai, la logique de session et la présentation des faces
- `operationalisation algo délai et présentation cards.md` - Traduction de la spec en plan technique concret (Polo-1)

### 📁 `/docs/planning/`
Ce qu'on fait.
- `PLAN_ACTION_IMMEDIATE.md` - Actions prioritaires
- `PLAN_INTEGRATION_GOOGLE_PLAY.md` - Checklist dédiée de préparation publication Google Play
- `BACKLOG.md` - Taches globales
- `MINI_GAMES_BACKLOG.md` - Backlog jeux (10 jeux)

### 📁 `/docs/guides/`
Comment on le fait.
- `GUIDELINES.md` - Workflow, roles, standards code
- `SETUP_AGENTS_PARALLEL.md` - Configuration environnement agents
- `CONSIGNES_TACHES.md` - Catalogue des taches a effectuer
- `TROUBLESHOOTING.md` - Solutions aux erreurs (20+)

### 📁 `/docs/status/`
Ou on en est.
- `etat_2026-02-27.md` - Dernier etat date (garder seulement le plus recent)

### 📁 `/docs/archive/`
Historique ancien.
- `FILES_CREATED.md`, `SESSION_SUMMARY.md`, anciens etats, etc.

### 📁 `/docs/obsolete/`
Fichiers obsoletes (archives techniques).
- `OBSOLETE_PROMPTS_AGENTS.md` - Remplace par `CONSIGNES_TACHES.md`
- `OBSOLETE_README_NAVIGATION.md` - Fusionne dans `INDEX_DOCUMENTS.md`
- `OBSOLETE_INDEX_DOCUMENTS_old.md` - Ancienne version avant fusion

---

## 🏗️ Architecture Code — Modules Clés

### 📁 `presentation/review/` — Écran d'entraînement (7 fichiers)

Refactorisé le 2026-04-05 depuis un monolithe de ~1700 lignes :

| Fichier | Responsabilité |
|---------|---------------|
| `ReviewScreen.kt` | Orchestrateur lean (~300 lignes) : Scaffold, navigation entre états |
| `ReviewModels.kt` | Data classes (`ReviewCardDisplay`, `ConfettiPiece`, `ReviewButtonColors`) + utilitaires (`decodeReviewText`, `buildReviewCardDisplay`) |
| `ReviewSharedComponents.kt` | Composables partagés : `ReviewHeader`, `ReviewContextHint`, `AudioTextLine`, `GradeButton`, `EmptyReviewState`, etc. |
| `NormalQuestionContent.kt` | Contenu question normale : flip recto/verso, contrôles fixes bas d'écran |
| `OrthographicContent.kt` | Contenu orthographique : question ortho (PASSER+VALIDER) + défis (VALIDER seul) |
| `EventContent.kt` | QCM (`MultipleChoiceEventContent`) + Matching (`MatchingEventContent`) |
| `SessionCelebration.kt` | Célébration fin de session + confettis |

### 📁 `domain/logic/` — Moteur de session Polo-1

| Fichier | Responsabilité |
|---------|---------------|
| `ReviewIntervalEngine.kt` | Calcul des délais de révision (ratio maîtrise, éligibilité) |
| `ReviewSessionPlanner.kt` | Planification d'une session : ordre global, priorités, anti-jumelles |
| `ReviewSessionEngine.kt` | Moteur de validation locale : règles de progression par question |

---

## 📄 Fichiers a la Racine

| Fichier | Usage | Lecteur |
|---------|-------|---------|
| `START_HERE.md` | Point de depart | Tous |
| `FEATURES.md` | Etat des features | Tous |
| `MASTER_INDEX.md` | Index executif | Tous |
| `DAILY_STANDUP.md` | Journal quotidien | Chef d'orchestre |
| `CONTRIBUTING.md` | Guide contribution | Agents |
| `CHANGELOG.md` | Historique projet | Tous |

---

## 🎯 Quel Document pour Quel Besoin ?

| Besoin | Document | Duree |
|--------|----------|-------|
| Debuter le projet | `START_HERE.md` → `BRIEF_EXECUTIF.md` | 10 min |
| Integrer des PRs | `PLAN_ACTION_IMMEDIATE.md` | 3-4h |
| Debugger une erreur | `TROUBLESHOOTING.md` | 5-30 min |
| Configurer mon editeur | `SETUP_AGENTS_PARALLEL.md` | 30 min |
| Trouver ma tache | `CONSIGNES_TACHES.md` | 5 min |
| Implementer un mini-jeu | `MINI_GAMES_BACKLOG.md` | 15 min |
| Standards de code | `GUIDELINES.md` | 15 min |
| Etat global du projet | `FEATURES.md` | 5 min |
| Vue complete | `SYNTHESE_COMPLETE_2026-02-27.md` | 25 min |

---

## 📊 Matrice Agents (actuelle)

| Role | Tache | Document consignes | Status | Editeur |
|------|-------|-------------------|--------|---------|
| DEV_PROGRESS | Gamification | `CONSIGNES_TACHES.md` | ⏳ A definir | VS Code |
| DEV_SEARCH | Recherche | `CONSIGNES_TACHES.md` | ⏳ A definir | Sublime |
| DEV_AUTH | Firebase Auth | `CONSIGNES_TACHES.md` | ⏳ A definir | Fleet |
| DEV_GAMES | Mini-jeux | `MINI_GAMES_BACKLOG.md` | ⏳ A definir | VS Code |

---

## 🚀 Checklist de Demarrage

### Pour Chef d'Orchestre
- [ ] Lire `START_HERE.md` (3 min)
- [ ] Lire `FEATURES.md` (5 min)
- [ ] Lire `BRIEF_EXECUTIF.md` (5 min)
- [ ] Parcourir `PLAN_ACTION_IMMEDIATE.md` (10 min)
- [ ] Mettre `TROUBLESHOOTING.md` en favoris

### Pour Agent Developpeur
- [ ] Lire `START_HERE.md` (3 min)
- [ ] Lire `SETUP_AGENTS_PARALLEL.md` (30 min) ⭐ **COMPLET**
- [ ] Lire `GUIDELINES.md` (15 min)
- [ ] Trouver ta tache dans `CONSIGNES_TACHES.md` (5 min)
- [ ] Lire `../integration_pending/README.md` (5 min)
- [ ] Mettre `TROUBLESHOOTING.md` en favoris

---

## 💡 Astuces Efficacite

- 📌 Bookmark `PLAN_ACTION_IMMEDIATE.md` (chef d'orchestre)
- 📌 Bookmark `TROUBLESHOOTING.md` (tous)
- 📌 Pin `FEATURES.md` dans ton IDE
- 📧 Partage `START_HERE.md` avec nouveaux membres
- 💾 Backup quotidien du repo
- 📝 Mets a jour `DAILY_STANDUP.md` chaque jour

---

## 🔗 Dependances Entre Documents

```
START_HERE.md
├── FEATURES.md
├── BRIEF_EXECUTIF.md
├── SETUP_AGENTS_PARALLEL.md
│   ├── CONSIGNES_TACHES.md
│   ├── GUIDELINES.md
│   └── TROUBLESHOOTING.md
└── INDEX_DOCUMENTS.md (ce fichier)

PLAN_ACTION_IMMEDIATE.md
├── integration_pending/
└── TROUBLESHOOTING.md
```

---

## 📈 Statistiques Documentation

| Type | Nombre | Exemples |
|------|--------|----------|
| Guides actifs | 4 | `GUIDELINES.md`, `CONSIGNES_TACHES.md`, etc. |
| Specifications | 4 | `BRIEF_EXECUTIF.md`, `SYNTHESE_COMPLETE...`, algo délai, opérationnalisation |
| Planning | 3 | `PLAN_ACTION_IMMEDIATE.md`, `BACKLOG.md`, etc. |
| Status | 1 | `etat_2026-02-27.md` (dernier seulement) |
| Architecture code | 10+ | Review (7 fichiers), Polo-1 engine (3 fichiers) |
| Archives | Variable | Anciens etats, sessions, etc. |
| Obsoletes | 3+ | `OBSOLETE_PROMPTS_AGENTS.md`, etc. |

---

## ✅ Verification Index

- [ ] Tu peux lire ce fichier (`INDEX_DOCUMENTS.md`) ✅
- [ ] Tu peux trouver `START_HERE.md` ✅
- [ ] Tu peux trouver `/docs/guides/` ✅
- [ ] Tu peux trouver `integration_pending/` ✅
- [ ] Build system fonctionne (`./gradlew --version`) ✅
- [ ] Git fonctionne (`git status`) ✅

Si tout est ✅: **Tu es pret !**

---

**Fichier :** `INDEX_DOCUMENTS.md`  
**Cree :** 2026-02-27  
**Fusionne :** `README_NAVIGATION.md` + ancien `INDEX_DOCUMENTS.md`  
**Derniere mise a jour :** 2026-04-05  
**Statut :** 🟢 Actif  
**Maintenance :** Mise a jour apres chaque changement structure doc

