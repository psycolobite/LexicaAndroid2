package com.example.lexicaandroid2.domain.model

data class ReviewSessionState(
    val sessionOrderQuestionIds: List<String>,
    val questionStates: Map<String, ReviewSessionQuestionState>,
    val currentQuestionId: String?,
    val currentOrderIndex: Int,
    val remainingQuestionsToValidate: Int,
    val isFinished: Boolean
) {
    val currentQuestionState: ReviewSessionQuestionState?
        get() = currentQuestionId?.let(questionStates::get)
}

