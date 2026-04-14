package com.example.lexicaandroid2.presentation.editword

import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.Sm2Stats
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class EditWordViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FlashcardRepository

    private val existingCard = Flashcard(
        id = "card-1",
        recto = "abnégation",
        verso = "Sacrifice de soi.",
        synonymes = listOf("dévouement"),
        exemples = listOf("Une abnégation admirable."),
        categorieGrammaticale = "nom féminin",
        registre = "soutenu",
        etymologie = "Du latin abnegatio",
        notesPersonnelles = "À revoir",
        sm2MotVersDef = Sm2Stats(interval = 5, repetitions = 2),
        sm2DefVersMot = Sm2Stats(interval = 4, repetitions = 2)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadCardPrefillsAllFields() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(existingCard))

        val viewModel = EditWordViewModel(cardId = existingCard.id, repository = repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(existingCard.recto, state.word)
        assertEquals(existingCard.verso, state.definition)
        assertEquals("dévouement", state.synonymes)
        assertEquals("nom féminin", state.categorie)
        assertEquals("soutenu", state.registre)
        assertEquals("Du latin abnegatio", state.etymologie)
        assertEquals("Une abnégation admirable.", state.exemples)
        assertEquals("À revoir", state.notes)
        assertTrue(state.optionalExpanded)
    }

    @Test
    fun saveUpdatesCardContentWithoutTouchingProgress() = runTest {
        whenever(repository.getAllCards()).thenReturn(listOf(existingCard), listOf(existingCard))

        val viewModel = EditWordViewModel(cardId = existingCard.id, repository = repository)
        advanceUntilIdle()

        viewModel.onWordChanged("abnégation choisie")
        viewModel.onDefinitionChanged("Renoncement volontaire")
        viewModel.onSynonymesChanged("dévouement, sacrifice")
        viewModel.onExemplesChanged("Premier exemple\nDeuxième exemple")
        viewModel.onNotesChanged("Note mise à jour")
        viewModel.save()
        advanceUntilIdle()

        verify(repository).updateCardContent(
            argThat {
                id == existingCard.id &&
                    recto == "abnégation choisie" &&
                    verso == "Renoncement volontaire" &&
                    synonymes == listOf("dévouement", "sacrifice") &&
                    exemples == listOf("Premier exemple", "Deuxième exemple") &&
                    notesPersonnelles == "Note mise à jour" &&
                    sm2MotVersDef == existingCard.sm2MotVersDef &&
                    sm2DefVersMot == existingCard.sm2DefVersMot
            }
        )
        assertTrue(viewModel.uiState.value.saveCompletedToken > 0L)
    }
}

