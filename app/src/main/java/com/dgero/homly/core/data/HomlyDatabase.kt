package com.dgero.homly.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dgero.homly.auth.data.local.UserDao
import com.dgero.homly.auth.data.local.UserEntity
import com.dgero.homly.calendar.data.local.CalendarEventDao
import com.dgero.homly.calendar.data.local.CalendarEventEntity
import com.dgero.homly.calendar.data.local.DateTimeConverters
import com.dgero.homly.shopping.data.local.ShoppingItemDao
import com.dgero.homly.shopping.data.local.ShoppingItemEntity
import com.dgero.homly.todolist.data.local.TodoItemDao
import com.dgero.homly.todolist.data.local.TodoItemEntity

@Database(
    entities = [UserEntity::class, ShoppingItemEntity::class, TodoItemEntity::class, CalendarEventEntity::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(DateTimeConverters::class)
abstract class HomlyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun todoItemDao(): TodoItemDao
    abstract fun calendarEventDao(): CalendarEventDao
}
