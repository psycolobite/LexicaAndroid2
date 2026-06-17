# -*- coding: utf-8 -*-
"""
data_loaders.py — Chargement des données externes.

Fonctions :
  - load_lexique()      : Charge Lexique383.zip (fréquences livres/films)
  - load_desrochers()   : Charge FreqSub_Imag_3600.tsv (imagabilité/fréquence subjective)
  - load_pageviews()    : Charge c1_wiktionary_pageviews.json
"""

import os
import csv
import math
import zipfile

TOOLS_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ZIP_LEXIQUE_PATH = os.path.join(TOOLS_DIR, "Lexique383.zip")
DESROCHERS_TSV_PATH = os.path.join(TOOLS_DIR, "FreqSub_Imag_3600.tsv")
PAGEVIEWS_PATH = os.path.join(TOOLS_DIR, "c1_wiktionary_pageviews.json")


def load_lexique() -> dict:
    """
    Charge Lexique383 depuis le zip et retourne un dict :
      mot_lower -> {freqlivres, freqfilms, lemme, cgram}
    Les entrées en doublon sont agrégées (somme des fréquences).
    """
    lexique = {}
    if not os.path.exists(ZIP_LEXIQUE_PATH):
        print(f"[data_loaders] Lexique383.zip introuvable à {ZIP_LEXIQUE_PATH}")
        return {}

    with zipfile.ZipFile(ZIP_LEXIQUE_PATH, "r") as z:
        tsv_name = next(
            (name for name in z.namelist() if name.endswith((".tsv", ".txt"))), None
        )
        if not tsv_name:
            print("[data_loaders] Aucun fichier TSV/TXT dans Lexique383.zip")
            return {}

        with z.open(tsv_name) as f:
            content = (line.decode("utf-8", errors="ignore") for line in f)
            reader = csv.DictReader(content, delimiter="\t")
            for row in reader:
                word = row["ortho"].lower().strip()
                if not word:
                    continue
                try:
                    freqlivres = float(row["freqlivres"])
                except (ValueError, KeyError):
                    freqlivres = 0.0
                try:
                    freqfilms = float(row["freqfilms2"])
                except (ValueError, KeyError):
                    freqfilms = 0.0

                lemme = row.get("lemme", "").lower().strip()
                cgram = row.get("cgram", "").upper().strip()

                if word in lexique:
                    lexique[word]["freqlivres"] += freqlivres
                    lexique[word]["freqfilms"] += freqfilms
                else:
                    lexique[word] = {
                        "freqlivres": freqlivres,
                        "freqfilms": freqfilms,
                        "lemme": lemme,
                        "cgram": cgram,
                    }

    print(f"[data_loaders] Lexique383 chargé : {len(lexique)} formes.")
    return lexique


def load_desrochers() -> dict:
    """
    Charge FreqSub_Imag_3600.tsv (Desrochers et al.) et retourne un dict :
      mot_lower -> {freq_mean, image_mean}
    Utilisé pour le calcul d'imagabilité/abstraction.
    """
    desrochers = {}
    if not os.path.exists(DESROCHERS_TSV_PATH):
        print(
            f"[data_loaders] Desrochers TSV introuvable à {DESROCHERS_TSV_PATH}"
        )
        return {}

    with open(DESROCHERS_TSV_PATH, "r", encoding="latin-1") as f:
        reader = csv.DictReader(f, delimiter="\t")
        for row in reader:
            word = row.get("Mot", "").lower().strip()
            if not word:
                continue
            try:
                freq_mean = float(row.get("FREQ_Mean", 0.0))
            except ValueError:
                freq_mean = 0.0
            try:
                image_mean = float(row.get("IMAGE_Mean", 0.0))
            except ValueError:
                image_mean = 0.0

            desrochers[word] = {"freq_mean": freq_mean, "image_mean": image_mean}

    print(f"[data_loaders] Desrochers chargé : {len(desrochers)} mots.")
    return desrochers


def load_pageviews() -> dict:
    """
    Charge le cache JSON des pageviews Wiktionnaire et retourne un dict :
      mot_lower -> int (nombre de vues)
    """
    import json

    if not os.path.exists(PAGEVIEWS_PATH):
        print(f"[data_loaders] Pageviews introuvables à {PAGEVIEWS_PATH}")
        return {}

    with open(PAGEVIEWS_PATH, "r", encoding="utf-8") as f:
        pageviews = json.load(f)

    print(f"[data_loaders] Pageviews chargées : {len(pageviews)} entrées.")
    return pageviews
