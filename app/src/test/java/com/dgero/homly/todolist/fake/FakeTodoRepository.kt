package com.dgero.homly.todolist.fake

import com.dgero.homly.todolist.domain.error.TodoError
import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.domain.model.TodoLimits
import com.dgero.homly.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTodoRepository : TodoRepository {

    private val items = MutableStateFlow<Map<Long, TodoItem>>(emptyMap())
    private val owners = mutableMapOf<Long, Long>() // itemId → userId
    private var nextId = 1L

    override fun getItems(userId: Long): Flow<List<TodoItem>> =
        items.map { map -> map.values.filter { owners[it.id] == userId } }

    override suspend fun add(userId: Long, title: String): Result<TodoItem> {
        val userItemCount = owners.values.count { it == userId }
        if (userItemCount >= TodoLimits.MAX_ITEMS) return Result.failure(TodoError.LimitReached)
        val item = TodoItem(id = nextId++, title = title, isDone = false, createdAt = System.currentTimeMillis())
        items.value = items.value + (item.id to item)
        owners[item.id] = userId
        return Result.success(item)
    }

    override suspend fun editTitle(id: Long, userId: Long, title: String): Result<Unit> {
        if (owners[id] != userId) return Result.failure(TodoError.Unauthorized)
        val item = items.value[id] ?: return Result.failure(TodoError.Unauthorized)
        items.value = items.value + (id to item.copy(title = title))
        return Result.success(Unit)
    }

    override suspend fun toggleDone(id: Long, userId: Long, isDone: Boolean): Result<Unit> {
        if (owners[id] != userId) return Result.failure(TodoError.Unauthorized)
        val item = items.value[id] ?: return Result.failure(TodoError.Unauthorized)
        items.value = items.value + (id to item.copy(isDone = isDone))
        return Result.success(Unit)
    }

    override suspend fun delete(id: Long, userId: Long): Result<Unit> {
        if (owners[id] != userId) return Result.failure(TodoError.Unauthorized)
        items.value = items.value - id
        owners.remove(id)
        return Result.success(Unit)
    }

    /** Test helper: insert a pre-built item with exact field values (e.g. specific createdAt). */
    fun seedItem(userId: Long, item: TodoItem) {
        items.value = items.value + (item.id to item)
        owners[item.id] = userId
        if (item.id >= nextId) nextId = item.id + 1
    }
}
