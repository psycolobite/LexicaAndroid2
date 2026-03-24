package com.example.lexicaandroid2.presentation.review.challenge

import android.content.Context

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
 * Validateur Jaccard TF-IDF — Jaccard score sur mots-clés
 * 
 * Avantages : Aucune dépendance, rapide, déterministe
 * Inconvénients : Synonymes non reconnus (ex: "auto" vs "voiture")
 */
class JaccardSemanticValidator : SemanticValidator {
    override fun validate(userInput: String, expected: String): ValidationResult {
        val (found, missing) = KeywordExtractor.analyzeKeywords(userInput, expected, topN = 5)
        val allKeywords = found + missing
        
        val keywordScore = if (allKeywords.isEmpty()) {
            if (userInput.isNotBlank()) 1f else 0f
        } else {
            found.size.toFloat() / allKeywords.size.toFloat()
        }

        val jaccardScore = KeywordExtractor.jaccardScore(userInput, expected)
        
        // Règle de validation (sans modèle TFLite) :
        val (isValid, xpBonus, feedbackMessage) = when {
            keywordScore >= 0.6f -> {
                val msg = "✅ Bonne définition ! Mots-clés trouvés : ${found.joinToString(", ")}"
                Triple(true, 15, msg)
            }
            keywordScore >= 0.3f -> {
                val msg = "💡 Presque ! Il manquait : ${missing.joinToString(", ")}"
                Triple(false, 5, msg)
            }
            else -> {
                val msg = "❌ Mots-clés manquants : ${missing.joinToString(", ")}"
                Triple(false, 0, msg)
            }
        }

        return ValidationResult(
            isValid = isValid,
            keywordScore = keywordScore,
            semanticScore = jaccardScore,
            foundKeywords = found,
            missingKeywords = missing,
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
 * Factory pour créer le validateur approprié
 * 
 * Logique :
 * - Si TFLite modèle disponible → TFLiteSemanticValidator (meilleure qualité)
 * - Sinon → JaccardSemanticValidator (fallback rapide)
 * - Pour Spelling → toujours SpellingValidator
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
