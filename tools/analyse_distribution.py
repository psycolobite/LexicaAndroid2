#!/usr/bin/env python3
"""Script d'analyse de la distribution des mots de référence dans Lexique383."""
import csv, math, zipfile, io

# Charger Lexique383
lexique = {}
with zipfile.ZipFile('Lexique383.zip', 'r') as zf:
    with zf.open('Lexique383.tsv') as f:
        content = f.read().decode('utf-8', errors='replace')
        reader = csv.DictReader(io.StringIO(content), delimiter='\t')
        for row in reader:
            lemme = row.get('lemme', row.get('ortho', '')).strip().lower()
            try:
                fb = float(row.get('freqlivres', 0) or 0)
                ff = float(row.get('freqfilms2', 0) or 0)
            except:
                fb = ff = 0.0
            zb = math.log10(fb)+3 if fb > 0 else 0.0
            zf_val = math.log10(ff)+3 if ff > 0 else 0.0
            ratio = (fb/ff) if ff > 0 else (10.0 if fb > 0 else 0.0)
            if lemme not in lexique or fb > lexique[lemme]['fb']:
                lexique[lemme] = {
                    'zb': round(zb,3),
                    'zf': round(zf_val,3),
                    'ratio': round(min(ratio,50),3),
                    'fb': fb
                }

# Charger reference
reference = {}
with open('consolidated_literary_words.csv', 'r', encoding='utf-8-sig') as f:
    for row in csv.DictReader(f, delimiter=';'):
        mot = row['mot'].strip().lower()
        if mot:
            reference[mot] = row['theme'].strip()

# Analyser
print('Analyse des mots de référence dans Lexique383')
print('=' * 50)

missing = []
zero_zb_with_ratio = []
has_good_data = []

for mot in sorted(reference.keys()):
    lex = lexique.get(mot)
    if not lex:
        missing.append(mot)
        continue
    zb = lex['zb']
    ratio = lex['ratio']
    if zb == 0 and ratio == 0:
        zero_zb_with_ratio.append((mot, 'zb=0, ratio=0 - VRAIS ABSENTS'))
    elif zb == 0:
        zero_zb_with_ratio.append((mot, f'zb=0, ratio={ratio:.2f}'))
    else:
        has_good_data.append((mot, zb, ratio))

print(f'Total référence: {len(reference)}')
print(f'Absents de Lexique383: {len(missing)}')
print(f'Dans Lexique mais zb=0: {len(zero_zb_with_ratio)}')
print(f'Données complètes (zb>0): {len(has_good_data)}')
print()

print('Mots avec zb=0 (premiers 20):')
for w, info in zero_zb_with_ratio[:20]:
    print(f'  {w}: {info}')
print()

print('Stats sur les mots avec données complètes:')
zb_vals = [x[1] for x in has_good_data]
ratio_vals = [x[2] for x in has_good_data]
print(f'Zipf min/max: {min(zb_vals):.2f} / {max(zb_vals):.2f}')
print(f'Ratio min/max: {min(ratio_vals):.2f} / {max(ratio_vals):.2f}')
print()
print('Distribution Zipf:')
for seuil in [1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0]:
    count = sum(1 for z in zb_vals if z <= seuil)
    print(f'  Zipf <= {seuil}: {count}/{len(zb_vals)} ({100*count//len(zb_vals)}%)')
print()
print('Distribution Ratio (livres/films):')
for seuil in [0.5, 1.0, 2.0, 3.0, 5.0, 10.0, 20.0]:
    count = sum(1 for r in ratio_vals if r < seuil)
    print(f'  Ratio < {seuil}: {count}/{len(ratio_vals)} ({100*count//len(ratio_vals)}%)')
print()

# Mots avec ratio < 1.0 (plus oraux qu'écrits)
oral_words = [(w, zb, r) for w, zb, r in has_good_data if r < 1.0]
print(f'Mots avec ratio < 1.0 (plus oraux): {len(oral_words)}')
for w, zb, r in sorted(oral_words, key=lambda x: x[2])[:20]:
    print(f'  {w}: zb={zb:.2f}, ratio={r:.3f}')

# Mots avec Zipf > 4.5 (très communs)
very_common = [(w, zb, r) for w, zb, r in has_good_data if zb > 4.5]
print(f'\nMots avec Zipf > 4.5 (très communs): {len(very_common)}')
for w, zb, r in sorted(very_common, key=lambda x: -x[1])[:15]:
    print(f'  {w}: zb={zb:.2f}, ratio={r:.2f}')
