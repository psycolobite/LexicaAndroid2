import json
import csv
import os

INPUT_JSON = "beautiful_literary_words.json"
OUTPUT_JSON = "consolidated_literary_words.json"
OUTPUT_CSV = "consolidated_literary_words.csv"

# 1. New words cited by the user
USER_WORDS = [
    {"mot": "fielueux", "theme": "Sentiments & Psyché"},
    {"mot": "impudence", "theme": "Esprit & Caractère"},
    {"mot": "moujik", "theme": "Esprit & Caractère"},
    {"mot": "inimitié", "theme": "Sentiments & Psyché"},
    {"mot": "neurasthénique", "theme": "Sentiments & Psyché"},
    {"mot": "causticité", "theme": "Esprit & Caractère"},
    {"mot": "caustique", "theme": "Esprit & Caractère"},
    {"mot": "prolixe", "theme": "Art & Langage"},
    {"mot": "malandrin", "theme": "Esprit & Caractère"},
    {"mot": "scrupuleux", "theme": "Esprit & Caractère"},
    {"mot": "casuistique", "theme": "Philosophie & Idées"},
    {"mot": "imbroglio", "theme": "Esprit & Caractère"},
    {"mot": "scabreux", "theme": "Esprit & Caractère"},
    {"mot": "purpurine", "theme": "Lumière & Ombres"},
    {"mot": "aphoristique", "theme": "Art & Langage"},
    {"mot": "éclectisme", "theme": "Philosophie & Idées"},
    {"mot": "corolle", "theme": "Nature & Cosmos"},
    {"mot": "varicose", "theme": "Sentiments & Psyché"},
    {"mot": "noévie", "theme": "Sentiments & Psyché"},
    {"mot": "cors", "theme": "Sentiments & Psyché"},
    {"mot": "parapathique", "theme": "Sentiments & Psyché"},
    {"mot": "cénesthésie", "theme": "Sentiments & Psyché"}
]

# 2. Large list of proposals, including poetic and related terms
PROPOSAL_WORDS = [
    # Esprit & Caractère (causticité, impudence, outrecuidance, moujik, malandrin)
    {"mot": "fielleux", "theme": "Esprit & Caractère"},
    {"mot": "vitriolique", "theme": "Esprit & Caractère"},
    {"mot": "atrabilaire", "theme": "Esprit & Caractère"},
    {"mot": "bilieux", "theme": "Esprit & Caractère"},
    {"mot": "sourcilleux", "theme": "Esprit & Caractère"},
    {"mot": "pointilleux", "theme": "Esprit & Caractère"},
    {"mot": "méticuleux", "theme": "Esprit & Caractère"},
    {"mot": "intègre", "theme": "Esprit & Caractère"},
    {"mot": "probe", "theme": "Esprit & Caractère"},
    {"mot": "effronterie", "theme": "Esprit & Caractère"},
    {"mot": "insolence", "theme": "Esprit & Caractère"},
    {"mot": "impertinence", "theme": "Esprit & Caractère"},
    {"mot": "irrévérence", "theme": "Esprit & Caractère"},
    {"mot": "outrageux", "theme": "Esprit & Caractère"},
    {"mot": "outrecuidant", "theme": "Esprit & Caractère"},
    {"mot": "sicaire", "theme": "Esprit & Caractère"},
    {"mot": "spadassin", "theme": "Esprit & Caractère"},
    {"mot": "sbire", "theme": "Esprit & Caractère"},
    {"mot": "condottiere", "theme": "Esprit & Caractère"},
    {"mot": "estafette", "theme": "Esprit & Caractère"},
    {"mot": "grognard", "theme": "Esprit & Caractère"},
    {"mot": "palefrenier", "theme": "Esprit & Caractère"},
    {"mot": "marmiton", "theme": "Esprit & Caractère"},
    {"mot": "flibustier", "theme": "Esprit & Caractère"},
    {"mot": "maraudeur", "theme": "Esprit & Caractère"},
    {"mot": "brigand", "theme": "Esprit & Caractère"},
    {"mot": "larron", "theme": "Esprit & Caractère"},
    {"mot": "fripon", "theme": "Esprit & Caractère"},
    {"mot": "gredin", "theme": "Esprit & Caractère"},
    {"mot": "scélérat", "theme": "Esprit & Caractère"},
    {"mot": "graveleux", "theme": "Esprit & Caractère"},
    {"mot": "licencieux", "theme": "Esprit & Caractère"},
    {"mot": "grivois", "theme": "Esprit & Caractère"},
    {"mot": "grivoiserie", "theme": "Esprit & Caractère"},
    {"mot": "satyre", "theme": "Esprit & Caractère"},
    {"mot": "libidineux", "theme": "Esprit & Caractère"},
    {"mot": "cénobite", "theme": "Esprit & Caractère"},
    {"mot": "anachorète", "theme": "Esprit & Caractère"},
    {"mot": "érémitique", "theme": "Esprit & Caractère"},
    {"mot": "ostracisme", "theme": "Esprit & Caractère"},
    
    # Sentiments & Psyché (inimitié, neurasthénique, varicose, noévie, cors, cénesthésie, etc.)
    {"mot": "aboulie", "theme": "Sentiments & Psyché"},
    {"mot": "apathie", "theme": "Sentiments & Psyché"},
    {"mot": "anémie", "theme": "Sentiments & Psyché"},
    {"mot": "hypocondrie", "theme": "Sentiments & Psyché"},
    {"mot": "neurasthénie", "theme": "Sentiments & Psyché"},
    {"mot": "paresthésie", "theme": "Sentiments & Psyché"},
    {"mot": "somatisation", "theme": "Sentiments & Psyché"},
    {"mot": "spleenétique", "theme": "Sentiments & Psyché"},
    {"mot": "venimeux", "theme": "Sentiments & Psyché"},
    {"mot": "dolent", "theme": "Sentiments & Psyché"},
    {"mot": "chagrin", "theme": "Sentiments & Psyché"},
    {"mot": "langoureux", "theme": "Sentiments & Psyché"},
    {"mot": "solitaire", "theme": "Sentiments & Psyché"},
    {"mot": "claustration", "theme": "Sentiments & Psyché"},
    {"mot": "séquestration", "theme": "Sentiments & Psyché"},
    {"mot": "bannissement", "theme": "Sentiments & Psyché"},
    {"mot": "nostalgique", "theme": "Sentiments & Psyché"},
    {"mot": "abattement", "theme": "Sentiments & Psyché"},
    {"mot": "affliction", "theme": "Sentiments & Psyché"},
    {"mot": "consternation", "theme": "Sentiments & Psyché"},
    {"mot": "accablement", "theme": "Sentiments & Psyché"},
    
    # Nature & Cosmos (corolle, et poésie de la nature)
    {"mot": "étamine", "theme": "Nature & Cosmos"},
    {"mot": "sépale", "theme": "Nature & Cosmos"},
    {"mot": "pistil", "theme": "Nature & Cosmos"},
    {"mot": "pétale", "theme": "Nature & Cosmos"},
    {"mot": "inflorescence", "theme": "Nature & Cosmos"},
    {"mot": "sillage", "theme": "Nature & Cosmos"},
    {"mot": "alcyon", "theme": "Nature & Cosmos"},
    {"mot": "chrysanthème", "theme": "Nature & Cosmos"},
    {"mot": "améthyste* (améthyste)", "theme": "Nature & Cosmos"},
    {"mot": "améthyste", "theme": "Nature & Cosmos"},
    {"mot": "équinoxe", "theme": "Nature & Cosmos"},
    {"mot": "dryade", "theme": "Nature & Cosmos"},
    {"mot": "sylphide", "theme": "Nature & Cosmos"},
    {"mot": "auréole", "theme": "Nature & Cosmos"},
    {"mot": "firmament", "theme": "Nature & Cosmos"},
    {"mot": "zodiaque", "theme": "Nature & Cosmos"},
    {"mot": "brume", "theme": "Nature & Cosmos"},
    {"mot": "ondée", "theme": "Nature & Cosmos"},
    
    # Art & Langage (prolixe, aphoristique, soliloque, et poésie)
    {"mot": "aphorisme", "theme": "Art & Langage"},
    {"mot": "apophtegme", "theme": "Art & Langage"},
    {"mot": "gnomique", "theme": "Art & Langage"},
    {"mot": "psalmodie", "theme": "Art & Langage"},
    {"mot": "rhapsode", "theme": "Art & Langage"},
    {"mot": "aède", "theme": "Art & Langage"},
    {"mot": "vaticination", "theme": "Art & Langage"},
    {"mot": "satirique", "theme": "Art & Langage"},
    {"mot": "satyrique", "theme": "Art & Langage"},
    {"mot": "verbeux", "theme": "Art & Langage"},
    {"mot": "loquacité", "theme": "Art & Langage"},
    {"mot": "prolixité", "theme": "Art & Langage"},
    {"mot": "garrulité", "theme": "Art & Langage"},
    {"mot": "volubilité", "theme": "Art & Langage"},
    {"mot": "élégie", "theme": "Art & Langage"},
    {"mot": "quiproquo", "theme": "Art & Langage"},
    {"mot": "pataquès", "theme": "Art & Langage"},
    {"mot": "allitération", "theme": "Art & Langage"},
    {"mot": "assonance", "theme": "Art & Langage"},
    {"mot": "hémistiche", "theme": "Art & Langage"},
    {"mot": "césure", "theme": "Art & Langage"},
    {"mot": "métrique", "theme": "Art & Langage"},
    {"mot": "lyrisme", "theme": "Art & Langage"},
    {"mot": "calligramme", "theme": "Art & Langage"},
    {"mot": "poétique", "theme": "Art & Langage"},
    {"mot": "mignonne", "theme": "Art & Langage"},
    
    # Philosophie & Idées (casuistique, éclectisme)
    {"mot": "sophisme", "theme": "Philosophie & Idées"},
    {"mot": "syllogisme", "theme": "Philosophie & Idées"},
    {"mot": "heuristique", "theme": "Philosophie & Idées"},
    {"mot": "truisme", "theme": "Philosophie & Idées"},
    {"mot": "syncrétisme", "theme": "Philosophie & Idées"},
    {"mot": "dogme", "theme": "Philosophie & Idées"},
    {"mot": "maxime", "theme": "Philosophie & Idées"},
    {"mot": "scholastique", "theme": "Philosophie & Idées"},
    {"mot": "ambiguïté", "theme": "Philosophie & Idées"},
    {"mot": "imbrication", "theme": "Philosophie & Idées"},
    {"mot": "ontologie", "theme": "Philosophie & Idées"},
    {"mot": "métaphysique", "theme": "Philosophie & Idées"},
    
    # Temps & Éphémère (séculaire)
    {"mot": "sénescence", "theme": "Temps & Éphémère"},
    {"mot": "caduc", "theme": "Temps & Éphémère"},
    {"mot": "obsolète", "theme": "Temps & Éphémère"},
    {"mot": "ancestral", "theme": "Temps & Éphémère"},
    {"mot": "immémorial", "theme": "Temps & Éphémère"},
    {"mot": "millénaire", "theme": "Temps & Éphémère"}
]

def main():
    if not os.path.exists(INPUT_JSON):
        print(f"Error: {INPUT_JSON} not found. Please run generate_beautiful_literary_db.py first.")
        return
        
    print(f"Reading existing words from {INPUT_JSON}...")
    with open(INPUT_JSON, "r", encoding="utf-8") as f:
        existing_data = json.load(f)
        
    consolidated_words = {}
    
    # 1. Add words from existing list (only mot and theme)
    for item in existing_data:
        word = item["mot"].strip().lower()
        theme = item["theme"].strip()
        consolidated_words[word] = theme
        
    # 2. Add user custom words
    for item in USER_WORDS:
        word = item["mot"].strip().lower()
        theme = item["theme"].strip()
        consolidated_words[word] = theme
        
    # 3. Add proposal words
    for item in PROPOSAL_WORDS:
        word = item["mot"].strip().lower()
        # Strip trailing markers
        if "*" in word:
            word = word.split("*")[0]
        word = word.strip()
        theme = item["theme"].strip()
        consolidated_words[word] = theme
        
    # Compile list of dicts
    final_list = []
    for word, theme in consolidated_words.items():
        final_list.append({
            "mot": word,
            "theme": theme
        })
        
    # Sort for output: 1. theme, 2. mot
    final_list.sort(key=lambda x: (x["theme"], x["mot"]))
    
    # Write to JSON
    with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
        json.dump(final_list, f, ensure_ascii=False, indent=2)
    print(f"JSON consolidated database generated: {OUTPUT_JSON}")
    
    # Write to CSV
    fieldnames = ["mot", "theme"]
    with open(OUTPUT_CSV, "w", encoding="utf-8-sig", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames, delimiter=";")
        writer.writeheader()
        writer.writerows(final_list)
    print(f"CSV consolidated spreadsheet generated: {OUTPUT_CSV}")
    
    # Print stats
    theme_counts = {}
    for r in final_list:
        theme_counts[r["theme"]] = theme_counts.get(r["theme"], 0) + 1
        
    print("\n" + "="*40)
    print("STATISTIQUES DE LA LISTE CONSOLIDÉE :")
    print("="*40)
    print(f"Total de mots précieux : {len(final_list)}")
    print("-"*40)
    print("Répartition par Thème :")
    for theme, count in sorted(theme_counts.items()):
        print(f" - {theme:<22} : {count} mots")
    print("="*40)

if __name__ == "__main__":
    main()
