package com.dgero.homly.calendar.domain.usecase

import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.fake.FakeCalendarEventRepository
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetEventByIdUseCaseTest {

    private val userId = 1L
    private val day = LocalDate.of(2026, 7, 15)
    private lateinit var repo: FakeCalendarEventRepository
    private lateinit var useCase: GetEventByIdUseCase

    @Before
    fun setUp() {
        repo = FakeCalendarEventRepository()
        useCase = GetEventByIdUseCase(repo)
    }

    @Test
    fun `invoke_existingEventOwnedByUser_returnsEvent`() = runTest {
        val event = CalendarEvent(1, userId, "Doctor", day, false, LocalTime.of(9, 0), LocalTime.of(10, 0))
        repo.seedEvent(event)

        val result = useCase(1, userId)

        assertEquals(event, result)
    }

    @Test
    fun `invoke_wrongUser_returnsNull`() = runTest {
        val event = CalendarEvent(1, userId, "Doctor", day, true, null, null)
        repo.seedEvent(event)

        val result = useCase(1, userId = 2L)

        assertNull(result)
    }

    @Test
    fun `invoke_nonExistentId_returnsNull`() = runTest {
        val result = useCase(999, userId)

        assertNull(result)
    }
}
