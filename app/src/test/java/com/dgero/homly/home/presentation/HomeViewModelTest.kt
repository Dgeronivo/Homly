package com.dgero.homly.home.presentation

import com.dgero.homly.auth.domain.model.User
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.auth.domain.repository.UserRepository
import com.dgero.homly.auth.domain.usecase.LogoutUseCase
import com.dgero.homly.calendar.domain.model.CalendarEvent
import com.dgero.homly.calendar.domain.usecase.GetEventsUseCase
import com.dgero.homly.calendar.domain.usecase.GetTodayEventsCountUseCaseImpl
import com.dgero.homly.calendar.fake.FakeCalendarEventRepository
import com.dgero.homly.shopping.domain.FakeShoppingRepository
import com.dgero.homly.shopping.domain.model.ShoppingItem
import com.dgero.homly.shopping.domain.usecase.GetUnboughtShoppingItemCountUseCaseImpl
import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.domain.usecase.GetPendingTodoCountUseCaseImpl
import com.dgero.homly.todolist.fake.FakeTodoRepository
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val today = LocalDate.now()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeSessionRepository(initial: Long? = null) : SessionRepository {
        val userIdFlow = MutableStateFlow(initial)
        override val currentUserId: Flow<Long?> = userIdFlow
        override suspend fun setSession(userId: Long) {
            userIdFlow.value = userId
        }

        override suspend fun clear() {
            userIdFlow.value = null
        }
    }

    private class FakeUserRepository : UserRepository {
        override suspend fun register(login: String, password: String): Result<User> =
            Result.success(User(1, login))

        override suspend fun login(login: String, password: String): Result<User> =
            Result.success(User(1, login))

        override suspend fun getUserById(id: Long): User? = User(id, "alex")
    }

    private fun makeViewModel(
        calendarRepo: FakeCalendarEventRepository = FakeCalendarEventRepository(),
        shoppingRepo: FakeShoppingRepository = FakeShoppingRepository(),
        todoRepo: FakeTodoRepository = FakeTodoRepository(),
        session: FakeSessionRepository = FakeSessionRepository(),
    ): HomeViewModel {
        return HomeViewModel(
            logoutUseCase = LogoutUseCase(session),
            userRepository = FakeUserRepository(),
            sessionRepository = session,
            getTodayEventsCount = GetTodayEventsCountUseCaseImpl(GetEventsUseCase(calendarRepo)),
            getUnboughtShoppingItemCount = GetUnboughtShoppingItemCountUseCaseImpl(shoppingRepo),
            getPendingTodoCount = GetPendingTodoCountUseCaseImpl(todoRepo),
        )
    }

    @Test
    fun `todayEventsCount_countsOnlyTodayEvents`() = runTest {
        val calendarRepo = FakeCalendarEventRepository()
        calendarRepo.seedEvent(
            CalendarEvent(id = 1, userId = 1, title = "Today", date = today, isAllDay = true, startTime = null, endTime = null),
        )
        calendarRepo.seedEvent(
            CalendarEvent(
                id = 2, userId = 1, title = "Other day", date = today.plusDays(5),
                isAllDay = true, startTime = null, endTime = null,
            ),
        )
        val session = FakeSessionRepository()
        val vm = makeViewModel(calendarRepo = calendarRepo, session = session)
        backgroundScope.launch { vm.todayEventsCount.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        assertEquals(1, vm.todayEventsCount.value)
    }

    @Test
    fun `todayEventsCount_zeroWhenNoEventsToday`() = runTest {
        val session = FakeSessionRepository()
        val vm = makeViewModel(session = session)
        backgroundScope.launch { vm.todayEventsCount.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        assertEquals(0, vm.todayEventsCount.value)
    }

    @Test
    fun `shoppingActiveCount_countsOnlyNotBoughtItems`() = runTest {
        val shoppingRepo = FakeShoppingRepository(
            seed = listOf(
                ShoppingItem(id = 1, name = "Milk", isBought = false, createdAt = 1L),
                ShoppingItem(id = 2, name = "Bread", isBought = true, createdAt = 2L),
                ShoppingItem(id = 3, name = "Eggs", isBought = false, createdAt = 3L),
            ),
        )
        val session = FakeSessionRepository()
        val vm = makeViewModel(shoppingRepo = shoppingRepo, session = session)
        backgroundScope.launch { vm.shoppingActiveCount.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        assertEquals(2, vm.shoppingActiveCount.value)
    }

    @Test
    fun `shoppingActiveCount_updatesOnlyAfterRefresh`() = runTest {
        val shoppingRepo = FakeShoppingRepository()
        val session = FakeSessionRepository()
        val vm = makeViewModel(shoppingRepo = shoppingRepo, session = session)
        backgroundScope.launch { vm.shoppingActiveCount.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        assertEquals(0, vm.shoppingActiveCount.value)

        // Simulates ShoppingListViewModel adding an item via the same repository, while this
        // HomeViewModel instance is still alive (e.g. Home in the back stack). No live update
        // is expected — the count is a one-shot fetch, refreshed only via refresh().
        shoppingRepo.add(1L, "Milk")
        advanceUntilIdle()

        assertEquals(0, vm.shoppingActiveCount.value)

        vm.refresh()
        advanceUntilIdle()

        assertEquals(1, vm.shoppingActiveCount.value)
    }

    @Test
    fun `todoPendingCount_countsOnlyNotDoneItems`() = runTest {
        val todoRepo = FakeTodoRepository()
        todoRepo.seedItem(1L, TodoItem(id = 1, title = "Pending", isDone = false, createdAt = 1L))
        todoRepo.seedItem(1L, TodoItem(id = 2, title = "Done", isDone = true, createdAt = 2L))
        val session = FakeSessionRepository()
        val vm = makeViewModel(todoRepo = todoRepo, session = session)
        backgroundScope.launch { vm.todoPendingCount.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        assertEquals(1, vm.todoPendingCount.value)
    }

    @Test
    fun `refresh_reloadsEventsAndTodoCounts`() = runTest {
        val calendarRepo = FakeCalendarEventRepository()
        val todoRepo = FakeTodoRepository()
        val session = FakeSessionRepository()
        val vm = makeViewModel(calendarRepo = calendarRepo, todoRepo = todoRepo, session = session)
        backgroundScope.launch { vm.todayEventsCount.collect {} }
        backgroundScope.launch { vm.todoPendingCount.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        assertEquals(0, vm.todayEventsCount.value)
        assertEquals(0, vm.todoPendingCount.value)

        // Simulates other ViewModels creating data via the same repositories while this
        // HomeViewModel instance is still alive (e.g. Home in the back stack).
        calendarRepo.seedEvent(
            CalendarEvent(id = 1, userId = 1, title = "New", date = today, isAllDay = true, startTime = null, endTime = null),
        )
        todoRepo.seedItem(1L, TodoItem(id = 1, title = "New task", isDone = false, createdAt = 1L))

        vm.refresh()
        advanceUntilIdle()

        assertEquals(1, vm.todayEventsCount.value)
        assertEquals(1, vm.todoPendingCount.value)
    }

    @Test
    fun `onLogout_stillCallsLogoutUseCaseAndOnDone`() = runTest {
        val session = FakeSessionRepository(initial = 1L)
        val vm = makeViewModel(session = session)
        backgroundScope.launch { vm.login.collect {} }
        advanceUntilIdle()

        var onDoneCalled = false
        vm.onLogout { onDoneCalled = true }
        advanceUntilIdle()

        assertTrue(onDoneCalled)
        assertEquals(null, session.userIdFlow.value)
    }
}
