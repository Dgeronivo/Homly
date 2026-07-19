package com.dgero.homly.todolist.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dgero.homly.core.data.HomlyDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodoItemDaoTest {

    private lateinit var database: HomlyDatabase
    private lateinit var dao: TodoItemDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomlyDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.todoItemDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGet_returnsItemsForUser() = runTest {
        dao.insert(TodoItemEntity(userId = 1, title = "Buy milk"))

        val items = dao.getByUser(1)
        assertEquals(1, items.size)
        assertEquals("Buy milk", items[0].title)
    }

    @Test
    fun getByUser_doesNotLeakOtherUsersItems() = runTest {
        dao.insert(TodoItemEntity(userId = 1, title = "Task A"))
        dao.insert(TodoItemEntity(userId = 2, title = "Task B"))

        val user1 = dao.getByUser(1)
        val user2 = dao.getByUser(2)
        assertEquals(listOf("Task A"), user1.map { it.title })
        assertEquals(listOf("Task B"), user2.map { it.title })
    }

    @Test
    fun countByUser_countsOnlyThatUser() = runTest {
        dao.insert(TodoItemEntity(userId = 1, title = "Task A"))
        dao.insert(TodoItemEntity(userId = 1, title = "Task B"))
        dao.insert(TodoItemEntity(userId = 2, title = "Task C"))

        assertEquals(2, dao.countByUser(1))
        assertEquals(1, dao.countByUser(2))
    }

    @Test
    fun updateTitle_withCorrectUser_returns1() = runTest {
        val id = dao.insert(TodoItemEntity(userId = 1, title = "Old title"))

        val rows = dao.updateTitle(id, 1, "New title")

        assertEquals(1, rows)
        assertEquals("New title", dao.getByUser(1).single().title)
    }

    @Test
    fun updateTitle_withWrongUser_returns0_noChange() = runTest {
        val id = dao.insert(TodoItemEntity(userId = 1, title = "Original"))

        val rows = dao.updateTitle(id, 2, "Hacked")

        assertEquals(0, rows)
        assertEquals("Original", dao.getByUser(1).single().title)
    }

    @Test
    fun updateDone_withCorrectUser_returns1() = runTest {
        val id = dao.insert(TodoItemEntity(userId = 1, title = "Task"))

        val rows = dao.updateDone(id, 1, true)

        assertEquals(1, rows)
        assertEquals(true, dao.getByUser(1).single().isDone)
    }

    @Test
    fun updateDone_withWrongUser_returns0() = runTest {
        val id = dao.insert(TodoItemEntity(userId = 1, title = "Task"))

        val rows = dao.updateDone(id, 2, true)

        assertEquals(0, rows)
        assertEquals(false, dao.getByUser(1).single().isDone)
    }

    @Test
    fun deleteById_withCorrectUser_returns1() = runTest {
        val id = dao.insert(TodoItemEntity(userId = 1, title = "Task"))

        val rows = dao.deleteById(id, 1)

        assertEquals(1, rows)
        assertEquals(0, dao.getByUser(1).size)
    }

    @Test
    fun deleteById_withWrongUser_returns0_noChange() = runTest {
        val id = dao.insert(TodoItemEntity(userId = 1, title = "Task"))

        val rows = dao.deleteById(id, 2)

        assertEquals(0, rows)
        assertEquals(1, dao.getByUser(1).size)
    }

    @Test
    fun deleteCompleted_removesOnlyDoneItemsForThatUser() = runTest {
        dao.insert(TodoItemEntity(userId = 1, title = "Done task", isDone = true))
        dao.insert(TodoItemEntity(userId = 1, title = "Active task", isDone = false))
        val otherUserDoneId = dao.insert(TodoItemEntity(userId = 2, title = "Other done", isDone = true))

        val rows = dao.deleteCompleted(1)

        assertEquals(1, rows)
        val remaining = dao.getByUser(1)
        assertEquals(listOf("Active task"), remaining.map { it.title })
        assertEquals(1, dao.getByUser(2).size)
        assertEquals(otherUserDoneId, dao.getByUser(2).single().id)
    }

    @Test
    fun deleteCompleted_noCompletedItems_returnsZeroAndDeletesNothing() = runTest {
        dao.insert(TodoItemEntity(userId = 1, title = "Active task", isDone = false))

        val rows = dao.deleteCompleted(1)

        assertEquals(0, rows)
        assertEquals(1, dao.getByUser(1).size)
    }
}
