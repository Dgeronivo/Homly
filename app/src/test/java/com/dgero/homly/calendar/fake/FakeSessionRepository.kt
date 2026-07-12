package com.dgero.homly.calendar.fake

import com.dgero.homly.auth.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSessionRepository : SessionRepository {
    private val _currentUserId = MutableStateFlow<Long?>(null)
    override val currentUserId: Flow<Long?> = _currentUserId
    override suspend fun setSession(userId: Long) { _currentUserId.value = userId }
    override suspend fun clear() { _currentUserId.value = null }
}
