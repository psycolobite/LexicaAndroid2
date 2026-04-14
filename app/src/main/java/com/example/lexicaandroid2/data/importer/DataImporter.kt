package com.example.lexicaandroid2.data.importer

import android.content.Context
import android.util.Log
import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.FlashcardEntity
import com.example.lexicaandroid2.data.local.ReviewQuestionDao
import com.example.lexicaandroid2.data.local.Sm2DataEmbedded
import com.example.lexicaandroid2.data.local.WordReserveDao
import com.example.lexicaandroid2.data.local.WordReserveEntity
import com.example.lexicaandroid2.data.mapper.toReviewQuestionProgressEntities
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DataImporter(
    private val context: Context,
    private val dao: FlashcardDao,
    private val reviewQuestionDao: ReviewQuestionDao,
    private val reserveDao: WordReserveDao? = null, // Optional for backward combat
    private val gson: Gson = Gson()
) {
    suspend fun importFromAssets(
        assetName: String = "local_storage.json",
        maxCards: Int? = null
    ) {
        Log.d(TAG, "Start importing...")
        val cards = readLegacyCards(assetName, maxCards)
        Log.d(TAG, "Entities created: ${cards.size} cards")
        if (cards.isEmpty()) {
            return
        }
        dao.insertAll(cards)
        reviewQuestionDao.insertAll(cards.flatMap { it.toReviewQuestionProgressEntities() })
        Log.d(TAG, "Import SUCCESS")
    }

    suspend fun importReserve(assetName: String = "mots_rares.json") {
        if (reserveDao == null) return
        Log.d(TAG, "Start importing reserve from $assetName...")
        try {
            context.assets.open(assetName).use { stream ->
                InputStreamReader(stream).use { reader ->
                    // Use streaming parser for large files
                    val jsonReader = gson.newJsonReader(reader)
                    jsonReader.beginArray()

                    val batch = mutableListOf<WordReserveEntity>()
                    var count = 0

                    while (jsonReader.hasNext()) {
                        val word: LegacyReserveWord = gson.fromJson(jsonReader, LegacyReserveWord::class.java)
                        batch.add(word.toEntity())

                        if (batch.size >= 500) {
                            reserveDao.insertAll(batch)
                            count += batch.size
                            Log.d(TAG, "Imported batch: $count words")
                            batch.clear()
                        }
                    }

                    // Insert remaining
                    if (batch.isNotEmpty()) {
                        reserveDao.insertAll(batch)
                        count += batch.size
                    }

                    jsonReader.endArray()
                    Log.d(TAG, "Reserve Import SUCCESS. Total: $count words")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import reserve: $e")
        }
    }

    private fun readLegacyCards(assetName: String, maxCards: Int? = null): List<FlashcardEntity> {
        context.assets.open(assetName).use { stream ->
            InputStreamReader(stream).use { reader ->
                val root = gson.fromJson(reader, LegacyStorage::class.java)
                val mapType = object : TypeToken<Map<String, LegacyCard>>() {}.type
                val cards = root?.cartes ?: emptyMap()
                val normalized = gson.fromJson<Map<String, LegacyCard>>(gson.toJson(cards), mapType)
                Log.d(TAG, "JSON read successfully")
                val selectedCards = maxCards?.let { normalized.values.take(it) } ?: normalized.values.toList()
                return selectedCards.map { it.toEntity() }
            }
        }
    }

    private fun LegacyCard.toEntity(): FlashcardEntity {
        val now = System.currentTimeMillis()
        return FlashcardEntity(
            id = id ?: "",
            mot = mot ?: "",
            definition = definition ?: "",
            synonymes = synonymes ?: emptyList(),
            exemples = exemples ?: emptyList(),
            categorieGrammaticale = categorieGrammaticale ?: "",
            registre = registre ?: "",
            etymologie = etymologie ?: "",
            dateAjout = parseDateToMillis(dateAjout, now),
            favori = favori ?: false,
            notesPersonnelles = notesPersonnelles ?: "",
            sm2MotVersDef = sm2MotVersDef.toEmbedded(now),
            sm2DefVersMot = sm2DefVersMot.toEmbedded(now)
        )
    }

    private fun LegacySm2?.toEmbedded(fallback: Long): Sm2DataEmbedded {
        val value = this ?: return Sm2DataEmbedded()
        return Sm2DataEmbedded(
            interval = value.interval ?: 0,
            easeFactor = value.easeFactor ?: 2.5,
            repetitions = value.repetitions ?: 0,
            nextReview = parseDateToMillis(value.nextReview, fallback),
            lastReview = parseDateToMillisOrNull(value.lastReview),
            totalReviews = value.totalReviews ?: 0,
            correctReviews = value.correctReviews ?: 0,
            lapses = value.lapses ?: 0
        )
    }

    private fun parseDateToMillis(value: String?, fallback: Long): Long {
        return parseDateToMillisOrNull(value) ?: fallback
    }

    private fun parseDateToMillisOrNull(value: String?): Long? {
        if (value.isNullOrBlank()) {
            return null
        }
        return try {
            val parsed = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
            parsed.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }

    private data class LegacyStorage(
        @SerializedName("cartes")
        val cartes: Map<String, LegacyCard>?
    )

    private data class LegacyCard(
        @SerializedName("id") val id: String?,
        @SerializedName("mot") val mot: String?,
        @SerializedName("definition") val definition: String?,
        @SerializedName("synonymes") val synonymes: List<String>?,
        @SerializedName("exemples") val exemples: List<String>?,
        @SerializedName("categorie_grammaticale") val categorieGrammaticale: String?,
        @SerializedName("registre") val registre: String?,
        @SerializedName("etymologie") val etymologie: String?,
        @SerializedName("date_ajout") val dateAjout: String?,
        @SerializedName("favori") val favori: Boolean?,
        @SerializedName("notes_personnelles") val notesPersonnelles: String?,
        @SerializedName("sm2_mot_vers_def") val sm2MotVersDef: LegacySm2?,
        @SerializedName("sm2_def_vers_mot") val sm2DefVersMot: LegacySm2?
    )

    private data class LegacySm2(
        @SerializedName("interval") val interval: Int?,
        @SerializedName("ease_factor") val easeFactor: Double?,
        @SerializedName("repetitions") val repetitions: Int?,
        @SerializedName("next_review") val nextReview: String?,
        @SerializedName("last_review") val lastReview: String?,
        @SerializedName("total_reviews") val totalReviews: Int?,
        @SerializedName("correct_reviews") val correctReviews: Int?,
        @SerializedName("lapses") val lapses: Int?
    )

    data class LegacyReserveWord(
        val id: String?,
        val mot: String?,
        val definition: String?,
        val categorie: String?,
        val synonymes: List<String>?,
        val exemples: List<String>?
    ) {
        fun toEntity(): WordReserveEntity {
            return WordReserveEntity(
                id = id ?: java.util.UUID.randomUUID().toString(),
                mot = mot ?: "",
                definition = definition ?: "",
                categorieGrammaticale = categorie ?: "",
                synonymes = synonymes ?: emptyList(),
                exemples = exemples ?: emptyList(),
                fromApi = false
            )
        }
    }

    companion object {
        private const val TAG = "DATA_IMPORT"
    }
}
