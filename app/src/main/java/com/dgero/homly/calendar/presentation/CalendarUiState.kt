package com.dgero.homly.calendar.presentation

import com.dgero.homly.calendar.domain.model.CalendarEvent
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val currentYearMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val daysWithEvents: Set<LocalDate> = emptySet(),
    val selectedDayEvents: List<CalendarEvent> = emptyList(),
)
