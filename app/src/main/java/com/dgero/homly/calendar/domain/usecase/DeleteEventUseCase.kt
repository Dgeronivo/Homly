package com.dgero.homly.calendar.domain.usecase

import com.dgero.homly.calendar.domain.repository.CalendarEventRepository

/**
 * Deletes the calendar event identified by [id] for [userId].
 *
 * Pure passthrough to [CalendarEventRepository.delete] — no validator is involved since there's
 * no event payload to validate, only an id/owner pair.
 */
class DeleteEventUseCase(private val repository: CalendarEventRepository) {

    suspend operator fun invoke(id: Long, userId: Long): Result<Unit> = repository.delete(id, userId)
}
