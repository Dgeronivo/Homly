package com.dgero.homly.todolist.domain.repository

import com.dgero.homly.todolist.domain.model.TodoItem

interface TodoRepository {
    suspend fun getItems(userId: Long): List<TodoItem>
    suspend fun add(userId: Long, title: String): Result<TodoItem>
    suspend fun editTitle(id: Long, userId: Long, title: String): Result<Unit>
    suspend fun toggleDone(id: Long, userId: Long, isDone: Boolean): Result<Unit>
    suspend fun delete(id: Long, userId: Long): Result<Unit>
}
