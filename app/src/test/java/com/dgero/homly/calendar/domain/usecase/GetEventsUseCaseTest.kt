package com.dgero.homly.calendar.domain.usecase

import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.fake.FakeCalendarEventRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetEventsUseCaseTest {

    private val userId = 1L
    private val repo = FakeCalendarEventRepository()
    private val useCase = GetEventsUseCase(repo)

    @Test
    fun `invoke_returnsRepositoryEventsForRequestedMonth`() = runTest {
        val day = LocalDate.of(2026, 7, 15)
        repo.seedEvent(CalendarEvent(1, userId, "In month", day, true, null, null))
        repo.seedEvent(CalendarEvent(2, userId, "Other month", LocalDate.of(2026, 8, 1), true, null, null))

        val result = useCase(userId, YearMonth.of(2026, 7))

        assertEquals(listOf("In month"), result.map { it.title })
    }

    // Seeded in deliberately shuffled order to prevent an accidental pass — verifies:
    //   • all-day events come before timed events (AC-01, QG-3)
    //   • within timed events: ascending startTime (AC-01, QG-3)
    @Test
    fun `forDay_mixedAllDayAndTimedEvents_sortsAllDayFirstThenByStartTimeAscending`() {
        val day = LocalDate.of(2026, 7, 15)
        val events = listOf(
            CalendarEvent(1, userId, "Timed-15:00", day, false, LocalTime.of(15, 0), LocalTime.of(15, 30)),
            CalendarEvent(2, userId, "AllDay-B", day, true, null, null),
            CalendarEvent(3, userId, "Timed-09:00", day, false, LocalTime.of(9, 0), LocalTime.of(9, 30)),
            CalendarEvent(4, userId, "AllDay-A", day, true, null, null),
            CalendarEvent(5, userId, "Timed-12:00", day, false, LocalTime.of(12, 0), LocalTime.of(12, 30)),
        )

        val result = GetEventsUseCase.forDay(events, day)

        val expected = listOf("AllDay-B", "AllDay-A", "Timed-09:00", "Timed-12:00", "Timed-15:00")
        assertEquals(expected, result.map { it.title })
    }

    @Test
    fun `forDay_dayWithNoEvents_returnsEmptyList`() {
        val events = listOf(
            CalendarEvent(1, userId, "Elsewhere", LocalDate.of(2026, 7, 10), true, null, null),
        )

        val result = GetEventsUseCase.forDay(events, LocalDate.of(2026, 7, 11))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `forDay_emptyMonthList_returnsEmptyList`() {
        val result = GetEventsUseCase.forDay(emptyList(), LocalDate.of(2026, 7, 11))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `forDay_excludesEventsFromOtherDaysInSameMonth`() {
        val day = LocalDate.of(2026, 7, 15)
        val events = listOf(
            CalendarEvent(1, userId, "On day", day, true, null, null),
            CalendarEvent(2, userId, "Other day", LocalDate.of(2026, 7, 16), true, null, null),
        )

        val result = GetEventsUseCase.forDay(events, day)

        assertEquals(listOf("On day"), result.map { it.title })
    }
}
