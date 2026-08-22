package com.example.lexicaandroid2.presentation.addwords

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lexicaandroid2.data.local.WordReserveEntity
import com.example.lexicaandroid2.data.remote.model.WordResult
import com.example.lexicaandroid2.domain.model.Flashcard

private val Purple = Color(0xFF6750A4)
private val PurpleLight = Color(0xFFEDE7F6)

@Composable
fun AddWordsScreen(viewModel: AddWordsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refreshProposedWords()
    }

    // ── AlertDialog doublon ───────────────────────────────────────────────────
    if (uiState.duplicateCandidate != null) {
        DuplicateDialog(
            word = uiState.duplicateCandidate!!.recto,
            onReplace = viewModel::resolveDuplicateReplace,
            onCancel  = viewModel::resolveDuplicateCancel
        )
    }

    // ── Fiche d'aperçu (dialog plein écran) ───────────────────────────────────
    if (uiState.selectedResult != null) {
        PreviewDialog(
            result             = uiState.selectedResult!!,
            definition         = uiState.previewDefinition,
            synonymes          = uiState.previewSynonymes,
            onDefinitionChange = viewModel::onPreviewDefinitionChanged,
            onSynonymesChange  = viewModel::onPreviewSynonymesChanged,
            onConfirm          = viewModel::confirmAddFromPreview,
            onDismiss          = viewModel::closePreview
        )
    }

    // ── Formulaire manuel (dialog plein écran) ────────────────────────────────
    if (uiState.manualMode) {
        ManualAddDialog(
            state              = uiState,
            onWordChanged      = viewModel::onManualWordChanged,
            onDefChanged       = viewModel::onManualDefinitionChanged,
            onSynChanged       = viewModel::onManualSynonymesChanged,
            onCatChanged       = viewModel::onManualCategorieChanged,
            onEtyChanged       = viewModel::onManualEtymologieChanged,
            onExChanged        = viewModel::onManualExemplesChanged,
            onToggleExpanded   = viewModel::toggleManualExpanded,
            onConfirm          = viewModel::confirmManualAdd,
            onDismiss          = viewModel::closeManualMode
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        // ── Barre de recherche ────────────────────────────────────────────────
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

        // ── Message succès ────────────────────────────────────────────────────
        uiState.successMessage?.let { msg ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2E7D32)
            ) {
                Text(
                    text = "✓ $msg",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // ── Contenu principal ─────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Section : résultats locaux (mots déjà en liste)
            if (uiState.localMatches.isNotEmpty()) {
                item {
                    SectionHeader(text = "✓ Déjà dans ta liste")
                }
                items(uiState.localMatches) { card ->
                    LocalMatchItem(card = card)
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
                item {
                    SectionHeader(text = "🌐 Définitions trouvées")
                }
                items(uiState.apiResults) { result ->
                    ApiResultItem(
                        result = result,
                        onAddClick = { viewModel.openPreview(result) }
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

            // Section : mots proposés (quand pas de recherche)
            if (uiState.searchQuery.isBlank()) {
                if (uiState.isLoadingProposed) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (uiState.proposedWords.isNotEmpty()) {
                    item { SectionHeader(text = "💡 Mots suggérés pour toi") }
                    items(uiState.proposedWords) { word ->
                        ReserveWordItem(
                            word = word,
                            onAddClick = { viewModel.addWordFromReserve(word) }
                        )
                    }
                }
            }

            // Espace en bas pour le bouton flottant
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        // ── Bouton "Ajouter manuellement" (toujours visible en bas) ──────────
        Surface(
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
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

// ─── Composables internes ────────────────────────────────────────────────────

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Purple,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun LocalMatchItem(card: Flashcard) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = PurpleLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Purple, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(card.recto, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    card.verso,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text("Déjà ajouté", fontSize = 11.sp, color = Purple, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ApiResultItem(result: WordResult, onAddClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(result.mot, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (result.categorieGrammaticale.isNotBlank()) {
                        Text(
                            text = result.categorieGrammaticale,
                            fontSize = 11.sp,
                            color = Purple,
                            modifier = Modifier
                                .background(PurpleLight, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    result.definition,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
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

@Composable
private fun ReserveWordItem(word: WordReserveEntity, onAddClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(word.mot, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    word.definition,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (word.categorieGrammaticale.isNotBlank()) {
                    Text(
                        word.categorieGrammaticale,
                        fontSize = 11.sp,
                        color = Purple,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Purple)
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
    onDefinitionChange: (String) -> Unit,
    onSynonymesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(result.mot, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (result.categorieGrammaticale.isNotBlank()) {
                    Text(result.categorieGrammaticale, fontSize = 12.sp, color = Purple)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Définition (modifiable)", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(
                    value = definition,
                    onValueChange = onDefinitionChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(8.dp)
                )
                Text("Synonymes (séparés par des virgules)", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(
                    value = synonymes,
                    onValueChange = onSynonymesChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("ex: rapide, vif, alerte") },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("✅ Confirmer l'ajout") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("❌ Annuler") }
        }
    )
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
        confirmButton = {
            Button(onClick = onReplace) { Text("Mettre à jour") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Annuler") }
        }
    )
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
                // Champs obligatoires
                OutlinedTextField(
                    value = state.manualWord,
                    onValueChange = onWordChanged,
                    label = { Text("Mot *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    isError = state.error != null && state.manualWord.isBlank(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = state.manualDefinition,
                    onValueChange = onDefChanged,
                    label = { Text("Définition *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    isError = state.error != null && state.manualDefinition.isBlank(),
                    shape = RoundedCornerShape(8.dp)
                )
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // Accordéon champs optionnels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleExpanded)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Champs optionnels",
                        fontSize = 13.sp,
                        color = Purple,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (state.manualExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Purple
                    )
                }
                AnimatedVisibility(
                    visible = state.manualExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.manualSynonymes,
                            onValueChange = onSynChanged,
                            label = { Text("Synonymes (virgule)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = state.manualCategorie,
                            onValueChange = onCatChanged,
                            label = { Text("Catégorie grammaticale") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("ex: nom masculin") },
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = state.manualEtymologie,
                            onValueChange = onEtyChanged,
                            label = { Text("Étymologie") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = state.manualExemples,
                            onValueChange = onExChanged,
                            label = { Text("Exemples (virgule)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("✅ Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

