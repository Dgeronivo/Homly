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
import com.dgero.homly.calendar.data.repository.LocalCalendarEventRepository
import com.dgero.homly.calendar.domain.repository.CalendarEventRepository
import com.dgero.homly.core.data.HomlyDatabase
import com.dgero.homly.shopping.data.repository.LocalShoppingRepository
import com.dgero.homly.shopping.domain.repository.ShoppingRepository
import com.dgero.homly.todolist.data.repository.LocalTodoRepository
import com.dgero.homly.todolist.domain.repository.TodoRepository

class HomlyApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(context: Context) {
    val db: HomlyDatabase = Room.databaseBuilder(
        context, HomlyDatabase::class.java, "homly.db"
    ).fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    val sessionRepository: SessionRepository = DataStoreSessionRepository(context)

    private val transactionRunner = object : TransactionRunner {
        override suspend fun <T> invoke(block: suspend () -> T): T = db.withTransaction { block() }
    }

    val userRepository: UserRepository = LocalUserRepository(
        userDao = db.userDao(),
        passwordHasher = Pbkdf2PasswordHasher(),
        runTransaction = transactionRunner,
    )

    val shoppingRepository: ShoppingRepository = LocalShoppingRepository(
        dao = db.shoppingItemDao(),
        runTransaction = transactionRunner,
    )

    val todoRepository: TodoRepository = LocalTodoRepository(
        dao = db.todoItemDao(),
        runTransaction = transactionRunner,
    )

    val calendarEventRepository: CalendarEventRepository = LocalCalendarEventRepository(
        dao = db.calendarEventDao(),
    )
}
