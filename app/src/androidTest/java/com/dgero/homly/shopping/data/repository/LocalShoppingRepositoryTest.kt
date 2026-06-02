package com.dgero.homly.shopping.data.repository

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dgero.homly.auth.data.repository.TransactionRunner
import com.dgero.homly.core.data.HomlyDatabase
import com.dgero.homly.shopping.domain.model.ShoppingError
import com.dgero.homly.shopping.domain.model.ShoppingLimits
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalShoppingRepositoryTest {

    private lateinit var database: HomlyDatabase
    private lateinit var repository: LocalShoppingRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomlyDatabase::class.java,
        ).build()
        val runner = object : TransactionRunner {
            override suspend fun <T> invoke(block: suspend () -> T): T = database.withTransaction { block() }
        }
        repository = LocalShoppingRepository(database.shoppingItemDao(), runner)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun add_isScopedPerUser() = runTest {
        repository.add(1, "Milk")
        repository.add(2, "Bread")

        val user1 = repository.observeItems(1).first()
        val user2 = repository.observeItems(2).first()
        assertEquals(listOf("Milk"), user1.map { it.name })
        assertEquals(listOf("Bread"), user2.map { it.name })
    }

    @Test
    fun add_atLimit_failsWithLimitReachedAndDoesNotInsert() = runTest {
        repeat(ShoppingLimits.MAX_ITEMS) { i ->
            assertTrue(repository.add(1, "item $i").isSuccess)
        }

        val result = repository.add(1, "overflow")

        assertEquals(ShoppingError.LimitReached, result.exceptionOrNull())
        assertEquals(ShoppingLimits.MAX_ITEMS, repository.observeItems(1).first().size)
    }

    @Test
    fun add_limitIsPerUser() = runTest {
        repeat(ShoppingLimits.MAX_ITEMS) { i ->
            repository.add(1, "item $i")
        }

        // User 1 is full, but user 2 can still add.
        val result = repository.add(2, "Bread")
        assertTrue(result.isSuccess)
    }

    @Test
    fun editName_changesOnlyName() = runTest {
        val added = repository.add(1, "Milk").getOrThrow()

        repository.editName(added.id, "Almond milk")

        val item = repository.observeItems(1).first().single()
        assertEquals("Almond milk", item.name)
        assertEquals(added.createdAt, item.createdAt)
        assertEquals(false, item.isBought)
    }

    @Test
    fun toggleBought_updatesFlag() = runTest {
        val added = repository.add(1, "Milk").getOrThrow()

        repository.toggleBought(added.id, true)

        assertTrue(repository.observeItems(1).first().single().isBought)
    }

    @Test
    fun delete_removesItem() = runTest {
        val added = repository.add(1, "Milk").getOrThrow()

        repository.delete(added.id)

        assertTrue(repository.observeItems(1).first().isEmpty())
    }
}
