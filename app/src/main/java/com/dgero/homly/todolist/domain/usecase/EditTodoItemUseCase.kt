package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.repository.TodoRepository
import com.dgero.homly.todolist.domain.validation.TodoTitleValidator

class EditTodoItemUseCase(
    private val repository: TodoRepository,
    private val validator: TodoTitleValidator,
) {
    suspend operator fun invoke(id: Long, userId: Long, title: String): Result<Unit> {
        val error = validator.validate(title)
        if (error != null) return Result.failure(error)
        return repository.editTitle(id, userId, title.trim())
    }
}
