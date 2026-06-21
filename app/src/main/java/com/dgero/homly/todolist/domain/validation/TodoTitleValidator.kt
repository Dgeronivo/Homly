package com.dgero.homly.todolist.domain.validation

import com.dgero.homly.todolist.domain.error.TodoError
import com.dgero.homly.todolist.domain.model.TodoLimits

object TodoTitleValidator {

    fun validateMaxLength(title: String): TodoError? =
        if (isTooLong(title.trim())) TodoError.TitleTooLong else null

    fun validate(title: String): TodoError? {
        val trimmed = title.trim()
        return when {
            trimmed.isBlank() -> TodoError.EmptyTitle
            isTooLong(trimmed) -> TodoError.TitleTooLong
            else -> null
        }
    }

    private fun isTooLong(title: String) = title.length > TodoLimits.MAX_TITLE_LENGTH
}
