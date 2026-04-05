package com.example.lexicaandroid2.domain.model

data class ReviewSessionSnapshot(
    val sessionId: String = ACTIVE_SESSION_ID,
    val createdAt: Long,
    val updatedAt: Long,
    val state: ReviewSessionSnapshotState,
    val undoState: ReviewSessionSnapshotState? = null,
    val plannedInsertions: List<String> = emptyList()
) {
    companion object {
        const val ACTIVE_SESSION_ID: String = "active_review_session"
    }
}

