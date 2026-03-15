package com.example.lexicaandroid2.features.sync

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Données de progression sauvegardées dans Firestore.
 * Document Firestore : users/{uid}
 */
data class CloudProgress(
    val xp: Long = 0L,
    val level: Int = 1,
    val streak: Int = 0,
    val lastLoginDate: Long = 0L,
    val favoriteCardIds: List<String> = emptyList()
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
        val data = mapOf(
            "xp" to progress.xp,
            "level" to progress.level,
            "streak" to progress.streak,
            "lastLoginDate" to progress.lastLoginDate,
            "favoriteCardIds" to progress.favoriteCardIds
        )
        db.collection(COLLECTION_USERS).document(uid).set(data).await()
        Log.d(TAG, "Upload OK: xp=${progress.xp} level=${progress.level}")
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
            val doc = db.collection(COLLECTION_USERS).document(uid).get().await()
            if (!doc.exists()) {
                Log.d(TAG, "No cloud document for uid=$uid")
                return null
            }
            @Suppress("UNCHECKED_CAST")
            CloudProgress(
                xp = doc.getLong("xp") ?: 0L,
                level = (doc.getLong("level") ?: 1L).toInt(),
                streak = (doc.getLong("streak") ?: 0L).toInt(),
                lastLoginDate = doc.getLong("lastLoginDate") ?: 0L,
                favoriteCardIds = (doc.get("favoriteCardIds") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
            ).also { Log.d(TAG, "Download OK: xp=${it.xp} level=${it.level}") }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed for uid=$uid: ${e.message}", e)
            null
        }
    }

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val TAG = "FirestoreSyncRepo"
    }
}
