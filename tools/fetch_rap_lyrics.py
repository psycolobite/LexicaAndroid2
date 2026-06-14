import urllib.request
import urllib.parse
import json
import re
import os
import sys
import time
import html

# Normaliser la sortie console
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, Gecko) Chrome/115.0.0.0 Safari/537.36'
}

CORPUS_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "rap_lyrics_corpus.json")

# Top artistes du rap français contemporain pour capter le jargon récent
ARTISTS = [
    "Jul", "Ninho", "Gazo", "PLK", "Werenoi", "SDM", "Tiakola", "Hamza", "Damso",
    "Freeze Corleone", "Nekfeu", "SCH", "Niska", "Zola", "Koba LaD", "Maes", "Booba",
    "Kaaris", "Orelsan", "Lomepal", "PNL", "Vald", "Heuss L'enfoiré", "Naza", "Leto",
    "Oboy", "Dinos", "Rim'K", "Kalash Criminel", "Sofiane"
]

def load_corpus():
    if os.path.exists(CORPUS_PATH):
        try:
            with open(CORPUS_PATH, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception as e:
            print(f"Error loading corpus: {e}")
    return {}

def save_corpus(corpus):
    try:
        with open(CORPUS_PATH, "w", encoding="utf-8") as f:
            json.dump(corpus, f, ensure_ascii=False, indent=2)
    except Exception as e:
        print(f"Error saving corpus: {e}")

def get_artist_id(artist_name):
    encoded = urllib.parse.quote(artist_name)
    url = f"https://genius.com/api/search/multi?q={encoded}"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            sections = data.get('response', {}).get('sections', [])
            for sec in sections:
                if sec.get('type') == 'artist':
                    hits = sec.get('hits', [])
                    if hits:
                        # Retourner le premier hit d'artiste
                        artist_info = hits[0].get('result', {})
                        return artist_info.get('id'), artist_info.get('name')
    except Exception as e:
        print(f"Error searching artist '{artist_name}': {e}")
    return None, None

def get_artist_songs(artist_id, limit=15):
    songs = []
    # Genius API list songs endpoint
    url = f"https://genius.com/api/artists/{artist_id}/songs?sort=popularity&per_page={limit}"
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            song_list = data.get('response', {}).get('songs', [])
            for s in song_list:
                songs.append({
                    'id': s.get('id'),
                    'title': s.get('title'),
                    'url': s.get('url'),
                    'pageviews': s.get('stats', {}).get('pageviews', 0),
                    'release_date': s.get('release_date_components')
                })
    except Exception as e:
        print(f"Error getting songs for artist {artist_id}: {e}")
    return songs

def scrape_lyrics(url):
    req = urllib.request.Request(url, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            html_content = resp.read().decode('utf-8', errors='ignore')
            
            # Extract lyrics containers
            containers = re.findall(r'<div[^>]*data-lyrics-container="true"[^>]*>(.*?)</div>', html_content, re.DOTALL)
            if not containers:
                containers = re.findall(r'<div class="lyrics">(.*?)</div>', html_content, re.DOTALL)
                
            lyrics_parts = []
            for container in containers:
                container = re.sub(r'<script.*?</script>', '', container, flags=re.DOTALL)
                container = re.sub(r'<br\s*/?>', '\n', container)
                text = re.sub(r'<[^>]+>', '', container)
                lyrics_parts.append(text)
                
            full_lyrics = "\n".join(lyrics_parts)
            full_lyrics = html.unescape(full_lyrics)
            full_lyrics = re.sub(r'\n{3,}', '\n\n', full_lyrics).strip()
            return full_lyrics
    except Exception as e:
        print(f"  Error scraping lyrics from {url}: {e}")
    return None

def main():
    print("==========================================================")
    print("  ACQUISITION DE LYRICS DE RAP FRANÇAIS (GENIUS SOURCING)")
    print("==========================================================")
    
    corpus = load_corpus()
    print(f"Corpus actuel : {len(corpus)} chansons chargées.")
    
    for artist_name in ARTISTS:
        print(f"\nRecherche de l'artiste : {artist_name}...")
        artist_id, real_name = get_artist_id(artist_name)
        if not artist_id:
            print(f"  ✗ Artiste '{artist_name}' introuvable.")
            continue
            
        print(f"  ✓ Trouvé : {real_name} (ID: {artist_id})")
        songs = get_artist_songs(artist_id, limit=15)
        print(f"  ✓ {len(songs)} chansons trouvées pour cet artiste.")
        
        new_downloads = 0
        for song in songs:
            song_id = str(song['id'])
            if song_id in corpus:
                # Déjà dans le cache
                continue
                
            print(f"    Téléchargement : '{song['title']}' ({song['pageviews']} pageviews)...")
            lyrics = scrape_lyrics(song['url'])
            time.sleep(0.6) # Eviter les blocages
            
            if lyrics:
                # Nettoyer les crochets de structure ([Intro], [Couplet 1], etc.)
                clean_lyrics = re.sub(r'\[.*?\]', '', lyrics)
                clean_lyrics = re.sub(r'\n+', '\n', clean_lyrics).strip()
                
                # Récupérer l'année de sortie
                year = None
                if song['release_date']:
                    year = song['release_date'].get('year')
                
                corpus[song_id] = {
                    'title': song['title'],
                    'artist': real_name,
                    'url': song['url'],
                    'pageviews': song['pageviews'],
                    'year': year,
                    'lyrics': clean_lyrics
                }
                new_downloads += 1
                
                # Sauvegarder progressivement
                if new_downloads % 5 == 0:
                    save_corpus(corpus)
                    
        if new_downloads > 0:
            save_corpus(corpus)
            print(f"  ✓ {new_downloads} nouvelles chansons ajoutées au corpus.")
        else:
            print(f"  ✓ Déjà à jour pour {real_name}.")
            
    print(f"\n[DONE] Sourcing terminé. Taille finale du corpus : {len(corpus)} chansons.")
    print(f"Corpus sauvegardé dans : {CORPUS_PATH}")

if __name__ == "__main__":
    main()
