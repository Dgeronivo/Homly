package com.dgero.homly.calendar.domain.repository

import com.dgero.homly.calendar.domain.model.CalendarEvent
import java.time.YearMonth

interface CalendarEventRepository {

    /** Events for [userId] within [yearMonth], unsorted (sorting is a use-case/UI concern). */
    suspend fun getEventsForMonth(userId: Long, yearMonth: YearMonth): List<CalendarEvent>

    suspend fun getEventCount(userId: Long): Int

    /** Single event lookup, scoped to [userId]. Returns `null` if not found or owned by another user. */
    suspend fun getById(id: Long, userId: Long): CalendarEvent?

    /** [event].id is ignored on input; the returned event carries the generated id. */
    suspend fun create(event: CalendarEvent): Result<CalendarEvent>

    /** Fails if no event with [CalendarEvent.id] exists, or it is owned by a different user. */
    suspend fun update(event: CalendarEvent): Result<Unit>

    suspend fun delete(id: Long, userId: Long): Result<Unit>
}
