package com.example.lexicaandroid2.presentation.games.common

import com.example.lexicaandroid2.domain.model.Flashcard

data class Question(
    val mot: String = "",
    val definition: String = "",
    val id: String = ""
)

data class MatchingItem(
    val id: String,
    val text: String,
    val type: String // "word" or "definition"
)

data class MatchingPair(
    val wordId: String,
    val definitionId: String,
    val wordText: String,
    val definitionText: String
)

object GameUtils {
    fun flashcardToQuestion(flashcard: Flashcard): Question {
        return Question(
            mot = flashcard.recto,
            definition = flashcard.verso,
            id = flashcard.id
        )
    }

    fun shuffleAnswers(correct: String, otherCards: List<Flashcard>): List<String> {
        val others = otherCards.map { it.verso }.take(3)
        return (listOf(correct) + others).shuffled()
    }

    fun calculateScore(correct: Int, total: Int): Int {
        return if (total == 0) 0 else (correct * 100) / total
    }

    fun createMatchingPairs(flashcards: List<Flashcard>): List<MatchingPair> {
        return flashcards.map { card ->
            MatchingPair(
                wordId = card.id,
                definitionId = card.id,
                wordText = card.recto,
                definitionText = card.verso
            )
        }
    }
}

