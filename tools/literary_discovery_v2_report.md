# Rapport V2 - Découverte de Mots Littéraires

**Algorithme** : V2 (catégorie Wiktionnaire = bonus, Lexique383 = critère central)
**Date** : 2026-06-10 21:02

---

## Statistiques Globales

| Métrique | Valeur |
|---|---|
| Mots de référence | 407 |
| Mots Wiktionnaire (4 catégories) | 2753 |
| Pool total analysé | 3095 |
| Mots de réf. avec catégorie Wiktionnaire | 65 (15%) |
| Mots de réf. avec données Lexique383 | 386 (94%) |

### Calibration des seuils (sur 407 mots de référence)

| Seuil | Score | Signification |
|---|---|---|
| Rejet | < 0.790 | Intrus potentiel |
| Bon | >= 0.890 | Mot pertinent |
| Excellent | >= 1.000 | Mot très pertinent |

**Score moyen des mots de référence** : 0.859
**Couverture de la liste de référence** : 276/407 mots (67%)

---

## Distribution des Scores (toutes catégories confondues)

| Classe | Tous mots | Mots de référence |
|---|---|---|
| Excellent | 459 | 147 |
| Bon | 186 | 129 |
| Moyen | 136 | 56 |
| Faible | 2314 | 75 |

---

## Intrus Avérés dans Notre Liste de Référence (54 mots)

Ces mots ont un score faible ET ont des données disponibles dans Lexique383/Wiktionnaire.
Ils sont probablement trop communs, trop oraux, ou mal classés dans notre liste.

| Mot | Thème | Score | Zipf | Ratio | Catégories Wikt | Raisons |
|---|---|---|---|---|---|---|
| **allitération** | Art & Langage | 0.220 | 0.00 | 0.00 | — | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| **heuristique** | Philosophie & Idées | 0.220 | 0.00 | 0.00 | — | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| **liminaire** | Art & Langage | 0.220 | 0.00 | 0.00 | — | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| **loquacité** | Art & Langage | 0.220 | 0.00 | 0.00 | — | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| **paresthésie** | Sentiments & Psyché | 0.220 | 0.00 | 0.00 | — | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| **procrastination** | Esprit & Caractère | 0.220 | 0.00 | 0.00 | — | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| **résilience** | Sentiments & Psyché | 0.220 | 0.00 | 0.00 | — | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| **satyrique** | Art & Langage | 0.220 | 0.00 | 0.00 | — | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| **sépale** | Nature & Cosmos | 0.220 | 0.00 | 0.00 | — | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| **téléologique** | Philosophie & Idées | 0.220 | 0.00 | 0.00 | — | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| **solipsisme** | Philosophie & Idées | 0.260 | 0.00 | 0.00 | littéraire | oral/familier (ratio=0.00) |
| **synesthésie** | Philosophie & Idées | 0.260 | 0.00 | 0.00 | littéraire | oral/familier (ratio=0.00) |
| **cryptique** | Philosophie & Idées | 0.264 | 0.00 | 0.00 | soutenu | oral/familier (ratio=0.00) |
| **chagrin** | Sentiments & Psyché | 0.470 | 4.59 | 1.90 | — | très commun (Zipf=4.6), absent des catégories Wiktionnaire |
| **obsolète** | Temps & Éphémère | 0.580 | 1.84 | 0.08 | — | oral/familier (ratio=0.08), absent des catégories Wiktionnaire |
| **aède** | Art & Langage | 0.630 | 2.30 | 2.00 | — | absent des catégories Wiktionnaire |
| **miséricorde** | Sentiments & Psyché | 0.630 | 3.71 | 0.58 | — | oral/familier (ratio=0.58), absent des catégories Wiktionnaire |
| **jadis** | Temps & Éphémère | 0.632 | 4.72 | 4.53 | poétique | très commun (Zipf=4.7) |
| **anémie** | Sentiments & Psyché | 0.640 | 2.61 | 0.31 | — | oral/familier (ratio=0.31), absent des catégories Wiktionnaire |
| **codex** | Art & Langage | 0.640 | 2.15 | 0.23 | — | oral/familier (ratio=0.23), absent des catégories Wiktionnaire |
| **cénacle** | Art & Langage | 0.640 | 2.15 | 0.34 | — | oral/familier (ratio=0.34), absent des catégories Wiktionnaire |
| **gredin** | Esprit & Caractère | 0.640 | 2.53 | 0.27 | — | oral/familier (ratio=0.27), absent des catégories Wiktionnaire |
| **mentor** | Esprit & Caractère | 0.640 | 2.79 | 0.38 | — | oral/familier (ratio=0.38), absent des catégories Wiktionnaire |
| **probe** | Esprit & Caractère | 0.640 | 2.15 | 0.39 | — | oral/familier (ratio=0.39), absent des catégories Wiktionnaire |
| **soporifique** | Art & Langage | 0.640 | 2.30 | 0.34 | — | oral/familier (ratio=0.34), absent des catégories Wiktionnaire |
| **solitaire** | Sentiments & Psyché | 0.650 | 4.32 | 1.89 | — | commun + peu littéraire (Zipf=4.3, ratio=1.9), absent des catégories Wiktionnaire |
| **sérénité** | Sentiments & Psyché | 0.690 | 4.11 | 2.74 | — | commun + peu littéraire (Zipf=4.1, ratio=2.7), absent des catégories Wiktionnaire |
| **céleste** | Nature & Cosmos | 0.710 | 3.76 | 1.31 | — | absent des catégories Wiktionnaire |
| **labyrinthe** | Nature & Cosmos | 0.710 | 3.85 | 1.44 | — | absent des catégories Wiktionnaire |
| **bannissement** | Sentiments & Psyché | 0.720 | 2.53 | 0.58 | — | oral/familier (ratio=0.58), absent des catégories Wiktionnaire |
| **brigand** | Esprit & Caractère | 0.720 | 3.37 | 0.78 | — | oral/familier (ratio=0.78), absent des catégories Wiktionnaire |
| **clémence** | Esprit & Caractère | 0.720 | 3.39 | 0.71 | — | oral/familier (ratio=0.71), absent des catégories Wiktionnaire |
| **dilettante** | Art & Langage | 0.720 | 2.83 | 0.80 | — | oral/familier (ratio=0.80), absent des catégories Wiktionnaire |
| **déliquescence** | Temps & Éphémère | 0.720 | 2.15 | 0.88 | — | oral/familier (ratio=0.88), absent des catégories Wiktionnaire |
| **enlumineur** | Art & Langage | 0.720 | 2.30 | 0.74 | — | oral/familier (ratio=0.74), absent des catégories Wiktionnaire |
| **intègre** | Esprit & Caractère | 0.720 | 2.91 | 0.54 | — | oral/familier (ratio=0.54), absent des catégories Wiktionnaire |
| **irrévérence** | Esprit & Caractère | 0.720 | 2.30 | 0.67 | — | oral/familier (ratio=0.67), absent des catégories Wiktionnaire |
| **licencieux** | Esprit & Caractère | 0.720 | 2.53 | 0.83 | — | oral/familier (ratio=0.83), absent des catégories Wiktionnaire |
| **panégyrique** | Art & Langage | 0.720 | 2.30 | 0.91 | — | oral/familier (ratio=0.91), absent des catégories Wiktionnaire |
| **parjure** | Esprit & Caractère | 0.720 | 2.83 | 0.59 | — | oral/familier (ratio=0.59), absent des catégories Wiktionnaire |
| **pointilleux** | Esprit & Caractère | 0.720 | 2.94 | 0.96 | — | oral/familier (ratio=0.96), absent des catégories Wiktionnaire |
| **scélérat** | Esprit & Caractère | 0.720 | 2.67 | 0.71 | — | oral/familier (ratio=0.71), absent des catégories Wiktionnaire |
| **élixir** | Art & Langage | 0.720 | 3.11 | 0.84 | — | oral/familier (ratio=0.84), absent des catégories Wiktionnaire |
| **crépuscule** | Lumière & Ombres | 0.730 | 4.40 | 3.49 | — | absent des catégories Wiktionnaire |
| **extase** | Sentiments & Psyché | 0.730 | 4.02 | 3.07 | — | absent des catégories Wiktionnaire |
| **nostalgie** | Sentiments & Psyché | 0.730 | 4.26 | 4.06 | — | absent des catégories Wiktionnaire |
| **volupté** | Sentiments & Psyché | 0.730 | 4.01 | 3.12 | — | absent des catégories Wiktionnaire |
| **azur** | Nature & Cosmos | 0.732 | 3.97 | 3.15 | littéraire+poétique | score composite faible |
| **allégresse** | Sentiments & Psyché | 0.770 | 4.07 | 5.01 | — | absent des catégories Wiktionnaire |
| **brume** | Nature & Cosmos | 0.770 | 4.55 | 8.60 | — | très commun (Zipf=4.6), absent des catégories Wiktionnaire |
| **ferveur** | Sentiments & Psyché | 0.770 | 4.03 | 6.28 | — | absent des catégories Wiktionnaire |
| **mélancolie** | Sentiments & Psyché | 0.770 | 4.32 | 7.95 | — | absent des catégories Wiktionnaire |
| **sillage** | Nature & Cosmos | 0.770 | 4.00 | 7.93 | — | absent des catégories Wiktionnaire |
| **somnambule** | Art & Langage | 0.770 | 3.61 | 1.74 | — | absent des catégories Wiktionnaire |

---

## Mots de Référence avec Lacune de Données (21 mots)

Ces mots de notre liste ont un score faible à cause d'un **manque de données** dans
Lexique383 ou les catégories Wiktionnaire — ils ne sont pas nécessairement de mauvais mots.

| Mot | Thème | Score | Raisons |
|---|---|---|---|
| cors | Sentiments & Psyché | 0.112 | absent de Lexique383, absent des catégories Wiktionnaire |
| vate | Art & Langage | 0.112 | absent de Lexique383, absent des catégories Wiktionnaire |
| aphoristique | Art & Langage | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| cénesthésie | Sentiments & Psyché | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| fielueux | Sentiments & Psyché | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| garrulité | Art & Langage | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| gnomique | Art & Langage | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| mignonne | Art & Langage | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| noévie | Sentiments & Psyché | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| némésis | Philosophie & Idées | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| parapathique | Sentiments & Psyché | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| purpurine | Lumière & Ombres | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| pétrichor | Nature & Cosmos | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| rhapsode | Art & Langage | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| scholastique | Philosophie & Idées | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| somatisation | Sentiments & Psyché | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| sérendipité | Philosophie & Idées | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| varicose | Sentiments & Psyché | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| viateur | Esprit & Caractère | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| érémitique | Esprit & Caractère | 0.160 | absent de Lexique383, absent des catégories Wiktionnaire |
| spleenétique | Sentiments & Psyché | 0.472 | absent de Lexique383 |

---

## Nouveaux Candidats Excellents (200 mots)

Mots avec score >= 1.000, non dans notre liste de référence, filtrés des exclusions explicites :

| Mot | Score | Zipf | Ratio | Catégories Wikt | Classe |
|---|---|---|---|---|---|
| **aboutissant** | 1.000 | 2.91 | 9.00 | littéraire | Excellent |
| **absinthe** | 1.000 | 3.46 | 2.27 | poétique | Excellent |
| **abstrus** | 1.000 | 2.53 | 10.00 | littéraire | Excellent |
| **accort** | 1.000 | 2.43 | 13.50 | littéraire | Excellent |
| **accouder** | 1.000 | 3.70 | 20.00 | littéraire | Excellent |
| **accroupir** | 1.000 | 3.92 | 11.44 | littéraire | Excellent |
| **acrimonieux** | 1.000 | 1.84 | 7.00 | soutenu | Excellent |
| **admonition** | 1.000 | 2.15 | 14.00 | soutenu | Excellent |
| **adonc** | 1.000 | 1.84 | 10.00 | archaïque | Excellent |
| **adoncques** | 1.000 | 1.84 | 10.00 | archaïque | Excellent |
| **adulateur** | 1.000 | 1.84 | 10.00 | soutenu+poétique | Excellent |
| **advenir** | 1.000 | 3.21 | 1.64 | littéraire+poétique | Excellent |
| **agathe** | 1.000 | 2.15 | 10.00 | archaïque | Excellent |
| **ahurir** | 1.000 | 3.13 | 4.66 | littéraire | Excellent |
| **aimeur** | 1.000 | 1.84 | 10.00 | littéraire | Excellent |
| **airain** | 1.000 | 3.23 | 9.94 | littéraire | Excellent |
| **albâtre** | 1.000 | 3.47 | 5.21 | poétique | Excellent |
| **amante** | 1.000 | 3.74 | 5.33 | littéraire | Excellent |
| **anatidé** | 1.000 | 1.84 | 10.00 | soutenu | Excellent |
| **annales** | 1.000 | 3.25 | 2.26 | soutenu | Excellent |
| **anter** | 1.000 | 1.84 | 10.00 | archaïque | Excellent |
| **aposter** | 1.000 | 1.84 | 10.00 | littéraire | Excellent |
| **apparat** | 1.000 | 3.68 | 9.41 | littéraire | Excellent |
| **appert** | 1.000 | 2.15 | 10.00 | littéraire | Excellent |
| **aquilon** | 1.000 | 2.67 | 10.00 | poétique | Excellent |
| **archipel** | 1.000 | 3.47 | 6.19 | archaïque | Excellent |
| **arder** | 1.000 | 2.15 | 10.00 | archaïque | Excellent |
| **ardre** | 1.000 | 1.84 | 10.00 | archaïque | Excellent |
| **arpion** | 1.000 | 3.00 | 33.67 | archaïque | Excellent |
| **arrière-neveux** | 1.000 | 1.84 | 10.00 | soutenu | Excellent |
| **artificieux** | 1.000 | 1.84 | 10.00 | littéraire | Excellent |
| **aspre** | 1.000 | 1.84 | 10.00 | archaïque | Excellent |
| **assomption** | 1.000 | 2.61 | 10.00 | soutenu | Excellent |
| **astreinte** | 1.000 | 1.84 | 3.50 | littéraire | Excellent |
| **atlante** | 1.000 | 2.43 | 5.40 | littéraire | Excellent |
| **autan** | 1.000 | 1.84 | 10.00 | poétique | Excellent |
| **azurer** | 1.000 | 1.84 | 10.00 | poétique | Excellent |
| **balsamique** | 1.000 | 2.61 | 4.10 | littéraire | Excellent |
| **barde** | 1.000 | 2.61 | 2.28 | littéraire | Excellent |
| **bayer** | 1.000 | 2.30 | 20.00 | archaïque | Excellent |
| **bellement** | 1.000 | 2.73 | 3.86 | littéraire | Excellent |
| **bergerie** | 1.000 | 3.45 | 2.87 | littéraire | Excellent |
| **bobine** | 1.000 | 3.47 | 1.53 | littéraire | Excellent |
| **bocager** | 1.000 | 1.84 | 10.00 | littéraire+poétique | Excellent |
| **bout-rimé** | 1.000 | 1.84 | 10.00 | poétique | Excellent |
| **brand** | 1.000 | 2.15 | 10.00 | archaïque | Excellent |
| **brisure** | 1.000 | 2.87 | 10.00 | poétique | Excellent |
| **brochet** | 1.000 | 3.50 | 7.23 | littéraire | Excellent |
| **cacochyme** | 1.000 | 2.67 | 10.00 | soutenu | Excellent |
| **captif** | 1.000 | 3.33 | 3.43 | soutenu | Excellent |
| **carmagnole** | 1.000 | 2.67 | 10.00 | archaïque | Excellent |
| **chaume** | 1.000 | 3.69 | 13.50 | poétique | Excellent |
| **chaumine** | 1.000 | 2.61 | 3.73 | littéraire | Excellent |
| **chenu** | 1.000 | 2.73 | 3.86 | poétique | Excellent |
| **chevaucheur** | 1.000 | 1.84 | 10.00 | littéraire | Excellent |
| **choir** | 1.000 | 3.84 | 20.88 | littéraire | Excellent |
| **châtel** | 1.000 | 1.84 | 10.00 | archaïque | Excellent |
| **circonstanciel** | 1.000 | 1.84 | 10.00 | soutenu | Excellent |
| **civilement** | 1.000 | 2.87 | 18.50 | archaïque | Excellent |
| **clameur** | 1.000 | 3.75 | 7.68 | littéraire | Excellent |
| **cohorte** | 1.000 | 3.52 | 6.75 | poétique | Excellent |
| **commodité** | 1.000 | 3.28 | 7.27 | archaïque+soutenu | Excellent |
| **commère** | 1.000 | 3.39 | 7.15 | archaïque | Excellent |
| **compas** | 1.000 | 3.45 | 1.89 | littéraire | Excellent |
| **confins** | 1.000 | 3.86 | 4.40 | littéraire | Excellent |
| **congru** | 1.000 | 2.53 | 10.00 | soutenu | Excellent |
| **conseilleur** | 1.000 | 1.84 | 10.00 | littéraire | Excellent |
| **contempteur** | 1.000 | 2.43 | 27.00 | littéraire | Excellent |
| **coquebin** | 1.000 | 2.53 | 10.00 | littéraire | Excellent |
| **corail** | 1.000 | 3.47 | 2.86 | poétique | Excellent |
| **courber** | 1.000 | 3.68 | 9.80 | poétique | Excellent |
| **courre** | 1.000 | 3.46 | 3.88 | archaïque | Excellent |
| **courtaud** | 1.000 | 2.79 | 2.90 | littéraire | Excellent |
| **courtil** | 1.000 | 1.84 | 10.00 | poétique | Excellent |
| **courtisan** | 1.000 | 3.32 | 3.17 | soutenu | Excellent |
| **cristallin** | 1.000 | 3.26 | 7.00 | poétique | Excellent |
| **crépusculaire** | 1.000 | 3.28 | 50.00 | littéraire | Excellent |
| **cétacé** | 1.000 | 2.15 | 4.67 | littéraire | Excellent |
| **dahoméen** | 1.000 | 1.84 | 10.00 | archaïque | Excellent |
| **darder** | 1.000 | 2.87 | 10.00 | poétique | Excellent |

---

## Nouveaux Candidats Bons (57 mots)

Mots avec score entre 0.890 et 1.000 :

| Mot | Score | Zipf | Ratio | Catégories Wikt |
|---|---|---|---|---|
| oublieux | 0.990 | 3.11 | 2.84 | archaïque |
| ensanglanter | 0.989 | 2.94 | 1.96 | poétique |
| fronde | 0.989 | 3.23 | 1.97 | poétique |
| pourpoint | 0.989 | 3.06 | 1.95 | poétique |
| redorer | 0.989 | 2.43 | 1.50 | poétique |
| chaux | 0.979 | 3.87 | 5.58 | archaïque |
| coîffe | 0.979 | 3.66 | 7.31 | archaïque |
| glacer | 0.978 | 3.89 | 4.23 | poétique |
| ivoire | 0.978 | 3.89 | 4.00 | poétique |
| plaintif | 0.978 | 3.52 | 4.09 | poétique |
| violette | 0.978 | 3.64 | 3.75 | poétique |
| commune | 0.972 | 3.89 | 2.71 | soutenu |
| couronner | 0.972 | 3.56 | 2.52 | soutenu |
| nippon | 0.960 | 2.61 | 1.24 | soutenu |
| éminent | 0.960 | 3.31 | 1.22 | soutenu |
| blâme | 0.956 | 3.61 | 2.19 | littéraire |
| dépouille | 0.956 | 3.70 | 2.96 | littéraire |
| tacher | 0.956 | 3.63 | 2.54 | littéraire |
| virilité | 0.956 | 3.80 | 2.58 | littéraire |
| arène | 0.947 | 3.62 | 1.83 | littéraire+poétique |
| humilié | 0.946 | 3.33 | 1.98 | archaïque |
| accidenter | 0.944 | 2.15 | 1.17 | littéraire |
| aliénation | 0.944 | 3.17 | 1.25 | littéraire |
| cristallisation | 0.944 | 2.43 | 1.35 | littéraire |
| déduit | 0.944 | 2.43 | 1.29 | littéraire |
| estoc | 0.944 | 2.30 | 1.25 | littéraire |
| flouer | 0.944 | 2.73 | 1.39 | littéraire |
| onéreux | 0.944 | 2.83 | 1.13 | littéraire |
| pourceau | 0.944 | 2.79 | 1.39 | littéraire |
| prévention | 0.944 | 3.13 | 1.01 | littéraire |
| périphrase | 0.944 | 2.15 | 1.27 | littéraire |
| sublimer | 0.944 | 2.73 | 1.20 | littéraire |
| vergeture | 0.944 | 2.61 | 1.00 | littéraire |
| nouër | 0.935 | 3.78 | 3.60 | archaïque |
| furie | 0.931 | 3.74 | 2.70 | poétique |
| artifice | 0.924 | 3.93 | 1.58 | soutenu |
| avide | 0.924 | 4.01 | 5.33 | soutenu |
| muraille | 0.924 | 4.04 | 8.60 | soutenu |
| péril | 0.924 | 4.00 | 1.58 | soutenu |
| redire | 0.924 | 3.71 | 1.67 | soutenu |
| étreinte | 0.924 | 4.09 | 6.00 | soutenu |
| boiteux | 0.920 | 3.35 | 1.10 | poétique |
| chalumeau | 0.920 | 3.37 | 1.48 | poétique |
| fondement | 0.920 | 3.39 | 1.38 | poétique |
| alentour | 0.909 | 3.96 | 1.91 | littéraire |
| dentelle | 0.909 | 4.24 | 5.76 | littéraire |
| décret | 0.909 | 3.81 | 1.81 | littéraire |
| geste | 0.909 | 5.24 | 5.48 | littéraire |
| ressortir | 0.909 | 3.90 | 1.71 | littéraire |
| trait | 0.909 | 4.77 | 7.40 | littéraire |
| épais | 0.909 | 4.61 | 6.75 | littéraire |
| érotique | 0.909 | 3.77 | 1.96 | littéraire |
| chevelure | 0.908 | 4.40 | 10.03 | poétique |
| paupière | 0.908 | 4.75 | 14.97 | poétique |
| barque | 0.898 | 4.48 | 3.14 | littéraire+poétique |
| gifle | 0.891 | 3.99 | 2.74 | archaïque |
| situër | 0.891 | 3.89 | 2.90 | archaïque |

---

## Top 50 Tous Mots Confondus

| Rang | Mot | Score | Zipf | Ratio | Catégories | Dans Ref. |
|---|---|---|---|---|---|---|
| 1 | **abattement** | 1.000 | 3.37 | 13.88 | — | ✓ |
| 2 | **aboulie** | 1.000 | 2.15 | 10.00 | — | ✓ |
| 3 | **aboutissant** | 1.000 | 2.91 | 9.00 | littéraire | — |
| 4 | **abscons** | 1.000 | 2.73 | 4.50 | soutenu | ✓ |
| 5 | **absinthe** | 1.000 | 3.46 | 2.27 | poétique | — |
| 6 | **abstrus** | 1.000 | 2.53 | 10.00 | littéraire | — |
| 7 | **accort** | 1.000 | 2.43 | 13.50 | littéraire | — |
| 8 | **accouder** | 1.000 | 3.70 | 20.00 | littéraire | — |
| 9 | **accroupir** | 1.000 | 3.92 | 11.44 | littéraire | — |
| 10 | **acrimonie** | 1.000 | 2.91 | 13.50 | — | ✓ |
| 11 | **acrimonieux** | 1.000 | 1.84 | 7.00 | soutenu | — |
| 12 | **admonition** | 1.000 | 2.15 | 14.00 | soutenu | — |
| 13 | **adonc** | 1.000 | 1.84 | 10.00 | archaïque | — |
| 14 | **adoncques** | 1.000 | 1.84 | 10.00 | archaïque | — |
| 15 | **adulateur** | 1.000 | 1.84 | 10.00 | soutenu+poétique | — |
| 16 | **advenir** | 1.000 | 3.21 | 1.64 | littéraire+poétique | — |
| 17 | **agathe** | 1.000 | 2.15 | 10.00 | archaïque | — |
| 18 | **ahurir** | 1.000 | 3.13 | 4.66 | littéraire | — |
| 19 | **aimeur** | 1.000 | 1.84 | 10.00 | littéraire | — |
| 20 | **airain** | 1.000 | 3.23 | 9.94 | littéraire | — |
| 21 | **alacrité** | 1.000 | 2.79 | 50.00 | soutenu | ✓ |
| 22 | **albâtre** | 1.000 | 3.47 | 5.21 | poétique | — |
| 23 | **amante** | 1.000 | 3.74 | 5.33 | littéraire | — |
| 24 | **amphigourique** | 1.000 | 2.15 | 10.00 | — | ✓ |
| 25 | **améthyste** | 1.000 | 2.73 | 27.00 | — | ✓ |
| 26 | **anatidé** | 1.000 | 1.84 | 10.00 | soutenu | — |
| 27 | **annales** | 1.000 | 3.25 | 2.26 | soutenu | — |
| 28 | **anter** | 1.000 | 1.84 | 10.00 | archaïque | — |
| 29 | **antédiluvien** | 1.000 | 2.61 | 41.00 | — | ✓ |
| 30 | **aphorisme** | 1.000 | 2.87 | 37.00 | — | ✓ |
| 31 | **apophtegme** | 1.000 | 2.30 | 10.00 | — | ✓ |
| 32 | **apostasie** | 1.000 | 2.61 | 41.00 | — | ✓ |
| 33 | **aposter** | 1.000 | 1.84 | 10.00 | littéraire | — |
| 34 | **apparat** | 1.000 | 3.68 | 9.41 | littéraire | — |
| 35 | **appert** | 1.000 | 2.15 | 10.00 | littéraire | — |
| 36 | **aquilon** | 1.000 | 2.67 | 10.00 | poétique | — |
| 37 | **arachnéen** | 1.000 | 2.43 | 10.00 | littéraire | ✓ |
| 38 | **archipel** | 1.000 | 3.47 | 6.19 | archaïque | — |
| 39 | **arder** | 1.000 | 2.15 | 10.00 | archaïque | — |
| 40 | **ardre** | 1.000 | 1.84 | 10.00 | archaïque | — |
| 41 | **arpion** | 1.000 | 3.00 | 33.67 | archaïque | — |
| 42 | **arrière-neveux** | 1.000 | 1.84 | 10.00 | soutenu | — |
| 43 | **artificieux** | 1.000 | 1.84 | 10.00 | littéraire | — |
| 44 | **aspre** | 1.000 | 1.84 | 10.00 | archaïque | — |
| 45 | **assomption** | 1.000 | 2.61 | 10.00 | soutenu | — |
| 46 | **assonance** | 1.000 | 2.30 | 20.00 | — | ✓ |
| 47 | **astreinte** | 1.000 | 1.84 | 3.50 | littéraire | — |
| 48 | **atlante** | 1.000 | 2.43 | 5.40 | littéraire | — |
| 49 | **atone** | 1.000 | 2.87 | 37.00 | — | ✓ |
| 50 | **atrabilaire** | 1.000 | 2.15 | 10.00 | — | ✓ |

---

## Analyse de Couverture de la Liste de Référence

### Mots de référence par classe de score :

| Classe | Mots de référence | % |
|---|---|---|
| Excellent | 147 | 36% |
| Bon | 129 | 31% |
| Moyen | 56 | 13% |
| Faible | 75 | 18% |

### Mots de référence dans la zone Faible (< P10) :

| Mot | Score | Zipf | Ratio | Raisons |
|---|---|---|---|---|
| cors | 0.112 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| vate | 0.112 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| aphoristique | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| cénesthésie | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| fielueux | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| garrulité | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| gnomique | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| mignonne | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| noévie | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| némésis | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| parapathique | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| purpurine | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| pétrichor | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| rhapsode | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| scholastique | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| somatisation | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| sérendipité | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| varicose | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| viateur | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| érémitique | 0.160 | 0.00 | 1.00 | absent de Lexique383, absent des catégories Wiktionnaire |
| allitération | 0.220 | 0.00 | 0.00 | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| heuristique | 0.220 | 0.00 | 0.00 | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| liminaire | 0.220 | 0.00 | 0.00 | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| loquacité | 0.220 | 0.00 | 0.00 | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| paresthésie | 0.220 | 0.00 | 0.00 | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| procrastination | 0.220 | 0.00 | 0.00 | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| résilience | 0.220 | 0.00 | 0.00 | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| satyrique | 0.220 | 0.00 | 0.00 | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| sépale | 0.220 | 0.00 | 0.00 | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |
| téléologique | 0.220 | 0.00 | 0.00 | oral/familier (ratio=0.00), absent des catégories Wiktionnaire |

---

## Recommandations Algorithmiques

### Critères de sélection optimaux (V2) :

1. **Score V2 >= 0.89** : seuil calibré sur P25 des mots de référence
2. **Zipf_books ∈ [1.0, 4.5]** : ni trop rare ni trop commun
3. **Ratio >= 1.5** : plus écrit qu'oral (sauf compensation par catégorie Wiktionnaire)
4. **Longueur >= 5 caractères** : les mots courts (4 lettres ou moins) sont rarement pertinents
5. **Pas de locutions** (pas d'espace dans le mot) : garder des mots simples
6. **Catégorie Wiktionnaire** : bonus fort si soutenu/littéraire/poétique, mais pas obligatoire

### Bilan :

- 276/407 mots de référence couverts (67%)
- 54 intrus avérés dans notre liste
- 21 mots de référence légitimes avec lacune de données
- 200 nouveaux candidats excellents
- 57 nouveaux candidats bons
