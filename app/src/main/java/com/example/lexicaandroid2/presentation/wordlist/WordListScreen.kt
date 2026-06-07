package com.example.lexicaandroid2.presentation.wordlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewCardAggregateState
import com.example.lexicaandroid2.domain.model.ReviewCardProgressSummary
import com.example.lexicaandroid2.presentation.common.EditWordIconButton

@Composable
fun WordListScreen(
    viewModel: WordListViewModel,
    onEditCard: (Flashcard) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCard by remember { mutableStateOf<Flashcard?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadWords()
    }

    LaunchedEffect(uiState.isSelectionMode) {
        if (uiState.isSelectionMode) {
            selectedCard = null
        }
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
            },
            onEditCard = {
                selectedCard = null
                onEditCard(card)
            }
        )
    }

    Scaffold(
        topBar = {
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredCards, key = { it.id }) { card ->
                    WordItem(
                        card = card,
                        progressSummary = uiState.progressByCardId[card.id]
                            ?: ReviewCardProgressSummary.fromFlashcard(card, System.currentTimeMillis()),
                        isSelected = card.id in uiState.selectedCardIds,
                        onCardClick = {
                            if (uiState.isSelectionMode) {
                                viewModel.toggleCardSelection(card.id)
                            } else {
                                selectedCard = card
                            }
                        },
                        onCardLongClick = {
                            viewModel.toggleCardSelection(card.id)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(card) },
                        onDeleteCard = { viewModel.deleteCard(card.id) },
                        onEditCard = { onEditCard(card) }
                    )
                }

                if (
                    uiState.searchQuery.isNotBlank() &&
                    uiState.filteredCards.isEmpty()
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


@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun WordItem(
    card: Flashcard,
    progressSummary: ReviewCardProgressSummary,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onCardLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteCard: () -> Unit,
    onEditCard: () -> Unit
) {
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
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = onCardClick,
                    onLongClick = onCardLongClick
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = card.recto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = card.verso,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Gray
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuestionStateChip(
                        label = "la définition",
                        state = progressSummary.wordToDefinitionState
                    )
                    QuestionStateChip(
                        label = "le mot",
                        state = progressSummary.definitionToWordState
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Sélectionné",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .size(18.dp)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
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
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }

                EditWordIconButton(onClick = onEditCard)
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

