# Politique de confidentialité — publication GitHub Pages

Ce dossier contient une version statique prête à publier de la politique de confidentialité de `Lexica`.

## Ce qui est déjà prêt

- `index.html` : page publique de politique de confidentialité
- contenu rédigé pour le projet `Lexica`
- compatible avec une publication GitHub Pages

## À compléter avant publication

- confirmer que l'adresse `mottet2.paul6@outlook.fr` est bien l'adresse de support publique à afficher ;
- si nécessaire, remplacer cette adresse directement dans `index.html`.

## Publication automatique via GitHub Pages

Le workflow prévu dans `.github/workflows/privacy-policy-pages.yml` déploie automatiquement ce dossier.
Il se déclenche sur `main` ainsi que sur les branches `integration/**`.

### Étapes externes restantes

1. pousser les changements sur le dépôt distant GitHub ;
2. dans GitHub, ouvrir **Settings** → **Pages** ;
3. choisir **Build and deployment** → **Source: GitHub Actions** ;
4. attendre l'exécution du workflow.

## URL attendue

Le workflow publie directement le contenu du dossier `privacy-policy` à la racine du site GitHub Pages du dépôt.

Si le dépôt GitHub est `LexicaAndroid2`, l'URL finale devrait donc ressembler à :

```text
https://<ton-compte-github>.github.io/LexicaAndroid2/
```

## Après publication

1. ouvrir l'URL publiquement dans un navigateur non connecté ;
2. vérifier que la page s'affiche bien en HTTPS ;
3. utiliser exactement cette URL :
   - dans Google Play Console ;
   - dans l'application via `privacyPolicyUrl`.

