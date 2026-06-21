package com.dgero.homly.todolist.domain.validation

import com.dgero.homly.todolist.domain.error.TodoError
import com.dgero.homly.todolist.domain.model.TodoLimits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TodoTitleValidatorTest {

    @Test
    fun validateMaxLength_tooLong_returnsTitleTooLong() {
        val title = "a".repeat(TodoLimits.MAX_TITLE_LENGTH + 1)
        assertEquals(TodoError.TitleTooLong, TodoTitleValidator.validateMaxLength(title))
    }

    @Test
    fun validateMaxLength_atMaxLength_returnsNull() {
        val title = "a".repeat(TodoLimits.MAX_TITLE_LENGTH)
        assertNull(TodoTitleValidator.validateMaxLength(title))
    }

    @Test
    fun validateMaxLength_blank_returnsNull() {
        assertNull(TodoTitleValidator.validateMaxLength("   "))
    }

    @Test
    fun validate_blank_returnsEmptyTitle() {
        assertEquals(TodoError.EmptyTitle, TodoTitleValidator.validate(""))
        assertEquals(TodoError.EmptyTitle, TodoTitleValidator.validate("   "))
    }

    @Test
    fun validate_tooLong_returnsTitleTooLong() {
        val title = "a".repeat(TodoLimits.MAX_TITLE_LENGTH + 1)
        assertEquals(TodoError.TitleTooLong, TodoTitleValidator.validate(title))
    }

    @Test
    fun validate_valid_returnsNull() {
        assertNull(TodoTitleValidator.validate("Buy milk"))
    }
}
