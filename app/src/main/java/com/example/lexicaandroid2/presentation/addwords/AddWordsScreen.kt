package com.example.lexicaandroid2.presentation.addwords

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.lexicaandroid2.data.local.WordReserveEntity
import com.example.lexicaandroid2.data.remote.model.WordResult
import com.example.lexicaandroid2.domain.model.Flashcard

private val Purple      = Color(0xFF6750A4)
private val PurpleLight = Color(0xFFEDE7F6)
private val GreenAdded  = Color(0xFFE8F5E9)   // fond vert clair pour les mots ajoutés
private val GreenDark   = Color(0xFF2E7D32)

@Composable
fun AddWordsScreen(viewModel: AddWordsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedLocalCard by remember { mutableStateOf<Flashcard?>(null) }

    // Recharge la liste + reset session à chaque ouverture de l'écran
    LaunchedEffect(Unit) {
        viewModel.onScreenEntered()
    }

    // ── AlertDialog doublon ───────────────────────────────────────────────────
    if (uiState.duplicateCandidate != null) {
        DuplicateDialog(
            word = uiState.duplicateCandidate!!.recto,
            onReplace = viewModel::resolveDuplicateReplace,
            onCancel = viewModel::resolveDuplicateCancel
        )
    }

        // ── Fiche d'aperçu ────────────────────────────────────────────────────────
    uiState.selectedResult?.let { result ->
        val previewKey   = result.mot.trim()
        val previewAdded = uiState.addedInSession[previewKey]
        PreviewDialog(
            result = result,
            definition = uiState.previewDefinition,
            synonymes = uiState.previewSynonymes,
            isAdded = previewAdded != null,
            isFavorite = previewAdded?.favori == true,
            onDefinitionChanged = viewModel::onPreviewDefinitionChanged,
            onSynonymesChanged = viewModel::onPreviewSynonymesChanged,
            onConfirm = viewModel::confirmAddFromPreview,
            onDismiss = viewModel::closePreview,
            onDeleteClick = { viewModel.deleteAddedWord(previewKey) },
            onFavoriteClick = { viewModel.toggleFavoriteAddedWord(previewKey) }
        )
    }

    // ── Détails mot existant ──────────────────────────────────────────────────
    selectedLocalCard?.let { card ->
        ExistingWordInfoDialog(card = card, onDismiss = { selectedLocalCard = null })
    }

    // ── Formulaire manuel ─────────────────────────────────────────────────────
    if (uiState.manualMode) {
        ManualAddDialog(
            state = uiState,
            onWordChanged = viewModel::onManualWordChanged,
            onDefChanged = viewModel::onManualDefinitionChanged,
            onSynChanged = viewModel::onManualSynonymesChanged,
            onCatChanged = viewModel::onManualCategorieChanged,
            onEtyChanged = viewModel::onManualEtymologieChanged,
            onExChanged = viewModel::onManualExemplesChanged,
            onToggleExpanded = viewModel::toggleManualExpanded,
            onConfirm = viewModel::confirmManualAdd,
            onDismiss = viewModel::closeManualMode
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // ── Barre de recherche ─────────────────────────────────────────────────
        Surface(shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder = { Text("Chercher un mot à ajouter...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearSearch) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    capitalization = KeyboardCapitalization.None
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }


        // ── Contenu principal ──────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Section : résultats locaux (mots déjà en liste)
            if (uiState.localMatches.isNotEmpty()) {
                item { SectionHeader(text = "✓ Déjà dans ta liste") }
                items(uiState.localMatches) { card ->
                    LocalMatchItem(card = card, onClick = { selectedLocalCard = card })
                }
            }

            // Section : résultats API
            if (uiState.isApiLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recherche en cours...", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            if (uiState.apiResults.isNotEmpty()) {
                item { SectionHeader(text = "🌐 Définitions trouvées") }
                items(uiState.apiResults) { result ->
                    val key = result.mot.trim()
                    val added = uiState.addedInSession[key]
                    WordCandidateItem(
                        mot = result.mot,
                        definition = result.definition,
                        categorieGrammaticale = result.categorieGrammaticale,
                        isAdded = added != null,
                        isFavorite = added?.favori == true,
                        onRowClick = { viewModel.openPreview(result) },
                        onAddClick = { viewModel.addWordResult(result) },
                        onDeleteClick = { viewModel.deleteAddedWord(key) },
                        onFavoriteClick = { viewModel.toggleFavoriteAddedWord(key) }
                    )
                }
            }

            // Message d'erreur / info
            uiState.error?.let { err ->
                item {
                    Text(
                        text = err,
                        color = Color(0xFF795548),
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }
            }

            // Section : mots suggérés (réserve locale)
            if (uiState.searchQuery.isBlank()) {
                if (uiState.isLoadingProposed) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                } else if (uiState.proposedWords.isNotEmpty()) {
                    item { SectionHeader(text = "💡 Mots suggérés pour toi") }
                    items(uiState.proposedWords) { word ->
                        val key   = word.mot.trim()   // même clé que doAddWord / addWordFromReserve
                        val added = uiState.addedInSession[key]
                        WordCandidateItem(
                            mot = word.mot,
                            definition = word.definition,
                            categorieGrammaticale = word.categorieGrammaticale,
                            isAdded = added != null,
                            isFavorite = added?.favori == true,
                            onRowClick = { viewModel.openPreview(word.toWordResult()) },
                            onAddClick = { viewModel.addWordFromReserve(word) },
                            onDeleteClick = { viewModel.deleteAddedWord(key) },
                            onFavoriteClick = { viewModel.toggleFavoriteAddedWord(key) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        // ── Bouton ajouter manuellement ────────────────────────────────────────
        Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
            Button(
                onClick = viewModel::openManualMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajouter manuellement")
            }
        }
    }
}

// ─── Composant unifié : ligne de mot candidat ─────────────────────────────────

/**
 * Présentation unique pour tous les mots proposables (réserve locale ET résultats API).
 *
 * État normal  : fond blanc, bouton "+ Ajouter" à droite.
 * État ajouté  : fond vert clair, icône ⭐ (favori) + 🗑️ (supprimer) à droite.
 * Clic sur la ligne : ouvre la fiche d'aperçu (uniquement si pas encore ajouté).
 */
@Composable
private fun WordCandidateItem(
    mot: String,
    definition: String,
    categorieGrammaticale: String,
    isAdded: Boolean,
    isFavorite: Boolean,
    onRowClick: () -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isAdded) GreenAdded else Color.White,
        shadowElevation = if (isAdded) 0.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRowClick)   // toujours cliquable (aperçu même si ajouté)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Texte (mot + définition + catégorie) ──────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(mot, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (isAdded) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GreenDark,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Text(
                    definition,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (categorieGrammaticale.isNotBlank()) {
                    Text(
                        categorieGrammaticale,
                        fontSize = 11.sp,
                        color = Purple,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // ── Actions ───────────────────────────────────────────────────────
            if (isAdded) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                            tint = if (isFavorite) Color(0xFFFFB800) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                FilledTonalButton(
                    onClick = onAddClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter", fontSize = 12.sp)
                }
            }
        }
    }
}

// ─── Dialog : Détails mot existant ───────────────────────────────────────────

@Composable
private fun ExistingWordInfoDialog(card: Flashcard, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(max = 560.dp)
        ) {
            Column {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(card.recto, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (card.categorieGrammaticale.isNotBlank()) {
                        Text(card.categorieGrammaticale, fontSize = 12.sp, color = Purple)
                    }
                    Text("Déjà dans ta liste", fontSize = 12.sp, color = Purple)
                }
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PreviewInfoSection(title = "Définition", content = card.verso)
                    if (card.exemples.isNotEmpty()) {
                        PreviewInfoSection(
                            title = "Exemples",
                            content = card.exemples.joinToString("\n") { "• $it" }
                        )
                    }
                    if (card.synonymes.isNotEmpty()) {
                        PreviewInfoSection(title = "Synonymes", content = card.synonymes.joinToString(", "))
                    }
                    if (card.etymologie.isNotBlank()) {
                        PreviewInfoSection(title = "Étymologie", content = card.etymologie)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) { Text("Fermer") }
                }
            }
        }
    }
}

// ─── Dialog : Fiche d'aperçu ─────────────────────────────────────────────────

@Composable
private fun PreviewDialog(
    result: WordResult,
    definition: String,
    synonymes: String,
    isAdded: Boolean,
    isFavorite: Boolean,
    onDefinitionChanged: (String) -> Unit,
    onSynonymesChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(max = 560.dp)
        ) {
            Column {
                // ── En-tête ───────────────────────────────────────────────────
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(result.mot, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                            modifier = Modifier.weight(1f))
                        if (isAdded) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null,
                                tint = GreenDark, modifier = Modifier.size(18.dp))
                        }
                    }
                    if (result.categorieGrammaticale.isNotBlank()) {
                        Text(result.categorieGrammaticale, fontSize = 12.sp, color = Purple)
                    }
                    if (result.source.isNotBlank()) {
                        Text("Source : ${result.source}", fontSize = 12.sp, color = Color.Gray)
                    }
                    if (isAdded) {
                        Text("Dans ta liste", fontSize = 12.sp, color = GreenDark,
                            fontWeight = FontWeight.Medium)
                    }
                }

                // ── Corps ─────────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PreviewInfoSection(title = "Définition", content = definition)
                    if (result.exemples.isNotEmpty()) {
                        PreviewInfoSection(
                            title = "Exemples",
                            content = result.exemples.joinToString("\n") { "• $it" }
                        )
                    }
                    if (synonymes.isNotBlank()) {
                        PreviewInfoSection(title = "Synonymes", content = synonymes)
                    }
                    if (!isAdded) {
                        Text("Appuie sur Ajouter pour enregistrer ce mot.",
                            fontSize = 12.sp, color = Color.Gray)
                    }
                }

                // ── Boutons ───────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAdded) {
                        // Mot déjà dans la collection : Fermer + Favori + Supprimer
                        TextButton(onClick = onDismiss) { Text("Fermer") }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onFavoriteClick, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                                tint = if (isFavorite) Color(0xFFFFB800) else Color.Gray
                            )
                        }
                        IconButton(
                            onClick = { onDeleteClick(); onDismiss() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        // Mot pas encore ajouté : Annuler + Ajouter
                        TextButton(onClick = onDismiss) { Text("Annuler") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onConfirm) { Text("Ajouter") }
                    }
                }
            }
        }
    }
}

// ─── Dialog : Ajout manuel ───────────────────────────────────────────────────

@Composable
private fun ManualAddDialog(
    state: AddWordsUiState,
    onWordChanged: (String) -> Unit,
    onDefChanged: (String) -> Unit,
    onSynChanged: (String) -> Unit,
    onCatChanged: (String) -> Unit,
    onEtyChanged: (String) -> Unit,
    onExChanged: (String) -> Unit,
    onToggleExpanded: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un mot manuellement") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state.manualWord, onValueChange = onWordChanged,
                    label = { Text("Mot *") }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    isError = state.error != null && state.manualWord.isBlank(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = state.manualDefinition, onValueChange = onDefChanged,
                    label = { Text("Définition *") }, modifier = Modifier.fillMaxWidth(),
                    minLines = 3, maxLines = 6,
                    isError = state.error != null && state.manualDefinition.isBlank(),
                    shape = RoundedCornerShape(8.dp)
                )
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

                // Accordéon champs optionnels
                Row(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpanded).padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Champs optionnels", fontSize = 13.sp, color = Purple, fontWeight = FontWeight.Medium)
                    Icon(
                        imageVector = if (state.manualExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null, tint = Purple
                    )
                }
                AnimatedVisibility(visible = state.manualExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = state.manualSynonymes, onValueChange = onSynChanged,
                            label = { Text("Synonymes (virgule)") }, modifier = Modifier.fillMaxWidth(),
                            singleLine = true, shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = state.manualCategorie, onValueChange = onCatChanged,
                            label = { Text("Catégorie grammaticale") }, modifier = Modifier.fillMaxWidth(),
                            singleLine = true, placeholder = { Text("ex: nom masculin") }, shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = state.manualEtymologie, onValueChange = onEtyChanged,
                            label = { Text("Étymologie") }, modifier = Modifier.fillMaxWidth(),
                            singleLine = true, shape = RoundedCornerShape(8.dp))
                        OutlinedTextField(value = state.manualExemples, onValueChange = onExChanged,
                            label = { Text("Exemples (virgule)") }, modifier = Modifier.fillMaxWidth(),
                            minLines = 2, maxLines = 4, shape = RoundedCornerShape(8.dp))
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("✅ Ajouter") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// ─── Section existant (déjà dans la collection) ───────────────────────────────

@Composable
private fun LocalMatchItem(card: Flashcard, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = PurpleLight,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Purple, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(card.recto, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(card.verso, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("Déjà ajouté", fontSize = 11.sp, color = Purple, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Helpers UI ──────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(text: String) {
    Text(text = text, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Purple,
        modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun PreviewInfoSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Purple)
        Text(content, fontSize = 14.sp, color = Color.DarkGray)
    }
}

// ─── Dialog : Doublon ────────────────────────────────────────────────────────

@Composable
private fun DuplicateDialog(
    word: String,
    onReplace: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF57C00)) },
        title = { Text("Mot déjà dans ta liste") },
        text = { Text("\"$word\" est déjà dans ta liste. Veux-tu mettre à jour sa définition ?") },
        confirmButton = { Button(onClick = onReplace) { Text("Mettre à jour") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Annuler") } }
    )
}

// ─── Extension ───────────────────────────────────────────────────────────────

private fun WordReserveEntity.toWordResult() = WordResult(
    mot = mot, definition = definition, categorieGrammaticale = categorieGrammaticale,
    exemples = exemples, synonymes = synonymes,
    source = if (fromApi) "Réserve API" else "Réserve locale"
)
