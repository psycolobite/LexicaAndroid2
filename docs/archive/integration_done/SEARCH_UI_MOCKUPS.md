# 📱 Interface du Module de Recherche

## 🎨 Design Material3

Le module de recherche utilise Material3 avec une interface fluide et moderne.

## 📸 Aperçu des écrans

### 1️⃣ Écran initial (recherche vide)

```
┌─────────────────────────────────────────────┐
│ ← [🔍 Rechercher...]              [×]       │ TopAppBar
├─────────────────────────────────────────────┤
│ [Global] [Mots] [Définitions] [⭐ Favoris]  │ Tabs
├─────────────────────────────────────────────┤
│                                             │
│                                             │
│              🔍                              │
│                                             │
│        Commencez à taper                    │
│         pour rechercher                     │
│                                             │
│                                             │
└─────────────────────────────────────────────┘
```

**Caractéristiques:**
- Barre de recherche active
- 4 onglets visibles
- Message d'accueil centré
- Icône de recherche grise

---

### 2️⃣ Recherche en cours (loading)

```
┌─────────────────────────────────────────────┐
│ ← [🔍 test...]                    [×]       │
├─────────────────────────────────────────────┤
│ [Global] [Mots] [Définitions] [⭐ Favoris]  │
├─────────────────────────────────────────────┤
│                                             │
│                                             │
│                  ⏳                          │
│            Chargement...                    │
│                                             │
│                                             │
└─────────────────────────────────────────────┘
```

**Caractéristiques:**
- CircularProgressIndicator centré
- Query visible dans la barre
- Onglet "Global" sélectionné

---

### 3️⃣ Résultats de recherche (avec highlighting)

```
┌─────────────────────────────────────────────┐
│ ← [🔍 test...]                    [×]       │
├─────────────────────────────────────────────┤
│ [Global] [Mots] [Définitions] [⭐ Favoris]  │
├─────────────────────────────────────────────┤
│ 24 résultat(s) trouvé(s)                    │
├─────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ │
│ │ ⚡test                                   │ │
│ │ Épreuve, vérification. Essai permettant │ │
│ │ de vérifier quelque chose...            │ │
│ │ Syn: épreuve, essai, examen             │ │
│ └─────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────┐ │
│ │ ⚡tester                              ❤️ │ │
│ │ Soumettre à un test, à une épreuve...  │ │
│ │ Syn: éprouver, essayer, vérifier        │ │
│ └─────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────┐ │
│ │ ⚡testament                              │ │
│ │ Acte par lequel une personne dispose   │ │
│ │ de ses biens pour après sa mort...      │ │
│ │ Syn: volonté, legs                      │ │
│ └─────────────────────────────────────────┘ │
│ ...                                         │
└─────────────────────────────────────────────┘
```

**Caractéristiques:**
- Compteur "24 résultat(s)"
- Cards avec ombre (elevation)
- Terme "test" surligné en 🟡 JAUNE
- Icône ❤️ pour favoris
- Synonymes en petit texte gris
- Scroll infini (LazyColumn)

---

### 4️⃣ Onglet "Mots" sélectionné

```
┌─────────────────────────────────────────────┐
│ ← [🔍 amour...]                   [×]       │
├─────────────────────────────────────────────┤
│ [Global] 【Mots】 [Définitions] [⭐ Favoris] │ ← Onglet bleu
├─────────────────────────────────────────────┤
│ 3 résultat(s) trouvé(s)                     │
├─────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ │
│ │ ⚡amour                                  │ │
│ │ Sentiment d'affection intense...        │ │
│ └─────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────┐ │
│ │ ⚡amoureux                               │ │
│ │ Qui aime quelqu'un d'un amour passionné │ │
│ └─────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────┐ │
│ │ ⚡amoureuse                              │ │
│ │ Féminin de amoureux                     │ │
│ └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
```

**Caractéristiques:**
- Onglet "Mots" actif (bleu)
- Recherche uniquement dans la colonne `mot`
- Highlighting sur mots seulement

---

### 5️⃣ Onglet "Favoris" avec filtre

```
┌─────────────────────────────────────────────┐
│ ← [🔍 vie...]                     [×]       │
├─────────────────────────────────────────────┤
│ [Global] [Mots] [Définitions] 【⭐ Favoris】│
├─────────────────────────────────────────────┤
│ 5 résultat(s) trouvé(s)                     │
├─────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────┐ │
│ │ ⚡vie                                 ❤️ │ │
│ │ Ensemble des phénomènes qui caractéri-  │ │
│ │ sent les êtres vivants...               │ │
│ │ Syn: existence, survie                  │ │
│ └─────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────┐ │
│ │ ⚡vivre                               ❤️ │ │
│ │ Être en vie, exister...                 │ │
│ │ Syn: exister, subsister                 │ │
│ └─────────────────────────────────────────┘ │
│ ...                                         │
└─────────────────────────────────────────────┘
```

**Caractéristiques:**
- Onglet "Favoris" actif avec icône ⭐
- Toutes les cartes ont l'icône ❤️
- Filtre `favori = 1` appliqué

---

### 6️⃣ Aucun résultat

```
┌─────────────────────────────────────────────┐
│ ← [🔍 xyz123...]                  [×]       │
├─────────────────────────────────────────────┤
│ [Global] [Mots] [Définitions] [⭐ Favoris]  │
├─────────────────────────────────────────────┤
│ 0 résultat(s) trouvé(s)                     │
├─────────────────────────────────────────────┤
│                                             │
│                                             │
│              🔍                              │
│                                             │
│        Aucun résultat pour                  │
│            "xyz123"                         │
│                                             │
│                                             │
└─────────────────────────────────────────────┘
```

**Caractéristiques:**
- Compteur à 0
- Message "Aucun résultat"
- Icône de recherche grise
- Query affichée entre guillemets

---

### 7️⃣ Erreur réseau/base de données

```
┌─────────────────────────────────────────────┐
│ ← [🔍 test...]                    [×]       │
├─────────────────────────────────────────────┤
│ [Global] [Mots] [Définitions] [⭐ Favoris]  │
├─────────────────────────────────────────────┤
│                                             │
│                                             │
│              ❌ Erreur                       │
│                                             │
│    Erreur lors de la recherche              │
│    Network error                            │
│                                             │
│                                             │
└─────────────────────────────────────────────┘
```

**Caractéristiques:**
- Emoji ❌
- Message d'erreur centré
- Erreur technique affichée

---

## 🎨 Code couleurs

### Couleurs utilisées

| Élément | Couleur | Code |
|---------|---------|------|
| Highlighting | 🟡 Jaune | `#FFEB3B` |
| Favori | ❤️ Rouge | `MaterialTheme` |
| Primary | 🔵 Bleu | `MaterialTheme.colorScheme.primary` |
| Background | ⚪ Off-white | `#FAFAFA` |
| Card | ⚪ Blanc | `#FFFFFF` |
| Text secondaire | 🔘 Gris | `Color.Gray` |
| Synonymes | 🔘 Gris foncé | `#666666` |

### Typographie

| Élément | Style |
|---------|-------|
| Mot (titre) | `titleMedium` + `FontWeight.Bold` |
| Définition | `bodyMedium` |
| Synonymes | `bodySmall` |
| Compteur | `bodySmall` + `Color.Gray` |
| Messages | `bodyLarge` |

---

## 📐 Dimensions

```
┌─ Padding écran: 16dp ──────────────────────────┐
│                                                 │
│ ┌─ Card spacing: 8dp ────────────────────────┐ │
│ │                                             │ │
│ │ ┌─ Card padding: 16dp ──────────────────┐  │ │
│ │ │ Mot                                    │  │ │
│ │ │ Définition                             │  │ │
│ │ │ Spacer: 4dp                            │  │ │
│ │ │ Synonymes                              │  │ │
│ │ └────────────────────────────────────────┘  │ │
│ │                                             │ │
│ │ Card elevation: 2dp                         │ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## 🎬 Animations

### 1. Navigation transitions
```kotlin
enterTransition = slideIntoContainer(SlideDirection.Left, tween(300))
exitTransition = slideOutOfContainer(SlideDirection.Right, tween(300))
```

**Effet:** Slide de gauche à droite (300ms)

### 2. Tab transitions
- Indicateur bleu qui glisse sous l'onglet actif
- Transition automatique Material3

### 3. LazyColumn items
- Apparition progressive lors du scroll
- Pas d'animation spéciale (performance)

---

## 🔍 Highlighting (Détail)

### Exemple de highlighting

**Query:** "est"

**Texte original:** "testament"

**Résultat:**
```
t【est】ament
  ↑ highlighting jaune
```

**Code:**
```kotlin
withStyle(
    style = SpanStyle(
        background = Color(0xFFFFEB3B),  // Jaune
        fontWeight = FontWeight.Bold
    )
) {
    append("est")
}
```

---

## 📱 Responsive Design

### Orientations supportées

- ✅ Portrait (principal)
- ✅ Landscape (scroll horizontal tabs si nécessaire)

### Tailles d'écran

- ✅ Phone (small): 360dp+
- ✅ Phone (medium): 411dp+
- ✅ Tablet: 600dp+

### Adaptations

| Écran | Adaptation |
|-------|-----------|
| Petit | ScrollableTabRow (défilement horizontal) |
| Grand | Tous les tabs visibles |
| Tablet | Colonnes multiples possibles (future) |

---

## 🎯 Points d'attention UX

### ✅ Ce qui est bien

1. **Debounce 300ms** - Pas de spam de requêtes
2. **Highlighting** - Résultats clairs
3. **Compteur** - Transparence sur nombre de résultats
4. **États explicites** - Loading, Empty, Error clairs
5. **Favoris** - Visible immédiatement (❤️)
6. **Synonymes** - Information enrichie

### 🔄 Améliorations possibles (futures)

1. **Historique de recherche** - Suggestions
2. **Filtres avancés** - Par catégorie grammaticale, registre
3. **Tri personnalisé** - Par date, alphabétique, etc.
4. **Actions rapides** - Ajouter aux favoris depuis la liste
5. **Recherche vocale** - Intégration Speech-to-Text
6. **Export** - Partager les résultats

---

## 🧪 Test visuel checklist

- [ ] TopAppBar affichée correctement
- [ ] TextField réactif au touch
- [ ] Clear button (×) fonctionne
- [ ] Tabs défilent horizontalement sur petit écran
- [ ] Highlighting jaune visible
- [ ] Icône favori (❤️) affichée
- [ ] Synonymes en texte plus petit
- [ ] Cards ont ombre (elevation)
- [ ] Scroll fluide dans LazyColumn
- [ ] État loading affiche CircularProgressIndicator
- [ ] État empty affiche message
- [ ] État error affiche message rouge
- [ ] Animations de navigation smooth (300ms)
- [ ] Compteur de résultats affiché
- [ ] Bouton retour fonctionne

---

**Design:** Material3  
**Framework:** Jetpack Compose  
**Animations:** 300ms transitions  
**Colors:** Material Theme + Custom (Highlighting)
