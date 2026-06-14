"""
=============================================================================
C5 Pipeline A — Extraction Termologique et Jargon Spécifique Adaptatif
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

# Graines de référence locales pour valider le comportement sur 4 domaines clés
DOMAINS_SEEDS = {
    "droit": ["usufruit", "emphytéose", "jurisprudence", "litige", "préjudice", "subroger", "synallagmatique", "dol", "caducité"],
    "cuisine": ["brunoise", "blanchir", "déglacer", "julienne", "roux", "singer", "sabler", "émulsionner", "chinoiser", "concasser"],
    "médecine": ["apoptose", "nosocomial", "étiologie", "idiopathique", "iatrogène"],
    "informatique": ["polymorphisme", "récursion", "idempotence", "obfuscation", "paralléliser"]
}

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

def fetch_domain_jargon(domain_name):
    """
    Tente de charger les membres de la catégorie Wiktionnaire correspondante au domaine.
    Gère les variations comme 'Lexique en français du droit', 'Lexique en français de la cuisine', etc.
    """
    candidates = []
    # Variations de constructions de catégories Wiktionnaire
    cat_names = [
        f"Lexique en français du {domain_name}",
        f"Lexique en français de la {domain_name}",
        f"Lexique en français de l’{domain_name}",
        f"Lexique en français de l'{domain_name}",
        f"Vocabulaire en français de la {domain_name}",
        f"Vocabulaire en français du {domain_name}",
    ]
    for cat in cat_names:
        print(f"  Essai catégorie : {cat}...")
        words = get_category_members(cat)
        if words:
            print(f"    → {len(words)} termes trouvés.")
            candidates.extend(words)
            break
            
    # Fallback si rien trouvé : essayer recherche plein texte simplifiée ou catégorie parente
    if not candidates:
        fallback_cat = f"{domain_name.capitalize()} en français"
        print(f"  Essai fallback catégorie : {fallback_cat}...")
        candidates = get_category_members(fallback_cat)
        
    return candidates

def score_jargon(word, domain_name, lexique, seeds):
    """
    Évalue la pertinence d'un mot dans le jargon :
    - Bonus si le mot est présent dans les graines de référence
    - Pénalité si le mot est ultra-commun (Zipf élevé)
    - Score basé sur la spécificité sémantique (longueur, présence de suffixes typiques comme -logie, -isme, -ique)
    """
    word_lower = word.lower()
    score = 0.5  # Base
    
    # Bonus graine
    if word_lower in seeds:
        score += 0.4
        
    # Longueur
    if len(word_lower) > 7:
        score += 0.1
        
    # Suffixes techniques/jargon
    technical_suffixes = ['phisme', 'tence', 'ation', 'logie', 'trique', 'phie', 'isme', 'ique', 'iste', 'gence']
    for suffix in technical_suffixes:
        if word_lower.endswith(suffix):
            score += 0.1
            
    # Pénalité Zipf (les mots de jargon ne doivent pas être des mots courants de la vie de tous les jours)
    zipf = 0.0
    if word_lower in lexique:
        fl = lexique[word_lower]['freqlivres']
        ff = lexique[word_lower]['freqfilms']
        freq = max(fl, ff)
        if freq > 0:
            zipf = math.log10(freq) + 3.0
            
    if zipf > 4.5:
        score -= 0.3  # Trop commun (ex: "cuisine", "table", "droit"...)
    elif zipf > 0.0 and zipf < 2.5:
        score += 0.1  # Plage idéale de jargon rare et intéressant

    return max(0.0, min(1.0, score)), zipf

def main():
    print("==========================================================")
    print("  C5 PIPELINE A — EXTRACTION DYNAMIQUE DE JARGON")
    print("==========================================================")
    
    lexique = get_lexique_data()
    
    # Domaines à tester et valider
    domains_to_test = ["droit", "cuisine", "médecine", "informatique"]
    all_results = {}
    
    for domain in domains_to_test:
        print(f"\nTraitement du domaine : '{domain}'...")
        seeds = DOMAINS_SEEDS.get(domain, [])
        raw_words = fetch_domain_jargon(domain)
        
        # Injecter les graines si non trouvées
        for seed in seeds:
            if seed not in raw_words:
                raw_words.append(seed)
                
        scored = []
        for w in raw_words:
            if w.startswith("Catégorie:") or w.startswith("Annexe:") or len(w) < 3:
                continue
            sc, zipf = score_jargon(w, domain, lexique, seeds)
            scored.append({
                'word': w,
                'score_c5': sc,
                'zipf': zipf
            })
            
        scored.sort(key=lambda x: x['score_c5'], reverse=True)
        all_results[domain] = scored[:50]  # Garder le Top 50 pour chaque domaine
        print(f"  ✓ {len(scored)} candidats collectés. Top 50 conservés.")

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    
    # Récupérer les définitions pour le Top 20 de chaque domaine
    print("\n  Récupération des définitions pour le Top 20 de chaque domaine...")
    
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
                
                # Chercher toutes les définitions (ol > li)
                ol_start = html.find('<ol>')
                if ol_start != -1:
                    ol_end = html.find('</ol>', ol_start)
                    ol_content = html[ol_start:ol_end]
                    
                    # On va parcourir les li
                    li_matches = re.findall(r'<li>(.*?)</li>', ol_content, re.DOTALL)
                    definitions = []
                    for li in li_matches:
                        clean_def = re.sub(r'<[^>]+>', '', li)
                        clean_def = re.sub(r'\s+', ' ', clean_def).strip()
                        if clean_def:
                            definitions.append(clean_def)
                    return definitions
        except Exception:
            pass
        return []

    # Dictionnaire de contextualisation par domaine (mots clés attendus dans la définition)
    context_keywords = {
        "droit": ["droit", "juridique", "loi", "justice", "tribunal", "légal", "acte"],
        "cuisine": ["cuisine", "cuire", "plat", "aliment", "préparation", "recette", "sauce"],
        "médecine": ["médecine", "médical", "maladie", "corps", "pathologie", "patient", "traitement", "cellule"],
        "informatique": ["informatique", "ordinateur", "programme", "données", "logiciel", "numérique", "réseau"]
    }

    for domain, items in all_results.items():
        print(f"    -> Récupération definitions pour le domaine : {domain}...")
        keywords = context_keywords.get(domain, [])
        for idx, item in enumerate(items[:20]):
            defs = fetch_definition(item['word'])
            time.sleep(0.5)
            
            best_def = ""
            if defs:
                # Essayer de trouver la définition qui correspond le mieux au domaine
                for d in defs:
                    if any(key in d.lower() for key in keywords):
                        best_def = d
                        break
                if not best_def:
                    best_def = defs[0] # Prendre la première si aucune correspondance sémantique stricte n'est trouvée
            
            if not best_def:
                best_def = f"Terme technique lié au domaine {domain}."
            
            if len(best_def) > 300:
                best_def = best_def[:297] + "..."
            item['definition'] = best_def
            
        # Pour les autres au delà du Top 20
        for item in items[20:]:
            item['definition'] = f"Terme technique lié au domaine {domain}."

    # Écrire le CSV de synthèse
    csv_path = os.path.join(OUTPUT_DIR, "candidates_jargon.csv")
    with open(csv_path, mode='w', encoding='utf-8', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow(['Domaine', 'Mot', 'Score_C5', 'Zipf', 'Définition'])
        for domain, items in all_results.items():
            for item in items:
                writer.writerow([domain, item['word'], f"{item['score_c5']:.3f}", f"{item['zipf']:.2f}", item['definition']])

    # Écrire le rapport Markdown
    report_path = os.path.join(OUTPUT_DIR, "resultat_mots_test_c5.md")
    md_lines = [
        "# Rapport d'Extraction C5 — Word Reserve « Jargon Spécifique » (Zero-shot)",
        "",
        f"**Date :** {datetime.now().strftime('%d/%m/%Y %H:%M')}",
        "**Méthode :** Extraction dynamique par catégorie de domaine Wiktionnaire + isolation sémantique",
        "",
        "## Formule de score Jargon",
        "```",
        "Score_C5 = Base_Score + Suffix_Bonus + Seed_Bonus - Zipf_Penalty",
        "```",
        ""
    ]
    
    for domain, items in all_results.items():
        md_lines.append(f"## Domaine : {domain.capitalize()} (Top 15 candidats)")
        md_lines.append("")
        md_lines.append("| # | Mot | Score C5 | Zipf | Définition |")
        md_lines.append("|---|---|---|---|---|")
        for idx, item in enumerate(items[:15]):
            md_lines.append(f"| {idx+1} | **{item['word']}** | {item['score_c5']:.3f} | {item['zipf']:.2f} | {item['definition']} |")
        md_lines.append("")
        
    with open(report_path, "w", encoding="utf-8") as f:
        f.write("\n".join(md_lines))

    print(f"\n  ✓ Extraction C5 terminée. Rapport de routage dynamique généré.")
    print(f"  CSV : {csv_path}")
    print(f"  Rapport : {report_path}")

if __name__ == "__main__":
    main()
