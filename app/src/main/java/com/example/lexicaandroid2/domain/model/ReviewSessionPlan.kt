package com.example.lexicaandroid2.domain.model

data class ReviewSessionPlan(
    val selectedQuestions: List<ReviewQuestionProgress>,
    val sessionOrder: List<ReviewQuestionProgress>,
    val remainingQuestionsCount: Int
)

