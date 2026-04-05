# 👨‍💼 Guide du Chef d'Orchestre

**Derniere mise a jour :** 2026-03-15

Ce guide clarifie exactement ce que le Chef d'Orchestre (vous) doit faire et ne pas faire concernant la documentation.

---

## 📊 Responsabilités du Chef d'Orchestre

## TL;DR
- `main` = stable
- `develop` = branche de travail et d'integration
- les agents ne creent pas de branche dediee par defaut
- les agents livrent via leur package + `integration_pending/`
- le Chef d'Orchestre relit, integre dans `develop`, teste, puis fusionne au moment opportun dans `main`
- suivi quotidien uniquement dans `DAILY_STANDUP.md`
- éviter toute nouvelle doc si une doc existante suffit

### 1. **Decoupage & Attribution des Taches**
- Decouper le travail en `TACHE_XX` avec scope ~100 000 tokens
- Attribuer les taches aux agents developpeurs
- Mettre a jour le fichier `docs/guides/CONSIGNES_TACHES.md`
- **Fichier concerne:** `docs/guides/CONSIGNES_TACHES.md` (section "Catalogue des taches")

### 2. **Validation & Fusion des PRs**
- Reviser les fichiers dans `integration_pending/`
- Valider la qualite du code et la coherence avec l'architecture
- Fusionner dans les fichiers coeur (`LexicaApp.kt`, `build.gradle.kts`, etc.)
- Lancer un build de verification apres chaque fusion

### 3. **Surveillance Build**
- Lancer `./gradlew clean :app:assembleDebug` apres chaque modification significative
- Verifier que le build est SUCCESS
- Verifier qu'il n'y a pas de warnings importants

### 4. **Documentation Quotidienne**
- Seul document a maintenir quotidiennement: `DAILY_STANDUP.md`
- Update a la fin de chaque journee de travail

---

## ✅ Documents que VOUS POUVEZ Créer / Modifier

### AUTORISÉ ✅

| Document | Frequence | Utilite | Exemple |
|----------|-----------|---------|---------|
| `DAILY_STANDUP.md` | **Quotidien** | Suivi unique du projet | Jour N: Tache X 70% done, bloquants: ... |
| `docs/guides/CONSIGNES_TACHES.md` | **Quand nouvelle tache** | Definir les taches | Ajouter TACHE_03 |
| `integration_pending/*.md` | **Après fusion** | Documenter les PRs integrees | `auth_pr.md` fusionne ✅ |
| Mises a jour docs existantes | **Si necessaire** | Correction coherence, obsolete marques | Corriger lien cassé dans INDEX_DOCUMENTS.md |
| `docs/obsolete/` | **Hebdomadaire** | Archiver vieux fichiers | Deplacer `etat_2026-02-22.md` |

---

## ❌ Documents que vous NE DEVEZ JAMAIS Créer

### INTERDICTION ❌

| Type | Raison | Alternative |
|------|--------|-------------|
| Fichier `etat_DATE.md` | Doublons, pollution | Utiliser `DAILY_STANDUP.md` |
| `rapport_*.md`, `summary_*.md` | Documents adhoc inutiles | Documenter dans `DAILY_STANDUP.md` |
| Notes personnelles `notes_*.md` | Pollution documentaire | Pas de fichier |
| `retrospective_*.md` | Documents ponctuels | Documenter dans `DAILY_STANDUP.md` |
| Fichiers Markdown "au cas où" | Pas de raison explicite | Evaluer si vraiment necessaire |

---

## 📝 DAILY_STANDUP.md - Format Unique

C'est le **SEUL fichier de suivi quotidien** a maintenir.

### Contenu Recommande

```markdown
## Jour N - Date (ex: 2026-03-04)

### ✅ Accompli Aujourd'hui
- TACHE_01 : Agents commencent l'auth, 20% du code fourni
- Build : ✅ SUCCESS (0 errors, 3 warnings)
- Doc : Verifie coherence INDEX_DOCUMENTS.md

### 🔴 Bloquants
- Firebase key pas encore fournie (en attente client)
- Agent sur TACHE_02 attend clarification sur scope

### 🔜 Demain
- Continuer fusion code TACHE_01 si pret
- Lancer TACHE_02 (corrections warnings)
- Verifier si cle Firebase disponible

### 📊 KPIs
- Build status: ✅ SUCCESS
- Taches actives: 2 (TACHE_01, TACHE_02)
- PRs completees: 0
- PRs en attente: 1
- Warnings: 3
```

---

## 🔄 Processus Quotidien

### Le Matin
- Lire les delivrables des agents (via messages ou PRs)
- Verifier qu'il n'y a pas d'erreurs evidentes

### L'Apres-Midi / Fin de Journee
1. Lancer un build si modification significative
2. Valider et fusionner PRs si pretes
3. **Mettre a jour `DAILY_STANDUP.md`** (OBLIGATOIRE)
4. Verifier coherence doc (liens, index, statuts)

### Hebdomadaire
- [ ] Archiver les vieux fichiers dans `docs/obsolete/`
- [ ] Verifier que `CONSIGNES_TACHES.md` est coherent
- [ ] Verifier que l'index doc (`INDEX_DOCUMENTS.md`) reflète la realite

---

## 🚨 Checklist d'Hygiene Documentaire

**Chaque jour :**
- [ ] A-t-on cree un fichier Markdown qui n'etait pas demande?
  - OUI → A SUPPRIMER ou RENOMMER EN OBSOLETE
  - NON → ✅ Bon
- [ ] `DAILY_STANDUP.md` est-il a jour?
  - OUI → ✅ Bon
  - NON → A METTRE A JOUR MAINTENANT

**Apres chaque integration PR :**
- [ ] Build lance et SUCCESS?
- [ ] Coherence doc verifiee (liens, references)?
- [ ] `DAILY_STANDUP.md` mis a jour?

**Hebdomadaire :**
- [ ] Fichiers inutiles `etat_*.md` ou rapports personnels?
  - OUI → Deplacer dans `docs/obsolete/`
  - NON → ✅ Bon
- [ ] Nombre total de fichiers de suivi > 1?
  - NON (seulement DAILY_STANDUP) → ✅ Parfait
  - OUI → ❌ Pollution, nettoyer

---

## 📚 Recap : Quels Documents Existent & Pourquoi

### Essentiels (A Tenir A Jour)
- ✅ `DAILY_STANDUP.md` - Suivi quotidien unique
- ✅ `docs/guides/CONSIGNES_TACHES.md` - Definition des taches
- ✅ `docs/guides/GUIDELINES.md` - Regles generales (rare modif)

### Reference (Statiques)
- ✅ `START_HERE.md` - Entree agents
- ✅ `FEATURES.md` - Statut features
- ✅ `docs/INDEX_DOCUMENTS.md` - Index navigation
- ✅ `docs/specifications/` - Specs projet (rarement modifiees). Pour l'entraînement / la présentation des mots, la référence courte est dans les docs actives et la référence complète est `docs/specifications/fonctionnement algo délai et présentation cards.md`
- ✅ `docs/planning/` - Backlog, roadmap

### Delivrables Agents (A Fusionner)
- ✅ `integration_pending/*.md` - PRs a integrer

### Archive (Ne Pas Toucher)
- ✅ `docs/obsolete/` - Vieux fichiers (preserver l'historique)
- ✅ `docs/archive/` - Sessions anciennes

---

## 🎯 Exemple de Ce Qu'ON PEUT Faire

**✅ BON :**
```
Jour 1 (2026-03-04):
- Tache 01 : 30% done
- Build : SUCCESS
- Demain : continuer fusion

Jour 2 (2026-03-05):
- Tache 01 : 70% done
- Build : SUCCESS
- Demain : lancer Tache 02
```

**❌ MAUVAIS (Pollution) :**
```
Jour 1 : rapport_jour1.md (pourquoi un fichier?)
Jour 1 : etat_2026-03-04.md (duplication de DAILY_STANDUP)
Jour 1 : notes_chef.md (notes personnelles)
Jour 1 : retrospective_session.md (inutile)
```

---

## 🗂️ Versions Anterieures du Projet

Utiles pour s'inspirer de logiques existantes, recuperer du code ou comprendre l'historique du produit.

| Version | Langage / Stack | Chemin Local |
|---------|----------------|--------------|
| **V1 - FlashcardsApp** | Python | `C:\Users\r0xef\Documents\FlashcardsApp` |
| **V2 - LexicaAndroid** | Kotlin + Jetpack (sans Compose) | `C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid` |
| **V3 - LexicaAndroid2** | Kotlin + Jetpack Compose + Material3 | *(ce projet)* |

### Quand les consulter ?
- Recuperer une **logique metier** deja implementee (algorithme de revision, scoring, etc.)
- Comprendre une **decision d'architecture** passee
- S'inspirer d'une **UI** deja validee
- Verifier si un **bug connu** avait deja ete resolu dans une version precedente

### ⚠️ Attention
- Ne pas copier-coller directement sans adapter au contexte Compose/Material3.
- Les agents developpeurs **n'ont pas acces** a ces chemins locaux, seulement le Chef d'Orchestre.
- Si un agent a besoin d'un extrait de code d'une version anterieure, c'est au Chef d'Orchestre de le fournir.

---

## 💡 Philosophie

**Simple :** Un fait documenté = une seule place  
**Propre :** Pas de fichiers "au cas où"  
**Evolutif :** Ajouter seulement si nécessaire

---

**Fichier :** `GUIDE_CHEF_DORCHESTRE.md`  
**Cree :** 2026-03-04  
**Statut :** 🟢 Reference  
**Maintenance :** Update si nouvelles consignes

