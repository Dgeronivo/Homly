package com.dgero.homly.calendar.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "calendar_events", indices = [Index("userId")])
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val date: LocalDate,
    val isAllDay: Boolean,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
)
