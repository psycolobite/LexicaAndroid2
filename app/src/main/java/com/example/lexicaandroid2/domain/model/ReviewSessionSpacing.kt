package com.example.lexicaandroid2.domain.model

const val MIN_INTERVENING_PRESENTATIONS_FOR_SAME_CARD_FAMILY = 2

fun ReviewQuestionProgress.toSessionSpacingKey(): String =
    "card:$cardId"

fun ReviewSessionEvent.toSessionSpacingKey(): String? {
    val resolvedCardId = cardId ?: questionId
        ?.let { candidateQuestionId ->
            candidateQuestionId.substringBefore("::", missingDelimiterValue = "")
                .takeIf { it.isNotBlank() }
        }

    return when (type) {
        ReviewSessionEventType.EXTRA_SPELLING -> resolvedCardId?.let { "card:$it" }
        ReviewSessionEventType.CHALLENGE -> when (challengeKind) {
            ReviewSessionChallengeKind.SPELLING,
            ReviewSessionChallengeKind.SEMANTIC -> resolvedCardId?.let { "card:$it" }
            ReviewSessionChallengeKind.USAGE,
            null -> "event:$eventId"
        }
        ReviewSessionEventType.QCM,
        ReviewSessionEventType.MATCHING -> "event:$eventId"
    }
}

