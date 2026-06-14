"""
extract_dashboard_data.py
=========================
Extrait le contenu structuré du dashboard HTML de Lexica et génère un fichier JSON compagnon.

USAGE (par l'agent ou manuellement) :
    python tools/extract_dashboard_data.py

Entrée  : docs/.../tableau_interactif_algorithmes.html
Sortie  : docs/.../tableau_data.json

Ce script est la passerelle entre l'interface HTML (ergonomique pour l'utilisateur)
et un format JSON léger (lisible/modifiable par l'agent AI sans parser 3000 lignes de HTML).
"""

import json
import re
import sys
from datetime import datetime
from pathlib import Path

# Forcer UTF-8 sur Windows pour les prints
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

try:
    from bs4 import BeautifulSoup
except ImportError:
    print("❌ BeautifulSoup non installé. Lancer : pip install beautifulsoup4")
    sys.exit(1)

# === Chemins ===
ROOT = Path(__file__).parent.parent
HTML_PATH = ROOT / "docs" / "amelioration_de_la_fonction_de_recherche" / "algorithme_de_presentation_des_extraits" / "tableau_interactif_algorithmes.html"
JSON_PATH = HTML_PATH.parent / "tableau_data.json"


def clean_text(text: str) -> str:
    """Nettoie le texte extrait : supprimer espaces multiples et caractères parasites."""
    if not text:
        return ""
    text = text.replace("🗑", "").replace("\u00a0", " ")
    text = re.sub(r"\n{3,}", "\n\n", text)
    return text.strip()


def extract_tabs(card_div) -> list[dict]:
    """Extrait les onglets (tab-btn + tab-content) d'une fiche card-detail."""
    tabs = []
    if not card_div:
        return tabs

    tab_btns = card_div.select(".tab-buttons .tab-btn:not(.add-tab-btn)")
    tab_contents = card_div.select(".tab-content")

    for i, btn in enumerate(tab_btns):
        title = clean_text(btn.get_text())
        content = ""
        if i < len(tab_contents):
            content = clean_text(tab_contents[i].get_text())
        tabs.append({"title": title, "content": content})

    return tabs


def extract_badges(card_div) -> list[str]:
    """Extrait les badges de statut d'une fiche."""
    if not card_div:
        return []
    return [clean_text(b.get_text()) for b in card_div.select(".badge") if b.get_text().strip()]


def extract_status(card_div) -> str:
    """Retourne 'done', 'todo' ou 'unknown'."""
    if not card_div:
        return "unknown"
    if card_div.select_one(".status-done"):
        return "done"
    if card_div.select_one(".status-todo"):
        return "todo"
    return "unknown"


def extract_pipeline(soup, pipe_id: str) -> dict:
    """Extrait une pipeline complète (nœud + catégories)."""
    pipe_node = soup.find(id=f"node-{pipe_id}")
    if not pipe_node:
        return None

    name_el = pipe_node.select_one(".node-title")
    subtitle_el = pipe_node.select_one(".node-subtitle")
    name = clean_text(name_el.get_text()) if name_el else clean_text(pipe_node.get_text())
    subtitle = clean_text(subtitle_el.get_text()) if subtitle_el else ""

    categories = []
    branch = soup.find(id=f"{pipe_id}-children")
    if branch:
        for cat_node in branch.select(".node-category"):
            cat_id = (cat_node.get("id") or "").replace("node-", "")
            if not cat_id:
                continue
            cat_name = clean_text(cat_node.get_text())

            card = soup.find(id=f"card-{cat_id}")
            categories.append({
                "id": cat_id,
                "name": cat_name,
                "status": extract_status(card),
                "badges": extract_badges(card),
                "tabs": extract_tabs(card),
            })

    return {
        "id": pipe_id,
        "name": name,
        "subtitle": subtitle,
        "categories": categories,
    }


def extract_data(html_path: Path) -> dict:
    """Point d'entrée principal : lit le HTML, retourne le dict structuré."""
    print(f"[HTML] Lecture de : {html_path}")
    with open(html_path, encoding="utf-8", errors="ignore") as f:
        soup = BeautifulSoup(f.read(), "html.parser")

    data = {
        "meta": {
            "version": "2.0",
            "last_export": datetime.now().isoformat(),
            "source_file": str(html_path.name),
            "description": "Lexica Dashboard — données structurées (généré par extract_dashboard_data.py)",
        },
        "global_notes": "",
        "pipelines": [],
    }

    # Notes transversales
    notes_el = soup.find(id="notes-transversales")
    if notes_el:
        data["global_notes"] = clean_text(notes_el.get_text())

    # Trouver toutes les pipelines à partir des nœuds pipeline
    pipeline_nodes = soup.select(".node-pipeline")
    for pn in pipeline_nodes:
        node_id = pn.get("id", "")
        pipe_id = node_id.replace("node-", "")
        if not pipe_id:
            continue
        pipeline = extract_pipeline(soup, pipe_id)
        if pipeline:
            data["pipelines"].append(pipeline)

    return data


def save_json(data: dict, json_path: Path) -> None:
    """Sauvegarde le JSON avec encodage UTF-8 et indentation lisible."""
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"[OK] JSON sauvegarde : {json_path}")
    print(f"   -> {len(data['pipelines'])} pipeline(s)")
    for p in data["pipelines"]:
        print(f"      [{p['id']}] {p['name']} -- {len(p['categories'])} categorie(s)")


def main():
    if not HTML_PATH.exists():
        print(f"[ERREUR] Fichier HTML introuvable : {HTML_PATH}")
        sys.exit(1)

    data = extract_data(HTML_PATH)
    save_json(data, JSON_PATH)
    print(f"\n[INFO] Usage agent : lire {JSON_PATH.name} pour acceder aux donnees sans parser le HTML.")


if __name__ == "__main__":
    main()
