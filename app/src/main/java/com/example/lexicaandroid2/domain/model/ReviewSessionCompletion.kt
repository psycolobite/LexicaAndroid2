package com.example.lexicaandroid2.domain.model

data class ReviewSessionCompletion(
    val finalQuestionProgress: List<ReviewQuestionProgress>,
    val finalQuestionStates: List<ReviewSessionQuestionState>
)

