#!/usr/bin/env python3
"""
discover_literary_embeddings.py
=================================
Algorithme basé sur les embeddings sémantiques pour identifier les mots littéraires.

Stratégie :
1. Charger nos 407 mots de référence et calculer leurs embeddings (vecteurs sémantiques)
2. Calculer le "centroïde littéraire" = moyenne de tous ces vecteurs
3. Pour chaque candidat (Wiktionnaire + Lexique), calculer sa similarité cosinus au centroïde
4. Combiner ce score sémantique avec le score V3 (fréquence + catégories)
5. Les faux positifs (confondre, décéder...) seront automatiquement éloignés du centroïde

Modèle : paraphrase-multilingual-MiniLM-L12-v2 (sentence-transformers)
         ~120 MB, multilingue, parfait pour le français
         
Alternative utilisée : dangvantuan/vietnamese-embedding OU
                        OrdalieTech/solon-embeddings-large-0.1 (français spécifique)
                   OU   Alibaba-NLP/gte-multilingual-base
"""

import json
import csv
import os
import time
import math
import zipfile
import io
import unicodedata
import numpy as np
from collections import defaultdict

TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))
REFERENCE_CSV = os.path.join(TOOLS_DIR, "consolidated_literary_words.csv")
LEXIQUE_ZIP = os.path.join(TOOLS_DIR, "Lexique383.zip")
V3_CANDIDATES_JSON = os.path.join(TOOLS_DIR, "literary_candidates_v3.json")

OUTPUT_REPORT = os.path.join(TOOLS_DIR, "literary_embedding_report.md")
OUTPUT_FINAL_CSV = os.path.join(TOOLS_DIR, "literary_final_combined.csv")
OUTPUT_FINAL_JSON = os.path.join(TOOLS_DIR, "literary_final_combined.json")

# ─── CONFIGURATION DU MODÈLE ──────────────────────────────────────────────────
# Modèles candidats (du plus rapide/léger au plus précis) :
# 1. "paraphrase-multilingual-MiniLM-L12-v2"  ~120 MB, 384 dim
# 2. "sentence-transformers/paraphrase-multilingual-mpnet-base-v2"  ~280 MB, 768 dim  
# 3. "OrdalieTech/solon-embeddings-large-0.1"  ~400 MB, 1024 dim (français spécialisé)
# 4. "dangvantuan/vietnamese-embedding"  → pas adapté

# On essaie dans l'ordre : modèle français spécialisé > multilingue
PREFERRED_MODELS = [
    "OrdalieTech/solon-embeddings-large-0.1",  # spécialisé français, très bon pour registre
    "paraphrase-multilingual-mpnet-base-v2",    # multilingue, bonne qualité
    "paraphrase-multilingual-MiniLM-L12-v2",    # léger et rapide
]

# ─── FILTRES ÉDITORIAUX ───────────────────────────────────────────────────────
# Ces mots sont à exclure même s'ils ont un bon score technique
EXCLUSIONS = {
    # Verbes courants parfois marqués "soutenu"
    "confondre", "décéder", "décédé", "désolé", "accouder", "accroupir", 
    "ahurir", "assaisonner", "colporter", "fouler", "advenir", "corroder",
    "habiter", "partir", "rester", "tomber", "lever", "porter",
    # Adverbes banaux
    "diligemment", "périlleusement",
    # Locutions latines (non adaptées à un jeu de vocab)
    "ab ovo", "hic et nunc",
    # Noms propres ou trop spécifiques
    "helvétique", "sylvie", "agathe", "diane", "nippon",
    # Trop techniques / non littéraires
    "acnéique", "balane", "graffito", "socque", "incomestible",
    "discontinuité", "concréter", "déhiscence", "pagure", "anatidé",
    # Mots courants malgré classification "soutenu"
    "avide", "gouffre", "guerrier", "dindon", "homicide", "milice",
    "nourrisson", "muraille", "commune", "gouffre", "faction",
    "favori", "prospère", "circonstanciel", "confondre",
    "couronner", "modérateur", "physionomiste", "arrière-neveux",
    # Mots trop courts
    "ami", "dame", "roi", "sire", "beau",
    # Verbes morphologiquement trop communs
    "aboutissant", "accidenter", "aliénation", "alentour", "amante",
    "amphitryon", "annales", "altitude",
}

# ─── UTILITAIRES ──────────────────────────────────────────────────────────────

def normalize(word: str) -> str:
    return word.strip().lower()

def strip_accents(word: str) -> str:
    nfkd = unicodedata.normalize('NFKD', word)
    return "".join(c for c in nfkd if not unicodedata.combining(c))

def cosine_similarity(a: np.ndarray, b: np.ndarray) -> float:
    norm_a = np.linalg.norm(a)
    norm_b = np.linalg.norm(b)
    if norm_a == 0 or norm_b == 0:
        return 0.0
    return float(np.dot(a, b) / (norm_a * norm_b))

# ─── CHARGEMENT DES DONNÉES ───────────────────────────────────────────────────

def load_reference_list() -> dict:
    reference = {}
    with open(REFERENCE_CSV, "r", encoding="utf-8-sig") as f:
        for row in csv.DictReader(f, delimiter=";"):
            mot = normalize(row.get("mot", ""))
            theme = row.get("theme", "").strip()
            if mot:
                reference[mot] = theme
    return reference

def load_lexique383() -> dict:
    lexique = {}
    try:
        with zipfile.ZipFile(LEXIQUE_ZIP, "r") as zf:
            tsv_files = [n for n in zf.namelist() if n.endswith(".tsv")]
            if not tsv_files:
                return {}
            with zf.open(tsv_files[0]) as f:
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
                    ratio = (fb / ff) if ff > 0 else (10.0 if fb > 0 else 0.0)
                    if lemme not in lexique or fb > lexique[lemme]["fb"]:
                        lexique[lemme] = {
                            "zb": round(zb, 3),
                            "ratio": round(min(ratio, 50.0), 3),
                            "fb": fb,
                        }
    except Exception as e:
        print(f"[!] Erreur Lexique: {e}")
    return lexique

def load_v3_candidates() -> dict:
    """Charge les résultats de l'algorithme V3 (scores fréquence + catégories Wiktionnaire)."""
    if not os.path.exists(V3_CANDIDATES_JSON):
        return {"new_excellent": [], "new_good": [], "true_intruders": []}
    with open(V3_CANDIDATES_JSON, "r", encoding="utf-8") as f:
        return json.load(f)

# ─── CHARGEMENT DU MODÈLE D'EMBEDDING ─────────────────────────────────────────

def load_embedding_model():
    """
    Charge le meilleur modèle disponible.
    Essaie dans l'ordre les modèles préférés.
    """
    from sentence_transformers import SentenceTransformer
    
    for model_name in PREFERRED_MODELS:
        try:
            print(f"  Chargement du modèle : {model_name}...")
            model = SentenceTransformer(model_name)
            print(f"  [OK] Modèle chargé : {model_name}")
            print(f"  Dimension des vecteurs : {model.get_sentence_embedding_dimension()}")
            return model, model_name
        except Exception as e:
            print(f"  [!] Impossible de charger {model_name} : {e}")
            continue
    
    raise RuntimeError("Aucun modèle d'embedding n'a pu être chargé.")

# ─── CALCUL DES EMBEDDINGS ────────────────────────────────────────────────────

def compute_embeddings_batch(model, words: list, batch_size: int = 128) -> dict:
    """
    Calcule les embeddings pour une liste de mots.
    
    Pour les mots littéraires, on enrichit la requête avec un contexte :
    "Le mot littéraire [mot] signifie..."
    Cela aide le modèle à mieux différencier les registres.
    """
    print(f"  Calcul des embeddings pour {len(words)} mots...")
    
    # Enrichissement contextuel pour guider le modèle vers le registre littéraire
    # Un mot littéraire ≠ un mot courant même si c'est le même lemme
    enriched = [f"{w}" for w in words]  # simple : juste le mot
    
    embeddings = {}
    for i in range(0, len(words), batch_size):
        batch_words = words[i:i+batch_size]
        batch_texts = enriched[i:i+batch_size]
        
        try:
            vecs = model.encode(batch_texts, show_progress_bar=False, normalize_embeddings=True)
            for word, vec in zip(batch_words, vecs):
                embeddings[word] = vec
        except Exception as e:
            print(f"  [!] Erreur batch {i}: {e}")
    
    print(f"  [OK] {len(embeddings)} embeddings calculés")
    return embeddings

def compute_literary_centroid(embeddings: dict, reference_words: list) -> np.ndarray:
    """
    Calcule le centroïde sémantique du vocabulaire littéraire.
    = Moyenne des embeddings de tous les mots de référence.
    """
    vecs = [embeddings[w] for w in reference_words if w in embeddings]
    if not vecs:
        raise ValueError("Aucun embedding trouvé pour les mots de référence !")
    centroid = np.mean(np.array(vecs), axis=0)
    norm = np.linalg.norm(centroid)
    if norm > 0:
        centroid = centroid / norm  # normaliser
    print(f"  [OK] Centroïde calculé sur {len(vecs)}/{len(reference_words)} mots de référence")
    return centroid

def compute_theme_centroids(embeddings: dict, reference: dict) -> dict:
    """
    Calcule un centroïde par thème de notre liste de référence.
    Permet d'identifier à quel thème un nouveau mot appartient.
    """
    theme_vecs = defaultdict(list)
    for word, theme in reference.items():
        if word in embeddings:
            theme_vecs[theme].append(embeddings[word])
    
    centroids = {}
    for theme, vecs in theme_vecs.items():
        if vecs:
            centroid = np.mean(np.array(vecs), axis=0)
            norm = np.linalg.norm(centroid)
            centroids[theme] = centroid / norm if norm > 0 else centroid
    
    return centroids

# ─── SCORE COMBINÉ ────────────────────────────────────────────────────────────

def compute_combined_score(
    word: str,
    semantic_sim: float,
    v3_freq_score: float,
    lex_data: dict | None,
    wikt_cats: list,
) -> dict:
    """
    Combine le score sémantique (embedding) avec le score V3 (fréquence + Wiktionnaire).
    
    Pondération :
    - Score sémantique (embedding) : 50% — discrimine les faux positifs
    - Score de fréquence V3        : 35% — calibrage de difficulté  
    - Bonus Wiktionnaire           : 15% — signal de registre
    
    Le score sémantique est la clé : un mot littéraire aura une sim cosinus élevée
    avec notre centroïde, alors qu'un mot courant comme "confondre" sera faible.
    """
    # Normaliser la similarité cosinus [0, 1] → elle est déjà dans [-1, 1]
    # Pour des mots en français, elle sera généralement dans [0.1, 0.9]
    sem_score = max(0.0, min(1.0, semantic_sim))
    
    # Score de fréquence
    freq_score = v3_freq_score
    
    # Bonus Wiktionnaire
    cat_priorities = {"soutenu": 0.15, "littéraire": 0.12, "poétique": 0.10, "archaïque": 0.07}
    wikt_bonus = max([cat_priorities.get(c, 0) for c in wikt_cats] or [0])
    if len(set(wikt_cats)) >= 2:
        wikt_bonus += 0.03
    
    # Score combiné
    combined = (sem_score * 0.50) + (freq_score * 0.35) + (wikt_bonus * 0.15 / 0.15)
    # Correction : wikt_bonus est déjà une valeur absolue, pas proportionnelle
    combined = (sem_score * 0.50) + (freq_score * 0.35) + min(wikt_bonus, 0.15)
    
    return {
        "mot": word,
        "score_final": round(min(1.0, combined), 4),
        "score_semantique": round(sem_score, 4),
        "score_frequence": round(freq_score, 4),
        "wikt_bonus": round(wikt_bonus, 4),
        "zipf_books": round(lex_data["zb"] if lex_data else 0, 3),
        "ratio": round(lex_data["ratio"] if lex_data else 0, 3),
        "categories_wikt": wikt_cats,
    }

# ─── ALGORITHME PRINCIPAL ─────────────────────────────────────────────────────

def main():
    print("=" * 70)
    print("ALGORITHME EMBEDDINGS - DÉCOUVERTE DE MOTS LITTÉRAIRES FRANÇAIS")
    print("=" * 70)
    
    # 1. Chargements
    reference = load_reference_list()
    reference_set = set(reference.keys())
    print(f"[OK] Référence : {len(reference_set)} mots")
    
    lexique = load_lexique383()
    print(f"[OK] Lexique383 : {len(lexique)} lemmes")
    
    v3_data = load_v3_candidates()
    threshold_meta = v3_data.get("metadata", {})
    print(f"[OK] Données V3 chargées")
    
    # 2. Charger le modèle
    print("\n--- Chargement du modèle d'embedding ---")
    model, model_name = load_embedding_model()
    
    # 3. Préparer le pool de mots à embedder
    # = mots de référence + candidats V3 + tous les mots Wiktionnaire déjà connus
    all_words_to_embed = set(reference_set)
    
    # Ajouter les candidats V3
    for item in v3_data.get("new_excellent", []):
        w = item.get("mot", "")
        if w and " " not in w and len(w) >= 4:
            all_words_to_embed.add(w)
    for item in v3_data.get("new_good", []):
        w = item.get("mot", "")
        if w and " " not in w and len(w) >= 4:
            all_words_to_embed.add(w)
    
    print(f"\n[OK] Pool total à embedder : {len(all_words_to_embed)} mots")
    
    # 4. Calculer les embeddings
    print("\n--- Calcul des embeddings ---")
    t0 = time.time()
    embeddings = compute_embeddings_batch(model, sorted(all_words_to_embed))
    print(f"  Temps : {time.time()-t0:.1f}s")
    
    # Sauvegarder les embeddings (pour ne pas recalculer à chaque run)
    embeddings_cache_path = os.path.join(TOOLS_DIR, "literary_embeddings_cache.npz")
    np.savez_compressed(
        embeddings_cache_path,
        words=np.array(sorted(embeddings.keys())),
        vectors=np.array([embeddings[w] for w in sorted(embeddings.keys())])
    )
    print(f"  [OK] Cache embeddings : {embeddings_cache_path}")
    
    # 5. Calculer le centroïde littéraire
    print("\n--- Calcul du centroïde littéraire ---")
    literary_centroid = compute_literary_centroid(embeddings, list(reference_set))
    
    # Centroïdes par thème
    theme_centroids = compute_theme_centroids(embeddings, reference)
    print(f"  [OK] {len(theme_centroids)} centroïdes thématiques")
    
    # 6. Calculer la similarité sémantique de chaque mot de référence
    # (permet de calibrer les seuils)
    print("\n--- Calibration sur les mots de référence ---")
    reference_sims = {}
    for word in reference_set:
        if word in embeddings:
            sim = cosine_similarity(embeddings[word], literary_centroid)
            reference_sims[word] = sim
    
    ref_sim_values = sorted(reference_sims.values(), reverse=True)
    n = len(ref_sim_values)
    
    if n > 0:
        avg_sim = sum(ref_sim_values) / n
        median_sim = ref_sim_values[n // 2]
        p10_sim = ref_sim_values[int(n * 0.90)]  # 10% les plus faibles
        p25_sim = ref_sim_values[int(n * 0.75)]  # 25% les plus faibles
        
        print(f"  Similarité moyenne (réf.) : {avg_sim:.4f}")
        print(f"  Similarité médiane (réf.)  : {median_sim:.4f}")
        print(f"  P10 (seuil de rejet)       : {p10_sim:.4f}")
        print(f"  P25 (seuil 'bon')          : {p25_sim:.4f}")
        
        # Seuil sémantique : P10 des mots de référence
        sem_threshold_reject = round(p10_sim - 0.02, 4)
        sem_threshold_good = round(p25_sim, 4)
    else:
        avg_sim = median_sim = p10_sim = p25_sim = 0.5
        sem_threshold_reject = 0.4
        sem_threshold_good = 0.5
    
    print(f"  Seuil rejet sémantique     : {sem_threshold_reject:.4f}")
    print(f"  Seuil bon sémantique       : {sem_threshold_good:.4f}")
    
    # 7. Identifier les mots de référence éloignés du centroïde (potentiels intrus)
    semantic_intruders = [
        (word, sim) for word, sim in reference_sims.items()
        if sim < sem_threshold_reject
    ]
    semantic_intruders.sort(key=lambda x: x[1])
    
    print(f"\n  Mots de référence < seuil sémantique : {len(semantic_intruders)}")
    for word, sim in semantic_intruders[:10]:
        print(f"    - {word:<22} (sim={sim:.4f}, thème={reference.get(word,'?')})")
    
    # 8. Scorer tous les candidats V3 avec le score sémantique
    print("\n--- Scoring des candidats V3 ---")
    
    # Préparer les données V3 par mot
    v3_by_word = {}
    for item in v3_data.get("new_excellent", []):
        v3_by_word[item["mot"]] = item
    for item in v3_data.get("new_good", []):
        v3_by_word[item["mot"]] = item
    
    # Construire la liste des catégories Wiktionnaire depuis le fichier CSV
    wikt_cats_by_word = {}
    analysis_csv = os.path.join(TOOLS_DIR, "literary_discovery_analysis.csv")
    if os.path.exists(analysis_csv):
        with open(analysis_csv, "r", encoding="utf-8-sig") as f:
            for row in csv.DictReader(f, delimiter=";"):
                mot = row.get("mot", "")
                cats_str = row.get("categories_wikt", "")
                if mot and cats_str:
                    wikt_cats_by_word[mot] = [c for c in cats_str.split("+") if c]
    
    # Score V3 pour chaque candidat
    # On recalcule le score de fréquence simplifié
    def freq_score_simple(zb: float) -> float:
        if zb == 0:
            return 0.40
        elif 1.84 <= zb <= 3.5:
            return 1.0
        elif 3.5 < zb <= 4.0:
            return 0.85
        elif 4.0 < zb <= 4.3:
            return 0.70
        elif 4.3 < zb <= 4.7:
            return 0.40
        elif zb > 4.7:
            return 0.10
        elif 1.5 <= zb < 1.84:
            return 0.88
        elif 1.0 <= zb < 1.5:
            return 0.65
        else:
            return 0.30
    
    candidates_scored = []
    for word, item in v3_by_word.items():
        if word in EXCLUSIONS or " " in word or len(word) < 5:
            continue
        
        # Score sémantique
        if word in embeddings:
            sem_sim = cosine_similarity(embeddings[word], literary_centroid)
        else:
            sem_sim = 0.0
        
        # Score de fréquence
        zb = item.get("zipf", 0.0)
        freq_sc = freq_score_simple(zb)
        
        # Catégories Wiktionnaire
        cats_raw = item.get("cats", [])
        if isinstance(cats_raw, str):
            cats_raw = [c for c in cats_raw.split("+") if c]
        
        combined = compute_combined_score(word, sem_sim, freq_sc, lexique.get(word), cats_raw)
        combined["in_reference"] = word in reference_set
        combined["theme_similar"] = find_closest_theme(word, embeddings, theme_centroids)
        candidates_scored.append(combined)
    
    # Trier par score final
    candidates_scored.sort(key=lambda x: (-x["score_final"], -x["score_semantique"], x["mot"]))
    
    # 9. Analyser les mots de référence avec le score combiné
    print("\n--- Analyse des mots de référence ---")
    ref_combined_scores = []
    for word in reference_set:
        if word in embeddings:
            sem_sim = cosine_similarity(embeddings[word], literary_centroid)
        else:
            sem_sim = 0.0
        
        lex = lexique.get(word) or lexique.get(strip_accents(word))
        zb = lex["zb"] if lex else 0.0
        freq_sc = freq_score_simple(zb)
        cats = wikt_cats_by_word.get(word, [])
        
        combined = compute_combined_score(word, sem_sim, freq_sc, lex, cats)
        combined["theme"] = reference.get(word, "?")
        ref_combined_scores.append(combined)
    
    ref_combined_scores.sort(key=lambda x: x["score_final"])
    
    # Seuil basé sur P10 des mots de référence (score combiné)
    ref_finals = [s["score_final"] for s in ref_combined_scores]
    combined_threshold_reject = ref_finals[int(len(ref_finals) * 0.10)] if ref_finals else 0.4
    combined_threshold_good = ref_finals[int(len(ref_finals) * 0.25)] if ref_finals else 0.55
    
    print(f"  Score final P10 (rejet) : {combined_threshold_reject:.4f}")
    print(f"  Score final P25 (bon)   : {combined_threshold_good:.4f}")
    
    # 10. Filtrage final des nouveaux candidats
    excellent_new = [
        c for c in candidates_scored
        if c["score_final"] >= combined_threshold_good
        and c["score_semantique"] >= sem_threshold_good
        and c["zipf_books"] > 0
        and c["zipf_books"] <= 4.3
        and not c["in_reference"]
    ]
    
    # 11. Rapport
    print("\n--- Génération du rapport ---")
    
    ref_intruders_combined = [
        s for s in ref_combined_scores
        if s["score_final"] < combined_threshold_reject
    ]
    
    with open(OUTPUT_REPORT, "w", encoding="utf-8") as f:
        f.write("# Rapport Embeddings - Découverte de Mots Littéraires\n\n")
        f.write(f"**Modèle** : `{model_name}`\n")
        f.write(f"**Date** : {time.strftime('%Y-%m-%d %H:%M')}\n\n")
        f.write("## Principe\n\n")
        f.write("Le score sémantique est calculé par **similarité cosinus** entre l'embedding\n")
        f.write("du mot et le **centroïde littéraire** (moyenne des 407 mots de référence).\n\n")
        f.write("Un mot littéraire sera proche du centroïde ; un mot courant (confondre,\n")
        f.write("décéder, guerrier...) sera éloigné même s'il est classé 'soutenu' par Wiktionnaire.\n\n")
        f.write("---\n\n")
        
        f.write("## Statistiques\n\n")
        f.write(f"| Métrique | Valeur |\n|---|---|\n")
        f.write(f"| Mots de référence | {len(reference_set)} |\n")
        f.write(f"| Mots embeddés | {len(embeddings)} |\n")
        f.write(f"| Sim. cosinus moyenne (réf.) | {avg_sim:.4f} |\n")
        f.write(f"| Sim. cosinus médiane (réf.) | {median_sim:.4f} |\n")
        f.write(f"| Seuil sémantique rejet | {sem_threshold_reject:.4f} |\n")
        f.write(f"| Seuil sémantique bon | {sem_threshold_good:.4f} |\n\n")
        
        f.write("---\n\n")
        
        # Intrus sémantiques
        f.write(f"## Intrus Sémantiques ({len(semantic_intruders)} mots)\n\n")
        f.write("Ces mots de notre liste sont **sémantiquement éloignés** du centroïde :\n\n")
        f.write("| Mot | Thème | Sim. Cosinus | Score Combiné |\n|---|---|---|---|\n")
        for word, sim in semantic_intruders:
            theme = reference.get(word, "?")
            ref_sc = next((s["score_final"] for s in ref_combined_scores if s["mot"] == word), 0)
            f.write(f"| **{word}** | {theme} | {sim:.4f} | {ref_sc:.4f} |\n")
        
        f.write("\n---\n\n")
        
        # Mots de référence avec intrus combinés
        combined_intruders = [s for s in ref_combined_scores if s["score_final"] < combined_threshold_reject]
        if combined_intruders:
            f.write(f"## Intrus Score Combiné ({len(combined_intruders)} mots)\n\n")
            f.write("| Mot | Thème | Score Final | Sim. Sém. | Freq. | Catégories |\n")
            f.write("|---|---|---|---|---|---|\n")
            for s in combined_intruders:
                cats_str = "+".join(s["categories_wikt"]) if s["categories_wikt"] else "—"
                f.write(f"| **{s['mot']}** | {s.get('theme', '?')} | {s['score_final']:.4f} | "
                        f"{s['score_semantique']:.4f} | {s['zipf_books']:.2f} | {cats_str} |\n")
            f.write("\n---\n\n")
        
        # Nouveaux candidats excellents
        f.write(f"## Nouveaux Candidats Excellents ({len(excellent_new)} mots)\n\n")
        f.write("Filtrés par score sémantique >= P25 référence ET score fréquence valide :\n\n")
        f.write("| Mot | Score Final | Sim. Sém. | Zipf | Ratio | Catégories | Thème Similaire |\n")
        f.write("|---|---|---|---|---|---|---|\n")
        for c in excellent_new[:80]:
            cats_str = "+".join(c["categories_wikt"]) if c["categories_wikt"] else "—"
            f.write(f"| **{c['mot']}** | {c['score_final']:.4f} | {c['score_semantique']:.4f} | "
                    f"{c['zipf_books']:.2f} | {c['ratio']:.2f} | {cats_str} | "
                    f"{c.get('theme_similar', '?')} |\n")
        
        f.write("\n---\n\n")
        
        # Top 30 tous mots confondus (référence + nouveaux)
        all_scored = candidates_scored + [
            {"mot": s["mot"], "score_final": s["score_final"],
             "score_semantique": s["score_semantique"],
             "zipf_books": s["zipf_books"], "ratio": s["ratio"],
             "categories_wikt": s["categories_wikt"],
             "in_reference": True, "theme_similar": reference.get(s["mot"], "?")}
            for s in ref_combined_scores
        ]
        all_scored.sort(key=lambda x: (-x["score_final"], x["mot"]))
        
        f.write("## Top 50 Mots Tous Confondus (Référence + Nouveaux)\n\n")
        f.write("| Rang | Mot | Score | Sim. Sém. | Zipf | Catégories | Réf. |\n")
        f.write("|---|---|---|---|---|---|---|\n")
        for i, c in enumerate([x for x in all_scored if not x.get("excluded")][:50]):
            cats_str = "+".join(c.get("categories_wikt", [])) if c.get("categories_wikt") else "—"
            ref_str = "✓" if c.get("in_reference") else "—"
            f.write(f"| {i+1} | **{c['mot']}** | {c['score_final']:.4f} | "
                    f"{c['score_semantique']:.4f} | {c['zipf_books']:.2f} | {cats_str} | {ref_str} |\n")
    
    print(f"[OK] Rapport : {OUTPUT_REPORT}")
    
    # 12. CSV final
    with open(OUTPUT_FINAL_CSV, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f, delimiter=";")
        writer.writerow(["mot", "score_final", "score_semantique", "score_frequence",
                         "zipf_books", "ratio", "categories_wikt", "theme_similar",
                         "in_reference", "classe"])
        for c in excellent_new:
            classe = "Excellent" if c["score_final"] >= combined_threshold_good else "Bon"
            writer.writerow([
                c["mot"], round(c["score_final"], 4),
                round(c["score_semantique"], 4),
                round(c["score_frequence"], 4),
                round(c["zipf_books"], 3),
                round(c["ratio"], 3),
                "+".join(c["categories_wikt"]),
                c.get("theme_similar", "?"),
                "non",
                classe,
            ])
    print(f"[OK] CSV final : {OUTPUT_FINAL_CSV}")
    
    # 13. JSON final
    final_json = {
        "metadata": {
            "date": time.strftime('%Y-%m-%d %H:%M'),
            "model": model_name,
            "literary_centroid_sim_mean": round(avg_sim, 4),
            "threshold_semantic_reject": sem_threshold_reject,
            "threshold_semantic_good": sem_threshold_good,
            "threshold_combined_reject": round(combined_threshold_reject, 4),
            "threshold_combined_good": round(combined_threshold_good, 4),
        },
        "semantic_intruders": [
            {"mot": w, "sim_cosinus": round(sim, 4), "theme": reference.get(w, "?")}
            for w, sim in semantic_intruders
        ],
        "combined_intruders": [
            {"mot": s["mot"], "score_final": s["score_final"],
             "score_semantique": s["score_semantique"], "theme": s.get("theme", "?")}
            for s in ref_intruders_combined
        ],
        "new_excellent_candidates": [
            {"mot": c["mot"], "score_final": c["score_final"],
             "score_semantique": c["score_semantique"],
             "zipf": c["zipf_books"], "ratio": c["ratio"],
             "cats": c["categories_wikt"],
             "theme_similar": c.get("theme_similar", "?")}
            for c in excellent_new[:200]
        ],
    }
    with open(OUTPUT_FINAL_JSON, "w", encoding="utf-8") as f:
        json.dump(final_json, f, ensure_ascii=False, indent=2)
    print(f"[OK] JSON final : {OUTPUT_FINAL_JSON}")
    
    # 14. Résumé
    print("\n" + "=" * 70)
    print("RÉSUMÉ EMBEDDINGS")
    print("=" * 70)
    print(f"  Modèle utilisé       : {model_name}")
    print(f"  Sim. cosinus moyenne : {avg_sim:.4f}")
    print(f"  Intrus sémantiques   : {len(semantic_intruders)}")
    print(f"  Nouveaux candidats   : {len(excellent_new)}")
    print("=" * 70)
    
    print(f"\n[!] Intrus sémantiques (les plus éloignés du centroïde) :")
    for word, sim in semantic_intruders[:15]:
        theme = reference.get(word, "?")
        print(f"   - {word:<22} (sim={sim:.4f}, thème={theme})")
    
    print(f"\n[+] Top 20 nouveaux candidats (score embedding + fréquence) :")
    for c in excellent_new[:20]:
        cats = "+".join(c["categories_wikt"]) if c["categories_wikt"] else "—"
        print(f"   + {c['mot']:<22} score={c['score_final']:.4f} (sem={c['score_semantique']:.3f},"
              f" Zipf={c['zipf_books']:.2f}, [{cats}])")
    
    return excellent_new, semantic_intruders

def find_closest_theme(word: str, embeddings: dict, theme_centroids: dict) -> str:
    """Trouve le thème de référence le plus proche d'un mot."""
    if word not in embeddings:
        return "?"
    
    best_theme = "?"
    best_sim = -1.0
    
    for theme, centroid in theme_centroids.items():
        sim = cosine_similarity(embeddings[word], centroid)
        if sim > best_sim:
            best_sim = sim
            best_theme = theme
    
    return best_theme

if __name__ == "__main__":
    main()
