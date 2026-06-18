#!/usr/bin/env python3
"""
discover_literary_words_max.py
==============================
Algorithme V3 avec suppression de la restriction de longueur minimale.
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

OUTPUT_REPORT = os.path.join(TOOLS_DIR, "literary_discovery_max_report.md")
OUTPUT_CANDIDATES_JSON = os.path.join(TOOLS_DIR, "literary_candidates_max.json")
OUTPUT_ENRICHED_CSV = os.path.join(TOOLS_DIR, "literary_enriched_max.csv")
OUTPUT_NEW_WORDS_CSV = os.path.join(TOOLS_DIR, "literary_new_max.csv")

WIKT_API_URL = "https://fr.wiktionary.org/w/api.php"
CACHE_DIR = TOOLS_DIR

WIKT_CATEGORIES = [
    "Termes soutenus en français",
    "Termes littéraires en français",
    "Termes poétiques en français",
    "Termes archaïques en français",
]

# Mots à exclure explicitement
EXPLICIT_EXCLUSIONS = {
    "accouder", "accroupir", "ahurir", "assaisonner", "colporter",
    "fouler", "advenir", "corroder",
    "diligemment", "périlleusement",
    "ab ovo", "hic et nunc",
    "helvétique", "sylvie", "agathe",
    "acnéique", "balane", "graffito", "socque", "incomestible",
    "discontinuité", "concréter", "déhiscence",
    "diane",
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

# ─── CHARGEMENTS ──────────────────────────────────────────────────────────────

def load_reference_list() -> dict:
    reference = {}
    with open(REFERENCE_CSV, "r", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f, delimiter=";")
        for row in reader:
            mot = normalize(row.get("mot", ""))
            theme = row.get("theme", "").strip()
            if mot:
                reference[mot] = theme
    print(f"[OK] Référence : {len(reference)} mots")
    return reference

def load_lexique383() -> dict:
    print("Chargement Lexique383...")
    lexique = {}
    try:
        with zipfile.ZipFile(LEXIQUE_ZIP, "r") as zf:
            tsv_files = [n for n in zf.namelist() if n.endswith(".tsv")]
            target = tsv_files[0] if tsv_files else None
            if not target:
                return {}
            with zf.open(target) as f:
                content = f.read().decode("utf-8", errors="replace")
                for row in csv.DictReader(io.StringIO(content), delimiter="\t"):
                    lemme = normalize(row.get("lemme", row.get("ortho", "")))
                    if not lemme:
                        continue
                    try:
                        fb = float(row.get("freqlivres", 0) or 0)
                        ff = float(row.get("freqfilms2", 0) or 0)
                    except:
                        fb = ff = 0.0
                    zb = math.log10(fb) + 3 if fb > 0 else 0.0
                    zf_val = math.log10(ff) + 3 if ff > 0 else 0.0
                    ratio = (fb / ff) if ff > 0 else (10.0 if fb > 0 else 0.0)
                    if lemme not in lexique or fb > lexique[lemme]["fb"]:
                        lexique[lemme] = {
                            "zb": round(zb, 3),
                            "zf": round(zf_val, 3),
                            "ratio": round(min(ratio, 50.0), 3),
                            "fb": fb,
                        }
    except Exception as e:
        print(f"[!] Erreur Lexique : {e}")
    print(f"[OK] Lexique383 : {len(lexique)} lemmes")
    return lexique

def fetch_wiktionary_category(category: str) -> list:
    cached = load_cache(category)
    if cached is not None:
        words = cached.get("words", [])
        print(f"  [cache] {category} : {len(words)} mots")
        return words
    
    print(f"  Wiktionnaire : {category}...")
    words = []
    cmcontinue = None
    while len(words) < 5000:  # Increased limit to get more words if possible
        params = {
            "action": "query", "list": "categorymembers",
            "cmtitle": f"Catégorie:{category}",
            "cmlimit": "500", "cmnamespace": "0", "format": "json",
        }
        if cmcontinue:
            params["cmcontinue"] = cmcontinue
        url = WIKT_API_URL + "?" + urllib.parse.urlencode(params)
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "LexicaAndroid/3.0"})
            with urllib.request.urlopen(req, timeout=15) as resp:
                data = json.loads(resp.read().decode("utf-8"))
        except Exception as e:
            print(f"  [!] Erreur : {e}")
            break
        for m in data.get("query", {}).get("categorymembers", []):
            title = m.get("title", "")
            if title and not title.startswith(("Wiktionnaire:", "Modèle:", "Annexe:")):
                fc = title[0] if title else ""
                if fc == fc.lower() or fc in "éèêëàâîïôùûüÿæœ":
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

# ─── SCORING V3 ───────────────────────────────────────────────────────────────

def score_v3(word: str, lex: dict | None, wikt_cats: list[str], reference: dict) -> dict:
    word_norm = normalize(word)
    in_reference = word_norm in reference
    in_exclusions = word_norm in EXPLICIT_EXCLUSIONS or (" " in word)
    
    zb = lex["zb"] if lex else 0.0
    zf = lex["zf"] if lex else 0.0
    ratio = lex["ratio"] if lex else 0.0
    has_lex = lex is not None and (zb > 0 or zf > 0)
    
    # Catégories Wiktionnaire
    cat_labels = []
    for cat in wikt_cats:
        cl = cat.lower()
        if "soutenus" in cl: cat_labels.append("soutenu")
        elif "littéraires" in cl: cat_labels.append("littéraire")
        elif "poétiques" in cl: cat_labels.append("poétique")
        elif "archaïques" in cl: cat_labels.append("archaïque")
    cat_labels = list(set(cat_labels))
    has_wikt = len(cat_labels) > 0
    
    # CRITÈRE A : Fréquence (Zipf_books)
    if not has_lex:
        freq_score = 0.55 if has_wikt else 0.25
    elif zb >= 1.84 and zb <= 3.5:
        freq_score = 1.0
    elif zb > 3.5 and zb <= 4.0:
        freq_score = 0.92
    elif zb > 4.0 and zb <= 4.3:
        freq_score = 0.78
    elif zb > 4.3 and zb <= 4.7:
        freq_score = 0.50
    elif zb > 4.7:
        freq_score = 0.15
    elif zb >= 1.5 and zb < 1.84:
        freq_score = 0.88
    elif zb >= 1.0 and zb < 1.5:
        freq_score = 0.70
    elif zb >= 0.5 and zb < 1.0:
        freq_score = 0.45
    elif zb > 0:
        freq_score = 0.25
    else:
        freq_score = 0.40
    
    # CRITÈRE B : Ratio livres/films
    if not has_lex or (zb == 0 and zf == 0):
        ratio_bonus = 0.0
    elif ratio >= 10.0:
        ratio_bonus = 0.15
    elif ratio >= 5.0:
        ratio_bonus = 0.10
    elif ratio >= 2.0:
        ratio_bonus = 0.05
    elif ratio >= 1.0:
        ratio_bonus = 0.0
    else:
        ratio_bonus = -0.05
    
    # CRITÈRE C : Catégorie Wiktionnaire
    cat_priority = {"soutenu": 0.20, "littéraire": 0.18, "poétique": 0.14, "archaïque": 0.08}
    if cat_labels:
        wikt_bonus = max(cat_priority.get(c, 0) for c in cat_labels)
        if len(cat_labels) >= 2:
            wikt_bonus += 0.05
    else:
        wikt_bonus = 0.0
    
    raw_score = freq_score + ratio_bonus + wikt_bonus
    raw_score = max(0.0, min(1.0, raw_score))
    
    if in_exclusions:
        raw_score = 0.0
    
    final_score = round(raw_score, 4)
    
    # Diagnostic
    reasons = []
    if zb > 4.3:
        reasons.append(f"trop commun (Zipf={zb:.2f})")
    if ratio < 1.0 and has_lex and zb > 0:
        reasons.append(f"ratio oral={ratio:.2f}")
    if not has_lex:
        reasons.append("absent Lexique383")
    if not has_wikt:
        reasons.append("absent Wiktionnaire")
    if in_exclusions:
        reasons.append("exclusion explicite")
    
    return {
        "mot": word,
        "score": final_score,
        "freq_score": round(freq_score, 3),
        "ratio_bonus": round(ratio_bonus, 3),
        "wikt_bonus": round(wikt_bonus, 3),
        "zipf_books": round(zb, 3),
        "zipf_films": round(zf, 3),
        "ratio": round(ratio, 3),
        "categories_wikt": cat_labels,
        "in_reference": in_reference,
        "has_lexique_data": has_lex,
        "has_wikt_category": has_wikt,
        "excluded": in_exclusions,
        "reasons": reasons,
    }

# ─── ALGORITHME PRINCIPAL ─────────────────────────────────────────────────────

def main():
    print("=" * 65)
    print("ALGORITHME MAX - DÉCOUVERTE DE MOTS LITTÉRAIRES (SANS SEUIL LONGUEUR)")
    print("=" * 65)
    
    reference = load_reference_list()
    reference_set = set(reference.keys())
    lexique = load_lexique383()
    
    print("\n--- Catégories Wiktionnaire ---")
    word_to_cats = defaultdict(list)
    for cat in WIKT_CATEGORIES:
        for w in fetch_wiktionary_category(cat):
            wn = normalize(w)
            if cat not in word_to_cats[wn]:
                word_to_cats[wn].append(cat)
    print(f"\n[OK] Wiktionnaire : {len(word_to_cats)} mots uniques")
    
    all_words = set(word_to_cats.keys()) | reference_set
    print(f"[OK] Pool total : {len(all_words)} mots")
    
    print("\n--- Scoring Max ---")
    scored = []
    for word in all_words:
        lex = lexique.get(word) or lexique.get(strip_accents(word))
        cats = list(word_to_cats.get(word, []))
        result = score_v3(word, lex, cats, reference)
        scored.append(result)
    
    scored.sort(key=lambda x: (-x["score"], x["mot"]))
    
    # Calibration sur les mots de référence
    print("\n--- Calibration ---")
    ref_scored = [s for s in scored if s["in_reference"]]
    ref_with_data = sorted(
        [s["score"] for s in ref_scored if not s["excluded"] and (s["has_lexique_data"] or s["has_wikt_category"])],
        reverse=True
    )
    n = len(ref_with_data)
    
    if n > 0:
        p10 = ref_with_data[int(n * 0.90)]
        p25 = ref_with_data[int(n * 0.75)]
        p75 = ref_with_data[int(n * 0.25)]
        threshold_reject = round(p10 - 0.02, 3)
        threshold_good = round(p25, 3)
        threshold_excellent = round(p75, 3)
    else:
        threshold_reject, threshold_good, threshold_excellent = 0.40, 0.60, 0.80
    
    print(f"  Seuil rejet     : < {threshold_reject:.3f}")
    print(f"  Seuil bon       : >= {threshold_good:.3f}")
    print(f"  Seuil excellent : >= {threshold_excellent:.3f}")
    
    def classify(s):
        if s["excluded"]:
            return "Exclu"
        sc = s["score"]
        if sc >= threshold_excellent:
            return "Excellent"
        elif sc >= threshold_good:
            return "Bon"
        elif sc >= threshold_reject:
            return "Moyen"
        else:
            return "Faible"
    
    intruders = [s for s in ref_scored if s["score"] < threshold_reject and not s["excluded"]]
    true_intruders = [s for s in intruders if s["has_lexique_data"]]
    data_gaps = [s for s in intruders if not s["has_lexique_data"]]
    covered = sum(1 for s in ref_scored if s["score"] >= threshold_good)
    
    new_excellent = [
        s for s in scored
        if not s["in_reference"] and not s["excluded"]
        and s["score"] >= threshold_excellent
        and " " not in s["mot"]
    ]
    
    new_good = [
        s for s in scored
        if not s["in_reference"] and not s["excluded"]
        and threshold_good <= s["score"] < threshold_excellent
        and " " not in s["mot"]
    ]
    
    print("\n--- Génération du rapport Max ---")
    
    with open(OUTPUT_REPORT, "w", encoding="utf-8") as f:
        f.write("# Rapport Max - Algorithme de Découverte (Sans Seuil Longueur)\n\n")
        f.write(f"| Seuil | Score |\n|---|---|\n")
        f.write(f"| Rejet | < {threshold_reject:.3f} |\n")
        f.write(f"| Bon | >= {threshold_good:.3f} |\n")
        f.write(f"| Excellent | >= {threshold_excellent:.3f} |\n\n")
        f.write(f"## Nouveaux Candidats Excellents ({len(new_excellent)} mots)\n\n")
        f.write("| Mot | Score | Zipf | Ratio | Catégories |\n|---|---|---|---|---|\n")
        for s in new_excellent[:250]:
            cats_str = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
            f.write(f"| **{s['mot']}** | {s['score']:.3f} | {s['zipf_books']:.2f} | {s['ratio']:.2f} | {cats_str} |\n")
            
    # CSV enrichi
    with open(OUTPUT_ENRICHED_CSV, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f, delimiter=";")
        writer.writerow(["mot", "theme", "score_v3", "classe", "zipf_books", "ratio",
                         "categories_wikt", "has_lexique", "has_wikt"])
        for s in scored:
            if s["in_reference"]:
                writer.writerow([
                    s["mot"], reference.get(s["mot"], "?"),
                    round(s["score"], 4), classify(s),
                    round(s["zipf_books"], 3), round(s["ratio"], 3),
                    "+".join(s["categories_wikt"]),
                    "oui" if s["has_lexique_data"] else "non",
                    "oui" if s["has_wikt_category"] else "non",
                ])
                
    # CSV nouveaux mots
    with open(OUTPUT_NEW_WORDS_CSV, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f, delimiter=";")
        writer.writerow(["mot", "score_v3", "classe", "zipf_books", "ratio", "categories_wikt"])
        for s in new_excellent + new_good:
            writer.writerow([
                s["mot"], round(s["score"], 4), classify(s),
                round(s["zipf_books"], 3), round(s["ratio"], 3),
                "+".join(s["categories_wikt"]),
            ])
            
    # JSON
    output_json = {
        "metadata": {
            "date": time.strftime('%Y-%m-%d %H:%M'),
            "algorithm": "Max",
            "threshold_reject": threshold_reject,
            "threshold_good": threshold_good,
            "threshold_excellent": threshold_excellent,
            "coverage": f"{covered}/{len(reference_set)}",
        },
        "true_intruders": [
            {"mot": s["mot"], "score": s["score"], "theme": reference.get(s["mot"], "?"),
             "zipf": s["zipf_books"], "ratio": s["ratio"], "reasons": s["reasons"]}
            for s in true_intruders
        ],
        "data_gaps": [
            {"mot": s["mot"], "score": s["score"], "theme": reference.get(s["mot"], "?"),
             "reasons": s["reasons"]}
            for s in data_gaps
        ],
        "new_excellent": [
            {"mot": s["mot"], "score": s["score"], "zipf": s["zipf_books"],
             "ratio": s["ratio"], "cats": s["categories_wikt"]}
            for s in new_excellent
        ],
        "new_good": [
            {"mot": s["mot"], "score": s["score"], "zipf": s["zipf_books"],
             "ratio": s["ratio"], "cats": s["categories_wikt"]}
            for s in new_good
        ],
    }
    with open(OUTPUT_CANDIDATES_JSON, "w", encoding="utf-8") as f:
        json.dump(output_json, f, ensure_ascii=False, indent=2)
        
    print(f"Bilan : {len(new_excellent)} excellents, {len(new_good)} bons")

if __name__ == "__main__":
    main()
