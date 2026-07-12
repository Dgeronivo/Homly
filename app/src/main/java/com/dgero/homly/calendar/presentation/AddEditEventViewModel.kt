package com.dgero.homly.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.calendar.domain.CalendarLimits
import com.dgero.homly.calendar.domain.error.CalendarError
import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.domain.usecase.CreateEventUseCase
import com.dgero.homly.calendar.domain.usecase.GetEventByIdUseCase
import com.dgero.homly.calendar.domain.usecase.UpdateEventUseCase
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the add/edit event form (SAD Flow 2, Flow 3).
 *
 * [eventId] is `null` when creating a new event, or the id of an existing event to pre-fill and
 * update in place (AC-05). [userId] is read once from [sessionRepository] on init, mirroring
 * [com.dgero.homly.todolist.presentation.TodoListViewModel]'s pattern.
 */
class AddEditEventViewModel(
    private val eventId: Long?,
    private val createEvent: CreateEventUseCase,
    private val updateEvent: UpdateEventUseCase,
    private val getEventById: GetEventByIdUseCase,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditEventUiState(isEditMode = eventId != null))
    val uiState: StateFlow<AddEditEventUiState> = _uiState.asStateFlow()

    private var userId: Long? = null

    init {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId.filterNotNull().first()
            userId = uid
            if (eventId != null) prefill(eventId, uid)
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, titleError = null) }
    }

    fun onDateChange(value: LocalDate) {
        _uiState.update { it.copy(date = value) }
    }

    fun onAllDayToggle(isAllDay: Boolean) {
        _uiState.update {
            it.copy(
                isAllDay = isAllDay,
                startTime = if (isAllDay) null else it.startTime,
                endTime = if (isAllDay) null else it.endTime,
                timeError = null,
            )
        }
    }

    fun onStartTimeChange(value: LocalTime) {
        _uiState.update { it.copy(startTime = value, timeError = null) }
    }

    fun onEndTimeChange(value: LocalTime) {
        _uiState.update { it.copy(endTime = value, timeError = null) }
    }

    fun onSave() {
        val uid = userId ?: return
        val current = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val draft = CalendarEvent(
                id = eventId ?: 0L,
                userId = uid,
                title = current.title,
                date = current.date,
                isAllDay = current.isAllDay,
                startTime = current.startTime,
                endTime = current.endTime,
            )

            val error = if (eventId == null) {
                createEvent(uid, draft).fold(onSuccess = { null }, onFailure = { it })
            } else {
                updateEvent(draft).fold(onSuccess = { null }, onFailure = { it })
            }

            _uiState.update { state ->
                if (error == null) {
                    state.copy(isSaving = false, saveCompleted = true, titleError = null, timeError = null, formError = null)
                } else {
                    applyError(state, error)
                }
            }
        }
    }

    private suspend fun prefill(id: Long, uid: Long) {
        val event = getEventById(id, uid) ?: return
        _uiState.update {
            it.copy(
                title = event.title,
                date = event.date,
                isAllDay = event.isAllDay,
                startTime = event.startTime,
                endTime = event.endTime,
            )
        }
    }

    private fun applyError(state: AddEditEventUiState, error: Throwable): AddEditEventUiState =
        when (error) {
            is CalendarError.EmptyTitle -> state.copy(isSaving = false, titleError = "Title cannot be empty")
            is CalendarError.TitleTooLong -> state.copy(
                isSaving = false,
                titleError = "Title is too long (max ${CalendarLimits.MAX_TITLE_LENGTH} characters)",
            )
            is CalendarError.EndNotAfterStart -> state.copy(
                isSaving = false,
                timeError = "End time must be after start time",
            )
            is CalendarError.EventLimitReached -> state.copy(
                isSaving = false,
                formError = "Event limit reached (max ${CalendarLimits.MAX_EVENTS} events)",
            )
            else -> state.copy(isSaving = false, formError = "Something went wrong")
        }

    class Factory(
        private val eventId: Long?,
        private val createEvent: CreateEventUseCase,
        private val updateEvent: UpdateEventUseCase,
        private val getEventById: GetEventByIdUseCase,
        private val sessionRepository: SessionRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AddEditEventViewModel(eventId, createEvent, updateEvent, getEventById, sessionRepository) as T
    }
}
