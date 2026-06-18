# Spécification Technique : Scoring & Taxonomie Continue des Objectifs Utilisateur

Ce document définit les critères mathématiques et algorithmiques permettant de classifier les mots du lexique selon les objectifs de l'utilisateur, d'initialiser sa réserve de mots (*Word Reserve* - Pipeline A), et de structurer le corpus d'extraits.

---

## 1. Représentation Vectorielle Continue d'un Mot

Contrairement à une classification binaire stricte (où un mot appartient à une seule catégorie), chaque mot $w$ possède un profil d'appartenance continue sous forme de vecteur à 5 dimensions correspondant aux objectifs utilisateur :
$$\vec{P}(w) = [p_1(w), p_2(w), p_3(w), p_4(w), p_5(w)]$$

### Contraintes mathématiques :
1.  **Bornes** : $\forall i \in \{1..5\}, \ p_i(w) \in [0.0, 1.0]$.
2.  **Normalisation** : $\sum_{i=1}^5 p_i(w) = 1.0$.
    Chaque coordonnée représente le pourcentage d'appartenance du mot à l'objectif associé.

### Exemples théoriques :
*   *Chimérique* (Littéraire) : `[0.70, 0.10, 0.00, 0.20, 0.00]`
*   *Dilatoire* (Rhétorique / Juridique) : `[0.15, 0.65, 0.00, 0.05, 0.15]`
*   *Miskine* (Argot / Informel) : `[0.00, 0.00, 0.95, 0.05, 0.00]`
*   *Apoptose* (Jargon scientifique spécifique) : `[0.00, 0.00, 0.00, 0.05, 0.95]`

---

## 2. Initialisation de la Word Reserve (Pipeline A - Onboarding)

Lors de la première utilisation de l'application, l'utilisateur configure son profil :
1.  **Ses Objectifs ($\vec{U}$)** : Il sélectionne un ou plusieurs buts. Cela génère un vecteur de poids normalisé $\vec{U} = [u_1, u_2, u_3, u_4, u_5]$ tel que $\sum u_i = 1.0$.
    *   *Exemple* : S'il choisit *Vocabulaire Littéraire* ($C_1$) et *Rhétorique & Débat* ($C_2$), $\vec{U} = [0.5, 0.5, 0.0, 0.0, 0.0]$.
2.  **Son Niveau ($L$)** : Détermine la plage de fréquence lexicale cible $f_{\text{target}}$ (sur l'échelle Zipf de Lexique.org).
3.  **Domaines Spécifiques ($K$)** (Optionnel) : Saisies textuelles (ex: "Droit", "Médecine").

Pour remplir sa Word Reserve avec les $N$ premiers mots pertinents, on calcule pour chaque mot $w$ du dictionnaire global le score suivant :

$$Score(w) = \left( \vec{P}(w) \cdot \vec{U} \right) \times \text{DifficultyMatch}(w, L) \times \text{DomainMatch}(w, K)$$

### A. Alignement d'Objectifs : $\vec{P}(w) \cdot \vec{U}$
Produit scalaire mesurant l'adéquation entre les pourcentages du mot et les priorités de l'utilisateur.

### B. Correspondance de Niveau : $\text{DifficultyMatch}(w, L)$
Filtre les termes pour éviter les extrêmes (mots trop simples déjà acquis ou trop complexes pour débuter) via une décroissance gaussienne :
$$\text{DifficultyMatch}(w, L) = \exp\left( -\frac{(f(w) - f_{\text{target}})^2}{2\sigma^2} \right)$$
Où $f(w)$ est la fréquence Zipf du mot.

### C. Correspondance de Domaine Spécifique : $\text{DomainMatch}(w, K)$
*   Si le mot $w$ est identifié comme faisant partie du domaine $K$ saisi (par correspondance de tags ou proximité sémantique) $\rightarrow \text{DomainMatch}(w, K) = 2.0$ (boost).
*   Si le mot possède une forte composante de jargon ($p_5(w) > 0.3$) mais ne correspond pas du tout au domaine $K$ $\rightarrow \text{DomainMatch}(w, K) = 0.1$ (pénalité forte pour éviter de polluer avec du jargon non désiré).
*   Dans les autres cas $\rightarrow 1.0$.

---

## 3. Critères d'Appartenance aux Catégories (Calcul de $\vec{P}(w)$)

Pour alimenter la base de données, les scores bruts $S_i(w)$ pour chaque catégorie sont calculés comme suit :

### C1. Vocabulaire Littéraire ($S_{\text{lit}}$)
*   **Ratio Littéraire/Oral** : Calculé à partir de Lexique.org :
    $$R_{\text{lit}}(w) = \frac{\text{Fréquence Livres}(w) + \epsilon}{\text{Fréquence Films}(w) + \epsilon}$$
    Ce ratio est normalisé via une fonction sigmoïde pour être borné entre 0 et 1.
*   **Indicateurs morphologiques & catégoriels** : Présence dans des listes prédéfinies ou catégories de registre soutenu/littéraire issues du Wiktionnaire.

### C2. Rhétorique & Débat ($S_{\text{rhe}}$)
*   **Similarité vectorielle** : Proximité cosinus de l'embedding du mot avec un centroïde sémantique calculé sur un ensemble de mots-clés d'argumentation (*syllogisme, réfuter, sophisme, dialectique, corrélation, antithèse, allégation*).
*   **Spécificité textuelle** : Fréquence d'apparition relative dans les corpus de textes argumentatifs (essais, transcriptions de débats).
*   **Grammaire** : Mots jouant un rôle de connecteur logique ou d'articulation de la pensée.

### C3. Informel & Argot ($S_{\text{inf}}$)
*   **Ratio Oral/Littéraire** : Inverse du ratio littéraire (mots surreprésentés dans les dialogues de films/séries ou les réseaux sociaux par rapport aux livres).
*   **Tags lexicaux** : Catégories Wiktionnaire d'argot, de verlan, de néologismes familiers ou de langage jeune.

### C4. Culture Générale & Curiosités ($S_{\text{cul}}$)
> [!NOTE]
> La rareté du mot n'est **pas** un critère de sélection ici. Un mot peut être d'usage courant mais avoir un intérêt culturel majeur, ou être rare mais sans intérêt particulier.
*   **Étrangeté & Curiosité** : Mots présentant des particularités morphologiques, étymologiques ou des sonorités insolites (ex: *callipyge*, *lucifuge*, *anachronique*).
*   **Intérêt encyclopédique** : Présence et pertinence du mot à travers de grandes catégories transversales de la culture générale (Histoire, Arts, Philosophie, Sciences de la Terre, Mythologies) dans l'encyclopédie Wikipédia.
*   **Dispersion sémantique** : Le mot est utilisé de manière transverse dans plusieurs domaines plutôt que confiné dans un seul jargon hermétique.

### C5. Domaines Spécifiques / Jargon ($S_{\text{jarg}}$)
*   **Spécificité thématique (TF-IDF)** : Le mot a une fréquence d'apparition anormalement élevée au sein d'un corpus spécialisé et presque nulle dans le langage courant.
*   **Isolement sémantique** : Les embeddings de ces mots forment des micro-clusters isolés.
*   **Catégorisation Wiktionnaire** : Listes thématiques (ex: *Vocabulaire du droit en français*, *Vocabulaire de la médecine en français*).

> [!WARNING]
> **Remarque importante (Limites des outils d'extraction automatique pour le Jargon) :**
> L'utilisation seule du Wiktionnaire et des similarités sémantiques par embeddings pour extraire le jargon thématique comporte des risques de dérives et de perte de pertinence (mots obscurs sans intérêt ou faux positifs).
> 
> *   **Alternative recommandée à tester (Pipeline B)** : Il est préférable d'acquérir les mots de jargon via le **parsing d'ouvrages et de textes de référence thématiques** (ex: manuels d'introduction au droit, précis de médecine) pour garantir que les mots extraits possèdent une réelle valeur pédagogique et métier.
> *   **Action** : Une phase de test comparative devra opposer la méthode d'extraction par Wiktionnaire/embeddings et la méthode de parsing d'ouvrages (Pipeline B) afin d'évaluer la qualité du jargon retenu.

---

## 4. Normalisation et Calcul Final

Une fois les scores bruts $[S_1, S_2, S_3, S_4, S_5]$ calculés pour un mot donné, le profil de pourcentage $\vec{P}(w)$ est obtenu par normalisation L1 :

$$p_i(w) = \frac{S_i(w)}{\sum_{j=1}^5 S_j(w)}$$

---

## 5. Perspectives & Améliorations Futures

Pour affiner ces classifications sans surcharger le moteur initial, plusieurs pistes d'optimisation sont envisageables :
1.  **Embeddings sémantiques plus performants** : Remplacement des embeddings légers par des modèles de représentations de phrases/mots spécialisés (comme CamemBERT ou des modèles de type Sentence-Transformers entraînés sur le français littéraire).
2.  **API d'intelligence artificielle (LLM)** : Utilisation ponctuelle et asynchrone côté serveur d'un LLM pour classer ou valider les scores des mots ambigus ou complexes, ou pour extraire de manière intelligente les mots-clés d'ouvrages thématiques.
3.  **Validation par la communauté** : Ajustement dynamique des vecteurs $\vec{P}(w)$ en fonction des retours réels des utilisateurs (notes, ajouts ou suppressions de la Word Reserve).
