package com.example.lexicaandroid2.domain.recommendation

import com.example.lexicaandroid2.data.corpus.ExtractCandidate
import com.example.lexicaandroid2.presentation.search.preferences.UserObjective
import com.example.lexicaandroid2.presentation.search.preferences.UserPreferences
import org.junit.Assert.*
import org.junit.Test

class ScoringEngineTest {

    private val scoringEngine = ScoringEngine()

    private val candidate1 = ExtractCandidate(
        id = "ext1",
        sourceId = "src_baudelaire",
        content = "Sois sage, ô ma Douleur, et tiens-toi plus tranquille.",
        startPosition = 0,
        endPosition = 50,
        wordCount = 8,
        suggestedWords = listOf("tranquille"),
        domainTags = listOf("poesie", "litterature_classique"),
        registerTags = listOf("soutenu"),
        difficulty = "moyen",
        contextQuality = 0.8f,
        hasCompleteSource = true
    )

    private val candidate2 = ExtractCandidate(
        id = "ext2",
        sourceId = "src_unknown",
        content = "Un texte lambda sans grand interêt pour la poésie.",
        startPosition = 0,
        endPosition = 50,
        wordCount = 8,
        suggestedWords = listOf("lambda"),
        domainTags = listOf("general"),
        registerTags = listOf("courant"),
        difficulty = "facile",
        contextQuality = 0.4f,
        hasCompleteSource = false
    )

    @Test
    fun testScoreIndividualExtractWithPreferences() {
        val userPrefs = UserPreferences(
            objectives = listOf(UserObjective.TEXTES_EXIGEANTS), // maps to lit, philo, etc.
            preferredDomains = listOf("poesie"),
            preferredRegisters = listOf("soutenu")
        )
        val context = ScoreContext(userPreferences = userPrefs)

        val scored1 = scoringEngine.score(candidate1, context)
        val scored2 = scoringEngine.score(candidate2, context)

        // candidate1 should score higher than candidate2 because of preferences, quality, and registers matching
        assertTrue("Scored 1 ($scored1) should be greater than Scored 2 ($scored2)", scored1.score > scored2.score)
    }

    @Test
    fun testScoreIndividualExtractWithoutPreferences() {
        val context = ScoreContext(userPreferences = null)

        val scored1 = scoringEngine.score(candidate1, context)
        val scored2 = scoringEngine.score(candidate2, context)

        // candidate1 should still score higher because of higher contextQuality (0.8 vs 0.4)
        assertTrue(scored1.score > scored2.score)
    }

    @Test
    fun testScoreBatchSorting() {
        val userPrefs = UserPreferences(
            objectives = listOf(UserObjective.TEXTES_EXIGEANTS),
            preferredDomains = listOf("poesie"),
            preferredRegisters = listOf("soutenu")
        )
        val context = ScoreContext(userPreferences = userPrefs)

        val list = listOf(candidate2, candidate1)
        val sorted = scoringEngine.scoreBatch(list, context)

        assertEquals(2, sorted.size)
        // Highest score should be first
        assertEquals("ext1", sorted[0].extract.id)
        assertEquals("ext2", sorted[1].extract.id)
    }

    @Test
    fun testDiversityScoreDecreaseWhenSourceSeen() {
        val userPrefs = UserPreferences(
            objectives = emptyList(),
            preferredDomains = emptyList(),
            preferredRegisters = emptyList()
        )
        // With history containing the source of candidate1
        val contextWithHistory = ScoreContext(
            userPreferences = userPrefs,
            history = listOf("src_baudelaire")
        )
        // Without history
        val contextWithoutHistory = ScoreContext(
            userPreferences = userPrefs,
            history = emptyList()
        )

        val scoredWithHistory = scoringEngine.score(candidate1, contextWithHistory)
        val scoredWithoutHistory = scoringEngine.score(candidate1, contextWithoutHistory)

        // Score with history should be lower because diversityScore decreases
        assertTrue("Score with history (${scoredWithHistory.score}) should be less than without history (${scoredWithoutHistory.score})",
            scoredWithHistory.score < scoredWithoutHistory.score)
    }
}
