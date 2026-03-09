package com.example.lexicaandroid2.presentation.search

import app.cash.turbine.test
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.repository.SearchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private lateinit var repository: SearchRepository
    private lateinit var viewModel: SearchViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockFlashcards = listOf(
        Flashcard(
            id = "1",
            recto = "test",
            verso = "définition test",
            favori = false
        ),
        Flashcard(
            id = "2",
            recto = "exemple",
            verso = "définition exemple",
            favori = true
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        
        coEvery { repository.searchGlobal(any(), any()) } returns flowOf(mockFlashcards)
        coEvery { repository.searchByWord(any(), any()) } returns flowOf(mockFlashcards)
        coEvery { repository.searchByDefinition(any(), any()) } returns flowOf(mockFlashcards)
        coEvery { repository.searchFavorites(any(), any()) } returns flowOf(mockFlashcards.filter { it.favori })
        coEvery { repository.countSearchResults(any()) } returns 2
        
        viewModel = SearchViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.query)
            assertEquals(SearchType.GLOBAL, state.searchType)
            assertFalse(state.isLoading)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `debounce delays search by 300ms`() = runTest {
        viewModel.onSearchQueryChanged("test")
        
        // Avant 300ms, aucune requête ne doit être lancée
        advanceTimeBy(200)
        coVerify(exactly = 1) { repository.searchGlobal("", any()) } // Initial load only
        
        // Après 300ms, la requête doit être lancée
        advanceTimeBy(150)
        advanceUntilIdle()
        coVerify(atLeast = 1) { repository.searchGlobal("test", any()) }
    }

    @Test
    fun `multiple rapid queries trigger only one search after debounce`() = runTest {
        // Simuler une frappe rapide
        viewModel.onSearchQueryChanged("t")
        advanceTimeBy(100)
        
        viewModel.onSearchQueryChanged("te")
        advanceTimeBy(100)
        
        viewModel.onSearchQueryChanged("tes")
        advanceTimeBy(100)
        
        viewModel.onSearchQueryChanged("test")
        advanceTimeBy(100)
        
        // À ce stade, aucune recherche ne devrait avoir été déclenchée (moins de 300ms)
        coVerify(exactly = 1) { repository.searchGlobal("", any()) } // Initial only
        
        // Attendre que le debounce expire
        advanceTimeBy(250)
        advanceUntilIdle()
        
        // Maintenant une seule recherche pour "test" devrait avoir été lancée
        coVerify(exactly = 1) { repository.searchGlobal("test", any()) }
    }

    @Test
    fun `search type change triggers immediate search`() = runTest {
        viewModel.onSearchQueryChanged("test")
        advanceTimeBy(350)
        advanceUntilIdle()
        
        // Changer le type de recherche
        viewModel.onSearchTypeChanged(SearchType.BY_WORD)
        advanceUntilIdle()
        
        coVerify { repository.searchByWord("test", any()) }
    }

    @Test
    fun `search favorites filters correctly`() = runTest {
        coEvery { repository.searchFavorites(any(), any()) } returns 
            flowOf(mockFlashcards.filter { it.favori })
        
        viewModel.onSearchTypeChanged(SearchType.FAVORITES)
        viewModel.onSearchQueryChanged("exemple")
        advanceTimeBy(350)
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.results.all { it.favori })
            assertEquals(1, state.results.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty search returns paginated results`() = runTest {
        viewModel.onSearchQueryChanged("")
        advanceTimeBy(350)
        advanceUntilIdle()
        
        coVerify { repository.searchGlobal("", any()) }
    }

    @Test
    fun `clearSearch resets query and state`() = runTest {
        viewModel.onSearchQueryChanged("test")
        advanceTimeBy(350)
        advanceUntilIdle()
        
        viewModel.clearSearch()
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.query)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loading state is set during search`() = runTest {
        viewModel.uiState.test {
            skipItems(1) // Skip initial state
            
            viewModel.onSearchQueryChanged("test")
            
            // L'état de loading devrait être true immédiatement
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            
            advanceTimeBy(350)
            advanceUntilIdle()
            
            // Puis false après les résultats
            val resultState = awaitItem()
            assertFalse(resultState.isLoading)
            assertEquals(2, resultState.results.size)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state is set on repository exception`() = runTest {
        coEvery { repository.searchGlobal(any(), any()) } throws Exception("Network error")
        
        viewModel.onSearchQueryChanged("test")
        advanceTimeBy(350)
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.error?.contains("Network error") == true)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search by definition uses correct repository method`() = runTest {
        viewModel.onSearchTypeChanged(SearchType.BY_DEFINITION)
        viewModel.onSearchQueryChanged("définition")
        advanceTimeBy(350)
        advanceUntilIdle()
        
        coVerify { repository.searchByDefinition("définition", any()) }
    }

    @Test
    fun `distinctUntilChanged prevents duplicate queries`() = runTest {
        viewModel.onSearchQueryChanged("test")
        advanceTimeBy(350)
        advanceUntilIdle()
        
        // Même query
        viewModel.onSearchQueryChanged("test")
        advanceTimeBy(350)
        advanceUntilIdle()
        
        // La recherche ne devrait être appelée qu'une fois (+ initial)
        coVerify(exactly = 1) { repository.searchGlobal("test", any()) }
    }
}
