#!/usr/bin/env python3
"""
generate_master_report.py
===========================
Script final : génère le rapport de synthèse complet de l'algorithme.
- Évalue chaque mot de la liste de référence avec le score combiné V3+Embedding
- Identifie les vrais intrus et les lacunes de données
- Propose une liste nette de nouveaux candidats après filtrage éditorial
- Génère un rapport Markdown lisible et un CSV propre
"""

import json, csv, os, re
from collections import defaultdict

TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))

# Chargement des fichiers intermédiaires
REFERENCE_CSV = os.path.join(TOOLS_DIR, "consolidated_literary_words.csv")
CANDIDATES_CSV = os.path.join(TOOLS_DIR, "literary_validated_candidates.csv")
EMBED_JSON = os.path.join(TOOLS_DIR, "literary_final_combined.json")
V3_JSON = os.path.join(TOOLS_DIR, "literary_candidates_v3.json")
ENRICHED_V3_CSV = os.path.join(TOOLS_DIR, "literary_enriched_v3.csv")

OUTPUT_REPORT = os.path.join(TOOLS_DIR, "literary_master_report.md")
OUTPUT_CLEAN_CSV = os.path.join(TOOLS_DIR, "literary_new_validated_clean.csv")

# ─── FILTRAGE ÉDITORIAL FINAL ─────────────────────────────────────────────────
# Mots à exclure car non pertinents pour du vocabulaire littéraire
# (même s'ils ont de bons scores techniques)

FINAL_EXCLUSIONS = {
    # ── Trop courants / sens trop général ──
    "confondre", "décéder", "décédé", "désolé", "frileux", "boiteux", "enrhumé",
    "avide", "gouffre", "guerrier", "dindon", "homicide", "milice", "nourrisson",
    "muraille", "commune", "faction", "favori", "prospère", "couronner",
    "modérateur", "cascadeur", "disant", "humilié",
    # ── Candidats écartés après relecture éditoriale ──
    "gémissement",   # trop courant, pas spécifiquement littéraire
    "déconcerter",   # verbe courant
    "contempteur",   # trop rare, obscur hors contexte spécialisé
    "chalumeau",     # ustensile/instrument, peu narratif
    "cygne",         # trop banal, image trop rebattue
    # ── Trop techniques / scientifiques ──
    "cétacé", "anatidé", "pagure", "balane", "acnéique", "astreinte",
    # ── Anatomiques / vulgaires ──
    "fondement", "crapule", "stupre",
    # ── Trop prosaïques / non poétiques ──
    "cascadeur", "commodité", "circonstanciel", "dériveur", "fructueux",
    "correspondance",  # trop général
    "coutumier",  # trop courant
    # ── Géographiques / propres ──
    "helvétique", "nippon", "diane", "sylvie", "agathe",
    # ── Locutions / multi-mots ──
    "ab ovo", "hic et nunc", "arrière-neveux",
    # ── Adverbes peu évocateurs ──
    "diligemment", "périlleusement", "nonobstant", "civilement", "bellement",
    # ── Verbes courants mal classés ──
    "accouder", "accroupir", "ahurir", "assaisonner", "colporter",
    "fouler", "advenir", "corroder", "accidenter", "aboutissant",
    # ── Autres mots non littéraires ──
    "graffito", "socque", "incomestible", "discontinuité", "concréter",
    "déhiscence", "checklist", "fleuronner", "esseuler", "difficultueux",
    "fantasquement", "chevaucheur", "aimeur", "douteur",  # trop rares/obscurs
    "appariteur", "fourrier", "chaumine", "courtil",  # trop spécialisés
    "encor",  # forme archaïque d'"encore" plutôt qu'un mot
    "dubitation", "faunesque", "faunesse", "aimeur",
    "artificieux",  # trop proche d'"artificiel" courant
    "assomption",  # théologique
    "fronde",  # arme / contexte trop précis
    "decomber",
}

# Mots que le Wiktionnaire classe mal (archaïque utilisé pour des mots simplement vieillis)
# Mais qui sont bons pour notre usage littéraire
KEEP_DESPITE_ARCHAIC = {
    "commère", "colombe", "chaume", "aquilon", "barde", "faucille",
    "albâtre", "bergerie", "gerbe", "cavale",
    "dépouille", "brisure", "décombre",
}

# Mots de notre liste de référence protégés contre le rejet sémantique.
# NOTE : le centroïde pénalise les mots les plus ORIGINAUX (spleen, opalin)
# — c'est leur qualité littéraire, pas un défaut. On ne les remet jamais en question.
PROTECTED_REFERENCE = {
    "spleen",          # Baudelaire — mot littéraire par excellence
    "opalin",          # adjectif poétique de couleur/lumière
    "torpeur",         # sensation littéraire très utilisée
    "abscons",         # registre philosophique/littéraire
    "hymen",           # au sens poétique = union, mariage (Racine, Ronsard...)
    "azur",            # couleur-symbole (Mallarmé : "L'Azur")
    "larron",          # archaïque mais littéraire (bon larron)
    "mentor",          # sens littéraire, origine mythologique
    "phosphorescence", # poétique (Rimbaud, Loti...)
    "quiproquo",       # théâtral, Molière
    "coruscant",       # poétique — étinceler
    "séquestration",   # peut être littéraire dans contexte romanesque
    "volubilité",      # trait de caractère, Balzac
    "collusion",       # registre romanesque/politique littéraire
    "précepteur",      # Rousseau, Voltaire — sens littéraire fort
    "éolien",          # poétique (harpe éolienne)
    "antédiluvien",    # sens figuré littéraire courant
    "abscons",         # philosophique-littéraire
    "circonspect",     # adjectif de caractère littéraire
}

def load_reference():
    ref = {}
    with open(REFERENCE_CSV, "r", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f, delimiter=";"):
            mot = row["mot"].strip().lower()
            if mot: ref[mot] = row["theme"].strip()
    return ref

def load_candidates():
    candidates = []
    if not os.path.exists(CANDIDATES_CSV):
        return candidates
    with open(CANDIDATES_CSV, "r", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f, delimiter=";"):
            mot = row["mot"].strip().lower()
            try:
                score_v3 = float(row["score_v3"])
                score_sem = float(row["score_semantique"])
                zipf = float(row["zipf_books"])
                ratio = float(row["ratio"])
            except:
                continue
            candidates.append({
                "mot": mot,
                "score_v3": score_v3,
                "score_sem": score_sem,
                "zipf": zipf,
                "ratio": ratio,
                "cats": row.get("categories_wikt", "").split("+") if row.get("categories_wikt") else [],
                "theme_similar": row.get("theme_similaire", "?"),
            })
    return candidates

def load_embed_data():
    if not os.path.exists(EMBED_JSON):
        return {}
    with open(EMBED_JSON, "r", encoding="utf-8") as f:
        return json.load(f)

def load_v3_enriched():
    rows = {}
    if not os.path.exists(ENRICHED_V3_CSV):
        return rows
    with open(ENRICHED_V3_CSV, "r", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f, delimiter=";"):
            mot = row["mot"].strip().lower()
            rows[mot] = row
    return rows

def main():
    print("=" * 65)
    print("RAPPORT MAÎTRE - SYNTHÈSE COMPLÈTE")
    print("=" * 65)
    
    reference = load_reference()
    ref_set = set(reference.keys())
    candidates_raw = load_candidates()
    embed_data = load_embed_data()
    v3_enriched = load_v3_enriched()
    
    print(f"[OK] Référence : {len(ref_set)} mots")
    print(f"[OK] Candidats bruts : {len(candidates_raw)}")
    
    # ─── 1. Filtrage éditorial des candidats ──────────────────────
    clean_candidates = []
    rejected_by_editorial = []
    
    for c in candidates_raw:
        mot = c["mot"]
        
        # Filtres stricts
        if mot in FINAL_EXCLUSIONS:
            rejected_by_editorial.append((mot, "exclusion éditoriale"))
            continue
        if mot in ref_set:
            continue
        if " " in mot or "-" in mot:
            # Garder les mots composés du type "clair-obscur" mais pas les locutions longues
            if mot.count("-") > 1 or len(mot) > 20:
                rejected_by_editorial.append((mot, "locution trop longue"))
                continue
        if len(mot) < 5:
            rejected_by_editorial.append((mot, "trop court"))
            continue
        if c["zipf"] > 4.3:
            rejected_by_editorial.append((mot, f"trop commun Zipf={c['zipf']:.2f}"))
            continue
        
        # Score sémantique minimal
        if c["score_sem"] < 0.595:
            rejected_by_editorial.append((mot, f"sem trop faible ({c['score_sem']:.3f})"))
            continue
        
        # Calcul du score de qualité éditorial (combiné + bonus thématique)
        cats = c["cats"]
        cat_weight = 0
        if "soutenu" in cats: cat_weight = 3
        elif "littéraire" in cats: cat_weight = 3
        elif "poétique" in cats: cat_weight = 2
        elif "archaïque" in cats: cat_weight = 1
        
        editorial_score = (c["score_sem"] * 0.5 + c["score_v3"] * 0.35 + cat_weight * 0.05)
        
        clean_candidates.append({
            **c,
            "editorial_score": round(editorial_score, 4),
            "cat_weight": cat_weight,
        })
    
    # Trier par score éditorial
    clean_candidates.sort(key=lambda x: (-x["editorial_score"], -x["score_sem"], x["mot"]))
    
    print(f"[OK] Candidats après filtrage éditorial : {len(clean_candidates)}")
    print(f"[OK] Rejetés par filtrage éditorial : {len(rejected_by_editorial)}")
    
    # ─── 2. Analyse des intrus sémantiques dans la liste de référence ─
    semantic_intruders = embed_data.get("semantic_intruders", [])
    
    # Intrus combinés (sémantique + fréquence)
    intruders_v3 = {item["mot"] for item in embed_data.get("combined_intruders", [])}
    intruders_sem = {item["mot"] for item in semantic_intruders}
    
    # Distinguer les vrais intrus des mots avec lacune de données
    true_intruders = []   # mauvais score ET données disponibles
    data_gaps = []        # mauvais score par manque de données

    for mot, row in v3_enriched.items():
        score = float(row.get("score_v3", 0))
        has_lex = row.get("has_lexique") == "oui"
        has_wikt = row.get("has_wikt") == "oui"
        cls = row.get("classe", "")
        
        if cls in ["Faible", "Exclu"] and score < 0.70:
            if has_lex:
                true_intruders.append((mot, score, reference.get(mot, "?"), row))
            elif not has_lex and not has_wikt:
                data_gaps.append((mot, score, reference.get(mot, "?")))
    
    true_intruders.sort(key=lambda x: x[1])
    data_gaps.sort(key=lambda x: x[1])
    
    # ─── 3. Classement des candidats par thème ──────────────────────
    by_theme = defaultdict(list)
    for c in clean_candidates:
        by_theme[c["theme_similar"]].append(c)
    
    # ─── 4. Rapport Markdown final ──────────────────────────────────
    print("\n--- Génération du rapport maître ---")
    
    with open(OUTPUT_REPORT, "w", encoding="utf-8") as f:
        f.write("# Rapport Maître — Découverte de Mots Littéraires Français\n\n")
        f.write("## Méthode Combinée : Lexique383 + Wiktionnaire + Embeddings Sémantiques\n\n")
        f.write("```\n")
        f.write("SOURCES :\n")
        f.write("  1. Lexique383 (lexique.org)  → Score de fréquence littéraire (Zipf_books)\n")
        f.write("  2. Wiktionnaire (4 catégories) → Bonus de registre (soutenu/littéraire/poétique)\n")
        f.write("  3. OrdalieTech/solon-embeddings-large-0.1 → Score sémantique (sim. cosinus)\n\n")
        f.write("SCORE FINAL = (50% sémantique) + (35% fréquence) + (15% Wiktionnaire)\n")
        f.write("```\n\n")
        f.write("---\n\n")
        
        # ── Section 1 : État de la liste de référence ──────────────
        f.write("## 1. État de Notre Liste de Référence (407 mots)\n\n")
        
        # Statistiques
        total_ref = len(ref_set)
        nb_true_intruders = len(true_intruders)
        nb_data_gaps = len(data_gaps)
        nb_good = total_ref - nb_true_intruders - nb_data_gaps
        
        f.write(f"| Catégorie | Nombre | % |\n|---|---|---|\n")
        f.write(f"| **Bons mots** (score >= seuil) | **{nb_good}** | {100*nb_good//total_ref}% |\n")
        f.write(f"| Intrus avérés (mauvais score + données) | {nb_true_intruders} | {100*nb_true_intruders//total_ref}% |\n")
        f.write(f"| Lacunes de données (pas dans Lexique/Wiktionnaire) | {nb_data_gaps} | {100*nb_data_gaps//total_ref}% |\n\n")
        
        # ── Intrus sémantiques ────────────────────────────────────
        f.write("### 1a. Distance au Centroïde Littéraire (information sémantique)\n\n")
        f.write("> **Rappel méthodologique** : la distance au centroïde NE DIS QUALIFIE PAS un mot.\n")
        f.write("> Les mots littéraires les plus **originaux** (spleen, opalin, azur) sont précisément\n")
        f.write("> ceux qui s'éloignent du centroïde — c'est leur qualité, pas un défaut.\n")
        f.write("> Le centroïde sert uniquement à filtrer les NOUVEAUX candidats (mots courants\n")
        f.write("> comme confondre, décéder qui auraient un bon score Wiktionnaire mais pas littéraires).\n\n")
        f.write("| Mot | Thème | Sim. Cosinus | Statut éditorial |\n|---|---|---|---|\n")
        for item in semantic_intruders[:20]:
            mot = item["mot"]
            theme = reference.get(mot, "?")
            sim = item["sim_cosinus"]
            
            # Statut éditorial basé sur la décision humaine
            if mot in PROTECTED_REFERENCE:
                statut = "✅ PROTÉGÉ — éloigné du centroïde MAIS mot littéraire de qualité"
            elif sim < 0.51:
                statut = "⚠️ À revoir — sémantiquement très éloigné"
            elif sim < 0.54:
                statut = "ℹ️ Information — originalité sémantique (conserver par défaut)"
            else:
                statut = "✓ Dans la marge acceptable — conserver"
            
            f.write(f"| **{mot}** | {theme} | {sim:.4f} | {statut} |\n")
        
        f.write("\n---\n\n")
        
        # ── Intrus avérés ───────────────────────────────────────────
        if true_intruders:
            f.write(f"### 1b. Intrus Avérés par l'Algorithme ({len(true_intruders)} mots)\n\n")
            f.write("Score faible avec données disponibles dans Lexique383 :\n\n")
            f.write("| Mot | Thème | Score V3 | Zipf | Ratio | Raison |\n")
            f.write("|---|---|---|---|---|---|\n")
            for mot, score, theme, row in true_intruders[:20]:
                zipf = float(row.get("zipf_books", 0))
                ratio = float(row.get("ratio", 0))
                if zipf > 4.3:
                    raison = f"Trop commun (Zipf={zipf:.2f})"
                elif ratio < 0.5:
                    raison = f"Registre trop oral (ratio={ratio:.2f})"
                else:
                    raison = "Score composite faible"
                f.write(f"| **{mot}** | {theme} | {score:.3f} | {zipf:.2f} | {ratio:.2f} | {raison} |\n")
        
        f.write("\n---\n\n")
        
        # ── Lacunes de données ──────────────────────────────────────
        if data_gaps:
            f.write(f"### 1c. Mots de Référence sans Données ({len(data_gaps)} mots)\n\n")
            f.write("Ces mots ont un mauvais score **uniquement parce qu'ils sont absents**\n")
            f.write("de Lexique383 et des catégories Wiktionnaire — ils sont probablement légitimes :\n\n")
            f.write("| Mot | Thème | Commentaire |\n|---|---|---|\n")
            for mot, score, theme in data_gaps[:25]:
                comment = "Néologisme ou terme très rare" if len(mot) > 10 else "Mot court ou spécialisé"
                f.write(f"| {mot} | {theme} | {comment} |\n")
        
        f.write("\n---\n\n")
        
        # ── Section 2 : Nouveaux candidats validés ──────────────────
        f.write(f"## 2. Nouveaux Candidats Validés ({len(clean_candidates)} mots)\n\n")
        f.write("Filtrés par :\n")
        f.write("- Score sémantique >= 0.595 (proche du centroïde littéraire)\n")
        f.write("- Zipf_books ∈ [1.0, 4.3]\n")
        f.write("- Longueur >= 5 caractères, pas de locution\n")
        f.write("- Filtrage éditorial manuel (exclusion des mots non pertinents)\n\n")
        
        f.write("| Rang | Mot | Score Sem. | Score V3 | Zipf | Ratio | Catégories | Thème Similaire |\n")
        f.write("|---|---|---|---|---|---|---|---|\n")
        for i, c in enumerate(clean_candidates):
            cats_str = "+".join(c["cats"]) if c["cats"] else "—"
            f.write(f"| {i+1} | **{c['mot']}** | {c['score_sem']:.3f} | {c['score_v3']:.3f} | "
                    f"{c['zipf']:.2f} | {c['ratio']:.1f} | {cats_str} | {c['theme_similar']} |\n")
        
        f.write("\n---\n\n")
        
        # ── Répartition par thème ───────────────────────────────────
        f.write("## 3. Répartition des Nouveaux Candidats par Thème Similaire\n\n")
        f.write("| Thème | Nombre | Top 5 mots |\n|---|---|---|\n")
        for theme, words in sorted(by_theme.items(), key=lambda x: -len(x[1])):
            top5 = ", ".join(c["mot"] for c in words[:5])
            f.write(f"| {theme} | {len(words)} | {top5} |\n")
        
        f.write("\n---\n\n")
        
        # ── Section 3 : Recommandations ─────────────────────────────
        f.write("## 4. Recommandations\n\n")
        f.write("### À faire pour la liste de référence :\n\n")
        f.write(f"- **Revoir** les {len(semantic_intruders)} mots sémantiquement éloignés du centroïde\n")
        f.write("  (spleen, opalin, séquestration, torpeur, abscons, mentor...)\n")
        f.write(f"- **Vérifier** les {len(true_intruders)} mots avec mauvais score algorithmique\n")
        f.write(f"- **Accepter** les {len(data_gaps)} mots absents des bases de données (lacunes normales)\n\n")
        
        f.write("### Pour enrichir la liste de référence :\n\n")
        f.write(f"Les **{len(clean_candidates)} nouveaux candidats** dans `literary_new_validated_clean.csv`\n")
        f.write("sont prêts pour revue éditoriale. Suggestions prioritaires :\n\n")
        f.write("**Tier 1 (score sem. > 0.67) — à intégrer en priorité :**\n\n")
        for c in clean_candidates:
            if c["score_sem"] >= 0.67:
                cats = "+".join(c["cats"])
                f.write(f"- **{c['mot']}** : {cats}, Zipf={c['zipf']:.2f}\n")
        
        f.write("\n**Tier 2 (score sem. 0.63–0.67) — bons candidats :**\n\n")
        for c in clean_candidates:
            if 0.63 <= c["score_sem"] < 0.67:
                cats = "+".join(c["cats"])
                f.write(f"- **{c['mot']}** : {cats}, Zipf={c['zipf']:.2f}, "
                        f"thème={c['theme_similar']}\n")
        
        f.write("\n---\n\n")
        
        # ── Section 4 : Mots rejetés par filtrage éditorial ─────────
        f.write(f"## 5. Mots Rejetés par Filtrage Éditorial ({len(rejected_by_editorial)} mots)\n\n")
        f.write("Ces mots avaient un bon score technique mais ont été exclus manuellement :\n\n")
        by_reason = defaultdict(list)
        for mot, reason in rejected_by_editorial:
            by_reason[reason].append(mot)
        for reason, words in sorted(by_reason.items(), key=lambda x: -len(x[1])):
            f.write(f"**{reason}** ({len(words)}) : {', '.join(sorted(words))}\n\n")
    
    print(f"[OK] Rapport maître : {OUTPUT_REPORT}")
    
    # ─── 5. CSV final propre ──────────────────────────────────────────
    with open(OUTPUT_CLEAN_CSV, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f, delimiter=";")
        writer.writerow(["rang", "mot", "score_editorial", "score_semantique", "score_v3",
                         "zipf_books", "ratio", "categories_wikt", "theme_similaire"])
        for i, c in enumerate(clean_candidates):
            writer.writerow([
                i + 1, c["mot"],
                round(c["editorial_score"], 4),
                round(c["score_sem"], 4),
                round(c["score_v3"], 4),
                round(c["zipf"], 3),
                round(c["ratio"], 3),
                "+".join(c["cats"]),
                c["theme_similar"],
            ])
    print(f"[OK] CSV propre : {OUTPUT_CLEAN_CSV}")
    
    # ─── 6. Résumé console ───────────────────────────────────────────
    print(f"\n{'='*65}")
    print(f"RÉSUMÉ FINAL")
    print(f"{'='*65}")
    print(f"  Mots de référence : {len(ref_set)}")
    print(f"  Bons mots réf.    : {nb_good}")
    print(f"  Intrus avérés     : {nb_true_intruders}")
    print(f"  Lacunes données   : {nb_data_gaps}")
    print(f"  Nouveaux validés  : {len(clean_candidates)}")
    print(f"{'='*65}")
    
    print(f"\nTIER 1 - Candidats prioritaires (sem >= 0.67) :")
    for c in clean_candidates:
        if c["score_sem"] >= 0.67:
            cats = "+".join(c["cats"])
            print(f"  ⭐ {c['mot']:<22} sem={c['score_sem']:.3f}, Zipf={c['zipf']:.2f}, [{cats}]")
    
    print(f"\nTIER 2 - Bons candidats (sem 0.63–0.67) :")
    for c in clean_candidates:
        if 0.63 <= c["score_sem"] < 0.67:
            cats = "+".join(c["cats"])
            print(f"  ★  {c['mot']:<22} sem={c['score_sem']:.3f}, Zipf={c['zipf']:.2f}, [{cats}] → {c['theme_similar']}")

if __name__ == "__main__":
    main()
