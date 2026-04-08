package com.example.lexicaandroid2.data.remote

import com.example.lexicaandroid2.data.remote.model.WordResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Normalizer

class DictionaryServiceImpl(
    private val sources: List<DictionaryLookupSource> = listOf(
        WiktionnaireApiSource(),
        WiktionnaireScraper()
    )
) : DictionaryService {

    override suspend fun searchWord(query: String): List<WordResult> {
        return withContext(Dispatchers.IO) {
            val trimmed = query.trim()
            if (trimmed.isBlank()) {
                return@withContext emptyList()
            }

            sources
                .flatMap { source ->
                    runCatching { source.search(trimmed) }
                        .getOrDefault(emptyList())
                }
                .filter { it.mot.isNotBlank() && it.definition.isNotBlank() }
                .distinctBy { result -> normalize(result.mot) + "|" + normalize(result.definition) }
                .sortedWith(
                    compareByDescending<WordResult> { normalize(it.mot) == normalize(trimmed) }
                        .thenBy { it.source }
                )
                .take(MAX_RESULTS)
        }
    }

    private fun normalize(value: String): String {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
            .trim()
    }

    companion object {
        private const val MAX_RESULTS = 6
    }
}

