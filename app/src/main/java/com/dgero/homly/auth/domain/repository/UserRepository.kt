package com.dgero.homly.auth.domain.repository

import com.dgero.homly.auth.domain.model.User

interface UserRepository {
    suspend fun register(login: String, password: String): Result<User>
    suspend fun login(login: String, password: String): Result<User>
    suspend fun getUserById(id: Long): User?
}
