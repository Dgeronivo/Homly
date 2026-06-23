package com.dgero.homly.todolist.domain.usecase

import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveTodoItemsUseCase(private val repository: TodoRepository) {
    operator fun invoke(userId: Long): Flow<List<TodoItem>> =
        repository.getItems(userId).map { items ->
            items.sortedWith(compareBy<TodoItem> { it.isDone }.thenByDescending { it.createdAt })
        }
}
