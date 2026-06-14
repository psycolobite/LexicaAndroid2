import json
import os

INPUT_JSON = "wiktionary_literary_words.json"
OUTPUT_REPORT = "c1_relevance_report.md"

def calculate_relevance(item):
    zb = item.get("zipf_books", 0.0)
    zf = item.get("zipf_films", 0.0)
    ratio = item.get("ratio", 1.0)
    cats = item.get("categories_wikt", [])
    
    # 1. Frequency suitability for C1 (should not be too common, nor completely obscure)
    # Ideal zipf_books for C1: between 1.5 and 3.2
    if 1.5 <= zb <= 3.2:
        freq_score = 1.0
    elif 3.2 < zb <= 3.6:
        # linear decrease from 1.0 to 0.5
        freq_score = 1.0 - (zb - 3.2) * 1.25
    elif zb > 3.6:
        # linear decrease from 0.5 to 0.0
        freq_score = max(0.0, 0.5 - (zb - 3.6) * 0.5)
    elif 1.0 <= zb < 1.5:
        # slightly rare but good for advanced/expert
        freq_score = 0.8
    elif 0.0 < zb < 1.0:
        freq_score = 0.4
    else:  # zb == 0.0
        # Check if zipf_films has something
        if zf > 0.0:
            if 1.5 <= zf <= 3.2:
                freq_score = 0.6
            else:
                freq_score = 0.3
        else:
            freq_score = 0.1 # No frequency info in either
            
    # 2. Literary Register suitability (ratio of book frequency to film frequency)
    # Higher ratio is better. If ratio is low (< 1.5), it is too colloquial.
    if ratio >= 4.0:
        ratio_score = 1.0
    elif ratio >= 2.0:
        ratio_score = 0.8
    elif ratio >= 1.0:
        ratio_score = 0.5
    else:
        ratio_score = 0.1 # More oral than written
        
    # 3. Category weight
    # Soutenus, Littéraires, and Poétiques are highly relevant.
    # Archaïsmes are also good but sometimes obsolete.
    cat_weight = 0.0
    for cat in cats:
        if "soutenus" in cat.lower():
            cat_weight += 1.0
        elif "littéraires" in cat.lower():
            cat_weight += 0.9
        elif "poétiques" in cat.lower():
            cat_weight += 0.8
        elif "archaïques" in cat.lower():
            cat_weight += 0.6
            
    # Normalize cat_weight (max weight for one cat is 1.0, multiple cats can give a bonus)
    cat_score = min(1.3, cat_weight if cat_weight > 0 else 0.5)
    
    # Final C1 relevance score: combination of frequency suitability, literary ratio and categories
    relevance_score = freq_score * ratio_score * cat_score
    
    # Penalize words that are extremely short (less than 3 letters) as they are rarely good test words
    if len(item.get("mot", "")) < 3:
        relevance_score *= 0.5
        
    return relevance_score

def main():
    if not os.path.exists(INPUT_JSON):
        print(f"Error: {INPUT_JSON} not found.")
        return
        
    with open(INPUT_JSON, "r", encoding="utf-8") as f:
        data = json.load(f)
        
    scored_words = []
    for item in data:
        score = calculate_relevance(item)
        scored_words.append((score, item))
        
    # Sort by score descending
    scored_words.sort(key=lambda x: (-x[0], x[1]["mot"]))
    
    # Write report
    with open(OUTPUT_REPORT, "w", encoding="utf-8") as f:
        f.write("# Rapport de Pertinence C1 (Vocabulaire Littéraire)\n\n")
        f.write("Ce rapport évalue la pertinence de chaque mot de notre extraction Wiktionnaire/Lexique vis-à-vis d'une catégorie de jeu **C1 (Vocabulaire Littéraire)**.\n\n")
        
        f.write("### 📊 Distribution des Mots par Niveau de Pertinence C1\n")
        
        # Count ranges
        ranges = {
            "Excellente pertinence (Score >= 0.8)": 0,
            "Bonne pertinence (0.5 <= Score < 0.8)": 0,
            "Pertinence moyenne (0.2 <= Score < 0.5)": 0,
            "Faible pertinence (Score < 0.2)": 0
        }
        
        for score, item in scored_words:
            if score >= 0.8:
                ranges["Excellente pertinence (Score >= 0.8)"] += 1
            elif score >= 0.5:
                ranges["Bonne pertinence (0.5 <= Score < 0.8)"] += 1
            elif score >= 0.2:
                ranges["Pertinence moyenne (0.2 <= Score < 0.5)"] += 1
            else:
                ranges["Faible pertinence (Score < 0.2)"] += 1
                
        for r, count in ranges.items():
            f.write(f"- **{r}** : {count} mots\n")
            
        f.write("\n### 🌟 Top 50 des mots les plus pertinents pour le niveau C1\n")
        f.write("| Rang | Mot | Pertinence | Catégorie | Zipf Livres | Zipf Films | Ratio Lit. | Catégories Wiktionnaire |\n")
        f.write("|---|---|---|---|---|---|---|---|\n")
        
        for idx, (score, item) in enumerate(scored_words[:50]):
            cats = ", ".join([c.replace(" en français", "") for c in item.get("categories_wikt", [])])
            # Categorize the word itself
            zb = item.get("zipf_books", 0.0)
            if zb >= 3.0:
                diff = "Débutant"
            elif zb >= 1.5:
                diff = "Intermédiaire"
            elif zb > 0.0:
                diff = "Expert"
            else:
                diff = "À retraiter"
                
            f.write(f"| {idx+1} | **{item['mot']}** | {score:.2f} | {diff} | {item.get('zipf_books', 0.0):.2f} | {item.get('zipf_films', 0.0):.2f} | {item.get('ratio', 1.0):.1f} | {cats} |\n")
            
        f.write("\n### ⚠️ Mots à faible pertinence (exemples trop simples, trop rares ou trop courts)\n")
        f.write("Voici quelques exemples de mots qui ont été écartés ou jugés peu pertinents pour un niveau C1 (score très bas) :\n\n")
        f.write("| Mot | Pertinence | Raison du score faible |\n")
        f.write("|---|---|---|\n")
        
        # Show some low score examples
        # Find some words with zipf_books > 4.5 (too common)
        too_common = [item for s, item in scored_words if item.get("zipf_books", 0.0) > 4.5][:5]
        for item in too_common:
            score = calculate_relevance(item)
            f.write(f"| {item['mot']} | {score:.2f} | Trop commun (Zipf Livres = {item.get('zipf_books', 0.0):.2f}) |\n")
            
        # Find some words with length < 3
        too_short = [item for s, item in scored_words if len(item.get("mot", "")) < 3][:5]
        for item in too_short:
            score = calculate_relevance(item)
            f.write(f"| {item['mot']} | {score:.2f} | Trop court ({len(item.get('mot', ''))} lettres) |\n")
            
        # Find some words with ratio < 1.0
        too_oral = [item for s, item in scored_words if item.get("ratio", 1.0) < 0.5][:5]
        for item in too_oral:
            score = calculate_relevance(item)
            f.write(f"| {item['mot']} | {score:.2f} | Registre trop oral/familier (Ratio = {item.get('ratio', 1.0):.2f}) |\n")

    print(f"Report written to {OUTPUT_REPORT}")
    
    # Save the full sorted list to a new CSV file for the user
    import csv
    with open("c1_relevant_words_sorted.csv", "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.writer(f, delimiter=";")
        writer.writerow(["mot", "pertinence_c1", "difficulté_brute", "zipf_books", "zipf_films", "ratio_littéraire", "categories_wiktionnaire"])
        for score, item in scored_words:
            cats = "; ".join(item.get("categories_wikt", []))
            zb = item.get("zipf_books", 0.0)
            if zb >= 3.0:
                diff = "Débutant"
            elif zb >= 1.5:
                diff = "Intermédiaire"
            elif zb > 0.0:
                diff = "Expert"
            else:
                diff = "À retraiter"
            writer.writerow([
                item["mot"],
                round(score, 2),
                diff,
                round(item.get("zipf_books", 0.0), 2),
                round(item.get("zipf_films", 0.0), 2),
                round(item.get("ratio", 1.0), 2),
                cats
            ])
    print("CSV c1_relevant_words_sorted.csv generated successfully.")

if __name__ == "__main__":
    main()
