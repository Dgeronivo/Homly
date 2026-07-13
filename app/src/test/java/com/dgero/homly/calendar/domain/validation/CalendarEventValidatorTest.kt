package com.dgero.homly.calendar.domain.validation

import com.dgero.homly.calendar.domain.CalendarLimits
import com.dgero.homly.calendar.domain.error.CalendarError
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarEventValidatorTest {

    @Test
    fun validate_emptyTitle_returnsEmptyTitle() {
        assertEquals(
            CalendarError.EmptyTitle,
            CalendarEventValidator.validate(
                title = "",
                isAllDay = true,
                startTime = null,
                endTime = null,
            ),
        )
    }

    @Test
    fun validate_blankTitle_returnsEmptyTitle() {
        assertEquals(
            CalendarError.EmptyTitle,
            CalendarEventValidator.validate(
                title = "   ",
                isAllDay = true,
                startTime = null,
                endTime = null,
            ),
        )
    }

    @Test
    fun validate_titleTooLong_returnsTitleTooLong() {
        val title = "a".repeat(CalendarLimits.MAX_TITLE_LENGTH + 1)
        assertEquals(
            CalendarError.TitleTooLong,
            CalendarEventValidator.validate(
                title = title,
                isAllDay = true,
                startTime = null,
                endTime = null,
            ),
        )
    }

    @Test
    fun validate_titleAtMaxLength_returnsNull() {
        val title = "a".repeat(CalendarLimits.MAX_TITLE_LENGTH)
        assertNull(
            CalendarEventValidator.validate(
                title = title,
                isAllDay = true,
                startTime = null,
                endTime = null,
            ),
        )
    }

    @Test
    fun validate_timedEventEndEqualsStart_returnsEndNotAfterStart() {
        val time = LocalTime.of(10, 0)
        assertEquals(
            CalendarError.EndNotAfterStart,
            CalendarEventValidator.validate(
                title = "Meeting",
                isAllDay = false,
                startTime = time,
                endTime = time,
            ),
        )
    }

    @Test
    fun validate_timedEventEndBeforeStart_returnsEndNotAfterStart() {
        assertEquals(
            CalendarError.EndNotAfterStart,
            CalendarEventValidator.validate(
                title = "Meeting",
                isAllDay = false,
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(9, 0),
            ),
        )
    }

    @Test
    fun validate_timedEventMissingStartTime_returnsEndNotAfterStart() {
        assertEquals(
            CalendarError.EndNotAfterStart,
            CalendarEventValidator.validate(
                title = "Meeting",
                isAllDay = false,
                startTime = null,
                endTime = LocalTime.of(10, 0),
            ),
        )
    }

    @Test
    fun validate_timedEventMissingEndTime_returnsEndNotAfterStart() {
        assertEquals(
            CalendarError.EndNotAfterStart,
            CalendarEventValidator.validate(
                title = "Meeting",
                isAllDay = false,
                startTime = LocalTime.of(9, 0),
                endTime = null,
            ),
        )
    }

    @Test
    fun validate_timedEventMissingBothTimes_returnsEndNotAfterStart() {
        assertEquals(
            CalendarError.EndNotAfterStart,
            CalendarEventValidator.validate(
                title = "Meeting",
                isAllDay = false,
                startTime = null,
                endTime = null,
            ),
        )
    }

    @Test
    fun validate_allDayEventIgnoresTimes_returnsNull() {
        assertNull(
            CalendarEventValidator.validate(
                title = "Holiday",
                isAllDay = true,
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(9, 0),
            ),
        )
    }

    @Test
    fun validate_validTimedEvent_returnsNull() {
        assertNull(
            CalendarEventValidator.validate(
                title = "Meeting",
                isAllDay = false,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(10, 0),
            ),
        )
    }

    @Test
    fun validate_validAllDayEvent_returnsNull() {
        assertNull(
            CalendarEventValidator.validate(
                title = "Holiday",
                isAllDay = true,
                startTime = null,
                endTime = null,
            ),
        )
    }
}
