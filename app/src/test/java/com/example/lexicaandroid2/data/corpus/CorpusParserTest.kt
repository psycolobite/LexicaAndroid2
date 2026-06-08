package com.example.lexicaandroid2.data.corpus

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CorpusParserTest {

    private val parser = CorpusParser()
    private val sampleSource = CorpusSource(
        id = "test-source",
        title = "Test Title",
        author = "Test Author",
        year = 2026,
        type = SourceType.BOOK,
        language = "fr",
        license = null,
        contentUrl = "http://example.com",
        domainTags = listOf("sciences", "philosophie")
    )

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @Test
    fun testParseSegmentsParagraphsCorrectly() {
        val rawText = """
            Ceci est le premier paragraphe de test. Il contient des mots intéressants et longs comme philosophique ou extraordinaire.
            
            Ceci est le deuxième paragraphe. Il doit être séparé du premier. Nous parlons de technologie et de cybernétique.
        """.trimIndent()

        val candidates = parser.parse(sampleSource, rawText)
        assertEquals(2, candidates.size)
        
        // Assertions on the first candidate
        val first = candidates[0]
        assertEquals("test-source", first.sourceId)
        assertTrue(first.content.contains("premier paragraphe"))
        assertTrue(first.suggestedWords.contains("philosophique") || first.suggestedWords.contains("extraordinaire"))
        assertTrue(first.domainTags.contains("sciences"))

        // Assertions on the second candidate
        val second = candidates[1]
        assertTrue(second.content.contains("deuxième paragraphe"))
        assertTrue(second.suggestedWords.contains("technologie") || second.suggestedWords.contains("cybernétique"))
    }

    @Test
    fun testParseFiltersTooShortOrStopWords() {
        val rawText = "Le la les un de et en dans pour qui que quoi. Ce sont des mots outils."
        val candidates = parser.parse(sampleSource, rawText)
        assertTrue(candidates.isEmpty())
    }

    @Test
    fun testParseCalculatesDifficultyAndQuality() {
        val rawText = "Voici un magnifique paragraphe contenant du vocabulaire particulièrement complexe et élaboré afin d'évaluer la qualité du contexte."
        val candidates = parser.parse(sampleSource, rawText)
        assertEquals(1, candidates.size)
        val candidate = candidates[0]
        
        // Verify difficulty and quality
        assertNotNull(candidate.difficulty)
        assertTrue(candidate.contextQuality >= 0.0f && candidate.contextQuality <= 1.0f)
    }
}
