import os
import json
import urllib.request
import urllib.parse
import zipfile
import csv
import math
import re
import time

ZIP_PATH = "Lexique383.zip"
OUTPUT_JSON = "wiktionary_literary_words.json"

CATEGORIES = [
    "Termes soutenus en français",
    "Termes littéraires en français",
    "Termes poétiques en français",
    "Termes archaïques en français"
]

def load_lexique():
    if not os.path.exists(ZIP_PATH):
        raise Exception("Lexique383.zip is missing from the tools directory.")
        
    lexique = {}
    print(f"Loading Lexique383 from {ZIP_PATH}...")
    with zipfile.ZipFile(ZIP_PATH, 'r') as z:
        tsv_filename = None
        for name in z.namelist():
            if name.endswith('.tsv') or name.endswith('.txt'):
                tsv_filename = name
                break
        
        with z.open(tsv_filename) as f:
            content = (line.decode('utf-8', errors='ignore') for line in f)
            reader = csv.DictReader(content, delimiter='\t')
            for row in reader:
                word = row['ortho'].lower().strip()
                if not word:
                    continue
                
                # Check islem (must be lemma)
                if row.get('islem') != '1':
                    continue
                
                cgram = row.get('cgram', '')
                if cgram not in ['NOM', 'ADJ', 'VER']:
                    continue
                    
                try:
                    freqlivres = float(row['freqlivres'])
                except ValueError:
                    freqlivres = 0.0
                try:
                    freqfilms = float(row['freqfilms2'])
                except ValueError:
                    freqfilms = 0.0
                
                if word in lexique:
                    lexique[word]['freqlivres'] += freqlivres
                    lexique[word]['freqfilms'] += freqfilms
                else:
                    lexique[word] = {
                        'freqlivres': freqlivres,
                        'freqfilms': freqfilms
                    }
    print(f"Loaded {len(lexique)} unique lemmas from Lexique383.")
    return lexique

def fetch_category_members(category_name):
    cache_file = "cache_" + re.sub(r'[^a-zA-Z0-9]', '_', category_name) + ".json"
    if os.path.exists(cache_file):
        print(f"Loading category '{category_name}' from local cache...")
        with open(cache_file, "r", encoding="utf-8") as c:
            return json.load(c)

    print(f"Fetching category '{category_name}' from fr.wiktionary.org...")
    base_url = "https://fr.wiktionary.org/w/api.php"
    words = []
    cmcontinue = None
    headers = {'User-Agent': 'LexicaAndroid2/1.0 (vocabulaire francais)'}
    
    fetch_complete = False
    while True:
        params = {
            "action": "query",
            "list": "categorymembers",
            "cmtitle": f"Catégorie:{category_name}",
            "cmtype": "page",
            "cmnamespace": "0",
            "cmlimit": "500",
            "format": "json",
            "formatversion": "2"
        }
        if cmcontinue:
            params["cmcontinue"] = cmcontinue
            
        url = base_url + "?" + urllib.parse.urlencode(params)
        req = urllib.request.Request(url, headers=headers)
        
        try:
            with urllib.request.urlopen(req) as resp:
                data = json.loads(resp.read().decode('utf-8'))
                members = data.get("query", {}).get("categorymembers", [])
                for member in members:
                    title = member.get("title", "").strip()
                    if title:
                        words.append(title)
                
                cmcontinue = data.get("continue", {}).get("cmcontinue")
                if not cmcontinue:
                    fetch_complete = True
                    break
            
            # Sleep 1 second between requests to avoid 429 rate limits
            time.sleep(1.0)
        except Exception as e:
            print(f"Error fetching wiktionary pages for {category_name}: {e}")
            break
            
    print(f"-> Retrieved {len(words)} pages from '{category_name}'")
    
    if words and fetch_complete:
        with open(cache_file, "w", encoding="utf-8") as c:
            json.dump(words, c, ensure_ascii=False)
            
    return words

def is_valid_word(word):
    if len(word) < 2:
        return False
    if ' ' in word or '-' in word or any(c.isdigit() for c in word):
        return False
    if not re.match(r'^[a-zàâäéèêëîïôöùûüçœæ]+$', word):
        return False
    return True

def main():
    lexique = load_lexique()
    
    # Query all categories
    wikt_words_map = {} # word -> list of categories it belongs to
    
    for cat in CATEGORIES:
        words = fetch_category_members(cat)
        for w in words:
            w_clean = w.strip()
            if is_valid_word(w_clean.lower()):
                w_lower = w_clean.lower()
                if w_lower in wikt_words_map:
                    if cat not in wikt_words_map[w_lower]['categories_wikt']:
                        wikt_words_map[w_lower]['categories_wikt'].append(cat)
                else:
                    wikt_words_map[w_lower] = {
                        'mot': w_clean, # keep original casing if any
                        'categories_wikt': [cat]
                    }
        # Sleep between categories to be safe
        time.sleep(1.0)
        
    print(f"Total unique valid words gathered from Wiktionnaire: {len(wikt_words_map)}")
    
    # Match with Lexique383 stats
    final_database = []
    matched_count = 0
    
    for w_lower, info in wikt_words_map.items():
        if w_lower in lexique:
            stats = lexique[w_lower]
            fl = stats['freqlivres']
            ff = stats['freqfilms']
            matched_count += 1
        else:
            # Word is extremely rare (not in Lexique383)
            fl = 0.0
            ff = 0.0
            
        ratio = (fl + 0.01) / (ff + 0.01)
        zf_films = math.log10(ff) + 3.0 if ff > 0 else 0.0
        zf_books = math.log10(fl) + 3.0 if fl > 0 else 0.0
        
        final_database.append({
            'mot': info['mot'],
            'categories_wikt': info['categories_wikt'],
            'freqlivres': fl,
            'freqfilms': ff,
            'ratio': ratio,
            'zipf_films': zf_films,
            'zipf_books': zf_books
        })
        
    print(f"Matching complete: {matched_count} words matched with Lexique383.")
    
    # Calculate detailed counts by category and difficulty
    print("\n" + "="*50)
    print("BILAN PAR CATÉGORIE DU WIKTIONNAIRE :")
    print("="*50)
    
    for cat in CATEGORIES:
        cat_words = [w for w in final_database if cat in w['categories_wikt']]
        deb = len([w for w in cat_words if w['zipf_films'] >= 3.0])
        inter = len([w for w in cat_words if 1.5 <= w['zipf_films'] < 3.0])
        exp = len([w for w in cat_words if w['zipf_films'] < 1.5])
        print(f"Catégorie '{cat}' :")
        print(f"  - Total : {len(cat_words)} mots")
        print(f"  - Débutant    (Zipf films >= 3.0) : {deb}")
        print(f"  - Intermédiaire (1.5 <= Zipf < 3.0) : {inter}")
        print(f"  - Expert       (Zipf films < 1.5) : {exp}")
        print("-" * 50)
        
    # Global counts
    deb_g = len([w for w in final_database if w['zipf_films'] >= 3.0])
    inter_g = len([w for w in final_database if 1.5 <= w['zipf_films'] < 3.0])
    exp_g = len([w for w in final_database if w['zipf_films'] < 1.5])
    print(f"\nTOTAL UNIQUE DE LA BASE : {len(final_database)} mots")
    print(f"  - Débutant (Zipf films >= 3.0) : {deb_g} mots")
    print(f"  - Intermédiaire (1.5 <= Zipf < 3.0) : {inter_g} mots")
    print(f"  - Expert (Zipf films < 1.5) : {exp_g} mots")
    print("="*50 + "\n")

    print(f"Saving database of {len(final_database)} words to {OUTPUT_JSON}...")
    with open(OUTPUT_JSON, "w", encoding="utf-8") as out:
        json.dump(final_database, out, ensure_ascii=False, indent=2)
        
    print("Execution complete. Database successfully generated.")

if __name__ == "__main__":
    main()
