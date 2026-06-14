#!/usr/bin/env python3
"""
consolidate_final_wordlist.py
================================
Script final de consolidation : fusionne les résultats de l'algorithme V3 (fréquence + 
Wiktionnaire) avec les résultats des embeddings sémantiques pour produire une liste
de candidats propre et validée.

Ce script :
1. Charge les résultats de literary_candidates_v3.json (score fréq + Wiktionnaire)
2. Charge les résultats de literary_final_combined.json (score embedding)
3. Croise les deux : garde uniquement les mots qui sont bons DANS LES DEUX approches
4. Propose une liste thématiquement équilibrée de nouveaux candidats
5. Génère un rapport final lisible

Usage : python consolidate_final_wordlist.py
"""

import json
import csv
import os
from collections import defaultdict

TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))

# Fichiers d'entrée
REFERENCE_CSV = os.path.join(TOOLS_DIR, "consolidated_literary_words.csv")
V3_JSON = os.path.join(TOOLS_DIR, "literary_candidates_v3.json")
EMBED_JSON = os.path.join(TOOLS_DIR, "literary_final_combined.json")

# Fichiers de sortie
OUTPUT_REPORT = os.path.join(TOOLS_DIR, "literary_final_report.md")
OUTPUT_NEW_CANDIDATES = os.path.join(TOOLS_DIR, "literary_validated_candidates.csv")

# Seuils de décision (calibrés manuellement)
# Un mot est retenu si :
# - Score V3 >= THRESHOLD_V3 (bon signal fréquence + Wiktionnaire)
# - Score sémantique >= THRESHOLD_SEM (proche du centroïde littéraire)
# - OU score V3 très élevé (>= THRESHOLD_V3_HIGH) même si sem modéré
THRESHOLD_V3 = 0.70       # issu des analyses précédentes
THRESHOLD_V3_HIGH = 0.90  # pour inclusion sans contrainte sémantique
THRESHOLD_SEM = 0.55      # similarité cosinus minimale au centroïde
THRESHOLD_SEM_LOOSE = 0.50 # pour les mots avec très bon score V3

# Exclusions éditoriales finales
FINAL_EXCLUSIONS = {
    # Verbes trop courants
    "confondre", "décéder", "décédé", "désolé", "accouder", "accroupir",
    "ahurir", "assaisonner", "colporter", "fouler", "advenir", "corroder",
    "habiter", "partir", "rester", "tomber", "lever", "porter",
    # Adverbes
    "diligemment", "périlleusement", "nonobstant",
    # Locutions
    "ab ovo", "hic et nunc",
    # Géographiques/propres
    "helvétique", "nippon", "diane",
    # Non littéraires malgré classification
    "acnéique", "balane", "graffito", "socque", "incomestible",
    "discontinuité", "concréter", "déhiscence", "pagure", "anatidé",
    "gouffre", "guerrier", "dindon", "homicide", "milice",
    "nourrisson", "muraille", "commune", "faction", "favori",
    "prospère", "circonstanciel", "modérateur", "physionomiste",
    "arrière-neveux", "couronner", "altitude", "annales", "alentour",
    "aliénation", "amante", "amphitryon", "aboutissant", "accidenter",
    "sylvie", "agathe", "avide", "artificiel", "artifice",
    # Mots de moins de 5 caractères (gérés par filtre)
}

def load_reference() -> dict:
    ref = {}
    with open(REFERENCE_CSV, "r", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f, delimiter=";"):
            mot = row["mot"].strip().lower()
            if mot:
                ref[mot] = row["theme"].strip()
    return ref

def load_v3() -> dict:
    if not os.path.exists(V3_JSON):
        print(f"[!] {V3_JSON} non trouvé. Lancez d'abord discover_literary_words_v3.py")
        return {}
    with open(V3_JSON, "r", encoding="utf-8") as f:
        return json.load(f)

def load_embeddings() -> dict:
    if not os.path.exists(EMBED_JSON):
        print(f"[!] {EMBED_JSON} non trouvé. Lancez d'abord discover_literary_embeddings.py")
        return {}
    with open(EMBED_JSON, "r", encoding="utf-8") as f:
        return json.load(f)

def main():
    print("=" * 65)
    print("CONSOLIDATION FINALE - ALGORITHME V3 + EMBEDDINGS")
    print("=" * 65)
    
    # Chargement
    reference = load_reference()
    ref_set = set(reference.keys())
    v3_data = load_v3()
    embed_data = load_embeddings()
    
    print(f"[OK] Référence : {len(ref_set)} mots")
    
    has_embeddings = bool(embed_data and embed_data.get("new_excellent_candidates"))
    print(f"[OK] Embeddings disponibles : {has_embeddings}")
    
    # ─── 1. Construire le dictionnaire des scores par mot ─────────────
    
    # Score V3
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
    
    # Score embedding
    embed_by_word = {}
    if has_embeddings:
        for item in embed_data.get("new_excellent_candidates", []):
            embed_by_word[item["mot"]] = {
                "score_embed": item.get("score_final", 0),
                "score_sem": item.get("score_semantique", 0),
                "theme_similar": item.get("theme_similar", "?"),
            }
    
    # ─── 2. Décision de validation ────────────────────────────────────
    
    print(f"\n  Candidats V3 disponibles : {len(v3_by_word)}")
    print(f"  Candidats embed disponibles : {len(embed_by_word)}")
    
    validated = []
    rejected = []
    
    # Pool = union V3 + embed
    all_candidates = set(v3_by_word.keys()) | set(embed_by_word.keys())
    
    for mot in all_candidates:
        # Filtres de base
        if mot in FINAL_EXCLUSIONS:
            continue
        if mot in ref_set:
            continue
        if " " in mot:
            continue
        if len(mot) < 5:
            continue
        
        v3_info = v3_by_word.get(mot, {})
        em_info = embed_by_word.get(mot, {})
        
        score_v3 = v3_info.get("score_v3", 0)
        zipf = v3_info.get("zipf", 0)
        ratio = v3_info.get("ratio", 0)
        cats = v3_info.get("cats", [])
        
        score_sem = em_info.get("score_sem", 0)
        theme_similar = em_info.get("theme_similar", "?")
        
        # Zipf hors plage
        if zipf > 4.3:
            continue
        if 0 < zipf < 1.0 and not cats:
            continue  # trop rare ET pas de catégorie Wiktionnaire
        
        # Décision de validation
        validated_by_v3 = score_v3 >= THRESHOLD_V3
        validated_by_embed = (score_sem >= THRESHOLD_SEM and score_v3 >= THRESHOLD_V3 * 0.80)
        validated_by_strong_v3 = score_v3 >= THRESHOLD_V3_HIGH
        
        # Validation si embeddings disponibles
        if has_embeddings:
            is_valid = (
                (validated_by_v3 and validated_by_embed) or
                validated_by_strong_v3
            ) and score_sem >= THRESHOLD_SEM_LOOSE
        else:
            # Sans embeddings : validation uniquement par V3
            is_valid = validated_by_v3
        
        if is_valid:
            validated.append({
                "mot": mot,
                "score_v3": round(score_v3, 4),
                "score_sem": round(score_sem, 4),
                "zipf_books": round(zipf, 3),
                "ratio": round(ratio, 3),
                "cats": cats if isinstance(cats, list) else cats.split("+") if cats else [],
                "theme_similar": theme_similar,
                "methode": "V3+Embedding" if has_embeddings else "V3 seul",
            })
        else:
            rejected.append({
                "mot": mot,
                "score_v3": round(score_v3, 4),
                "score_sem": round(score_sem, 4),
                "zipf": round(zipf, 3),
            })
    
    # Trier par score V3 décroissant, puis sémantique
    validated.sort(key=lambda x: (-x["score_v3"], -x["score_sem"], x["mot"]))
    
    # ─── 3. Analyse des intrus dans notre liste de référence ──────────
    
    # Utiliser les données d'intrus V3 et embeddings
    v3_intruders = {item["mot"] for item in v3_data.get("true_intruders", [])}
    embed_intruders = {item["mot"] for item in embed_data.get("semantic_intruders", [])} if has_embeddings else set()
    
    # Intrus confirmés par les deux méthodes
    confirmed_intruders = v3_intruders & embed_intruders
    suspected_intruders = v3_intruders | embed_intruders
    
    # ─── 4. Rapport ───────────────────────────────────────────────────
    
    print(f"\n  Candidats validés : {len(validated)}")
    print(f"  Candidats rejetés : {len(rejected)}")
    print(f"  Intrus confirmés  : {len(confirmed_intruders)}")
    print(f"  Intrus suspectés  : {len(suspected_intruders)}")
    
    with open(OUTPUT_REPORT, "w", encoding="utf-8") as f:
        f.write("# Rapport Final - Mots Littéraires Validés\n\n")
        f.write("**Méthode** : Algorithme V3 (fréquence + Wiktionnaire) × Embeddings sémantiques\n\n")
        
        f.write("---\n\n")
        
        # Intrus
        f.write(f"## Intrus Confirmés dans Notre Liste de Référence\n\n")
        f.write(f"Mots signalés à la fois par l'algorithme V3 ET les embeddings :\n\n")
        if confirmed_intruders:
            f.write("| Mot | Thème | Raison |\n|---|---|---|\n")
            for w in sorted(confirmed_intruders):
                theme = reference.get(w, "?")
                f.write(f"| **{w}** | {theme} | Signalé par V3 + Embeddings |\n")
        else:
            f.write("✅ Aucun intrus confirmé par les deux méthodes.\n")
        
        if embed_intruders - v3_intruders:
            f.write(f"\n### Intrus Sémantiques Uniquement ({len(embed_intruders - v3_intruders)} mots)\n\n")
            f.write("Signalés par les embeddings seulement (fréquence correcte, mais sémantique éloignée) :\n\n")
            for w in sorted(embed_intruders - v3_intruders):
                theme = reference.get(w, "?")
                f.write(f"- **{w}** ({theme})\n")
        
        if v3_intruders - embed_intruders:
            f.write(f"\n### Intrus Fréquentiels Uniquement ({len(v3_intruders - embed_intruders)} mots)\n\n")
            f.write("Signalés par V3 seulement (mauvaise fréquence, mais sémantique proche du centroïde) :\n\n")
            for w in sorted(v3_intruders - embed_intruders):
                theme = reference.get(w, "?")
                f.write(f"- **{w}** ({theme})\n")
        
        f.write("\n---\n\n")
        
        # Candidats validés par thème
        by_theme = defaultdict(list)
        for c in validated:
            by_theme[c["theme_similar"]].append(c)
        
        f.write(f"## Nouveaux Candidats Validés ({len(validated)} mots)\n\n")
        f.write(f"Validés par V3 (score >= {THRESHOLD_V3}) ET embeddings (sim >= {THRESHOLD_SEM}) :\n\n")
        
        f.write("| Mot | Score V3 | Sim. Sém. | Zipf | Ratio | Catégories | Thème Similaire |\n")
        f.write("|---|---|---|---|---|---|---|\n")
        for c in validated[:100]:
            cats_str = "+".join(c["cats"]) if c["cats"] else "—"
            f.write(f"| **{c['mot']}** | {c['score_v3']:.3f} | {c['score_sem']:.3f} | "
                    f"{c['zipf_books']:.2f} | {c['ratio']:.2f} | {cats_str} | {c['theme_similar']} |\n")
        
        f.write("\n---\n\n")
        
        # Répartition par thème similaire
        f.write("## Répartition par Thème Similaire\n\n")
        f.write("| Thème | Nombre de candidats |\n|---|---|\n")
        for theme, words in sorted(by_theme.items(), key=lambda x: -len(x[1])):
            f.write(f"| {theme} | {len(words)} |\n")
        
        f.write("\n---\n\n")
        
        # Résumé
        f.write("## Résumé des Recommandations\n\n")
        f.write(f"### Pour la liste de référence :\n")
        f.write(f"- **{len(confirmed_intruders)}** mots à reconsidérer (signalés par les deux méthodes)\n")
        f.write(f"- **{len(suspected_intruders) - len(confirmed_intruders)}** mots en zone grise (signalés par une seule méthode)\n\n")
        f.write(f"### Nouveaux mots à intégrer :\n")
        f.write(f"- **{len(validated)}** candidats validés disponibles dans `literary_validated_candidates.csv`\n")
        f.write(f"- Recommandation : ajouter les **{min(50, len(validated))} meilleurs** après revue éditoriale\n\n")
    
    print(f"[OK] Rapport final : {OUTPUT_REPORT}")
    
    # CSV des candidats validés
    with open(OUTPUT_NEW_CANDIDATES, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f, delimiter=";")
        writer.writerow(["mot", "score_v3", "score_semantique", "zipf_books", "ratio",
                         "categories_wikt", "theme_similaire", "methode"])
        for c in validated:
            writer.writerow([
                c["mot"], c["score_v3"], c["score_sem"],
                c["zipf_books"], c["ratio"],
                "+".join(c["cats"]),
                c["theme_similar"],
                c["methode"],
            ])
    print(f"[OK] Candidats CSV : {OUTPUT_NEW_CANDIDATES}")
    
    # Affichage console
    print(f"\n{'='*65}")
    print(f"TOP 30 CANDIDATS VALIDÉS :")
    print(f"{'='*65}")
    for i, c in enumerate(validated[:30]):
        cats = "+".join(c["cats"]) if c["cats"] else "—"
        print(f"  {i+1:2d}. {c['mot']:<22} V3={c['score_v3']:.3f} | Sem={c['score_sem']:.3f} | "
              f"Zipf={c['zipf_books']:.2f} | [{cats}]")
    
    if confirmed_intruders:
        print(f"\n{'='*65}")
        print(f"INTRUS CONFIRMÉS À REVOIR ({len(confirmed_intruders)}) :")
        for w in sorted(confirmed_intruders):
            print(f"  - {w} ({reference.get(w,'?')})")

if __name__ == "__main__":
    main()
