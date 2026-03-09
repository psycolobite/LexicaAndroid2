package com.example.lexicaandroid2.data.remote

import com.example.lexicaandroid2.data.remote.model.WordResult
import kotlinx.coroutines.flow.Flow

interface DictionaryService {
    suspend fun searchWord(query: String): List<WordResult>
}

