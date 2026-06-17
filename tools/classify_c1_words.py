# -*- coding: utf-8 -*-
"""
Script de classification automatique et assistée par règles des 407 mots de C1
selon la taxonomie scientifique (mutuellement exclusive).
Ajoute également une colonne Difficulte_Abstraction pour comparaison.
"""

import os
import json
import csv
import math
import zipfile

# Chemins
TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))
WORDS_JSON_PATH = os.path.join(TOOLS_DIR, "consolidated_literary_words.json")
ZIP_LEXIQUE_PATH = os.path.join(TOOLS_DIR, "Lexique383.zip")

OUTPUT_CSV_PATH = os.path.join(
    os.path.dirname(TOOLS_DIR),
    "docs", "amelioration_de_la_fonction_de_recherche",
    "algorithme_de_presentation_des_extraits", "classified_c1_words.csv"
)

# Chargement du Lexique
def load_lexique():
    lexique = {}
    if not os.path.exists(ZIP_LEXIQUE_PATH):
        print("Lexique383.zip introuvable, utilisation de valeurs de repli.")
        return {}
    with zipfile.ZipFile(ZIP_LEXIQUE_PATH, 'r') as z:
        tsv_name = next((name for name in z.namelist() if name.endswith(('.tsv', '.txt'))), None)
        if not tsv_name:
            return {}
        with z.open(tsv_name) as f:
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

# RÈGLES DE MAPPING
POLE_MAP = {
    "Art & Langage": "ARTS_ET_LANGAGE",
    "Esprit & Caractère": "ESPRIT_ET_CARACTERE",
    "Nature & Cosmos": "NATURE_ET_COSMOS",
    "Philosophie & Idées": "PHILOSOPHIE_ET_IDEES",
    "Sentiments & Psyché": "SENTIMENTS_ET_PSYCHE"
}

# Mappings explicites pour Lumière & Ombres et Temps & Éphémère
THEMES_SPECIFIC_MAP = {
    # Lumière & Ombres
    "aurore": "NATURE_ET_COSMOS",
    "clair-obscur": "ARTS_ET_LANGAGE",
    "coruscant": "NATURE_ET_COSMOS",
    "crépuscule": "NATURE_ET_COSMOS",
    "diaphane": "NATURE_ET_COSMOS",
    "exsangue": "SENTIMENTS_ET_PSYCHE",
    "fuligineux": "PHILOSOPHIE_ET_IDEES",
    "incandescent": "NATURE_ET_COSMOS",
    "irisé": "NATURE_ET_COSMOS",
    "luminescence": "NATURE_ET_COSMOS",
    "nimbe": "ARTS_ET_LANGAGE",
    "opalin": "NATURE_ET_COSMOS",
    "phosphorescence": "NATURE_ET_COSMOS",
    "purpurine": "ARTS_ET_LANGAGE",
    "pénombre": "NATURE_ET_COSMOS",
    "ténébreux": "SENTIMENTS_ET_PSYCHE",
    "vespéral": "NATURE_ET_COSMOS",
    # Temps & Éphémère
    "ancestral": "PHILOSOPHIE_ET_IDEES",
    "antédiluvien": "PHILOSOPHIE_ET_IDEES",
    "apogée": "PHILOSOPHIE_ET_IDEES",
    "caduc": "PHILOSOPHIE_ET_IDEES",
    "caducité": "PHILOSOPHIE_ET_IDEES",
    "célérité": "ESPRIT_ET_CARACTERE",
    "déliquescence": "PHILOSOPHIE_ET_IDEES",
    "désuet": "ARTS_ET_LANGAGE",
    "fugace": "SENTIMENTS_ET_PSYCHE",
    "hécatombe": "PHILOSOPHIE_ET_IDEES",
    "imminent": "PHILOSOPHIE_ET_IDEES",
    "immuable": "PHILOSOPHIE_ET_IDEES",
    "immémorial": "PHILOSOPHIE_ET_IDEES",
    "inexorable": "PHILOSOPHIE_ET_IDEES",
    "intemporel": "PHILOSOPHIE_ET_IDEES",
    "jadis": "PHILOSOPHIE_ET_IDEES",
    "millénaire": "PHILOSOPHIE_ET_IDEES",
    "obsolescence": "PHILOSOPHIE_ET_IDEES",
    "obsolète": "PHILOSOPHIE_ET_IDEES",
    "précurseur": "PHILOSOPHIE_ET_IDEES",
    "prémices": "NATURE_ET_COSMOS",
    "périssable": "PHILOSOPHIE_ET_IDEES",
    "réminiscence": "SENTIMENTS_ET_PSYCHE",
    "sénescence": "NATURE_ET_COSMOS",
    "transitoire": "PHILOSOPHIE_ET_IDEES",
    "trépas": "PHILOSOPHIE_ET_IDEES",
    "vicissitude": "PHILOSOPHIE_ET_IDEES",
    "éphémère": "SENTIMENTS_ET_PSYCHE",
    "évanescent": "SENTIMENTS_ET_PSYCHE"
}

# Dictionnaires de mots-clés pour inférer le Domaine d'Écriture Cible
DOMAINE_WORDS = {
    "THEATRE": [
        "stichomythie", "quiproquo", "tirade", "réplique", "faconde", "harangue",
        "loquace", "prolixe", "soliloque", "invective", "sardonique", "goguenard",
        "simagrée", "potentat", "courroux", "tragedie", "comédie", "tragique",
        "burlesque", "cabotin", "dramaturge", "masque", "chiasme", "tautologie",
        "pérorer", "ergoter", "fripon", "gredin", "grivois", "grivoiserie",
        "imbroglio", "insolence", "irrévérence", "larron", "satyre", "sbire",
        "spadassin", "tintamarre", "pataquès"
    ],
    "POESIE": [
        "allitération", "assonance", "aède", "calligramme", "cantilène", "césure",
        "élégiaque", "élégie", "lyrisme", "mélopée", "rhapsode", "rhapsodie",
        "azur", "céruléen", "effluve", "empyrée", "firmament", "zéphyr", "aurore",
        "crépuscule", "diaphane", "opalin", "vespéral", "évanescent", "spleen",
        "volupté", "ondine", "sylphide", "nymphe", "nacré", "irisé", "diaphanéité",
        "frémissement", "murmure", "psalmodie", "psalmodier", "flâneur", "noctambule",
        "mignonne", "ménestrel", "métrique", "troubadour", "vate", "éolien"
    ],
    "ESSAI_PHILOSOPHIQUE": [
        "aphorisme", "apophtegme", "ratiociner", "vaticination", "gnomique",
        "contingence", "dogme", "hermétique", "heuristique", "immanence",
        "intrinsèque", "noumène", "ontologie", "paradigme", "solipsisme",
        "sophisme", "syllogisme", "syncrétisme", "sérendipité", "transcendance",
        "truisme", "maxime", "ontologique", "métaphysique", "téléologique",
        "éclectisme", "solipsiste", "casuistique", "conjecture", "dirimant",
        "infrangible", "substantifique", "ataraxie", "cénobite", "ilote",
        "paria", "pernicieux", "prodigalité", "érémitique", "oblation"
    ],
    "CRITIQUE_MEMOIRES": [
        "cénacle", "diatribe", "laudateur", "liminaire", "panégyrique",
        "acrimonie", "anachorète", "atermoiement", "atrabilaire", "bonhomie",
        "caustique", "causticité", "circonspect", "clémence", "connivence",
        "flegmatique", "ignominie", "impudence", "intransigeance", "magnanime",
        "mansuétude", "obséquieux", "opiniâtre", "ostracisme", "outrecuidance",
        "parangon", "parjure", "probe", "pugnace", "pusillanime", "taciturne",
        "thuriféraire", "turpitude", "urbanité", "velléité", "vilipender",
        "collusion", "incurie", "impéritie", "procrastination", "sycophante",
        "dilettante", "esthète", "soporifique", "épigone", "condottiere",
        "estafette", "grognard", "impertinence", "impérieux", "indocile",
        "insidieux", "insoumis", "intègre", "libidineux", "licencieux",
        "mentor", "moujik", "méticuleux", "ombrageux", "ostentatoire",
        "outrageux", "patricien", "plébéien", "pointilleux", "préséance",
        "renégat", "scabreux", "scrupuleux", "sourcilleux", "valétudinaire",
        "vergogne", "égrotant"
    ]
}

# Listes pour le Registre et la Tonalité
REGISTRE_WORDS = {
    "ARCHAIQUE_RECHERCHE": [
        "aède", "calice", "codex", "héraut", "libation", "oblation", "ostensoir",
        "patène", "psautier", "rhapsode", "vélin", "vate", "anachorète",
        "atrabilaire", "cénobite", "estafette", "famélique", "ilote", "larron",
        "malandrin", "moujik", "preux", "sicaire", "spadassin", "sycophante",
        "viateur", "chthonien", "haruspice", "pythonisse", "thaumaturge",
        "aboulie", "noévie", "entéléchie", "cacochyme", "glèbe", "feintise",
        "caduc", "trépas", "vergogne", "condottiere", "potantat", "marmiton",
        "palefrenier", "sbire", "suzerain", "haruspice", "scholastique", "oblation",
        "patène"
    ],
    "POETIQUE_LYRIQUE": [
        "allitération", "assonance", "cantilène", "élégiaque", "élégie",
        "lyrisme", "mélopée", "aurore", "crépuscule", "diaphane", "irisé",
        "luminescence", "nimbe", "opalin", "phosphorescence", "pénombre",
        "vespéral", "alcyon", "arachnéen", "azur", "bocage", "céruléen",
        "effluve", "empyrée", "firmament", "nymphe", "ondine", "sylphide",
        "zéphyr", "éthéré", "onirique", "allégresse", "ataraxie", "félicité",
        "idylle", "indolence", "langueur", "lascif", "mélancolie", "quiétude",
        "spleen", "sérénité", "torpeur", "volupté", "éphémère", "évanescent",
        "frémissement", "murmure", "psalmodie", "flâneur", "noctambule", "mignonne",
        "ménestrel", "troubadour", "viateur"
    ],
    "TRAGIQUE_DRAMATIQUE": [
        "diatribe", "estocade", "invective", "acrimonie", "fielleux", "ignominie",
        "ostracisme", "parjure", "sardonique", "scélérat", "turpitude",
        "vilipender", "vitriolique", "exsangue", "fuligineux", "ténébreux",
        "anathème", "némésis", "abattement", "accablement", "affliction",
        "anémie", "courroux", "morose", "neurasthénie", "stupeur", "trépas",
        "hécatombe", "inexorable", "déliquescence", "périssable"
    ],
    "COMIC_BURLESQUE": [
        "amphigourique", "faconde", "garrulité", "pataquès", "quiproquo",
        "soliloque", "verbeux", "volubile", "facétieux", "goguenard", "grivois",
        "grivoiserie", "imbroglio", "narquois", "simagrée", "ubuesque",
        "kafkaïen", "turlupiner", "foucade"
    ]
}

# Listes pour le Profil Émotionnel
EMOTION_WORDS = {
    "POSITIF_EXCITANT": [
        "alacrité", "allégresse", "effervescence", "exaltaion", "exaltation",
        "extase", "ferveur", "jubilation", "pétulant", "exultation", "enthousiasme"
    ],
    "NEGATIF_EXCITANT": [
        "acrimonie", "courroux", "diatribe", "invective", "fustiger",
        "vindicatif", "acrimonieux", "hargne", "invective", "blâme", "collusion",
        "ignominie", "tumulte"
    ],
    "POSITIF_CALME": [
        "bonhomie", "clémence", "félicité", "quiétude", "sérénité", "tempérance",
        "mansuétude", "volupté", "limpide", "lustral", "sérénité", "placidité"
    ],
    "NEGATIF_CALME": [
        "abattement", "accablement", "affliction", "apathie", "atonie",
        "désabusé", "indolence", "langueur", "lascif", "léthargie", "morose",
        "mélancolie", "neurasthénie", "spleen", "torpeur", "évanescent",
        "fugace", "déliquescence", "exsangue", "caducité", "morosité"
    ]
}

# Époque d'apparition / usage
EPOQUE_WORDS = {
    "CLASSIQUE_17_18": [
        "aède", "apophtegme", "ratiociner", "libation", "oblation", "ostensoir",
        "patène", "psautier", "cénobite", "condottiere", "ilote", "potentat",
        "preux", "suzerain", "haruspice", "scholastique", "courroux", "trépas",
        "soliloque", "faconde", "harangue", "magnanime", "mansuétude", "probe",
        "taciturne", "urbanité", "vergogne", "maxime", "patène", "cénobite",
        "ilote", "preux", "sbire", "spadassin"
    ],
    "ROMANTIQUE_19": [
        "spleen", "mélancolie", "céruléen", "empyrée", "nébuleux", "crépuscule",
        "diaphane", "exsangue", "opalin", "pénombre", "ténébreux", "bucolique",
        "zéphyr", "chimère", "onirique", "volupté", "élégiaque", "élégie",
        "lyrisme", "mélopée", "langueur", "lascif", "torpeur", "évanescent",
        "flâneur", "funambule", "cénacle", "diatribe"
    ],
    "MODERNE_20": [
        "neurasthénie", "résilience"
    ],
    "CONTEMPORAIN_21": [
        "masterclass", "demisexuel", "queer", "non-binaire", "cisgenre",
        "intersectionnel"
    ]
}

MODERN_BUZZWORDS = {
    "procrastination": 3.6,
    "procrastiner": 3.4,
    "résilience": 3.8,
    "résilient": 3.5
}

def get_fallback_zipf(word_lower, lexique):
    if word_lower in MODERN_BUZZWORDS:
        return MODERN_BUZZWORDS[word_lower]
        
    best_zipf = 0.0
    if word_lower in lexique:
        fl = lexique[word_lower]['freqlivres']
        ff = lexique[word_lower]['freqfilms']
        freq = max(fl, ff)
        if freq > 0:
            best_zipf = math.log10(freq) + 3.0
            
    fallbacks = []
    
    # 1. Noms en -issement -> verbe en -ir (ex. bannissement -> bannir)
    if word_lower.endswith("issement"):
        fallbacks.append(word_lower[:-8] + "ir")
    # Noms en -ement -> verbe en -er (ex. achèvement -> achever)
    elif word_lower.endswith("ement") and not word_lower.endswith("issement"):
        fallbacks.append(word_lower[:-5] + "er")
        
    # 2. Noms en -ation -> verbe en -er ou formes courtes (ex. séquestration -> séquestrer/séquestre/séquestré)
    if word_lower.endswith("ation"):
        fallbacks.append(word_lower[:-5] + "er")
        fallbacks.append(word_lower[:-5] + "e")
        fallbacks.append(word_lower[:-5] + "é")
        
    # 3. Adjectifs en -able/-issable -> verbe en -er/-ir (ex. périssable -> périr)
    if word_lower.endswith("able"):
        if word_lower.endswith("issable"):
            fallbacks.append(word_lower[:-7] + "ir")
        else:
            fallbacks.append(word_lower[:-4] + "er")
            fallbacks.append(word_lower[:-4] + "ir")
            
    # 4. Adjectifs spécifiques en -imeux -> nom en -in (ex. venimeux -> venin)
    if word_lower.endswith("imeux"):
        fallbacks.append(word_lower[:-5] + "in")
            
    # 5. Mots en -icité / -ibilité / -ité spécifique
    if word_lower.endswith("ibilité"):
        fallbacks.append(word_lower[:-7] + "ible")
    elif word_lower.endswith("icité"):
        fallbacks.append(word_lower[:-5] + "ique")
        fallbacks.append(word_lower[:-5] + "ice")
    elif word_lower.endswith("ité"):
        if word_lower.endswith("acité"):
            fallbacks.append(word_lower[:-5] + "ace")
        
    # 6. Suffixes d'adjectifs en -ique spécifique (ex. aphoristique -> aphorisme)
    if word_lower.endswith("ique"):
        if word_lower.endswith("istique"):
            fallbacks.append(word_lower[:-7] + "isme")
            
    for fb in fallbacks:
        if fb in lexique:
            fl = lexique[fb]['freqlivres']
            ff = lexique[fb]['freqfilms']
            freq = max(fl, ff)
            if freq > 0:
                fb_zipf = math.log10(freq) + 3.0
                inherited_zipf = fb_zipf - 0.1
                if inherited_zipf > best_zipf:
                    best_zipf = inherited_zipf
                    
    return best_zipf

# Liste explicite d'objets ou êtres concrets (physiques) pour baisser l'abstraction
CONCRETE_OBJECTS = [
    "calice", "codex", "grimoire", "patène", "psautier", "vélin", "alcyon",
    "chrysanthème", "constellation", "corolle", "dryade", "labyrinthe",
    "nymphe", "ondine", "parvis", "pistil", "pétale", "sépale", "solstice",
    "sylphide", "zodiaque", "étamine", "marmiton", "brigand", "condottiere",
    "estafette", "flibustier", "grognard", "larron", "malandrin", "maraudeur",
    "moujik", "paladin", "palefrenier", "satyre", "sbire", "sicaire",
    "spadassin", "chaume", "décombre", "arène", "glèbe", "ostensoir"
]

def calculate_abstraction(word_lower, pole):
    """
    Calcule un score d'abstraction continu (0.00 = très concret, 1.00 = extrêmement abstrait).
    Basé sur le pôle sémantique et des heuristiques morphologiques/lexicales.
    """
    # Base par pôle sémantique
    if pole == "PHILOSOPHIE_ET_IDEES":
        base_abs = 0.85
    elif pole == "SENTIMENTS_ET_PSYCHE":
        base_abs = 0.75
    elif pole == "ESPRIT_ET_CARACTERE":
        base_abs = 0.60
    elif pole == "ARTS_ET_LANGAGE":
        base_abs = 0.50
    else: # NATURE_ET_COSMOS
        base_abs = 0.20
        
    # Boost de suffixes d'abstraction typiques (-isme, -logie, -ence, -tence, -té, -ité, -tion, -ance)
    suffix_boost = 0.0
    if any(word_lower.endswith(s) for s in ['isme', 'logie', 'ence', 'tence', 'té', 'ité', 'tion', 'ance', 'ude']):
        suffix_boost = 0.15
    elif any(word_lower.endswith(s) for s in ['ique', 'iste', 'aire', 'el']):
        suffix_boost = 0.05
        
    # Pénalité de concrétitude (si c'est un objet physique, un lieu ou un être vivant concret)
    concrete_penalty = 0.0
    if word_lower in CONCRETE_OBJECTS:
        concrete_penalty = 0.35
    elif pole == "NATURE_ET_COSMOS" and any(word_lower.endswith(s) for s in ['e', 'a', 'on']):
        concrete_penalty = 0.15
        
    abs_score = base_abs + suffix_boost - concrete_penalty
    return max(0.0, min(1.0, round(abs_score, 3)))

def classify_word(word, theme, lexique, pageviews):
    word_lower = word.lower().strip()
    
    # 1. Pôle sémantique
    pole = THEMES_SPECIFIC_MAP.get(word_lower)
    if not pole:
        pole = POLE_MAP.get(theme, "PHILOSOPHIE_ET_IDEES")
        
    # 2. Domaine d'écriture
    domaine = "ROMANESQUE" # Par défaut
    for dom, words in DOMAINE_WORDS.items():
        if word_lower in words:
            domaine = dom
            break
            
    # 3. Registre & Tonalité
    registre = "LITTERAIRE_STANDARD"
    for reg, words in REGISTRE_WORDS.items():
        if word_lower in words:
            registre = reg
            break
            
    # 4. Profil émotionnel
    emotion = "NEUTRE"
    for emo, words in EMOTION_WORDS.items():
        if word_lower in words:
            emotion = emo
            break
            
    # 5. Époque d'apparition
    epoque = "ROMANTIQUE_19" # Par défaut
    for ep, words in EPOQUE_WORDS.items():
        if word_lower in words:
            epoque = ep
            break
            
    # 6. Difficulté continue classique recalibrée (avec replis morphologiques et surcharges)
    zipf = get_fallback_zipf(word_lower, lexique)
    
    # Zipf bounds recalibrated so Zipf >= 3.3 (common words) has 0.0 base difficulty
    zipf_max = 3.3
    zipf_min = 1.2
    if zipf >= zipf_max:
        diff_base = 0.0
    elif zipf <= zipf_min:
        diff_base = 1.0
    else:
        diff_base = (zipf_max - zipf) / (zipf_max - zipf_min)
        
    # Pénalité de longueur réduite : +0.01 par lettre au-delà de 5 (max +0.08)
    len_bonus = max(0.0, min(0.08, (len(word_lower) - 5) * 0.01))
    
    # Suffixes techniques : seulement les plus complexes, bonus réduit à 0.05
    suffix_bonus = 0.0
    if any(word_lower.endswith(s) for s in ['phisme', 'logie', 'trique', 'phie', 'isme']):
        suffix_bonus = 0.05
        
    # Malus dynamique basé sur les pageviews Wiktionnaire (buzzword)
    # Appliqué uniquement aux mots rares historiquement (Zipf < 2.5) pour éviter de fausser les mots littéraires recherchés
    buzz_malus = 0.0
    views = pageviews.get(word_lower, 0)
    if zipf < 2.5 and views > 1000:
        buzz_malus = min(0.25, math.log10(views / 1000.0) * 0.15)
        
    difficulty = diff_base + len_bonus + suffix_bonus - buzz_malus
    difficulty = max(0.0, min(1.0, round(difficulty, 3)))
    
    # 7. Difficulté continue basée sur l'Abstraction
    difficulty_abstraction = calculate_abstraction(word_lower, pole)
    
    # 8. Niveau de Pertinence (Dynamique)
    pertinence = 0.50
    
    return {
        "word": word,
        "pole": pole,
        "domaine": domaine,
        "registre": registre,
        "emotion": emotion,
        "epoque": epoque,
        "difficulty": difficulty,
        "difficulty_abstraction": difficulty_abstraction,
        "pertinence": pertinence
    }

def main():
    print("Chargement des mots de C1...")
    if not os.path.exists(WORDS_JSON_PATH):
        print(f"Erreur: {WORDS_JSON_PATH} introuvable.")
        return
        
    with open(WORDS_JSON_PATH, "r", encoding="utf-8") as f:
        words_data = json.load(f)
        
    print(f"{len(words_data)} mots charges.")
    lexique = load_lexique()
    print(f"Lexique charge avec {len(lexique)} mots.")
    
    # Chargement du cache des pageviews
    PAGEVIEWS_PATH = os.path.join(TOOLS_DIR, "c1_wiktionary_pageviews.json")
    if os.path.exists(PAGEVIEWS_PATH):
        with open(PAGEVIEWS_PATH, "r", encoding="utf-8") as f:
            pageviews = json.load(f)
        print(f"Cache des pageviews charge avec {len(pageviews)} mots.")
    else:
        pageviews = {}
        print("Warning: c1_wiktionary_pageviews.json introuvable. Aucun malus buzz ne sera applique.")
    
    classified_list = []
    for item in words_data:
        word = item["mot"]
        theme = item["theme"]
        res = classify_word(word, theme, lexique, pageviews)
        classified_list.append(res)
        
    # Écriture du fichier de sortie CSV
    os.makedirs(os.path.dirname(OUTPUT_CSV_PATH), exist_ok=True)
    with open(OUTPUT_CSV_PATH, mode='w', encoding='utf-8', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow(['Mot', 'Pole_Semantique', 'Domaine_Ecriture', 'Registre_Tonalite', 'Profil_Emotionnel', 'Epoque', 'Difficulte', 'Difficulte_Abstraction', 'Pertinence'])
        for item in classified_list:
            writer.writerow([
                item['word'],
                item['pole'],
                item['domaine'],
                item['registre'],
                item['emotion'],
                item['epoque'],
                f"{item['difficulty']:.3f}",
                f"{item['difficulty_abstraction']:.3f}",
                f"{item['pertinence']:.2f}"
            ])
            
    print(f"Classification terminee avec succes. Fichier cree : {OUTPUT_CSV_PATH}")
    
    # Statistiques rapides
    stats = {"pole": {}, "domaine": {}, "registre": {}, "emotion": {}, "epoque": {}}
    for item in classified_list:
        for k in stats.keys():
            val = item[k]
            stats[k][val] = stats[k].get(val, 0) + 1
            
    print("\n--- STATISTIQUES DE CLASSIFICATION ---")
    for category, dist in stats.items():
        print(f"\nDistribution {category.capitalize()} :")
        for val, count in sorted(dist.items(), key=lambda x: x[1], reverse=True):
            print(f"  - {val} : {count} ({count/len(classified_list)*100:.1f}%)")

if __name__ == "__main__":
    main()
