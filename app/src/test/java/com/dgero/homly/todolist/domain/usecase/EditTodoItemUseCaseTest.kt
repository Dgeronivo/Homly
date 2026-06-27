package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.error.TodoError
import com.dgero.homly.todolist.domain.model.TodoLimits
import com.dgero.homly.todolist.domain.validation.TodoTitleValidator
import com.dgero.homly.todolist.fake.FakeTodoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EditTodoItemUseCaseTest {

    private val userId = 1L
    private val repo = FakeTodoRepository()
    private val addUseCase = AddTodoItemUseCase(repo, TodoTitleValidator)
    private val useCase = EditTodoItemUseCase(repo, TodoTitleValidator)

    @Test
    fun `blankTitle_returnsEmptyTitle`() = runTest {
        val item = addUseCase(userId, "original").getOrThrow()
        val result = useCase(item.id, userId, "   ")
        assertEquals(TodoError.EmptyTitle, result.exceptionOrNull())
    }

    @Test
    fun `titleTooLong_returnsTitleTooLong`() = runTest {
        val item = addUseCase(userId, "original").getOrThrow()
        val longTitle = "a".repeat(TodoLimits.MAX_TITLE_LENGTH + 1)
        val result = useCase(item.id, userId, longTitle)
        assertEquals(TodoError.TitleTooLong, result.exceptionOrNull())
    }

    @Test
    fun `validTitle_updatesItem`() = runTest {
        val item = addUseCase(userId, "original").getOrThrow()
        val result = useCase(item.id, userId, "  updated  ")
        assertEquals(Result.success(Unit), result)
        val stored = repo.getItems(userId).first { it.id == item.id }
        assertEquals("updated", stored.title)
    }

    @Test
    fun `wrongUser_returnsUnauthorized`() = runTest {
        val item = addUseCase(userId, "original").getOrThrow()
        val result = useCase(item.id, userId = 99L, "new title")
        assertEquals(TodoError.Unauthorized, result.exceptionOrNull())
    }
}
