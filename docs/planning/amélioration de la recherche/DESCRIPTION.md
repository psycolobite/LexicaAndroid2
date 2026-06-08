# Description — Amélioration de la recherche

## 1. Rôle de ce document

Ce document sert de brief produit et fonctionnel pour le prochain chantier de recherche.

Son objectif est de permettre au prochain Chef d'Orchestre de comprendre rapidement :
- l'état actuel du module de recherche,
- la direction produit visée,
- les nouvelles fonctionnalités principales,
- les contraintes à respecter avant de découper le travail en tâches agents.

Le découpage opérationnel viendra ensuite dans `PLAN.md`.

---

## 2. État actuel à préserver

Aujourd'hui, la recherche est principalement pensée comme un écran spécialisé d'ajout de mots.

L'utilisateur peut déjà :
- parcourir une liste de mots générée automatiquement,
- ajouter des mots depuis cette liste,
- utiliser une barre de recherche qui interroge une API pour récupérer la définition d'un mot,
- ajouter manuellement une flashcard en remplissant le recto et le verso.

Ces fonctionnalités ne doivent pas disparaître.

En revanche, elles ne doivent plus constituer le coeur de l'expérience. Elles deviennent des modes annexes d'ajout de mots, accessibles à la demande par l'utilisateur.

---

## 3. Changement de logique produit

La recherche ne doit plus être conçue seulement comme un outil pour "trouver un mot".

La fonction principale doit devenir une expérience de découverte contextuelle de vocabulaire, guidée par les intérêts de l'utilisateur.

L'idée centrale est la suivante :
- proposer à l'utilisateur des extraits courts mais pertinents,
- faire apparaître dans ces extraits des mots intéressants à apprendre,
- laisser l'utilisateur sélectionner les mots qu'il souhaite ajouter,
- utiliser ses choix pour améliorer progressivement les propositions suivantes.

Autrement dit, le futur module recherche devient un module d'exploration et de sélection de vocabulaire en contexte.

---

## 4. Expérience cible

### 4.1 Fonction principale

L'écran de recherche doit, à terme, proposer en priorité :
- des passages de texte courts,
- des extraits vidéo avec sous-titres,
- potentiellement d'autres formats contextuels plus tard si cela a du sens.

Dans chaque extrait, un ou plusieurs mots pertinents doivent pouvoir être mis en avant pour apprentissage.

L'utilisateur peut alors :
- ajouter un mot à son deck,
- retirer un mot présélectionné s'il ne l'intéresse pas,
- noter l'extrait,
- indiquer implicitement ou explicitement que le contenu lui correspond ou non.

### 4.2 Fonction secondaire

Les fonctionnalités actuelles d'ajout direct restent disponibles dans un espace secondaire clairement identifiable :
- liste de mots suggérés automatiquement,
- recherche de mot par API,
- création manuelle de flashcard.

Le principe UX est donc :
- coeur d'expérience = découverte par extraits,
- outils d'appoint = ajout manuel ou recherche ciblée.

### 4.3 Structure cible de la page recherche

La page recherche devra s'organiser autour d'une hiérarchie simple :
- une très grande zone centrale consacrée à l'extrait courant,
- un mode de navigation rapide entre extraits,
- une zone secondaire en bas pour accéder aux autres outils de recherche.

La structure visée de manière précise est la suivante :
- en arrivant sur la page recherche, l'utilisateur voit immédiatement un extrait déjà affiché,
- la plus grande partie de l'écran est réservée à cet extrait,
- l'extrait peut être textuel ou vidéo avec sous-titres,
- les mots jugés potentiellement intéressants sont surlignés dans l'extrait,
- tout en bas, une barre divisée en deux entrées permet d'accéder aux fonctions secondaires :
	- `Chercher des ouvrages`
	- `Rechercher des mots`

Le bouton `Chercher des ouvrages` doit ouvrir un catalogue d'ouvrages ou de contenus complets.

À terme, ce catalogue devra lui aussi être ordonné en priorité selon les préférences détectées chez l'utilisateur.

Le bouton `Rechercher des mots` doit rediriger vers le système actuel d'ajout de mots, c'est-à-dire la page de recherche déjà existante avec :
- la liste de mots générée automatiquement,
- la recherche API de mots,
- l'ajout manuel de flashcards.

Cette page actuelle doit donc être conservée comme un sous-espace spécialisé, et non plus comme le coeur de l'expérience.

---

## 5. Personnalisation au premier lancement

Lors de la première ouverture de l'application, l'utilisateur doit être invité à préciser pourquoi il veut enrichir son vocabulaire.

Cette étape sert à orienter les premiers contenus proposés.

Les catégories initiales à prévoir sont, au minimum :
- mieux tenir des conversations plus soutenues,
- mieux comprendre des ouvrages complexes dans des domaines qui l'intéressent,
- découvrir ou apprendre des dialectes, argots ou usages situés,
- explorer le vocabulaire d'un domaine particulier.

### 5.1 Exemples de formulations possibles côté produit

- "Je veux mieux m'exprimer dans des conversations soutenues"
- "Je veux mieux comprendre des textes exigeants"
- "Je veux découvrir des façons de parler particulières"
- "Je veux apprendre le vocabulaire d'un domaine précis"

### 5.2 Remarque produit

Cette liste n'est pas figée.

Elle constitue une première base de cadrage. Elle devra ensuite être affinée par retours réels, entretiens utilisateurs et itérations produit.

---

## 6. Comment les propositions doivent être choisies

Les extraits proposés doivent être cohérents avec :
- les objectifs choisis au premier lancement,
- les interactions ultérieures de l'utilisateur,
- les mots qu'il ajoute ou retire,
- les notes qu'il attribue aux extraits,
- les domaines qu'il semble privilégier dans la durée.

Exemples :
- si l'utilisateur ajoute souvent des mots techniques liés à la cuisine, les extraits liés à la cuisine doivent être davantage proposés,
- s'il ignore systématiquement certains registres ou certaines sources, leur fréquence doit diminuer,
- s'il note favorablement des extraits de littérature, l'algorithme doit augmenter ce type de propositions.

Le système doit donc combiner deux niveaux :
- une orientation initiale explicite par préférences déclarées,
- un affinage progressif par comportement observé.

Une autre stratégie possible doit aussi être retenue dès le cadrage :
- partir non pas d'un extrait candidat, mais d'une liste de mots cibles susceptibles d'intéresser l'utilisateur,
- générer cette liste de mots à partir de ses préférences, de ses thèmes et de ses comportements,
- rechercher ensuite ces mots dans des ouvrages, entretiens, archives ou autres corpus,
- proposer enfin des extraits construits autour de ces mots cibles.

Autrement dit, le système peut fonctionner dans les deux sens :
- approche `extrait -> mots intéressants`,
- approche `mots cibles -> extraits d'usage`.

Les détails de mise en oeuvre sont à formaliser dans `PLAN.md`, notamment pour éviter une dépendance coûteuse à un appel IA sur chaque extrait.

---

## 7. Types de contenus visés

### 7.1 Extraits de texte

Le module doit pouvoir proposer :
- des passages de livres,
- des extraits d'entretiens,
- des textes de réflexion,
- des contenus documentaires ou spécialisés,
- des corpus pertinents selon les objectifs choisis.

Chaque extrait doit idéalement permettre :
- l'identification claire du ou des mots intéressants,
- la compréhension du contexte d'usage,
- un accès ultérieur à la source complète quand c'est possible.

### 7.2 Extraits vidéo avec sous-titres

Le module doit aussi pouvoir proposer des extraits vidéo sous-titrés lorsque la source le permet.

Exemples possibles :
- interviews,
- conférences,
- archives audiovisuelles,
- contenus documentaires,
- autres sources compatibles avec l'objectif d'apprentissage du vocabulaire.

L'intérêt de ce format est double :
- fournir du vocabulaire en contexte oral réel,
- conserver l'appui du sous-titre pour repérer facilement les mots utiles.

---

## 8. Accès à la source complète et aux contenus complets

Chaque fois que c'est possible, un extrait doit pointer vers la ressource complète.

Exemples :
- un extrait de livre renvoie vers le livre complet,
- un extrait d'interview renvoie vers l'interview complète,
- un extrait d'archive renvoie vers sa source ou sa fiche complète,
- un extrait vidéo renvoie vers la vidéo ou la ressource intégrale consultable.

L'objectif n'est pas seulement de montrer un mot isolé, mais aussi de permettre une exploration culturelle ou documentaire plus large à partir de l'extrait.

Cette continuité entre l'extrait et la source complète doit, lorsque c'est possible, rester accessible dans l'application ou via un lien clair vers la source.

En complément, l'application doit à terme offrir un accès de consultation à certains contenus entiers lorsque cela est juridiquement et techniquement possible.

Exemples de cibles :
- livres du domaine public,
- films anciens ou archives librement consultables,
- documents culturels ou patrimoniaux accessibles légalement,
- autres corpus utiles à l'apprentissage du vocabulaire.

Cette fonctionnalité ne remplace pas le moteur d'extraits, mais l'étend.

Elle permettrait à l'utilisateur :
- de lire un ouvrage entier,
- d'explorer un corpus source,
- de continuer son apprentissage au-delà de l'extrait recommandé.

---

## 9. Contraintes légales et éditoriales

Le chantier doit intégrer une vraie vigilance sur les droits d'usage.

Questions à encadrer explicitement dans la suite du projet :
- peut-on afficher librement un extrait ?
- à quelles conditions peut-on afficher la source complète ?
- quelles sources sont libres de droit, sous licence compatible, ou consultables légalement par simple lien ?
- faut-il distinguer les contenus hébergés, les contenus seulement référencés, et les contenus accessibles via redirection ?

À ce stade, la direction à retenir est prudente :
- privilégier d'abord les contenus libres de droit ou juridiquement sûrs,
- n'intégrer les autres cas que si le modèle d'accès est clairement légal et maîtrisé,
- documenter ultérieurement les familles de sources autorisées.

---

## 10. Signal utilisateur à exploiter

Le futur module devra pouvoir apprendre à partir de plusieurs signaux simples :
- mots ajoutés,
- mots retirés ou ignorés,
- note donnée à un extrait,
- récurrence d'intérêt pour un domaine,
- préférence implicite pour certains formats ou certaines sources.

Ces signaux ne doivent pas servir uniquement à personnaliser un mot isolé, mais à affiner progressivement :
- les thèmes proposés,
- les types de sources,
- le niveau de difficulté,
- le registre de langue,
- les domaines privilégiés.

### 10.1 Profil d'intérêt par domaine

Le module devra à terme maintenir un profil d'intérêt de l'utilisateur par domaine, registre ou grande famille thématique.

Ce profil ne doit pas être pensé comme une simple valeur statique enregistrée une fois pour toutes.

Il doit combiner :
- les choix initiaux déclarés par l'utilisateur au premier lancement,
- les mots qu'il ajoute ou retire,
- les extraits qu'il note positivement ou négativement,
- les ouvrages qu'il ouvre ou consulte,
- les interactions répétées qui confirment un intérêt durable.

Le score final d'intérêt ne doit pas être la donnée source de vérité.

La source de vérité doit plutôt être un ensemble d'événements utilisateur synchronisables, à partir desquels le profil d'intérêt peut être recalculé.

Autrement dit :
- on synchronise les événements d'intérêt,
- on dérive ensuite localement ou côté moteur un `interestScore` par domaine,
- on évite ainsi les conflits absurdes entre appareils et les écrasements de score.

Cette logique est cohérente avec la personnalisation future du moteur de recherche et avec une synchronisation multi-appareils plus robuste.

---

## 11. Impact attendu sur l'interface

L'interface actuelle de recherche devra être repensée pour intégrer cette nouvelle hiérarchie de fonctionnalités sans casser les usages existants.

Le futur écran devra rendre intuitivement compréhensibles :
- la découverte d'extraits recommandés,
- la sélection de mots dans un extrait,
- l'accès à la source complète,
- l'accès secondaire aux outils classiques d'ajout de mots,
- la logique de personnalisation selon les intérêts de l'utilisateur.

L'objectif n'est pas seulement fonctionnel. L'écran doit aussi devenir plus lisible, plus cohérent et plus esthétique.

La refonte devra donc traiter à la fois :
- la page de recherche,
- la hiérarchie visuelle,
- la clarté des actions,
- la compréhension immédiate du rôle de chaque zone de l'écran.

Comportements d'interaction à prévoir :
- les mots potentiellement intéressants sont surlignés,
- un tap court sur un mot surligné l'ajoute à la liste de l'utilisateur,
- après ajout, le surlignage du mot change visuellement pour indiquer que l'ajout a été pris en compte,
- un nouveau tap sur ce mot peut permettre de le retirer si l'utilisateur revient sur son choix,
- un appui long sur un mot surligné affiche sa définition,
- un appui long sur un autre mot non présélectionné doit aussi permettre d'afficher sa définition et de proposer son ajout,
- l'utilisateur doit pouvoir naviguer rapidement d'un extrait à l'autre,
- cette navigation peut être pensée soit comme un swipe latéral gauche/droite, soit comme un défilement vertical de type feed,
- si un signal explicite supplémentaire est conservé, il peut prendre la forme d'une estimation d'intérêt de `0` à `5` en bas de l'écran.

L'interface devra faire attention à une contrainte importante :
- ne pas accumuler plusieurs signaux d'intérêt redondants sans nécessité,
- choisir une hiérarchie claire entre geste de navigation, action sur les mots et éventuelle note explicite.

---

## 12. Contraintes de continuité produit

Le nouveau module doit s'insérer dans l'application actuelle sans casser :
- les fonctionnalités existantes d'ajout de mots,
- la création manuelle de flashcards,
- les flux déjà présents autour de la recherche et du deck utilisateur,
- la compréhension globale de l'application pour les utilisateurs actuels.

Le chantier est donc une évolution structurante, pas une suppression brutale de l'existant.

---

## 13. Résumé décisionnel

À retenir pour le prochain Chef d'Orchestre :

- conserver le moteur actuel de recherche et d'ajout, mais le repositionner comme fonctionnalité annexe,
- faire des extraits textuels et vidéo le coeur du futur module recherche,
- personnaliser les propositions à partir des objectifs utilisateur puis de ses interactions réelles,
- prévoir un accès à la source complète chaque fois que cela est légalement et techniquement possible,
- penser en parallèle le moteur de recommandation, les sources, les droits d'usage et la nouvelle interface,
- préparer une passation claire vers un découpage multi-agents sans perdre la cohérence produit.

---

## 14. Points encore ouverts

Ces points ne bloquent pas la rédaction du plan, mais devront être clarifiés :
- liste initiale exacte des objectifs proposés au premier lancement,
- sources textuelles et vidéo prioritaires,
- stratégie juridique par type de contenu,
- niveau de personnalisation à implémenter dans une première version,
- place précise de la consultation de corpus complets dans le MVP,
- architecture UX finale de l'écran de recherche refondu.