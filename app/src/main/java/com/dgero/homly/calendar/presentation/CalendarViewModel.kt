package com.dgero.homly.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.domain.usecase.CreateEventUseCase
import com.dgero.homly.calendar.domain.usecase.DeleteEventUseCase
import com.dgero.homly.calendar.domain.usecase.GetEventsUseCase
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val getEvents: GetEventsUseCase,
    private val deleteEvent: DeleteEventUseCase,
    private val createEvent: CreateEventUseCase,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _monthEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())
    private val _selectedDayEvents = MutableStateFlow<List<CalendarEvent>>(emptyList())

    /** One-off navigation/UI events (SAD Flow 4) — collected by [CalendarScreen] via `LaunchedEffect`. */
    private val _navigateToAddEvent = Channel<Unit>(Channel.BUFFERED)
    val navigateToAddEvent = _navigateToAddEvent.receiveAsFlow()

    private val _eventLimitReached = Channel<Unit>(Channel.BUFFERED)
    val eventLimitReached = _eventLimitReached.receiveAsFlow()

    private var userId: Long? = null

    init {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId.filterNotNull().first()
            userId = uid
            loadMonth(_currentYearMonth.value)
        }
    }

    val uiState: StateFlow<CalendarUiState> = combine(
        _currentYearMonth, _selectedDate, _monthEvents, _selectedDayEvents,
    ) { yearMonth, selectedDate, monthEvents, selectedDayEvents ->
        CalendarUiState(
            currentYearMonth = yearMonth,
            selectedDate = selectedDate,
            daysWithEvents = monthEvents.map { it.date }.toSet(),
            selectedDayEvents = selectedDayEvents,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    /**
     * Re-fetches the given month's events from the repository (SAD Flow 1). Keeps the same
     * day-of-month selected (clamped to the new month's length) so the FAB's add-event
     * pre-filled date (AC-13) always stays within the visible month.
     */
    fun onMonthChanged(yearMonth: YearMonth) {
        _currentYearMonth.value = yearMonth
        _selectedDate.value = clampToMonth(_selectedDate.value, yearMonth)
        viewModelScope.launch { loadMonth(yearMonth) }
    }

    private fun clampToMonth(date: LocalDate, yearMonth: YearMonth): LocalDate =
        yearMonth.atDay(minOf(date.dayOfMonth, yearMonth.lengthOfMonth()))

    /**
     * Jumps straight back to today (the "Сьогодні" chip): switches the visible month to the
     * current one and selects today's date. Doesn't reuse [onMonthChanged] because its
     * [clampToMonth] would keep the previously selected day-of-month instead of today's.
     */
    fun onTodayClick() {
        val today = LocalDate.now()
        _currentYearMonth.value = YearMonth.from(today)
        _selectedDate.value = today
        viewModelScope.launch { loadMonth(_currentYearMonth.value) }
    }

    /**
     * Re-fetches the currently displayed month. Called when [CalendarScreen] resumes (e.g.
     * returning from creating/editing an event in a separate nav destination — AC-05/AC-06),
     * since this ViewModel instance survives that round-trip and would otherwise keep serving
     * stale data.
     */
    fun refresh() {
        viewModelScope.launch { loadMonth(_currentYearMonth.value) }
    }

    /** Filters the already-loaded month's events down to [date] — no new repository call. */
    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
        _selectedDayEvents.value = GetEventsUseCase.forDay(_monthEvents.value, date)
    }

    /**
     * Deletes the event with [id] and removes it from the already-held in-memory state
     * immediately (no repository round-trip to refresh) so the UI updates without delay.
     */
    fun onDeleteEvent(id: Long) {
        val uid = userId ?: return
        viewModelScope.launch {
            val result = deleteEvent(id, uid)
            if (result.isSuccess) {
                _monthEvents.value = _monthEvents.value.filterNot { it.id == id }
                _selectedDayEvents.value = _selectedDayEvents.value.filterNot { it.id == id }
            }
        }
    }

    /**
     * Handles a FAB tap (SAD Flow 4): checks the per-user event limit via
     * [CreateEventUseCase.checkCanCreate] and emits [navigateToAddEvent] when under the limit,
     * or [eventLimitReached] so the screen shows a Snackbar instead of navigating (AC-07b).
     */
    fun onAddEventClick() {
        val uid = userId ?: return
        viewModelScope.launch {
            val canCreate = createEvent.checkCanCreate(uid)
            if (canCreate.isSuccess) _navigateToAddEvent.send(Unit) else _eventLimitReached.send(Unit)
        }
    }

    private suspend fun loadMonth(yearMonth: YearMonth) {
        val uid = userId ?: return
        val events = getEvents(uid, yearMonth)
        _monthEvents.value = events
        _selectedDayEvents.value = GetEventsUseCase.forDay(events, _selectedDate.value)
    }

    class Factory(
        private val getEvents: GetEventsUseCase,
        private val deleteEvent: DeleteEventUseCase,
        private val createEvent: CreateEventUseCase,
        private val sessionRepository: SessionRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CalendarViewModel(getEvents, deleteEvent, createEvent, sessionRepository) as T
    }
}
