package com.dgero.homly.todolist.presentation

import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.domain.model.TodoLimits
import com.dgero.homly.todolist.domain.usecase.AddTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.DeleteCompletedTodoItemsUseCase
import com.dgero.homly.todolist.domain.usecase.DeleteTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.EditTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.GetTodoItemsUseCase
import com.dgero.homly.todolist.domain.usecase.ToggleTodoItemUseCase
import com.dgero.homly.todolist.domain.validation.TodoTitleValidator
import com.dgero.homly.todolist.fake.FakeSessionRepository
import com.dgero.homly.todolist.fake.FakeTodoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
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
class TodoListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeViewModel(
        repo: FakeTodoRepository = FakeTodoRepository(),
        session: FakeSessionRepository = FakeSessionRepository(),
    ): TodoListViewModel {
        return TodoListViewModel(
            getItems = GetTodoItemsUseCase(repo),
            addItem = AddTodoItemUseCase(repo, TodoTitleValidator),
            editItem = EditTodoItemUseCase(repo, TodoTitleValidator),
            toggleItem = ToggleTodoItemUseCase(repo),
            deleteItem = DeleteTodoItemUseCase(repo),
            deleteCompletedItems = DeleteCompletedTodoItemsUseCase(repo),
            validator = TodoTitleValidator,
            sessionRepository = session,
        )
    }

    @Test
    fun `noUser_itemsAreEmpty`() = runTest {
        val vm = makeViewModel()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertTrue(vm.uiState.value.items.isEmpty())
    }

    @Test
    fun `items_areScopedToCurrentUser`() = runTest {
        val repo = FakeTodoRepository()
        val session = FakeSessionRepository()
        repo.seedItem(1L, TodoItem(id = 1, title = "user1 item", isDone = false, createdAt = 1000L))
        repo.seedItem(2L, TodoItem(id = 2, title = "user2 item", isDone = false, createdAt = 2000L))

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("user1 item", vm.uiState.value.items[0].title)
    }

    @Test
    fun `isLimitReached_true_whenAt50Items`() = runTest {
        val repo = FakeTodoRepository()
        val session = FakeSessionRepository()
        repeat(TodoLimits.MAX_ITEMS) { i ->
            repo.seedItem(1L, TodoItem(id = i.toLong() + 1, title = "item $i", isDone = false, createdAt = i.toLong()))
        }

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isLimitReached)
    }

    @Test
    fun `onNewItemTitleChange_tooLong_setsTitleError`() = runTest {
        val vm = makeViewModel()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onNewItemTitleChange("a".repeat(TodoLimits.MAX_TITLE_LENGTH + 1))
        advanceUntilIdle()

        assertEquals("Name is too long (max 100 characters)", vm.uiState.value.titleError)
    }

    @Test
    fun `onNewItemTitleChange_withinLimit_clearsTitleError`() = runTest {
        val vm = makeViewModel()
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.onNewItemTitleChange("a".repeat(TodoLimits.MAX_TITLE_LENGTH + 1))
        advanceUntilIdle()
        vm.onNewItemTitleChange("valid title")
        advanceUntilIdle()

        assertNull(vm.uiState.value.titleError)
    }

    @Test
    fun `onAdd_blankTitle_setsTitleError`() = runTest {
        val session = FakeSessionRepository()
        val vm = makeViewModel(session = session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onNewItemTitleChange("   ")
        vm.onAdd()
        advanceUntilIdle()

        assertEquals("Name cannot be empty", vm.uiState.value.titleError)
    }

    @Test
    fun `onAdd_atLimit_setsFormError`() = runTest {
        val repo = FakeTodoRepository()
        val session = FakeSessionRepository()
        repeat(TodoLimits.MAX_ITEMS) { i ->
            repo.seedItem(1L, TodoItem(id = i.toLong() + 1, title = "item $i", isDone = false, createdAt = i.toLong()))
        }

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onNewItemTitleChange("new item")
        vm.onAdd()
        advanceUntilIdle()

        assertEquals("List is full (max 50 items)", vm.uiState.value.formError)
    }

    @Test
    fun `onAdd_success_clearsInputAndErrors`() = runTest {
        val session = FakeSessionRepository()
        val vm = makeViewModel(session = session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onNewItemTitleChange("valid task")
        vm.onAdd()
        advanceUntilIdle()

        assertEquals("", vm.uiState.value.newItemTitle)
        assertNull(vm.uiState.value.titleError)
        assertNull(vm.uiState.value.formError)
    }

    @Test
    fun `onEdit_wrongUser_noErrorSet`() = runTest {
        val repo = FakeTodoRepository()
        val session = FakeSessionRepository()
        repo.seedItem(2L, TodoItem(id = 1, title = "other user item", isDone = false, createdAt = 1000L))

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onEdit(id = 1L, newTitle = "hacked")
        advanceUntilIdle()

        assertNull(vm.uiState.value.titleError)
        assertNull(vm.uiState.value.formError)
    }

    @Test
    fun `onToggle_wrongUser_noErrorSet`() = runTest {
        val repo = FakeTodoRepository()
        val session = FakeSessionRepository()
        val item = TodoItem(id = 1, title = "other user item", isDone = false, createdAt = 1000L)
        repo.seedItem(2L, item)

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onToggle(item)
        advanceUntilIdle()

        assertNull(vm.uiState.value.titleError)
        assertNull(vm.uiState.value.formError)
    }

    @Test
    fun `onToggleActiveOnly_hidesCompletedItemsInUiState`() = runTest {
        val repo = FakeTodoRepository()
        val session = FakeSessionRepository()
        repo.seedItem(1L, TodoItem(id = 1, title = "active", isDone = false, createdAt = 1L))
        repo.seedItem(1L, TodoItem(id = 2, title = "done", isDone = true, createdAt = 2L))

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onToggleActiveOnly()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("active", vm.uiState.value.items[0].title)
        assertTrue(vm.uiState.value.showActiveOnly)
    }

    @Test
    fun `onToggleActiveOnly_toggledOff_showsAllItemsAgain`() = runTest {
        val repo = FakeTodoRepository()
        val session = FakeSessionRepository()
        repo.seedItem(1L, TodoItem(id = 1, title = "active", isDone = false, createdAt = 1L))
        repo.seedItem(1L, TodoItem(id = 2, title = "done", isDone = true, createdAt = 2L))

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onToggleActiveOnly()
        advanceUntilIdle()
        vm.onToggleActiveOnly()
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.items.size)
        assertFalse(vm.uiState.value.showActiveOnly)
    }

    @Test
    fun `onClearCompleted_removesAllDoneItemsFromState`() = runTest {
        val repo = FakeTodoRepository()
        val session = FakeSessionRepository()
        repo.seedItem(1L, TodoItem(id = 1, title = "active", isDone = false, createdAt = 1L))
        repo.seedItem(1L, TodoItem(id = 2, title = "done1", isDone = true, createdAt = 2L))
        repo.seedItem(1L, TodoItem(id = 3, title = "done2", isDone = true, createdAt = 3L))

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onClearCompleted()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("active", vm.uiState.value.items[0].title)
        assertEquals(0, vm.uiState.value.completedCount)
    }

    @Test
    fun `onClearCompleted_noCompletedItems_noOpSafely`() = runTest {
        val repo = FakeTodoRepository()
        val session = FakeSessionRepository()
        repo.seedItem(1L, TodoItem(id = 1, title = "active", isDone = false, createdAt = 1L))

        val vm = makeViewModel(repo, session)
        backgroundScope.launch { vm.uiState.collect {} }
        session.setSession(1L)
        advanceUntilIdle()

        vm.onClearCompleted()
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("active", vm.uiState.value.items[0].title)
    }
}
