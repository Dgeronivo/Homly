package com.dgero.homly.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dgero.homly.auth.data.local.UserDao
import com.dgero.homly.auth.data.local.UserEntity
import com.dgero.homly.shopping.data.local.ShoppingItemDao
import com.dgero.homly.shopping.data.local.ShoppingItemEntity
import com.dgero.homly.todolist.data.local.TodoItemDao
import com.dgero.homly.todolist.data.local.TodoItemEntity

@Database(entities = [UserEntity::class, ShoppingItemEntity::class, TodoItemEntity::class], version = 3, exportSchema = false)
abstract class HomlyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun todoItemDao(): TodoItemDao
}
