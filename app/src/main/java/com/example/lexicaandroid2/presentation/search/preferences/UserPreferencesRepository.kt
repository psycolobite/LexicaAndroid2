package com.example.lexicaandroid2.presentation.search.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Repository de stockage et d'accès aux préférences utilisateur.
 *
 * Utilise SharedPreferences comme backend de stockage.
 * Le choix de SharedPreferences (plutôt que DataStore ou Room) est délibéré :
 * - les données sont simples et peu volumineuses,
 * - pas de dépendance supplémentaire,
 * - facile à migrer vers DataStore plus tard si besoin.
 *
 * @param context Contexte Android pour accéder aux SharedPreferences
 */
class UserPreferencesRepository(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val KEY_OBJECTIVES = "objectives"
        private const val KEY_DOMAINS = "preferred_domains"
        private const val KEY_REGISTERS = "preferred_registers"
        private const val KEY_LAST_UPDATED = "last_updated"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val SEPARATOR = ","
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Flow interne pour notifier les changements de préférences
    private val _preferencesFlow = MutableStateFlow(getPreferencesSync())

    /**
     * Flow des préférences utilisateur.
     * Émet à chaque modification et au premier abonnement.
     */
    fun getPreferences(): Flow<UserPreferences?> = _preferencesFlow.asStateFlow()

    /**
     * Flow indiquant si l'onboarding a été complété.
     */
    fun hasCompletedOnboarding(): Flow<Boolean> =
        _preferencesFlow.map { it != null }

    /**
     * Sauvegarde les préférences utilisateur.
     *
     * @param prefs Les préférences à sauvegarder
     */
    suspend fun savePreferences(preferences: UserPreferences) {
        val updated = preferences.copy(lastUpdated = System.currentTimeMillis())
        saveToPrefs(updated)
        _preferencesFlow.value = updated
    }

    /**
     * Efface toutes les préférences utilisateur.
     */
    suspend fun clearPreferences() {
        prefs.edit().clear().apply()
        _preferencesFlow.value = null
    }

    /**
     * Vérifie si l'utilisateur a complété l'onboarding.
     * Version synchrone pour utilisation dans les guards d'UI.
     */
    fun hasCompletedOnboardingSync(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_DONE, false)

    /**
     * Marque l'onboarding comme complété sans modifier les préférences.
     */
    suspend fun markOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
        // Ne pas notifier le flow car les préférences n'ont pas changé
    }

    // -----------------------------------------------------------------------
    // Méthodes privées
    // -----------------------------------------------------------------------

    /**
     * Lecture synchrone depuis SharedPreferences.
     */
    private fun getPreferencesSync(): UserPreferences? {
        if (!prefs.contains(KEY_OBJECTIVES)) return null

        val objectives = prefs.getString(KEY_OBJECTIVES, "")?.let { decodeList(it) }
            ?.mapNotNull { name ->
                try {
                    UserObjective.valueOf(name)
                } catch (_: IllegalArgumentException) {
                    null
                }
            } ?: return null

        val domains = prefs.getString(KEY_DOMAINS, "")?.let { decodeList(it) } ?: emptyList()
        val registers = prefs.getString(KEY_REGISTERS, "")?.let { decodeList(it) } ?: emptyList()
        val lastUpdated = prefs.getLong(KEY_LAST_UPDATED, 0L)

        return UserPreferences(
            objectives = objectives,
            preferredDomains = domains,
            preferredRegisters = registers,
            lastUpdated = lastUpdated
        )
    }

    /**
     * Écriture dans SharedPreferences.
     */
    private fun saveToPrefs(preferences: UserPreferences) {
        prefs.edit()
            .putString(KEY_OBJECTIVES, encodeList(preferences.objectives.map { it.name }))
            .putString(KEY_DOMAINS, encodeList(preferences.preferredDomains))
            .putString(KEY_REGISTERS, encodeList(preferences.preferredRegisters))
            .putLong(KEY_LAST_UPDATED, preferences.lastUpdated)
            .putBoolean(KEY_ONBOARDING_DONE, true)
            .apply()
    }

    /**
     * Encode une liste de chaînes en une chaîne séparée par des virgules.
     */
    private fun encodeList(list: List<String>): String =
        list.joinToString(SEPARATOR)

    /**
     * Décode une chaîne séparée par des virgules en une liste de chaînes.
     * Retourne une liste vide si la chaîne est vide ou null.
     */
    private fun decodeList(encoded: String): List<String> =
        if (encoded.isBlank()) emptyList()
        else encoded.split(SEPARATOR).map { it.trim() }.filter { it.isNotBlank() }
}
