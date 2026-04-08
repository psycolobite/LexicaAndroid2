# 🔭 VISION FUTURE — Lexica Android
> Section datée : **2026-04-08**  
> Statut : **Cadrage produit consolidé — QC2 & QC3 validées, QC1 en attente**

---

## 1. Définition précise du produit

Lexica n'est **pas seulement une app de flashcards**.  
Lexica est une **application d'enrichissement du vocabulaire français** qui combine :

1. **Découverte contextualisée** de mots utiles, rares ou spécialisés
2. **Sélection thématique** selon les centres d'intérêt ou domaines de l'utilisateur
3. **Validation active** avant intégration au deck
4. **Consolidation long terme** via l'algo **POLO-1**
5. **Montée en maîtrise réelle** par des exercices de compréhension et d'usage

### Promesse produit
Permettre à un utilisateur francophone de développer un vocabulaire plus riche, plus précis et mieux utilisable **dans des domaines choisis**, en apprenant les mots **en situation**, puis en les réactivant durablement.

### Positionnement
- Plus riche et contextualisé qu'un deck de cartes classique
- Plus sérieux sur le fond qu'une app de mini-jeux seule
- Plus orienté **usage réel du mot** qu'un simple apprentissage définition ↔ mot

---

## 2. Question stratégique : faut-il lancer l'app avant de poursuivre cette vision ?

### Réponse courte
**Non, pas en lancement public large.**

Il ne faut pas attendre d'avoir des utilisateurs pour clarifier la vision, le modèle métier et le MVP.  
En revanche, il faudra **tester tôt** avec une **alpha fermée** plutôt que lancer publiquement trop tôt.

### Recommandation produit
- **Maintenant** : cadrage solide + MVP exécutable
- **Ensuite** : implémentation d'un premier flux thématique réduit
- **Puis** : **alpha fermée** (5 à 20 testeurs)
- **Seulement après** : ouverture plus large si les signaux sont bons

### Pourquoi
Sans cadrage, tu risques de coder trop large.  
Sans test réel, tu risques de rester dans la théorie.  
Donc l'ordre sain est :

```text
Vision claire → MVP solide → alpha fermée → itérations → ouverture plus large
```

---

## 3. Public cible initial

Le produit cible en premier des utilisateurs francophones qui veulent enrichir leur vocabulaire dans une logique sérieuse et progressive.

### Segments initiaux probables
- Étudiants / lecteurs exigeants
- Personnes attirées par la littérature, la philosophie ou les domaines spécialisés
- Utilisateurs qui aiment apprendre du vocabulaire de manière structurée

### Non-prioritaire au départ
- Multilingue
- Marché grand public très large
- Usage totalement passif sans effort de validation ou de production

---

## 4. Thèmes de mots — Architecture validée

### 4.1 Définition métier d'un thème

Un **thème** est un **cadre d'apprentissage centré sur l'usage que l'utilisateur veut faire du vocabulaire**.

Le thème ne se définit donc plus seulement par un domaine “objectif”, mais par le **type d'utilisation visé** :
- mieux discuter,
- mieux écrire,
- mieux verbaliser ses ressentis,
- mieux maîtriser un domaine particulier.

Le thème se différencie des autres par le **contexte d'usage principal** du vocabulaire qu'il contient.

Exemples :
- **Discussion générale / intellectuelle** → vocabulaire utile pour mieux argumenter, nuancer, expliquer, discuter
- **Littérature / écriture** → vocabulaire utile pour écrire, analyser, lire ou employer un style plus riche
- **Verbaliser les ressentis** → vocabulaire utile pour nommer, nuancer et exprimer plus précisément ses émotions, sensations et états intérieurs
- **Domaine particulier** → vocabulaire utile pour fonctionner dans un champ précis (automobile, cuisine, droit, médecine...)

➡️ Un thème n'est donc **pas un simple tag**.  
Il porte aussi une logique de :
- sélection des mots,
- niveau de difficulté,
- ordre de présentation,
- exemples de contexte,
- qualité éditoriale.

### 4.2 Grandes familles de thèmes

| Famille | Intention utilisateur | Exemples |
|---------|-----------------------|----------|
| Discussion générale / intellectuelle | mieux discuter, nuancer, argumenter, expliquer | vocabulaire abstrait, nuances, concepts fréquents en discussion soutenue |
| Littérature / écriture | mieux lire, écrire, analyser, enrichir son style | figures de style, vocabulaire d'auteur, analyse littéraire |
| Verbaliser les ressentis | mieux exprimer ce que l'on ressent avec précision | émotions, sensations, nuances affectives, états intérieurs |
| Domaine particulier | mieux comprendre et utiliser le vocabulaire d'un champ précis | automobile, cuisine, médecine, droit |

### 4.3 Deux catégories de distribution

**Thèmes généraux (fixes, curés par l'équipe)**  
Mêmes thèmes pour tous les utilisateurs, avec gestion de niveaux de difficulté.

| Famille | Description | Source de données prioritaire |
|---------|-------------|-------------------------------|
| Discussion générale / intellectuelle | vocabulaire utile à la discussion, à l'argumentation et à la nuance | CNRTL / corpus d'essais / textes de réflexion |
| Littérature / écriture | vocabulaire utile à l'écriture, à la lecture et à l'analyse littéraire | Gutenberg / Wikisource / Gallica |
| Verbaliser les ressentis | vocabulaire utile pour nommer finement émotions, sensations et états psychiques | CNRTL / corpus littéraires / essais introspectifs |
| Domaine particulier | vocabulaire utile à un champ précis | FranceTerme / sources spécialisées |

**Thèmes à la demande (extensibles, supervisés)**  
- Si l'utilisateur demande un thème absent → processus supervisé
- L'utilisateur soumet une demande in-app
- L'équipe supervise la création
- Déploiement via Firebase / mise à jour
- **QC1 reste en attente** : mécanique exacte à définir plus tard

### 4.4 Périmètre linguistique
**Français uniquement** pour le produit cible actuel.

### 4.5 Domaines particuliers
Les domaines particuliers restent une liste **fermée mais extensible**.  
Pas de création libre côté utilisateur dans le MVP.

---

## 5. Processus pédagogique

Chaque mot suit un **pipeline d'acquisition** en 3 phases :

```text
Phase 1 — Découverte en situation
    → Présentation du mot dans son contexte réel (extrait, phrase authentique)
    → Première intuition du sens avant la définition formelle

Phase 2 — Ancrage multi-associatif  [plus tard]
    → Étymologie, image mentale, synonymes, champ sémantique, mémoire épisodique

Phase 3 — Consolidation à long terme
    → Intégration dans l'algo POLO-1
    → Questions variées : QCM, correspondance, orthographe, défi sémantique, phrase inventée
```

---

## 6. Niveaux de difficulté

Pour chaque thème, **3 niveaux** influencent la sélection des mots, pas la difficulté mécanique des exercices.

| Niveau | Critères |
|--------|----------|
| 🟢 Débutant | Fréquence élevée, définition courte, mot plus accessible |
| 🟡 Intermédiaire | Fréquence moyenne, polysémie possible |
| 🔴 Avancé | Faible fréquence, richesse sémantique, registre soutenu ou technique |

---

## 7. Ordre de présentation des mots — ✅ Validé

**Auto par défaut** : tri par fréquence décroissante (du plus courant au plus rare).  
**Override manuel (curator)** : l'équipe peut redéfinir l'ordre via l'admin pour structurer conceptuellement un thème.  
Les deux coexistent.

### 7.1 Ordre de sélection du QCM intégré

Quand un `QCM` intégré doit être programmé, les distracteurs sont choisis dans cet ordre :

1. **Cartes déjà présentes dans la session** (priorité aux autres cartes actuellement travaillées)
2. **Cartes du cache global** si la session ne suffit pas à produire 3 distracteurs distincts

Le `QCM` ne part que si l'on obtient **4 options distinctes** au total (1 bonne réponse + 3 distracteurs).

### 7.2 Ordre de sélection du Matching intégré

Quand un `Matching` intégré doit être programmé, les cartes sont choisies dans cet ordre :

1. **Cartes à travailler de la session en cours**
2. **Carte en cours** si elle n'est pas déjà incluse
3. **Carte distractrice issue du cache global** si la session ne fournit pas assez de cartes distinctes

Le `Matching` ne part que si l'on obtient **au moins 2 cartes distinctes**.

---

## 8. Validation avant intégration au deck

Les mots suggérés via source/API ne sont **jamais ajoutés automatiquement**.

Flux retenu :

```text
Source → normalisation → aperçu utilisateur → validation/rejet → ajout au deck
```

Contenu minimal de l'aperçu :
- mot,
- définition,
- exemple en situation,
- source,
- niveau,
- thème d'origine.

---

## 9. Stratégie pré-utilisateurs / pré-lancement

### Décision
Comme il n'y a **pas encore d'utilisateurs**, l'objectif n'est pas un lancement public immédiat.  
L'objectif est de construire une **alpha fermée testable**.

### Ce qu'il faut prouver avant de parler de lancement large
1. qu'un thème plaît réellement,
2. que les mots proposés sont jugés pertinents,
3. que l'aperçu + validation est compris,
4. que le deck thématique mélangé avec POLO-1 reste cohérent,
5. que la source choisie tient techniquement.

### Stratégie recommandée
- pas de lancement grand public maintenant,
- d'abord un MVP thématique réduit,
- ensuite une **alpha fermée** avec peu de testeurs,
- puis seulement élargissement si les signaux sont bons.

---

## 10. MVP — Définition claire

### Définition
Le **MVP** est la plus petite version utile du produit qui permet de valider la promesse principale sans construire tout le système final.

### MVP recommandé pour Lexica

#### Scope MVP
- 1 à 2 **thèmes fixes** maximum
- 3 niveaux de difficulté
- 1 source prioritaire par famille de contenu
- écran de sélection thème + niveau
- pipeline **aperçu → validation utilisateur → ajout au deck**
- liaison carte ↔ thème
- filtre de session par thème
- intégration normale dans **POLO-1**

#### Hors MVP
- création de thèmes à la demande
- validation avancée offline de "Invente une phrase"
- fusion complexe de plusieurs sources
- ancrage multi-associatif riche
- workflow admin complet de curation avancée

### Première recommandation de focus
Commencer par **Vocabulaire général** ou **Littérature technique**, car ce sont des terrains plus simples à curer proprement que Philosophie complète.

---

## 11. Modèle métier minimal recommandé

### Pourquoi
Il ne faut **pas enrichir `Flashcard` trop tôt avec trop de champs instables**.  
Sinon on mélange : apprentissage, sourcing, éditorial, thématisation et ingestion dans une seule entité.

### Entités minimales

#### `Theme`
Décrit un thème comme objet métier.

Champs minimaux :
- `id`
- `slug`
- `title`
- `description`
- `category`
- `isFixed`
- `supportsOfflineCache`
- `defaultOrderingMode`
- `status`

#### `ThemeLevel`
Enum simple :
- `BEGINNER`
- `INTERMEDIATE`
- `ADVANCED`

#### `ThemeWordCandidate`
Mot issu d'une source thématique avant/au moment de l'ajout au deck.

Champs minimaux :
- `id`
- `themeId`
- `lemma`
- `definition`
- `usageExample`
- `sourceName`
- `sourceRef`
- `level`
- `rank`
- `isApproved`

#### `FlashcardThemeLink`
Lien entre une carte réellement apprise et son origine thématique.

Champs minimaux :
- `flashcardId`
- `themeId`
- `level`
- `originType`
- `rankAtImport`

### Règle de conception
- `Flashcard` = objet d'apprentissage
- `Theme` = objet catalogue
- `ThemeWordCandidate` = objet d'ingestion / proposition

---

## 12. Qualité des données — Règles minimales

Un mot **n'entre pas** dans le système thématique si :
- la définition est trop floue,
- l'exemple n'est pas contextualisé,
- la source n'est pas traçable,
- le niveau ne peut pas être attribué,
- le mot est probablement hors thème,
- le doublon est évident,
- l'exemple n'aide pas vraiment à comprendre l'usage.

### Une entrée thématique minimale de qualité contient
- le mot,
- une définition exploitable,
- au moins un exemple de contexte,
- une source,
- un niveau,
- un thème,
- un rang de présentation.

---

## 13. Présentation en situation — Offline/Online

| Type de thème | Stratégie cache |
|---------------|-----------------|
| Thèmes généraux fixes | Cache local possible si données stabilisées |
| Thèmes à la demande | Online uniquement |

---

## 14. Relation avec le deck existant

- Le deck utilisateur reste **unique**
- Les mots manuels et thématiques sont **mélangés par défaut**
- Un **filtre optionnel** permet de cibler un ou plusieurs thèmes pendant une session

---

## 15. Nouveau type de question : "Invente une phrase"

### Principe
L'utilisateur rédige une phrase originale utilisant correctement le mot cible.  
Objectif : prouver une **compréhension active et productive**.

### Exemple
> Mot : **"aporie"**  
> ✅ *"Le philosophe se heurtait à une aporie : défendre la liberté semblait nécessiter de contraindre ceux qui en abusaient."*  
> ❌ *"L'aporie est un mot difficile."* (trop vague)

### Validation retenue : **Option C — Hybride** ✅

**Étape 1 — Local**
- mot présent dans la phrase,
- longueur ≥ 8 mots,
- score MiniLM embarqué ≥ 0.55 vs exemples de bon usage.

**Étape 2 — Fine (si connecté)**
- HuggingFace Inference API gratuite (`cross-encoder/nli-MiniLM2-L6-H768`),
- vérification plus fine de la cohérence sémantique.

**Feedback gradué**
- ✅ Très bien,
- ⚠️ À améliorer,
- ❌ Hors contexte.

**Règle offline** : hors ligne, cette question n'est **pas proposée**.  
À terme : fonctionnalité **premium** possible si un modèle offline plus fort est intégré.

### Important pour le MVP
Cette question peut être **intégrée tôt dans l'entraînement** sous forme de **défi utilisation**.  
En revanche, elle ne doit **pas** influencer le cœur de `POLO-1` tant que sa validation n'est pas jugée suffisamment fiable.

---

## 16. Sources de données — Gravité et sérieux

| Source | Usage | Accès | Niveau de risque |
|--------|-------|-------|------------------|
| CNRTL | Définitions + exemples vocabulaire général | Gratuit | Moyen |
| Project Gutenberg | Textes classiques libres | Gratuit | Moyen |
| BNF Gallica | Corpus littéraire français | Gratuit | Moyen à élevé |
| Wikisource FR | Œuvres libres en français | Gratuit | Moyen |
| FranceTerme | Domaines spécifiques | Gratuit | Faible à moyen |

### Risques à prendre au sérieux
- instabilité ou hétérogénéité des sources,
- qualité irrégulière des exemples,
- normalisation difficile,
- droits ou limites d'usage à vérifier,
- dépendance excessive à une seule source.

### Règle de prudence
On ne fonde pas tout le produit sur une source non fiabilisée.  
On commence par **une source prioritaire par besoin**.

---

## 17. Hiérarchie des fonctionnalités

### Must-have / MVP
- 1 à 2 thèmes fixes
- 3 niveaux
- 1 source principale par famille de contenu
- aperçu + validation utilisateur
- ajout au deck
- lien carte ↔ thème
- filtre de session par thème

### V2
- plusieurs thèmes fixes bien curés
- découverte en situation plus riche
- cache local amélioré
- override éditorial plus poussé

### Plus tard
- thèmes à la demande
- workflow admin complet
- fusion multi-sources avancée
- ancrage multi-associatif riche

### Premium / avancé
- validation offline avancée de "Invente une phrase"
- modèles plus lourds
- enrichissement sémantique plus profond

---

## 18. Stratégie de mesure

Même sans base d'utilisateurs large, il faut mesurer dès l'alpha.

### Mesures minimales
- taux d'ouverture d'un thème,
- taux d'ajout au deck,
- taux de rejet,
- taux de révision par thème,
- usage du filtre thème,
- taux de complétion du flux découverte,
- rétention testeurs J7 / J30,
- usage et qualité perçue des exemples,
- essais de "Invente une phrase" une fois prototypé.

### But
Savoir :
- quels thèmes plaisent,
- quelles sources produisent les meilleurs candidats,
- si les exemples en situation augmentent vraiment l'adoption,
- si le filtre thème est utile,
- si le flux thématique mérite d'être étendu.

### Calibration technique du défi utilisation
Le défi utilisation doit être calibré à partir d'un **dataset versionné** de phrases candidates avec verdict attendu humain.

Format recommandé :
- fichier CSV UTF-8,
- un mot cible,
- une définition attendue,
- une phrase candidate,
- un verdict attendu (`ACCEPT`, `BORDERLINE`, `REJECT`),
- des notes de contexte.

Objectif : faire converger progressivement le comportement de l'app vers les jugements humains supervisés.

---

## 19. Questions complémentaires

### QC1 — Processus de création de thème supervisé ⏳ en attente
À traiter dans une itération dédiée, après validation du cœur thématique fixe.

### QC2 — Validation "Invente une phrase" ✅ Validée → Option C hybride
Voir section 15.

### QC3 — Ordre pédagogique des mots ✅ Validé → auto + override manuel
Voir section 7.

---

## 20. Ordre recommandé des travaux

```text
1. Figer le modèle métier minimal
2. Choisir le thème MVP + la source MVP
3. Construire le flux sélection thème → aperçu → validation → ajout deck
4. Ajouter le filtre thème dans les sessions POLO-1
5. Tester en alpha fermée
6. Étendre les contenus / améliorer la découverte en situation
7. Prototyper sérieusement "Invente une phrase"
8. Traiter les thèmes à la demande
```

---

## 21. Tâches numérotées d'exécution

> Format inspiré de `docs/guides/CONSIGNES_TACHES.md` : chaque tâche décrit un chantier exécutable, ses livrables et ses contraintes.

---

## TACHE_VF_01 - Cadrage du modèle métier thématique
- **Scope estimé :** élevé
- **Zone produit / code :** domain model, couche data, documentation d'architecture
- **Objectif :** figer le modèle minimal pour éviter de surcharger `Flashcard` trop tôt.

### Livrables
- Définition stable des entités : `Theme`, `ThemeLevel`, `ThemeWordCandidate`, `FlashcardThemeLink`
- Règles de responsabilité :
  - `Flashcard` = objet d'apprentissage
  - `Theme` = objet catalogue
  - `ThemeWordCandidate` = objet d'ingestion/proposition
- Mapping clair avec l'existant (`Flashcard`, réserve, session POLO-1)

### Contraintes
- Ne pas mélanger sourcing, éditorial et apprentissage dans une seule entité
- Ne pas ajouter de champs instables à `Flashcard` sans justification forte

### Critère de sortie
Le modèle métier est assez stable pour ouvrir les tâches data/UI sans refonte immédiate.

---

## TACHE_VF_02 - Choisir le MVP contenu
- **Scope estimé :** moyen
- **Zone produit :** cadrage MVP, stratégie contenu, dépendances externes
- **Objectif :** réduire la vision à un premier incrément testable.

### Livrables
- Choix de **1 à 2 thèmes MVP maximum**
- Choix de **1 source prioritaire par besoin**
- Définition du flux MVP : `sélection thème → aperçu → validation → ajout deck → révision`
- Liste explicite de ce qui reste hors MVP

### Recommandation actuelle
- Démarrer par `Vocabulaire général` ou `Littérature technique`
- Reporter `Philosophie complète`, `thèmes à la demande` et `Invente une phrase` structurante

### Critère de sortie
Le MVP tient en un périmètre simple, compréhensible et implémentable sans dette excessive.

---

## TACHE_VF_03 - Qualité des données et règles éditoriales minimales
- **Scope estimé :** moyen
- **Zone produit / contenu :** ingestion, curation, validation humaine/assistée
- **Objectif :** empêcher l'entrée de contenu faible dans le système.

### Livrables
- Critères minimaux d'acceptation d'une entrée thématique
- Check-list qualité d'un mot candidat
- Règles de rejet (hors thème, flou, doublon, source non traçable, exemple inutile)

### Une entrée acceptable doit contenir
- le mot
- une définition exploitable
- au moins un exemple contextualisé
- une source traçable
- un niveau
- un thème
- un rang de présentation

### Critère de sortie
Un humain ou une IA supervisée peut décider rapidement si un mot candidat entre ou non dans le système.

---

## TACHE_VF_04 - Intégrer la couche data thématique minimale
- **Scope estimé :** élevé
- **Zone code :** Room, repositories, migrations, domain/data mapping
- **Objectif :** rendre le modèle thématique stockable sans casser l'existant.

### Livrables
- Tables / entities minimales pour les thèmes et candidats
- Migration Room compatible avec les cartes existantes
- Repositories d'accès aux thèmes et candidats
- Liaison persistée entre une carte et son origine thématique

### Contraintes
- `POLO-1` ne doit pas être cassé
- Le deck existant reste valide
- Les cartes manuelles doivent continuer à fonctionner sans thème

### Critère de sortie
Le stockage thématique existe en production locale sans régression sur la révision actuelle.

---

## TACHE_VF_05 - Écran de sélection thème + niveau
- **Scope estimé :** moyen
- **Zone code :** UI/UX, navigation, ViewModel
- **Objectif :** permettre à l'utilisateur de choisir son angle d'apprentissage avant ajout au deck.

### Livrables
- Écran listant les thèmes MVP
- Sélecteur de niveau (`Débutant`, `Intermédiaire`, `Avancé`)
- État vide / chargement / erreur
- Navigation vers la liste de candidats ou l'aperçu

### Contraintes UX
- Vue compacte et lisible sur mobile
- Pas de surcharge visuelle
- Possibilité d'évolution future vers plus de thèmes

### Critère de sortie
L'utilisateur peut sélectionner un thème et un niveau sans ambiguïté.

---

## TACHE_VF_06 - Pipeline aperçu → validation → ajout au deck
- **Scope estimé :** élevé
- **Zone code :** repositories, use cases, UI de validation, logique d'ajout
- **Objectif :** créer le cœur du flux thématique.

### Livrables
- Écran / composant d'aperçu d'un mot candidat
- Affichage de : mot, définition, exemple, niveau, source, thème
- Action `Valider` / `Rejeter`
- Ajout au deck existant si validé
- Historique minimal ou statut du candidat si nécessaire

### Contraintes
- Aucun ajout automatique
- Le flux doit être rapide et compréhensible
- Le contenu affiché doit être strictement traçable à sa source

### Critère de sortie
Un candidat validé devient une carte apprenable, un candidat rejeté n'entre pas dans le deck.

---

## TACHE_VF_07 - Lien thème ↔ carte et filtrage dans POLO-1
- **Scope estimé :** élevé
- **Zone code :** moteur review, filtres, session planner, UI de session
- **Objectif :** faire exister le thème dans l'entraînement sans casser la logique globale.

### Livrables
- Lien persistant entre carte et thème
- Filtre optionnel par thème dans les sessions
- Comportement par défaut : deck mélangé
- Vérification que `POLO-1` continue à fonctionner normalement

### Contraintes
- Le filtre est optionnel, jamais obligatoire
- Les cartes sans thème doivent rester compatibles

### Critère de sortie
L'utilisateur peut s'entraîner soit sur tout son deck, soit sur un ou plusieurs thèmes ciblés.

---

## TACHE_VF_08 - Instrumentation et stratégie de mesure
- **Scope estimé :** moyen
- **Zone code / produit :** analytics locale ou distante, logs métier, tableau de suivi
- **Objectif :** apprendre même sans base d'utilisateurs large.

### Livrables
- Mesures minimales :
  - ouverture d'un thème
  - ajout au deck
  - rejet
  - révision par thème
  - usage du filtre
  - rétention alpha J7 / J30
- Définition des signaux de succès / échec

### Contraintes
- Ne pas attendre un lancement public pour mesurer
- Les métriques doivent répondre à des questions produit concrètes

### Critère de sortie
On sait dire si le flux thématique intéresse réellement les premiers testeurs.

---

## TACHE_VF_09 - Alpha fermée du MVP thématique
- **Scope estimé :** moyen
- **Zone produit :** distribution, feedback, itérations UX et contenu
- **Objectif :** tester le cœur du produit avant ouverture plus large.

### Livrables
- Version testable du MVP
- Petit panel de testeurs (alpha fermée)
- Recueil structuré des retours
- Corrections prioritaires contenu/UX

### Critères observés
- Les utilisateurs comprennent-ils la promesse ?
- Ajoutent-ils réellement des mots au deck ?
- Le contexte améliore-t-il la perception de valeur ?
- Le filtre thème est-il utilisé ?

### Critère de sortie
Décision éclairée : poursuivre, corriger ou resserrer le scope avant tout lancement plus large.

---

## TACHE_VF_10 - Découverte en situation enrichie
- **Scope estimé :** moyen à élevé
- **Zone code :** UI dédiée, contenu, cache local
- **Objectif :** renforcer la promesse différenciante de Lexica.

### Livrables
- UI dédiée à la phase de découverte
- Extrait ou phrase de contexte mis en avant avant la définition
- Stratégie de cache pour thèmes fixes

### Contraintes
- À lancer après validation du MVP de base
- Ne pas complexifier le flux avant preuve d'intérêt

### Critère de sortie
La phase de découverte devient une vraie étape produit et non un simple détail d'affichage.

---

## TACHE_VF_11 - Prototype isolé de la question "Invente une phrase"
- **Scope estimé :** élevé
- **Zone code :** IA locale, appels distants éventuels, UX de feedback
- **Objectif :** tester sérieusement la faisabilité avant intégration profonde.

### Livrables
- Prototype isolé hors cœur critique de la révision
- Vérification locale : mot présent, longueur minimale, score MiniLM
- Validation fine si connecté via HuggingFace
- Feedback gradué : ✅ / ⚠️ / ❌
- Dataset CSV d'évaluation + test automatisé de concordance attendu humain / verdict app

### Contraintes
- Hors ligne : question non proposée
- Ne pas faire de cette question un signal fort de maîtrise tant que la qualité n'est pas prouvée

### Critère de sortie
On sait si cette feature est crédible, frustrante ou à repositionner.

---

## TACHE_VF_12 - Thèmes à la demande / workflow admin
- **Scope estimé :** élevé
- **Zone code / produit :** demande utilisateur, admin, synchronisation Firebase
- **Objectif :** ouvrir le catalogue sans dégrader la qualité.

### Livrables
- Formulaire de demande de thème
- Interface admin de supervision
- Workflow de validation et publication

### Contraintes
- À traiter après stabilisation des thèmes fixes
- La qualité éditoriale reste prioritaire sur la quantité

### Critère de sortie
Le système peut s'étendre sans casser la cohérence globale du catalogue.

---

## 22. Vue synthétique des priorités

### Priorité immédiate
- `TACHE_VF_01`
- `TACHE_VF_02`
- `TACHE_VF_03`

### Bloc MVP exécutable
- `TACHE_VF_04`
- `TACHE_VF_05`
- `TACHE_VF_06`
- `TACHE_VF_07`
- `TACHE_VF_08`
- `TACHE_VF_09`

### Extensions après validation
- `TACHE_VF_10`
- `TACHE_VF_11`
- `TACHE_VF_12`

---

*Fin de la section active. Les éléments ci-dessous sont conservés comme annales de travail et historique de discussion.*


la discu :

toi :

Intéressant comme angle d'attaque ! Avant de planifier, j'ai besoin de comprendre la direction. Quelques questions rapides :
Saisie/édition — tu veux revoir comment l'utilisateur crée et édite ses mots/définitions (formulaire, validation, richesse des champs) ?
Structure des données — tu veux enrichir ce qu'une carte peut contenir (exemples d'usage, étymologie, synonymes, tags, niveau de difficulté...) ?
Import/export — tu veux améliorer comment les mots entrent dans l'appli (import CSV/Anki, scraping de dictionnaire, génération via IA...) ?
Qualité des définitions — tu veux un mécanisme pour s'assurer que les définitions sont claires, complètes, utiles pour l'algo de révision ?
Plusieurs des axes ci-dessus ?



moi :

pour l'édition utilisateur rien à faire de ce coté full control utilisateur.
Non pas forcément. Ou pas pour l'instant en tout cas.
oui exact.
pas particulièrement, j'espère qu'elles sont correcte par défaut.
Comment je vois le future de l'app :
L'utilisateur pour choisir des "thèmes de mots" :
plutot type Philosophie
plutot type littérature (technique genre vocabulaire particulier au théatre à l'analyse de la littérature comme les métaphore, et vocabulaire avancé d'utilisation littéraire : vocabulaire utilisé par les auteurs dans leurs oeuvres.
Pour ces deux là j'aimerai avoir accès à une grosse base de données de texte classic (accès API) de la même manière que fonctionne reverso pour la traduction, ou les mots interressant pour notre appli sont utilisé et que je pourrai présenté à l'utilisateur pour l'apprentissage.
vocabulaire générale
Là l'idéal se serait d'avoir une api genre dico larrousse ou je peux puiser à l'infini et si possible des exemples en situation comme au dessus.
domaine spécifique : automobile cuisine
(l'apprentissage d'un domaine passe par l'apprentissage du vocabulaire spécifique à ce domaine !)
En gros l'ajout des mots passera par ces phases, qui correspondent de manière pertinente aux études de psycho cognitive et aux méthodes des champions de mémoire) :
d'après le domaine que l'utilisateur aime, des présentation en situation puis des activité pour faire associé le plus de choses possibles à l'utilisa (ça on vera après). puis présentation avec notre supper alg opour garder en mémoire à long terme sur la durée.
L'utilisateur devra pouvoir après la selection du thème choisir la dificulté parmis 3 proposés et les mots doivent correspondre.
Il faut qu'on mette un nouveau type de question, basé sur notre modèle d'embendigs ou un autre si celui là peut pas le faire : L'utilisateur doit inventer une phrase ou il utilise correctement le mot (et prouve qu'il sait l'utilisé, ça doit être suffisament détaillé.)
Avant de commencer plusieurs choses, on va intégrer tout ça de manière détaillé au fichier futures , dans une nouvelle section daté. Puis tu me posera des questions sur les points les moins clairs pour qu'on affine, après on fera un plan globale pour l'execution.

toi :
Le fichier docs/planning/VISION_FUTURE.md est créé. Voici les 8 questions d'affinage pour qu'on cible juste avant de planifier l'exécution :

Q1 — Thèmes : fixes ou personnalisables ? La liste Philosophie / Littérature / Vocabulaire général / Domaines est-elle fermée dans l'app, ou l'utilisateur peut-il créer ses propres thèmes ?
Q2 — Périmètre linguistique Français uniquement pour l'instant, ou tu envisages le multilangue à terme ?
Q3 — Domaines spécifiques : liste fermée ou libre ? Tu proposes automobile/cuisine en exemple — est-ce une liste fixe dans l'app ou l'utilisateur peut saisir n'importe quel domaine ?
Q4 — Validation "Invente une phrase" : jusqu'où ? Le MiniLM embarqué peut vérifier la cohérence sémantique mais pas vraiment la qualité d'usage. Tu préfères : validation partielle locale (suffit pour MVP) ou validation fine via API LLM (GPT ou autre) dès le départ ?
Q5 — Curriculum progressif ou libre ? Quand l'utilisateur choisit un thème + niveau, les mots arrivent dans un ordre pédagogique défini ou il pioche librement dans le catalogue ?
Q6 — Validation avant intégration au deck Les mots suggérés via API s'ajoutent automatiquement ou l'utilisateur valide/rejette chaque mot avant qu'il entre dans sa révision ?
Q7 — Online ou offline pour les exemples en situation ? Les extraits de textes classiques nécessitent-ils une connexion à chaque fois, ou tu veux un cache/mode offline ?
Q8 — Relation avec le deck existant Les mots des thèmes viennent s'ajouter au deck personnel existant, ou c'est un espace de révision séparé ?





moi :

Q1 très pertinente : en vrai j'en sais rien. Le mécanisme auxquel j'avais pensé concerne les thèmes annexe, je voudrais que lorsque l'utilisateur formule un nouveau souhait de thème qui n'existe pas, ça lance un processu de création qui permette de le formé et que je puisse le supervisé. Pour les thèmes généraux il faut gérer les difficultés mais oui ça doit être plus pratique si c'est à peu près le même pour tout le monde...
Q2 que français.
Q3 limité à toutes les listes existantes, et qui devrait s'étandre avec le temps et les demandes !
Q4 il faut qu'on trouve un système qui nous peermettre d'intégrer ça, que ça fonctionne, sans clée API payante... Il faut quelque chose d'assez fin, le but c'est de vérifier que l'utilisateur sait utiliser le mot !
Q5 ordre pédagogique se serait le top mais je sais pas trop comment on va faire.
Q6 système de validation
Q7 Si y'a des thèmes fixes et pour "tout le monde" on peut en mettre un peu en cache. Sinon online.
Q8 dans l'algo de entrainement c'est tout mélanger par défaut mais on ajoutera un filtre pour que l'uti puisse selectionner des thèmes s'il veut.



toi :
Le doc est à jour. Il reste 3 points à trancher avant de lancer l'exécution :

QC1 — Comment tu supervises la création d'un thème à la demande ?
Le mode admin est déjà dans l'app. La voie la plus élégante serait :
L'utilisateur soumet sa demande in-app → elle remonte dans Firebase Firestore
Toi (en mode admin) tu la vois directement dans l'app, tu crées le thème mot par mot, et il est pushé à tous les utilisateurs via Firestore
C'est autosuffisant, pas de console Firebase à ouvrir. Ça te parle ?

QC2 — Validation "Invente une phrase" : Option A, B ou C ?
Ma recommandation : Option C (hybride)
Offline/immédiat : mot présent dans la phrase + longueur > 8 mots + score MiniLM ≥ 0.55 vs les exemples de bon usage stockés dans la carte → validation locale
Si connecté : confirmation via HuggingFace Inference API gratuite (modèle NLI cross-encoder/nli-MiniLM2-L6-H768) pour un verdict plus fin
Résultat : 3 niveaux de feedback — ✅ Très bien / ⚠️ À améliorer / ❌ Hors contexte
C'est faisable sans aucun coût. Est-ce que ce niveau de précision te semble suffisant ?

QC3 — Ordre pédagogique dans un thème : qui décide ?
Deux approches :
Auto : trier par fréquence décroissante (du plus courant au plus rare) → simple, données disponibles
Manuel (curator) : toi tu définis l'ordre dans l'outil admin → plus de contrôle, plus de travail
Les deux peuvent coexister : ordre auto par défaut, override manuel si tu veux structurer conceptuellement (ex : en Philosophie, grouper les termes épistémologiques ensemble avant les termes ontologiques).


moi:

QC1 on laisse de cote pour l'instant
QC2 euh c'est quoi les autres choix ? ça me paraît bien, mais si mode offline on présente pas ce genre de question (plus tard ce sera une "fonctionnalité premium"
QC3 auto et oui pour
