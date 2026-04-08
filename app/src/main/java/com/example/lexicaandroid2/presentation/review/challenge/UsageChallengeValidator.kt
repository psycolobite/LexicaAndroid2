package com.example.lexicaandroid2.presentation.review.challenge

private const val MIN_USAGE_WORD_COUNT = 6
private const val USAGE_PARTIAL_THRESHOLD = 0.25f

class UsageChallengeValidator(
    private val semanticValidatorProvider: () -> SemanticValidator
) {
    fun validate(
        userInput: String,
        targetWord: String,
        expectedDefinition: String,
        examples: List<String> = emptyList()
    ): ValidationResult {
        val trimmedInput = userInput.trim()
        if (trimmedInput.isBlank()) {
            return ValidationResult(
                isValid = false,
                semanticScore = 0f,
                feedbackMessage = "❌ Aucune phrase fournie"
            )
        }

        val inputTokens = KeywordExtractor.tokenize(trimmedInput)
        val targetWordTokens = KeywordExtractor.tokenize(targetWord)
        val containsTargetWord = targetWordTokens.isNotEmpty() && targetWordTokens.all { it in inputTokens }
        if (!containsTargetWord) {
            return ValidationResult(
                isValid = false,
                semanticScore = 0f,
                feedbackMessage = "💡 Utilise explicitement le mot « $targetWord » dans ta phrase."
            )
        }

        if (inputTokens.size < MIN_USAGE_WORD_COUNT) {
            return ValidationResult(
                isValid = false,
                semanticScore = 0f,
                feedbackMessage = "💡 Écris une phrase un peu plus complète pour montrer comment tu utilises ce mot."
            )
        }

        val expectedContext = buildString {
            append(expectedDefinition)
            examples.firstOrNull()?.takeIf { it.isNotBlank() }?.let {
                append(' ')
                append(it)
            }
        }

        val semanticResult = semanticValidatorProvider().validate(trimmedInput, expectedContext)
        val semanticScore = semanticResult.semanticScore.takeIf { it >= 0f } ?: semanticResult.keywordScore
        val isSemanticallyAcceptable = semanticResult.isValid || semanticResult.keywordScore >= USAGE_PARTIAL_THRESHOLD

        return if (isSemanticallyAcceptable) {
            ValidationResult(
                isValid = true,
                keywordScore = semanticResult.keywordScore,
                semanticScore = semanticScore,
                foundKeywords = semanticResult.foundKeywords,
                missingKeywords = semanticResult.missingKeywords,
                feedbackMessage = "✅ Bonne utilisation du mot dans une phrase."
            )
        } else {
            ValidationResult(
                isValid = false,
                keywordScore = semanticResult.keywordScore,
                semanticScore = semanticScore,
                foundKeywords = semanticResult.foundKeywords,
                missingKeywords = semanticResult.missingKeywords,
                feedbackMessage = "⚠️ La phrase contient bien le mot, mais l'usage semble encore trop éloigné du sens attendu."
            )
        }
    }
}

