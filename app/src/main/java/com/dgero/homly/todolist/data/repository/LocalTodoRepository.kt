package com.dgero.homly.todolist.data.repository

import com.dgero.homly.auth.data.repository.TransactionRunner
import com.dgero.homly.todolist.data.local.TodoItemDao
import com.dgero.homly.todolist.data.local.TodoItemEntity
import com.dgero.homly.todolist.domain.error.TodoError
import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.domain.model.TodoLimits
import com.dgero.homly.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalTodoRepository(
    private val dao: TodoItemDao,
    private val runTransaction: TransactionRunner,
) : TodoRepository {

    override fun getItems(userId: Long): Flow<List<TodoItem>> =
        dao.getByUser(userId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(userId: Long, title: String): Result<TodoItem> = try {
        val item = runTransaction {
            if (dao.countByUser(userId) >= TodoLimits.MAX_ITEMS) throw TodoError.LimitReached
            val entity = TodoItemEntity(userId = userId, title = title)
            val id = dao.insert(entity)
            entity.copy(id = id).toDomain()
        }
        Result.success(item)
    } catch (e: TodoError) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(TodoError.Unknown(e))
    }

    override suspend fun editTitle(id: Long, userId: Long, title: String): Result<Unit> =
        runMutation { dao.updateTitle(id, userId, title) }

    override suspend fun toggleDone(id: Long, userId: Long, isDone: Boolean): Result<Unit> =
        runMutation { dao.updateDone(id, userId, isDone) }

    override suspend fun delete(id: Long, userId: Long): Result<Unit> =
        runMutation { dao.deleteById(id, userId) }

    private suspend fun runMutation(block: suspend () -> Int): Result<Unit> = try {
        if (block() == 0) Result.failure(TodoError.Unauthorized)
        else Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(TodoError.Unknown(e))
    }

    private fun TodoItemEntity.toDomain() = TodoItem(
        id = id,
        title = title,
        isDone = isDone,
        createdAt = createdAt,
    )
}
