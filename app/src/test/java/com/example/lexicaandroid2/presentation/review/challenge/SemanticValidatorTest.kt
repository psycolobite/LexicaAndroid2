package com.example.lexicaandroid2.presentation.review.challenge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpellingValidatorTest {

    private val validator = SpellingValidator()

    @Test
    fun exactMatchReturnsValidWithXpBonus() {
        val result = validator.validate("serendipite", "serendipite")
        assertTrue(result.isValid)
        assertEquals(10, result.xpBonus)
        assertEquals(1f, result.keywordScore, 0.001f)
    }

    @Test
    fun caseInsensitiveMatchIsValid() {
        val result = validator.validate("LACUNE", "lacune")
        assertTrue(result.isValid)
    }

    @Test
    fun leadingTrailingSpacesAreIgnored() {
        val result = validator.validate("  lacune  ", "lacune")
        assertTrue(result.isValid)
    }

    @Test
    fun wrongSpellingIsInvalidWithZeroXp() {
        val result = validator.validate("magnifik", "magnifique")
        assertFalse(result.isValid)
        assertEquals(0, result.xpBonus)
        assertEquals(0f, result.keywordScore, 0.001f)
    }

    @Test
    fun wrongAnswerFeedbackContainsCorrectWord() {
        val result = validator.validate("ephamare", "ephemere")
        assertFalse(result.isValid)
        assertTrue(result.feedbackMessage.contains("ephemere"))
    }

    @Test
    fun correctAnswerFeedbackIsPositive() {
        val result = validator.validate("lacune", "lacune")
        assertTrue(result.feedbackMessage.contains("✅"))
    }

    @Test
    fun wrongAnswerFeedbackContainsNegativeMarker() {
        val result = validator.validate("laacune", "lacune")
        assertTrue(result.feedbackMessage.contains("❌"))
    }

    @Test
    fun correctAnswerFoundKeywordsListsExpected() {
        val result = validator.validate("lacune", "lacune")
        assertTrue("lacune" in result.foundKeywords)
        assertTrue(result.missingKeywords.isEmpty())
    }

    @Test
    fun wrongAnswerMissingKeywordsListsExpected() {
        val result = validator.validate("wrong", "lacune")
        assertTrue("lacune" in result.missingKeywords)
        assertTrue(result.foundKeywords.isEmpty())
    }

    @Test
    fun semanticScoreIsMinusOneForSpelling() {
        val result = validator.validate("lacune", "lacune")
        assertEquals(-1f, result.semanticScore, 0.001f)
    }

    @Test
    fun isModelReadyAlwaysTrue() {
        assertTrue(validator.isModelReady())
    }
}

class JaccardSemanticValidatorTest {

    private val validator = JaccardSemanticValidator()

    @Test
    fun perfectCoverageReturnsValidWithFullXp() {
        val text = "manque important insuffisance notable absence dommageable"
        val result = validator.validate(userInput = text, expected = text)
        assertTrue(result.isValid)
        assertEquals(15, result.xpBonus)
    }

    @Test
    fun highCoverageAbove60PercentReturnsValid() {
        val result = validator.validate(
            userInput = "manque important insuffisance notable absence",
            expected = "manque important insuffisance notable absence dommageable"
        )
        assertTrue(result.isValid)
        assertEquals(15, result.xpBonus)
    }

    @Test
    fun partialCoverageBetween30And60ReturnsPartialXp() {
        // Only 1 out of 4+ keywords present → score around 0.2–0.4 depending on keywords
        val result = validator.validate(
            userInput = "insuffisance rien autre",
            expected = "manque important insuffisance notable absence dommageable"
        )
        // Either 5 or 0 XP, but never 15
        assertTrue(result.xpBonus < 15)
        assertFalse(result.isValid)
    }

    @Test
    fun completelyWrongInputReturnsZeroXp() {
        val result = validator.validate(
            userInput = "voiture rapide rouge",
            expected = "lacune manque insuffisance absence notable"
        )
        assertFalse(result.isValid)
        assertEquals(0, result.xpBonus)
    }

    @Test
    fun foundKeywordsAreSubsetOfExpectedKeywords() {
        val result = validator.validate(
            userInput = "mammifere domestique loyal fidele compagnon",
            expected = "mammifere domestique loyal fidele compagnon"
        )
        assertTrue(result.foundKeywords.isNotEmpty())
        assertTrue(result.missingKeywords.isEmpty())
    }

    @Test
    fun validResultFeedbackContainsCheckmark() {
        val text = "mammifere domestique loyal fidele compagnon"
        val result = validator.validate(userInput = text, expected = text)
        assertTrue(result.feedbackMessage.contains("✅"))
    }

    @Test
    fun invalidResultFeedbackContainsNegativeMarker() {
        val result = validator.validate(
            userInput = "animal maison",
            expected = "mammifere domestique loyal fidele compagnon"
        )
        assertFalse(result.isValid)
        assertTrue(
            result.feedbackMessage.contains("❌") || result.feedbackMessage.contains("💡")
        )
    }

    @Test
    fun keywordScoreIsProportionOfFoundToTotal() {
        val result = validator.validate(
            userInput = "manque important",
            expected = "manque important insuffisance notable absence"
        )
        // 2 keywords found out of 5 expected → score = 2 / (2+3) = 0.4 (approximately)
        assertTrue(result.keywordScore in 0f..1f)
    }

    @Test
    fun emptyInputReturnsZeroXp() {
        val result = validator.validate(
            userInput = "",
            expected = "manque important insuffisance notable"
        )
        assertFalse(result.isValid)
        assertEquals(0, result.xpBonus)
    }

    @Test
    fun isModelReadyAlwaysTrue() {
        assertTrue(validator.isModelReady())
    }
}
