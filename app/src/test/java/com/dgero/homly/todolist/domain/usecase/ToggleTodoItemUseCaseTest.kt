package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.error.TodoError
import com.dgero.homly.todolist.domain.validation.TodoTitleValidator
import com.dgero.homly.todolist.fake.FakeTodoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleTodoItemUseCaseTest {

    private val userId = 1L
    private val repo = FakeTodoRepository()
    private val addUseCase = AddTodoItemUseCase(repo, TodoTitleValidator)
    private val useCase = ToggleTodoItemUseCase(repo)

    @Test
    fun `correctUser_togglesDone`() = runTest {
        val item = addUseCase(userId, "task").getOrThrow()
        val result = useCase(item.id, userId, true)
        assertTrue(result.isSuccess)
        val stored = repo.getItems(userId).first().first { it.id == item.id }
        assertEquals(true, stored.isDone)
    }

    @Test
    fun `wrongUser_returnsUnauthorized`() = runTest {
        val item = addUseCase(userId, "task").getOrThrow()
        val result = useCase(item.id, userId = 99L, true)
        assertEquals(TodoError.Unauthorized, result.exceptionOrNull())
    }
}
