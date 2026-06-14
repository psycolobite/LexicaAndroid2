import zipfile
import csv
import math
import re

ZIP_PATH = "Lexique383.zip"

def main():
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
                
                # Filter criteria:
                # 1. Must be a lemma (islem == '1')
                if row.get('islem') != '1':
                    continue
                
                # 2. Grammatical category must be NOM, ADJ, or VER
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
                    
    # Now filter by:
    # - len >= 5
    # - no special characters (only lowercase a-z, including accents)
    # - Ratio > 4.0
    lex_literary = []
    for word, stats in lexique.items():
        if len(word) < 5:
            continue
        if ' ' in word or '-' in word or any(c.isdigit() for c in word):
            continue
        # Allow only alphabetical letters and accents (standard French)
        if not re.match(r'^[a-zàâäéèêëîïôöùûüçœæ]+$', word):
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
            
    print(f"Total literary lemmas (NOM, ADJ, VER) in Lexique383: {len(lex_literary)}")
    
    # Classify under NEW rules based entirely on Zipf_films:
    # Débutant: Zipf_films >= 3.0
    # Intermédiaire: 1.5 <= Zipf_films < 3.0
    # Expert: Zipf_films < 1.5
    new_cat = {
        'debutant': [],
        'intermediaire': [],
        'expert': []
    }
    
    for item in lex_literary:
        zf = item['zf']
        if zf >= 3.0:
            new_cat['debutant'].append(item)
        elif zf >= 1.5:
            new_cat['intermediaire'].append(item)
        else:
            new_cat['expert'].append(item)
            
    print(f"Classification under Zipf_films Only rules:")
    print(f" - Débutant (Zipf_films >= 3.0): {len(new_cat['debutant'])} words")
    print(f" - Intermédiaire (1.5 <= Zipf_films < 3.0): {len(new_cat['intermediaire'])} words")
    print(f" - Expert (Zipf_films < 1.5): {len(new_cat['expert'])} words")
    
    # Print sample
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
