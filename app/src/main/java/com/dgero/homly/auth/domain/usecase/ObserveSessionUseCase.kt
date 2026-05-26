package com.dgero.homly.auth.domain.usecase

import com.dgero.homly.auth.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

class ObserveSessionUseCase(private val sessionRepository: SessionRepository) {
    operator fun invoke(): Flow<Long?> = sessionRepository.currentUserId
}
