package com.example.lexicaandroid2.data.repository

import com.example.lexicaandroid2.data.local.ReviewSessionSnapshotDao
import com.example.lexicaandroid2.data.local.ReviewSessionSnapshotEntity
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewSessionSnapshot
import com.example.lexicaandroid2.domain.model.ReviewSessionSnapshotState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ReviewSessionSnapshotRepositoryImplTest {
    private val dao: ReviewSessionSnapshotDao = mock()
    private val repository = ReviewSessionSnapshotRepositoryImpl(dao)

    @Test
    fun saveActiveSessionSerializesSnapshot() = runTest {
        val snapshot = ReviewSessionSnapshot(
            createdAt = 1L,
            updatedAt = 2L,
            state = ReviewSessionSnapshotState(
                currentCard = Flashcard(id = "1", recto = "mot", verso = "def"),
                pendingCards = listOf(Flashcard(id = "2", recto = "mot2", verso = "def2")),
                studiedCount = 1,
                totalInSession = 2
            )
        )

        repository.saveActiveSession(snapshot)

        verify(dao).insertOrReplace(
            argThat {
                sessionId == ReviewSessionSnapshot.ACTIVE_SESSION_ID &&
                    createdAt == 1L &&
                    updatedAt == 2L &&
                    payloadJson.contains("\"studiedCount\":1")
            }
        )
    }

    @Test
    fun getActiveSessionDeserializesSnapshot() = runTest {
        whenever(dao.getById(ReviewSessionSnapshot.ACTIVE_SESSION_ID)).thenReturn(
            ReviewSessionSnapshotEntity(
                sessionId = ReviewSessionSnapshot.ACTIVE_SESSION_ID,
                createdAt = 1L,
                updatedAt = 2L,
                payloadJson = "{\"sessionId\":\"active_review_session\",\"createdAt\":1,\"updatedAt\":2,\"state\":{\"currentCard\":{\"id\":\"1\",\"recto\":\"mot\",\"verso\":\"def\",\"synonymes\":[],\"exemples\":[],\"categorieGrammaticale\":\"\",\"registre\":\"\",\"etymologie\":\"\",\"dateAjout\":0,\"favori\":false,\"notesPersonnelles\":\"\",\"sm2MotVersDef\":{\"interval\":0,\"repetitions\":0,\"easeFactor\":2.5,\"nextReviewDate\":0,\"lastReviewDate\":null,\"totalReviews\":0,\"correctReviews\":0,\"lapses\":0},\"sm2DefVersMot\":{\"interval\":0,\"repetitions\":0,\"easeFactor\":2.5,\"nextReviewDate\":0,\"lastReviewDate\":null,\"totalReviews\":0,\"correctReviews\":0,\"lapses\":0}},\"pendingCards\":[],\"currentFaceIsMotVersDef\":true,\"trainingModeCursor\":0,\"isAnswerRevealed\":false,\"isSessionFinished\":false,\"studiedCount\":1,\"totalInSession\":2,\"xpBonusAccumulated\":0,\"autoSpeakWord\":false,\"autoSpeakDefinition\":false,\"presentationModeName\":\"WORD_TO_DEFINITION\",\"activeChallengeTypeName\":null,\"challengeInput\":\"\",\"challengeResult\":null},\"undoState\":null,\"plannedInsertions\":[]}"
            )
        )

        val result = repository.getActiveSession()

        assertEquals("1", result?.state?.currentCard?.id)
        assertEquals(1, result?.state?.studiedCount)
    }

    @Test
    fun getActiveSessionReturnsNullWhenNothingPersisted() = runTest {
        whenever(dao.getById(ReviewSessionSnapshot.ACTIVE_SESSION_ID)).thenReturn(null)

        val result = repository.getActiveSession()

        assertNull(result)
    }

    @Test
    fun clearActiveSessionDelegatesToDao() = runTest {
        repository.clearActiveSession()

        verify(dao).deleteById(ReviewSessionSnapshot.ACTIVE_SESSION_ID)
    }
}

