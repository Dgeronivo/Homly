package com.dgero.homly.calendar.domain.usecase

import com.dgero.homly.calendar.domain.usecase.port.GetTodayEventsCountUseCase
import java.time.LocalDate
import java.time.YearMonth

class GetTodayEventsCountUseCaseImpl(
    private val getEvents: GetEventsUseCase,
) : GetTodayEventsCountUseCase {
    override suspend fun invoke(userId: Long): Int {
        val monthEvents = getEvents(userId, YearMonth.now())
        return GetEventsUseCase.forDay(monthEvents, LocalDate.now()).size
    }
}
