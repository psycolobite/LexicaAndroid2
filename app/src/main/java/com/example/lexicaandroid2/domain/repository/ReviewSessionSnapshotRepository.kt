package com.example.lexicaandroid2.domain.repository

import com.example.lexicaandroid2.domain.model.ReviewSessionSnapshot

interface ReviewSessionSnapshotRepository {
    suspend fun getActiveSession(): ReviewSessionSnapshot?
    suspend fun saveActiveSession(snapshot: ReviewSessionSnapshot)
    suspend fun clearActiveSession()
}

