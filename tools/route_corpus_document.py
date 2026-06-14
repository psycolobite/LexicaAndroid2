import os
import sys
import re
import sqlite3
from classify_book_difficulty import load_lexique, analyze_text_difficulty

# S'assurer du bon encodage de la console
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

DB_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "lexica_corpus.db")

# Liste de connecteurs logiques pour identifier C2 (Rhétorique)
CONNECTORS = {
    "nonobstant", "subséquemment", "corrélativement", "partant", "a fortiori", 
    "prémisse", "induction", "déchotomie", "antinomie", "sophisme", "paralogisme",
    "tautologie", "pléonasme", "litote", "réfuter", "concéder", "corroborer", 
    "inférer", "postuler", "objection", "réquisitoire", "plaidoyer", "néanmoins",
    "pourtant", "cependant", "toutefois", "ainsi", "par conséquent", "en effet"
}

def init_db():
    """Initialise la base de données SQLite unifiée."""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    # Table des documents
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS documents (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        author TEXT,
        category TEXT NOT NULL, -- C1, C2, C3, C4, C5
        difficulty TEXT NOT NULL, -- Débutant, Intermédiaire, Avancé, Expert
        source TEXT, -- Gutenberg, YouTube, Wikipédia, etc.
        unique_key TEXT UNIQUE -- Évite les doublons d'ingestion
    )
    """)
    
    # Table des extraits / passages
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS excerpts (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        document_id INTEGER,
        text TEXT NOT NULL,
        score REAL DEFAULT 0.0,
        timestamp TEXT, -- Pour les vidéos (optionnel)
        FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
    )
    """)
    
    conn.commit()
    return conn

def route_document(title, author, text, source, lexique, category_override=None):
    """
    Analyse un document et l'assigne à la meilleure catégorie C1-C5,
    évalue sa difficulté et l'enregistre en base avec ses extraits pertinents.
    """
    conn = init_db()
    cursor = conn.cursor()
    
    # 1. Classification de la difficulté via notre classifieur automatique
    difficulty_info = analyze_text_difficulty(text, lexique)
    difficulty = difficulty_info["difficulty"]
    
    # 2. Routage vers C1-C5
    category = "C1" # Par défaut : Littéraire
    if category_override:
        category = category_override
    else:
        text_lower = text.lower()
        
        # C3 (Argot/Oral) : Présence forte de mots d'argot/verlan
        slang_keywords = {"seum", "chelou", "relou", "boloss", "kiffer", "wesh", "daron", "daronne", "askip", "moula", "binks", "igo", "tieks"}
        slang_hits = sum(1 for w in slang_keywords if w in text_lower)
        
        # C2 (Rhétorique) : Présence de connecteurs logiques
        connector_hits = sum(1 for c in CONNECTORS if c in text_lower)
        
        # C5 (Jargon) : Si contient des indices de cuisine, médecine, droit ou informatique
        jargon_cuisine = {"brunoise", "blanchir", "déglacer", "julienne", "roux", "singer", "sabler", "émulsionner"}
        jargon_droit = {"usufruit", "emphytéose", "jurisprudence", "litige", "préjudice", "subroger"}
        jargon_hits = sum(1 for j in (jargon_cuisine | jargon_droit) if j in text_lower)
        
        # C4 (Culture G / Encyclopédique) : si c'est Wikipédia et n'a pas d'argot/connecteurs
        is_wikipedia = "wikipedia" in source.lower()
        
        if slang_hits >= 2:
            category = "C3"
        elif connector_hits >= 3:
            category = "C2"
        elif jargon_hits >= 2:
            category = "C5"
        elif is_wikipedia:
            category = "C4"
            
    # 3. Insertion du document en base
    unique_key = f"{title.lower()}_{author.lower() if author else 'unknown'}_{category}"
    try:
        cursor.execute("""
        INSERT INTO documents (title, author, category, difficulty, source, unique_key)
        VALUES (?, ?, ?, ?, ?, ?)
        """, (title, author, category, difficulty, source, unique_key))
        doc_id = cursor.lastrowid
    except sqlite3.IntegrityError:
        # Document déjà existant, récupérer son ID
        cursor.execute("SELECT id FROM documents WHERE unique_key = ?", (unique_key,))
        doc_id = cursor.fetchone()[0]
        
    # 4. Découpage en paragraphes/extraits et enregistrement
    # On découpe grossièrement sur les doubles retours à la ligne
    paragraphs = re.split(r'\n\s*\n', text)
    inserted_excerpts = 0
    
    for para in paragraphs:
        para = para.strip()
        if len(para) < 50: # Exclure les paragraphes trop courts
            continue
            
        # Score de pertinence simple
        score = 0.5
        if category == "C2":
            # Score basé sur la densité de connecteurs logiques
            hits = sum(1 for c in CONNECTORS if c in para.lower())
            score += hits * 0.1
        elif category == "C3":
            # Score basé sur la présence de termes argotiques
            hits = sum(1 for w in slang_keywords if w in para.lower())
            score += hits * 0.15
            
        cursor.execute("""
        INSERT INTO excerpts (document_id, text, score)
        VALUES (?, ?, ?)
        """, (doc_id, para, score))
        inserted_excerpts += 1
        
    conn.commit()
    conn.close()
    
    return category, difficulty, inserted_excerpts

if __name__ == "__main__":
    print("==========================================================")
    print("  ROUTEUR AUTOMATIQUE DE CORPUS & ENREGISTREMENT SQLITE")
    print("==========================================================")
    
    lex = load_lexique()
    
    # 1. Document Test Littéraire (C1)
    doc_c1 = (
        "Le ciel était d’un céruléen parfait, sans un nuage pour troubler cette clarté "
        "printanière qui baignait la glèbe fumante. Dans le silence de la vallée, "
        "la neurasthénie de l'écrivain semblait s'effacer face à cette splendeur poétique."
    )
    
    # 2. Document Test Rhétorique (C2)
    doc_c2 = (
        "L'obéissance au seul appétit est esclavage, et la liberté est l'obéissance à la loi "
        "qu'on s'est prescrite. Or, sans cette règle, l'état social ne serait qu'une tyrannie. "
        "Par conséquent, il est indubitable que le contrat social corroboré par la raison est légitime, "
        "nonobstant les objections des sceptiques."
    )
    
    # 3. Document Test Argot (C3)
    doc_c3 = (
        "Franchement, j'ai trop le seum pour ce qui s'est passé hier. Mon daron m'a capté "
        "alors que j'allais pécho une moula en bas du binks. C'est trop relou, il m'a "
        "grave fait bader pour rien."
    )
    
    # Traitement
    c_c1, d_c1, count_c1 = route_document("Poésie d'Automne", "Maupassant", doc_c1, "Gutenberg", lex)
    print(f"Document 1 : Classé dans {c_c1} (Niveau: {d_c1}) - {count_c1} extraits enregistrés.")
    
    c_c2, d_c2, count_c2 = route_document("Du Contrat Social (Extrait)", "Rousseau", doc_c2, "Gutenberg", lex)
    print(f"Document 2 : Classé dans {c_c2} (Niveau: {d_c2}) - {count_c2} extraits enregistrés.")
    
    c_c3, d_c3, count_c3 = route_document("Discussion de Rue", "Genius Scraper", doc_c3, "YouTube", lex)
    print(f"Document 3 : Classé dans {c_c3} (Niveau: {d_c3}) - {count_c3} extraits enregistrés.")
    
    print(f"\n✓ Base de données SQLite unique mise à jour avec succès : {DB_PATH}")
