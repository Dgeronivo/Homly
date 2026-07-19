package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.fake.FakeTodoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteCompletedTodoItemsUseCaseTest {

    private val userId = 1L
    private val repo = FakeTodoRepository()
    private val useCase = DeleteCompletedTodoItemsUseCase(repo)

    @Test
    fun `invoke_delegatesToRepository_andReturnsCount`() = runTest {
        repo.seedItem(userId, TodoItem(id = 1, title = "active", isDone = false, createdAt = 1L))
        repo.seedItem(userId, TodoItem(id = 2, title = "done1", isDone = true, createdAt = 2L))
        repo.seedItem(userId, TodoItem(id = 3, title = "done2", isDone = true, createdAt = 3L))

        val result = useCase(userId)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())
        val remaining = repo.getItems(userId)
        assertEquals(1, remaining.size)
        assertEquals("active", remaining[0].title)
    }
}
