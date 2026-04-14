package com.example.lexicaandroid2.features.sync

import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.ReviewQuestionDao
import com.example.lexicaandroid2.data.local.ReviewSessionSnapshotDao
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.model.ReviewQuestionType
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.features.gamification.data.UserStatsDao
import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SyncManagerTest {

    @Test
    fun checkOnLoginReturnsEmptyCloudChoiceWhenLocalProgressIsMeaningful() = runTest {
        val firestoreSyncRepository = mock<FirestoreSyncRepository>()
        val flashcardDao = mock<FlashcardDao>()
        val reviewQuestionDao = mock<ReviewQuestionDao>()
        val reviewSessionSnapshotDao = mock<ReviewSessionSnapshotDao>()
        val userStatsDao = mock<UserStatsDao>()
        val userStatsRepository = mock<UserStatsRepository>()
        val flashcardRepository = mock<FlashcardRepository>()
        val dailyReviewStatDao = mock<DailyReviewStatDao>()

        whenever(firestoreSyncRepository.downloadProgress("uid-1")).thenReturn(null)
        whenever(userStatsRepository.getUserStats()).thenReturn(flowOf(UserStatsEntity(xp = 12, level = 2)))
        whenever(flashcardRepository.getAllCards()).thenReturn(emptyList())
        whenever(flashcardRepository.getAllQuestionProgress()).thenReturn(
            listOf(question(firstAnsweredAt = 1L))
        )
        whenever(dailyReviewStatDao.getAllOnce()).thenReturn(emptyList())

        val manager = SyncManager(
            firestoreSyncRepository = firestoreSyncRepository,
            flashcardDao = flashcardDao,
            reviewQuestionDao = reviewQuestionDao,
            reviewSessionSnapshotDao = reviewSessionSnapshotDao,
            userStatsDao = userStatsDao,
            userStatsRepository = userStatsRepository,
            flashcardRepository = flashcardRepository,
            dailyReviewStatDao = dailyReviewStatDao
        )

        val result = manager.checkOnLogin("uid-1")

        assertTrue(result is SyncCheckResult.EmptyCloudAccount)
        result as SyncCheckResult.EmptyCloudAccount
        assertTrue(result.requiresChoice)
        assertEquals(12L, result.local.xp)
        assertEquals(1, result.local.startedQuestionCount)
    }

    @Test
    fun checkOnLoginReturnsEmptyLocalImportWhenCloudExistsAndLocalIsEmpty() = runTest {
        val firestoreSyncRepository = mock<FirestoreSyncRepository>()
        val flashcardDao = mock<FlashcardDao>()
        val reviewQuestionDao = mock<ReviewQuestionDao>()
        val reviewSessionSnapshotDao = mock<ReviewSessionSnapshotDao>()
        val userStatsDao = mock<UserStatsDao>()
        val userStatsRepository = mock<UserStatsRepository>()
        val flashcardRepository = mock<FlashcardRepository>()
        val dailyReviewStatDao = mock<DailyReviewStatDao>()
        val cloudProgress = CloudProgress(
            xp = 50,
            level = 3,
            streak = 4,
            lastLoginDate = 1234L
        )

        whenever(firestoreSyncRepository.downloadProgress("uid-2")).thenReturn(cloudProgress)
        whenever(userStatsRepository.getUserStats()).thenReturn(flowOf(UserStatsEntity()))
        whenever(flashcardRepository.getAllCards()).thenReturn(emptyList())
        whenever(flashcardRepository.getAllQuestionProgress()).thenReturn(emptyList())
        whenever(dailyReviewStatDao.getAllOnce()).thenReturn(emptyList())

        val manager = SyncManager(
            firestoreSyncRepository = firestoreSyncRepository,
            flashcardDao = flashcardDao,
            reviewQuestionDao = reviewQuestionDao,
            reviewSessionSnapshotDao = reviewSessionSnapshotDao,
            userStatsDao = userStatsDao,
            userStatsRepository = userStatsRepository,
            flashcardRepository = flashcardRepository,
            dailyReviewStatDao = dailyReviewStatDao
        )

        val result = manager.checkOnLogin("uid-2")

        assertTrue(result is SyncCheckResult.EmptyLocalImport)
        result as SyncCheckResult.EmptyLocalImport
        assertEquals(50L, result.cloudProgress.xp)
        assertEquals(3, result.cloudProgress.level)
    }

    private fun question(firstAnsweredAt: Long? = null): ReviewQuestionProgress = ReviewQuestionProgress(
        questionId = ReviewQuestionType.DEFINITION_TO_WORD.questionId("card-a"),
        cardId = "card-a",
        questionType = ReviewQuestionType.DEFINITION_TO_WORD,
        globalOrder = 0,
        firstAnsweredAt = firstAnsweredAt
    )
}

