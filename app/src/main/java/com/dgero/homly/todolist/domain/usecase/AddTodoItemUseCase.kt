package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.domain.repository.TodoRepository
import com.dgero.homly.todolist.domain.validation.TodoTitleValidator

class AddTodoItemUseCase(
    private val repository: TodoRepository,
    private val validator: TodoTitleValidator,
) {
    suspend operator fun invoke(userId: Long, title: String): Result<TodoItem> {
        val error = validator.validate(title)
        if (error != null) return Result.failure(error)
        return repository.add(userId, title.trim())
    }
}
