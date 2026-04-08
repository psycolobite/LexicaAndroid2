package com.example.lexicaandroid2.presentation.review.challenge

import android.content.Context

private const val LEXICAL_SUCCESS_THRESHOLD = 0.50f
private const val LEXICAL_PARTIAL_THRESHOLD = 0.25f

data class ValidationResult(
    val isValid: Boolean,
    val keywordScore: Float = 0f,
    val semanticScore: Float = -1f,
    val foundKeywords: List<String> = emptyList(),
    val missingKeywords: List<String> = emptyList(),
    val xpBonus: Int = 0,
    val feedbackMessage: String = ""
)

interface SemanticValidator {
    fun validate(userInput: String, expected: String): ValidationResult
    fun isModelReady(): Boolean
}

/**
 * Validateur lexical de secours.
 *
 * Avantages : aucune dépendance, rapide, déterministe.
 * Limites : reste moins bon qu'un vrai modèle sémantique pour les reformulations libres.
 */
class JaccardSemanticValidator : SemanticValidator {
    override fun validate(userInput: String, expected: String): ValidationResult {
        if (userInput.isBlank()) {
            return ValidationResult(
                isValid = false,
                keywordScore = 0f,
                semanticScore = 0f,
                foundKeywords = emptyList(),
                missingKeywords = emptyList(),
                xpBonus = 0,
                feedbackMessage = "❌ Aucune réponse fournie"
            )
        }

        val lexicalScore = KeywordExtractor.lexicalFallbackScore(userInput, expected)

        val (isValid, xpBonus, feedbackMessage) = when {
            lexicalScore >= LEXICAL_SUCCESS_THRESHOLD -> {
                Triple(true, 15, "✅ Bonne définition !")
            }
            lexicalScore >= LEXICAL_PARTIAL_THRESHOLD -> {
                Triple(false, 5, "💡 Presque ! Reformule encore un peu ta réponse.")
            }
            else -> {
                Triple(false, 0, "❌ La réponse est trop éloignée du sens attendu.")
            }
        }

        return ValidationResult(
            isValid = isValid,
            keywordScore = lexicalScore,
            semanticScore = lexicalScore,
            foundKeywords = emptyList(),
            missingKeywords = emptyList(),
            xpBonus = xpBonus,
            feedbackMessage = feedbackMessage
        )
    }

    override fun isModelReady(): Boolean = true
}

/**
 * Validateur Spelling — Comparaison exacte du mot
 * 
 * Utilisé pour les défis orthographiques (case-insensitive)
 */
class SpellingValidator : SemanticValidator {
    override fun validate(userInput: String, expected: String): ValidationResult {
        val userNormalized = userInput.trim().lowercase()
        val expectedNormalized = expected.trim().lowercase()
        
        val isValid = userNormalized == expectedNormalized
        val feedbackMessage = if (isValid) {
            "✅ Excellente orthographe !"
        } else {
            "❌ L'orthographe correcte : $expected"
        }

        return ValidationResult(
            isValid = isValid,
            keywordScore = if (isValid) 1f else 0f,
            semanticScore = -1f,
            foundKeywords = if (isValid) listOf(expected) else emptyList(),
            missingKeywords = if (!isValid) listOf(expected) else emptyList(),
            xpBonus = if (isValid) 10 else 0,
            feedbackMessage = feedbackMessage
        )
    }

    override fun isModelReady(): Boolean = true
}

/**
 * Factory pour créer le validateur approprié.
 *
 * Logique :
 * - Si le bundle embeddings on-device est disponible → `TFLiteSemanticValidator`
 * - Sinon → `JaccardSemanticValidator` (fallback déterministe)
 * - Pour Spelling → toujours `SpellingValidator`
 */
object SemanticValidatorFactory {
    fun createSemanticValidator(context: Context): SemanticValidator {
        val modelManager = ModelDownloadManager(context)
        
        return if (modelManager.isModelCached()) {
            TFLiteSemanticValidator(context, modelManager)
        } else {
            JaccardSemanticValidator()
        }
    }
    
    fun createSemanticValidatorWithTFLite(
        context: Context,
        modelManager: ModelDownloadManager
    ): TFLiteSemanticValidator = TFLiteSemanticValidator(context, modelManager)
    
    fun createJaccardValidator(): JaccardSemanticValidator = JaccardSemanticValidator()
    
    fun createSpellingValidator(): SpellingValidator = SpellingValidator()
}
