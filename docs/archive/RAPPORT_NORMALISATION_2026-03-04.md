# Rapport de Normalisation Documentation - 2026-03-04

## ✅ Actions Executees

### 1. Creation du dossier obsolete
- **Cree:** `docs/obsolete/`
- **Utilite:** Regrouper tous les fichiers obsoletes sans les supprimer

### 2. Fichiers deplaces vers obsolete

| Fichier Source | Nouveau Nom | Raison |
|----------------|-------------|--------|
| `docs/guides/PROMPTS_AGENTS.md` | `docs/obsolete/OBSOLETE_PROMPTS_AGENTS.md` | Remplace par `CONSIGNES_TACHES.md` |
| `docs/guides/README_NAVIGATION.md` | `docs/obsolete/OBSOLETE_README_NAVIGATION.md` | Fusionne dans `INDEX_DOCUMENTS.md` |
| `docs/INDEX_DOCUMENTS.md` (ancien) | `docs/obsolete/OBSOLETE_INDEX_DOCUMENTS_old.md` | Remplace par version fusionnee |
| `docs/archive/etat_2026-02-22_1916.md` | `docs/obsolete/OBSOLETE_etat_2026-02-22_1916.md` | Garde seulement dernier etat |
| `docs/archive/etat2026-02-27-15h43.md` | `docs/obsolete/OBSOLETE_etat2026-02-27-15h43.md` | Garde seulement dernier etat |

### 3. Nouveau fichier cree
- **`docs/guides/CONSIGNES_TACHES.md`**
  - Remplace `PROMPTS_AGENTS.md`
  - Organisation par taches (`TACHE_XX`) plutot que par agent
  - Scope cible: ~100 000 tokens par tache
  - Ajout regle 8: signalement incoherences doc
  - Ajout regle 9: propositions ameliorations (optionnel)

### 4. Fichier fusionne cree
- **`docs/INDEX_DOCUMENTS.md`** (nouvelle version)
  - Fusionne `README_NAVIGATION.md` + ancien `INDEX_DOCUMENTS.md`
  - Elimine les doublons
  - Navigation claire par role
  - Reference `CONSIGNES_TACHES.md` au lieu de `PROMPTS_AGENTS.md`

### 5. Fichiers mis a jour

#### `START_HERE.md`
- Parcours agent mis a jour vers `CONSIGNES_TACHES.md`
- Ajout rappel: pas de build, pas de fichiers coeur, pas de `.txt`
- Retire doublon lien `CONTRIBUTING.md`

#### `MASTER_INDEX.md`
- Reference `CONSIGNES_TACHES.md` au lieu de `PROMPTS_AGENTS.md`
- Section guides mise a jour
- Map de navigation corrigee
- Sections QUICK FACTS et NEXT ACTIONS reduites (doublons enleves)
- Date de mise a jour ajoutee

#### `docs/guides/GUIDELINES.md`
- Ajout gouvernance mode open source
- Decoupage par taches ~100k tokens
- Interdiction build pour agents developpeurs
- Interdiction fichiers `.txt`
- Role de surveillance chef d'orchestre (build post-modif, coherence doc)

### 6. Etat des fichiers status/archive
- **Garde:** `docs/status/etat_2026-02-27.md` (dernier etat)
- **Deplaces vers obsolete:** anciens etats dates

---

## 📊 Statut Final

### Documents Actifs (docs/guides/)
- ✅ `GUIDELINES.md` - Roles, workflow, standards
- ✅ `CONSIGNES_TACHES.md` - Catalogue taches (nouveau)
- ✅ `SETUP_AGENTS_PARALLEL.md` - Config environnement
- ✅ `TROUBLESHOOTING.md` - Solutions erreurs

### Documents Obsoletes (docs/obsolete/)
- 📦 `OBSOLETE_PROMPTS_AGENTS.md`
- 📦 `OBSOLETE_README_NAVIGATION.md`
- 📦 `OBSOLETE_INDEX_DOCUMENTS_old.md`
- 📦 `OBSOLETE_etat_2026-02-22_1916.md`
- 📦 `OBSOLETE_etat2026-02-27-15h43.md`

### Index et Navigation
- ✅ `docs/INDEX_DOCUMENTS.md` - Version fusionnee unifiee
- ✅ `MASTER_INDEX.md` - Harmonise avec nouvelle structure
- ✅ `START_HERE.md` - Parcours agents mis a jour

---

## 🎯 Changements Cles

### Pour les Agents Developpeurs
1. Nouveau fichier de reference: `docs/guides/CONSIGNES_TACHES.md`
2. Organisation par taches numerotees (`TACHE_01`, `TACHE_02`, etc.)
3. Scope cible par tache: ~100 000 tokens
4. Interdiction explicite de build local
5. Interdiction explicite fichiers `.txt`
6. Obligation signaler incoherences doc
7. Possibilite proposer ameliorations (optionnel)

### Pour le Chef d'Orchestre
1. Role de surveillance ajoute explicitement:
   - Build apres chaque modif significative
   - Verification coherence doc
   - Verification doc a jour
2. Gouvernance type open source (mainteneur vs contributeurs)
3. Navigation simplifiee (moins de doublons)

---

## ✅ Verification Coherence

- [x] Tous les liens vers `PROMPTS_AGENTS.md` dans docs actifs corriges
- [x] `START_HERE.md` parcours agent coherent
- [x] `MASTER_INDEX.md` references a jour
- [x] `INDEX_DOCUMENTS.md` unifie et a jour
- [x] Anciens fichiers preserves dans `obsolete/` (pas supprimes)
- [x] Dernier etat `etat_2026-02-27.md` conserve dans `status/`

---

## 🚀 Prochaines Etapes Recommandees

1. Relire `START_HERE.md` pour valider parcours
2. Verifier que `docs/INDEX_DOCUMENTS.md` repond a tous les besoins
3. Commencer a definir les taches dans `CONSIGNES_TACHES.md` (section "Catalogue des taches")
4. Mettre a jour `DAILY_STANDUP.md` avec cette normalisation

---

**Rapport genere:** 2026-03-04  
**Normalisation:** Complete ✅  
**Fichiers deplaces (non supprimes):** 5  
**Fichiers crees/mis a jour:** 6  
**Coherence doc:** Verifiee ✅

