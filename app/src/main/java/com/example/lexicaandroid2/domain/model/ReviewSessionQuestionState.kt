package com.example.lexicaandroid2.domain.model

data class ReviewSessionQuestionState(
    val progress: ReviewQuestionProgress,
    val firstAnswer: ReviewAnswer? = null,
    val firstAnswerAt: Long? = null,
    val lastAnswer: ReviewAnswer? = null,
    val lastAnsweredAt: Long? = null,
    val presentationCount: Int = 0,
    val gotItCount: Int = 0,
    val consecutiveGotItCount: Int = 0,
    val againCount: Int = 0,
    val qcmAlreadyScheduled: Boolean = false,
    val extraSpellingAlreadyScheduled: Boolean = false,
    val challengeAlreadyScheduled: Boolean = false,
    val isValidated: Boolean = false,
    val validationReason: ReviewSessionValidationReason? = null
) {
    val effectiveLongTermAnswer: ReviewAnswer?
        get() = when (validationReason) {
            ReviewSessionValidationReason.FIVE_AGAIN -> ReviewAnswer.AGAIN
            else -> firstAnswer
        }
}

