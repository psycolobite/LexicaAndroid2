package com.example.lexicaandroid2.presentation.review

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.core.tts.LexicaTtsService
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DrivingModeUiState(
    val cards: List<Flashcard> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val isSpeaking: Boolean = false,
    val ttsReady: Boolean = false,
    val currentPhaseLabel: String = "",
    val error: String? = null
) {
    val currentCard: Flashcard?
        get() = cards.getOrNull(currentIndex)
}

class DrivingModeViewModel(
    application: Application,
    private val repository: FlashcardRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DrivingModeUiState())
    val uiState: StateFlow<DrivingModeUiState> = _uiState.asStateFlow()

    private val ttsService = LexicaTtsService(application)
    private var playbackJob: Job? = null

    init {
        observeTtsState()
    }

    private fun observeTtsState() {
        viewModelScope.launch {
            ttsService.isReady.collect { ready ->
                _uiState.update { it.copy(ttsReady = ready) }
                if (ready && _uiState.value.isPlaying && playbackJob == null && !_uiState.value.isFinished) {
                    startPlaybackLoop()
                }
            }
        }

        viewModelScope.launch {
            ttsService.isSpeaking.collect { speaking ->
                _uiState.update { it.copy(isSpeaking = speaking) }
            }
        }

        viewModelScope.launch {
            ttsService.errorMessage.collect { error ->
                if (!error.isNullOrBlank()) {
                    _uiState.update { it.copy(error = error) }
                }
            }
        }
    }

    fun loadSession(limit: Int = 20) {
        stopPlayback()
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, error = null, isFinished = false) }
            try {
                val cards = repository.getCardsToReview(limit)
                    .filter { it.recto.isNotBlank() && it.verso.isNotBlank() }

                if (cards.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Aucune carte disponible pour le mode voiture"
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        cards = cards,
                        currentIndex = 0,
                        isLoading = false,
                        isPaused = false,
                        isPlaying = true,
                        isFinished = false,
                        currentPhaseLabel = "Lecture du mot"
                    )
                }
                startPlaybackLoop()
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

    fun pause() {
        stopPlayback()
        _uiState.update { it.copy(isPaused = true, isPlaying = false, currentPhaseLabel = "En pause") }
    }

    fun resume() {
        val state = _uiState.value
        if (state.currentCard == null || state.isFinished) return
        _uiState.update { it.copy(isPaused = false, isPlaying = true) }
        startPlaybackLoop()
    }

    fun repeatCurrentCard() {
        val state = _uiState.value
        if (state.currentCard == null) return
        stopPlayback()
        _uiState.update { it.copy(isPaused = false, isPlaying = true) }
        startPlaybackLoop(playSingleCardOnly = true)
    }

    fun nextCard() {
        val state = _uiState.value
        val hasNext = state.currentIndex + 1 < state.cards.size
        stopPlayback()

        if (!hasNext) {
            _uiState.update { it.copy(isFinished = true, isPlaying = false, isPaused = false, currentPhaseLabel = "Session terminée") }
            return
        }

        _uiState.update {
            it.copy(
                currentIndex = it.currentIndex + 1,
                isPaused = false,
                isPlaying = true,
                currentPhaseLabel = "Lecture du mot"
            )
        }
        startPlaybackLoop()
    }

    private fun startPlaybackLoop(playSingleCardOnly: Boolean = false) {
        if (!_uiState.value.ttsReady) return

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                val state = _uiState.value
                val card = state.currentCard ?: break
                if (state.isPaused || state.isFinished) break

                _uiState.update { it.copy(currentPhaseLabel = "Lecture du mot") }
                ttsService.speak(card.recto)
                delay(1800)
                if (_uiState.value.isPaused) break

                _uiState.update { it.copy(currentPhaseLabel = "Lecture de la définition") }
                ttsService.speak(card.verso)
                delay(2600)
                if (_uiState.value.isPaused) break

                if (playSingleCardOnly) {
                    _uiState.update { it.copy(isPlaying = false, currentPhaseLabel = "Lecture répétée terminée") }
                    break
                }

                val hasNext = _uiState.value.currentIndex + 1 < _uiState.value.cards.size
                if (!hasNext) {
                    _uiState.update {
                        it.copy(
                            isFinished = true,
                            isPlaying = false,
                            isPaused = false,
                            currentPhaseLabel = "Session terminée"
                        )
                    }
                    break
                }

                _uiState.update {
                    it.copy(
                        currentIndex = it.currentIndex + 1,
                        currentPhaseLabel = "Carte suivante"
                    )
                }
                delay(900)
            }
        }
    }

    private fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        ttsService.stop()
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
        ttsService.shutdown()
    }
}
