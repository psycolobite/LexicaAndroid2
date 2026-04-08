package com.example.lexicaandroid2.presentation.wordlist

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewCardAggregateState
import com.example.lexicaandroid2.domain.model.ReviewCardProgressSummary
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WordDetailUiState(
    val card: Flashcard? = null,
    val progressSummary: ReviewCardProgressSummary? = null,
    val isLoading: Boolean = false,
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
            _uiState.update { it.copy(isLoading = true) }
            try {
                val allCards = repository.getAllCards()
                val card = allCards.find { it.id == cardId }
                val progressSummary = card?.let {
                    val questionProgress = repository.getQuestionProgressForCard(it.id)
                    questionProgress.takeIf { progress -> progress.isNotEmpty() }
                        ?.let { progress ->
                            ReviewCardProgressSummary.fromProgress(
                                cardId = it.id,
                                progress = progress,
                                now = System.currentTimeMillis()
                            )
                        }
                        ?: ReviewCardProgressSummary.fromFlashcard(it, System.currentTimeMillis())
                }
                _uiState.update {
                    it.copy(
                        card = card,
                        progressSummary = progressSummary,
                        isLoading = false,
                        error = if (card == null) "Mot non trouvé" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
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
            // Pas de reload après suppression, le composable gérera la navigation
        }
    }
}

class WordDetailViewModelFactory(
    private val cardId: String,
    private val repository: FlashcardRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WordDetailViewModel(cardId, repository) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun WordDetailScreen(
    cardId: String,
    viewModel: WordDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val card = uiState.card
    val progressSummary = uiState.progressSummary
    val showDeleteConfirm = remember { mutableStateOf(false) }
    var isDeleted by remember { mutableStateOf(false) }

    if (isDeleted) {
        onBack()
        return
    }

    if (showDeleteConfirm.value) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm.value = false },
            title = { Text("Supprimer ce mot ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm.value = false
                        viewModel.deleteCard()
                        isDeleted = true
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirm.value = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (card != null) {
                        Text(card.recto, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Détail du mot")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                modifier = Modifier.height(40.dp),
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
                        IconButton(onClick = { showDeleteConfirm.value = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (card == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Mot non trouvé")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFAFAFA))
        ) {
            item {
                WordDetailContent(
                    card = card,
                    progressSummary = progressSummary
                        ?: ReviewCardProgressSummary.fromFlashcard(card, System.currentTimeMillis())
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailDialog(
    card: Flashcard,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteCard: () -> Unit
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
                        onDismiss()
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
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = { Text(card.recto, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.height(40.dp),
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
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .background(Color(0xFFFAFAFA))
                ) {
                    item {
                        WordDetailContent(
                            card = card,
                            progressSummary = ReviewCardProgressSummary.fromFlashcard(
                                card,
                                System.currentTimeMillis()
                            )
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Fermer")
                }
            }
        }
    }
}

@Composable
private fun WordDetailContent(
    card: Flashcard,
    progressSummary: ReviewCardProgressSummary
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
    ) {
        if (card.categorieGrammaticale.isNotBlank() || card.registre.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (card.categorieGrammaticale.isNotBlank()) {
                    Surface(
                        color = Color(0xFF6750A4).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = card.categorieGrammaticale,
                            color = Color(0xFF6750A4),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                if (card.registre.isNotBlank()) {
                    Surface(
                        color = Color(0xFF6750A4).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = card.registre,
                            color = Color(0xFF6750A4),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        if (card.verso.isNotBlank()) {
            DetailSection(title = "📖 Définition", content = card.verso)
        }

        if (card.exemples.isNotEmpty()) {
            DetailSection(
                title = "💡 Exemples d'usage",
                content = card.exemples.joinToString("\n") { "• $it" }
            )
        }

        if (card.synonymes.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "🔗 Synonymes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    card.synonymes.forEach { synonym ->
                        Surface(
                            color = Color(0xFF6750A4).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = synonym,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        if (card.etymologie.isNotBlank()) {
            DetailSection(title = "🌿 Étymologie", content = card.etymologie)
        }

        ProgressionSection(card = card, progressSummary = progressSummary)
    }
}

@Composable
fun DetailSection(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun ProgressionSection(
    card: Flashcard,
    progressSummary: ReviewCardProgressSummary
) {
    val (stateText, stateColor) = progressSummary.aggregateState.toLabelAndColor()
    val isNew = progressSummary.aggregateState == ReviewCardAggregateState.TO_WORK &&
        card.sm2MotVersDef.totalReviews == 0 &&
        card.sm2DefVersMot.totalReviews == 0

    val nextReviewDays = if (card.sm2MotVersDef.nextReviewDate > 0) {
        ((card.sm2MotVersDef.nextReviewDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    } else {
        0
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "📊 Progression",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = stateColor.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("État :", style = MaterialTheme.typography.bodySmall)
                    Text(
                        stateText,
                        color = stateColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                FaceProgressRow(
                    title = "Mot → Définition",
                    state = progressSummary.wordToDefinitionState
                )

                FaceProgressRow(
                    title = "Définition → Mot",
                    state = progressSummary.definitionToWordState
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Révisions :", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${card.sm2MotVersDef.totalReviews}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (nextReviewDays >= 0 && !isNew) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Prochaine révision :", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "dans $nextReviewDays jour${if (nextReviewDays != 1) "s" else ""}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FaceProgressRow(
    title: String,
    state: ReviewCardAggregateState
) {
    val (label, color) = state.toLabelAndColor()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall)
        Text(
            text = label,
            color = color,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun ReviewCardAggregateState.toLabelAndColor(): Pair<String, Color> = when (this) {
    ReviewCardAggregateState.TO_WORK -> label to Color(0xFF1E3A5F)
    ReviewCardAggregateState.IN_PROGRESS -> label to Color(0xFFD35400)
    ReviewCardAggregateState.KNOWN -> label to Color(0xFF27AE60)
}

