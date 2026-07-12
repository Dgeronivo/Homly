package com.dgero.homly.calendar.domain.usecase

import com.dgero.homly.calendar.domain.CalendarLimits
import com.dgero.homly.calendar.domain.error.CalendarError
import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.fake.FakeCalendarEventRepository
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateEventUseCaseTest {

    private val userId = 1L
    private val day = LocalDate.of(2026, 7, 15)
    private lateinit var repo: FakeCalendarEventRepository
    private lateinit var useCase: UpdateEventUseCase

    @Before
    fun setUp() {
        repo = FakeCalendarEventRepository()
        useCase = UpdateEventUseCase(repo)
    }

    @Test
    fun `invoke_titleChangeHappyPath_updatesEvent`() = runTest {
        val original = seedOriginal()

        val result = useCase(original.copy(title = "Dentist"))

        assertTrue(result.isSuccess)
        assertEquals(1, repo.updateCallCount)
        val updated = repo.getEventsForMonth(userId, java.time.YearMonth.from(day)).single()
        assertEquals("Dentist", updated.title)
    }

    @Test
    fun `invoke_dateChangeHappyPath_updatesEvent`() = runTest {
        val original = seedOriginal()
        val newDate = day.plusDays(3)

        val result = useCase(original.copy(date = newDate))

        assertTrue(result.isSuccess)
        val updated = repo.getEventsForMonth(userId, java.time.YearMonth.from(newDate)).single()
        assertEquals(newDate, updated.date)
    }

    @Test
    fun `invoke_timeChangeHappyPath_updatesEvent`() = runTest {
        val original = seedOriginal()
        val newStart = LocalTime.of(11, 0)
        val newEnd = LocalTime.of(12, 0)

        val result = useCase(original.copy(startTime = newStart, endTime = newEnd))

        assertTrue(result.isSuccess)
        val updated = repo.getEventsForMonth(userId, java.time.YearMonth.from(day)).single()
        assertEquals(newStart, updated.startTime)
        assertEquals(newEnd, updated.endTime)
    }

    @Test
    fun `invoke_emptyTitle_failsWithoutWritingToRepository`() = runTest {
        val original = seedOriginal()

        val result = useCase(original.copy(title = "   "))

        assertEquals(CalendarError.EmptyTitle, result.exceptionOrNull())
        assertEquals(0, repo.updateCallCount)
        assertEquals("Doctor", repo.getEventsForMonth(userId, java.time.YearMonth.from(day)).single().title)
    }

    @Test
    fun `invoke_titleTooLong_failsWithoutWritingToRepository`() = runTest {
        val original = seedOriginal()

        val result = useCase(original.copy(title = "x".repeat(101)))

        assertEquals(CalendarError.TitleTooLong, result.exceptionOrNull())
        assertEquals(0, repo.updateCallCount)
    }

    @Test
    fun `invoke_endNotAfterStart_failsWithoutWritingToRepository`() = runTest {
        val original = seedOriginal()

        val result = useCase(original.copy(startTime = LocalTime.of(10, 0), endTime = LocalTime.of(10, 0)))

        assertEquals(CalendarError.EndNotAfterStart, result.exceptionOrNull())
        assertEquals(0, repo.updateCallCount)
    }

    @Test
    fun `invoke_atOrOverEventLimit_stillSucceeds_noLimitCheckPerformed`() = runTest {
        seedEvents(count = CalendarLimits.MAX_EVENTS)
        val original = seedOriginal(id = (CalendarLimits.MAX_EVENTS + 1).toLong())

        val result = useCase(original.copy(title = "Still editable"))

        assertTrue(result.isSuccess)
        assertEquals(1, repo.updateCallCount)
    }

    private fun seedOriginal(id: Long = 1L): CalendarEvent {
        val event = CalendarEvent(
            id = id,
            userId = userId,
            title = "Doctor",
            date = day,
            isAllDay = false,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
        )
        repo.seedEvent(event)
        return event
    }

    private fun seedEvents(count: Int) {
        repeat(count) { i ->
            repo.seedEvent(
                CalendarEvent(
                    id = (i + 1).toLong(),
                    userId = userId,
                    title = "Event $i",
                    date = day,
                    isAllDay = true,
                    startTime = null,
                    endTime = null,
                ),
            )
        }
    }
}
