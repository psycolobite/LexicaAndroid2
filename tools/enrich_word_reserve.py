import json
import csv
import os
import re
import urllib.request
import urllib.parse
import time

def normalize_key(w):
    return w.strip().lower() if w else ''

def parse_wiktionary_extract(extract):
    if not extract:
        return None
    
    fr_section = extract
    if '== Français ==' in extract:
        fr_section = extract.split('== Français ==')[1]
        if '\n== ' in fr_section:
            fr_section = fr_section.split('\n== ')[0]
            
    etymologie = ''
    if '=== Étymologie ===' in fr_section:
        ety_part = fr_section.split('=== Étymologie ===')[1].split('\n===')[0].strip()
        lines = [l.strip() for l in ety_part.split('\n') if l.strip() and not l.startswith('=')]
        if lines:
            etymologie = lines[0]
            
    cat = ''
    definition = ''
    exemples = []
    synonymes = []
    
    headers = list(re.finditer(r'=== (Nom [^=]+|Adjectif[^=]*|Verbe[^=]*|Adverbe[^=]*) ===', fr_section))
    if headers:
        first_h = headers[0]
        cat = first_h.group(1).strip()
        section_text = fr_section[first_h.end():]
        if '\n===' in section_text:
            section_text = section_text.split('\n===')[0]
            
        lines = [l.strip() for l in section_text.split('\n') if l.strip()]
        def_lines = []
        for line in lines:
            if line.startswith('\\') or 'prononciation' in line.lower() or line.startswith('masculin') or line.startswith('féminin'):
                continue
            if re.match(r'^\d+\.\s*', line):
                line = re.sub(r'^\d+\.\s*', '', line)
            def_lines.append(line)
            
        if def_lines:
            definition = def_lines[0]
            for ex in def_lines[1:]:
                if (ex.startswith('—') or ex.startswith('«') or ex.startswith('"') or ex.startswith('Note :')) and len(exemples) < 2:
                    exemples.append(ex)
                    
    if '=== Synonymes ===' in fr_section:
        syn_part = fr_section.split('=== Synonymes ===')[1].split('\n===')[0].strip()
        syn_lines = [re.sub(r'^[•\-\*]\s*', '', l).strip() for l in syn_part.split('\n') if l.strip()]
        for sl in syn_lines[:5]:
            synonymes.extend([s.strip() for s in sl.split(',') if s.strip()])
            
    if not definition:
        # Fallback to first line after French header
        raw_lines = [l.strip() for l in fr_section.split('\n') if l.strip() and not l.startswith('=') and not l.startswith('\\')]
        if raw_lines:
            definition = raw_lines[0]
            
    return {
        'definition': definition,
        'categorie': cat,
        'etymologie': etymologie,
        'exemples': exemples,
        'synonymes': synonymes
    }

def main():
    all_words = {}

    def add_entry(mot, definition, categorie='', synonymes=None, exemples=None, source_priority=1):
        if not mot or not definition:
            return
        mot_clean = mot.strip()
        key = normalize_key(mot_clean)
        if not key:
            return
            
        if isinstance(synonymes, str):
            syn_list = [s.strip() for s in re.split(r'[,;]', synonymes) if s.strip()]
        elif isinstance(synonymes, list):
            syn_list = [str(s).strip() for s in synonymes if str(s).strip()]
        else:
            syn_list = []
            
        if isinstance(exemples, str):
            ex_list = [e.strip() for e in exemples.split('\n') if e.strip()]
        elif isinstance(exemples, list):
            ex_list = [str(e).strip() for e in exemples if str(e).strip()]
        else:
            ex_list = []

        def_clean = definition.strip()
        cat_clean = categorie.strip() if categorie else ''

        if key in all_words:
            existing = all_words[key]
            if source_priority > existing['priority']:
                existing['mot'] = mot_clean
                existing['definition'] = def_clean
                existing['categorie'] = cat_clean or existing['categorie']
                existing['synonymes'] = syn_list or existing['synonymes']
                existing['exemples'] = ex_list or existing['exemples']
                existing['priority'] = source_priority
            else:
                if not existing['categorie'] and cat_clean:
                    existing['categorie'] = cat_clean
                if not existing['synonymes'] and syn_list:
                    existing['synonymes'] = syn_list
                if not existing['exemples'] and ex_list:
                    existing['exemples'] = ex_list
        else:
            all_words[key] = {
                'mot': mot_clean,
                'definition': def_clean,
                'categorie': cat_clean,
                'synonymes': syn_list,
                'exemples': ex_list,
                'priority': source_priority
            }

    # 1. Load beautiful_literary_words.json
    blw_path = r'C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\tools\beautiful_literary_words.json'
    if os.path.exists(blw_path):
        with open(blw_path, 'r', encoding='utf-8') as f:
            for item in json.load(f):
                add_entry(
                    mot=item.get('mot'),
                    definition=item.get('définition') or item.get('definition'),
                    categorie=item.get('theme', ''),
                    synonymes=item.get('synonymes', ''),
                    exemples=[item.get('exemple')] if item.get('exemple') else [],
                    source_priority=5
                )

    # 2. Load original mots_rares.json
    mr_path = r'C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\app\src\main\assets\mots_rares.json'
    if os.path.exists(mr_path):
        with open(mr_path, 'r', encoding='utf-8') as f:
            for item in json.load(f):
                add_entry(
                    mot=item.get('mot'),
                    definition=item.get('definition'),
                    categorie=item.get('categorie', ''),
                    synonymes=item.get('synonymes', []),
                    exemples=item.get('exemples', []),
                    source_priority=4
                )

    # 3. Load rhetoric_candidates_v1.csv
    rc_path = r'C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\tools\rhetoric_candidates_v1.csv'
    if os.path.exists(rc_path):
        with open(rc_path, 'r', encoding='utf-8', errors='ignore') as f:
            for row in csv.DictReader(f, delimiter=';'):
                add_entry(
                    mot=row.get('Mot'),
                    definition=row.get('Définition'),
                    categorie='Rhétorique',
                    source_priority=3
                )

    # 4. Load candidates_jargon.csv
    cj_path = r'C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\docs\archive\amelioration_de_la_fonction_de_recherche\algorithme_de_presentation_des_extraits\candidates_jargon.csv'
    if os.path.exists(cj_path):
        with open(cj_path, 'r', encoding='utf-8', errors='ignore') as f:
            for row in csv.DictReader(f, delimiter=';'):
                add_entry(
                    mot=row.get('Mot'),
                    definition=row.get('Définition'),
                    categorie='Jargon ' + str(row.get('Domaine', '')),
                    source_priority=3
                )

    # 5. Load candidates_slang_trending.csv
    cs_path = r'C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\docs\archive\amelioration_de_la_fonction_de_recherche\algorithme_de_presentation_des_extraits\candidates_slang_trending.csv'
    if os.path.exists(cs_path):
        with open(cs_path, 'r', encoding='utf-8', errors='ignore') as f:
            for row in csv.DictReader(f, delimiter=';'):
                add_entry(
                    mot=row.get('Mot'),
                    definition=row.get('Définition'),
                    categorie='Argot & Tendances',
                    source_priority=3
                )

    # 6. Load candidates_rhetoric_politics.csv
    crp_path = r'C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\docs\archive\amelioration_de_la_fonction_de_recherche\algorithme_de_presentation_des_extraits\candidates_rhetoric_politics.csv'
    if os.path.exists(crp_path):
        with open(crp_path, 'r', encoding='utf-8', errors='ignore') as f:
            for row in csv.DictReader(f, delimiter=';'):
                add_entry(
                    mot=row.get('Mot'),
                    definition=row.get('Définition'),
                    categorie='Rhétorique & Débat',
                    source_priority=3
                )

    # 7. Now read all 903 words from objectif_1_liste1_mots_caracterise.csv
    c1_path = r'C:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\mots_et_extraits\trouver_des_mots_pipeline_A\objectif_1_vocabulaire_litteraire\data\objectif_1_liste1_mots_caracterise.csv'
    c1_words = []
    if os.path.exists(c1_path):
        with open(c1_path, 'r', encoding='utf-8', errors='ignore') as f:
            for row in csv.DictReader(f, delimiter=';'):
                mot = row.get('\ufeffMot') or row.get('Mot')
                if mot and mot.strip():
                    pole = row.get('Pole_Semantique', '')
                    domaine = row.get('Domaine_Ecriture', '')
                    cat_label = f'{pole} ({domaine})'.strip(' ()') if pole else ''
                    c1_words.append((mot.strip(), cat_label, row))

    print(f'Total C1 words from objectif_1_liste1_mots_caracterise.csv: {len(c1_words)}')
    
    # Identify missing C1 words
    missing_c1 = []
    for mot, cat_label, row in c1_words:
        k = normalize_key(mot)
        if k not in all_words or not all_words[k]['definition']:
            missing_c1.append((mot, cat_label, row))

    print(f'C1 words already with definition: {len(c1_words) - len(missing_c1)}')
    print(f'C1 words to fetch from Wiktionnaire: {len(missing_c1)}')

    # Batch fetch missing words from Wiktionary API
    batch_size = 40
    for i in range(0, len(missing_c1), batch_size):
        batch = missing_c1[i:i+batch_size]
        titles = '|'.join(urllib.parse.quote(m[0]) for m in batch)
        url = 'https://fr.wiktionary.org/w/api.php?action=query&prop=extracts&explaintext=1&format=json&titles=' + titles
        req = urllib.request.Request(url, headers={'User-Agent': 'LexicaVocabularyApp/1.0 (contact@lexica.app)'})
        
        try:
            with urllib.request.urlopen(req, timeout=10) as resp:
                data = json.loads(resp.read().decode('utf-8'))
                pages = data.get('query', {}).get('pages', {})
                for pid, pdata in pages.items():
                    if pid != '-1':
                        title = pdata.get('title', '')
                        extract = pdata.get('extract', '')
                        parsed = parse_wiktionary_extract(extract)
                        if parsed and parsed.get('definition'):
                            # Find matching cat_label
                            match_cat = next((m[1] for m in batch if normalize_key(m[0]) == normalize_key(title)), 'Littéraire')
                            add_entry(
                                mot=title,
                                definition=parsed['definition'],
                                categorie=parsed['categorie'] or match_cat,
                                synonymes=parsed['synonymes'],
                                exemples=parsed['exemples'],
                                source_priority=2
                            )
            print(f'Fetched batch {i//batch_size + 1}/{(len(missing_c1) + batch_size - 1)//batch_size} (progress: {min(i+batch_size, len(missing_c1))}/{len(missing_c1)})')
        except Exception as e:
            print(f'Error fetching batch {i}: {e}')
        time.sleep(0.3)

    # Any remaining C1 words without definition get a descriptive fallback
    for mot, cat_label, row in missing_c1:
        k = normalize_key(mot)
        if k not in all_words:
            pole_clean = pole.replace('_', ' ').lower()
            dom_clean = domaine.replace('_', ' ').lower()
            cat_clean = pole.replace('_', ' ')
            add_entry(
                mot=mot,
                definition=f'Terme littéraire ({pole_clean}, {dom_clean}).',
                categorie=cat_clean,
                source_priority=1
            )

    # Build final list sorted alphabetically
    final_list = []
    for idx, key in enumerate(sorted(all_words.keys()), start=1):
        entry = all_words[key]
        final_list.append({
            'id': f'mr_{idx}',
            'mot': entry['mot'],
            'definition': entry['definition'],
            'categorie': entry['categorie'],
            'synonymes': entry['synonymes'],
            'exemples': entry['exemples']
        })

    out_path = r'c:\Users\r0xef\AndroidStudioProjects\LexicaAndroidV1\app\src\main\assets\mots_rares.json'
    with open(out_path, 'w', encoding='utf-8') as f:
        json.dump(final_list, f, ensure_ascii=False, indent=2)

    print(f'\n SUCCESS: Total consolidated unique words in {out_path}: {len(final_list)}')

if __name__ == '__main__':
    main()
