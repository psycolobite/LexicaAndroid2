#!/usr/bin/env python3
"""
discover_literary_embeddings_max.py
=================================
Algorithme basé sur les embeddings sémantiques sans restriction de longueur minimale.
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
V3_CANDIDATES_JSON = os.path.join(TOOLS_DIR, "literary_candidates_max.json")

OUTPUT_REPORT = os.path.join(TOOLS_DIR, "literary_embedding_max_report.md")
OUTPUT_FINAL_CSV = os.path.join(TOOLS_DIR, "literary_final_combined_max.csv")
OUTPUT_FINAL_JSON = os.path.join(TOOLS_DIR, "literary_final_combined_max.json")

PREFERRED_MODELS = [
    "OrdalieTech/solon-embeddings-large-0.1",
    "paraphrase-multilingual-mpnet-base-v2",
    "paraphrase-multilingual-MiniLM-L12-v2",
]

# Exclusions de mots courants (on commente les mots courts exclus à cause de la longueur pour respecter la suppression de la limite)
EXCLUSIONS = {
    "confondre", "décéder", "décédé", "désolé", "accouder", "accroupir", 
    "ahurir", "assaisonner", "colporter", "fouler", "advenir", "corroder",
    "habiter", "partir", "rester", "tomber", "lever", "porter",
    "diligemment", "périlleusement",
    "ab ovo", "hic et nunc",
    "helvétique", "sylvie", "agathe", "diane", "nippon",
    "acnéique", "balane", "graffito", "socque", "incomestible",
    "discontinuité", "concréter", "déhiscence", "pagure", "anatidé",
    "avide", "gouffre", "guerrier", "dindon", "homicide", "milice",
    "nourrisson", "muraille", "commune", "gouffre", "faction",
    "favori", "prospère", "circonstanciel", "confondre",
    "couronner", "modérateur", "physionomiste", "arrière-neveux",
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
    if not os.path.exists(V3_CANDIDATES_JSON):
        return {"new_excellent": [], "new_good": [], "true_intruders": []}
    with open(V3_CANDIDATES_JSON, "r", encoding="utf-8") as f:
        return json.load(f)

# ─── CHARGEMENT DU MODÈLE D'EMBEDDING ─────────────────────────────────────────

def load_embedding_model():
    from sentence_transformers import SentenceTransformer
    for model_name in PREFERRED_MODELS:
        try:
            print(f"  Chargement du modèle : {model_name}...")
            model = SentenceTransformer(model_name)
            print(f"  [OK] Modèle chargé : {model_name}")
            return model, model_name
        except Exception as e:
            print(f"  [!] Impossible de charger {model_name} : {e}")
            continue
    raise RuntimeError("Aucun modèle d'embedding n'a pu être chargé.")

# ─── CALCUL DES EMBEDDINGS ────────────────────────────────────────────────────

def compute_embeddings_batch(model, words: list, batch_size: int = 128) -> dict:
    print(f"  Calcul des embeddings pour {len(words)} mots...")
    enriched = [f"{w}" for w in words]
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
    vecs = [embeddings[w] for w in reference_words if w in embeddings]
    if not vecs:
        raise ValueError("Aucun embedding trouvé pour les mots de référence !")
    centroid = np.mean(np.array(vecs), axis=0)
    norm = np.linalg.norm(centroid)
    if norm > 0:
        centroid = centroid / norm
    print(f"  [OK] Centroïde calculé sur {len(vecs)}/{len(reference_words)} mots de référence")
    return centroid

def compute_theme_centroids(embeddings: dict, reference: dict) -> dict:
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
    sem_score = max(0.0, min(1.0, semantic_sim))
    freq_score = v3_freq_score
    cat_priorities = {"soutenu": 0.15, "littéraire": 0.12, "poétique": 0.10, "archaïque": 0.07}
    wikt_bonus = max([cat_priorities.get(c, 0) for c in wikt_cats] or [0])
    if len(set(wikt_cats)) >= 2:
        wikt_bonus += 0.03
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
    print("ALGORITHME EMBEDDINGS MAX (SANS SEUIL LONGUEUR)")
    print("=" * 70)
    
    reference = load_reference_list()
    reference_set = set(reference.keys())
    lexique = load_lexique383()
    v3_data = load_v3_candidates()
    
    model, model_name = load_embedding_model()
    
    all_words_to_embed = set(reference_set)
    for item in v3_data.get("new_excellent", []):
        w = item.get("mot", "")
        if w and " " not in w:
            all_words_to_embed.add(w)
    for item in v3_data.get("new_good", []):
        w = item.get("mot", "")
        if w and " " not in w:
            all_words_to_embed.add(w)
            
    print(f"\n[OK] Pool total à embedder : {len(all_words_to_embed)} mots")
    embeddings = compute_embeddings_batch(model, sorted(all_words_to_embed))
    
    literary_centroid = compute_literary_centroid(embeddings, list(reference_set))
    theme_centroids = compute_theme_centroids(embeddings, reference)
    
    reference_sims = {}
    for word in reference_set:
        if word in embeddings:
            sim = cosine_similarity(embeddings[word], literary_centroid)
            reference_sims[word] = sim
            
    ref_sim_values = sorted(reference_sims.values(), reverse=True)
    n = len(ref_sim_values)
    if n > 0:
        p10_sim = ref_sim_values[int(n * 0.90)]
        p25_sim = ref_sim_values[int(n * 0.75)]
        sem_threshold_reject = round(p10_sim - 0.02, 4)
        sem_threshold_good = round(p25_sim, 4)
    else:
        sem_threshold_reject = 0.4
        sem_threshold_good = 0.5
        
    v3_by_word = {}
    for item in v3_data.get("new_excellent", []):
        v3_by_word[item["mot"]] = item
    for item in v3_data.get("new_good", []):
        v3_by_word[item["mot"]] = item
        
    def freq_score_simple(zb: float) -> float:
        if zb == 0: return 0.40
        elif 1.84 <= zb <= 3.5: return 1.0
        elif 3.5 < zb <= 4.0: return 0.85
        elif 4.0 < zb <= 4.3: return 0.70
        elif 4.3 < zb <= 4.7: return 0.40
        elif zb > 4.7: return 0.10
        elif 1.5 <= zb < 1.84: return 0.88
        elif 1.0 <= zb < 1.5: return 0.65
        else: return 0.30
        
    candidates_scored = []
    for word, item in v3_by_word.items():
        if word in EXCLUSIONS or " " in word:
            continue
        if word in embeddings:
            sem_sim = cosine_similarity(embeddings[word], literary_centroid)
        else:
            sem_sim = 0.0
        zb = item.get("zipf", 0.0)
        freq_sc = freq_score_simple(zb)
        cats_raw = item.get("cats", [])
        if isinstance(cats_raw, str):
            cats_raw = [c for c in cats_raw.split("+") if c]
        combined = compute_combined_score(word, sem_sim, freq_sc, lexique.get(word), cats_raw)
        combined["in_reference"] = word in reference_set
        combined["theme_similar"] = find_closest_theme(word, embeddings, theme_centroids)
        candidates_scored.append(combined)
        
    candidates_scored.sort(key=lambda x: (-x["score_final"], -x["score_semantique"], x["mot"]))
    
    ref_combined_scores = []
    for word in reference_set:
        if word in embeddings:
            sem_sim = cosine_similarity(embeddings[word], literary_centroid)
        else:
            sem_sim = 0.0
        lex = lexique.get(word) or lexique.get(strip_accents(word))
        zb = lex["zb"] if lex else 0.0
        freq_sc = freq_score_simple(zb)
        combined = compute_combined_score(word, sem_sim, freq_sc, lex, [])
        combined["theme"] = reference.get(word, "?")
        ref_combined_scores.append(combined)
        
    ref_finals = sorted([s["score_final"] for s in ref_combined_scores])
    combined_threshold_reject = ref_finals[int(len(ref_finals) * 0.10)] if ref_finals else 0.4
    combined_threshold_good = ref_finals[int(len(ref_finals) * 0.25)] if ref_finals else 0.55
    
    excellent_new = [
        c for c in candidates_scored
        if c["score_final"] >= combined_threshold_good
        and c["score_semantique"] >= sem_threshold_good
        and c["zipf_books"] > 0
        and c["zipf_books"] <= 4.3
        and not c["in_reference"]
    ]
    
    with open(OUTPUT_REPORT, "w", encoding="utf-8") as f:
        f.write("# Rapport Embeddings Max\n\n")
        f.write(f"## Nouveaux Candidats Excellents ({len(excellent_new)} mots)\n\n")
        f.write("| Mot | Score Final | Sim. Sém. | Zipf | Catégories | Thème |\n|---|---|---|---|---|---|\n")
        for c in excellent_new[:250]:
            cats_str = "+".join(c["categories_wikt"]) if c["categories_wikt"] else "—"
            f.write(f"| **{c['mot']}** | {c['score_final']:.4f} | {c['score_semantique']:.4f} | "
                    f"{c['zipf_books']:.2f} | {cats_str} | {c.get('theme_similar', '?')} |\n")
            
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
            
    final_json = {
        "metadata": {
            "date": time.strftime('%Y-%m-%d %H:%M'),
            "model": model_name,
            "threshold_combined_good": round(combined_threshold_good, 4),
        },
        "new_excellent_candidates": [
            {"mot": c["mot"], "score_final": c["score_final"],
             "score_semantique": c["score_semantique"],
             "zipf": c["zipf_books"], "ratio": c["ratio"],
             "cats": c["categories_wikt"],
             "theme_similar": c.get("theme_similar", "?")}
            for c in excellent_new
        ],
    }
    with open(OUTPUT_FINAL_JSON, "w", encoding="utf-8") as f:
        json.dump(final_json, f, ensure_ascii=False, indent=2)
        
    print(f"[OK] Embedding Max terminé : {len(excellent_new)} candidats")

def find_closest_theme(word: str, embeddings: dict, theme_centroids: dict) -> str:
    if word not in embeddings: return "?"
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
