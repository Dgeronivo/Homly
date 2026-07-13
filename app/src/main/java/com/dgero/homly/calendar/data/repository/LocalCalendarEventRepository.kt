package com.dgero.homly.calendar.data.repository

import com.dgero.homly.calendar.data.local.CalendarEventDao
import com.dgero.homly.calendar.data.local.CalendarEventEntity
import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.domain.repository.CalendarEventRepository
import java.time.YearMonth

class LocalCalendarEventRepository(
    private val dao: CalendarEventDao,
) : CalendarEventRepository {

    override suspend fun getEventsForMonth(userId: Long, yearMonth: YearMonth): List<CalendarEvent> {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        return dao.getEventsForRange(userId, startDate, endDate).map { it.toDomain() }
    }

    override suspend fun getEventCount(userId: Long): Int = dao.getEventCount(userId)

    override suspend fun getById(id: Long, userId: Long): CalendarEvent? {
        val entity = dao.getById(id)
        if (entity == null || entity.userId != userId) return null
        return entity.toDomain()
    }

    override suspend fun create(event: CalendarEvent): Result<CalendarEvent> = try {
        val id = dao.insert(event.toEntity())
        Result.success(event.copy(id = id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun update(event: CalendarEvent): Result<Unit> = try {
        val existing = dao.getById(event.id)
        if (existing == null || existing.userId != event.userId) {
            Result.failure(NoSuchElementException("Calendar event ${event.id} not found for user ${event.userId}"))
        } else {
            dao.update(event.toEntity())
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun delete(id: Long, userId: Long): Result<Unit> = try {
        val entity = dao.getById(id)
        if (entity == null || entity.userId != userId) {
            Result.failure(NoSuchElementException("Calendar event $id not found for user $userId"))
        } else {
            dao.delete(entity)
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun CalendarEventEntity.toDomain() = CalendarEvent(
        id = id,
        userId = userId,
        title = title,
        date = date,
        isAllDay = isAllDay,
        startTime = startTime,
        endTime = endTime,
    )

    private fun CalendarEvent.toEntity() = CalendarEventEntity(
        id = id,
        userId = userId,
        title = title,
        date = date,
        isAllDay = isAllDay,
        startTime = startTime,
        endTime = endTime,
    )
}
