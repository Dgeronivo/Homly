package com.dgero.homly.calendar.presentation

import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.domain.usecase.CreateEventUseCase
import com.dgero.homly.calendar.domain.usecase.GetEventByIdUseCase
import com.dgero.homly.calendar.domain.usecase.UpdateEventUseCase
import com.dgero.homly.calendar.fake.FakeCalendarEventRepository
import com.dgero.homly.calendar.fake.FakeSessionRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditEventViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userId = 1L
    private val day = LocalDate.of(2026, 7, 15)

    private lateinit var repo: FakeCalendarEventRepository
    private lateinit var session: FakeSessionRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeCalendarEventRepository()
        session = FakeSessionRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel(eventId: Long? = null): AddEditEventViewModel =
        AddEditEventViewModel(
            eventId = eventId,
            createEvent = CreateEventUseCase(repo),
            updateEvent = UpdateEventUseCase(repo),
            getEventById = GetEventByIdUseCase(repo),
            sessionRepository = session,
        )

    /** Collects [vm]'s state in the background and lets init's session read + prefill settle. */
    private suspend fun TestScope.primeSession(vm: AddEditEventViewModel) {
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(userId)
        advanceUntilIdle()
    }

    @Test
    fun `onSave_validTimedInput_persistsEventAndSetsSaveCompleted`() = runTest {
        val vm = makeViewModel()
        primeSession(vm)

        vm.onTitleChange("Dentist")
        vm.onDateChange(day)
        vm.onStartTimeChange(LocalTime.of(9, 0))
        vm.onEndTimeChange(LocalTime.of(10, 0))

        vm.onSave()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.saveCompleted)
        assertEquals(1, repo.getEventCount(userId))
        val saved = repo.getEventsForMonth(userId, YearMonth.from(day)).single()
        assertEquals("Dentist", saved.title)
        assertFalse(saved.isAllDay)
        assertEquals(LocalTime.of(9, 0), saved.startTime)
        assertEquals(LocalTime.of(10, 0), saved.endTime)
    }

    @Test
    fun `onSave_validAllDayInput_persistsIsAllDayTrue`() = runTest {
        val vm = makeViewModel()
        primeSession(vm)

        vm.onTitleChange("Holiday")
        vm.onDateChange(day)
        vm.onAllDayToggle(true)

        vm.onSave()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.saveCompleted)
        val saved = repo.getEventsForMonth(userId, YearMonth.from(day)).single()
        assertTrue(saved.isAllDay)
        assertNull(saved.startTime)
        assertNull(saved.endTime)
    }

    @Test
    fun `onSave_endTimeNotAfterStartTime_setsTimeErrorAndDoesNotSaveOrComplete`() = runTest {
        val vm = makeViewModel()
        primeSession(vm)

        vm.onTitleChange("Meeting")
        vm.onDateChange(day)
        vm.onStartTimeChange(LocalTime.of(10, 0))
        vm.onEndTimeChange(LocalTime.of(10, 0))

        vm.onSave()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("End time must be after start time", state.timeError)
        assertFalse(state.saveCompleted)
        assertEquals(0, repo.getEventCount(userId))
    }

    @Test
    fun `onSave_emptyTitle_setsTitleErrorAndDoesNotSave`() = runTest {
        val vm = makeViewModel()
        primeSession(vm)

        vm.onTitleChange("   ")
        vm.onDateChange(day)
        vm.onAllDayToggle(true)

        vm.onSave()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("Title cannot be empty", state.titleError)
        assertFalse(state.saveCompleted)
        assertEquals(0, repo.getEventCount(userId))
    }

    @Test
    fun `onSave_titleTooLong_setsTitleErrorAndDoesNotSave`() = runTest {
        val vm = makeViewModel()
        primeSession(vm)

        vm.onTitleChange("x".repeat(101))
        vm.onDateChange(day)
        vm.onAllDayToggle(true)

        vm.onSave()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("Title is too long (max 100 characters)", state.titleError)
        assertFalse(state.saveCompleted)
        assertEquals(0, repo.getEventCount(userId))
    }

    @Test
    fun `init_withExistingEventId_prefillsFormFields`() = runTest {
        val existing = CalendarEvent(
            id = 5, userId = userId, title = "Existing", date = day,
            isAllDay = false, startTime = LocalTime.of(11, 0), endTime = LocalTime.of(12, 0),
        )
        repo.seedEvent(existing)

        val vm = makeViewModel(eventId = 5)
        primeSession(vm)

        val state = vm.uiState.value
        assertTrue(state.isEditMode)
        assertEquals("Existing", state.title)
        assertEquals(day, state.date)
        assertFalse(state.isAllDay)
        assertEquals(LocalTime.of(11, 0), state.startTime)
        assertEquals(LocalTime.of(12, 0), state.endTime)
    }

    @Test
    fun `onSave_editModeWithExistingEvent_updatesInPlaceRatherThanCreating`() = runTest {
        val existing = CalendarEvent(
            id = 5, userId = userId, title = "Existing", date = day,
            isAllDay = false, startTime = LocalTime.of(11, 0), endTime = LocalTime.of(12, 0),
        )
        repo.seedEvent(existing)

        val vm = makeViewModel(eventId = 5)
        primeSession(vm)

        vm.onTitleChange("Updated title")

        vm.onSave()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.saveCompleted)
        assertEquals(1, repo.getEventCount(userId))
        val saved = repo.getEventsForMonth(userId, YearMonth.from(day)).single()
        assertEquals(5L, saved.id)
        assertEquals("Updated title", saved.title)
    }
}
