package com.example.lexicaandroid2.data.corpus

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CorpusIndexTest {

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    private val candidate1 = ExtractCandidate(
        id = "1",
        sourceId = "src1",
        content = "Contenu avec des mots intéressants pour la philosophie.",
        startPosition = 0,
        endPosition = 50,
        wordCount = 8,
        suggestedWords = listOf("intéressants", "philosophie"),
        domainTags = listOf("philosophie"),
        registerTags = listOf("courant"),
        difficulty = "moyen",
        contextQuality = 0.8f,
        hasCompleteSource = false
    )

    private val candidate2 = ExtractCandidate(
        id = "2",
        sourceId = "src2",
        content = "Contenu scientifique rigoureux et très technique sur la physique quantique.",
        startPosition = 0,
        endPosition = 70,
        wordCount = 10,
        suggestedWords = listOf("scientifique", "rigoureux", "quantique"),
        domainTags = listOf("sciences"),
        registerTags = listOf("technique"),
        difficulty = "avancé",
        contextQuality = 0.9f,
        hasCompleteSource = true
    )

    @Test
    fun testIndexPopulationAndClear() {
        val index = CorpusIndex()
        assertEquals(0, index.size())

        index.add(candidate1)
        assertEquals(1, index.size())
        assertEquals(1, index.count())

        index.addAll(listOf(candidate2))
        assertEquals(2, index.size())

        index.clear()
        assertEquals(0, index.size())
    }

    @Test
    fun testGetCandidatesByDomain() {
        val index = CorpusIndex.index(listOf(candidate1, candidate2))

        val philosophyCandidates = index.getCandidates(listOf("philosophie"))
        assertEquals(1, philosophyCandidates.size)
        assertEquals("1", philosophyCandidates[0].id)

        val scienceCandidates = index.getCandidates(listOf("sciences"))
        assertEquals(1, scienceCandidates.size)
        assertEquals("2", scienceCandidates[0].id)

        val bothCandidates = index.getCandidates(listOf("philosophie", "sciences"))
        assertEquals(2, bothCandidates.size)

        val emptyQueryCandidates = index.getCandidates(emptyList())
        assertEquals(2, emptyQueryCandidates.size)
    }

    @Test
    fun testGetCandidatesByDifficulty() {
        val index = CorpusIndex.index(listOf(candidate1, candidate2))

        val advancedCandidates = index.getCandidatesByDifficulty("avancé")
        assertEquals(1, advancedCandidates.size)
        assertEquals("2", advancedCandidates[0].id)

        val easyCandidates = index.getCandidatesByDifficulty("facile")
        assertEquals(0, easyCandidates.size)
    }

    @Test
    fun testAvailableDomains() {
        val index = CorpusIndex.index(listOf(candidate1, candidate2))
        val domains = index.availableDomains()
        assertEquals(2, domains.size)
        assertTrue(domains.contains("philosophie"))
        assertTrue(domains.contains("sciences"))
    }
}
