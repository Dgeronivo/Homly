package com.dgero.homly

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import com.dgero.homly.auth.data.crypto.Pbkdf2PasswordHasher
import com.dgero.homly.auth.data.repository.LocalUserRepository
import com.dgero.homly.auth.data.repository.TransactionRunner
import com.dgero.homly.auth.data.session.DataStoreSessionRepository
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.auth.domain.repository.UserRepository
import com.dgero.homly.core.data.HomlyDatabase

class HomlyApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(context: Context) {
    val db: HomlyDatabase = Room.databaseBuilder(
        context, HomlyDatabase::class.java, "homly.db"
    ).build()

    val sessionRepository: SessionRepository = DataStoreSessionRepository(context)

    val userRepository: UserRepository = LocalUserRepository(
        userDao = db.userDao(),
        passwordHasher = Pbkdf2PasswordHasher(),
        runTransaction = object : TransactionRunner {
            override suspend fun <T> invoke(block: suspend () -> T): T = db.withTransaction { block() }
        },
    )
}
