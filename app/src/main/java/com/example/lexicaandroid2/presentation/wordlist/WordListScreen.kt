package com.example.lexicaandroid2.presentation.wordlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lexicaandroid2.data.remote.model.WordResult
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewCardAggregateState
import com.example.lexicaandroid2.domain.model.ReviewCardProgressSummary

@Composable
fun WordListScreen(
    viewModel: WordListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCard by remember { mutableStateOf<Flashcard?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadWords()
    }

    selectedCard?.let { card ->
        WordDetailDialog(
            card = card,
            onDismiss = { selectedCard = null },
            onToggleFavorite = {
                viewModel.toggleFavorite(card)
                selectedCard = card.copy(favori = !card.favori)
            },
            onDeleteCard = {
                viewModel.deleteCard(card.id)
                selectedCard = null
            }
        )
    }

    uiState.apiPreviewResult?.let { result ->
        ExternalWordPreviewDialog(
            result = result,
            onDismiss = viewModel::closeApiPreview,
            onConfirmAdd = { viewModel.addWordFromApi(result) }
        )
    }

    Scaffold(
        topBar = {
             // Search within MY words (filtering)
             SearchBar(
                 query = uiState.searchQuery,
                 onQueryChange = viewModel::onSearchQueryChanged,
                 onClearClick = { viewModel.onSearchQueryChanged("") }
             )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFAFAFA)) // Updated background to match Dashboard
        ) {
            uiState.successMessage?.let { message ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "✓ $message",
                        color = Color(0xFF1B5E20),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredCards) { card ->
                    WordItem(
                        card = card,
                        progressSummary = uiState.progressByCardId[card.id]
                            ?: ReviewCardProgressSummary.fromFlashcard(card, System.currentTimeMillis()),
                        onCardClick = { selectedCard = card },
                        onToggleFavorite = { viewModel.toggleFavorite(card) },
                        onDeleteCard = { viewModel.deleteCard(card.id) }
                    )
                }

                if (uiState.searchQuery.isNotBlank() && uiState.isApiLoading) {
                    item {
                        ExternalSearchLoadingItem()
                    }
                }

                if (uiState.apiSearchResults.isNotEmpty()) {
                    item {
                        Text(
                            text = "🌐 Résultats proposés depuis la base de recherche",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(uiState.apiSearchResults) { result ->
                        ApiSearchResultItem(
                            result = result,
                            onPreviewClick = { viewModel.openApiPreview(result) }
                        )
                    }
                }

                uiState.apiError?.let { error ->
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                color = Color(0xFF8D6E63),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                if (
                    uiState.searchQuery.isNotBlank() &&
                    uiState.filteredCards.isEmpty() &&
                    !uiState.isApiLoading &&
                    uiState.apiSearchResults.isEmpty() &&
                    uiState.apiError == null
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Aucun mot trouvé dans ta collection pour cette recherche.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Rechercher un mot...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = onClearClick) {
                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                    }
                }
            },
            singleLine = true
        )
    }
}

@Composable
private fun ExternalSearchLoadingItem() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Text(
                text = "Recherche dans la base de recherche…",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ApiSearchResultItem(
    result: WordResult,
    onPreviewClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.mot,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (result.categorieGrammaticale.isNotBlank()) {
                        Text(
                            text = result.categorieGrammaticale,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                OutlinedButton(onClick = onPreviewClick) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Proposer la carte")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (result.source.isNotBlank()) {
                Text(
                    text = "Source : ${result.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ExternalWordPreviewDialog(
    result: WordResult,
    onDismiss: () -> Unit,
    onConfirmAdd: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(result.mot, fontWeight = FontWeight.Bold)
                if (result.categorieGrammaticale.isNotBlank()) {
                    Text(
                        text = result.categorieGrammaticale,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (result.source.isNotBlank()) {
                    Text(
                        text = "Source : ${result.source}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PreviewSection(title = "Définition", content = result.definition)

                if (result.exemples.isNotEmpty()) {
                    PreviewSection(
                        title = "Exemples",
                        content = result.exemples.joinToString("\n") { "• $it" }
                    )
                }

                if (result.synonymes.isNotEmpty()) {
                    PreviewSection(
                        title = "Synonymes",
                        content = result.synonymes.joinToString(", ")
                    )
                }

                Text(
                    text = "Cette carte sera ajoutée automatiquement avec les informations trouvées, sans saisie manuelle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirmAdd) {
                Text("Ajouter à mes mots")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
private fun PreviewSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
    }
}

@Composable
fun WordItem(
    card: Flashcard,
    progressSummary: ReviewCardProgressSummary,
    onCardClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteCard: () -> Unit
) {
    val (stateText, stateColor) = progressSummary.aggregateState.toLabelAndColor()

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer ce mot ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteCard()
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable { onCardClick() }
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.recto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = card.verso,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuestionStateChip(
                        label = "Mot → Définition",
                        state = progressSummary.wordToDefinitionState
                    )
                    QuestionStateChip(
                        label = "Définition → Mot",
                        state = progressSummary.definitionToWordState
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = stateColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = stateText,
                        color = stateColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (card.favori) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Favori",
                        tint = if (card.favori) Color(0xFFFFB800) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionStateChip(
    label: String,
    state: ReviewCardAggregateState
) {
    val (_, color) = state.toLabelAndColor()
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "$label • ${state.label}",
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun ReviewCardAggregateState.toLabelAndColor(): Pair<String, Color> = when (this) {
    ReviewCardAggregateState.TO_WORK -> label to Color(0xFF1E3A5F)
    ReviewCardAggregateState.IN_PROGRESS -> label to Color(0xFFD35400)
    ReviewCardAggregateState.KNOWN -> label to Color(0xFF27AE60)
}

