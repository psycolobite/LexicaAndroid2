package com.example.lexicaandroid2.presentation.wordlist

import com.example.lexicaandroid2.data.remote.DictionaryService
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.Sm2Stats
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class WordListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FlashcardRepository
    private lateinit var dictionaryService: DictionaryService
    private lateinit var viewModel: WordListViewModel

    private val card1 = Flashcard(id = "1", recto = "abscisse", verso = "coordonnee horizontale")
    private val card2 = Flashcard(id = "2", recto = "ordonnee", verso = "coordonnee verticale")
    private val knownCard = Flashcard(
        id = "3",
        recto = "ephemere",
        verso = "qui dure peu",
        sm2MotVersDef = Sm2Stats(interval = 25, repetitions = 5),
        sm2DefVersMot = Sm2Stats(interval = 25, repetitions = 5)
    )
    private val freshCard = Flashcard(
        id = "4",
        recto = "lacune",
        verso = "manque important",
        sm2MotVersDef = Sm2Stats(interval = 0, repetitions = 0),
        sm2DefVersMot = Sm2Stats(interval = 0, repetitions = 0)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        dictionaryService = mock()
        viewModel = WordListViewModel(repository, dictionaryService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region loadWords

    @Test
    fun loadWordsPopulatesCardsInState() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2))
        viewModel.loadWords()
        assertEquals(2, viewModel.uiState.value.cards.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadWordsResultsAreSortedByRecto() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card2, card1))
        viewModel.loadWords()
        assertEquals("abscisse", viewModel.uiState.value.filteredCards.first().recto)
        assertEquals("ordonnee", viewModel.uiState.value.filteredCards.last().recto)
    }

    @Test
    fun loadWordsEmptyRepositoryResultsInEmptyState() = runTest {
        whenever(repository.getAllCards()).thenReturn(emptyList())
        viewModel.loadWords()
        assertTrue(viewModel.uiState.value.cards.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)
    }

    // endregion

    // region onSearchQueryChanged

    @Test
    fun onSearchQueryChangedFiltersCardsByRecto() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2))
        viewModel.loadWords()
        viewModel.onSearchQueryChanged("abscisse")
        assertEquals(1, viewModel.uiState.value.filteredCards.size)
        assertEquals("abscisse", viewModel.uiState.value.filteredCards.first().recto)
    }

    @Test
    fun onSearchQueryChangedFiltersCardsByVerso() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2))
        viewModel.loadWords()
        viewModel.onSearchQueryChanged("verticale")
        assertEquals(1, viewModel.uiState.value.filteredCards.size)
        assertEquals("ordonnee", viewModel.uiState.value.filteredCards.first().recto)
    }

    @Test
    fun onSearchQueryChangedIsCaseInsensitive() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2))
        viewModel.loadWords()
        viewModel.onSearchQueryChanged("ABSCISSE")
        assertEquals(1, viewModel.uiState.value.filteredCards.size)
    }

    @Test
    fun onSearchQueryChangedEmptyQueryRestoresAllCards() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2))
        viewModel.loadWords()
        viewModel.onSearchQueryChanged("abscisse")
        viewModel.onSearchQueryChanged("")
        assertEquals(2, viewModel.uiState.value.filteredCards.size)
    }

    @Test
    fun onSearchQueryChangedNoMatchReturnsEmptyList() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2))
        viewModel.loadWords()
        viewModel.onSearchQueryChanged("xyzabc")
        assertTrue(viewModel.uiState.value.filteredCards.isEmpty())
    }

    // endregion

    // region onFilterSelected

    @Test
    fun onFilterSelectedKnownShowsOnlyKnownCards() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, knownCard))
        viewModel.loadWords()
        viewModel.onFilterSelected("KNOWN")
        assertEquals(1, viewModel.uiState.value.filteredCards.size)
        assertEquals("ephemere", viewModel.uiState.value.filteredCards.first().recto)
    }

    @Test
    fun onFilterSelectedToLearnShowsOnlyFreshCards() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(freshCard, knownCard))
        viewModel.loadWords()
        viewModel.onFilterSelected("TO_LEARN")
        assertEquals(1, viewModel.uiState.value.filteredCards.size)
        assertEquals("lacune", viewModel.uiState.value.filteredCards.first().recto)
    }

    @Test
    fun onFilterSelectedNullRemovesFilter() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2))
        viewModel.loadWords()
        viewModel.onFilterSelected("KNOWN")
        viewModel.onFilterSelected(null)
        assertEquals(2, viewModel.uiState.value.filteredCards.size)
    }

    @Test
    fun onFilterSelectedUpdatesSelectedFilterInState() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1))
        viewModel.loadWords()
        viewModel.onFilterSelected("LEARNING")
        assertEquals("LEARNING", viewModel.uiState.value.selectedFilter)
    }

    // endregion

    // region toggleFavorite

    @Test
    fun toggleFavoriteCallsRepositoryWithCorrectArgs() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1))
        viewModel.loadWords()
        viewModel.toggleFavorite(card1)
        verify(repository).setFavorite("1", true)
    }

    @Test
    fun toggleFavoriteOnFavoritedCardTogglesOff() = runTest {
        val favoritedCard = card1.copy(favori = true)
        whenever(repository.getAllCards()).thenReturn(listOf(favoritedCard))
        viewModel.loadWords()
        viewModel.toggleFavorite(favoritedCard)
        verify(repository).setFavorite("1", false)
    }

    // endregion

    // region deleteCard

    @Test
    fun deleteCardCallsRepositoryWithCorrectId() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2))
        viewModel.loadWords()
        whenever(repository.getAllCards()).thenReturn(listOf(card2))
        viewModel.deleteCard("1")
        verify(repository).deleteCard("1")
    }

    @Test
    fun deleteCardReloadsCardsAfterDeletion() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2))
        viewModel.loadWords()
        whenever(repository.getAllCards()).thenReturn(listOf(card2))
        viewModel.deleteCard("1")
        assertEquals(1, viewModel.uiState.value.cards.size)
        assertEquals("ordonnee", viewModel.uiState.value.cards.first().recto)
    }

    // endregion
}
