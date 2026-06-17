# -*- coding: utf-8 -*-
"""
Script de classification automatique V2 des 407 mots de C1.
Intègre les normes psycholinguistiques de Desrochers (FreqSub_Imag_3600)
et un ajustement dynamique de la difficulté pour les mots contemporains (pageviews Wiktionnaire).
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
DESROCHERS_TSV_PATH = os.path.join(TOOLS_DIR, "FreqSub_Imag_3600.tsv")

OUTPUT_CSV_PATH = os.path.join(
    os.path.dirname(TOOLS_DIR),
    "docs", "amelioration_de_la_fonction_de_recherche",
    "algorithme_de_presentation_des_extraits", "classified_c1_words_v2.csv"
)

# Chargement de la base Desrochers (FreqSub_Imag_3600)
def load_desrochers():
    desrochers = {}
    if not os.path.exists(DESROCHERS_TSV_PATH):
        print(f"Warning: {DESROCHERS_TSV_PATH} introuvable. Aucun score Desrochers ne sera applique.")
        return {}
    
    with open(DESROCHERS_TSV_PATH, "r", encoding="latin-1") as f:
        reader = csv.DictReader(f, delimiter="\t")
        for row in reader:
            word = row.get('Mot', '').lower().strip()
            if not word:
                continue
            try:
                freq_mean = float(row.get('FREQ_Mean', 0.0))
            except ValueError:
                freq_mean = 0.0
            try:
                image_mean = float(row.get('IMAGE_Mean', 0.0))
            except ValueError:
                image_mean = 0.0
            
            desrochers[word] = {
                'freq_mean': freq_mean,
                'image_mean': image_mean
            }
    print(f"Base Desrochers chargee avec {len(desrochers)} mots.")
    return desrochers

# Chargement du Lexique383
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
                
                lemme = row.get('lemme', '').lower().strip()
                cgram = row.get('cgram', '').upper().strip()
                
                if word in lexique:
                    lexique[word]['freqlivres'] += freqlivres
                    lexique[word]['freqfilms'] += freqfilms
                else:
                    lexique[word] = {
                        'freqlivres': freqlivres,
                        'freqfilms': freqfilms,
                        'lemme': lemme,
                        'cgram': cgram
                    }
    return lexique

# RÈGLES DE MAPPING
POLE_MAP = {
    "Art & Langage": "ARTS_ET_LANGAGE",
    "Esprit & Caractère": "ESPRIT_ET_CARACTERE",
    "Nature & Cosmos": "NATURE_ET_COSMOS",
    "Philosophie & Idées": "PHILOSOPHIE_ET_IDEES",
    "Sentiments & Psyché": "SENTIMENTS_ET_PSYCHE"
}

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

def get_fallback_zipf(word_lower, lexique, pageviews, difficulty_abstraction, registre, epoque):
    best_zipf = 0.0
    ff = 0.0
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
        
    # 2. Noms en -ation -> verbe en -er ou formes courtes (ex. séquestration -> séquestrer)
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
            ff_fb = lexique[fb]['freqfilms']
            ff = max(ff, ff_fb)
            freq = max(fl, ff_fb)
            if freq > 0:
                fb_zipf = math.log10(freq) + 3.0
                inherited_zipf = fb_zipf - 0.1
                if inherited_zipf > best_zipf:
                    best_zipf = inherited_zipf
                    
    # AJUSTEMENT DYNAMIQUE (BUZZWORDS) - FORMULE V2.8 :
    # Si le mot a un nombre élevé de pageviews, on simule une fréquence moderne accrue.
    # Les mots archaïques ou classiques ne bénéficient pas de ce boost (recherche motivée par la curiosité).
    views = pageviews.get(word_lower, 0)
    if views > 1000:
        is_archaic = (registre == "ARCHAIQUE_RECHERCHE" or epoque == "CLASSIQUE_17_18")
        if registre == "LITTERAIRE_STANDARD" and not is_archaic:
            boost = math.log10(views / 1000.0) * 1.5
            coef = max(0.1, 1.2 - difficulty_abstraction)
            base = max(best_zipf, 2.2)
            dynamic_zipf = base + boost * coef
        else:
            # Très léger boost pour les mots purement livresques ou archaïques populaires en recherche
            boost = math.log10(views / 1000.0) * 0.2
            base = best_zipf if best_zipf > 0 else 1.0
            dynamic_zipf = base + boost
            
        if dynamic_zipf > best_zipf:
            best_zipf = dynamic_zipf
            
    return best_zipf

# Liste explicite d'objets ou êtres concrets (physiques) pour baisser l'abstraction si hors base Desrochers
CONCRETE_OBJECTS = [
    "calice", "codex", "grimoire", "patène", "psautier", "vélin", "alcyon",
    "chrysanthème", "constellation", "corolle", "dryade", "labyrinthe",
    "nymphe", "ondine", "parvis", "pistil", "pétale", "sépale", "solstice",
    "sylphide", "zodiaque", "étamine", "marmiton", "brigand", "condottiere",
    "estafette", "flibustier", "grognard", "larron", "malandrin", "maraudeur",
    "moujik", "paladin", "palefrenier", "satyre", "sbire", "sicaire",
    "spadassin", "chaume", "décombre", "arène", "glèbe", "ostensoir"
]

def calculate_abstraction(word_lower, pole, lexique, desrochers):
    """
    Calcule un score d'abstraction continu (0.00 = très concret, 1.00 = extrêmement abstrait).
    V2 : S'appuie d'abord sur la base Desrochers (FreqSub_Imag_3600), sinon utilise un fallback.
    """
    def clean_accent(w):
        w = w.replace("é", "").replace("è", "").replace("à", "").replace("ù", "")
        w = w.replace("â", "").replace("ê", "").replace("î", "").replace("ô", "").replace("û", "")
        w = w.replace("ç", "")
        return w
    
    clean_w = clean_accent(word_lower)
    
    # 1. Recherche directe dans la base Desrochers
    target_entry = desrochers.get(word_lower) or desrochers.get(clean_w)
    
    # 2. Recherche par lemme si verbe/adjectif
    if not target_entry and word_lower in lexique:
        lemma = lexique[word_lower].get('lemme')
        if lemma:
            clean_lem = clean_accent(lemma)
            target_entry = desrochers.get(lemma) or desrochers.get(clean_lem)
            
    # 3. Fallbacks morphologiques vers des substantifs dans Desrochers
    if not target_entry:
        morpho_fallbacks = []
        if word_lower.endswith("er") or word_lower.endswith("ir"):
            morpho_fallbacks.append(word_lower[:-2] + "ation")
            morpho_fallbacks.append(word_lower[:-2] + "ement")
        elif word_lower.endswith("eux") or word_lower.endswith("ive") or word_lower.endswith("if"):
            morpho_fallbacks.append(word_lower[:-3] + "ité")
            morpho_fallbacks.append(word_lower[:-3] + "ence")
            
        for mf in morpho_fallbacks:
            clean_mf = clean_accent(mf)
            target_entry = desrochers.get(mf) or desrochers.get(clean_mf)
            if target_entry:
                break
                
    # Si on a trouvé une entrée dans Desrochers
    if target_entry and target_entry['image_mean'] > 0:
        imageability = target_entry['image_mean'] # Entre 1.0 (abstrait) et 7.0 (concret)
        # Normalisation entre 0.0 et 1.0 (0.0 = très concret, 1.0 = très abstrait)
        abs_score = (7.0 - imageability) / 6.0
        return max(0.0, min(1.0, round(abs_score, 3)))
        
    # 4. Fallback Heuristique sémantique (V1 originale)
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
        
    suffix_boost = 0.0
    if any(word_lower.endswith(s) for s in ['isme', 'logie', 'ence', 'tence', 'té', 'ité', 'tion', 'ance', 'ude']):
        suffix_boost = 0.15
    elif any(word_lower.endswith(s) for s in ['ique', 'iste', 'aire', 'el']):
        suffix_boost = 0.05
        
    concrete_penalty = 0.0
    if word_lower in CONCRETE_OBJECTS:
        concrete_penalty = 0.35
    elif pole == "NATURE_ET_COSMOS" and any(word_lower.endswith(s) for s in ['e', 'a', 'on']):
        concrete_penalty = 0.15
        
    abs_score = base_abs + suffix_boost - concrete_penalty
    return max(0.0, min(1.0, round(abs_score, 3)))

def guess_origine_geographique(word_lower):
    """
    Détermine l'origine géographique d'un mot.
    Seules les origines géographiques réelles sont retenues :
    RUSSE, ITALIEN, ANGLAIS, ALLEMAND, FRANCAIS.
    Les mots à racines gréco-latines assimilés au français sont classifiés FRANCAIS.
    """
    slavic_words = ['moujik', 'tsar', 'steppes']
    italian_words = [
        'dilettante', 'condottiere', 'estocade', 'imbroglio', 'quiproquo',
        'fiasco', 'incognito', 'pantalon', 'bouffon', 'carrousel', 'balcon'
    ]
    english_words = [
        'spleen', 'masterclass', 'queer', 'non-binaire', 'cisgenre',
        'intersectionnel', 'sérendipité', 'chiller', 'chilling'
    ]
    german_words = ['leitmotiv', 'diktat', 'kafkaïen', 'nickel']

    if word_lower in slavic_words:
        return 'RUSSE'
    if word_lower in italian_words:
        return 'ITALIEN'
    if word_lower in english_words:
        return 'ANGLAIS'
    if word_lower in german_words:
        return 'ALLEMAND'

    # Par défaut : FRANCAIS (inclut tous les mots d'origine gréco-latine
    # assimilés, ainsi que les mots purement français)
    return 'FRANCAIS'

def guess_epoque_v2(word_lower, registre, lexique, pageviews):
    # 1. Graines explicites de mots contemporains et modernes
    contemporary_seeds = ['masterclass', 'demisexuel', 'queer', 'non-binaire', 'cisgenre', 'intersectionnel']
    if any(s in word_lower for s in contemporary_seeds):
        return 'CONTEMPORAIN_21'
        
    modern_seeds = ['résilience', 'procrastiner', 'procrastination', 'neurasthénie']
    if word_lower in modern_seeds:
        return 'MODERNE_20'

    # Suffixes contemporains
    if any(word_lower.endswith(s) for s in ['ing', 'queer', 'genre', 'binaire']):
        return 'CONTEMPORAIN_21'

    # Vérification archaïque / classique d'abord pour bloquer le boost moderne
    is_historically_classic = (registre == "ARCHAIQUE_RECHERCHE" or any(word_lower in words for ep, words in EPOQUE_WORDS.items() if ep == "CLASSIQUE_17_18"))

    # Récupération des statistiques Lexique383
    fl = 0.0
    ff = 0.0
    if word_lower in lexique:
        fl = lexique[word_lower]['freqlivres']
        ff = lexique[word_lower]['freqfilms']
        
    views = pageviews.get(word_lower, 0)
    
    if not is_historically_classic:
        # 2. Popularité Moderne OU Oralité (Priorité Haute)
        if views > 8000 or (fl > 0 and ff / fl > 8.0 and ff > 0.5):
            return 'CONTEMPORAIN_21'
        elif views > 3000 or (fl > 0 and ff / fl > 3.0 and ff > 0.2) or (ff > 0 and fl == 0 and (ff > 1.0 or views > 2000)):
            return 'MODERNE_20'
        
    # 3. Ratios Littéraires Historiques (Écrit Classique)
    if (fl > 0 and ff > 0 and fl / ff > 15.0 and fl > 1.0) or (fl > 2.0 and ff == 0) or is_historically_classic:
        return 'CLASSIQUE_17_18'
        
    # 4. Registre Lexical (En repli de dernier recours)
    if registre == "POETIQUE_LYRIQUE":
        return "ROMANTIQUE_19"

    # 5. Valeur par défaut
    return 'ROMANTIQUE_19'

def classify_word(word, theme, lexique, pageviews, desrochers):
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
            
    # 5. Origine Géographique & Époque d'apparition (Dynamique)
    origine_geographique = guess_origine_geographique(word_lower)
    
    # Listes des mots d'antiquité explicites (mythologie, antiquité romaine/grecque)
    antiquite_words = [
        'aède', 'codex', 'ilote', 'rhapsode', 'haruspice', 'pythonisse', 'thaumaturge',
        'dryade', 'nymphe', 'sylphide', 'ondine', 'stichomythie', 'apophtegme', 'patène',
        'ostensoir', 'libation', 'ade', 'patne', 'vate'
    ]
    
    if word_lower in antiquite_words:
        epoque = "ANTIQUITE"
    else:
        epoque = guess_epoque_v2(word_lower, registre, lexique, pageviews)
            
    # 6. Abstraction (calculée en premier car nécessaire pour Zipf modulé)
    difficulty_abstraction = calculate_abstraction(word_lower, pole, lexique, desrochers)
            
    # 7. Difficulté continue classique recalibrée (avec pageviews dynamiques et modulation)
    zipf = get_fallback_zipf(word_lower, lexique, pageviews, difficulty_abstraction, registre, epoque)
    
    # Bornes Zipf recalibrées
    zipf_max = 3.3
    zipf_min = 1.2
    if zipf >= zipf_max:
        diff_base = 0.0
    elif zipf <= zipf_min:
        diff_base = 1.0
    else:
        diff_base = (zipf_max - zipf) / (zipf_max - zipf_min)
        
    # Pénalité de longueur : +0.01 par lettre au-delà de 5 (max +0.08)
    len_bonus = max(0.0, min(0.08, (len(word_lower) - 5) * 0.01))
    
    # Suffixes techniques : +0.05
    suffix_bonus = 0.0
    if any(word_lower.endswith(s) for s in ['phisme', 'logie', 'trique', 'phie', 'isme']):
        suffix_bonus = 0.05
        
    # Malus dynamique basé sur les pageviews Wiktionnaire (buzzword)
    buzz_malus = 0.0
    views = pageviews.get(word_lower, 0)
    if zipf < 2.5 and views > 1000:
        buzz_malus = min(0.15, math.log10(views / 1000.0) * 0.10)
        
    difficulty = diff_base + len_bonus + suffix_bonus - buzz_malus
    difficulty = max(0.0, min(1.0, round(difficulty, 3)))
    
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
        "pertinence": pertinence,
        "origine_geographique": origine_geographique
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
        print("Warning: c1_wiktionary_pageviews.json introuvable.")
        
    # Base Desrochers
    desrochers = load_desrochers()
    
    classified_list = []
    for item in words_data:
        word = item["mot"]
        theme = item["theme"]
        res = classify_word(word, theme, lexique, pageviews, desrochers)
        classified_list.append(res)
        
    # Écriture du fichier de sortie CSV
    os.makedirs(os.path.dirname(OUTPUT_CSV_PATH), exist_ok=True)
    with open(OUTPUT_CSV_PATH, mode='w', encoding='utf-8', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow(['Mot', 'Pole_Semantique', 'Domaine_Ecriture', 'Registre_Tonalite', 'Profil_Emotionnel', 'Epoque', 'Difficulte', 'Difficulte_Abstraction', 'Pertinence', 'Origine_Geographique'])
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
                f"{item['pertinence']:.2f}",
                item['origine_geographique']
            ])
            
    print(f"Classification terminee avec succes. Fichier cree : {OUTPUT_CSV_PATH}")

if __name__ == "__main__":
    main()
