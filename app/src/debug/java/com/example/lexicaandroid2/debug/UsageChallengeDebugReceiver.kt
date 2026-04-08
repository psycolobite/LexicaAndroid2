package com.example.lexicaandroid2.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Base64
import com.example.lexicaandroid2.presentation.review.challenge.JaccardSemanticValidator
import com.example.lexicaandroid2.presentation.review.challenge.SemanticValidator
import com.example.lexicaandroid2.presentation.review.challenge.SemanticValidatorFactory
import com.example.lexicaandroid2.presentation.review.challenge.UsageChallengeValidator
import com.example.lexicaandroid2.presentation.review.challenge.ValidationResult
import org.json.JSONObject
import java.io.File

class UsageChallengeDebugReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val word = readTextExtra(intent, "word")
        val definition = readTextExtra(intent, "definition")
        val example = readTextExtra(intent, "example")
        val sentence = readTextExtra(intent, "sentence")
        val validatorMode = intent.getStringExtra("validator_mode")?.lowercase().orEmpty()

        val semanticValidator = createValidator(context, validatorMode)
        val usageValidator = UsageChallengeValidator { semanticValidator }
        val result = usageValidator.validate(
            userInput = sentence,
            targetWord = word,
            expectedDefinition = definition,
            examples = listOfNotNull(example.takeIf { it.isNotBlank() })
        )

        val payload = JSONObject().apply {
            put("word", word)
            put("definition", definition)
            put("example", example)
            put("sentence", sentence)
            put("validatorMode", if (validatorMode.isBlank()) "jaccard" else validatorMode)
            put("isValid", result.isValid)
            put("keywordScore", result.keywordScore.toDouble())
            put("semanticScore", result.semanticScore.toDouble())
            put("feedbackMessage", result.feedbackMessage)
            put("verdict", classify(result))
        }.toString()

        File(context.filesDir, RESULT_FILE_NAME).writeText(payload)
        resultData = payload
    }

    private fun createValidator(context: Context, mode: String): SemanticValidator = when (mode) {
        "auto" -> SemanticValidatorFactory.createSemanticValidator(context.applicationContext)
        else -> JaccardSemanticValidator()
    }

    private fun readTextExtra(intent: Intent, key: String): String {
        val direct = intent.getStringExtra(key)
        if (!direct.isNullOrBlank()) return direct

        val encoded = intent.getStringExtra("${key}_b64")
        if (encoded.isNullOrBlank()) return ""

        return String(Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP), Charsets.UTF_8)
    }

    private fun classify(result: ValidationResult): String = when {
        result.isValid -> "ACCEPT"
        result.feedbackMessage.contains("plus complète", ignoreCase = true) -> "REJECT"
        result.feedbackMessage.contains("Utilise explicitement", ignoreCase = true) -> "REJECT"
        result.feedbackMessage.contains("N'explique pas le mot", ignoreCase = true) -> "REJECT"
        result.feedbackMessage.contains("mal employé", ignoreCase = true) -> "REJECT"
        result.keywordScore >= 0.20f || result.semanticScore >= 0.20f -> "BORDERLINE"
        else -> "REJECT"
    }

    companion object {
        private const val RESULT_FILE_NAME = "usage_challenge_last_result.json"
    }
}

