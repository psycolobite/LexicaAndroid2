package com.example.lexicaandroid2.presentation.review

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.core.text.HtmlCompat
import com.example.lexicaandroid2.domain.model.Flashcard

internal fun promptTitleForNormal(mode: ReviewPresentationMode): String = when (mode) {
    ReviewPresentationMode.WORD_TO_DEFINITION -> "Quelle est la définition du mot ?"
    ReviewPresentationMode.DEFINITION_TO_WORD -> "À quel mot correspond cette définition ?"
    ReviewPresentationMode.SPELLING_CHALLENGE -> "Défi orthographique"
    ReviewPresentationMode.SEMANTIC_CHALLENGE -> "Défi sémantique"
}

internal fun buildReviewCardDisplay(card: Flashcard, mode: ReviewPresentationMode): ReviewCardDisplay {
    val word = decodeReviewText(card.recto)
    val definition = decodeReviewText(card.verso)

    return when (mode) {
        ReviewPresentationMode.WORD_TO_DEFINITION -> ReviewCardDisplay(word, word, definition, true, false)
        ReviewPresentationMode.DEFINITION_TO_WORD -> ReviewCardDisplay(definition, definition, word, false, false)
        ReviewPresentationMode.SPELLING_CHALLENGE -> ReviewCardDisplay(word, word, definition, true, false)
        ReviewPresentationMode.SEMANTIC_CHALLENGE -> ReviewCardDisplay(definition, definition, word, false, false)
    }
}

internal fun decodeReviewText(value: String): String {
    return HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_LEGACY)
        .toString()
        .replace('\u00A0', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
}

internal fun sanitizeAdminTestMessage(message: String?): String? {
    return message
        ?.replace(" (mode test admin)", "")
        ?.trim()
}

internal data class ReviewCardDisplay(
    val frontText: String,
    val answerText: String,
    val supportingText: String,
    val frontUsesSerif: Boolean,
    val showSupportingFirst: Boolean
)

internal object ReviewButtonColors {
    val Error = Color(0xFFB3261E)
    val Ok = Color(0xFF2E7D32)
    val Easy = Color(0xFF1565C0)
}

internal data class ConfettiPiece(
    val xFraction: Float,
    val yFraction: Float,
    val size: Dp,
    val color: Color,
    val rotation: Float
)

