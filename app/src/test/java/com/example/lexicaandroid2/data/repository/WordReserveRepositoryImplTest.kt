package com.example.lexicaandroid2.data.repository

import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.ReviewQuestionDao
import com.example.lexicaandroid2.data.local.WordReserveEntity
import com.example.lexicaandroid2.data.local.WordReserveDao
import com.example.lexicaandroid2.data.remote.DictionaryService
import com.example.lexicaandroid2.data.remote.model.WordResult
import com.example.lexicaandroid2.domain.model.Flashcard
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class WordReserveRepositoryImplTest {
    private val reserveDao: WordReserveDao = mock()
    private val flashcardDao: FlashcardDao = mock()
    private val reviewQuestionDao: ReviewQuestionDao = mock()
    private val dictionaryService: DictionaryService = mock()
    private val repository = WordReserveRepositoryImpl(reserveDao, flashcardDao, reviewQuestionDao, dictionaryService)

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

    @Test
    fun addToCollectionCreatesTwoQuestionProgressRows() = runTest {
        val word = WordReserveEntity(
            id = "word-1",
            mot = "abnégation",
            definition = "Action de se sacrifier volontairement."
        )

        repository.addToCollection(word)

        verify(flashcardDao).insert(any())
        verify(reviewQuestionDao).insertAll(
            argThat { size == 2 && any { it.questionId == "word-1::WORD_TO_DEFINITION" } && any { it.questionId == "word-1::DEFINITION_TO_WORD" } }
        )
        verify(reserveDao).delete(word)
    }

    @Test
    fun addCustomWordToCollectionCreatesTwoQuestionProgressRows() = runTest {
        val flashcard = Flashcard(
            id = "custom-1",
            recto = "sagace",
            verso = "Qui juge avec finesse.",
            dateAjout = 1_700_000_000_000L
        )

        repository.addCustomWordToCollection(flashcard)

        verify(flashcardDao).insert(any())
        verify(reviewQuestionDao).insertAll(
            argThat { size == 2 && any { it.cardId == "custom-1" } }
        )
    }
}
