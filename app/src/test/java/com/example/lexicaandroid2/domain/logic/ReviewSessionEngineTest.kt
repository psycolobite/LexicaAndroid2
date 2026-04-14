package com.example.lexicaandroid2.domain.logic

import com.example.lexicaandroid2.domain.model.ReviewAnswer
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.model.ReviewQuestionType
import com.example.lexicaandroid2.domain.model.ReviewSessionPlan
import com.example.lexicaandroid2.domain.model.ReviewSessionValidationReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewSessionEngineTest {
    @Test
    fun twoGotItValidatesQuestionAndRemovesItProgressivelyFromLoop() {
        val questionA = question("card-a", ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0)
        val questionB = question("card-b", ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 2)
        val plan = ReviewSessionPlan(
            selectedQuestions = listOf(questionA, questionB),
            sessionOrder = listOf(questionA, questionB),
            remainingQuestionsCount = 0
        )

        var state = ReviewSessionEngine.start(plan)
        assertEquals("card-a::WORD_TO_DEFINITION", state.currentQuestionId)
        assertEquals(2, state.remainingQuestionsToValidate)

        state = ReviewSessionEngine.answerCurrentQuestion(state, ReviewAnswer.GOT_IT, answeredAt = 1_000L)
        assertEquals("card-b::WORD_TO_DEFINITION", state.currentQuestionId)
        assertEquals(2, state.remainingQuestionsToValidate)
        assertFalse(state.questionStates.getValue(questionA.questionId).isValidated)

        state = ReviewSessionEngine.answerCurrentQuestion(state, ReviewAnswer.TOO_EASY, answeredAt = 2_000L)
        assertEquals("card-a::WORD_TO_DEFINITION", state.currentQuestionId)
        assertEquals(1, state.remainingQuestionsToValidate)
        assertTrue(state.questionStates.getValue(questionB.questionId).isValidated)

        state = ReviewSessionEngine.answerCurrentQuestion(state, ReviewAnswer.GOT_IT, answeredAt = 3_000L)
        assertTrue(state.isFinished)
        assertNull(state.currentQuestionId)
        assertEquals(0, state.remainingQuestionsToValidate)
        assertEquals(
            ReviewSessionValidationReason.TWO_GOT_IT,
            state.questionStates.getValue(questionA.questionId).validationReason
        )
    }

    @Test
    fun tooEasyValidatesImmediately() {
        val question = question("card-a", ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0)
        var state = ReviewSessionEngine.start(singleQuestionPlan(question))

        state = ReviewSessionEngine.answerCurrentQuestion(state, ReviewAnswer.TOO_EASY, answeredAt = 1_000L)

        val questionState = state.questionStates.getValue(question.questionId)
        assertTrue(state.isFinished)
        assertTrue(questionState.isValidated)
        assertEquals(ReviewSessionValidationReason.TOO_EASY, questionState.validationReason)
        assertEquals(ReviewAnswer.TOO_EASY, questionState.effectiveLongTermAnswer)
    }

    @Test
    fun extraSpellingSuccessValidatesImmediatelyWithGotItLongTermAnswer() {
        val question = question("card-a", ReviewQuestionType.DEFINITION_TO_WORD, globalOrder = 0)
        var state = ReviewSessionEngine.start(singleQuestionPlan(question))

        state = ReviewSessionEngine.validateCurrentQuestionFromExtraSpelling(state, answeredAt = 1_000L)

        val questionState = state.questionStates.getValue(question.questionId)
        assertTrue(state.isFinished)
        assertTrue(questionState.isValidated)
        assertEquals(ReviewSessionValidationReason.EXTRA_SPELLING_SUCCESS, questionState.validationReason)
        assertEquals(ReviewAnswer.GOT_IT, questionState.effectiveLongTermAnswer)
    }

    @Test
    fun extraSpellingSuccessCanValidateTargetQuestionWithoutReplacingCurrentQuestion() {
        val questionA = question("card-a", ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0)
        val questionB = question("card-b", ReviewQuestionType.DEFINITION_TO_WORD, globalOrder = 1, firstAnsweredAt = 1L)
        val plan = ReviewSessionPlan(
            selectedQuestions = listOf(questionA, questionB),
            sessionOrder = listOf(questionA, questionB),
            remainingQuestionsCount = 0
        )

        val state = ReviewSessionEngine.applyExtraSpellingSuccess(
            state = ReviewSessionEngine.start(plan),
            questionId = questionB.questionId,
            answeredAt = 1_000L
        )

        assertEquals(questionA.questionId, state.currentQuestionId)
        assertEquals(1, state.remainingQuestionsToValidate)
        assertTrue(state.questionStates.getValue(questionB.questionId).isValidated)
        assertEquals(
            ReviewSessionValidationReason.EXTRA_SPELLING_SUCCESS,
            state.questionStates.getValue(questionB.questionId).validationReason
        )
    }

    @Test
    fun gotItOnFirstPresentationValidatesDirectlyWhenIntervalIsAboveT2() {
        val question = question(
            cardId = "card-a",
            type = ReviewQuestionType.WORD_TO_DEFINITION,
            globalOrder = 0,
            currentIntervalDurationMs = ReviewIntervalEngine.durationForIntervalIndex(3)
        )
        var state = ReviewSessionEngine.start(singleQuestionPlan(question))

        state = ReviewSessionEngine.answerCurrentQuestion(state, ReviewAnswer.GOT_IT, answeredAt = 1_000L)

        val questionState = state.questionStates.getValue(question.questionId)
        assertTrue(state.isFinished)
        assertEquals(
            ReviewSessionValidationReason.FIRST_GOT_IT_ABOVE_T2,
            questionState.validationReason
        )
    }

    @Test
    fun firstAgainDisablesDirectOneShotGotItRuleAndStillRequiresTwoGotIt() {
        val question = question(
            cardId = "card-a",
            type = ReviewQuestionType.WORD_TO_DEFINITION,
            globalOrder = 0,
            currentIntervalDurationMs = ReviewIntervalEngine.durationForIntervalIndex(3)
        )
        var state = ReviewSessionEngine.start(singleQuestionPlan(question))

        state = ReviewSessionEngine.answerCurrentQuestion(state, ReviewAnswer.AGAIN, answeredAt = 1_000L)
        assertFalse(state.isFinished)
        state = ReviewSessionEngine.answerCurrentQuestion(state, ReviewAnswer.GOT_IT, answeredAt = 2_000L)
        assertFalse(state.isFinished)
        state = ReviewSessionEngine.answerCurrentQuestion(state, ReviewAnswer.GOT_IT, answeredAt = 3_000L)

        val questionState = state.questionStates.getValue(question.questionId)
        assertTrue(state.isFinished)
        assertEquals(ReviewSessionValidationReason.TWO_GOT_IT, questionState.validationReason)
    }

    @Test
    fun fiveAgainForcesQuestionOutOfSession() {
        val question = question("card-a", ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0)
        var state = ReviewSessionEngine.start(singleQuestionPlan(question))

        repeat(5) { index ->
            state = ReviewSessionEngine.answerCurrentQuestion(
                state,
                ReviewAnswer.AGAIN,
                answeredAt = (index + 1) * 1_000L
            )
        }

        val questionState = state.questionStates.getValue(question.questionId)
        assertTrue(state.isFinished)
        assertEquals(5, questionState.againCount)
        assertEquals(ReviewSessionValidationReason.FIVE_AGAIN, questionState.validationReason)
        assertEquals(ReviewAnswer.AGAIN, questionState.effectiveLongTermAnswer)
    }

    @Test
    fun gotItThenFiveAgainUsesAgainForLongTermCalculation() {
        val question = question("card-a", ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0)
        var state = ReviewSessionEngine.start(singleQuestionPlan(question))

        state = ReviewSessionEngine.answerCurrentQuestion(state, ReviewAnswer.GOT_IT, answeredAt = 1_000L)
        repeat(5) { index ->
            state = ReviewSessionEngine.answerCurrentQuestion(
                state,
                ReviewAnswer.AGAIN,
                answeredAt = (index + 2) * 1_000L
            )
        }

        val completion = ReviewSessionEngine.completeSession(state, finishedAt = 10_000L)
        val finalState = completion.finalQuestionStates.single()
        val finalProgress = completion.finalQuestionProgress.single()

        assertEquals(ReviewAnswer.AGAIN, finalState.effectiveLongTermAnswer)
        assertEquals(0, finalProgress.intervalIndex)
        assertEquals(10_000L + ReviewIntervalEngine.durationForIntervalIndex(0), finalProgress.nextDueAt)
    }

    @Test
    fun completeSessionAppliesFirstAnswerOnlyAtSessionEnd() {
        val question = question(
            cardId = "card-a",
            type = ReviewQuestionType.WORD_TO_DEFINITION,
            globalOrder = 0,
            currentIntervalDurationMs = ReviewIntervalEngine.durationForIntervalIndex(0),
            weightedSuccess = 2.0,
            weightedFailure = 0.0,
            recentStreak = 1,
            lastSessionFirstAnswerAt = 0L,
            firstAnsweredAt = 100L
        )
        var state = ReviewSessionEngine.start(singleQuestionPlan(question))
        state = ReviewSessionEngine.answerCurrentQuestion(state, ReviewAnswer.GOT_IT, answeredAt = 500L)
        state = ReviewSessionEngine.answerCurrentQuestion(state, ReviewAnswer.GOT_IT, answeredAt = 1_000L)

        val completion = ReviewSessionEngine.completeSession(state, finishedAt = 5_000L)
        val finalProgress = completion.finalQuestionProgress.single()

        assertEquals(5_000L, finalProgress.lastSessionFirstAnswerAt)
        assertEquals(5_000L, finalProgress.lastAskedAt)
        assertTrue(finalProgress.nextDueAt > 5_000L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun completeSessionRequiresFinishedState() {
        val question = question("card-a", ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0)
        val state = ReviewSessionEngine.start(singleQuestionPlan(question))

        ReviewSessionEngine.completeSession(state, finishedAt = 1_000L)
    }

    @Test
    fun eventGotItCreditsLocalProgressWithoutSettingFirstAnswer() {
        val question = question("card-a", ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0)
        var state = ReviewSessionEngine.start(singleQuestionPlan(question))

        state = ReviewSessionEngine.applyEventGotIt(state, question.questionId, answeredAt = 1_000L)
        val questionState = state.questionStates.getValue(question.questionId)

        assertEquals(1, questionState.gotItCount)
        assertEquals(1, questionState.consecutiveGotItCount)
        assertNull(questionState.firstAnswer)
        assertFalse(questionState.isValidated)
    }

    @Test
    fun eventAgainCanForceQuestionOutAfterFiveFailures() {
        val question = question("card-a", ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0)
        var state = ReviewSessionEngine.start(singleQuestionPlan(question))

        repeat(5) { index ->
            state = ReviewSessionEngine.applyEventAgain(state, question.questionId, answeredAt = (index + 1) * 1_000L)
        }

        val questionState = state.questionStates.getValue(question.questionId)
        assertTrue(state.isFinished)
        assertEquals(ReviewSessionValidationReason.FIVE_AGAIN, questionState.validationReason)
    }

    @Test
    fun schedulingFlagsCanBeMarkedOnQuestionState() {
        val question = question("card-a", ReviewQuestionType.WORD_TO_DEFINITION, globalOrder = 0)
        var state = ReviewSessionEngine.start(singleQuestionPlan(question))

        state = ReviewSessionEngine.markQcmScheduled(state, question.questionId)
        state = ReviewSessionEngine.markExtraSpellingScheduled(state, question.questionId)
        state = ReviewSessionEngine.markChallengeScheduled(state, question.questionId)

        val questionState = state.questionStates.getValue(question.questionId)
        assertTrue(questionState.qcmAlreadyScheduled)
        assertTrue(questionState.extraSpellingAlreadyScheduled)
        assertTrue(questionState.challengeAlreadyScheduled)
    }

    @Test
    fun clearPendingReplacementChallengeRemovesPersistentFlagFromQuestionProgress() {
        val question = question("card-a", ReviewQuestionType.DEFINITION_TO_WORD, globalOrder = 0)
            .copy(pendingReplacementChallengeKind = com.example.lexicaandroid2.domain.model.ReviewSessionChallengeKind.SPELLING)
        var state = ReviewSessionEngine.start(singleQuestionPlan(question))

        state = ReviewSessionEngine.clearPendingReplacementChallenge(state, question.questionId)

        val updated = state.questionStates.getValue(question.questionId)
        assertNull(updated.progress.pendingReplacementChallengeKind)
    }

    private fun singleQuestionPlan(question: ReviewQuestionProgress): ReviewSessionPlan = ReviewSessionPlan(
        selectedQuestions = listOf(question),
        sessionOrder = listOf(question),
        remainingQuestionsCount = 0
    )

    private fun question(
        cardId: String,
        type: ReviewQuestionType,
        globalOrder: Long,
        currentIntervalDurationMs: Long = ReviewIntervalEngine.durationForIntervalIndex(0),
        weightedSuccess: Double = 0.0,
        weightedFailure: Double = 0.0,
        recentStreak: Int = 0,
        lastSessionFirstAnswerAt: Long? = null,
        firstAnsweredAt: Long? = null
    ): ReviewQuestionProgress = ReviewQuestionProgress(
        questionId = type.questionId(cardId),
        cardId = cardId,
        questionType = type,
        globalOrder = globalOrder,
        level = 0.0,
        intervalIndex = 0,
        peakIntervalIndex = 0,
        weightedSuccess = weightedSuccess,
        weightedFailure = weightedFailure,
        recentStreak = recentStreak,
        recoveryReserve = 0.0,
        currentIntervalDurationMs = currentIntervalDurationMs,
        nextDueAt = 0L,
        lastSessionFirstAnswerAt = lastSessionFirstAnswerAt,
        lastAskedAt = lastSessionFirstAnswerAt,
        firstAnsweredAt = firstAnsweredAt
    )
}

