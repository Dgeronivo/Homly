package com.dgero.homly.todolist.presentation

import com.dgero.homly.todolist.domain.model.TodoItem

data class TodoListUiState(
    val items: List<TodoItem> = emptyList(),
    val newItemTitle: String = "",
    val isLimitReached: Boolean = false,
    val titleError: String? = null,
    val formError: String? = null,
)
