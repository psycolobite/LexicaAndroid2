package com.example.lexicaandroid2.features.sync

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lexicaandroid2.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * État global de la synchronisation Firestore.
 */
sealed class SyncUiState {
    /** Aucune synchro en cours / terminée normalement. */
    object Idle : SyncUiState()

    /** Vérification Firestore en cours ou upload en arrière-plan. */
    object Loading : SyncUiState()

    /**
     * Conflit détecté : l'utilisateur doit choisir entre progression cloud et locale.
     * Ce state affiche [SyncConfirmDialog] dans LexicaApp.
     */
    data class PendingConflict(
        val uid: String,
        val kind: SyncConflictKind,
        val cloud: CloudProgress?,
        val local: LocalProgressSummary
    ) : SyncUiState()

    /** Message non-bloquant (succès ou erreur légère). */
    data class Message(val text: String) : SyncUiState()
}

enum class SyncConflictKind {
    EMPTY_CLOUD_ACCOUNT,
    CLOUD_VS_LOCAL
}

/**
 * ViewModel de synchronisation Firestore.
 *
 * Cycle de vie :
 * - Créé dans MainActivity, passé en paramètre à LexicaApp.
 * - Observe automatiquement [AuthRepository.currentUser] et déclenche la vérification cloud
 *   dès qu'un uid non-null apparaît.
 * - Expose [uiState] pour que LexicaApp affiche [SyncConfirmDialog] si besoin.
 *
 * Flux de déconnexion :
 * - Appeler [signOutWithSync] depuis ProfileScreen au lieu de authRepository.signOut() direct.
 */
class SyncViewModel(
    private val syncManager: SyncManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    private var periodicSyncJob: Job? = null
    private var lastObservedUid: String? = null
    private var initialAuthStateHandled = false

    init {
        // Observe les changements d'utilisateur connecté
        viewModelScope.launch {
            authRepository.currentUser
                .map { it?.uid }
                .distinctUntilChanged()
                .collect { uid ->
                    if (!initialAuthStateHandled) {
                        initialAuthStateHandled = true
                        if (uid != null) {
                            lastObservedUid = uid
                            syncExistingAuthenticatedSession(uid)
                            startPeriodicSync(uid)
                        } else {
                            _uiState.update { SyncUiState.Idle }
                        }
                    } else if (uid != null && uid != lastObservedUid) {
                        lastObservedUid = uid
                        checkAndSync(uid)
                        startPeriodicSync(uid)
                    } else if (uid == null) {
                        lastObservedUid = null
                        stopPeriodicSync()
                        _uiState.update { SyncUiState.Idle }
                    }
                }
        }
    }

    private fun syncExistingAuthenticatedSession(uid: String) {
        viewModelScope.launch {
            when (val result = syncManager.checkOnLogin(uid)) {
                is SyncCheckResult.EmptyCloudAccount -> {
                    if (result.requiresChoice) {
                        _uiState.update {
                            SyncUiState.PendingConflict(
                                uid = uid,
                                kind = SyncConflictKind.EMPTY_CLOUD_ACCOUNT,
                                cloud = null,
                                local = result.local
                            )
                        }
                    } else {
                        syncManager.uploadLocalToCloud(uid)
                        _uiState.update { SyncUiState.Idle }
                    }
                }
                is SyncCheckResult.EmptyLocalImport -> {
                    try {
                        syncManager.silentImportFromCloud(uid, result.cloudProgress)
                    } finally {
                        _uiState.update { SyncUiState.Idle }
                    }
                }
                is SyncCheckResult.UpToDate -> {
                    _uiState.update { SyncUiState.Idle }
                }
                is SyncCheckResult.Conflict -> {
                    _uiState.update {
                        SyncUiState.PendingConflict(
                            uid = uid,
                            kind = SyncConflictKind.CLOUD_VS_LOCAL,
                            cloud = result.cloud,
                            local = result.local
                        )
                    }
                }
                is SyncCheckResult.NetworkError -> {
                    _uiState.update { SyncUiState.Idle }
                }
            }
        }
    }

    /**
     * Vérifie Firestore après login et met à jour l'état.
     * Appelé automatiquement mais peut aussi être appelé manuellement.
     */
    fun checkAndSync(uid: String) {
        viewModelScope.launch {
            _uiState.update { SyncUiState.Loading }
            when (val result = syncManager.checkOnLogin(uid)) {
                is SyncCheckResult.EmptyCloudAccount -> {
                    if (result.requiresChoice) {
                        _uiState.update {
                            SyncUiState.PendingConflict(
                                uid = uid,
                                kind = SyncConflictKind.EMPTY_CLOUD_ACCOUNT,
                                cloud = null,
                                local = result.local
                            )
                        }
                    } else {
                        syncManager.uploadLocalToCloud(uid)
                        _uiState.update { SyncUiState.Idle }
                        Log.d(TAG, "Empty cloud account — uploaded current local baseline")
                    }
                }
                is SyncCheckResult.EmptyLocalImport -> {
                    // Import silencieux : progression locale vide
                    try {
                        syncManager.silentImportFromCloud(uid, result.cloudProgress)
                        _uiState.update {
                            SyncUiState.Message("✅ Progression du compte récupérée depuis le cloud")
                        }
                    } catch (e: Exception) {
                        _uiState.update { SyncUiState.Message("⚠️ Import cloud partiel : ${e.message}") }
                    }
                }
                is SyncCheckResult.UpToDate -> {
                    _uiState.update { SyncUiState.Idle }
                    Log.d(TAG, "Cloud and local are in sync")
                }
                is SyncCheckResult.Conflict -> {
                    // Déléguer le choix à l'utilisateur via le dialog
                    _uiState.update {
                        SyncUiState.PendingConflict(
                            uid = uid,
                            kind = SyncConflictKind.CLOUD_VS_LOCAL,
                            cloud = result.cloud,
                            local = result.local
                        )
                    }
                }
                is SyncCheckResult.NetworkError -> {
                    // Continuer en mode local — pas de blocage UI
                    _uiState.update { SyncUiState.Idle }
                    Log.w(TAG, "Sync skipped (network): ${result.message}")
                }
            }
        }
    }

    /**
     * L'utilisateur a confirmé le remplacement de sa progression locale par le cloud.
     * Appelé depuis [SyncConfirmDialog] bouton "Remplacer".
     */
    fun confirmReplaceWithCloud(uid: String, cloudProgress: CloudProgress) {
        viewModelScope.launch {
            _uiState.update { SyncUiState.Loading }
            try {
                syncManager.replaceLocalWithCloud(uid, cloudProgress)
                _uiState.update { SyncUiState.Message("✅ Progression cloud appliquée (Niveau ${cloudProgress.level})") }
            } catch (e: Exception) {
                _uiState.update { SyncUiState.Message("❌ Erreur lors de l'import : ${e.message}") }
            }
        }
    }

    fun startFreshOnEmptyCloudAccount(uid: String) {
        viewModelScope.launch {
            _uiState.update { SyncUiState.Loading }
            try {
                syncManager.resetLocalAndUploadEmpty(uid)
                _uiState.update { SyncUiState.Message("✅ Compte initialisé à zéro") }
            } catch (e: Exception) {
                _uiState.update { SyncUiState.Message("❌ Impossible d'initialiser ce compte : ${e.message}") }
            }
        }
    }

    /**
     * L'utilisateur choisit de conserver sa progression locale.
     * Upload local → cloud et ferme le dialog.
     */
    fun keepLocal(uid: String) {
        viewModelScope.launch {
            _uiState.update { SyncUiState.Loading }
            syncManager.uploadLocalToCloud(uid)
            _uiState.update { SyncUiState.Message("✅ Progression locale envoyée vers le compte") }
            Log.d(TAG, "User kept local progress — uploaded to cloud")
        }
    }

    /**
     * Ferme le message informatif (toast différé).
     */
    fun dismissMessage() {
        _uiState.update { SyncUiState.Idle }
    }

    /**
     * Déconnexion avec sauvegarde préalable dans Firestore.
     * À appeler depuis ProfileScreen à la place de authRepository.signOut() direct.
     */
    fun signOutWithSync(uid: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _uiState.update { SyncUiState.Loading }
                syncManager.saveBeforeLogout(uid)
                authRepository.signOut()
                _uiState.update { SyncUiState.Idle }
                onComplete()
            } catch (e: Exception) {
                // Déconnecter quand même même si l'upload a échoué
                Log.w(TAG, "Save before logout failed: ${e.message}")
                try { authRepository.signOut() } catch (_: Exception) {}
                _uiState.update { SyncUiState.Idle }
                onComplete()
            }
        }
    }

    // ── Sync périodique ─────────────────────────────────────────────────────

    private fun startPeriodicSync(uid: String) {
        stopPeriodicSync()
        periodicSyncJob = viewModelScope.launch {
            while (true) {
                delay(PERIODIC_SYNC_INTERVAL_MS)
                syncManager.periodicPush(uid)
                Log.d(TAG, "Periodic push done for uid=$uid")
            }
        }
    }

    private fun stopPeriodicSync() {
        periodicSyncJob?.cancel()
        periodicSyncJob = null
    }

    companion object {
        private const val TAG = "SyncViewModel"
        private const val PERIODIC_SYNC_INTERVAL_MS = 30 * 60 * 1000L // 30 minutes
    }
}

class SyncViewModelFactory(
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncViewModel::class.java)) {
            return SyncViewModel(syncManager, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
