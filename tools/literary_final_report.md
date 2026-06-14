# Rapport Final - Mots Littéraires Validés

**Méthode** : Algorithme V3 (fréquence + Wiktionnaire) × Embeddings sémantiques

---

## Intrus Confirmés dans Notre Liste de Référence

Mots signalés à la fois par l'algorithme V3 ET les embeddings :

✅ Aucun intrus confirmé par les deux méthodes.

### Intrus Sémantiques Uniquement (21 mots)

Signalés par les embeddings seulement (fréquence correcte, mais sémantique éloignée) :

- **abscons** (Philosophie & Idées)
- **antédiluvien** (Temps & Éphémère)
- **azur** (Nature & Cosmos)
- **circonspect** (Esprit & Caractère)
- **collusion** (Esprit & Caractère)
- **cors** (Sentiments & Psyché)
- **coruscant** (Lumière & Ombres)
- **larron** (Esprit & Caractère)
- **mentor** (Esprit & Caractère)
- **obsolescence** (Temps & Éphémère)
- **opalin** (Lumière & Ombres)
- **paria** (Esprit & Caractère)
- **phosphorescence** (Lumière & Ombres)
- **précepteur** (Esprit & Caractère)
- **quiproquo** (Art & Langage)
- **spleen** (Sentiments & Psyché)
- **séquestration** (Sentiments & Psyché)
- **torpeur** (Sentiments & Psyché)
- **volubilité** (Art & Langage)
- **élixir** (Art & Langage)
- **éolien** (Nature & Cosmos)

### Intrus Fréquentiels Uniquement (33 mots)

Signalés par V3 seulement (mauvaise fréquence, mais sémantique proche du centroïde) :

- **allitération** (Art & Langage)
- **allégresse** (Sentiments & Psyché)
- **brume** (Nature & Cosmos)
- **chagrin** (Sentiments & Psyché)
- **cryptique** (Philosophie & Idées)
- **crépuscule** (Lumière & Ombres)
- **céleste** (Nature & Cosmos)
- **extase** (Sentiments & Psyché)
- **ferveur** (Sentiments & Psyché)
- **heuristique** (Philosophie & Idées)
- **jadis** (Temps & Éphémère)
- **labyrinthe** (Nature & Cosmos)
- **liminaire** (Art & Langage)
- **loquacité** (Art & Langage)
- **miséricorde** (Sentiments & Psyché)
- **murmure** (Art & Langage)
- **mélancolie** (Sentiments & Psyché)
- **nostalgie** (Sentiments & Psyché)
- **paresthésie** (Sentiments & Psyché)
- **procrastination** (Esprit & Caractère)
- **pénombre** (Lumière & Ombres)
- **résilience** (Sentiments & Psyché)
- **satyrique** (Art & Langage)
- **sillage** (Nature & Cosmos)
- **solipsisme** (Philosophie & Idées)
- **solitaire** (Sentiments & Psyché)
- **somnambule** (Art & Langage)
- **stupeur** (Sentiments & Psyché)
- **synesthésie** (Philosophie & Idées)
- **sépale** (Nature & Cosmos)
- **sérénité** (Sentiments & Psyché)
- **téléologique** (Philosophie & Idées)
- **volupté** (Sentiments & Psyché)

---

## Nouveaux Candidats Validés (82 mots)

Validés par V3 (score >= 0.7) ET embeddings (sim >= 0.55) :

| Mot | Score V3 | Sim. Sém. | Zipf | Ratio | Catégories | Thème Similaire |
|---|---|---|---|---|---|---|
| **acrimonieux** | 1.000 | 0.696 | 1.84 | 7.00 | soutenu | Esprit & Caractère |
| **frileux** | 1.000 | 0.690 | 3.56 | 4.93 | littéraire | Esprit & Caractère |
| **glèbe** | 1.000 | 0.685 | 2.94 | 1.73 | littéraire | Art & Langage |
| **entéléchie** | 1.000 | 0.685 | 1.84 | 10.00 | littéraire | Philosophie & Idées |
| **blâme** | 1.000 | 0.682 | 3.61 | 2.19 | littéraire | Art & Langage |
| **feintise** | 1.000 | 0.677 | 1.84 | 10.00 | littéraire | Esprit & Caractère |
| **commère** | 1.000 | 0.658 | 3.39 | 7.15 | archaïque | Art & Langage |
| **douteur** | 1.000 | 0.658 | 2.15 | 10.00 | littéraire | Esprit & Caractère |
| **arène** | 1.000 | 0.655 | 3.62 | 1.83 | littéraire+poétique | Nature & Cosmos |
| **boiteux** | 1.000 | 0.650 | 3.35 | 1.10 | poétique | Esprit & Caractère |
| **gémissement** | 1.000 | 0.647 | 3.97 | 9.61 | poétique | Sentiments & Psyché |
| **chaume** | 1.000 | 0.645 | 3.69 | 13.50 | poétique | Nature & Cosmos |
| **exultation** | 1.000 | 0.644 | 2.83 | 13.60 | littéraire | Sentiments & Psyché |
| **désobligeance** | 1.000 | 0.641 | 2.15 | 10.00 | soutenu | Esprit & Caractère |
| **décombre** | 1.000 | 0.641 | 3.82 | 6.01 | littéraire | Sentiments & Psyché |
| **courtisan** | 1.000 | 0.639 | 3.32 | 3.17 | soutenu | Esprit & Caractère |
| **enrhumé** | 1.000 | 0.639 | 2.91 | 3.86 | littéraire | Sentiments & Psyché |
| **foucade** | 1.000 | 0.639 | 2.53 | 10.00 | littéraire | Lumière & Ombres |
| **déconcerter** | 1.000 | 0.638 | 3.15 | 7.47 | littéraire | Sentiments & Psyché |
| **contempteur** | 1.000 | 0.636 | 2.43 | 27.00 | littéraire | Esprit & Caractère |
| **fantasquement** | 1.000 | 0.635 | 1.84 | 10.00 | littéraire | Sentiments & Psyché |
| **cygne** | 1.000 | 0.634 | 3.67 | 0.88 | poétique | Nature & Cosmos |
| **cascadeur** | 1.000 | 0.631 | 2.15 | 0.07 | littéraire | Art & Langage |
| **bellement** | 1.000 | 0.629 | 2.73 | 3.86 | littéraire | Esprit & Caractère |
| **extatique** | 1.000 | 0.629 | 3.00 | 4.59 | soutenu | Philosophie & Idées |
| **faunesque** | 1.000 | 0.628 | 1.84 | 10.00 | littéraire | Lumière & Ombres |
| **humilié** | 1.000 | 0.628 | 3.33 | 1.98 | archaïque | Sentiments & Psyché |
| **dubitation** | 1.000 | 0.628 | 1.84 | 10.00 | littéraire | Sentiments & Psyché |
| **artificieux** | 1.000 | 0.627 | 1.84 | 10.00 | littéraire | Esprit & Caractère |
| **accort** | 1.000 | 0.626 | 2.43 | 13.50 | littéraire | Esprit & Caractère |
| **assomption** | 1.000 | 0.625 | 2.61 | 10.00 | soutenu | Sentiments & Psyché |
| **crépusculaire** | 1.000 | 0.625 | 3.28 | 50.00 | littéraire | Lumière & Ombres |
| **furie** | 1.000 | 0.624 | 3.74 | 2.70 | poétique | Sentiments & Psyché |
| **adulateur** | 1.000 | 0.623 | 1.84 | 10.00 | soutenu+poétique | Esprit & Caractère |
| **courtil** | 1.000 | 0.622 | 1.84 | 10.00 | poétique | Nature & Cosmos |
| **admonition** | 1.000 | 0.622 | 2.15 | 14.00 | soutenu | Sentiments & Psyché |
| **froidure** | 1.000 | 0.621 | 3.19 | 14.09 | littéraire+poétique | Sentiments & Psyché |
| **aimeur** | 1.000 | 0.621 | 1.84 | 10.00 | littéraire | Art & Langage |
| **chevaucheur** | 1.000 | 0.620 | 1.84 | 10.00 | littéraire | Art & Langage |
| **disant** | 1.000 | 0.620 | 3.85 | 5.54 | littéraire | Sentiments & Psyché |
| **astreinte** | 1.000 | 0.620 | 1.84 | 3.50 | littéraire | Esprit & Caractère |
| **chaumine** | 1.000 | 0.620 | 2.61 | 3.73 | littéraire | Art & Langage |
| **faucille** | 1.000 | 0.620 | 3.35 | 4.37 | poétique | Nature & Cosmos |
| **cavale** | 1.000 | 0.619 | 3.63 | 1.10 | littéraire+poétique | Nature & Cosmos |
| **commodité** | 1.000 | 0.618 | 3.28 | 7.27 | soutenu+archaïque | Temps & Éphémère |
| **fondement** | 1.000 | 0.618 | 3.39 | 1.38 | poétique | Nature & Cosmos |
| **coutumier** | 1.000 | 0.618 | 3.41 | 12.85 | littéraire | Esprit & Caractère |
| **déparer** | 1.000 | 0.617 | 2.67 | 4.27 | soutenu | Sentiments & Psyché |
| **difficultueux** | 1.000 | 0.615 | 1.84 | 10.00 | littéraire | Esprit & Caractère |
| **bouvier** | 1.000 | 0.615 | 2.83 | 5.67 | littéraire | Esprit & Caractère |
| **chalumeau** | 1.000 | 0.615 | 3.37 | 1.48 | poétique | Lumière & Ombres |
| **barde** | 1.000 | 0.614 | 2.61 | 2.28 | littéraire | Art & Langage |
| **captif** | 1.000 | 0.613 | 3.33 | 3.43 | soutenu | Esprit & Caractère |
| **coquebin** | 1.000 | 0.613 | 2.53 | 10.00 | littéraire | Nature & Cosmos |
| **faunesse** | 1.000 | 0.613 | 1.84 | 10.00 | poétique | Esprit & Caractère |
| **civilement** | 1.000 | 0.612 | 2.87 | 18.50 | archaïque | Esprit & Caractère |
| **cétacé** | 1.000 | 0.612 | 2.15 | 4.67 | littéraire | Nature & Cosmos |
| **gerbe** | 1.000 | 0.611 | 3.89 | 20.26 | littéraire | Nature & Cosmos |
| **cacochyme** | 1.000 | 0.611 | 2.67 | 10.00 | soutenu | Nature & Cosmos |
| **colombe** | 1.000 | 0.610 | 3.54 | 0.69 | littéraire+poétique | Nature & Cosmos |
| **balsamique** | 1.000 | 0.609 | 2.61 | 4.10 | littéraire | Art & Langage |
| **esseuler** | 1.000 | 0.608 | 2.15 | 0.82 | littéraire | Nature & Cosmos |
| **foudroyant** | 1.000 | 0.608 | 3.51 | 32.40 | poétique | Lumière & Ombres |
| **encor** | 1.000 | 0.605 | 2.43 | 0.64 | poétique | Sentiments & Psyché |
| **appariteur** | 1.000 | 0.604 | 2.53 | 1.21 | archaïque | Esprit & Caractère |
| **fourrier** | 1.000 | 0.604 | 3.23 | 50.00 | littéraire | Esprit & Caractère |
| **aquilon** | 1.000 | 0.604 | 2.67 | 10.00 | poétique | Nature & Cosmos |
| **correspondance** | 1.000 | 0.603 | 4.08 | 2.23 | littéraire | Sentiments & Psyché |
| **bergerie** | 1.000 | 0.603 | 3.45 | 2.87 | littéraire | Art & Langage |
| **brisure** | 1.000 | 0.602 | 2.87 | 10.00 | poétique | Sentiments & Psyché |
| **dépouille** | 1.000 | 0.600 | 3.70 | 2.96 | littéraire | Nature & Cosmos |
| **fronde** | 1.000 | 0.599 | 3.23 | 1.97 | poétique | Esprit & Caractère |
| **désobligeant** | 1.000 | 0.599 | 3.00 | 11.22 | soutenu | Esprit & Caractère |
| **crapule** | 1.000 | 0.598 | 3.40 | 0.56 | archaïque | Nature & Cosmos |
| **hymen** | 1.000 | 0.597 | 2.53 | 0.17 | poétique | Nature & Cosmos |
| **clameur** | 1.000 | 0.597 | 3.75 | 7.68 | littéraire | Art & Langage |
| **brise** | 1.000 | 0.596 | 4.04 | 1.94 | littéraire+poétique | Nature & Cosmos |
| **fructueux** | 1.000 | 0.596 | 3.13 | 4.22 | poétique | Esprit & Caractère |
| **dériveur** | 1.000 | 0.596 | 2.15 | 1.00 | archaïque | Esprit & Caractère |
| **fleuronner** | 1.000 | 0.596 | 1.84 | 10.00 | littéraire | Art & Langage |
| **albâtre** | 1.000 | 0.596 | 3.47 | 5.21 | poétique | Nature & Cosmos |
| **atlante** | 1.000 | 0.595 | 2.43 | 5.40 | littéraire | Nature & Cosmos |

---

## Répartition par Thème Similaire

| Thème | Nombre de candidats |
|---|---|
| Esprit & Caractère | 25 |
| Nature & Cosmos | 20 |
| Sentiments & Psyché | 17 |
| Art & Langage | 12 |
| Lumière & Ombres | 5 |
| Philosophie & Idées | 2 |
| Temps & Éphémère | 1 |

---

## Résumé des Recommandations

### Pour la liste de référence :
- **0** mots à reconsidérer (signalés par les deux méthodes)
- **54** mots en zone grise (signalés par une seule méthode)

### Nouveaux mots à intégrer :
- **82** candidats validés disponibles dans `literary_validated_candidates.csv`
- Recommandation : ajouter les **50 meilleurs** après revue éditoriale

