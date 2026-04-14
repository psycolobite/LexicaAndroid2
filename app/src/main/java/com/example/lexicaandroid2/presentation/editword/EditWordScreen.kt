package com.example.lexicaandroid2.presentation.editword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.presentation.common.WordEditorForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditWordUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val loadedCard: Flashcard? = null,
    val word: String = "",
    val definition: String = "",
    val synonymes: String = "",
    val categorie: String = "",
    val registre: String = "",
    val etymologie: String = "",
    val exemples: String = "",
    val notes: String = "",
    val optionalExpanded: Boolean = true,
    val error: String? = null,
    val saveCompletedToken: Long = 0L
)

class EditWordViewModel(
    private val cardId: String,
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditWordUiState())
    val uiState: StateFlow<EditWordUiState> = _uiState.asStateFlow()

    init {
        loadCard()
    }

    private fun loadCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val card = repository.getAllCards().firstOrNull { it.id == cardId }
            if (card == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Mot introuvable"
                    )
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    loadedCard = card,
                    word = card.recto,
                    definition = card.verso,
                    synonymes = card.synonymes.joinToString(", "),
                    categorie = card.categorieGrammaticale,
                    registre = card.registre,
                    etymologie = card.etymologie,
                    exemples = card.exemples.joinToString("\n"),
                    notes = card.notesPersonnelles,
                    optionalExpanded = card.hasOptionalContent(),
                    error = null
                )
            }
        }
    }

    fun onWordChanged(value: String) = _uiState.update { it.copy(word = value, error = null) }
    fun onDefinitionChanged(value: String) = _uiState.update { it.copy(definition = value, error = null) }
    fun onSynonymesChanged(value: String) = _uiState.update { it.copy(synonymes = value) }
    fun onCategorieChanged(value: String) = _uiState.update { it.copy(categorie = value) }
    fun onRegistreChanged(value: String) = _uiState.update { it.copy(registre = value) }
    fun onEtymologieChanged(value: String) = _uiState.update { it.copy(etymologie = value) }
    fun onExemplesChanged(value: String) = _uiState.update { it.copy(exemples = value) }
    fun onNotesChanged(value: String) = _uiState.update { it.copy(notes = value) }
    fun toggleOptionalExpanded() = _uiState.update { it.copy(optionalExpanded = !it.optionalExpanded) }

    fun save() {
        val state = _uiState.value
        val currentCard = state.loadedCard ?: return
        val normalizedWord = state.word.trim()
        val normalizedDefinition = state.definition.trim()

        if (normalizedWord.isBlank() || normalizedDefinition.isBlank()) {
            _uiState.update { it.copy(error = "Le mot et la définition sont obligatoires") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val duplicate = repository.getAllCards().firstOrNull {
                it.id != currentCard.id && it.recto.equals(normalizedWord, ignoreCase = true)
            }
            if (duplicate != null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = "Un autre mot avec ce nom existe déjà dans ta liste"
                    )
                }
                return@launch
            }

            val updatedCard = currentCard.copy(
                recto = normalizedWord,
                verso = normalizedDefinition,
                synonymes = splitListField(state.synonymes),
                categorieGrammaticale = state.categorie.trim(),
                registre = state.registre.trim(),
                etymologie = state.etymologie.trim(),
                exemples = splitListField(state.exemples),
                notesPersonnelles = state.notes.trim()
            )
            repository.updateCardContent(updatedCard)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    loadedCard = updatedCard,
                    saveCompletedToken = System.currentTimeMillis(),
                    error = null
                )
            }
        }
    }

    private fun splitListField(value: String): List<String> = value
        .split('\n', ',')
        .map { it.trim() }
        .filter { it.isNotBlank() }

    private fun Flashcard.hasOptionalContent(): Boolean =
        synonymes.isNotEmpty() ||
            categorieGrammaticale.isNotBlank() ||
            registre.isNotBlank() ||
            etymologie.isNotBlank() ||
            exemples.isNotEmpty() ||
            notesPersonnelles.isNotBlank()
}

class EditWordViewModelFactory(
    private val cardId: String,
    private val repository: FlashcardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditWordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditWordViewModel(cardId = cardId, repository = repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun EditWordScreen(
    viewModel: EditWordViewModel,
    onSaved: (String) -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveCompletedToken) {
        val cardId = uiState.loadedCard?.id
        if (uiState.saveCompletedToken != 0L && cardId != null) {
            onSaved(cardId)
        }
    }

    when {
        uiState.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text("Chargement du mot…")
            }
        }

        uiState.loadedCard == null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.error ?: "Mot introuvable",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = onCancel) {
                    Text("Retour")
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Modifier mon mot",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Retouche les informations puis enregistre. Les champs sont préremplis automatiquement.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WordEditorForm(
                        word = uiState.word,
                        definition = uiState.definition,
                        synonymes = uiState.synonymes,
                        categorie = uiState.categorie,
                        registre = uiState.registre,
                        etymologie = uiState.etymologie,
                        exemples = uiState.exemples,
                        notes = uiState.notes,
                        optionalExpanded = uiState.optionalExpanded,
                        error = uiState.error,
                        onWordChanged = viewModel::onWordChanged,
                        onDefinitionChanged = viewModel::onDefinitionChanged,
                        onSynonymesChanged = viewModel::onSynonymesChanged,
                        onCategorieChanged = viewModel::onCategorieChanged,
                        onRegistreChanged = viewModel::onRegistreChanged,
                        onEtymologieChanged = viewModel::onEtymologieChanged,
                        onExemplesChanged = viewModel::onExemplesChanged,
                        onNotesChanged = viewModel::onNotesChanged,
                        onToggleExpanded = viewModel::toggleOptionalExpanded,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Text(
                    text = "Astuce : pour les exemples longs, tu peux mettre une phrase par ligne pour garder quelque chose de lisible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = viewModel::save,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving
                    ) {
                        Text(if (uiState.isSaving) "Enregistrement…" else "Enregistrer les modifications")
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving
                    ) {
                        Text("Annuler")
                    }
                }
            }
        }
    }
}

