# Fonctionnement algo délai et présentation cards

**Statut :** Référence produit + base d'implémentation  
**Objectif :** Remplacer les anciennes explications floues de l'algorithme de révision, de session et de présentation des cartes.

---

## 1. Principe fondamental

Le pilotage de l'entraînement se fait **par question** et non plus par carte entière.

Chaque carte génère 2 questions indépendantes :
1. `Mot -> Définition`
2. `Définition -> Mot`

Chaque question possède sa propre logique :
- historique de réponses
- délai
- intervalle actuel
- état dans la session
- vitesse de progression

En revanche, le statut `connu` reste un statut de **carte entière**.

Conséquence :
- une carte peut avoir `Mot -> Définition` en `à travailler`
- et `Définition -> Mot` en `en cours`
- mais elle ne passe dans `connu` que lorsque **ses 2 questions** atteignent ensemble le niveau requis

Quand les 2 faces d'une même carte se retrouvent dans la même liste :
- on les regroupe visuellement en un seul élément
- on ne montre pas 2 cartes/boutons distincts
- on affiche un indicateur montrant que les 2 faces sont concernées

---

## 2. Terminologie produit

Le terme `à apprendre` est remplacé par :
- `à travailler`

Le paramètre de session doit être remis dans les réglages avec :
- valeur par défaut : `10`

Important :
- en interne, on parle bien de `questions`
- mais côté UI il faut éviter un libellé du type `nombre de questions par session`
- car l'utilisateur pourrait croire qu'il ne verra que 10 écrans, alors qu'il en verra potentiellement plus puisque certaines questions reviennent jusqu'à validation

Le libellé utilisateur devra donc exprimer l'idée d'un **lot de travail initial par session**, sans confusion sur le nombre total d'écrans affichés.

---

## 3. Constitution d'une session

Une session est construite à partir d'un **lot fixe de 10 questions par défaut**.

Ce lot est :
- sélectionné au départ
- conservé pendant la session
- présenté dans une boucle jusqu'à validation des questions
- progressivement vidé au fur et à mesure que les questions sont validées

L'utilisateur pourra voir plus de 10 écrans dans la session, car certaines questions reviennent plusieurs fois jusqu'à validation.

---

## 4. Priorités de sélection

### 4.1 Priorité globale

Priorité absolue aux questions **déjà commencées** :
- donc aux questions auxquelles l'utilisateur a déjà répondu au moins une fois
- et dont le délai est maintenant écoulé
- donc revenues dans `à travailler`

Les questions **jamais commencées** ne doivent être utilisées que :
- pour compléter une session
- s'il n'y a pas assez de questions déjà commencées revenues à échéance

### 4.2 Priorité interne parmi les questions déjà commencées

À l'intérieur des questions déjà commencées, priorité à celles :
- dont le délai est écoulé depuis le plus longtemps

Donc, parmi les questions déjà dues :
- celles qui attendent depuis le plus longtemps passent avant les autres

### 4.3 Priorité interne parmi les questions jamais commencées

À l'intérieur des questions jamais commencées, priorité à celles :
- qui ont été ajoutées en premier
- donc les plus anciennes cartes ajoutées

### 4.4 Règle globale de mélange

Les questions déjà commencées et revenues à échéance sont toujours prioritaires sur les questions jamais commencées.

Les deux types peuvent cohabiter dans une session :
- uniquement s'il n'y a pas assez de questions déjà commencées pour compléter le lot

---

## 5. Ordre global et ordre de session

### 5.1 Ordre global

Il existe un **ordre global** des questions.

Dans cet ordre global :
- les 2 questions d'une même carte se suivent
- et cet ordre suit la chronologie d'ajout des cartes

Autrement dit :
- quand une carte est ajoutée
- ses 2 questions sont ajoutées à la suite dans l'ordre global

### 5.2 Ordre de session

Une session prend :
- les `10 premières questions` de l'ordre global priorisé

Puis, pour la session elle-même :
- ces 10 questions sont triées aléatoirement
- avec la contrainte que les 2 questions d'une même carte ne soient pas demandées successivement si possible

Tolérance admise :
- si la 10e question d'une session a sa jumelle juste après dans l'ordre global
- alors elles ne seront pas dans la même session
- ce n'est pas bloquant

---

## 6. Fonctionnement d'une session

La session tourne en boucle sur son lot fixe.

Les questions sont reposées dans le même ordre de session jusqu'à validation locale.

### 6.1 Validation normale dans la session

Une question est validée dans la session si :
- elle reçoit `Je l'ai` **2 fois** dans la session
  - pas forcément d'affilée
- ou `Trop facile` **1 fois**

### 6.2 Règle spéciale si l'intervalle actuel est supérieur à t2

Si l'intervalle actuel de la question est **strictement supérieur à `t2` (1 jour)** :
- alors un `Je l'ai` à la **première présentation de session** valide directement la question
- mais si la première réponse est `À revoir`
  - alors il faudra quand même `2 "Je l'ai"` pour valider la question dans la session

### 6.3 Règle absolue sur `Trop facile`

- `Trop facile` valide toujours directement la question dans la session

### 6.4 Limite de répétition dans la session

Si une question atteint :
- `5 "À revoir"`

alors :
- elle est quand même considérée comme validée pour sortir de la session

### 6.5 Cas particulier de calcul

Si :
- la première réponse de session est un `Je l'ai` non validant
- puis l'utilisateur enchaîne `5 "À revoir"`

alors, pour le calcul long terme :
- on comptera un `À revoir`

### 6.6 Compteur de session

Le compteur affiché dans l'entraînement indique :
- le **nombre de questions restantes à valider**

Il ne doit pas afficher :
- le nombre total initial de questions sélectionnées
- le nombre d'écrans restants

Le rendu doit être lisible, esthétique et explicite.

---

## 7. Moment où la sortie de `à travailler` devient effective

Une question ne sort pas immédiatement de `à travailler` dès sa première bonne réponse de session.

La sortie de `à travailler` ne devient effective :
- qu'à la fin de la session

Et le nouveau délai démarre :
- à la fin de la session

Conséquence :
- pendant la session, on continue à travailler localement la question
- puis, à la clôture de session, on applique les nouveaux états et le nouveau délai

---

## 8. États métier

### 8.1 `à travailler`
Une question est `à travailler` si :
- elle est due immédiatement
- ou si son délai est expiré

### 8.2 `en cours`
Une question est `en cours` si :
- elle a été correctement traitée en session
- son délai est actif
- mais elle n'a pas encore atteint le seuil `connu`

### 8.3 `connu`
Une carte entière passe dans `connu` si :
- ses 2 questions ont un intervalle actuel correspondant à `t4` ou supérieur à `t4`

Important :
- une carte reste `connue` tant que son délai n'est pas expiré
- même si sa prochaine présentation est proche
- dès que le délai est écoulé, elle repasse dans `à travailler`

Cas typique :
- une question déjà `connue` et répondue directement `Je l'ai` reste au-dessus de `t4`
- donc elle reste `connue`
- sauf si l'autre face de la carte échoue (`À revoir`) et fait retomber l'état global de la carte

---

## 9. Intervalles de révision

Échelle cible :
- `t0 = 10 minutes`
- `t1 = 1 heure`
- `t2 = 1 jour`
- `t3 = 1 semaine`
- `t4 = 1 mois`
- `t5 = 3 mois`
- `t6 = 6 mois`
- `t7 = 1 an`
- `t8 = 2 ans`
- puis `4 ans`, `8 ans`, `16 ans`, etc.

---

## 10. Ce qui compte pour le calcul long terme

Pour le calcul du prochain intervalle :
- seule la **première réponse de la question dans la session** compte

Les répétitions suivantes dans la même session :
- ne comptent pas pour le calcul long terme
- servent seulement à la validation locale de la session

### Pondération des réponses

- `À revoir` = échec fort
- `Je l'ai` = réussite normale
- `Trop facile` = équivalent à `3 "Je l'ai"`

---

## 11. Ratio de difficulté / vitesse de progression

Chaque question doit posséder un indicateur de difficulté basé sur :
- le ratio bonnes réponses / erreurs
- avec pondération plus forte des réponses récentes

Effet attendu :
- une question historiquement forte monte plus vite dans les intervalles
- une question historiquement faible monte plus lentement
- après un `À revoir`, une question forte doit pouvoir revenir rapidement vers l'intervalle atteint avant la chute
- une question difficile doit reconstruire plus lentement sa progression

Le cas où une carte est revue trop tôt par rapport à son intervalle réel ne concerne ici que :
- les répétitions d'une même question à l'intérieur d'une session

Puisque seul le premier jugement de session impacte le calcul long terme.

---

## 12. Proposition de fonction de calcul du délai

Le moteur doit rester discret côté produit (t0, t1, t2, etc.), mais utiliser une valeur continue en interne pour éviter une progression trop rigide.

### 12.1 Variables à stocker par question

Pour chaque question, on propose de stocker :
- `level` : niveau continu de maîtrise (réel >= 0)
- `intervalIndex` : index discret dérivé de `floor(level)`
- `peakIntervalIndex` : plus haut intervalle déjà atteint
- `weightedSuccess` : score cumulé de réussites pondérées
- `weightedFailure` : score cumulé d'erreurs pondérées
- `recentStreak` : série récente de réussites éligibles
- `recoveryReserve` : réserve de remontée après échec
- `lastSessionFirstAnswerAt` : date du dernier vrai calcul
- `currentIntervalDurationMs` : durée correspondant à l'intervalle actuel

### 12.2 Durées d'intervalle

On définit :

```text
D(0)=10min
D(1)=1h
D(2)=1j
D(3)=1sem
D(4)=1mois
D(5)=3mois
D(6)=6mois
D(7)=1an
D(8)=2ans
D(n)=2^(n-8) * 2ans pour n>=8
```

### 12.3 Ratio de maîtrise

On définit un ratio lissé :

```text
R = weightedSuccess / (weightedSuccess + weightedFailure + 1)
```

Avec :
- `R` proche de 1 pour une question maîtrisée
- `R` proche de 0 pour une question difficile

Mise à jour proposée après chaque **première réponse de session** :

```text
weightedSuccess' = 0.85 * weightedSuccess + successWeight
weightedFailure' = 0.85 * weightedFailure + failureWeight
```

où :
- `successWeight = 1` pour `Je l'ai`
- `successWeight = 3` pour `Trop facile`
- `failureWeight = 1` pour `À revoir`

### 12.4 Facteur d'éligibilité temporelle

Pour ne pas survaloriser une question revue trop tôt, on définit :

```text
E = min(1, elapsedSinceLastRealReview / currentIntervalDuration)
```

avec :
- `E = 1` si la question a vraiment attendu son délai
- `E < 1` si elle est revue trop tôt

Dans le cadre de ce produit, ce cas concerne surtout les répétitions multiples d'une même question à l'intérieur d'une session.

Comme seule la première réponse de session compte pour le calcul long terme, ce facteur sert surtout à formaliser le fait qu'une reprise trop rapide ne doit pas gonfler artificiellement la progression.

### 12.5 En cas de `À revoir`

Règle produit :
- retour immédiat à `t0`

En interne on propose :

```text
peakIntervalIndex = max(peakIntervalIndex, intervalIndex)
recoveryReserve = peakIntervalIndex * (0.35 + 0.45 * R)
level' = 0
intervalIndex' = 0
recentStreak' = 0
```

Effet :
- la question repart à `t0`
- mais conserve une mémoire de récupération
- plus la question était historiquement forte, plus sa remontée future peut être rapide

### 12.6 En cas de `Je l'ai`

On propose :

```text
gain = E * (1
            + 1.10 * R
            + 0.25 * min(recentStreak, 4)
            + 0.35 * min(recoveryReserve, 3))
```

Puis :

```text
level' = level + gain
intervalIndex' = floor(level')
recentStreak' = recentStreak + 1
recoveryReserve' = max(0, recoveryReserve - 1)
peakIntervalIndex' = max(peakIntervalIndex, intervalIndex')
```

Effet :
- une question forte progresse plus vite
- 4 `Je l'ai` d'affilée accélèrent franchement la montée
- une question revenue de `t0` peut remonter plus vite si elle était auparavant solide

### 12.7 En cas de `Trop facile`

On propose de le traiter comme 3 réussites condensées :

```text
gainTF = E * (3
              + 1.30 * R
              + 0.40 * min(recentStreak, 4)
              + 0.40 * min(recoveryReserve, 3))
```

Puis :

```text
level' = level + gainTF
intervalIndex' = floor(level')
recentStreak' = recentStreak + 3
recoveryReserve' = max(0, recoveryReserve - 2)
peakIntervalIndex' = max(peakIntervalIndex, intervalIndex')
```

Effet :
- `Trop facile` fait monter très vite
- sans casser la logique de ratio, de récupération et de plafond naturel

### 12.8 Pourquoi cette fonction répond au besoin

Cette fonction respecte les contraintes produit :
- `À revoir` remet à `t0`
- `Trop facile` vaut 3 réussites
- une question forte remonte plus vite qu'une question faible
- 4 réussites consécutives accélèrent réellement la progression
- les dernières réponses pèsent davantage
- une question revue trop tôt ne gonfle pas artificiellement son niveau
- le système reste compatible avec une représentation finale par paliers t0, t1, t2, etc.

### 12.9 Décision produit encore ouverte

Cette fonction est une **proposition de base robuste**.
Elle devra être calibrée empiriquement sur :
- vitesse réelle de montée souhaitée
- fréquence d'exposition acceptable
- sensation utilisateur
- distribution du vocabulaire facile / difficile

---

## 13. Session interrompue

Si l'utilisateur quitte l'entraînement avant la fin :
- la session en cours doit être enregistrée
- il doit pouvoir la reprendre ensuite exactement là où il s'était arrêté

La persistance de session doit conserver au minimum :
- le lot de questions sélectionnées
- leur ordre de session
- le nombre de validations déjà obtenues par question
- le nombre de `À revoir` par question dans la session
- les événements annexes déjà planifiés (QCM, matching, défi remplaçant, question orthographique additionnelle)
- la dernière réponse, pour permettre le bouton retour/annulation

---

## 14. Bouton retour / annulation

L'entraînement doit proposer un bouton retour qui :
- annule la dernière réponse donnée
- ramène à la dernière question
- permet de répondre à nouveau

Ce mécanisme impose :
- de garder un historique local de session
- et de ne pas valider définitivement les effets long terme avant la clôture effective de la session

---

## 15. Matching intégré dans la session

Quand, au cours d'une session, on a atteint un volume de travail équivalent au paramètre de session :
- on lance un jeu de `Correspondance`
- directement dans la page d'entraînement
- sans passer par l'écran mini-jeux

### Sélection
On choisit :
- `5 cartes`
- parmi les cartes présentes dans la session
- en priorité celles qui ont accumulé le plus de `À revoir` dans la session

### Règle de validation du matching
Le matching fonctionne comme dans le mini-jeu :
- on relie d'abord toutes les cartes
- puis on valide

### Impact sur la session
Le matching :
- n'impacte pas le calcul long terme
- mais compte comme un `Je l'ai` pour l'avancement dans la session
- et cela **carte par carte** correctement matchée, même si toutes les cartes du matching ne sont pas correctes

### Clarification sur le crédit local du matching
Pour chaque carte correctement appariée dans le jeu de correspondance :
- si les 2 questions de cette carte sont présentes dans la session, alors chacune reçoit localement l'équivalent d'un `Je l'ai`
- si une seule des 2 questions de la carte est présente dans la session, alors seule cette question reçoit le crédit local
- la question jumelle absente de la session n'est pas modifiée

---

## 16. QCM intégré dans la session

Dès qu'une **même question** atteint son `3e "À revoir"` :
- un QCM est programmé

Exemple :
- `Quelle est la définition de turpitude ?`
- atteint 3 `À revoir`
- alors un QCM sur `turpitude` est inséré avant la prochaine occurrence de cette même question

### Positionnement
Le QCM est inséré :
- à une position aléatoire
- entre le moment du déclenchement
- et la prochaine présentation de la question fautive

### Sens du QCM
Si la question fautive est `Mot -> Définition` :
- on montre le mot
- on propose des définitions

Si la question fautive est `Définition -> Mot` :
- on montre la définition
- on propose des mots

### Impact
Le QCM :
- n'impacte pas le calcul long terme
- mais compte comme un `Je l'ai` pour l'avancement dans la session

---

## 17. Question orthographique additionnelle dans la session

Pour chaque question du type :
- `Quel est la définition du mot ?`
- donc pour chaque question `Mot -> Définition` présente dans la session

on ajoute aléatoirement une question additionnelle du type :
- mot flouté visuellement
- mot lu à haute voix
- l'utilisateur doit le taper et valider
- objectif : vérifier s'il sait l'orthographier

### Contraintes
Cette question :
- n'a pas d'impact sur le calcul long terme
- n'a pas d'impact sur la validation de session
- peut être passée par l'utilisateur

Si un défi orthographique remplaçant est déjà prévu pour ce cas :
- cette question additionnelle est annulée
- cette annulation vaut aussi si le défi orthographique a été armé dans une session précédente et n'a pas encore été consommé

---

## 18. Défis hors session

Les défis :
- remplacent une présentation normale
- ne viennent pas en plus

### Déclenchement
Un défi est déclenché lorsqu'une question atteint :
- `3 "Je l'ai"` d'affilée
- ou `1 "Trop facile"`
- peu importe que cela arrive à la première présentation de session ou non

### Clarification sur la portée temporelle
Le déclenchement d'un défi remplaçant n'est pas limité à la session courante.

Plus précisément :
- la condition `3 "Je l'ai" d'affilée` peut être atteinte sur plusieurs sessions
- l'état "défi à lancer" doit donc être conservé de manière persistante par question
- une fois armé, le défi est consommé lors de la **prochaine première présentation** de cette question dans une session ultérieure
- le défi ne s'ajoute pas à la session en plus de la question : il **remplace** cette première présentation normale

Le cas `1 "Trop facile"` suit la même logique :
- il arme un défi remplaçant persistant pour la prochaine première présentation de la question

### Type de défi
Pour `Définition -> Mot` :
- défi orthographique

Pour `Mot -> Définition` :
- défi sémantique

Le défi sémantique devra s'appuyer à terme sur le modèle d'embeddings contextuels.

### Impact métier du défi remplaçant
Le défi remplaçant :
- compte comme la **première réponse de session** de la question qu'il remplace
- participe donc au calcul long terme du prochain délai exactement comme une première présentation normale
- compte aussi pour l'avancement local de session puisqu'il remplace une présentation normale

---

## 19. Résumé des impacts des activités annexes

### Impact sur le calcul long terme
Comptent :
- première réponse normale de session
- défi remplaçant, puisqu'il prend la place de cette première présentation

Ne comptent pas :
- répétitions normales suivantes dans la session
- matching intégré
- QCM intégré
- question orthographique additionnelle

### Impact sur l'avancement dans la session
Comptent :
- réponses normales
- matching intégré (carte par carte correctement matchée)
- QCM intégré comme `Je l'ai`
- défi remplaçant, puisqu'il remplace une présentation

Ne comptent pas :
- question orthographique additionnelle

---

## 20. Traduction opérationnelle minimale

Pour l'implémentation, il faudra introduire :

### 20.1 Niveau persistant par question
Un modèle persistant distinct pour :
- `Mot -> Définition`
- `Définition -> Mot`

avec au minimum :
- identifiant question
- type de question
- `level`
- `intervalIndex`
- `nextDueAt`
- `peakIntervalIndex`
- `weightedSuccess`
- `weightedFailure`
- `recentStreak`
- `recoveryReserve`
- `lastSessionFirstAnswerAt`
- un indicateur persistant de défi remplaçant armé pour une prochaine session si nécessaire

### 20.2 Niveau session persistant
Un snapshot de session contenant :
- les questions sélectionnées
- leur ordre
- leur état local de validation
- leur compteur de `Je l'ai` / `À revoir` de session
- les insertions planifiées (`matching`, `QCM`, défi, question orthographique)
- l'historique nécessaire au bouton retour

### 20.3 Niveau produit / listes
Une projection regroupée par carte pour :
- `à travailler`
- `en cours`
- `connu`

avec regroupement automatique des 2 faces quand elles sont dans la même liste.

---

## 21. Résumé final

Le système cible est :
- une planification par question
- une consolidation par session
- un statut `connu` par carte entière
- des états `à travailler` / `en cours` potentiellement partiels par face
- un calcul long terme basé uniquement sur la première réponse de session
- un calcul de délai discret piloté par une progression continue
- des événements annexes injectés dans la session
- une session persistante et reprenable
- un bouton d'annulation de la dernière réponse

Ce document sert désormais de base de référence pour la conception et l'implémentation de l'algorithme.
