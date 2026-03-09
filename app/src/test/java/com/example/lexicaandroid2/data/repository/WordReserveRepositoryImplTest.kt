package com.example.lexicaandroid2.data.repository

import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.WordReserveDao
import com.example.lexicaandroid2.data.remote.DictionaryService
import com.example.lexicaandroid2.data.remote.model.WordResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class WordReserveRepositoryImplTest {
    private val reserveDao: WordReserveDao = mock()
    private val flashcardDao: FlashcardDao = mock()
    private val dictionaryService: DictionaryService = mock()
    private val repository = WordReserveRepositoryImpl(reserveDao, flashcardDao, dictionaryService)

    @Test
    fun testSearchOnline_WithValidWord() = runTest {
        val query = "abnégation"
        val result = WordResult(
            mot = query,
            definition = "Action de se sacrifier volontairement.",
            categorieGrammaticale = "Nom féminin",
            exemples = listOf("Elle agit avec abnégation."),
            synonymes = listOf("dévouement"),
            source = "Wiktionnaire"
        )
        whenever(dictionaryService.searchWord(query)).thenReturn(listOf(result))

        val results = repository.searchOnline(query)

        assertTrue(results.isNotEmpty())
        assertEquals(query, results.first().mot)
    }

    @Test
    fun testSearchOnline_WithInvalidWord() = runTest {
        val query = "xyzabc"
        whenever(dictionaryService.searchWord(query)).thenReturn(emptyList())

        val results = repository.searchOnline(query)

        assertTrue(results.isEmpty())
    }

    @Test
    fun testSearchOnline_WithNetworkError() = runTest {
        val query = "abnégation"
        whenever(dictionaryService.searchWord(query)).thenThrow(RuntimeException("Network error"))

        val results = repository.searchOnline(query)

        assertTrue(results.isEmpty())
    }
}
