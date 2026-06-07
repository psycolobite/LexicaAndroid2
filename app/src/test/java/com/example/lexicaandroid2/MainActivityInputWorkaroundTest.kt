package com.example.lexicaandroid2

import android.view.MotionEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityInputWorkaroundTest {

    @Test
    fun shouldIgnoreComposeHoverExitCrashReturnsTrueForKnownMessageAndMouseActions() {
        val message = "The ACTION_HOVER_EXIT event was not cleared."

        assertTrue(shouldIgnoreComposeHoverExitCrash(message, MotionEvent.ACTION_SCROLL))
        assertTrue(shouldIgnoreComposeHoverExitCrash(message, MotionEvent.ACTION_HOVER_ENTER))
        assertTrue(shouldIgnoreComposeHoverExitCrash(message, MotionEvent.ACTION_HOVER_EXIT))
        assertTrue(shouldIgnoreComposeHoverExitCrash(message, MotionEvent.ACTION_HOVER_MOVE))
    }

    @Test
    fun shouldIgnoreComposeHoverExitCrashReturnsFalseForOtherMessages() {
        assertFalse(shouldIgnoreComposeHoverExitCrash("Another crash", MotionEvent.ACTION_SCROLL))
        assertFalse(shouldIgnoreComposeHoverExitCrash(null, MotionEvent.ACTION_HOVER_EXIT))
    }

    @Test
    fun shouldIgnoreComposeHoverExitCrashReturnsFalseForUnrelatedActions() {
        val message = "The ACTION_HOVER_EXIT event was not cleared."

        assertFalse(shouldIgnoreComposeHoverExitCrash(message, MotionEvent.ACTION_DOWN))
        assertFalse(shouldIgnoreComposeHoverExitCrash(message, MotionEvent.ACTION_UP))
    }
}

