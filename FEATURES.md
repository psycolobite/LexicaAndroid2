# ✅ FEATURES - Lexica Android 2 - Master Checklist

**Dernière mise à jour:** 2026-03-16  
**Statut global:** 🟢 En excellente santé — Optimisation Build ✅ Refonte UI Mini-Jeux ✅ Matching v2 ✅ TACHE_09 à 23 ✅

---

## 🏗️ INFRASTRUCTURE & PROCESS

### 🔴 PRIORITÉ IMMÉDIATE
- [x] **Optimisation Build Gradle** (Parallélisme + Cache) ✅
- [x] **Refactoring Navigation** (Screen.kt extrait) ✅
- [x] **Réorganiser la documentation** (Structure `docs/planning`, `docs/archive`, etc.)
- [ ] **Exécuter le script de migration** (`organize.bat` à la racine)
- [ ] **Configurer les éditeurs pour Agents** (VS Code, Android Studio, etc.)
- [ ] **Intégrer Github Copilot** sur chaque instance d'éditeur

---

## 🎮 MINI-JEUX (10 TOTAL)

### ✅ TERMINÉS & UNIFIÉS (10/10)
**Note:** Tous les jeux disposent désormais d'une `GameTopAppBar` unifiée et optimisée (16/03/2026).

- [x] **Jeu de Correspondance (Matching)**
  - [x] MatchingViewModel.kt
  - [x] MatchingScreen.kt
  - [x] **Refonte v2** : Validation globale, règles 3 erreurs, pénalité XP
  - [x] Tests fonctionnels
  - 📁 Location: `presentation/games/matching/`

- [x] **QCM (Questions à Choix Multiples)**
  - [x] QcmViewModel.kt
  - [x] QcmScreen.kt
  - [x] Génération questions
  - [x] Validation réponses
  - [x] Tests fonctionnels
  - 📁 Location: `presentation/games/qcm/`

- [x] **Jeu du Pendu (Hangman)**
  - [x] HangmanViewModel.kt
  - [x] HangmanScreen.kt
  - [x] Logique lettres
  - [x] Animation pendu
  - [x] Tests fonctionnels
  - 📁 Location: `presentation/games/hangman/`

- [x] **Jeu de Dictée (Spelling Game avec TTS)**
  - [x] SpellingGameViewModel.kt
  - [x] SpellingGameScreen.kt
  - [x] TextToSpeech natif Android
  - [x] Écoute du mot
  - [x] Validation saisie
  - [x] Tests fonctionnels
  - 📁 Location: `presentation/games/qcm/` (NB: nommé SpellingGame mais dans package qcm/ pour l'instant)

- [x] **Autres Jeux (6)**
  - [x] Anagrammes (Anagrams)
  - [x] Chrono
  - [x] Memory
  - [x] Définition à Compléter (FillWord)
  - [x] Associations Sémantiques (Semantic)
  - [x] Spelling Avancé (SpellingAdvanced)

---

### ⏳ EN PRIORITÉ 1 (En cours)

- [x] **Fix Recherche** — TACHE_S1 + TACHE_S2 : bug écran blanc, accents et fallback API ✅
- [x] **Refonte UI Mini-Jeux** — Uniformisation TopBar + suppression headers 2026-03-16 ✅
- [x] **Menus & Profil** — TACHE_11 à 13 ✅
- [x] **Défis intégrés dans la révision** — TACHE_14 : défi ortho + sémantique (Jaccard) ✅
- [x] **Validation sémantique TFLite MiniLM** — TACHE_14b : `TFLiteSemanticValidator` + fallback Jaccard ✅
- [x] **Barre "Ajouter un mot" intelligente** — TACHE_15 : UX recherche locale + API + apercu ✅
- ⚠️ **Moteur de révision / présentation des mots** — nouvelle base de vérité définie : 2 questions indépendantes par carte, lot de session initial de 10, priorités par échéance, délais calculés sur la première réponse de session, statut `connu` seulement si les 2 faces dépassent `t4` (voir `docs/specifications/fonctionnement algo délai et présentation cards.md`)
- [ ] **Crash fin de session Review** — session de 20 cartes / fin de lot
- ⚠️ **Audio / TTS global** — service central intégré + premiers branchements, validation runtime restante
- ⚠️ **Mode voiture** — première version intégrée, options avancées et emplacement final à décider
- ⚠️ **Bottom Nav / Réglages UX** — intégrés mais peaufinage visuel encore en cours
- [ ] **Matching rendu graphique avancé**
- [ ] **Utilisation : vrais exercices d'emploi**

---

### ⏳ EN PRIORITÉ 2 (Backlog)

- [x] **Connexion XP aux mini-jeux** — TACHE_10 ✅
- [x] **Daily Challenge** 📅 — TACHE_09 ✅
- [x] **Déverrouillage progressif des jeux par XP** — TACHE_20 ✅
- [ ] **Spelling avancé (phonetique)** — voir TACHE_06 (Spelling Avance)

---
