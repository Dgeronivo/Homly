package com.dgero.homly.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    exportSchema = true,
)
@TypeConverters(DateTimeConverters::class)
abstract class HomlyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun todoItemDao(): TodoItemDao
    abstract fun calendarEventDao(): CalendarEventDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `calendar_events` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`userId` INTEGER NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`date` INTEGER NOT NULL, " +
                        "`isAllDay` INTEGER NOT NULL, " +
                        "`startTime` INTEGER, " +
                        "`endTime` INTEGER)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_calendar_events_userId` ON `calendar_events` (`userId`)"
                )
            }
        }
    }
}
