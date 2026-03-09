package com.example.lexicaandroid2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_reserve")
data class WordReserveEntity(
    @PrimaryKey
    val id: String,
    val mot: String,
    val definition: String,
    val synonymes: List<String> = emptyList(),
    val exemples: List<String> = emptyList(),
    val categorieGrammaticale: String = "",
    val fromApi: Boolean = false,
    val addedToCollection: Boolean = false
)

