package com.example.lexicaandroid2.features.recommendation

import com.example.lexicaandroid2.data.corpus.ExtractCandidate
import com.example.lexicaandroid2.domain.recommendation.InterestProfile
import com.example.lexicaandroid2.domain.recommendation.ScoreContext
import com.example.lexicaandroid2.domain.recommendation.ScoringEngine
import com.example.lexicaandroid2.presentation.search.preferences.UserObjective
import com.example.lexicaandroid2.presentation.search.preferences.UserPreferences
import org.junit.Assert.*
import org.junit.Test

class InterestProfileTest {

    @Test
    fun testCalculatorWithEmptyEventsAndPreferences() {
        val userPrefs = UserPreferences(
            objectives = listOf(UserObjective.TEXTES_EXIGEANTS), // maps to litterature, philosophie, biologie, physique, etc.
            preferredDomains = listOf("poesie"),
            preferredRegisters = listOf("soutenu")
        )

        val profile = InterestProfileCalculator.calculateProfile(userPrefs, emptyList())

        // Initial preferred domains should have a score of 0.5f
        val poesieScore = profile.domainScores["poesie"]
        assertEquals(0.5f, poesieScore)

        val philoScore = profile.domainScores["philosophie"]
        assertEquals(0.5f, philoScore)

        // Non-preferred domains should not be in the initial map (defaulting to 0.0f in ScoringEngine)
        val cuisineScore = profile.domainScores["cuisine"]
        assertNull(cuisineScore)
    }

    @Test
    fun testCalculatorWithEvents() {
        val userPrefs = UserPreferences(
            objectives = emptyList(),
            preferredDomains = listOf("poesie"),
            preferredRegisters = emptyList()
        )

        val events = listOf(
            InterestEvent(
                id = "evt1",
                timestamp = 1000L,
                type = InterestEventType.WORD_ADDED,
                domainIds = listOf("poesie", "philosophie")
            ),
            InterestEvent(
                id = "evt2",
                timestamp = 2000L,
                type = InterestEventType.EXTRACT_RATED,
                domainIds = listOf("poesie"),
                value = 5.0f // 5 stars -> +0.2f
            ),
            InterestEvent(
                id = "evt3",
                timestamp = 3000L,
                type = InterestEventType.WORD_REMOVED,
                domainIds = listOf("philosophie")
            ),
            InterestEvent(
                id = "evt4",
                timestamp = 4000L,
                type = InterestEventType.SOURCE_OPENED,
                domainIds = listOf("voyage")
            )
        )

        val profile = InterestProfileCalculator.calculateProfile(userPrefs, events)

        // "poesie": start 0.5f + 0.3f (added) + 0.2f (rated 5) = 1.0f
        assertEquals(1.0f, profile.domainScores["poesie"] ?: 0.0f, 0.01f)

        // "philosophie": start 0.0f + 0.3f (added) - 0.3f (removed) = 0.0f
        assertEquals(0.0f, profile.domainScores["philosophie"] ?: 0.0f, 0.01f)

        // "voyage": start 0.0f + 0.15f (source opened) = 0.15f
        assertEquals(0.15f, profile.domainScores["voyage"] ?: 0.0f, 0.01f)
    }

    @Test
    fun testChronologicalOrderingOfEvents() {
        val userPrefs = UserPreferences(
            objectives = emptyList(),
            preferredDomains = emptyList(),
            preferredRegisters = emptyList()
        )

        // Events out of chronological order in list:
        // evt2 (remove) has timestamp 2000L, but evt1 (add) has timestamp 1000L.
        // If order is respected: 0.0 -> +0.3 (add) -> -0.3 (remove) = 0.0
        // If order is NOT respected (evt2 first): 0.0 -> -0.3 (clamped to 0.0) -> +0.3 (add) = 0.3
        val events = listOf(
            InterestEvent(
                id = "evt2",
                timestamp = 2000L,
                type = InterestEventType.WORD_REMOVED,
                domainIds = listOf("philosophie")
            ),
            InterestEvent(
                id = "evt1",
                timestamp = 1000L,
                type = InterestEventType.WORD_ADDED,
                domainIds = listOf("philosophie")
            )
        )

        val profile = InterestProfileCalculator.calculateProfile(userPrefs, events)
        assertEquals(0.0f, profile.domainScores["philosophie"] ?: 0.0f, 0.01f)
    }

    @Test
    fun testClampingOfScores() {
        val userPrefs = UserPreferences(
            objectives = emptyList(),
            preferredDomains = emptyList(),
            preferredRegisters = emptyList()
        )

        val events = listOf(
            InterestEvent(
                id = "evt1",
                timestamp = 1000L,
                type = InterestEventType.WORD_ADDED,
                domainIds = listOf("philosophie")
            ),
            InterestEvent(
                id = "evt2",
                timestamp = 2000L,
                type = InterestEventType.WORD_ADDED,
                domainIds = listOf("philosophie")
            ),
            InterestEvent(
                id = "evt3",
                timestamp = 3000L,
                type = InterestEventType.WORD_ADDED,
                domainIds = listOf("philosophie")
            ),
            InterestEvent(
                id = "evt4",
                timestamp = 4000L,
                type = InterestEventType.WORD_ADDED,
                domainIds = listOf("philosophie")
            ) // total adds = +1.2f, but must clamp to 1.0f
        )

        val profile = InterestProfileCalculator.calculateProfile(userPrefs, events)
        assertEquals(1.0f, profile.domainScores["philosophie"] ?: 0.0f, 0.01f)
    }

    @Test
    fun testScoringEngineIntegration() {
        val scoringEngine = ScoringEngine()

        val candidate = ExtractCandidate(
            id = "ext1",
            sourceId = "src_baudelaire",
            content = "Extrait de test",
            startPosition = 0,
            endPosition = 15,
            wordCount = 3,
            suggestedWords = listOf("test"),
            domainTags = listOf("poesie"),
            registerTags = emptyList(),
            difficulty = "facile",
            contextQuality = 1.0f,
            hasCompleteSource = true
        )

        val userPrefs = UserPreferences(
            objectives = emptyList(),
            preferredDomains = listOf("poesie"),
            preferredRegisters = emptyList()
        )

        // Without dynamic profile (preferred domain has default affinity 0.5f)
        val contextWithoutProfile = ScoreContext(
            userPreferences = userPrefs,
            interestProfile = null
        )
        val scoreWithoutProfile = scoringEngine.score(candidate, contextWithoutProfile)

        // With dynamic profile having poesie at 1.0f
        val profile = InterestProfile(domainScores = mapOf("poesie" to 1.0f))
        val contextWithProfile = ScoreContext(
            userPreferences = userPrefs,
            interestProfile = profile
        )
        val scoreWithProfile = scoringEngine.score(candidate, contextWithProfile)

        // The scored value should be higher with the dynamic interest profile
        assertTrue(
            "Score with profile (${scoreWithProfile.score}) should be greater than without profile (${scoreWithoutProfile.score})",
            scoreWithProfile.score > scoreWithoutProfile.score
        )
    }
}
