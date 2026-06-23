package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.error.TodoError
import com.dgero.homly.todolist.domain.validation.TodoTitleValidator
import com.dgero.homly.todolist.fake.FakeTodoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteTodoItemUseCaseTest {

    private val userId = 1L
    private val repo = FakeTodoRepository()
    private val addUseCase = AddTodoItemUseCase(repo, TodoTitleValidator)
    private val useCase = DeleteTodoItemUseCase(repo)

    @Test
    fun `correctUser_deletesItem`() = runTest {
        val item = addUseCase(userId, "task").getOrThrow()
        val result = useCase(item.id, userId)
        assertTrue(result.isSuccess)
        val items = repo.getItems(userId).first()
        assertTrue(items.none { it.id == item.id })
    }

    @Test
    fun `wrongUser_returnsUnauthorized`() = runTest {
        val item = addUseCase(userId, "task").getOrThrow()
        val result = useCase(item.id, userId = 99L)
        assertEquals(TodoError.Unauthorized, result.exceptionOrNull())
    }
}
