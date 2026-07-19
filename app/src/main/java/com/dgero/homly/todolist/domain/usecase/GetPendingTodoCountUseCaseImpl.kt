package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.repository.TodoRepository
import com.dgero.homly.todolist.domain.usecase.port.GetPendingTodoCountUseCase

class GetPendingTodoCountUseCaseImpl(
    private val repository: TodoRepository,
) : GetPendingTodoCountUseCase {
    override suspend fun invoke(userId: Long): Int =
        repository.getItems(userId).count { !it.isDone }
}
