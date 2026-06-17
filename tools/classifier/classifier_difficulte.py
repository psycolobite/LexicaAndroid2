# -*- coding: utf-8 -*-
"""
classifier_difficulte.py — Calcul de la Difficulté et de l'Abstraction.

Fonctions :
  - calculate_abstraction()  : Score 0.0 (concret) → 1.0 (abstrait)
  - calculate_difficulty()   : Score 0.0 (facile) → 1.0 (difficile)
  - _get_zipf()              : Calcul du score Zipf avec fallbacks morphologiques
"""

import math
from .word_lists import CONCRETE_OBJECTS


# ---------------------------------------------------------------------------
# Helpers internes
# ---------------------------------------------------------------------------

def _clean_accent(w: str) -> str:
    """Supprime les accents pour la recherche dans Desrochers."""
    for src, dst in [
        ("é", ""), ("è", ""), ("à", ""), ("ù", ""), ("â", ""),
        ("ê", ""), ("î", ""), ("ô", ""), ("û", ""), ("ç", ""),
    ]:
        w = w.replace(src, dst)
    return w


def _morpho_fallbacks(word_lower: str) -> list:
    """
    Génère des formes morphologiquement proches pour la recherche dans Lexique383.
    Ex: 'bannissement' → 'bannir', 'séquestration' → 'séquestrer'
    """
    fallbacks = []
    w = word_lower

    if w.endswith("issement"):
        fallbacks.append(w[:-8] + "ir")
    elif w.endswith("ement") and not w.endswith("issement"):
        fallbacks.append(w[:-5] + "er")

    if w.endswith("ation"):
        fallbacks.extend([w[:-5] + "er", w[:-5] + "e", w[:-5] + "é"])

    if w.endswith("issable"):
        fallbacks.append(w[:-7] + "ir")
    elif w.endswith("able"):
        fallbacks.extend([w[:-4] + "er", w[:-4] + "ir"])

    if w.endswith("imeux"):
        fallbacks.append(w[:-5] + "in")

    if w.endswith("ibilité"):
        fallbacks.append(w[:-7] + "ible")
    elif w.endswith("icité"):
        fallbacks.extend([w[:-5] + "ique", w[:-5] + "ice"])
    elif w.endswith("acité"):
        fallbacks.append(w[:-5] + "ace")

    if w.endswith("istique"):
        fallbacks.append(w[:-7] + "isme")

    return fallbacks


def _get_zipf(
    word_lower: str,
    lexique: dict,
    pageviews: dict,
    difficulty_abstraction: float,
    registre: str,
    epoque: str,
) -> float:
    """
    Calcule le score Zipf d'un mot en utilisant Lexique383 et des fallbacks
    morphologiques, avec un ajustement dynamique basé sur les pageviews.
    """
    best_zipf = 0.0

    # Mot direct dans Lexique383
    if word_lower in lexique:
        fl = lexique[word_lower]["freqlivres"]
        ff = lexique[word_lower]["freqfilms"]
        freq = max(fl, ff)
        if freq > 0:
            best_zipf = math.log10(freq) + 3.0

    # Fallbacks morphologiques
    for fb in _morpho_fallbacks(word_lower):
        if fb in lexique:
            fl_fb = lexique[fb]["freqlivres"]
            ff_fb = lexique[fb]["freqfilms"]
            freq_fb = max(fl_fb, ff_fb)
            if freq_fb > 0:
                fb_zipf = math.log10(freq_fb) + 3.0 - 0.1  # légère pénalité
                if fb_zipf > best_zipf:
                    best_zipf = fb_zipf

    # Ajustement dynamique pageviews (buzzword moderne)
    views = pageviews.get(word_lower, 0)
    if views > 1000:
        is_archaic = registre == "ARCHAIQUE_RECHERCHE" or epoque == "CLASSIQUE_17_18"
        if registre == "LITTERAIRE_STANDARD" and not is_archaic:
            boost = math.log10(views / 1000.0) * 1.5
            coef = max(0.1, 1.2 - difficulty_abstraction)
            base = max(best_zipf, 2.2)
            dynamic_zipf = base + boost * coef
        else:
            boost = math.log10(views / 1000.0) * 0.2
            base = best_zipf if best_zipf > 0 else 1.0
            dynamic_zipf = base + boost

        if dynamic_zipf > best_zipf:
            best_zipf = dynamic_zipf

    return best_zipf


# ---------------------------------------------------------------------------
# Abstraction
# ---------------------------------------------------------------------------

def calculate_abstraction(
    word_lower: str,
    pole: str,
    lexique: dict,
    desrochers: dict,
) -> float:
    """
    Calcule un score d'abstraction continu.
      0.00 = très concret (ex: 'pétale', 'constellation')
      1.00 = extrêmement abstrait (ex: 'contingence', 'solipsisme')

    Priorité :
      1. Base Desrochers (IMAGE_Mean, imagabilité empirique)
      2. Lemme dans Desrochers (via Lexique383)
      3. Fallbacks morphologiques dans Desrochers
      4. Heuristique sémantique (pôle + suffixes)
    """
    clean_w = _clean_accent(word_lower)

    # 1. Recherche directe Desrochers
    entry = desrochers.get(word_lower) or desrochers.get(clean_w)

    # 2. Via lemme Lexique383
    if not entry and word_lower in lexique:
        lemma = lexique[word_lower].get("lemme", "")
        if lemma:
            entry = desrochers.get(lemma) or desrochers.get(_clean_accent(lemma))

    # 3. Fallbacks morphologiques
    if not entry:
        w = word_lower
        morpho = []
        if w.endswith(("er", "ir")):
            morpho += [w[:-2] + "ation", w[:-2] + "ement"]
        elif w.endswith(("eux", "ive", "if")):
            morpho += [w[:-3] + "ité", w[:-3] + "ence"]
        for mf in morpho:
            entry = desrochers.get(mf) or desrochers.get(_clean_accent(mf))
            if entry:
                break

    # 4a. Score Desrochers trouvé
    if entry and entry["image_mean"] > 0:
        # IMAGE_Mean : 1.0 (abstrait) → 7.0 (concret)
        return max(0.0, min(1.0, round((7.0 - entry["image_mean"]) / 6.0, 3)))

    # 4b. Heuristique sémantique (fallback)
    base_abs = {
        "PHILOSOPHIE_ET_IDEES": 0.85,
        "SENTIMENTS_ET_PSYCHE": 0.75,
        "ESPRIT_ET_CARACTERE": 0.60,
        "ARTS_ET_LANGAGE":     0.50,
        "NATURE_ET_COSMOS":    0.20,
    }.get(pole, 0.60)

    suffix_boost = 0.0
    if any(word_lower.endswith(s) for s in ["isme", "logie", "ence", "tence", "té", "ité", "tion", "ance", "ude"]):
        suffix_boost = 0.15
    elif any(word_lower.endswith(s) for s in ["ique", "iste", "aire", "el"]):
        suffix_boost = 0.05

    concrete_penalty = 0.35 if word_lower in CONCRETE_OBJECTS else (
        0.15 if pole == "NATURE_ET_COSMOS" and word_lower.endswith(("e", "a", "on")) else 0.0
    )

    return max(0.0, min(1.0, round(base_abs + suffix_boost - concrete_penalty, 3)))


# ---------------------------------------------------------------------------
# Difficulté
# ---------------------------------------------------------------------------

def calculate_difficulty(
    word_lower: str,
    pole: str,
    registre: str,
    epoque: str,
    lexique: dict,
    pageviews: dict,
    desrochers: dict,
) -> tuple[float, float]:
    """
    Calcule la difficulté et l'abstraction d'un mot.

    Retourne
    --------
    (difficulty, difficulty_abstraction) : tuple de deux floats dans [0.0, 1.0]

    Formule difficulté :
      difficulty = diff_base(Zipf) + len_bonus + suffix_bonus - buzz_malus
    """
    abstraction = calculate_abstraction(word_lower, pole, lexique, desrochers)

    zipf = _get_zipf(word_lower, lexique, pageviews, abstraction, registre, epoque)

    # Base Zipf recalibrée : [1.2, 3.3]
    zipf_max = 3.3
    zipf_min = 1.2
    if zipf >= zipf_max:
        diff_base = 0.0
    elif zipf <= zipf_min:
        diff_base = 1.0
    else:
        diff_base = (zipf_max - zipf) / (zipf_max - zipf_min)

    # Bonus longueur (+0.01/lettre au-delà de 5, max +0.08)
    len_bonus = max(0.0, min(0.08, (len(word_lower) - 5) * 0.01))

    # Bonus suffixes techniques
    suffix_bonus = 0.05 if any(
        word_lower.endswith(s) for s in ["phisme", "logie", "trique", "phie", "isme"]
    ) else 0.0

    # Malus buzz (mot rare mais populaire sur Wiktionnaire)
    views = pageviews.get(word_lower, 0)
    buzz_malus = 0.0
    if zipf < 2.5 and views > 1000:
        buzz_malus = min(0.15, math.log10(views / 1000.0) * 0.10)

    difficulty = max(0.0, min(1.0, round(diff_base + len_bonus + suffix_bonus - buzz_malus, 3)))
    return difficulty, abstraction
