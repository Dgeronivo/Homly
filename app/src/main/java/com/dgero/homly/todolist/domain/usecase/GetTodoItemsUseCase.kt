package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.domain.repository.TodoRepository

class GetTodoItemsUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(userId: Long): List<TodoItem> =
        repository.getItems(userId)
            .sortedWith(compareBy<TodoItem> { it.isDone }.thenByDescending { it.createdAt })
}
