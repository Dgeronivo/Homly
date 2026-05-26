package com.dgero.homly.auth.data.repository

import com.dgero.homly.auth.data.crypto.PasswordHasher
import com.dgero.homly.auth.data.local.UserDao
import com.dgero.homly.auth.data.local.UserEntity
import com.dgero.homly.auth.domain.model.AuthError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ---------------------------------------------------------------------------
// Fakes
// ---------------------------------------------------------------------------

private class FakeUserDao : UserDao {
    private val users = mutableListOf<UserEntity>()
    private var nextId = 1L

    override suspend fun insert(user: UserEntity): Long {
        val id = nextId++
        users.add(user.copy(id = id))
        return id
    }

    override suspend fun findByLogin(login: String): List<UserEntity> =
        users.filter { it.login == login }

    override suspend fun findById(id: Long): UserEntity? =
        users.firstOrNull { it.id == id }
}

/** Fake hasher: hash = "hash:$password:$salt", verify checks equality. */
private class FakePasswordHasher : PasswordHasher {
    private var saltCounter = 0

    override fun generateSalt(): String = "salt${saltCounter++}"

    override fun hash(password: String, salt: String): String = "hash:$password:$salt"

    override fun verify(password: String, hash: String, salt: String): Boolean =
        hash == "hash:$password:$salt"
}

private class NoOpTransactionRunner : TransactionRunner {
    override suspend fun <T> invoke(block: suspend () -> T): T = block()
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

class LocalUserRepositoryTest {

    private lateinit var fakeDao: FakeUserDao
    private lateinit var fakeHasher: FakePasswordHasher
    private lateinit var repository: LocalUserRepository

    @Before
    fun setUp() {
        fakeDao = FakeUserDao()
        fakeHasher = FakePasswordHasher()
        repository = LocalUserRepository(fakeDao, fakeHasher, NoOpTransactionRunner())
    }

    @Test
    fun `register happy path returns success with correct login`() = runTest {
        val result = repository.register("alice", "pass1234")
        assertTrue(result.isSuccess)
        assertEquals("alice", result.getOrThrow().login)
    }

    @Test
    fun `register duplicate same login and same password returns DuplicateAccount failure`() = runTest {
        repository.register("alice", "pass1234")
        val result = repository.register("alice", "pass1234")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AuthError.DuplicateAccount)
    }

    @Test
    fun `register same login with different password is allowed`() = runTest {
        repository.register("alice", "pass1234")
        val result = repository.register("alice", "differentPass")
        assertTrue(result.isSuccess)
        assertEquals("alice", result.getOrThrow().login)
    }

    @Test
    fun `login with correct credentials returns success`() = runTest {
        repository.register("alice", "pass1234")
        val result = repository.login("alice", "pass1234")
        assertTrue(result.isSuccess)
        assertEquals("alice", result.getOrThrow().login)
    }

    @Test
    fun `login with wrong password returns InvalidCredentials failure`() = runTest {
        repository.register("alice", "pass1234")
        val result = repository.login("alice", "wrongpass")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AuthError.InvalidCredentials)
    }

    @Test
    fun `login with unknown user returns InvalidCredentials failure`() = runTest {
        val result = repository.login("nobody", "pass1234")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AuthError.InvalidCredentials)
    }
}
