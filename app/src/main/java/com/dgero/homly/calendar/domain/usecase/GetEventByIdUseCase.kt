package com.dgero.homly.calendar.domain.usecase

import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.domain.repository.CalendarEventRepository

/**
 * Fetches a single calendar event by [id], scoped to [userId].
 *
 * Pure passthrough to [CalendarEventRepository.getById] — used by `AddEditEventViewModel` to
 * pre-fill the form when editing an existing event (AC-05). Returns `null` when the event
 * doesn't exist or isn't owned by [userId].
 */
class GetEventByIdUseCase(private val repository: CalendarEventRepository) {

    suspend operator fun invoke(id: Long, userId: Long): CalendarEvent? = repository.getById(id, userId)
}
