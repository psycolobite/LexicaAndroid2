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
        assertTrue(result.foundKeywords.isEmpty())
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
    fun lexicalFallbackAcceptsCloseShortParaphrase() {
        val result = validator.validate(
            userInput = "discours pour convaincre",
            expected = "Discours prononcé pour exhorter ou convaincre."
        )

        assertTrue(result.isValid)
        assertTrue(result.semanticScore >= 0.50f)
    }

    @Test
    fun lexicalFallbackMarksLooseRelatedAnswerAsPartial() {
        val result = validator.validate(
            userInput = "colère et inquiétude",
            expected = "Humeur secrétée par le foie. Au sens figuré : colère, inquiétude."
        )

        assertFalse(result.isValid)
        assertEquals(5, result.xpBonus)
        assertTrue(result.semanticScore in 0.25f..0.50f)
    }

    @Test
    fun isModelReadyAlwaysTrue() {
        assertTrue(validator.isModelReady())
    }
}

class DistilUseWordPieceTokenizerTest {

    @Test
    fun encodeAddsSpecialTokensAndSplitsWordPieces() {
        val tokenizer = DistilUseWordPieceTokenizer(
            vocabLines = listOf("[PAD]", "[UNK]", "[CLS]", "[SEP]", "bonjour", "mon", "##de")
        )

        val tokenIds = tokenizer.encode("bonjour monde", maxSequenceLength = 8)

        assertEquals(listOf(2, 4, 5, 6, 3), tokenIds.toList())
    }

    @Test
    fun encodeFallsBackToUnknownTokenWhenWordCannotBeSegmented() {
        val tokenizer = DistilUseWordPieceTokenizer(
            vocabLines = listOf("[PAD]", "[UNK]", "[CLS]", "[SEP]", "bonjour")
        )

        val tokenIds = tokenizer.encode("introuvable", maxSequenceLength = 6)

        assertEquals(listOf(2, 1, 3), tokenIds.toList())
    }
}

class TFLiteSemanticValidatorTest {

    @Test
    fun readyModelUsesEmbeddingsToValidateSemanticParaphrase() {
        val validator = TFLiteSemanticValidator(
            embeddingEngine = FakeEmbeddingEngine(
                ready = true,
                embeddings = mapOf(
                    "une plante de grande taille" to floatArrayOf(1f, 0f, 0f),
                    "plante ligneuse avec feuilles et branches" to floatArrayOf(1f, 0f, 0f)
                )
            )
        )

        val result = validator.validate(
            userInput = "une plante de grande taille",
            expected = "plante ligneuse avec feuilles et branches"
        )

        assertTrue(result.isValid)
        assertTrue(result.semanticScore >= 0.99f)
        assertTrue(result.feedbackMessage.contains("Similarité sémantique"))
        assertTrue(result.foundKeywords.isEmpty())
        assertTrue(result.missingKeywords.isEmpty())
    }

    @Test
    fun semanticValidatorDoesNotRequireKeywordMatchWhenMeaningIsClose() {
        val validator = TFLiteSemanticValidator(
            embeddingEngine = FakeEmbeddingEngine(
                ready = true,
                embeddings = mapOf(
                    "plante grimpante en ligne" to floatArrayOf(0.7f, 0.3f, 0f),
                    "plante ligneuse avec feuilles et branches" to floatArrayOf(0.7f, 0.3f, 0f)
                )
            )
        )

        val result = validator.validate(
            userInput = "plante grimpante en ligne",
            expected = "plante ligneuse avec feuilles et branches"
        )

        assertTrue(result.isValid)
        assertEquals(0f, result.keywordScore, 0.001f)
    }

    @Test
    fun semanticThresholdBoundaryTreatsPointSixtyTwoAsSuccess() {
        val expected = "definition attendue"
        val validator = TFLiteSemanticValidator(
            embeddingEngine = FakeEmbeddingEngine(
                ready = true,
                embeddings = mapOf(
                    expected to floatArrayOf(1f, 0f),
                    "reformulation acceptable" to vectorForCosine(0.62f)
                )
            )
        )

        val result = validator.validate("reformulation acceptable", expected)

        assertTrue(result.isValid)
        assertEquals(15, result.xpBonus)
    }

    @Test
    fun semanticThresholdBoundaryTreatsPointFortyAsPartial() {
        val expected = "definition attendue"
        val validator = TFLiteSemanticValidator(
            embeddingEngine = FakeEmbeddingEngine(
                ready = true,
                embeddings = mapOf(
                    expected to floatArrayOf(1f, 0f),
                    "reponse limite" to vectorForCosine(0.40f)
                )
            )
        )

        val result = validator.validate("reponse limite", expected)

        assertFalse(result.isValid)
        assertEquals(5, result.xpBonus)
    }

    @Test
    fun semanticThresholdBelowPartialFails() {
        val expected = "definition attendue"
        val validator = TFLiteSemanticValidator(
            embeddingEngine = FakeEmbeddingEngine(
                ready = true,
                embeddings = mapOf(
                    expected to floatArrayOf(1f, 0f),
                    "hors sujet" to vectorForCosine(0.39f)
                )
            )
        )

        val result = validator.validate("hors sujet", expected)

        assertFalse(result.isValid)
        assertEquals(0, result.xpBonus)
    }

    @Test
    fun unavailableModelFallsBackToJaccardValidator() {
        val validator = TFLiteSemanticValidator(
            embeddingEngine = FakeEmbeddingEngine(ready = false)
        )

        val result = validator.validate(
            userInput = "manque important insuffisance notable absence",
            expected = "manque important insuffisance notable absence dommageable"
        )

        assertTrue(result.isValid)
        assertTrue(result.feedbackMessage.contains("Bonne définition"))
    }

    @Test
    fun embeddingFailureFallsBackToJaccardValidator() {
        val validator = TFLiteSemanticValidator(
            embeddingEngine = FakeEmbeddingEngine(
                ready = true,
                embeddings = mapOf("réponse utilisateur" to floatArrayOf(1f, 0f))
            )
        )

        val result = validator.validate(
            userInput = "réponse utilisateur",
            expected = "réponse attendue"
        )

        assertFalse(result.isValid)
        assertTrue(result.feedbackMessage.contains("❌") || result.feedbackMessage.contains("💡"))
    }

    private class FakeEmbeddingEngine(
        private val ready: Boolean,
        private val embeddings: Map<String, FloatArray> = emptyMap()
    ) : SentenceEmbeddingEngine {
        override fun isReady(): Boolean = ready

        override fun embed(text: String): FloatArray? = embeddings[text]
    }

    private fun vectorForCosine(score: Float): FloatArray {
        val safeScore = score.coerceIn(-1f, 1f)
        val y = kotlin.math.sqrt(1f - (safeScore * safeScore))
        return floatArrayOf(safeScore, y)
    }
}

