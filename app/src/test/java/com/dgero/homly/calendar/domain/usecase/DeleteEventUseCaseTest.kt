package com.dgero.homly.calendar.domain.usecase

import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.fake.FakeCalendarEventRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteEventUseCaseTest {

    private val userId = 1L
    private val day = LocalDate.of(2026, 7, 15)
    private lateinit var repo: FakeCalendarEventRepository
    private lateinit var useCase: DeleteEventUseCase

    @Before
    fun setUp() {
        repo = FakeCalendarEventRepository()
        useCase = DeleteEventUseCase(repo)
    }

    @Test
    fun `invoke_existingEvent_removesItFromSubsequentMonthQuery`() = runTest {
        val created = repo.create(
            CalendarEvent(
                id = 0L,
                userId = userId,
                title = "Doctor",
                date = day,
                isAllDay = false,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(10, 0),
            ),
        ).getOrThrow()

        val result = useCase(created.id, userId)

        assertTrue(result.isSuccess)
        assertTrue(repo.getEventsForMonth(userId, YearMonth.from(day)).isEmpty())
    }

    @Test
    fun `invoke_wrongUser_fails_andEventStillPresentForOwner`() = runTest {
        val created = repo.create(
            CalendarEvent(
                id = 0L,
                userId = userId,
                title = "Doctor",
                date = day,
                isAllDay = false,
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(10, 0),
            ),
        ).getOrThrow()

        val result = useCase(created.id, userId = 999L)

        assertTrue(result.isFailure)
        assertTrue(repo.getEventsForMonth(userId, YearMonth.from(day)).isNotEmpty())
    }
}
