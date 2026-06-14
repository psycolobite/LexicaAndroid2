"""
apply_json_to_html.py
=====================
Lit tableau_data.json et réinjecte ou génère dynamiquement les pipelines,
catégories, onglets et contenus associés dans le HTML du dashboard.

USAGE :
    python tools/apply_json_to_html.py
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
# Helpers de structure & styles
# ============================================================

def get_color_class(cat_id: str, index: int) -> str:
    """Déduit la classe CSS de couleur (c1 à c5) pour une catégorie."""
    m = re.search(r'c(\d+)', cat_id, re.IGNORECASE)
    if m:
        val = int(m.group(1))
        return f"c{((val - 1) % 5) + 1}"
    return f"c{str((index % 5) + 1)}"


def get_color_hex(color_class: str) -> str:
    """Retourne le code couleur Hex correspondant aux variables CSS."""
    colors = {
        "c1": "#38bdf8",
        "c2": "#fb923c",
        "c3": "#4ade80",
        "c4": "#f472b6",
        "c5": "#c084fc",
    }
    return colors.get(color_class, "#38bdf8")


def set_element_text(el, text: str):
    """Vide le contenu d'un element et y place le texte brut (en preservant le tag)."""
    if el is None:
        return
    for child in list(el.children):
        child.extract()
    el.append(NavigableString(text))


def clean_json_text(text: str) -> str:
    """Normalise le texte JSON (supprime les espaces de formatage HTML excessifs)."""
    if not text:
        return ""
    text = re.sub(r'\n{3,}', '\n\n', text)
    return text.strip()


# ============================================================
# Génération Dynamique d'Éléments HTML Manquants
# ============================================================

def ensure_pipeline_nodes(soup, pipeline: dict):
    """S'assure que le noeud pipeline et ses conteneurs de catégories filles existent."""
    pipe_id = pipeline.get("id", "")
    pipe_name = pipeline.get("name", "")
    pipe_subtitle = pipeline.get("subtitle", "")

    # 1. Noeud Pipeline
    pipe_node = soup.find(id=f"node-{pipe_id}")
    if not pipe_node:
        print(f"  [GEN] Création du nœud pipeline #{pipe_id}")
        pipes_col = soup.find(class_="pipelines-col")
        add_btn = pipes_col.find(class_="add-pipeline-btn")

        pipe_node = soup.new_tag("div", attrs={
            "class": f"node node-pipeline {pipe_id}",
            "id": f"node-{pipe_id}",
            "onclick": f"togglePipeline('{pipe_id}')",
            "style": f"border-left: 6px solid {get_color_hex('c1')}; position: relative;"
        })

        title_div = soup.new_tag("div", attrs={"class": "node-title", "contenteditable": "true", "spellcheck": "false"})
        title_div.string = pipe_name
        pipe_node.append(title_div)

        subtitle_div = soup.new_tag("div", attrs={"class": "node-subtitle", "contenteditable": "true", "spellcheck": "false"})
        subtitle_div.string = pipe_subtitle
        pipe_node.append(subtitle_div)

        del_btn = soup.new_tag("button", attrs={"class": "delete-btn", "onclick": f"deletePipeline('{pipe_id}', event)"})
        del_btn.string = "🗑"
        pipe_node.append(del_btn)

        if add_btn:
            add_btn.insert_before(pipe_node)
        else:
            pipes_col.append(pipe_node)

    # 2. Conteneur de catégories filles dans la colonne 3
    branch_container = soup.find(id=f"{pipe_id}-children")
    if not branch_container:
        print(f"  [GEN] Création du conteneur de catégories #{pipe_id}-children")
        cats_col = soup.find(class_="categories-col")
        branch_container = soup.new_tag("div", attrs={"class": "category-branch", "id": f"{pipe_id}-children"})

        add_cat_btn = soup.new_tag("button", attrs={
            "class": "add-category-btn",
            "onclick": f"addCategory('{pipe_id}')",
            "title": f"Ajouter une catégorie à {pipe_name}"
        })
        add_cat_btn.string = "＋ Catégorie"
        branch_container.append(add_cat_btn)
        cats_col.append(branch_container)


def ensure_category_nodes(soup, pipe_id: str, category: dict, index: int):
    """S'assure que le nœud catégorie, le sub-branch et la fiche de détails existent."""
    cat_id = category.get("id", "")
    cat_name = category.get("name", "")
    cat_badges = category.get("badges", [])
    color_class = get_color_class(cat_id, index)
    color_hex = get_color_hex(color_class)

    # 1. Nœud de Catégorie dans la Mind Map (Col 3)
    cat_node = soup.find(id=f"node-{cat_id}")
    if not cat_node:
        print(f"  [GEN] Création du nœud catégorie #{cat_id}")
        branch = soup.find(id=f"{pipe_id}-children")
        add_btn = branch.find(class_="add-category-btn")

        cat_node = soup.new_tag("div", attrs={
            "class": f"node node-category {color_class}",
            "contenteditable": "true",
            "id": f"node-{cat_id}",
            "onclick": f"selectCategory('{cat_id}')",
            "spellcheck": "false",
            "style": f"border-left: 4px solid {color_hex}; position: relative;"
        })
        cat_node.string = cat_name

        del_btn = soup.new_tag("button", attrs={"class": "delete-btn", "onclick": f"deleteCategory('{cat_id}', event)"})
        del_btn.string = "🗑"
        cat_node.append(del_btn)

        if add_btn:
            add_btn.insert_before(cat_node)
        else:
            branch.append(cat_node)

    # 2. Conteneur de sous-catégories (Col 4)
    sub_branch = soup.find(id=f"{cat_id}-sub-branch")
    if not sub_branch:
        print(f"  [GEN] Création du conteneur de sous-branche #{cat_id}-sub-branch")
        sub_col = soup.find(class_="subcategories-col")
        sub_branch = soup.new_tag("div", attrs={
            "class": "category-branch",
            "id": f"{cat_id}-sub-branch",
            "style": "display: none; gap: 10px;"
        })
        sub_col.append(sub_branch)

    # 3. Fiche détaillée (Detail Panel à droite)
    card = soup.find(id=f"card-{cat_id}")
    if not card:
        print(f"  [GEN] Création de la fiche de détails #card-{cat_id}")
        detail_panel = soup.find(class_="detail-panel")

        card = soup.new_tag("div", attrs={
            "class": f"card-detail {color_class}",
            "id": f"card-{cat_id}",
            "style": f"--cat-color: {color_hex};"
        })

        header_row = soup.new_tag("div", attrs={"class": "card-header-row"})
        title_div = soup.new_tag("div", attrs={
            "class": "card-title",
            "contenteditable": "true",
            "spellcheck": "false",
            "style": f"color: {color_hex}"
        })
        title_div.string = cat_name
        header_row.append(title_div)

        tag_span = soup.new_tag("span", attrs={
            "class": "card-tag",
            "contenteditable": "true",
            "spellcheck": "false",
            "style": f"background: {color_hex}20; color: {color_hex}"
        })
        tag_span.string = cat_name
        header_row.append(tag_span)
        card.append(header_row)

        status_row = soup.new_tag("div", attrs={"style": "display:flex; gap:8px; align-items:center; margin-bottom:12px; flex-wrap:wrap;"})
        status_span = soup.new_tag("span", attrs={"class": "status-todo"})
        status_span.string = "⚙ En cours"
        status_row.append(status_span)

        for badge in cat_badges:
            badge_span = soup.new_tag("span", attrs={
                "class": f"badge badge-{color_class if color_class != 'c1' else 'blue'}",
                "contenteditable": "true",
                "spellcheck": "false"
            })
            badge_span.string = badge
            status_row.append(badge_span)

        card.append(status_row)

        sub_tabs_container = soup.new_tag("div", attrs={"class": "sub-tabs-container"})
        tab_buttons = soup.new_tag("div", attrs={"class": "tab-buttons", "id": f"tabs-{cat_id}"})

        add_tab_btn = soup.new_tag("button", attrs={
            "class": "add-tab-btn",
            "onclick": f"addTab('card-{cat_id}')",
            "title": "Ajouter un onglet"
        })
        add_tab_btn.string = "＋"
        tab_buttons.append(add_tab_btn)
        sub_tabs_container.append(tab_buttons)
        card.append(sub_tabs_container)

        detail_panel.append(card)


def ensure_tab_elements(soup, cat_id: str, color_class: str, tab_data: dict, tab_index: int):
    """S'assure que les boutons d'onglets, contenus éditables et nœuds subcategories existent."""
    card = soup.find(id=f"card-{cat_id}")
    tab_title = tab_data.get("title", "")
    tab_content_text = tab_data.get("content", "")

    # 1. Bouton d'onglet
    tab_buttons = card.find(class_="tab-buttons")
    add_tab_btn = tab_buttons.find(class_="add-tab-btn")
    tab_wrappers = tab_buttons.find_all(class_="tab-btn-wrapper")

    if tab_index >= len(tab_wrappers):
        print(f"  [GEN] Création de l'onglet {tab_index} pour la fiche #card-{cat_id}")
        btn_wrapper = soup.new_tag("div", attrs={"class": "tab-btn-wrapper"})
        btn = soup.new_tag("button", attrs={
            "class": "tab-btn" + (" active" if tab_index == 0 else ""),
            "onclick": f"switchSubTab('card-{cat_id}', {tab_index}, this)"
        })
        btn.string = tab_title
        btn_wrapper.append(btn)

        del_btn = soup.new_tag("button", attrs={"class": "delete-btn", "onclick": f"deleteTab('card-{cat_id}', {tab_index}, event)"})
        del_btn.string = "🗑"
        btn_wrapper.append(del_btn)

        if add_tab_btn:
            add_tab_btn.insert_before(btn_wrapper)
        else:
            tab_buttons.append(btn_wrapper)

    # 2. Contenu d'onglet
    sub_tabs_container = card.find(class_="sub-tabs-container")
    tab_contents = sub_tabs_container.find_all(class_="tab-content", recursive=False)

    if tab_index >= len(tab_contents):
        content_div = soup.new_tag("div", attrs={"class": "tab-content" + (" active" if tab_index == 0 else "")})
        editable_area = soup.new_tag("div", attrs={
            "class": "editable-area",
            "contenteditable": "true",
            "spellcheck": "false"
        })
        editable_area.string = tab_content_text
        content_div.append(editable_area)
        sub_tabs_container.append(content_div)

    # 3. Nœud de sous-branche (Col 4)
    sub_branch = soup.find(id=f"{cat_id}-sub-branch")
    sub_node = soup.find(id=f"node-{cat_id}-sub{tab_index}")
    if not sub_node:
        sub_node = soup.new_tag("div", attrs={
            "class": f"node node-subcategory {color_class}",
            "contenteditable": "true",
            "id": f"node-{cat_id}-sub{tab_index}",
            "onclick": f"selectSubCategory('{cat_id}', {tab_index})",
            "spellcheck": "false"
        })
        sub_node.string = tab_title

        del_btn = soup.new_tag("button", attrs={"class": "delete-btn", "onclick": f"deleteTab('card-{cat_id}', {tab_index}, event)"})
        del_btn.string = "🗑"
        sub_node.append(del_btn)

        # Mettre en premier actif si c'est l'index 0
        if tab_index == 0:
            sub_node["class"] = f"node node-subcategory {color_class} active"

        sub_branch.append(sub_node)


# ============================================================
# Synchronisation & Injection
# ============================================================

def apply_global_notes(soup, data: dict) -> int:
    notes_el = soup.find(id="notes-transversales")
    if not notes_el:
        return 0
    text = clean_json_text(data.get("global_notes", ""))
    if text:
        set_element_text(notes_el, text)
        return 1
    return 0


def apply_pipeline_names(soup, pipeline: dict) -> int:
    updated = 0
    pipe_id = pipeline.get("id", "")
    node = soup.find(id=f"node-{pipe_id}")
    if not node:
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


def apply_category(soup, category: dict, index: int) -> int:
    updated = 0
    cat_id = category.get("id", "")
    color_class = get_color_class(cat_id, index)

    # --- Nœud Mindmap
    cat_node = soup.find(id=f"node-{cat_id}")
    if cat_node and category.get("name"):
        delete_btns = cat_node.select(".delete-btn")
        set_element_text(cat_node, clean_json_text(category["name"]))
        for btn in delete_btns:
            cat_node.append(btn)
        updated += 1

    # --- Fiche détaillée
    card = soup.find(id=f"card-{cat_id}")
    if not card:
        return updated

    # Titre de la fiche
    card_title = card.select_one(".card-title")
    if card_title and category.get("name"):
        set_element_text(card_title, clean_json_text(category["name"]))
        updated += 1

    # --- Onglets & Contenus
    tabs = category.get("tabs", [])
    tab_btns = card.select(".tab-btn:not(.add-tab-btn)")
    tab_contents = card.select(".tab-content")

    for i, tab_data in enumerate(tabs):
        # Mettre à jour le titre sur l'onglet
        if i < len(tab_btns) and tab_data.get("title"):
            btn = tab_btns[i]
            delete_btns = btn.select(".delete-btn")
            set_element_text(btn, clean_json_text(tab_data["title"]))
            for dbtn in delete_btns:
                btn.append(dbtn)
            updated += 1

        # Mettre à jour le texte éditable de l'onglet
        if i < len(tab_contents) and tab_data.get("content"):
            content_div = tab_contents[i]
            ea = content_div.select_one(".editable-area")
            target = ea if ea else content_div
            set_element_text(target, clean_json_text(tab_data["content"]))
            updated += 1

        # Nœud sous-branche dans la Col 4
        sub_node = soup.find(id=f"node-{cat_id}-sub{i}")
        if sub_node and tab_data.get("title"):
            delete_btns = sub_node.select(".delete-btn")
            set_element_text(sub_node, clean_json_text(tab_data["title"]))
            for dbtn in delete_btns:
                sub_node.append(dbtn)
            updated += 1

    return updated


def apply_json_to_html(json_path: Path, html_path: Path) -> None:
    print(f"[JSON] Chargement : {json_path.name}")
    with open(json_path, encoding="utf-8") as f:
        data = json.load(f)

    print(f"[HTML] Lecture : {html_path.name}")
    with open(html_path, encoding="utf-8", errors="ignore") as f:
        soup = BeautifulSoup(f.read(), "html.parser")

    # 1. S'assurer de la présence de tous les conteneurs (génération si absents)
    print("\n--- Analyse & Génération Dynamique ---")
    for pipeline in data.get("pipelines", []):
        pipe_id = pipeline.get("id", "")
        ensure_pipeline_nodes(soup, pipeline)

        for i, cat in enumerate(pipeline.get("categories", [])):
            cat_id = cat.get("id", "")
            ensure_category_nodes(soup, pipe_id, cat, i)

            color_class = get_color_class(cat_id, i)
            for j, tab_data in enumerate(cat.get("tabs", [])):
                ensure_tab_elements(soup, cat_id, color_class, tab_data, j)

    # 1.5. S'assurer que chaque sous-branche (colonne 4) possède le bouton add-sub-btn
    for sub_branch in soup.find_all("div", class_="category-branch"):
        if sub_branch.get("id", "").endswith("-sub-branch"):
            cat_id = sub_branch["id"].replace("-sub-branch", "")
            add_btn = sub_branch.find(class_="add-sub-btn")
            if not add_btn:
                print(f"  [GEN] Ajout du bouton d'ajout d'onglet au conteneur #{sub_branch['id']}")
                add_btn = soup.new_tag("button", attrs={
                    "class": "add-sub-btn",
                    "onclick": f"addTab('card-{cat_id}')",
                    "title": "Ajouter un onglet"
                })
                add_btn.string = "＋"
                sub_branch.append(add_btn)

    # 2. Injecter les données textuelles à jour
    print("\n--- Ingestion des Textes & Mises à Jour ---")
    total_updates = apply_global_notes(soup, data)

    for pipeline in data.get("pipelines", []):
        pipe_id = pipeline.get("id", "?")
        pipe_name = pipeline.get("name", "?")
        print(f"\n  Pipeline [{pipe_id}] {pipe_name}")

        total_updates += apply_pipeline_names(soup, pipeline)

        for i, cat in enumerate(pipeline.get("categories", [])):
            cat_id = cat.get("id", "?")
            cat_name = cat.get("name", "?").split("\n")[0].strip()
            n_tabs = len(cat.get("tabs", []))
            print(f"    -> [{cat_id}] {cat_name} ({n_tabs} onglets)")
            n = apply_category(soup, cat, i)
            total_updates += n
            print(f"       {n} elements mis a jour")

    # Timestamp de mise à jour
    ts = datetime.now().strftime("%Y-%m-%d %H:%M")
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

    print(f"\n[DONE] Synchronisation terminee. {total_updates} elements synchronises.")


def main():
    if not JSON_PATH.exists():
        print(f"[ERREUR] JSON introuvable : {JSON_PATH}")
        sys.exit(1)
    if not HTML_PATH.exists():
        print(f"[ERREUR] HTML introuvable : {HTML_PATH}")
        sys.exit(1)

    apply_json_to_html(JSON_PATH, HTML_PATH)


if __name__ == "__main__":
    main()
