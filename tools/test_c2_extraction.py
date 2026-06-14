import os
import json
import urllib.request
import urllib.parse
import zipfile
import csv
import math
import re
import time

ZIP_URL = "http://www.lexique.org/databases/Lexique383/Lexique383.zip"
ZIP_PATH = "Lexique383.zip"

def get_lexique_data():
    if not os.path.exists(ZIP_PATH):
        print(f"Downloading Lexique383.zip from {ZIP_URL}...")
        urllib.request.urlretrieve(ZIP_URL, ZIP_PATH)
        print("Download complete.")
    else:
        print("Lexique383.zip already downloaded.")

    lexique = {}
    with zipfile.ZipFile(ZIP_PATH, 'r') as z:
        tsv_filename = None
        for name in z.namelist():
            if name.endswith('.tsv') or name.endswith('.txt'):
                tsv_filename = name
                break
        
        if not tsv_filename:
            raise Exception("No TSV or TXT file found in Lexique383.zip")
        
        print(f"Parsing {tsv_filename}...")
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
                    lexique[word] = {
                        'freqlivres': freqlivres,
                        'freqfilms': freqfilms
                    }
                    
    print(f"Loaded {len(lexique)} unique words from Lexique383.")
    return lexique

# Predefined high-quality local definitions in case of Wiktionary rate limits (429)
LOCAL_DEFINITIONS = {
    "nonobstant": "Malgré, en dépit de.",
    "dialectique": "Méthode de raisonnement consistant à analyser la réalité en mettant en évidence ses contradictions et à tenter de les dépasser.",
    "péremptoire": "Qui détruit d’avance toute objection ; contre quoi on ne peut rien répliquer.",
    "partant": "Par conséquent, donc.",
    "plausible": "Qui semble devoir être admis ; admissible, croyable.",
    "réquisitoire": "Discours ou écrit dans lequel on accumule les accusations contre quelqu'un ou quelque chose.",
    "diatribe": "Critique violente, pamphlet, écrit satirique ou argumentaire à charge.",
    "fallacieux": "Qui est trompeur, mensonger, illusoire.",
    "étayer": "Soutenir, appuyer une thèse ou un argument par des faits ou des preuves.",
    "plaidoyer": "Discours verbal ou écrit en faveur de quelqu’un ou de quelque chose.",
    "harangue": "Discours solennel, pompeux ou moralisateur adressé à une assemblée ou à une personne.",
    "réfuter": "Combattre et rejeter une affirmation, un argument ou une thèse en prouvant sa fausseté.",
    "concéder": "Admettre ou accorder un point à l'adversaire dans une discussion ou un débat.",
    "indubitable": "Dont on ne peut douter ; certain, évident.",
    "discréditer": "Faire perdre sa valeur, son crédit ou sa réputation à quelqu'un ou quelque chose.",
    "apologue": "Court récit écrit en prose ou en vers, dont on tire une instruction morale.",
    "allégorie": "Représentation d’une idée abstraite sous la forme d’une image, d’un tableau ou d’un récit.",
    "antinomie": "Contradiction réelle ou apparente entre deux lois, deux principes ou deux idées.",
    "dichotomie": "Division de quelque chose en deux parties souvent opposées.",
    "postulat": "Principe non démontré que l'on admet comme base d'un raisonnement.",
    "axiome": "Vérité évidente par elle-même qui ne nécessite aucune démonstration.",
    "assertion": "Proposition ou affirmation soutenue comme vraie.",
    "inférence": "Opération logique par laquelle on admet une proposition en vertu de sa liaison avec d'autres propositions déjà admises.",
    "syllogisme": "Raisonnement logique composé de trois propositions (deux prémisses et une conclusion) tel que la conclusion découle nécessairement des prémisses.",
    "sophisme": "Raisonnement qui présente une apparence de logique mais qui est volontairement trompeur ou fallacieux.",
    "paralogisme": "Faux raisonnement fait de bonne foi, contrairement au sophisme.",
    "tautologie": "Proposition qui répète la même chose sous des formes différentes, raisonnement circulaire.",
    "pléonasme": "Répétition de termes ayant le même sens (ex: monter en haut).",
    "litote": "Figure de rhétorique qui consiste à dire moins pour faire entendre plus (ex: ce n'est pas mauvais).",
    "allégation": "Affirmation que l'on produit pour soutenir une cause ou un fait, souvent sans preuve immédiate.",
    "digression": "Action de s'écarter du sujet principal dans un discours ou un écrit.",
    "anaphore": "Répétition d'un mot ou d'un groupe de mots au début de plusieurs phrases ou propositions successives.",
    "chiasme": "Figure de style croisant des éléments de manière symétrique (ex: Il faut manger pour vivre et non vivre pour manger).",
    "antithèse": "Mise en opposition de deux expressions ou idées contraires.",
    "oxymore": "Alliance de deux mots de sens opposés dans un même groupe syntaxique (ex: un silence éloquent).",
    "paralipse": "Figure par laquelle on déclare passer sous silence une chose sur laquelle on attire néanmoins l'attention.",
    "casuistique": "Partie de la théologie morale qui applique les principes généraux à des cas particuliers de conscience.",
    "métaphore": "Figure de style qui consiste à désigner une chose par une autre en vertu d'une analogie sémantique.",
    "métonymie": "Figure par laquelle on remplace un mot par un autre lié par un rapport de contiguïté (ex: boire un verre).",
    "synecdoque": "Type de métonymie consistant à prendre la partie pour le tout, ou le tout pour la partie.",
    "hyperbole": "Exagération extrême visant à frapper les esprits.",
    "prosopopée": "Figure consistant à faire parler ou agir une personne absente, un mort, un animal ou une chose personnifiée.",
    "prétérition": "Action de parler de quelque chose en feignant de ne pas vouloir en parler.",
    "ellipse": "Omission volontaire d'un mot qui peut être facilement rétabli par le contexte.",
    "parabole": "Récit allégorique contenant un enseignement moral ou religieux.",
    "objecter": "Opposer un argument ou une objection à une proposition.",
    "inférer": "Tirer une conséquence d'un principe ou d'un fait.",
    "corroborer": "Confirmer ou fortifier une théorie ou une déclaration par de nouvelles preuves.",
    "subodorer": "Pressentir ou deviner quelque chose par intuition.",
    "fustiger": "Critiquer avec une extrême violence ou condamner sévèrement.",
    "tempérer": "Modérer ou adoucir des propos ou des émotions.",
    "élucubrer": "Créer des théories ou des écrits extravagants à force de réflexions laborieuses.",
    "conjecturer": "Supposer ou faire des hypothèses sur la base de probabilités.",
    "vilipender": "Dénigrer ou rabaisser quelqu'un avec mépris.",
    "invalider": "Rendre nul ou priver de valeur légale ou scientifique.",
    "sous-tendre": "Servir de base, de fondement ou de structure cachée à quelque chose.",
    "discréditer": "Faire perdre la confiance, le crédit ou l'autorité de quelqu'un.",
    "légitimer": "Reconnaître comme conforme aux lois, aux règles ou à la raison.",
    "infirmer": "Affaiblir ou contredire une hypothèse ou une décision antérieure.",
    "arguer": "Tirer argument de quelque chose, ou prétendre.",
    "insinuer": "Faire entendre habilement et indirectement quelque chose.",
    "réhabiliter": "Rétablir quelqu'un dans ses droits, sa réputation ou son honneur.",
    "contester": "Mettre en doute ou refuser de reconnaître la validité d'une opinion ou d'une décision.",
    "persuader": "Amener quelqu'un à croire ou à faire quelque chose en faisant appel à ses sentiments.",
    "convaincre": "Amener quelqu'un à reconnaître la vérité d'une proposition par des preuves logiques.",
    "pérorer": "Parler avec emphase et de manière prétentieuse devant un public.",
    "ergoter": "Chicaner sur des détails insignifiants dans une discussion.",
    "ratiociner": "Raisonner de façon trop subtile et stérile.",
    "philippique": "Discours de combat, attaque verbale violente.",
    "pamphlet": "Court écrit satirique et polémique attaquant une personnalité ou une institution.",
    "controverse": "Discussion argumentée et prolongée sur une question délicate.",
    "polémique": "Débat agressif et contradictoire par écrit ou par parole.",
    "apologie": "Discours ou écrit prenant la défense de quelqu'un ou de quelque chose.",
    "plaidoirie": "Exposé oral fait par un avocat pour défendre une cause devant un tribunal.",
    "allocution": "Discours court prononcé par une personnalité officielle.",
    "exorde": "Première partie d'un discours classique destiné à capter l'attention de l'auditoire.",
    "péroraison": "Conclusion d'un discours qui résume les arguments et en appelle aux émotions.",
}

def fetch_definition(word):
    url = f"https://fr.wiktionary.org/w/api.php?action=parse&page={urllib.parse.quote(word)}&prop=text&redirects=true&format=json&formatversion=2"
    headers = {'User-Agent': 'LexicaAndroid2/1.0 (vocabulaire francais)'}
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=3) as response:
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
    except Exception as e:
        pass
    return None

STATIC_RHETORIC_SEEDS = [
    # Connecteurs & Logique formelle
    "nonobstant", "subséquemment", "corrélativement", "partant", "prémisse", 
    "induction", "déduction", "causalité", "corrélation", "dichotomie", "antinomie", 
    "postulat", "axiome", "assertion", "inférence", "syllogisme",
    # Procédés de sophisme & rhétorique
    "sophisme", "paralogisme", "dialectique", "aporie", "tautologie", "pléonasme", 
    "litote", "euphémisme", "allégation", "digression", "anaphore", "chiasme", "antithèse", 
    "oxymore", "réfutation", "objection", "paralipse", "casuistique", "métaphore", 
    "métonymie", "synecdoque", "hyperbole", "prosopopée", "prétérition", "ellipse", 
    "apologue", "allégorie", "parabole",
    # Verbes d'argumentation active
    "alléguer", "objecter", "inférer", "corroborer", "postuler", "subodorer", "fustiger", 
    "tempérer", "réfuter", "élucubrer", "conjecturer", "concéder", "vilipender", 
    "invalider", "sous-tendre", "étayer", "discréditer", "légitimer", "infirmer", 
    "arguer", "insinuer", "réhabiliter", "contester", "argumenter", 
    "justifier", "persuader", "convaincre", "pérorer", "ergoter", 
    "ratiociner",
    # Adjectifs dialectiques & évaluatifs
    "tendancieux", "spécieux", "péremptoire", "dogmatique", "pragmatique", "indubitable", 
    "probant", "irréfragable", "fallacieux", "captieux", "plausible", "axiomatique", 
    "réfutable", "contingent", "arbitraire", "subjectif", "objectif", "sceptique", 
    "équivoque", "univoque", "lucide", "pertinent", "cohérent", "incohérent", 
    "erroné", "infondé", "véridique",
    # Outils du discours & argumentation
    "diatribe", "réquisitoire", "plaidoyer", "panégyrique", "harangue", "philippique", 
    "pamphlet", "controverse", "polémique", "apologie", "plaidoirie", "allocution", 
    "exorde", "péroraison", "argument",
    # Qualificatifs d'orientation idéologique / sociétale
    "réactionnaire", "progressiste", "conservateur", "réformiste", "gaulliste", 
    "monarchiste", "souverainiste", "mondialiste", "anarchiste", "colbertiste", 
    "jacobin", "girondin", "néolibéral"
]

# Update local definitions with new words if API fails
LOCAL_DEFINITIONS.update({
    "réactionnaire": "Qui s’oppose au progrès social, politique ou économique et cherche à rétablir un état de choses antérieur.",
    "progressiste": "Partisan du progrès social, politique ou économique, et des réformes favorisant ce progrès.",
    "conservateur": "Qui tend à maintenir l'ordre social, politique et les traditions existantes.",
    "réformiste": "Partisan de réformes graduelles et pacifiques au sein du cadre législatif existant.",
    "gaulliste": "Partisan des doctrines politiques fondées sur les idées et l'action du général de Gaulle.",
    "monarchiste": "Partisan de la royauté ou du rétablissement de la monarchie.",
    "souverainiste": "Partisan de la souveraineté nationale face à des organismes supranationaux.",
    "mondialiste": "Partisan d'une organisation mondiale de l'économie ou de la politique.",
    "anarchiste": "Partisan de l'anarchie, rejetant toute autorité étatique et hiérarchique.",
    "colbertiste": "Partisan d'une forte intervention de l'État dans le développement économique et industriel.",
    "jacobin": "Partisan d'une centralisation étatique stricte et d'un pouvoir républicain unitaire.",
    "girondin": "Partisan d'une autonomie locale forte et d'une structure étatique décentralisée ou fédérale.",
    "néolibéral": "Partisan d'un libéralisme économique moderne réduisant l'intervention de l'État au minimum."
})

def main():
    # Load Lexique database
    lexique = get_lexique_data()
    
    candidates = []
    for word in STATIC_RHETORIC_SEEDS:
        word_lower = word.lower()
        if word_lower in lexique:
            stats = lexique[word_lower]
            fl = stats['freqlivres']
            ff = stats['freqfilms']
            
            zipf_books = math.log10(fl) + 3.0 if fl > 0 else 0.0
            zipf_films = math.log10(ff) + 3.0 if ff > 0 else 0.0
            
            if zipf_books >= 3.0:
                difficulty = "Débutant"
            elif zipf_books >= 1.5:
                difficulty = "Intermédiaire"
            else:
                difficulty = "Expert"
                
            candidates.append({
                'word': word,
                'difficulty': difficulty,
                'freqlivres': fl,
                'freqfilms': ff,
                'zipf_books': zipf_books,
                'zipf_films': zipf_films
            })
            
    # Sort candidates by Zipf books descending
    candidates.sort(key=lambda x: x['zipf_books'], reverse=True)
    
    # We want to display all candidates
    top_candidates = candidates
    
    # Fetch definitions or fall back to local dict
    print("Populating definitions...")
    for i, item in enumerate(top_candidates):
        word = item['word']
        word_lower = word.lower()
        
        # Prioritize local definitions to avoid Wiktionary homonym confusion (like "partant")
        if word_lower in LOCAL_DEFINITIONS:
            print(f"[{i+1}/{len(top_candidates)}] Using high-quality local definition for: {word}")
            definition = LOCAL_DEFINITIONS[word_lower]
        else:
            # Fetch definition with a robust sleep to respect API limits (1.5 seconds)
            print(f"[{i+1}/{len(top_candidates)}] Fetching online definition for: {word}")
            definition = fetch_definition(word)
            time.sleep(1.5)
            
            # Fallback to local definitions if online fails
            if not definition:
                definition = "Définition non renseignée"
            
        item['definition'] = definition

    output_dir = r"c:\Users\r0xef\AndroidStudioProjects\LexicaAndroid2\docs\amelioration_de_la_fonction_de_recherche\algorithme_de_presentation_des_extraits"
    os.makedirs(output_dir, exist_ok=True)

    # Save to CSV (classeur) with Difficulty column
    csv_path = os.path.join(output_dir, "candidates_rhetoric_politics.csv")
    with open(csv_path, mode='w', encoding='utf-8', newline='') as f:
        writer = csv.writer(f, delimiter=';')
        writer.writerow(['Mot', 'Difficulté', 'Zipf Livres', 'Zipf Films', 'Freq Livres (ppm)', 'Freq Films (ppm)', 'Définition'])
        for item in top_candidates:
            writer.writerow([
                item['word'],
                item['difficulty'],
                f"{item['zipf_books']:.2f}",
                f"{item['zipf_films']:.2f}",
                f"{item['freqlivres']:.2f}",
                f"{item['freqfilms']:.2f}",
                item['definition']
            ])
            
    # Save to markdown report grouped by difficulty level
    report_path = os.path.join(output_dir, "resultat_mots_test_c2_complet.md")
    
    # Group candidates
    by_diff = {"Débutant": [], "Intermédiaire": [], "Expert": []}
    for item in top_candidates:
        by_diff[item['difficulty']].append(item)
        
    md_lines = [
        "# Rapport d'Extraction de Vocabulaire Rhétorique & Débats (C2) - Version Complète",
        f"Ce rapport présente les {len(top_candidates)} mots qualifiés pour l'objectif **Discussions Soutenues & Rhétorique**, répartis par niveaux de difficulté.",
        "",
    ]
    
    for level in ["Débutant", "Intermédiaire", "Expert"]:
        words_in_level = by_diff[level]
        md_lines.append(f"## Niveau : {level} (Total: {len(words_in_level)} mots)")
        md_lines.append("| Rang | Mot | Zipf Livres | Zipf Films | Définition |")
        md_lines.append("|---|---|---|---|---|")
        for idx, item in enumerate(words_in_level):
            md_lines.append(f"| {idx+1} | **{item['word']}** | {item['zipf_books']:.2f} | {item['zipf_films']:.2f} | {item['definition']} |")
        md_lines.append("")
        
    with open(report_path, "w", encoding="utf-8") as f:
        f.write("\n".join(md_lines))
        
    print(f"CSV spreadsheet generated at: {csv_path}")
    print(f"Markdown report generated at: {report_path}")

if __name__ == "__main__":
    main()
