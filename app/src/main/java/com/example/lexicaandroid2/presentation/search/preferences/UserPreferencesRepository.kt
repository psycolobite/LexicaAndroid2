package com.example.lexicaandroid2.presentation.search.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Dépôt pour la persistance des préférences utilisateur liées à la recherche.
 *
 * Utilise [SharedPreferences] pour la légèreté (pas de Room), sur le même modèle
 * que [com.example.lexicaandroid2.presentation.settings.UserPrefsRepository]
 * et [com.example.lexicaandroid2.presentation.admin.AdminPrefsRepository].
 *
 * @param context Contexte Android utilisé pour accéder aux SharedPreferences.
 */
class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Centres d'intérêt ──────────────────────────────────────────────────

    /**
     * Identifiants des catégories d'intérêt sélectionnées par l'utilisateur.
     * Stockés sous forme de `Set<String>` dans les SharedPreferences.
     * Par défaut, toutes les catégories sont sélectionnées.
     */
    var selectedInterestIds: Set<String>
        get() {
            val stored = prefs.getStringSet(KEY_SELECTED_INTERESTS, null)
            return if (stored != null && stored.isNotEmpty()) {
                stored
            } else {
                // Valeur par défaut : toutes les catégories
                InterestCategory.defaultSelection.map { it.id }.toSet()
            }
        }
        set(value) {
            prefs.edit { putStringSet(KEY_SELECTED_INTERESTS, value) }
        }

    // ── Filtres ────────────────────────────────────────────────────────────

    /**
     * Active/désactive le filtrage des résultats par centres d'intérêt.
     */
    var filterByInterests: Boolean
        get() = prefs.getBoolean(KEY_FILTER_BY_INTERESTS, false)
        set(value) = prefs.edit { putBoolean(KEY_FILTER_BY_INTERESTS, value) }

    /**
     * Nombre maximum de résultats affichés par recherche.
     * Contraint entre [UserPreferences.MIN_RESULTS] et [UserPreferences.MAX_RESULTS].
     */
    var maxResults: Int
        get() = prefs.getInt(
            KEY_MAX_RESULTS,
            UserPreferences.DEFAULT_MAX_RESULTS
        ).coerceIn(UserPreferences.MIN_RESULTS, UserPreferences.MAX_RESULTS)
        set(value) = prefs.edit {
            putInt(
                KEY_MAX_RESULTS,
                value.coerceIn(UserPreferences.MIN_RESULTS, UserPreferences.MAX_RESULTS)
            )
        }

    /**
     * Code langue pour la recherche (ex: "fr", "en").
     */
    var searchLanguage: String
        get() = prefs.getString(KEY_SEARCH_LANGUAGE, DEFAULT_SEARCH_LANGUAGE)
            ?: DEFAULT_SEARCH_LANGUAGE
        set(value) = prefs.edit { putString(KEY_SEARCH_LANGUAGE, value) }

    // ── Méthodes d'accès groupées ──────────────────────────────────────────

    /**
     * Retourne l'objet [UserPreferences] complet à partir des valeurs stockées.
     */
    fun getPreferences(): UserPreferences = UserPreferences(
        selectedInterestIds = selectedInterestIds,
        filterByInterests = filterByInterests,
        maxResults = maxResults,
        searchLanguage = searchLanguage
    )

    /**
     * Persiste l'ensemble des préférences en une seule opération.
     */
    fun savePreferences(preferences: UserPreferences) {
        prefs.edit {
            putStringSet(KEY_SELECTED_INTERESTS, preferences.selectedInterestIds)
            putBoolean(KEY_FILTER_BY_INTERESTS, preferences.filterByInterests)
            putInt(KEY_MAX_RESULTS, preferences.maxResults.coerceIn(
                UserPreferences.MIN_RESULTS, UserPreferences.MAX_RESULTS
            ))
            putString(KEY_SEARCH_LANGUAGE, preferences.searchLanguage)
        }
    }

    /**
     * Réinitialise toutes les préférences à leurs valeurs par défaut.
     */
    fun resetAll() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "search_user_prefs"
        private const val KEY_SELECTED_INTERESTS = "selected_interests"
        private const val KEY_FILTER_BY_INTERESTS = "filter_by_interests"
        private const val KEY_MAX_RESULTS = "max_results"
        private const val KEY_SEARCH_LANGUAGE = "search_language"
        private const val DEFAULT_SEARCH_LANGUAGE = "fr"
    }
}
