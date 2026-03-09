package com.example.lexicaandroid2.data.remote.model

data class WordResult(
    val mot: String,
    val definition: String,
    val categorieGrammaticale: String = "",
    val exemples: List<String> = emptyList(),
    val synonymes: List<String> = emptyList(),
    val source: String = ""
)

