package com.example.lexicaandroid2.presentation.search.explore

import com.example.lexicaandroid2.data.corpus.CorpusIndex
import com.example.lexicaandroid2.data.corpus.ExtractCandidate
import com.example.lexicaandroid2.presentation.search.preferences.UserObjective
import com.example.lexicaandroid2.presentation.search.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var corpusIndex: CorpusIndex
    private lateinit var viewModel: ExploreViewModel

    private val candidate1 = ExtractCandidate(
        id = "1",
        sourceId = "src1",
        content = "Ceci est un premier extrait de texte très intéressant.",
        startPosition = 0,
        endPosition = 53,
        wordCount = 9,
        suggestedWords = listOf("premier", "extrait", "intéressant"),
        domainTags = listOf("general"),
        registerTags = listOf("courant"),
        difficulty = "moyen",
        contextQuality = 0.8f,
        hasCompleteSource = false
    )

    private val candidate2 = ExtractCandidate(
        id = "2",
        sourceId = "src2",
        content = "Voici un deuxième extrait philosophique et complexe pour le test.",
        startPosition = 0,
        endPosition = 66,
        wordCount = 10,
        suggestedWords = listOf("deuxième", "philosophique", "complexe"),
        domainTags = listOf("philosophie"),
        registerTags = listOf("soutenu"),
        difficulty = "avancé",
        contextQuality = 0.9f,
        hasCompleteSource = true
    )

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        Dispatchers.setMain(testDispatcher)
        corpusIndex = CorpusIndex()
        viewModel = ExploreViewModel(corpusIndex)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        assertEquals(ExploreUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `loadInitialExtract sets NoExtractAvailable when index is empty`() = runTest {
        viewModel.loadInitialExtract()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ExploreUiState.NoExtractAvailable)
        val state = viewModel.uiState.value as ExploreUiState.NoExtractAvailable
        assertEquals(NoExtractReason.CORPUS_EMPTY, state.reason)
    }

    @Test
    fun `loadInitialExtract displays extract when index has data`() = runTest {
        corpusIndex.add(candidate1)

        viewModel.loadInitialExtract()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ExploreUiState.ExtractDisplayed)
        val state = viewModel.uiState.value as ExploreUiState.ExtractDisplayed
        assertEquals("1", state.extract.id)
        assertEquals("Ceci est un premier extrait de texte très intéressant.", state.extract.content)
    }

    @Test
    fun `toggleWord switches word status between SUGGESTED and ADDED`() = runTest {
        corpusIndex.add(candidate1)

        viewModel.loadInitialExtract()
        advanceUntilIdle()

        var state = viewModel.uiState.value as ExploreUiState.ExtractDisplayed
        // In buildWordStates, words from candidate1.content matching suggestedWords (case-insensitive) are set to SUGGESTED
        // Let's assert on the word 'premier' (matching suggestedWords)
        assertEquals(WordStatus.SUGGESTED, state.wordStates["premier"])

        viewModel.toggleWord("premier")
        // Advance time for transition delay (200ms in ViewModel)
        advanceTimeBy(300)
        advanceUntilIdle()

        state = viewModel.uiState.value as ExploreUiState.ExtractDisplayed
        assertEquals(WordStatus.ADDED, state.wordStates["premier"])

        viewModel.toggleWord("premier")
        advanceTimeBy(300)
        advanceUntilIdle()

        state = viewModel.uiState.value as ExploreUiState.ExtractDisplayed
        assertEquals(WordStatus.SUGGESTED, state.wordStates["premier"])
    }

    @Test
    fun `rateExtract sets interest rating`() = runTest {
        corpusIndex.add(candidate1)

        viewModel.loadInitialExtract()
        advanceUntilIdle()

        viewModel.rateExtract(4)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ExploreUiState.ExtractDisplayed
        assertEquals(4, state.interestRating)
    }

    @Test
    fun `navigateToNext and navigateToPrevious works correctly`() = runTest {
        corpusIndex.addAll(listOf(candidate1, candidate2))

        viewModel.loadInitialExtract()
        advanceUntilIdle()

        var state = viewModel.uiState.value as ExploreUiState.ExtractDisplayed
        assertEquals("1", state.extract.id)

        viewModel.navigateToNext()
        advanceTimeBy(400) // delay for transition (300ms in ViewModel)
        advanceUntilIdle()

        state = viewModel.uiState.value as ExploreUiState.ExtractDisplayed
        assertEquals("2", state.extract.id)

        viewModel.navigateToPrevious()
        advanceTimeBy(400)
        advanceUntilIdle()

        state = viewModel.uiState.value as ExploreUiState.ExtractDisplayed
        assertEquals("1", state.extract.id)
    }
}
