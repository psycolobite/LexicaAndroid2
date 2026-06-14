import os
import json
import re
import math
import sys
import csv
import time
import urllib.request
import urllib.parse
from datetime import datetime

# Normaliser la sortie console
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

CORPUS_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "rap_lyrics_corpus.json")
ZIP_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "Lexique383.zip")

OUTPUT_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "docs", "amelioration_de_la_fonction_de_recherche",
    "algorithme_de_presentation_des_extraits"
)

HEADERS = {
    'User-Agent': 'LexicaAndroid2/1.0 (vocabulaire francais; contact: lexica@test.com)'
}

def load_lexique():
    import zipfile
    lexique = {}
    if not os.path.exists(ZIP_PATH):
        print("Lexique383.zip introuvable.")
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
                
                # Conserver la fréquence maximale
                freq = max(freqlivres, freqfilms)
                zipf = math.log10(freq) + 3.0 if freq > 0 else 0.0
                
                if word in lexique:
                    lexique[word] = max(lexique[word], zipf)
                else:
                    lexique[word] = zipf
    return lexique

def clean_word(w):
    w = w.lower().strip()
    # Remplacer les ligatures typographiques courantes en français pour le matching Lexique383
    w = w.replace('œ', 'oe').replace('æ', 'ae')
    # Supprimer les apostrophes de début (l', d', m', j', t', s', c', n', qu')
    w = re.sub(r'^[ldmtscnj]’', '', w)
    w = re.sub(r'^[ldmtscnj]\'', '', w)
    w = re.sub(r'^qu’', '', w)
    w = re.sub(r'^qu\'', '', w)
    # Ne garder que les caractères alphabétiques français et tirets
    w = re.sub(r'[^a-zâäàéèùûüêëîïôöçñæœ-]', '', w)
    return w

def fetch_wiktionary_info(word):
    """
    Vérifie si le mot existe sur le Wiktionnaire et extrait :
    - S'il est tagué comme argot, populaire, familier, verlan
    - Sa première définition dans la section Français uniquement
    """
    for w_query in [word, word.capitalize()]:
        url = (
            f"https://fr.wiktionary.org/w/api.php?action=parse"
            f"&page={urllib.parse.quote(w_query)}&prop=text&redirects=true"
            f"&format=json&formatversion=2"
        )
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=5) as response:
                data = json.loads(response.read().decode('utf-8'))
                if 'error' in data:
                    continue
                html_content = data.get('parse', {}).get('text', '')
                if not html_content:
                    continue
                
                # S'assurer que la section Français existe spécifiquement
                french_idx = html_content.find('id="Français"')
                if french_idx == -1:
                    french_idx = html_content.find('Français')
                
                if french_idx == -1:
                    # Pas de section français
                    continue
                    
                html_french = html_content[french_idx:]
                next_h2 = html_french.find('<h2', 10)
                if next_h2 != -1:
                    html_french = html_french[:next_h2]
                    
                # Détecter le registre dans la section française
                is_slang = False
                slang_types = []
                lower_html = html_french.lower()
                
                registres = {
                    "argot": "argot",
                    "populaire": "populaire",
                    "familier": "familier",
                    "verlan": "verlan",
                    "néologisme": "neologisme"
                }
                for reg, label in registres.items():
                    if reg in lower_html:
                        is_slang = True
                        slang_types.append(label)
                
                # Extraire la première définition dans la section française
                ol_start = html_french.find('<ol>')
                definition = ""
                if ol_start != -1:
                    ol_end = html_french.find('</ol>', ol_start)
                    ol_content = html_french[ol_start:ol_end]
                    li_start = ol_content.find('<li>')
                    if li_start != -1:
                        li_end = ol_content.find('</li>', li_start)
                        li_content = ol_content[li_start+4:li_end]
                        definition = re.sub(r'<[^>]+>', '', li_content)
                        definition = re.sub(r'\s+', ' ', definition).strip()
                        if len(definition) > 200:
                            definition = definition[:197] + "..."
                
                status = ", ".join(slang_types) if slang_types else "standard"
                return True, status, definition
        except Exception:
            pass
    return False, "Non trouvé", ""

def main():
    print("==========================================================")
    print("  ANALYSE DU CORPUS RAP & EXTRACTION ARGOT C3")
    print("==========================================================")
    
    if not os.path.exists(CORPUS_PATH):
        print(f"Corpus introuvable à l'emplacement : {CORPUS_PATH}")
        print("Veuillez d'abord exécuter tools/fetch_rap_lyrics.py.")
        return
        
    with open(CORPUS_PATH, "r", encoding="utf-8") as f:
        corpus = json.load(f)
        
    print(f"Chargement du corpus : {len(corpus)} chansons.")
    print("Chargement du Lexique de référence...")
    lexique = load_lexique()
    print(f"Lexique de référence chargé ({len(lexique)} mots).")
    
    # Étape 1 : Analyse des fréquences et agrégations
    word_data = {}
    total_artists = set()
    max_pageviews = 1
    
    for song_id, song in corpus.items():
        artist = song['artist']
        total_artists.add(artist)
        pvs = song.get('pageviews', 0) or 0
        if pvs > max_pageviews:
            max_pageviews = pvs
            
        lyrics = song['lyrics']
        year = song.get('year') or 2020 # Par défaut 2020
        
        # Tokeniser les paroles
        words = re.split(r'[^a-zA-Z0-9âäàéèùûüêëîïôöçñæœ\-’\']+', lyrics)
        seen_in_song = set()
        
        for w in words:
            cw = clean_word(w)
            if len(cw) < 3:
                continue
            if cw in seen_in_song:
                continue
            seen_in_song.add(cw)
            
            if cw not in word_data:
                word_data[cw] = {
                    'word': cw,
                    'occurrences': 0,
                    'artists': set(),
                    'pageviews_sum': 0,
                    'years': []
                }
                
            word_data[cw]['occurrences'] += 1
            word_data[cw]['artists'].add(artist)
            word_data[cw]['pageviews_sum'] += pvs
            word_data[cw]['years'].append(year)

    print(f"Nombre total de mots uniques analysés : {len(word_data)}")
    
    # Étape 2 : Filtrer et Scorer
    candidates = []
    for cw, data in word_data.items():
        # Filtrer les entêtes Genius et mots très longs non valides
        if len(cw) > 15 and ("contributor" in cw or "translation" in cw or "english" in cw):
            continue
            
        # Exclure les contractions courantes sans apostrophe et petits mots grammaticaux ou scories
        exclusions = {
            "jai", "cest", "dla", "dun", "quun", "tua", "yen", "ten", "men", 
            "quil", "quon", "touts", "jen", "dans", "sans", "pour", 
            "avec", "tout", "tous", "mais", "plus", "meme", "bah", "wouh", 
            "hey", "yah", "oh-oh-oh", "lyrics"
        }
        if cw in exclusions:
            continue
            
        # Exclure les pronoms/verbes reliés par un tiret (ex: dis-moi, dis-leur)
        if "-" in cw:
            parts = cw.split("-")
            pronouns = {"moi", "toi", "soi", "lui", "leur", "en", "y", "nous", "vous", "ils", "elles", "t", "il", "elle", "on"}
            if any(p in pronouns for p in parts):
                continue

        # Exclure les stopwords anglais courants pour éviter la pollution par les titres/traductions Genius
        english_stopwords = {
            "you", "the", "and", "for", "with", "this", "that", "what", 
            "she", "his", "him", "her", "they", "them", "are", "was", 
            "were", "been", "have", "has", "had", "will", "would", 
            "can", "could", "like", "not", "but", "your", "its", "out"
        }
        if cw in english_stopwords:
            continue

        # Filtrer les mots classiques communs de Lexique383
        # On exclut les mots qui ont un Zipf élevé (> 2.3) dans la langue courante
        zipf = lexique.get(cw, 0.0)
        if zipf > 2.3:
            continue
            
        # Métriques
        artists_count = len(data['artists'])
        if artists_count < 2:
            # Rejeter les mots inventés par un seul artiste
            continue
            
        avg_year = sum(data['years']) / len(data['years'])
        
        # Normalisation des sous-scores
        dispersion_score = math.log10(1 + artists_count) / math.log10(1 + len(total_artists))
        popularity_score = math.log10(1 + data['pageviews_sum']) / math.log10(1 + max_pageviews) if max_pageviews > 1 else 0.5
        
        # Récence (2015 à 2026)
        recency_score = (avg_year - 2015) / (2026 - 2015)
        recency_score = max(0.0, min(1.0, recency_score))
        
        # Formule de score équilibrée
        # 40% Dispersion + 40% Popularité + 20% Récence
        score_rap = (0.40 * dispersion_score) + (0.40 * popularity_score) + (0.20 * recency_score)
        
        candidates.append({
            'word': cw,
            'score_rap': score_rap,
            'artists_count': artists_count,
            'occurrences': data['occurrences'],
            'pageviews': data['pageviews_sum'],
            'avg_year': avg_year,
            'zipf_lexique': zipf
        })
        
    candidates.sort(key=lambda x: x['score_rap'], reverse=True)
    print(f"Candidats potentiels après filtrage : {len(candidates)}")
    # Étape 3 : Récupérer des définitions et filtrer par statut Wiktionnaire
    print("\nFiltrage et récupération des définitions sur le Top 150 de l'API...")
    top_candidates = []
    untracked_candidates = []
    checked_count = 0
    
    for item in candidates[:150]:
        checked_count += 1
        print(f"  Vérification sémantique de '{item['word']}' ({checked_count}/150)...")
        found, status, definition = fetch_wiktionary_info(item['word'])
        
        # Filtres d'exclusion de bruit linguistique post-Wiktionnaire
        if status in ["standard", "Non français", "Erreur API"]:
            continue
            
        if status == "Non trouvé":
            item['wikt_status'] = "Non trouvé"
            item['definition'] = "Potentiel argot émergent non répertorié."
            untracked_candidates.append(item)
            continue
            
        item['wikt_found'] = found
        item['wikt_status'] = status
        item['definition'] = definition if definition else "Néologisme ou expression récurrente issue du rap français contemporain."
        top_candidates.append(item)
        
        if len(top_candidates) >= 30:
            break
            
        time.sleep(0.4)
        
    print(f"  ✓ {len(top_candidates)} candidats argotiques de haute qualité validés.")
    print(f"  ✓ {len(untracked_candidates)} candidats non répertoriés sauvegardés pour analyse.")
        
    # Enregistrer le CSV des résultats rap C3
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    csv_path = os.path.join(OUTPUT_DIR, "candidates_slang_rap.csv")
    with open(csv_path, mode='w', encoding='utf-8', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow(['Mot', 'Score_Rap', 'Artists_Count', 'Occurrences', 'Pageviews', 'Avg_Year', 'Zipf_Lexique', 'Wikt_Status', 'Definition'])
        for item in top_candidates:
            writer.writerow([
                item['word'],
                f"{item['score_rap']:.3f}",
                item['artists_count'],
                item['occurrences'],
                item['pageviews'],
                f"{item['avg_year']:.1f}",
                f"{item['zipf_lexique']:.2f}",
                item.get('wikt_status', 'N/A'),
                item.get('definition', '')
            ])

    # Enregistrer le CSV des candidats non répertoriés (pour analyse manuelle / bleeding-edge)
    untracked_csv_path = os.path.join(OUTPUT_DIR, "candidates_slang_rap_untracked.csv")
    with open(untracked_csv_path, mode='w', encoding='utf-8', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow(['Mot', 'Score_Rap', 'Artists_Count', 'Occurrences', 'Pageviews', 'Avg_Year', 'Zipf_Lexique', 'Wikt_Status', 'Definition'])
        for item in untracked_candidates:
            writer.writerow([
                item['word'],
                f"{item['score_rap']:.3f}",
                item['artists_count'],
                item['occurrences'],
                item['pageviews'],
                f"{item['avg_year']:.1f}",
                f"{item['zipf_lexique']:.2f}",
                item.get('wikt_status', 'Non trouvé'),
                item.get('definition', '')
            ])
            
            
    # Écrire le rapport Markdown de validation
    report_path = os.path.join(OUTPUT_DIR, "resultat_mots_test_c3_rap.md")
    md_lines = [
        "# Rapport d'Extraction C3 — Analyse de l'Argot du Rap Français",
        "",
        f"**Date :** {datetime.now().strftime('%d/%m/%Y %H:%M')}",
        f"**Corpus analysé :** Paroles de rap français (Genius)",
        "",
        "## Formule de Score Argot Rap",
        "```",
        "Score_Rap = (0.40 × Dispersion_Artistes) + (0.40 × Popularité_Genius) + (0.20 × Récence_Année)",
        "```",
        "",
        "## Top 30 Candidats extraits du Rap Français",
        "",
        "| # | Mot | Score | Artistes | Freq | Année moyenne | Statut Wiktionnaire | Définition |",
        "|---|---|---|---|---|---|---|---|"
    ]
    
    for idx, item in enumerate(top_candidates):
        md_lines.append(
            f"| {idx+1} | **{item['word']}** | {item['score_rap']:.3f} | {item['artists_count']} | {item['occurrences']} | "
            f"{item['avg_year']:.1f} | *{item.get('wikt_status', 'N/A')}* | {item.get('definition', '')} |"
        )
        
    with open(report_path, "w", encoding="utf-8") as f:
        f.write("\n".join(md_lines))
        
    print(f"\n✓ Analyse terminée avec succès.")
    print(f"  CSV : {csv_path}")
    print(f"  Rapport : {report_path}")

if __name__ == "__main__":
    main()
