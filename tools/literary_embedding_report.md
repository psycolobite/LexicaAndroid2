# Rapport Embeddings - Découverte de Mots Littéraires

**Modèle** : `OrdalieTech/solon-embeddings-large-0.1`
**Date** : 2026-06-10 21:30

## Principe

Le score sémantique est calculé par **similarité cosinus** entre l'embedding
du mot et le **centroïde littéraire** (moyenne des 407 mots de référence).

Un mot littéraire sera proche du centroïde ; un mot courant (confondre,
décéder, guerrier...) sera éloigné même s'il est classé 'soutenu' par Wiktionnaire.

---

## Statistiques

| Métrique | Valeur |
|---|---|
| Mots de référence | 407 |
| Mots embeddés | 607 |
| Sim. cosinus moyenne (réf.) | 0.6174 |
| Sim. cosinus médiane (réf.) | 0.6180 |
| Seuil sémantique rejet | 0.5451 |
| Seuil sémantique bon | 0.5922 |

---

## Intrus Sémantiques (21 mots)

Ces mots de notre liste sont **sémantiquement éloignés** du centroïde :

| Mot | Thème | Sim. Cosinus | Score Combiné |
|---|---|---|---|
| **spleen** | Sentiments & Psyché | 0.4870 | 0.5935 |
| **séquestration** | Sentiments & Psyché | 0.4982 | 0.5991 |
| **opalin** | Lumière & Ombres | 0.5099 | 0.6049 |
| **torpeur** | Sentiments & Psyché | 0.5132 | 0.5016 |
| **abscons** | Philosophie & Idées | 0.5170 | 0.7585 |
| **mentor** | Esprit & Caractère | 0.5222 | 0.6111 |
| **volubilité** | Art & Langage | 0.5240 | 0.6120 |
| **azur** | Nature & Cosmos | 0.5253 | 0.7101 |
| **larron** | Esprit & Caractère | 0.5277 | 0.7338 |
| **cors** | Sentiments & Psyché | 0.5311 | 0.4055 |
| **collusion** | Esprit & Caractère | 0.5316 | 0.6158 |
| **précepteur** | Esprit & Caractère | 0.5337 | 0.6168 |
| **éolien** | Nature & Cosmos | 0.5349 | 0.6175 |
| **phosphorescence** | Lumière & Ombres | 0.5351 | 0.6176 |
| **quiproquo** | Art & Langage | 0.5356 | 0.6178 |
| **obsolescence** | Temps & Éphémère | 0.5357 | 0.6178 |
| **circonspect** | Esprit & Caractère | 0.5380 | 0.6190 |
| **antédiluvien** | Temps & Éphémère | 0.5383 | 0.6191 |
| **coruscant** | Lumière & Ombres | 0.5413 | 0.7407 |
| **paria** | Esprit & Caractère | 0.5432 | 0.6216 |
| **élixir** | Art & Langage | 0.5451 | 0.6225 |

---

## Intrus Score Combiné (40 mots)

| Mot | Thème | Score Final | Sim. Sém. | Freq. | Catégories |
|---|---|---|---|---|---|
| **cors** | Sentiments & Psyché | 0.4055 | 0.5311 | 0.00 | — |
| **jadis** | Temps & Éphémère | 0.4152 | 0.5604 | 4.72 | poétique |
| **varicose** | Sentiments & Psyché | 0.4194 | 0.5588 | 0.00 | — |
| **vate** | Art & Langage | 0.4208 | 0.5615 | 0.00 | — |
| **somatisation** | Sentiments & Psyché | 0.4240 | 0.5680 | 0.00 | — |
| **pétrichor** | Nature & Cosmos | 0.4311 | 0.5823 | 0.00 | — |
| **liminaire** | Art & Langage | 0.4339 | 0.5878 | 0.00 | — |
| **brume** | Nature & Cosmos | 0.4352 | 0.5903 | 4.55 | — |
| **solitaire** | Sentiments & Psyché | 0.4358 | 0.5916 | 4.32 | — |
| **sérendipité** | Philosophie & Idées | 0.4359 | 0.5917 | 0.00 | — |
| **résilience** | Sentiments & Psyché | 0.4383 | 0.5966 | 0.00 | — |
| **viateur** | Esprit & Caractère | 0.4392 | 0.5984 | 0.00 | — |
| **scholastique** | Philosophie & Idées | 0.4410 | 0.6021 | 0.00 | — |
| **parapathique** | Sentiments & Psyché | 0.4413 | 0.6026 | 0.00 | — |
| **chagrin** | Sentiments & Psyché | 0.4415 | 0.6030 | 4.59 | — |
| **némésis** | Philosophie & Idées | 0.4424 | 0.6047 | 0.00 | — |
| **procrastination** | Esprit & Caractère | 0.4425 | 0.6049 | 0.00 | — |
| **noévie** | Sentiments & Psyché | 0.4449 | 0.6097 | 0.00 | — |
| **téléologique** | Philosophie & Idées | 0.4460 | 0.6120 | 0.00 | — |
| **purpurine** | Lumière & Ombres | 0.4472 | 0.6144 | 0.00 | — |
| **crépuscule** | Lumière & Ombres | 0.4479 | 0.6159 | 4.40 | — |
| **mignonne** | Art & Langage | 0.4480 | 0.6160 | 0.00 | — |
| **allitération** | Art & Langage | 0.4497 | 0.6193 | 0.00 | — |
| **rhapsode** | Art & Langage | 0.4511 | 0.6222 | 0.00 | — |
| **cénesthésie** | Sentiments & Psyché | 0.4581 | 0.6362 | 0.00 | — |
| **paresthésie** | Sentiments & Psyché | 0.4582 | 0.6364 | 0.00 | — |
| **gnomique** | Art & Langage | 0.4589 | 0.6378 | 0.00 | — |
| **heuristique** | Philosophie & Idées | 0.4615 | 0.6430 | 0.00 | — |
| **mélancolie** | Sentiments & Psyché | 0.4636 | 0.6472 | 4.32 | — |
| **loquacité** | Art & Langage | 0.4664 | 0.6527 | 0.00 | — |
| **érémitique** | Esprit & Caractère | 0.4674 | 0.6548 | 0.00 | — |
| **stupeur** | Sentiments & Psyché | 0.4687 | 0.6574 | 4.38 | — |
| **garrulité** | Art & Langage | 0.4700 | 0.6601 | 0.00 | — |
| **aphoristique** | Art & Langage | 0.4707 | 0.6615 | 0.00 | — |
| **sépale** | Nature & Cosmos | 0.4716 | 0.6633 | 0.00 | — |
| **pénombre** | Lumière & Ombres | 0.4763 | 0.6727 | 4.45 | — |
| **satyrique** | Art & Langage | 0.4791 | 0.6781 | 0.00 | — |
| **fielueux** | Sentiments & Psyché | 0.4855 | 0.6909 | 0.00 | — |
| **torpeur** | Sentiments & Psyché | 0.5016 | 0.5132 | 4.12 | — |
| **extase** | Sentiments & Psyché | 0.5449 | 0.5999 | 4.02 | — |

---

## Nouveaux Candidats Excellents (83 mots)

Filtrés par score sémantique >= P25 référence ET score fréquence valide :

| Mot | Score Final | Sim. Sém. | Zipf | Ratio | Catégories | Thème Similaire |
|---|---|---|---|---|---|---|
| **acrimonieux** | 0.8479 | 0.6958 | 1.84 | 7.00 | soutenu | Esprit & Caractère |
| **désobligeance** | 0.8207 | 0.6413 | 2.15 | 10.00 | soutenu | Esprit & Caractère |
| **courtisan** | 0.8196 | 0.6393 | 3.32 | 3.17 | soutenu | Esprit & Caractère |
| **extatique** | 0.8147 | 0.6295 | 3.00 | 4.59 | soutenu | Philosophie & Idées |
| **assomption** | 0.8128 | 0.6255 | 2.61 | 10.00 | soutenu | Sentiments & Psyché |
| **glèbe** | 0.8124 | 0.6849 | 2.94 | 1.73 | littéraire | Art & Langage |
| **entéléchie** | 0.8123 | 0.6846 | 1.84 | 10.00 | littéraire | Philosophie & Idées |
| **adulateur** | 0.8112 | 0.6225 | 1.84 | 10.00 | soutenu+poétique | Esprit & Caractère |
| **admonition** | 0.8110 | 0.6219 | 2.15 | 14.00 | soutenu | Sentiments & Psyché |
| **froidure** | 0.8106 | 0.6212 | 3.19 | 14.09 | littéraire+poétique | Sentiments & Psyché |
| **commodité** | 0.8092 | 0.6184 | 3.28 | 7.27 | soutenu+archaïque | Temps & Éphémère |
| **feintise** | 0.8088 | 0.6775 | 1.84 | 10.00 | littéraire | Esprit & Caractère |
| **déparer** | 0.8085 | 0.6170 | 2.67 | 4.27 | soutenu | Sentiments & Psyché |
| **captif** | 0.8065 | 0.6131 | 3.33 | 3.43 | soutenu | Esprit & Caractère |
| **cacochyme** | 0.8053 | 0.6107 | 2.67 | 10.00 | soutenu | Nature & Cosmos |
| **désobligeant** | 0.7993 | 0.5985 | 3.00 | 11.22 | soutenu | Esprit & Caractère |
| **douteur** | 0.7991 | 0.6582 | 2.15 | 10.00 | littéraire | Esprit & Caractère |
| **exultation** | 0.7921 | 0.6441 | 2.83 | 13.60 | littéraire | Sentiments & Psyché |
| **enrhumé** | 0.7895 | 0.6389 | 2.91 | 3.86 | littéraire | Sentiments & Psyché |
| **foucade** | 0.7893 | 0.6386 | 2.53 | 10.00 | littéraire | Lumière & Ombres |
| **déconcerter** | 0.7889 | 0.6379 | 3.15 | 7.47 | littéraire | Sentiments & Psyché |
| **contempteur** | 0.7879 | 0.6358 | 2.43 | 27.00 | littéraire | Esprit & Caractère |
| **fantasquement** | 0.7877 | 0.6354 | 1.84 | 10.00 | littéraire | Sentiments & Psyché |
| **cascadeur** | 0.7853 | 0.6307 | 2.15 | 0.07 | littéraire | Art & Langage |
| **bellement** | 0.7848 | 0.6295 | 2.73 | 3.86 | littéraire | Esprit & Caractère |
| **faunesque** | 0.7841 | 0.6281 | 1.84 | 10.00 | littéraire | Lumière & Ombres |
| **dubitation** | 0.7840 | 0.6280 | 1.84 | 10.00 | littéraire | Sentiments & Psyché |
| **artificieux** | 0.7833 | 0.6267 | 1.84 | 10.00 | littéraire | Esprit & Caractère |
| **accort** | 0.7832 | 0.6264 | 2.43 | 13.50 | littéraire | Esprit & Caractère |
| **crépusculaire** | 0.7825 | 0.6249 | 3.28 | 50.00 | littéraire | Lumière & Ombres |
| **aimeur** | 0.7803 | 0.6206 | 1.84 | 10.00 | littéraire | Art & Langage |
| **chevaucheur** | 0.7800 | 0.6200 | 1.84 | 10.00 | littéraire | Art & Langage |
| **astreinte** | 0.7799 | 0.6197 | 1.84 | 3.50 | littéraire | Esprit & Caractère |
| **chaumine** | 0.7798 | 0.6195 | 2.61 | 3.73 | littéraire | Art & Langage |
| **coutumier** | 0.7788 | 0.6176 | 3.41 | 12.85 | littéraire | Esprit & Caractère |
| **difficultueux** | 0.7777 | 0.6154 | 1.84 | 10.00 | littéraire | Esprit & Caractère |
| **bouvier** | 0.7774 | 0.6149 | 2.83 | 5.67 | littéraire | Esprit & Caractère |
| **barde** | 0.7770 | 0.6139 | 2.61 | 2.28 | littéraire | Art & Langage |
| **coquebin** | 0.7764 | 0.6129 | 2.53 | 10.00 | littéraire | Nature & Cosmos |
| **cétacé** | 0.7760 | 0.6121 | 2.15 | 4.67 | littéraire | Nature & Cosmos |
| **arène** | 0.7751 | 0.6552 | 3.62 | 1.83 | littéraire+poétique | Nature & Cosmos |
| **boiteux** | 0.7751 | 0.6502 | 3.35 | 1.10 | poétique | Esprit & Caractère |
| **balsamique** | 0.7746 | 0.6091 | 2.61 | 4.10 | littéraire | Art & Langage |
| **esseuler** | 0.7740 | 0.6080 | 2.15 | 0.82 | littéraire | Nature & Cosmos |
| **fourrier** | 0.7720 | 0.6041 | 3.23 | 50.00 | littéraire | Esprit & Caractère |
| **bergerie** | 0.7714 | 0.6028 | 3.45 | 2.87 | littéraire | Art & Langage |
| **fleuronner** | 0.7678 | 0.5956 | 1.84 | 10.00 | littéraire | Art & Langage |
| **atlante** | 0.7673 | 0.5946 | 2.43 | 5.40 | littéraire | Nature & Cosmos |
| **artifice** | 0.7666 | 0.6383 | 3.93 | 1.58 | soutenu | Art & Langage |
| **frileux** | 0.7625 | 0.6900 | 3.56 | 4.93 | littéraire | Esprit & Caractère |
| **courtil** | 0.7610 | 0.6220 | 1.84 | 10.00 | poétique | Nature & Cosmos |
| **faucille** | 0.7598 | 0.6195 | 3.35 | 4.37 | poétique | Nature & Cosmos |
| **fondement** | 0.7590 | 0.6180 | 3.39 | 1.38 | poétique | Nature & Cosmos |
| **blâme** | 0.7586 | 0.6821 | 3.61 | 2.19 | littéraire | Art & Langage |
| **chalumeau** | 0.7574 | 0.6148 | 3.37 | 1.48 | poétique | Lumière & Ombres |
| **cavale** | 0.7570 | 0.6190 | 3.63 | 1.10 | littéraire+poétique | Nature & Cosmos |
| **faunesse** | 0.7563 | 0.6126 | 1.84 | 10.00 | poétique | Esprit & Caractère |
| **encor** | 0.7527 | 0.6054 | 2.43 | 0.64 | poétique | Sentiments & Psyché |
| **colombe** | 0.7526 | 0.6101 | 3.54 | 0.69 | littéraire+poétique | Nature & Cosmos |
| **aquilon** | 0.7518 | 0.6035 | 2.67 | 10.00 | poétique | Nature & Cosmos |
| **brisure** | 0.7508 | 0.6016 | 2.87 | 10.00 | poétique | Sentiments & Psyché |
| **fronde** | 0.7497 | 0.5993 | 3.23 | 1.97 | poétique | Esprit & Caractère |
| **commère** | 0.7491 | 0.6582 | 3.39 | 7.15 | archaïque | Art & Langage |
| **hymen** | 0.7487 | 0.5974 | 2.53 | 0.17 | poétique | Nature & Cosmos |
| **fructueux** | 0.7479 | 0.5958 | 3.13 | 4.22 | poétique | Esprit & Caractère |
| **albâtre** | 0.7478 | 0.5955 | 3.47 | 5.21 | poétique | Nature & Cosmos |
| **décombre** | 0.7381 | 0.6411 | 3.82 | 6.01 | littéraire | Sentiments & Psyché |
| **humilié** | 0.7340 | 0.6281 | 3.33 | 1.98 | archaïque | Sentiments & Psyché |
| **disant** | 0.7275 | 0.6200 | 3.85 | 5.54 | littéraire | Sentiments & Psyché |
| **civilement** | 0.7260 | 0.6121 | 2.87 | 18.50 | archaïque | Esprit & Caractère |
| **gerbe** | 0.7230 | 0.6109 | 3.89 | 20.26 | littéraire | Nature & Cosmos |
| **appariteur** | 0.7222 | 0.6044 | 2.53 | 1.21 | archaïque | Esprit & Caractère |
| **gémissement** | 0.7211 | 0.6472 | 3.97 | 9.61 | poétique | Sentiments & Psyché |
| **chaume** | 0.7200 | 0.6449 | 3.69 | 13.50 | poétique | Nature & Cosmos |
| **crapule** | 0.7190 | 0.5980 | 3.40 | 0.56 | archaïque | Nature & Cosmos |
| **dériveur** | 0.7178 | 0.5957 | 2.15 | 1.00 | archaïque | Esprit & Caractère |
| **dépouille** | 0.7177 | 0.6004 | 3.70 | 2.96 | littéraire | Nature & Cosmos |
| **clameur** | 0.7159 | 0.5968 | 3.75 | 7.68 | littéraire | Art & Langage |
| **cygne** | 0.7144 | 0.6338 | 3.67 | 0.88 | poétique | Nature & Cosmos |
| **furie** | 0.7093 | 0.6236 | 3.74 | 2.70 | poétique | Sentiments & Psyché |

---

## Top 50 Mots Tous Confondus (Référence + Nouveaux)

| Rang | Mot | Score | Sim. Sém. | Zipf | Catégories | Réf. |
|---|---|---|---|---|---|---|
| 1 | **acrimonieux** | 0.8479 | 0.6958 | 1.84 | soutenu | — |
| 2 | **laudateur** | 0.8316 | 0.6631 | 1.84 | soutenu | ✓ |
| 3 | **équanimité** | 0.8277 | 0.6553 | 2.15 | soutenu | ✓ |
| 4 | **alacrité** | 0.8274 | 0.6547 | 2.79 | soutenu | ✓ |
| 5 | **simagrée** | 0.8270 | 0.6541 | 3.47 | soutenu | ✓ |
| 6 | **dirimant** | 0.8258 | 0.6515 | 1.84 | soutenu | ✓ |
| 7 | **incurie** | 0.8250 | 0.6499 | 2.83 | soutenu | ✓ |
| 8 | **impéritie** | 0.8212 | 0.6423 | 2.30 | soutenu | ✓ |
| 9 | **désobligeance** | 0.8207 | 0.6413 | 2.15 | soutenu | — |
| 10 | **diaphane** | 0.8199 | 0.6399 | 3.31 | soutenu | ✓ |
| 11 | **courtisan** | 0.8196 | 0.6393 | 3.32 | soutenu | — |
| 12 | **anathème** | 0.8182 | 0.6364 | 2.94 | soutenu | ✓ |
| 13 | **infrangible** | 0.8177 | 0.6355 | 2.43 | soutenu+littéraire | ✓ |
| 14 | **extatique** | 0.8147 | 0.6295 | 3.00 | soutenu | — |
| 15 | **assomption** | 0.8128 | 0.6255 | 2.61 | soutenu | — |
| 16 | **glèbe** | 0.8124 | 0.6849 | 2.94 | littéraire | — |
| 17 | **entéléchie** | 0.8123 | 0.6846 | 1.84 | littéraire | — |
| 18 | **adulateur** | 0.8112 | 0.6225 | 1.84 | soutenu+poétique | — |
| 19 | **admonition** | 0.8110 | 0.6219 | 2.15 | soutenu | — |
| 20 | **froidure** | 0.8106 | 0.6212 | 3.19 | littéraire+poétique | — |
| 21 | **commisération** | 0.8092 | 0.6184 | 3.40 | soutenu | ✓ |
| 22 | **commodité** | 0.8092 | 0.6184 | 3.28 | soutenu+archaïque | — |
| 23 | **valétudinaire** | 0.8092 | 0.6183 | 2.15 | soutenu | ✓ |
| 24 | **feintise** | 0.8088 | 0.6775 | 1.84 | littéraire | — |
| 25 | **déparer** | 0.8085 | 0.6170 | 2.67 | soutenu | — |
| 26 | **pusillanime** | 0.8071 | 0.6741 | 2.91 | littéraire | ✓ |
| 27 | **captif** | 0.8065 | 0.6131 | 3.33 | soutenu | — |
| 28 | **cacochyme** | 0.8053 | 0.6107 | 2.67 | soutenu | — |
| 29 | **longanimité** | 0.8053 | 0.6706 | 2.15 | littéraire | ✓ |
| 30 | **sicaire** | 0.8038 | 0.6676 | 1.84 | littéraire | ✓ |
| 31 | **vespéral** | 0.8031 | 0.6063 | 2.79 | soutenu | ✓ |
| 32 | **thuriféraire** | 0.7995 | 0.5990 | 2.30 | soutenu | ✓ |
| 33 | **désobligeant** | 0.7993 | 0.5985 | 3.00 | soutenu | — |
| 34 | **douteur** | 0.7991 | 0.6582 | 2.15 | littéraire | — |
| 35 | **ménestrel** | 0.7988 | 0.5975 | 2.15 | soutenu | ✓ |
| 36 | **dilection** | 0.7947 | 0.6495 | 2.73 | littéraire | ✓ |
| 37 | **trépas** | 0.7944 | 0.5889 | 3.19 | soutenu | ✓ |
| 38 | **épigone** | 0.7942 | 0.6485 | 2.30 | littéraire | ✓ |
| 39 | **bocage** | 0.7940 | 0.5880 | 3.40 | littéraire+poétique | ✓ |
| 40 | **nimbe** | 0.7935 | 0.6470 | 2.91 | littéraire | ✓ |
| 41 | **égrotant** | 0.7922 | 0.6445 | 2.30 | littéraire | ✓ |
| 42 | **exultation** | 0.7921 | 0.6441 | 2.83 | littéraire | — |
| 43 | **cantilène** | 0.7911 | 0.6422 | 2.53 | littéraire | ✓ |
| 44 | **bocager** | 0.7907 | 0.5814 | 1.84 | littéraire+poétique | — |
| 45 | **empyrée** | 0.7901 | 0.6401 | 2.87 | littéraire | ✓ |
| 46 | **enrhumé** | 0.7895 | 0.6389 | 2.91 | littéraire | — |
| 47 | **foucade** | 0.7893 | 0.6386 | 2.53 | littéraire | — |
| 48 | **déconcerter** | 0.7889 | 0.6379 | 3.15 | littéraire | — |
| 49 | **pugnace** | 0.7889 | 0.6379 | 2.15 | littéraire | ✓ |
| 50 | **contempteur** | 0.7879 | 0.6358 | 2.43 | littéraire | — |
