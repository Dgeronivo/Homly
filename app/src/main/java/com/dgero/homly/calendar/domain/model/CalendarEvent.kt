package com.dgero.homly.calendar.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class CalendarEvent(
    val id: Long,
    val userId: Long,
    val title: String,
    val date: LocalDate,
    val isAllDay: Boolean,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
)
