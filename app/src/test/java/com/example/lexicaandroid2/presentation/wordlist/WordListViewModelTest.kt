package com.example.lexicaandroid2.presentation.wordlist

import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.model.ReviewQuestionType
import com.example.lexicaandroid2.domain.model.Sm2Stats
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class WordListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FlashcardRepository
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
        viewModel = WordListViewModel(repository)
        runBlocking {
            whenever(repository.getAllQuestionProgress()).thenReturn(emptyList())
        }
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

    @Test
    fun onSearchQueryChangedWithNoMatchKeepsOnlyLocalEmptyResults() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2))

        viewModel.loadWords()
        viewModel.onSearchQueryChanged("abnégation")

        assertTrue(viewModel.uiState.value.filteredCards.isEmpty())
        assertEquals("abnégation", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun onSearchQueryChangedPrioritizesRectoThenVersoThenOtherFields() = runTest {
        val notesMatchCard = Flashcard(
            id = "5",
            recto = "abime",
            verso = "trou profond",
            notesPersonnelles = "pense au motif euclidien"
        )
        val versoMatchCard = Flashcard(
            id = "6",
            recto = "prisme",
            verso = "forme avec teinte bleue"
        )
        val rectoMatchCard = Flashcard(
            id = "7",
            recto = "euphonie",
            verso = "harmonie sonore"
        )

        whenever(repository.getAllCards()).thenReturn(listOf(notesMatchCard, versoMatchCard, rectoMatchCard))

        viewModel.loadWords()
        viewModel.onSearchQueryChanged("eu")

        assertEquals(
            listOf(rectoMatchCard.id, versoMatchCard.id, notesMatchCard.id),
            viewModel.uiState.value.filteredCards.map { it.id }
        )
    }

    @Test
    fun onSearchQueryChangedMatchesOtherFieldsAfterDefinition() = runTest {
        val synonymMatchCard = Flashcard(
            id = "8",
            recto = "sagace",
            verso = "qui comprend vite",
            synonymes = listOf("perspicace", "astucieux")
        )
        val versoMatchCard = Flashcard(
            id = "9",
            recto = "mesure",
            verso = "attitude perspicace dans le jugement"
        )

        whenever(repository.getAllCards()).thenReturn(listOf(synonymMatchCard, versoMatchCard))

        viewModel.loadWords()
        viewModel.onSearchQueryChanged("perspic")

        assertEquals(
            listOf(versoMatchCard.id, synonymMatchCard.id),
            viewModel.uiState.value.filteredCards.map { it.id }
        )
    }

    // endregion

    // region onFilterSelected

    @Test
    fun onFilterSelectedKnownShowsOnlyKnownCards() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, knownCard))
        whenever(repository.getAllQuestionProgress()).thenReturn(
            listOf(
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId(card1.id),
                    cardId = card1.id,
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION,
                    globalOrder = 0,
                    nextDueAt = System.currentTimeMillis() - 1_000L
                ),
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.DEFINITION_TO_WORD.questionId(card1.id),
                    cardId = card1.id,
                    questionType = ReviewQuestionType.DEFINITION_TO_WORD,
                    globalOrder = 1,
                    nextDueAt = System.currentTimeMillis() - 1_000L
                ),
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId(knownCard.id),
                    cardId = knownCard.id,
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION,
                    globalOrder = 2,
                    intervalIndex = 4,
                    nextDueAt = System.currentTimeMillis() + 60_000L,
                    firstAnsweredAt = System.currentTimeMillis() - 120_000L
                ),
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.DEFINITION_TO_WORD.questionId(knownCard.id),
                    cardId = knownCard.id,
                    questionType = ReviewQuestionType.DEFINITION_TO_WORD,
                    globalOrder = 3,
                    intervalIndex = 4,
                    nextDueAt = System.currentTimeMillis() + 60_000L,
                    firstAnsweredAt = System.currentTimeMillis() - 120_000L
                )
            )
        )
        viewModel.loadWords()
        viewModel.onFilterSelected("KNOWN")
        assertEquals(1, viewModel.uiState.value.filteredCards.size)
        assertEquals("ephemere", viewModel.uiState.value.filteredCards.first().recto)
    }

    @Test
    fun onFilterSelectedToLearnShowsOnlyFreshCards() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(freshCard, knownCard))
        whenever(repository.getAllQuestionProgress()).thenReturn(
            listOf(
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId(freshCard.id),
                    cardId = freshCard.id,
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION,
                    globalOrder = 0,
                    nextDueAt = System.currentTimeMillis() - 1_000L
                ),
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.DEFINITION_TO_WORD.questionId(freshCard.id),
                    cardId = freshCard.id,
                    questionType = ReviewQuestionType.DEFINITION_TO_WORD,
                    globalOrder = 1,
                    nextDueAt = System.currentTimeMillis() - 1_000L
                ),
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId(knownCard.id),
                    cardId = knownCard.id,
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION,
                    globalOrder = 2,
                    intervalIndex = 4,
                    nextDueAt = System.currentTimeMillis() + 60_000L,
                    firstAnsweredAt = System.currentTimeMillis() - 120_000L
                ),
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.DEFINITION_TO_WORD.questionId(knownCard.id),
                    cardId = knownCard.id,
                    questionType = ReviewQuestionType.DEFINITION_TO_WORD,
                    globalOrder = 3,
                    intervalIndex = 4,
                    nextDueAt = System.currentTimeMillis() + 60_000L,
                    firstAnsweredAt = System.currentTimeMillis() - 120_000L
                )
            )
        )
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

    @Test
    fun onFilterSelectedToWorkUsesQuestionProjectionWhenAvailable() = runTest {
        val now = System.currentTimeMillis()
        whenever(repository.getAllCards()).thenReturn(listOf(card1, knownCard))
        whenever(repository.getAllQuestionProgress()).thenReturn(
            listOf(
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId(card1.id),
                    cardId = card1.id,
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION,
                    globalOrder = 0,
                    nextDueAt = now - 1_000L
                ),
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.DEFINITION_TO_WORD.questionId(card1.id),
                    cardId = card1.id,
                    questionType = ReviewQuestionType.DEFINITION_TO_WORD,
                    globalOrder = 1,
                    nextDueAt = now + 60_000L,
                    intervalIndex = 2,
                    firstAnsweredAt = now - 120_000L
                ),
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId(knownCard.id),
                    cardId = knownCard.id,
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION,
                    globalOrder = 2,
                    nextDueAt = now + 60_000L,
                    intervalIndex = 4,
                    firstAnsweredAt = now - 120_000L
                ),
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.DEFINITION_TO_WORD.questionId(knownCard.id),
                    cardId = knownCard.id,
                    questionType = ReviewQuestionType.DEFINITION_TO_WORD,
                    globalOrder = 3,
                    nextDueAt = now + 60_000L,
                    intervalIndex = 4,
                    firstAnsweredAt = now - 120_000L
                )
            )
        )

        viewModel.loadWords()
        viewModel.onFilterSelected("TO_WORK")

        assertEquals(1, viewModel.uiState.value.filteredCards.size)
        assertEquals(card1.id, viewModel.uiState.value.filteredCards.first().id)
    }

    @Test
    fun onFilterSelectedKeepsSearchPriorityWithinFilteredCards() = runTest {
        val knownRectoMatch = knownCard.copy(recto = "euphorie")
        val knownOtherMatch = knownCard.copy(
            id = "10",
            recto = "zenith",
            verso = "sommet calme",
            exemples = listOf("Une lueur euphorique dans la pièce")
        )

        whenever(repository.getAllCards()).thenReturn(listOf(knownOtherMatch, knownRectoMatch))
        whenever(repository.getAllQuestionProgress()).thenReturn(
            listOf(
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId(knownRectoMatch.id),
                    cardId = knownRectoMatch.id,
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION,
                    globalOrder = 0,
                    intervalIndex = 4,
                    nextDueAt = System.currentTimeMillis() + 60_000L,
                    firstAnsweredAt = System.currentTimeMillis() - 120_000L
                ),
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.DEFINITION_TO_WORD.questionId(knownRectoMatch.id),
                    cardId = knownRectoMatch.id,
                    questionType = ReviewQuestionType.DEFINITION_TO_WORD,
                    globalOrder = 1,
                    intervalIndex = 4,
                    nextDueAt = System.currentTimeMillis() + 60_000L,
                    firstAnsweredAt = System.currentTimeMillis() - 120_000L
                ),
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId(knownOtherMatch.id),
                    cardId = knownOtherMatch.id,
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION,
                    globalOrder = 2,
                    intervalIndex = 4,
                    nextDueAt = System.currentTimeMillis() + 60_000L,
                    firstAnsweredAt = System.currentTimeMillis() - 120_000L
                ),
                ReviewQuestionProgress(
                    questionId = ReviewQuestionType.DEFINITION_TO_WORD.questionId(knownOtherMatch.id),
                    cardId = knownOtherMatch.id,
                    questionType = ReviewQuestionType.DEFINITION_TO_WORD,
                    globalOrder = 3,
                    intervalIndex = 4,
                    nextDueAt = System.currentTimeMillis() + 60_000L,
                    firstAnsweredAt = System.currentTimeMillis() - 120_000L
                )
            )
        )

        viewModel.loadWords()
        viewModel.onFilterSelected("KNOWN")
        viewModel.onSearchQueryChanged("euph")

        assertEquals(
            listOf(knownRectoMatch.id, knownOtherMatch.id),
            viewModel.uiState.value.filteredCards.map { it.id }
        )
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

    @Test
    fun favoriteSelectedCardsMarksEachSelectedCardAsFavoriteAndClearsSelection() = runTest {
        whenever(repository.getAllCards()).thenReturn(
            listOf(card1, card2),
            listOf(card1.copy(favori = true), card2.copy(favori = true))
        )

        viewModel.loadWords()
        viewModel.toggleCardSelection(card1.id)
        viewModel.toggleCardSelection(card2.id)
        viewModel.favoriteSelectedCards()

        verify(repository).setFavorite(card1.id, true)
        verify(repository).setFavorite(card2.id, true)
        assertTrue(viewModel.uiState.value.selectedCardIds.isEmpty())
        assertFalse(viewModel.uiState.value.isSelectionMode)
    }

    @Test
    fun favoriteSelectedCardsSkipsCardsAlreadyFavorite() = runTest {
        val alreadyFavorite = card1.copy(favori = true)
        whenever(repository.getAllCards()).thenReturn(
            listOf(alreadyFavorite, card2),
            listOf(alreadyFavorite, card2.copy(favori = true))
        )

        viewModel.loadWords()
        viewModel.toggleCardSelection(alreadyFavorite.id)
        viewModel.toggleCardSelection(card2.id)
        viewModel.favoriteSelectedCards()

        verify(repository, never()).setFavorite(alreadyFavorite.id, true)
        verify(repository).setFavorite(card2.id, true)
        assertTrue(viewModel.uiState.value.selectedCardIds.isEmpty())
    }

    // endregion

    // region selection

    @Test
    fun toggleCardSelectionAddsThenRemovesSelection() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2))

        viewModel.loadWords()
        viewModel.toggleCardSelection(card1.id)

        assertTrue(viewModel.uiState.value.isSelectionMode)
        assertEquals(setOf(card1.id), viewModel.uiState.value.selectedCardIds)

        viewModel.toggleCardSelection(card1.id)

        assertFalse(viewModel.uiState.value.isSelectionMode)
        assertTrue(viewModel.uiState.value.selectedCardIds.isEmpty())
    }

    @Test
    fun selectAllVisibleOnlySelectsCurrentlyFilteredCards() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2, knownCard))

        viewModel.loadWords()
        viewModel.onSearchQueryChanged("coordonnee")
        viewModel.selectAllVisible()

        assertEquals(setOf(card1.id, card2.id), viewModel.uiState.value.selectedCardIds)
    }

    @Test
    fun onSearchQueryChangedKeepsOnlySelectionsStillVisible() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2))

        viewModel.loadWords()
        viewModel.toggleCardSelection(card1.id)
        viewModel.toggleCardSelection(card2.id)

        viewModel.onSearchQueryChanged("abscisse")

        assertEquals(setOf(card1.id), viewModel.uiState.value.selectedCardIds)
        assertTrue(viewModel.uiState.value.isSelectionMode)
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

    @Test
    fun deleteSelectedCardsDeletesEachSelectedCardAndClearsSelection() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(card1, card2), listOf(knownCard))

        viewModel.loadWords()
        viewModel.toggleCardSelection(card1.id)
        viewModel.toggleCardSelection(card2.id)
        viewModel.deleteSelectedCards()

        verify(repository).deleteCard(card1.id)
        verify(repository).deleteCard(card2.id)
        assertEquals(listOf(knownCard), viewModel.uiState.value.cards)
        assertTrue(viewModel.uiState.value.selectedCardIds.isEmpty())
        assertFalse(viewModel.uiState.value.isSelectionMode)
    }

    @Test
    fun resetProgressForSelectedCardsResetsEachCardAndClearsSelection() = runTest {
        whenever(repository.getAllCards()).thenReturn(
            listOf(knownCard, card1),
            listOf(knownCard.copy(sm2MotVersDef = Sm2Stats(), sm2DefVersMot = Sm2Stats()), card1)
        )

        viewModel.loadWords()
        viewModel.toggleCardSelection(knownCard.id)
        viewModel.resetProgressForSelectedCards()

        verify(repository).updateCardProgress(
            eq(knownCard.id),
            argThat {
                interval == 0 && repetitions == 0 && easeFactor == 2.5 &&
                    lastReviewDate == null && totalReviews == 0 && correctReviews == 0 && lapses == 0
            },
            argThat {
                interval == 0 && repetitions == 0 && easeFactor == 2.5 &&
                    lastReviewDate == null && totalReviews == 0 && correctReviews == 0 && lapses == 0
            }
        )
        assertTrue(viewModel.uiState.value.selectedCardIds.isEmpty())
        assertFalse(viewModel.uiState.value.isSelectionMode)
    }


    // endregion
}
