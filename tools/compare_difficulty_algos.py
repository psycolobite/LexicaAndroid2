import os
import json
import math
import csv

TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))
WORDS_JSON_PATH = os.path.join(TOOLS_DIR, "consolidated_literary_words.json")
PAGEVIEWS_PATH = os.path.join(TOOLS_DIR, "c1_wiktionary_pageviews.json")

OUTPUT_CSV_PATH = os.path.join(
    os.path.dirname(TOOLS_DIR),
    "docs", "amelioration_de_la_fonction_de_recherche",
    "algorithme_de_presentation_des_extraits", "difficulty_comparison.csv"
)

from classify_c1_words import load_lexique
lex = load_lexique()

if os.path.exists(PAGEVIEWS_PATH):
    with open(PAGEVIEWS_PATH, "r", encoding="utf-8") as f:
        pageviews = json.load(f)
else:
    pageviews = {}

# Surcharges manuelles pour buzzwords réels (si absents)
MODERN_BUZZWORDS = {
    "procrastination": 3.6,
    "procrastiner": 3.4,
    "résilience": 3.8,
    "résilient": 3.5
}

def get_old_zipf(word_lower):
    if word_lower in lex:
        fl = lex[word_lower]['freqlivres']
        ff = lex[word_lower]['freqfilms']
        freq = max(fl, ff)
        if freq > 0:
            return math.log10(freq) + 3.0
    return 0.0

def get_new_zipf(word_lower):
    if word_lower in MODERN_BUZZWORDS:
        return MODERN_BUZZWORDS[word_lower]
        
    best_zipf = 0.0
    if word_lower in lex:
        fl = lex[word_lower]['freqlivres']
        ff = lex[word_lower]['freqfilms']
        freq = max(fl, ff)
        if freq > 0:
            best_zipf = math.log10(freq) + 3.0
            
    fallbacks = []
    
    if word_lower.endswith("issement"):
        fallbacks.append(word_lower[:-8] + "ir")
    elif word_lower.endswith("ement") and not word_lower.endswith("issement"):
        fallbacks.append(word_lower[:-5] + "er")
        
    if word_lower.endswith("ation"):
        fallbacks.append(word_lower[:-5] + "er")
        fallbacks.append(word_lower[:-5] + "e")
        fallbacks.append(word_lower[:-5] + "é")
    elif word_lower.endswith("tion") and not word_lower.endswith("ation"):
        fallbacks.append(word_lower[:-4] + "er")
        fallbacks.append(word_lower[:-4] + "e")
        fallbacks.append(word_lower[:-4] + "é")
        
    if word_lower.endswith("ateur"):
        fallbacks.append(word_lower[:-5] + "er")
    elif word_lower.endswith("atrice"):
        fallbacks.append(word_lower[:-6] + "er")
        
    if word_lower.endswith("eur") and not word_lower.endswith("ateur"):
        fallbacks.append(word_lower[:-3] + "er")
        fallbacks.append(word_lower[:-3] + "ure")
        
    if word_lower.endswith("able"):
        if word_lower.endswith("issable"):
            fallbacks.append(word_lower[:-7] + "ir")
        else:
            fallbacks.append(word_lower[:-4] + "er")
            fallbacks.append(word_lower[:-4] + "ir")
            
    if word_lower.endswith("imeux"):
        fallbacks.append(word_lower[:-5] + "in")
            
    if word_lower.endswith("ibilité"):
        fallbacks.append(word_lower[:-7] + "ible")
    elif word_lower.endswith("icité"):
        fallbacks.append(word_lower[:-5] + "ique")
        fallbacks.append(word_lower[:-5] + "ice")
    elif word_lower.endswith("ité"):
        if word_lower.endswith("acité"):
            fallbacks.append(word_lower[:-5] + "ace")
        
    if word_lower.endswith("ique"):
        if word_lower.endswith("istique"):
            fallbacks.append(word_lower[:-7] + "isme")
            
    for fb in fallbacks:
        if fb in lex:
            fl = lex[fb]['freqlivres']
            ff = lex[fb]['freqfilms']
            freq = max(fl, ff)
            if freq > 0:
                fb_zipf = math.log10(freq) + 3.0
                inherited_zipf = fb_zipf - 0.1
                if inherited_zipf > best_zipf:
                    best_zipf = inherited_zipf
                    
    return best_zipf

def calc_old_difficulty(word_lower, zipf):
    if zipf > 0:
        diff_base = (4.3 - zipf) / (4.3 - 1.5)
        diff_base = max(0.0, min(1.0, diff_base))
    else:
        diff_base = 0.75
        
    len_bonus = max(0.0, min(0.15, (len(word_lower) - 5) * 0.02))
    suffix_bonus = 0.0
    if any(word_lower.endswith(s) for s in ['phisme', 'tence', 'ation', 'logie', 'trique', 'phie', 'isme', 'ique', 'iste', 'gence']):
        suffix_bonus = 0.10
    rare_letters_bonus = 0.0
    if any(c in word_lower for c in ['y', 'z', 'k', 'x', 'w']):
        rare_letters_bonus = 0.05
    difficulty = diff_base + len_bonus + suffix_bonus + rare_letters_bonus
    return max(0.0, min(1.0, round(difficulty, 3)))

def calc_new_difficulty(word_lower, zipf, views):
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
        
    # Malus dynamique basé sur les pageviews Wiktionnaire (buzzword)
    # Appliqué uniquement aux mots rares historiquement (Zipf < 2.5) pour éviter de fausser les mots littéraires recherchés
    buzz_malus = 0.0
    if zipf < 2.5 and views > 1000:
        buzz_malus = min(0.25, math.log10(views / 1000.0) * 0.15)
        
    difficulty = diff_base + len_bonus + suffix_bonus - buzz_malus
    return max(0.0, min(1.0, round(difficulty, 3))), buzz_malus

def main():
    with open(WORDS_JSON_PATH, "r", encoding="utf-8") as f:
        words_data = json.load(f)
        
    comparison_list = []
    
    for item in words_data:
        w = item["mot"].lower().strip()
        views = pageviews.get(w, 0)
        
        old_z = get_old_zipf(w)
        new_z = get_new_zipf(w)
        
        old_diff = calc_old_difficulty(w, old_z)
        new_diff, malus = calc_new_difficulty(w, new_z, views)
        
        delta = new_diff - old_diff
        
        reasons = []
        if abs(new_z - old_z) > 0.01:
            reasons.append(f"Repli morphologique (Zipf: {old_z:.2f} -> {new_z:.2f})")
        if malus > 0:
            reasons.append(f"Malus Buzz pageviews: -{malus:.3f} ({views} vues)")
        if any(c in w for c in ['x', 'y', 'z']):
            reasons.append("Retrait bonus lettre rare")
        if len(w) > 5:
            reasons.append("Recalibrage longueur")
        if any(w.endswith(s) for s in ['ation', 'ique', 'iste', 'gence', 'tence']):
            reasons.append("Retrait bonus suffixe standard")
            
        explication = ", ".join(reasons) if reasons else "Ajustement global des bornes Zipf"
        
        comparison_list.append({
            'word': w,
            'old_zipf': old_z,
            'new_zipf': new_z,
            'old_diff': old_diff,
            'new_diff': new_diff,
            'delta': delta,
            'views': views,
            'explication': explication
        })
        
    with open(OUTPUT_CSV_PATH, mode='w', encoding='utf-8', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow(['Mot', 'Pageviews', 'Zipf_Ancien', 'Zipf_Nouveau', 'Difficulte_Ancienne', 'Difficulte_Nouvelle', 'Delta', 'Explication'])
        for item in comparison_list:
            writer.writerow([
                item['word'],
                item['views'],
                f"{item['old_zipf']:.3f}",
                f"{item['new_zipf']:.3f}",
                f"{item['old_diff']:.3f}",
                f"{item['new_diff']:.3f}",
                f"{item['delta']:.3f}",
                item['explication']
            ])
            
    print(f"Comparison CSV created successfully: {OUTPUT_CSV_PATH}")
    
    sorted_delta = sorted(comparison_list, key=lambda x: x['delta'])
    
    print("\n--- TOP 10 LARGEST DECREASES IN DIFFICULTY (EASIER) ---")
    for item in sorted_delta[:10]:
        print(f"  * {item['word']:<18} | {item['old_diff']:.3f} -> {item['new_diff']:.3f} (Delta: {item['delta']:.3f}) | {item['explication']}")
        
    print("\n--- TOP 10 LARGEST INCREASES IN DIFFICULTY (HARDER) ---")
    for item in sorted_delta[-10:]:
        print(f"  * {item['word']:<18} | {item['old_diff']:.3f} -> {item['new_diff']:.3f} (Delta: {item['delta']:.3f}) | {item['explication']}")

if __name__ == "__main__":
    main()
