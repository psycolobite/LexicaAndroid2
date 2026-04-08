package com.example.lexicaandroid2.features.auth.data

import com.example.lexicaandroid2.features.auth.domain.model.AuthUser
import com.example.lexicaandroid2.features.auth.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        trySend(firebaseAuth.currentUser?.toAuthUser())
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> =
        runCatching {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.toAuthUser() ?: error("Aucun utilisateur retourné")
        }

    override suspend fun registerWithEmail(email: String, password: String): Result<AuthUser> =
        runCatching {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.toAuthUser() ?: error("Aucun utilisateur retourné")
        }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthUser> =
        runCatching {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user?.toAuthUser() ?: error("Aucun utilisateur retourné")
        }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser ?: error("Aucun compte connecté")
        user.delete().await()
    }

    override suspend fun signOut() = firebaseAuth.signOut()
}

private fun com.google.firebase.auth.FirebaseUser.toAuthUser() = AuthUser(
    uid = uid,
    email = email,
    displayName = displayName
)
