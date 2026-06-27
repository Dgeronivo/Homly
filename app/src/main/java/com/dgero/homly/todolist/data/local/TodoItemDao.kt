package com.dgero.homly.todolist.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
@Dao
interface TodoItemDao {
    @Query("SELECT * FROM todo_items WHERE userId = :userId")
    suspend fun getByUser(userId: Long): List<TodoItemEntity>

    @Query("SELECT COUNT(*) FROM todo_items WHERE userId = :userId")
    suspend fun countByUser(userId: Long): Int

    @Insert
    suspend fun insert(entity: TodoItemEntity): Long

    @Query("UPDATE todo_items SET title = :title WHERE id = :id AND userId = :userId")
    suspend fun updateTitle(id: Long, userId: Long, title: String): Int

    @Query("UPDATE todo_items SET isDone = :isDone WHERE id = :id AND userId = :userId")
    suspend fun updateDone(id: Long, userId: Long, isDone: Boolean): Int

    @Query("DELETE FROM todo_items WHERE id = :id AND userId = :userId")
    suspend fun deleteById(id: Long, userId: Long): Int
}
