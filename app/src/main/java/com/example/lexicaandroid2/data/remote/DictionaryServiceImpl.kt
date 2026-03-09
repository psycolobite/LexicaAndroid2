package com.example.lexicaandroid2.data.remote

import com.example.lexicaandroid2.data.remote.model.WordResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DictionaryServiceImpl : DictionaryService {
    private val scraper = WiktionnaireScraper()

    override suspend fun searchWord(query: String): List<WordResult> {
        return withContext(Dispatchers.IO) {
            val result = scraper.search(query)
            if (result != null) listOf(result) else emptyList()
        }
    }
}

