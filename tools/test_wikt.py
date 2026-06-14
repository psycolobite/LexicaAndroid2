import urllib.request
import urllib.parse
import json

base_url = "https://fr.wiktionary.org/w/api.php"
params = {
    "action": "query",
    "list": "categorymembers",
    "cmtitle": "Catégorie:Rhétorique en français",
    "cmtype": "page",
    "cmnamespace": "0",
    "cmlimit": "10",
    "format": "json",
    "formatversion": "2"
}
url = base_url + "?" + urllib.parse.urlencode(params)
headers = {'User-Agent': 'LexicaAndroid2/1.0 (vocabulaire francais)'}
req = urllib.request.Request(url, headers=headers)
try:
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        print(json.dumps(data, indent=2, ensure_ascii=False))
except Exception as e:
    print(f"Error: {e}")
