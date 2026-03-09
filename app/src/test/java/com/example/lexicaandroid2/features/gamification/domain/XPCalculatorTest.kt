package com.example.lexicaandroid2.features.gamification.domain

import org.junit.Assert.*
import org.junit.Test

class XPCalculatorTest {

    @Test
    fun `calculateLevel with 0 XP returns level 1`() {
        val level = XPCalculator.calculateLevel(0L)
        assertEquals(1, level)
    }

    @Test
    fun `calculateLevel with 99 XP returns level 1`() {
        val level = XPCalculator.calculateLevel(99L)
        assertEquals(1, level)
    }

    @Test
    fun `calculateLevel with 100 XP returns level 2`() {
        val level = XPCalculator.calculateLevel(100L)
        assertEquals(2, level)
    }

    @Test
    fun `calculateLevel with 400 XP returns level 3`() {
        val level = XPCalculator.calculateLevel(400L)
        assertEquals(3, level)
    }

    @Test
    fun `calculateLevel with 900 XP returns level 4`() {
        val level = XPCalculator.calculateLevel(900L)
        assertEquals(4, level)
    }

    @Test
    fun `calculateLevel with negative XP returns level 1`() {
        val level = XPCalculator.calculateLevel(-50L)
        assertEquals(1, level)
    }

    @Test
    fun `calculateXpForLevel level 1 returns 0`() {
        val xp = XPCalculator.calculateXpForLevel(1)
        assertEquals(0L, xp)
    }

    @Test
    fun `calculateXpForLevel level 2 returns 100`() {
        val xp = XPCalculator.calculateXpForLevel(2)
        assertEquals(100L, xp)
    }

    @Test
    fun `calculateXpForLevel level 3 returns 400`() {
        val xp = XPCalculator.calculateXpForLevel(3)
        assertEquals(400L, xp)
    }

    @Test
    fun `calculateXpForLevel level 4 returns 900`() {
        val xp = XPCalculator.calculateXpForLevel(4)
        assertEquals(900L, xp)
    }

    @Test
    fun `calculateXpForLevel level 5 returns 1600`() {
        val xp = XPCalculator.calculateXpForLevel(5)
        assertEquals(1600L, xp)
    }

    @Test
    fun `calculateXpToNextLevel at level 1 with 0 XP returns 100`() {
        val xpToNext = XPCalculator.calculateXpToNextLevel(1, 0L)
        assertEquals(100L, xpToNext)
    }

    @Test
    fun `calculateXpToNextLevel at level 1 with 50 XP returns 50`() {
        val xpToNext = XPCalculator.calculateXpToNextLevel(1, 50L)
        assertEquals(50L, xpToNext)
    }

    @Test
    fun `calculateXpToNextLevel at level 2 with 150 XP returns 250`() {
        val xpToNext = XPCalculator.calculateXpToNextLevel(2, 150L)
        assertEquals(250L, xpToNext)
    }

    @Test
    fun `calculateProgressToNextLevel at level 1 with 0 XP returns 0`() {
        val progress = XPCalculator.calculateProgressToNextLevel(1, 0L)
        assertEquals(0f, progress, 0.01f)
    }

    @Test
    fun `calculateProgressToNextLevel at level 1 with 50 XP returns 0_5`() {
        val progress = XPCalculator.calculateProgressToNextLevel(1, 50L)
        assertEquals(0.5f, progress, 0.01f)
    }

    @Test
    fun `calculateProgressToNextLevel at level 1 with 100 XP returns 1`() {
        val progress = XPCalculator.calculateProgressToNextLevel(1, 100L)
        assertEquals(1.0f, progress, 0.01f)
    }

    @Test
    fun `calculateProgressToNextLevel at level 2 with 250 XP returns 0_5`() {
        // Level 2 starts at 100, Level 3 starts at 400
        // Range: 100-400 (300 XP needed)
        // Current: 250 - 100 = 150 XP in level
        // Progress: 150 / 300 = 0.5
        val progress = XPCalculator.calculateProgressToNextLevel(2, 250L)
        assertEquals(0.5f, progress, 0.01f)
    }

    @Test
    fun `calculateXpForLearning with 0 words returns 0`() {
        val xp = XPCalculator.calculateXpForLearning(0)
        assertEquals(0, xp)
    }

    @Test
    fun `calculateXpForLearning with 5 words returns 50`() {
        val xp = XPCalculator.calculateXpForLearning(5)
        assertEquals(50, xp)
    }

    @Test
    fun `calculateXpForReview with 0 words returns 0`() {
        val xp = XPCalculator.calculateXpForReview(0)
        assertEquals(0, xp)
    }

    @Test
    fun `calculateXpForReview with 10 words returns 50`() {
        val xp = XPCalculator.calculateXpForReview(10)
        assertEquals(50, xp)
    }

    @Test
    fun `calculateXpForGame without perfect score returns 15`() {
        val xp = XPCalculator.calculateXpForGame(perfectScore = false)
        assertEquals(15, xp)
    }

    @Test
    fun `calculateXpForGame with perfect score returns 25`() {
        val xp = XPCalculator.calculateXpForGame(perfectScore = true)
        assertEquals(25, xp)
    }

    @Test
    fun `calculateStreakBonus with 0 days returns 0`() {
        val xp = XPCalculator.calculateStreakBonus(0)
        assertEquals(0, xp)
    }

    @Test
    fun `calculateStreakBonus with 7 days returns 140`() {
        val xp = XPCalculator.calculateStreakBonus(7)
        assertEquals(140, xp)
    }

    @Test
    fun `constants have correct values`() {
        assertEquals(10, XPCalculator.XP_WORD_LEARNED)
        assertEquals(5, XPCalculator.XP_WORD_REVIEWED)
        assertEquals(20, XPCalculator.XP_DAILY_STREAK_BONUS)
        assertEquals(15, XPCalculator.XP_GAME_COMPLETED)
        assertEquals(10, XPCalculator.XP_PERFECT_SCORE_BONUS)
    }
}
