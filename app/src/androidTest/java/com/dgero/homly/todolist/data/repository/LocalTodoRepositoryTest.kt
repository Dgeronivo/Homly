package com.dgero.homly.todolist.data.repository

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dgero.homly.auth.data.repository.TransactionRunner
import com.dgero.homly.core.data.HomlyDatabase
import com.dgero.homly.todolist.domain.error.TodoError
import com.dgero.homly.todolist.domain.model.TodoLimits
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalTodoRepositoryTest {

    private lateinit var database: HomlyDatabase
    private lateinit var repository: LocalTodoRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomlyDatabase::class.java,
        ).build()
        val runner = object : TransactionRunner {
            override suspend fun <T> invoke(block: suspend () -> T): T = database.withTransaction { block() }
        }
        repository = LocalTodoRepository(database.todoItemDao(), runner)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun add_isScopedPerUser() = runTest {
        repository.add(1, "Task A")
        repository.add(2, "Task B")

        val user1 = repository.getItems(1)
        val user2 = repository.getItems(2)
        assertEquals(listOf("Task A"), user1.map { it.title })
        assertEquals(listOf("Task B"), user2.map { it.title })
    }

    @Test
    fun add_atLimit_returnsLimitReachedAndDoesNotInsert() = runTest {
        repeat(TodoLimits.MAX_ITEMS) { i ->
            assertTrue(repository.add(1, "item $i").isSuccess)
        }

        val result = repository.add(1, "overflow")

        assertEquals(TodoError.LimitReached, result.exceptionOrNull())
        assertEquals(TodoLimits.MAX_ITEMS, repository.getItems(1).first().size)
    }

    @Test
    fun add_limitIsPerUser() = runTest {
        repeat(TodoLimits.MAX_ITEMS) { i ->
            repository.add(1, "item $i")
        }

        val result = repository.add(2, "Task B")
        assertTrue(result.isSuccess)
    }

    @Test
    fun editTitle_withCorrectUser_changesTitle() = runTest {
        val added = repository.add(1, "Old title").getOrThrow()

        repository.editTitle(added.id, 1, "New title")

        val item = repository.getItems(1).first().single()
        assertEquals("New title", item.title)
        assertEquals(added.createdAt, item.createdAt)
        assertEquals(false, item.isDone)
    }

    @Test
    fun editTitle_withWrongUser_returnsUnauthorized() = runTest {
        val added = repository.add(1, "My task").getOrThrow()

        val result = repository.editTitle(added.id, 2, "Hacked")

        assertEquals(TodoError.Unauthorized, result.exceptionOrNull())
        assertEquals("My task", repository.getItems(1).first().single().title)
    }

    @Test
    fun toggleDone_withCorrectUser_updatesFlag() = runTest {
        val added = repository.add(1, "Task").getOrThrow()

        repository.toggleDone(added.id, 1, true)

        assertTrue(repository.getItems(1).first().single().isDone)
    }

    @Test
    fun toggleDone_withWrongUser_returnsUnauthorized() = runTest {
        val added = repository.add(1, "Task").getOrThrow()

        val result = repository.toggleDone(added.id, 2, true)

        assertEquals(TodoError.Unauthorized, result.exceptionOrNull())
        assertEquals(false, repository.getItems(1).first().single().isDone)
    }

    @Test
    fun delete_withCorrectUser_removesItem() = runTest {
        val added = repository.add(1, "Task").getOrThrow()

        repository.delete(added.id, 1)

        assertTrue(repository.getItems(1).first().isEmpty())
    }

    @Test
    fun delete_withWrongUser_returnsUnauthorized() = runTest {
        val added = repository.add(1, "Task").getOrThrow()

        val result = repository.delete(added.id, 2)

        assertEquals(TodoError.Unauthorized, result.exceptionOrNull())
        assertEquals(1, repository.getItems(1).first().size)
    }
}
