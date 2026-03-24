package com.example.lexicaandroid2.presentation.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Dépôt léger pour les préférences utilisateur (thème, police, couleur, entraînement, notifications).
 * Utilise SharedPreferences (pas Room) pour la légèreté.
 * Ne pas dupliquer les clés déjà présentes dans AdminPrefsRepository.
 */
class UserPrefsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Apparence ──────────────────────────────────────────────────────────

    var theme: AppTheme
        get() = AppTheme.fromString(prefs.getString(KEY_THEME, AppTheme.SYSTEM.value))
        set(value) = prefs.edit { putString(KEY_THEME, value.value) }

    var fontSize: Float
        get() = prefs.getFloat(KEY_FONT_SIZE, DEFAULT_FONT_SIZE)
        set(value) = prefs.edit { putFloat(KEY_FONT_SIZE, value.coerceIn(12f, 22f)) }

    var accentColor: AccentColor
        get() = AccentColor.fromString(prefs.getString(KEY_ACCENT_COLOR, AccentColor.VIOLET.value))
        set(value) = prefs.edit { putString(KEY_ACCENT_COLOR, value.value) }

    // ── Entraînement ───────────────────────────────────────────────────────

    var cardsPerSession: Int
        get() = prefs.getInt(KEY_CARDS_PER_SESSION, DEFAULT_CARDS_PER_SESSION)
        set(value) = prefs.edit { putInt(KEY_CARDS_PER_SESSION, value.coerceIn(5, 50)) }

    var showDefinitionFirst: Boolean
        get() = prefs.getBoolean(KEY_SHOW_DEFINITION_FIRST, false)
        set(value) = prefs.edit { putBoolean(KEY_SHOW_DEFINITION_FIRST, value) }

    var challengesEnabled: Boolean
        get() = prefs.getBoolean(KEY_CHALLENGES_ENABLED, true)
        set(value) = prefs.edit { putBoolean(KEY_CHALLENGES_ENABLED, value) }

    // ── Notifications ──────────────────────────────────────────────────────

    var dailyReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_DAILY_REMINDER, false)
        set(value) = prefs.edit { putBoolean(KEY_DAILY_REMINDER, value) }

    var reminderTime: String
        get() = prefs.getString(KEY_REMINDER_TIME, DEFAULT_REMINDER_TIME) ?: DEFAULT_REMINDER_TIME
        set(value) = prefs.edit { putString(KEY_REMINDER_TIME, value) }

    companion object {
        const val PREFS_NAME = "user_prefs"
        private const val KEY_THEME = "theme"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_CARDS_PER_SESSION = "cards_per_session"
        private const val KEY_SHOW_DEFINITION_FIRST = "show_definition_first"
        private const val KEY_CHALLENGES_ENABLED = "challenges_enabled"
        private const val KEY_DAILY_REMINDER = "daily_reminder"
        private const val KEY_REMINDER_TIME = "reminder_time"

        const val DEFAULT_FONT_SIZE = 16f
        const val DEFAULT_CARDS_PER_SESSION = 20
        const val DEFAULT_REMINDER_TIME = "20:00"
    }
}

enum class AppTheme(val value: String, val label: String) {
    LIGHT("light", "Clair"),
    DARK("dark", "Sombre"),
    SYSTEM("system", "Système");

    companion object {
        fun fromString(value: String?): AppTheme =
            entries.find { it.value == value } ?: SYSTEM
    }
}

enum class AccentColor(val value: String, val label: String, val colorHex: Long) {
    BLUE("blue", "Bleu", 0xFF4F8EF7),
    GREEN("green", "Vert", 0xFF4CAF50),
    VIOLET("violet", "Violet", 0xFF6750A4),
    ORANGE("orange", "Orange", 0xFFFF9800),
    PINK("pink", "Rose", 0xFFE91E63);

    companion object {
        fun fromString(value: String?): AccentColor =
            entries.find { it.value == value } ?: VIOLET
    }
}
