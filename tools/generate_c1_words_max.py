import os
import json
import urllib.request
import urllib.parse
import zipfile
import csv
import math
import re

# We will run this from the tools/ directory or project root.
# Let's support finding the zip file in either the current directory or parent directory.
if os.path.exists("Lexique383.zip"):
    ZIP_PATH = "Lexique383.zip"
elif os.path.exists("tools/Lexique383.zip"):
    ZIP_PATH = "tools/Lexique383.zip"
elif os.path.exists("../Lexique383.zip"):
    ZIP_PATH = "../Lexique383.zip"
else:
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
                
                # Check islem (must be lemma)
                if row.get('islem') != '1':
                    continue
                
                # Check grammatical category
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
                    if cgram not in lexique[word]['cgrams']:
                        lexique[word]['cgrams'].append(cgram)
                else:
                    lexique[word] = {
                        'freqlivres': freqlivres,
                        'freqfilms': freqfilms,
                        'cgrams': [cgram]
                    }
    return lexique

def main():
    print(f"Using ZIP_PATH: {ZIP_PATH}")
    lexique = load_lexique()
    
    # Analyze entire Lexique database with Ratio > 2.0
    all_lex_words = []
    for word, stats in lexique.items():
        if len(word) < 5:
            continue
        if ' ' in word or '-' in word or any(c.isdigit() for c in word):
            continue
        if not re.match(r'^[a-zàâäéèêëîïôöùûüçœæ]+$', word):
            continue
            
        fl = stats['freqlivres']
        ff = stats['freqfilms']
        ratio = (fl + 0.01) / (ff + 0.01)
        
        if ratio > 2.0:
            zb = math.log10(fl) + 3.0 if fl > 0 else 0.0
            zf = math.log10(ff) + 3.0 if ff > 0 else 0.0
            all_lex_words.append({
                'word': word,
                'fl': fl,
                'ff': ff,
                'ratio': ratio,
                'zb': zb,
                'zf': zf,
                'cgrams': stats['cgrams']
            })
            
    print(f"Total literary lemmas (Ratio > 2.0) in Lexique383: {len(all_lex_words)}")
    
    # Classify under Proposed Zipf_films Only rules
    cats_films = {'debutant': [], 'intermediaire': [], 'expert': []}
    for item in all_lex_words:
        zf = item['zf']
        if zf >= 3.0:
            cats_films['debutant'].append(item)
        elif zf >= 1.5:
            cats_films['intermediaire'].append(item)
        else:
            cats_films['expert'].append(item)
            
    # Classify the STATIC_CANDIDATES list under Ratio > 2.0
    static_results = []
    unique_candidates = sorted(list(set(STATIC_CANDIDATES)))
    for word in unique_candidates:
        word_lower = word.lower()
        if word_lower in lexique:
            stats = lexique[word_lower]
            fl = stats['freqlivres']
            ff = stats['freqfilms']
            ratio = (fl + 0.01) / (ff + 0.01)
            if ratio > 2.0:
                zb = math.log10(fl) + 3.0 if fl > 0 else 0.0
                zf = math.log10(ff) + 3.0 if ff > 0 else 0.0
                static_results.append({
                    'word': word,
                    'fl': fl,
                    'ff': ff,
                    'ratio': ratio,
                    'zb': zb,
                    'zf': zf
                })
                
    static_cats = {'debutant': [], 'intermediaire': [], 'expert': []}
    for item in static_results:
        zf = item['zf']
        if zf >= 3.0:
            static_cats['debutant'].append(item)
        elif zf >= 1.5:
            static_cats['intermediaire'].append(item)
        else:
            static_cats['expert'].append(item)
            
    # Generate CSV of all extracted words
    csv_output_filename = "tools/c1_words_maximum_extraction_20260618.csv"
    with open(csv_output_filename, "w", encoding="utf-8", newline="") as csvfile:
        fieldnames = ["word", "freqlivres", "freqfilms", "ratio", "zipf_livres", "zipf_films", "cgrams", "tier"]
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
        writer.writeheader()
        
        # Sort and write
        all_lex_words.sort(key=lambda x: x['ratio'], reverse=True)
        for item in all_lex_words:
            zf = item['zf']
            tier = 'debutant' if zf >= 3.0 else ('intermediaire' if zf >= 1.5 else 'expert')
            writer.writerow({
                "word": item['word'],
                "freqlivres": f"{item['fl']:.4f}",
                "freqfilms": f"{item['ff']:.4f}",
                "ratio": f"{item['ratio']:.4f}",
                "zipf_livres": f"{item['zb']:.4f}",
                "zipf_films": f"{item['zf']:.4f}",
                "cgrams": ", ".join(item['cgrams']),
                "tier": tier
            })
            
    # Generate enhanced report
    report_lines = []
    report_lines.append("# Rapport de Vocabulaire Littéraire (C1) - Extraction Maximale")
    report_lines.append(f"Filtre appliqué : Ratio Littéraire (Livres / Films) > 2.0")
    report_lines.append(f"Classification : Débutant (Zipf films >= 3.0), Intermédiaire (1.5 <= Zipf films < 3.0), Expert (Zipf films < 1.5)")
    report_lines.append(f"Fichier de données CSV complet généré : [{os.path.basename(csv_output_filename)}](file:///{os.path.abspath(csv_output_filename)})")
    report_lines.append("\n" + "="*80 + "\n")
    
    report_lines.append("## PARTIE 1 : Extraction sur la liste statique 'RareWordsCandidates'")
    report_lines.append(f"Mots correspondants à Lexique383 : {len(static_results)} (sur {len(unique_candidates)})")
    report_lines.append(f" - Débutant : {len(static_cats['debutant'])} mots")
    report_lines.append(f" - Intermédiaire : {len(static_cats['intermediaire'])} mots")
    report_lines.append(f" - Expert : {len(static_cats['expert'])} mots")
    report_lines.append("\n")
    
    def process_static_tier(name, lst):
        lines = []
        lines.append(f"### {name}")
        lines.append("| Mot | Freq Livres | Freq Films | Ratio | Zipf Livres | Zipf Films |")
        lines.append("|---|---|---|---|---|---|")
        lst.sort(key=lambda x: x['ratio'], reverse=True)
        # Display all of them since it's a small candidate list
        for item in lst:
            lines.append(f"| **{item['word']}** | {item['fl']:.2f} | {item['ff']:.2f} | {item['ratio']:.2f} | {item['zb']:.2f} | {item['zf']:.2f} |")
        lines.append("\n")
        return lines

    report_lines.extend(process_static_tier("Débutant (Zipf films >= 3.0)", static_cats['debutant']))
    report_lines.extend(process_static_tier("Intermédiaire (1.5 <= Zipf films < 3.0)", static_cats['intermediaire']))
    report_lines.extend(process_static_tier("Expert (Zipf films < 1.5)", static_cats['expert']))
    
    report_lines.append("\n" + "="*80 + "\n")
    report_lines.append("## PARTIE 2 : Extraction GLOBALE sur TOUT Lexique.org (Lemmes NOM, ADJ, VER)")
    report_lines.append(f"Total des mots littéraires qualifiés : {len(all_lex_words)}")
    report_lines.append(f" - Débutant (Zipf films >= 3.0) : {len(cats_films['debutant'])} mots")
    report_lines.append(f" - Intermédiaire (1.5 <= Zipf films < 3.0) : {len(cats_films['intermediaire'])} mots")
    report_lines.append(f" - Expert (Zipf films < 1.5) : {len(cats_films['expert'])} mots")
    report_lines.append("\n")
    
    def process_global_tier(name, lst):
        lines = []
        lines.append(f"### Échantillon de la catégorie {name} (Top 250 par Ratio Littéraire)")
        lines.append("| Mot | Freq Livres | Freq Films | Ratio | Zipf Livres | Zipf Films | Catégorie grammaticale |")
        lines.append("|---|---|---|---|---|---|---|")
        lst.sort(key=lambda x: x['ratio'], reverse=True)
        # Display top 250 for maximum visibility in report
        for item in lst[:250]:
            lines.append(f"| **{item['word']}** | {item['fl']:.2f} | {item['ff']:.2f} | {item['ratio']:.2f} | {item['zb']:.2f} | {item['zf']:.2f} | {', '.join(item['cgrams'])} |")
        
        if len(lst) > 250:
            lines.append(f"\n*Et {len(lst) - 250} autres mots dans cette catégorie (voir le fichier CSV complet).*")
        lines.append("\n")
        return lines
        
    report_lines.extend(process_global_tier("Débutant", cats_films['debutant']))
    report_lines.extend(process_global_tier("Intermédiaire", cats_films['intermediaire']))
    report_lines.extend(process_global_tier("Expert", cats_films['expert']))
    
    # Write to output file
    output_filename = "tools/resultat_mots_test_c1_maximum_20260618.md"
    with open(output_filename, "w", encoding="utf-8") as out:
        out.write("\n".join(report_lines))
        
    print(f"Maximal report written to {output_filename}")
    print(f"Maximal CSV written to {csv_output_filename}")

if __name__ == "__main__":
    main()
