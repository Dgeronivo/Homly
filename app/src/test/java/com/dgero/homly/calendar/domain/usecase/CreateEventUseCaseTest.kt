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

class CreateEventUseCaseTest {

    private val userId = 1L
    private val day = LocalDate.of(2026, 7, 15)
    private lateinit var repo: FakeCalendarEventRepository
    private lateinit var useCase: CreateEventUseCase

    @Before
    fun setUp() {
        repo = FakeCalendarEventRepository()
        useCase = CreateEventUseCase(repo)
    }

    @Test
    fun `invoke_timedEventHappyPath_createsEventAndStampsUserId`() = runTest {
        val draft = CalendarEvent(
            id = 0L,
            userId = 0L,
            title = "Doctor",
            date = day,
            isAllDay = false,
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(10, 0),
        )

        val result = useCase(userId, draft)

        assertTrue(result.isSuccess)
        val created = result.getOrThrow()
        assertEquals(userId, created.userId)
        assertEquals("Doctor", created.title)
        assertEquals(1, repo.getEventCount(userId))
    }

    @Test
    fun `invoke_allDayEventHappyPath_createsEvent`() = runTest {
        val draft = CalendarEvent(
            id = 0L,
            userId = 0L,
            title = "Family trip",
            date = day,
            isAllDay = true,
            startTime = null,
            endTime = null,
        )

        val result = useCase(userId, draft)

        assertTrue(result.isSuccess)
        assertEquals(1, repo.getEventCount(userId))
    }

    @Test
    fun `invoke_emptyTitle_failsWithoutWritingToRepository`() = runTest {
        val draft = CalendarEvent(0L, 0L, "   ", day, false, LocalTime.of(9, 0), LocalTime.of(10, 0))

        val result = useCase(userId, draft)

        assertEquals(CalendarError.EmptyTitle, result.exceptionOrNull())
        assertEquals(0, repo.getEventCount(userId))
    }

    @Test
    fun `invoke_titleTooLong_failsWithoutWritingToRepository`() = runTest {
        val draft = CalendarEvent(0L, 0L, "x".repeat(101), day, false, LocalTime.of(9, 0), LocalTime.of(10, 0))

        val result = useCase(userId, draft)

        assertEquals(CalendarError.TitleTooLong, result.exceptionOrNull())
        assertEquals(0, repo.getEventCount(userId))
    }

    @Test
    fun `invoke_endNotAfterStart_failsWithoutWritingToRepository`() = runTest {
        val draft = CalendarEvent(0L, 0L, "Meeting", day, false, LocalTime.of(10, 0), LocalTime.of(10, 0))

        val result = useCase(userId, draft)

        assertEquals(CalendarError.EndNotAfterStart, result.exceptionOrNull())
        assertEquals(0, repo.getEventCount(userId))
    }

    @Test
    fun `invoke_timedEventMissingTimes_failsWithoutWritingToRepository`() = runTest {
        val draft = CalendarEvent(0L, 0L, "Meeting", day, false, null, null)

        val result = useCase(userId, draft)

        assertEquals(CalendarError.EndNotAfterStart, result.exceptionOrNull())
        assertEquals(0, repo.getEventCount(userId))
    }

    @Test
    fun `invoke_eventLimitReached_failsWithoutWritingToRepository`() = runTest {
        seedEvents(count = CalendarLimits.MAX_EVENTS)
        val draft = CalendarEvent(0L, 0L, "One too many", day, true, null, null)

        val result = useCase(userId, draft)

        assertEquals(CalendarError.EventLimitReached, result.exceptionOrNull())
        assertEquals(CalendarLimits.MAX_EVENTS, repo.getEventCount(userId))
    }

    @Test
    fun `checkCanCreate_underLimit_succeeds`() = runTest {
        seedEvents(count = CalendarLimits.MAX_EVENTS - 1)

        val result = useCase.checkCanCreate(userId)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `checkCanCreate_atLimit_fails`() = runTest {
        seedEvents(count = CalendarLimits.MAX_EVENTS)

        val result = useCase.checkCanCreate(userId)

        assertEquals(CalendarError.EventLimitReached, result.exceptionOrNull())
    }

    @Test
    fun `checkCanCreate_overLimit_fails`() = runTest {
        seedEvents(count = CalendarLimits.MAX_EVENTS + 5)

        val result = useCase.checkCanCreate(userId)

        assertEquals(CalendarError.EventLimitReached, result.exceptionOrNull())
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
