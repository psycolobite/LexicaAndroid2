# Plan — Amélioration de la recherche

## 1. Rôle de ce document

Ce document sert de plan de mise en route pour le prochain Chef d'Orchestre.

Il ne décrit pas seulement quoi faire, mais aussi des pistes réalistes de réalisation pour les parties les plus difficiles du chantier.

Objectif : préparer un découpage multi-agents cohérent, avec un cap technique défendable et un ordre d'exécution raisonnable.

---

## 2. Principes de mise en oeuvre

Le chantier doit suivre 4 principes :
- préserver l'existant et le repositionner au lieu de le casser,
- éviter une architecture dépendante d'appels IA coûteux à chaque interaction,
- séparer clairement ingestion des contenus, sélection des extraits et personnalisation utilisateur,
- valider rapidement une première boucle produit avant de raffiner l'algorithme.

---

## 3. Les 2 difficultés principales à traiter d'emblée

### 3.1 Faire concorder automatiquement les extraits avec les choix utilisateur

C'est un point central et potentiellement coûteux si le tri dépend trop d'une IA appelée à la volée.

La direction à privilégier est donc une approche majoritairement automatique et prétraitée.

Piste de réalisation :
- constituer des corpus sources structurés par familles,
- pré-extraire des passages candidats hors ligne,
- annoter ou scorer ces passages avec des signaux calculables sans appel IA temps réel,
- ne réserver l'IA qu'à des tâches limitées, ponctuelles, ou de préparation offline si besoin.

### 3.2 Présenter les extraits dans un ordre qui s'affine avec l'usage

L'autre difficulté est l'algorithme de présentation des extraits.

La bonne analogie n'est pas un moteur de dictionnaire classique, mais un système de ranking proche d'un feed :
- type TikTok,
- type YouTube,
- ou plus largement un moteur de recommandation de scroll.

L'idée n'est pas de copier une logique réseau social dans son intensité, mais de reprendre les principes utiles :
- scoring des candidats,
- mélange entre exploration et exploitation,
- prise en compte des signaux utilisateur,
- ajustement progressif du ranking.

---

## 4. Pistes de réalisation pour le moteur de sélection d'extraits

### 4.1 Pipeline recommandé

Le pipeline cible devrait être découpé comme suit :

1. ingestion des sources,
2. normalisation des métadonnées,
3. découpage en passages candidats,
4. calcul de signaux automatiques,
5. indexation,
6. ranking des extraits selon le profil utilisateur,
7. retour d'interactions utilisateur vers le moteur de score.

Il faut cependant prévoir dès le départ deux variantes de pipeline :

### Variante A — partir des extraits
- on découpe les corpus en passages candidats,
- on détecte dans chaque passage les mots potentiellement intéressants,
- on score ensuite l'extrait selon sa compatibilité avec le profil utilisateur.

### Variante B — partir des mots cibles
- on génère d'abord une liste de mots susceptibles d'intéresser l'utilisateur,
- cette liste est déduite de ses préférences déclarées, de ses thèmes et de ses comportements,
- on recherche ensuite ces mots dans les corpus disponibles,
- on remonte enfin les extraits dans lesquels ces mots apparaissent en contexte.

Cette deuxième variante est importante, car elle peut être plus simple à contrôler dans certains cas :
- elle permet de piloter directement le vocabulaire cible,
- elle facilite la recherche de contextes d'usage pour un ensemble de mots déjà jugés intéressants,
- elle peut constituer une bonne base pour une V1 si la détection automatique de "bons extraits" s'avère trop floue.

### 4.2 Signaux automatiques à calculer sans IA temps réel

Chaque extrait candidat peut être enrichi avec des métadonnées calculables automatiquement :
- source,
- type de contenu,
- domaine estimé,
- niveau de difficulté,
- densité en mots potentiellement intéressants,
- registre supposé,
- longueur,
- clarté du contexte autour du mot,
- présence d'un lien vers la source complète,
- disponibilité juridique du contenu.

### 4.3 Première stratégie de matching simple

Pour une V1, il faut viser un moteur sobre et explicable.

Exemple de logique :
- mapper les objectifs initiaux utilisateur vers quelques thèmes internes,
- taguer les sources et extraits selon ces mêmes thèmes,
- attribuer un score de base par proximité thème-utilisateur,
- ajouter des bonus ou malus selon les actions réelles de l'utilisateur.

Cette logique peut s'appliquer à deux niveaux différents :
- soit pour scorer directement un extrait candidat,
- soit pour scorer d'abord une liste de mots cibles, puis récupérer des extraits d'usage associés à ces mots.

Une piste pratique pour la variante `mots cibles -> extraits` est la suivante :
- produire une liste pondérée de mots potentiellement utiles,
- chercher leurs occurrences dans un corpus indexé,
- filtrer les occurrences avec des critères simples de qualité de contexte,
- ne proposer que les passages où l'usage du mot est suffisamment clair et exploitable.

Forme simple de score initial :

$$
score = affiniteTheme + affiniteDomaine + bonusSource + bonusNiveau + bonusComportement
$$

Cette formule n'est pas un choix final, mais un bon point de départ parce qu'elle est explicable, peu coûteuse et améliorable.

### 4.4 Usage éventuel d'IA

L'IA peut rester utile, mais à des endroits limités :
- aide à la classification initiale de gros corpus,
- suggestion ponctuelle de tags ou de résumés de passages,
- aide au contrôle qualité éditorial.

En revanche, il faut éviter une dépendance où chaque consultation d'extrait déclenche une requête coûteuse.

---

## 5. Pistes de réalisation pour l'algorithme de présentation

### 5.1 Objectif

À chaque ouverture de l'écran recherche, l'utilisateur doit voir un extrait immédiatement pertinent, puis pouvoir naviguer facilement vers d'autres propositions.

### 5.2 Stratégie de ranking inspirée des feeds

La logique recommandée est :
- générer un pool restreint de candidats déjà compatibles avec le profil,
- scorer ce pool,
- injecter un peu d'exploration pour éviter de toujours montrer le même type de contenu,
- réordonner selon les retours utilisateur.

Exemples de signaux utilisateur à intégrer :
- mot ajouté depuis l'extrait,
- mot retiré ou ignoré,
- note explicite de 0 à 5,
- temps passé sur l'extrait,
- ouverture ou non de la source complète,
- navigation rapide vers l'extrait suivant,
- récurrence d'intérêt pour un domaine ou un format.

### 5.3 Règle de base utile pour la V1

Il faut probablement distinguer :
- exploitation : montrer davantage ce qui marche déjà,
- exploration : tester de nouveaux domaines proches ou voisins.

Une règle simple possible :
- 70 % d'extraits fortement compatibles,
- 20 % d'extraits voisins,
- 10 % d'extraits exploratoires.

Cette répartition est indicative. Elle donne un cadre testable avant toute sophistication supplémentaire.

### 5.4 Inspiration open source à rechercher

Le prochain Chef d'Orchestre devrait prévoir une courte phase de recherche ciblée sur GitHub pour identifier des repositories utiles sur :
- systèmes de recommandation légers,
- ranking de contenus,
- exploration/exploitation,
- feeds scrollables Android/Compose,
- visualisation d'extraits textuels ou vidéo avec interactions.

Le but est de s'inspirer des patterns de conception, pas de recopier une implémentation opaque ou surdimensionnée.

---

## 5 bis. Pistes de réalisation pour le profil d'intérêt utilisateur

### 5.5 Principe

Le futur moteur de recherche devra s'appuyer sur un profil d'intérêt par domaine, mais ce profil ne doit pas être stocké comme une simple valeur finale écrasée à chaque synchronisation.

La stratégie la plus robuste est :
- synchroniser les événements qui font évoluer l'intérêt,
- recalculer ensuite un score agrégé par domaine.

### 5.6 Source de vérité recommandée

La source de vérité doit être composée de signaux tels que :
- choix initiaux de l'utilisateur,
- mot ajouté,
- mot retiré,
- extrait noté,
- ouvrage ouvert,
- consultation répétée d'un domaine,
- désintérêt explicite ou implicite pour un type de contenu.

Le `interestScore` final devient alors une vue dérivée et recalculable, non une donnée maîtresse difficile à fusionner.

### 5.7 Intérêt de cette approche

Cette approche :
- évite les conflits de synchronisation entre appareils,
- permet une évolution progressive du profil,
- facilite le recalcul si les règles de scoring changent,
- reste cohérente avec une architecture de recommandation plus robuste à long terme.

### 5.8 Effet sur le chantier

Le prochain Chef d'Orchestre devra donc prévoir, dans le chantier recherche, un lot ou sous-lot dédié à :
- la définition des événements d'intérêt,
- le tagging des mots, extraits et ouvrages par domaine,
- la formule de calcul du profil agrégé.

---

## 6. Structure fonctionnelle cible de la page recherche

### 6.1 Hiérarchie générale

L'écran recherche doit être pensé comme une page principale de découverte.

Structure visée :
- en ouverture, un extrait est déjà affiché sans action préalable de l'utilisateur,
- une zone dominante affichant l'extrait courant occupe la majeure partie de l'écran,
- une logique de navigation entre extraits permet de passer rapidement au contenu suivant,
- une barre basse secondaire divisée en deux entrées :
	- `Chercher des ouvrages`
	- `Rechercher des mots`

Règle produit importante : la page recherche principale ne doit plus être une page de recherche de mot classique. Elle doit d'abord être une page de découverte d'extraits.

### 6.2 Comportement dans l'extrait

Les mots potentiellement intéressants doivent être surlignés.

Interactions proposées :
- tap court sur mot surligné : ajoute le mot à la liste utilisateur,
- après ajout, le style du surlignage change pour montrer que le mot a déjà été sélectionné,
- second tap sur ce mot : retire le mot si l'utilisateur change d'avis,
- appui long sur mot surligné : affiche une définition rapide,
- appui long sur un mot non présélectionné : affiche aussi une définition et propose l'ajout du mot.

Il faut donc prévoir dès la conception UI :
- un état visuel "mot suggéré",
- un état visuel "mot ajouté",
- une interaction simple pour ne pas casser la lecture de l'extrait.

### 6.3 Signal d'intérêt pour l'extrait

Le comportement cible demandé à ce stade est le suivant :
- l'utilisateur doit pouvoir passer rapidement à un autre extrait,
- le geste principal peut être soit un swipe gauche/droite, soit un scroll vertical de type feed,
- ce choix devra être tranché pendant la phase de prototypage UX,
- l'interaction retenue ne devra pas entrer en conflit avec le tap et l'appui long sur les mots.

En complément, une réglette ou une action simple de notation en bas d'écran peut permettre d'indiquer un intérêt de `0` à `5`.

Ce point devra être testé rapidement car il peut devenir redondant si trop de signaux sont demandés en même temps.

Règle de conception :
- ne pas cumuler inutilement swipe, scroll et note explicite si cela alourdit la compréhension,
- garder un signal principal clair et un signal secondaire seulement s'il apporte une vraie valeur.

### 6.4 Accès secondaire

`Chercher des ouvrages` ouvre un catalogue de contenus complets.

À terme, le haut de ce catalogue doit aussi proposer en priorité des livres, entretiens, archives ou autres contenus cohérents avec les préférences déjà inférées.

`Rechercher des mots` ouvre le système actuel de recherche et d'ajout de mots, conservé comme mode secondaire.

Ce sous-espace secondaire correspond explicitement à la page actuelle de recherche de mots, déjà jugée utile et bien faite.

---

## 7. Ordre de réalisation recommandé

### Phase 1 — cadrage et architecture
- figer le parcours cible de l'écran recherche,
- définir les types de sources autorisées en priorité,
- définir les métadonnées minimales par extrait,
- définir le modèle de signaux utilisateur.

### Phase 2 — moteur de contenu V1
- construire un petit corpus pilote,
- produire un pipeline de découpage d'extraits,
- calculer un premier score automatique par thème/domaine/niveau,
- servir des extraits pertinents sans personnalisation complexe.

### Phase 3 — UX interactive V1
- intégrer l'écran principal d'extraits,
- ajouter le surlignage et les interactions sur les mots,
- conserver un accès clair au système actuel d'ajout de mots,
- brancher un premier signal d'intérêt utilisateur.

### Phase 4 — personnalisation progressive
- mémoriser les interactions utilisateur,
- faire évoluer le ranking avec ces signaux,
- introduire exploration/exploitation,
- ajuster la proposition d'ouvrages complets.

### Phase 5 — approfondissement
- étendre les sources,
- traiter les extraits vidéo avec sous-titres,
- affiner le juridique par famille de contenu,
- raffiner le ranking et la qualité éditoriale.

---

## 8. Répartition possible pour un pilotage multi-agents

Le prochain Chef d'Orchestre peut probablement découper le chantier en lots relativement indépendants :
- lot produit et UX de l'écran recherche,
- lot pipeline de contenus et métadonnées,
- lot moteur de scoring et de recommandation,
- lot catalogue d'ouvrages et accès source complète,
- lot droits d'usage et stratégie de sources,
- lot intégration Android et conservation des flux existants.

Le plus important est de séparer tôt :
- la logique de contenu,
- la logique de ranking,
- la logique d'interface,
- la logique juridique/source.

---

## 9. Risques principaux

- vouloir construire trop tôt un moteur de recommandation trop "intelligent" et trop opaque,
- dépendre d'appels IA coûteux pour des tâches répétitives,
- mélanger extraction de contenu, personnalisation et UI dans un seul lot,
- créer une interface trop chargée en signaux et gestes concurrents,
- sous-estimer les contraintes de droits sur les sources complètes.

---

## 10. Résultat attendu de ce plan

À l'issue de la prochaine passation, le Chef d'Orchestre doit pouvoir :
- donner à chaque agent un périmètre clair,
- lancer une V1 techniquement réaliste,
- garder le chantier aligné avec la vision produit,
- éviter les choix techniques coûteux ou prématurés,
- préserver la cohérence avec l'application actuelle.