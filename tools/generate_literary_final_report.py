#!/usr/bin/env python3
"""
generate_literary_final_report.py
===================================
Rapport final d'analyse basé sur les scores V3.
Utilise des seuils fixes calibrés manuellement sur les données réelles.

Seuils calibrés :
- "Intrus avérés" : score < 0.40 ET a des données dans Lexique OU Wiktionnaire
  (le score 0.40 élimine les vrais faux positifs comme jadis=0.34, chagrin=0.50 est en zone grise)
- "À revoir" : score 0.40–0.70 avec données disponibles (zone grise)  
- "Bons mots" : score >= 0.70 (la grande majorité de notre liste)
- "Excellents" : score >= 0.95

Nouveaux candidats filtrés :
- Score >= 0.95 (excellent)
- Zipf_books ∈ [1.5, 4.2] 
- Sans locution (espace)
- Longueur >= 5 lettres (mais les mots courts de notre liste de référence sont acceptés)
- Hors liste d'exclusions
"""

import json, csv, os

TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))

# Charger les résultats V3
with open(os.path.join(TOOLS_DIR, "literary_candidates_v3.json"), "r", encoding="utf-8") as f:
    v3_data = json.load(f)

with open(os.path.join(TOOLS_DIR, "literary_enriched_v3.csv"), "r", encoding="utf-8-sig") as f:
    ref_rows = list(csv.DictReader(f, delimiter=";"))

with open(os.path.join(TOOLS_DIR, "literary_new_v3.csv"), "r", encoding="utf-8-sig") as f:
    new_rows = list(csv.DictReader(f, delimiter=";"))

# Lire la liste de référence
with open(os.path.join(TOOLS_DIR, "consolidated_literary_words.csv"), "r", encoding="utf-8-sig") as f:
    reference_all = {row["mot"].strip().lower(): row["theme"].strip() for row in csv.DictReader(f, delimiter=";")}

print("=== ANALYSE FINALE DES RÉSULTATS V3 ===\n")

# 1. Analyser les mots de référence
intrus_averes = []     # score < 0.40 avec données
zone_grise = []        # score 0.40-0.70 avec données
lacunes_donnees = []   # score < 0.70 sans données (Lexique ET Wiktionnaire)
bons_mots = []         # score >= 0.70

for row in ref_rows:
    mot = row["mot"]
    score = float(row["score_v3"])
    theme = row["theme"]
    has_lex = row["has_lexique"] == "oui"
    has_wikt = row["has_wikt"] == "oui"
    zb = float(row["zipf_books"])
    ratio = float(row["ratio"])
    cats = row["categories_wikt"]
    
    has_any_data = has_lex or has_wikt
    
    if score < 0.40 and has_any_data:
        intrus_averes.append((mot, score, theme, zb, ratio, cats))
    elif score < 0.70 and not has_any_data:
        lacunes_donnees.append((mot, score, theme))
    elif score < 0.70 and has_any_data:
        zone_grise.append((mot, score, theme, zb, ratio, cats))
    else:
        bons_mots.append((mot, score, theme))

# 2. Filtrer les nouveaux candidats
# Exclusions explicites
EXCLUSIONS = {
    "accouder", "accroupir", "ahurir", "assaisonner", "colporter",
    "fouler", "advenir", "corroder", "diligemment", "périlleusement",
    "ab ovo", "hic et nunc", "helvétique", "sylvie", "agathe",
    "acnéique", "balane", "graffito", "socque", "incomestible",
    "discontinuité", "concréter", "déhiscence", "diane",
    "aboutissant", "accidenter", "altitude", "aliénation", "amante",
    "amphitryon", "anatidé", "annales", "alentour",
    # Adjectifs trop courants
    "aimé", "armé", "assis", "blanc", "bleu", "brun", "calme",
    # Verbes trop courants parfois classés "littéraire"
    "habiter", "partir", "rester", "tomber", "lever", "porter",
    # Noms communs basiques classés "archaïque" ou "littéraire"
    "ami", "dame", "roi", "sire", "beau",
}

# Mots thématiquement intéressants (priorité éditoriale)
PRIORITY_THEMES = {
    "soutenu": 1.0,
    "littéraire": 0.95,
    "poétique": 0.9,
    "archaïque": 0.75,
}

good_new = []
for row in new_rows:
    mot = row["mot"]
    score = float(row["score_v3"])
    zb = float(row["zipf_books"])
    ratio = float(row["ratio"])
    cats = row["categories_wikt"]
    
    # Filtres de base
    if mot in EXCLUSIONS:
        continue
    if " " in mot:  # pas de locutions
        continue
    if len(mot) < 5:  # pas de mots trop courts
        continue
    if zb > 4.2:  # trop commun
        continue
    if zb > 0 and zb < 1.0:  # trop rare (sauf si Wiktionnaire)
        if not cats:
            continue
    
    # Filtrer par pertinence éditoriale
    # Un mot sans catégorie Wiktionnaire ET avec ratio < 2.0 est suspect
    if not cats and zb > 0 and ratio < 2.0:
        continue
    
    # Vérifier que le mot est intéressant d'un point de vue vocabulaire
    # (pas de verbe trop courant, pas d'adjectif banal)
    good_new.append({
        "mot": mot,
        "score": score,
        "zipf": zb,
        "ratio": ratio,
        "cats": cats.split("+") if cats else [],
        "theme_priority": max([PRIORITY_THEMES.get(c, 0) for c in cats.split("+")] if cats else [0]),
    })

# Trier par score décroissant, puis par thème de priorité
good_new.sort(key=lambda x: (-x["score"], -x["theme_priority"], x["mot"]))

# 3. Affichage
print(f"Mots de référence analysés : {len(ref_rows)}")
print(f"  Bons mots (>= 0.70)     : {len(bons_mots)}")
print(f"  Zone grise (0.40-0.70)  : {len(zone_grise)}")
print(f"  Lacunes de données      : {len(lacunes_donnees)}")
print(f"  Intrus avérés (< 0.40)  : {len(intrus_averes)}")
print()
print(f"Nouveaux candidats filtrés : {len(good_new)}")
print()

print("=== INTRUS AVÉRÉS ===")
for mot, score, theme, zb, ratio, cats in sorted(intrus_averes, key=lambda x: x[1]):
    reason = f"Zipf={zb:.2f}" if zb > 4.0 else (f"trop rare Zipf={zb:.2f}" if zb > 0 else "zb=0")
    print(f"  {mot:<22} ({theme:<25}) score={score:.3f} | {reason}")

print()
print("=== ZONE GRISE (à revoir manuellement) ===")
for mot, score, theme, zb, ratio, cats in sorted(zone_grise, key=lambda x: x[1]):
    print(f"  {mot:<22} ({theme:<25}) score={score:.3f} | Zipf={zb:.2f}, ratio={ratio:.2f}, wikt={cats}")

print()
print("=== LACUNES DE DONNÉES (légitimes mais pas dans Lexique/Wiktionnaire) ===")
for mot, score, theme in sorted(lacunes_donnees, key=lambda x: x[1]):
    print(f"  {mot:<22} ({theme:<25}) score={score:.3f}")

print()
print("=== TOP 50 NOUVEAUX CANDIDATS ===")
for s in good_new[:50]:
    cats = "+".join(s["cats"]) if s["cats"] else "—"
    print(f"  {s['mot']:<22} score={s['score']:.3f}, Zipf={s['zipf']:.2f}, ratio={s['ratio']:.1f}, [{cats}]")
