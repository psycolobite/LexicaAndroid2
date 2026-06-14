import os
import json
import urllib.request
import urllib.parse
import zipfile
import csv
import math

ZIP_PATH = "Lexique383.zip"

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

def load_lexique():
    lexique = {}
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
                try:
                    freqlivres = float(row['freqlivres'])
                except ValueError:
                    freqlivres = 0.0
                try:
                    freqfilms = float(row['freqfilms2'])
                except ValueError:
                    freqfilms = 0.0
                
                # Check cgram to keep nouns, verbs, adjectives, adverbs
                cgram = row.get('cgram', '')
                
                if word in lexique:
                    lexique[word]['freqlivres'] += freqlivres
                    lexique[word]['freqfilms'] += freqfilms
                    if cgram not in lexique[word]['cgrams']:
                        lexique[word]['cgrams'].append(cgram)
                else:
                    lexique[word] = {
                        'freqlivres': freqlivres,
                        'freqfilms': freqfilms,
                        'cgrams': [cgram]
                    }
    return lexique

def get_wiktionary_category_count(category_name):
    base_url = "https://fr.wiktionary.org/w/api.php"
    params = {
        "action": "query",
        "format": "json",
        "list": "categorymembers",
        "cmtitle": f"Catégorie:{category_name}",
        "cmlimit": "1",
        "formatversion": "2"
    }
    url = base_url + "?" + urllib.parse.urlencode(params)
    req = urllib.request.Request(url, headers={'User-Agent': 'Lexica'})
    try:
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            # MediaWiki categorymembers API doesn't give total count directly unless we query list=categorymembers and count it,
            # or query action=query&prop=categoryinfo&titles=Catégorie:category_name
            pass
    except:
        pass
    
    # Correct way to get category count:
    params_info = {
        "action": "query",
        "format": "json",
        "prop": "categoryinfo",
        "titles": f"Catégorie:{category_name}",
        "formatversion": "2"
    }
    url = base_url + "?" + urllib.parse.urlencode(params_info)
    req = urllib.request.Request(url, headers={'User-Agent': 'Lexica'})
    try:
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            pages = data.get("query", {}).get("pages", [])
            if pages:
                return pages[0].get("categoryinfo", {}).get("size", 0)
    except Exception as e:
        return f"Error: {e}"
    return 0

def main():
    lexique = load_lexique()
    
    # Q1. Analysis of excluded words
    excluded_missing = []  # Not in Lexique
    excluded_low_ratio = [] # In Lexique, but ratio <= 4.0
    
    unique_candidates = sorted(list(set(STATIC_CANDIDATES)))
    
    for word in unique_candidates:
        word_lower = word.lower()
        if word_lower not in lexique:
            excluded_missing.append(word)
        else:
            stats = lexique[word_lower]
            fl = stats['freqlivres']
            ff = stats['freqfilms']
            ratio = (fl + 0.01) / (ff + 0.01)
            if ratio <= 4.0:
                excluded_low_ratio.append((word, fl, ff, ratio))
                
    print(f"Total Unique Candidates: {len(unique_candidates)}")
    print(f"Missing from Lexique383: {len(excluded_missing)}")
    print(excluded_missing)
    print(f"Excluded due to Low Ratio (<= 4.0): {len(excluded_low_ratio)}")
    
    excluded_low_ratio.sort(key=lambda x: x[3], reverse=True)
    print("\nTop Excluded due to Low Ratio (<= 4.0):")
    print(f"{'Word':<18} | {'Freq Livres':<12} | {'Freq Films':<12} | {'Ratio':<10}")
    print("-" * 60)
    for word, fl, ff, ratio in excluded_low_ratio[:30]:
        print(f"{word:<18} | {fl:<12.2f} | {ff:<12.2f} | {ratio:<10.2f}")
        
    # Q2. Why zero Expert words for Zipf books?
    # Let's check the Zipf Livres score distribution of the 119 literary candidates
    literary_cands = []
    for word in unique_candidates:
        word_lower = word.lower()
        if word_lower in lexique:
            stats = lexique[word_lower]
            fl = stats['freqlivres']
            ff = stats['freqfilms']
            ratio = (fl + 0.01) / (ff + 0.01)
            if ratio > 4.0:
                zb = math.log10(fl) + 3.0 if fl > 0 else 0.0
                zf = math.log10(ff) + 3.0 if ff > 0 else 0.0
                literary_cands.append((word, fl, zb))
                
    literary_cands.sort(key=lambda x: x[2])
    print("\nLowest Zipf Livres for literary candidates:")
    for word, fl, zb in literary_cands[:10]:
        print(f"{word}: Freq Livres = {fl:.4f}, Zipf = {zb:.2f}")

    # Q3. Analyze Wiktionnaire Category pages total
    cat_size = get_wiktionary_category_count("Termes soutenus en français")
    print(f"\nTotal pages in Catégorie:Termes soutenus en français: {cat_size}")
    
    # Q4. primary search based ONLY on Lexique.org database
    # Let's count how many words in Lexique383 satisfy:
    # 1. length >= 5
    # 2. starts with lowercase, no digit, no space
    # 3. Ratio (freqlivres + 0.01) / (freqfilms + 0.01) > 4.0
    # 4. We group by categories
    lex_literary = []
    for word, stats in lexique.items():
        if len(word) < 5:
            continue
        if ' ' in word or '-' in word or any(c.isdigit() for c in word):
            continue
        if not word.islower():
            continue
            
        fl = stats['freqlivres']
        ff = stats['freqfilms']
        ratio = (fl + 0.01) / (ff + 0.01)
        if ratio > 4.0:
            zb = math.log10(fl) + 3.0 if fl > 0 else 0.0
            zf = math.log10(ff) + 3.0 if ff > 0 else 0.0
            lex_literary.append({
                'word': word,
                'fl': fl,
                'ff': ff,
                'ratio': ratio,
                'zb': zb,
                'zf': zf,
                'cgrams': stats['cgrams']
            })
            
    print(f"\nTotal literary words in Lexique383 meeting criteria: {len(lex_literary)}")
    
    # Let's classify them according to user's new rules:
    # Debutant: Zipf_films >= 3.0
    # Expert: Zipf_films < 1.5 AND Zipf_books < 1.5
    # Intermediate: other
    new_cat = {
        'debutant': [],
        'expert': [],
        'intermediaire': []
    }
    
    for item in lex_literary:
        zf = item['zf']
        zb = item['zb']
        if zf >= 3.0:
            new_cat['debutant'].append(item)
        elif zf < 1.5 and zb < 1.5:
            new_cat['expert'].append(item)
        else:
            new_cat['intermediaire'].append(item)
            
    print(f"Classification under NEW rules:")
    print(f" - Débutant: {len(new_cat['debutant'])} words")
    print(f" - Intermédiaire: {len(new_cat['intermediaire'])} words")
    print(f" - Expert: {len(new_cat['expert'])} words")
    
    # Print a few samples of each
    print("\nSamples - Débutant:")
    new_cat['debutant'].sort(key=lambda x: x['ratio'], reverse=True)
    for item in new_cat['debutant'][:10]:
        print(f"  {item['word']} (Ratio={item['ratio']:.2f}, Zf={item['zf']:.2f}, Zb={item['zb']:.2f}, Cgrams={item['cgrams']})")
        
    print("\nSamples - Intermédiaire:")
    new_cat['intermediaire'].sort(key=lambda x: x['ratio'], reverse=True)
    for item in new_cat['intermediaire'][:10]:
        print(f"  {item['word']} (Ratio={item['ratio']:.2f}, Zf={item['zf']:.2f}, Zb={item['zb']:.2f}, Cgrams={item['cgrams']})")
        
    print("\nSamples - Expert:")
    new_cat['expert'].sort(key=lambda x: x['ratio'], reverse=True)
    for item in new_cat['expert'][:10]:
        print(f"  {item['word']} (Ratio={item['ratio']:.2f}, Zf={item['zf']:.2f}, Zb={item['zb']:.2f}, Cgrams={item['cgrams']})")

if __name__ == "__main__":
    main()
