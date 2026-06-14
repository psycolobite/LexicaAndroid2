import os
import re
import math
import sys
import zipfile
import csv

# S'assurer du bon encodage de la console
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

ZIP_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "Lexique383.zip")

def load_lexique():
    lexique = {}
    if not os.path.exists(ZIP_PATH):
        print("Avertissement : Lexique383.zip introuvable pour la classification fine.")
        return {}
    
    with zipfile.ZipFile(ZIP_PATH, 'r') as z:
        tsv_filename = None
        for name in z.namelist():
            if name.endswith('.tsv') or name.endswith('.txt'):
                tsv_filename = name
                break
        if not tsv_filename:
            return {}
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
                
                freq = max(freqlivres, freqfilms)
                zipf = math.log10(freq) + 3.0 if freq > 0 else 0.0
                
                if word in lexique:
                    lexique[word] = max(lexique[word], zipf)
                else:
                    lexique[word] = zipf
    return lexique

def clean_and_tokenize(text):
    # Séparer les phrases (sur les points suivis d'espaces/majuscules)
    sentences = re.split(r'(?<!\w\.\w.)(?<![A-Z][a-z]\.)(?<=\.|\?)\s', text)
    sentences = [s.strip() for s in sentences if s.strip()]
    
    # Tokeniser les mots
    words = re.findall(r'[a-zA-Z0-9âäàéèùûüêëîïôöçñæœ\-’\']+', text)
    cleaned_words = []
    for w in words:
        w_clean = w.lower().strip()
        # Enlever apostrophe de début
        w_clean = re.sub(r'^[ldmtscnj]’', '', w_clean)
        w_clean = re.sub(r'^[ldmtscnj]\'', '', w_clean)
        w_clean = re.sub(r'^qu’', '', w_clean)
        w_clean = re.sub(r'^qu\'', '', w_clean)
        w_clean = re.sub(r'[^a-zâäàéèùûüêëîïôöçñæœ-]', '', w_clean)
        if len(w_clean) > 0:
            cleaned_words.append(w_clean)
            
    return sentences, cleaned_words

def analyze_text_difficulty(text, lexique=None):
    if not lexique:
        lexique = {}
        
    sentences, words = clean_and_tokenize(text)
    
    if not words or not sentences:
        return {
            "difficulty": "Indéterminée",
            "avg_sentence_length": 0,
            "ttr": 0,
            "rare_words_ratio": 0,
            "subjunctive_count": 0
        }
        
    # 1. Longueur moyenne des phrases
    avg_sentence_length = len(words) / len(sentences)
    
    # 2. Type-Token Ratio (TTR) sur les 1000 premiers mots pour normaliser
    sample_words = words[:1000]
    unique_words = set(sample_words)
    ttr = len(unique_words) / len(sample_words) if sample_words else 0
    
    # 3. Ratio de mots rares (Zipf < 2.0 dans Lexique383)
    rare_words = 0
    words_in_lexique = 0
    for w in words:
        if w in lexique:
            words_in_lexique += 1
            if lexique[w] < 2.0:
                rare_words += 1
        else:
            # Hors lexique de référence = potentiellement très rare/néologisme
            rare_words += 1
            
    rare_words_ratio = rare_words / len(words)
    
    # 4. Proxy de conjugaisons complexes (Subjonctif Imparfait / Conditionnel Passé endings)
    # Exemples : -assent, -issent, -ât, -ît, -ussent, -ût
    subjunctive_patterns = r'\b\w+(assent|issent|ussent|assions|issions|ussions|ât|ît|ût)\b'
    subjunctive_matches = re.findall(subjunctive_patterns, text, re.IGNORECASE)
    subjunctive_ratio = len(subjunctive_matches) / len(sentences)
    
    # Algorithme multicritère de classification de la difficulté
    # On calcule un score composite de difficulté de 0 à 100
    # Longueur phrase (poids 40%) : max 50 mots
    score_len = min(1.0, avg_sentence_length / 45.0) * 40
    # Mots rares (poids 30%) : max 15% de mots rares
    score_rare = min(1.0, rare_words_ratio / 0.15) * 30
    # TTR (poids 20%) : TTR max à 0.65
    score_ttr = min(1.0, ttr / 0.65) * 20
    # Subjonctif (poids 10%) : max 0.1 par phrase
    score_subj = min(1.0, subjunctive_ratio / 0.10) * 10
    
    composite_score = score_len + score_rare + score_ttr + score_subj
    
    if composite_score >= 70 or (avg_sentence_length > 35 and rare_words_ratio > 0.08):
        difficulty = "Expert"
    elif composite_score >= 45 or (avg_sentence_length > 22 and rare_words_ratio > 0.04):
        difficulty = "Avancé"
    elif composite_score >= 20 or (avg_sentence_length > 12):
        difficulty = "Intermédiaire"
    else:
        difficulty = "Débutant"
        
    return {
        "difficulty": difficulty,
        "composite_score": composite_score,
        "avg_sentence_length": avg_sentence_length,
        "ttr": ttr,
        "rare_words_ratio": rare_words_ratio,
        "subjunctive_count": len(subjunctive_matches)
    }

if __name__ == "__main__":
    # Petit test de démonstration
    lex = load_lexique()
    
    test_text_debutant = (
        "Le petit chat noir dort sous la table. Le soleil brille aujourd'hui dans le ciel. "
        "Il fait beau et chaud. Nous allons faire une promenade dans la forêt."
    )
    
    test_text_expert = (
        "Longtemps, je me suis couché de bonne heure. Parfois, à peine ma bougie éteinte, "
        "mes yeux se fermaient si vite que je n'avais pas le temps de me dire : « Je m'endors. » "
        "Et, une demi-heure après, la pensée qu'il était temps de chercher le sommeil m'éveillait ; "
        "je voulais poser le volume que je croyais avoir encore dans les mains et souffler ma lumière ; "
        "je n'avais pas cessé en dormant de faire des réflexions sur ce que je venais de lire, "
        "mais ces réflexions avaient pris un tour un peu particulier ; il me semblait que j'étais "
        "moi-même ce dont parlait l'ouvrage : une église, un quatuor, la rivalité de François Ier et de Charles-Quint."
    )
    
    print("\n--- TEST CLASSIFICATION DE DIFFICULTÉ ---")
    res_deb = analyze_text_difficulty(test_text_debutant, lex)
    print(f"Texte Débutant : Niveau={res_deb['difficulty']} (Score composite={res_deb['composite_score']:.1f})")
    print(f"  - Longueur moyenne phrase : {res_deb['avg_sentence_length']:.2f}")
    print(f"  - Mots rares : {res_deb['rare_words_ratio']*100:.1f}%")
    
    res_exp = analyze_text_difficulty(test_text_expert, lex)
    print(f"\nTexte Proust : Niveau={res_exp['difficulty']} (Score composite={res_exp['composite_score']:.1f})")
    print(f"  - Longueur moyenne phrase : {res_exp['avg_sentence_length']:.2f}")
    print(f"  - Mots rares : {res_exp['rare_words_ratio']*100:.1f}%")
    print(f"  - Occurrences subjonctif/conjugaisons complexes : {res_exp['subjunctive_count']}")
