# Zone d'Intégration

Ce dossier conserve les livraisons agents qui demandent une validation ou un branchement final sur les fichiers cœur.

## État au 2026-03-12

### ✅ Déjà intégrés dans le code principal
- `admin_mode_pr.md`
- `app_icon_v2_pr.md`
- `bottom_nav_pr.md`
- `daily_challenge_pr.md`
- `firebase_diagnostic_pr.md`
- `firebase_sync_pr.md` — branchement code fait, tests multi-appareils encore à faire
- `game_unlock_pr.md`
- `matching_ux_fix_pr.md`
- `profile_pr.md`
- `profile_stats_pr.md`
- `review_challenges_pr.md`
- `review_challenges_tflite_pr.md`
- `settings_pr.md`
- `tests_pr.md`
- `wordlist_actions_pr.md`
- `xp_games_pr.md`

### ⚠️ Livré mais avec action manuelle restante
- `admin_mode_pr.md` : remplacer l'e-mail placeholder dans `AdminConfig.kt`
- `firebase_diagnostic_pr.md` : activer Email/Password et renseigner le SHA-1 debug dans Firebase Console
- `firebase_sync_pr.md` : valider le flux réel sur émulateur/appareil avec deux sessions
- `app_icon_v2_pr.md` / `logo_v4_pr.md` : régénérer les bitmaps legacy uniquement si le support API < 26 reste nécessaire

### ⏳ À garder comme backlog ou référence
- `auth_pr.md`
- `git_init_pr.md`
- `memory_pr.md`
- `semantic_pr.md`
- `spelling_advanced_pr.md`

## Règle d'usage
- Si un fichier décrit une intégration déjà branchée dans `MainActivity.kt`, `LexicaApp.kt`, `AppDatabase.kt` ou `build.gradle.kts`, il sert désormais de référence et non plus de todo actif.
- Après intégration effective, mettre à jour `FEATURES.md` et `DAILY_STANDUP.md` le même jour.
- Garder ici uniquement ce qui aide encore à valider, tester, ou finir une action manuelle.

## Checklist avant fermeture d'une PR agent
- [ ] Build debug OK
- [ ] Flux principal testé manuellement
- [ ] `FEATURES.md` aligné sur l'état réel
- [ ] `DAILY_STANDUP.md` mis à jour
- [ ] Action manuelle restante explicitée si nécessaire
