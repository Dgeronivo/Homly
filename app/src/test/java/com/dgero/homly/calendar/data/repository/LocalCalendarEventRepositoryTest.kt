package com.dgero.homly.calendar.data.repository

import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.fake.FakeCalendarEventDao
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalCalendarEventRepositoryTest {

    private val dao = FakeCalendarEventDao()
    private val repository = LocalCalendarEventRepository(dao)

    @Test
    fun `create_thenGetEventsForMonth_roundTripsTimedEventFields`() = runTest {
        val draft = CalendarEvent(
            id = 0,
            userId = 1,
            title = "Dentist",
            date = LocalDate.of(2026, 7, 15),
            isAllDay = false,
            startTime = LocalTime.of(9, 30),
            endTime = LocalTime.of(10, 15),
        )

        val created = repository.create(draft).getOrThrow()
        val events = repository.getEventsForMonth(1, YearMonth.of(2026, 7))

        assertEquals(1, events.size)
        val roundTripped = events.single()
        assertEquals(created.id, roundTripped.id)
        assertEquals(draft.userId, roundTripped.userId)
        assertEquals(draft.title, roundTripped.title)
        assertEquals(draft.date, roundTripped.date)
        assertEquals(draft.isAllDay, roundTripped.isAllDay)
        assertEquals(draft.startTime, roundTripped.startTime)
        assertEquals(draft.endTime, roundTripped.endTime)
    }

    @Test
    fun `create_allDayEvent_roundTripsNullTimes`() = runTest {
        val draft = CalendarEvent(
            id = 0,
            userId = 1,
            title = "Holiday",
            date = LocalDate.of(2026, 7, 20),
            isAllDay = true,
            startTime = null,
            endTime = null,
        )

        repository.create(draft)
        val roundTripped = repository.getEventsForMonth(1, YearMonth.of(2026, 7)).single()

        assertNull(roundTripped.startTime)
        assertNull(roundTripped.endTime)
        assertTrue(roundTripped.isAllDay)
    }

    @Test
    fun `create_assignsGeneratedId`() = runTest {
        val draft = CalendarEvent(
            id = 0,
            userId = 1,
            title = "Meeting",
            date = LocalDate.of(2026, 7, 1),
            isAllDay = true,
            startTime = null,
            endTime = null,
        )

        val created = repository.create(draft).getOrThrow()

        assertTrue(created.id != 0L)
    }

    @Test
    fun `getEventsForMonth_excludesEventsFromOtherMonths`() = runTest {
        repository.create(
            CalendarEvent(0, 1, "In June", LocalDate.of(2026, 6, 30), true, null, null)
        )
        repository.create(
            CalendarEvent(0, 1, "In July", LocalDate.of(2026, 7, 1), true, null, null)
        )
        repository.create(
            CalendarEvent(0, 1, "In August", LocalDate.of(2026, 8, 1), true, null, null)
        )

        val events = repository.getEventsForMonth(1, YearMonth.of(2026, 7))

        assertEquals(listOf("In July"), events.map { it.title })
    }

    @Test
    fun `getEventsForMonth_excludesOtherUsersEvents`() = runTest {
        repository.create(CalendarEvent(0, 1, "Mine", LocalDate.of(2026, 7, 5), true, null, null))
        repository.create(CalendarEvent(0, 2, "Theirs", LocalDate.of(2026, 7, 5), true, null, null))

        val events = repository.getEventsForMonth(1, YearMonth.of(2026, 7))

        assertEquals(listOf("Mine"), events.map { it.title })
    }

    @Test
    fun `update_changesStoredFields`() = runTest {
        val created = repository.create(
            CalendarEvent(0, 1, "Old title", LocalDate.of(2026, 7, 10), true, null, null)
        ).getOrThrow()

        val result = repository.update(created.copy(title = "New title", date = LocalDate.of(2026, 7, 11)))

        assertTrue(result.isSuccess)
        val updated = repository.getEventsForMonth(1, YearMonth.of(2026, 7)).single()
        assertEquals("New title", updated.title)
        assertEquals(LocalDate.of(2026, 7, 11), updated.date)
    }

    @Test
    fun `update_withWrongUser_failsAndKeepsOriginal`() = runTest {
        val created = repository.create(
            CalendarEvent(0, 1, "Old title", LocalDate.of(2026, 7, 10), true, null, null)
        ).getOrThrow()

        val result = repository.update(created.copy(userId = 2, title = "Hijacked"))

        assertTrue(result.isFailure)
        val stored = repository.getEventsForMonth(1, YearMonth.of(2026, 7)).single()
        assertEquals("Old title", stored.title)
    }

    @Test
    fun `update_nonExistentId_fails`() = runTest {
        val result = repository.update(
            CalendarEvent(999, 1, "Ghost", LocalDate.of(2026, 7, 10), true, null, null)
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `delete_withCorrectUser_removesEvent`() = runTest {
        val created = repository.create(
            CalendarEvent(0, 1, "Task", LocalDate.of(2026, 7, 10), true, null, null)
        ).getOrThrow()

        val result = repository.delete(created.id, 1)

        assertTrue(result.isSuccess)
        assertTrue(repository.getEventsForMonth(1, YearMonth.of(2026, 7)).isEmpty())
    }

    @Test
    fun `delete_withWrongUser_failsAndKeepsEvent`() = runTest {
        val created = repository.create(
            CalendarEvent(0, 1, "Task", LocalDate.of(2026, 7, 10), true, null, null)
        ).getOrThrow()

        val result = repository.delete(created.id, 2)

        assertTrue(result.isFailure)
        assertEquals(1, repository.getEventsForMonth(1, YearMonth.of(2026, 7)).size)
    }

    @Test
    fun `delete_nonExistentId_fails`() = runTest {
        val result = repository.delete(999, 1)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getById_existingEventOwnedByUser_returnsMappedEvent`() = runTest {
        val created = repository.create(
            CalendarEvent(0, 1, "Dentist", LocalDate.of(2026, 7, 15), false, LocalTime.of(9, 0), LocalTime.of(10, 0))
        ).getOrThrow()

        val found = repository.getById(created.id, 1)

        assertEquals(created, found)
    }

    @Test
    fun `getById_wrongUser_returnsNull`() = runTest {
        val created = repository.create(
            CalendarEvent(0, 1, "Dentist", LocalDate.of(2026, 7, 15), true, null, null)
        ).getOrThrow()

        val found = repository.getById(created.id, 2)

        assertNull(found)
    }

    @Test
    fun `getById_nonExistentId_returnsNull`() = runTest {
        val found = repository.getById(999, 1)

        assertNull(found)
    }

    @Test
    fun `getEventCount_countsOnlyGivenUsersEvents`() = runTest {
        repository.create(CalendarEvent(0, 1, "A", LocalDate.of(2026, 7, 1), true, null, null))
        repository.create(CalendarEvent(0, 1, "B", LocalDate.of(2026, 7, 2), true, null, null))
        repository.create(CalendarEvent(0, 2, "C", LocalDate.of(2026, 7, 3), true, null, null))

        assertEquals(2, repository.getEventCount(1))
        assertEquals(1, repository.getEventCount(2))
    }
}
