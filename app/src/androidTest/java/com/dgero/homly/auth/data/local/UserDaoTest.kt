package com.dgero.homly.auth.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dgero.homly.core.data.HomlyDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var database: HomlyDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HomlyDatabase::class.java,
        ).allowMainThreadQueries().build()
        userDao = database.userDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndFindByLogin_returnsInsertedUser() = runTest {
        val entity = UserEntity(login = "alice", passwordHash = "hash", salt = "salt")
        userDao.insert(entity)

        val results = userDao.findByLogin("alice")
        assertEquals(1, results.size)
        assertEquals("alice", results[0].login)
        assertEquals("hash", results[0].passwordHash)
        assertEquals("salt", results[0].salt)
    }

    @Test
    fun insertAndFindById_returnsInsertedUser() = runTest {
        val entity = UserEntity(login = "bob", passwordHash = "hash2", salt = "salt2")
        val id = userDao.insert(entity)

        val found = userDao.findById(id)
        assertNotNull(found)
        assertEquals(id, found!!.id)
        assertEquals("bob", found.login)
    }

    @Test
    fun findByLoginWithNoMatch_returnsEmptyList() = runTest {
        val results = userDao.findByLogin("nonexistent")
        assertTrue(results.isEmpty())
    }

    @Test
    fun findByIdWithNoMatch_returnsNull() = runTest {
        val found = userDao.findById(999L)
        assertNull(found)
    }

    @Test
    fun twoUsersWithSameLogin_bothReturnedByFindByLogin() = runTest {
        val first = UserEntity(login = "charlie", passwordHash = "hash1", salt = "salt1")
        val second = UserEntity(login = "charlie", passwordHash = "hash2", salt = "salt2")
        userDao.insert(first)
        userDao.insert(second)

        val results = userDao.findByLogin("charlie")
        assertEquals(2, results.size)
    }
}
