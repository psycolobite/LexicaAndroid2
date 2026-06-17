# -*- coding: utf-8 -*-
"""
classifier_domaine.py — Classifieur du Domaine d'Écriture Cible.

Valeurs possibles :
  ROMANESQUE | THEATRE | POESIE | ESSAI_PHILOSOPHIQUE | CRITIQUE_MEMOIRES
"""

from .word_lists import DOMAINE_WORDS


def guess_domaine(word_lower: str) -> str:
    """
    Détermine le domaine d'écriture cible d'un mot.

    Priorité :
      1. Liste explicite DOMAINE_WORDS (correspondance exacte)
      2. Fallback : ROMANESQUE (style narratif/descriptif général)
    """
    for domaine, words in DOMAINE_WORDS.items():
        if word_lower in words:
            return domaine
    return "ROMANESQUE"
