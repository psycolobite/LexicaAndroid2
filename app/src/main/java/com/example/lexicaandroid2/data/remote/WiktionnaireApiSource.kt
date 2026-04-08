package com.example.lexicaandroid2.data.remote

import android.util.Log
import com.example.lexicaandroid2.data.remote.model.WordResult
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.Normalizer
import java.util.concurrent.TimeUnit

class WiktionnaireApiSource(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val parser: WiktionnaireHtmlParser = WiktionnaireHtmlParser()
) : DictionaryLookupSource {

    override suspend fun search(query: String): List<WordResult> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return emptyList()

        val titles = linkedSetOf<String>()
        titles += trimmed
        titles += searchCandidateTitles(trimmed)

        return titles
            .take(MAX_TITLES)
            .flatMap { title -> fetchParsedEntry(title) }
            .distinctBy { result -> result.normalizedKey() }
    }

    private fun searchCandidateTitles(query: String): List<String> {
        val url = BASE_API_URL.toHttpUrl().newBuilder()
            .addQueryParameter("action", "query")
            .addQueryParameter("list", "search")
            .addQueryParameter("srsearch", query)
            .addQueryParameter("srlimit", MAX_TITLES.toString())
            .addQueryParameter("format", "json")
            .addQueryParameter("formatversion", "2")
            .build()

        return executeJson(url.toString())
            ?.getAsJsonObject("query")
            ?.getAsJsonArray("search")
            ?.mapNotNull { element ->
                element.asJsonObject.get("title")?.asString?.trim()?.takeIf { it.isNotBlank() }
            }
            ?: emptyList()
    }

    private fun fetchParsedEntry(title: String): List<WordResult> {
        val url = BASE_API_URL.toHttpUrl().newBuilder()
            .addQueryParameter("action", "parse")
            .addQueryParameter("page", title)
            .addQueryParameter("prop", "text")
            .addQueryParameter("redirects", "true")
            .addQueryParameter("format", "json")
            .addQueryParameter("formatversion", "2")
            .build()

        val parseObject = executeJson(url.toString())?.getAsJsonObject("parse") ?: return emptyList()
        val resolvedTitle = parseObject.get("title")?.asString?.trim().orEmpty().ifBlank { title }
        val html = parseObject.get("text")?.asString.orEmpty()
        if (html.isBlank()) return emptyList()

        val parsed = parser.parse(
            html = html,
            fallbackWord = resolvedTitle,
            source = "Wiktionnaire API"
        ) ?: return emptyList()

        return listOf(parsed)
    }

    private fun executeJson(url: String): JsonObject? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

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
            Log.w(TAG, "Echec appel API Wiktionnaire: ${e.message}")
            null
        }
    }

    private fun WordResult.normalizedKey(): String {
        return normalize(mot) + "|" + normalize(definition)
    }

    private fun normalize(value: String): String {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase()
            .trim()
    }

    companion object {
        private const val BASE_API_URL = "https://fr.wiktionary.org/w/api.php"
        private const val USER_AGENT = "LexicaAndroid2/1.0"
        private const val MAX_TITLES = 4
        private const val TAG = "WiktionnaireApiSource"
    }
}


