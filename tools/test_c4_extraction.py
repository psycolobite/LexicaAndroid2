"""
=============================================================================
C4 Pipeline A — Extraction de la Word Reserve "Culture Générale"
Curiosités lexicales, LGBT & Diversité, et Genre Grammatical Confus
=============================================================================
"""

import sys
import io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

import os
import json
import urllib.request
import urllib.parse
import zipfile
import csv
import math
import time
import re
from datetime import datetime

HEADERS = {'User-Agent': 'LexicaAndroid2/1.0 (vocabulaire francais; contact: lexica@test.com)'}

OUTPUT_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "docs", "amelioration_de_la_fonction_de_recherche",
    "algorithme_de_presentation_des_extraits"
)

# ============================================================================
# BASE DE DONNÉES LOCALE DE RÉFÉRENCE : MOTS À GENRE GRAMMATICAL CONFUS
# ============================================================================
GENDER_CONFUSED_WORDS = {
    "oasis": ("f", "Lieu de végétation dans un désert (souvent confondu avec le masculin : une oasis)."),
    "tentacule": ("m", "Appendice de forme allongée de certains invertébrés (souvent confondu avec le féminin : un tentacule)."),
    "apothéose": ("f", "Glorification, couronnement d'un parcours (souvent confondu avec le masculin : une apothéose)."),
    "abysse": ("m", "Fosse sous-marine très profonde (souvent confondu avec le féminin : un abysse)."),
    "effluve": ("m", "Émanation odorante (souvent confondu avec le féminin : un effluve)."),
    "orchidée": ("f", "Fleur exotique complexe (souvent confondue avec le masculin : une orchidée)."),
    "épithète": ("f", "Adjectif qualifiant directement un nom (souvent confondue avec le masculin : une épithète)."),
    "hélice": ("f", "Organe propulseur rotatif (souvent confondue avec le masculin : une hélice)."),
    "pétale": ("m", "Partie colorée de la fleur (souvent confondu avec le féminin : un pétale)."),
    "autoroute": ("f", "Voie de circulation rapide à chaussées séparées (parfois confondue au masculin par les apprenants)."),
    "stalactite": ("f", "Concrétion calcaire qui descend du plafond des grottes (souvent confondue avec le masculin)."),
    "stalagmite": ("f", "Concrétion calcaire qui monte du sol des grottes (souvent confondue avec le masculin)."),
    "en-tête": ("m", "Indication imprimée au sommet d'une lettre (souvent confondu avec le féminin)."),
    "après-midi": ("m_f", "Partie de la journée qui va de midi au soir (le ou la après-midi sont admis, source fréquente d'hésitation)."),
    "armistice": ("m", "Convention suspendant les hostilités (souvent confondu avec le féminin : un armistice)."),
    "anagramme": ("f", "Mot obtenu en changeant l'ordre des lettres d'un autre (souvent confondue au masculin : une anagramme)."),
    "ébène": ("f", "Bois précieux très noir et lourd (souvent confondu avec le masculin : une ébène)."),
    "insigne": ("m", "Marque distinctive d'une dignité ou d'une fonction (souvent confondu avec le féminin : un insigne)."),
    "orbite": ("f", "Trajectoire fermée d'un corps céleste (souvent confondue avec le masculin : une orbite)."),
    "astérisque": ("m", "Signe typographique en forme d'étoile (souvent confondu avec le féminin : un astérisque)."),
    "interstice": ("m", "Tout petit espace vide entre les parties d'un corps (souvent confondu avec le féminin : un interstice)."),
    "exode": ("m", "Départ en masse d'un peuple ou d'une population (souvent confondu avec le féminin : un exode)."),
    "hémisphère": ("m", "Moitié du globe terrestre ou du cerveau (souvent confondu avec le féminin : un hémisphère)."),
    "épisode": ("m", "Événement formant un tout dans une suite d'actions (parfois confondu au féminin)."),
    "optimum": ("m", "Le point ou l'état le plus favorable (souvent confondu avec le féminin)."),
    "antidote": ("m", "Substance combattant un poison (souvent confondu avec le féminin : un antidote)."),
    "appendice": ("m", "Partie ajoutée ou prolongement d'un organe (souvent confondu avec le féminin : un appendice)."),
    "azalée": ("f", "Arbrisseau à fleurs printanières (souvent confondu avec le masculin : une azalée)."),
    "échappatoire": ("f", "Moyen de se tirer d'embarras, dérobade (souvent confondue avec le masculin : une échappatoire)."),
    "immondice": ("f", "Ordure ménagère, déchet dégoûtant (généralement pluriel, souvent confondu avec le masculin)."),
    "éloge": ("m", "Discours de louange (souvent confondu avec le féminin : un éloge)."),
    "escompte": ("m", "Déduction faite sur un paiement anticipé (souvent confondu avec le féminin : un escompte)."),
    "octave": ("f", "Intervalle de huit notes en musique (parfois confondue avec le masculin)."),
    "écritoire": ("f", "Nécessaire d'écriture (souvent confondue avec le masculin : une écritoire)."),
    "effigie": ("f", "Représentation du visage d'une personne sur une monnaie ou médaille (souvent confondue)."),
    "aéronef": ("m", "Véhicule capable de s'élever et de circuler dans les airs (souvent confondu avec le féminin)."),
}

# ============================================================================
# SEED WORDS — Graines de référence (Culture G standard, LGBTQIA+ et Diversité)
# ============================================================================
SEED_WORDS = [
    # Curiosités
    "callipyge", "lucifuge", "anachronique", "synesthésie", "mentor", "codex", 
    "palindrome", "chimérique", "antédiluvien", "ubuesque", "kafkaïen", "nycthémère", 
    "sybarite", "vernaculaire", "zinzolin", "turlupiner",
    # Diversité & Orientations
    "pansexuel", "demisexuel", "asexuel", "bisexuel", "saphique", "queer", 
    "non-binaire", "cisgenre", "transgenre", "aromantique", "polyamoureux", 
    "hétéroflexible", "intersectionnel", "cisnormatif", "hétéronormatif", "agenre"
]

def get_lexique_data():
    zip_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "Lexique383.zip")
    lexique = {}
    if not os.path.exists(zip_path):
        return {}
    with zipfile.ZipFile(zip_path, 'r') as z:
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
                
                if word in lexique:
                    lexique[word]['freqlivres'] += freqlivres
                    lexique[word]['freqfilms'] += freqfilms
                else:
                    lexique[word] = {'freqlivres': freqlivres, 'freqfilms': freqfilms}
    return lexique

def get_category_members(category, max_pages=150):
    words = []
    encoded_cat = urllib.parse.quote(f"Catégorie:{category}")
    url = (
        f"https://fr.wiktionary.org/w/api.php?action=query"
        f"&list=categorymembers&cmtitle={encoded_cat}"
        f"&cmlimit=150&cmtype=page&cmnamespace=0&format=json"
    )
    try:
        req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            members = data.get('query', {}).get('categorymembers', [])
            for m in members:
                words.append(m['title'])
    except Exception as e:
        print(f"Error category {category}: {e}")
    return words

def compute_morpho_score(word):
    """Calcule l'étrangeté morphologique du mot (lettres rares, longueur atypique)."""
    score = 0.0
    rare_letters = ['z', 'y', 'x', 'k', 'w', 'h', 'ï', 'ê', 'â']
    for letter in rare_letters:
        if letter in word.lower():
            score += 0.15
    # Palindrome bonus
    if word.lower() == word.lower()[::-1] and len(word) > 3:
        score += 0.30
    return min(1.0, score + (len(word) / 20.0))

def main():
    print("==========================================================")
    print("  C4 PIPELINE A — EXTRACTION DE LA CULTURE GÉNÉRALE")
    print("==========================================================")
    
    lexique = get_lexique_data()
    all_candidates = {}

    # 1. Charger les mots à genre confus
    for word, (gender, defn) in GENDER_CONFUSED_WORDS.items():
        all_candidates[word.lower()] = {
            'word': word,
            'source': 'Genre Grammatical Confus',
            'definition': defn,
            'orality_boost': 0.3,
            'morpho_boost': 0.2
        }

    # 2. Charger depuis Wiktionnaire "Mots insolites" et "LGBT"
    insolite_words = get_category_members("Mots insolites en français")
    if not insolite_words:
        insolite_words = get_category_members("Mots rares en français")
    if not insolite_words:
        insolite_words = get_category_members("Termes rares en français")
    print(f"  → {len(insolite_words)} mots insolites/rares récupérés.")
    for w in insolite_words:
        if w.lower() not in all_candidates and not w.startswith("Catégorie:") and not w.startswith("Annexe:"):
            all_candidates[w.lower()] = {
                'word': w,
                'source': 'Mots insolites',
                'definition': '',
                'orality_boost': 0.1,
                'morpho_boost': 0.3
            }

    lgbt_words = get_category_members("LGBT en français")
    if not lgbt_words:
        lgbt_words = get_category_members("Lexique en français de la sexualité")
    print(f"  → {len(lgbt_words)} mots LGBT/diversité récupérés.")
    for w in lgbt_words:
        if w.lower() not in all_candidates and not w.startswith("Catégorie:") and not w.startswith("Annexe:"):
            all_candidates[w.lower()] = {
                'word': w,
                'source': 'LGBT & Orientations',
                'definition': '',
                'orality_boost': 0.2,
                'morpho_boost': 0.1
            }

    # Ajouter les graines de référence si absentes
    for seed in SEED_WORDS:
        if seed.lower() not in all_candidates:
            all_candidates[seed.lower()] = {
                'word': seed,
                'source': 'Graine de référence',
                'definition': '',
                'orality_boost': 0.2,
                'morpho_boost': 0.2
            }

    # Filtrer et scorer
    scored_candidates = []
    for word_lower, info in all_candidates.items():
        if len(word_lower) < 3:
            continue
        
        # Scoring morphologique
        morpho = compute_morpho_score(word_lower) + info['morpho_boost']
        
        # Rareté / Accessibilité (basée sur le Zipf)
        zipf = 0.0
        if word_lower in lexique:
            fl = lexique[word_lower]['freqlivres']
            ff = lexique[word_lower]['freqfilms']
            freq = max(fl, ff)
            if freq > 0:
                zipf = math.log10(freq) + 3.0
        
        # Les mots de culture générale doivent être rares mais compréhensibles (Zipf dans [1.0, 4.0])
        zipf_score = 1.0 - abs(2.5 - zipf)/2.5 if zipf > 0 else 0.4
        
        score_final = (0.50 * morpho) + (0.50 * zipf_score)
        
        scored_candidates.append({
            'word': info['word'],
            'source': info['source'],
            'score_c4': score_final,
            'morpho_score': morpho,
            'zipf': zipf,
            'definition': info['definition']
        })

    scored_candidates.sort(key=lambda x: x['score_c4'], reverse=True)

    # Récupérer les définitions pour le Top 60
    print("\n  Récupération des définitions pour le Top 60...")
    
    def fetch_definition(word):
        url = (
            f"https://fr.wiktionary.org/w/api.php?action=parse"
            f"&page={urllib.parse.quote(word)}&prop=text&redirects=true"
            f"&format=json&formatversion=2"
        )
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=5) as response:
                data = json.loads(response.read().decode('utf-8'))
                html = data.get('parse', {}).get('text', '')
                if not html:
                    return None
                
                idx = html.find('id="Français"')
                if idx == -1:
                    idx = html.find('Français')
                if idx != -1:
                    html = html[idx:]
                    next_h2 = html.find('<h2', 10)
                    if next_h2 != -1:
                        html = html[:next_h2]
                
                ol_start = html.find('<ol>')
                if ol_start != -1:
                    ol_end = html.find('</ol>', ol_start)
                    ol_content = html[ol_start:ol_end]
                    li_start = ol_content.find('<li>')
                    if li_start != -1:
                        li_end = ol_content.find('</li>', li_start)
                        li_content = ol_content[li_start+4:li_end]
                        definition = re.sub(r'<[^>]+>', '', li_content)
                        definition = re.sub(r'\s+', ' ', definition).strip()
                        if len(definition) > 300:
                            definition = definition[:297] + "..."
                        return definition
        except Exception:
            pass
        return None

    for idx, item in enumerate(scored_candidates[:60]):
        if not item['definition']:
            definition = fetch_definition(item['word'])
            if definition:
                item['definition'] = definition
            else:
                item['definition'] = f"Terme lié à {item['source']}."
            time.sleep(0.5)

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    
    # Écrire le CSV
    csv_path = os.path.join(OUTPUT_DIR, "candidates_culture_g.csv")
    with open(csv_path, mode='w', encoding='utf-8', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow(['Mot', 'Score_C4', 'Morpho_Score', 'Zipf', 'Source', 'Définition'])
        for item in scored_candidates:
            writer.writerow([
                item['word'],
                f"{item['score_c4']:.3f}",
                f"{item['morpho_score']:.2f}",
                f"{item['zipf']:.2f}",
                item['source'],
                item['definition']
            ])

    # Écrire le rapport Markdown
    report_path = os.path.join(OUTPUT_DIR, "resultat_mots_test_c4.md")
    md_lines = [
        "# Rapport d'Extraction C4 — Word Reserve « Culture Générale »",
        "",
        f"**Date :** {datetime.now().strftime('%d/%m/%Y %H:%M')}",
        f"**Candidats collectés :** {len(scored_candidates)}",
        "",
        "## Formule de score C4",
        "```",
        "Score_C4 = (0.50 × Morpho_Score) + (0.50 × Zipf_Accessibility_Score)",
        "```",
        "",
        "## Top 50 Candidats Culture Générale",
        "",
        "| # | Mot | Score C4 | Morpho | Zipf | Source | Définition |",
        "|---|---|---|---|---|---|---|"
    ]
    for idx, item in enumerate(scored_candidates[:50]):
        md_lines.append(
            f"| {idx+1} | **{item['word']}** | {item['score_c4']:.3f} | "
            f"{item['morpho_score']:.2f} | {item['zipf']:.2f} | {item['source']} | {item['definition']} |"
        )

    with open(report_path, "w", encoding="utf-8") as f:
        f.write("\n".join(md_lines))

    print(f"\n  ✓ Extraction C4 terminée avec succès. {len(scored_candidates)} mots extraits.")
    print(f"  CSV : {csv_path}")
    print(f"  Rapport : {report_path}")

if __name__ == "__main__":
    main()
