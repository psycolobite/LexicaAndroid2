# -*- coding: utf-8 -*-
"""
classifier_epoque.py — Classifieur de l'Époque d'apparition / usage prédominant.

Valeurs possibles :
  ANTIQUITE | CLASSIQUE_17_18 | ROMANTIQUE_19 | MODERNE_20 | CONTEMPORAIN_21

═══════════════════════════════════════════════════════════════════════════════
ARCHITECTURE : 5 couches de priorité décroissante
═══════════════════════════════════════════════════════════════════════════════

COUCHE 1 — Dictionnaire étymologique explicite (EPOQUE_EXPLICITE dans word_lists.py)
  Priorité absolue. Couvre les ~200 mots où les heuristiques échoueraient
  (mots anciens classés modernes par erreur, néologismes discrets, etc.).
  Source : TLFi / CNRTL / Académie française / première attestation + usage
           littéraire prédominant.

COUCHE 2 — Détecteurs de néologismes contemporains / modernes
  Suffixes et patterns morphologiques caractéristiques d'une époque :
  - '-ing', '-genre', '-phobie' etc.          → CONTEMPORAIN_21
  - '-isation', '-thérapie'                  → MODERNE_20
  Permet de capturer les futurs mots ajoutés sans hardcoding.

COUCHE 3 — Inférence par registre (signal sémantique fort)
  Certains registres sont historiquement datés :
  - ARCHAIQUE_RECHERCHE → CLASSIQUE_17_18 (souvent)
  - POETIQUE_LYRIQUE    → ROMANTIQUE_19 (souvent, par défaut)
  Seulement utilisé si les couches 1-2 n'ont pas conclu.

COUCHE 4 — Signaux corpus Lexique383 + pageviews Wiktionnaire
  Conserve la logique de l'algo V2 original MAIS avec des seuils plus prudents.
  Ne s'applique QUE si les couches 1-3 n'ont pas conclu.
  Problèmes connus de cette couche (raisons pour lesquelles elle est en dernière
  position) :
  - Les mots très rares ont fl=0 et ff=0, ce qui ne permet pas de conclure.
  - Les pageviews sont bruitées (curiosité ≠ usage).
  - Le ratio fl/ff est fiable uniquement pour les mots moyennement fréquents.

COUCHE 5 — Valeur par défaut
  ROMANTIQUE_19 (XIXe s.) : époque de première attestation la plus probable pour
  un mot littéraire rare non identifié par les couches précédentes.

═══════════════════════════════════════════════════════════════════════════════
"""

import math
from .word_lists import EPOQUE_EXPLICITE, SUFFIXES_CONTEMPORAIN, SUFFIXES_MODERNE


# ---------------------------------------------------------------------------
# Suffixes et préfixes caractéristiques par époque
# ---------------------------------------------------------------------------

# Suffixes typiques du XIXe (romantisme, réalisme)
_ROMANTIQUE_SUFFIXES = [
    "esque",   # pittoresque, romanesque → littéraire XIXe
]

# Suffixes typiques du XXe (psychologie, sociologie)
_MODERNE_SUFFIXES = [
    "isation",     # somatisation → XXe psychologie
    "thérapie",    # ergo-, psycho-thérapie
    "pathologie",
    "logie",       # psychologie, sociologie — ATTENTION : aussi antique, voir Couche 2 note
]

# Patterns entiers caractéristiques du contemporain (XXIe)
_CONTEMPORAIN_PATTERNS = [
    "ing",       # buzzwords anglophones
    "genre",     # cisgenre, non-binaire
    "sexual",    # demisexuel
    "binaire",
    "phobie",    # néologismes identitaires
    "inclusif",
    "intersect",
]

# Racines grecques antiques (aide pour distinguer antiquité de moderne)
_ANTIQUITE_ROOTS = [
    "atarax",    # ataraxie → épicurien
    "rhaps",     # rhapsode → grec
    "aoid",      # aède → ἀοιδός
    "harsp",     # haruspice
    "chthon",    # chthonien → χθόνιος
    "polis",
]

# Indicateurs de mots très récents (XXIe) — marqueurs culturels
_CONTEMPORAIN_CULTURAL_MARKERS = {
    "pétrichor",    # forgé en 1964, usage répandu XXIe
    "sérendipité",  # emprunt anglais, usage français XXIe
    "parangon",     # relancé politiquement XXIe
    "impéritie",    # usage politique/médiatique XXIe
    "abscons",      # (reste en CLASSIQUE via EPOQUE_EXPLICITE)
    "fuligineux",   # Gracq XXe → MODERNE_20
    "coruscant",    # Star Wars pop culture XXIe
}

# Mots dont le sens dominant est clairement antique (mythologie, rites)
_ANTIQUITE_STRONG = {
    "aède", "apophtegme", "ataraxie", "dryade", "haruspice",
    "ilote", "libation", "nymphe", "ondine", "pythonisse",
    "rhapsode", "scholastique", "sylphide", "thaumaturge",
    "vate", "codex", "oblation", "ostensoir", "patène",
}


# ---------------------------------------------------------------------------
# Couche 4 — Seuils corpus (conservatifs)
# ---------------------------------------------------------------------------

# Seuil de ratio freqfilms/freqlivres signalant un mot d'usage oral/moderne
_RATIO_FF_FL_CONTEMPORAIN = 8.0   # ff/fl > 8 → CONTEMPORAIN_21
_RATIO_FF_FL_MODERNE = 3.0        # ff/fl > 3 → MODERNE_20

_VIEWS_CONTEMPORAIN = 8000        # pageviews très élevées → usage moderne
_VIEWS_MODERNE = 3000             # pageviews modérées → MODERNE_20

# Seuil de ratio freqlivres/freqfilms signalant un mot purement littéraire/ancien
_RATIO_FL_FF_CLASSIQUE = 15.0     # fl/ff > 15 → CLASSIQUE_17_18
_FL_MIN_CLASSIQUE = 1.0           # doit avoir une fréquence livres réelle


# ---------------------------------------------------------------------------
# Fonction principale
# ---------------------------------------------------------------------------

def guess_epoque(
    word_lower: str,
    registre: str,
    lexique: dict,
    pageviews: dict,
) -> str:
    """
    Détermine l'époque d'apparition / usage prédominant d'un mot.

    Paramètres
    ----------
    word_lower : str
        Mot en minuscules normalisé.
    registre : str
        Registre calculé par classifier_registre (ex. 'ARCHAIQUE_RECHERCHE').
    lexique : dict
        Données Lexique383 {mot -> {freqlivres, freqfilms, lemme, cgram}}.
    pageviews : dict
        Cache pageviews Wiktionnaire {mot -> int}.

    Retourne
    --------
    str : L'une des 5 valeurs d'époque.
    """

    # ── COUCHE 1 : Dictionnaire étymologique explicite ────────────────────
    explicit = EPOQUE_EXPLICITE.get(word_lower)
    if explicit:
        return explicit

    # ── COUCHE 2 : Détecteurs morphologiques néologismes ─────────────────
    # 2a. Contemporain (XXIe) — patterns très caractéristiques
    for pat in _CONTEMPORAIN_PATTERNS:
        if word_lower.endswith(pat) or pat in word_lower:
            return "CONTEMPORAIN_21"

    if word_lower in _CONTEMPORAIN_CULTURAL_MARKERS:
        return "CONTEMPORAIN_21"

    # 2b. Moderne (XXe) — suffixes productifs au XXe
    # Note : '-logie' est ambigu (antique + moderne). On ne l'utilise pas seul.
    for sfx in ("isation", "thérapie", "pathologie"):
        if word_lower.endswith(sfx):
            return "MODERNE_20"

    # 2c. Antiquité — racines très caractéristiques (si non couvert par couche 1)
    if word_lower in _ANTIQUITE_STRONG:
        return "ANTIQUITE"

    # ── COUCHE 3 : Inférence par registre ────────────────────────────────
    # Un registre ARCHAIQUE_RECHERCHE signale presque toujours un mot ancien
    if registre == "ARCHAIQUE_RECHERCHE":
        return "CLASSIQUE_17_18"

    # ── COUCHE 4 : Signaux corpus (en dernier recours) ────────────────────
    fl = 0.0
    ff = 0.0
    if word_lower in lexique:
        fl = lexique[word_lower]["freqlivres"]
        ff = lexique[word_lower]["freqfilms"]

    views = pageviews.get(word_lower, 0)

    # 4a. Les mots clairement classiques (forte fréquence livres, absents des films)
    #     ne peuvent pas être modernes ou contemporains.
    is_clearly_literary = (
        (fl > _FL_MIN_CLASSIQUE and ff == 0.0)
        or (fl > 0 and ff > 0 and fl / ff > _RATIO_FL_FF_CLASSIQUE and fl > _FL_MIN_CLASSIQUE)
    )

    if not is_clearly_literary:
        # 4b. Popularité orale/numérique très forte → CONTEMPORAIN_21
        if views > _VIEWS_CONTEMPORAIN or (
            fl > 0 and ff / fl > _RATIO_FF_FL_CONTEMPORAIN and ff > 0.5
        ):
            return "CONTEMPORAIN_21"

        # 4c. Popularité modérée ou oral/film → MODERNE_20
        if views > _VIEWS_MODERNE or (
            fl > 0 and ff / fl > _RATIO_FF_FL_MODERNE and ff > 0.2
        ) or (
            ff > 0 and fl == 0.0 and (ff > 1.0 or views > 2000)
        ):
            return "MODERNE_20"

    if is_clearly_literary:
        return "CLASSIQUE_17_18"

    # 4d. Registre poétique lyrique → signal romantique XIXe
    if registre == "POETIQUE_LYRIQUE":
        return "ROMANTIQUE_19"

    # ── COUCHE 5 : Valeur par défaut ─────────────────────────────────────
    return "ROMANTIQUE_19"


# ---------------------------------------------------------------------------
# Utilitaire de diagnostic (usage en dev / test)
# ---------------------------------------------------------------------------

def explain_epoque(word_lower: str, registre: str, lexique: dict, pageviews: dict) -> str:
    """
    Retourne une explication textuelle du raisonnement de guess_epoque.
    Utile pour le debogage et la validation.
    """
    lines = [f"=== Epoque de '{word_lower}' ==="]

    explicit = EPOQUE_EXPLICITE.get(word_lower)
    if explicit:
        lines.append(f"  COUCHE 1 -> Dictionnaire explicite : {explicit}")
        return "\n".join(lines)

    lines.append("  COUCHE 1 -> Non trouve dans le dictionnaire explicite")

    for pat in _CONTEMPORAIN_PATTERNS:
        if word_lower.endswith(pat) or pat in word_lower:
            lines.append(f"  COUCHE 2a -> Pattern contemporain '{pat}' -> CONTEMPORAIN_21")
            return "\n".join(lines)

    for sfx in ("isation", "therapie", "pathologie"):
        if word_lower.endswith(sfx):
            lines.append(f"  COUCHE 2b -> Suffixe moderne '{sfx}' -> MODERNE_20")
            return "\n".join(lines)

    if word_lower in _ANTIQUITE_STRONG:
        lines.append(f"  COUCHE 2c -> Racine antique forte -> ANTIQUITE")
        return "\n".join(lines)

    if registre == "ARCHAIQUE_RECHERCHE":
        lines.append("  COUCHE 3 -> Registre ARCHAIQUE_RECHERCHE -> CLASSIQUE_17_18")
        return "\n".join(lines)

    fl = lexique.get(word_lower, {}).get("freqlivres", 0.0)
    ff = lexique.get(word_lower, {}).get("freqfilms", 0.0)
    views = pageviews.get(word_lower, 0)

    lines.append(f"  COUCHE 4 -> fl={fl:.4f}, ff={ff:.4f}, views={views}")

    is_clearly_literary = (
        (fl > _FL_MIN_CLASSIQUE and ff == 0.0)
        or (fl > 0 and ff > 0 and fl / ff > _RATIO_FL_FF_CLASSIQUE and fl > _FL_MIN_CLASSIQUE)
    )
    lines.append(f"  is_clearly_literary={is_clearly_literary}")

    result = guess_epoque(word_lower, registre, lexique, pageviews)
    lines.append(f"  -> Resultat final : {result}")
    return "\n".join(lines)
