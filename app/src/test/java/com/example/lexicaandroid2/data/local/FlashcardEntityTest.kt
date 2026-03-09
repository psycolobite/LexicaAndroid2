package com.example.lexicaandroid2.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class FlashcardEntityTest {
    @Test
    fun defaultsAreApplied() {
        val entity = FlashcardEntity(
            id = "test-id",
            mot = "mot",
            definition = "definition"
        )

        assertEquals("test-id", entity.id)
        assertEquals("mot", entity.mot)
        assertEquals("definition", entity.definition)
        assertEquals(0, entity.sm2MotVersDef.interval)
        assertEquals(0, entity.sm2DefVersMot.interval)
    }
}

