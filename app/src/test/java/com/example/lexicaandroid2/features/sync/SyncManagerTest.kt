package com.example.lexicaandroid2.features.sync

import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.FlashcardSyncStateDao
import com.example.lexicaandroid2.data.local.ReviewQuestionDao
import com.example.lexicaandroid2.data.local.ReviewAnswerSyncEventDao
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
    fun mergeCloudProgressUnionsCardsAndKeepsMaxStats() {
        val local = CloudProgress(
            updatedAt = 10L,
            xp = 120,
            level = 4,
            streak = 3,
            flashcards = listOf(
                CloudFlashcard(id = "local-only", recto = "local", verso = "local def", favori = true),
                CloudFlashcard(id = "shared", recto = "mot", verso = "def", notesPersonnelles = "note locale", lastModifiedAt = 10L)
            )
        )
        val cloud = CloudProgress(
            updatedAt = 100L,
            xp = 250,
            level = 5,
            streak = 6,
            flashcards = listOf(
                CloudFlashcard(id = "cloud-only", recto = "cloud", verso = "cloud def"),
                CloudFlashcard(id = "shared", recto = "mot", verso = "definition cloud plus longue", favori = true, lastModifiedAt = 100L, favoriteUpdatedAt = 100L)
            )
        )

        val result = mergeCloudProgress(local, cloud)

        assertEquals(250L, result.xp)
        assertEquals(5, result.level)
        assertEquals(6, result.streak)
        assertEquals(3, result.flashcards.size)
        assertTrue(result.flashcards.any { it.id == "local-only" })
        assertTrue(result.flashcards.any { it.id == "cloud-only" })
        val shared = result.flashcards.first { it.id == "shared" }
        assertTrue(shared.favori)
        assertEquals("definition cloud plus longue", shared.verso)
        assertEquals("note locale", shared.notesPersonnelles)
    }

    @Test
    fun mergeCloudProgressKeepsNewestFavoriteAndAppliesTombstone() {
        val local = CloudProgress(
            flashcards = listOf(
                CloudFlashcard(
                    id = "card-a",
                    recto = "mot",
                    verso = "def",
                    favori = false,
                    lastModifiedAt = 200L,
                    favoriteUpdatedAt = 200L
                ),
                CloudFlashcard(
                    id = "card-b",
                    recto = "obsolete",
                    verso = "to delete",
                    lastModifiedAt = 50L
                )
            ),
            deletedFlashcards = listOf(CloudDeletedFlashcard(cardId = "card-b", deletedAt = 300L))
        )
        val cloud = CloudProgress(
            flashcards = listOf(
                CloudFlashcard(
                    id = "card-a",
                    recto = "mot",
                    verso = "def",
                    favori = true,
                    lastModifiedAt = 100L,
                    favoriteUpdatedAt = 100L
                )
            )
        )

        val result = mergeCloudProgress(local, cloud)

        assertEquals(1, result.flashcards.size)
        val card = result.flashcards.single()
        assertEquals("card-a", card.id)
        assertEquals(false, card.favori)
        assertEquals(1, result.deletedFlashcards.size)
        assertEquals("card-b", result.deletedFlashcards.single().cardId)
    }

    @Test
    fun mergeCloudProgressIgnoresPreResetOfflineData() {
        val local = CloudProgress(
            resetGeneration = 1L,
            updatedAt = 50L,
            xp = 400,
            level = 6,
            streak = 10,
            flashcards = listOf(
                CloudFlashcard(
                    id = "stale-card",
                    recto = "ancien",
                    verso = "obsolete",
                    dateAjout = 100L,
                    lastModifiedAt = 100L
                )
            ),
            reviewQuestionProgress = listOf(
                CloudReviewQuestionProgress(
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId("stale-card"),
                    cardId = "stale-card",
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION.name,
                    firstAnsweredAt = 120L,
                    lastSessionFirstAnswerAt = 130L,
                    nextDueAt = 200L
                )
            ),
            reviewAnswerEvents = listOf(
                CloudReviewAnswerSyncEvent(
                    eventId = "stale-review",
                    sessionId = "s-old",
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId("stale-card"),
                    cardId = "stale-card",
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION.name,
                    answer = "GOT_IT",
                    answeredAt = 130L
                )
            ),
            userStatsEvents = listOf(
                CloudUserStatsEvent(
                    eventId = "stale-xp",
                    eventType = "XP_AWARDED",
                    occurredAt = 140L,
                    xpDelta = 400L
                )
            )
        )
        val cloud = CloudProgress(
            resetAt = 1_000L,
            resetGeneration = 2L,
            updatedAt = 1_100L,
            xp = 0,
            level = 1,
            streak = 0,
            userStatsEvents = listOf(
                CloudUserStatsEvent(
                    eventId = "reset",
                    eventType = "STATS_RESET",
                    occurredAt = 1_000L
                )
            )
        )

        val result = mergeCloudProgress(local, cloud)

        assertEquals(1_000L, result.resetAt)
        assertEquals(2L, result.resetGeneration)
        assertEquals(0L, result.xp)
        assertEquals(1, result.level)
        assertEquals(0, result.streak)
        assertTrue(result.flashcards.isEmpty())
        assertTrue(result.reviewQuestionProgress.isEmpty())
        assertTrue(result.reviewAnswerEvents.isEmpty())
        assertEquals(listOf("reset"), result.userStatsEvents.map { it.eventId })
    }

    @Test
    fun mergeCloudProgressKeepsPostResetOfflineData() {
        val local = CloudProgress(
            resetAt = 1_000L,
            resetGeneration = 2L,
            flashcards = listOf(
                CloudFlashcard(
                    id = "new-card",
                    recto = "nouveau",
                    verso = "encore valide",
                    dateAjout = 1_050L,
                    lastModifiedAt = 1_050L
                )
            ),
            reviewAnswerEvents = listOf(
                CloudReviewAnswerSyncEvent(
                    eventId = "new-review",
                    sessionId = "s-new",
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId("new-card"),
                    cardId = "new-card",
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION.name,
                    answer = "GOT_IT",
                    answeredAt = 1_060L
                )
            ),
            userStatsEvents = listOf(
                CloudUserStatsEvent(
                    eventId = "xp-new",
                    eventType = "XP_AWARDED",
                    occurredAt = 1_070L,
                    xpDelta = 20L
                )
            )
        )
        val cloud = CloudProgress(
            resetAt = 1_000L,
            resetGeneration = 2L,
            userStatsEvents = listOf(
                CloudUserStatsEvent(
                    eventId = "reset",
                    eventType = "STATS_RESET",
                    occurredAt = 1_000L
                )
            )
        )

        val result = mergeCloudProgress(local, cloud)

        assertEquals(1_000L, result.resetAt)
        assertEquals(2L, result.resetGeneration)
        assertEquals(20L, result.xp)
        assertEquals(1, result.flashcards.size)
        assertEquals("new-card", result.flashcards.single().id)
        assertEquals(listOf("new-review"), result.reviewAnswerEvents.map { it.eventId })
        assertEquals(listOf("reset", "xp-new").sorted(), result.userStatsEvents.map { it.eventId }.sorted())
    }

    @Test
    fun mergeCloudProgressPrefersHigherResetGenerationEvenWhenTimestampIsOlder() {
        val local = CloudProgress(
            resetAt = 5_000L,
            resetGeneration = 1L,
            flashcards = listOf(
                CloudFlashcard(id = "legacy-card", recto = "vieux", verso = "etat", dateAjout = 6_000L, lastModifiedAt = 6_000L)
            )
        )
        val cloud = CloudProgress(
            resetAt = 4_000L,
            resetGeneration = 2L,
            userStatsEvents = listOf(
                CloudUserStatsEvent(
                    eventId = "reset-g2",
                    eventType = "STATS_RESET",
                    occurredAt = 4_000L
                )
            )
        )

        val result = mergeCloudProgress(local, cloud)

        assertEquals(4_000L, result.resetAt)
        assertEquals(2L, result.resetGeneration)
        assertTrue(result.flashcards.isEmpty())
        assertEquals(listOf("reset-g2"), result.userStatsEvents.map { it.eventId })
    }

    @Test
    fun mergeCloudProgressUnionsReviewAnswerEvents() {
        val local = CloudProgress(
            reviewAnswerEvents = listOf(
                CloudReviewAnswerSyncEvent(eventId = "e1", questionId = "q1", cardId = "c1", answeredAt = 10L)
            )
        )
        val cloud = CloudProgress(
            reviewAnswerEvents = listOf(
                CloudReviewAnswerSyncEvent(eventId = "e2", questionId = "q2", cardId = "c2", answeredAt = 20L)
            )
        )

        val result = mergeCloudProgress(local, cloud)

        assertEquals(2, result.reviewAnswerEvents.size)
        assertEquals(listOf("e1", "e2"), result.reviewAnswerEvents.map { it.eventId })
    }

    @Test
    fun mergeCloudProgressProjectsUserStatsFromEvents() {
        val local = CloudProgress(
            xp = 50,
            level = 2,
            streak = 1,
            lastLoginDate = 100L,
            userStatsEvents = listOf(
                CloudUserStatsEvent(
                    eventId = "xp-1",
                    eventType = "XP_AWARDED",
                    occurredAt = 10L,
                    xpDelta = 120L
                ),
                CloudUserStatsEvent(
                    eventId = "streak-1",
                    eventType = "STREAK_UPDATED",
                    occurredAt = 20L,
                    streakValue = 3,
                    lastLoginDateValue = 200L
                )
            )
        )
        val cloud = CloudProgress(
            xp = 999,
            level = 10,
            streak = 99,
            lastLoginDate = 999L,
            userStatsEvents = listOf(
                CloudUserStatsEvent(
                    eventId = "xp-2",
                    eventType = "XP_AWARDED",
                    occurredAt = 30L,
                    xpDelta = 80L
                )
            )
        )

        val result = mergeCloudProgress(local, cloud)

        assertEquals(200L, result.xp)
        assertEquals(2, result.level)
        assertEquals(3, result.streak)
        assertEquals(200L, result.lastLoginDate)
        assertEquals(listOf("streak-1", "xp-1", "xp-2").sorted(), result.userStatsEvents.map { it.eventId }.sorted())
    }

    @Test
    fun mergeCloudProgressFallsBackToLegacySnapshotStatsWhenNoEventsExist() {
        val local = CloudProgress(xp = 40, level = 1, streak = 2, lastLoginDate = 100L)
        val cloud = CloudProgress(xp = 90, level = 3, streak = 4, lastLoginDate = 300L)

        val result = mergeCloudProgress(local, cloud)

        assertEquals(90L, result.xp)
        assertEquals(3, result.level)
        assertEquals(4, result.streak)
        assertEquals(300L, result.lastLoginDate)
        assertTrue(result.userStatsEvents.isEmpty())
    }

    @Test
    fun mergeCloudProgressProjectsQuestionProgressFromNewerReviewEvents() {
        val local = CloudProgress(
            flashcards = listOf(
                CloudFlashcard(id = "card-a", recto = "mot", verso = "def", dateAjout = 1L)
            ),
            reviewQuestionProgress = listOf(
                CloudReviewQuestionProgress(
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId("card-a"),
                    cardId = "card-a",
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION.name,
                    globalOrder = 2L,
                    level = 1.0,
                    intervalIndex = 1,
                    weightedSuccess = 0.4,
                    recentStreak = 1,
                    currentIntervalDurationMs = 60_000L,
                    lastSessionFirstAnswerAt = 100L,
                    firstAnsweredAt = 10L,
                    nextDueAt = 100L
                )
            ),
            reviewAnswerEvents = listOf(
                CloudReviewAnswerSyncEvent(
                    eventId = "e1",
                    sessionId = "s1",
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId("card-a"),
                    cardId = "card-a",
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION.name,
                    answer = "GOT_IT",
                    answeredAt = 200L
                )
            )
        )
        val cloud = CloudProgress(
            flashcards = listOf(
                CloudFlashcard(id = "card-a", recto = "mot", verso = "def", dateAjout = 1L)
            ),
            reviewQuestionProgress = listOf(
                CloudReviewQuestionProgress(
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId("card-a"),
                    cardId = "card-a",
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION.name,
                    globalOrder = 2L,
                    level = 2.0,
                    intervalIndex = 3,
                    weightedSuccess = 0.8,
                    recentStreak = 4,
                    currentIntervalDurationMs = 120_000L,
                    lastSessionFirstAnswerAt = 300L,
                    firstAnsweredAt = 20L,
                    nextDueAt = 300L
                )
            ),
            reviewAnswerEvents = listOf(
                CloudReviewAnswerSyncEvent(
                    eventId = "e2",
                    sessionId = "s2",
                    questionId = ReviewQuestionType.WORD_TO_DEFINITION.questionId("card-a"),
                    cardId = "card-a",
                    questionType = ReviewQuestionType.WORD_TO_DEFINITION.name,
                    answer = "TOO_EASY",
                    answeredAt = 400L
                )
            )
        )

        val result = mergeCloudProgress(local, cloud)

        val question = result.reviewQuestionProgress.single()
        assertEquals(ReviewQuestionType.WORD_TO_DEFINITION.questionId("card-a"), question.questionId)
        assertEquals(400L, question.lastSessionFirstAnswerAt)
        assertTrue(question.level > 1.0)
        assertEquals(1, question.intervalIndex)
        assertTrue(question.weightedSuccess > 0.8)
        assertEquals(5, question.recentStreak)
        assertEquals(10L, question.firstAnsweredAt)
    }

    @Test
    fun mergeCloudProgressKeepsMaxDailyStatsPerDay() {
        val local = CloudProgress(
            dailyReviewStats = listOf(CloudDailyReviewStat(dateKey = "2026-06-07", cardsReviewed = 10, correctAnswers = 8, totalTimeSeconds = 120))
        )
        val cloud = CloudProgress(
            dailyReviewStats = listOf(CloudDailyReviewStat(dateKey = "2026-06-07", cardsReviewed = 7, correctAnswers = 9, totalTimeSeconds = 90))
        )

        val result = mergeCloudProgress(local, cloud)

        val day = result.dailyReviewStats.single()
        assertEquals(10, day.cardsReviewed)
        assertEquals(9, day.correctAnswers)
        assertEquals(120, day.totalTimeSeconds)
    }

    @Test
    fun checkOnLoginReturnsEmptyCloudChoiceWhenLocalProgressIsMeaningful() = runTest {
        val firestoreSyncRepository = mock<FirestoreSyncRepository>()
        val flashcardDao = mock<FlashcardDao>()
        val reviewQuestionDao = mock<ReviewQuestionDao>()
        val flashcardSyncStateDao = mock<FlashcardSyncStateDao>()
        val reviewAnswerSyncEventDao = mock<ReviewAnswerSyncEventDao>()
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
            flashcardSyncStateDao = flashcardSyncStateDao,
            reviewAnswerSyncEventDao = reviewAnswerSyncEventDao,
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
        val flashcardSyncStateDao = mock<FlashcardSyncStateDao>()
        val reviewAnswerSyncEventDao = mock<ReviewAnswerSyncEventDao>()
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
            flashcardSyncStateDao = flashcardSyncStateDao,
            reviewAnswerSyncEventDao = reviewAnswerSyncEventDao,
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

