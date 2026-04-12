package com.example.lexicaandroid2.presentation.addwords

import com.example.lexicaandroid2.data.local.WordReserveEntity
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.domain.repository.WordReserveRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AddWordsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var wordReserveRepository: WordReserveRepository
    private lateinit var flashcardRepository: FlashcardRepository

    private val suggestedWord = WordReserveEntity(
        id = "reserve-1",
        mot = "abnégation",
        definition = "Sacrifice de soi."
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        wordReserveRepository = mock()
        flashcardRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addedSuggestedWordStaysVisibleDuringSessionButDisappearsAfterScreenReentry() = runTest {
        whenever(flashcardRepository.getAllCards())
            .thenReturn(emptyList(), listOf(suggestedWord.toFlashcard()))
        whenever(wordReserveRepository.getProposedWords(50))
            .thenReturn(listOf(suggestedWord))

        val viewModel = AddWordsViewModel(wordReserveRepository, flashcardRepository)
        advanceUntilIdle()

        assertEquals(listOf(suggestedWord), viewModel.uiState.value.proposedWords)

        viewModel.addWordFromReserve(suggestedWord)
        advanceUntilIdle()

        assertEquals(listOf(suggestedWord), viewModel.uiState.value.proposedWords)
        assertTrue(viewModel.uiState.value.addedInSession.containsKey(suggestedWord.mot.trim()))

        viewModel.onScreenEntered()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.addedInSession.isEmpty())
        assertTrue(viewModel.uiState.value.proposedWords.isEmpty())
    }

    @Test
    fun searchStillShowsExistingOwnedWordInLocalMatches() = runTest {
        whenever(flashcardRepository.getAllCards()).thenReturn(listOf(suggestedWord.toFlashcard()))
        whenever(wordReserveRepository.getProposedWords(50)).thenReturn(listOf(suggestedWord))
        whenever(wordReserveRepository.searchOnline("abn")).thenReturn(emptyList())

        val viewModel = AddWordsViewModel(wordReserveRepository, flashcardRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.proposedWords.isEmpty())

        viewModel.onSearchQueryChanged("abn")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.localMatches.size)
        assertEquals(suggestedWord.id, viewModel.uiState.value.localMatches.first().id)
        assertEquals(suggestedWord.mot, viewModel.uiState.value.localMatches.first().recto)
    }

    @Test
    fun clearSearchPreventsStaleApiResultsFromPreviousQuery() = runTest {
        whenever(flashcardRepository.getAllCards()).thenReturn(emptyList())
        whenever(wordReserveRepository.getProposedWords(50)).thenReturn(listOf(suggestedWord))
        whenever(wordReserveRepository.searchOnline("abn")).thenAnswer {
            runBlocking {
                delay(1_000)
                listOf(suggestedWord)
            }
        }

        val viewModel = AddWordsViewModel(wordReserveRepository, flashcardRepository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("abn")
        advanceTimeBy(500)
        advanceUntilIdle()

        viewModel.clearSearch()
        advanceTimeBy(1_000)
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.searchQuery)
        assertTrue(viewModel.uiState.value.localMatches.isEmpty())
        assertTrue(viewModel.uiState.value.apiResults.isEmpty())
        assertTrue(!viewModel.uiState.value.isApiLoading)
    }

    @Test
    fun onScreenEnteredResetsPreviousSearchQueryAndResults() = runTest {
        whenever(flashcardRepository.getAllCards()).thenReturn(emptyList(), emptyList())
        whenever(wordReserveRepository.getProposedWords(50)).thenReturn(listOf(suggestedWord), listOf(suggestedWord))
        whenever(wordReserveRepository.searchOnline("abn")).thenReturn(listOf(suggestedWord))

        val viewModel = AddWordsViewModel(wordReserveRepository, flashcardRepository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("abn")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertEquals("abn", viewModel.uiState.value.searchQuery)

        viewModel.onScreenEntered()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.searchQuery)
        assertTrue(viewModel.uiState.value.localMatches.isEmpty())
        assertTrue(viewModel.uiState.value.apiResults.isEmpty())
        assertTrue(!viewModel.uiState.value.isApiLoading)
    }

    private fun WordReserveEntity.toFlashcard() = Flashcard(
        id = id,
        recto = mot,
        verso = definition,
        synonymes = synonymes,
        exemples = exemples,
        categorieGrammaticale = categorieGrammaticale
    )
}


