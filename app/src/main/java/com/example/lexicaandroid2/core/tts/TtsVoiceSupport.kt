package com.example.lexicaandroid2.core.tts

import java.util.Locale

internal fun hasFrenchVoiceSupport(availableVoiceTags: List<String>): Boolean {
    return availableVoiceTags.any { tag ->
        val normalized = tag.trim()
        if (normalized.isBlank()) return@any false

        val localeFromTag = Locale.forLanguageTag(normalized.replace('_', '-'))
        localeFromTag.language.equals("fr", ignoreCase = true) ||
            normalized.startsWith("fr", ignoreCase = true)
    }
}

