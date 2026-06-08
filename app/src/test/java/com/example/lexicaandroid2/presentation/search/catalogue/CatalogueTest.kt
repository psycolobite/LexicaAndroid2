package com.example.lexicaandroid2.presentation.search.catalogue

import com.example.lexicaandroid2.data.corpus.CorpusIndex
import com.example.lexicaandroid2.data.corpus.CorpusSource
import com.example.lexicaandroid2.data.corpus.CorpusSources
import com.example.lexicaandroid2.data.corpus.ExtractCandidate
import com.example.lexicaandroid2.data.corpus.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogueTest {

    private val testDispatcher = StandardTestDispatcher()

    private val extract1 = ExtractCandidate(
        id = "ext1",
        sourceId = "baudelaire-fleurs-du-mal",
        content = "Extrait 1",
        startPosition = 0,
        endPosition = 10,
        wordCount = 2,
        suggestedWords = emptyList(),
        domainTags = listOf("poesie"),
        registerTags = listOf("soutenu"),
        difficulty = "moyen",
        contextQuality = 0.8f,
        hasCompleteSource = true
    )

    private val extract2 = ExtractCandidate(
        id = "ext2",
        sourceId = "descartes-discours",
        content = "Extrait 2",
        startPosition = 0,
        endPosition = 10,
        wordCount = 2,
        suggestedWords = emptyList(),
        domainTags = listOf("philosophie"),
        registerTags = listOf("soutenu"),
        difficulty = "avancé",
        contextQuality = 0.9f,
        hasCompleteSource = true
    )

    private lateinit var corpusIndex: CorpusIndex
    private lateinit var repository: CatalogueRepository
    private lateinit var viewModel: CatalogueViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        corpusIndex = CorpusIndex.index(listOf(extract1, extract2))
        repository = CatalogueRepository(corpusIndex)
        viewModel = CatalogueViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRepositoryGetSourcesByDomain() {
        val allSources = repository.getSourcesByDomain(emptyList())
        assertEquals(CorpusSources.ALL.size, allSources.size)

        val poetrySources = repository.getSourcesByDomain(listOf("poesie"))
        assertEquals(1, poetrySources.size)
        assertEquals("baudelaire-fleurs-du-mal", poetrySources[0].id)
    }

    @Test
    fun testRepositoryGetSourceById() {
        val source = repository.getSourceById("baudelaire-fleurs-du-mal")
        assertNotNull(source)
        assertEquals("Les Fleurs du Mal", source?.title)

        val nonExisting = repository.getSourceById("non-existing")
        assertNull(nonExisting)
    }

    @Test
    fun testRepositoryGetExtractsBySource() {
        val extracts = repository.getExtractsBySource("baudelaire-fleurs-du-mal")
        assertEquals(1, extracts.size)
        assertEquals("ext1", extracts[0].id)
    }

    @Test
    fun testRepositoryGetRelatedSources() {
        // Baudelaire shares nothing with Descartes directly, but let's check
        val related = repository.getRelatedSources("baudelaire-fleurs-du-mal")
        // No other sources share "poesie" or "litterature_classique" (Moliere has litterature_classique, theatre)
        // Moliere and Baudelaire share "litterature_classique"!
        assertEquals(1, related.size)
        assertEquals("moliere-misanthrope", related[0].id)
    }

    @Test
    fun testViewModelLoadSourcesAndSorting() = runTest {
        viewModel.loadSources()
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(CorpusSources.ALL.size, state.sources.size)
        assertFalse(state.isLoading)
        assertNull(state.error)

        // Test sorting by date
        viewModel.changeSortOption(CatalogueSortOption.DATE)
        state = viewModel.uiState.value
        // Baudelaire (1857), Descartes (1637), Molière (1666) -> Baudelaire, Molière, Descartes
        assertEquals("baudelaire-fleurs-du-mal", state.sources[0].id)
        assertEquals("moliere-misanthrope", state.sources[1].id)
        assertEquals("descartes-discours", state.sources[2].id)
    }

    @Test
    fun testViewModelSelection() = runTest {
        viewModel.selectSource("baudelaire-fleurs-du-mal")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("baudelaire-fleurs-du-mal", state.selectedSource?.id)
        assertEquals(1, state.extracts.size)
        assertEquals("ext1", state.extracts[0].id)

        // Related sources to Baudelaire: Moliere
        assertEquals(1, state.relatedSources.size)
        assertEquals("moliere-misanthrope", state.relatedSources[0].id)

        // Clear selection
        viewModel.selectSource(null)
        val clearedState = viewModel.uiState.value
        assertNull(clearedState.selectedSource)
        assertTrue(clearedState.extracts.isEmpty())
    }

    @Test
    fun testViewModelUrlOpening() = runTest {
        assertNull(viewModel.openUrlEvent.value)

        viewModel.openSourceUrl("https://example.com")
        assertEquals("https://example.com", viewModel.openUrlEvent.value)

        viewModel.consumeOpenUrlEvent()
        assertNull(viewModel.openUrlEvent.value)
    }
}
private fun assertFalse(actual: Boolean) {
    org.junit.Assert.assertFalse(actual)
}
