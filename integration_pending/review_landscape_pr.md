# TACHE_31 – Correction mode paysage pour ReviewScreen

## Objectif
Adapter l’écran d’entraînement/révision pour qu’il soit entièrement utilisable en mode paysage sur téléphone et tablette.

## Modifications apportées
- Ajout d’un scroll vertical sur le contenu principal.
- Disposition adaptative : en paysage, la carte s’affiche à gauche, les boutons à droite.
- Le bouton "VOIR REPONSE" reste accessible sans gêner la lecture.
- En portrait, le layout reste classique.

## Instructions de test
1. Ouvrir l’écran de révision sur un téléphone et une tablette.
2. Passer en mode paysage : vérifier que la carte et les boutons sont bien séparés, tout reste lisible.
3. Vérifier que le bouton de retournement est accessible.
4. Tester le scroll si l’espace vertical manque.
5. Valider que le flux de révision et les défis intégrés fonctionnent normalement.

## Points de validation
- Aucun élément ne sort de la zone visible.
- Pas de blocage UX en orientation horizontale.
- Le bouton de retournement n’est jamais superposé sur le texte.

---
Modifications dans : `app/src/main/java/com/example/lexicaandroid2/presentation/review/ReviewScreen.kt`
