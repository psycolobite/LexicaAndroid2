#!/usr/bin/env python3
"""
discover_literary_words.py
==========================
Algorithme d'exploration et de validation de mots littéraires français.

Sources :
  - Lexique383 (lexique.org) : données de fréquence (zipf_books, zipf_films, ratio)
  - Wiktionnaire (API MediaWiki) : registre lexical (soutenu, littéraire, poétique, archaïque)

Référence de validation :
  - consolidated_literary_words.csv : notre liste de référence (les "bons" mots)

Procédé :
  1. Pour chaque catégorie Wiktionnaire (Termes soutenus, Termes littéraires, etc.),
     on récupère les mots membres de la catégorie.
  2. Pour chaque mot, on interroge Lexique383 pour obtenir les données de fréquence.
  3. On calcule un score de pertinence littéraire multi-critères.
  4. On compare avec notre liste de référence pour calibrer l'algorithme.
  5. On détecte les "intrus" (mots avec mauvais score) et les "manqués" (mots de référence
     non retrouvés ou avec un mauvais score).
  6. On génère un rapport d'analyse et une liste de candidats.
"""

import json
import csv
import os
import time
import zipfile
import io
import re
import urllib.request
import urllib.parse
import unicodedata

# ─── CONFIGURATION ────────────────────────────────────────────────────────────

TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))
REFERENCE_CSV = os.path.join(TOOLS_DIR, "consolidated_literary_words.csv")
LEXIQUE_ZIP = os.path.join(TOOLS_DIR, "Lexique383.zip")
LEXIQUE_TSV = "Lexique383.tsv"  # nom interne dans le ZIP

OUTPUT_REPORT = os.path.join(TOOLS_DIR, "literary_discovery_report.md")
OUTPUT_CANDIDATES = os.path.join(TOOLS_DIR, "literary_candidates.json")
OUTPUT_ANALYSIS = os.path.join(TOOLS_DIR, "literary_discovery_analysis.csv")

WIKT_API_URL = "https://fr.wiktionary.org/w/api.php"

# Catégories Wiktionnaire à explorer
WIKT_CATEGORIES = [
    "Termes soutenus en français",
    "Termes littéraires en français",
    "Termes poétiques en français",
    "Termes archaïques en français",
]

# Cache local pour les appels API
CACHE_DIR = TOOLS_DIR

# Thèmes de référence de notre liste consolidée
REFERENCE_THEMES = [
    "Art & Langage",
    "Esprit & Caractère",
    "Lumière & Ombres",
    "Nature & Cosmos",
    "Philosophie & Idées",
    "Sentiments & Psyché",
    "Temps & Éphémère",
]

# ─── UTILITAIRES ──────────────────────────────────────────────────────────────

def normalize(word: str) -> str:
    """Normalise un mot : minuscules, suppression des espaces superflus."""
    return word.strip().lower()

def strip_accents_for_lookup(word: str) -> str:
    """Retire les accents pour comparaison avec Lexique (qui peut manquer d'accents)."""
    nfkd = unicodedata.normalize('NFKD', word)
    return "".join(c for c in nfkd if not unicodedata.combining(c))

def load_cache(category_name: str) -> dict | None:
    """Charge le cache JSON pour une catégorie Wiktionnaire."""
    safe_name = "cache_" + re.sub(r'[^\w]', '_', category_name) + ".json"
    path = os.path.join(CACHE_DIR, safe_name)
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)
    return None

def save_cache(category_name: str, data: dict):
    """Sauvegarde le cache JSON pour une catégorie."""
    safe_name = "cache_" + re.sub(r'[^\w]', '_', category_name) + ".json"
    path = os.path.join(CACHE_DIR, safe_name)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

# ─── 1. CHARGEMENT DE LA LISTE DE RÉFÉRENCE ───────────────────────────────────

def load_reference_list() -> dict:
    """Charge la liste consolidée. Retourne {mot_normalisé: theme}."""
    reference = {}
    with open(REFERENCE_CSV, "r", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f, delimiter=";")
        for row in reader:
            mot = normalize(row.get("mot", ""))
            theme = row.get("theme", "").strip()
            if mot:
                reference[mot] = theme
    print(f"✓ Liste de référence chargée : {len(reference)} mots")
    return reference

# ─── 2. CHARGEMENT DE LEXIQUE383 ──────────────────────────────────────────────

def load_lexique383() -> dict:
    """
    Charge Lexique383.tsv depuis le ZIP local.
    Retourne {lemma_normalisé: {zipf_books, zipf_films, ratio, cgram, ...}}.
    """
    print("Chargement de Lexique383...")
    lexique = {}
    
    if not os.path.exists(LEXIQUE_ZIP):
        print(f"⚠️  Lexique383.zip non trouvé dans {TOOLS_DIR}")
        print("   Téléchargement depuis lexique.org...")
        url = "http://www.lexique.org/databases/Lexique383/Lexique383.zip"
        try:
            urllib.request.urlretrieve(url, LEXIQUE_ZIP)
            print("✓ Téléchargement réussi")
        except Exception as e:
            print(f"✗ Téléchargement impossible : {e}")
            return {}
    
    try:
        with zipfile.ZipFile(LEXIQUE_ZIP, "r") as zf:
            # Cherche le fichier TSV dans le ZIP
            tsv_files = [n for n in zf.namelist() if n.endswith(".tsv") or n.endswith(".txt")]
            print(f"  Fichiers dans le ZIP : {tsv_files}")
            
            target = None
            for name in tsv_files:
                if "lexique" in name.lower() or "383" in name:
                    target = name
                    break
            if not target and tsv_files:
                target = tsv_files[0]
            
            if not target:
                print("✗ Aucun fichier TSV trouvé dans Lexique383.zip")
                return {}
            
            print(f"  Lecture de : {target}")
            with zf.open(target) as f:
                content = f.read().decode("utf-8", errors="replace")
                reader = csv.DictReader(io.StringIO(content), delimiter="\t")
                
                for row in reader:
                    # Colonnes Lexique383 importantes :
                    # ortho, lemme, cgram, freqlivres, freqfilms2, ...
                    # Les colonnes Zipf sont calculées (log10 des fréquences par million)
                    lemme = normalize(row.get("lemme", row.get("ortho", "")))
                    if not lemme:
                        continue
                    
                    try:
                        freq_books = float(row.get("freqlivres", 0) or 0)
                        freq_films = float(row.get("freqfilms2", 0) or 0)
                    except (ValueError, TypeError):
                        freq_books = 0.0
                        freq_films = 0.0
                    
                    # Calcul du score Zipf (log10 de la fréquence par million de mots + 3)
                    import math
                    zipf_books = math.log10(freq_books) + 3 if freq_books > 0 else 0.0
                    zipf_films = math.log10(freq_films) + 3 if freq_films > 0 else 0.0
                    
                    # Ratio livres/films : > 1 = plus littéraire qu'oral
                    ratio = (freq_books / freq_films) if freq_films > 0 else (10.0 if freq_books > 0 else 1.0)
                    ratio = min(ratio, 50.0)  # cap
                    
                    cgram = row.get("cgram", "")
                    
                    # Garder le meilleur enregistrement pour chaque lemme
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
        print(f"✗ Erreur lors du chargement de Lexique383 : {e}")
        import traceback
        traceback.print_exc()
        return {}
    
    print(f"✓ Lexique383 chargé : {len(lexique)} lemmes")
    return lexique

# ─── 3. INTERROGATION DU WIKTIONNAIRE ─────────────────────────────────────────

def fetch_wiktionary_category(category: str, max_pages: int = 2000) -> list:
    """
    Récupère les membres d'une catégorie Wiktionnaire via l'API MediaWiki.
    Retourne une liste de mots (titres de pages sans l'espace de noms).
    """
    # Vérifier le cache
    cached = load_cache(category)
    if cached is not None:
        words = cached.get("words", [])
        print(f"  [cache] {category} : {len(words)} mots")
        return words
    
    print(f"  Interrogation Wiktionnaire : {category}...")
    words = []
    cmcontinue = None
    
    while len(words) < max_pages:
        params = {
            "action": "query",
            "list": "categorymembers",
            "cmtitle": f"Catégorie:{category}",
            "cmlimit": "500",
            "cmnamespace": "0",  # espace principal uniquement
            "format": "json",
        }
        if cmcontinue:
            params["cmcontinue"] = cmcontinue
        
        url = WIKT_API_URL + "?" + urllib.parse.urlencode(params)
        
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "LexicaAndroid/1.0 (research)"})
            with urllib.request.urlopen(req, timeout=15) as resp:
                data = json.loads(resp.read().decode("utf-8"))
        except Exception as e:
            print(f"    ✗ Erreur API : {e}")
            break
        
        members = data.get("query", {}).get("categorymembers", [])
        for m in members:
            title = m.get("title", "")
            # Filtrer : garder uniquement les titres en minuscules (les mots ordinaires)
            # Exclure les pages spéciales, les noms propres, etc.
            if title and not title.startswith(("Wiktionnaire:", "Modèle:", "Annexe:")):
                # Exclure les noms propres (commencent par majuscule, sauf accents)
                first_char = title[0] if title else ""
                if first_char == first_char.lower() or first_char in "éèêëàâîïôùûüÿæœ":
                    words.append(title)
        
        # Pagination
        cont = data.get("continue", {})
        if "cmcontinue" in cont:
            cmcontinue = cont["cmcontinue"]
            time.sleep(0.3)  # respecter les limites de l'API
        else:
            break
    
    # Sauvegarder le cache
    save_cache(category, {"category": category, "words": words, "count": len(words)})
    print(f"  ✓ {len(words)} mots récupérés")
    return words

# ─── 4. SCORING DE PERTINENCE LITTÉRAIRE ──────────────────────────────────────

def score_literary_relevance(
    word: str,
    lex_data: dict | None,
    wikt_categories: list[str],
    reference_set: set,
) -> dict:
    """
    Calcule un score de pertinence littéraire multi-critères.
    
    Critères :
    A. Fréquence (Lexique383) : ni trop commun, ni trop rare
    B. Ratio livres/films : > 1.5 = registre littéraire/écrit
    C. Catégorie Wiktionnaire : soutenu > littéraire > poétique > archaïque
    D. Longueur minimale : >= 5 lettres (les bons mots littéraires sont rarement courts)
    E. Bonus de validation : le mot est dans notre liste de référence
    
    Retourne un dict avec le score et les détails.
    """
    
    # Données Lexique
    zb = lex_data["zipf_books"] if lex_data else 0.0
    zf = lex_data["zipf_films"] if lex_data else 0.0
    ratio = lex_data["ratio"] if lex_data else 1.0
    
    # ── A. Score de fréquence ──────────────────────────────────────────────
    # Plage idéale pour un vocabulaire littéraire C1/C2 : zipf_books entre 1.5 et 3.5
    # Trop commun (>4.0) : pas intéressant
    # Trop rare (<0.5) : impraticable
    if 2.0 <= zb <= 3.5:
        freq_score = 1.0
    elif 1.5 <= zb < 2.0:
        freq_score = 0.85
    elif 3.5 < zb <= 4.0:
        freq_score = 0.7
    elif 1.0 <= zb < 1.5:
        freq_score = 0.6
    elif 4.0 < zb <= 4.5:
        freq_score = 0.4
    elif 0.5 <= zb < 1.0:
        freq_score = 0.35
    elif zb > 4.5:
        freq_score = 0.1  # trop commun
    elif zb > 0:
        freq_score = 0.2
    else:
        # Pas de données Lexique : on se fie uniquement au Wiktionnaire
        if zf > 0:
            freq_score = 0.4
        else:
            freq_score = 0.25
    
    # ── B. Score du ratio livres/films ─────────────────────────────────────
    # Un bon mot littéraire est plus courant dans les livres que dans les films
    if ratio >= 5.0:
        ratio_score = 1.0
    elif ratio >= 3.0:
        ratio_score = 0.9
    elif ratio >= 2.0:
        ratio_score = 0.75
    elif ratio >= 1.2:
        ratio_score = 0.55
    elif ratio >= 0.8:
        ratio_score = 0.35
    else:
        ratio_score = 0.1  # plus oral qu'écrit
    
    # ── C. Score de catégorie Wiktionnaire ────────────────────────────────
    cat_score = 0.0
    cat_labels = []
    for cat in wikt_categories:
        cat_l = cat.lower()
        if "soutenus" in cat_l:
            cat_score = max(cat_score, 1.0)
            cat_labels.append("soutenu")
        elif "littéraires" in cat_l:
            cat_score = max(cat_score, 0.9)
            cat_labels.append("littéraire")
        elif "poétiques" in cat_l:
            cat_score = max(cat_score, 0.8)
            cat_labels.append("poétique")
        elif "archaïques" in cat_l:
            cat_score = max(cat_score, 0.65)
            cat_labels.append("archaïque")
    
    # Bonus pour multiple catégories
    if len(set(cat_labels)) >= 2:
        cat_score = min(1.0, cat_score + 0.1)
    
    # Si aucune catégorie Wiktionnaire (cas rare : mot venu d'une autre source)
    if cat_score == 0.0:
        cat_score = 0.3  # pénalité douce
    
    # ── D. Longueur minimale ───────────────────────────────────────────────
    word_len = len(word)
    if word_len < 4:
        len_factor = 0.3
    elif word_len < 5:
        len_factor = 0.7
    else:
        len_factor = 1.0
    
    # ── E. Bonus de validation (liste de référence) ────────────────────────
    in_reference = normalize(word) in reference_set
    ref_bonus = 1.15 if in_reference else 1.0
    
    # ── Score final ───────────────────────────────────────────────────────
    # Pondérations :
    # - Catégorie Wiktionnaire : poids fort (c'est notre signal principal)
    # - Fréquence : poids moyen-fort (calibrage de difficulté)
    # - Ratio : poids moyen (qualité littéraire vs oral)
    raw_score = (cat_score * 0.45) + (freq_score * 0.35) + (ratio_score * 0.20)
    final_score = raw_score * len_factor * ref_bonus
    final_score = min(1.0, round(final_score, 4))
    
    return {
        "mot": word,
        "score": final_score,
        "freq_score": round(freq_score, 3),
        "ratio_score": round(ratio_score, 3),
        "cat_score": round(cat_score, 3),
        "zipf_books": round(zb, 3),
        "zipf_films": round(zf, 3),
        "ratio": round(ratio, 3),
        "categories_wikt": cat_labels,
        "in_reference": in_reference,
        "has_lexique_data": lex_data is not None,
    }

# ─── 5. ALGORITHME PRINCIPAL ──────────────────────────────────────────────────

def main():
    print("=" * 60)
    print("ALGORITHME DE DÉCOUVERTE DE MOTS LITTÉRAIRES FRANÇAIS")
    print("=" * 60)
    
    # 1. Charger la liste de référence
    reference = load_reference_list()
    reference_set = set(reference.keys())
    
    # 2. Charger Lexique383
    lexique = load_lexique383()
    
    # 3. Récupérer les mots de toutes les catégories Wiktionnaire
    print("\n--- Récupération des catégories Wiktionnaire ---")
    word_to_categories = {}  # {mot: [liste des catégories]}
    
    for cat in WIKT_CATEGORIES:
        words = fetch_wiktionary_category(cat)
        for w in words:
            w_norm = normalize(w)
            if w_norm not in word_to_categories:
                word_to_categories[w_norm] = []
            if cat not in word_to_categories[w_norm]:
                word_to_categories[w_norm].append(cat)
    
    print(f"\n✓ Total de mots uniques issus du Wiktionnaire : {len(word_to_categories)}")
    
    # 4. Scorer chaque mot
    print("\n--- Calcul des scores de pertinence ---")
    scored_words = []
    
    for word, cats in word_to_categories.items():
        # Chercher dans Lexique383 (exact puis sans accents)
        lex_data = lexique.get(word)
        if lex_data is None:
            # Essayer sans accents
            word_no_accent = strip_accents_for_lookup(word)
            lex_data = lexique.get(word_no_accent)
        
        result = score_literary_relevance(word, lex_data, cats, reference_set)
        scored_words.append(result)
    
    # Trier par score décroissant
    scored_words.sort(key=lambda x: (-x["score"], x["mot"]))
    
    # 5. Analyse de la liste de référence
    print("\n--- Analyse de la liste de référence ---")
    
    reference_scores = {}
    for word in reference_set:
        # Chercher dans les mots scorés
        found = next((s for s in scored_words if s["mot"] == word), None)
        if found:
            reference_scores[word] = found["score"]
        else:
            # Mot de référence non trouvé dans Wiktionnaire : scorer quand même avec Lexique
            lex_data = lexique.get(word) or lexique.get(strip_accents_for_lookup(word))
            result = score_literary_relevance(word, lex_data, [], reference_set)
            result["in_reference"] = True
            result["missing_from_wikt"] = True
            reference_scores[word] = result["score"]
            scored_words.append(result)
    
    # Recalculer le tri après ajout des mots de référence manquants
    scored_words.sort(key=lambda x: (-x["score"], x["mot"]))
    
    # 6. Détection des intrus et mots manqués
    print("\n--- Détection des intrus et mots manqués ---")
    
    # Seuil de "bon mot" basé sur la médiane des scores de référence
    ref_score_list = sorted(reference_scores.values(), reverse=True)
    if ref_score_list:
        median_ref_score = ref_score_list[len(ref_score_list) // 2]
        p25_ref_score = ref_score_list[int(len(ref_score_list) * 0.75)]
        p75_ref_score = ref_score_list[int(len(ref_score_list) * 0.25)]
    else:
        median_ref_score = 0.5
        p25_ref_score = 0.3
        p75_ref_score = 0.7
    
    print(f"  Score médian des mots de référence : {median_ref_score:.3f}")
    print(f"  Score P25 (bas) des mots de référence : {p25_ref_score:.3f}")
    print(f"  Score P75 (haut) des mots de référence : {p75_ref_score:.3f}")
    
    # Seuil de filtrage : on utilise P25 - 0.05 pour être légèrement permissif
    threshold_good = max(0.25, p25_ref_score - 0.05)
    threshold_excellent = p75_ref_score
    
    print(f"  Seuil 'bon mot' : {threshold_good:.3f}")
    print(f"  Seuil 'excellent' : {threshold_excellent:.3f}")
    
    # Catégoriser les mots
    excellent_words = [s for s in scored_words if s["score"] >= threshold_excellent]
    good_words = [s for s in scored_words if threshold_good <= s["score"] < threshold_excellent]
    mediocre_words = [s for s in scored_words if s["score"] < threshold_good]
    
    # Intrus dans notre liste de référence (mots de référence avec score < seuil)
    intruders = [w for w, sc in reference_scores.items() if sc < threshold_good]
    
    # Mots de référence manqués dans le Wiktionnaire
    missing_from_wikt = [s for s in scored_words if s.get("missing_from_wikt") and s["in_reference"]]
    
    # Nouveaux candidats excellents (dans Wiktionnaire mais pas encore dans notre liste)
    new_excellent = [s for s in excellent_words if not s["in_reference"]][:100]
    
    # 7. Générer le rapport
    print("\n--- Génération du rapport ---")
    
    with open(OUTPUT_REPORT, "w", encoding="utf-8") as f:
        f.write("# Rapport de Découverte de Mots Littéraires\n\n")
        f.write("**Sources** : Lexique383 (lexique.org) + Wiktionnaire\n")
        f.write(f"**Date** : {time.strftime('%Y-%m-%d %H:%M')}\n\n")
        
        f.write("---\n\n")
        
        # Statistiques globales
        f.write("## 📊 Statistiques Globales\n\n")
        f.write(f"| Métrique | Valeur |\n|---|---|\n")
        f.write(f"| Mots dans notre liste de référence | {len(reference_set)} |\n")
        f.write(f"| Mots trouvés dans les catégories Wiktionnaire | {len(word_to_categories)} |\n")
        f.write(f"| Mots avec données Lexique383 | {sum(1 for s in scored_words if s['has_lexique_data'])} |\n")
        f.write(f"| **Score médian des mots de référence** | **{median_ref_score:.3f}** |\n")
        f.write(f"| Seuil 'bon mot' | {threshold_good:.3f} |\n")
        f.write(f"| Seuil 'excellent' | {threshold_excellent:.3f} |\n\n")
        
        f.write(f"| Catégorie | Nombre |\n|---|---|\n")
        f.write(f"| Mots excellents (≥ {threshold_excellent:.2f}) | {len(excellent_words)} |\n")
        f.write(f"| Mots bons ({threshold_good:.2f}–{threshold_excellent:.2f}) | {len(good_words)} |\n")
        f.write(f"| Mots médiocres (< {threshold_good:.2f}) | {len(mediocre_words)} |\n\n")
        
        # Couverture de la liste de référence
        covered = sum(1 for w in reference_set if w in {s["mot"] for s in scored_words if s["score"] >= threshold_good})
        f.write(f"**Couverture de la liste de référence** par l'algorithme : "
                f"{covered}/{len(reference_set)} mots ({100*covered//max(1,len(reference_set))}%)\n\n")
        
        f.write("---\n\n")
        
        # Intrus dans notre liste de référence
        f.write("## ⚠️ Intrus Potentiels dans Notre Liste de Référence\n\n")
        f.write(f"Ces {len(intruders)} mots de notre liste de référence ont un score **inférieur au seuil** "
                f"({threshold_good:.2f}). Ce sont potentiellement des mots à revoir :\n\n")
        f.write("| Mot | Score | Zipf Livres | Ratio Lit. | Catégories Wikt | Raison |\n")
        f.write("|---|---|---|---|---|---|\n")
        
        for word in sorted(intruders):
            found = next((s for s in scored_words if s["mot"] == word), None)
            if found:
                reasons = []
                if found["zipf_books"] > 4.0:
                    reasons.append(f"trop commun (Zipf={found['zipf_books']:.2f})")
                if found["zipf_books"] < 0.5 and found["zipf_books"] > 0:
                    reasons.append(f"trop rare (Zipf={found['zipf_books']:.2f})")
                if found["ratio"] < 1.0:
                    reasons.append(f"trop oral (ratio={found['ratio']:.2f})")
                if not found["categories_wikt"]:
                    reasons.append("absent du Wiktionnaire")
                reason_str = ", ".join(reasons) if reasons else "score composite faible"
                cats_str = "+".join(found["categories_wikt"]) if found["categories_wikt"] else "—"
                f.write(f"| **{word}** | {found['score']:.3f} | {found['zipf_books']:.2f} | "
                        f"{found['ratio']:.2f} | {cats_str} | {reason_str} |\n")
        
        f.write("\n---\n\n")
        
        # Mots de référence manqués par le Wiktionnaire
        if missing_from_wikt:
            f.write("## 🔍 Mots de Référence Non Trouvés dans les Catégories Wiktionnaire\n\n")
            f.write(f"Ces {len(missing_from_wikt)} mots de notre liste n'apparaissent dans **aucune** des "
                    f"catégories Wiktionnaire interrogées :\n\n")
            f.write("| Mot | Thème | Score Lexique | Raison Probable |\n")
            f.write("|---|---|---|---|\n")
            for s in missing_from_wikt:
                theme = reference.get(s["mot"], "?")
                reason = "Pas de données Lexique" if not s["has_lexique_data"] else f"Zipf={s['zipf_books']:.2f}"
                f.write(f"| {s['mot']} | {theme} | {s['score']:.3f} | {reason} |\n")
            f.write("\n---\n\n")
        
        # Nouveaux candidats excellents
        f.write("## 🌟 Nouveaux Candidats Excellents (non encore dans notre liste)\n\n")
        f.write(f"Ces {len(new_excellent)} mots ont un **score excellent** dans l'algorithme "
                f"et ne sont pas encore dans notre liste de référence. Ce sont des candidats à ajouter :\n\n")
        f.write("| Mot | Score | Zipf Livres | Ratio Lit. | Catégories Wikt | Potentiel |\n")
        f.write("|---|---|---|---|---|---|\n")
        
        for s in new_excellent[:60]:
            cats_str = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
            if s["score"] >= threshold_excellent + 0.1:
                potential = "⭐⭐⭐ Excellent"
            elif s["score"] >= threshold_excellent:
                potential = "⭐⭐ Très bon"
            else:
                potential = "⭐ Bon"
            f.write(f"| **{s['mot']}** | {s['score']:.3f} | {s['zipf_books']:.2f} | "
                    f"{s['ratio']:.2f} | {cats_str} | {potential} |\n")
        
        f.write("\n---\n\n")
        
        # Top 100 tous mots confondus
        f.write("## 🏆 Top 100 des Mots les Plus Pertinents (toutes sources)\n\n")
        f.write("| Rang | Mot | Score | Zipf Livres | Ratio Lit. | Catégories | Dans Ref. |\n")
        f.write("|---|---|---|---|---|---|---|\n")
        
        for i, s in enumerate(scored_words[:100]):
            cats_str = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
            ref_str = "✓" if s["in_reference"] else "—"
            f.write(f"| {i+1} | **{s['mot']}** | {s['score']:.3f} | {s['zipf_books']:.2f} | "
                    f"{s['ratio']:.2f} | {cats_str} | {ref_str} |\n")
        
        f.write("\n---\n\n")
        
        # Analyse des critères de filtrage
        f.write("## 🔬 Analyse des Critères de Filtrage (Calibrage)\n\n")
        f.write("Cette section analyse la distribution des scores pour les mots de référence,\n")
        f.write("afin de comprendre quels critères sont les plus discriminants.\n\n")
        
        ref_found = [s for s in scored_words if s["in_reference"]]
        non_ref_good = [s for s in scored_words if not s["in_reference"] and s["score"] >= threshold_good]
        
        f.write(f"### Mots de référence (N={len(ref_found)})\n\n")
        if ref_found:
            avg_score = sum(s["score"] for s in ref_found) / len(ref_found)
            avg_zb = sum(s["zipf_books"] for s in ref_found if s["zipf_books"] > 0) / max(1, sum(1 for s in ref_found if s["zipf_books"] > 0))
            avg_ratio = sum(s["ratio"] for s in ref_found) / len(ref_found)
            pct_wikt = 100 * sum(1 for s in ref_found if s["categories_wikt"]) / len(ref_found)
            f.write(f"- Score moyen : **{avg_score:.3f}**\n")
            f.write(f"- Zipf Livres moyen (si disponible) : **{avg_zb:.3f}**\n")
            f.write(f"- Ratio Livres/Films moyen : **{avg_ratio:.2f}**\n")
            f.write(f"- % avec catégorie Wiktionnaire : **{pct_wikt:.1f}%**\n\n")
        
        f.write(f"### Nouveaux candidats valides (N={len(non_ref_good)}) - Comparaison\n\n")
        if non_ref_good:
            avg_score2 = sum(s["score"] for s in non_ref_good) / len(non_ref_good)
            avg_zb2 = sum(s["zipf_books"] for s in non_ref_good if s["zipf_books"] > 0) / max(1, sum(1 for s in non_ref_good if s["zipf_books"] > 0))
            avg_ratio2 = sum(s["ratio"] for s in non_ref_good) / len(non_ref_good)
            f.write(f"- Score moyen : **{avg_score2:.3f}**\n")
            f.write(f"- Zipf Livres moyen : **{avg_zb2:.3f}**\n")
            f.write(f"- Ratio Livres/Films moyen : **{avg_ratio2:.2f}**\n\n")
        
        f.write("### Recommandations pour l'algorithme\n\n")
        f.write("Basé sur cette analyse, les critères optimaux sont :\n\n")
        f.write(f"1. **Score minimum** : ≥ {threshold_good:.2f} (calibré sur P25 des mots de référence)\n")
        f.write("2. **Zipf Livres** : entre 1.0 et 4.0 (zone C1/C2 littéraire)\n")
        f.write("3. **Ratio livres/films** : ≥ 1.2 (plus écrit qu'oral)\n")
        f.write("4. **Catégorie Wiktionnaire** : au moins une parmi soutenu/littéraire/poétique/archaïque\n")
        f.write("5. **Longueur** : ≥ 5 caractères (les mots de 3-4 lettres sont rarement pertinents)\n\n")
    
    print(f"✓ Rapport généré : {OUTPUT_REPORT}")
    
    # 8. Sauvegarder la liste de candidats en JSON
    candidates_output = {
        "metadata": {
            "date": time.strftime('%Y-%m-%d %H:%M'),
            "threshold_good": threshold_good,
            "threshold_excellent": threshold_excellent,
            "total_wikt_words": len(word_to_categories),
            "total_reference_words": len(reference_set),
        },
        "excellent_candidates": [s for s in excellent_words if not s["in_reference"]][:150],
        "intruders_in_reference": intruders,
        "missing_from_wiktionnaire": [s["mot"] for s in missing_from_wikt],
        "top_100_all": scored_words[:100],
    }
    
    with open(OUTPUT_CANDIDATES, "w", encoding="utf-8") as f:
        json.dump(candidates_output, f, ensure_ascii=False, indent=2)
    print(f"✓ Candidats JSON : {OUTPUT_CANDIDATES}")
    
    # 9. Sauvegarder le CSV d'analyse complet
    with open(OUTPUT_ANALYSIS, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f, delimiter=";")
        writer.writerow(["mot", "score", "freq_score", "ratio_score", "cat_score",
                         "zipf_books", "zipf_films", "ratio", "categories_wikt",
                         "in_reference", "has_lexique_data"])
        for s in scored_words:
            writer.writerow([
                s["mot"],
                round(s["score"], 4),
                round(s["freq_score"], 3),
                round(s["ratio_score"], 3),
                round(s["cat_score"], 3),
                round(s["zipf_books"], 3),
                round(s["zipf_films"], 3),
                round(s["ratio"], 3),
                "+".join(s["categories_wikt"]),
                "oui" if s["in_reference"] else "non",
                "oui" if s["has_lexique_data"] else "non",
            ])
    print(f"✓ Analyse CSV : {OUTPUT_ANALYSIS}")
    
    # 10. Résumé final
    print("\n" + "=" * 60)
    print("RÉSUMÉ DE L'ANALYSE")
    print("=" * 60)
    print(f"  Mots de référence : {len(reference_set)}")
    print(f"  Mots Wiktionnaire : {len(word_to_categories)}")
    print(f"  Mots excellents   : {len(excellent_words)}")
    print(f"  Nouveaux candidats: {len(new_excellent)}")
    print(f"  Intrus potentiels : {len(intruders)}")
    print(f"  Absents Wikt.     : {len(missing_from_wikt)}")
    print(f"  Couverture réf.   : {covered}/{len(reference_set)} ({100*covered//max(1,len(reference_set))}%)")
    print("=" * 60)
    
    if intruders:
        print(f"\n🔴 {len(intruders)} intrus potentiels dans notre liste de référence :")
        for w in sorted(intruders):
            sc = reference_scores.get(w, 0)
            print(f"   - {w} (score={sc:.3f})")
    
    if new_excellent[:10]:
        print(f"\n🟢 Top 10 nouveaux candidats à ajouter :")
        for s in new_excellent[:10]:
            cats = "+".join(s["categories_wikt"])
            print(f"   + {s['mot']} (score={s['score']:.3f}, Zipf={s['zipf_books']:.2f}, {cats})")
    
    print(f"\n📄 Rapport détaillé : {OUTPUT_REPORT}")
    print(f"📊 CSV d'analyse   : {OUTPUT_ANALYSIS}")
    print(f"📋 Candidats JSON  : {OUTPUT_CANDIDATES}")

if __name__ == "__main__":
    main()
