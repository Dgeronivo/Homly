package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.repository.TodoRepository

class ToggleTodoItemUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(id: Long, userId: Long, isDone: Boolean): Result<Unit> = TODO()
}
