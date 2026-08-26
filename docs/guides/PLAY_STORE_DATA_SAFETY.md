# 🛡️ Guide Complet : Déclaration Data Safety (Sécurité des données) Google Play

Ce guide vous donne les réponses exactes à cocher dans la console Google Play pour la section **Sécurité des données (Data Safety)** de l'application **Lexica** (`com.lexica.app`).

---

## 1. Collecte et sécurité des données (Vue d'ensemble)

| Question Google Play | Réponse à choisir | Justification / Détails |
| :--- | :--- | :--- |
| **Votre application collecte-t-elle ou partage-t-elle des types de données utilisateur obligatoires ?** | **Oui** | L'app gère la connexion utilisateur et la synchronisation cloud optionnelle. |
| **Toutes les données utilisateur collectées par votre application sont-elles chiffrées en transit ?** | **Oui** | Toutes les connexions vers Firebase (Auth / Firestore) et les APIs externes utilisent HTTPS / TLS. |
| **Proposez-vous aux utilisateurs un moyen de demander la suppression de leurs données ?** | **Oui** | L'application dispose d'un bouton de suppression de compte et de réinitialisation dans l'écran Profil. |

---

## 2. Détail des types de données à déclarer

### A. Informations personnelles (Personal Info)

#### 1. Adresse e-mail (Email address)
- **Collectée ?** : Oui (si l'utilisateur crée un compte avec Firebase Auth ou Google Sign-In).
- **Partagée avec des tiers ?** : Non.
- **Traitée de manière éphémère ?** : Non (stockée sur Firebase Auth).
- **Obligatoire ou facultative ?** : **Facultative** (l'utilisateur peut utiliser l'application en mode invité / hors-ligne).
- **Finalités de collecte** :
  - *Gestion du compte (Account management)* : Pour authentifier l'utilisateur.

#### 2. Nom de l'utilisateur (Name)
- **Collecté ?** : Oui (nom/pseudo affiché récupéré via Google Sign-In si connexion Google).
- **Partagé ?** : Non.
- **Obligatoire ou facultative ?** : **Facultative**.
- **Finalités** : *Gestion du compte & Personnalisation de l'application*.

---

### B. Activité sur l'application (App Activity) & Télémétrie / Stats

> [!NOTE]
> **Pourquoi déclarer les statistiques de mots même si elles sont anonymes ?**  
> Google Play exige la déclaration de toute donnée qui **quitte l'appareil** pour aller vers un serveur, même si elle est anonyme. Si les statistiques restent sur le téléphone dans la base locale (Room), ce n'est pas considéré comme une collecte. Si vous les synchronisez dans Firestore / Cloud pour améliorer l'algorithme :

#### 1. Interactions avec l'application (App interactions)
- **Collectée ?** : Oui (si synchronisation cloud ou envoi des stats de mots).
- **Partagée ?** : Non.
- **Liée à l'identité de l'utilisateur ?** : **Non** (si anonymisé sans user_id).
- **Finalités** :
  - *Analytics / Analyse*
  - *Fonctionnalité de l'application (App functionality)* : Recommandation intelligente et personnalisation de la répétition des mots.

---

### C. Informations et performances de l'application

#### 1. Journaux de plantage et diagnostics (Crash logs / Diagnostics)
- **Collecté ?** : Oui (si utilisation de Google Play Core / Firebase Crashlytics).
- **Partagé ?** : Non.
- **Lié à l'utilisateur ?** : Non.
- **Finalité** : *Analytics / Diagnostics techniques*.

---

## 3. Déclaration relative aux enfants (Politique pour les familles / COPPA)

- **Public cible** : Sélectionner **13 ans et plus** (ou 16 ans et plus selon vos préférences).
- **Votre application s'adresse-t-elle principalement aux enfants ?** : **Non**.

---

## 4. Accès aux applications (Compte de test pour la revue Google)

Dans la console Google Play, sous la section **Contenu de l'application > Accès aux applications** :
- Choisir : **Toutes les fonctionnalités ou certaines d'entre elles sont restreintes**.
- Ajouter des instructions de connexion pour les testeurs de Google :
  - **Nom d'utilisateur / Email** : `google-review@lexica.app`
  - **Mot de passe** : `LexicaReview2026!`
  - **Instructions** : *"L'application peut être testée en mode invité ou en se connectant avec ces identifiants de test pour évaluer la synchronisation cloud et le profil."*
