package com.dgero.homly.calendar.domain.usecase

import com.dgero.homly.calendar.domain.CalendarLimits
import com.dgero.homly.calendar.domain.error.CalendarError
import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.domain.repository.CalendarEventRepository
import com.dgero.homly.calendar.domain.validation.CalendarEventValidator

/**
 * Creates a calendar event for [userId] from a [draft].
 *
 * [draft] is a [CalendarEvent] whose `id`/`userId` are not-yet-meaningful (mirrors
 * [CalendarEventRepository.create]'s contract that the incoming `id` is ignored); this use case
 * stamps the real [userId] onto it before persisting. Validation (T02) always runs first and, on
 * failure, no repository call is made at all — nor is one made when the per-user event limit
 * ([CalendarLimits.MAX_EVENTS]) is already reached (AC-07b).
 */
class CreateEventUseCase(private val repository: CalendarEventRepository) {

    suspend operator fun invoke(userId: Long, draft: CalendarEvent): Result<CalendarEvent> {
        val validationError = CalendarEventValidator.validate(
            title = draft.title,
            isAllDay = draft.isAllDay,
            startTime = draft.startTime,
            endTime = draft.endTime,
        )
        if (validationError != null) return Result.failure(validationError)

        if (isAtLimit(userId)) return Result.failure(CalendarError.EventLimitReached)

        return repository.create(draft.copy(userId = userId))
    }

    /**
     * Standalone limit check used before even opening the add-event form (SAD Flow 4), so the
     * caller can pre-empt the FAB press when the user is already at [CalendarLimits.MAX_EVENTS].
     */
    suspend fun checkCanCreate(userId: Long): Result<Unit> =
        if (isAtLimit(userId)) Result.failure(CalendarError.EventLimitReached) else Result.success(Unit)

    private suspend fun isAtLimit(userId: Long): Boolean =
        repository.getEventCount(userId) >= CalendarLimits.MAX_EVENTS
}
