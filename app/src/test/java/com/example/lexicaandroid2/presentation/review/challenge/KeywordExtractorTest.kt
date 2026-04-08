package com.example.lexicaandroid2.presentation.review.challenge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeywordExtractorTest {

    // region tokenize

    @Test
    fun tokenizeRemovesDiacriticsAndLowercases() {
        val result = KeywordExtractor.tokenize("Éléphant à l'ère")
        assertEquals(listOf("elephant", "a", "l", "ere"), result)
    }

    @Test
    fun tokenizeRemovesPunctuation() {
        val result = KeywordExtractor.tokenize("bonjour, monde!")
        assertEquals(listOf("bonjour", "monde"), result)
    }

    @Test
    fun tokenizeEmptyStringReturnsEmptyList() {
        val result = KeywordExtractor.tokenize("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun tokenizeSplitsOnMultipleSpaces() {
        val result = KeywordExtractor.tokenize("un   deux   trois")
        assertEquals(listOf("un", "deux", "trois"), result)
    }

    // endregion

    // region extractKeywords

    @Test
    fun extractKeywordsFiltersStopwordsAndShortWords() {
        val result = KeywordExtractor.extractKeywords("le chat est dans la maison")
        assertTrue("chat" in result)
        assertTrue("maison" in result)
        assertTrue("le" !in result)
        assertTrue("est" !in result)
    }

    @Test
    fun extractKeywordsReturnsAtMostTopN() {
        val text = "intelligence artificielle apprentissage automatique profond reseau neuronal transformateur"
        val result = KeywordExtractor.extractKeywords(text, topN = 3)
        assertEquals(3, result.size)
    }

    @Test
    fun extractKeywordsDefaultsToTwoMainKeywords() {
        val result = KeywordExtractor.extractKeywords(
            "plante ligneuse avec feuilles et branches"
        )

        assertEquals(2, result.size)
        assertEquals(listOf("plante", "ligneuse"), result)
    }

    @Test
    fun extractKeywordsFiltersWordsUnderFourChars() {
        val result = KeywordExtractor.extractKeywords("un bus car taxi metro avion bateau")
        assertTrue("bus" !in result)
        assertTrue("car" !in result)
        assertTrue("taxi" in result)
    }

    @Test
    fun extractKeywordsNoDuplicates() {
        val result = KeywordExtractor.extractKeywords("chat chat chaton chaton")
        assertEquals(result.size, result.distinct().size)
    }

    @Test
    fun extractKeywordsIgnoresGenericDefinitionWordsWhenBetterCandidatesExist() {
        val result = KeywordExtractor.extractKeywords(
            "personne qui pratique la medecine avec experience"
        )

        assertTrue("personne" !in result)
        assertTrue("pratique" in result || "medecine" in result || "experience" in result)
    }

    // endregion

    // region jaccardScore

    @Test
    fun jaccardScoreIdenticalTextsReturnsOne() {
        val score = KeywordExtractor.jaccardScore("chien animal domestique", "chien animal domestique")
        assertEquals(1.0f, score, 0.001f)
    }

    @Test
    fun jaccardScoreCompletelyDifferentTextsReturnsZero() {
        val score = KeywordExtractor.jaccardScore("chat maison", "voiture route")
        assertEquals(0.0f, score, 0.001f)
    }

    @Test
    fun jaccardScorePartialOverlapReturnsMidRange() {
        val score = KeywordExtractor.jaccardScore("grand animal sauvage", "animal sauvage foret")
        assertTrue(score > 0f && score < 1f)
    }

    @Test
    fun jaccardScoreEmptyUserInputReturnsZero() {
        val score = KeywordExtractor.jaccardScore("", "animal domestique")
        assertEquals(0.0f, score, 0.001f)
    }

    @Test
    fun jaccardScoreBothEmptyReturnsZero() {
        val score = KeywordExtractor.jaccardScore("", "")
        assertEquals(0.0f, score, 0.001f)
    }

    // endregion

    // region analyzeKeywords

    @Test
    fun analyzeKeywordsReturnsFoundAndMissing() {
        val (found, missing) = KeywordExtractor.analyzeKeywords(
            userInput = "mammifere domestique fidele",
            expectedDef = "mammifere domestique fidele compagnon loyal"
        )
        assertTrue(found.isNotEmpty())
        assertTrue(found.all { it in listOf("mammifere", "domestique") })
        assertTrue(missing.all { it in listOf("mammifere", "domestique") })
    }

    @Test
    fun analyzeKeywordsEmptyInputReturnsAllKeywordsMissing() {
        val (found, missing) = KeywordExtractor.analyzeKeywords(
            userInput = "",
            expectedDef = "mammifere domestique fidele compagnon"
        )
        assertTrue(found.isEmpty())
        assertTrue(missing.isNotEmpty())
    }

    @Test
    fun analyzeKeywordsPerfectMatchReturnsNoMissing() {
        val text = "mammifere domestique fidele compagnon loyal"
        val (found, missing) = KeywordExtractor.analyzeKeywords(
            userInput = text,
            expectedDef = text
        )
        assertTrue(missing.isEmpty())
        assertTrue(found.isNotEmpty())
    }

    // endregion
}
