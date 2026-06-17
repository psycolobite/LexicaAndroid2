# -*- coding: utf-8 -*-
"""
classify_c1_words_v2.py — Orchestrateur de classification des mots C1.

Ce script est le point d'entrée unique. Il délègue chaque variable
taxonomique à son module spécialisé dans le package `classifier/`.

Architecture du package classifier/ :
  classifier_pole.py        → Pôle sémantique
  classifier_domaine.py     → Domaine d'écriture cible
  classifier_registre.py    → Registre & Tonalité
  classifier_emotion.py     → Profil émotionnel (Valence/Arousal)
  classifier_epoque.py      → Époque (refonte 5 couches)
  classifier_difficulte.py  → Difficulté + Abstraction
  classifier_origine.py     → Origine géographique
  data_loaders.py           → Chargement Lexique383, Desrochers, pageviews
  word_lists.py             → Toutes les constantes et listes de mots
"""

import os
import sys
import json
import csv

# ---------------------------------------------------------------------------
# Résolution des chemins
# ---------------------------------------------------------------------------
TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))
WORDS_JSON_PATH = os.path.join(TOOLS_DIR, "consolidated_literary_words.json")
OUTPUT_CSV_PATH = os.path.join(
    os.path.dirname(TOOLS_DIR),
    "docs", "amelioration_de_la_fonction_de_recherche",
    "algorithme_de_presentation_des_extraits", "classified_c1_words_v2.csv",
)

# ---------------------------------------------------------------------------
# Imports du package classifier
# ---------------------------------------------------------------------------
sys.path.insert(0, TOOLS_DIR)

from classifier.data_loaders import load_lexique, load_desrochers, load_pageviews
from classifier.classifier_pole import guess_pole
from classifier.classifier_domaine import guess_domaine
from classifier.classifier_registre import guess_registre
from classifier.classifier_emotion import guess_emotion
from classifier.classifier_epoque import guess_epoque
from classifier.classifier_difficulte import calculate_difficulty
from classifier.classifier_origine import guess_origine_geographique


# ---------------------------------------------------------------------------
# Fonction de classification d'un mot
# ---------------------------------------------------------------------------

def classify_word(word: str, theme: str, lexique: dict, pageviews: dict, desrochers: dict) -> dict:
    """
    Classifie un mot selon les 8 variables taxonomiques.

    Retourne un dict avec les clés :
      word, pole, domaine, registre, emotion, epoque,
      difficulty, difficulty_abstraction, pertinence, origine_geographique
    """
    w = word.lower().strip()

    # 1. Pôle sémantique
    pole = guess_pole(w, theme)

    # 2. Domaine d'écriture
    domaine = guess_domaine(w)

    # 3. Registre & Tonalité
    registre = guess_registre(w)

    # 4. Profil émotionnel
    emotion = guess_emotion(w)

    # 5. Origine géographique
    origine = guess_origine_geographique(w)

    # 6. Époque (nécessite registre pour la couche 3)
    epoque = guess_epoque(w, registre, lexique, pageviews)

    # 7. Difficulté + Abstraction (nécessite epoque pour le boost pageviews)
    difficulty, abstraction = calculate_difficulty(
        w, pole, registre, epoque, lexique, pageviews, desrochers
    )

    # 8. Pertinence (dynamique, initialisée à 0.50)
    pertinence = 0.50

    return {
        "word": word,
        "pole": pole,
        "domaine": domaine,
        "registre": registre,
        "emotion": emotion,
        "epoque": epoque,
        "difficulty": difficulty,
        "difficulty_abstraction": abstraction,
        "pertinence": pertinence,
        "origine_geographique": origine,
    }


# ---------------------------------------------------------------------------
# Point d'entrée principal
# ---------------------------------------------------------------------------

def main():
    print("=" * 60)
    print("Classification C1 — v3 (package classifier/)")
    print("=" * 60)

    # Chargement des données
    if not os.path.exists(WORDS_JSON_PATH):
        print(f"[ERREUR] {WORDS_JSON_PATH} introuvable.")
        return

    with open(WORDS_JSON_PATH, "r", encoding="utf-8") as f:
        words_data = json.load(f)
    print(f"[OK] {len(words_data)} mots chargés depuis consolidated_literary_words.json")

    lexique = load_lexique()
    desrochers = load_desrochers()
    pageviews = load_pageviews()

    # Classification
    classified_list = []
    for item in words_data:
        word = item["mot"]
        theme = item["theme"]
        result = classify_word(word, theme, lexique, pageviews, desrochers)
        classified_list.append(result)

    # Écriture CSV
    os.makedirs(os.path.dirname(OUTPUT_CSV_PATH), exist_ok=True)
    with open(OUTPUT_CSV_PATH, mode="w", encoding="utf-8", newline="") as f:
        writer = csv.writer(f, delimiter=";")
        writer.writerow([
            "Mot", "Pole_Semantique", "Domaine_Ecriture", "Registre_Tonalite",
            "Profil_Emotionnel", "Epoque", "Difficulte", "Difficulte_Abstraction",
            "Pertinence", "Origine_Geographique",
        ])
        for item in classified_list:
            writer.writerow([
                item["word"],
                item["pole"],
                item["domaine"],
                item["registre"],
                item["emotion"],
                item["epoque"],
                f"{item['difficulty']:.3f}",
                f"{item['difficulty_abstraction']:.3f}",
                f"{item['pertinence']:.2f}",
                item["origine_geographique"],
            ])

    print(f"[OK] Classification terminee -> {OUTPUT_CSV_PATH}")
    print(f"[OK] {len(classified_list)} mots classifies.")


if __name__ == "__main__":
    main()
