package com.example.lexicaandroid2.data.repository

import com.example.lexicaandroid2.data.local.ReviewSessionSnapshotDao
import com.example.lexicaandroid2.data.local.ReviewSessionSnapshotEntity
import com.example.lexicaandroid2.domain.model.ReviewSessionSnapshot
import com.example.lexicaandroid2.domain.repository.ReviewSessionSnapshotRepository
import com.google.gson.Gson

class ReviewSessionSnapshotRepositoryImpl(
    private val dao: ReviewSessionSnapshotDao,
    private val gson: Gson = Gson()
) : ReviewSessionSnapshotRepository {
    override suspend fun getActiveSession(): ReviewSessionSnapshot? {
        val entity = dao.getById(ReviewSessionSnapshot.ACTIVE_SESSION_ID) ?: return null
        return gson.fromJson(entity.payloadJson, ReviewSessionSnapshot::class.java)
    }

    override suspend fun saveActiveSession(snapshot: ReviewSessionSnapshot) {
        dao.insertOrReplace(
            ReviewSessionSnapshotEntity(
                sessionId = snapshot.sessionId,
                createdAt = snapshot.createdAt,
                updatedAt = snapshot.updatedAt,
                payloadJson = gson.toJson(snapshot)
            )
        )
    }

    override suspend fun clearActiveSession() {
        dao.deleteById(ReviewSessionSnapshot.ACTIVE_SESSION_ID)
    }
}

