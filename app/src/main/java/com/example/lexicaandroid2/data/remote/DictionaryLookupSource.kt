package com.example.lexicaandroid2.data.remote

import com.example.lexicaandroid2.data.remote.model.WordResult

interface DictionaryLookupSource {
    suspend fun search(query: String): List<WordResult>
}

