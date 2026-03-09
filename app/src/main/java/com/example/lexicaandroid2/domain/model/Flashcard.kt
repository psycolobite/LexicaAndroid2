package com.example.lexicaandroid2.domain.model

data class Flashcard(
    val id: String,
    val recto: String,
    val verso: String,
    val synonymes: List<String> = emptyList(),
    val exemples: List<String> = emptyList(),
    val categorieGrammaticale: String = "",
    val registre: String = "",
    val etymologie: String = "",
    val dateAjout: Long = System.currentTimeMillis(),
    val favori: Boolean = false,
    val notesPersonnelles: String = "",
    val sm2MotVersDef: Sm2Stats = Sm2Stats(),
    val sm2DefVersMot: Sm2Stats = Sm2Stats()
)

data class Sm2Stats(
    val interval: Int = 0,
    val repetitions: Int = 0,
    val easeFactor: Double = 2.5,
    val nextReviewDate: Long = System.currentTimeMillis(),
    val lastReviewDate: Long? = null,
    val totalReviews: Int = 0,
    val correctReviews: Int = 0,
    val lapses: Int = 0
)

