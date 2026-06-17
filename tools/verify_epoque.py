import csv

targets = [
    'pusillanime','valétudinaire','abscons','obsidional','firmament','aurore',
    'chagrin','jadis','éphémère','ataraxie','chimère','idylle','heur',
    'miséricorde','réminiscence','apogée','sérénité','diaphane','azur',
    'crépuscule','effluve','spleen','nostalgie','langueur','intransigeance',
    'flâneur','pataquès','brigand','flibustier','allitération','assonance',
    'parangon','impéritie','pugnace','obsolète','heuristique',
]
targets_norm = set(t.lower() for t in targets)

csv_path = '../docs/amelioration_de_la_fonction_de_recherche/algorithme_de_presentation_des_extraits/classified_c1_words_v2.csv'

print(f"{'Mot':<22} {'Epoque'}")
print("-" * 44)
with open(csv_path, encoding='utf-8') as f:
    reader = csv.DictReader(f, delimiter=';')
    for row in reader:
        mot_lower = row['Mot'].lower()
        if mot_lower in targets_norm:
            print(f"{row['Mot']:<22} {row['Epoque']}")
