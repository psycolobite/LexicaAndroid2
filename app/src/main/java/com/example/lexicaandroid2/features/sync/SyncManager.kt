package com.example.lexicaandroid2.features.sync

import android.util.Log
import com.example.lexicaandroid2.domain.repository.FlashcardRepository
import com.example.lexicaandroid2.features.gamification.data.UserStatsDao
import com.example.lexicaandroid2.features.gamification.data.UserStatsEntity
import com.example.lexicaandroid2.features.gamification.domain.UserStatsRepository
import com.example.lexicaandroid2.features.gamification.domain.XPCalculator
import kotlinx.coroutines.flow.firstOrNull

/**
 * Résultat de la vérification de synchronisation au moment du login.
 */
sealed class SyncCheckResult {
    /** Aucun document cloud → premier appareil. Upload silencieux. */
    object NoCloudData : SyncCheckResult()

    /** Cloud présent, mais progression locale vide. Import silencieux. */
    data class EmptyLocalImport(val cloudProgress: CloudProgress) : SyncCheckResult()

    /** XP identiques de chaque côté. Rien à faire. */
    object UpToDate : SyncCheckResult()

    /** Les deux côtés ont des données différentes. L'utilisateur doit choisir. */
    data class Conflict(
        val cloud: CloudProgress,
        val localXp: Long,
        val localLevel: Int
    ) : SyncCheckResult()

    /** Erreur réseau — continuer en local. */
    data class NetworkError(val message: String) : SyncCheckResult()
}

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
    private val userStatsDao: UserStatsDao,
    private val userStatsRepository: UserStatsRepository,
    private val flashcardRepository: FlashcardRepository
) {

    /**
     * Vérifie l'état Firestore immédiatement après connexion.
     * L'appelant décide quoi faire selon le résultat.
     */
    suspend fun checkOnLogin(uid: String): SyncCheckResult {
        return try {
            val cloudProgress = firestoreSyncRepository.downloadProgress(uid)
            val localStats = userStatsRepository.getUserStats().firstOrNull()
            val localXp = localStats?.xp ?: 0L

            when {
                cloudProgress == null -> SyncCheckResult.NoCloudData
                localXp == 0L -> SyncCheckResult.EmptyLocalImport(cloudProgress)
                cloudProgress.xp == localXp -> SyncCheckResult.UpToDate
                else -> SyncCheckResult.Conflict(
                    cloud = cloudProgress,
                    localXp = localXp,
                    localLevel = localStats?.level ?: 1
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
            val localStats = userStatsRepository.getUserStats().firstOrNull() ?: return
            val favorites = try {
                flashcardRepository.getAllCards().filter { it.favori }.map { it.id }
            } catch (e: Exception) {
                Log.w(TAG, "Could not read favorites: ${e.message}")
                emptyList()
            }
            firestoreSyncRepository.uploadProgress(
                uid,
                CloudProgress(
                    xp = localStats.xp,
                    level = localStats.level,
                    streak = localStats.streak,
                    lastLoginDate = localStats.lastLoginDate,
                    favoriteCardIds = favorites
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "uploadLocalToCloud failed: ${e.message}", e)
        }
    }

    /**
     * Remplace la progression locale par celle du cloud.
     * Appelé quand l'utilisateur confirme dans [SyncConfirmDialog].
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun replaceLocalWithCloud(uid: String, cloudProgress: CloudProgress) {
        try {
            // 1. Écraser les stats utilisateur (XP, level, streak, lastLoginDate)
            val newLevel = XPCalculator.calculateLevel(cloudProgress.xp)
            userStatsDao.insertOrUpdate(
                UserStatsEntity(
                    userId = "currentUser",
                    xp = cloudProgress.xp,
                    level = newLevel,
                    streak = cloudProgress.streak,
                    lastLoginDate = cloudProgress.lastLoginDate
                )
            )

            // 2. Appliquer les favoris cloud sur les cartes locales
            val cloudFavoriteSet = cloudProgress.favoriteCardIds.toSet()
            val allCards = try { flashcardRepository.getAllCards() } catch (e: Exception) { emptyList() }
            allCards.forEach { card ->
                val shouldBeFavorite = card.id in cloudFavoriteSet
                if (card.favori != shouldBeFavorite) {
                    try {
                        flashcardRepository.setFavorite(card.id, shouldBeFavorite)
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not set favorite for card ${card.id}: ${e.message}")
                    }
                }
            }
            Log.d(TAG, "Local data replaced with cloud: xp=${cloudProgress.xp}")
        } catch (e: Exception) {
            Log.e(TAG, "replaceLocalWithCloud failed: ${e.message}", e)
            throw e
        }
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
            val localStats = userStatsRepository.getUserStats().firstOrNull() ?: return
            // Push XP + streak uniquement (pas les favoris pour alléger)
            val current = firestoreSyncRepository.downloadProgress(uid)
            firestoreSyncRepository.uploadProgress(
                uid,
                CloudProgress(
                    xp = localStats.xp,
                    level = localStats.level,
                    streak = localStats.streak,
                    lastLoginDate = localStats.lastLoginDate,
                    favoriteCardIds = current?.favoriteCardIds ?: emptyList()
                )
            )
        } catch (e: Exception) {
            Log.d(TAG, "Periodic push skipped: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SyncManager"
    }
}
