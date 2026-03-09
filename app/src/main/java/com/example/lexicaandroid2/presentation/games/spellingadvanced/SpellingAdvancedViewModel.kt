package com.example.lexicaandroid2.presentation.games.spellingadvanced

import android.app.Application
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SpellingAdvancedUiState(
    val cards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val userInput: String = "",
    val score: Int = 0,
    val totalRounds: Int = 0,
    val jokersRemaining: Int = 3,
    val revealedIndices: Set<Int> = emptySet(),
    val answerSubmitted: Boolean = false,
    val isCorrect: Boolean? = null,
    val feedback: String? = null,
    val isLoading: Boolean = true,
    val isSpeaking: Boolean = false,
    val ttsReady: Boolean = false,
    val gameOver: Boolean = false,
    val error: String? = null
) {
    val currentCard: Flashcard?
        get() = cards.getOrNull(currentIndex)

    val progress: Float
        get() = if (totalRounds == 0) 0f else currentIndex.toFloat() / totalRounds.toFloat()
}

class SpellingAdvancedViewModel(
    application: Application,
    private val repository: FlashcardRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SpellingAdvancedUiState())
    val uiState: StateFlow<SpellingAdvancedUiState> = _uiState.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        initTts()
    }

    fun loadGame() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val cards = repository.getAllCards()
                    .filter { it.recto.isNotBlank() }
                    .shuffled()
                    .take(10)

                if (cards.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Aucune carte disponible"
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
                        jokersRemaining = 3,
                        revealedIndices = emptySet(),
                        answerSubmitted = false,
                        isCorrect = null,
                        feedback = null,
                        isLoading = false,
                        gameOver = false
                    )
                }
                replayWord(useJoker = false)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Erreur inconnue"
                    )
                }
            }
        }
    }

    fun onUserInputChanged(input: String) {
        _uiState.update { state ->
            if (state.answerSubmitted) state else state.copy(userInput = input)
        }
    }

    fun replayWord(useJoker: Boolean = false) {
        val state = _uiState.value
        val card = state.currentCard ?: return

        if (useJoker) {
            if (state.jokersRemaining <= 0 || state.answerSubmitted) return
            _uiState.update { it.copy(jokersRemaining = it.jokersRemaining - 1) }
        }

        val utteranceId = UUID.randomUUID().toString()
        val bundle = Bundle()
        bundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        tts?.speak(card.recto, TextToSpeech.QUEUE_FLUSH, bundle, utteranceId)
    }

    fun useRevealLetterJoker() {
        val state = _uiState.value
        val card = state.currentCard ?: return

        if (state.jokersRemaining <= 0 || state.answerSubmitted) return

        val clean = card.recto
        val hiddenIndices = clean.indices.filter { it !in state.revealedIndices }
        if (hiddenIndices.isEmpty()) return

        val revealIndex = hiddenIndices.random()
        _uiState.update {
            it.copy(
                jokersRemaining = it.jokersRemaining - 1,
                revealedIndices = it.revealedIndices + revealIndex,
                feedback = "Indice: lettre revelee"
            )
        }
    }

    fun useSkipJoker() {
        val state = _uiState.value
        if (state.jokersRemaining <= 0 || state.answerSubmitted || state.currentCard == null) return

        _uiState.update {
            it.copy(
                jokersRemaining = it.jokersRemaining - 1,
                answerSubmitted = true,
                isCorrect = false,
                feedback = "Joker utilise: mot saute"
            )
        }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val card = state.currentCard ?: return
        if (state.answerSubmitted) return

        val target = card.recto
        val input = state.userInput.trim()
        if (input.isBlank()) return

        val evaluation = evaluateAnswer(input, target)

        _uiState.update {
            it.copy(
                answerSubmitted = true,
                isCorrect = evaluation.correct,
                score = if (evaluation.correct) it.score + 1 else it.score,
                feedback = evaluation.feedback
            )
        }
    }

    fun nextRound() {
        val state = _uiState.value
        if (!state.answerSubmitted) return

        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.totalRounds) {
            _uiState.update { it.copy(gameOver = true) }
            return
        }

        _uiState.update {
            it.copy(
                currentIndex = nextIndex,
                userInput = "",
                revealedIndices = emptySet(),
                answerSubmitted = false,
                isCorrect = null,
                feedback = null
            )
        }
        replayWord(useJoker = false)
    }

    fun restart() {
        _uiState.update { SpellingAdvancedUiState(ttsReady = _uiState.value.ttsReady) }
        loadGame()
    }

    private fun initTts() {
        tts = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.FRENCH
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        viewModelScope.launch {
                            _uiState.update { it.copy(isSpeaking = true) }
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        viewModelScope.launch {
                            _uiState.update { it.copy(isSpeaking = false) }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        viewModelScope.launch {
                            _uiState.update { it.copy(isSpeaking = false) }
                        }
                    }
                })
                _uiState.update { it.copy(ttsReady = true) }
            } else {
                _uiState.update { it.copy(ttsReady = false, error = "Echec initialisation TTS") }
            }
        }
    }

    private data class AnswerEvaluation(
        val correct: Boolean,
        val feedback: String
    )

    private fun evaluateAnswer(userInput: String, targetWord: String): AnswerEvaluation {
        val userRaw = userInput.trim()
        val targetRaw = targetWord.trim()

        if (userRaw.equals(targetRaw, ignoreCase = true)) {
            return AnswerEvaluation(true, "Correct")
        }

        val userNorm = normalize(userRaw)
        val targetNorm = normalize(targetRaw)

        if (userNorm == targetNorm) {
            return AnswerEvaluation(true, "Correct (accents/diacritiques toleres)")
        }

        if (isPluralVariant(userNorm, targetNorm)) {
            return AnswerEvaluation(true, "Correct (variante singulier/pluriel toleree)")
        }

        val distance = levenshtein(userNorm, targetNorm)
        val feedback = when {
            distance <= 1 -> "Presque: 1 caractere d'ecart"
            distance <= 2 -> "Proche: quelques lettres differentes"
            userNorm.firstOrNull() != targetNorm.firstOrNull() -> "Incorrect: attention a la premiere lettre"
            else -> "Incorrect: attendu \"$targetRaw\""
        }
        return AnswerEvaluation(false, feedback)
    }

    private fun isPluralVariant(a: String, b: String): Boolean {
        val variants = listOf("s", "x", "es")
        return variants.any { suffix ->
            a == b + suffix || b == a + suffix
        }
    }

    private fun normalize(value: String): String {
        return Normalizer
            .normalize(value.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace(Regex("[^a-z]"), "")
            .trim()
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[a.length][b.length]
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
    }
}
