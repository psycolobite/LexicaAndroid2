package com.example.lexicaandroid2.features.sync

import android.util.Log
import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.FlashcardEntity
import com.example.lexicaandroid2.data.local.ReviewQuestionDao
import com.example.lexicaandroid2.data.local.ReviewQuestionProgressEntity
import com.example.lexicaandroid2.data.local.ReviewSessionSnapshotDao
import com.example.lexicaandroid2.data.local.Sm2DataEmbedded
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStat
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.features.gamification.data.UserStatsDao
import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import kotlinx.coroutines.flow.firstOrNull

/**
 * Résultat de la vérification de synchronisation au moment du login.
 */
sealed class SyncCheckResult {
    /** Compte cloud sans progression complète exploitable. */
    data class EmptyCloudAccount(
        val local: LocalProgressSummary,
        val requiresChoice: Boolean
    ) : SyncCheckResult()

    /** Cloud présent, mais progression locale vide. Import silencieux. */
    data class EmptyLocalImport(val cloudProgress: CloudProgress) : SyncCheckResult()

    /** XP identiques de chaque côté. Rien à faire. */
    object UpToDate : SyncCheckResult()

    /** Les deux côtés ont des données différentes. L'utilisateur doit choisir. */
    data class Conflict(
        val cloud: CloudProgress,
        val local: LocalProgressSummary
    ) : SyncCheckResult()

    /** Erreur réseau — continuer en local. */
    data class NetworkError(val message: String) : SyncCheckResult()
}

data class LocalProgressSummary(
    val xp: Long,
    val level: Int,
    val streak: Int,
    val cardCount: Int,
    val startedQuestionCount: Int,
    val favoriteCount: Int,
    val dailyReviewDays: Int,
    val hasMeaningfulProgress: Boolean
)

private data class LocalProgressSnapshot(
    val stats: UserStatsEntity,
    val cards: List<Flashcard>,
    val reviewQuestionProgress: List<ReviewQuestionProgress>,
    val dailyReviewStats: List<DailyReviewStat>
)

/**
 * Orchestrateur de synchronisation.
 *
 * Utilisé par [SyncViewModel]. N'est pas un ViewModel lui-même.
 *
 * @param firestoreSyncRepository  Accès Firestore.
 * @param userStatsDao             DAO direct pour écritures complètes d'entité (non disponible via l'interface repository).
 * @param userStatsRepository      Pour les lectures via Flow.
 * @param flashcardRepository      Pour récupérer / appliquer les favoris.
 */
class SyncManager(
    private val firestoreSyncRepository: FirestoreSyncRepository,
    private val flashcardDao: FlashcardDao,
    private val reviewQuestionDao: ReviewQuestionDao,
    private val reviewSessionSnapshotDao: ReviewSessionSnapshotDao,
    private val userStatsDao: UserStatsDao,
    private val userStatsRepository: UserStatsRepository,
    private val flashcardRepository: FlashcardRepository,
    private val dailyReviewStatDao: DailyReviewStatDao? = null
) {

    /**
     * Vérifie l'état Firestore immédiatement après connexion.
     * L'appelant décide quoi faire selon le résultat.
     */
    suspend fun checkOnLogin(uid: String): SyncCheckResult {
        return try {
            val cloudProgress = firestoreSyncRepository.downloadProgress(uid)
            val localSnapshot = readLocalProgress()
            val localSummary = localSnapshot.summary()

            when {
                cloudProgress == null -> SyncCheckResult.EmptyCloudAccount(
                    local = localSummary,
                    requiresChoice = localSummary.hasMeaningfulProgress
                )
                !localSummary.hasMeaningfulProgress -> SyncCheckResult.EmptyLocalImport(cloudProgress)
                localSnapshot.toCloudProgress().normalized() == cloudProgress.normalized() -> SyncCheckResult.UpToDate
                else -> SyncCheckResult.Conflict(
                    cloud = cloudProgress,
                    local = localSummary
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkOnLogin failed: ${e.message}", e)
            SyncCheckResult.NetworkError(e.message ?: "Erreur réseau")
        }
    }

    /**
     * Upload la progression locale vers Firestore.
     * Appelé quand :
     *  - Aucun document cloud existe (premier appareil).
     *  - L'utilisateur choisit de conserver sa progression locale.
     *  - Avant une déconnexion.
     */
    suspend fun uploadLocalToCloud(uid: String) {
        try {
            firestoreSyncRepository.uploadProgress(uid, readLocalProgress().toCloudProgress())
        } catch (e: Exception) {
            Log.e(TAG, "uploadLocalToCloud failed: ${e.message}", e)
        }
    }

    /**
     * Remplace la progression locale par celle du cloud.
     * Appelé quand l'utilisateur confirme dans [SyncConfirmDialog].
     */
    suspend fun replaceLocalWithCloud(uid: String, cloudProgress: CloudProgress) {
        try {
            applyCloudProgressLocally(cloudProgress)
            Log.d(
                TAG,
                "Local data replaced with cloud: xp=${cloudProgress.xp}, cards=${cloudProgress.flashcards.size}, questions=${cloudProgress.reviewQuestionProgress.size}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "replaceLocalWithCloud failed: ${e.message}", e)
            throw e
        }
    }

    suspend fun resetLocalAndUploadEmpty(uid: String) {
        val emptyCloudProgress = CloudProgress(updatedAt = System.currentTimeMillis())
        applyCloudProgressLocally(emptyCloudProgress)
        firestoreSyncRepository.uploadProgress(uid, emptyCloudProgress)
    }

    /**
     * Import silencieux du cloud quand la progression locale est vide.
     * Ne demande aucune confirmation.
     */
    suspend fun silentImportFromCloud(uid: String, cloudProgress: CloudProgress) {
        replaceLocalWithCloud(uid, cloudProgress)
    }

    /**
     * Sauvegarde avant déconnexion.
     * NE déconnecte PAS — l'appelant appelle ensuite authRepository.signOut().
     */
    suspend fun saveBeforeLogout(uid: String) {
        uploadLocalToCloud(uid)
    }

    suspend fun deleteCloudAccountData(uid: String) {
        try {
            firestoreSyncRepository.deleteProgress(uid)
        } catch (e: Exception) {
            Log.w(TAG, "deleteCloudAccountData failed: ${e.message}")
        }
    }

    /**
     * Push silencieux périodique (XP + streak uniquement).
     * Appelé toutes les 30 minutes depuis SyncViewModel si l'utilisateur est connecté.
     */
    suspend fun periodicPush(uid: String) {
        try {
            firestoreSyncRepository.uploadProgress(uid, readLocalProgress().toCloudProgress())
        } catch (e: Exception) {
            Log.d(TAG, "Periodic push skipped: ${e.message}")
        }
    }

    private suspend fun readLocalProgress(): LocalProgressSnapshot {
        val stats = userStatsRepository.getUserStats().firstOrNull() ?: UserStatsEntity()
        val cards = try {
            flashcardRepository.getAllCards()
        } catch (e: Exception) {
            Log.w(TAG, "Could not read flashcards: ${e.message}")
            emptyList()
        }
        val reviewProgress = try {
            flashcardRepository.getAllQuestionProgress()
        } catch (e: Exception) {
            Log.w(TAG, "Could not read review progress: ${e.message}")
            emptyList()
        }
        val dailyStats = try {
            dailyReviewStatDao?.getAllOnce().orEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Could not read daily review stats: ${e.message}")
            emptyList()
        }
        return LocalProgressSnapshot(
            stats = stats,
            cards = cards,
            reviewQuestionProgress = reviewProgress,
            dailyReviewStats = dailyStats
        )
    }

    private suspend fun applyCloudProgressLocally(cloudProgress: CloudProgress) {
        reviewSessionSnapshotDao.clearAll()
        reviewQuestionDao.deleteAll()
        flashcardDao.deleteAll()
        dailyReviewStatDao?.clearAll()

        if (cloudProgress.flashcards.isNotEmpty()) {
            flashcardDao.insertAll(cloudProgress.flashcards.map(CloudFlashcard::toEntity))
        }
        if (cloudProgress.reviewQuestionProgress.isNotEmpty()) {
            reviewQuestionDao.insertAll(cloudProgress.reviewQuestionProgress.map(CloudReviewQuestionProgress::toEntity))
        }
        if (cloudProgress.dailyReviewStats.isNotEmpty()) {
            dailyReviewStatDao?.upsertAll(cloudProgress.dailyReviewStats.map(CloudDailyReviewStat::toEntity))
        }

        userStatsDao.insertOrUpdate(
            UserStatsEntity(
                userId = "currentUser",
                xp = cloudProgress.xp,
                level = cloudProgress.level.coerceAtLeast(1),
                streak = cloudProgress.streak,
                lastLoginDate = cloudProgress.lastLoginDate
            )
        )
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}

private fun LocalProgressSnapshot.summary(): LocalProgressSummary {
    val meaningful = hasMeaningfulProgress()
    return LocalProgressSummary(
        xp = stats.xp,
        level = stats.level,
        streak = stats.streak,
        cardCount = cards.size,
        startedQuestionCount = reviewQuestionProgress.count { it.isStarted },
        favoriteCount = cards.count { it.favori },
        dailyReviewDays = dailyReviewStats.count { it.cardsReviewed > 0 || it.correctAnswers > 0 || it.totalTimeSeconds > 0 },
        hasMeaningfulProgress = meaningful
    )
}

private fun LocalProgressSnapshot.hasMeaningfulProgress(): Boolean {
    if (stats.xp > 0L || stats.streak > 0) return true
    if (cards.any { it.favori || it.notesPersonnelles.isNotBlank() }) return true
    if (reviewQuestionProgress.any {
            it.isStarted ||
                it.recentStreak > 0 ||
                it.weightedSuccess > 0.0 ||
                it.weightedFailure > 0.0 ||
                it.pendingReplacementChallengeKind != null
        }
    ) {
        return true
    }
    if (dailyReviewStats.any { it.cardsReviewed > 0 || it.correctAnswers > 0 || it.totalTimeSeconds > 0 }) {
        return true
    }
    return false
}

private fun LocalProgressSnapshot.toCloudProgress(updatedAt: Long = System.currentTimeMillis()): CloudProgress = CloudProgress(
    updatedAt = updatedAt,
    xp = stats.xp,
    level = stats.level,
    streak = stats.streak,
    lastLoginDate = stats.lastLoginDate,
    flashcards = cards.map(Flashcard::toCloudModel),
    reviewQuestionProgress = reviewQuestionProgress.map(ReviewQuestionProgress::toCloudModel),
    dailyReviewStats = dailyReviewStats.map(DailyReviewStat::toCloudModel)
)

private fun CloudProgress.normalized(): CloudProgress = copy(
    updatedAt = 0L,
    flashcards = flashcards.sortedBy { it.id },
    reviewQuestionProgress = reviewQuestionProgress.sortedBy { it.questionId },
    dailyReviewStats = dailyReviewStats.sortedBy { it.dateKey }
)

private fun Flashcard.toCloudModel(): CloudFlashcard = CloudFlashcard(
    id = id,
    recto = recto,
    verso = verso,
    synonymes = synonymes,
    exemples = exemples,
    categorieGrammaticale = categorieGrammaticale,
    registre = registre,
    etymologie = etymologie,
    dateAjout = dateAjout,
    favori = favori,
    notesPersonnelles = notesPersonnelles,
    sm2MotVersDef = sm2MotVersDef.toCloudModel(),
    sm2DefVersMot = sm2DefVersMot.toCloudModel()
)

private fun com.example.lexicaandroid2.domain.model.Sm2Stats.toCloudModel(): CloudSm2Stats = CloudSm2Stats(
    interval = interval,
    repetitions = repetitions,
    easeFactor = easeFactor,
    nextReviewDate = nextReviewDate,
    lastReviewDate = lastReviewDate,
    totalReviews = totalReviews,
    correctReviews = correctReviews,
    lapses = lapses
)

private fun ReviewQuestionProgress.toCloudModel(): CloudReviewQuestionProgress = CloudReviewQuestionProgress(
    questionId = questionId,
    cardId = cardId,
    questionType = questionType.name,
    globalOrder = globalOrder,
    level = level,
    intervalIndex = intervalIndex,
    peakIntervalIndex = peakIntervalIndex,
    weightedSuccess = weightedSuccess,
    weightedFailure = weightedFailure,
    recentStreak = recentStreak,
    recoveryReserve = recoveryReserve,
    currentIntervalDurationMs = currentIntervalDurationMs,
    nextDueAt = nextDueAt,
    lastSessionFirstAnswerAt = lastSessionFirstAnswerAt,
    lastAskedAt = lastAskedAt,
    firstAnsweredAt = firstAnsweredAt,
    pendingReplacementChallengeKind = pendingReplacementChallengeKind?.name
)

private fun DailyReviewStat.toCloudModel(): CloudDailyReviewStat = CloudDailyReviewStat(
    dateKey = dateKey,
    cardsReviewed = cardsReviewed,
    correctAnswers = correctAnswers,
    totalTimeSeconds = totalTimeSeconds
)

private fun CloudFlashcard.toEntity(): FlashcardEntity = FlashcardEntity(
    id = id,
    mot = recto,
    definition = verso,
    synonymes = synonymes,
    exemples = exemples,
    categorieGrammaticale = categorieGrammaticale,
    registre = registre,
    etymologie = etymologie,
    dateAjout = dateAjout,
    favori = favori,
    notesPersonnelles = notesPersonnelles,
    state = "LEARNING",
    sm2MotVersDef = sm2MotVersDef.toEntity(),
    sm2DefVersMot = sm2DefVersMot.toEntity()
)

private fun CloudSm2Stats.toEntity(): Sm2DataEmbedded = Sm2DataEmbedded(
    interval = interval,
    easeFactor = easeFactor,
    repetitions = repetitions,
    nextReview = nextReviewDate,
    lastReview = lastReviewDate,
    totalReviews = totalReviews,
    correctReviews = correctReviews,
    lapses = lapses
)

private fun CloudReviewQuestionProgress.toEntity(): ReviewQuestionProgressEntity = ReviewQuestionProgressEntity(
    questionId = questionId,
    cardId = cardId,
    questionType = questionType,
    globalOrder = globalOrder,
    level = level,
    intervalIndex = intervalIndex,
    peakIntervalIndex = peakIntervalIndex,
    weightedSuccess = weightedSuccess,
    weightedFailure = weightedFailure,
    recentStreak = recentStreak,
    recoveryReserve = recoveryReserve,
    currentIntervalDurationMs = currentIntervalDurationMs,
    nextDueAt = nextDueAt,
    lastSessionFirstAnswerAt = lastSessionFirstAnswerAt,
    lastAskedAt = lastAskedAt,
    firstAnsweredAt = firstAnsweredAt,
    pendingReplacementChallengeKind = pendingReplacementChallengeKind
)

private fun CloudDailyReviewStat.toEntity(): DailyReviewStat = DailyReviewStat(
    dateKey = dateKey,
    cardsReviewed = cardsReviewed,
    correctAnswers = correctAnswers,
    totalTimeSeconds = totalTimeSeconds
)

