#!/usr/bin/env python3
"""
discover_literary_words_v3.py
==============================
Algorithme V3 - Découverte et filtrage du Vocabulaire Littéraire (Objectif C1).

FONCTIONNEMENT GÉNÉRAL DE L'ALGORITHME :
----------------------------------------
Ce script analyse un pool de mots (mots de référence et mots issus des catégories du Wiktionnaire)
et calcule un score d'intérêt littéraire (V3) pour chaque mot selon trois critères :

1. CRITÈRE A - Fréquence d'usage (Zipf_books dans Lexique383) :
   - Plage cible idéale : Zipf_books ∈ [1.5, 4.3].
   - Les mots trop courants (Zipf > 4.3, comme "chagrin" ou "solitaire") ou trop rares (Zipf < 1.0)
     reçoivent un score réduit pour se concentrer sur la zone optimale d'apprentissage littéraire.
   - Les mots absents de Lexique383 reçoivent une valeur par défaut moyenne s'ils sont dans le Wiktionnaire.

2. CRITÈRE B - Ratio Littéraire vs Oral (Livres / Films) :
   - Bonus modeste (jusqu'à +0.15) si le mot apparaît nettement plus souvent dans les livres que dans
     les dialogues de films (ex: ratio >= 10.0). Le ratio n'est pas pénalisant s'il est faible.

3. CRITÈRE C - Catégorisation Wiktionnaire (Bonus de registre) :
   - Ajoute un bonus significatif si le mot est classé dans l'une des catégories cibles du Wiktionnaire :
     * "Termes soutenus en français" (+0.20)
     * "Termes littéraires en français" (+0.18)
     * "Termes poétiques en français" (+0.14)
     * "Termes archaïques en français" (+0.08)

Filtres de nettoyage (EXCLUSIONS) :
-----------------------------------
- Les mots de moins de 5 lettres, les locutions contenant un espace, et une liste noire éditoriale
  manuelle (mots trop ordinaires, prénoms, ou termes trop technico-scientifiques) sont exclus.

RAPPORTS ET FICHIERS GÉNÉRÉS :
------------------------------
Le script produit plusieurs sorties dans le dossier `tools/` :
- `literary_candidates_v3.json` : Structuration JSON des nouveaux candidats triés par niveau.
- `literary_discovery_v3_report.md` : Rapport d'analyse et de calibration sur la liste de référence.
- `literary_enriched_v3.csv` : Le fichier enrichi complet contenant toutes les colonnes de diagnostic :
  [mot;theme;score_v3;classe;zipf_books;ratio;categories_wikt;has_lexique;has_wikt].
  (C'est ce fichier qui a été épuré pour donner "objectif_1_liste1_mots.csv" contenant uniquement [mot;theme]).
- `literary_new_v3.csv` : CSV simplifié des nouveaux mots validés et classés par niveau.
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

OUTPUT_REPORT = os.path.join(TOOLS_DIR, "literary_discovery_v3_report.md")
OUTPUT_CANDIDATES_JSON = os.path.join(TOOLS_DIR, "literary_candidates_v3.json")
OUTPUT_ENRICHED_CSV = os.path.join(TOOLS_DIR, "literary_enriched_v3.csv")
OUTPUT_NEW_WORDS_CSV = os.path.join(TOOLS_DIR, "literary_new_v3.csv")

WIKT_API_URL = "https://fr.wiktionary.org/w/api.php"
CACHE_DIR = TOOLS_DIR

WIKT_CATEGORIES = [
    "Termes soutenus en français",
    "Termes littéraires en français",
    "Termes poétiques en français",
    "Termes archaïques en français",
]

# Mots à exclure explicitement malgré un bon score technique
# (trop courants/oraux, mots outils, ou hors scope littéraire)
EXPLICIT_EXCLUSIONS = {
    # Verbes très courants parfois classés "soutenu" mais hors scope
    "accouder", "accroupir", "ahurir", "assaisonner", "colporter",
    "fouler", "advenir", "corroder",
    # Adverbes
    "diligemment", "périlleusement",
    # Locutions latines
    "ab ovo", "hic et nunc",
    # Noms propres ou mots trop spécifiques
    "helvétique", "sylvie", "agathe",
    # Mots trop techniques non littéraires
    "acnéique", "balane", "graffito", "socque", "incomestible",
    "discontinuité", "concréter", "déhiscence",
    # Mots ambigus (prénom/commun)
    "diane",
    # Mots trop courts (< 5 lettres)
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
    while len(words) < 2000:
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
    """
    Score V3 - Calibré sur les statistiques réelles des 407 mots de référence.
    
    Plage de Zipf de référence : [1.84, 4.72] avec 94% dans [1.84, 4.0]
    Le ratio est un bonus léger, pas un critère discriminant.
    """
    word_norm = normalize(word)
    in_reference = word_norm in reference
    in_exclusions = word_norm in EXPLICIT_EXCLUSIONS or (len(word) < 5) or (" " in word)
    
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
    
    # ═══════════════════════════════════════════════════════════════
    # CRITÈRE A : Fréquence (Zipf_books)
    # Plage cible : [1.5, 4.3] — basé sur les données réelles de référence
    # 94% des mots de référence ont Zipf ∈ [1.84, 4.0]
    # ═══════════════════════════════════════════════════════════════
    if not has_lex:
        # Pas de données Lexique → score de base si Wiktionnaire, sinon très faible
        freq_score = 0.55 if has_wikt else 0.25
    elif zb >= 1.84 and zb <= 3.5:
        freq_score = 1.0  # zone parfaite
    elif zb > 3.5 and zb <= 4.0:
        freq_score = 0.92  # légèrement commun, toujours bon
    elif zb > 4.0 and zb <= 4.3:
        freq_score = 0.78  # commun mais encore dans la plage de référence (brume, chagrin)
    elif zb > 4.3 and zb <= 4.7:
        freq_score = 0.50  # trop commun — rare dans notre référence (jadis=4.72)
    elif zb > 4.7:
        freq_score = 0.15  # hors plage
    elif zb >= 1.5 and zb < 1.84:
        freq_score = 0.88  # très rare mais dans certains cas valide
    elif zb >= 1.0 and zb < 1.5:
        freq_score = 0.70
    elif zb >= 0.5 and zb < 1.0:
        freq_score = 0.45
    elif zb > 0:
        freq_score = 0.25
    else:
        # zb=0 mais lex=True → données contradictoires
        freq_score = 0.40
    
    # ═══════════════════════════════════════════════════════════════
    # CRITÈRE B : Ratio livres/films (bonus modeste)
    # NOTE : 6% de notre référence a ratio < 1.0
    # → Le ratio ne peut pas être un critère de rejet strict
    # → C'est un bonus, pas un malus sévère
    # ═══════════════════════════════════════════════════════════════
    if not has_lex or (zb == 0 and zf == 0):
        ratio_bonus = 0.0  # pas de données → pas de bonus/malus
    elif ratio >= 10.0:
        ratio_bonus = 0.15   # très littéraire : fort bonus
    elif ratio >= 5.0:
        ratio_bonus = 0.10
    elif ratio >= 2.0:
        ratio_bonus = 0.05
    elif ratio >= 1.0:
        ratio_bonus = 0.0   # neutre
    else:
        ratio_bonus = -0.05  # léger malus si très oral (mais pas éliminatoire)
    
    # ═══════════════════════════════════════════════════════════════
    # CRITÈRE C : Catégorie Wiktionnaire (bonus significatif)
    # 16% de notre référence a une catégorie → c'est un signal fort quand présent
    # ═══════════════════════════════════════════════════════════════
    cat_priority = {"soutenu": 0.20, "littéraire": 0.18, "poétique": 0.14, "archaïque": 0.08}
    if cat_labels:
        wikt_bonus = max(cat_priority.get(c, 0) for c in cat_labels)
        # Bonus multi-catégories
        if len(cat_labels) >= 2:
            wikt_bonus += 0.05
    else:
        wikt_bonus = 0.0
    
    # ═══════════════════════════════════════════════════════════════
    # SCORE FINAL
    # Base = freq_score, ajusté par ratio et wiktionnaire
    # On utilise une formule additive pour plus de transparence
    # ═══════════════════════════════════════════════════════════════
    raw_score = freq_score + ratio_bonus + wikt_bonus
    raw_score = max(0.0, min(1.0, raw_score))
    
    # Malus pour exclusions
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
        reasons.append("exclusion explicite/longueur")
    
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
    print("ALGORITHME V3 - DÉCOUVERTE DE MOTS LITTÉRAIRES FRANÇAIS")
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
    
    print("\n--- Scoring V3 ---")
    scored = []
    for word in all_words:
        lex = lexique.get(word) or lexique.get(strip_accents(word))
        cats = list(word_to_cats.get(word, []))
        result = score_v3(word, lex, cats, reference)
        scored.append(result)
    
    scored.sort(key=lambda x: (-x["score"], x["mot"]))
    
    # ─── Calibration sur les mots de référence ────────────────────
    print("\n--- Calibration ---")
    ref_scored = [s for s in scored if s["in_reference"]]
    
    # Calculer les seuils uniquement sur les mots avec données
    ref_with_data = sorted(
        [s["score"] for s in ref_scored if not s["excluded"] and (s["has_lexique_data"] or s["has_wikt_category"])],
        reverse=True
    )
    n = len(ref_with_data)
    print(f"  Mots de référence avec données : {n}")
    
    # Statistiques de la distribution
    if n > 0:
        avg = sum(ref_with_data) / n
        median = ref_with_data[n // 2]
        p10 = ref_with_data[int(n * 0.90)]   # 10% les plus faibles
        p25 = ref_with_data[int(n * 0.75)]   # 25% les plus faibles
        p75 = ref_with_data[int(n * 0.25)]   # 25% les plus élevés
        print(f"  Score moyen     : {avg:.3f}")
        print(f"  Score médian    : {median:.3f}")
        print(f"  P10 (rej.)      : {p10:.3f}")
        print(f"  P25 (bon)       : {p25:.3f}")
        print(f"  P75 (excellent) : {p75:.3f}")
        
        threshold_reject = round(p10 - 0.02, 3)  # léger tampon
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
    
    # ─── Résultats ────────────────────────────────────────────────
    
    # Intrus dans notre liste de référence
    intruders = [s for s in ref_scored if s["score"] < threshold_reject and not s["excluded"]]
    
    # Séparation intrus vrais vs lacunes données
    true_intruders = [s for s in intruders if s["has_lexique_data"]]
    data_gaps = [s for s in intruders if not s["has_lexique_data"]]
    
    # Couverture
    covered = sum(1 for s in ref_scored if s["score"] >= threshold_good)
    
    # Nouveaux candidats
    new_excellent = [
        s for s in scored
        if not s["in_reference"] and not s["excluded"]
        and s["score"] >= threshold_excellent
        and " " not in s["mot"]
        and len(s["mot"]) >= 5
    ]
    
    new_good = [
        s for s in scored
        if not s["in_reference"] and not s["excluded"]
        and threshold_good <= s["score"] < threshold_excellent
        and " " not in s["mot"]
        and len(s["mot"]) >= 5
    ]
    
    # ─── Rapport ──────────────────────────────────────────────────
    print("\n--- Génération du rapport V3 ---")
    
    with open(OUTPUT_REPORT, "w", encoding="utf-8") as f:
        f.write("# Rapport V3 - Algorithme de Découverte de Mots Littéraires\n\n")
        f.write(f"**Date** : {time.strftime('%Y-%m-%d %H:%M')}\n\n")
        f.write("## Principe de l'Algorithme V3\n\n")
        f.write("Critères (par ordre de priorité) :\n")
        f.write("1. **Zipf_books ∈ [1.5, 4.3]** : fréquence dans les livres (critère principal)\n")
        f.write("2. **Catégorie Wiktionnaire** : bonus de 14–25% si soutenu/littéraire/poétique\n")
        f.write("3. **Ratio livres/films** : bonus modeste (max +15%), pas de rejet strict\n\n")
        f.write("> **Justification** : 94% des mots de référence ont Zipf ∈ [1.84, 4.0],\n")
        f.write("> et 6% ont un ratio livres/films < 1.0 (mentor, codex, brigand...)\n")
        f.write("> — ce qui prouve que le ratio ne peut pas être un critère éliminatoire.\n\n")
        f.write("---\n\n")
        
        # Stats
        ref_wikt_pct = 100 * sum(1 for s in ref_scored if s["has_wikt_category"]) // max(1, len(ref_scored))
        ref_lex_pct = 100 * sum(1 for s in ref_scored if s["has_lexique_data"]) // max(1, len(ref_scored))
        
        f.write("## Statistiques\n\n")
        f.write(f"| Métrique | Valeur |\n|---|---|\n")
        f.write(f"| Mots de référence | {len(reference_set)} |\n")
        f.write(f"| Mots Wiktionnaire | {len(word_to_cats)} |\n")
        f.write(f"| Pool total | {len(all_words)} |\n")
        f.write(f"| % réf. dans Wiktionnaire | {ref_wikt_pct}% |\n")
        f.write(f"| % réf. dans Lexique383 | {ref_lex_pct}% |\n")
        f.write(f"| Couverture (score >= {threshold_good:.2f}) | {covered}/{len(reference_set)} ({100*covered//max(1,len(reference_set))}%) |\n\n")
        
        f.write(f"| Seuil | Score |\n|---|---|\n")
        f.write(f"| Rejet | < {threshold_reject:.3f} |\n")
        f.write(f"| Bon | >= {threshold_good:.3f} |\n")
        f.write(f"| Excellent | >= {threshold_excellent:.3f} |\n\n")
        
        f.write("---\n\n")
        
        # Intrus avérés
        f.write(f"## Intrus Avérés ({len(true_intruders)} mots)\n\n")
        f.write("Ces mots de notre liste ont des données Lexique ET un score < seuil de rejet :\n\n")
        if true_intruders:
            f.write("| Mot | Thème | Score | Zipf | Ratio | Catégories | Raisons |\n")
            f.write("|---|---|---|---|---|---|---|\n")
            for s in sorted(true_intruders, key=lambda x: x["score"]):
                theme = reference.get(s["mot"], "?")
                cats_str = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
                reasons = ", ".join(s["reasons"][:2]) if s["reasons"] else "?"
                f.write(f"| **{s['mot']}** | {theme} | {s['score']:.3f} | "
                        f"{s['zipf_books']:.2f} | {s['ratio']:.2f} | {cats_str} | {reasons} |\n")
        else:
            f.write("✅ Aucun intrus avéré !\n")
        f.write("\n---\n\n")
        
        # Lacunes de données
        if data_gaps:
            f.write(f"## Mots de Référence sans Données Suffisantes ({len(data_gaps)} mots)\n\n")
            f.write("Ces mots de notre liste ont un mauvais score à cause d'un manque de données.\n")
            f.write("Ils sont probablement légitimes (néologismes, termes spécialisés, etc.)\n\n")
            f.write("| Mot | Thème | Score | Raisons |\n|---|---|---|---|\n")
            for s in sorted(data_gaps, key=lambda x: x["score"]):
                theme = reference.get(s["mot"], "?")
                reasons = ", ".join(s["reasons"]) if s["reasons"] else "?"
                f.write(f"| {s['mot']} | {theme} | {s['score']:.3f} | {reasons} |\n")
            f.write("\n---\n\n")
        
        # Mots de référence avec mauvais score mais données disponibles (zone grise)
        borderline = [s for s in ref_scored 
                      if threshold_reject <= s["score"] < threshold_good 
                      and s["has_lexique_data"] and not s["excluded"]]
        if borderline:
            f.write(f"## Mots de Référence en Zone Grise (score moyen, {len(borderline)} mots)\n\n")
            f.write(f"Score entre {threshold_reject:.3f} et {threshold_good:.3f} :\n\n")
            f.write("| Mot | Thème | Score | Zipf | Ratio | Catégories |\n|---|---|---|---|---|---|\n")
            for s in sorted(borderline, key=lambda x: x["score"]):
                theme = reference.get(s["mot"], "?")
                cats_str = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
                f.write(f"| {s['mot']} | {theme} | {s['score']:.3f} | "
                        f"{s['zipf_books']:.2f} | {s['ratio']:.2f} | {cats_str} |\n")
            f.write("\n---\n\n")
        
        # Nouveaux candidats excellents
        f.write(f"## Nouveaux Candidats Excellents ({len(new_excellent)} mots)\n\n")
        f.write(f"Score >= {threshold_excellent:.3f}, non dans notre liste :\n\n")
        f.write("| Mot | Score | Zipf | Ratio | Catégories |\n|---|---|---|---|---|\n")
        for s in new_excellent[:80]:
            cats_str = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
            f.write(f"| **{s['mot']}** | {s['score']:.3f} | {s['zipf_books']:.2f} | "
                    f"{s['ratio']:.2f} | {cats_str} |\n")
        f.write("\n---\n\n")
        
        # Nouveaux candidats bons
        f.write(f"## Nouveaux Candidats Bons ({len(new_good)} mots)\n\n")
        f.write(f"Score entre {threshold_good:.3f} et {threshold_excellent:.3f} :\n\n")
        f.write("| Mot | Score | Zipf | Ratio | Catégories |\n|---|---|---|---|---|\n")
        for s in new_good[:60]:
            cats_str = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
            f.write(f"| {s['mot']} | {s['score']:.3f} | {s['zipf_books']:.2f} | "
                    f"{s['ratio']:.2f} | {cats_str} |\n")
        f.write("\n---\n\n")
        
        # Top 80 tous mots confondus
        f.write("## Top 80 - Tous Mots Confondus\n\n")
        f.write("| Rang | Mot | Score | Zipf | Ratio | Catégories | Dans Réf. |\n")
        f.write("|---|---|---|---|---|---|---|\n")
        filtered_top = [s for s in scored if not s["excluded"]]
        for i, s in enumerate(filtered_top[:80]):
            cats_str = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
            ref_str = "✓" if s["in_reference"] else "—"
            f.write(f"| {i+1} | **{s['mot']}** | {s['score']:.3f} | {s['zipf_books']:.2f} | "
                    f"{s['ratio']:.2f} | {cats_str} | {ref_str} |\n")
        
        f.write("\n---\n\n")
        f.write("## Bilan et Recommandations\n\n")
        f.write(f"- **Couverture** : {covered}/{len(reference_set)} mots de référence ({100*covered//max(1,len(reference_set))}%) correctement identifiés\n")
        f.write(f"- **Intrus avérés** : {len(true_intruders)} mots à reconsidérer dans notre liste\n")
        f.write(f"- **Lacunes de données** : {len(data_gaps)} mots légitimes non couverts par Lexique/Wiktionnaire\n")
        f.write(f"- **Nouveaux excellents** : {len(new_excellent)} candidats à ajouter\n")
        f.write(f"- **Nouveaux bons** : {len(new_good)} candidats supplémentaires\n\n")
        f.write("### Recommandation de filtrage pour la recherche de nouveaux mots :\n\n")
        f.write("```\n")
        f.write(f"INCLUDE si :\n")
        f.write(f"  - Zipf_books ∈ [1.5, 4.3]  # plage de fréquence littéraire\n")
        f.write(f"  - ET au moins une de ces conditions :\n")
        f.write(f"    a) Catégorie Wiktionnaire : soutenu/littéraire/poétique/archaïque\n")
        f.write(f"    b) Ratio livres/films >= 2.0  # clairement plus écrit qu'oral\n")
        f.write(f"    c) Score V3 >= {threshold_good:.2f}\n")
        f.write(f"EXCLUDE si :\n")
        f.write(f"  - Zipf_books > 4.3  # trop commun\n")
        f.write(f"  - OU longueur < 5 caractères\n")
        f.write(f"  - OU locution (espace dans le mot)\n")
        f.write(f"  - OU dans la liste d'exclusions explicites\n")
        f.write("```\n")
    
    print(f"[OK] Rapport V3 : {OUTPUT_REPORT}")
    
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
    print(f"[OK] CSV enrichi : {OUTPUT_ENRICHED_CSV}")
    
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
    print(f"[OK] Nouveaux mots CSV : {OUTPUT_NEW_WORDS_CSV}")
    
    # JSON
    output_json = {
        "metadata": {
            "date": time.strftime('%Y-%m-%d %H:%M'),
            "algorithm": "V3",
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
            for s in new_excellent[:200]
        ],
        "new_good": [
            {"mot": s["mot"], "score": s["score"], "zipf": s["zipf_books"],
             "ratio": s["ratio"], "cats": s["categories_wikt"]}
            for s in new_good[:200]
        ],
    }
    with open(OUTPUT_CANDIDATES_JSON, "w", encoding="utf-8") as f:
        json.dump(output_json, f, ensure_ascii=False, indent=2)
    print(f"[OK] JSON V3 : {OUTPUT_CANDIDATES_JSON}")
    
    # Résumé
    print("\n" + "=" * 65)
    print("RÉSUMÉ V3")
    print("=" * 65)
    print(f"  Pool analysé        : {len(all_words)}")
    print(f"  Couverture réf.     : {covered}/{len(reference_set)} ({100*covered//max(1,len(reference_set))}%)")
    print(f"  Intrus avérés       : {len(true_intruders)}")
    print(f"  Lacunes de données  : {len(data_gaps)}")
    print(f"  Nouveaux excellents : {len(new_excellent)}")
    print(f"  Nouveaux bons       : {len(new_good)}")
    print("=" * 65)
    
    if true_intruders:
        print(f"\n[!] Intrus avérés ({len(true_intruders)}) :")
        for s in sorted(true_intruders, key=lambda x: x["score"]):
            theme = reference.get(s["mot"], "?")
            print(f"   - {s['mot']:<22} ({theme:<25}) score={s['score']:.3f}, Zipf={s['zipf_books']:.2f}, ratio={s['ratio']:.2f}")
    else:
        print("\n[OK] Aucun intrus avéré dans notre liste de référence !")
    
    print(f"\n[+] Top 20 nouveaux candidats excellents :")
    for s in new_excellent[:20]:
        cats = "+".join(s["categories_wikt"])
        print(f"   + {s['mot']:<22} score={s['score']:.3f}, Zipf={s['zipf_books']:.2f}, ratio={s['ratio']:.1f}, [{cats}]")

if __name__ == "__main__":
    main()
