package com.example.lexicaandroid2.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey
    val id: String,
    val mot: String,
    val definition: String,
    val synonymes: List<String> = emptyList(),
    val exemples: List<String> = emptyList(),
    val categorieGrammaticale: String = "",
    val registre: String = "",
    val etymologie: String = "",
    val dateAjout: Long = System.currentTimeMillis(),
    val favori: Boolean = false,
    val notesPersonnelles: String = "",
    val state: String = "TO_LEARN",
    @Embedded(prefix = "sm2_mot_vers_def_")
    val sm2MotVersDef: Sm2DataEmbedded = Sm2DataEmbedded(),
    @Embedded(prefix = "sm2_def_vers_mot_")
    val sm2DefVersMot: Sm2DataEmbedded = Sm2DataEmbedded()
)

data class Sm2DataEmbedded(
    val interval: Int = 0,
    val easeFactor: Double = 2.5,
    val repetitions: Int = 0,
    val nextReview: Long = System.currentTimeMillis(),
    val lastReview: Long? = null,
    val totalReviews: Int = 0,
    val correctReviews: Int = 0,
    val lapses: Int = 0
)
