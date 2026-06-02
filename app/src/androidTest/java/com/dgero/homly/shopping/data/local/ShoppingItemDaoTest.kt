package com.dgero.homly.shopping.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dgero.homly.core.data.HomlyDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShoppingItemDaoTest {

    private lateinit var database: HomlyDatabase
    private lateinit var dao: ShoppingItemDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomlyDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.shoppingItemDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndObserve_returnsItemForUser() = runTest {
        dao.insert(ShoppingItemEntity(userId = 1, name = "Milk"))

        val items = dao.observeByUser(1).first()
        assertEquals(1, items.size)
        assertEquals("Milk", items[0].name)
    }

    @Test
    fun observeByUser_doesNotLeakOtherUsersItems() = runTest {
        dao.insert(ShoppingItemEntity(userId = 1, name = "Milk"))
        dao.insert(ShoppingItemEntity(userId = 2, name = "Bread"))

        val user1 = dao.observeByUser(1).first()
        val user2 = dao.observeByUser(2).first()
        assertEquals(listOf("Milk"), user1.map { it.name })
        assertEquals(listOf("Bread"), user2.map { it.name })
    }

    @Test
    fun countByUser_countsOnlyThatUser() = runTest {
        dao.insert(ShoppingItemEntity(userId = 1, name = "Milk"))
        dao.insert(ShoppingItemEntity(userId = 1, name = "Eggs"))
        dao.insert(ShoppingItemEntity(userId = 2, name = "Bread"))

        assertEquals(2, dao.countByUser(1))
        assertEquals(1, dao.countByUser(2))
    }

    @Test
    fun updateName_isReflectedInFlow() = runTest {
        val id = dao.insert(ShoppingItemEntity(userId = 1, name = "Milk"))

        dao.updateName(id, "Almond milk")

        val item = dao.observeByUser(1).first().single()
        assertEquals("Almond milk", item.name)
    }

    @Test
    fun updateBought_isReflectedInFlow() = runTest {
        val id = dao.insert(ShoppingItemEntity(userId = 1, name = "Milk"))

        dao.updateBought(id, true)

        val item = dao.observeByUser(1).first().single()
        assertTrue(item.isBought)
    }

    @Test
    fun deleteById_removesItemFromFlow() = runTest {
        val id = dao.insert(ShoppingItemEntity(userId = 1, name = "Milk"))

        dao.deleteById(id)

        assertTrue(dao.observeByUser(1).first().isEmpty())
    }
}
