package com.example.lexicaandroid2.domain.repository

import com.example.lexicaandroid2.data.local.WordReserveEntity
import com.example.lexicaandroid2.domain.model.Flashcard

interface WordReserveRepository {
    suspend fun getProposedWords(limit: Int = 100): List<WordReserveEntity>
    suspend fun countAvailable(): Int
    suspend fun addToCollection(word: WordReserveEntity) // Moves from reserve to flashcards
    suspend fun searchOnline(query: String): List<WordReserveEntity> // Uses external API, results are transient
    suspend fun addCustomWordToCollection(flashcard: Flashcard) // Add directly to collection (manual search)
}

