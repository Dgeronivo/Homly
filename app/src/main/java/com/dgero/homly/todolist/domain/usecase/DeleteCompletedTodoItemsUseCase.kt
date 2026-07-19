package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.repository.TodoRepository

class DeleteCompletedTodoItemsUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(userId: Long): Result<Int> = repository.deleteCompleted(userId)
}
