# -*- coding: utf-8 -*-
"""
classifier_registre.py — Classifieur du Registre & Tonalité.

Valeurs possibles :
  LITTERAIRE_STANDARD | POETIQUE_LYRIQUE | ARCHAIQUE_RECHERCHE
  | TRAGIQUE_DRAMATIQUE | COMIC_BURLESQUE
"""

from .word_lists import REGISTRE_WORDS


def guess_registre(word_lower: str) -> str:
    """
    Détermine le registre et la tonalité d'un mot.

    Priorité :
      1. Liste explicite REGISTRE_WORDS (correspondance exacte)
      2. Fallback : LITTERAIRE_STANDARD
    """
    for registre, words in REGISTRE_WORDS.items():
        if word_lower in words:
            return registre
    return "LITTERAIRE_STANDARD"
