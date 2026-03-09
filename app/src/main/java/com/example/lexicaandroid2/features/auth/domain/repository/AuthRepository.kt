package com.example.lexicaandroid2.features.auth.domain.repository

import com.example.lexicaandroid2.features.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>

    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>

    suspend fun registerWithEmail(email: String, password: String): Result<AuthUser>

    suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthUser>

    suspend fun signOut()
}
