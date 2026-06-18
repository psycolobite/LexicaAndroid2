# -*- coding: utf-8 -*-
import os
import json
import csv
import math

# We can import functions from classify_c1_words_v2
import sys
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from classify_c1_words_v2 import load_lexique, load_desrochers, get_fallback_zipf

# We will define a modified classify_word that accepts a flat_poles boolean
from classify_c1_words_v2 import POLE_MAP, THEMES_SPECIFIC_MAP, DOMAINE_WORDS, REGISTRE_WORDS, EMOTION_WORDS, EPOQUE_WORDS, CONCRETE_OBJECTS

def calculate_abstraction_sim(word_lower, pole, lexique, desrochers, flat_poles=False):
    def clean_accent(w):
        w = w.replace("é", "").replace("è", "").replace("à", "").replace("ù", "")
        w = w.replace("â", "").replace("ê", "").replace("î", "").replace("ô", "").replace("û", "")
        w = w.replace("ç", "")
        return w
    
    clean_w = clean_accent(word_lower)
    
    # 1. Direct match
    target_entry = desrochers.get(word_lower) or desrochers.get(clean_w)
    
    # 2. Lemma
    if not target_entry and word_lower in lexique:
        lemma = lexique[word_lower].get('lemme')
        if lemma:
            clean_lem = clean_accent(lemma)
            target_entry = desrochers.get(lemma) or desrochers.get(clean_lem)
            
    # 3. Fallbacks morphologiques
    if not target_entry:
        morpho_fallbacks = []
        if word_lower.endswith("er") or word_lower.endswith("ir"):
            morpho_fallbacks.append(word_lower[:-2] + "ation")
            morpho_fallbacks.append(word_lower[:-2] + "ement")
        elif word_lower.endswith("eux") or word_lower.endswith("ive") or word_lower.endswith("if"):
            morpho_fallbacks.append(word_lower[:-3] + "ité")
            morpho_fallbacks.append(word_lower[:-3] + "ence")
            
        for mf in morpho_fallbacks:
            clean_mf = clean_accent(mf)
            target_entry = desrochers.get(mf) or desrochers.get(clean_mf)
            if target_entry:
                break
                
    if target_entry and target_entry['image_mean'] > 0:
        imageability = target_entry['image_mean']
        abs_score = (7.0 - imageability) / 6.0
        return max(0.0, min(1.0, round(abs_score, 3)))
        
    # 4. Fallback Heuristique sémantique
    if flat_poles:
        # We decrease the difference: we compress all poles towards a neutral 0.50
        # Instead of 0.20 to 0.85, we compress them by 80% towards 0.50
        # Or let's use a flat 0.50 for all
        base_abs = 0.50
    else:
        if pole == "PHILOSOPHIE_ET_IDEES":
            base_abs = 0.85
        elif pole == "SENTIMENTS_ET_PSYCHE":
            base_abs = 0.75
        elif pole == "ESPRIT_ET_CARACTERE":
            base_abs = 0.60
        elif pole == "ARTS_ET_LANGAGE":
            base_abs = 0.50
        else: # NATURE_ET_COSMOS
            base_abs = 0.20
        
    suffix_boost = 0.0
    if any(word_lower.endswith(s) for s in ['isme', 'logie', 'ence', 'tence', 'té', 'ité', 'tion', 'ance', 'ude']):
        suffix_boost = 0.15
    elif any(word_lower.endswith(s) for s in ['ique', 'iste', 'aire', 'el']):
        suffix_boost = 0.05
        
    concrete_penalty = 0.0
    if word_lower in CONCRETE_OBJECTS:
        concrete_penalty = 0.35
    elif pole == "NATURE_ET_COSMOS" and any(word_lower.endswith(s) for s in ['e', 'a', 'on']):
        concrete_penalty = 0.15
        
    abs_score = base_abs + suffix_boost - concrete_penalty
    return max(0.0, min(1.0, round(abs_score, 3)))

def classify_word_sim(word, theme, lexique, pageviews, desrochers, flat_poles=False):
    word_lower = word.lower().strip()
    
    pole = THEMES_SPECIFIC_MAP.get(word_lower)
    if not pole:
        pole = POLE_MAP.get(theme, "PHILOSOPHIE_ET_IDEES")
        
    domaine = "ROMANESQUE"
    for dom, words in DOMAINE_WORDS.items():
        if word_lower in words:
            domaine = dom
            break
            
    registre = "LITTERAIRE_STANDARD"
    for reg, words in REGISTRE_WORDS.items():
        if word_lower in words:
            registre = reg
            break
            
    emotion = "NEUTRE"
    for emo, words in EMOTION_WORDS.items():
        if word_lower in words:
            emotion = emo
            break
            
    epoque = "ROMANTIQUE_19"
    for ep, words in EPOQUE_WORDS.items():
        if word_lower in words:
            epoque = ep
            break
            
    difficulty_abstraction = calculate_abstraction_sim(word_lower, pole, lexique, desrochers, flat_poles)
    zipf = get_fallback_zipf(word_lower, lexique, pageviews, difficulty_abstraction, registre, epoque)
    
    zipf_max = 3.3
    zipf_min = 1.2
    if zipf >= zipf_max:
        diff_base = 0.0
    elif zipf <= zipf_min:
        diff_base = 1.0
    else:
        diff_base = (zipf_max - zipf) / (zipf_max - zipf_min)
        
    len_bonus = max(0.0, min(0.08, (len(word_lower) - 5) * 0.01))
    
    suffix_bonus = 0.0
    if any(word_lower.endswith(s) for s in ['phisme', 'logie', 'trique', 'phie', 'isme']):
        suffix_bonus = 0.05
        
    buzz_malus = 0.0
    views = pageviews.get(word_lower, 0)
    if zipf < 2.5 and views > 1000:
        buzz_malus = min(0.15, math.log10(views / 1000.0) * 0.10)
        
    difficulty = diff_base + len_bonus + suffix_bonus - buzz_malus
    difficulty = max(0.0, min(1.0, round(difficulty, 3)))
    
    return difficulty, difficulty_abstraction

def get_category(diff):
    if diff < 0.10:
        return "Très facile (Débutant)"
    elif diff < 0.30:
        return "Facile (Intermédiaire)"
    elif diff < 0.50:
        return "Moyen (Avancé)"
    elif diff < 0.75:
        return "Difficile (Expert)"
    else:
        return "Très difficile (Grand Expert)"

def main():
    tools_dir = os.path.dirname(os.path.abspath(__file__))
    json_path = os.path.join(tools_dir, "consolidated_literary_words.json")
    
    with open(json_path, "r", encoding="utf-8") as f:
        words_data = json.load(f)
        
    lexique = load_lexique()
    desrochers = load_desrochers()
    
    PAGEVIEWS_PATH = os.path.join(tools_dir, "c1_wiktionary_pageviews.json")
    with open(PAGEVIEWS_PATH, "r", encoding="utf-8") as f:
        pageviews = json.load(f)
        
    results_curr = []
    results_flat = []
    
    for item in words_data:
        word = item["mot"]
        theme = item["theme"]
        
        diff_c, abs_c = classify_word_sim(word, theme, lexique, pageviews, desrochers, flat_poles=False)
        diff_f, abs_f = classify_word_sim(word, theme, lexique, pageviews, desrochers, flat_poles=True)
        
        results_curr.append((word, diff_c, abs_c))
        results_flat.append((word, diff_f, abs_f))
        
    # Analyze distributions
    def get_stats(lst):
        diffs = [x[1] for x in lst]
        abss = [x[2] for x in lst]
        cats = {}
        for x in diffs:
            cat = get_category(x)
            cats[cat] = cats.get(cat, 0) + 1
        return {
            "avg_diff": sum(diffs) / len(diffs),
            "avg_abs": sum(abss) / len(abss),
            "cats": cats
        }
        
    stats_curr = get_stats(results_curr)
    stats_flat = get_stats(results_flat)
    
    print("=== STATISTIQUES COMPAREES ===")
    print(f"Difficulté moyenne (Actuelle) : {stats_curr['avg_diff']:.3f}")
    print(f"Difficulté moyenne (Pôles plats - 0.50) : {stats_flat['avg_diff']:.3f}")
    print("---")
    print(f"Abstraction moyenne (Actuelle) : {stats_curr['avg_abs']:.3f}")
    print(f"Abstraction moyenne (Pôles plats - 0.50) : {stats_flat['avg_abs']:.3f}")
    print("\n=== DISTRIBUTION DES NIVEAUX DE DIFFICULTE ===")
    categories_ordered = [
        "Très facile (Débutant)",
        "Facile (Intermédiaire)",
        "Moyen (Avancé)",
        "Difficile (Expert)",
        "Très difficile (Grand Expert)"
    ]
    print(f"{'Catégorie':<30} | {'Actuel':<10} | {'Pôles Plats':<10}")
    print("-" * 60)
    for cat in categories_ordered:
        count_c = stats_curr["cats"].get(cat, 0)
        count_f = stats_flat["cats"].get(cat, 0)
        print(f"{cat:<30} | {count_c:<10} | {count_f:<10}")
        
    # Find words with the biggest changes
    changes = []
    for i in range(len(words_data)):
        word = results_curr[i][0]
        diff_c, abs_c = results_curr[i][1], results_curr[i][2]
        diff_f, abs_f = results_flat[i][1], results_flat[i][2]
        if abs_c != abs_f or diff_c != diff_f:
            changes.append((word, abs_c, abs_f, diff_c, diff_f))
            
    print("\n=== TOP DES CHANGEMENTS (EXEMPLES) ===")
    print(f"{'Mot':<20} | {'Abs Act':<7} -> {'Abs Flat':<7} | {'Diff Act':<8} -> {'Diff Flat':<8}")
    print("-" * 65)
    for c in sorted(changes, key=lambda x: abs(x[4] - x[3]), reverse=True)[:15]:
        print(f"{c[0]:<20} | {c[1]:<7.3f} -> {c[2]:<7.3f} | {c[3]:<8.3f} -> {c[4]:<8.3f}")

if __name__ == "__main__":
    main()
