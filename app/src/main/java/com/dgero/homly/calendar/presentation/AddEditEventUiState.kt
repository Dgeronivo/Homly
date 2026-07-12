package com.dgero.homly.calendar.presentation

import java.time.LocalDate
import java.time.LocalTime

data class AddEditEventUiState(
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val isAllDay: Boolean = false,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val titleError: String? = null,
    val timeError: String? = null,
    val formError: String? = null,
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    /** One-shot signal: `true` once the save succeeded, so the screen can navigate back. */
    val saveCompleted: Boolean = false,
)
