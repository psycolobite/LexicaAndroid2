#!/usr/bin/env python3
"""
consolidate_final_wordlist_max.py
================================
Consolidation finale sans limite de longueur minimale.
"""

import json
import csv
import os
from collections import defaultdict

TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))

V3_JSON = os.path.join(TOOLS_DIR, "literary_candidates_max.json")
EMBED_JSON = os.path.join(TOOLS_DIR, "literary_final_combined_max.json")

OUTPUT_REPORT = os.path.join(TOOLS_DIR, "literary_final_report_max.md")
OUTPUT_NEW_CANDIDATES = os.path.join(TOOLS_DIR, "literary_validated_candidates_max.csv")

THRESHOLD_V3 = 0.70
THRESHOLD_V3_HIGH = 0.90
THRESHOLD_SEM = 0.55
THRESHOLD_SEM_LOOSE = 0.50

FINAL_EXCLUSIONS = {
    "confondre", "décéder", "décédé", "désolé", "accouder", "accroupir",
    "ahurir", "assaisonner", "colporter", "fouler", "advenir", "corroder",
    "habiter", "partir", "rester", "tomber", "lever", "porter",
    "diligemment", "périlleusement", "nonobstant",
    "ab ovo", "hic et nunc",
    "helvétique", "nippon", "diane",
    "acnéique", "balane", "graffito", "socque", "incomestible",
    "discontinuité", "concréter", "déhiscence", "pagure", "anatidé",
    "gouffre", "guerrier", "dindon", "homicide", "milice",
    "nourrisson", "muraille", "commune", "faction", "favori",
    "prospère", "circonstanciel", "modérateur", "physionomiste",
    "arrière-neveux", "couronner", "altitude", "annales", "alentour",
    "aliénation", "amante", "amphitryon", "aboutissant", "accidenter",
    "sylvie", "agathe", "avide", "artificiel", "artifice",
}

def load_reference() -> dict:
    ref = {}
    REFERENCE_CSV = os.path.join(TOOLS_DIR, "consolidated_literary_words.csv")
    with open(REFERENCE_CSV, "r", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f, delimiter=";"):
            mot = row["mot"].strip().lower()
            if mot:
                ref[mot] = row["theme"].strip()
    return ref

def load_v3() -> dict:
    with open(V3_JSON, "r", encoding="utf-8") as f:
        return json.load(f)

def load_embeddings() -> dict:
    with open(EMBED_JSON, "r", encoding="utf-8") as f:
        return json.load(f)

def main():
    print("=" * 65)
    print("CONSOLIDATION MAX (SANS SEUIL LONGUEUR)")
    print("=" * 65)
    
    reference = load_reference()
    ref_set = set(reference.keys())
    v3_data = load_v3()
    embed_data = load_embeddings()
    
    v3_by_word = {}
    for item in v3_data.get("new_excellent", []):
        v3_by_word[item["mot"]] = {
            "score_v3": item.get("score", 0),
            "zipf": item.get("zipf", 0),
            "ratio": item.get("ratio", 0),
            "cats": item.get("cats", []),
        }
    for item in v3_data.get("new_good", []):
        v3_by_word.setdefault(item["mot"], {
            "score_v3": item.get("score", 0),
            "zipf": item.get("zipf", 0),
            "ratio": item.get("ratio", 0),
            "cats": item.get("cats", []),
        })
        
    embed_by_word = {}
    for item in embed_data.get("new_excellent_candidates", []):
        embed_by_word[item["mot"]] = {
            "score_embed": item.get("score_final", 0),
            "score_sem": item.get("score_semantique", 0),
            "theme_similar": item.get("theme_similar", "?"),
        }
        
    all_candidates = set(v3_by_word.keys()) | set(embed_by_word.keys())
    validated = []
    
    for mot in all_candidates:
        if mot in FINAL_EXCLUSIONS or mot in ref_set or " " in mot:
            continue
            
        v3_info = v3_by_word.get(mot, {})
        em_info = embed_by_word.get(mot, {})
        
        score_v3 = v3_info.get("score_v3", 0)
        zipf = v3_info.get("zipf", 0)
        ratio = v3_info.get("ratio", 0)
        cats = v3_info.get("cats", [])
        
        score_sem = em_info.get("score_sem", 0)
        theme_similar = em_info.get("theme_similar", "?")
        
        if zipf > 4.3:
            continue
            
        is_valid = (
            (score_v3 >= THRESHOLD_V3 and score_sem >= THRESHOLD_SEM) or
            score_v3 >= THRESHOLD_V3_HIGH
        ) and score_sem >= THRESHOLD_SEM_LOOSE
        
        if is_valid:
            validated.append({
                "mot": mot,
                "score_v3": round(score_v3, 4),
                "score_sem": round(score_sem, 4),
                "zipf_books": round(zipf, 3),
                "ratio": round(ratio, 3),
                "cats": cats if isinstance(cats, list) else cats.split("+") if cats else [],
                "theme_similar": theme_similar,
            })
            
    validated.sort(key=lambda x: (-x["score_v3"], -x["score_sem"], x["mot"]))
    
    with open(OUTPUT_REPORT, "w", encoding="utf-8") as f:
        f.write("# Rapport Final Max - Mots Littéraires Validés (Sans Limite de Longueur)\n\n")
        f.write(f"## Candidats validés ({len(validated)} mots)\n\n")
        f.write("| Mot | Score V3 | Sim. Sém. | Zipf | Ratio | Catégories | Thème |\n|---|---|---|---|---|---|---|\n")
        for c in validated:
            cats_str = "+".join(c["cats"]) if c["cats"] else "—"
            f.write(f"| **{c['mot']}** | {c['score_v3']:.3f} | {c['score_sem']:.3f} | "
                    f"{c['zipf_books']:.2f} | {c['ratio']:.2f} | {cats_str} | {c['theme_similar']} |\n")
            
    with open(OUTPUT_NEW_CANDIDATES, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f, delimiter=";")
        writer.writerow(["mot", "score_v3", "score_semantique", "zipf_books", "ratio",
                         "categories_wikt", "theme_similaire"])
        for c in validated:
            writer.writerow([
                c["mot"], c["score_v3"], c["score_sem"],
                c["zipf_books"], c["ratio"],
                "+".join(c["cats"]),
                c["theme_similar"],
            ])
            
    print(f"[OK] Consolidation Max terminée : {len(validated)} candidats validés.")

if __name__ == "__main__":
    main()
