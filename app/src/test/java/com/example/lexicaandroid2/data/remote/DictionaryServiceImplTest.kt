package com.example.lexicaandroid2.data.remote

import com.example.lexicaandroid2.data.remote.model.WordResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryServiceImplTest {

    @Test
    fun searchWord_returnsEmptyList_whenQueryIsBlank() = runTest {
        val firstSource = FakeLookupSource(results = listOf(sampleResult()))
        val service = DictionaryServiceImpl(listOf(firstSource))

        val results = service.searchWord("   ")

        assertTrue(results.isEmpty())
        assertEquals(emptyList<String>(), firstSource.queries)
    }

    @Test
    fun searchWord_keepsFallbackResults_whenFirstSourceFails() = runTest {
        val service = DictionaryServiceImpl(
            listOf(
                FakeLookupSource(error = IllegalStateException("boom")),
                FakeLookupSource(results = listOf(sampleResult(source = "Fallback")))
            )
        )

        val results = service.searchWord("abnégation")

        assertEquals(1, results.size)
        assertEquals("Fallback", results.first().source)
    }

    @Test
    fun searchWord_deduplicatesSameEntryReturnedByMultipleSources() = runTest {
        val duplicated = sampleResult(source = "API")
        val service = DictionaryServiceImpl(
            listOf(
                FakeLookupSource(results = listOf(duplicated)),
                FakeLookupSource(results = listOf(duplicated.copy(source = "Scraper")))
            )
        )

        val results = service.searchWord("abnegation")

        assertEquals(1, results.size)
        assertEquals("abnégation", results.first().mot)
    }

    @Test
    fun searchWord_prioritizesExactWordMatch_beforeCloseSuggestions() = runTest {
        val service = DictionaryServiceImpl(
            listOf(
                FakeLookupSource(
                    results = listOf(
                        sampleResult(mot = "abnégationnaire", definition = "Mot inventé", source = "API"),
                        sampleResult(mot = "abnégation", definition = "Action de se sacrifier volontairement.", source = "API")
                    )
                )
            )
        )

        val results = service.searchWord("abnégation")

        assertEquals("abnégation", results.first().mot)
    }

    private fun sampleResult(
        mot: String = "abnégation",
        definition: String = "Action de se sacrifier volontairement.",
        source: String = "Wiktionnaire API"
    ) = WordResult(
        mot = mot,
        definition = definition,
        categorieGrammaticale = "Nom féminin",
        exemples = listOf("Elle agit avec abnégation."),
        synonymes = listOf("dévouement"),
        source = source
    )

    private class FakeLookupSource(
        private val results: List<WordResult> = emptyList(),
        private val error: Throwable? = null
    ) : DictionaryLookupSource {
        val queries = mutableListOf<String>()

        override suspend fun search(query: String): List<WordResult> {
            queries += query
            error?.let { throw it }
            return results
        }
    }
}

