package com.dgero.homly.calendar.domain.usecase

import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.domain.repository.CalendarEventRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

/**
 * Fetches a user's calendar events for a given month.
 *
 * Callers that need a single day's events (e.g. the calendar ViewModel deriving the selected
 * day's list) should filter the returned month list with [forDay], which also applies the
 * required display order.
 */
class GetEventsUseCase(private val repository: CalendarEventRepository) {

    suspend operator fun invoke(userId: Long, yearMonth: YearMonth): List<CalendarEvent> =
        repository.getEventsForMonth(userId, yearMonth)

    companion object {

        /**
         * Filters [events] down to [day] and orders them all-day events first, then timed
         * events ascending by [CalendarEvent.startTime] (AC-01, QG-3). Returns an empty list
         * when no events fall on [day].
         */
        fun forDay(events: List<CalendarEvent>, day: LocalDate): List<CalendarEvent> =
            events.filter { it.date == day }
                .sortedWith(compareBy({ !it.isAllDay }, { it.startTime ?: LocalTime.MIN }))
    }
}
