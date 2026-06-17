# C1 (Vocabulaire Littéraire) — Taxonomie & Algorithmes de Classification

Ce document détaille les variables, les règles d'évaluation heuristiques et l'implémentation de la classification pour les 407 mots de la Word Reserve **C1 (Littéraire)** de Lexica V1.0.

---

## 1. Structure de la Taxonomie Scientifique

Chaque mot de la Word Reserve C1 est caractérisé de manière unique et exclusive selon les variables suivantes :

1.  **Domaine d'Écriture Cible** (`ROMANESQUE`, `THEATRE`, `POESIE`, `ESSAI_PHILOSOPHIQUE`, `CRITIQUE_MEMOIRES`)
2.  **Pôle Sémantique** (`ARTS_ET_LANGAGE`, `ESPRIT_ET_CARACTERE`, `NATURE_ET_COSMOS`, `PHILOSOPHIE_ET_IDEES`, `SENTIMENTS_ET_PSYCHE`)
3.  **Registre & Tonalité** (`LITTERAIRE_STANDARD`, `POETIQUE_LYRIQUE`, `ARCHAIQUE_RECHERCHE`, `TRAGIQUE_DRAMATIQUE`, `COMIC_BURLESQUE`)
4.  **Profil Émotionnel (Valence/Arousal)** (`POSITIF_EXCITANT`, `NEGATIF_EXCITANT`, `POSITIF_CALME`, `NEGATIF_CALME`, `NEUTRE`)
5.  **Époque d'apparition / usage** (`CLASSIQUE_17_18`, `ROMANTIQUE_19`, `MODERNE_20`, `CONTEMPORAIN_21`)
6.  **Difficulté** (Continue de `0.00` à `1.00`)
7.  **Niveau de Pertinence** (Continue de `0.00` à `1.00` — Dynamique)

---

## 2. Algorithmes de Classification Statiques (Heuristiques & Métrique)

Les caractéristiques statiques (1 à 6) sont déterminées par le script `tools/classify_c1_words.py` à l'aide des règles suivantes :

### A. Pôle Sémantique
Mappé à partir des thèmes historiques du projet, avec une conversion explicite des catégories transverses :
- *Lumière & Ombres* est réparti sémantiquement (ex: *aurore, crépuscule* $\rightarrow$ `NATURE_ET_COSMOS` ; *ténébreux, exsangue* $\rightarrow$ `SENTIMENTS_ET_PSYCHE`).
- *Temps & Éphémère* est également ventilé (ex: *millénaire, caduc* $\rightarrow$ `PHILOSOPHIE_ET_IDEES` ; *fugace, réminiscence* $\rightarrow$ `SENTIMENTS_ET_PSYCHE`).

### B. Domaine d'Écriture Cible
Déterminé par des groupes de mots-clés typologiques. En l'absence de correspondance, le mot est classé par défaut en `ROMANESQUE` (style narratif/descriptif général).
- **THEATRE** : Mots liés au dialogue, à l'oralité théâtrale, au conflit interpersonnel (ex: *stichomythie*, *quiproquo*, *goguenard*, *fripon*).
- **POESIE** : Mots à forte connotation sensorielle ou liés à la forme poétique (ex: *allitération*, *lyrisme*, *céruléen*, *ondine*).
- **ESSAI_PHILOSOPHIQUE** : Termes d'abstraction ou d'argumentation théorique (ex: *heuristique*, *contingence*, *solipsisme*).
- **CRITIQUE_MEMOIRES** : Termes décrivant les vices, vertus, et attitudes humaines ou les structures sociales (ex: *acrimonie*, *probité*, *obséquieux*).

### C. Registre, Émotion & Époque
- **Registre** : Inféré via la présence dans des listes ciblées (ex: *sycophante* $\rightarrow$ `ARCHAIQUE_RECHERCHE` ; *spleen* $\rightarrow$ `POETIQUE_LYRIQUE` ; *déliquescence* $\rightarrow$ `TRAGIQUE_DRAMATIQUE`).
- **Profil Émotionnel** : Cartographie des indices de Valence (positif/négatif) et d'Arousal (actif/passif) à partir du lexique psycholinguistique FAN / Bonin.
- **Époque** : Catégorisation temporelle par siècle de première attestation d'usage prédominant (XVII-XVIIIe pour `CLASSIQUE_17_18`, XIXe pour `ROMANTIQUE_19` [par défaut], XXe pour `MODERNE_20`).

### D. Difficulté Continue
Calculée via une formule composite normalisée :
$$Difficulty = Base_{Zipf} + Bonus_{Longueur} + Bonus_{Suffixe} + Bonus_{Morpho}$$

1.  **Base Zipf** : Déduite de la fréquence littéraire $Zipf \in [1.5, 4.3]$ dans Lexique383.
    $$Base_{Zipf} = \frac{4.3 - Zipf}{4.3 - 1.5}$$
    *(Si absent du Lexique383, valeur par défaut de $0.75$).*
2.  **Longueur** : Plus le mot est long, plus sa mémorisation orthographique est complexe.
    $$Bonus_{Longueur} = \min(0.15, (Longueur - 5) \times 0.02)$$
3.  **Suffixe** : $+0.10$ pour les suffixes techniques/abstraits (`-logie`, `-isme`, `-phisme`, etc.).
4.  **Morphologie** : $+0.05$ si le mot contient des lettres rares (`y`, `z`, `k`, `x`, `w`).
5.  **Bornage** : Le score final est bridé entre `0.00` et `1.00`.

---

## 3. Algorithme Dynamique : Niveau de Pertinence

Le **Niveau de Pertinence** est la seule variable dynamique qui évolue en temps réel selon les signaux d'usage collectés auprès de l'utilisateur et de la communauté.

### Formule de Calcul
$$\text{Pertinence}_{\text{mot}} = \frac{N_{\text{ajouts}}}{N_{\text{propositions}}}$$

*   $N_{\text{propositions}}$ : Nombre total de fois où le mot a été proposé à l'utilisateur au sein d'un extrait de lecture/exploration.
*   $N_{\text{ajouts}}$ : Nombre de fois où le mot a été effectivement ajouté à la liste d'apprentissage active de l'utilisateur (soit par clic direct, soit par validation d'une flashcard).

### Intégration dans l'Application (Mobile & Cloud)
1.  **Démarrage (Cold Start)** : Initialisé à une valeur neutre de `0.50` en base de données.
2.  **Seuil de Notabilité** : La formule dynamique ne s'applique qu'après un minimum de **100 présentations cumulées** ($N_{\text{propositions}} \ge 100$) pour éviter les biais de petits échantillons.
3.  **Synchronisation silencieuse** : Les compteurs d'ajouts et de propositions sont persistés dans la base Room locale et synchronisés périodiquement avec Firebase.
4.  **Rôle dans la Recommandation** :
    *   Les mots ayant un score de pertinence élevé ($\ge 0.70$) sont prioritairement proposés aux profils similaires d'utilisateurs.
    *   Les mots avec une pertinence très faible ($\le 0.15$) après le seuil de notabilité sont exclus de la réserve principale de mots et redirigés vers le flux d'exploration rare.
