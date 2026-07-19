package com.dgero.homly.calendar.domain.usecase.port

interface GetTodayEventsCountUseCase {
    suspend operator fun invoke(userId: Long): Int
}
