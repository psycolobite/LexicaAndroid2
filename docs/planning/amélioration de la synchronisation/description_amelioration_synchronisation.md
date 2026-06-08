# Description — Amélioration de la synchronisation

## 1. Rôle de ce document

Ce document sert de brief fonctionnel pour le futur chantier d'amélioration de la synchronisation.

L'objectif est de faire évoluer la synchro actuelle vers un système plus robuste, plus silencieux, et plus fiable en multi-appareils.

---

## 2. Problème général

La logique actuelle de synchronisation repose encore largement sur des comparaisons d'état local contre état cloud, avec des cas de choix ou d'écrasement.

Cette approche fonctionne dans les cas simples, mais elle devient fragile quand :
- plusieurs appareils utilisent le même compte,
- un appareil progresse hors connexion pendant qu'un autre continue d'avancer,
- certaines données doivent être fusionnées et non remplacées,
- des suppressions, retraits de favoris ou modifications concurrentes apparaissent.

---

## 3. Direction produit et technique

La synchronisation future doit suivre une logique `local-first` :
- toute action utilisateur est d'abord enregistrée localement,
- la synchronisation cloud agit ensuite comme mécanisme de convergence entre appareils,
- les conflits doivent être résolus automatiquement dans la plupart des cas,
- l'utilisateur ne doit être sollicité que dans des cas réellement ambigus ou destructifs.

---

## 4. Principe recommandé

La stratégie recommandée n'est pas d'échanger uniquement de gros snapshots complets.

Il faut progressivement aller vers :
- un journal d'événements ou d'opérations,
- des identifiants stables par entité,
- des horodatages par action,
- des règles de fusion spécifiques par type de donnée.

---

## 5. Règles de fusion par type de donnée

La future synchronisation ne doit pas utiliser une seule règle globale.

Exemples :
- cartes ajoutées : union si les identifiants diffèrent,
- favoris : dernier changement réel gagne,
- suppressions : tombstones ou `deletedAt` pour éviter les réapparitions,
- contenu édité d'une carte : version la plus récente ou conflit conservé comme brouillon,
- progression de révision : idéalement reconstruite depuis des événements,
- XP : dérivée d'événements de session, pas addition brute d'un entier,
- streak : recalculée à partir de jours d'activité,
- profil d'intérêt futur : recalculé à partir d'événements d'usage.

---

## 6. Cas d'usage à couvrir

La future synchro doit couvrir au minimum :
- utilisateur sur un seul appareil,
- utilisateur hors connexion puis retour réseau,
- utilisateur qui change de téléphone,
- utilisateur connecté sur plusieurs appareils,
- ajout simultané de mots depuis plusieurs appareils,
- favoris modifiés sur plusieurs appareils,
- modifications concurrentes de cartes,
- suppressions d'un côté et modifications de l'autre,
- futur profil d'intérêt par domaine calculé à partir des interactions.

---

## 7. Résultat attendu

À terme, la synchronisation doit devenir :
- silencieuse dans l'immense majorité des cas,
- plus robuste en cas d'usage multi-appareils,
- moins sujette aux régressions de données,
- compatible avec les futures fonctionnalités de personnalisation et de recommandation.

---

## 8. SA1 — Audit du fonctionnement actuel

### 8.1 Données actuellement synchronisées

D'après le code actuel, la synchro cloud porte aujourd'hui sur un snapshot complet de `CloudProgress` contenant :
- `xp`
- `level`
- `streak`
- `lastLoginDate`
- `flashcards`
- `reviewQuestionProgress`
- `dailyReviewStats`

Le stockage cloud actuel est organisé sous `users/{uid}` avec 3 sous-collections :
- `flashcards`
- `reviewQuestionProgress`
- `dailyReviewStats`

### 8.2 Déclencheurs actuels de push et d'import

Les déclencheurs réellement en place aujourd'hui sont :
- vérification au lancement si une session est déjà authentifiée,
- vérification lors d'une nouvelle connexion,
- import silencieux si le local est vide et que le cloud contient déjà des données,
- upload silencieux si le cloud est vide,
- push périodique toutes les `5 minutes`,
- push à la fin d'une session de révision,
- push lors du passage de l'application en arrière-plan (`onStop`),
- push avant déconnexion,
- upload d'un état vide dans certains cas de reset de compte.

### 8.3 Cas de dialogue encore présents

Le dialogue n'est plus montré au lancement pour une session déjà authentifiée.

Il reste cependant dans les scénarios de nouvelle connexion où `checkAndSync()` détecte :
- un compte cloud vide mais un local significatif qui mérite encore un choix,
- ou un conflit `cloud vs local` sur une connexion fraîche.

### 8.4 Faiblesses de l'architecture actuelle

L'architecture actuelle reste fondée sur la comparaison de snapshots complets.

Ses limites principales sont :
- absence de journal d'opérations ou d'événements,
- absence de `deviceId` stable dans le modèle de synchro,
- absence de curseur de synchronisation incrémental,
- pas de fusion par type de donnée : on choisit ou on remplace,
- pas de tombstones pour les suppressions,
- remplacement complet de sous-collections cloud lors d'un upload,
- difficulté à converger proprement si deux appareils avancent hors ligne puis reviennent.

### 8.5 Conclusion SA1

Le système actuel est correct pour un usage mono-appareil ou très simple, mais pas encore pour un usage multi-appareils robuste.

Le besoin réel est donc confirmé : il faut aller vers un modèle `local-first` avec opérations synchronisables et règles de fusion spécifiques.

---

## 9. SA2 — Modèle cible de synchronisation

### 9.1 Principe général

Le futur système doit conserver le local comme source de vérité immédiate pour l'interface, puis utiliser le cloud comme point de convergence entre appareils.

L'unité de synchro recommandée n'est plus le snapshot global, mais l'opération métier ou l'événement métier.

### 9.2 Objets techniques à introduire

Le modèle cible doit introduire au minimum :
- un `deviceId` stable par installation,
- un `opId` unique par opération,
- un `entityId` stable pour chaque entité métier,
- un `occurredAt` pour dater l'action utilisateur,
- un `deletedAt` ou tombstone pour les suppressions,
- un `lastSyncCursor` pour savoir jusqu'où le local a déjà récupéré le cloud.

### 9.3 File locale d'opérations

Le local doit maintenir une file d'opérations en attente de synchronisation.

Chaque opération doit être :
- idempotente,
- rejouable,
- identifiable de manière unique,
- traçable par type d'action et entité concernée.

### 9.4 Structure cloud recommandée

Le cloud doit progressivement héberger :
- une projection agrégée du compte pour lecture rapide,
- un journal d'opérations ou sous-ensemble d'événements pour convergence,
- des métadonnées de version ou curseurs de synchro.

### 9.5 Conclusion SA2

Le modèle cible retenu est donc un modèle `snapshot + journal d'opérations`, avec transition progressive depuis le snapshot seul vers une convergence par événements.

---

## 10. SA3 — Politique de fusion par type de donnée

### 10.1 Principe

Il ne faut pas une seule règle globale de fusion.

Chaque catégorie de donnée doit avoir sa propre règle de convergence.

### 10.2 Matrice de fusion recommandée

- `Cartes ajoutées` : union par `cardId`
- `Contenu édité d'une carte` : la version la plus récente gagne, avec conservation éventuelle d'un brouillon de conflit
- `Favoris` : dernier changement explicite gagne, pas simple union
- `Suppressions` : tombstone ou `deletedAt` synchronisé
- `Progression review` : idéalement reconstruction depuis événements de réponse/session
- `XP` : calcul à partir d'événements de gain, pas somme aveugle de deux snapshots
- `Niveau` : dérivé du XP fusionné
- `Streak` : recalculé à partir des jours d'activité
- `Stats quotidiennes` : fusion par clé métier (`dayKey`, `sessionId` ou équivalent), ou recalcul
- `Profil d'intérêt futur` : dérivé d'événements d'intérêt, pas stocké comme source de vérité finale

### 10.3 Cas sensibles

Les catégories les plus sensibles sont :
- retraits de favoris,
- suppressions de cartes,
- édition concurrente d'un même contenu,
- progression review sur plusieurs appareils,
- XP si la donnée reste stockée comme simple entier final.

### 10.4 Conclusion SA3

La future synchro devra donc traiter explicitement les suppressions, les favoris et la progression review comme des données métier de premier niveau, et non comme de simples champs dans un snapshot.

---

## 11. SA4 — Progression review et événements métier

### 11.1 Problème actuel

La progression de révision est aujourd'hui synchronisée comme état final de question.

Cela reste praticable en mono-appareil, mais fragile en multi-appareils si plusieurs sessions avancent hors ligne avant convergence.

### 11.2 Modèle cible recommandé

Le modèle recommandé est un journal d'événements de révision, avec au minimum :
- `SessionStarted`
- `QuestionAnswered`
- `QuestionValidated`
- `SessionCompleted`
- éventuellement `CardEdited`, `FavoriteChanged`, `CardDeleted` dans le même écosystème global d'opérations.

### 11.3 Recalcul

La progression finale d'une question ou d'une carte devient une vue dérivée, recalculée depuis l'union des événements applicables.

Cette approche :
- réduit le risque de régression,
- simplifie la convergence multi-appareils,
- évite de choisir brutalement entre deux états finaux partiellement divergents.

### 11.4 Impact sur l'existant

Les composants les plus concernés seront :
- la persistance de session/review,
- la logique de commit de fin de session,
- la synchro Firestore,
- les stats dérivées comme XP, niveau, streak et activité quotidienne.

### 11.5 Conclusion SA4

La progression review doit à terme basculer d'une synchro d'état vers une synchro d'événements, au moins pour les éléments les plus sensibles de la progression.

---

## 12. SA5 — Migration progressive

### 12.1 Contrainte

La migration ne doit pas casser la synchro actuelle, ni rendre l'application inutilisable pendant la transition.

### 12.2 Plan de migration recommandé

Phase 1 :
- documenter précisément l'existant,
- figer les règles métier cibles,
- introduire les identifiants et métadonnées manquantes.

Phase 2 :
- ajouter une file locale d'opérations,
- conserver le snapshot comme projection de compatibilité.

Phase 3 :
- basculer les catégories simples (`favoris`, `ajouts`, `suppressions`) vers des opérations idempotentes.

Phase 4 :
- traiter la progression review via des événements métier.

Phase 5 :
- recalculer les données dérivées (`xp`, `niveau`, `streak`, stats) depuis les événements.

Phase 6 :
- réduire progressivement le rôle du snapshot global à un rôle de cache ou de projection rapide.

### 12.3 Compatibilité montante

Le système doit pouvoir lire l'ancien modèle pendant une période transitoire et produire le nouveau sans régression de compte.

### 12.4 Conclusion SA5

La migration doit être incrémentale, pilotée par lots, avec coexistence temporaire ancien/nouveau modèle.

---

## 13. SA6 — Plan de validation

### 13.1 Scénarios minimaux

La validation devra couvrir :
- mono-appareil en ligne,
- mono-appareil hors ligne puis retour réseau,
- nouvelle connexion avec cloud vide,
- nouvelle connexion avec cloud déjà peuplé,
- deux appareils connectés en parallèle,
- deux appareils hors ligne puis retour réseau,
- suppression d'une carte sur un appareil et modification sur un autre,
- retrait de favori sur un appareil et ajout sur un autre,
- réinitialisation de téléphone avant le dernier push.

### 13.2 Niveaux de test

Le chantier devra prévoir :
- tests unitaires de règles de fusion,
- tests d'intégration de la file d'opérations,
- tests de convergence multi-appareils simulés,
- recettes manuelles sur appareils réels pour les cas principaux.

### 13.3 Critères de réussite

Le système sera considéré satisfaisant si :
- les données convergent sans dialogue dans la majorité des cas,
- les suppressions et retraits ne réapparaissent pas indûment,
- la progression review ne régresse pas après usage multi-appareils,
- la personnalisation future reste compatible avec la synchro.

### 13.4 Conclusion SA6

Le plan de validation doit être conçu comme une partie du chantier, pas comme une étape de fin de parcours improvisée.