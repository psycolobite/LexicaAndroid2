# 🔭 VISION FUTURE — Lexica Android
> Section datée : **2026-04-08**  
> Statut : **Ébauche — en cours d'affinage**

---

## 1. Concept central

Le cœur du projet est le **contenu** : mots, définitions, exemples en situation.  
L'algo de présentation (SM2 + événements intégrés) est le cerveau — mais il est inutile sans un cœur riche et pertinent.

La vision : Lexica devient une **application d'enrichissement du vocabulaire français** à vocation sérieuse, ancrée dans la psychologie cognitive et les méthodes des champions de mémoire.

---

## 2. Thèmes de mots (sélection utilisateur)

L'utilisateur choisit un ou plusieurs **thèmes** qui alimentent son deck de révision.

### 2.1 Philosophie
- Vocabulaire philosophique : termes techniques, concepts, courants
- Source envisagée : corpus de textes philosophiques classiques via API

### 2.2 Littérature
Deux sous-axes :
- **Technique littéraire** : figures de style, vocabulaire d'analyse (métaphore, analepse, hyperbole, etc.)
- **Vocabulaire d'auteur** : mots rares ou avancés réellement utilisés dans les œuvres classiques
- Source envisagée : API ou corpus de textes classiques numérisés (type Gutenberg, BNF Gallica, Wikisource)
- Le mot doit être **présenté en situation** : extrait de l'œuvre où il apparaît, comme Reverso présente les traductions en contexte

### 2.3 Vocabulaire général
- Mots du français courant à vocabulaire riche (niveau B2–C2)
- Source envisagée : API dictionnaire (Larousse, CNRTL, ou équivalent)
- Accompagné d'exemples d'usage en situation (phrases réelles, pas seulement des définitions sèches)

### 2.4 Domaines spécifiques
- Exemples : automobile, cuisine, médecine, droit, architecture, musique…
- Principe : maîtriser un domaine passe par la maîtrise de son vocabulaire spécifique
- Source envisagée : dictionnaires spécialisés ou bases terminologiques

---

## 3. Processus pédagogique (basé psychologie cognitive + méthodes des champions de mémoire)

Chaque mot suit un **pipeline d'acquisition** en plusieurs phases :

```
Phase 1 — Découverte en situation
    → Présentation du mot dans son contexte réel (extrait, phrase authentique)
    → Première intuition du sens avant la définition formelle

Phase 2 — Ancrage multi-associatif  [à détailler dans une prochaine itération]
    → Activités pour associer le maximum de choses au mot
    → Étymologie, image mentale, synonymes, champ sémantique, mémoire épisodique

Phase 3 — Consolidation à long terme
    → Intégration dans le deck de révision espacée (algo SM2 existant)
    → Questions variées : QCM, correspondance, orthographe, défi sémantique, phrase inventée
```

---

## 4. Niveaux de difficulté

Pour chaque thème, **3 niveaux proposés** à la sélection :

| Niveau | Description | Critères de sélection des mots |
|--------|-------------|-------------------------------|
| 🟢 Débutant | Mots fréquents du domaine | Fréquence élevée, définition courte |
| 🟡 Intermédiaire | Mots moins courants | Fréquence moyenne, polysémie possible |
| 🔴 Avancé | Mots rares, techniques ou littéraires | Faible fréquence, richesse sémantique |

Le niveau influence la sélection des mots proposés à l'utilisateur, pas la difficulté des exercices.

---

## 5. Nouveau type de question : "Invente une phrase"

### Principe
L'utilisateur doit **rédiger une phrase originale** dans laquelle il utilise **correctement et de manière contextuelle** le mot cible.

### Objectif pédagogique
- Prouver une compréhension active (pas juste mémorisation de la définition)
- Ancre l'usage productif du mot dans la mémoire à long terme

### Critères de validation
- La phrase doit être suffisamment détaillée/contextuelle
- Le mot doit être utilisé dans le bon sens, dans le bon registre
- Validation par le **modèle d'embeddings embarqué** (TFLite MiniLM déjà présent) ou modèle IA externe si les embeddings locaux sont insuffisants pour cette tâche

### Exemple
> Mot : **"aporie"**  
> ✅ *"Le philosophe se heurtait à une aporie : défendre la liberté semblait nécessiter de contraindre ceux qui en abusaient."*  
> ❌ *"L'aporie est un mot difficile."* (trop vague, n'utilise pas le mot en contexte)

---

## 6. Sources de données — État des réflexions

| Source | Usage | Statut |
|--------|-------|--------|
| API Larousse / CNRTL | Définitions + exemples vocabulaire général | À évaluer (accès API ?) |
| Project Gutenberg | Textes classiques en accès libre | API REST disponible |
| BNF Gallica | Corpus littéraire français | API OAI-PMH disponible |
| Wikisource FR | Œuvres libres de droits en français | Scraping/API Mediawiki |
| Base terminologique (ex: FranceTerme) | Vocabulaire de domaines spécifiques | Données ouvertes |

---

## 7. Points à affiner (questions ouvertes)

Voir section **Questions d'affinage** ci-dessous — à compléter avec les réponses du porteur de projet.

### Q1 — Thèmes : fixes ou personnalisables ?
Les thèmes (Philosophie, Littérature, etc.) sont-ils une liste fixe dans l'app, ou l'utilisateur peut-il créer des thèmes personnalisés ?

### Q2 — Périmètre linguistique
Français uniquement pour l'instant, ou multilangue envisagé à terme (anglais, etc.) ?

### Q3 — Domaines spécifiques : liste fixe ou ouverte ?
Y a-t-il une liste fermée de domaines (automobile, cuisine…) ou l'utilisateur peut-il saisir un domaine libre ?

### Q4 — Validation "Invente une phrase" : embarqué ou API ?
Le modèle TFLite MiniLM déjà présent peut valider la **similarité sémantique** mais est plus limité pour évaluer la **qualité d'usage en contexte**. Est-ce qu'une validation partielle (vérification que le mot est bien dans la phrase + score de cohérence sémantique) est suffisante, ou faut-il viser une validation plus fine (via API LLM type GPT) ?

### Q5 — Curriculum ou libre choix ?
Quand l'utilisateur choisit un thème et un niveau, les mots lui sont-ils proposés **dans un ordre pédagogique** (curriculum progressif) ou il peut piocher librement dans le catalogue ?

### Q6 — Validation avant intégration au deck
Les mots suggérés via API sont-ils **automatiquement ajoutés** au deck de l'utilisateur, ou l'utilisateur valide/rejette chaque mot avant qu'il entre dans sa révision ?

### Q7 — Présentation en situation : online ou offline ?
Les extraits de textes classiques (contexte d'usage) nécessitent-ils une connexion réseau à chaque fois, ou faut-il prévoir un cache/téléchargement pour un usage offline ?

### Q8 — Relation entre thèmes et deck existant
L'utilisateur qui a déjà des mots dans son deck (ajoutés manuellement) : les thèmes viennent s'**ajouter** au deck existant ou c'est un espace séparé ?

---

## 8. Plan d'exécution global (brouillon — à valider)

> À compléter après réponses aux questions d'affinage.

```
Étape A — Infrastructure contenu
  A1. Choisir et intégrer les APIs sources (dictionnaire + corpus)
  A2. Modéliser les métadonnées enrichies d'une carte (niveau, thème, exemples)
  A3. Migrer le modèle de données Flashcard (Room) sans casser l'existant

Étape B — Sélection de thème & difficulté
  B1. Écran de sélection de thème (onboarding ou Settings)
  B2. Sélection du niveau de difficulté par thème
  B3. Pipeline de suggestion de mots (API → aperçu → validation utilisateur)

Étape C — Présentation en situation
  C1. Phase 1 du pipeline pédagogique : carte "découverte" avec extrait
  C2. UI dédiée à la présentation en situation (avant la révision)

Étape D — Nouveau type de question
  D1. Type "Invente une phrase" dans le moteur de révision
  D2. Validation par TFLite MiniLM (ou API externe si insuffisant)
  D3. Feedback utilisateur (score + explication)

Étape E — Phase d'ancrage multi-associatif  [itération future]
  E1. À définir en détail
```

---

*Document vivant — mis à jour à chaque session de travail.*

