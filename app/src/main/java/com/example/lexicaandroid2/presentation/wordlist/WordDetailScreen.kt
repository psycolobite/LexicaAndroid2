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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WordDetailUiState(
    val card: Flashcard? = null,
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
                _uiState.update { it.copy(card = card, isLoading = false, error = if (card == null) "Mot non trouvé" else null) }
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
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WordDetailViewModel(cardId, repository) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    cardId: String,
    viewModel: WordDetailViewModel,
    onBack: () -> Unit
) {
    val uiState = remember { viewModel.uiState }
    val card = uiState.value.card
    val showDeleteConfirm = remember { mutableStateOf(false) }
    var isDeleted = remember { mutableStateOf(false) }

    if (isDeleted.value) {
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
                        isDeleted.value = true
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Catégorie et registre
                    if (card.categorieGrammaticale.isNotBlank() || card.registre.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
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
                }
            }

            // Définition
            if (card.verso.isNotBlank()) {
                item {
                    DetailSection(title = "📖 Définition", content = card.verso)
                }
            }

            // Exemples
            if (card.exemples.isNotEmpty()) {
                item {
                    DetailSection(
                        title = "💡 Exemples d'usage",
                        content = card.exemples.joinToString("\n") { "• $it" }
                    )
                }
            }

            // Synonymes
            if (card.synonymes.isNotEmpty()) {
                item {
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
            }

            // Étymologie
            if (card.etymologie.isNotBlank()) {
                item {
                    DetailSection(title = "🌿 Étymologie", content = card.etymologie)
                }
            }

            // Progression SM2
            item {
                ProgressionSection(card = card)
            }

            // Spacer pour le bouton du bas
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
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
fun ProgressionSection(card: Flashcard) {
    val isNew = card.sm2MotVersDef.repetitions == 0 && card.sm2DefVersMot.repetitions == 0
    val isKnown = card.sm2MotVersDef.interval > 20 && card.sm2DefVersMot.interval > 20
    val (stateText, stateColor) = when {
        isNew -> "À apprendre" to Color(0xFF1E3A5F)
        isKnown -> "Connu" to Color(0xFF27AE60)
        else -> "En cours" to Color(0xFFD35400)
    }

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
