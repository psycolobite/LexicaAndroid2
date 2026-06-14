"""
=============================================================================
C3 Pipeline A — Extraction de la Word Reserve "Dernier Cri"
Détection d'expressions populaires trending via Wiktionnaire + Pageviews
=============================================================================

STRATÉGIE INNOVANTE (Documentée pour les futurs agents) :
---------------------------------------------------------
Contrairement à C1 (centroïde embeddings littéraire) et C2 (multi-ancres
sémantiques rhétoriques), les mots d'argot n'ont PAS de cohérence sémantique
— ils partagent un REGISTRE, pas un SENS. L'approche par embeddings est
donc inapplicable.

À la place, on exploite un proxy indirect du buzz linguistique :
→ Quand un mot d'argot se propage viralement, les gens qui ne le connaissent
  pas vont le chercher sur Wiktionnaire → PIC DE PAGEVIEWS.
→ Le "Trend Ratio" (vues récentes / vues historiques) mesure si un mot
  est EN TRAIN de buzzer (ratio > 1.0) ou s'il est déjà établi (ratio < 1.0).

L'algorithme combine 3 signaux :
  1. BUZZ     (40%) : Trend Ratio des pageviews Wiktionnaire
  2. RECENCY  (35%) : Date d'ajout aux catégories Wiktionnaire 
  3. ORALITÉ  (25%) : Ratio freqFilms/freqLivres de Lexique383

Sources de données :
  - Wiktionnaire API (catégories : argot, familier, néologismes, etc.)
  - Wikimedia Pageviews API (fr.wiktionary.org)
  - Lexique383 (Lexique.org)
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
from datetime import datetime, timedelta

HEADERS = {'User-Agent': 'LexicaAndroid2/1.0 (vocabulaire francais; contact: lexica@test.com)'}

OUTPUT_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "docs", "amelioration_de_la_fonction_de_recherche",
    "algorithme_de_presentation_des_extraits"
)

# ============================================================================
# BLACKLIST — Mots à exclure (injures gratuites, violence, etc.)
# ============================================================================
BLACKLIST_PATTERNS = [
    # On exclut les expressions trop longues (locutions de plus de 5 mots)
    # et les mots de moins de 2 caractères
]

BLACKLIST_WORDS = {
    # Injures racistes / haineuses — aucune valeur pédagogique
    "nègre", "négro", "bougnoule", "youpin", "bicot", "raton",
    # Violence gratuite sans intérêt lexical
    "buter", "dessouder",
}

# ============================================================================
# MOTS DÉJÀ DANS C1 / C2 — Anti-pollution croisée
# ============================================================================
C1_C2_WORDS = {
    # Échantillon représentatif de C1 (littéraire) et C2 (rhétorique)
    # pour éviter les doublons. En production, chargé depuis les CSV.
    "nonobstant", "dialectique", "péremptoire", "partant", "plausible",
    "réquisitoire", "diatribe", "fallacieux", "étayer", "plaidoyer",
    "harangue", "réfuter", "concéder", "indubitable", "discréditer",
    "allégorie", "antinomie", "dichotomie", "postulat", "axiome",
    "assertion", "inférence", "syllogisme", "sophisme", "paralogisme",
    "tautologie", "pléonasme", "litote", "métaphore", "métonymie",
    "synecdoque", "hyperbole", "prosopopée", "oxymore", "chiasme",
    "anaphore", "antithèse", "allégation", "digression", "casuistique",
    "réactionnaire", "progressiste", "conservateur", "gaulliste",
    "monarchiste", "souverainiste", "anarchiste", "jacobin",
    # C1 Littéraire
    "allitération", "amphigourique", "aphorisme", "apophtegme",
    "ataraxie", "bucolique", "céruléen", "effluve", "empyrée",
    "éthéré", "mélancolie", "neurasthénie", "quiétude", "spleen",
    "volupté", "magnanimité", "mansuétude", "pusillanime",
}

# ============================================================================
# SEED WORDS — Graines de référence (validées éditorialement)
# ============================================================================
SEED_WORDS = [
    "miskine", "seum", "chelou", "relou", "boloss", "grave", "kiffer",
    "go", "fissa", "wesh", "bader", "chiller", "flemme", "stylé",
    "chanmé", "daron", "daronne", "askip", "osef", "pécho",
    "s'enjailler", "crari", "dwich", "masterclass", "paf",
]

# Définitions locales de qualité pour les graines
LOCAL_DEFINITIONS = {
    "miskine": "Personne qui inspire la pitié ; par extension, interjection exprimant la compassion ou la moquerie.",
    "seum": "Sentiment intense de dégoût, de frustration ou de déception (de l'arabe 'sem', venin).",
    "chelou": "Bizarre, louche, suspect (verlan de 'louche').",
    "relou": "Pénible, agaçant, ennuyeux (verlan de 'lourd').",
    "boloss": "Personne naïve, victime ou perdante ; synonyme de 'bouffon'.",
    "grave": "Intensificateur : beaucoup, vraiment, sérieusement ('j'ai grave kiffé').",
    "kiffer": "Aimer beaucoup, apprécier intensément quelque chose ou quelqu'un.",
    "go": "Fille, copine (emprunté à l'anglais ou au bambara).",
    "fissa": "Vite, rapidement (de l'arabe 'fissa', en hâte).",
    "wesh": "Interjection de salutation ou d'interpellation (de l'arabe dialectal).",
    "bader": "Être triste, déprimé, mélancolique.",
    "chiller": "Se détendre, ne rien faire, traîner tranquillement (de l'anglais 'to chill').",
    "flemme": "Paresse, manque de motivation pour agir.",
    "stylé": "Qui a du style, de la classe ; cool, impressionnant.",
    "chanmé": "Méchant en verlan ; utilisé positivement pour dire 'incroyable, génial'.",
    "daron": "Père (argot).",
    "daronne": "Mère (argot).",
    "askip": "Abréviation de 'à ce qu'il paraît' ; apparemment, soi-disant.",
    "osef": "Acronyme de 'on s'en fout' ; exprime l'indifférence totale.",
    "pécho": "Attraper, choper ; séduire quelqu'un (verlan de 'choper').",
    "s'enjailler": "S'amuser, faire la fête avec enthousiasme.",
    "crari": "Faire genre, prétendre, faire comme si (emprunté au nouchi ivoirien).",
    "dwich": "Sandwich (abréviation familière).",
    "masterclass": "Coup d'éclat, performance exceptionnelle (détourné de l'anglais).",
    "paf": "Pénis (argot très récent, propagation virale).",
}

# ============================================================================
# FONCTIONS UTILITAIRES
# ============================================================================

def get_lexique_data():
    """Charge Lexique383 pour le ratio d'oralité (réutilisation du code C2)."""
    zip_url = "http://www.lexique.org/databases/Lexique383/Lexique383.zip"
    zip_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "Lexique383.zip")
    
    if not os.path.exists(zip_path):
        print(f"  Téléchargement de Lexique383.zip...")
        urllib.request.urlretrieve(zip_url, zip_path)
        print("  Téléchargement terminé.")
    else:
        print("  Lexique383.zip déjà présent.")

    lexique = {}
    with zipfile.ZipFile(zip_path, 'r') as z:
        tsv_filename = None
        for name in z.namelist():
            if name.endswith('.tsv') or name.endswith('.txt'):
                tsv_filename = name
                break
        
        if not tsv_filename:
            raise Exception("No TSV/TXT file found in Lexique383.zip")
        
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
                    
    print(f"  → {len(lexique)} mots chargés depuis Lexique383.")
    return lexique


def get_category_members_all(category, max_pages=500):
    """Récupère TOUS les mots d'une catégorie Wiktionnaire (avec pagination)."""
    words = []
    continue_token = None
    
    while len(words) < max_pages:
        encoded_cat = urllib.parse.quote(f"Catégorie:{category}")
        url = (
            f"https://fr.wiktionary.org/w/api.php?action=query"
            f"&list=categorymembers&cmtitle={encoded_cat}"
            f"&cmlimit=500&cmtype=page&cmnamespace=0"
            f"&cmsort=timestamp&cmdir=desc&format=json"
        )
        if continue_token:
            url += f"&cmcontinue={urllib.parse.quote(continue_token)}"
        
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=15) as resp:
                data = json.loads(resp.read().decode('utf-8'))
                members = data.get('query', {}).get('categorymembers', [])
                for m in members:
                    words.append(m['title'])
                
                cont = data.get('continue', {})
                if 'cmcontinue' in cont and len(members) > 0:
                    continue_token = cont['cmcontinue']
                else:
                    break
        except Exception as e:
            print(f"    Erreur API catégorie '{category}': {e}")
            break
        
        time.sleep(0.3)
    
    return words


def get_pageviews(word, months_back=18):
    """Récupère les pageviews mensuelles d'un mot sur fr.wiktionary.org."""
    end_date = datetime.now()
    start_date = end_date - timedelta(days=months_back * 30)
    start_str = start_date.strftime("%Y%m01")
    end_str = end_date.strftime("%Y%m01")
    
    encoded = urllib.parse.quote(word, safe='')
    url = (
        f"https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/"
        f"fr.wiktionary.org/all-access/user/{encoded}/monthly/{start_str}/{end_str}"
    )
    try:
        req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            items = data.get('items', [])
            return [(item['timestamp'][:6], item['views']) for item in items]
    except Exception:
        return []


def fetch_definition(word):
    """Récupère la première définition française depuis Wiktionnaire."""
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


# ============================================================================
# FONCTIONS DE SCORING
# ============================================================================

def compute_buzz_score(views_data):
    """
    Signal A : BUZZ — Trend Ratio des pageviews Wiktionnaire.
    Compare les 3 derniers mois aux mois précédents.
    """
    if not views_data or len(views_data) < 4:
        return 0.0, 0.0, 0  # score, ratio, total
    
    # Séparer récent (3 derniers mois) vs historique
    recent = views_data[-3:]
    older = views_data[:-3]
    
    recent_avg = sum(v for _, v in recent) / max(len(recent), 1)
    older_avg = sum(v for _, v in older) / max(len(older), 1)
    total = sum(v for _, v in views_data)
    
    if older_avg <= 0:
        trend_ratio = 2.0 if recent_avg > 10 else 0.0
    else:
        trend_ratio = recent_avg / older_avg
    
    # Convertir en score [0, 1]
    if trend_ratio >= 3.0:
        score = 1.0
    elif trend_ratio >= 1.5:
        score = 0.8
    elif trend_ratio >= 1.0:
        score = 0.5
    elif trend_ratio >= 0.7:
        score = 0.3
    else:
        score = 0.2
    
    # Bonus si les pageviews absolues sont élevées (le mot est populaire)
    if total > 5000:
        score = min(1.0, score + 0.1)
    
    return score, trend_ratio, total


def compute_recency_score(word, category_position, total_in_category):
    """
    Signal B : RECENCY — Position dans la catégorie triée par timestamp.
    Les premiers mots (position basse) sont les plus récemment ajoutés.
    """
    if total_in_category <= 0:
        return 0.5
    
    # Normaliser la position : 0 = le plus récent, 1 = le plus ancien
    normalized_pos = category_position / total_in_category
    
    if normalized_pos < 0.05:  # Top 5% les plus récents
        return 1.0
    elif normalized_pos < 0.15:  # Top 15%
        return 0.8
    elif normalized_pos < 0.30:  # Top 30%
        return 0.6
    elif normalized_pos < 0.50:
        return 0.4
    else:
        return 0.2


def compute_orality_score(word, lexique):
    """
    Signal C : ORALITÉ — Ratio freqFilms / freqLivres de Lexique383.
    Un mot absent de Lexique383 est probablement trop récent/informel → bonus.
    """
    word_lower = word.lower()
    if word_lower not in lexique:
        return 0.9  # Absent de Lexique383 = très informel/récent
    
    stats = lexique[word_lower]
    fl = stats['freqlivres']
    ff = stats['freqfilms']
    
    if fl <= 0:
        return 0.8 if ff > 0 else 0.1
    
    ratio = ff / fl
    
    if ratio > 10.0:
        return 1.0
    elif ratio > 4.0:
        return 0.7
    elif ratio > 2.0:
        return 0.4
    else:
        return 0.1


def classify_difficulty(word, lexique):
    """
    Classification par difficulté.
    Absent de Lexique383 → Expert (bleeding edge).
    Présent → basé sur le Zipf.
    """
    word_lower = word.lower()
    if word_lower not in lexique:
        return "Expert"
    
    stats = lexique[word_lower]
    fl = stats['freqlivres']
    ff = stats['freqfilms']
    
    # Utiliser le max des deux fréquences pour l'argot
    freq = max(fl, ff)
    if freq <= 0:
        return "Expert"
    
    zipf = math.log10(freq) + 3.0
    
    if zipf >= 3.5:
        return "Débutant"
    elif zipf >= 2.0:
        return "Intermédiaire"
    else:
        return "Expert"


# ============================================================================
# MAIN
# ============================================================================

def main():
    print("=" * 72)
    print("  C3 PIPELINE A — EXTRACTION WORD RESERVE « DERNIER CRI »")
    print("  Stratégie : Wiktionnaire Categories + Pageviews Trending")
    print("=" * 72)
    
    # === ÉTAPE 1 : Charger Lexique383 ===
    print("\n[1/6] Chargement de Lexique383...")
    lexique = get_lexique_data()
    
    # === ÉTAPE 2 : Collecter les candidats depuis Wiktionnaire ===
    print("\n[2/6] Collecte des candidats depuis les catégories Wiktionnaire...")
    
    categories_config = [
        ("Termes argotiques en français", 400),
        ("Néologismes en français", 200),
        ("Argot Internet en français", 278),  # Tout prendre (petite catégorie)
        ("Termes populaires en français", 300),
        ("Termes familiers en français", 300),
    ]
    
    # Collecter avec position (pour le score de recency)
    all_candidates = {}  # word -> {position, total, category}
    
    for cat_name, max_fetch in categories_config:
        print(f"  📁 {cat_name} (max {max_fetch})...")
        words = get_category_members_all(cat_name, max_pages=max_fetch)
        real_words = [w for w in words if not w.startswith("Catégorie:") and not w.startswith("Annexe:")]
        print(f"     → {len(real_words)} mots récupérés")
        
        for pos, word in enumerate(real_words):
            word_lower = word.lower().strip()
            if word_lower not in all_candidates:
                all_candidates[word_lower] = {
                    'original': word,
                    'position': pos,
                    'total_in_cat': len(real_words),
                    'category': cat_name,
                }
            else:
                # Si déjà vu dans une autre catégorie, garder la meilleure position
                existing = all_candidates[word_lower]
                if pos / max(len(real_words), 1) < existing['position'] / max(existing['total_in_cat'], 1):
                    all_candidates[word_lower] = {
                        'original': word,
                        'position': pos,
                        'total_in_cat': len(real_words),
                        'category': cat_name,
                    }
    
    print(f"\n  → Pool brut : {len(all_candidates)} mots uniques")
    
    # Ajouter les graines si elles ne sont pas déjà dans le pool
    for seed in SEED_WORDS:
        if seed.lower() not in all_candidates:
            all_candidates[seed.lower()] = {
                'original': seed,
                'position': 0,
                'total_in_cat': 1,
                'category': 'Graines de référence',
            }
    
    # === ÉTAPE 3 : Filtrage ===
    print("\n[3/6] Filtrage des candidats...")
    
    filtered = {}
    excluded_reasons = {'c1_c2': 0, 'blacklist': 0, 'too_short': 0, 'locution_longue': 0}
    
    for word_lower, info in all_candidates.items():
        # Exclure les mots déjà dans C1/C2
        if word_lower in C1_C2_WORDS:
            excluded_reasons['c1_c2'] += 1
            continue
        
        # Exclure la blacklist
        if word_lower in BLACKLIST_WORDS:
            excluded_reasons['blacklist'] += 1
            continue
        
        # Exclure les mots trop courts
        if len(word_lower) < 2:
            excluded_reasons['too_short'] += 1
            continue
        
        # Exclure les locutions de plus de 6 mots
        if len(word_lower.split()) > 6:
            excluded_reasons['locution_longue'] += 1
            continue
        
        filtered[word_lower] = info
    
    print(f"  → {len(filtered)} mots après filtrage")
    print(f"     Exclus C1/C2: {excluded_reasons['c1_c2']}, Blacklist: {excluded_reasons['blacklist']}, "
          f"Trop courts: {excluded_reasons['too_short']}, Locutions longues: {excluded_reasons['locution_longue']}")
    
    # === ÉTAPE 4 : Scoring (Pageviews + Recency + Oralité) ===
    print("\n[4/6] Scoring des candidats (Pageviews API + Lexique383)...")
    print("  ⏳ Requête des pageviews Wiktionnaire (cela prend quelques minutes)...")
    
    # Sélectionner les candidats à scorer via pageviews
    # Priorité : les graines + les 300 premiers de chaque catégorie (les plus récents)
    candidates_to_score = []
    
    # Toujours inclure les graines
    for seed in SEED_WORDS:
        if seed.lower() in filtered:
            candidates_to_score.append(seed.lower())
    
    # Ajouter les candidats par position (les plus récents d'abord)
    sorted_by_recency = sorted(
        filtered.items(),
        key=lambda x: x[1]['position'] / max(x[1]['total_in_cat'], 1)
    )
    
    for word_lower, info in sorted_by_recency:
        if word_lower not in candidates_to_score:
            candidates_to_score.append(word_lower)
        if len(candidates_to_score) >= 350:
            break
    
    print(f"  → {len(candidates_to_score)} candidats sélectionnés pour le scoring pageviews")
    
    # Scorer chaque candidat
    scored_candidates = []
    
    for idx, word_lower in enumerate(candidates_to_score):
        info = filtered[word_lower]
        
        # Signal A : BUZZ (Pageviews)
        views = get_pageviews(info['original'])
        buzz_score, trend_ratio, total_views = compute_buzz_score(views)
        
        # Signal B : RECENCY
        recency_score = compute_recency_score(
            word_lower, info['position'], info['total_in_cat']
        )
        
        # Signal C : ORALITÉ
        orality_score = compute_orality_score(word_lower, lexique)
        
        # Score final combiné
        score_final = (0.40 * buzz_score) + (0.35 * recency_score) + (0.25 * orality_score)
        
        # Difficulté
        difficulty = classify_difficulty(word_lower, lexique)
        
        scored_candidates.append({
            'word': info['original'],
            'score_c3': score_final,
            'buzz_score': buzz_score,
            'trend_ratio': trend_ratio,
            'total_views': total_views,
            'recency_score': recency_score,
            'orality_score': orality_score,
            'difficulty': difficulty,
            'category': info['category'],
        })
        
        if (idx + 1) % 50 == 0:
            print(f"  ... {idx+1}/{len(candidates_to_score)} mots scorés")
        
        time.sleep(0.2)  # Rate limiting API
    
    # Trier par score final décroissant
    scored_candidates.sort(key=lambda x: x['score_c3'], reverse=True)
    
    print(f"\n  ✓ {len(scored_candidates)} candidats scorés avec succès.")
    
    # === ÉTAPE 5 : Récupération des définitions ===
    print("\n[5/6] Récupération des définitions (top 100)...")
    
    for idx, item in enumerate(scored_candidates[:100]):
        word = item['word']
        word_lower = word.lower()
        
        if word_lower in LOCAL_DEFINITIONS:
            item['definition'] = LOCAL_DEFINITIONS[word_lower]
        else:
            definition = fetch_definition(word)
            time.sleep(0.8)
            item['definition'] = definition if definition else "Définition non renseignée"
        
        if (idx + 1) % 20 == 0:
            print(f"  ... {idx+1}/100 définitions récupérées")
    
    # Les candidats au-delà du top 100 n'ont pas de définition
    for item in scored_candidates[100:]:
        if 'definition' not in item:
            item['definition'] = ""
    # === ÉTAPE 5.5 : Fusion des Candidats Rap (si présents) ===
    rap_csv = os.path.join(OUTPUT_DIR, "candidates_slang_rap.csv")
    if os.path.exists(rap_csv):
        print(f"\n[5.5/6] Fusion des candidats du rap français depuis {os.path.basename(rap_csv)}...")
        try:
            with open(rap_csv, mode='r', encoding='utf-8') as rf:
                reader = csv.DictReader(rf, delimiter=';')
                for row in reader:
                    # Eviter les doublons
                    if any(item['word'].lower() == row['Mot'].lower() for item in scored_candidates):
                        continue
                    scored_candidates.append({
                        'word': row['Mot'],
                        'score_c3': float(row['Score_Rap']),
                        'buzz_score': float(row['Score_Rap']),
                        'trend_ratio': 1.5,
                        'total_views': int(row['Pageviews']),
                        'recency_score': 1.0,
                        'orality_score': 1.0,
                        'difficulty': 'Expert',
                        'category': f"Rap ({row['Wikt_Status']})",
                        'definition': row['Definition']
                    })
            # Retrier par score
            scored_candidates.sort(key=lambda x: x['score_c3'], reverse=True)
            print(f"  ✓ Fusion réussie. Total après fusion : {len(scored_candidates)} candidats.")
        except Exception as e:
            print(f"  ✗ Erreur lors de la fusion du CSV Rap : {e}")

    # === ÉTAPE 6 : Export ===
    print("\n[6/6] Export des résultats...")
    
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    
    # CSV
    csv_path = os.path.join(OUTPUT_DIR, "candidates_slang_trending.csv")
    with open(csv_path, mode='w', encoding='utf-8', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow([
            'Mot', 'Difficulté', 'Score_C3', 'Buzz_Score', 'Trend_Ratio',
            'Pageviews_Total', 'Recency_Score', 'Orality_Score',
            'Catégorie_Source', 'Définition'
        ])
        for item in scored_candidates:
            writer.writerow([
                item['word'],
                item['difficulty'],
                f"{item['score_c3']:.3f}",
                f"{item['buzz_score']:.2f}",
                f"{item['trend_ratio']:.2f}",
                item['total_views'],
                f"{item['recency_score']:.2f}",
                f"{item['orality_score']:.2f}",
                item['category'],
                item.get('definition', '')
            ])
    
    # Rapport Markdown
    report_path = os.path.join(OUTPUT_DIR, "resultat_mots_test_c3_trending.md")
    
    by_diff = {"Débutant": [], "Intermédiaire": [], "Expert": []}
    for item in scored_candidates[:100]:  # Top 100 seulement
        by_diff[item['difficulty']].append(item)
    
    md_lines = [
        "# Rapport d'Extraction C3 — Word Reserve « Dernier Cri » (Expressions Populaires Trending)",
        "",
        f"**Date :** {datetime.now().strftime('%d/%m/%Y %H:%M')}",
        f"**Candidats scorés :** {len(scored_candidates)}",
        f"**Méthode :** Wiktionnaire Categories + Pageviews Trending + Lexique383 Oralité",
        "",
        "## Formule de scoring",
        "```",
        "Score_C3 = (0.40 × Buzz) + (0.35 × Recency) + (0.25 × Oralité)",
        "```",
        "",
        "---",
        "",
    ]
    
    for level in ["Débutant", "Intermédiaire", "Expert"]:
        words_in_level = by_diff[level]
        md_lines.append(f"## Niveau : {level} (Top candidats : {len(words_in_level)} mots)")
        md_lines.append("")
        md_lines.append("| # | Mot | Score C3 | Buzz | Trend↑ | Views | Recency | Oralité | Définition |")
        md_lines.append("|---|---|---|---|---|---|---|---|---|")
        for idx, item in enumerate(words_in_level):
            trend_arrow = "🔥" if item['trend_ratio'] > 1.5 else ("↑" if item['trend_ratio'] > 1.0 else ("→" if item['trend_ratio'] > 0.7 else "↓"))
            defn = item.get('definition', '')
            if len(defn) > 80:
                defn = defn[:77] + "..."
            md_lines.append(
                f"| {idx+1} | **{item['word']}** | {item['score_c3']:.3f} | "
                f"{item['buzz_score']:.2f} | {trend_arrow}{item['trend_ratio']:.2f} | "
                f"{item['total_views']} | {item['recency_score']:.2f} | "
                f"{item['orality_score']:.2f} | {defn} |"
            )
        md_lines.append("")
    
    with open(report_path, "w", encoding="utf-8") as f:
        f.write("\n".join(md_lines))
    
    # Affichage résumé
    print(f"\n{'=' * 72}")
    print(f"  RÉSULTATS — TOP 30 EXPRESSIONS « DERNIER CRI »")
    print(f"{'=' * 72}")
    print(f"{'#':>3} {'Mot':<20} {'Score':>6} {'Buzz':>5} {'Trend':>6} {'Views':>7} {'Diff':<15}")
    print(f"{'-'*72}")
    
    for idx, item in enumerate(scored_candidates[:30]):
        trend_sym = ">>" if item['trend_ratio'] > 1.5 else (">" if item['trend_ratio'] > 1.0 else ("=" if item['trend_ratio'] > 0.7 else "<"))
        print(
            f"{idx+1:>3} {item['word']:<20} {item['score_c3']:>6.3f} "
            f"{item['buzz_score']:>5.2f} {trend_sym}{item['trend_ratio']:>5.2f} "
            f"{item['total_views']:>7} {item['difficulty']:<15}"
        )
    
    print(f"\n  CSV : {csv_path}")
    print(f"  Rapport : {report_path}")
    print(f"\n{'=' * 72}")
    print(f"  TERMINÉ — {len(scored_candidates)} mots scorés et classés.")
    print(f"{'=' * 72}")


if __name__ == "__main__":
    main()
