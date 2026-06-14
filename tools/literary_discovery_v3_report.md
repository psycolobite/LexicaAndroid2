# Rapport V3 - Algorithme de Découverte de Mots Littéraires

**Date** : 2026-06-10 21:11

## Principe de l'Algorithme V3

Critères (par ordre de priorité) :
1. **Zipf_books ∈ [1.5, 4.3]** : fréquence dans les livres (critère principal)
2. **Catégorie Wiktionnaire** : bonus de 14–25% si soutenu/littéraire/poétique
3. **Ratio livres/films** : bonus modeste (max +15%), pas de rejet strict

> **Justification** : 94% des mots de référence ont Zipf ∈ [1.84, 4.0],
> et 6% ont un ratio livres/films < 1.0 (mentor, codex, brigand...)
> — ce qui prouve que le ratio ne peut pas être un critère éliminatoire.

---

## Statistiques

| Métrique | Valeur |
|---|---|
| Mots de référence | 407 |
| Mots Wiktionnaire | 2753 |
| Pool total | 3095 |
| % réf. dans Wiktionnaire | 15% |
| % réf. dans Lexique383 | 94% |
| Couverture (score >= 1.00) | 309/407 (75%) |

| Seuil | Score |
|---|---|
| Rejet | < 0.930 |
| Bon | >= 1.000 |
| Excellent | >= 1.000 |

---

## Intrus Avérés (33 mots)

Ces mots de notre liste ont des données Lexique ET un score < seuil de rejet :

| Mot | Thème | Score | Zipf | Ratio | Catégories | Raisons |
|---|---|---|---|---|---|---|
| **jadis** | Temps & Éphémère | 0.340 | 4.72 | 4.53 | poétique | trop commun (Zipf=4.72) |
| **allitération** | Art & Langage | 0.350 | 0.00 | 0.00 | — | absent Wiktionnaire |
| **heuristique** | Philosophie & Idées | 0.350 | 0.00 | 0.00 | — | absent Wiktionnaire |
| **liminaire** | Art & Langage | 0.350 | 0.00 | 0.00 | — | absent Wiktionnaire |
| **loquacité** | Art & Langage | 0.350 | 0.00 | 0.00 | — | absent Wiktionnaire |
| **paresthésie** | Sentiments & Psyché | 0.350 | 0.00 | 0.00 | — | absent Wiktionnaire |
| **procrastination** | Esprit & Caractère | 0.350 | 0.00 | 0.00 | — | absent Wiktionnaire |
| **résilience** | Sentiments & Psyché | 0.350 | 0.00 | 0.00 | — | absent Wiktionnaire |
| **satyrique** | Art & Langage | 0.350 | 0.00 | 0.00 | — | absent Wiktionnaire |
| **sépale** | Nature & Cosmos | 0.350 | 0.00 | 0.00 | — | absent Wiktionnaire |
| **téléologique** | Philosophie & Idées | 0.350 | 0.00 | 0.00 | — | absent Wiktionnaire |
| **chagrin** | Sentiments & Psyché | 0.500 | 4.59 | 1.90 | — | trop commun (Zipf=4.59), absent Wiktionnaire |
| **solitaire** | Sentiments & Psyché | 0.500 | 4.32 | 1.89 | — | trop commun (Zipf=4.32), absent Wiktionnaire |
| **solipsisme** | Philosophie & Idées | 0.530 | 0.00 | 0.00 | littéraire | ? |
| **synesthésie** | Philosophie & Idées | 0.530 | 0.00 | 0.00 | littéraire | ? |
| **cryptique** | Philosophie & Idées | 0.550 | 0.00 | 0.00 | soutenu | ? |
| **crépuscule** | Lumière & Ombres | 0.550 | 4.40 | 3.49 | — | trop commun (Zipf=4.40), absent Wiktionnaire |
| **brume** | Nature & Cosmos | 0.600 | 4.55 | 8.60 | — | trop commun (Zipf=4.55), absent Wiktionnaire |
| **mélancolie** | Sentiments & Psyché | 0.600 | 4.32 | 7.95 | — | trop commun (Zipf=4.32), absent Wiktionnaire |
| **pénombre** | Lumière & Ombres | 0.650 | 4.45 | 22.98 | — | trop commun (Zipf=4.45), absent Wiktionnaire |
| **stupeur** | Sentiments & Psyché | 0.650 | 4.38 | 15.32 | — | trop commun (Zipf=4.38), absent Wiktionnaire |
| **extase** | Sentiments & Psyché | 0.830 | 4.02 | 3.07 | — | absent Wiktionnaire |
| **nostalgie** | Sentiments & Psyché | 0.830 | 4.26 | 4.06 | — | absent Wiktionnaire |
| **sérénité** | Sentiments & Psyché | 0.830 | 4.11 | 2.74 | — | absent Wiktionnaire |
| **volupté** | Sentiments & Psyché | 0.830 | 4.01 | 3.12 | — | absent Wiktionnaire |
| **murmure** | Art & Langage | 0.850 | 4.40 | 15.52 | soutenu | trop commun (Zipf=4.40) |
| **miséricorde** | Sentiments & Psyché | 0.870 | 3.71 | 0.58 | — | ratio oral=0.58, absent Wiktionnaire |
| **allégresse** | Sentiments & Psyché | 0.880 | 4.07 | 5.01 | — | absent Wiktionnaire |
| **ferveur** | Sentiments & Psyché | 0.880 | 4.03 | 6.28 | — | absent Wiktionnaire |
| **sillage** | Nature & Cosmos | 0.880 | 4.00 | 7.93 | — | absent Wiktionnaire |
| **céleste** | Nature & Cosmos | 0.920 | 3.76 | 1.31 | — | absent Wiktionnaire |
| **labyrinthe** | Nature & Cosmos | 0.920 | 3.85 | 1.44 | — | absent Wiktionnaire |
| **somnambule** | Art & Langage | 0.920 | 3.61 | 1.74 | — | absent Wiktionnaire |

---

## Mots de Référence sans Données Suffisantes (19 mots)

Ces mots de notre liste ont un mauvais score à cause d'un manque de données.
Ils sont probablement légitimes (néologismes, termes spécialisés, etc.)

| Mot | Thème | Score | Raisons |
|---|---|---|---|
| aphoristique | Art & Langage | 0.250 | absent Lexique383, absent Wiktionnaire |
| cénesthésie | Sentiments & Psyché | 0.250 | absent Lexique383, absent Wiktionnaire |
| fielueux | Sentiments & Psyché | 0.250 | absent Lexique383, absent Wiktionnaire |
| garrulité | Art & Langage | 0.250 | absent Lexique383, absent Wiktionnaire |
| gnomique | Art & Langage | 0.250 | absent Lexique383, absent Wiktionnaire |
| mignonne | Art & Langage | 0.250 | absent Lexique383, absent Wiktionnaire |
| noévie | Sentiments & Psyché | 0.250 | absent Lexique383, absent Wiktionnaire |
| némésis | Philosophie & Idées | 0.250 | absent Lexique383, absent Wiktionnaire |
| parapathique | Sentiments & Psyché | 0.250 | absent Lexique383, absent Wiktionnaire |
| purpurine | Lumière & Ombres | 0.250 | absent Lexique383, absent Wiktionnaire |
| pétrichor | Nature & Cosmos | 0.250 | absent Lexique383, absent Wiktionnaire |
| rhapsode | Art & Langage | 0.250 | absent Lexique383, absent Wiktionnaire |
| scholastique | Philosophie & Idées | 0.250 | absent Lexique383, absent Wiktionnaire |
| somatisation | Sentiments & Psyché | 0.250 | absent Lexique383, absent Wiktionnaire |
| sérendipité | Philosophie & Idées | 0.250 | absent Lexique383, absent Wiktionnaire |
| varicose | Sentiments & Psyché | 0.250 | absent Lexique383, absent Wiktionnaire |
| viateur | Esprit & Caractère | 0.250 | absent Lexique383, absent Wiktionnaire |
| érémitique | Esprit & Caractère | 0.250 | absent Lexique383, absent Wiktionnaire |
| spleenétique | Sentiments & Psyché | 0.730 | absent Lexique383 |

---

## Mots de Référence en Zone Grise (score moyen, 41 mots)

Score entre 0.930 et 1.000 :

| Mot | Thème | Score | Zipf | Ratio | Catégories |
|---|---|---|---|---|---|
| exaltation | Sentiments & Psyché | 0.930 | 4.18 | 15.31 | — |
| torpeur | Sentiments & Psyché | 0.930 | 4.12 | 16.97 | — |
| tumulte | Nature & Cosmos | 0.930 | 4.08 | 11.30 | — |
| anémie | Sentiments & Psyché | 0.950 | 2.61 | 0.31 | — |
| bannissement | Sentiments & Psyché | 0.950 | 2.53 | 0.58 | — |
| brigand | Esprit & Caractère | 0.950 | 3.37 | 0.78 | — |
| clémence | Esprit & Caractère | 0.950 | 3.39 | 0.71 | — |
| codex | Art & Langage | 0.950 | 2.15 | 0.23 | — |
| cénacle | Art & Langage | 0.950 | 2.15 | 0.34 | — |
| dilettante | Art & Langage | 0.950 | 2.83 | 0.80 | — |
| déliquescence | Temps & Éphémère | 0.950 | 2.15 | 0.88 | — |
| enlumineur | Art & Langage | 0.950 | 2.30 | 0.74 | — |
| gredin | Esprit & Caractère | 0.950 | 2.53 | 0.27 | — |
| intègre | Esprit & Caractère | 0.950 | 2.91 | 0.54 | — |
| irrévérence | Esprit & Caractère | 0.950 | 2.30 | 0.67 | — |
| licencieux | Esprit & Caractère | 0.950 | 2.53 | 0.83 | — |
| mentor | Esprit & Caractère | 0.950 | 2.79 | 0.38 | — |
| obsolète | Temps & Éphémère | 0.950 | 1.84 | 0.08 | — |
| panégyrique | Art & Langage | 0.950 | 2.30 | 0.91 | — |
| parjure | Esprit & Caractère | 0.950 | 2.83 | 0.59 | — |
| pointilleux | Esprit & Caractère | 0.950 | 2.94 | 0.96 | — |
| probe | Esprit & Caractère | 0.950 | 2.15 | 0.39 | — |
| scélérat | Esprit & Caractère | 0.950 | 2.67 | 0.71 | — |
| soporifique | Art & Langage | 0.950 | 2.30 | 0.34 | — |
| élixir | Art & Langage | 0.950 | 3.11 | 0.84 | — |
| abnégation | Sentiments & Psyché | 0.970 | 3.55 | 3.93 | — |
| auréole | Nature & Cosmos | 0.970 | 3.66 | 4.41 | — |
| chimère | Philosophie & Idées | 0.970 | 3.52 | 2.74 | — |
| constellation | Nature & Cosmos | 0.970 | 3.55 | 2.22 | — |
| fugace | Temps & Éphémère | 0.970 | 3.50 | 4.68 | — |
| ignominie | Esprit & Caractère | 0.970 | 3.50 | 4.18 | — |
| insolence | Esprit & Caractère | 0.970 | 3.94 | 3.26 | — |
| limpide | Nature & Cosmos | 0.970 | 3.99 | 4.25 | — |
| millénaire | Temps & Éphémère | 0.970 | 3.67 | 3.43 | — |
| méticuleux | Esprit & Caractère | 0.970 | 3.62 | 2.30 | — |
| nostalgique | Sentiments & Psyché | 0.970 | 3.58 | 3.40 | — |
| pittoresque | Nature & Cosmos | 0.970 | 3.66 | 3.08 | — |
| poétique | Art & Langage | 0.970 | 3.79 | 2.63 | — |
| pétale | Nature & Cosmos | 0.970 | 3.83 | 2.95 | — |
| ténébreux | Lumière & Ombres | 0.970 | 3.67 | 4.20 | — |
| éphémère | Temps & Éphémère | 0.970 | 3.76 | 4.32 | — |

---

## Nouveaux Candidats Excellents (455 mots)

Score >= 1.000, non dans notre liste :

| Mot | Score | Zipf | Ratio | Catégories |
|---|---|---|---|---|
| **aboutissant** | 1.000 | 2.91 | 9.00 | littéraire |
| **absinthe** | 1.000 | 3.46 | 2.27 | poétique |
| **abstrus** | 1.000 | 2.53 | 10.00 | littéraire |
| **accidenter** | 1.000 | 2.15 | 1.17 | littéraire |
| **accort** | 1.000 | 2.43 | 13.50 | littéraire |
| **acrimonieux** | 1.000 | 1.84 | 7.00 | soutenu |
| **admonition** | 1.000 | 2.15 | 14.00 | soutenu |
| **adonc** | 1.000 | 1.84 | 10.00 | archaïque |
| **adoncques** | 1.000 | 1.84 | 10.00 | archaïque |
| **adulateur** | 1.000 | 1.84 | 10.00 | soutenu+poétique |
| **aimeur** | 1.000 | 1.84 | 10.00 | littéraire |
| **airain** | 1.000 | 3.23 | 9.94 | littéraire |
| **albâtre** | 1.000 | 3.47 | 5.21 | poétique |
| **alentour** | 1.000 | 3.96 | 1.91 | littéraire |
| **aliénation** | 1.000 | 3.17 | 1.25 | littéraire |
| **altitude** | 1.000 | 3.80 | 1.00 | littéraire |
| **amante** | 1.000 | 3.74 | 5.33 | littéraire |
| **amphitryon** | 1.000 | 1.84 | 0.54 | soutenu |
| **anatidé** | 1.000 | 1.84 | 10.00 | soutenu |
| **annales** | 1.000 | 3.25 | 2.26 | soutenu |
| **anter** | 1.000 | 1.84 | 10.00 | archaïque |
| **aposter** | 1.000 | 1.84 | 10.00 | littéraire |
| **apparat** | 1.000 | 3.68 | 9.41 | littéraire |
| **appariteur** | 1.000 | 2.53 | 1.21 | archaïque |
| **appert** | 1.000 | 2.15 | 10.00 | littéraire |
| **aquilon** | 1.000 | 2.67 | 10.00 | poétique |
| **archipel** | 1.000 | 3.47 | 6.19 | archaïque |
| **arder** | 1.000 | 2.15 | 10.00 | archaïque |
| **ardre** | 1.000 | 1.84 | 10.00 | archaïque |
| **arpion** | 1.000 | 3.00 | 33.67 | archaïque |
| **arrière-neveux** | 1.000 | 1.84 | 10.00 | soutenu |
| **artifice** | 1.000 | 3.93 | 1.58 | soutenu |
| **artificieux** | 1.000 | 1.84 | 10.00 | littéraire |
| **arène** | 1.000 | 3.62 | 1.83 | littéraire+poétique |
| **aspre** | 1.000 | 1.84 | 10.00 | archaïque |
| **assomption** | 1.000 | 2.61 | 10.00 | soutenu |
| **astreinte** | 1.000 | 1.84 | 3.50 | littéraire |
| **atlante** | 1.000 | 2.43 | 5.40 | littéraire |
| **augmentation** | 1.000 | 3.51 | 0.49 | littéraire |
| **autan** | 1.000 | 1.84 | 10.00 | poétique |
| **avide** | 1.000 | 4.01 | 5.33 | soutenu |
| **azurer** | 1.000 | 1.84 | 10.00 | poétique |
| **balsamique** | 1.000 | 2.61 | 4.10 | littéraire |
| **barde** | 1.000 | 2.61 | 2.28 | littéraire |
| **bayer** | 1.000 | 2.30 | 20.00 | archaïque |
| **bellement** | 1.000 | 2.73 | 3.86 | littéraire |
| **bergerie** | 1.000 | 3.45 | 2.87 | littéraire |
| **blâme** | 1.000 | 3.61 | 2.19 | littéraire |
| **bobine** | 1.000 | 3.47 | 1.53 | littéraire |
| **bocager** | 1.000 | 1.84 | 10.00 | littéraire+poétique |
| **boiteux** | 1.000 | 3.35 | 1.10 | poétique |
| **bout-rimé** | 1.000 | 1.84 | 10.00 | poétique |
| **bouvier** | 1.000 | 2.83 | 5.67 | littéraire |
| **brand** | 1.000 | 2.15 | 10.00 | archaïque |
| **brise** | 1.000 | 4.04 | 1.94 | littéraire+poétique |
| **brisure** | 1.000 | 2.87 | 10.00 | poétique |
| **brochet** | 1.000 | 3.50 | 7.23 | littéraire |
| **brune** | 1.000 | 3.81 | 1.45 | poétique |
| **butin** | 1.000 | 3.72 | 0.83 | poétique |
| **cacochyme** | 1.000 | 2.67 | 10.00 | soutenu |
| **captif** | 1.000 | 3.33 | 3.43 | soutenu |
| **carmagnole** | 1.000 | 2.67 | 10.00 | archaïque |
| **cascadeur** | 1.000 | 2.15 | 0.07 | littéraire |
| **cavale** | 1.000 | 3.63 | 1.10 | littéraire+poétique |
| **celer** | 1.000 | 2.30 | 0.50 | littéraire |
| **chalumeau** | 1.000 | 3.37 | 1.48 | poétique |
| **chanteur** | 1.000 | 3.96 | 0.94 | poétique |
| **chaume** | 1.000 | 3.69 | 13.50 | poétique |
| **chaumine** | 1.000 | 2.61 | 3.73 | littéraire |
| **chaux** | 1.000 | 3.87 | 5.58 | archaïque |
| **chenu** | 1.000 | 2.73 | 3.86 | poétique |
| **chevaucheur** | 1.000 | 1.84 | 10.00 | littéraire |
| **choir** | 1.000 | 3.84 | 20.88 | littéraire |
| **châtel** | 1.000 | 1.84 | 10.00 | archaïque |
| **circonstanciel** | 1.000 | 1.84 | 10.00 | soutenu |
| **civilement** | 1.000 | 2.87 | 18.50 | archaïque |
| **clameur** | 1.000 | 3.75 | 7.68 | littéraire |
| **cohorte** | 1.000 | 3.52 | 6.75 | poétique |
| **colombe** | 1.000 | 3.54 | 0.69 | littéraire+poétique |
| **commodité** | 1.000 | 3.28 | 7.27 | soutenu+archaïque |

---

## Nouveaux Candidats Bons (0 mots)

Score entre 1.000 et 1.000 :

| Mot | Score | Zipf | Ratio | Catégories |
|---|---|---|---|---|

---

## Top 80 - Tous Mots Confondus

| Rang | Mot | Score | Zipf | Ratio | Catégories | Dans Réf. |
|---|---|---|---|---|---|---|
| 1 | **abattement** | 1.000 | 3.37 | 13.88 | — | ✓ |
| 2 | **aboulie** | 1.000 | 2.15 | 10.00 | — | ✓ |
| 3 | **aboutissant** | 1.000 | 2.91 | 9.00 | littéraire | — |
| 4 | **abscons** | 1.000 | 2.73 | 4.50 | soutenu | ✓ |
| 5 | **absinthe** | 1.000 | 3.46 | 2.27 | poétique | — |
| 6 | **abstrus** | 1.000 | 2.53 | 10.00 | littéraire | — |
| 7 | **accablement** | 1.000 | 3.55 | 17.05 | — | ✓ |
| 8 | **accidenter** | 1.000 | 2.15 | 1.17 | littéraire | — |
| 9 | **accort** | 1.000 | 2.43 | 13.50 | littéraire | — |
| 10 | **acrimonie** | 1.000 | 2.91 | 13.50 | — | ✓ |
| 11 | **acrimonieux** | 1.000 | 1.84 | 7.00 | soutenu | — |
| 12 | **admonition** | 1.000 | 2.15 | 14.00 | soutenu | — |
| 13 | **adonc** | 1.000 | 1.84 | 10.00 | archaïque | — |
| 14 | **adoncques** | 1.000 | 1.84 | 10.00 | archaïque | — |
| 15 | **adulateur** | 1.000 | 1.84 | 10.00 | soutenu+poétique | — |
| 16 | **affliction** | 1.000 | 3.40 | 2.29 | — | ✓ |
| 17 | **aimeur** | 1.000 | 1.84 | 10.00 | littéraire | — |
| 18 | **airain** | 1.000 | 3.23 | 9.94 | littéraire | — |
| 19 | **alacrité** | 1.000 | 2.79 | 50.00 | soutenu | ✓ |
| 20 | **albâtre** | 1.000 | 3.47 | 5.21 | poétique | — |
| 21 | **alcyon** | 1.000 | 2.79 | 4.69 | — | ✓ |
| 22 | **alentour** | 1.000 | 3.96 | 1.91 | littéraire | — |
| 23 | **aliénation** | 1.000 | 3.17 | 1.25 | littéraire | — |
| 24 | **altitude** | 1.000 | 3.80 | 1.00 | littéraire | — |
| 25 | **amante** | 1.000 | 3.74 | 5.33 | littéraire | — |
| 26 | **ambiguïté** | 1.000 | 3.45 | 7.28 | — | ✓ |
| 27 | **amphigourique** | 1.000 | 2.15 | 10.00 | — | ✓ |
| 28 | **amphitryon** | 1.000 | 1.84 | 0.54 | soutenu | — |
| 29 | **améthyste** | 1.000 | 2.73 | 27.00 | — | ✓ |
| 30 | **anachorète** | 1.000 | 2.61 | 4.10 | — | ✓ |
| 31 | **anathème** | 1.000 | 2.94 | 0.81 | soutenu | ✓ |
| 32 | **anatidé** | 1.000 | 1.84 | 10.00 | soutenu | — |
| 33 | **ancestral** | 1.000 | 3.31 | 2.09 | — | ✓ |
| 34 | **annales** | 1.000 | 3.25 | 2.26 | soutenu | — |
| 35 | **anter** | 1.000 | 1.84 | 10.00 | archaïque | — |
| 36 | **antédiluvien** | 1.000 | 2.61 | 41.00 | — | ✓ |
| 37 | **apathie** | 1.000 | 3.17 | 1.64 | — | ✓ |
| 38 | **aphorisme** | 1.000 | 2.87 | 37.00 | — | ✓ |
| 39 | **apogée** | 1.000 | 3.25 | 1.26 | — | ✓ |
| 40 | **apophtegme** | 1.000 | 2.30 | 10.00 | — | ✓ |
| 41 | **apostasie** | 1.000 | 2.61 | 41.00 | — | ✓ |
| 42 | **aposter** | 1.000 | 1.84 | 10.00 | littéraire | — |
| 43 | **apparat** | 1.000 | 3.68 | 9.41 | littéraire | — |
| 44 | **appariteur** | 1.000 | 2.53 | 1.21 | archaïque | — |
| 45 | **appert** | 1.000 | 2.15 | 10.00 | littéraire | — |
| 46 | **aquilon** | 1.000 | 2.67 | 10.00 | poétique | — |
| 47 | **arachnéen** | 1.000 | 2.43 | 10.00 | littéraire | ✓ |
| 48 | **archipel** | 1.000 | 3.47 | 6.19 | archaïque | — |
| 49 | **arder** | 1.000 | 2.15 | 10.00 | archaïque | — |
| 50 | **ardre** | 1.000 | 1.84 | 10.00 | archaïque | — |
| 51 | **arpion** | 1.000 | 3.00 | 33.67 | archaïque | — |
| 52 | **arrière-neveux** | 1.000 | 1.84 | 10.00 | soutenu | — |
| 53 | **artifice** | 1.000 | 3.93 | 1.58 | soutenu | — |
| 54 | **artificieux** | 1.000 | 1.84 | 10.00 | littéraire | — |
| 55 | **arène** | 1.000 | 3.62 | 1.83 | littéraire+poétique | — |
| 56 | **aspre** | 1.000 | 1.84 | 10.00 | archaïque | — |
| 57 | **assomption** | 1.000 | 2.61 | 10.00 | soutenu | — |
| 58 | **assonance** | 1.000 | 2.30 | 20.00 | — | ✓ |
| 59 | **astreinte** | 1.000 | 1.84 | 3.50 | littéraire | — |
| 60 | **ataraxie** | 1.000 | 1.84 | 3.50 | — | ✓ |
| 61 | **atermoiement** | 1.000 | 2.98 | 3.39 | — | ✓ |
| 62 | **atlante** | 1.000 | 2.43 | 5.40 | littéraire | — |
| 63 | **atone** | 1.000 | 2.87 | 37.00 | — | ✓ |
| 64 | **atrabilaire** | 1.000 | 2.15 | 10.00 | — | ✓ |
| 65 | **augmentation** | 1.000 | 3.51 | 0.49 | littéraire | — |
| 66 | **auguste** | 1.000 | 3.28 | 1.89 | — | ✓ |
| 67 | **aurore** | 1.000 | 3.97 | 3.12 | soutenu | ✓ |
| 68 | **autan** | 1.000 | 1.84 | 10.00 | poétique | — |
| 69 | **avide** | 1.000 | 4.01 | 5.33 | soutenu | — |
| 70 | **azurer** | 1.000 | 1.84 | 10.00 | poétique | — |
| 71 | **balsamique** | 1.000 | 2.61 | 4.10 | littéraire | — |
| 72 | **barde** | 1.000 | 2.61 | 2.28 | littéraire | — |
| 73 | **bayer** | 1.000 | 2.30 | 20.00 | archaïque | — |
| 74 | **bellement** | 1.000 | 2.73 | 3.86 | littéraire | — |
| 75 | **bergerie** | 1.000 | 3.45 | 2.87 | littéraire | — |
| 76 | **bilieux** | 1.000 | 2.61 | 10.00 | — | ✓ |
| 77 | **blâme** | 1.000 | 3.61 | 2.19 | littéraire | — |
| 78 | **bobine** | 1.000 | 3.47 | 1.53 | littéraire | — |
| 79 | **bocage** | 1.000 | 3.40 | 17.86 | littéraire+poétique | ✓ |
| 80 | **bocager** | 1.000 | 1.84 | 10.00 | littéraire+poétique | — |

---

## Bilan et Recommandations

- **Couverture** : 309/407 mots de référence (75%) correctement identifiés
- **Intrus avérés** : 33 mots à reconsidérer dans notre liste
- **Lacunes de données** : 19 mots légitimes non couverts par Lexique/Wiktionnaire
- **Nouveaux excellents** : 455 candidats à ajouter
- **Nouveaux bons** : 0 candidats supplémentaires

### Recommandation de filtrage pour la recherche de nouveaux mots :

```
INCLUDE si :
  - Zipf_books ∈ [1.5, 4.3]  # plage de fréquence littéraire
  - ET au moins une de ces conditions :
    a) Catégorie Wiktionnaire : soutenu/littéraire/poétique/archaïque
    b) Ratio livres/films >= 2.0  # clairement plus écrit qu'oral
    c) Score V3 >= 1.00
EXCLUDE si :
  - Zipf_books > 4.3  # trop commun
  - OU longueur < 5 caractères
  - OU locution (espace dans le mot)
  - OU dans la liste d'exclusions explicites
```
