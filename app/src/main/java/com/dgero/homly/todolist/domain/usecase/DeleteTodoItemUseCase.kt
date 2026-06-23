package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.repository.TodoRepository

class DeleteTodoItemUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(id: Long, userId: Long): Result<Unit> = TODO()
}
