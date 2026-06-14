"""
C3 Pipeline A — Probe de faisabilité : Détection d'expressions trending
via les pageviews Wiktionnaire (proxy du "dernier cri" linguistique).

Hypothèse : Quand un mot d'argot se propage viralement, les gens qui
ne le connaissent pas le cherchent sur Wiktionnaire → pic de pageviews.
Un pic de pageviews = le mot est en train de "buzzer".
"""

import sys
import io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

import urllib.request
import urllib.parse
import json
import time

HEADERS = {'User-Agent': 'LexicaAndroid2/1.0 (vocabulaire francais; contact: lexica@test.com)'}

def get_pageviews(word, start="20250101", end="20260601"):
    """Récupère les pageviews mensuelles d'un mot sur fr.wiktionary.org"""
    encoded = urllib.parse.quote(word, safe='')
    url = (
        f"https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/"
        f"fr.wiktionary.org/all-access/user/{encoded}/monthly/{start}/{end}"
    )
    try:
        req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            items = data.get('items', [])
            return [(item['timestamp'][:6], item['views']) for item in items]
    except Exception as e:
        return []

def get_category_members(category, limit=50):
    """Récupère les mots d'une catégorie Wiktionnaire"""
    encoded_cat = urllib.parse.quote(f"Catégorie:{category}")
    url = (
        f"https://fr.wiktionary.org/w/api.php?action=query"
        f"&list=categorymembers&cmtitle={encoded_cat}"
        f"&cmlimit={limit}&cmtype=page&cmnamespace=0&format=json"
    )
    try:
        req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            members = data.get('query', {}).get('categorymembers', [])
            return [m['title'] for m in members]
    except Exception as e:
        print(f"  Erreur catégorie '{category}': {e}")
        return []


def main():
    print("=" * 70)
    print("PROBE C3 : Détection du 'Dernier Cri' via Wiktionnaire Pageviews")
    print("=" * 70)
    
    # === ÉTAPE 1 : Tester le signal pageviews sur des mots connus ===
    print("\n--- ÉTAPE 1 : Signal Pageviews sur mots d'argot connus ---")
    print("Hypothèse : les mots d'argot populaires ont des pageviews élevées")
    print("           sur Wiktionnaire (les gens les cherchent).\n")
    
    test_words = {
        # Argot viral récent (devrait avoir des pageviews élevées)
        "seum": "argot récent - dégoût/frustration",
        "miskine": "argot récent - emprunté à l'arabe",
        "chelou": "argot récent - verlan de louche",
        "boloss": "argot récent - victime/nul",
        "kiffer": "argot établi - aimer beaucoup",
        "masterclass": "néologisme viral",
        "paf": "argot très récent (sens sexuel)",
        "daron": "argot - père",
        "wesh": "interjection argotique",
        "crari": "argot récent - faire genre",
        "askip": "abréviation - à ce qu'il paraît",
        # Mots de contrôle (vocabulaire soutenu, pas d'argot)
        "nonobstant": "CONTRÔLE - mot littéraire/rhétorique",
        "magnanimité": "CONTRÔLE - mot littéraire rare",
    }
    
    results = []
    for word, desc in test_words.items():
        views = get_pageviews(word)
        if views:
            total = sum(v for _, v in views)
            recent_3m = sum(v for ts, v in views if ts >= "202603")
            older_avg = 0
            older = [(ts, v) for ts, v in views if ts < "202603"]
            if older:
                older_avg = sum(v for _, v in older) // len(older)
            recent_avg = recent_3m // 3 if recent_3m > 0 else 0
            trend_ratio = recent_avg / max(older_avg, 1)
            
            results.append({
                'word': word,
                'desc': desc,
                'total': total,
                'monthly_avg': total // max(len(views), 1),
                'recent_avg': recent_avg,
                'older_avg': older_avg,
                'trend_ratio': trend_ratio,
                'raw_views': views
            })
        else:
            results.append({
                'word': word,
                'desc': desc,
                'total': 0,
                'monthly_avg': 0,
                'recent_avg': 0,
                'older_avg': 0,
                'trend_ratio': 0,
                'raw_views': []
            })
        time.sleep(0.3)  # Rate limiting
    
    # Affichage trié par pageviews
    results.sort(key=lambda x: x['total'], reverse=True)
    
    print(f"{'Mot':<15} {'Type':<35} {'Total':>7} {'Moy/m':>6} {'Récent':>7} {'Ancien':>7} {'Trend↑':>7}")
    print("-" * 95)
    for r in results:
        trend_arrow = "↑↑" if r['trend_ratio'] > 1.5 else ("↑" if r['trend_ratio'] > 1.1 else ("→" if r['trend_ratio'] > 0.8 else "↓"))
        print(f"{r['word']:<15} {r['desc']:<35} {r['total']:>7} {r['monthly_avg']:>6} {r['recent_avg']:>7} {r['older_avg']:>7} {r['trend_ratio']:>6.2f}{trend_arrow}")
    
    # === ÉTAPE 2 : Scanner les catégories Wiktionnaire disponibles ===
    print("\n\n--- ÉTAPE 2 : Catégories Wiktionnaire disponibles pour C3 ---")
    
    categories = [
        "Termes argotiques en français",
        "Néologismes en français", 
        "Termes familiers en français",
        "Termes vulgaires en français",
        "Argot Internet en français",
        "Verlan en français",
        "Termes populaires en français",
    ]
    
    for cat in categories:
        members = get_category_members(cat, limit=15)
        real_words = [m for m in members if not m.startswith("Catégorie:") and not m.startswith("Annexe:")]
        print(f"\n📁 {cat} ({len(real_words)} premiers mots) :")
        if real_words:
            print(f"   → {', '.join(real_words[:12])}{'...' if len(real_words) > 12 else ''}")
        else:
            print(f"   → (vide ou inaccessible)")
        time.sleep(0.5)
    
    # === ÉTAPE 3 : Comptage total des mots disponibles ===
    print("\n\n--- ÉTAPE 3 : Comptage des mots par catégorie ---")
    for cat in categories:
        encoded_cat = urllib.parse.quote(f"Catégorie:{cat}")
        url = (
            f"https://fr.wiktionary.org/w/api.php?action=query"
            f"&prop=categoryinfo&titles={encoded_cat}&format=json"
        )
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=10) as resp:
                data = json.loads(resp.read().decode('utf-8'))
                pages = data.get('query', {}).get('pages', {})
                for pid, page in pages.items():
                    info = page.get('categoryinfo', {})
                    count = info.get('pages', 0) + info.get('subcats', 0)
                    pages_count = info.get('pages', 0)
                    subcats = info.get('subcats', 0)
                    print(f"  {cat:<40} → {pages_count:>5} pages, {subcats:>3} sous-catégories")
        except Exception as e:
            print(f"  {cat}: erreur - {e}")
        time.sleep(0.3)
    
    print("\n" + "=" * 70)
    print("FIN DE LA PROBE — Analyse des résultats ci-dessus pour décider")
    print("de la stratégie algorithmique C3.")
    print("=" * 70)


if __name__ == "__main__":
    main()
