# Rapport R&D - Validation et Sourcing d'Extraits Pertinents (Pipeline A)
Ce rapport démontre l'algorithme optimisé de validation sémantique par embeddings. 
Il utilise une analyse hybride (sens de la sous-définition Wiktionnaire d'origine + fenêtre glissante locale de la phrase) et s'arrête dès le premier match valide (Performance-First).

---

## Objectif : C1 (Littéraire)
*(Seuil de tolérance sémantique pour cette catégorie : **0.38**)*


### Mot : **céruléen**
- *Définition ciblée* : « D'un bleu pur, semblable à celui du ciel. »

| Exemple d'utilisation candidat | Source / Méthode | Score Combiné | Statut |
|---|---|---|---|
| « Lady Diana entrouvrit ses jolies lèvres, laissa des volutes de fumée céruléenne monter en spirales lentes vers le lustre, et conclut :« Monsieur Varichkine, vous m’effrayez ! » — (Maurice Dekobra, La Madone des sleepings, 1925, réédition Le Livre de Poche, page 85) » | Wiktionary (Sub-def Match: 0.51, Window Match: 0.29) | 0.4425 | Validé ✓ |

👉 **Exemple retenu** : « Lady Diana entrouvrit ses jolies lèvres, laissa des volutes de fumée céruléenne monter en spirales lentes vers le lustre, et conclut :« Monsieur Varichkine, vous m’effrayez ! » — (Maurice Dekobra, La Madone des sleepings, 1925, réédition Le Livre de Poche, page 85) » *(Source: Wiktionary, Score: 0.4425)*

---

### Mot : **glèbe**
- *Définition ciblée* : « Motte de terre, champ cultivé, ou condition de servage attaché à la terre. »

| Exemple d'utilisation candidat | Source / Méthode | Score Combiné | Statut |
|---|---|---|---|
| « En Pologne, chaque paysan, attaché en naissant à la glèbe du maître, cultive pour son propre compte une fraction de cette glèbe, dont il ne doit à son maître qu’une faible redevance, laquelle est un hommage lige plutôt qu’un impôt de quelque valeur. — (François-Vincent Raspail, De la Pologne — Pour une réforme agraire, 1839) » | Wiktionary (Sub-def Match: 0.53, Window Match: 0.54) | 0.5340 | Validé ✓ |

👉 **Exemple retenu** : « En Pologne, chaque paysan, attaché en naissant à la glèbe du maître, cultive pour son propre compte une fraction de cette glèbe, dont il ne doit à son maître qu’une faible redevance, laquelle est un hommage lige plutôt qu’un impôt de quelque valeur. — (François-Vincent Raspail, De la Pologne — Pour une réforme agraire, 1839) » *(Source: Wiktionary, Score: 0.5340)*

---

## Objectif : C2 (Rhétorique)
*(Seuil de tolérance sémantique pour cette catégorie : **0.30**)*


### Mot : **nonobstant**
- *Définition ciblée* : « Malgré, sans avoir égard à. »

| Exemple d'utilisation candidat | Source / Méthode | Score Combiné | Statut |
|---|---|---|---|
| « Suppose que ne sachant pas quoi faire, et nonobstant les recommandations expresses d’Augusta, je te rejoigne dans ta cabine ou que tu me rejoignes dans la mienne, tout le bateau le saura dans les cinq minutes et on ne parlera que de ça au dîner. — (journal 20 minutes, édition Paris-IDF, 31 janvier 2024, pages 29-30) » | Wiktionary (Sub-def Match: 0.72, Window Match: 0.35) | 0.6091 | Validé ✓ |

👉 **Exemple retenu** : « Suppose que ne sachant pas quoi faire, et nonobstant les recommandations expresses d’Augusta, je te rejoigne dans ta cabine ou que tu me rejoignes dans la mienne, tout le bateau le saura dans les cinq minutes et on ne parlera que de ça au dîner. — (journal 20 minutes, édition Paris-IDF, 31 janvier 2024, pages 29-30) » *(Source: Wiktionary, Score: 0.6091)*

---

### Mot : **partant**
- *Définition ciblée* : « Par conséquent, de ce fait, donc. »

| Exemple d'utilisation candidat | Source / Méthode | Score Combiné | Statut |
|---|---|---|---|
| « - Moi, je vais vous dire, je suis partant pour aller becqueter tout de suite . — (Tito Topin, Shanghai Skipper, Série noire, Gallimard, 1986, page 48) » | Wiktionary (Sub-def Match: 0.46, Window Match: 0.40) | 0.4452 | Validé ✓ |

👉 **Exemple retenu** : « - Moi, je vais vous dire, je suis partant pour aller becqueter tout de suite . — (Tito Topin, Shanghai Skipper, Série noire, Gallimard, 1986, page 48) » *(Source: Wiktionary, Score: 0.4452)*

---

## Objectif : C3 (Argot)
*(Seuil de tolérance sémantique pour cette catégorie : **0.22**)*


### Mot : **moula**
- *Définition ciblée* : « Argent, cannabis, ou personne ayant du charisme et du flow. »

| Exemple d'utilisation candidat | Source / Méthode | Score Combiné | Statut |
|---|---|---|---|
| « Au programme de cette combinaison explosive illustrée par un clip stylé : du bif, du bif et encore du bif. Entourés par leurs potes et des jolies créatures, les deux artistes sapés comme des princes prennent du bon temps dans un hôtel particulier et font surtout l’apologie de la moula tout au long de la vidéo. — (Team Mouv', MHD et Ninho prennent du bon temps dans le clip luxueux de "Bénéfice" , www.mouv.fr, 9 mars 2018) » | Wiktionary (Sub-def Match: 0.29, Window Match: 0.09) | 0.2306 | Validé ✓ |

👉 **Exemple retenu** : « Au programme de cette combinaison explosive illustrée par un clip stylé : du bif, du bif et encore du bif. Entourés par leurs potes et des jolies créatures, les deux artistes sapés comme des princes prennent du bon temps dans un hôtel particulier et font surtout l’apologie de la moula tout au long de la vidéo. — (Team Mouv', MHD et Ninho prennent du bon temps dans le clip luxueux de "Bénéfice" , www.mouv.fr, 9 mars 2018) » *(Source: Wiktionary, Score: 0.2306)*

---

### Mot : **seum**
- *Définition ciblée* : « Rancœur, colère, déception intense ou frustration. »

| Exemple d'utilisation candidat | Source / Méthode | Score Combiné | Statut |
|---|---|---|---|
| « alors tu peux bien écouter son p’tit son à la Zazie tu te tapes quand même un putain de seum total quand tu la téma faire ouin-ouin . — (Quentin Leclerc & Michel Pimpant, Les Boloss des belles lettres : La littérature pour tous les waloufs, Éditions J'ai lu, 2013) » | Wiktionary (Sub-def Match: 0.87, Window Match: 0.33) | 0.7100 | Validé ✓ |

👉 **Exemple retenu** : « alors tu peux bien écouter son p’tit son à la Zazie tu te tapes quand même un putain de seum total quand tu la téma faire ouin-ouin . — (Quentin Leclerc & Michel Pimpant, Les Boloss des belles lettres : La littérature pour tous les waloufs, Éditions J'ai lu, 2013) » *(Source: Wiktionary, Score: 0.7100)*

---

## Objectif : C4 (Culture G)
*(Seuil de tolérance sémantique pour cette catégorie : **0.38**)*


### Mot : **lucifuge**
- *Définition ciblée* : « Qui fuit la lumière, qui vit dans l'obscurité. »

| Exemple d'utilisation candidat | Source / Méthode | Score Combiné | Statut |
|---|---|---|---|
| « M. Marcel de Serres prétend que les yeux des insectes lucifuges sont privés de pigmentum ; cette assertion n'est pas exacte : M. Treviranus a vu un pigmentum violet entre les cônes transparens chez la Blatte orientale; . — (M. F. Muller, « Sur les yeux et la vision des Insectes, des Arachnides et des Crustacés », traduit de l'allemand, dans les Annales des sciences naturelles, dirigées par MM. Audouin, Ad. Brongniart & Dumas, tome 18, Paris : chez Crochard, 1829, page 98) » | Wiktionary (Sub-def Match: 0.84, Window Match: 0.35) | 0.6912 | Validé ✓ |

👉 **Exemple retenu** : « M. Marcel de Serres prétend que les yeux des insectes lucifuges sont privés de pigmentum ; cette assertion n'est pas exacte : M. Treviranus a vu un pigmentum violet entre les cônes transparens chez la Blatte orientale; . — (M. F. Muller, « Sur les yeux et la vision des Insectes, des Arachnides et des Crustacés », traduit de l'allemand, dans les Annales des sciences naturelles, dirigées par MM. Audouin, Ad. Brongniart & Dumas, tome 18, Paris : chez Crochard, 1829, page 98) » *(Source: Wiktionary, Score: 0.6912)*

---

### Mot : **callipyge**
- *Définition ciblée* : « Qui possède de belles fesses harmonieuses. »

| Exemple d'utilisation candidat | Source / Méthode | Score Combiné | Statut |
|---|---|---|---|
| « Il y eut à Syracuse deux femmes aux belles fesses, ou callipyges. Ces deux femmes, devenues riches, firent élever un temple à Vénus, qu’elles appelèrent la déesse aux belles fesses, selon ce que dit Archélaüs dans ses Iambes. — (Athénée, Banquet des savants) » | Wiktionary (Sub-def Match: 0.96, Window Match: 0.55) | 0.8371 | Validé ✓ |

👉 **Exemple retenu** : « Il y eut à Syracuse deux femmes aux belles fesses, ou callipyges. Ces deux femmes, devenues riches, firent élever un temple à Vénus, qu’elles appelèrent la déesse aux belles fesses, selon ce que dit Archélaüs dans ses Iambes. — (Athénée, Banquet des savants) » *(Source: Wiktionary, Score: 0.8371)*

---

## Objectif : C5 (Jargon)
*(Seuil de tolérance sémantique pour cette catégorie : **0.35**)*


### Mot : **emphytéose**
- *Définition ciblée* : « Bail immobilier de très longue durée (de 18 à 99 ans) conférant un droit réel au locataire. »

| Exemple d'utilisation candidat | Source / Méthode | Score Combiné | Statut |
|---|---|---|---|
| « Les emphytéoses sont des espèces d’aliénations, à cause de leur longue durée. » | Wiktionary (Sub-def Match: 0.74, Window Match: -0.07) | 0.4936 | Validé ✓ |

👉 **Exemple retenu** : « Les emphytéoses sont des espèces d’aliénations, à cause de leur longue durée. » *(Source: Wiktionary, Score: 0.4936)*

---

### Mot : **blanchir**
- *Définition ciblée* : « Cuisine : Plonger un aliment quelques minutes dans de l'eau bouillante puis glacée pour le cuire légèrement ou en enlever l'âpreté. »

| Exemple d'utilisation candidat | Source / Méthode | Score Combiné | Statut |
|---|---|---|---|
| « Les élégantes composaient les rosés de leur teint à l’aide d’un vermillon habilement nuancé ; le kohl d’Égypte servait à faire ressortir l’éclat des yeux, la pierre ponce calcinée, à blanchir les dents. — (Émile Jonveaux, Curiosités de la toilette - La recherche de la beauté, dans « Musée des familles : lectures du soir » - page 323, 1867) » | Wiktionary (Sub-def Match: 0.13, Window Match: 0.08) | 0.1141 | Rejeté ❌ |
| « Cette terrible histoire des Vaudois, dois-je en parler ou m’en taire ? En parler ? Elle est trop cruelle ; personne ne la racontera sans que la plume n’hésite, et que l’encre, en écrivant, ne blanchisse de larmes. — (Jules Michelet, Le prêtre, la femme, la famille, Paris : Chamerot, 1862 (8e édition), page 23) » | Wiktionary (Sub-def Match: 0.13, Window Match: 0.08) | 0.1138 | Rejeté ❌ |
| « Un oratoire (msalla), simple mur en maçonnerie blanchi à la chaux, avait été construit à la hâte sur une colline voisine, toute blanche de pâquerettes. — (Frédéric Weisgerber, Trois mois de campagne au Maroc : étude géographique de la région parcourue, Paris : Ernest Leroux, 1904, page 132) » | Wiktionary (Sub-def Match: 0.13, Window Match: 0.03) | 0.0981 | Rejeté ❌ |
| « L’action de blanchir une matière lui permet d’acquérir la couleur blanche telle que nous la reconnaissons communément. » | Wiktionary (Sub-def Match: 0.31, Window Match: 0.27) | 0.2961 | Rejeté ❌ |
| « Le textile et le papier sont chimiquement blanchis pour éviter qu’ils soient jaunâtre. » | Wiktionary (Sub-def Match: 0.31, Window Match: 0.13) | 0.2533 | Rejeté ❌ |
| « Les détergents permettent de blanchir le carrelage. » | Wiktionary (Sub-def Match: 0.31, Window Match: 0.19) | 0.2714 | Rejeté ❌ |
| « Blanchir du linge. » | Wiktionary (Sub-def Match: 0.31, Window Match: 0.23) | 0.2819 | Rejeté ❌ |
| « Donner du linge à blanchir. » | Wiktionary (Sub-def Match: 0.31, Window Match: 0.20) | 0.2727 | Rejeté ❌ |
| « Blanchir quelqu’un, blanchir son linge. » | Wiktionary (Sub-def Match: 0.31, Window Match: 0.21) | 0.2773 | Rejeté ❌ |
| « La nuit venue, Jeanne alla au pré ramasser des pièces de toile neuve qu’elle y faisait blanchir, et qu’elle y laissait souvent la nuit impunément. — (George Sand, Jeanne, 1844) » | Wiktionary (Sub-def Match: 0.31, Window Match: 0.12) | 0.2501 | Rejeté ❌ |
| « Marthe était blanchisseuse et travaillait dans l’atelier où Blanche faisait son apprentissage. Comme elle le disait, on avait voulu que Blanche pût blanchir les autres. — (Charles-Louis Philippe, Bubu de Montparnasse, 1901, réédition Garnier-Flammarion, page 72) » | Wiktionary (Sub-def Match: 0.31, Window Match: 0.14) | 0.2551 | Rejeté ❌ |
| « Si l’on blanchit trop ce chou , il se réduira en bouillie en le cuisant pour la table : et s’il ne l’est pas suffisamment, on ne pourra le cuire assez pour le rendre mangeable; l’expérience seule apprend le point juste ; . — (François Rozier, Cours complet d’agriculture ou Nouveau dictionnaire d’agriculture, Paris: Pourrat Frères, 1834, volume 6, page 361) » | Wiktionary (Sub-def Match: 0.77, Window Match: 0.35) | 0.6429 | Validé ✓ |

👉 **Exemple retenu** : « Si l’on blanchit trop ce chou , il se réduira en bouillie en le cuisant pour la table : et s’il ne l’est pas suffisamment, on ne pourra le cuire assez pour le rendre mangeable; l’expérience seule apprend le point juste ; . — (François Rozier, Cours complet d’agriculture ou Nouveau dictionnaire d’agriculture, Paris: Pourrat Frères, 1834, volume 6, page 361) » *(Source: Wiktionary, Score: 0.6429)*

---