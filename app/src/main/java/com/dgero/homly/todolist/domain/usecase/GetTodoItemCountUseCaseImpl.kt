package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.repository.TodoRepository
import com.dgero.homly.todolist.domain.usecase.port.GetTodoItemCountUseCase

class GetTodoItemCountUseCaseImpl(
    private val repository: TodoRepository,
) : GetTodoItemCountUseCase {
    override suspend fun invoke(userId: Long): Int = repository.getItems(userId).size
}
