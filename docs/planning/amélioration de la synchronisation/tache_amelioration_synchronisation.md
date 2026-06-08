# TACHE_AMELIORATION_SYNCHRONISATION — Decoupage initial

## 1. Rôle du document

Ce document sert de premier découpage du futur chantier d'amélioration de la synchronisation.

Le but est de fournir au prochain Chef d'Orchestre une base pour distribuer le travail entre plusieurs agents IA avec des scopes réalistes.

---

## 2. Logique de découpage

Le chantier doit être scindé de façon à séparer :
- l'audit de l'existant,
- le modèle de données de synchronisation,
- les règles de fusion métier,
- la migration progressive depuis la logique actuelle,
- les tests et scénarios multi-appareils.

---

## 3. Lots proposés

## TACHE_SA1 — Audit complet de la synchro actuelle

### Objectif
Cartographier précisément ce qui est aujourd'hui enregistré localement, poussé vers le cloud, importé depuis le cloud et résolu silencieusement.

### Sorties attendues
- inventaire des données synchronisées,
- inventaire des déclencheurs de push,
- inventaire des cas de dialogue et de résolution,
- liste des points fragiles multi-appareils.

### Complexité cible
- moyenne.

### Réalisation
- données synchronisées identifiées : `xp`, `level`, `streak`, `lastLoginDate`, `flashcards`, `reviewQuestionProgress`, `dailyReviewStats`
- structure cloud actuelle identifiée : document `users/{uid}` + sous-collections `flashcards`, `reviewQuestionProgress`, `dailyReviewStats`
- déclencheurs actuels recensés : lancement session authentifiée, nouvelle connexion, push périodique `5 min`, fin de session review, passage arrière-plan, déconnexion, reset ciblé
- cas de dialogue recensés : surtout nouvelle connexion avec divergence significative
- fragilités multi-appareils confirmées : pas de journal d'opérations, pas de tombstones, pas de merge fin par type de donnée, remplacement de sous-collections

---

## TACHE_SA2 — Modèle cible de synchronisation

### Objectif
Définir le futur modèle de synchronisation local-first basé sur événements ou opérations.

### Périmètre
- `deviceId`, `opId`, `entityId`, `occurredAt`, `deletedAt`, curseur de sync,
- structure minimale d'une file d'opérations locale,
- stratégie de convergence cloud.

### Sorties attendues
- schéma du modèle cible,
- règles d'idempotence,
- stratégie de récupération et de replay.

### Complexité cible
- forte.

### Réalisation
- modèle cible retenu : `local-first` avec file locale d'opérations + projection snapshot
- éléments techniques requis figés : `deviceId`, `opId`, `entityId`, `occurredAt`, `deletedAt`, `lastSyncCursor`
- stratégie de convergence retenue : push des opérations locales non vues puis pull incrémental des opérations cloud non encore consommées
- contrainte d'idempotence explicitée : une opération rejouée ne doit pas casser l'état

---

## TACHE_SA3 — Politique de fusion par type de donnée

### Objectif
Définir une règle claire pour chaque catégorie de donnée synchronisée.

### Périmètre
- cartes,
- favoris,
- suppressions,
- progression review,
- XP / niveau / streak,
- stats quotidiennes,
- futures données de personnalisation.

### Sorties attendues
- matrice de fusion par type de donnée,
- cas de conflit et règle de résolution,
- liste des données dérivées à recalculer.

### Complexité cible
- forte.

### Réalisation
- règle distincte par catégorie de donnée validée
- cartes ajoutées : union par identifiant stable
- favoris : dernier changement explicite gagne
- suppressions : tombstones obligatoires
- contenu de carte édité : version la plus récente ou conflit préservé
- progression review : cible recommandée = reconstruction depuis événements
- XP / niveau / streak : données dérivées à recalculer plutôt qu'à écraser
- futur profil d'intérêt : également dérivé d'événements

---

## TACHE_SA4 — Progression review et événements métier

### Objectif
Concevoir la migration de la progression de révision depuis un état fusionné grossièrement vers un journal d'événements ou un mécanisme équivalent plus robuste.

### Sorties attendues
- modèle d'événements de session/réponse,
- stratégie de recalcul,
- estimation des impacts sur les composants existants.

### Complexité cible
- forte.

### Réalisation
- direction retenue : migrer la progression review d'un état final synchronisé vers un journal d'événements métier
- événements minimaux proposés : `SessionStarted`, `QuestionAnswered`, `QuestionValidated`, `SessionCompleted`
- bénéfice principal retenu : meilleure convergence multi-appareils et moins de régression en cas d'usage hors connexion
- impacts identifiés : persistance review, synchro cloud, recalcul de stats dérivées

---

## TACHE_SA5 — Migration progressive depuis la synchro actuelle

### Objectif
Proposer un plan de transition réaliste sans casser l'application actuelle.

### Sorties attendues
- étapes de migration,
- mode de coexistence ancien/nouveau modèle,
- stratégie de compatibilité montante.

### Complexité cible
- moyenne a forte.

### Réalisation
- migration prévue en phases incrémentales, sans rupture brutale
- conservation temporaire du snapshot global comme projection de compatibilité
- bascule progressive recommandée : favoris / suppressions / ajouts -> progression review -> données dérivées
- coexistence ancien/nouveau modèle explicitement retenue pendant la transition

---

## TACHE_SA6 — Tests et scénarios multi-appareils

### Objectif
Définir le plan de validation du futur système de synchronisation.

### Sorties attendues
- scénarios mono-appareil et multi-appareils,
- scénarios hors connexion,
- scénarios de suppression/modification concurrente,
- stratégie de tests automatisés et manuels.

### Complexité cible
- moyenne.

### Réalisation
- liste de scénarios critiques définie : mono-appareil, multi-appareils, hors connexion, suppressions, modifications concurrentes, reset appareil
- stratégie de validation à 3 niveaux retenue : unitaire, intégration, recette manuelle
- critères de réussite clarifiés : convergence silencieuse, pas de réapparition indue, pas de régression de progression review, compatibilité future avec personnalisation

---

## 4. Ordre recommandé

Ordre conseillé :
1. `TACHE_SA1`
2. `TACHE_SA2`
3. `TACHE_SA3`
4. `TACHE_SA4`
5. `TACHE_SA5`
6. `TACHE_SA6`

---

## 5. Résultat attendu

Une fois ces lots traités, le Chef d'Orchestre doit pouvoir lancer une exécution progressive d'une nouvelle synchronisation robuste, sans retomber dans une logique fragile de simple écrasement `local` contre `cloud`.

Le travail SA1 à SA6 est désormais cadré dans ce document et détaillé dans `description_amelioration_synchronisation.md`.