# -*- coding: utf-8 -*-
"""
word_lists.py — Toutes les constantes et listes de mots utilisées par les classifieurs.

Organisation :
  - POLE_MAP / THEMES_SPECIFIC_MAP      : Pôle sémantique
  - DOMAINE_WORDS                        : Domaine d'écriture
  - REGISTRE_WORDS                       : Registre & Tonalité
  - EMOTION_WORDS                        : Profil émotionnel
  - CONCRETE_OBJECTS                     : Objets concrets (aide à l'abstraction)
  - EPOQUE_EXPLICITE                     : Dict étymologique étendu (priorité absolue)
  - EPOQUE_SEEDS_*                       : Listes heuristiques par époque
  - ORIGINE_*                            : Listes d'origines géographiques
"""

# ---------------------------------------------------------------------------
# PÔLE SÉMANTIQUE
# ---------------------------------------------------------------------------

POLE_MAP = {
    "Art & Langage": "ARTS_ET_LANGAGE",
    "Esprit & Caractère": "ESPRIT_ET_CARACTERE",
    "Nature & Cosmos": "NATURE_ET_COSMOS",
    "Philosophie & Idées": "PHILOSOPHIE_ET_IDEES",
    "Sentiments & Psyché": "SENTIMENTS_ET_PSYCHE",
}

# Overrides spécifiques pour les thèmes transverses (Lumière & Ombres, Temps & Éphémère)
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
    "évanescent": "SENTIMENTS_ET_PSYCHE",
}

# ---------------------------------------------------------------------------
# DOMAINE D'ÉCRITURE
# ---------------------------------------------------------------------------

DOMAINE_WORDS = {
    "THEATRE": [
        "stichomythie", "quiproquo", "tirade", "réplique", "faconde", "harangue",
        "loquace", "prolixe", "soliloque", "invective", "sardonique", "goguenard",
        "simagrée", "potentat", "courroux", "tragédie", "comédie", "tragique",
        "burlesque", "cabotin", "dramaturge", "masque", "chiasme", "tautologie",
        "pérorer", "ergoter", "fripon", "gredin", "grivois", "grivoiserie",
        "imbroglio", "insolence", "irrévérence", "larron", "satyre", "sbire",
        "spadassin", "tintamarre", "pataquès",
    ],
    "POESIE": [
        "allitération", "assonance", "aède", "calligramme", "cantilène", "césure",
        "élégiaque", "élégie", "lyrisme", "mélopée", "rhapsode", "rhapsodie",
        "azur", "céruléen", "effluve", "empyrée", "firmament", "zéphyr", "aurore",
        "crépuscule", "diaphane", "opalin", "vespéral", "évanescent", "spleen",
        "volupté", "ondine", "sylphide", "nymphe", "nacré", "irisé", "diaphanéité",
        "frémissement", "murmure", "psalmodie", "psalmodier", "flâneur", "noctambule",
        "mignonne", "ménestrel", "métrique", "troubadour", "vate", "éolien",
    ],
    "ESSAI_PHILOSOPHIQUE": [
        "aphorisme", "apophtegme", "ratiociner", "vaticination", "gnomique",
        "contingence", "dogme", "hermétique", "heuristique", "immanence",
        "intrinsèque", "noumène", "ontologie", "paradigme", "solipsisme",
        "sophisme", "syllogisme", "syncrétisme", "sérendipité", "transcendance",
        "truisme", "maxime", "ontologique", "métaphysique", "téléologique",
        "éclectisme", "casuistique", "conjecture", "dirimant",
        "infrangible", "substantifique", "ataraxie", "cénobite", "ilote",
        "paria", "pernicieux", "prodigalité", "érémitique", "oblation",
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
        "vergogne", "égrotant",
    ],
}

# ---------------------------------------------------------------------------
# REGISTRE & TONALITÉ
# ---------------------------------------------------------------------------

REGISTRE_WORDS = {
    "ARCHAIQUE_RECHERCHE": [
        "aède", "calice", "codex", "héraut", "libation", "oblation", "ostensoir",
        "patène", "psautier", "rhapsode", "vélin", "vate", "anachorète",
        "atrabilaire", "cénobite", "estafette", "famélique", "ilote", "larron",
        "malandrin", "moujik", "preux", "sicaire", "spadassin", "sycophante",
        "viateur", "chthonien", "haruspice", "pythonisse", "thaumaturge",
        "aboulie", "noévie", "caduc", "trépas", "vergogne", "condottiere",
        "marmiton", "palefrenier", "sbire", "suzerain", "scholastique",
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
        "ménestrel", "troubadour", "viateur",
    ],
    "TRAGIQUE_DRAMATIQUE": [
        "diatribe", "estocade", "invective", "acrimonie", "fielleux", "ignominie",
        "ostracisme", "parjure", "sardonique", "scélérat", "turpitude",
        "vilipender", "vitriolique", "exsangue", "fuligineux", "ténébreux",
        "anathème", "némésis", "abattement", "accablement", "affliction",
        "anémie", "courroux", "morose", "neurasthénie", "stupeur", "trépas",
        "hécatombe", "inexorable", "déliquescence", "périssable",
    ],
    "COMIC_BURLESQUE": [
        "amphigourique", "faconde", "garrulité", "pataquès", "quiproquo",
        "soliloque", "verbeux", "volubile", "facétieux", "goguenard", "grivois",
        "grivoiserie", "imbroglio", "narquois", "simagrée",
    ],
}

# ---------------------------------------------------------------------------
# PROFIL ÉMOTIONNEL
# ---------------------------------------------------------------------------

EMOTION_WORDS = {
    "POSITIF_EXCITANT": [
        "alacrité", "allégresse", "effervescence", "exaltation",
        "extase", "ferveur", "jubilation", "pétulant",
    ],
    "NEGATIF_EXCITANT": [
        "acrimonie", "courroux", "diatribe", "invective",
        "collusion", "ignominie", "tumulte",
    ],
    "POSITIF_CALME": [
        "bonhomie", "clémence", "félicité", "quiétude", "sérénité",
        "mansuétude", "volupté", "limpide", "lustral", "placidité",
    ],
    "NEGATIF_CALME": [
        "abattement", "accablement", "affliction", "apathie",
        "désabusé", "indolence", "langueur", "lascif", "léthargie", "morose",
        "mélancolie", "neurasthénie", "spleen", "torpeur", "évanescent",
        "fugace", "déliquescence", "exsangue", "caducité",
    ],
}

# ---------------------------------------------------------------------------
# OBJETS CONCRETS (aide au calcul d'abstraction en fallback)
# ---------------------------------------------------------------------------

CONCRETE_OBJECTS = [
    "calice", "codex", "grimoire", "patène", "psautier", "vélin", "alcyon",
    "chrysanthème", "constellation", "corolle", "dryade", "labyrinthe",
    "nymphe", "ondine", "parvis", "pistil", "pétale", "sépale", "solstice",
    "sylphide", "zodiaque", "étamine", "marmiton", "brigand", "condottiere",
    "estafette", "flibustier", "grognard", "larron", "malandrin", "maraudeur",
    "moujik", "paladin", "palefrenier", "satyre", "sbire", "sicaire",
    "spadassin", "ostensoir",
]

# ---------------------------------------------------------------------------
# ÉPOQUE — Dictionnaire explicite (COUCHE 1, priorité absolue)
#
# Format : mot_lowercase -> époque
# N'inclut que les mots où les couches heuristiques 2-4 échoueraient.
# Sources : première attestation française significative + époque d'usage
#           littéraire prédominant (TLFi, CNRTL, Académie française).
# ---------------------------------------------------------------------------

EPOQUE_EXPLICITE = {
    # ── ANTIQUITE ──────────────────────────────────────────────────────────
    # Mythologie, liturgie antique, vocabulaire grec/romain pré-chrétien
    "aède":           "ANTIQUITE",   # grec ἀοιδός, aède homérique
    "apophtegme":     "ANTIQUITE",   # grec ἀπόφθεγμα, maxime antique
    "ataraxie":       "ANTIQUITE",   # concept épicurien / stoïcien grec
    "codex":          "ANTIQUITE",   # latin codex, manuscrit antique
    "dryade":         "ANTIQUITE",   # nymphe des bois, mythologie grecque
    "haruspice":      "ANTIQUITE",   # devin étrusque/romain
    "ilote":          "ANTIQUITE",   # esclave spartiate (Sparte)
    "libation":       "ANTIQUITE",   # rite religieux grec/romain
    "nymphe":         "ANTIQUITE",   # divinité naturelle grecque
    "oblation":       "ANTIQUITE",   # offrande liturgique antique
    "ondine":         "ANTIQUITE",   # esprit des eaux, antiquité nordique/latine
    "ostensoir":      "ANTIQUITE",   # vase liturgique antique
    "patène":         "ANTIQUITE",   # disque liturgique antique
    "pythonisse":     "ANTIQUITE",   # prophétesse de Delphes
    "rhapsode":       "ANTIQUITE",   # récitant grec (Homère)
    "sylphide":       "ANTIQUITE",   # sylphe, esprit de l'air (mythologie)
    "thaumaturge":    "ANTIQUITE",   # faiseur de miracles, Antiquité grecque
    "vate":           "ANTIQUITE",   # poète-prophète latin (vates)
    "scholastique":   "ANTIQUITE",   # courant philosophique médiéval/antique

    # ── CLASSIQUE_17_18 ─────────────────────────────────────────────────────
    # Première attestation ou usage littéraire dominant aux XVIIe-XVIIIe s.
    "alacrité":       "CLASSIQUE_17_18",  # lat. alacritas, attesté XVIe-XVIIe
    "allitération":   "CLASSIQUE_17_18",  # codifié par Boileau, Art poétique 1674
    "anachorète":     "CLASSIQUE_17_18",  # ascète chrétien, usage XVIIe
    "apogée":         "CLASSIQUE_17_18",  # terme astronomique XVIIe (Académie 1694)
    "assonance":      "CLASSIQUE_17_18",  # arts poétiques XVIIe (Boileau)
    "atrabilaire":    "CLASSIQUE_17_18",  # humeur noire, médecine XVIIe
    "aurore":         "CLASSIQUE_17_18",  # usage poétique dominant Racine, Bossuet
    "bocage":         "CLASSIQUE_17_18",  # paysage pastoral, XVIIe-XVIIIe
    "bonhomie":       "CLASSIQUE_17_18",  # attesté XVIIe (Molière)
    "brigand":        "CLASSIQUE_17_18",  # roman picaresque XVIIe-XVIIIe (Lesage)
    "bucolique":      "CLASSIQUE_17_18",  # genre pastoral latin, usage dominant XVIIe
    "cénobite":       "CLASSIQUE_17_18",  # moine en communauté, textes XVIIe
    "chagrin":        "CLASSIQUE_17_18",  # Racine, Molière, omniprésent XVIIe
    "claustration":   "CLASSIQUE_17_18",  # lat. claustrum, usage XVIIe-XVIIIe
    "condottiere":    "CLASSIQUE_17_18",  # emprunt italien XVIe-XVIIe
    "courroux":       "CLASSIQUE_17_18",  # vieux français, dominant XVIIe (Racine)
    "désabusé":       "CLASSIQUE_17_18",  # attesté XVIIe (La Rochefoucauld)
    "diaphane":       "CLASSIQUE_17_18",  # optique et poésie XVIe-XVIIe
    "éphémère":       "CLASSIQUE_17_18",  # grec ἐφήμερος, usage XVIe-XVIIe
    "estafe":         "CLASSIQUE_17_18",
    "estafette":      "CLASSIQUE_17_18",  # courrier militaire, XVIIe
    "exaltation":     "CLASSIQUE_17_18",  # Bossuet, Pascal, dominant XVIIe
    "faconde":        "CLASSIQUE_17_18",  # Molière, XVIIe
    "famélique":      "CLASSIQUE_17_18",  # lat. famelicus, textes XVIIe
    "firmament":      "CLASSIQUE_17_18",  # lat. firmamentum, Racine, Bossuet
    "flibustier":     "CLASSIQUE_17_18",  # pirates antillais XVIIe (boucaniers)
    "flâneur":        "ROMANTIQUE_19",    # figure culturelle baudelairienne XIXe
    "harangue":       "CLASSIQUE_17_18",  # rhétorique XVIIe, Bossuet
    "heur":           "CLASSIQUE_17_18",  # "n'avoir pas l'heur", expression XVIIe
    "idylle":         "CLASSIQUE_17_18",  # genre pastoral XVIIe-XVIIIe
    "impérieux":      "CLASSIQUE_17_18",  # lat. imperiosus, La Bruyère
    "jubilation":     "CLASSIQUE_17_18",  # textes bibliques et XVIIe
    "larron":         "CLASSIQUE_17_18",  # La Fontaine, Molière, XVIIe
    "liturgie":       "CLASSIQUE_17_18",  # usage dominant XVIIe théologie
    "magnanime":      "CLASSIQUE_17_18",  # lat. magnanimus, La Rochefoucauld
    "malandrin":      "CLASSIQUE_17_18",  # brigand, XVIIe
    "mansuétude":     "CLASSIQUE_17_18",  # lat. mansuetudo, textes XVIIe
    "marmiton":       "CLASSIQUE_17_18",  # Molière, cuisine XVIIe
    "maxime":         "CLASSIQUE_17_18",  # La Rochefoucauld, Maximes 1665
    "miséricorde":    "CLASSIQUE_17_18",  # textes religieux XVIIe
    "moujik":         "CLASSIQUE_17_18",  # paysan russe, textes XVIIe-XVIIIe
    "narquois":       "CLASSIQUE_17_18",  # argot XVIIe (Molière)
    "oblation":       "CLASSIQUE_17_18",  # offrande liturgique, usage XVIIe
    "ondée":          "CLASSIQUE_17_18",  # averse, textes XVIIe-XVIIIe
    "palefrenier":    "CLASSIQUE_17_18",  # valet d'écurie, textes XVIIe
    "palimpseste":    "CLASSIQUE_17_18",  # manuscrit, usage savant XVIIe
    "parvis":         "CLASSIQUE_17_18",  # esplanade d'église, usage XVIIe
    "perplexité":     "CLASSIQUE_17_18",  # lat. perplexitas, Descartes, Pascal
    "placidité":      "CLASSIQUE_17_18",  # lat. placiditas, textes XVIIe
    "potentat":       "CLASSIQUE_17_18",  # souverain, textes XVIIe
    "preux":          "CLASSIQUE_17_18",  # vieux français, chevalerie, XVIIe
    "probe":          "CLASSIQUE_17_18",  # lat. probus, La Bruyère
    "psautier":       "CLASSIQUE_17_18",  # recueil de psaumes, usage liturgique XVIIe
    "réminiscence":   "CLASSIQUE_17_18",  # lat. reminiscentia, Descartes, Leibniz
    "sbire":          "CLASSIQUE_17_18",  # agent de police, usage XVIIe
    "sérénité":       "CLASSIQUE_17_18",  # lat. serenitas, usage XVIe-XVIIe
    "sicaire":        "CLASSIQUE_17_18",  # assassin, lat. sicarius, textes XVIIe
    "soliloque":      "CLASSIQUE_17_18",  # monologue, XVIIe (Corneille)
    "sourcilleux":    "CLASSIQUE_17_18",  # exigeant, XVIIe (Molière)
    "spadassin":      "CLASSIQUE_17_18",  # duelliste, XVIIe
    "stupeur":        "CLASSIQUE_17_18",  # lat. stupor, dominant XVIIe
    "suzerain":       "CLASSIQUE_17_18",  # droit féodal, usage savant XVIIe
    "sycophante":     "CLASSIQUE_17_18",  # flatteur, grec σῡκοφάντης, usage XVIIe
    "taciturne":      "CLASSIQUE_17_18",  # lat. taciturnus, La Bruyère
    "torpeur":        "CLASSIQUE_17_18",  # lat. torpor, Fénelon, XVIIe
    "trépas":         "CLASSIQUE_17_18",  # mort, vieux français, Racine
    "urbanité":       "CLASSIQUE_17_18",  # lat. urbanitas, morale XVIIe
    "valétudinaire":  "CLASSIQUE_17_18",  # lat. valetudinarius, Molière
    "vergogne":       "CLASSIQUE_17_18",  # honte, vieux français, XVIIe
    "vélin":          "CLASSIQUE_17_18",  # parchemin, XVIIe
    "villégiature":   "CLASSIQUE_17_18",  # séjour à la campagne, XVIIe-XVIIIe
    "abscons":        "CLASSIQUE_17_18",  # lat. absconsus, philosophie XVIIe
    "aboulie":        "CLASSIQUE_17_18",  # grec ἀβουλία, médecine XVIIe-XVIIIe
    "pusillanime":    "CLASSIQUE_17_18",  # lat. pusillanimis, Bossuet, La Bruyère
    "obsidional":     "CLASSIQUE_17_18",  # lat. obsidionalis, militaire XVIIe
    "chthonien":      "CLASSIQUE_17_18",  # grec χθόνιος, savant XVIIe
    "goguenard":      "CLASSIQUE_17_18",  # moqueur, XVIIe (Molière)
    "limpide":        "CLASSIQUE_17_18",  # lat. limpidus, usage XVIIe
    "lustral":        "CLASSIQUE_17_18",  # lat. lustralis, rituel XVIIe
    "équinoxe":       "CLASSIQUE_17_18",  # astronomie XVIIe (Académie 1694)
    "jadis":          "CLASSIQUE_17_18",  # adverbe vieux français, lat. jam dudum

    # ── ROMANTIQUE_19 ───────────────────────────────────────────────────────
    # Usage littéraire prédominant au XIXe siècle
    "acrimonie":      "ROMANTIQUE_19",   # prose critique XIXe
    "affliction":     "ROMANTIQUE_19",   # romantisme sentimental XIXe
    "atermoiement":   "ROMANTIQUE_19",   # prose moraliste XIXe
    "azur":           "ROMANTIQUE_19",   # symbolisme Mallarmé, Rimbaud XIXe
    "caustique":      "ROMANTIQUE_19",   # critique journalistique XIXe
    "cénacle":        "ROMANTIQUE_19",   # cercle littéraire romantique XIXe
    "céruléen":       "ROMANTIQUE_19",   # symbolisme poétique XIXe
    "chimère":        "ROMANTIQUE_19",   # romantisme, Nerval XIXe
    "collusion":      "ROMANTIQUE_19",   # droit et presse XIXe
    "crépuscule":     "ROMANTIQUE_19",   # romantisme, Victor Hugo XIXe
    "diatribe":       "ROMANTIQUE_19",   # presse critique XIXe
    "dilettante":     "ROMANTIQUE_19",   # critique artistique XIXe
    "effluve":        "ROMANTIQUE_19",   # Baudelaire, Zola XIXe
    "élégie":         "ROMANTIQUE_19",   # genre romantique dominant XIXe
    "élégiaque":      "ROMANTIQUE_19",   # romantisme XIXe
    "empyrée":        "ROMANTIQUE_19",   # poésie romantique XIXe
    "éthéré":         "ROMANTIQUE_19",   # romantisme XIXe
    "flâneur":        "ROMANTIQUE_19",   # Baudelaire, Benjamin XIXe
    "flegmatique":    "ROMANTIQUE_19",   # psychologie des caractères XIXe
    "funambule":      "ROMANTIQUE_19",   # romanesque XIXe
    "grognard":       "ROMANTIQUE_19",   # soldats napoléoniens XIXe
    "ignominie":      "ROMANTIQUE_19",   # rhétorique romantique XIXe
    "impertinence":   "ROMANTIQUE_19",   # morale sociale XIXe
    "incandescent":   "ROMANTIQUE_19",   # métaphore romantique XIXe
    "indolence":      "ROMANTIQUE_19",   # oisiveté romantique XIXe
    "intransigeance": "ROMANTIQUE_19",   # néologisme politique 1870 (Espagne)
    "irisé":          "ROMANTIQUE_19",   # symbolisme XIXe
    "langueur":       "ROMANTIQUE_19",   # romantisme, Verlaine XIXe
    "lyrisme":        "ROMANTIQUE_19",   # concept romantique dominant XIXe
    "mélancolie":     "ROMANTIQUE_19",   # romantisme XIXe
    "ménestrel":      "ROMANTIQUE_19",   # médiévalisme romantique XIXe
    "métrique":       "ROMANTIQUE_19",   # poésie romantique XIXe
    "murmure":        "ROMANTIQUE_19",   # poésie XIXe (Lamartine, Hugo)
    "nostalgie":      "ROMANTIQUE_19",   # mot médical 1688, romantisé XIXe
    "noctambule":     "ROMANTIQUE_19",   # figure romantique XIXe
    "ostracisme":     "ROMANTIQUE_19",   # politique XIXe
    "paladin":        "ROMANTIQUE_19",   # médiévalisme romantique XIXe
    "psalmodie":      "ROMANTIQUE_19",   # poésie romantique XIXe
    "spleen":         "ROMANTIQUE_19",   # Baudelaire, Fleurs du Mal 1857
    "tintamarre":     "ROMANTIQUE_19",   # roman réaliste XIXe
    "troubadour":     "ROMANTIQUE_19",   # médiévalisme romantique XIXe
    "vaticination":   "ROMANTIQUE_19",   # prophétie, romantisme XIXe
    "vitriolique":    "ROMANTIQUE_19",   # critique XIXe
    "volubilité":     "ROMANTIQUE_19",   # roman XIXe

    # ── MODERNE_20 ──────────────────────────────────────────────────────────
    # Néologismes ou usage dominant au XXe siècle
    "abnégation":     "MODERNE_20",      # psychologie XXe
    "esthète":        "MODERNE_20",      # courant esthétiste fin XIXe-XXe
    "fuligineux":     "MODERNE_20",      # usage littéraire XXe (Gracq)
    "heuristique":    "MODERNE_20",      # Bolzano 1837, vulgarisé sciences XXe
    "mentor":         "MODERNE_20",      # usage courant généralisé XXe
    "neurasthénie":   "MODERNE_20",      # terme médical Beard 1869, dominant XXe
    "obsolescence":   "MODERNE_20",      # économie industrielle XXe
    "obsolète":       "MODERNE_20",      # usage littéraire XXe
    "onirique":       "MODERNE_20",      # surréalisme XXe
    "opiniâtre":      "MODERNE_20",      # usage courant XXe
    "paradigme":      "MODERNE_20",      # Kuhn 1962, sciences XXe
    "procrastination":"MODERNE_20",      # psychologie comportementale XXe
    "résilience":     "MODERNE_20",      # Cyrulnik années 1990
    "synesthésie":    "MODERNE_20",      # psychologie sensorielle XXe
    "vespéral":       "MODERNE_20",      # usage symboliste fin XIXe-XXe

    # ── CONTEMPORAIN_21 ─────────────────────────────────────────────────────
    # Néologismes ou usage culturel dominant au XXIe siècle
    "coruscant":      "CONTEMPORAIN_21", # usage très rare, relancé XXIe (Star Wars)
    "fallacieux":     "CONTEMPORAIN_21", # usage politique/médiatique dominant XXIe
    "impéritie":      "CONTEMPORAIN_21", # terme juridique, usage politique actuel
    "obsidional":     "CLASSIQUE_17_18", # (override ci-dessus)
    "parangon":       "CONTEMPORAIN_21", # usage rhétorique relancé XXIe
    "pataquès":       "ROMANTIQUE_19",   # attesté 1813, XVIIIe-XIXe
    "pétrichor":      "CONTEMPORAIN_21", # mot forgé en 1964, usage répandu XXIe
    "pusillanime":    "CLASSIQUE_17_18", # (override ci-dessus)
    "sérendipité":    "CONTEMPORAIN_21", # emprunt à l'anglais, usage français XXIe
    "valétudinaire":  "CLASSIQUE_17_18", # (override ci-dessus)
    "fallacieux":     "CONTEMPORAIN_21", # usage politique/médiatique dominant XXIe
    "parapathique":   "CONTEMPORAIN_21", # néologisme psychiatrique récent
    "fuligineux":     "MODERNE_20",      # usage littéraire XXe (Gracq)
}

# ---------------------------------------------------------------------------
# ÉPOQUE — Heuristiques par suffixes (COUCHE 2)
# Suffixes caractéristiques d'une époque d'apparition
# ---------------------------------------------------------------------------

# Suffixes → forte probabilité CLASSIQUE_17_18
SUFFIXES_CLASSIQUE = [
    "tude",       # fortitude, mansuétude, lassitude (lat. -tudo)
    "ité",        # urbanité, pusillanime... (lat. -itas) mais aussi romantique
]

# Suffixes → forte probabilité ROMANTIQUE_19
SUFFIXES_ROMANTIQUE = [
    "esque",      # romantesque, picaresque (XIXe littéraire)
    "ien",        # baudelairien, mallarméen
]

# Suffixes → forte probabilité MODERNE_20
SUFFIXES_MODERNE = [
    "isation",    # somatisation, procrastination
    "isation",
    "isation",
    "thérapie",
    "ique",       # heuristique, paradigmatique (si non classique)
]

# Suffixes → forte probabilité CONTEMPORAIN_21
SUFFIXES_CONTEMPORAIN = [
    "ing",        # buzzwords anglophones
    "genre",      # cisgenre, non-binaire
    "sexual",     # demisexuel
    "binaire",
    "phobie",     # néologismes identitaires
    "inclusif",
]

# ---------------------------------------------------------------------------
# ÉPOQUE — Racines étymologiques (COUCHE 3)
# Mots dont la racine latine/grecque les ancre dans une époque
# ---------------------------------------------------------------------------

# Racines grecques de la philosophie antique → ANTIQUITE
RACINES_ANTIQUITE = [
    "apat",   # apathie → mais usage romantique/moderne
    "atarax", # ataraxie
    "logos",  # logorrhée
    "psych",  # psyché → mais sens moderne
    "pathos",
    "aisth",  # esthétique
    "polis",  # politique
    "mytho",  # mythologie
]

# Mots-clés de la littérature classique française (Académie, Versailles)
SEEDS_CLASSIQUE_17_18 = [
    "bienséance", "galanterie", "vertu", "mémoires", "fable",
    "proverbe", "sentence", "comédie", "pastorale",
]

# Mots-clés du romantisme / réalisme XIXe
SEEDS_ROMANTIQUE_19 = [
    "spleen", "mélancolie", "médiévalisme", "orientalisme",
    "fantastique", "sentiment", "passion", "nature",
]

# Mots-clés du XXe (psychanalyse, sociologie, surréalisme, existentialisme)
SEEDS_MODERNE_20 = [
    "névrose", "psychose", "complexe", "refoulement",
    "existentiel", "aliénation", "absurde",
]

# ---------------------------------------------------------------------------
# ORIGINE GÉOGRAPHIQUE
# ---------------------------------------------------------------------------

SLAVIC_WORDS = ["moujik", "tsar", "steppes"]
ITALIAN_WORDS = [
    "dilettante", "condottiere", "estocade", "imbroglio", "quiproquo",
    "fiasco", "incognito", "pantalon", "bouffon", "carrousel", "balcon",
]
ENGLISH_WORDS = [
    "spleen", "masterclass", "queer", "non-binaire", "cisgenre",
    "intersectionnel", "sérendipité", "chiller", "chilling",
]
GERMAN_WORDS = ["leitmotiv", "diktat", "kafkaïen", "nickel"]
