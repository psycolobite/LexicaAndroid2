# Spécification Logique Métier : Système de Recommandation et Présentation d'Extraits

Ce document décrit en détail le fonctionnement logique et sémantique proposé pour le moteur de recherche et de découverte d'extraits dans Lexica.

---

## 1. Flux Ingestion & Synchronisation (Hybride Local-Distant)

Pour concilier un corpus massif (dizaines de milliers de livres) et le support hors-connexion, l'architecture est divisée en deux couches :

```
[Serveur / Traitement en amont]
      │ (Analyse FastText, Lexique.org, extraction de paragraphes)
      ▼
[Base de Données Distante]
      │
      │ Requête API filtrée (envoie la version condensée du profil utilisateur)
      ▼
[Pool SQLite Local (100 Extraits)] ◄─── Représente l'état Hors-connexion
      │ (50 Pipeline A / 50 Pipeline B)
      ▼
[Scoring Engine Local] ◄────────────── Réévaluation réactive en temps réel (swipes/notes)
      │
      ▼
[Écran Explore (Présentation)]
```

### Règles de ravitaillement du cache local :
1.  **Taille du Pool local** : Fixée à **100 extraits candidats**.
    *   **50 extraits Pipeline A** : Liés à la recherche directe de la Word Reserve de l'utilisateur.
    *   **50 extraits Pipeline B** : Liés à la découverte spontanée sémantique.
2.  **Seuil de déclenchement (Ravitaillement partiel)** : Lorsque le pool local descend sous **70 extraits** (c'est-à-dire que 30 extraits ont été consommés/swipés), l'application lance une requête d'arrière-plan asynchrone pour télécharger 30 nouveaux extraits.
3.  **Réinitialisation complète (Reset du Pool)** : Tous les **70 mots ajoutés** à sa collection personnelle, le système invalide la totalité du pool. Lors de la prochaine connexion internet, le pool local de 100 extraits est entièrement régénéré de zéro pour s'aligner parfaitement sur l'évolution du niveau et des intérêts de l'utilisateur.
4.  **Durée de vie maximale (Expiration/TTL)** : Pour garantir la fraîcheur du contenu, tout extrait resté inutilisé dans le pool local pendant plus de **7 jours** est automatiquement expiré et supprimé.
5.  **Nettoyage** : Les extraits consommés (swipés à gauche ou notés) sont purgés de la table locale après chaque session pour économiser l'espace disque.

---

## 2. Définition des Objectifs Utilisateur (Le "Pourquoi")

L'onboarding de l'utilisateur ne lui demande pas ses thèmes préférés (pas de silos d'apprentissage), mais son **but d'apprentissage** (ses objectifs). Nous définissons 5 objectifs majeurs :

1.  **Vocabulaire Littéraire (Lecture & Écriture de Littérature, Poésie, Théâtre)** :
    *   *Mots cibles* : Mots rares, termes descriptifs riches, adjectifs littéraires, archaïsmes élégants, figures de style...
    *   *Priorité* : Mots à forte dimension poétique et littéraire.
2.  **Avoir des discussions soutenues & Rhétorique** :
    *   *Mots cibles* : Connecteurs logiques soutenus, vocabulaire de débat intellectuel, verbes d'opinion précis, adjectifs qualificatifs sociopolitiques et idéologiques de positionnement ou de réaction envers le système politique et les changements sociétaux (ex: *réactionnaire*, *progressiste*, *conservateur*, *gaulliste*, *monarchiste*, *souverainiste*, *anarchiste*), termes du sophisme (ex: *obsolescent*, *dilatoire*).
    *   *Priorité* : Mots facilitant la construction logique, l'expression structurée et le positionnement dans une discussion oratoire ou politique.
3.  **Comprendre les discussions informelles contemporaines — « Le Dernier Cri » en expressions populaires** :
    *   *Philosophie* : Cette catégorie traque les mots et expressions qui se propagent de manière virale dans la société française — ceux qui ont ce côté « à la mode » irrésistible, où l'on se dit *« ça sonne trop bien, il faut que j'utilise ce mot »*. L'objectif est de capter **le dernier cri en matière d'expressions populaires**, au moment même où elles sont en phase d'adoption massive.
    *   *Cycle de vie typique* : Ces expressions émergent dans un milieu restreint (souvent la cité, parfois les réseaux sociaux ou un emprunt étranger), puis se propagent très vite — au travail, entre collègues, dans les médias — jusqu'à devenir mainstream. Dans la société française, les mots et expressions nés dans la cité (argot urbain, verlan, emprunts à l'arabe, au romani) constituent la source d'influence dominante, bien que d'autres vecteurs existent (anglicismes Internet, culture gaming, pop culture asiatique).
    *   *Mots cibles* : Néologismes viraux, expressions populaires en pleine adoption, argot urbain contemporain, mots issus d'emprunts étrangers ayant percé dans le mainstream (ex: *paf*, *miskine*, *seum*, *masterclass*).
    *   *Priorité* : Expressions **en vogue et en cours de propagation** — registre familier, argotique, vivant. L'intérêt pédagogique est de les connaître *pendant* leur phase de mode, pas après.
    *   *Vision cible (outil idéal)* : Un système de traitement massif des vidéos TikTok et YouTube récentes, capable de repérer les expressions émergentes, de mesurer leur vitesse de propagation, et de distinguer celles qui « prennent » de celles qui restent confidentielles. En attendant cette capacité, l'algorithme doit s'en approcher via des proxies accessibles (Wiktionnaire vivant, ratio d'oralité Lexique383, corpus de sous-titres).
4.  **Développer sa culture générale** :
    *   *Mots cibles* : Curiosités lexicales, mots insolites ou insolents qui provoquent l'étonnement (ex: *callipyge*, *lucifuge*), et vocabulaire contemporain lié à la diversité des orientations relationnelles, d'attirance et d'identités de genre (ex: *pansexuel*, *saphique*, *demisexuel*, *non-binaire*).
    *   *Priorité* : Éclectisme, variété sémantique et compréhension des évolutions sociétales contemporaines.
5.  **Domaine Spécifique (Optionnel)** :
    *   *Mots cibles* : Jargon technique. L'utilisateur peut saisir n'importe quel domaine textuellement (ex: *Droit*, *Automobile*, *Psychologie*).

---

## 3. Classification sémantique : Abstraction vs Spécificité Technique

Pour cibler au mieux les mots, nous séparons la complexité en deux natures :

### A. Mots Abstraits (Concepts, Nuances, Qualités)
*   **Définition** : Mots décrivant des idées, des états ou des concepts transversaux.
*   **Heuristique de détection** :
    *   *Ratio Littéraire Lexique.org* : Fréquence Livres élevée / Fréquence Films très basse (Ratio > 4.0).
    *   *Proximité vectorielle (Embeddings)* : Proximité sémantique étroite avec les vecteurs de la pensée, du raisonnement et de l'art (calculé offline côté serveur).
    *   *Indicateurs secondaires* : Suffixes conceptuels (`-isme`, `-ité`, `-ence`, etc.).
    *   *Détection des qualificatifs d'orientation idéologique (C2)* :
        *   Recherche par suffixes politiques/doctrinaux : `-iste` (quand associé à des courants d'idées, ex: *gaulliste*, *monarchiste*, *progressiste*), `-crate` (ex: *démocrate*, *technocrate*).
        *   Filtres de catégories sémantiques Wiktionnaire : Extraction à partir des catégories Wiktionnaire liées à la politique, aux idéologies, aux doctrines et aux partis politiques, croisées avec des fréquences d'usage adéquates pour exclure les termes trop historiques ou obscurs (ex: garder *gaulliste*, mais écarter *philippiste*).
        *   Modèle de phrase "Tu es..." / "Il est..." : Extraction prioritaire des adjectifs applicables à une personne morale ou physique pour catégoriser son orientation sociétale dans un débat.

### B. Mots Techniques / Jargon (Objets, Procédés)
*   **Définition** : Mots désignant des objets ou des concepts spécifiques à un domaine d'activité.
*   **Heuristique de détection** :
    *   *Focalisation Thématique* : Mots très présents dans des corpus spécialisés (Wiktionnaire thématique, Wikipedia technique) mais quasiment inexistants à l'oral populaire.
    *   *Structure des Embeddings* : Forment des grappes vectorielles isolées (clusters) dans des micro-domaines sémantiques.
    *   *Indicateurs secondaires* : Suffixes scientifiques (`-ose`, `-ite`, `-tomie`, etc.).

---

## 4. Algorithme de Scoring Local Réactif

Le score final d'un extrait dans le pool local est calculé par la formule dynamique suivante :

$$\text{ScoreFinal} = \left( W_{\text{pref}} \cdot \text{ScorePreferences} + W_{\text{qual}} \cdot \text{ScoreQualité} + W_{\text{div}} \cdot \text{ScoreDiversité} \right) \cdot \text{PénalitéNote} \cdot \text{PoidsPipeline}$$

### A. Ajustement dynamique du Poids de Pipeline (A vs B)
L'application maintient un équilibre `pipelineABalance` (de 0.0 à 1.0, initialisé à 0.5) :
*   Si l'utilisateur note mal (1-2 étoiles) ou ignore (swipe rapide à gauche) un extrait provenant de **Pipeline A** (Word Reserve) $\rightarrow$ `pipelineABalance` diminue, ce qui pénalise le Pipeline A et fait remonter le **Pipeline B** (Découverte Spontanée) dans la liste.
*   Si l'utilisateur note mal un extrait provenant de **Pipeline B** $\rightarrow$ `pipelineABalance` augmente, renforçant la priorité des mots issus de sa Word Reserve.

### B. Pénalité Note (Feedback sur l'extrait précis)
*   Si un extrait est noté négativement (1 ou 2 étoiles) par l'utilisateur ou la communauté (moyenne globale < 2.5), il reçoit un multiplicateur de pénalité de **0.0** (masquage immédiat pour cet utilisateur) ou **0.2** (relégation au fond de la pile).

### C. Limitation de la densité
Dans chaque extrait affiché, le moteur limite le surbrillance (fond jaune/vert) à **1 ou 2 mots complexes maximum**, afin de préserver le confort de lecture et d'éviter l'effet "un mot surligné toutes les trois lignes".
