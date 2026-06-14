import os
import sys
import sqlite3
import csv

# S'assurer du bon encodage de la console
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

DB_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "lexica_corpus.db")
OUTPUT_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "docs", "amelioration_de_la_fonction_de_recherche",
    "algorithme_de_presentation_des_extraits"
)

def init_words_table():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    # Table des mots candidats de la Word Reserve (Pipeline A)
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS words (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        word TEXT NOT NULL,
        category TEXT NOT NULL, -- C1, C2, C3, C4, C5
        difficulty TEXT NOT NULL, -- Débutant, Intermédiaire, Avancé, Expert
        score REAL,
        definition TEXT,
        unique_key TEXT UNIQUE -- Évite les doublons
    )
    """)
    conn.commit()
    conn.close()

def import_csv_to_db(csv_filename, category, word_col, diff_col, score_col, def_col, delimiter=';'):
    csv_path = os.path.join(OUTPUT_DIR, csv_filename)
    if not os.path.exists(csv_path):
        # Essayer aussi dans tools/
        csv_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), csv_filename)
        if not os.path.exists(csv_path):
            print(f"Fichier {csv_filename} absent, ignoré.")
            return 0
            
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    
    imported = 0
    with open(csv_path, mode='r', encoding='utf-8-sig') as f:
        reader = csv.DictReader(f, delimiter=delimiter)
        for row in reader:
            try:
                word = row[word_col].strip()
                difficulty = row[diff_col].strip() if diff_col in row else "Intermédiaire"
                score = float(row[score_col].strip()) if score_col in row else 0.0
                definition = row[def_col].strip() if def_col in row else ""
                
                unique_key = f"{word.lower()}_{category.lower()}"
                
                cursor.execute("""
                INSERT OR REPLACE INTO words (word, category, difficulty, score, definition, unique_key)
                VALUES (?, ?, ?, ?, ?, ?)
                """, (word, category, difficulty, score, definition, unique_key))
                imported += 1
            except Exception as e:
                # Gérer d'éventuelles colonnes manquantes
                continue
                
    conn.commit()
    conn.close()
    return imported

def main():
    print("==========================================================")
    print("  CENTRALISATION DE LA WORD RESERVE (PIPELINE A) EN SQLITE")
    print("==========================================================")
    
    init_words_table()
    
    # 1. C1 - Littéraire (consolidated_literary_words.csv)
    # Note : Le CSV C1 a pour colonnes 'mot;theme'
    c1_count = import_csv_to_db("consolidated_literary_words.csv", "C1", "mot", "Difficulté", "Score_final", "Définition")
    print(f"C1 Littéraire : {c1_count} mots importés.")
    
    # 2. C2 - Rhétorique (candidates_rhetoric_politics.csv)
    c2_count = import_csv_to_db("candidates_rhetoric_politics.csv", "C2", "Mot", "Difficulté", "Score_C2", "Définition")
    print(f"C2 Rhétorique : {c2_count} mots importés.")
    
    # 3. C3 - Argot (candidates_slang_trending.csv)
    c3_count = import_csv_to_db("candidates_slang_trending.csv", "C3", "Mot", "Difficulté", "Score_C3", "Définition")
    print(f"C3 Argot : {c3_count} mots importés.")
    
    # 4. C4 - Culture G (candidates_culture_g.csv)
    c4_count = import_csv_to_db("candidates_culture_g.csv", "C4", "Mot", "Difficulté", "Score_C4", "Définition")
    print(f"C4 Culture G : {c4_count} mots importés.")
    
    # 5. C5 - Jargon (candidates_jargon.csv)
    # Note: Dans C5 le séparateur peut différer ou être standard.
    c5_count = import_csv_to_db("candidates_jargon.csv", "C5", "Mot", "Difficulté", "Score_C5", "Définition")
    print(f"C5 Jargon : {c5_count} mots importés.")
    
    print("\n✓ Tous les mots candidats R&D de la Pipeline A sont centralisés en base.")
    print(f"Fichier : {DB_PATH}")

if __name__ == "__main__":
    main()
