# PR LOGO_V4 — Refonte Icône Launcher "L'Éclat du Savoir"

## Résumé

Remplacement de l'icône launcher v3 (deux cartes empilées + L violet) par un **livre ouvert avec étoile dorée**, plus lisible et plus cohérent avec l'identité d'une app de vocabulaire.

---

## Fichiers modifiés (déjà appliqués)

| Fichier | Changement |
|---------|-----------|
| `app/src/main/res/drawable/ic_launcher_foreground.xml` | ✅ Remplacé — livre ouvert + étoile |
| `app/src/main/res/drawable/ic_launcher_background.xml` | ✅ Remplacé — dégradé bleu→sarcelle |

---

## Design v4 — Description

### Foreground (`ic_launcher_foreground.xml`)

```
Viewport : 108 × 108 dp   |   Safe zone : x:21→87, y:21→87

Éléments (de bas en haut) :
  1. Ombre portée      — trapèze semi-transparent (#1A000000) sous le livre
  2. Page gauche       — parallélogramme blanc  (24,38)→(52,34)→(52,80)→(24,84)
  3. Page droite       — parallélogramme blanc  (56,34)→(84,38)→(84,84)→(56,80)
  4. Dos (spine)       — rectangle bleu clair   (52,34)→(56,34)→(56,80)→(52,80)
  5. Lignes de texte   — 4 traits/page, #55000000, strokeWidth=3, round caps
  6. Étoile 4 pointes  — dorée #FFD600, centre:(73,24), r.ext=7, r.int=2.5
```

**Inclinaison du livre** : les pages sont des parallélogrammes avec 4 dp de décalage vertical
(bords extérieurs légèrement plus bas que le dos → effet perspective légère).

**Étoile** : flotte au-dessus de la page droite, dans le quart supérieur-droit.
Valeurs exactes des 8 sommets :
```
M73,17  L74.8,22.2  L80,24  L74.8,25.8
L73,31  L71.2,25.8  L66,24  L71.2,22.2  Z
```

### Background (`ic_launcher_background.xml`)

| Élément | Valeur |
|---------|--------|
| Type | Dégradé linéaire diagonal (top-left → bottom-right) |
| Couleur début | `#FF1565C0` — Bleu Material 800 |
| Couleur fin | `#FF006064` — Sarcelle Material 900 |
| Halo central | Radial `#1AFFFFFF → #00FFFFFF`, r=45, centre:(54,54) |

---

## Palette de couleurs

| Rôle | Hex | Aperçu |
|------|-----|--------|
| Fond (début dégradé) | `#1565C0` | Bleu royal |
| Fond (fin dégradé) | `#006064` | Sarcelle profonde |
| Pages du livre | `#FFFFFF` | Blanc pur |
| Dos du livre | `#BBDEFB` | Bleu 100 (très clair) |
| Lignes de texte | `#55000000` | Noir 33 % alpha |
| Étoile | `#FFD600` | Or / Amber A700 |

---

## Action requise par le Chef d'Orchestre

### ⚠️ Mettre à jour les bitmaps WebP (appareils API < 26)

Les fichiers vectoriels (`mipmap-anydpi-v26/`) sont déjà à jour.
Les anciens bitmaps WebP dans les dossiers `mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/` affichent encore l'ancien logo sur les appareils Android 7.0 et 7.1 (API 24-25).

**Pour les régénérer dans Android Studio :**

1. Clic droit sur `res/` → **New → Image Asset**
2. *Icon Type* : `Launcher Icons (Adaptive and Legacy)`
3. *Foreground Layer* → Source Asset : `@drawable/ic_launcher_foreground`
4. *Background Layer* → Source Asset : `@drawable/ic_launcher_background`
5. Cliquer **Next → Finish** → les WebP seront régénérés automatiquement

> ℹ️ Si `minSdk ≥ 26` dans le futur, les fichiers WebP pourront être supprimés.

---

## Comparatif v3 → v4

| Critère | v3 (ancienne) | v4 (nouvelle) |
|---------|---------------|---------------|
| Concept | Deux cartes empilées + "L" violet | Livre ouvert + étoile |
| Fond | Violet → Indigo (`#651FFF → #304FFE`) | Bleu → Sarcelle (`#1565C0 → #006064`) |
| Éléments | 6 (groupe rotation + ombre + carte + L + barre + diamant) | 5 (ombre + 2 pages + dos + lignes + étoile) |
| Lisibilité @48dp | Moyenne (L difficile à lire, diamant invisible) | Bonne (silhouette livre immédiatement reconnaissable) |
| Scalabilité | Faible (arcs bezier complexes) | Élevée (formes simples) |
