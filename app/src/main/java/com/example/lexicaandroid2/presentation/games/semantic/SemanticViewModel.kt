package com.example.lexicaandroid2.presentation.games.semantic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SemanticQuestionType {
    SYNONYM,
    DEFINITION,
    RELATION
}

data class SemanticChoice(
    val id: Int,
    val text: String,
    val isCorrect: Boolean
)

data class SemanticUiState(
    val cards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val questionType: SemanticQuestionType = SemanticQuestionType.DEFINITION,
    val promptWord: String = "",
    val prompt: String = "",
    val details: String? = null,
    val choices: List<SemanticChoice> = emptyList(),
    val selectedChoiceId: Int? = null,
    val score: Int = 0,
    val totalRounds: Int = 0,
    val answered: Boolean = false,
    val isCorrect: Boolean? = null,
    val isLoading: Boolean = true,
    val gameOver: Boolean = false,
    val error: String? = null
) {
    val progress: Float
        get() = if (totalRounds == 0) 0f else currentIndex.toFloat() / totalRounds.toFloat()
}

class SemanticViewModel(private val repository: FlashcardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SemanticUiState())
    val uiState: StateFlow<SemanticUiState> = _uiState.asStateFlow()

    fun loadGame() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val cards = repository.getAllCards()
                    .filter { it.recto.isNotBlank() && it.verso.isNotBlank() }
                    .shuffled()
                    .take(12)

                if (cards.size < 4) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Pas assez de cartes pour lancer ce jeu"
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        cards = cards,
                        currentIndex = 0,
                        score = 0,
                        totalRounds = cards.size,
                        isLoading = false,
                        gameOver = false
                    )
                }
                generateQuestion(0)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Erreur inconnue")
                }
            }
        }
    }

    fun selectChoice(choiceId: Int) {
        _uiState.update { state ->
            if (state.answered) state else state.copy(selectedChoiceId = choiceId)
        }
    }

    fun validateAnswer() {
        val state = _uiState.value
        val selected = state.selectedChoiceId ?: return
        if (state.answered) return

        val isCorrect = state.choices.find { it.id == selected }?.isCorrect == true
        _uiState.update {
            it.copy(
                answered = true,
                isCorrect = isCorrect,
                score = if (isCorrect) it.score + 1 else it.score
            )
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (!state.answered) return

        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.totalRounds) {
            _uiState.update { it.copy(gameOver = true) }
            return
        }

        generateQuestion(nextIndex)
    }

    fun restart() {
        _uiState.update { SemanticUiState() }
        loadGame()
    }

    private fun generateQuestion(index: Int) {
        val cards = _uiState.value.cards
        val current = cards[index]
        val availableTypes = mutableListOf(SemanticQuestionType.DEFINITION)

        if (current.synonymes.any { it.isNotBlank() }) {
            availableTypes.add(SemanticQuestionType.SYNONYM)
        }

        val hasRelation = current.categorieGrammaticale.isNotBlank() &&
            cards.count {
                it.id != current.id &&
                    it.categorieGrammaticale.equals(current.categorieGrammaticale, ignoreCase = true)
            } > 0
        if (hasRelation) {
            availableTypes.add(SemanticQuestionType.RELATION)
        }

        val type = availableTypes.random()
        val question = when (type) {
            SemanticQuestionType.SYNONYM -> buildSynonymQuestion(current, cards)
            SemanticQuestionType.DEFINITION -> buildDefinitionQuestion(current, cards)
            SemanticQuestionType.RELATION -> buildRelationQuestion(current, cards)
        }

        _uiState.update {
            it.copy(
                currentIndex = index,
                questionType = type,
                promptWord = current.recto,
                prompt = question.prompt,
                details = question.details,
                choices = question.choices.shuffled(),
                selectedChoiceId = null,
                answered = false,
                isCorrect = null
            )
        }
    }

    private data class BuiltQuestion(
        val prompt: String,
        val details: String?,
        val choices: List<SemanticChoice>
    )

    private fun buildSynonymQuestion(current: Flashcard, cards: List<Flashcard>): BuiltQuestion {
        val normalizedSynonyms = current.synonymes
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }

        val correct = normalizedSynonyms.firstOrNull() ?: current.recto
        val distractors = closestWords(cards, current.recto, 50)
            .map { it.recto }
            .filter { candidate ->
                !candidate.equals(current.recto, ignoreCase = true) &&
                    !candidate.equals(correct, ignoreCase = true) &&
                    normalizedSynonyms.none { it.equals(candidate, ignoreCase = true) }
            }
            .distinctBy { it.lowercase() }
            .take(3)
            .toMutableList()

        if (distractors.size < 3) {
            val fallback = cards.map { it.recto }
                .filter {
                    !it.equals(current.recto, ignoreCase = true) &&
                        !it.equals(correct, ignoreCase = true) &&
                        distractors.none { d -> d.equals(it, ignoreCase = true) }
                }
                .take(3 - distractors.size)
            distractors.addAll(fallback)
        }

        val options = (listOf(correct) + distractors).distinct().take(4)
        val choices = options.mapIndexed { idx, option ->
            SemanticChoice(id = idx, text = option, isCorrect = option.equals(correct, ignoreCase = true))
        }

        return BuiltQuestion(
            prompt = "Choisis le synonyme de \"${current.recto}\"",
            details = "Type: synonyme",
            choices = ensureFourChoices(choices, cards.map { it.recto }, correct)
        )
    }

    private fun buildDefinitionQuestion(current: Flashcard, cards: List<Flashcard>): BuiltQuestion {
        val correct = current.verso
        val distractors = cards
            .filter { it.id != current.id }
            .sortedBy { kotlin.math.abs(it.verso.length - correct.length) }
            .map { it.verso }
            .distinctBy { it.lowercase() }
            .take(3)

        val options = (listOf(correct) + distractors).shuffled()
        val choices = options.mapIndexed { idx, option ->
            SemanticChoice(id = idx, text = option, isCorrect = option == correct)
        }

        return BuiltQuestion(
            prompt = "Quelle definition correspond a \"${current.recto}\" ?",
            details = "Type: definition",
            choices = ensureFourChoices(choices, cards.map { it.verso }, correct)
        )
    }

    private fun buildRelationQuestion(current: Flashcard, cards: List<Flashcard>): BuiltQuestion {
        val category = current.categorieGrammaticale
        val sameCategory = cards
            .filter { it.id != current.id && it.categorieGrammaticale.equals(category, ignoreCase = true) }
        val correct = sameCategory.random().recto

        val distractors = cards
            .filter { !it.categorieGrammaticale.equals(category, ignoreCase = true) }
            .map { it.recto }
            .distinctBy { it.lowercase() }
            .shuffled()
            .take(3)

        val options = (listOf(correct) + distractors).shuffled()
        val choices = options.mapIndexed { idx, option ->
            SemanticChoice(id = idx, text = option, isCorrect = option == correct)
        }

        return BuiltQuestion(
            prompt = "Quel mot partage la meme categorie grammaticale que \"${current.recto}\" ?",
            details = "Type: relation (${category.ifBlank { "non specifiee" }})",
            choices = ensureFourChoices(choices, cards.map { it.recto }, correct)
        )
    }

    private fun closestWords(cards: List<Flashcard>, anchor: String, window: Int): List<Flashcard> {
        return cards
            .sortedBy { kotlin.math.abs(it.recto.length - anchor.length) }
            .take(window)
    }

    private fun ensureFourChoices(
        choices: List<SemanticChoice>,
        fallbackPool: List<String>,
        correct: String
    ): List<SemanticChoice> {
        if (choices.size >= 4) return choices.take(4)

        val mutable = choices.toMutableList()
        val used = mutable.map { it.text.lowercase() }.toMutableSet()

        fallbackPool.forEach { item ->
            if (mutable.size >= 4) return@forEach
            if (used.contains(item.lowercase())) return@forEach
            used.add(item.lowercase())
            mutable.add(
                SemanticChoice(
                    id = mutable.size,
                    text = item,
                    isCorrect = item.equals(correct, ignoreCase = true)
                )
            )
        }

        return mutable.take(4)
    }
}
