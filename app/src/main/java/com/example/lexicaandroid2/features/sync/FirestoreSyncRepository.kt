package com.example.lexicaandroid2.features.sync

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Données de progression sauvegardées dans Firestore.
 * Document Firestore : users/{uid}
 */
data class CloudProgress(
    val updatedAt: Long = 0L,
    val xp: Long = 0L,
    val level: Int = 1,
    val streak: Int = 0,
    val lastLoginDate: Long = 0L,
    val flashcards: List<CloudFlashcard> = emptyList(),
    val reviewQuestionProgress: List<CloudReviewQuestionProgress> = emptyList(),
    val dailyReviewStats: List<CloudDailyReviewStat> = emptyList()
)

data class CloudFlashcard(
    val id: String = "",
    val recto: String = "",
    val verso: String = "",
    val synonymes: List<String> = emptyList(),
    val exemples: List<String> = emptyList(),
    val categorieGrammaticale: String = "",
    val registre: String = "",
    val etymologie: String = "",
    val dateAjout: Long = 0L,
    val favori: Boolean = false,
    val notesPersonnelles: String = "",
    val sm2MotVersDef: CloudSm2Stats = CloudSm2Stats(),
    val sm2DefVersMot: CloudSm2Stats = CloudSm2Stats()
)

data class CloudSm2Stats(
    val interval: Int = 0,
    val repetitions: Int = 0,
    val easeFactor: Double = 2.5,
    val nextReviewDate: Long = 0L,
    val lastReviewDate: Long? = null,
    val totalReviews: Int = 0,
    val correctReviews: Int = 0,
    val lapses: Int = 0
)

data class CloudReviewQuestionProgress(
    val questionId: String = "",
    val cardId: String = "",
    val questionType: String = "WORD_TO_DEFINITION",
    val globalOrder: Long = 0L,
    val level: Double = 0.0,
    val intervalIndex: Int = 0,
    val peakIntervalIndex: Int = 0,
    val weightedSuccess: Double = 0.0,
    val weightedFailure: Double = 0.0,
    val recentStreak: Int = 0,
    val recoveryReserve: Double = 0.0,
    val currentIntervalDurationMs: Long = 0L,
    val nextDueAt: Long = 0L,
    val lastSessionFirstAnswerAt: Long? = null,
    val lastAskedAt: Long? = null,
    val firstAnsweredAt: Long? = null,
    val pendingReplacementChallengeKind: String? = null
)

data class CloudDailyReviewStat(
    val dateKey: String = "",
    val cardsReviewed: Int = 0,
    val correctAnswers: Int = 0,
    val totalTimeSeconds: Int = 0
)

/**
 * Dépôt Firestore pour la synchronisation de la progression utilisateur.
 * Toutes les opérations échouent silencieusement en cas d'absence de réseau.
 */
class FirestoreSyncRepository {

    private val db by lazy { Firebase.firestore }

    /**
     * Envoie la progression locale vers Firestore.
     * Retourne success même si offline (Firestore met en cache et re-tente automatiquement).
     */
    suspend fun uploadProgress(uid: String, progress: CloudProgress): Result<Unit> = runCatching {
        val userDoc = db.collection(COLLECTION_USERS).document(uid)
        val data = mapOf(
            "schemaVersion" to CLOUD_SCHEMA_VERSION,
            "updatedAt" to progress.updatedAt,
            "xp" to progress.xp,
            "level" to progress.level,
            "streak" to progress.streak,
            "lastLoginDate" to progress.lastLoginDate,
            "cardCount" to progress.flashcards.size,
            "reviewQuestionProgressCount" to progress.reviewQuestionProgress.size,
            "dailyReviewStatCount" to progress.dailyReviewStats.size
        )
        userDoc.set(data).await()
        replaceCollection(
            collection = userDoc.collection(SUBCOLLECTION_FLASHCARDS),
            documents = progress.flashcards,
            idSelector = { it.id },
            mapper = ::cloudFlashcardToMap
        )
        replaceCollection(
            collection = userDoc.collection(SUBCOLLECTION_REVIEW_PROGRESS),
            documents = progress.reviewQuestionProgress,
            idSelector = { it.questionId },
            mapper = ::cloudReviewQuestionProgressToMap
        )
        replaceCollection(
            collection = userDoc.collection(SUBCOLLECTION_DAILY_STATS),
            documents = progress.dailyReviewStats,
            idSelector = { it.dateKey },
            mapper = ::cloudDailyReviewStatToMap
        )
        Log.d(
            TAG,
            "Upload OK: xp=${progress.xp} level=${progress.level} cards=${progress.flashcards.size} questions=${progress.reviewQuestionProgress.size}"
        )
        Unit
    }.also { result ->
        result.onFailure { Log.e(TAG, "Upload failed for uid=$uid: ${it.message}", it) }
    }

    /**
     * Télécharge la progression depuis Firestore.
     * Retourne null si le document n'existe pas ou si le réseau est indisponible.
     */
    suspend fun downloadProgress(uid: String): CloudProgress? {
        return try {
            val userDoc = db.collection(COLLECTION_USERS).document(uid)
            val doc = userDoc.get().await()
            if (!doc.exists()) {
                Log.d(TAG, "No cloud document for uid=$uid")
                return null
            }

            val flashcards = userDoc.collection(SUBCOLLECTION_FLASHCARDS)
                .get()
                .await()
                .documents
                .map(::documentToCloudFlashcard)
            val reviewProgress = userDoc.collection(SUBCOLLECTION_REVIEW_PROGRESS)
                .get()
                .await()
                .documents
                .map(::documentToCloudReviewQuestionProgress)
            val dailyStats = userDoc.collection(SUBCOLLECTION_DAILY_STATS)
                .get()
                .await()
                .documents
                .map(::documentToCloudDailyReviewStat)

            CloudProgress(
                updatedAt = doc.getLong("updatedAt") ?: 0L,
                xp = doc.getLong("xp") ?: 0L,
                level = (doc.getLong("level") ?: 1L).toInt(),
                streak = (doc.getLong("streak") ?: 0L).toInt(),
                lastLoginDate = doc.getLong("lastLoginDate") ?: 0L,
                flashcards = flashcards,
                reviewQuestionProgress = reviewProgress,
                dailyReviewStats = dailyStats
            ).also {
                Log.d(
                    TAG,
                    "Download OK: xp=${it.xp} level=${it.level} cards=${it.flashcards.size} questions=${it.reviewQuestionProgress.size}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed for uid=$uid: ${e.message}", e)
            null
        }
    }

    suspend fun deleteProgress(uid: String): Result<Unit> = runCatching {
        val userDoc = db.collection(COLLECTION_USERS).document(uid)
        deleteCollection(userDoc.collection(SUBCOLLECTION_FLASHCARDS))
        deleteCollection(userDoc.collection(SUBCOLLECTION_REVIEW_PROGRESS))
        deleteCollection(userDoc.collection(SUBCOLLECTION_DAILY_STATS))
        userDoc.delete().await()
        Log.d(TAG, "Delete OK for uid=$uid")
        Unit
    }.also { result ->
        result.onFailure { Log.e(TAG, "Delete failed for uid=$uid: ${it.message}", it) }
    }

    private suspend fun <T> replaceCollection(
        collection: CollectionReference,
        documents: List<T>,
        idSelector: (T) -> String,
        mapper: (T) -> Map<String, Any?>
    ) {
        deleteCollection(collection)
        documents.chunked(MAX_BATCH_SIZE).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { document ->
                batch.set(collection.document(idSelector(document)), mapper(document))
            }
            batch.commit().await()
        }
    }

    private suspend fun deleteCollection(collection: CollectionReference) {
        collection.get().await().documents.chunked(MAX_BATCH_SIZE).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { document -> batch.delete(document.reference) }
            batch.commit().await()
        }
    }

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val TAG = "FirestoreSyncRepo"
        private const val CLOUD_SCHEMA_VERSION = 2
        private const val MAX_BATCH_SIZE = 400
        private const val SUBCOLLECTION_FLASHCARDS = "flashcards"
        private const val SUBCOLLECTION_REVIEW_PROGRESS = "reviewQuestionProgress"
        private const val SUBCOLLECTION_DAILY_STATS = "dailyReviewStats"
    }
}

private fun cloudFlashcardToMap(card: CloudFlashcard): Map<String, Any?> = mapOf(
    "id" to card.id,
    "recto" to card.recto,
    "verso" to card.verso,
    "synonymes" to card.synonymes,
    "exemples" to card.exemples,
    "categorieGrammaticale" to card.categorieGrammaticale,
    "registre" to card.registre,
    "etymologie" to card.etymologie,
    "dateAjout" to card.dateAjout,
    "favori" to card.favori,
    "notesPersonnelles" to card.notesPersonnelles,
    "sm2MotVersDef" to cloudSm2StatsToMap(card.sm2MotVersDef),
    "sm2DefVersMot" to cloudSm2StatsToMap(card.sm2DefVersMot)
)

private fun cloudSm2StatsToMap(stats: CloudSm2Stats): Map<String, Any?> = mapOf(
    "interval" to stats.interval,
    "repetitions" to stats.repetitions,
    "easeFactor" to stats.easeFactor,
    "nextReviewDate" to stats.nextReviewDate,
    "lastReviewDate" to stats.lastReviewDate,
    "totalReviews" to stats.totalReviews,
    "correctReviews" to stats.correctReviews,
    "lapses" to stats.lapses
)

private fun cloudReviewQuestionProgressToMap(progress: CloudReviewQuestionProgress): Map<String, Any?> = mapOf(
    "questionId" to progress.questionId,
    "cardId" to progress.cardId,
    "questionType" to progress.questionType,
    "globalOrder" to progress.globalOrder,
    "level" to progress.level,
    "intervalIndex" to progress.intervalIndex,
    "peakIntervalIndex" to progress.peakIntervalIndex,
    "weightedSuccess" to progress.weightedSuccess,
    "weightedFailure" to progress.weightedFailure,
    "recentStreak" to progress.recentStreak,
    "recoveryReserve" to progress.recoveryReserve,
    "currentIntervalDurationMs" to progress.currentIntervalDurationMs,
    "nextDueAt" to progress.nextDueAt,
    "lastSessionFirstAnswerAt" to progress.lastSessionFirstAnswerAt,
    "lastAskedAt" to progress.lastAskedAt,
    "firstAnsweredAt" to progress.firstAnsweredAt,
    "pendingReplacementChallengeKind" to progress.pendingReplacementChallengeKind
)

private fun cloudDailyReviewStatToMap(stat: CloudDailyReviewStat): Map<String, Any?> = mapOf(
    "dateKey" to stat.dateKey,
    "cardsReviewed" to stat.cardsReviewed,
    "correctAnswers" to stat.correctAnswers,
    "totalTimeSeconds" to stat.totalTimeSeconds
)

private fun documentToCloudFlashcard(document: com.google.firebase.firestore.DocumentSnapshot): CloudFlashcard {
    val sm2MotVersDef = document.get("sm2MotVersDef") as? Map<*, *>
    val sm2DefVersMot = document.get("sm2DefVersMot") as? Map<*, *>
    return CloudFlashcard(
        id = document.getString("id") ?: document.id,
        recto = document.getString("recto") ?: "",
        verso = document.getString("verso") ?: "",
        synonymes = (document.get("synonymes") as? List<*>)?.filterIsInstance<String>().orEmpty(),
        exemples = (document.get("exemples") as? List<*>)?.filterIsInstance<String>().orEmpty(),
        categorieGrammaticale = document.getString("categorieGrammaticale") ?: "",
        registre = document.getString("registre") ?: "",
        etymologie = document.getString("etymologie") ?: "",
        dateAjout = document.getLong("dateAjout") ?: 0L,
        favori = document.getBoolean("favori") == true,
        notesPersonnelles = document.getString("notesPersonnelles") ?: "",
        sm2MotVersDef = mapToCloudSm2Stats(sm2MotVersDef),
        sm2DefVersMot = mapToCloudSm2Stats(sm2DefVersMot)
    )
}

private fun documentToCloudReviewQuestionProgress(document: com.google.firebase.firestore.DocumentSnapshot): CloudReviewQuestionProgress =
    CloudReviewQuestionProgress(
        questionId = document.getString("questionId") ?: document.id,
        cardId = document.getString("cardId") ?: "",
        questionType = document.getString("questionType") ?: "WORD_TO_DEFINITION",
        globalOrder = document.getLong("globalOrder") ?: 0L,
        level = document.getDouble("level") ?: 0.0,
        intervalIndex = (document.getLong("intervalIndex") ?: 0L).toInt(),
        peakIntervalIndex = (document.getLong("peakIntervalIndex") ?: 0L).toInt(),
        weightedSuccess = document.getDouble("weightedSuccess") ?: 0.0,
        weightedFailure = document.getDouble("weightedFailure") ?: 0.0,
        recentStreak = (document.getLong("recentStreak") ?: 0L).toInt(),
        recoveryReserve = document.getDouble("recoveryReserve") ?: 0.0,
        currentIntervalDurationMs = document.getLong("currentIntervalDurationMs") ?: 0L,
        nextDueAt = document.getLong("nextDueAt") ?: 0L,
        lastSessionFirstAnswerAt = document.getLong("lastSessionFirstAnswerAt"),
        lastAskedAt = document.getLong("lastAskedAt"),
        firstAnsweredAt = document.getLong("firstAnsweredAt"),
        pendingReplacementChallengeKind = document.getString("pendingReplacementChallengeKind")
    )

private fun documentToCloudDailyReviewStat(document: com.google.firebase.firestore.DocumentSnapshot): CloudDailyReviewStat =
    CloudDailyReviewStat(
        dateKey = document.getString("dateKey") ?: document.id,
        cardsReviewed = (document.getLong("cardsReviewed") ?: 0L).toInt(),
        correctAnswers = (document.getLong("correctAnswers") ?: 0L).toInt(),
        totalTimeSeconds = (document.getLong("totalTimeSeconds") ?: 0L).toInt()
    )

private fun mapToCloudSm2Stats(data: Map<*, *>?): CloudSm2Stats = CloudSm2Stats(
    interval = (data?.get("interval") as? Number)?.toInt() ?: 0,
    repetitions = (data?.get("repetitions") as? Number)?.toInt() ?: 0,
    easeFactor = (data?.get("easeFactor") as? Number)?.toDouble() ?: 2.5,
    nextReviewDate = (data?.get("nextReviewDate") as? Number)?.toLong() ?: 0L,
    lastReviewDate = (data?.get("lastReviewDate") as? Number)?.toLong(),
    totalReviews = (data?.get("totalReviews") as? Number)?.toInt() ?: 0,
    correctReviews = (data?.get("correctReviews") as? Number)?.toInt() ?: 0,
    lapses = (data?.get("lapses") as? Number)?.toInt() ?: 0
)

