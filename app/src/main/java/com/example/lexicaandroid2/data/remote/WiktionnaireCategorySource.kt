package com.example.lexicaandroid2.data.remote

import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Récupère des listes de mots candidats depuis les catégories de fr.wiktionary.org.
 *
 * Utilise l'API MediaWiki `list=categorymembers` pour obtenir les pages appartenant à des
 * catégories lexicales pertinentes (registre soutenu, philosophie, rhétorique, etc.).
 *
 * Chaque appel est silencieux en cas d'erreur : si une catégorie ne répond pas,
 * on passe à la suivante.
 */
class WiktionnaireCategorySource(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    /**
     * Retourne une liste mélangée de mots candidats tirés des catégories Wiktionnaire.
     *
     * @param maxWords Nombre maximum de candidats à retourner (peut être supérieur au besoin
     *                 réel pour laisser de la marge après filtrage anti-doublon).
     */
    suspend fun fetchCandidateWords(maxWords: Int = 400): List<String> =
        withContext(Dispatchers.IO) {
            val collected = mutableListOf<String>()
            val seen = mutableSetOf<String>()

            for (category in CATEGORIES.shuffled()) {
                if (collected.size >= maxWords) break
                try {
                    val words = fetchFromCategory(category)
                    for (word in words) {
                        if (collected.size >= maxWords) break
                        val normalized = word.lowercase().trim()
                        if (normalized !in seen && isValidCandidate(word)) {
                            collected.add(word)
                            seen.add(normalized)
                        }
                    }
                    Log.d(TAG, "Catégorie «$category» : ${words.size} entrées récupérées")
                } catch (e: Exception) {
                    Log.w(TAG, "Échec catégorie «$category» : ${e.message}")
                }
            }

            Log.d(TAG, "Total candidats Wiktionnaire : ${collected.size}")
            collected.shuffled()
        }

    // ── Appel API ─────────────────────────────────────────────────────────────

    private fun fetchFromCategory(category: String): List<String> {
        val url = BASE_URL.toHttpUrl().newBuilder()
            .addQueryParameter("action", "query")
            .addQueryParameter("list", "categorymembers")
            .addQueryParameter("cmtitle", "Catégorie:$category")
            .addQueryParameter("cmlimit", MAX_PER_CATEGORY.toString())
            .addQueryParameter("cmtype", "page")
            .addQueryParameter("cmnamespace", "0")   // namespace 0 = mots, pas templates/catégories
            .addQueryParameter("format", "json")
            .addQueryParameter("formatversion", "2")
            .build()

        val json = executeJson(url.toString()) ?: return emptyList()

        return json.getAsJsonObject("query")
            ?.getAsJsonArray("categorymembers")
            ?.mapNotNull { element ->
                element.asJsonObject.get("title")?.asString?.trim()?.takeIf { it.isNotBlank() }
            }
            ?: emptyList()
    }

    private fun executeJson(url: String): JsonObject? {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "HTTP ${response.code} pour $url")
                    return null
                }
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return null
                JsonParser.parseString(body).asJsonObject
            }
        } catch (e: Exception) {
            Log.w(TAG, "Erreur réseau : ${e.message}")
            null
        }
    }

    // ── Filtre heuristique ────────────────────────────────────────────────────

    /**
     * Garde uniquement les mots :
     * - sans espace (pas d'expressions multi-mots)
     * - commençant par une minuscule (pas de noms propres)
     * - d'au moins 5 caractères (pas de mots trop courts)
     * - sans chiffres ni caractères spéciaux (sauf tiret interne)
     */
    private fun isValidCandidate(word: String): Boolean {
        if (word.contains(' ')) return false
        if (word.isEmpty() || word[0].isUpperCase()) return false
        if (word.length < 5) return false
        if (word.any { it.isDigit() }) return false
        if (word.startsWith('-') || word.endsWith('-')) return false
        return true
    }

    // ── Constantes ────────────────────────────────────────────────────────────

    companion object {
        private const val BASE_URL = "https://fr.wiktionary.org/w/api.php"
        private const val USER_AGENT = "LexicaAndroid2/1.0 (vocabulaire français)"
        private const val TAG = "WIKT_CATEGORY"
        private const val MAX_PER_CATEGORY = 500

        /**
         * Catégories fr.wiktionary.org ciblées pour l'apprentissage du vocabulaire avancé.
         * Elles sont mélangées aléatoirement à chaque appel pour varier les sources.
         */
        val CATEGORIES = listOf(
            "Registre soutenu en français",
            "Vocabulaire de la philosophie en français",
            "Vocabulaire de la rhétorique en français",
            "Vocabulaire de la psychologie en français",
            "Vocabulaire de la linguistique en français",
            "Vocabulaire de la littérature en français",
            "Vocabulaire du droit en français",
            "Vocabulaire de la politique en français",
            "Vocabulaire de la médecine en français",
            "Vocabulaire de la sociologie en français"
        )
    }
}

