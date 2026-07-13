package com.dgero.homly.calendar.fake

import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.domain.repository.CalendarEventRepository
import java.time.YearMonth

/** In-memory fake for [CalendarEventRepository], used by JVM unit tests. */
class FakeCalendarEventRepository : CalendarEventRepository {

    private val events = mutableMapOf<Long, CalendarEvent>()
    private var nextId = 1L

    /** Test helper: counts calls to [update], so tests can assert it was never invoked. */
    var updateCallCount = 0
        private set

    override suspend fun getEventsForMonth(userId: Long, yearMonth: YearMonth): List<CalendarEvent> =
        events.values.filter { it.userId == userId && YearMonth.from(it.date) == yearMonth }

    override suspend fun getEventCount(userId: Long): Int =
        events.values.count { it.userId == userId }

    override suspend fun getById(id: Long, userId: Long): CalendarEvent? {
        val event = events[id] ?: return null
        return if (event.userId != userId) null else event
    }

    override suspend fun create(event: CalendarEvent): Result<CalendarEvent> {
        val created = event.copy(id = nextId++)
        events[created.id] = created
        return Result.success(created)
    }

    override suspend fun update(event: CalendarEvent): Result<Unit> {
        updateCallCount++
        val existing = events[event.id]
        return if (existing == null || existing.userId != event.userId) {
            Result.failure(NoSuchElementException("Calendar event ${event.id} not found for user ${event.userId}"))
        } else {
            events[event.id] = event
            Result.success(Unit)
        }
    }

    override suspend fun delete(id: Long, userId: Long): Result<Unit> {
        val event = events[id]
        return if (event == null || event.userId != userId) {
            Result.failure(NoSuchElementException("Calendar event $id not found for user $userId"))
        } else {
            events.remove(id)
            Result.success(Unit)
        }
    }

    /** Test helper: insert a pre-built event with exact field values (bypasses [create]'s id generation). */
    fun seedEvent(event: CalendarEvent) {
        events[event.id] = event
        if (event.id >= nextId) nextId = event.id + 1
    }
}
