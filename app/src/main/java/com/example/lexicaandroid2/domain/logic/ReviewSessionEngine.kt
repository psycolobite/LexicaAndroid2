package com.example.lexicaandroid2.domain.logic

import com.example.lexicaandroid2.domain.model.MIN_INTERVENING_PRESENTATIONS_FOR_SAME_CARD_FAMILY
import com.example.lexicaandroid2.domain.model.ReviewAnswer
import com.example.lexicaandroid2.domain.model.ReviewSessionCompletion
import com.example.lexicaandroid2.domain.model.ReviewSessionPlan
import com.example.lexicaandroid2.domain.model.ReviewSessionQuestionState
import com.example.lexicaandroid2.domain.model.ReviewSessionState
import com.example.lexicaandroid2.domain.model.ReviewSessionValidationReason
import com.example.lexicaandroid2.domain.model.toSessionSpacingKey

object ReviewSessionEngine {
    private val T2_DURATION_MS = ReviewIntervalEngine.durationForIntervalIndex(2)

    fun start(plan: ReviewSessionPlan): ReviewSessionState {
        val distinctSessionOrder = plan.sessionOrder.distinctBy { it.questionId }
        val questionStates = distinctSessionOrder.associate { question ->
            question.questionId to ReviewSessionQuestionState(progress = question)
        }
        val firstQuestionId = distinctSessionOrder.firstOrNull()?.questionId

        return ReviewSessionState(
            sessionOrderQuestionIds = distinctSessionOrder.map { it.questionId },
            questionStates = questionStates,
            currentQuestionId = firstQuestionId,
            currentOrderIndex = if (firstQuestionId == null) -1 else 0,
            remainingQuestionsToValidate = questionStates.size,
            isFinished = questionStates.isEmpty()
        )
    }

    fun answerCurrentQuestion(
        state: ReviewSessionState,
        answer: ReviewAnswer,
        answeredAt: Long = System.currentTimeMillis(),
        recentPresentationKeys: List<String> = emptyList()
    ): ReviewSessionState {
        if (state.isFinished) return state

        val currentQuestionId = state.currentQuestionId
            ?: error("Cannot answer a session with no current question")
        return applyAnswerToQuestion(
            state = state,
            questionId = currentQuestionId,
            answer = answer,
            answeredAt = answeredAt,
            countsForLongTerm = true,
            countsAsPresentation = true,
            advanceFromCurrentQuestion = true,
            recentPresentationKeys = recentPresentationKeys
        )
    }

    fun validateCurrentQuestionFromExtraSpelling(
        state: ReviewSessionState,
        answeredAt: Long = System.currentTimeMillis(),
        recentPresentationKeys: List<String> = emptyList()
    ): ReviewSessionState {
        if (state.isFinished) return state

        val currentQuestionId = state.currentQuestionId
            ?: error("Cannot validate a session with no current question")
        return applyAnswerToQuestion(
            state = state,
            questionId = currentQuestionId,
            answer = ReviewAnswer.GOT_IT,
            answeredAt = answeredAt,
            countsForLongTerm = true,
            countsAsPresentation = true,
            advanceFromCurrentQuestion = true,
            recentPresentationKeys = recentPresentationKeys,
            forcedValidationReason = ReviewSessionValidationReason.EXTRA_SPELLING_SUCCESS
        )
    }

    fun applyExtraSpellingSuccess(
        state: ReviewSessionState,
        questionId: String,
        answeredAt: Long = System.currentTimeMillis()
    ): ReviewSessionState = applyAnswerToQuestion(
        state = state,
        questionId = questionId,
        answer = ReviewAnswer.GOT_IT,
        answeredAt = answeredAt,
        countsForLongTerm = true,
        countsAsPresentation = false,
        advanceFromCurrentQuestion = false,
        recentPresentationKeys = emptyList(),
        forcedValidationReason = ReviewSessionValidationReason.EXTRA_SPELLING_SUCCESS
    )

    fun applyExtraSpellingFailure(
        state: ReviewSessionState,
        questionId: String,
        answeredAt: Long = System.currentTimeMillis()
    ): ReviewSessionState = applyAnswerToQuestion(
        state = state,
        questionId = questionId,
        answer = ReviewAnswer.AGAIN,
        answeredAt = answeredAt,
        countsForLongTerm = true,
        countsAsPresentation = false,
        advanceFromCurrentQuestion = false,
        recentPresentationKeys = emptyList()
    )

    fun applyEventGotIt(
        state: ReviewSessionState,
        questionId: String,
        answeredAt: Long = System.currentTimeMillis()
    ): ReviewSessionState = applyAnswerToQuestion(
        state = state,
        questionId = questionId,
        answer = ReviewAnswer.GOT_IT,
        answeredAt = answeredAt,
        countsForLongTerm = false,
        countsAsPresentation = false,
        advanceFromCurrentQuestion = false,
        recentPresentationKeys = emptyList()
    )

    fun applyEventAgain(
        state: ReviewSessionState,
        questionId: String,
        answeredAt: Long = System.currentTimeMillis()
    ): ReviewSessionState = applyAnswerToQuestion(
        state = state,
        questionId = questionId,
        answer = ReviewAnswer.AGAIN,
        answeredAt = answeredAt,
        countsForLongTerm = false,
        countsAsPresentation = false,
        advanceFromCurrentQuestion = false,
        recentPresentationKeys = emptyList()
    )

    fun markQcmScheduled(
        state: ReviewSessionState,
        questionId: String
    ): ReviewSessionState = updateQuestionState(state, questionId) {
        it.copy(qcmAlreadyScheduled = true)
    }

    fun markExtraSpellingScheduled(
        state: ReviewSessionState,
        questionId: String
    ): ReviewSessionState = updateQuestionState(state, questionId) {
        it.copy(extraSpellingAlreadyScheduled = true)
    }

    fun markChallengeScheduled(
        state: ReviewSessionState,
        questionId: String
    ): ReviewSessionState = updateQuestionState(state, questionId) {
        it.copy(challengeAlreadyScheduled = true)
    }

    fun clearPendingReplacementChallenge(
        state: ReviewSessionState,
        questionId: String
    ): ReviewSessionState = updateQuestionState(state, questionId) { questionState ->
        questionState.copy(
            progress = questionState.progress.copy(pendingReplacementChallengeKind = null)
        )
    }

    fun completeSession(
        state: ReviewSessionState,
        finishedAt: Long = System.currentTimeMillis()
    ): ReviewSessionCompletion {
        require(state.isFinished || state.remainingQuestionsToValidate == 0) {
            "Session must be finished before computing long-term updates"
        }

        val finalStates = state.sessionOrderQuestionIds.map { questionId ->
            state.questionStates.getValue(questionId)
        }
        val finalProgress = finalStates.map { questionState ->
            val effectiveAnswer = questionState.effectiveLongTermAnswer
                ?: error("Question ${questionState.progress.questionId} has no effective long-term answer")
            ReviewIntervalEngine.applyFirstAnswer(
                progress = questionState.progress,
                answer = effectiveAnswer,
                nowMs = finishedAt
            )
        }

        return ReviewSessionCompletion(
            finalQuestionProgress = finalProgress,
            finalQuestionStates = finalStates
        )
    }

    private fun applyAnswerToQuestion(
        state: ReviewSessionState,
        questionId: String,
        answer: ReviewAnswer,
        answeredAt: Long,
        countsForLongTerm: Boolean,
        countsAsPresentation: Boolean,
        advanceFromCurrentQuestion: Boolean,
        recentPresentationKeys: List<String>,
        forcedValidationReason: ReviewSessionValidationReason? = null
    ): ReviewSessionState {
        if (state.isFinished) return state

        val questionState = state.questionStates[questionId] ?: return state
        if (questionState.isValidated) return state

        val updatedQuestionState = applyLocalAnswer(
            questionState = questionState,
            answer = answer,
            answeredAt = answeredAt,
            countsForLongTerm = countsForLongTerm,
            countsAsPresentation = countsAsPresentation,
            forcedValidationReason = forcedValidationReason
        )
        val updatedQuestionStates = state.questionStates + (questionId to updatedQuestionState)
        val remainingQuestions = updatedQuestionStates.values.count { !it.isValidated }

        if (remainingQuestions == 0) {
            return state.copy(
                questionStates = updatedQuestionStates,
                currentQuestionId = null,
                currentOrderIndex = -1,
                remainingQuestionsToValidate = 0,
                isFinished = true
            )
        }

        val currentQuestionId = state.currentQuestionId
        val currentQuestionStillAvailable = currentQuestionId != null && updatedQuestionStates[currentQuestionId]?.isValidated == false
        val nextOrderIndex = when {
            advanceFromCurrentQuestion -> findNextUnvalidatedIndex(
                sessionOrderQuestionIds = state.sessionOrderQuestionIds,
                currentOrderIndex = state.currentOrderIndex,
                questionStates = updatedQuestionStates,
                recentPresentationKeys = recentPresentationKeys
            )
            currentQuestionStillAvailable -> state.currentOrderIndex
            else -> findNextUnvalidatedIndex(
                sessionOrderQuestionIds = state.sessionOrderQuestionIds,
                currentOrderIndex = state.currentOrderIndex,
                questionStates = updatedQuestionStates,
                recentPresentationKeys = recentPresentationKeys
            )
        }
        val nextQuestionId = state.sessionOrderQuestionIds[nextOrderIndex]

        return state.copy(
            questionStates = updatedQuestionStates,
            currentQuestionId = nextQuestionId,
            currentOrderIndex = nextOrderIndex,
            remainingQuestionsToValidate = remainingQuestions,
            isFinished = false
        )
    }

    private fun applyLocalAnswer(
        questionState: ReviewSessionQuestionState,
        answer: ReviewAnswer,
        answeredAt: Long,
        countsForLongTerm: Boolean,
        countsAsPresentation: Boolean,
        forcedValidationReason: ReviewSessionValidationReason? = null
    ): ReviewSessionQuestionState {
        val isFirstPresentation = countsAsPresentation && questionState.presentationCount == 0
        val firstAnswer = if (countsForLongTerm) questionState.firstAnswer ?: answer else questionState.firstAnswer
        val firstAnswerAt = if (countsForLongTerm) questionState.firstAnswerAt ?: answeredAt else questionState.firstAnswerAt
        val gotItIncrement = if (answer == ReviewAnswer.GOT_IT) 1 else 0
        val gotItCount = questionState.gotItCount + gotItIncrement
        val againCount = questionState.againCount + if (answer == ReviewAnswer.AGAIN) 1 else 0
        val consecutiveGotItCount = when (answer) {
            ReviewAnswer.GOT_IT -> questionState.consecutiveGotItCount + 1
            ReviewAnswer.TOO_EASY -> questionState.consecutiveGotItCount + 3
            ReviewAnswer.AGAIN -> 0
        }
        val validationReason = forcedValidationReason ?: resolveValidationReason(
            questionState = questionState,
            answer = answer,
            isFirstPresentation = isFirstPresentation,
            gotItCount = gotItCount,
            againCount = againCount
        )

        return questionState.copy(
            firstAnswer = firstAnswer,
            firstAnswerAt = firstAnswerAt,
            lastAnswer = answer,
            lastAnsweredAt = answeredAt,
            presentationCount = questionState.presentationCount + if (countsAsPresentation) 1 else 0,
            gotItCount = gotItCount,
            consecutiveGotItCount = consecutiveGotItCount,
            againCount = againCount,
            isValidated = validationReason != null,
            validationReason = validationReason
        )
    }

    private fun resolveValidationReason(
        questionState: ReviewSessionQuestionState,
        answer: ReviewAnswer,
        isFirstPresentation: Boolean,
        gotItCount: Int,
        againCount: Int
    ): ReviewSessionValidationReason? {
        return when {
            answer == ReviewAnswer.TOO_EASY -> ReviewSessionValidationReason.TOO_EASY
            answer == ReviewAnswer.AGAIN && againCount >= 5 -> ReviewSessionValidationReason.FIVE_AGAIN
            answer == ReviewAnswer.GOT_IT && isFirstPresentation && questionState.progress.currentIntervalDurationMs > T2_DURATION_MS -> {
                ReviewSessionValidationReason.FIRST_GOT_IT_ABOVE_T2
            }
            answer == ReviewAnswer.GOT_IT && gotItCount >= 2 -> ReviewSessionValidationReason.TWO_GOT_IT
            else -> null
        }
    }

    private fun updateQuestionState(
        state: ReviewSessionState,
        questionId: String,
        transform: (ReviewSessionQuestionState) -> ReviewSessionQuestionState
    ): ReviewSessionState {
        val existing = state.questionStates[questionId] ?: return state
        return state.copy(
            questionStates = state.questionStates + (questionId to transform(existing))
        )
    }

    private fun findNextUnvalidatedIndex(
        sessionOrderQuestionIds: List<String>,
        currentOrderIndex: Int,
        questionStates: Map<String, ReviewSessionQuestionState>,
        recentPresentationKeys: List<String>
    ): Int {
        val blockedSpacingKeys = recentPresentationKeys
            .takeLast(MIN_INTERVENING_PRESENTATIONS_FOR_SAME_CARD_FAMILY)
            .toSet()
        var fallbackIndex: Int? = null

        for (offset in 1..sessionOrderQuestionIds.size) {
            val candidateIndex = (currentOrderIndex + offset) % sessionOrderQuestionIds.size
            val candidateQuestionId = sessionOrderQuestionIds[candidateIndex]
            val candidateState = questionStates.getValue(candidateQuestionId)
            if (!candidateState.isValidated) {
                if (fallbackIndex == null) {
                    fallbackIndex = candidateIndex
                }

                val candidateSpacingKey = candidateState.progress.toSessionSpacingKey()
                if (candidateSpacingKey !in blockedSpacingKeys) {
                    return candidateIndex
                }
            }
        }
        return fallbackIndex ?: currentOrderIndex.coerceAtLeast(0)
    }
}
