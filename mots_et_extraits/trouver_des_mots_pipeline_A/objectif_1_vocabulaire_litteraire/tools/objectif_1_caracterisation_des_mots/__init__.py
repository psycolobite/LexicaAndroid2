# -*- coding: utf-8 -*-
"""
Package classifier — Classification automatique des mots C1.
Un module par variable taxonomique.
"""
from .classifier_pole import guess_pole
from .classifier_domaine import guess_domaine
from .classifier_registre import guess_registre
from .classifier_epoque import guess_epoque
from .classifier_difficulte import calculate_abstraction, calculate_difficulty
from .classifier_origine import guess_origine_geographique

__all__ = [
    "guess_pole",
    "guess_domaine",
    "guess_registre",
    "guess_epoque",
    "calculate_abstraction",
    "calculate_difficulty",
    "guess_origine_geographique",
]
