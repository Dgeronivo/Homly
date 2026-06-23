package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.error.TodoError
import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.domain.model.TodoLimits
import com.dgero.homly.todolist.domain.validation.TodoTitleValidator
import com.dgero.homly.todolist.fake.FakeTodoRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddTodoItemUseCaseTest {

    private val userId = 1L
    private val repo = FakeTodoRepository()
    private val useCase = AddTodoItemUseCase(repo, TodoTitleValidator)

    @Test
    fun `blankTitle_returnsEmptyTitle`() = runTest {
        val result = useCase(userId, "   ")
        assertEquals(TodoError.EmptyTitle, result.exceptionOrNull())
    }

    @Test
    fun `titleTooLong_returnsTitleTooLong`() = runTest {
        val longTitle = "a".repeat(TodoLimits.MAX_TITLE_LENGTH + 1)
        val result = useCase(userId, longTitle)
        assertEquals(TodoError.TitleTooLong, result.exceptionOrNull())
    }

    @Test
    fun `titleAtMaxLength_succeeds`() = runTest {
        val maxTitle = "a".repeat(TodoLimits.MAX_TITLE_LENGTH)
        val result = useCase(userId, maxTitle)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `atLimit_returnsLimitReached`() = runTest {
        val fullRepo = FakeTodoRepository()
        val uc = AddTodoItemUseCase(fullRepo, TodoTitleValidator)
        repeat(TodoLimits.MAX_ITEMS) { i -> uc(userId, "item $i") }

        val result = uc(userId, "one more")
        assertEquals(TodoError.LimitReached, result.exceptionOrNull())
    }

    @Test
    fun `validTitle_isTrimmedAndStored`() = runTest {
        val result = useCase(userId, "  hello  ")
        assertTrue(result.isSuccess)
        assertEquals("hello", result.getOrThrow().title)
    }
}
