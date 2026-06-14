"""
apply_json_to_html.py
=====================
Lit tableau_data.json et réinjecte les contenus modifiés dans le HTML du dashboard.

USAGE :
    python tools/apply_json_to_html.py

Flux de travail :
    1. L'agent modifie tableau_data.json
    2. L'agent (ou l'utilisateur) lance ce script
    3. Le HTML est mis à jour automatiquement
    4. L'utilisateur fait F5 dans le navigateur → les changements sont visibles

Ce que le script met à jour :
    - Le contenu textuel de chaque onglet (tab-content > editable-area)
    - Le nom des catégories dans les nœuds de la mind map
    - Le titre des fiches dans le panneau droit
    - Les notes transversales (notes-transversales)
    - Les noms des pipelines (node-title / node-subtitle)

Ce que le script ne modifie PAS (structure statique) :
    - Le CSS, le JS, les SVG de connexion
    - Les nœuds et branches de la mind map (structure HTML profonde)
    - Les badges de statut (éditer manuellement si nécessaire)
"""

import json
import re
import sys
from pathlib import Path
from datetime import datetime

try:
    from bs4 import BeautifulSoup, NavigableString
except ImportError:
    print("[ERREUR] BeautifulSoup non installe. Lancer : pip install beautifulsoup4")
    sys.exit(1)

if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

# === Chemins ===
ROOT = Path(__file__).parent.parent
HTML_PATH = ROOT / "docs" / "amelioration_de_la_fonction_de_recherche" / "algorithme_de_presentation_des_extraits" / "tableau_interactif_algorithmes.html"
JSON_PATH = HTML_PATH.parent / "tableau_data.json"


# ============================================================
# Helpers
# ============================================================

def set_element_text(el, text: str):
    """Vide le contenu d'un element et y place le texte brut (en preservant le tag)."""
    if el is None:
        return
    # Supprimer tous les enfants
    for child in list(el.children):
        child.extract()
    el.append(NavigableString(text))


def find_nth_tab_content(card_div, n: int):
    """Retourne le n-ieme .tab-content dans une fiche card-detail."""
    tab_contents = card_div.select(".tab-content")
    if n < len(tab_contents):
        return tab_contents[n]
    return None


def find_editable_area(tab_content_div):
    """Retourne le .editable-area dans un tab-content (ou le tab-content lui-meme)."""
    if tab_content_div is None:
        return None
    ea = tab_content_div.select_one(".editable-area")
    return ea if ea else tab_content_div


def clean_json_text(text: str) -> str:
    """Normalise le texte JSON (supprime les espaces de formatage HTML excessifs)."""
    if not text:
        return ""
    # Normaliser les sauts de ligne multiples
    text = re.sub(r'\n{3,}', '\n\n', text)
    return text.strip()


# ============================================================
# Fonctions d'application
# ============================================================

def apply_global_notes(soup, data: dict) -> int:
    """Met a jour les notes transversales."""
    notes_el = soup.find(id="notes-transversales")
    if not notes_el:
        print("  [SKIP] #notes-transversales introuvable")
        return 0
    text = clean_json_text(data.get("global_notes", ""))
    if text:
        set_element_text(notes_el, text)
        print(f"  [OK] notes-transversales mises a jour ({len(text)} chars)")
        return 1
    return 0


def apply_pipeline_names(soup, pipeline: dict) -> int:
    """Met a jour le nom et le sous-titre d'un noeud pipeline dans la mind map."""
    updated = 0
    pipe_id = pipeline.get("id", "")
    node = soup.find(id=f"node-{pipe_id}")
    if not node:
        print(f"  [SKIP] noeud pipeline #{pipe_id} introuvable")
        return 0

    name_el = node.select_one(".node-title")
    subtitle_el = node.select_one(".node-subtitle")

    if name_el and pipeline.get("name"):
        set_element_text(name_el, clean_json_text(pipeline["name"]))
        updated += 1

    if subtitle_el and pipeline.get("subtitle"):
        set_element_text(subtitle_el, clean_json_text(pipeline["subtitle"]))
        updated += 1

    return updated


def apply_category(soup, category: dict) -> int:
    """Met a jour une categorie : noeud mind map + titre fiche + tous les onglets."""
    updated = 0
    cat_id = category.get("id", "")

    # --- Noeud dans la mind map (3eme colonne)
    cat_node = soup.find(id=f"node-{cat_id}")
    if cat_node and category.get("name"):
        # Garder les boutons delete s'ils existent
        delete_btns = cat_node.select(".delete-btn")
        set_element_text(cat_node, clean_json_text(category["name"]))
        for btn in delete_btns:
            cat_node.append(btn)
        updated += 1

    # --- Fiche dans le panneau droit
    card = soup.find(id=f"card-{cat_id}")
    if not card:
        print(f"  [SKIP] fiche #card-{cat_id} introuvable (categorie pas encore dans le HTML)")
        return updated

    # Titre de la fiche
    card_title = card.select_one(".card-title")
    if card_title and category.get("name"):
        set_element_text(card_title, clean_json_text(category["name"]))
        updated += 1

    # --- Onglets
    tabs = category.get("tabs", [])
    tab_btns = card.select(".tab-btn:not(.add-tab-btn)")

    for i, tab_data in enumerate(tabs):
        # Mettre a jour le bouton d'onglet (titre)
        if i < len(tab_btns) and tab_data.get("title"):
            # Garder les enfants (delete btn) s'il y en a
            btn = tab_btns[i]
            delete_btns = btn.select(".delete-btn")
            set_element_text(btn, clean_json_text(tab_data["title"]))
            for dbtn in delete_btns:
                btn.append(dbtn)
            updated += 1

        # Mettre a jour le contenu de l'onglet
        tab_content = find_nth_tab_content(card, i)
        editable = find_editable_area(tab_content)
        if editable and tab_data.get("content"):
            set_element_text(editable, clean_json_text(tab_data["content"]))
            updated += 1

        # Noeud dans la 4e colonne (sub-branch)
        sub_node = soup.find(id=f"node-{cat_id}-sub{i}")
        if sub_node and tab_data.get("title"):
            delete_btns = sub_node.select(".delete-btn")
            set_element_text(sub_node, clean_json_text(tab_data["title"]))
            for dbtn in delete_btns:
                sub_node.append(dbtn)
            updated += 1

    return updated


def apply_json_to_html(json_path: Path, html_path: Path) -> None:
    """Point d'entree principal : charge JSON, parse HTML, reinjecte, sauvegarde."""
    print(f"[JSON] Chargement : {json_path.name}")
    with open(json_path, encoding="utf-8") as f:
        data = json.load(f)

    print(f"[HTML] Lecture : {html_path.name}")
    with open(html_path, encoding="utf-8", errors="ignore") as f:
        soup = BeautifulSoup(f.read(), "html.parser")

    total_updates = 0
    print("\n--- Application des modifications ---")

    # Notes transversales
    total_updates += apply_global_notes(soup, data)

    # Pipelines et leurs categories
    for pipeline in data.get("pipelines", []):
        pipe_id = pipeline.get("id", "?")
        pipe_name = pipeline.get("name", "?")
        print(f"\n  Pipeline [{pipe_id}] {pipe_name}")

        total_updates += apply_pipeline_names(soup, pipeline)

        for cat in pipeline.get("categories", []):
            cat_id = cat.get("id", "?")
            cat_name = cat.get("name", "?").split("\n")[0].strip()
            n_tabs = len(cat.get("tabs", []))
            print(f"    -> [{cat_id}] {cat_name} ({n_tabs} onglets)")
            n = apply_category(soup, cat)
            total_updates += n
            print(f"       {n} elements mis a jour")

    # Mettre a jour la meta dans le HTML (commentaire)
    ts = datetime.now().strftime("%Y-%m-%d %H:%M")
    comment_marker = soup.find(string=re.compile(r'apply_json_to_html'))
    # Ajouter un commentaire en tete du body si absent
    body = soup.find("body")
    if body:
        old_comment = body.find(string=re.compile(r'Last JSON apply:'))
        if old_comment:
            old_comment.replace_with(f" Last JSON apply: {ts} | apply_json_to_html.py ")
        else:
            from bs4 import Comment
            body.insert(0, Comment(f" Last JSON apply: {ts} | apply_json_to_html.py "))

    # Sauvegarde
    print(f"\n[HTML] Ecriture : {html_path.name}")
    with open(html_path, "w", encoding="utf-8") as f:
        f.write(str(soup))

    print(f"\n[DONE] {total_updates} elements mis a jour dans le HTML.")
    print(f"       Recharger le fichier dans le navigateur (F5) pour voir les changements.")


def main():
    if not JSON_PATH.exists():
        print(f"[ERREUR] JSON introuvable : {JSON_PATH}")
        print("  Lancer d'abord : python tools/extract_dashboard_data.py")
        sys.exit(1)
    if not HTML_PATH.exists():
        print(f"[ERREUR] HTML introuvable : {HTML_PATH}")
        sys.exit(1)

    apply_json_to_html(JSON_PATH, HTML_PATH)


if __name__ == "__main__":
    main()
