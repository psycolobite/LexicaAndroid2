package com.example.lexicaandroid2.core.tts

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsVoiceSupportTest {

    @Test
    fun detectsFrenchVoiceFromModernLocaleTag() {
        assertTrue(hasFrenchVoiceSupport(listOf("fr-FR", "en-US")))
    }

    @Test
    fun detectsFrenchVoiceFromLegacyLocaleTag() {
        assertTrue(hasFrenchVoiceSupport(listOf("fr_FR")))
    }

    @Test
    fun returnsFalseWhenNoFrenchVoiceIsAvailable() {
        assertFalse(hasFrenchVoiceSupport(listOf("en-US", "en-GB", "de-DE")))
    }

    @Test
    fun returnsFalseForEmptyVoiceList() {
        assertFalse(hasFrenchVoiceSupport(emptyList()))
    }
}

