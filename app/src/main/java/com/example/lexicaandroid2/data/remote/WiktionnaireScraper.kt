package com.example.lexicaandroid2.data.remote

import android.util.Log
import com.example.lexicaandroid2.data.remote.model.WordResult
import org.jsoup.Jsoup

class WiktionnaireScraper(
    private val parser: WiktionnaireHtmlParser = WiktionnaireHtmlParser()
) : DictionaryLookupSource {
    private val BASE_URL = "https://fr.wiktionary.org/wiki/"
    private val TAG = "WiktionnaireScraper"

    override suspend fun search(query: String): List<WordResult> {
        return listOfNotNull(searchSingle(query))
    }

    fun searchSingle(word: String): WordResult? {
        val url = BASE_URL + word
        Log.d(TAG, "Searching for word: $word at URL: $url")
        try {
            val connection = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(15000)
                .ignoreHttpErrors(true)

            val response = connection.execute()
            Log.d(TAG, "HTTP status for $url: ${response.statusCode()}")
            if (response.statusCode() != 200) {
                Log.d(TAG, "Non-200 response, aborting parse for $word")
                return null
            }

            val parsed = parser.parseDocument(
                document = response.parse(),
                fallbackWord = word,
                source = "Wiktionnaire"
            )
            Log.d(TAG, "Parsed result for $word: success=${parsed != null}")
            return parsed
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping $word", e)
            return null
        }
    }
}
