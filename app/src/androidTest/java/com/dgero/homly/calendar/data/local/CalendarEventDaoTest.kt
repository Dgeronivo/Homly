package com.dgero.homly.calendar.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dgero.homly.core.data.HomlyDatabase
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarEventDaoTest {

    private lateinit var database: HomlyDatabase
    private lateinit var dao: CalendarEventDao

    private val june1 = LocalDate.of(2026, 6, 1)
    private val june15 = LocalDate.of(2026, 6, 15)
    private val june30 = LocalDate.of(2026, 6, 30)
    private val july1 = LocalDate.of(2026, 7, 1)

    private fun event(
        userId: Long,
        date: LocalDate,
        title: String = "Event",
    ) = CalendarEventEntity(
        userId = userId,
        title = title,
        date = date,
        isAllDay = true,
        startTime = null,
        endTime = null,
    )

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomlyDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.calendarEventDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetEventsForRange_returnsInsertedEvent() = runTest {
        dao.insert(event(userId = 1, date = june15, title = "Doctor visit"))

        val events = dao.getEventsForRange(1, june1, june30)

        assertEquals(1, events.size)
        assertEquals("Doctor visit", events[0].title)
    }

    @Test
    fun getEventsForRange_excludesEventsOutsideRange() = runTest {
        dao.insert(event(userId = 1, date = june15, title = "In range"))
        dao.insert(event(userId = 1, date = july1, title = "Out of range"))

        val events = dao.getEventsForRange(1, june1, june30)

        assertEquals(listOf("In range"), events.map { it.title })
    }

    @Test
    fun getEventsForRange_doesNotLeakOtherUsersEvents() = runTest {
        dao.insert(event(userId = 1, date = june15, title = "User 1 event"))
        dao.insert(event(userId = 2, date = june15, title = "User 2 event"))

        val user1Events = dao.getEventsForRange(1, june1, june30)
        val user2Events = dao.getEventsForRange(2, june1, june30)

        assertEquals(listOf("User 1 event"), user1Events.map { it.title })
        assertTrue(user1Events.none { it.userId == 2L })
        assertEquals(listOf("User 2 event"), user2Events.map { it.title })
        assertTrue(user2Events.none { it.userId == 1L })
    }

    @Test
    fun getEventCount_countsOnlyThatUser() = runTest {
        dao.insert(event(userId = 1, date = june1))
        dao.insert(event(userId = 1, date = june15))
        dao.insert(event(userId = 2, date = june1))

        assertEquals(2, dao.getEventCount(1))
        assertEquals(1, dao.getEventCount(2))
    }

    @Test
    fun update_changesEventFields() = runTest {
        val id = dao.insert(event(userId = 1, date = june1, title = "Old title"))
        val inserted = dao.getEventsForRange(1, june1, june30).single()

        dao.update(inserted.copy(id = id, title = "New title", date = june15))

        val updated = dao.getEventsForRange(1, june1, june30).single()
        assertEquals("New title", updated.title)
        assertEquals(june15, updated.date)
    }

    @Test
    fun delete_removesEvent() = runTest {
        val id = dao.insert(event(userId = 1, date = june1))
        val inserted = dao.getEventsForRange(1, june1, june30).single()

        dao.delete(inserted.copy(id = id))

        assertEquals(0, dao.getEventsForRange(1, june1, june30).size)
    }
}
