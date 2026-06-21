package com.dgero.homly.todolist.domain.model

data class TodoItem(
    val id: Long,
    val title: String,
    val isDone: Boolean,
    val createdAt: Long,
)
