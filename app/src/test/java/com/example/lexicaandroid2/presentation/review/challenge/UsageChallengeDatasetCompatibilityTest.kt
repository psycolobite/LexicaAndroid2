package com.example.lexicaandroid2.presentation.review.challenge

import org.junit.Assert.assertTrue
import org.junit.Test

class UsageChallengeDatasetCompatibilityTest {

    private enum class ExpectedVerdict {
        ACCEPT,
        BORDERLINE,
        REJECT
    }

    private data class DatasetRow(
        val caseId: String,
        val targetWord: String,
        val expectedDefinition: String,
        val exampleHint: String,
        val candidateSentence: String,
        val expectedVerdict: ExpectedVerdict,
        val notes: String
    )

    @Test
    fun datasetConcordanceStaysHighAndFalsePositivesStayLow() {
        val validator = UsageChallengeValidator { JaccardSemanticValidator() }
        val rows = loadDataset()

        val mismatches = mutableListOf<String>()
        var exactMatches = 0
        var toleratedMatches = 0
        var falsePositives = 0

        rows.forEach { row ->
            val result = validator.validate(
                userInput = row.candidateSentence,
                targetWord = row.targetWord,
                expectedDefinition = row.expectedDefinition,
                examples = listOfNotNull(row.exampleHint.takeIf { it.isNotBlank() })
            )

            val actualVerdict = classify(result)
            val exact = actualVerdict == row.expectedVerdict
            val tolerated = isTolerated(row.expectedVerdict, actualVerdict)
            val isFalsePositive = row.expectedVerdict == ExpectedVerdict.REJECT && actualVerdict == ExpectedVerdict.ACCEPT

            when {
                exact -> exactMatches++
                tolerated -> toleratedMatches++
                else -> mismatches += buildString {
                    append(row.caseId)
                    append(" | attendu=")
                    append(row.expectedVerdict)
                    append(" | obtenu=")
                    append(actualVerdict)
                    append(" | phrase=")
                    append(row.candidateSentence)
                    append(" | note=")
                    append(row.notes)
                    append(" | feedback=")
                    append(result.feedbackMessage)
                    append(" | semanticScore=")
                    append(result.semanticScore)
                    append(" | keywordScore=")
                    append(result.keywordScore)
                }
            }

            if (isFalsePositive) falsePositives++
        }

        val weightedScore = (exactMatches + (toleratedMatches * 0.5f)) / rows.size.toFloat()
        val summary = buildString {
            appendLine("Concordance défi utilisation insuffisante")
            appendLine("Rows=${rows.size}")
            appendLine("Exact=$exactMatches")
            appendLine("Tolérés=$toleratedMatches")
            appendLine("FalsePositives=$falsePositives")
            appendLine("WeightedScore=$weightedScore")
            if (mismatches.isNotEmpty()) {
                appendLine("Divergences:")
                mismatches.take(10).forEach { appendLine(it) }
            }
        }

        assertTrue(summary, falsePositives == 0)
        assertTrue(summary, weightedScore >= 0.85f)
    }

    private fun classify(result: ValidationResult): ExpectedVerdict = when {
        result.isValid -> ExpectedVerdict.ACCEPT
        result.feedbackMessage.contains("plus complète", ignoreCase = true) -> ExpectedVerdict.REJECT
        result.feedbackMessage.contains("Utilise explicitement", ignoreCase = true) -> ExpectedVerdict.REJECT
        result.keywordScore >= 0.20f || result.semanticScore >= 0.20f -> ExpectedVerdict.BORDERLINE
        else -> ExpectedVerdict.REJECT
    }

    private fun isTolerated(expected: ExpectedVerdict, actual: ExpectedVerdict): Boolean = when {
        expected == ExpectedVerdict.BORDERLINE && actual == ExpectedVerdict.REJECT -> true
        expected == ExpectedVerdict.BORDERLINE && actual == ExpectedVerdict.ACCEPT -> true
        expected == ExpectedVerdict.ACCEPT && actual == ExpectedVerdict.BORDERLINE -> true
        else -> false
    }

    private fun loadDataset(): List<DatasetRow> {
        val stream = checkNotNull(javaClass.getResourceAsStream("/usage_challenge_dataset.csv")) {
            "Fichier usage_challenge_dataset.csv introuvable"
        }
        return stream.bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines
                .drop(1)
                .filter { it.isNotBlank() }
                .map { parseRow(it) }
                .toList()
        }
    }

    private fun parseRow(line: String): DatasetRow {
        val parts = line.split(';').map { it.trim() }
        require(parts.size >= 8) { "Ligne CSV invalide: $line" }
        return DatasetRow(
            caseId = parts[0],
            targetWord = parts[2],
            expectedDefinition = parts[3],
            exampleHint = parts[4],
            candidateSentence = parts[5],
            expectedVerdict = ExpectedVerdict.valueOf(parts[6].uppercase()),
            notes = parts[7]
        )
    }
}

