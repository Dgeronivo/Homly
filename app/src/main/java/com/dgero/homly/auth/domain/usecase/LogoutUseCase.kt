package com.dgero.homly.auth.domain.usecase

import com.dgero.homly.auth.domain.repository.SessionRepository

class LogoutUseCase(private val sessionRepository: SessionRepository) {
    suspend operator fun invoke() = sessionRepository.clear()
}
