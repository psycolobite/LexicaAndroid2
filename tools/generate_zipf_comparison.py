import zipfile
import csv
import math
import re
import random

ZIP_PATH = "Lexique383.zip"

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
                
                # Filter criteria: lemma only
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
    lexique = load_lexique()
    
    # Filter valid lemmas spelling-wise
    valid_lemmas = []
    for word, stats in lexique.items():
        if len(word) < 5:
            continue
        if ' ' in word or '-' in word or any(c.isdigit() for c in word):
            continue
        if not re.match(r'^[a-zàâäéèêëîïôöùûüçœæ]+$', word):
            continue
            
        fl = stats['freqlivres']
        ff = stats['freqfilms']
        
        # Calculate Zipf scores
        zf_films = math.log10(ff) + 3.0 if ff > 0 else 0.0
        zf_books = math.log10(fl) + 3.0 if fl > 0 else 0.0
        zf_combined = math.log10((fl + ff) / 2.0) + 3.0 if (fl + ff) > 0 else 0.0
        
        valid_lemmas.append({
            'word': word,
            'fl': fl,
            'ff': ff,
            'zf_films': zf_films,
            'zf_books': zf_books,
            'zf_combined': zf_combined,
            'cgrams': stats['cgrams']
        })
        
    print(f"Total valid lemmas loaded: {len(valid_lemmas)}")
    
    # We will build three sections: Films, Livres, Combined
    # For each, we categorize: Débutant (>= 3.0), Intermédiaire (1.5 - 3.0), Expert (< 1.5)
    # We want to sample between 300 words for each category to give a good representative pack.
    # To keep it interesting, we'll sort by frequency/Zipf score descending and take the top 300.
    
    def partition_and_sample(key_name, size=300):
        debutant = []
        intermediaire = []
        expert = []
        
        for item in valid_lemmas:
            val = item[key_name]
            if val >= 3.0:
                debutant.append(item)
            elif val >= 1.5:
                intermediaire.append(item)
            else:
                expert.append(item)
                
        # Sort descending by Zipf score
        debutant.sort(key=lambda x: x[key_name], reverse=True)
        intermediaire.sort(key=lambda x: x[key_name], reverse=True)
        expert.sort(key=lambda x: x[key_name], reverse=True)
        
        return {
            'debutant': debutant[:size],
            'debutant_total': len(debutant),
            'intermediaire': intermediaire[:size],
            'intermediaire_total': len(intermediaire),
            'expert': expert[:size],
            'expert_total': len(expert)
        }
        
    print("Categorizing by Films...")
    films_data = partition_and_sample('zf_films')
    print("Categorizing by Livres...")
    books_data = partition_and_sample('zf_books')
    print("Categorizing by Combined...")
    combined_data = partition_and_sample('zf_combined')
    
    report_lines = []
    report_lines.append("# Comparatif des Classifications Zipf (Lexique.org)")
    report_lines.append("Ce fichier montre les mots classés par difficulté selon 3 approches différentes (Films, Livres, et Combiné).")
    report_lines.append("Chaque catégorie contient un échantillon des 300 premiers mots classés par ordre de fréquence décroissante.")
    report_lines.append("\n" + "="*80 + "\n")
    
    def write_section(title, data, key_name):
        sec = []
        sec.append(f"## 🎬 CLASSIFICATION : {title}")
        sec.append(f"- **Débutant (Zipf >= 3.0)** : {data['debutant_total']} mots trouvés (échantillon de {len(data['debutant'])})")
        sec.append(f"- **Intermédiaire (1.5 <= Zipf < 3.0)** : {data['intermediaire_total']} mots trouvés (échantillon de {len(data['intermediaire'])})")
        sec.append(f"- **Expert (Zipf < 1.5)** : {data['expert_total']} mots trouvés (échantillon de {len(data['expert'])})")
        sec.append("\n")
        
        for level in ['debutant', 'intermediaire', 'expert']:
            sec.append(f"### Niveau : {level.upper()}")
            words_formatted = []
            for item in data[level]:
                val = item[key_name]
                words_formatted.append(f"{item['word']} ({val:.2f})")
            sec.append(", ".join(words_formatted))
            sec.append("\n")
        return sec
        
    report_lines.extend(write_section("FILMS (Sous-titres de films / langage parlé)", films_data, 'zf_films'))
    report_lines.append("\n" + "="*80 + "\n")
    report_lines.extend(write_section("LIVRES (Corpus écrit / littéraire)", books_data, 'zf_books'))
    report_lines.append("\n" + "="*80 + "\n")
    report_lines.extend(write_section("MÉLANGÉ (Films + Livres combinés)", combined_data, 'zf_combined'))
    
    output_filename = "resultat_zipf_comparatif.md"
    with open(output_filename, "w", encoding="utf-8") as out:
        out.write("\n".join(report_lines))
        
    print(f"Comparative report written to {output_filename}")

if __name__ == "__main__":
    main()
