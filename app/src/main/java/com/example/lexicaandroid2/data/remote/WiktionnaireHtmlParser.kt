package com.example.lexicaandroid2.data.remote

import com.example.lexicaandroid2.data.remote.model.WordResult
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class WiktionnaireHtmlParser {

    fun parse(html: String, fallbackWord: String, source: String): WordResult? {
        return parseDocument(Jsoup.parse(html), fallbackWord, source)
    }

    fun parseDocument(document: Document, fallbackWord: String, source: String): WordResult? {
        val parserOutput = document.selectFirst("#mw-content-text > div.mw-parser-output")
            ?: document.selectFirst("div.mw-parser-output")
            ?: document.body()
            ?: return null

        val sectionElements = extractFrenchSection(parserOutput)
            ?: parserOutput.children().toList()

        val definitions = sectionElements.firstOrNull { element ->
            element.tagName() == "ol" && element.select("li").isNotEmpty()
        } ?: parserOutput.selectFirst("ol")

        val firstDefinition = definitions
            ?.children()
            ?.firstOrNull { it.tagName() == "li" }
            ?: definitions?.select("li")?.firstOrNull()
            ?: return null

        val cleanedDefinition = firstDefinition.clone().apply {
            select("ul, ol, dl, table, sup.reference, span.API, style, script").remove()
        }.text().trim()

        if (cleanedDefinition.isBlank()) {
            return null
        }

        val examples = firstDefinition.select("ul > li, dl > dd, dd > i")
            .map { it.text().trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(3)

        val category = extractCategory(sectionElements, definitions)
        val synonyms = extractListFromSection(sectionElements, "synonymes")
        val resolvedWord = document.selectFirst("h1#firstHeading")?.text()?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: fallbackWord.trim()

        return WordResult(
            mot = resolvedWord,
            definition = cleanedDefinition,
            categorieGrammaticale = category,
            exemples = examples,
            synonymes = synonyms,
            source = source
        )
    }

    private fun extractFrenchSection(parserOutput: Element): List<Element>? {
        val frenchHeadline = parserOutput.select("span.mw-headline").firstOrNull {
            it.text().trim().equals("Français", ignoreCase = true)
        } ?: parserOutput.selectFirst("span.mw-headline#Français")

        val frenchHeader = frenchHeadline?.parent() ?: return null
        val section = mutableListOf<Element>()
        var current = frenchHeader.nextElementSibling()
        while (current != null && current.tagName() != "h2") {
            section += current
            current = current.nextElementSibling()
        }
        return section
    }

    private fun extractCategory(sectionElements: List<Element>, definitions: Element?): String {
        if (definitions == null) return "Mot"

        val definitionIndex = sectionElements.indexOf(definitions)
        if (definitionIndex == -1) return "Mot"

        for (index in definitionIndex downTo 0) {
            val candidate = sectionElements[index]
            if (candidate.tagName() == "h3" || candidate.tagName() == "h4") {
                val label = candidate.select("span.mw-headline").text().trim()
                if (label.isNotBlank() && !label.equals("Synonymes", ignoreCase = true)) {
                    return label
                }
            }
        }

        return "Mot"
    }

    private fun extractListFromSection(sectionElements: List<Element>, sectionTitle: String): List<String> {
        val headerIndex = sectionElements.indexOfFirst { element ->
            (element.tagName() == "h3" || element.tagName() == "h4" || element.tagName() == "h5") &&
                element.select("span.mw-headline").text().trim().contains(sectionTitle, ignoreCase = true)
        }
        if (headerIndex == -1) return emptyList()

        val collected = mutableListOf<String>()
        for (index in headerIndex + 1 until sectionElements.size) {
            val element = sectionElements[index]
            if (element.tagName().matches(Regex("h[2-5]"))) {
                break
            }
            if (element.tagName() == "ul") {
                collected += element.select("li").map { it.text().trim() }
            }
        }

        return collected.filter { it.isNotBlank() }.distinct().take(5)
    }
}


