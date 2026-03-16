package com.example.lexicaandroid2.core.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LexicaTtsService(
    context: Context,
    initialLocale: Locale = Locale.FRENCH
) {
    private val appContext = context.applicationContext

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var requestedLocale: Locale = initialLocale
    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })

                val languageApplied = applyLanguage(requestedLocale)
                _isReady.value = languageApplied
                _errorMessage.value = if (languageApplied) null else "Langue TTS non disponible"
            } else {
                _isReady.value = false
                _errorMessage.value = "Échec initialisation TTS"
            }
        }
    }

    fun setLanguage(locale: Locale): Boolean {
        requestedLocale = locale
        val applied = applyLanguage(locale)
        _isReady.value = applied
        _errorMessage.value = if (applied) null else "Langue TTS non disponible"
        return applied
    }

    fun speak(text: String) {
        if (!_isReady.value || text.isBlank()) return

        stop()

        val utteranceId = UUID.randomUUID().toString()
        val bundle = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, bundle, utteranceId)
    }

    fun stop() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _isReady.value = false
    }

    private fun applyLanguage(locale: Locale): Boolean {
        val result = textToSpeech?.setLanguage(locale) ?: return false
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }
}
