package com.example.lexicaandroid2.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.lexicaandroid2.domain.model.Flashcard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onFlashcardClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SearchTopBar(
                query = uiState.query,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onClearClick = { viewModel.clearSearch() },
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            // Onglets de filtrage
            SearchTypeTabs(
                selectedType = uiState.searchType,
                onTypeSelected = { viewModel.onSearchTypeChanged(it) }
            )

            // Indicateur de résultats
            if (!uiState.isLoading && uiState.query.isNotBlank()) {
                Text(
                    text = "${uiState.totalResults} résultat(s) trouvé(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Contenu principal
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.error != null -> {
                        ErrorMessage(
                            message = uiState.error!!,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    uiState.results.isEmpty() -> {
                        EmptyState(
                            query = uiState.query,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        SearchResults(
                            results = uiState.results,
                            query = uiState.query,
                            onFlashcardClick = onFlashcardClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Rechercher...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Recherche"
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClearClick) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Effacer"
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retour"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun SearchTypeTabs(
    selectedType: SearchType,
    onTypeSelected: (SearchType) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedType.ordinal,
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 8.dp
    ) {
        SearchType.entries.forEach { type ->
            Tab(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                text = {
                    Text(
                        text = when (type) {
                            SearchType.GLOBAL -> "Global"
                            SearchType.BY_WORD -> "Mots"
                            SearchType.BY_DEFINITION -> "Définitions"
                            SearchType.FAVORITES -> "Favoris"
                        }
                    )
                },
                icon = {
                    if (type == SearchType.FAVORITES) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (selectedType == type) 
                                MaterialTheme.colorScheme.primary 
                            else Color.Gray
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun SearchResults(
    results: List<Flashcard>,
    query: String,
    onFlashcardClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(results, key = { it.id }) { flashcard ->
            FlashcardResultCard(
                flashcard = flashcard,
                query = query,
                onClick = { onFlashcardClick(flashcard.id) }
            )
        }
    }
}

@Composable
fun FlashcardResultCard(
    flashcard: Flashcard,
    query: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = highlightQuery(flashcard.recto, query),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (flashcard.favori) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favori",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = highlightQuery(flashcard.verso, query),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (flashcard.synonymes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Syn: ${flashcard.synonymes.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun highlightQuery(text: String, query: String) = buildAnnotatedString {
    if (query.isBlank()) {
        append(text)
    } else {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var lastIndex = 0
        
        while (true) {
            val index = lowerText.indexOf(lowerQuery, lastIndex)
            if (index == -1) {
                append(text.substring(lastIndex))
                break
            }
            
            append(text.substring(lastIndex, index))
            withStyle(
                style = SpanStyle(
                    background = Color(0xFFFFEB3B),
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(index, index + query.length))
            }
            lastIndex = index + query.length
        }
    }
}

@Composable
fun EmptyState(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (query.isBlank()) {
                "Commencez à taper pour rechercher"
            } else {
                "Aucun résultat pour \"$query\""
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "❌ Erreur",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}
