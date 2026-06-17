import os
import json
import urllib.request
import urllib.parse
import time
import sys
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))
WORDS_JSON_PATH = os.path.join(TOOLS_DIR, "consolidated_literary_words.json")
CACHE_PATH = os.path.join(TOOLS_DIR, "c1_wiktionary_pageviews.json")

HEADERS = {'User-Agent': 'LexicaAndroid2/1.0 (vocabulaire francais; contact: lexica@test.com)'}

def get_pageviews(word, start="20250101", end="20260601"):
    encoded = urllib.parse.quote(word, safe='')
    url = (
        f"https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/"
        f"fr.wiktionary.org/all-access/user/{encoded}/monthly/{start}/{end}"
    )
    try:
        req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=5) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            items = data.get('items', [])
            return sum(item['views'] for item in items)
    except Exception:
        return 0

def main():
    if not os.path.exists(WORDS_JSON_PATH):
        print(f"Error: {WORDS_JSON_PATH} not found.")
        return

    with open(WORDS_JSON_PATH, "r", encoding="utf-8") as f:
        words_data = json.load(f)

    # Load existing cache if any to avoid re-fetching
    cache = {}
    if os.path.exists(CACHE_PATH):
        try:
            with open(CACHE_PATH, "r", encoding="utf-8") as f:
                cache = json.load(f)
            print(f"Loaded existing cache with {len(cache)} words.")
        except Exception:
            pass

    print(f"Starting pageview fetch for {len(words_data)} words...")
    count = 0
    for i, item in enumerate(words_data):
        w = item["mot"].lower().strip()
        if w in cache:
            continue
        
        views = get_pageviews(w)
        cache[w] = views
        count += 1
        
        if count % 20 == 0:
            print(f"Progress: {i+1}/{len(words_data)} fetched (saving cache)...")
            with open(CACHE_PATH, "w", encoding="utf-8") as f:
                json.dump(cache, f, ensure_ascii=False, indent=2)
            
        time.sleep(0.05)  # 50ms delay to avoid aggressive rate limiting

    with open(CACHE_PATH, "w", encoding="utf-8") as f:
        json.dump(cache, f, ensure_ascii=False, indent=2)
    print("Pageview fetch complete. Cache saved.")

if __name__ == "__main__":
    main()
