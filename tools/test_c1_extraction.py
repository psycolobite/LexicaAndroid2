import os
import json
import urllib.request
import urllib.parse
import zipfile
import csv
import math
import re
import time

ZIP_URL = "http://www.lexique.org/databases/Lexique383/Lexique383.zip"
ZIP_PATH = "Lexique383.zip"

def get_lexique_data():
    if not os.path.exists(ZIP_PATH):
        print(f"Downloading Lexique383.zip from {ZIP_URL}...")
        urllib.request.urlretrieve(ZIP_URL, ZIP_PATH)
        print("Download complete.")
    else:
        print("Lexique383.zip already downloaded.")

    lexique = {}
    with zipfile.ZipFile(ZIP_PATH, 'r') as z:
        tsv_filename = None
        for name in z.namelist():
            if name.endswith('.tsv') or name.endswith('.txt'):
                tsv_filename = name
                break
        
        if not tsv_filename:
            raise Exception("No TSV or TXT file found in Lexique383.zip")
        
        print(f"Parsing {tsv_filename}...")
        with z.open(tsv_filename) as f:
            # Decode line by line
            content = (line.decode('utf-8', errors='ignore') for line in f)
            reader = csv.DictReader(content, delimiter='\t')
            for row in reader:
                word = row['ortho'].lower().strip()
                if not word:
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
                    
    print(f"Loaded {len(lexique)} unique words from Lexique383.")
    return lexique

def fetch_wiktionary_category_words(category_name, max_pages=2000):
    print(f"Fetching pages in category '{category_name}' from fr.wiktionary.org...")
    base_url = "https://fr.wiktionary.org/w/api.php"
    words = []
    cmcontinue = None
    
    headers = {'User-Agent': 'LexicaAndroid2/1.0 (vocabulaire francais)'}
    
    while len(words) < max_pages:
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
                    word = member.get("title", "").strip()
                    if word:
                        words.append(word)
                
                print(f"Fetched {len(words)} pages so far...")
                
                cmcontinue = data.get("continue", {}).get("cmcontinue")
                if not cmcontinue:
                    break
            
            # Rate limiting
            time.sleep(0.5)
        except Exception as e:
            print(f"Error fetching wiktionary pages: {e}")
            break
            
    return words

def is_valid_candidate(word):

    if not word:
        return False
    if ' ' in word:
        return False
    if word[0].isupper():
        return False
    if len(word) < 5:
        return False
    if any(c.isdigit() for c in word):
        return False
    if word.startswith('-') or word.endswith('-'):
        return False
    return True

def fetch_definition(word):
    url = f"https://fr.wiktionary.org/w/api.php?action=parse&page={urllib.parse.quote(word)}&prop=text&redirects=true&format=json&formatversion=2"
    headers = {'User-Agent': 'LexicaAndroid2/1.0 (vocabulaire francais)'}
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=5) as response:
            data = json.loads(response.read().decode('utf-8'))
            html = data.get('parse', {}).get('text', '')
            if not html:
                return None
            
            # Find French section
            idx = html.find('id="Français"')
            if idx == -1:
                idx = html.find('Français')
            if idx != -1:
                html = html[idx:]
                next_h2 = html.find('<h2', 10)
                if next_h2 != -1:
                    html = html[:next_h2]
            
            # Find first <ol>
            ol_start = html.find('<ol>')
            if ol_start != -1:
                ol_end = html.find('</ol>', ol_start)
                ol_content = html[ol_start:ol_end]
                # Find first <li>
                li_start = ol_content.find('<li>')
                if li_start != -1:
                    li_end = ol_content.find('</li>', li_start)
                    li_content = ol_content[li_start+4:li_end]
                    # Strip tags
                    definition = re.sub(r'<[^>]+>', '', li_content)
                    definition = re.sub(r'\s+', ' ', definition).strip()
                    # Shorten if extremely long
                    if len(definition) > 300:
                        definition = definition[:297] + "..."
                    return definition
    except Exception as e:
        pass
    return None

STATIC_CANDIDATES = [
    "abnégation", "acrimonie", "alacrite", "animosité", "ataraxie", "avarice", "bienveillance",
    "bonhomie", "candeur", "circonspection", "clémence", "concupiscence", "condescendance",
    "cupidité", "cynisme", "déférence", "désinvolture", "duplicité", "équanimité", "fausseté",
    "félonie", "flagornerie", "frugalité", "galanterie", "grandiloquence", "hypocrisie",
    "impudence", "inconstance", "indolence", "iniquité", "insolence", "irascibilité", "laconisme",
    "lâcheté", "loquacité", "magnanimité", "mansuétude", "médisance", "mesquinerie", "miséricorde",
    "modestie", "mollesse", "morosité", "obstination", "opiniâtreté", "ostracisme", "outrecuidance",
    "parjure", "parsimonie", "pédanterie", "perfidie", "perspicacité", "pétulance", "piété",
    "placidité", "présomption", "probité", "procrastination", "prodigalité", "pusillanimité",
    "querulence", "rancœur", "rectitude", "roublardise", "sagacité", "servilité", "sincérité",
    "stoïcisme", "suffisance", "susceptibilité", "sycophante", "témérité", "tergiversation",
    "turpitude", "vénalité", "vicissitude", "vaillance", "vanité", "urbanité", "algarade",
    "ambages", "atavisme", "casuistique", "cénacle", "coercition", "démiurge", "déliquescence",
    "eschatologie", "exégèse", "idiosyncrasie", "nihilisme", "aporie", "axiome", "catharsis",
    "dialectique", "épistémè", "herméneutique", "heuristique", "immanence", "maïeutique",
    "ontologie", "paradigme", "sophisme", "syncrétisme", "téléologie", "transcendance",
    "apophtegme", "déontologie", "tautologie", "syllogisme", "éristique", "nominalisme",
    "empirisme", "solipsisme", "allégorie", "anachronisme", "antiphrase", "aphorisme",
    "catachrèse", "chiasme", "circonlocution", "diégèse", "ellipse", "euphémisme", "hyperbole",
    "litote", "métonymie", "oxymore", "parabole", "pléonasme", "prétérition", "synecdoque",
    "zeugme", "analepse", "prolepse", "périphrase", "anaphore", "épiphore", "asyndète",
    "polysyndète", "acédie", "atrabilaire", "égarement", "emportement", "eudémonie", "mélancolie",
    "neurasthénie", "nostalgie", "quiétude", "ressentiment", "sérénité", "spleen", "torpeur",
    "velléité", "affect", "ambivalence", "angoisse", "apathie", "euphorie", "fébrilité",
    "langueur", "lassitude", "léthargie", "méfiance", "perplexité", "répugnance", "satiété",
    "stupéfaction", "stupeur", "volupté", "altruisme", "antagonisme", "clientélisme",
    "démagogie", "despotisme", "égalitarisme", "élitisme", "fédéralisme", "formalisme",
    "hédonisme", "individualisme", "mercantilisme", "nationalisme", "opportunisme", "paternalisme",
    "pragmatisme", "relativisme", "sectarisme", "utopisme", "conformisme", "dissidence",
    "idolâtrie", "intolérance", "mécénat", "néophyte", "philantropie", "prosélytisme", "schisme",
    "tolérance", "abscons", "acception", "atermoiement", "badinage", "bienséance", "calembredaine",
    "clairvoyance", "commisération", "compunction", "contrit", "décrépitude", "désabusement",
    "dichotomie", "dilapidation", "discernement", "dissimulation", "emphase", "éphémère",
    "équivoque", "érudition", "exaltation", "flegme", "frivolité", "gabegie", "incongruité",
    "ineptie", "ingénuité", "intransigeance", "ironie", "jovialité", "litigieux", "logorrhée",
    "lucidité", "maladresse", "malveillance", "mortification", "munificence", "mysticisme",
    "naïveté", "nonchalance", "obliquité", "obstruction", "pédantisme", "philologie", "prévenance",
    "propension", "pugnacité", "pusillanimité", "ratiocination", "résilience", "réthorique",
    "révérence", "scrupule", "sérendipité", "sibyllin", "simulacre", "subtilité", "versatilité",
    "véhémence", "zèle"
]

def main():
    # 1. Load Lexique database
    lexique = get_lexique_data()
    
    # 2. Fetch pages from category "Termes soutenus en français"
    wikt_words = fetch_wiktionary_category_words("Termes soutenus en français", max_pages=3000)
    print(f"Total pages retrieved from Wiktionnaire: {len(wikt_words)}")
    
    if not wikt_words:
        print("Wiktionnaire API fetch failed or empty (likely due to HTTP 429). Falling back to STATIC_CANDIDATES list.")
        wikt_words = STATIC_CANDIDATES
        
    # 3. Clean and map
    candidates = []
    for word in wikt_words:
        if is_valid_candidate(word):
            word_lower = word.lower()
            if word_lower in lexique:
                stats = lexique[word_lower]
                fl = stats['freqlivres']
                ff = stats['freqfilms']
                
                # Compute literary ratio (epsilon = 0.01 to avoid extreme division)
                ratio = (fl + 0.01) / (ff + 0.01)
                
                # Compute Zipf for books and films
                zipf_books = math.log10(fl) + 3.0 if fl > 0 else 0.0
                zipf_films = math.log10(ff) + 3.0 if ff > 0 else 0.0
                
                candidates.append({
                    'word': word,
                    'freqlivres': fl,
                    'freqfilms': ff,
                    'ratio': ratio,
                    'zipf_books': zipf_books,
                    'zipf_films': zipf_films
                })
    
    print(f"Total valid candidates matching Lexique383: {len(candidates)}")
    
    # 4. Filter by Ratio Littéraire > 4.0
    literary_candidates = [c for c in candidates if c['ratio'] > 4.0]
    print(f"Total literary candidates (Ratio > 4.0): {len(literary_candidates)}")
    
    # 5. Classify by Zipf (we'll do both classifications to analyze)
    # Classification A: by Zipf_books
    cat_books = {
        'debutant_intermediaire': [], # Zipf_books >= 3.0
        'avance': [],                 # Zipf_books in [1.5, 3.0[
        'expert': []                  # Zipf_books < 1.5
    }
    
    # Classification B: by Zipf_films
    cat_films = {
        'debutant_intermediaire': [], # Zipf_films >= 3.0
        'avance': [],                 # Zipf_films in [1.5, 3.0[
        'expert': []                  # Zipf_films < 1.5
    }
    
    for c in literary_candidates:
        # Categorize by Zipf_books
        zb = c['zipf_books']
        if zb >= 3.0:
            cat_books['debutant_intermediaire'].append(c)
        elif zb >= 1.5:
            cat_books['avance'].append(c)
        else:
            cat_books['expert'].append(c)
            
        # Categorize by Zipf_films
        zf = c['zipf_films']
        if zf >= 3.0:
            cat_films['debutant_intermediaire'].append(c)
        elif zf >= 1.5:
            cat_films['avance'].append(c)
        else:
            cat_films['expert'].append(c)
            
    # Sort lists by ratio descending
    for key in cat_books:
        cat_books[key].sort(key=lambda x: x['ratio'], reverse=True)
    for key in cat_films:
        cat_films[key].sort(key=lambda x: x['ratio'], reverse=True)
        
    print("\nSummary of Zipf_books classification:")
    for k, v in cat_books.items():
        print(f" - {k}: {len(v)} words")
        
    print("\nSummary of Zipf_films classification:")
    for k, v in cat_films.items():
        print(f" - {k}: {len(v)} words")
        
    # Generate report
    report_lines = []
    report_lines.append("# Rapport d'Extraction de Vocabulaire Littéraire (C1)")
    report_lines.append(f"Nombre total de pages récupérées depuis fr.wiktionary.org : {len(wikt_words)}")
    report_lines.append(f"Nombre de candidats valides dans Lexique383 : {len(candidates)}")
    report_lines.append(f"Nombre de mots littéraires (Ratio Littéraire > 4.0) : {len(literary_candidates)}")
    report_lines.append("\n" + "="*80 + "\n")
    
    # Let's show samples from BOTH classifications, fetch definitions for top 5 of each
    
    def process_tier(name, word_list, zipf_key):
        lines = []
        lines.append(f"## Catégorie: {name} (Total: {len(word_list)} mots)")
        lines.append("| Mot | Freq Livres (ppm) | Freq Films (ppm) | Ratio | Zipf Livres | Zipf Films | Définition Wiktionnaire |")
        lines.append("|---|---|---|---|---|---|---|")
        
        # Take top 30 to display in the table, fetch definitions for top 10
        sample = word_list[:30]
        for i, item in enumerate(sample):
            word = item['word']
            definition = "N/A"
            if i < 10:
                print(f"Fetching definition for: {word}...")
                fetched = fetch_definition(word)
                if fetched:
                    definition = fetched
                time.sleep(0.3)
            
            lines.append(f"| **{word}** | {item['freqlivres']:.2f} | {item['freqfilms']:.2f} | {item['ratio']:.2f} | {item['zipf_books']:.2f} | {item['zipf_films']:.2f} | {definition} |")
        
        # List other words in this category as a comma-separated list
        if len(word_list) > 30:
            remaining = [w['word'] for w in word_list[30:]]
            lines.append("\n**Autres mots de cette catégorie :**")
            lines.append(", ".join(remaining))
        
        lines.append("\n")
        return lines

    report_lines.append("# CLASSIFICATION SELON ZIPF LIVRES (Fréquence dans les Livres)")
    report_lines.append("Cette classification utilise la fréquence du mot dans le corpus écrit (Livres) pour déterminer son niveau.")
    report_lines.extend(process_tier("Débutant / Intermédiaire (Zipf >= 3.0)", cat_books['debutant_intermediaire'], 'zipf_books'))
    report_lines.extend(process_tier("Avancé (1.5 <= Zipf < 3.0)", cat_books['avance'], 'zipf_books'))
    report_lines.extend(process_tier("Expert (Zipf < 1.5)", cat_books['expert'], 'zipf_books'))
    
    report_lines.append("\n" + "="*80 + "\n")
    
    report_lines.append("# CLASSIFICATION SELON ZIPF FILMS (Fréquence dans les Films/Séries)")
    report_lines.append("Cette classification utilise la fréquence du mot dans le langage oral/courant (Films) pour déterminer son niveau.")
    report_lines.extend(process_tier("Débutant / Intermédiaire (Zipf >= 3.0)", cat_films['debutant_intermediaire'], 'zipf_films'))
    report_lines.extend(process_tier("Avancé (1.5 <= Zipf < 3.0)", cat_films['avance'], 'zipf_films'))
    report_lines.extend(process_tier("Expert (Zipf < 1.5)", cat_films['expert'], 'zipf_films'))
    
    # Write to output file
    output_filename = "resultat_mots_test_c1.md"
    with open(output_filename, "w", encoding="utf-8") as out:
        out.write("\n".join(report_lines))
        
    print(f"\nReport written to {output_filename}")

if __name__ == "__main__":
    main()
