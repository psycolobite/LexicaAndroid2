# Lexica — Guide d'Accueil pour les Agents IA (Readme)

> [!IMPORTANT]
> **À lire impérativement lors du premier démarrage d'une session de pair-programming.** Ce document contient les règles d'engagement, la cartographie des composants et l'état actuel des travaux concernant l'avancement dans les logiques de mots et d'extraits.
si besoin d'informations plus générale sur le projet lire le TART_HERE à la racine du projet.

---

## 1. Règles d'Engagement & Droits d'Accès

*   **Accès Terminal Libre :** Vous disposez d'une autorisation permanente pour exécuter toutes les commandes de terminal nécessaires (analyses, tests, scripts) directement et **sans demande d'approbation préalable**.
*   **Validation des Plans de Modification :** Avant toute modification importante du code source ou de la documentation métier, vous **devez** élaborer un plan détaillé et **attendre l'approbation explicite de l'utilisateur** avant de procéder à l'implémentation.
*   **Innovation & Force de Proposition :** L'innovation est fortement valorisée dans ce projet. Si vous identifiez un outil, une bibliothèque ou une méthode plus performante pour résoudre un problème, proposez-la, testez-la et documentez-la.
*   **Mise à Jour Systématique :** Chaque modification apportée à un algorithme, une règle de filtrage ou une structure doit être documentée. **Ne jamais parser ou éditer directement le HTML** — utiliser le fichier JSON compagnon (voir §5 ci-dessous).
*   **Pour l'amélioration des fonctions de propositions d'extraits, ce qui implique la réfraction d'un nombre important de logique métier, et dans un soucis de simplification, une modification du lieu de stockage et une selection des fichiers importants à été opéré en date de 18/06/2026.** la nouvelle destination est C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\Mots_et_extraits qui se compose d'un dossier par logique métier dans lequel on pourra trouver un dossier tools. La localisation initial était [C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\docs\amelioration_de_la_fonction_de_recherche](../archive/amelioration_de_la_fonction_de_recherche) et C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\tools. Auncuns fichier n'a été supprimé. 
* **dans le cadre de la V1.0 de Lexica uniquement les objectifs C1 et C5 seront implémentés**
---

## 2. Périmètre des Travaux : 

### Philosophie Métier (Double Logique Hybride)
L'expérience utilisateur repose sur une distribution invisible **50% / 50%** de deux pipelines de recommandation :
1.  **Pipeline A (Word Reserve - Guidée par les mots) :** L'application sélectionne des mots d'intérêt (Word Reserve), cherche des extraits pertinents contenant ces mots dans le corpus, et les propose en flashcards avec la définition contextualisée correcte. La difficulté ici : générer une liste de mots avec des caractéristique permettant d'identifier les intérets de l'utilisateur puis proposer des mots en accords avec ses intérets. Et trouver des extraits pertinents au regard de chaques mots
2.  **Pipeline B (Corpus-First - Guidée par les extraits) :** L'application analyse directement le corpus (transcriptions de vidéos, livres, discours) pour identifier des extraits riches et captivants au regard des intérêts de l'utilisateur, puis en extrait des mots clés d'apprentissage. le point complexe constitue ici la selection d'OUVRAGE en lien avec les interets de l'utilisateur. Puis la manière dont ces ouvrages sont parsé pour trouver les mots puis selectionner un extrait les contenants.
3. **Architecture du dossier :**
mots_et_extraits/
├── Readme.md                                                            # Le guide d'accueil mis à jour
├── tableau_interactif_algorithmes.html                                  # Dashboard utilisateur
├── tableau_data.json                                                    # Source de vérité JSON
├── prevision_logique_metier_et_UI_mots_et_extraits.md                   # prévision et idée
├── dossier_avec_nom_de_la_fonction
    ├── dossier_avec_precision(optionnel)               
       ├── data/                             # Fichiers CSV/JSON de mots de référence ou candidats
       └── tools/                            # Scripts de découverte et d'enrichissement de mots
    ├── dossier_avec_precision2(optionnel)
├── dossier_avec_nom_de_la_fonction2
 
+ autre potentiels fichier d'informations(optionnel)
Parfois des dossiers de test en plus dans data et tools

---

## 3. Architecture : Le Tableau de Bord Interactif

### Infos générales

Le point d'ancrage de notre conception est le fichier **[tableau_interactif_algorithmes.html](c:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\Mots_et_extraits\tableau_interactif_algorithmes.html)**. Qui est le tableau de bord utilisateur. Il est syncronisé à un fichier dans le même dossier nommé tableau_data.json dont le format devrait  te fournir un accès plus simple que le html. Les consignes pour assurer la syncronisation sont dessus.

Il est structuré sous forme de carte mentale rétractable :
```
                           ┌── Pipeline A (Word Reserve) ──► C1 à C5 (Mots)
[Lexica Moteur de Recherche]
                           └── Pipeline B (Corpus-First) ──► C1 à C5 (Extraits)
```

Chaque pipeline se divise selon les **5 objectifs utilisateur (C1 à C5)** définis lors de l'onboarding :
*   **C1 (Littéraire) :** Lecture et écriture d'œuvres classiques, théâtre, poésie.
*   **C2 (Discussion & Rhétorique) :** Débat oratoire, connecteurs logiques, opinion, et qualificatifs politiques/sociétaux.
*   **C3 (Informel & Argot) :** Argot contemporain, internet, Gen Z, dialogues réels.
*   **C4 (Culture Générale) :** Mots insolites, curiosités lexicales, termes LGBTQIA+ et diversité.
*   **C5 (Jargon Spécifique) :** Jargon professionnel thématique (Droit, Cuisine, Médecine, etc.).

chaque sous-catégorie comporte 4 sections :
1.  *Description & Caractéristiques* (Objectifs, thèmes, graines de référence et TODO).
2.  *Algorithme & Filtres* (Formules de score, critères d'inclusion/exclusion et scripts).
3.  *Candidats Potentiels* (Listes des mots détectés classés par niveau de difficulté).
4.  *Essais & R&D* (Historique des tentatives, apprentissages et innovations documentés pour réutilisation).

### la caractérisation des mots

Les mots doivent être caractérisé de manière référentialisé (dans le sens scientifique du terme **"description référentialisé"** utilisé notamment en technique d'observation en psychologie). C'est à dire définir des Variables(ou Catégories) ayant des modalitées(ou caractéristiques) homogène et mutuellement exclusive. chaque mots se vera donc attribué une modalité de chaques variable. L'ensemble des caracteristique doit permettre de caractériser le mots de manière fine et précise.
Même chose pour les extraits.
Chaque objectif dans chaques pipeline peut disposer de sa manière de référentialiser (ses variables et modalités)
La description précise des variables et modalitées doit se trouver dans le fichier json et html.
les mots variables ou catégories sont utilisé pour décrire la même chose. 
les mots modalités ou caractéristiques sont utilisé pour décrire la même chose. (chose différente de variable) 
---

## 4. Guide Pratique pour l'Agent IA

### Comment démarrer votre session de travail :
1.  **Vérifiez le tableau de bord :** Ouvrez et analysez le fichier tableau_data.json pour identifier les priorités et lire la documentation R&D déjà en place.

2.  **Analysez les scripts existants :** Les outils d'extraction et de test se trouveaient initialement dans le dossier `tools/` (ex: [test_c2_extraction.py](file:///c:/Users/r0xef/AndroidStudioProjects/LexicaAndroid2/tools/test_c2_extraction.py), [test_c3_extraction.py](file:///c:/Users/r0xef/AndroidStudioProjects/LexicaAndroid2/tools/test_c3_extraction.py)). Toutefois dans un soucis d'accessibilité, les scripts importants on étaient déplacé en C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\Mots_et_extraits avec une section pour chaque partie du travail (recherche des mots, chercher des ouvrages, parser en extraits...) ayant chacune leurs dossier tools. 


### Comment documenter une innovation R&D :
Lorsqu'une solution algorithmique originale est mise en œuvre (comme la méthode multi-ancres ou la désambiguïsation sémantique locale), vous devez :
1.  Ouvrir le fichier (tableau_data.json)
2.  Aller dans l'onglet **"4. Essais & R&D"** de la sous-catégorie concernée. (ici c'est l'architecture du html qui est décrite)
3.  Ajouter une section claire explicitant :
    *   *Le problème rencontré* (ex: fausses définitions ou homonymes).
    *   *L'essai infructueux* si applicable (pour éviter que d'autres agents ne reproduisent l'erreur).
    *   *La solution innovante implémentée* (comment elle fonctionne techniquement).



---

## 5. Dashboard v2 — Système JSON Compagnon (⚠ À LIRE IMPÉRATIVEMENT)

> [!IMPORTANT]
> Le fichier HTML du tableau de bord (~190 Ko, 3 800+ lignes) est **trop volumineux pour être édité ou parsé directement par un agent**. Les tentatives de modification directe du HTML ont historiquement causé des corruptions de structure, des troncatures et des incohérences d'encodage.
> 
> **Ne pas tenter de lire, parser ou modifier le fichier HTML directement.**


### Workflow Bidirectionnel Agent ↔ Utilisateur

Ce système permet une synchronisation transparente dans les deux sens :

```
    [Utilisateur dans le navigateur]                 [Agent IA (Moi)]
              │                                             │
      Édite texte/structure                                 │
      ou ajoute pipelines/catégories                        │
              │                                             │
      Clic "Exporter"                                       │
              │                                             │
              ├──→ .html (sauvegardé localement)            │
              └──→ .json ───────────────────────────────────┤
                                                        Lit le JSON
                                                            ou
                                                      Modifie le JSON
                                                            │
      Fait F5 dans le navigateur ◄─────────────────── Lance apply_json_to_html.py
```

### Comment interagir avec le dashboard

**1. Pour LIRE les informations du dashboard :**
*   Consulter directement le fichier (tableau_data.json)
*   Si le JSON semble désynchronisé (après des modifications manuelles de l'utilisateur sur le HTML), exécuter :
    ```powershell
    python tools/extract_dashboard_data.py
    ```

**2. Pour MODIFIER ou AJOUTER du contenu (Texte, Candidats, Onglets, R&D...) :**
*   L'agent modifie les champs voulus directement dans le fichier compagnon (tableau_data.json)(par exemple : ajouter un onglet, éditer la R&D, etc.).
*   L'agent exécute ensuite le script de propagation :
    ```powershell
    python tools/apply_json_to_html.py
    ```
*   Cela met à jour instantanément la structure et les textes du fichier [tableau_interactif_algorithmes.html].
*   L'utilisateur a simplement à rafraîchir son navigateur (touche `F5` ou rechargement de la page) pour voir les nouvelles modifications en direct de façon esthétique et ergonomique.

**3. Pour AJOUTER une nouvelle pipeline ou catégorie structurelle :**
*   L'utilisateur peut le faire via les boutons dynamiques **＋ Pipeline** / **＋ Catégorie** directement dans le navigateur puis cliquer sur "Exporter" pour mettre à jour le JSON.
*   L'agent peut également ajouter une structure dans le JSON et utiliser `apply_json_to_html.py`.



> [!TIP]
> **Flux de travail recommandé pour l'agent** : Effectuer toutes les mises à jour textuelles de R&D ou de candidats dans le JSON compagnon, puis lancer `python tools/apply_json_to_html.py` à la fin de la tâche pour pousser les résultats vers le dashboard interactif de l'utilisateur.

---

## 7. Spécifications R&D Récentes (Pipeline B)

*   **Scoring d'Extraits C3** : Bonus substantiel de popularité appliqué conditionnellement si l'utilisateur s'intéresse à la catégorie C3 (argot/populaire) pour prioriser les mots à forte croissance.
*   **Longueur des extraits** : Pas d'exclusion stricte, mais bonification gaussienne mineure (autour de 300 caractères).
*   **Classification C1 Littéraire** : Automatisée via `tools/classify_book_difficulty.py` sur la base de la longueur des phrases, de la richesse lexicale (TTR) et des occurrences de mots rares (Zipf < 2.0).
*   **Sourcing Vidéo & Transcription** : Intégration à venir TODO (vérifié l'avancement actuel dans le json). 
---



---

## 8. État de la Liste C1 — Vocabulaire Littéraire (Pipeline A)

### Fichier de référence
`mots_et_extraits/trouver_des_mots_pipeline_A/objectif_1_vocabulaire_litteraire/data/objectif_1_liste1_mots_caracterise.csv`

### Évolution (session 18/06/2026)

| Étape | Action | Résultat |
|-------|--------|----------|
| V1 — Nettoyage | Suppression 5 mots invalides + remappage pôles sémantiques | 403 → 402 mots |
| V2 — Correction défauts | Correction NEUTRE/ROMANTIQUE_19/FRANCAIS (223 mots) | Profil, Epoque, Origine corrigés |
| V3 — Expansion | Ajout 313 mots littéraires C1 rares (sans caractéristiques) | 402 → 715 mots |
| V4 — Caractérisation | Injection caractéristiques complètes (292 mots + 32 nouveaux) | 715 → **747 mots** |

### Méthode de correction des valeurs par défaut
Les 3 champs suivants étaient remplis "par défaut" faute de données dans les bases sources (FAN, CNRTL) :
- `Profil_Emotionnel = NEUTRE` : corrigé mot par mot selon valence réelle
- `Epoque = ROMANTIQUE_19` : corrigé vers ANTIQUITE / CLASSIQUE_17_18 / MODERNE_20 selon époque d'usage réelle
- `Origine_Geographique = FRANCAIS` : ajout ARABE (élixir, talisman, azur…), ITALIEN (sbire, spadassin), ANGLAIS (truisme), PORTUGAIS (paria)

### Scripts de correction (dans scratch/)
- `correct_defaults.py` — corrige les valeurs par défaut NEUTRE/ROMANTIQUE_19/FRANCAIS
- `add_new_words.py` — ajoute des mots sans caractéristiques (dédupliqué automatiquement)
- `inject_characteristics.py` — injecte les caractéristiques complètes pour les mots vides
- `validate_c1_lists.py` — valide la cohérence entre les deux CSV

> [!NOTE]
> ~455 mots restants sans caractéristiques. À caractériser lors d'une prochaine session.