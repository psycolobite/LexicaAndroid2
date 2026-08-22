package com.example.lexicaandroid2.presentation.wordlist

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewCardAggregateState
import com.example.lexicaandroid2.domain.model.ReviewCardProgressSummary
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.presentation.common.EditWordIconButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WordDetailUiState(
    val card: Flashcard? = null,
    val progressSummary: ReviewCardProgressSummary? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class WordDetailViewModel(
    private val cardId: String,
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordDetailUiState())
    val uiState: StateFlow<WordDetailUiState> = _uiState.asStateFlow()

    init {
        loadCard()
    }

    private fun loadCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val card = repository.getAllCards().find { it.id == cardId }
            if (card != null) {
                val progressList = repository.getQuestionProgressForCard(cardId)
                val summary = if (progressList.isNotEmpty()) {
                    ReviewCardProgressSummary.fromProgress(
                        cardId = card.id,
                        progress = progressList,
                        now = System.currentTimeMillis()
                    )
                } else {
                    ReviewCardProgressSummary.fromFlashcard(card, System.currentTimeMillis())
                }
                _uiState.update {
                    it.copy(
                        card = card,
                        progressSummary = summary,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Mot non trouvé"
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        val currentCard = _uiState.value.card ?: return
        viewModelScope.launch {
            repository.setFavorite(currentCard.id, !currentCard.favori)
            loadCard()
        }
    }

    fun deleteCard() {
        val currentCard = _uiState.value.card ?: return
        viewModelScope.launch {
            repository.deleteCard(currentCard.id)
        }
    }

    fun refreshCard() {
        loadCard()
    }
}

class WordDetailViewModelFactory(
    private val cardId: String,
    private val repository: FlashcardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WordDetailViewModel(cardId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    cardId: String,
    viewModel: WordDetailViewModel,
    onBack: () -> Unit,
    onEditCard: (Flashcard) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val card = uiState.card
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
                        viewModel.deleteCard()
                        onBack()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(card?.recto ?: "Détail du mot") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(),
                actions = {
                    if (card != null) {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (card.favori) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Favori",
                                tint = if (card.favori) Color(0xFFFFB800) else Color.Gray
                            )
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        EditWordIconButton(onClick = { onEditCard(card) })
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            card != null -> {
                WordDetailContent(
                    card = card,
                    progressSummary = uiState.progressSummary
                        ?: ReviewCardProgressSummary.fromFlashcard(card, System.currentTimeMillis()),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailDialog(
    card: Flashcard,
    progressSummary: ReviewCardProgressSummary,
    onDismiss: () -> Unit,
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = { Text(card.recto, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(),
                    actions = {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (card.favori) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Favori",
                                tint = if (card.favori) Color(0xFFFFB800) else Color.Gray
                            )
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        EditWordIconButton(onClick = onEditCard)
                    }
                )

                WordDetailContent(
                    card = card,
                    progressSummary = progressSummary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    bottomPadding = 8.dp
                )

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Fermer")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordDetailContent(
    card: Flashcard,
    progressSummary: ReviewCardProgressSummary,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 16.dp
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailSection(title = "📖 Définition") {
            Text(
                text = card.verso,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (card.categorieGrammaticale.isNotBlank()) {
            DetailSection(title = "🏷️ Catégorie grammaticale") {
                Text(
                    text = card.categorieGrammaticale,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (card.registre.isNotBlank()) {
            DetailSection(title = "🎭 Registre") {
                Text(
                    text = card.registre,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (card.synonymes.isNotEmpty()) {
            DetailSection(title = "🔄 Synonymes") {
                Text(
                    text = card.synonymes.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (card.exemples.isNotEmpty()) {
            DetailSection(title = "💡 Exemples d'usage") {
                card.exemples.forEach { exemple ->
                    Text(
                        text = "• $exemple",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        if (card.etymologie.isNotBlank()) {
            DetailSection(title = "📜 Étymologie") {
                Text(
                    text = card.etymologie,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (card.notesPersonnelles.isNotBlank()) {
            DetailSection(title = "📝 Notes personnelles") {
                Text(
                    text = card.notesPersonnelles,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        DetailSection(title = "📊 Progression SM-2") {
            Sm2ProgressSection(progressSummary = progressSummary)
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun Sm2ProgressSection(progressSummary: ReviewCardProgressSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Sm2DirectionRow(
            label = "Mot ➔ Définition",
            state = progressSummary.wordToDefinitionState
        )
        Sm2DirectionRow(
            label = "Définition ➔ Mot",
            state = progressSummary.definitionToWordState
        )
    }
}

@Composable
private fun Sm2DirectionRow(
    label: String,
    state: ReviewCardAggregateState
) {
    val (_, color) = state.toLabelAndColor()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Surface(
            color = color.copy(alpha = 0.12f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = state.label,
                color = color,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

private fun ReviewCardAggregateState.toLabelAndColor(): Pair<String, Color> = when (this) {
    ReviewCardAggregateState.TO_WORK -> label to Color(0xFF1E3A5F)
    ReviewCardAggregateState.IN_PROGRESS -> label to Color(0xFFD35400)
    ReviewCardAggregateState.KNOWN -> label to Color(0xFF27AE60)
}
