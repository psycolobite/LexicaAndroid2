import json
import csv
import os

INPUT_JSON = "wiktionary_literary_words.json"
OUTPUT_CSV = "wiktionary_literary_words.csv"

def get_difficulty_category(score):
    if score >= 3.0:
        return "Débutant"
    elif score >= 1.5:
        return "Intermédiaire"
    else:
        return "Expert"

def main():
    if not os.path.exists(INPUT_JSON):
        print(f"Error: {INPUT_JSON} not found.")
        return
        
    print(f"Reading {INPUT_JSON}...")
    with open(INPUT_JSON, "r", encoding="utf-8") as f:
        data = json.load(f)
        
    processed_rows = []
    
    for item in data:
        zb = item.get("zipf_books", 0.0)
        zf = item.get("zipf_films", 0.0)
        
        if zb > 0.0:
            score = zb
            source = "livre"
            category = get_difficulty_category(score)
        elif zf > 0.0:
            score = zf
            source = "film"
            category = get_difficulty_category(score)
        else:
            score = 0.0
            source = "aucun"
            category = "À retraiter"
        
        # Clean up wiktionary categories list to a simple string
        wikt_cats = "; ".join(item.get("categories_wikt", []))
        
        processed_rows.append({
            "mot": item.get("mot", ""),
            "difficulté": category,
            "source_score": source,
            "score_utilisé": round(score, 2),
            "zipf_books": round(zb, 2),
            "zipf_films": round(zf, 2),
            "ratio_littéraire": round(item.get("ratio", 1.0), 2),
            "categories_wiktionnaire": wikt_cats,
            "freqlivres": round(item.get("freqlivres", 0.0), 2),
            "freqfilms": round(item.get("freqfilms", 0.0), 2)
        })
        
    # Sort order:
    # 1. Category rank (Débutant first, then Intermédiaire, then Expert, then À retraiter)
    # 2. Within category, sort by score_utilisé descending
    # 3. Alphabetical order of word
    def sort_key(row):
        diff = row["difficulté"]
        if diff == "Débutant":
            rank = 1
        elif diff == "Intermédiaire":
            rank = 2
        elif diff == "Expert":
            rank = 3
        else:
            rank = 4
        return (rank, -row["score_utilisé"], row["mot"])
        
    processed_rows.sort(key=sort_key)
    
    # Write to CSV using semicolon separator (best for French Excel / LibreOffice Calc)
    fieldnames = [
        "mot", "difficulté", "source_score", "score_utilisé", "zipf_books", "zipf_films", 
        "ratio_littéraire", "categories_wiktionnaire", "freqlivres", "freqfilms"
    ]
    
    print(f"Writing {len(processed_rows)} rows to {OUTPUT_CSV}...")
    with open(OUTPUT_CSV, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames, delimiter=";")
        writer.writeheader()
        writer.writerows(processed_rows)
        
    # Count rows by difficulty
    counts = {"Débutant": 0, "Intermédiaire": 0, "Expert": 0, "À retraiter": 0}
    for r in processed_rows:
        counts[r["difficulté"]] += 1
        
    print("\n" + "="*40)
    print("STATISTIQUES DU COMPILÉ (LOGIQUE AJUSTÉE) :")
    print("="*40)
    print(f"Débutant      (Zipf >= 3.0) : {counts['Débutant']} mots")
    print(f"Intermédiaire (1.5 <= Zipf < 3.0) : {counts['Intermédiaire']} mots")
    print(f"Expert        (Zipf < 1.5)  : {counts['Expert']} mots")
    print(f"À retraiter   (Zipf = 0.0)  : {counts['À retraiter']} mots")
    print(f"Total                       : {len(processed_rows)} mots")
    print("="*40)
    print("Fichier CSV généré avec succès.")

if __name__ == "__main__":
    main()
