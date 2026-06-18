#!/usr/bin/env python3
import os
import json
import urllib.request
import urllib.parse
import re
import time
import copy
import numpy as np
from bs4 import BeautifulSoup

# Setup directories
TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))

# 1. Target Words & Definitions for prototype testing
TEST_DATA = {
    "C1 (Littéraire)": [
        {
            "word": "céruléen",
            "definition": "D'un bleu pur, semblable à celui du ciel."
        },
        {
            "word": "glèbe",
            "definition": "Motte de terre, champ cultivé, ou condition de servage attaché à la terre."
        }
    ],
    "C2 (Rhétorique)": [
        {
            "word": "nonobstant",
            "definition": "Malgré, sans avoir égard à."
        },
        {
            "word": "partant",
            "definition": "Par conséquent, de ce fait, donc."
        }
    ],
    "C3 (Argot)": [
        {
            "word": "moula",
            "definition": "Argent, cannabis, ou personne ayant du charisme et du flow."
        },
        {
            "word": "seum",
            "definition": "Rancœur, colère, déception intense ou frustration."
        }
    ],
    "C4 (Culture G)": [
        {
            "word": "lucifuge",
            "definition": "Qui fuit la lumière, qui vit dans l'obscurité."
        },
        {
            "word": "callipyge",
            "definition": "Qui possède de belles fesses harmonieuses."
        }
    ],
    "C5 (Jargon)": [
        {
            "word": "emphytéose",
            "definition": "Bail immobilier de très longue durée (de 18 à 99 ans) conférant un droit réel au locataire."
        },
        {
            "word": "blanchir",
            "definition": "Cuisine : Plonger un aliment quelques minutes dans de l'eau bouillante puis glacée pour le cuire légèrement ou en enlever l'âpreté."
        }
    ]
}

# Category-specific similarity thresholds
THRESHOLDS = {
    "C1 (Littéraire)": 0.38,
    "C2 (Rhétorique)": 0.30, # Lowered to avoid rejecting valid, complex usages like 'partant'
    "C3 (Argot)": 0.22,
    "C4 (Culture G)": 0.38,
    "C5 (Jargon)": 0.35
}

# Local Mock Library representing special documents/books (Control+F fallback)
LOCAL_MOCK_LIBRARY = [
    {
        "title": "Code Civil Français - Chapitre des Baux",
        "category": "C5 (Jargon)",
        "content": "L'emphytéose, ou bail emphytéotique, est un contrat de lease immobilier de très longue durée. Ce bail confère au preneur un droit réel susceptible d'hypothèque, ce droit pouvant être saisi et vendu dans les formes prescrites pour la saisie immobilière. Le preneur s'oblige en retour à payer une redevance annuelle et à améliorer le fonds."
    },
    {
        "title": "Le Grand Guide de la Cuisine Traditionnelle",
        "category": "C5 (Jargon)",
        "content": "Pour blanchir les légumes vertes, il convient de les plonger dans une grande casserole d'eau bouillante salée pendant deux minutes. Dès que le temps est écoulé, retirez-les à l'aide d'une écumoire et plongez-les immédiatement dans un saladier d'eau glacée pour stopper la cuisson."
    }
]

def cosine_similarity(a, b):
    norm_a = np.linalg.norm(a)
    norm_b = np.linalg.norm(b)
    if norm_a == 0 or norm_b == 0:
        return 0.0
    return float(np.dot(a, b) / (norm_a * norm_b))

def fetch_html(url):
    headers = {'User-Agent': 'LexicaAndroid2/1.0 (R&D Example Extractor; mailto:contact@lexica.org)'}
    retries = 3
    delay = 1.0
    for attempt in range(retries):
        time.sleep(delay)
        try:
            req = urllib.request.Request(url, headers=headers)
            with urllib.request.urlopen(req, timeout=10) as response:
                return response.read().decode('utf-8', errors='ignore')
        except urllib.error.HTTPError as e:
            if e.code == 429:
                print(f"  [!] HTTP 429. Retrying after {delay * 2}s...")
                delay *= 2.5
                continue
            print(f"  [!] HTTP Error for {url}: {e}")
            return None
        except Exception as e:
            print(f"  [!] Error fetching {url}: {e}")
            return None
    return None

def get_window(text, word, window_size=8):
    """Extracts a window of words around the target word to avoid semantic dilution."""
    # Normalize punctuation and split by whitespace
    words = re.findall(r'\w+|[^\w\s]', text, re.UNICODE)
    word_lower = word.lower()
    
    # Try to find the exact word or its stem
    target_idx = -1
    for i, w in enumerate(words):
        w_clean = w.lower()
        if w_clean == word_lower or (len(word_lower) > 4 and w_clean.startswith(word_lower[:-2])):
            target_idx = i
            break
            
    if target_idx == -1:
        return text # Fallback to full text
        
    start = max(0, target_idx - window_size)
    end = min(len(words), target_idx + window_size + 1)
    return " ".join(words[start:end])

def get_wiktionary_examples(word, target_def_text):
    """Fetches definition and associated examples from Wiktionary HTML."""
    url = f"https://fr.wiktionary.org/w/api.php?action=parse&page={urllib.parse.quote(word)}&prop=text&redirects=true&format=json&formatversion=2"
    raw_json = fetch_html(url)
    if not raw_json:
        return []
    
    try:
        data = json.loads(raw_json)
        html = data.get('parse', {}).get('text', '')
        if not html:
            return []
        
        soup = BeautifulSoup(html, 'html.parser')
        
        # Locate French section
        french_section = []
        french_h2 = soup.find(lambda tag: tag.name == 'h2' and 'ran' in tag.text)
        if french_h2:
            container = french_h2.find_parent('div')
            if container and any(cls in container.get('class', []) for cls in ['mw-heading', 'mw-heading2']):
                curr = container.find_next_sibling()
            else:
                curr = french_h2.find_next_sibling()
            
            while curr:
                if curr.name == 'h2':
                    break
                if curr.name == 'div' and ('mw-heading2' in curr.get('class', []) or curr.find('h2')):
                    break
                french_section.append(curr)
                curr = curr.find_next_sibling()
        else:
            french_section = soup.contents
            
        examples = []
        for element in french_section:
            if element.name == 'ol':
                for li in element.find_all('li', recursive=False):
                    # Check definition text
                    clean_li = copy.copy(li)
                    # Remove sublists of examples
                    for sub in clean_li.select('ul, ol, dl, table, style, script'):
                        sub.extract()
                    def_text = clean_li.text.strip()
                    
                    # Extract examples under this specific li definition
                    for item in li.select('ul > li, dl > dd, dd > i'):
                        ex_text = item.text.strip()
                        # Clean wiki citations or templates
                        ex_text = re.sub(r'\(Translation.*?\)', '', ex_text)
                        ex_text = re.sub(r'\[.*?\]', '', ex_text)
                        ex_text = re.sub(r'\s+', ' ', ex_text).strip()
                        if ex_text and len(ex_text) > 8:
                            examples.append({
                                "text": ex_text,
                                "source": "Wiktionary",
                                "for_def": def_text
                            })
        return examples
    except Exception as e:
        print(f"  [!] Parsing error for {word}: {e}")
        return []

def search_wikipedia_sentences(word):
    """Ctrl+F Fallback querying Wikipedia API for pages and extracting sentences containing the word."""
    search_url = f"https://fr.wikipedia.org/w/api.php?action=query&list=search&srsearch={urllib.parse.quote(word)}&format=json&formatversion=2"
    raw_json = fetch_html(search_url)
    if not raw_json:
        return []
        
    candidate_sentences = []
    try:
        data = json.loads(raw_json)
        search_results = data.get("query", {}).get("search", [])
        
        for res in search_results[:3]: # Scan top 3 pages
            page_title = res.get("title")
            page_url = f"https://fr.wikipedia.org/w/api.php?action=parse&page={urllib.parse.quote(page_title)}&prop=text&format=json&formatversion=2"
            page_json = fetch_html(page_url)
            if not page_json:
                continue
                
            page_data = json.loads(page_json)
            html = page_data.get("parse", {}).get("text", "")
            if not html:
                continue
                
            soup = BeautifulSoup(html, 'html.parser')
            # Get text from paragraphs
            text_blocks = [p.text for p in soup.find_all('p')]
            full_text = "\n".join(text_blocks)
            full_text = re.sub(r'\[\d+\]', '', full_text)
            
            # Split into sentences
            sentences = re.split(r'(?<=[.!?])\s+', full_text)
            for s in sentences:
                s = s.strip()
                pattern = re.compile(rf'\b{re.escape(word)}\w*\b', re.IGNORECASE)
                if pattern.search(s) and 15 < len(s) < 250:
                    candidate_sentences.append({
                        "text": s,
                        "source": f"Wikipedia ({page_title})"
                    })
    except Exception as e:
        print(f"  [!] Wikipedia search error for {word}: {e}")
        
    return candidate_sentences

def search_local_library(word):
    """Local 'Control+F' database search as a fallback."""
    candidate_sentences = []
    word_pattern = re.compile(rf'\b{re.escape(word)}\w*\b', re.IGNORECASE)
    
    for book in LOCAL_MOCK_LIBRARY:
        sentences = re.split(r'(?<=[.!?])\s+', book["content"])
        for s in sentences:
            s = s.strip()
            if word_pattern.search(s) and 15 < len(s) < 250:
                candidate_sentences.append({
                    "text": s,
                    "source": f"Bibliothèque Locale ({book['title']})"
                })
    return candidate_sentences

def main():
    print("Loading Sentence-Transformers model...")
    from sentence_transformers import SentenceTransformer
    model = SentenceTransformer("paraphrase-multilingual-MiniLM-L12-v2")
    
    report_lines = []
    report_lines.append("# Rapport R&D - Validation et Sourcing d'Extraits Pertinents (Pipeline A)")
    report_lines.append("Ce rapport démontre l'algorithme optimisé de validation sémantique par embeddings. ")
    report_lines.append("Il utilise une analyse hybride (sens de la sous-définition Wiktionnaire d'origine + fenêtre glissante locale de la phrase) et s'arrête dès le premier match valide (Performance-First).\n")
    report_lines.append("---")
    
    for category, words_data in TEST_DATA.items():
        print(f"\nProcessing category: {category}")
        report_lines.append(f"\n## Objectif : {category}")
        threshold = THRESHOLDS[category]
        report_lines.append(f"*(Seuil de tolérance sémantique pour cette catégorie : **{threshold:.2f}**)*\n")
        
        for item in words_data:
            word = item["word"]
            target_def = item["definition"]
            print(f"  Word: {word}...")
            
            report_lines.append(f"\n### Mot : **{word}**")
            report_lines.append(f"- *Définition ciblée* : « {target_def} »")
            
            # Embed the target definition
            target_emb = model.encode(target_def, normalize_embeddings=True)
            
            # Step 1: Try Wiktionary
            print(f"    Fetching Wiktionary examples...")
            all_candidates = get_wiktionary_examples(word, target_def)
            
            # Step 2: Try Local Library (Control+F) if no Wiktionary examples
            if not all_candidates:
                print(f"    [Fallback 1] Searching local library (Control+F)...")
                all_candidates = search_local_library(word)
                
            # Step 3: Try Wikipedia (Ctrl+F) if still no candidates
            if not all_candidates:
                print(f"    [Fallback 2] Querying Wikipedia...")
                all_candidates = search_wikipedia_sentences(word)
            
            print(f"    Found {len(all_candidates)} candidate sentences total.")
            
            if not all_candidates:
                report_lines.append("- ❌ **Statut : Orphelin** (Aucun candidat trouvé sur aucune source).")
                continue
                
            scored_candidates = []
            valid_found = None
            
            for ex in all_candidates:
                ex_text = ex["text"]
                
                # Windowed sentence to prevent semantic dilution
                window_text = get_window(ex_text, word, window_size=7)
                window_emb = model.encode(window_text, normalize_embeddings=True)
                sim_sentence = cosine_similarity(target_emb, window_emb)
                
                # If Wiktionary provides a sub-definition, we calculate its similarity with the target definition
                if "for_def" in ex:
                    for_def_emb = model.encode(ex["for_def"], normalize_embeddings=True)
                    sim_def = cosine_similarity(target_emb, for_def_emb)
                    # Weighted score: 70% of sub-definition match, 30% of sentence window match
                    score = 0.7 * sim_def + 0.3 * sim_sentence
                    meta_info = f"Wiktionary (Sub-def Match: {sim_def:.2f}, Window Match: {sim_sentence:.2f})"
                else:
                    score = sim_sentence
                    meta_info = f"{ex['source']} (Window Match: {sim_sentence:.2f})"
                
                status = "Validé ✓" if score >= threshold else "Rejeté ❌"
                
                candidate_data = {
                    "text": ex_text,
                    "window": window_text,
                    "source": ex["source"],
                    "score": score,
                    "status": status,
                    "meta": meta_info
                }
                scored_candidates.append(candidate_data)
                
                if score >= threshold:
                    valid_found = candidate_data
                    print(f"    [MATCH FOUND] Stopped search on first valid example: {ex_text[:40]}... (Score: {score:.4f})")
                    break
            
            # Display results in table
            report_lines.append("\n| Exemple d'utilisation candidat | Source / Méthode | Score Combiné | Statut |")
            report_lines.append("|---|---|---|---|")
            for sc in scored_candidates:
                report_lines.append(f"| « {sc['text']} » | {sc['meta']} | {sc['score']:.4f} | {sc['status']} |")
                
            # Summarize choice
            if valid_found:
                report_lines.append(f"\n👉 **Exemple retenu** : « {valid_found['text']} » *(Source: {valid_found['source']}, Score: {valid_found['score']:.4f})*")
            else:
                report_lines.append("\n⚠️ **Alerte** : Aucun candidat n'a passé le test sémantique minimum.")
            
            report_lines.append("\n---")
            
    # Write to final report
    report_path = os.path.join(TOOLS_DIR, "rapport_exemples_pipeline_a.md")
    with open(report_path, "w", encoding="utf-8") as out:
        out.write("\n".join(report_lines))
        
    print(f"\n[DONE] Prototype simulation complete. Results saved in: {report_path}")

if __name__ == "__main__":
    main()
