package com.example.lexicaandroid2.domain.model

enum class ReviewCardAggregateState(
    val filterKey: String,
    val legacyFilterKey: String,
    val label: String
) {
    TO_WORK("TO_WORK", "TO_LEARN", "À travailler"),
    IN_PROGRESS("IN_PROGRESS", "LEARNING", "En cours"),
    KNOWN("KNOWN", "KNOWN", "Connu");

    companion object {
        private const val KNOWN_INTERVAL_INDEX_THRESHOLD = 4

        fun fromFilterKey(value: String?): ReviewCardAggregateState? =
            entries.firstOrNull { state ->
                value == state.filterKey || value == state.legacyFilterKey
            }

        fun fromQuestionProgress(progress: ReviewQuestionProgress, now: Long): ReviewCardAggregateState = when {
            progress.nextDueAt <= now -> TO_WORK
            progress.intervalIndex >= KNOWN_INTERVAL_INDEX_THRESHOLD -> KNOWN
            else -> IN_PROGRESS
        }
    }
}

data class ReviewCardProgressSummary(
    val cardId: String,
    val aggregateState: ReviewCardAggregateState,
    val wordToDefinitionState: ReviewCardAggregateState,
    val definitionToWordState: ReviewCardAggregateState
) {
    fun matchesFilter(filterKey: String?): Boolean {
        val requested = ReviewCardAggregateState.fromFilterKey(filterKey) ?: return true
        return aggregateState == requested
    }

    companion object {
        fun fromProgress(cardId: String, progress: List<ReviewQuestionProgress>, now: Long): ReviewCardProgressSummary {
            val progressByType = progress.associateBy { it.questionType }
            val wordToDefinitionState = progressByType[ReviewQuestionType.WORD_TO_DEFINITION]
                ?.let { ReviewCardAggregateState.fromQuestionProgress(it, now) }
                ?: ReviewCardAggregateState.TO_WORK
            val definitionToWordState = progressByType[ReviewQuestionType.DEFINITION_TO_WORD]
                ?.let { ReviewCardAggregateState.fromQuestionProgress(it, now) }
                ?: ReviewCardAggregateState.TO_WORK

            val aggregateState = when {
                wordToDefinitionState == ReviewCardAggregateState.TO_WORK ||
                    definitionToWordState == ReviewCardAggregateState.TO_WORK -> ReviewCardAggregateState.TO_WORK
                wordToDefinitionState == ReviewCardAggregateState.KNOWN &&
                    definitionToWordState == ReviewCardAggregateState.KNOWN -> ReviewCardAggregateState.KNOWN
                else -> ReviewCardAggregateState.IN_PROGRESS
            }

            return ReviewCardProgressSummary(
                cardId = cardId,
                aggregateState = aggregateState,
                wordToDefinitionState = wordToDefinitionState,
                definitionToWordState = definitionToWordState
            )
        }

        fun fromFlashcard(card: Flashcard, now: Long): ReviewCardProgressSummary {
            val wordToDefinitionState = when {
                card.sm2MotVersDef.repetitions == 0 -> ReviewCardAggregateState.TO_WORK
                card.sm2MotVersDef.interval > 20 -> ReviewCardAggregateState.KNOWN
                card.sm2MotVersDef.nextReviewDate <= now -> ReviewCardAggregateState.TO_WORK
                else -> ReviewCardAggregateState.IN_PROGRESS
            }
            val definitionToWordState = when {
                card.sm2DefVersMot.repetitions == 0 -> ReviewCardAggregateState.TO_WORK
                card.sm2DefVersMot.interval > 20 -> ReviewCardAggregateState.KNOWN
                card.sm2DefVersMot.nextReviewDate <= now -> ReviewCardAggregateState.TO_WORK
                else -> ReviewCardAggregateState.IN_PROGRESS
            }

            val aggregateState = when {
                wordToDefinitionState == ReviewCardAggregateState.TO_WORK ||
                    definitionToWordState == ReviewCardAggregateState.TO_WORK -> ReviewCardAggregateState.TO_WORK
                wordToDefinitionState == ReviewCardAggregateState.KNOWN &&
                    definitionToWordState == ReviewCardAggregateState.KNOWN -> ReviewCardAggregateState.KNOWN
                else -> ReviewCardAggregateState.IN_PROGRESS
            }

            return ReviewCardProgressSummary(
                cardId = card.id,
                aggregateState = aggregateState,
                wordToDefinitionState = wordToDefinitionState,
                definitionToWordState = definitionToWordState
            )
        }

        fun indexByCardId(
            cards: List<Flashcard>,
            questionProgress: List<ReviewQuestionProgress>,
            now: Long = System.currentTimeMillis()
        ): Map<String, ReviewCardProgressSummary> {
            val progressByCardId = questionProgress.groupBy { it.cardId }
            return cards.associate { card ->
                card.id to (
                    progressByCardId[card.id]
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { fromProgress(card.id, it, now) }
                        ?: fromFlashcard(card, now)
                )
            }
        }
    }
}
