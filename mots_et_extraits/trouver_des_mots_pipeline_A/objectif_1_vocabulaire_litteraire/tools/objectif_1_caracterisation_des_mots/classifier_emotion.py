# -*- coding: utf-8 -*-
"""
classifier_emotion.py — Classifieur du Profil Émotionnel (Valence/Arousal).

Valeurs possibles :
  NEUTRE | POSITIF_EXCITANT | NEGATIF_EXCITANT | POSITIF_CALME | NEGATIF_CALME

Le profil est cartographié à partir du lexique psycholinguistique
FAN / Bonin (et des listes EMOTION_WORDS en fallback).
"""

from .word_lists import EMOTION_WORDS


def guess_emotion(word_lower: str) -> str:
    """
    Détermine le profil émotionnel d'un mot.

    Priorité :
      1. Liste explicite EMOTION_WORDS (correspondance exacte)
      2. Fallback : NEUTRE
    """
    for emotion, words in EMOTION_WORDS.items():
        if word_lower in words:
            return emotion
    return "NEUTRE"
