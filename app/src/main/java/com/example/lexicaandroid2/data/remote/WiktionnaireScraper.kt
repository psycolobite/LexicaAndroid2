package com.example.lexicaandroid2.data.remote

import android.util.Log
import com.example.lexicaandroid2.data.remote.model.WordResult
import org.jsoup.Jsoup

class WiktionnaireScraper {
    private val BASE_URL = "https://fr.wiktionary.org/wiki/"
    private val TAG = "WiktionnaireScraper"

    fun search(word: String): WordResult? {
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

            val doc = response.parse()

            // Find French section by text content, not just ID which can be flaky
            var frenchHeadline = doc.select("span.mw-headline").firstOrNull {
                it.text().trim().equals("Français", ignoreCase = true)
            }

            if (frenchHeadline == null) {
                // Fallback to ID
                frenchHeadline = doc.selectFirst("span.mw-headline#Français")
            }

            if (frenchHeadline == null) {
                Log.d(TAG, "Could not find element: French header")
                return null
            }

            val frenchHeader = frenchHeadline.parent()
            if (frenchHeader == null) {
                Log.d(TAG, "French header parent is null")
                return null
            }

            val contentElement = doc.selectFirst("#mw-content-text > div.mw-parser-output")
            if (contentElement == null) {
                Log.d(TAG, "Could not find element: content container")
                return null
            }

            val sectionElements = mutableListOf<org.jsoup.nodes.Element>()
            var current = frenchHeader.nextElementSibling()
            while (current != null && current.tagName() != "h2") {
                sectionElements.add(current)
                current = current.nextElementSibling()
            }
            Log.d(TAG, "French section element count: ${sectionElements.size}")

            // Find the first definition list <ol> in the French section
            val definitions = sectionElements
                .firstOrNull { it.tagName() == "ol" }

            if (definitions == null) {
                Log.d(TAG, "Could not find element: definitions <ol> in French section")
                // Fallback: simply look for first <ol> after header if section parsing failed
                return null
            }

            val firstDefLi = definitions.select("li").firstOrNull()
            if (firstDefLi == null) {
                Log.d(TAG, "Could not find element: first definition <li>")
                return null
            }

            val rawDefinition = firstDefLi.ownText()
            if (rawDefinition.isBlank()) {
                Log.d(TAG, "Definition is blank for $word")
                return null
            }

            val example = firstDefLi.select("ul > li > i").firstOrNull()?.text()
            if (example == null) {
                Log.d(TAG, "Could not find element: example <ul><li><i>")
            }
            val examplesList = if (example != null) listOf(example) else emptyList()

            var category = "Mot"
            var prev = definitions.previousElementSibling()
            while (prev != null) {
                if (prev.tagName() == "h3") {
                    category = prev.select("span.mw-headline").text()
                    if (category.isBlank()) {
                        Log.d(TAG, "Grammatical category headline is blank")
                        category = "Mot"
                    }
                    break
                }
                prev = prev.previousElementSibling()
            }
            if (prev == null) {
                Log.d(TAG, "Could not find element: grammatical category <h3>")
            }

            Log.d(TAG, "Parsed result for $word: category=$category, hasExample=${examplesList.isNotEmpty()}")
            return WordResult(
                mot = word,
                definition = rawDefinition,
                categorieGrammaticale = category,
                exemples = examplesList,
                source = "Wiktionnaire"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping $word", e)
            return null
        }
    }
}
