package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.fake.FakeTodoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTodoItemsUseCaseTest {

    private val userId = 1L

    // 10 items seeded in shuffled order — verifies:
    //   • not-done items come before done items (AC-03)
    //   • within not-done: newer createdAt first (AC-03)
    //   • within done: newer createdAt first (AC-03)
    @Test
    fun `items are sorted not-done first then by createdAt descending`() = runTest {
        val repo = FakeTodoRepository()

        // Seed in deliberately shuffled order to prevent accidental pass
        repo.seedItem(userId, TodoItem(id = 3,  title = "nd-3", isDone = false, createdAt = 300))
        repo.seedItem(userId, TodoItem(id = 8,  title = "d-3",  isDone = true,  createdAt = 30))
        repo.seedItem(userId, TodoItem(id = 1,  title = "nd-1", isDone = false, createdAt = 100))
        repo.seedItem(userId, TodoItem(id = 6,  title = "d-1",  isDone = true,  createdAt = 10))
        repo.seedItem(userId, TodoItem(id = 5,  title = "nd-5", isDone = false, createdAt = 500))
        repo.seedItem(userId, TodoItem(id = 10, title = "d-5",  isDone = true,  createdAt = 50))
        repo.seedItem(userId, TodoItem(id = 2,  title = "nd-2", isDone = false, createdAt = 200))
        repo.seedItem(userId, TodoItem(id = 9,  title = "d-4",  isDone = true,  createdAt = 40))
        repo.seedItem(userId, TodoItem(id = 4,  title = "nd-4", isDone = false, createdAt = 400))
        repo.seedItem(userId, TodoItem(id = 7,  title = "d-2",  isDone = true,  createdAt = 20))

        val result = GetTodoItemsUseCase(repo)(userId)

        val expected = listOf("nd-5", "nd-4", "nd-3", "nd-2", "nd-1", "d-5", "d-4", "d-3", "d-2", "d-1")
        assertEquals(expected, result.map { it.title })
    }
}
