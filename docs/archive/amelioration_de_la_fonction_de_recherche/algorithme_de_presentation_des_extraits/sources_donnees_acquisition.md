# Plan d'Acquisition : Sources de Données & Filtrage du Corpus

Ce document liste les sources de données en accès libre, les API associées, et les méthodes de filtrage permettant de constituer notre base de données d'extraits pour la **Pipeline B** et d'alimenter la **Word Reserve**.

---

## 1. Vocabulaire Littéraire & Rhétorique (C1, C2)
*Source principale : Project Gutenberg*

### A. Filtrage par Célébrité / Notoriété (Pipeline B)
Pour éviter de présenter des extraits de textes obscurs ou de traductions de mauvaise qualité, le script appliquera deux filtres :
1.  **Popularité (Gutendex)** : Tri par nombre de téléchargements lors de l'appel à l'API Gutendex (`sort=popular`).
2.  **Liste d'auteurs classiques de référence** : Priorité absolue aux auteurs français reconnus. Le script téléchargera en priorité les œuvres de :
    *   *Littérature* : Victor Hugo, Émile Zola, Honoré de Balzac, Marcel Proust, Guy de Maupassant, Gustave Flaubert, Jules Verne, Stendhal.
    *   *Philosophie & Rhétorique* : René Descartes, Jean-Jacques Rousseau, Denis Diderot, Montesquieu, Blaise Pascal.

### B. Requête API type (Gutendex)
```http
GET https://gutendex.com/books/?languages=fr&sort=popular
```
Le script télécharge ensuite le fichier texte brut associé à la clé `text/plain` dans la réponse JSON.

---

## 2. Jargon & Domaines Spécifiques (C5)
*Sources principales : HAL, API Wikipédia (Portails, Catégories, Recherche), Wikibooks*

Le script d'acquisition implémente une **stratégie de routage adaptatif** selon la nature du domaine saisi par l'utilisateur ($K$) pour maximiser la pertinence des extraits obtenus.

### A. Classification & Routage Dynamique
Lorsqu'un domaine $K$ est saisi, le script tente de le catégoriser :

1.  **Domaines Académiques / Scientifiques** (ex: *Droit, Médecine, Psychologie, Économie, Sociologie, Physique*) :
    *   **Route 1 (HAL API)** : Recherche de résumés d'articles de recherche.
        *   *API de Recherche HAL* : `https://api.archives-ouvertes.fr/search/`
        *   *Paramètres de requête* : `q=*:*&fq=domain_s:(shs.droit)&fq=language_s:fr&fl=title_s,abstract_s,domain_s&wt=json&rows=100` (exemple pour le droit).
    *   **Route 2 (Catégories Wikipédia)** : Récupération des articles liés au portail ou à la catégorie scientifique correspondante.
        *   *API* : `https://fr.wikipedia.org/w/api.php?action=query&list=categorymembers&cmtitle=Catégorie:Psychologie&cmlimit=50&format=json`
2.  **Domaines Pratiques / Hobbies / Grand Public** (ex: *Cuisine, Couture, Jardinage, Automobile, Maquillage, Jeux Vidéo, Bricolage*) :
    *   **Bypass HAL** : Les publications universitaires étant trop théoriques ou inexistantes pour ces sujets, HAL est ignoré.
    *   **Route 1 (Recherche Wikipédia et Wikibooks)** : Requête sur les portails thématiques, les projets ou les guides pratiques de Wikibooks (ex: recettes de cuisine, guides de mécanique).
        *   *API Recherche Wikipédia* : `https://fr.wikipedia.org/w/api.php?action=query&list=search&srsearch=K&format=json` (Recherche universelle sur n'importe quel mot-clé).
    *   **Route 2 (Transcriptions thématiques / Blogs)** : Extraction de sous-titres de vidéos spécialisées de vulgarisation ou de tutoriels (ex: tutoriels de cuisine ou de couture).

### B. Algorithme de Fallback Universel
Si l'API spécialisée (HAL ou Catégories spécifiques) ne renvoie pas assez de résultats (seuil minimal de 50 paragraphes de qualité) :
1.  Le script bascule automatiquement sur la **Recherche Textuelle Générale Wikipédia** (`action=query&list=search&srsearch=K`) qui renvoie les 20 articles les plus pertinents pour le mot-clé $K$.
2.  Si aucun article n'est trouvé, le script interroge la catégorie correspondante dans le **Wiktionnaire** thématique (`Catégorie:Vocabulaire_de_la_K_en_français`).

---

## 3. Informel, Argot & Gen Z (C3)
*Sources principales : YouTube Transcripts, Twitter Datasets & Wiktionnaire Dynamique*

L'argot Gen Z et le langage d'Internet évoluant très rapidement, les sources statiques (romans, dictionnaires papier) sont complétées par du contenu web en temps réel.

### A. YouTube Transcripts API (Le "TikTok Indirect")
Puisqu'il est difficile de scraper TikTok de manière stable, le script extrait le langage parlé contemporain des créateurs populaires auprès des jeunes en France.
*   **Méthode** : Utilisation de la bibliothèque Python `youtube-transcript-api`.
*   **Cibles** : Liste pré-définie d'identifiants de chaînes YouTube (ex : Squeezie, Amixem, GP Moutarde, etc.).
*   **Traitement** : Téléchargement des transcriptions textuelles automatiques (ou soumises) des vidéos récentes, nettoyage des tags de temps, et reconstruction de paragraphes cohérents.

### B. Wiktionnaire Collaboratif
Récupération régulière des catégories d'argot vivant du Wiktionnaire pour mettre à jour nos listes de mots cibles :
*   `Catégorie:Argot_Internet_en_français`
*   `Catégorie:Argot_scolaire_en_français`
*   `Catégorie:Néologismes_en_français`

### C. Jeux de données Twitter sur Hugging Face
Téléchargement de corpus de tweets en français (ex : datasets académiques d'analyse des réseaux sociaux) pour extraire des tournures de phrases familières et réelles.

---

## 4. Critères Généraux de Filtrage des Extraits (Pipeline B)

Quel que soit le texte source téléchargé, le script applique les filtres heuristiques suivants sur chaque paragraphe pour garantir la qualité de la lecture dans l'application :

1.  **Longueur de l'extrait** : Entre **150 et 450 caractères** (format optimal pour un écran de smartphone, lisible en 15-30 secondes).
2.  **Propreté orthographique (Taux d'OCR)** : Élimination des paragraphes contenant plus de 2 % de caractères inhabituels ou de mots non reconnus dans un dictionnaire français classique (permet de rejeter les erreurs d'OCR ou les scans corrompus).
3.  **Indépendance sémantique** : Le paragraphe doit se suffire à lui-même. On rejette les phrases commençant par des pronoms sans référent direct au début de la phrase (ex : *"Il lui dit alors que..."* en début de paragraphe) ou des connecteurs de transition trop brusques.
4.  **Ponctuation** : Doit se terminer par une ponctuation forte (`.`, `!`, `?`).
