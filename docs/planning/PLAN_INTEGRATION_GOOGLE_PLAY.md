# 🛍️ Plan d'intégration Google Play

**Date d'extraction :** 2026-04-08  
**Source initiale :** `DAILY_STANDUP.md` (entrée du 2026-04-07)  
**Objet :** centraliser la préparation de publication Google Play hors du journal quotidien.

---

## P0 — Bloquants avant première soumission

- [ ] **Décider du package final public**
  - confirmer si `com.example.lexicaandroid2` est conservé ou remplacé par un vrai package définitif
  - si changement : réaligner `applicationId`, Firebase, OAuth Google Sign-In, `google-services.json`, SHA release
- [ ] **Publier une vraie politique de confidentialité**
  - héberger une URL publique stable
  - ✅ page statique prête dans `privacy-policy/index.html`
  - ✅ workflow GitHub Pages prêt dans `.github/workflows/privacy-policy-pages.yml`
  - brancher le lien réel dans `SettingsScreen.kt` *(le placeholder UI a été retiré ; URL publique encore à fournir)*
- [ ] **Prévoir la suppression de compte**
  - ajouter un vrai flux de suppression compte/données ou une procédure conforme Play
  - vérifier l'alignement Firebase Auth + données Firestore/locales
- [ ] **Décider de la stratégie de backup Android**
  - ~~confirmer si `allowBackup` reste activé~~ → **P0 repo : désactivé temporairement** (`allowBackup=false`)
  - ✅ `backup_rules.xml` et `data_extraction_rules.xml` remplacés par une exclusion explicite de toutes les données locales
- [ ] **Nettoyer/masquer les traces internes avant prod**
  - ✅ `GamificationDemoScreen` retiré de la navigation publique
  - labels / messages `mode test admin`
  - accès admin si non destiné aux utilisateurs finaux

## P1 — Important avant upload release

- [ ] **Préparer la signature release**
  - keystore d'upload
  - config Play App Signing
- [ ] **Construire et tester un vrai build release/AAB**
  - vérifier login email
  - vérifier Google Sign-In en config release
  - vérifier sync Firestore en build release
- [ ] **Vérifier la version de publication**
  - ajuster `versionCode`
  - ajuster `versionName`
- [ ] **Revalider Firebase pour la release**
  - SHA-1 / SHA-256 release ajoutés
  - provider Google + Email/Password actifs côté console
- [ ] **Faire une passe QA appareil réelle**
  - onboarding/auth
  - review/session
  - mini-jeux
  - reset progression
  - sync cloud/local

## P2 — Polish Play Console / conformité produit

- [ ] **Préparer la fiche Store**
  - nom affiché
  - description courte
  - description longue
  - e-mail/support
- [ ] **Préparer les assets Play Console**
  - icône 512x512
  - screenshots téléphone
  - feature graphic si utilisée
- [ ] **Compléter les formulaires Play**
  - Data safety
  - catégorie app
  - content rating
  - audience cible
- [ ] **Vérifier les textes visibles en production**
  - pas de `Demo`
  - pas de placeholder
  - pas de libellés trop techniques / internes

## Vérifications déjà identifiées dans ce repo

- [~] `SettingsScreen.kt` : UI nettoyée, mais URL publique réelle encore manquante
- [ ] `app/build.gradle.kts` : `applicationId` / `namespace` encore en `com.example.lexicaandroid2`
- [x] `AuthRepository.kt` / auth flow : suppression de compte ajoutée côté app + Firebase/Firestore
- [x] `backup_rules.xml` / `data_extraction_rules.xml` : règles d'exclusion explicites en place
- [x] `LexicaApp.kt` : route `GamificationDemoScreen` retirée

