package com.example.lexicaandroid2.presentation.review.challenge

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageChallengeValidatorTest {

    @Test
    fun validateRejectsWhenTargetWordIsMissing() {
        val validator = UsageChallengeValidator { JaccardSemanticValidator() }

        val result = validator.validate(
            userInput = "Cette phrase parle du sens sans reprendre le terme attendu.",
            targetWord = "aporie",
            expectedDefinition = "impasse dans un raisonnement"
        )

        assertFalse(result.isValid)
        assertTrue(result.feedbackMessage.contains("Utilise explicitement"))
    }

    @Test
    fun validateRejectsWhenSentenceIsTooShort() {
        val validator = UsageChallengeValidator { JaccardSemanticValidator() }

        val result = validator.validate(
            userInput = "Aporie dans un débat.",
            targetWord = "aporie",
            expectedDefinition = "impasse dans un raisonnement"
        )

        assertFalse(result.isValid)
        assertTrue(result.feedbackMessage.contains("phrase un peu plus complète"))
    }

    @Test
    fun validateAcceptsSentenceWhenWordIsPresentAndMeaningIsCoherent() {
        val validator = UsageChallengeValidator { JaccardSemanticValidator() }

        val result = validator.validate(
            userInput = "Cette aporie montre une véritable impasse dans le raisonnement du philosophe.",
            targetWord = "aporie",
            expectedDefinition = "impasse dans un raisonnement"
        )

        assertTrue(result.isValid)
        assertTrue(result.feedbackMessage.contains("Bonne utilisation"))
    }
}

