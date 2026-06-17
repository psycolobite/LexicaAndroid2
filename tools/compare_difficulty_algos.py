import os
import json
import csv

TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))
WORDS_JSON_PATH = os.path.join(TOOLS_DIR, "consolidated_literary_words.json")

OUTPUT_CSV_PATH = os.path.join(
    os.path.dirname(TOOLS_DIR),
    "docs", "amelioration_de_la_fonction_de_recherche",
    "algorithme_de_presentation_des_extraits", "difficulty_comparison.csv"
)

# Importations des deux versions
import classify_c1_words as v1
import classify_c1_words_v2 as v2

def main():
    print("Chargement des ressources...")
    lex_v1 = v1.load_lexique()
    lex_v2 = v2.load_lexique()
    
    # Pageviews
    PAGEVIEWS_PATH = os.path.join(TOOLS_DIR, "c1_wiktionary_pageviews.json")
    if os.path.exists(PAGEVIEWS_PATH):
        with open(PAGEVIEWS_PATH, "r", encoding="utf-8") as f:
            pageviews = json.load(f)
    else:
        pageviews = {}
        
    desrochers = v2.load_desrochers()
    
    with open(WORDS_JSON_PATH, "r", encoding="utf-8") as f:
        words_data = json.load(f)
        
    comparison_list = []
    
    for item in words_data:
        w = item["mot"]
        theme = item["theme"]
        
        # V1
        res_v1 = v1.classify_word(w, theme, lex_v1, pageviews)
        # V2
        res_v2 = v2.classify_word(w, theme, lex_v2, pageviews, desrochers)
        
        delta_diff = res_v2["difficulty"] - res_v1["difficulty"]
        delta_abs = res_v2["difficulty_abstraction"] - res_v1["difficulty_abstraction"]
        
        comparison_list.append({
            'word': w,
            'views': pageviews.get(w.lower(), 0),
            'diff_v1': res_v1["difficulty"],
            'diff_v2': res_v2["difficulty"],
            'delta_diff': delta_diff,
            'abs_v1': res_v1["difficulty_abstraction"],
            'abs_v2': res_v2["difficulty_abstraction"],
            'delta_abs': delta_abs
        })
        
    with open(OUTPUT_CSV_PATH, mode='w', encoding='utf-8', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow([
            'Mot', 'Pageviews', 
            'Difficulte_V1', 'Difficulte_V2', 'Delta_Difficulte',
            'Abstraction_V1', 'Abstraction_V2', 'Delta_Abstraction'
        ])
        for item in comparison_list:
            writer.writerow([
                item['word'],
                item['views'],
                f"{item['diff_v1']:.3f}",
                f"{item['diff_v2']:.3f}",
                f"{item['delta_diff']:.3f}",
                f"{item['abs_v1']:.3f}",
                f"{item['abs_v2']:.3f}",
                f"{item['delta_abs']:.3f}"
            ])
            
    print(f"Fichier de comparaison CSV cree : {OUTPUT_CSV_PATH}")
    
    # Analyse des deltas
    sorted_diff = sorted(comparison_list, key=lambda x: x['delta_diff'])
    sorted_abs = sorted(comparison_list, key=lambda x: x['delta_abs'])
    
    print("\n--- TOP 10 PLUS GRANDS CHANGEMENTS DE DIFFICULTÉ (V2 vs V1) ---")
    for item in sorted_diff[:5]:
        print(f"  * {item['word']:<18} | V1: {item['diff_v1']:.3f} -> V2: {item['diff_v2']:.3f} (Delta: {item['delta_diff']:.3f}) | Views: {item['views']}")
    for item in sorted_diff[-5:]:
        print(f"  * {item['word']:<18} | V1: {item['diff_v1']:.3f} -> V2: {item['diff_v2']:.3f} (Delta: {item['delta_diff']:.3f}) | Views: {item['views']}")
        
    print("\n--- TOP 10 PLUS GRANDS CHANGEMENTS D'ABSTRACTION (V2 vs V1) ---")
    for item in sorted_abs[:5]:
        print(f"  * {item['word']:<18} | V1: {item['abs_v1']:.3f} -> V2: {item['abs_v2']:.3f} (Delta: {item['delta_abs']:.3f})")
    for item in sorted_abs[-5:]:
        print(f"  * {item['word']:<18} | V1: {item['abs_v1']:.3f} -> V2: {item['abs_v2']:.3f} (Delta: {item['delta_abs']:.3f})")

if __name__ == "__main__":
    main()
