package com.dgero.homly.auth.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val currentUserId: Flow<Long?>
    suspend fun setSession(userId: Long)
    suspend fun clear()
}
