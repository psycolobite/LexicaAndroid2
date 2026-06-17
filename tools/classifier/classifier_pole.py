# -*- coding: utf-8 -*-
"""
classifier_pole.py — Classifieur du Pôle Sémantique.

Valeurs possibles :
  ARTS_ET_LANGAGE | ESPRIT_ET_CARACTERE | NATURE_ET_COSMOS
  | PHILOSOPHIE_ET_IDEES | SENTIMENTS_ET_PSYCHE
"""

from .word_lists import POLE_MAP, THEMES_SPECIFIC_MAP


def guess_pole(word_lower: str, theme: str) -> str:
    """
    Détermine le pôle sémantique d'un mot.

    Priorité :
      1. Override explicite dans THEMES_SPECIFIC_MAP (thèmes transverses)
      2. Mapping depuis le thème d'origine (POLE_MAP)
      3. Fallback : PHILOSOPHIE_ET_IDEES
    """
    # Couche 1 : override explicite pour les thèmes transverses
    override = THEMES_SPECIFIC_MAP.get(word_lower)
    if override:
        return override

    # Couche 2 : mapping depuis le thème JSON d'origine
    return POLE_MAP.get(theme, "PHILOSOPHIE_ET_IDEES")
