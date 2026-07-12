package com.dgero.homly.calendar.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.dgero.homly.core.data.HomlyDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CalendarMigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        HomlyDatabase::class.java,
    )

    @Test
    fun migrate3To4_preservesExistingTodoAndShoppingData() {
        helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                "INSERT INTO users (id, login, passwordHash, salt, createdAt) " +
                    "VALUES (1, 'alex', 'hash', 'salt', 1000)"
            )
            execSQL(
                "INSERT INTO todo_items (id, userId, title, isDone, createdAt) " +
                    "VALUES (1, 1, 'Buy milk', 0, 1000)"
            )
            execSQL(
                "INSERT INTO shopping_items (id, userId, name, isBought, createdAt) " +
                    "VALUES (1, 1, 'Bread', 0, 1000)"
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 4, true, HomlyDatabase.MIGRATION_3_4)

        migrated.query("SELECT title FROM todo_items WHERE id = 1").use { cursor ->
            assertEquals(1, cursor.count)
            assertEquals(true, cursor.moveToFirst())
            assertEquals("Buy milk", cursor.getString(0))
        }

        migrated.query("SELECT name FROM shopping_items WHERE id = 1").use { cursor ->
            assertEquals(1, cursor.count)
            assertEquals(true, cursor.moveToFirst())
            assertEquals("Bread", cursor.getString(0))
        }
    }

    @Test
    fun migrate3To4_createsCalendarEventsTable_insertAndReadRoundTrips() {
        helper.createDatabase(TEST_DB, 3).apply {
            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 4, true, HomlyDatabase.MIGRATION_3_4)

        migrated.execSQL(
            "INSERT INTO calendar_events (id, userId, title, date, isAllDay, startTime, endTime) " +
                "VALUES (1, 1, 'Doctor visit', 19000, 0, 32400, 36000)"
        )

        migrated.query("SELECT title, date, isAllDay, startTime, endTime FROM calendar_events WHERE id = 1")
            .use { cursor ->
                assertEquals(1, cursor.count)
                assertEquals(true, cursor.moveToFirst())
                assertEquals("Doctor visit", cursor.getString(0))
                assertEquals(19000L, cursor.getLong(1))
                assertEquals(0, cursor.getInt(2))
                assertEquals(32400, cursor.getInt(3))
                assertEquals(36000, cursor.getInt(4))
            }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
