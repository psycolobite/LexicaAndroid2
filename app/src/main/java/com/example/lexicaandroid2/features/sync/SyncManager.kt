package com.example.lexicaandroid2.features.sync

import android.util.Log
import com.example.lexicaandroid2.data.local.FlashcardDao
import com.example.lexicaandroid2.data.local.FlashcardEntity
import com.example.lexicaandroid2.data.local.FlashcardSyncStateDao
import com.example.lexicaandroid2.data.local.FlashcardSyncStateEntity
import com.example.lexicaandroid2.data.local.ReviewQuestionDao
import com.example.lexicaandroid2.data.local.ReviewQuestionProgressEntity
import com.example.lexicaandroid2.data.local.ReviewAnswerSyncEventDao
import com.example.lexicaandroid2.data.local.ReviewAnswerSyncEventEntity
import com.example.lexicaandroid2.data.local.ReviewSessionSnapshotDao
import com.example.lexicaandroid2.data.local.Sm2DataEmbedded
import com.example.lexicaandroid2.data.local.SyncResetMetadataDao
import com.example.lexicaandroid2.data.local.SyncResetMetadataEntity
import com.example.lexicaandroid2.domain.logic.ReviewIntervalEngine
import com.example.lexicaandroid2.domain.model.Flashcard
import com.example.lexicaandroid2.domain.model.ReviewAnswer
import com.example.lexicaandroid2.domain.model.ReviewQuestionProgress
import com.example.lexicaandroid2.domain.model.ReviewQuestionType
import com.example.lexicaandroid2.domain.model.ReviewSessionChallengeKind
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStat
import com.example.lexicaandroid2.features.gamification.data.DailyReviewStatDao
import com.example.lexicaandroid2.features.gamification.data.UserStatsEventType
import com.example.lexicaandroid2.features.gamification.data.UserStatsDao
import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import com.example.lexicaandroid2.features.gamification.data.UserStatsSyncEventDao
import com.example.lexicaandroid2.features.gamification.data.UserStatsSyncEventEntity
import com.example.lexicaandroid2.features.gamification.domain.XPCalculator
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

internal enum class SilentConflictResolution {
    KEEP_LOCAL,
    KEEP_CLOUD
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
    val userStatsEvents: List<UserStatsSyncEventEntity>,
    val cards: List<Flashcard>,
    val reviewQuestionProgress: List<ReviewQuestionProgress>,
    val dailyReviewStats: List<DailyReviewStat>,
    val flashcardSyncStates: List<FlashcardSyncStateEntity>,
    val reviewAnswerEvents: List<ReviewAnswerSyncEventEntity>,
    val resetMetadata: SyncResetMetadataEntity?
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
    private val flashcardSyncStateDao: FlashcardSyncStateDao? = null,
    private val reviewAnswerSyncEventDao: ReviewAnswerSyncEventDao? = null,
    private val reviewSessionSnapshotDao: ReviewSessionSnapshotDao,
    private val syncResetMetadataDao: SyncResetMetadataDao? = null,
    private val userStatsDao: UserStatsDao,
    private val userStatsSyncEventDao: UserStatsSyncEventDao? = null,
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
        val resetAt = System.currentTimeMillis()
        val currentGeneration = syncResetMetadataDao?.getCurrent()?.resetGeneration ?: 0L
        val emptyCloudProgress = CloudProgress(
            updatedAt = resetAt,
            resetAt = resetAt,
            resetGeneration = currentGeneration + 1L
        )
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
     * Résout silencieusement un conflit sans afficher de dialogue.
     * Utilisé pour les sessions déjà authentifiées au lancement de l'app.
     *
     * Politique V2 : fusionner local et cloud de manière additive quand c'est
     * raisonnablement sûr, puis propager l'état fusionné des deux côtés.
     */
    internal suspend fun resolveConflictSilently(uid: String, cloudProgress: CloudProgress) {
        val localSnapshot = readLocalProgress()
        val mergedProgress = mergeCloudProgress(localSnapshot.toCloudProgress(), cloudProgress)
            .copy(updatedAt = System.currentTimeMillis())
        applyCloudProgressLocally(mergedProgress)
        firestoreSyncRepository.uploadProgress(uid, mergedProgress)
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
        val userStatsEvents = try {
            userStatsSyncEventDao?.getAll().orEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Could not read user stats sync events: ${e.message}")
            emptyList()
        }
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
        val flashcardSyncStates = try {
            flashcardSyncStateDao?.getAll().orEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Could not read flashcard sync state: ${e.message}")
            emptyList()
        }
        val reviewAnswerEvents = try {
            reviewAnswerSyncEventDao?.getAll().orEmpty()
        } catch (e: Exception) {
            Log.w(TAG, "Could not read review answer sync events: ${e.message}")
            emptyList()
        }
        val resetMetadata = try {
            syncResetMetadataDao?.getCurrent()
        } catch (e: Exception) {
            Log.w(TAG, "Could not read sync reset metadata: ${e.message}")
            null
        }
        return LocalProgressSnapshot(
            stats = stats,
            userStatsEvents = userStatsEvents,
            cards = cards,
            reviewQuestionProgress = reviewProgress,
            dailyReviewStats = dailyStats,
            flashcardSyncStates = flashcardSyncStates,
            reviewAnswerEvents = reviewAnswerEvents,
            resetMetadata = resetMetadata
        )
    }

    private suspend fun applyCloudProgressLocally(cloudProgress: CloudProgress) {
        reviewSessionSnapshotDao.clearAll()
        reviewQuestionDao.deleteAll()
        flashcardDao.deleteAll()
        dailyReviewStatDao?.clearAll()
        flashcardSyncStateDao?.clearAll()
        reviewAnswerSyncEventDao?.clearAll()
        userStatsSyncEventDao?.clearAll()
        syncResetMetadataDao?.clearAll()

        if (cloudProgress.flashcards.isNotEmpty()) {
            flashcardDao.insertAll(cloudProgress.flashcards.map(CloudFlashcard::toEntity))
        }
        if (cloudProgress.reviewQuestionProgress.isNotEmpty()) {
            reviewQuestionDao.insertAll(cloudProgress.reviewQuestionProgress.map(CloudReviewQuestionProgress::toEntity))
        }
        if (cloudProgress.dailyReviewStats.isNotEmpty()) {
            dailyReviewStatDao?.upsertAll(cloudProgress.dailyReviewStats.map(CloudDailyReviewStat::toEntity))
        }
        val syncStates = cloudProgress.flashcards.map(CloudFlashcard::toSyncStateEntity) +
            cloudProgress.deletedFlashcards.map(CloudDeletedFlashcard::toSyncStateEntity)
        if (syncStates.isNotEmpty()) {
            flashcardSyncStateDao?.upsertAll(syncStates.distinctBy { it.cardId })
        }
        if (cloudProgress.reviewAnswerEvents.isNotEmpty()) {
            reviewAnswerSyncEventDao?.insertAll(cloudProgress.reviewAnswerEvents.map(CloudReviewAnswerSyncEvent::toEntity))
        }
        if (cloudProgress.userStatsEvents.isNotEmpty()) {
            userStatsSyncEventDao?.insertAll(cloudProgress.userStatsEvents.map(CloudUserStatsEvent::toEntity))
        }
        syncResetMetadataDao?.upsert(
            SyncResetMetadataEntity(
                resetAt = cloudProgress.resetAt,
                resetGeneration = cloudProgress.resetGeneration
            )
        )

        val projectedStats = projectUserStats(
            cloudProgress.userStatsEvents,
            fallback = UserStatsEntity(
                xp = cloudProgress.xp,
                level = cloudProgress.level.coerceAtLeast(1),
                streak = cloudProgress.streak,
                lastLoginDate = cloudProgress.lastLoginDate
            )
        )

        userStatsDao.insertOrUpdate(
            projectedStats.copy(userId = "currentUser")
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

internal fun mergeCloudProgress(local: CloudProgress, cloud: CloudProgress): CloudProgress {
    val resetMarker = dominantResetMarker(local, cloud)
    val effectiveLocal = local.dropDataBeforeReset(resetMarker)
    val effectiveCloud = cloud.dropDataBeforeReset(resetMarker)

    val mergedCards = mergeCloudFlashcards(effectiveLocal.flashcards, effectiveCloud.flashcards)
    val activeDeletedFlashcards = filterActiveDeletedFlashcards(
        mergeDeletedFlashcards(effectiveLocal.deletedFlashcards, effectiveCloud.deletedFlashcards),
        mergedCards
    )
    val deletedCardIds = activeDeletedFlashcards.map { it.cardId }.toSet()
    val mergedUserStatsEvents = mergeUserStatsEvents(effectiveLocal.userStatsEvents, effectiveCloud.userStatsEvents)
    val projectedStats = projectUserStats(
        events = mergedUserStatsEvents,
        fallback = UserStatsEntity(
            xp = maxOf(effectiveLocal.xp, effectiveCloud.xp),
            level = maxOf(effectiveLocal.level, effectiveCloud.level),
            streak = maxOf(effectiveLocal.streak, effectiveCloud.streak),
            lastLoginDate = maxOf(effectiveLocal.lastLoginDate, effectiveCloud.lastLoginDate)
        )
    )
    return CloudProgress(
        updatedAt = maxOf(effectiveLocal.updatedAt, effectiveCloud.updatedAt),
        resetAt = resetMarker.resetAt,
        resetGeneration = resetMarker.resetGeneration,
        xp = projectedStats.xp,
        level = projectedStats.level,
        streak = projectedStats.streak,
        lastLoginDate = projectedStats.lastLoginDate,
        userStatsEvents = mergedUserStatsEvents,
        flashcards = mergedCards.filterNot { it.id in deletedCardIds },
        deletedFlashcards = activeDeletedFlashcards,
        reviewQuestionProgress = projectReviewQuestionProgress(
            localSnapshots = effectiveLocal.reviewQuestionProgress,
            cloudSnapshots = effectiveCloud.reviewQuestionProgress,
            activeCards = mergedCards.filterNot { it.id in deletedCardIds },
            reviewAnswerEvents = mergeReviewAnswerEvents(effectiveLocal.reviewAnswerEvents, effectiveCloud.reviewAnswerEvents)
        ),
        dailyReviewStats = mergeDailyStats(effectiveLocal.dailyReviewStats, effectiveCloud.dailyReviewStats),
        reviewAnswerEvents = mergeReviewAnswerEvents(effectiveLocal.reviewAnswerEvents, effectiveCloud.reviewAnswerEvents)
            .filter { it.cardId !in deletedCardIds }
    )
}

private data class ResetMarker(
    val resetAt: Long,
    val resetGeneration: Long
)

private fun dominantResetMarker(local: CloudProgress, cloud: CloudProgress): ResetMarker {
    return when {
        local.resetGeneration > cloud.resetGeneration -> ResetMarker(local.resetAt, local.resetGeneration)
        cloud.resetGeneration > local.resetGeneration -> ResetMarker(cloud.resetAt, cloud.resetGeneration)
        else -> ResetMarker(maxOf(local.resetAt, cloud.resetAt), local.resetGeneration)
    }
}

private fun CloudProgress.dropDataBeforeReset(marker: ResetMarker): CloudProgress {
    if (resetGeneration < marker.resetGeneration) {
        return copy(
            resetAt = resetAt,
            resetGeneration = resetGeneration,
            xp = 0L,
            level = 1,
            streak = 0,
            lastLoginDate = 0L,
            userStatsEvents = emptyList(),
            flashcards = emptyList(),
            deletedFlashcards = emptyList(),
            reviewQuestionProgress = emptyList(),
            dailyReviewStats = emptyList(),
            reviewAnswerEvents = emptyList()
        )
    }

    if (marker.resetAt <= 0L) return copy(resetAt = 0L)

    val activeFlashcards = flashcards.filter { card ->
        maxOf(card.lastModifiedAt, card.favoriteUpdatedAt, card.dateAjout) >= marker.resetAt
    }
    val activeCardIds = activeFlashcards.map { it.id }.toSet()

    return copy(
        resetAt = marker.resetAt,
        resetGeneration = marker.resetGeneration,
        userStatsEvents = userStatsEvents.filter { it.occurredAt >= marker.resetAt },
        flashcards = activeFlashcards,
        deletedFlashcards = deletedFlashcards.filter { it.deletedAt >= marker.resetAt },
        reviewQuestionProgress = reviewQuestionProgress.filter { progress ->
            progress.cardId in activeCardIds &&
                maxOf(
                    progress.lastSessionFirstAnswerAt ?: Long.MIN_VALUE,
                    progress.lastAskedAt ?: Long.MIN_VALUE,
                    progress.firstAnsweredAt ?: Long.MIN_VALUE,
                    progress.nextDueAt
                ) >= marker.resetAt
        },
        dailyReviewStats = dailyReviewStats.filter { it.dateKey >= marker.resetAt.toIsoLocalDateKey() },
        reviewAnswerEvents = reviewAnswerEvents.filter { event ->
            event.cardId in activeCardIds && event.answeredAt >= marker.resetAt
        }
    )
}

private fun mergeCloudFlashcards(local: List<CloudFlashcard>, cloud: List<CloudFlashcard>): List<CloudFlashcard> {
    return (local + cloud)
        .groupBy { it.id }
        .values
        .map { entries -> entries.reduce(::mergeCloudFlashcard) }
        .sortedBy { it.id }
}

private fun mergeCloudFlashcard(local: CloudFlashcard, cloud: CloudFlashcard): CloudFlashcard {
    val preferredContent = when {
        local.lastModifiedAt > cloud.lastModifiedAt -> local
        cloud.lastModifiedAt > local.lastModifiedAt -> cloud
        else -> null
    }
    val favorite = when {
        local.favoriteUpdatedAt == 0L && cloud.favoriteUpdatedAt == 0L -> local.favori || cloud.favori
        local.favoriteUpdatedAt > cloud.favoriteUpdatedAt -> local.favori
        cloud.favoriteUpdatedAt > local.favoriteUpdatedAt -> cloud.favori
        else -> cloud.favori
    }
    return CloudFlashcard(
        id = preferNonBlank(local.id, cloud.id),
        recto = preferredContent?.recto?.takeIf { it.isNotBlank() } ?: preferText(local.recto, cloud.recto),
        verso = preferredContent?.verso?.takeIf { it.isNotBlank() } ?: preferText(local.verso, cloud.verso),
        synonymes = mergeDistinct(local.synonymes, cloud.synonymes),
        exemples = mergeDistinct(local.exemples, cloud.exemples),
        categorieGrammaticale = preferredContent?.categorieGrammaticale?.takeIf { it.isNotBlank() }
            ?: preferText(local.categorieGrammaticale, cloud.categorieGrammaticale),
        registre = preferredContent?.registre?.takeIf { it.isNotBlank() }
            ?: preferText(local.registre, cloud.registre),
        etymologie = preferredContent?.etymologie?.takeIf { it.isNotBlank() }
            ?: preferText(local.etymologie, cloud.etymologie),
        dateAjout = listOf(local.dateAjout, cloud.dateAjout).filter { it > 0L }.minOrNull() ?: maxOf(local.dateAjout, cloud.dateAjout),
        favori = favorite,
        notesPersonnelles = preferredContent?.notesPersonnelles?.takeIf { it.isNotBlank() }
            ?: preferText(local.notesPersonnelles, cloud.notesPersonnelles),
        lastModifiedAt = maxOf(local.lastModifiedAt, cloud.lastModifiedAt),
        favoriteUpdatedAt = maxOf(local.favoriteUpdatedAt, cloud.favoriteUpdatedAt),
        sm2MotVersDef = mergeSm2Stats(local.sm2MotVersDef, cloud.sm2MotVersDef),
        sm2DefVersMot = mergeSm2Stats(local.sm2DefVersMot, cloud.sm2DefVersMot)
    )
}

private fun mergeDeletedFlashcards(
    local: List<CloudDeletedFlashcard>,
    cloud: List<CloudDeletedFlashcard>
): List<CloudDeletedFlashcard> {
    return (local + cloud)
        .groupBy { it.cardId }
        .values
        .map { entries -> entries.maxByOrNull { it.deletedAt } ?: entries.first() }
        .sortedBy { it.cardId }
}

private fun filterActiveDeletedFlashcards(
    deletedFlashcards: List<CloudDeletedFlashcard>,
    flashcards: List<CloudFlashcard>
): List<CloudDeletedFlashcard> {
    val cardsById = flashcards.associateBy { it.id }
    return deletedFlashcards.filter { deleted ->
        val card = cardsById[deleted.cardId] ?: return@filter true
        deleted.deletedAt >= maxOf(card.lastModifiedAt, card.favoriteUpdatedAt, card.dateAjout)
    }
}

private fun mergeSm2Stats(local: CloudSm2Stats, cloud: CloudSm2Stats): CloudSm2Stats {
    return CloudSm2Stats(
        interval = maxOf(local.interval, cloud.interval),
        repetitions = maxOf(local.repetitions, cloud.repetitions),
        easeFactor = maxOf(local.easeFactor, cloud.easeFactor),
        nextReviewDate = maxOf(local.nextReviewDate, cloud.nextReviewDate),
        lastReviewDate = maxNullable(local.lastReviewDate, cloud.lastReviewDate),
        totalReviews = maxOf(local.totalReviews, cloud.totalReviews),
        correctReviews = maxOf(local.correctReviews, cloud.correctReviews),
        lapses = maxOf(local.lapses, cloud.lapses)
    )
}

private fun mergeQuestionProgress(
    local: List<CloudReviewQuestionProgress>,
    cloud: List<CloudReviewQuestionProgress>
): List<CloudReviewQuestionProgress> {
    return (local + cloud)
        .groupBy { it.questionId }
        .values
        .map { entries -> entries.reduce(::mergeQuestionProgressEntry) }
        .sortedBy { it.questionId }
}

private fun mergeQuestionProgressEntry(
    local: CloudReviewQuestionProgress,
    cloud: CloudReviewQuestionProgress
): CloudReviewQuestionProgress {
    return CloudReviewQuestionProgress(
        questionId = preferNonBlank(local.questionId, cloud.questionId),
        cardId = preferNonBlank(local.cardId, cloud.cardId),
        questionType = preferNonBlank(local.questionType, cloud.questionType),
        globalOrder = maxOf(local.globalOrder, cloud.globalOrder),
        level = maxOf(local.level, cloud.level),
        intervalIndex = maxOf(local.intervalIndex, cloud.intervalIndex),
        peakIntervalIndex = maxOf(local.peakIntervalIndex, cloud.peakIntervalIndex),
        weightedSuccess = maxOf(local.weightedSuccess, cloud.weightedSuccess),
        weightedFailure = maxOf(local.weightedFailure, cloud.weightedFailure),
        recentStreak = maxOf(local.recentStreak, cloud.recentStreak),
        recoveryReserve = maxOf(local.recoveryReserve, cloud.recoveryReserve),
        currentIntervalDurationMs = maxOf(local.currentIntervalDurationMs, cloud.currentIntervalDurationMs),
        nextDueAt = maxOf(local.nextDueAt, cloud.nextDueAt),
        lastSessionFirstAnswerAt = maxNullable(local.lastSessionFirstAnswerAt, cloud.lastSessionFirstAnswerAt),
        lastAskedAt = maxNullable(local.lastAskedAt, cloud.lastAskedAt),
        firstAnsweredAt = maxNullable(local.firstAnsweredAt, cloud.firstAnsweredAt),
        pendingReplacementChallengeKind = local.pendingReplacementChallengeKind ?: cloud.pendingReplacementChallengeKind
    )
}

private fun mergeDailyStats(local: List<CloudDailyReviewStat>, cloud: List<CloudDailyReviewStat>): List<CloudDailyReviewStat> {
    return (local + cloud)
        .groupBy { it.dateKey }
        .values
        .map { entries ->
            entries.reduce { left, right ->
                CloudDailyReviewStat(
                    dateKey = preferNonBlank(left.dateKey, right.dateKey),
                    cardsReviewed = maxOf(left.cardsReviewed, right.cardsReviewed),
                    correctAnswers = maxOf(left.correctAnswers, right.correctAnswers),
                    totalTimeSeconds = maxOf(left.totalTimeSeconds, right.totalTimeSeconds)
                )
            }
        }
        .sortedBy { it.dateKey }
}

private fun mergeReviewAnswerEvents(
    local: List<CloudReviewAnswerSyncEvent>,
    cloud: List<CloudReviewAnswerSyncEvent>
): List<CloudReviewAnswerSyncEvent> {
    return (local + cloud)
        .groupBy { it.eventId }
        .values
        .map { entries -> entries.maxByOrNull { it.answeredAt } ?: entries.first() }
        .sortedWith(compareBy<CloudReviewAnswerSyncEvent> { it.answeredAt }.thenBy { it.eventId })
}

private fun projectReviewQuestionProgress(
    localSnapshots: List<CloudReviewQuestionProgress>,
    cloudSnapshots: List<CloudReviewQuestionProgress>,
    activeCards: List<CloudFlashcard>,
    reviewAnswerEvents: List<CloudReviewAnswerSyncEvent>
): List<CloudReviewQuestionProgress> {
    val mergedSnapshots = mergeQuestionProgress(localSnapshots, cloudSnapshots)
    val activeCardIds = activeCards.map { it.id }.toSet()
    val activeCardsById = activeCards.associateBy { it.id }
    val localByQuestion = localSnapshots.associateBy { it.questionId }
    val cloudByQuestion = cloudSnapshots.associateBy { it.questionId }
    val eventsByQuestion = reviewAnswerEvents
        .filter { it.cardId in activeCardIds }
        .groupBy { it.questionId }

    val questionIds = buildSet {
        addAll(mergedSnapshots.map { it.questionId })
        addAll(eventsByQuestion.keys)
    }

    return questionIds.mapNotNull { questionId ->
        val mergedSnapshot = mergedSnapshots.firstOrNull { it.questionId == questionId }
        val cardId = mergedSnapshot?.cardId ?: eventsByQuestion[questionId]?.firstOrNull()?.cardId ?: return@mapNotNull null
        if (cardId !in activeCardIds) return@mapNotNull null

        val events = eventsByQuestion[questionId].orEmpty().sortedBy { it.answeredAt }
        if (events.isEmpty()) {
            return@mapNotNull mergedSnapshot ?: activeCardsById[cardId]?.let { seedQuestionProgress(it, questionId) }
        }

        val baseline = chooseReviewReplayBaseline(
            local = localByQuestion[questionId],
            cloud = cloudByQuestion[questionId],
            card = activeCardsById.getValue(cardId),
            questionId = questionId
        )
        val replayEvents = events.filter { event ->
            val baselineAnsweredAt = baseline.lastSessionFirstAnswerAt ?: Long.MIN_VALUE
            event.answeredAt > baselineAnsweredAt
        }

        if (replayEvents.isEmpty()) {
            return@mapNotNull mergedSnapshot ?: baseline
        }

        replayEvents.fold(baseline) { progress, event ->
            ReviewIntervalEngine.applyFirstAnswer(
                progress = progress.copy(pendingReplacementChallengeKind = null).toDomain(),
                answer = event.toReviewAnswer(),
                nowMs = event.answeredAt
            ).toCloudModel()
        }
    }.sortedBy { it.questionId }
}

private fun chooseReviewReplayBaseline(
    local: CloudReviewQuestionProgress?,
    cloud: CloudReviewQuestionProgress?,
    card: CloudFlashcard,
    questionId: String
): CloudReviewQuestionProgress {
    return when {
        local == null && cloud == null -> seedQuestionProgress(card, questionId)
        local == null -> cloud!!
        cloud == null -> local
        else -> {
            val localAnsweredAt = local.lastSessionFirstAnswerAt ?: Long.MIN_VALUE
            val cloudAnsweredAt = cloud.lastSessionFirstAnswerAt ?: Long.MIN_VALUE
            if (localAnsweredAt <= cloudAnsweredAt) local else cloud
        }
    }
}

private fun seedQuestionProgress(card: CloudFlashcard, questionId: String): CloudReviewQuestionProgress {
    val questionType = questionTypeFromQuestionId(questionId)
    return CloudReviewQuestionProgress(
        questionId = questionId,
        cardId = card.id,
        questionType = questionType.name,
        globalOrder = (card.dateAjout * 2L) + questionType.globalOrderOffset,
        currentIntervalDurationMs = ReviewQuestionProgress.DEFAULT_INTERVAL_DURATION_MS,
        nextDueAt = card.dateAjout.coerceAtLeast(0L)
    )
}

private fun questionTypeFromQuestionId(questionId: String): ReviewQuestionType =
    ReviewQuestionType.fromStorage(questionId.substringAfterLast("::"))

private fun CloudReviewAnswerSyncEvent.toReviewAnswer(): ReviewAnswer = ReviewAnswer.valueOf(answer)

private fun mergeUserStatsEvents(
    local: List<CloudUserStatsEvent>,
    cloud: List<CloudUserStatsEvent>
): List<CloudUserStatsEvent> {
    val merged = (local + cloud)
        .groupBy { it.eventId }
        .values
        .map { entries -> entries.maxByOrNull { it.occurredAt } ?: entries.first() }
        .sortedWith(compareBy<CloudUserStatsEvent> { it.occurredAt }.thenBy { it.eventId })

    if (merged.isNotEmpty()) return merged

    return listOfNotNull(
        local.legacyBootstrapUserStatsEvent(),
        cloud.legacyBootstrapUserStatsEvent()
    )
        .distinctBy { it.eventId }
        .sortedWith(compareBy<CloudUserStatsEvent> { it.occurredAt }.thenBy { it.eventId })
}

private fun preferText(local: String, cloud: String): String {
    val normalizedLocal = local.trim()
    val normalizedCloud = cloud.trim()
    return when {
        normalizedLocal.isBlank() -> cloud
        normalizedCloud.isBlank() -> local
        normalizedCloud.length > normalizedLocal.length -> cloud
        else -> local
    }
}

private fun preferNonBlank(local: String, cloud: String): String = if (local.isNotBlank()) local else cloud

private fun mergeDistinct(local: List<String>, cloud: List<String>): List<String> = (local + cloud)
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .distinct()

private fun maxNullable(local: Long?, cloud: Long?): Long? = when {
    local == null -> cloud
    cloud == null -> local
    else -> maxOf(local, cloud)
}

private fun LocalProgressSnapshot.hasMeaningfulProgress(): Boolean {
    if (stats.xp > 0L || stats.streak > 0 || userStatsEvents.isNotEmpty()) return true
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

private fun LocalProgressSnapshot.toCloudProgress(updatedAt: Long = System.currentTimeMillis()): CloudProgress {
    val syncStatesByCardId = flashcardSyncStates.associateBy(FlashcardSyncStateEntity::cardId)
    val projectedStats = projectUserStats(userStatsEvents.map(UserStatsSyncEventEntity::toCloudModel), stats)
    val cloudUserStatsEvents = if (userStatsEvents.isNotEmpty()) {
        userStatsEvents.map(UserStatsSyncEventEntity::toCloudModel)
    } else {
        legacyBootstrapUserStatsEvents(stats)
    }
    val resetMetadata = resetMetadata ?: inferLegacyResetMetadata()
    return CloudProgress(
        updatedAt = updatedAt,
        resetAt = resetMetadata.resetAt,
        resetGeneration = resetMetadata.resetGeneration,
        xp = projectedStats.xp,
        level = projectedStats.level,
        streak = projectedStats.streak,
        lastLoginDate = projectedStats.lastLoginDate,
        userStatsEvents = cloudUserStatsEvents,
        flashcards = cards.map { it.toCloudModel(syncStatesByCardId[it.id]) },
        deletedFlashcards = flashcardSyncStates.mapNotNull { state ->
            state.deletedAt?.let { deletedAt -> CloudDeletedFlashcard(cardId = state.cardId, deletedAt = deletedAt) }
        },
        reviewQuestionProgress = reviewQuestionProgress.map(ReviewQuestionProgress::toCloudModel),
        dailyReviewStats = dailyReviewStats.map(DailyReviewStat::toCloudModel),
        reviewAnswerEvents = reviewAnswerEvents.map(ReviewAnswerSyncEventEntity::toCloudModel)
    )
}

private fun LocalProgressSnapshot.inferLegacyResetMetadata(): SyncResetMetadataEntity {
    val latestStatsResetAt = userStatsEvents
        .filter { it.eventType == UserStatsEventType.STATS_RESET.name }
        .maxOfOrNull(UserStatsSyncEventEntity::occurredAt)
        ?: 0L
    val latestCardResetAt = flashcardSyncStates.maxOfOrNull { it.deletedAt ?: 0L } ?: 0L
    val resetAt = when {
        cards.isNotEmpty() -> latestStatsResetAt
        latestStatsResetAt <= 0L && latestCardResetAt <= 0L -> 0L
        else -> maxOf(latestStatsResetAt, latestCardResetAt)
    }

    return SyncResetMetadataEntity(
        resetAt = resetAt,
        resetGeneration = if (resetAt > 0L) 1L else 0L
    )
}

private fun CloudProgress.normalized(): CloudProgress = copy(
    updatedAt = 0L,
    resetAt = resetAt,
    resetGeneration = resetGeneration,
    userStatsEvents = userStatsEvents.sortedBy { it.eventId },
    flashcards = flashcards.sortedBy { it.id },
    deletedFlashcards = deletedFlashcards.sortedBy { it.cardId },
    reviewQuestionProgress = reviewQuestionProgress.sortedBy { it.questionId },
    dailyReviewStats = dailyReviewStats.sortedBy { it.dateKey },
    reviewAnswerEvents = reviewAnswerEvents.sortedBy { it.eventId }
)

private fun Flashcard.toCloudModel(syncState: FlashcardSyncStateEntity?): CloudFlashcard = CloudFlashcard(
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
    lastModifiedAt = syncState?.lastModifiedAt ?: dateAjout,
    favoriteUpdatedAt = syncState?.favoriteUpdatedAt ?: if (favori) dateAjout else 0L,
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

private fun CloudReviewQuestionProgress.toDomain(): ReviewQuestionProgress = ReviewQuestionProgress(
    questionId = questionId,
    cardId = cardId,
    questionType = ReviewQuestionType.fromStorage(questionType),
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
    pendingReplacementChallengeKind = pendingReplacementChallengeKind?.let(ReviewSessionChallengeKind::valueOf)
)

private fun DailyReviewStat.toCloudModel(): CloudDailyReviewStat = CloudDailyReviewStat(
    dateKey = dateKey,
    cardsReviewed = cardsReviewed,
    correctAnswers = correctAnswers,
    totalTimeSeconds = totalTimeSeconds
)

private fun ReviewAnswerSyncEventEntity.toCloudModel(): CloudReviewAnswerSyncEvent = CloudReviewAnswerSyncEvent(
    eventId = eventId,
    sessionId = sessionId,
    questionId = questionId,
    cardId = cardId,
    questionType = questionType,
    answer = answer,
    answeredAt = answeredAt,
    challengeKind = challengeKind
)

private fun UserStatsSyncEventEntity.toCloudModel(): CloudUserStatsEvent = CloudUserStatsEvent(
    eventId = eventId,
    eventType = eventType,
    occurredAt = occurredAt,
    xpDelta = xpDelta,
    levelValue = levelValue,
    streakValue = streakValue,
    lastLoginDateValue = lastLoginDateValue
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

private fun CloudFlashcard.toSyncStateEntity(): FlashcardSyncStateEntity = FlashcardSyncStateEntity(
    cardId = id,
    lastModifiedAt = if (lastModifiedAt > 0L) lastModifiedAt else dateAjout,
    favoriteUpdatedAt = if (favoriteUpdatedAt > 0L) favoriteUpdatedAt else if (favori) dateAjout else 0L,
    deletedAt = null
)

private fun CloudDeletedFlashcard.toSyncStateEntity(): FlashcardSyncStateEntity = FlashcardSyncStateEntity(
    cardId = cardId,
    lastModifiedAt = 0L,
    favoriteUpdatedAt = 0L,
    deletedAt = deletedAt
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

private fun CloudReviewAnswerSyncEvent.toEntity(): ReviewAnswerSyncEventEntity = ReviewAnswerSyncEventEntity(
    eventId = eventId,
    sessionId = sessionId,
    questionId = questionId,
    cardId = cardId,
    questionType = questionType,
    answer = answer,
    answeredAt = answeredAt,
    challengeKind = challengeKind
)

private fun CloudUserStatsEvent.toEntity(): UserStatsSyncEventEntity = UserStatsSyncEventEntity(
    eventId = eventId,
    eventType = eventType,
    occurredAt = occurredAt,
    xpDelta = xpDelta,
    levelValue = levelValue,
    streakValue = streakValue,
    lastLoginDateValue = lastLoginDateValue
)

private fun projectUserStats(events: List<CloudUserStatsEvent>, fallback: UserStatsEntity): UserStatsEntity {
    if (events.isEmpty()) return fallback.copy(level = fallback.level.coerceAtLeast(1))

    return events.sortedWith(compareBy<CloudUserStatsEvent> { it.occurredAt }.thenBy { it.eventId })
        .fold(UserStatsEntity()) { current, event ->
            when (UserStatsEventType.valueOf(event.eventType)) {
                UserStatsEventType.XP_AWARDED -> {
                    val xp = (current.xp + (event.xpDelta ?: 0L)).coerceAtLeast(0L)
                    current.copy(xp = xp, level = XPCalculator.calculateLevel(xp))
                }
                UserStatsEventType.LEVEL_SET -> {
                    val level = (event.levelValue ?: 1).coerceAtLeast(1)
                    current.copy(level = level, xp = XPCalculator.calculateXpForLevel(level))
                }
                UserStatsEventType.STREAK_UPDATED,
                UserStatsEventType.STREAK_SIMULATED -> current.copy(
                    streak = (event.streakValue ?: current.streak).coerceAtLeast(0),
                    lastLoginDate = event.lastLoginDateValue ?: current.lastLoginDate
                )
                UserStatsEventType.STATS_RESET -> UserStatsEntity()
            }
        }
        .copy(userId = fallback.userId)
}

private fun legacyBootstrapUserStatsEvents(stats: UserStatsEntity): List<CloudUserStatsEvent> {
    if (stats.xp <= 0L && stats.streak <= 0 && stats.lastLoginDate <= 0L && stats.level <= 1) return emptyList()

    return listOf(
        CloudUserStatsEvent(
            eventId = "legacy-user-stats-${stats.xp}-${stats.level}-${stats.streak}-${stats.lastLoginDate}",
            eventType = UserStatsEventType.LEVEL_SET.name,
            occurredAt = maxOf(stats.lastLoginDate, 1L),
            levelValue = stats.level.coerceAtLeast(1)
        ),
        CloudUserStatsEvent(
            eventId = "legacy-user-stats-xp-${stats.xp}-${stats.level}",
            eventType = UserStatsEventType.XP_AWARDED.name,
            occurredAt = maxOf(stats.lastLoginDate, 1L) + 1L,
            xpDelta = (stats.xp - XPCalculator.calculateXpForLevel(stats.level.coerceAtLeast(1))).coerceAtLeast(0L)
        ),
        CloudUserStatsEvent(
            eventId = "legacy-user-stats-streak-${stats.streak}-${stats.lastLoginDate}",
            eventType = UserStatsEventType.STREAK_SIMULATED.name,
            occurredAt = maxOf(stats.lastLoginDate, 1L) + 2L,
            streakValue = stats.streak,
            lastLoginDateValue = stats.lastLoginDate
        )
    )
}

private fun List<CloudUserStatsEvent>.legacyBootstrapUserStatsEvent(): CloudUserStatsEvent? = null

private fun Long.toIsoLocalDateKey(): String = java.time.Instant.ofEpochMilli(this)
    .atZone(java.time.ZoneId.systemDefault())
    .toLocalDate()
    .toString()

