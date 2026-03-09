package com.example.lexicaandroid2.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlashcardDatabaseTest {
    private lateinit var database: LexicaDatabase
    private lateinit var dao: FlashcardDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LexicaDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = database.flashcardDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadFlashcard() = runBlocking {
        val entity = FlashcardEntity(
            id = "test-123",
            mot = "exemple",
            definition = "une definition complexe",
            synonymes = listOf("illustration", "specimen"),
            exemples = listOf("Un exemple parlant."),
            categorieGrammaticale = "nom",
            registre = "courant",
            etymologie = "latin exemplum",
            favori = true,
            notesPersonnelles = "note",
            sm2MotVersDef = Sm2DataEmbedded(
                interval = 7,
                easeFactor = 2.1,
                repetitions = 3,
                totalReviews = 5,
                correctReviews = 4,
                lapses = 1
            )
        )

        dao.insert(entity)
        val read = dao.getById("test-123")

        assertNotNull(read)
        assertEquals("exemple", read?.mot)
        assertEquals(listOf("illustration", "specimen"), read?.synonymes)
        assertEquals(7, read?.sm2MotVersDef?.interval)
        assertEquals(2.1, read?.sm2MotVersDef?.easeFactor ?: 0.0, 0.0001)
    }
}

