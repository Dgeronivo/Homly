package com.dgero.homly.auth.data.repository

import com.dgero.homly.auth.data.crypto.PasswordHasher
import com.dgero.homly.auth.data.local.UserDao
import com.dgero.homly.auth.data.local.UserEntity
import com.dgero.homly.auth.domain.model.AuthError
import com.dgero.homly.auth.domain.model.User
import com.dgero.homly.auth.domain.repository.UserRepository

interface TransactionRunner {
    suspend operator fun <T> invoke(block: suspend () -> T): T
}

class LocalUserRepository(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
    private val runTransaction: TransactionRunner,
) : UserRepository {

    override suspend fun register(login: String, password: String): Result<User> = try {
        val user = runTransaction {
            val candidates = userDao.findByLogin(login)
            if (candidates.any { passwordHasher.verify(password, it.passwordHash, it.salt) }) {
                throw AuthError.DuplicateAccount
            }
            val salt = passwordHasher.generateSalt()
            val hash = passwordHasher.hash(password, salt)
            val id = userDao.insert(UserEntity(login = login, passwordHash = hash, salt = salt))
            User(id = id, login = login)
        }
        Result.success(user)
    } catch (e: AuthError) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(AuthError.Unknown(e))
    }

    override suspend fun login(login: String, password: String): Result<User> = try {
        val candidates = userDao.findByLogin(login)
        val match = candidates.firstOrNull { passwordHasher.verify(password, it.passwordHash, it.salt) }
            ?: throw AuthError.InvalidCredentials
        Result.success(User(id = match.id, login = login))
    } catch (e: AuthError) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(AuthError.Unknown(e))
    }

    override suspend fun getUserById(id: Long): User? {
        val entity = userDao.findById(id) ?: return null
        return User(id = entity.id, login = entity.login)
    }
}
