package com.example.lexicaandroid2.presentation.admin

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Dépôt léger pour les préférences du mode admin.
 * Utilise SharedPreferences (pas Room) pour la légèreté.
 */
class AdminPrefsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- Review mode ---
    var reviewMode: ReviewMode
        get() = ReviewMode.fromString(prefs.getString(KEY_REVIEW_MODE, ReviewMode.BOTH.value))
        set(value) = prefs.edit { putString(KEY_REVIEW_MODE, value.value) }

    // --- Challenges ---
    var challengeOrthoEnabled: Boolean
        get() = prefs.getBoolean(KEY_CHALLENGE_ORTHO, true)
        set(value) = prefs.edit { putBoolean(KEY_CHALLENGE_ORTHO, value) }

    var challengeSemanticEnabled: Boolean
        get() = prefs.getBoolean(KEY_CHALLENGE_SEMANTIC, true)
        set(value) = prefs.edit { putBoolean(KEY_CHALLENGE_SEMANTIC, value) }

    // --- Session size ---
    var sessionSize: Int
        get() = prefs.getInt(KEY_SESSION_SIZE, DEFAULT_SESSION_SIZE)
        set(value) = prefs.edit { putInt(KEY_SESSION_SIZE, value.coerceIn(5, 50)) }

    // --- Game overrides ---
    var qcmQuestionCount: Int
        get() = prefs.getInt(KEY_QCM_COUNT, DEFAULT_QCM_COUNT)
        set(value) = prefs.edit { putInt(KEY_QCM_COUNT, value.coerceIn(3, 20)) }

    var memoryGridSize: MemoryGridSize
        get() = MemoryGridSize.fromString(prefs.getString(KEY_MEMORY_GRID, MemoryGridSize.SIZE_4X4.value))
        set(value) = prefs.edit { putString(KEY_MEMORY_GRID, value.value) }

    fun resetAll() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "admin_prefs"
        private const val KEY_REVIEW_MODE = "admin_review_mode"
        private const val KEY_CHALLENGE_ORTHO = "admin_challenge_ortho_enabled"
        private const val KEY_CHALLENGE_SEMANTIC = "admin_challenge_semantic_enabled"
        private const val KEY_SESSION_SIZE = "admin_session_size"
        private const val KEY_QCM_COUNT = "admin_qcm_count"
        private const val KEY_MEMORY_GRID = "admin_memory_grid"

        const val DEFAULT_SESSION_SIZE = 20
        const val DEFAULT_QCM_COUNT = 10
    }
}

enum class ReviewMode(val value: String, val label: String) {
    VOCAB("vocab", "Mot → Définition"),
    DEFINITION("definition", "Définition → Mot"),
    BOTH("both", "Les deux");

    companion object {
        fun fromString(value: String?): ReviewMode =
            entries.find { it.value == value } ?: BOTH
    }
}

enum class MemoryGridSize(val value: String, val label: String, val cols: Int, val rows: Int) {
    SIZE_2X2("2x2", "2×2 (4 cartes)", 2, 2),
    SIZE_2X3("2x3", "2×3 (6 cartes)", 2, 3),
    SIZE_4X4("4x4", "4×4 (16 cartes)", 4, 4),
    SIZE_4X6("4x6", "4×6 (24 cartes)", 4, 6);

    companion object {
        fun fromString(value: String?): MemoryGridSize =
            entries.find { it.value == value } ?: SIZE_4X4
    }
}

