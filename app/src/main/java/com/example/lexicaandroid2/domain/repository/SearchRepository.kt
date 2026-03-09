package com.example.lexicaandroid2.domain.repository

import com.example.lexicaandroid2.domain.model.Flashcard
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchByWord(query: String, limit: Int = 50): Flow<List<Flashcard>>
    fun searchByDefinition(query: String, limit: Int = 50): Flow<List<Flashcard>>
    fun searchGlobal(query: String, limit: Int = 50): Flow<List<Flashcard>>
    fun searchFavorites(query: String, limit: Int = 50): Flow<List<Flashcard>>
    fun getAllPaginated(limit: Int = 50, offset: Int = 0): Flow<List<Flashcard>>
    suspend fun countSearchResults(query: String): Int
}
