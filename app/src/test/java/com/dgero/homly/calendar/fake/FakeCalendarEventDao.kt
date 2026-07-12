package com.dgero.homly.calendar.fake

import com.dgero.homly.calendar.data.local.CalendarEventDao
import com.dgero.homly.calendar.data.local.CalendarEventEntity
import java.time.LocalDate

/** In-memory fake for [CalendarEventDao], used by JVM unit tests that don't need real Room. */
class FakeCalendarEventDao : CalendarEventDao {

    private val events = mutableMapOf<Long, CalendarEventEntity>()
    private var nextId = 1L

    override suspend fun getEventsForRange(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<CalendarEventEntity> = events.values.filter {
        it.userId == userId && !it.date.isBefore(startDate) && !it.date.isAfter(endDate)
    }

    override suspend fun getEventCount(userId: Long): Int =
        events.values.count { it.userId == userId }

    override suspend fun getById(id: Long): CalendarEventEntity? = events[id]

    override suspend fun insert(entity: CalendarEventEntity): Long {
        val id = nextId++
        events[id] = entity.copy(id = id)
        return id
    }

    override suspend fun update(entity: CalendarEventEntity) {
        if (events.containsKey(entity.id)) {
            events[entity.id] = entity
        }
    }

    override suspend fun delete(entity: CalendarEventEntity) {
        events.remove(entity.id)
    }
}
