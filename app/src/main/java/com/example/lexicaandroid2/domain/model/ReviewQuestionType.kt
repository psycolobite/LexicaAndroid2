package com.example.lexicaandroid2.domain.model

enum class ReviewQuestionType {
    WORD_TO_DEFINITION,
    DEFINITION_TO_WORD;

    fun questionId(cardId: String): String = "$cardId::$name"

    val globalOrderOffset: Long
        get() = when (this) {
            WORD_TO_DEFINITION -> 0L
            DEFINITION_TO_WORD -> 1L
        }

    companion object {
        fun fromStorage(value: String): ReviewQuestionType =
            entries.firstOrNull { it.name == value }
                ?: error("Unknown ReviewQuestionType: $value")
    }
}

