package com.dgero.homly.calendar.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate

@Dao
interface CalendarEventDao {

    /**
     * Returns events for [userId] whose `date` (stored as epoch-day, see [DateTimeConverters])
     * falls within [startDate]..[endDate] inclusive — used for month/day range views.
     */
    @Query("SELECT * FROM calendar_events WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getEventsForRange(userId: Long, startDate: LocalDate, endDate: LocalDate): List<CalendarEventEntity>

    @Query("SELECT COUNT(*) FROM calendar_events WHERE userId = :userId")
    suspend fun getEventCount(userId: Long): Int

    /**
     * Looks up a single event by primary key, unfiltered by owner. Needed by
     * `LocalCalendarEventRepository.delete` to obtain a full [CalendarEventEntity] instance
     * for [delete] (Room's `@Delete` matches by primary key, but still requires a populated
     * entity to call it with). Per ADR-0002, write-time ownership checks belong to the
     * use-case layer — callers of this method are responsible for verifying `userId` on
     * the returned entity before acting on it.
     */
    @Query("SELECT * FROM calendar_events WHERE id = :id")
    suspend fun getById(id: Long): CalendarEventEntity?

    @Insert
    suspend fun insert(entity: CalendarEventEntity): Long

    @Update
    suspend fun update(entity: CalendarEventEntity)

    @Delete
    suspend fun delete(entity: CalendarEventEntity)
}
