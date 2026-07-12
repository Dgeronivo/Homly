package com.dgero.homly.calendar.domain.usecase

import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.domain.repository.CalendarEventRepository
import com.dgero.homly.calendar.domain.validation.CalendarEventValidator

/**
 * Updates an existing calendar [event].
 *
 * [event] is a fully-formed [CalendarEvent] whose `id`/`userId` are already meaningful (set by
 * the caller). Validation (T02) always runs first and, on failure, no repository call is made at
 * all. Unlike [CreateEventUseCase], no per-user event-limit check is performed here: editing an
 * existing event doesn't add a row, so [com.dgero.homly.calendar.domain.CalendarLimits.MAX_EVENTS]
 * is never consulted.
 */
class UpdateEventUseCase(private val repository: CalendarEventRepository) {

    suspend operator fun invoke(event: CalendarEvent): Result<Unit> {
        val validationError = CalendarEventValidator.validate(
            title = event.title,
            isAllDay = event.isAllDay,
            startTime = event.startTime,
            endTime = event.endTime,
        )
        if (validationError != null) return Result.failure(validationError)

        return repository.update(event)
    }
}
