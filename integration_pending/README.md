# Zone d'Intégration

Ce dossier sert de zone tampon pour les modifications "Cœur" demandées par les agents.
Les agents ne doivent pas modifier directement les fichiers globaux comme `LexicaApp.kt` ou `build.gradle.kts`.
Ils doivent déposer ici un fichier Markdown décrivant les changements requis.

Exemple : `auth_changes.md` contenant "Ajouter la route `composable("login") { ... }` dans `LexicaApp.kt`".
L'Architecte (ou l'humain) se charge ensuite d'appliquer ces changements.

## 📋 Fichiers de PR en attente

### ✅ Complétés (Prêts pour intégration)
- **`gamification_pr.md`** - Système d'XP et niveaux (DEV_PROGRESS) ✅
  - UserStatsEntity, Dao, Repository, ViewModel
  - XpProgressBar composants UI
  - **Statut :** Prêt pour intégration
  
### ⏳ En cours / Planifiés
- `search_pr.md` - Moteur de recherche (DEV_SEARCH)
- `auth_pr.md` - Authentification Firebase (DEV_AUTH)

---

## ✅ Checklist avant merge de chaque PR
- [ ] Documentation complète
- [ ] Code compilable
- [ ] Aucune modification de fichiers cœurs
- [ ] Instructions d'intégration claires
- [ ] Architecture Clean respectée
