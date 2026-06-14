# Rapport Maître — Découverte de Mots Littéraires Français

## Méthode Combinée : Lexique383 + Wiktionnaire + Embeddings Sémantiques

```
SOURCES :
  1. Lexique383 (lexique.org)  → Score de fréquence littéraire (Zipf_books)
  2. Wiktionnaire (4 catégories) → Bonus de registre (soutenu/littéraire/poétique)
  3. OrdalieTech/solon-embeddings-large-0.1 → Score sémantique (sim. cosinus)

SCORE FINAL = (50% sémantique) + (35% fréquence) + (15% Wiktionnaire)
```

---

## 1. État de Notre Liste de Référence (407 mots)

| Catégorie | Nombre | % |
|---|---|---|
| **Bons mots** (score >= seuil) | **363** | 89% |
| Intrus avérés (mauvais score + données) | 24 | 5% |
| Lacunes de données (pas dans Lexique/Wiktionnaire) | 20 | 4% |

### 1a. Distance au Centroïde Littéraire (information sémantique)

> **Rappel méthodologique** : la distance au centroïde NE DIS QUALIFIE PAS un mot.
> Les mots littéraires les plus **originaux** (spleen, opalin, azur) sont précisément
> ceux qui s'éloignent du centroïde — c'est leur qualité, pas un défaut.
> Le centroïde sert uniquement à filtrer les NOUVEAUX candidats (mots courants
> comme confondre, décéder qui auraient un bon score Wiktionnaire mais pas littéraires).

| Mot | Thème | Sim. Cosinus | Statut éditorial |
|---|---|---|---|
| **spleen** | Sentiments & Psyché | 0.4870 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **séquestration** | Sentiments & Psyché | 0.4982 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **opalin** | Lumière & Ombres | 0.5099 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **torpeur** | Sentiments & Psyché | 0.5132 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **abscons** | Philosophie & Idées | 0.5170 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **mentor** | Esprit & Caractère | 0.5222 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **volubilité** | Art & Langage | 0.5240 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **azur** | Nature & Cosmos | 0.5253 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **larron** | Esprit & Caractère | 0.5277 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **cors** | Sentiments & Psyché | 0.5311 | ℹ️ Information — originalité sémantique (conserver par défaut) |
| **collusion** | Esprit & Caractère | 0.5316 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **précepteur** | Esprit & Caractère | 0.5337 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **éolien** | Nature & Cosmos | 0.5349 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **phosphorescence** | Lumière & Ombres | 0.5351 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **quiproquo** | Art & Langage | 0.5356 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **obsolescence** | Temps & Éphémère | 0.5357 | ℹ️ Information — originalité sémantique (conserver par défaut) |
| **circonspect** | Esprit & Caractère | 0.5380 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **antédiluvien** | Temps & Éphémère | 0.5383 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **coruscant** | Lumière & Ombres | 0.5413 | ✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité |
| **paria** | Esprit & Caractère | 0.5432 | ✓ Dans la marge acceptable — conserver |

---

### 1b. Intrus Avérés par l'Algorithme (24 mots)

Score faible avec données disponibles dans Lexique383 :

| Mot | Thème | Score V3 | Zipf | Ratio | Raison |
|---|---|---|---|---|---|
| **azur** | Nature & Cosmos | 0.000 | 3.97 | 3.15 | Score composite faible |
| **aède** | Art & Langage | 0.000 | 2.30 | 2.00 | Score composite faible |
| **heur** | Sentiments & Psyché | 0.000 | 2.67 | 10.00 | Score composite faible |
| **jadis** | Temps & Éphémère | 0.340 | 4.72 | 4.53 | Trop commun (Zipf=4.72) |
| **allitération** | Art & Langage | 0.350 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **heuristique** | Philosophie & Idées | 0.350 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **liminaire** | Art & Langage | 0.350 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **loquacité** | Art & Langage | 0.350 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **paresthésie** | Sentiments & Psyché | 0.350 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **procrastination** | Esprit & Caractère | 0.350 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **résilience** | Sentiments & Psyché | 0.350 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **satyrique** | Art & Langage | 0.350 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **sépale** | Nature & Cosmos | 0.350 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **téléologique** | Philosophie & Idées | 0.350 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **chagrin** | Sentiments & Psyché | 0.500 | 4.59 | 1.90 | Trop commun (Zipf=4.59) |
| **solitaire** | Sentiments & Psyché | 0.500 | 4.32 | 1.89 | Trop commun (Zipf=4.32) |
| **solipsisme** | Philosophie & Idées | 0.530 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **synesthésie** | Philosophie & Idées | 0.530 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **cryptique** | Philosophie & Idées | 0.550 | 0.00 | 0.00 | Registre trop oral (ratio=0.00) |
| **crépuscule** | Lumière & Ombres | 0.550 | 4.40 | 3.49 | Trop commun (Zipf=4.40) |

---

### 1c. Mots de Référence sans Données (20 mots)

Ces mots ont un mauvais score **uniquement parce qu'ils sont absents**
de Lexique383 et des catégories Wiktionnaire — ils sont probablement légitimes :

| Mot | Thème | Commentaire |
|---|---|---|
| cors | Sentiments & Psyché | Mot court ou spécialisé |
| vate | Art & Langage | Mot court ou spécialisé |
| aphoristique | Art & Langage | Néologisme ou terme très rare |
| cénesthésie | Sentiments & Psyché | Néologisme ou terme très rare |
| fielueux | Sentiments & Psyché | Mot court ou spécialisé |
| garrulité | Art & Langage | Mot court ou spécialisé |
| gnomique | Art & Langage | Mot court ou spécialisé |
| mignonne | Art & Langage | Mot court ou spécialisé |
| noévie | Sentiments & Psyché | Mot court ou spécialisé |
| némésis | Philosophie & Idées | Mot court ou spécialisé |
| parapathique | Sentiments & Psyché | Néologisme ou terme très rare |
| purpurine | Lumière & Ombres | Mot court ou spécialisé |
| pétrichor | Nature & Cosmos | Mot court ou spécialisé |
| rhapsode | Art & Langage | Mot court ou spécialisé |
| scholastique | Philosophie & Idées | Néologisme ou terme très rare |
| somatisation | Sentiments & Psyché | Néologisme ou terme très rare |
| sérendipité | Philosophie & Idées | Néologisme ou terme très rare |
| varicose | Sentiments & Psyché | Mot court ou spécialisé |
| viateur | Esprit & Caractère | Mot court ou spécialisé |
| érémitique | Esprit & Caractère | Mot court ou spécialisé |

---

## 2. Nouveaux Candidats Validés (41 mots)

Filtrés par :
- Score sémantique >= 0.595 (proche du centroïde littéraire)
- Zipf_books ∈ [1.0, 4.3]
- Longueur >= 5 caractères, pas de locution
- Filtrage éditorial manuel (exclusion des mots non pertinents)

| Rang | Mot | Score Sem. | Score V3 | Zipf | Ratio | Catégories | Thème Similaire |
|---|---|---|---|---|---|---|---|
| 1 | **acrimonieux** | 0.696 | 1.000 | 1.84 | 7.0 | soutenu | Esprit & Caractère |
| 2 | **glèbe** | 0.685 | 1.000 | 2.94 | 1.7 | littéraire | Art & Langage |
| 3 | **entéléchie** | 0.685 | 1.000 | 1.84 | 10.0 | littéraire | Philosophie & Idées |
| 4 | **blâme** | 0.682 | 1.000 | 3.61 | 2.2 | littéraire | Art & Langage |
| 5 | **feintise** | 0.677 | 1.000 | 1.84 | 10.0 | littéraire | Esprit & Caractère |
| 6 | **arène** | 0.655 | 1.000 | 3.62 | 1.8 | littéraire+poétique | Nature & Cosmos |
| 7 | **exultation** | 0.644 | 1.000 | 2.83 | 13.6 | littéraire | Sentiments & Psyché |
| 8 | **désobligeance** | 0.641 | 1.000 | 2.15 | 10.0 | soutenu | Esprit & Caractère |
| 9 | **décombre** | 0.641 | 1.000 | 3.82 | 6.0 | littéraire | Sentiments & Psyché |
| 10 | **courtisan** | 0.639 | 1.000 | 3.32 | 3.2 | soutenu | Esprit & Caractère |
| 11 | **foucade** | 0.639 | 1.000 | 2.53 | 10.0 | littéraire | Lumière & Ombres |
| 12 | **extatique** | 0.629 | 1.000 | 3.00 | 4.6 | soutenu | Philosophie & Idées |
| 13 | **accort** | 0.626 | 1.000 | 2.43 | 13.5 | littéraire | Esprit & Caractère |
| 14 | **crépusculaire** | 0.625 | 1.000 | 3.28 | 50.0 | littéraire | Lumière & Ombres |
| 15 | **adulateur** | 0.623 | 1.000 | 1.84 | 10.0 | soutenu+poétique | Esprit & Caractère |
| 16 | **admonition** | 0.622 | 1.000 | 2.15 | 14.0 | soutenu | Sentiments & Psyché |
| 17 | **froidure** | 0.621 | 1.000 | 3.19 | 14.1 | littéraire+poétique | Sentiments & Psyché |
| 18 | **cavale** | 0.619 | 1.000 | 3.63 | 1.1 | littéraire+poétique | Nature & Cosmos |
| 19 | **déparer** | 0.617 | 1.000 | 2.67 | 4.3 | soutenu | Sentiments & Psyché |
| 20 | **bouvier** | 0.615 | 1.000 | 2.83 | 5.7 | littéraire | Esprit & Caractère |
| 21 | **barde** | 0.614 | 1.000 | 2.61 | 2.3 | littéraire | Art & Langage |
| 22 | **captif** | 0.613 | 1.000 | 3.33 | 3.4 | soutenu | Esprit & Caractère |
| 23 | **coquebin** | 0.613 | 1.000 | 2.53 | 10.0 | littéraire | Nature & Cosmos |
| 24 | **gerbe** | 0.611 | 1.000 | 3.89 | 20.3 | littéraire | Nature & Cosmos |
| 25 | **cacochyme** | 0.611 | 1.000 | 2.67 | 10.0 | soutenu | Nature & Cosmos |
| 26 | **colombe** | 0.610 | 1.000 | 3.54 | 0.7 | littéraire+poétique | Nature & Cosmos |
| 27 | **balsamique** | 0.609 | 1.000 | 2.61 | 4.1 | littéraire | Art & Langage |
| 28 | **bergerie** | 0.603 | 1.000 | 3.45 | 2.9 | littéraire | Art & Langage |
| 29 | **dépouille** | 0.600 | 1.000 | 3.70 | 3.0 | littéraire | Nature & Cosmos |
| 30 | **désobligeant** | 0.599 | 1.000 | 3.00 | 11.2 | soutenu | Esprit & Caractère |
| 31 | **clameur** | 0.597 | 1.000 | 3.75 | 7.7 | littéraire | Art & Langage |
| 32 | **brise** | 0.596 | 1.000 | 4.04 | 1.9 | littéraire+poétique | Nature & Cosmos |
| 33 | **chaume** | 0.645 | 1.000 | 3.69 | 13.5 | poétique | Nature & Cosmos |
| 34 | **furie** | 0.624 | 1.000 | 3.74 | 2.7 | poétique | Sentiments & Psyché |
| 35 | **faucille** | 0.620 | 1.000 | 3.35 | 4.4 | poétique | Nature & Cosmos |
| 36 | **foudroyant** | 0.608 | 1.000 | 3.51 | 32.4 | poétique | Lumière & Ombres |
| 37 | **aquilon** | 0.604 | 1.000 | 2.67 | 10.0 | poétique | Nature & Cosmos |
| 38 | **brisure** | 0.602 | 1.000 | 2.87 | 10.0 | poétique | Sentiments & Psyché |
| 39 | **hymen** | 0.597 | 1.000 | 2.53 | 0.2 | poétique | Nature & Cosmos |
| 40 | **albâtre** | 0.596 | 1.000 | 3.47 | 5.2 | poétique | Nature & Cosmos |
| 41 | **commère** | 0.658 | 1.000 | 3.39 | 7.1 | archaïque | Art & Langage |

---

## 3. Répartition des Nouveaux Candidats par Thème Similaire

| Thème | Nombre | Top 5 mots |
|---|---|---|
| Nature & Cosmos | 13 | arène, cavale, coquebin, gerbe, cacochyme |
| Esprit & Caractère | 9 | acrimonieux, feintise, désobligeance, courtisan, accort |
| Art & Langage | 7 | glèbe, blâme, barde, balsamique, bergerie |
| Sentiments & Psyché | 7 | exultation, décombre, admonition, froidure, déparer |
| Lumière & Ombres | 3 | foucade, crépusculaire, foudroyant |
| Philosophie & Idées | 2 | entéléchie, extatique |

---

## 4. Recommandations

### À faire pour la liste de référence :

- **Revoir** les 21 mots sémantiquement éloignés du centroïde
  (spleen, opalin, séquestration, torpeur, abscons, mentor...)
- **Vérifier** les 24 mots avec mauvais score algorithmique
- **Accepter** les 20 mots absents des bases de données (lacunes normales)

### Pour enrichir la liste de référence :

Les **41 nouveaux candidats** dans `literary_new_validated_clean.csv`
sont prêts pour revue éditoriale. Suggestions prioritaires :

**Tier 1 (score sem. > 0.67) — à intégrer en priorité :**

- **acrimonieux** : soutenu, Zipf=1.84
- **glèbe** : littéraire, Zipf=2.94
- **entéléchie** : littéraire, Zipf=1.84
- **blâme** : littéraire, Zipf=3.61
- **feintise** : littéraire, Zipf=1.84

**Tier 2 (score sem. 0.63–0.67) — bons candidats :**

- **arène** : littéraire+poétique, Zipf=3.62, thème=Nature & Cosmos
- **exultation** : littéraire, Zipf=2.83, thème=Sentiments & Psyché
- **désobligeance** : soutenu, Zipf=2.15, thème=Esprit & Caractère
- **décombre** : littéraire, Zipf=3.82, thème=Sentiments & Psyché
- **courtisan** : soutenu, Zipf=3.32, thème=Esprit & Caractère
- **foucade** : littéraire, Zipf=2.53, thème=Lumière & Ombres
- **chaume** : poétique, Zipf=3.69, thème=Nature & Cosmos
- **commère** : archaïque, Zipf=3.39, thème=Art & Langage

---

## 5. Mots Rejetés par Filtrage Éditorial (41 mots)

Ces mots avaient un bon score technique mais ont été exclus manuellement :

**exclusion éditoriale** (40) : aimeur, appariteur, artificieux, assomption, astreinte, bellement, boiteux, cascadeur, chalumeau, chaumine, chevaucheur, civilement, commodité, contempteur, correspondance, courtil, coutumier, crapule, cygne, cétacé, difficultueux, disant, douteur, dubitation, déconcerter, dériveur, encor, enrhumé, esseuler, fantasquement, faunesque, faunesse, fleuronner, fondement, fourrier, frileux, fronde, fructueux, gémissement, humilié

**sem trop faible (0.595)** (1) : atlante

