package com.example.lexicaandroid2.presentation.admin

import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun applyNormalPresentationPresetEnablesNormalPresentationMode() = runTest {
        Dispatchers.setMain(testDispatcher)
        try {
            val adminPrefsRepository = mock<AdminPrefsRepository>()
            val userStatsRepository = mock<UserStatsRepository>()
            whenever(userStatsRepository.getUserStats()).thenReturn(flowOf(UserStatsEntity(level = 3)))

            whenever(adminPrefsRepository.normalPresentationEnabled).thenReturn(false)
            whenever(adminPrefsRepository.reviewWordToDefinitionEnabled).thenReturn(false)
            whenever(adminPrefsRepository.reviewDefinitionToWordEnabled).thenReturn(false)
            whenever(adminPrefsRepository.extraSpellingEnabled).thenReturn(false)
            whenever(adminPrefsRepository.reviewQcmEnabled).thenReturn(false)
            whenever(adminPrefsRepository.reviewMatchingEnabled).thenReturn(false)
            whenever(adminPrefsRepository.challengeSemanticEnabled).thenReturn(false)
            whenever(adminPrefsRepository.challengeOrthoEnabled).thenReturn(false)
            whenever(adminPrefsRepository.sessionSize).thenReturn(AdminPrefsRepository.DEFAULT_SESSION_SIZE)
            whenever(adminPrefsRepository.qcmQuestionCount).thenReturn(AdminPrefsRepository.DEFAULT_QCM_COUNT)
            whenever(adminPrefsRepository.memoryGridSize).thenReturn(MemoryGridSize.SIZE_4X4)

            val viewModel = AdminViewModel(
                adminPrefsRepository = adminPrefsRepository,
                userStatsRepository = userStatsRepository,
                dailyReviewStatDao = null
            )

            viewModel.applyNormalPresentationPreset()

            verify(adminPrefsRepository).normalPresentationEnabled = true
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun consumePendingReviewSettingsChangeReturnsTrueOnlyAfterReviewChange() = runTest {
        Dispatchers.setMain(testDispatcher)
        try {
            val adminPrefsRepository = mock<AdminPrefsRepository>()
            val userStatsRepository = mock<UserStatsRepository>()
            whenever(userStatsRepository.getUserStats()).thenReturn(flowOf(UserStatsEntity(level = 3)))

            whenever(adminPrefsRepository.normalPresentationEnabled).thenReturn(true)
            whenever(adminPrefsRepository.reviewWordToDefinitionEnabled).thenReturn(true)
            whenever(adminPrefsRepository.reviewDefinitionToWordEnabled).thenReturn(true)
            whenever(adminPrefsRepository.extraSpellingEnabled).thenReturn(true)
            whenever(adminPrefsRepository.reviewQcmEnabled).thenReturn(true)
            whenever(adminPrefsRepository.reviewMatchingEnabled).thenReturn(true)
            whenever(adminPrefsRepository.challengeSemanticEnabled).thenReturn(true)
            whenever(adminPrefsRepository.challengeOrthoEnabled).thenReturn(true)
            whenever(adminPrefsRepository.sessionSize).thenReturn(AdminPrefsRepository.DEFAULT_SESSION_SIZE)
            whenever(adminPrefsRepository.qcmQuestionCount).thenReturn(AdminPrefsRepository.DEFAULT_QCM_COUNT)
            whenever(adminPrefsRepository.memoryGridSize).thenReturn(MemoryGridSize.SIZE_4X4)

            val viewModel = AdminViewModel(
                adminPrefsRepository = adminPrefsRepository,
                userStatsRepository = userStatsRepository,
                dailyReviewStatDao = null
            )

            assertFalse(viewModel.consumePendingReviewSettingsChange())

            viewModel.setNormalPresentationEnabled(false)

            assertTrue(viewModel.consumePendingReviewSettingsChange())
            assertFalse(viewModel.consumePendingReviewSettingsChange())
        } finally {
            Dispatchers.resetMain()
        }
    }
}

