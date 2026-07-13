package com.dgero.homly.calendar.domain.validation

import com.dgero.homly.calendar.domain.CalendarLimits
import com.dgero.homly.calendar.domain.error.CalendarError
import java.time.LocalTime

/**
 * Validates draft calendar event fields.
 *
 * Pure — no Room/repository dependency. The event-count limit (AC-07b,
 * [CalendarError.EventLimitReached]) is not this validator's job: it needs a
 * repository count and belongs to `CreateEventUseCase`.
 */
object CalendarEventValidator {

    fun validate(
        title: String,
        isAllDay: Boolean,
        startTime: LocalTime?,
        endTime: LocalTime?,
    ): CalendarError? {
        val trimmed = title.trim()
        return when {
            trimmed.isBlank() -> CalendarError.EmptyTitle
            trimmed.length > CalendarLimits.MAX_TITLE_LENGTH -> CalendarError.TitleTooLong
            !isAllDay && !isEndAfterStart(startTime, endTime) -> CalendarError.EndNotAfterStart
            else -> null
        }
    }

    /** A timed event must have both times set, with [endTime] strictly after [startTime]. */
    private fun isEndAfterStart(startTime: LocalTime?, endTime: LocalTime?): Boolean {
        if (startTime == null || endTime == null) return false
        return endTime.isAfter(startTime)
    }
}
