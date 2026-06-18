# -*- coding: utf-8 -*-
"""
classifier_origine.py — Classifieur de l'Origine Géographique.

Valeurs possibles :
  FRANCAIS | RUSSE | ITALIEN | ANGLAIS | ALLEMAND

Les mots d'origine gréco-latine assimilés au français sont classifiés FRANCAIS.
"""

from .word_lists import SLAVIC_WORDS, ITALIAN_WORDS, ENGLISH_WORDS, GERMAN_WORDS


def guess_origine_geographique(word_lower: str) -> str:
    """
    Détermine l'origine géographique d'un mot.

    Priorité :
      1. Correspondance dans les listes d'origines connues
      2. Fallback : FRANCAIS (inclut les mots gréco-latins assimilés)
    """
    if word_lower in SLAVIC_WORDS:
        return "RUSSE"
    if word_lower in ITALIAN_WORDS:
        return "ITALIEN"
    if word_lower in ENGLISH_WORDS:
        return "ANGLAIS"
    if word_lower in GERMAN_WORDS:
        return "ALLEMAND"
    return "FRANCAIS"
