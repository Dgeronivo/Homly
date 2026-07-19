package com.dgero.homly.calendar.presentation

import com.dgero.homly.calendar.domain.CalendarLimits
import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.domain.usecase.CreateEventUseCase
import com.dgero.homly.calendar.domain.usecase.DeleteEventUseCase
import com.dgero.homly.calendar.domain.usecase.GetEventsUseCase
import com.dgero.homly.calendar.fake.FakeCalendarEventRepository
import com.dgero.homly.calendar.fake.FakeSessionRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val yearMonth = YearMonth.of(2026, 7)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel(
        repo: FakeCalendarEventRepository = FakeCalendarEventRepository(),
        session: FakeSessionRepository = FakeSessionRepository(),
    ): CalendarViewModel {
        return CalendarViewModel(
            getEvents = GetEventsUseCase(repo),
            deleteEvent = DeleteEventUseCase(repo),
            createEvent = CreateEventUseCase(repo),
            sessionRepository = session,
        )
    }

    @Test
    fun `onDateSelected_dayWithMixedEvents_isAllDayFirstThenAscendingStartTime`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()
        val day = yearMonth.atDay(10)

        val timed2 = CalendarEvent(
            id = 1, userId = 1, title = "Dinner", date = day,
            isAllDay = false, startTime = LocalTime.of(18, 0), endTime = LocalTime.of(19, 0),
        )
        val timed1 = CalendarEvent(
            id = 2, userId = 1, title = "Meeting", date = day,
            isAllDay = false, startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 0),
        )
        val allDay = CalendarEvent(
            id = 3, userId = 1, title = "Birthday", date = day,
            isAllDay = true, startTime = null, endTime = null,
        )
        repo.seedEvent(timed2)
        repo.seedEvent(timed1)
        repo.seedEvent(allDay)

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onDateSelected(day)
        advanceUntilIdle()

        val ids = vm.uiState.value.selectedDayEvents.map { it.id }
        assertEquals(listOf(3L, 2L, 1L), ids)
    }

    @Test
    fun `onDateSelected_emptyDay_selectedDayEventsIsEmpty`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()
        repo.seedEvent(
            CalendarEvent(
                id = 1, userId = 1, title = "Something", date = yearMonth.atDay(5),
                isAllDay = true, startTime = null, endTime = null,
            ),
        )

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onDateSelected(yearMonth.atDay(12))
        advanceUntilIdle()

        assertTrue(vm.uiState.value.selectedDayEvents.isEmpty())
    }

    @Test
    fun `dataIsolation_onlyCurrentUsersEventsAppearInMonthAndDayState`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()
        val sharedDay = yearMonth.atDay(15)

        val user1Event = CalendarEvent(
            id = 1, userId = 1, title = "User1 event", date = sharedDay,
            isAllDay = true, startTime = null, endTime = null,
        )
        val user2Event = CalendarEvent(
            id = 2, userId = 2, title = "User2 event", date = sharedDay,
            isAllDay = true, startTime = null, endTime = null,
        )
        val user2OnlyDayEvent = CalendarEvent(
            id = 3, userId = 2, title = "User2 other day", date = yearMonth.atDay(20),
            isAllDay = true, startTime = null, endTime = null,
        )
        repo.seedEvent(user1Event)
        repo.seedEvent(user2Event)
        repo.seedEvent(user2OnlyDayEvent)

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onDateSelected(sharedDay)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(listOf(1L), state.selectedDayEvents.map { it.id })
        assertFalse(state.daysWithEvents.contains(yearMonth.atDay(20)))
        assertTrue(state.daysWithEvents.contains(sharedDay))
    }

    @Test
    fun `onMonthChanged_selectedDayFitsNewMonth_keepsSameDayOfMonth`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onDateSelected(yearMonth.atDay(15))
        advanceUntilIdle()

        vm.onMonthChanged(YearMonth.of(2026, 8))
        advanceUntilIdle()

        assertEquals(LocalDate.of(2026, 8, 15), vm.uiState.value.selectedDate)
    }

    @Test
    fun `onMonthChanged_selectedDayBeyondNewMonthLength_clampsToLastDayOfMonth`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onDateSelected(LocalDate.of(2026, 1, 31))
        advanceUntilIdle()

        vm.onMonthChanged(YearMonth.of(2026, 2))
        advanceUntilIdle()

        assertEquals(LocalDate.of(2026, 2, 28), vm.uiState.value.selectedDate)
    }

    @Test
    fun `onDeleteEvent_lastEventOnDay_removesFromSelectedDayEventsAndClearsDayDot`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()
        val day = yearMonth.atDay(10)
        val event = CalendarEvent(
            id = 1, userId = 1, title = "Only event", date = day,
            isAllDay = true, startTime = null, endTime = null,
        )
        repo.seedEvent(event)

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onDateSelected(day)
        advanceUntilIdle()
        assertEquals(listOf(1L), vm.uiState.value.selectedDayEvents.map { it.id })
        assertTrue(vm.uiState.value.daysWithEvents.contains(day))

        vm.onDeleteEvent(1L)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.selectedDayEvents.isEmpty())
        assertFalse(state.daysWithEvents.contains(day))
    }

    @Test
    fun `onDeleteEvent_thenReselectSameDay_deletedEventDoesNotReappear`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()
        val day = yearMonth.atDay(10)
        val deleted = CalendarEvent(
            id = 1, userId = 1, title = "To be deleted", date = day,
            isAllDay = true, startTime = null, endTime = null,
        )
        val remaining = CalendarEvent(
            id = 2, userId = 1, title = "Stays", date = day,
            isAllDay = false, startTime = LocalTime.of(9, 0), endTime = LocalTime.of(10, 0),
        )
        repo.seedEvent(deleted)
        repo.seedEvent(remaining)

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onDateSelected(day)
        advanceUntilIdle()

        vm.onDeleteEvent(1L)
        advanceUntilIdle()

        // Re-select a different day then come back — proves the underlying in-memory
        // month state was actually mutated, not just a transient filter on the current view.
        vm.onDateSelected(yearMonth.atDay(1))
        advanceUntilIdle()
        vm.onDateSelected(day)
        advanceUntilIdle()

        assertEquals(listOf(2L), vm.uiState.value.selectedDayEvents.map { it.id })
    }

    @Test
    fun `refresh_afterEventAddedElsewhere_picksUpNewEventInCurrentMonth`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.daysWithEvents.isEmpty())

        // Simulates AddEditEventViewModel creating an event via the same repository while
        // CalendarViewModel (a separate, already-loaded instance) is still on screen.
        val day = yearMonth.atDay(3)
        repo.seedEvent(
            CalendarEvent(
                id = 1, userId = 1, title = "New event", date = day,
                isAllDay = true, startTime = null, endTime = null,
            ),
        )

        vm.refresh()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.daysWithEvents.contains(day))
    }

    @Test
    fun `onAddEventClick_underLimit_emitsNavigateToAddEvent`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onAddEventClick()
        advanceUntilIdle()

        assertNotNull(vm.navigateToAddEvent.first())
    }

    @Test
    fun `onAddEventClick_atLimit_emitsEventLimitReached`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()
        repeat(CalendarLimits.MAX_EVENTS) { index ->
            repo.seedEvent(
                CalendarEvent(
                    id = (index + 1).toLong(), userId = 1, title = "Event $index",
                    date = yearMonth.atDay(1), isAllDay = true, startTime = null, endTime = null,
                ),
            )
        }

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onAddEventClick()
        advanceUntilIdle()

        assertNotNull(vm.eventLimitReached.first())
    }

    @Test
    fun `onTodayClick_fromAnotherMonth_setsCurrentYearMonthToNow`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onMonthChanged(YearMonth.of(2020, 1))
        advanceUntilIdle()

        vm.onTodayClick()
        advanceUntilIdle()

        assertEquals(YearMonth.now(), vm.uiState.value.currentYearMonth)
    }

    @Test
    fun `onTodayClick_afterSelectingOtherDay_setsSelectedDateToToday`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        // Picks a day-of-month that would survive clampToMonth unchanged, proving onTodayClick
        // sets today's date directly rather than clamping the previously selected day.
        vm.onDateSelected(LocalDate.of(2020, 1, 5))
        advanceUntilIdle()

        vm.onTodayClick()
        advanceUntilIdle()

        assertEquals(LocalDate.now(), vm.uiState.value.selectedDate)
    }

    @Test
    fun `onTodayClick_reloadsEventsForCurrentMonth`() = runTest {
        val repo = FakeCalendarEventRepository()
        val session = FakeSessionRepository()
        val today = LocalDate.now()
        val todayEvent = CalendarEvent(
            id = 1, userId = 1, title = "Today's event", date = today,
            isAllDay = true, startTime = null, endTime = null,
        )
        repo.seedEvent(todayEvent)

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onMonthChanged(YearMonth.of(2020, 1))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.selectedDayEvents.isEmpty())

        vm.onTodayClick()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.daysWithEvents.contains(today))
        assertEquals(listOf(1L), state.selectedDayEvents.map { it.id })
    }
}
