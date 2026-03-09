package com.example.lexicaandroid2.data.mapper

import com.example.lexicaandroid2.data.local.FlashcardEntity
import com.example.lexicaandroid2.data.local.Sm2DataEmbedded
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.Sm2Stats

fun FlashcardEntity.toDomain(): Flashcard = Flashcard(
    id = id,
    recto = mot,
    verso = definition,
    synonymes = synonymes,
    exemples = exemples,
    categorieGrammaticale = categorieGrammaticale,
    registre = registre,
    etymologie = etymologie,
    dateAjout = dateAjout,
    favori = favori,
    notesPersonnelles = notesPersonnelles,
    sm2MotVersDef = sm2MotVersDef.toDomain(),
    sm2DefVersMot = sm2DefVersMot.toDomain()
)

fun Flashcard.toEntity(): FlashcardEntity {
    val isNew = sm2MotVersDef.repetitions == 0 && sm2DefVersMot.repetitions == 0
    val isKnown = sm2MotVersDef.interval > 20 && sm2DefVersMot.interval > 20
    val state = when {
        isNew -> "TO_LEARN"
        isKnown -> "KNOWN"
        else -> "LEARNING"
    }
    
    return FlashcardEntity(
        id = id,
        mot = recto,
        definition = verso,
        synonymes = synonymes,
        exemples = exemples,
        categorieGrammaticale = categorieGrammaticale,
        registre = registre,
        etymologie = etymologie,
        dateAjout = dateAjout,
        favori = favori,
        notesPersonnelles = notesPersonnelles,
        state = state,
        sm2MotVersDef = sm2MotVersDef.toEmbedded(),
        sm2DefVersMot = sm2DefVersMot.toEmbedded()
    )
}

fun Sm2DataEmbedded.toDomain(): Sm2Stats = Sm2Stats(
    interval = interval,
    repetitions = repetitions,
    easeFactor = easeFactor,
    nextReviewDate = nextReview,
    lastReviewDate = lastReview,
    totalReviews = totalReviews,
    correctReviews = correctReviews,
    lapses = lapses
)

fun Sm2Stats.toEmbedded(): Sm2DataEmbedded = Sm2DataEmbedded(
    interval = interval,
    repetitions = repetitions,
    easeFactor = easeFactor,
    nextReview = nextReviewDate,
    lastReview = lastReviewDate,
    totalReviews = totalReviews,
    correctReviews = correctReviews,
    lapses = lapses
)

