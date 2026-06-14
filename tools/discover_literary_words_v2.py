#!/usr/bin/env python3
"""
discover_literary_words_v2.py
==============================
Algorithme V2 - Amélioré après calibrage sur la liste de référence.

Enseignements de V1 :
  - Seulement ~16% des mots de référence ont une catégorie Wiktionnaire explicite
  - Les catégories Wiktionnaire sont un SIGNAL FORT mais pas exclusif
  - Le ratio livres/films dans Lexique383 est l'indicateur le plus fiable
  - Lexique383 ne couvre pas tous les mots rares ou littéraires très spécialisés

Améliorations V2 :
  1. La catégorie Wiktionnaire devient un BONUS, pas un prérequis
  2. La fréquence (Zipf_books entre 1.5 et 4.0) est le critère central
  3. Le ratio livres/films sert de filtre principal de registre
  4. Les mots sans données Lexique reçoivent un score intermédiaire si Wiktionnaire les valide
  5. Détection des "intrus vrais" vs "mots absents par lacune du Wiktionnaire"
  6. Nouveau filtrage des mots communs trop répandus (procrastination, résilience...) même si littéraires
"""

import json
import csv
import os
import time
import zipfile
import io
import re
import math
import urllib.request
import urllib.parse
import unicodedata
from collections import defaultdict

# ─── CONFIGURATION ────────────────────────────────────────────────────────────

TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))
REFERENCE_CSV = os.path.join(TOOLS_DIR, "consolidated_literary_words.csv")
LEXIQUE_ZIP = os.path.join(TOOLS_DIR, "Lexique383.zip")

OUTPUT_REPORT = os.path.join(TOOLS_DIR, "literary_discovery_v2_report.md")
OUTPUT_CANDIDATES = os.path.join(TOOLS_DIR, "literary_candidates_v2.json")
OUTPUT_ENRICHED_CSV = os.path.join(TOOLS_DIR, "literary_enriched_v2.csv")
OUTPUT_NEW_WORDS = os.path.join(TOOLS_DIR, "literary_new_candidates_v2.csv")

WIKT_API_URL = "https://fr.wiktionary.org/w/api.php"
CACHE_DIR = TOOLS_DIR

WIKT_CATEGORIES = [
    "Termes soutenus en français",
    "Termes littéraires en français",
    "Termes poétiques en français",
    "Termes archaïques en français",
]

# Mots "faux amis" à exclure explicitement (mots communs, trop oraux, ou non pertinents)
# Ces mots peuvent être dans les catégories Wiktionnaire mais ne sont pas intéressants pour nous
EXPLICIT_EXCLUSIONS = {
    # Trop communs pour être des "mots de vocabulaire"
    "faire", "dire", "être", "avoir", "aller", "voir", "vouloir", "pouvoir",
    "falloir", "prendre", "venir", "savoir", "mettre", "partir",
    "bon", "grand", "petit", "long", "haut", "bas", "beau", "fort",
    "seul", "tout", "même", "autre", "tel", "tel",
    # Multi-mots (locutions)
    "ab ovo", "hic et nunc",  # peuvent être ajoutés manuellement si voulus
    # Mots trop techniques / médicaux non littéraires
    "acnéique", "balane", "concréter", "graffito", "socque", "incomestible",
    "corroder", "discontinuité",
    # Trop oraux ou trop familiers
    "bouvier", "assaisonner", "colporter", "coutumier", "fouler",
    # Adverbes peu intéressants seuls
    "diligemment", "périlleusement",
}

# Mots à valoriser explicitement même si pas dans Wiktionnaire (notre liste de référence)
# Ces thèmes sont considérés comme "littéraires" par convention
LITERARY_THEMES_BY_WORD_PATTERN = {
    # Mots de sentiment/psyché avec ratio élevé et bon Zipf = littéraires
    # Mots de temps et éphémère = littéraires par essence
    # Mots de nature/cosmos poétiques = littéraires
}

# ─── UTILITAIRES ──────────────────────────────────────────────────────────────

def normalize(word: str) -> str:
    return word.strip().lower()

def strip_accents(word: str) -> str:
    nfkd = unicodedata.normalize('NFKD', word)
    return "".join(c for c in nfkd if not unicodedata.combining(c))

def load_cache(category_name: str) -> dict | None:
    safe_name = "cache_" + re.sub(r'[^\w]', '_', category_name) + ".json"
    path = os.path.join(CACHE_DIR, safe_name)
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)
    return None

def save_cache(category_name: str, data: dict):
    safe_name = "cache_" + re.sub(r'[^\w]', '_', category_name) + ".json"
    path = os.path.join(CACHE_DIR, safe_name)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

# ─── 1. CHARGEMENT DE LA LISTE DE RÉFÉRENCE ───────────────────────────────────

def load_reference_list() -> dict:
    reference = {}
    with open(REFERENCE_CSV, "r", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f, delimiter=";")
        for row in reader:
            mot = normalize(row.get("mot", ""))
            theme = row.get("theme", "").strip()
            if mot:
                reference[mot] = theme
    print(f"[OK] Liste de référence chargée : {len(reference)} mots")
    return reference

# ─── 2. CHARGEMENT DE LEXIQUE383 ──────────────────────────────────────────────

def load_lexique383() -> dict:
    print("Chargement de Lexique383...")
    lexique = {}
    
    if not os.path.exists(LEXIQUE_ZIP):
        print(f"[!] Lexique383.zip non trouvé dans {TOOLS_DIR}")
        return {}
    
    try:
        with zipfile.ZipFile(LEXIQUE_ZIP, "r") as zf:
            tsv_files = [n for n in zf.namelist() if n.endswith(".tsv") or n.endswith(".txt")]
            target = next((n for n in tsv_files if "lexique" in n.lower() or "383" in n), None)
            if not target and tsv_files:
                target = tsv_files[0]
            if not target:
                print("[!] Aucun fichier TSV trouvé dans le ZIP")
                return {}
            
            with zf.open(target) as f:
                content = f.read().decode("utf-8", errors="replace")
                reader = csv.DictReader(io.StringIO(content), delimiter="\t")
                
                for row in reader:
                    lemme = normalize(row.get("lemme", row.get("ortho", "")))
                    if not lemme:
                        continue
                    
                    try:
                        freq_books = float(row.get("freqlivres", 0) or 0)
                        freq_films = float(row.get("freqfilms2", 0) or 0)
                    except (ValueError, TypeError):
                        freq_books = 0.0
                        freq_films = 0.0
                    
                    zipf_books = math.log10(freq_books) + 3 if freq_books > 0 else 0.0
                    zipf_films = math.log10(freq_films) + 3 if freq_films > 0 else 0.0
                    ratio = (freq_books / freq_films) if freq_films > 0 else (10.0 if freq_books > 0 else 1.0)
                    ratio = min(ratio, 50.0)
                    cgram = row.get("cgram", "")
                    
                    if lemme not in lexique or freq_books > lexique[lemme]["freq_books"]:
                        lexique[lemme] = {
                            "zipf_books": round(zipf_books, 3),
                            "zipf_films": round(zipf_films, 3),
                            "ratio": round(ratio, 3),
                            "freq_books": freq_books,
                            "freq_films": freq_films,
                            "cgram": cgram,
                        }
    except Exception as e:
        print(f"[!] Erreur lors du chargement : {e}")
        import traceback; traceback.print_exc()
        return {}
    
    print(f"[OK] Lexique383 chargé : {len(lexique)} lemmes")
    return lexique

# ─── 3. RÉCUPÉRATION WIKTIONNAIRE ─────────────────────────────────────────────

def fetch_wiktionary_category(category: str, max_pages: int = 2000) -> list:
    cached = load_cache(category)
    if cached is not None:
        words = cached.get("words", [])
        print(f"  [cache] {category} : {len(words)} mots")
        return words
    
    print(f"  Wiktionnaire : {category}...")
    words = []
    cmcontinue = None
    
    while len(words) < max_pages:
        params = {
            "action": "query",
            "list": "categorymembers",
            "cmtitle": f"Catégorie:{category}",
            "cmlimit": "500",
            "cmnamespace": "0",
            "format": "json",
        }
        if cmcontinue:
            params["cmcontinue"] = cmcontinue
        
        url = WIKT_API_URL + "?" + urllib.parse.urlencode(params)
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "LexicaAndroid/2.0 (research)"})
            with urllib.request.urlopen(req, timeout=15) as resp:
                data = json.loads(resp.read().decode("utf-8"))
        except Exception as e:
            print(f"    [!] Erreur API : {e}")
            break
        
        members = data.get("query", {}).get("categorymembers", [])
        for m in members:
            title = m.get("title", "")
            if title and not title.startswith(("Wiktionnaire:", "Modèle:", "Annexe:")):
                first_char = title[0] if title else ""
                if first_char == first_char.lower() or first_char in "éèêëàâîïôùûüÿæœ":
                    words.append(title)
        
        cont = data.get("continue", {})
        if "cmcontinue" in cont:
            cmcontinue = cont["cmcontinue"]
            time.sleep(0.3)
        else:
            break
    
    save_cache(category, {"category": category, "words": words, "count": len(words)})
    print(f"  [OK] {len(words)} mots")
    return words

# ─── 4. SCORING V2 ────────────────────────────────────────────────────────────

def score_v2(
    word: str,
    lex_data: dict | None,
    wikt_categories: list[str],
    reference: dict,
) -> dict:
    """
    Score V2 : la catégorie Wiktionnaire est un BONUS, pas un prérequis.
    Le cœur du score est basé sur Lexique383 (fréquence + ratio).
    
    Logique :
    - Un bon mot littéraire a Zipf_books ∈ [1.5, 4.2] et ratio livres/films élevé
    - Les catégories Wiktionnaire bonifient le score
    - Les mots sans données Lexique mais avec catégorie Wiktionnaire reçoivent un score moyen
    - Les mots "faux amis" sont pénalisés même s'ils ont une catégorie Wiktionnaire
    """
    
    word_norm = normalize(word)
    in_reference = word_norm in reference
    in_exclusions = word_norm in EXPLICIT_EXCLUSIONS
    
    # Données Lexique
    zb = lex_data["zipf_books"] if lex_data else 0.0
    zf = lex_data["zipf_films"] if lex_data else 0.0
    ratio = lex_data["ratio"] if lex_data else 1.0
    has_lexique = lex_data is not None
    
    # Catégories Wiktionnaire détectées
    cat_labels = []
    for cat in wikt_categories:
        cat_l = cat.lower()
        if "soutenus" in cat_l:
            cat_labels.append("soutenu")
        elif "littéraires" in cat_l:
            cat_labels.append("littéraire")
        elif "poétiques" in cat_l:
            cat_labels.append("poétique")
        elif "archaïques" in cat_l:
            cat_labels.append("archaïque")
    
    has_wikt = len(cat_labels) > 0
    
    # ── Calcul du score de base ────────────────────────────────────────────
    
    if has_lexique:
        # ─ A. Score de fréquence (critère principal)
        # Zone idéale pour vocabulaire littéraire C1/C2 : 1.5 à 4.0
        if 2.0 <= zb <= 3.5:
            freq_score = 1.0  # zone parfaite
        elif 1.5 <= zb < 2.0:
            freq_score = 0.90
        elif 3.5 < zb <= 4.0:
            freq_score = 0.85  # légèrement commun mais acceptable
        elif 4.0 < zb <= 4.5:
            # Zone grise : mots communs mais potentiellement littéraires (mélancolie, nostalgie...)
            # On ne pénalise pas trop fort si le ratio est excellent
            freq_score = 0.65
        elif 1.0 <= zb < 1.5:
            freq_score = 0.70  # rare mais utilisé
        elif 4.5 < zb <= 5.0:
            freq_score = 0.35  # trop commun
        elif zb > 5.0:
            freq_score = 0.10  # quasi-vocabulaire de base
        elif 0.5 <= zb < 1.0:
            freq_score = 0.40
        elif 0 < zb < 0.5:
            freq_score = 0.20
        else:
            freq_score = 0.30  # zb=0 mais a des données Lexique (étrange)
        
        # ─ B. Score du ratio livres/films (qualité du registre)
        # IMPORTANT : si zipf_books = 0, le ratio peut être 0 ou 1 par défaut
        # On ne pénalise pas le ratio si on n'a pas de données fiables
        if zb == 0.0 and zf == 0.0:
            # Pas de données du tout dans Lexique : ratio non fiable
            ratio_score = 0.40  # score neutre
        elif ratio >= 10.0:
            ratio_score = 1.0  # très littéraire
        elif ratio >= 5.0:
            ratio_score = 0.95
        elif ratio >= 3.0:
            ratio_score = 0.85
        elif ratio >= 2.0:
            ratio_score = 0.75
        elif ratio >= 1.5:
            ratio_score = 0.65
        elif ratio >= 1.0:
            ratio_score = 0.50
        elif ratio >= 0.5:
            ratio_score = 0.30
        else:
            ratio_score = 0.10  # plus oral qu'écrit
        
        # Compensation : si ratio très fort (>=5) et freq_score pénalisé (mot commun)
        # c'est en réalité un bon mot littéraire (ex: mélancolie, sérénité)
        if ratio >= 5.0 and freq_score < 0.70:
            freq_score = max(freq_score, 0.65)
        
        # Score Lexique combiné
        lexique_score = (freq_score * 0.60) + (ratio_score * 0.40)
        
    else:
        # Pas de données Lexique : score neutre-bas
        # Ce mot sera évalué principalement par sa présence Wiktionnaire
        lexique_score = 0.40  # score de base pour mots sans données Lexique
        freq_score = 0.40
        ratio_score = 0.40
    
    # ─ C. Bonus/malus Wiktionnaire ────────────────────────────────────────
    # V2 : le Wiktionnaire bonifie le score, pas le compose
    if has_wikt:
        # Calcul du bonus catégorie
        cat_bonus_values = {"soutenu": 0.20, "littéraire": 0.18, "poétique": 0.15, "archaïque": 0.10}
        wikt_bonus = max(cat_bonus_values.get(c, 0) for c in cat_labels)
        # Bonus supplémentaire pour multi-catégories
        if len(set(cat_labels)) >= 2:
            wikt_bonus += 0.05
        wikt_multiplier = 1.0 + wikt_bonus
    else:
        wikt_multiplier = 1.0  # pas de bonus, pas de malus
    
    # ─ D. Facteur longueur ────────────────────────────────────────────────
    word_len = len(word)
    if word_len < 4:
        len_factor = 0.25
    elif word_len == 4:
        len_factor = 0.70
    else:
        len_factor = 1.0
    
    # ─ E. Score final ─────────────────────────────────────────────────────
    base_score = lexique_score * wikt_multiplier * len_factor
    
    # NOTE: pas de bonus in_reference dans le score (biaiserait la calibration)
    # Les mots de référence sont utilisés pour calibrer les SEUILS, pas les scores
    
    # Pénalité pour exclusions explicites
    if in_exclusions:
        base_score *= 0.20
    
    # Pénalité pour mots sans données ET sans catégorie Wiktionnaire
    if not has_lexique and not has_wikt:
        base_score *= 0.40
    
    final_score = min(1.0, round(base_score, 4))
    
    # ─ Diagnostic de la raison du score ───────────────────────────────────
    reasons = []
    if zb > 4.5:
        reasons.append(f"très commun (Zipf={zb:.1f})")
    elif zb > 4.0 and ratio < 3.0:
        reasons.append(f"commun + peu littéraire (Zipf={zb:.1f}, ratio={ratio:.1f})")
    if ratio < 1.0 and has_lexique:
        reasons.append(f"oral/familier (ratio={ratio:.2f})")
    if not has_lexique:
        reasons.append("absent de Lexique383")
    if not has_wikt:
        reasons.append("absent des catégories Wiktionnaire")
    if in_exclusions:
        reasons.append("exclu explicitement")
    
    return {
        "mot": word,
        "score": final_score,
        "freq_score": round(freq_score, 3),
        "ratio_score": round(ratio_score, 3),
        "lexique_score": round(lexique_score, 3),
        "wikt_multiplier": round(wikt_multiplier, 3),
        "zipf_books": round(zb, 3),
        "zipf_films": round(zf, 3),
        "ratio": round(ratio, 3),
        "categories_wikt": list(set(cat_labels)),
        "in_reference": in_reference,
        "has_lexique_data": has_lexique,
        "has_wikt_category": has_wikt,
        "reasons": reasons,
    }

# ─── 5. SEUILS DE PERTINENCE ──────────────────────────────────────────────────

def calibrate_thresholds(reference_scored: list) -> tuple[float, float, float]:
    """
    Calcule les seuils optimaux en analysant la distribution des scores de référence.
    Retourne (seuil_faible, seuil_bon, seuil_excellent).
    
    On exclut de la calibration les mots sans données (score purement arbitraire).
    """
    # Utiliser uniquement les mots avec données Lexique OU Wiktionnaire pour calibrer
    scores_with_data = sorted(
        [s["score"] for s in reference_scored if s["has_lexique_data"] or s["has_wikt_category"]],
        reverse=True
    )
    n = len(scores_with_data)
    if n < 5:
        return (0.40, 0.55, 0.70)
    
    # P15 = seuil de "rejet" (les 15% les plus faibles avec données)
    p15 = scores_with_data[int(n * 0.85)]
    # P30 = seuil de "bon"
    p30 = scores_with_data[int(n * 0.70)]
    # P65 = seuil d'"excellent"
    p65 = scores_with_data[int(n * 0.35)]
    
    print(f"  (Calibration sur {n} mots de réf. avec données)")
    return (round(p15, 3), round(p30, 3), round(p65, 3))

# ─── 6. ALGORITHME PRINCIPAL ──────────────────────────────────────────────────

def main():
    print("=" * 65)
    print("ALGORITHME V2 - DÉCOUVERTE DE MOTS LITTÉRAIRES FRANÇAIS")
    print("=" * 65)
    
    # 1. Charger la liste de référence
    reference = load_reference_list()
    reference_set = set(reference.keys())
    
    # 2. Charger Lexique383
    lexique = load_lexique383()
    
    # 3. Récupérer les catégories Wiktionnaire
    print("\n--- Catégories Wiktionnaire ---")
    word_to_categories = defaultdict(list)
    
    for cat in WIKT_CATEGORIES:
        words = fetch_wiktionary_category(cat)
        for w in words:
            w_norm = normalize(w)
            if cat not in word_to_categories[w_norm]:
                word_to_categories[w_norm].append(cat)
    
    print(f"\n[OK] Total mots Wiktionnaire uniques : {len(word_to_categories)}")
    
    # 4. Construire le pool complet :
    # - Tous les mots Wiktionnaire
    # - Tous les mots de référence (même si pas dans Wiktionnaire)
    all_words = set(word_to_categories.keys()) | reference_set
    print(f"[OK] Pool total (Wikt + référence) : {len(all_words)} mots")
    
    # 5. Scorer chaque mot
    print("\n--- Calcul des scores V2 ---")
    scored_words = []
    
    for word in all_words:
        # Chercher dans Lexique383
        lex_data = lexique.get(word) or lexique.get(strip_accents(word))
        cats = list(word_to_categories.get(word, []))
        
        result = score_v2(word, lex_data, cats, reference)
        scored_words.append(result)
    
    # Trier par score décroissant
    scored_words.sort(key=lambda x: (-x["score"], x["mot"]))
    
    # 6. Calibration sur les mots de référence
    print("\n--- Calibration des seuils ---")
    reference_scored = [s for s in scored_words if s["in_reference"]]
    seuil_faible, seuil_bon, seuil_excellent = calibrate_thresholds(reference_scored)
    
    print(f"  Seuil rejet (P10 ref.)    : < {seuil_faible:.3f}")
    print(f"  Seuil bon mot (P25 ref.)  : >= {seuil_bon:.3f}")
    print(f"  Seuil excellent (P60 ref.): >= {seuil_excellent:.3f}")
    
    # 7. Catégorisation
    def classify(s):
        if s >= seuil_excellent:
            return "Excellent"
        elif s >= seuil_bon:
            return "Bon"
        elif s >= seuil_faible:
            return "Moyen"
        else:
            return "Faible"
    
    # Intrus potentiels dans notre liste de référence
    intruders = [(s["mot"], s["score"], s["reasons"]) 
                 for s in reference_scored 
                 if s["score"] < seuil_faible]
    
    # Vrais intrus vs lacunes du Wiktionnaire
    true_intruders = [(w, sc, r) for w, sc, r in intruders 
                      if not any("absent du Wiktionnaire" in x or "absent de Lexique383" in x for x in r)]
    
    # Mots de référence légitimes avec mauvais score (à cause d'une lacune de données)
    data_gap_words = [(w, sc, r) for w, sc, r in intruders if (w, sc, r) not in true_intruders]
    
    # Nouveaux candidats excellents non encore dans notre liste
    new_excellent = [s for s in scored_words 
                     if not s["in_reference"] 
                     and s["score"] >= seuil_excellent
                     and s["mot"] not in EXPLICIT_EXCLUSIONS
                     and len(s["mot"]) >= 5
                     and " " not in s["mot"]][:200]
    
    # Nouveaux candidats "bons"
    new_good = [s for s in scored_words 
                if not s["in_reference"] 
                and seuil_bon <= s["score"] < seuil_excellent
                and s["mot"] not in EXPLICIT_EXCLUSIONS
                and len(s["mot"]) >= 5
                and " " not in s["mot"]][:200]
    
    # ─── 8. Rapport V2 ────────────────────────────────────────────────────────
    print("\n--- Génération du rapport V2 ---")
    
    ref_with_wikt = sum(1 for s in reference_scored if s["has_wikt_category"])
    ref_with_lexique = sum(1 for s in reference_scored if s["has_lexique_data"])
    
    scores_ref = [s["score"] for s in reference_scored]
    avg_score_ref = sum(scores_ref) / len(scores_ref) if scores_ref else 0
    
    covered = sum(1 for s in reference_scored if s["score"] >= seuil_bon)
    
    with open(OUTPUT_REPORT, "w", encoding="utf-8") as f:
        f.write("# Rapport V2 - Découverte de Mots Littéraires\n\n")
        f.write("**Algorithme** : V2 (catégorie Wiktionnaire = bonus, Lexique383 = critère central)\n")
        f.write(f"**Date** : {time.strftime('%Y-%m-%d %H:%M')}\n\n")
        f.write("---\n\n")
        
        # Statistiques
        f.write("## Statistiques Globales\n\n")
        f.write(f"| Métrique | Valeur |\n|---|---|\n")
        f.write(f"| Mots de référence | {len(reference_set)} |\n")
        f.write(f"| Mots Wiktionnaire (4 catégories) | {len(word_to_categories)} |\n")
        f.write(f"| Pool total analysé | {len(all_words)} |\n")
        f.write(f"| Mots de réf. avec catégorie Wiktionnaire | {ref_with_wikt} ({100*ref_with_wikt//len(reference_set)}%) |\n")
        f.write(f"| Mots de réf. avec données Lexique383 | {ref_with_lexique} ({100*ref_with_lexique//len(reference_set)}%) |\n\n")
        
        f.write(f"### Calibration des seuils (sur {len(reference_scored)} mots de référence)\n\n")
        f.write(f"| Seuil | Score | Signification |\n|---|---|---|\n")
        f.write(f"| Rejet | < {seuil_faible:.3f} | Intrus potentiel |\n")
        f.write(f"| Bon | >= {seuil_bon:.3f} | Mot pertinent |\n")
        f.write(f"| Excellent | >= {seuil_excellent:.3f} | Mot très pertinent |\n\n")
        
        f.write(f"**Score moyen des mots de référence** : {avg_score_ref:.3f}\n")
        f.write(f"**Couverture de la liste de référence** : {covered}/{len(reference_set)} mots ({100*covered//max(1,len(reference_set))}%)\n\n")
        
        f.write("---\n\n")
        
        # Distribution des catégories
        f.write("## Distribution des Scores (toutes catégories confondues)\n\n")
        all_by_class = {"Excellent": 0, "Bon": 0, "Moyen": 0, "Faible": 0}
        ref_by_class = {"Excellent": 0, "Bon": 0, "Moyen": 0, "Faible": 0}
        for s in scored_words:
            cls = classify(s["score"])
            all_by_class[cls] += 1
            if s["in_reference"]:
                ref_by_class[cls] += 1
        
        f.write(f"| Classe | Tous mots | Mots de référence |\n|---|---|---|\n")
        for cls in ["Excellent", "Bon", "Moyen", "Faible"]:
            f.write(f"| {cls} | {all_by_class[cls]} | {ref_by_class[cls]} |\n")
        f.write("\n---\n\n")
        
        # INTRUS VRAIS
        f.write(f"## Intrus Avérés dans Notre Liste de Référence ({len(true_intruders)} mots)\n\n")
        if true_intruders:
            f.write("Ces mots ont un score faible ET ont des données disponibles dans Lexique383/Wiktionnaire.\n")
            f.write("Ils sont probablement trop communs, trop oraux, ou mal classés dans notre liste.\n\n")
            f.write("| Mot | Thème | Score | Zipf | Ratio | Catégories Wikt | Raisons |\n")
            f.write("|---|---|---|---|---|---|---|\n")
            for word, sc, reasons in sorted(true_intruders, key=lambda x: x[1]):
                found = next((s for s in scored_words if s["mot"] == word), None)
                if found:
                    theme = reference.get(word, "?")
                    cats_str = "+".join(found["categories_wikt"]) if found["categories_wikt"] else "—"
                    reason_str = ", ".join(reasons[:2]) if reasons else "score composite faible"
                    f.write(f"| **{word}** | {theme} | {sc:.3f} | {found['zipf_books']:.2f} | "
                            f"{found['ratio']:.2f} | {cats_str} | {reason_str} |\n")
        else:
            f.write("✅ Aucun intrus avéré détecté !\n")
        
        f.write("\n---\n\n")
        
        # Mots de référence avec lacune de données
        if data_gap_words:
            f.write(f"## Mots de Référence avec Lacune de Données ({len(data_gap_words)} mots)\n\n")
            f.write("Ces mots de notre liste ont un score faible à cause d'un **manque de données** dans\n")
            f.write("Lexique383 ou les catégories Wiktionnaire — ils ne sont pas nécessairement de mauvais mots.\n\n")
            f.write("| Mot | Thème | Score | Raisons |\n|---|---|---|---|\n")
            for word, sc, reasons in sorted(data_gap_words, key=lambda x: x[1]):
                theme = reference.get(word, "?")
                reason_str = ", ".join(reasons[:2]) if reasons else "?"
                f.write(f"| {word} | {theme} | {sc:.3f} | {reason_str} |\n")
            f.write("\n---\n\n")
        
        # Nouveaux candidats excellents
        f.write(f"## Nouveaux Candidats Excellents ({len(new_excellent)} mots)\n\n")
        f.write(f"Mots avec score >= {seuil_excellent:.3f}, non dans notre liste de référence, "
                f"filtrés des exclusions explicites :\n\n")
        f.write("| Mot | Score | Zipf | Ratio | Catégories Wikt | Classe |\n")
        f.write("|---|---|---|---|---|---|\n")
        for s in new_excellent[:80]:
            cats_str = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
            cls = classify(s["score"])
            f.write(f"| **{s['mot']}** | {s['score']:.3f} | {s['zipf_books']:.2f} | "
                    f"{s['ratio']:.2f} | {cats_str} | {cls} |\n")
        
        f.write("\n---\n\n")
        
        # Nouveaux candidats bons
        f.write(f"## Nouveaux Candidats Bons ({len(new_good)} mots)\n\n")
        f.write(f"Mots avec score entre {seuil_bon:.3f} et {seuil_excellent:.3f} :\n\n")
        f.write("| Mot | Score | Zipf | Ratio | Catégories Wikt |\n|---|---|---|---|---|\n")
        for s in new_good[:60]:
            cats_str = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
            f.write(f"| {s['mot']} | {s['score']:.3f} | {s['zipf_books']:.2f} | "
                    f"{s['ratio']:.2f} | {cats_str} |\n")
        
        f.write("\n---\n\n")
        
        # Top 50 (tous mots)
        f.write("## Top 50 Tous Mots Confondus\n\n")
        f.write("| Rang | Mot | Score | Zipf | Ratio | Catégories | Dans Ref. |\n")
        f.write("|---|---|---|---|---|---|---|\n")
        for i, s in enumerate(scored_words[:50]):
            cats_str = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
            ref_str = "✓" if s["in_reference"] else "—"
            f.write(f"| {i+1} | **{s['mot']}** | {s['score']:.3f} | {s['zipf_books']:.2f} | "
                    f"{s['ratio']:.2f} | {cats_str} | {ref_str} |\n")
        
        f.write("\n---\n\n")
        
        # Analyse de la couverture de la liste de référence
        f.write("## Analyse de Couverture de la Liste de Référence\n\n")
        f.write("### Mots de référence par classe de score :\n\n")
        f.write("| Classe | Mots de référence | % |\n|---|---|---|\n")
        for cls in ["Excellent", "Bon", "Moyen", "Faible"]:
            pct = 100 * ref_by_class[cls] // max(1, len(reference_scored))
            f.write(f"| {cls} | {ref_by_class[cls]} | {pct}% |\n")
        
        f.write("\n### Mots de référence dans la zone Faible (< P10) :\n\n")
        f.write("| Mot | Score | Zipf | Ratio | Raisons |\n|---|---|---|---|---|\n")
        for s in sorted(reference_scored, key=lambda x: x["score"])[:30]:
            if s["score"] < seuil_faible:
                reasons = ", ".join(s["reasons"][:2]) if s["reasons"] else "?"
                f.write(f"| {s['mot']} | {s['score']:.3f} | {s['zipf_books']:.2f} | "
                        f"{s['ratio']:.2f} | {reasons} |\n")
        
        f.write("\n---\n\n")
        
        # Recommendations finales
        f.write("## Recommandations Algorithmiques\n\n")
        f.write("### Critères de sélection optimaux (V2) :\n\n")
        f.write(f"1. **Score V2 >= {seuil_bon:.2f}** : seuil calibré sur P25 des mots de référence\n")
        f.write("2. **Zipf_books ∈ [1.0, 4.5]** : ni trop rare ni trop commun\n")
        f.write("3. **Ratio >= 1.5** : plus écrit qu'oral (sauf compensation par catégorie Wiktionnaire)\n")
        f.write("4. **Longueur >= 5 caractères** : les mots courts (4 lettres ou moins) sont rarement pertinents\n")
        f.write("5. **Pas de locutions** (pas d'espace dans le mot) : garder des mots simples\n")
        f.write("6. **Catégorie Wiktionnaire** : bonus fort si soutenu/littéraire/poétique, mais pas obligatoire\n\n")
        f.write(f"### Bilan :\n\n")
        f.write(f"- {covered}/{len(reference_set)} mots de référence couverts ({100*covered//max(1,len(reference_set))}%)\n")
        f.write(f"- {len(true_intruders)} intrus avérés dans notre liste\n")
        f.write(f"- {len(data_gap_words)} mots de référence légitimes avec lacune de données\n")
        f.write(f"- {len(new_excellent)} nouveaux candidats excellents\n")
        f.write(f"- {len(new_good)} nouveaux candidats bons\n")
    
    print(f"[OK] Rapport V2 : {OUTPUT_REPORT}")
    
    # 9. CSV enrichi (liste de référence + scores V2)
    with open(OUTPUT_ENRICHED_CSV, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f, delimiter=";")
        writer.writerow(["mot", "theme", "score_v2", "classe", "zipf_books", "ratio",
                         "categories_wikt", "has_lexique", "has_wikt", "raisons"])
        for s in scored_words:
            if s["in_reference"]:
                writer.writerow([
                    s["mot"],
                    reference.get(s["mot"], "?"),
                    round(s["score"], 4),
                    classify(s["score"]),
                    round(s["zipf_books"], 3),
                    round(s["ratio"], 3),
                    "+".join(s["categories_wikt"]),
                    "oui" if s["has_lexique_data"] else "non",
                    "oui" if s["has_wikt_category"] else "non",
                    "|".join(s["reasons"][:2]),
                ])
    print(f"[OK] CSV enrichi : {OUTPUT_ENRICHED_CSV}")
    
    # 10. CSV des nouveaux candidats
    with open(OUTPUT_NEW_WORDS, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f, delimiter=";")
        writer.writerow(["mot", "score_v2", "classe", "zipf_books", "ratio", "categories_wikt"])
        for s in new_excellent + new_good:
            if len(s["mot"]) >= 5 and " " not in s["mot"]:
                writer.writerow([
                    s["mot"],
                    round(s["score"], 4),
                    classify(s["score"]),
                    round(s["zipf_books"], 3),
                    round(s["ratio"], 3),
                    "+".join(s["categories_wikt"]),
                ])
    print(f"[OK] Nouveaux candidats CSV : {OUTPUT_NEW_WORDS}")
    
    # 11. JSON complet
    candidates_v2 = {
        "metadata": {
            "date": time.strftime('%Y-%m-%d %H:%M'),
            "algorithm": "V2",
            "seuil_faible": seuil_faible,
            "seuil_bon": seuil_bon,
            "seuil_excellent": seuil_excellent,
            "pool_size": len(all_words),
            "reference_size": len(reference_set),
            "coverage": f"{covered}/{len(reference_set)}",
        },
        "true_intruders": [{"mot": w, "score": sc, "reasons": r} for w, sc, r in true_intruders],
        "data_gap_words": [{"mot": w, "score": sc, "reasons": r} for w, sc, r in data_gap_words],
        "new_excellent": [{"mot": s["mot"], "score": s["score"], "zipf": s["zipf_books"],
                           "ratio": s["ratio"], "cats": s["categories_wikt"]} for s in new_excellent],
        "new_good": [{"mot": s["mot"], "score": s["score"], "zipf": s["zipf_books"],
                      "ratio": s["ratio"], "cats": s["categories_wikt"]} for s in new_good],
    }
    with open(OUTPUT_CANDIDATES, "w", encoding="utf-8") as f:
        json.dump(candidates_v2, f, ensure_ascii=False, indent=2)
    print(f"[OK] JSON candidats V2 : {OUTPUT_CANDIDATES}")
    
    # 12. Résumé console
    print("\n" + "=" * 65)
    print("RÉSUMÉ V2")
    print("=" * 65)
    print(f"  Pool total analysé    : {len(all_words)} mots")
    print(f"  Mots de référence     : {len(reference_set)}")
    print(f"  Couverture référence  : {covered}/{len(reference_set)} ({100*covered//max(1,len(reference_set))}%)")
    print(f"  Intrus avérés         : {len(true_intruders)}")
    print(f"  Mots lacune données   : {len(data_gap_words)}")
    print(f"  Nouveaux excellents   : {len(new_excellent)}")
    print(f"  Nouveaux bons         : {len(new_good)}")
    print("=" * 65)
    
    if true_intruders:
        print(f"\n[!] {len(true_intruders)} intrus avérés dans notre liste de référence :")
        for w, sc, r in sorted(true_intruders, key=lambda x: x[1])[:15]:
            reasons = ", ".join(r[:2]) if r else "?"
            print(f"   - {w} (score={sc:.3f}) : {reasons}")
    else:
        print("\n[OK] Aucun intrus avéré dans notre liste de référence !")
    
    print(f"\n[+] Top 15 nouveaux candidats excellents :")
    for s in new_excellent[:15]:
        cats = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
        print(f"   + {s['mot']:<22} (score={s['score']:.3f}, Zipf={s['zipf_books']:.2f}, ratio={s['ratio']:.1f}, {cats})")

if __name__ == "__main__":
    main()
