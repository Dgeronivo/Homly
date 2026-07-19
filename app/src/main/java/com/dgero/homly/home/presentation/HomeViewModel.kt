package com.dgero.homly.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.auth.domain.repository.UserRepository
import com.dgero.homly.auth.domain.usecase.LogoutUseCase
import com.dgero.homly.calendar.domain.usecase.GetEventsUseCase
import com.dgero.homly.shopping.domain.model.ShoppingSortOrder
import com.dgero.homly.shopping.domain.usecase.ObserveShoppingItemsUseCase
import com.dgero.homly.todolist.domain.usecase.GetTodoItemsUseCase
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val getEventsUseCase: GetEventsUseCase,
    observeShoppingItems: ObserveShoppingItemsUseCase,
    private val getTodoItems: GetTodoItemsUseCase,
) : ViewModel() {

    val login = sessionRepository.currentUserId
        .map { userId -> userId?.let { userRepository.getUserById(it)?.login } ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private var userId: Long? = null

    private val _todayEventsCount = MutableStateFlow(0)
    val todayEventsCount: StateFlow<Int> = _todayEventsCount

    private val _todoPendingCount = MutableStateFlow(0)
    val todoPendingCount: StateFlow<Int> = _todoPendingCount

    val shoppingActiveCount: StateFlow<Int> = sessionRepository.currentUserId
        .filterNotNull()
        .flatMapLatest { uid -> observeShoppingItems(uid, ShoppingSortOrder.DATE_DESC) }
        .map { items -> items.count { !it.isBought } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId.filterNotNull().first()
            userId = uid
            loadSuspendCounts(uid)
        }
    }

    /**
     * Re-fetches the suspend-sourced summary counts (today's events, pending todos) — called
     * when [HomeScreen] resumes (e.g. returning from Calendar/Todo after changes), since this
     * ViewModel instance survives that round-trip and would otherwise keep showing stale data.
     * `shoppingActiveCount` doesn't need this: it's backed by a reactive [Flow] that updates
     * itself.
     */
    fun refresh() {
        val uid = userId ?: return
        viewModelScope.launch { loadSuspendCounts(uid) }
    }

    private suspend fun loadSuspendCounts(uid: Long) {
        val monthEvents = getEventsUseCase(uid, YearMonth.now())
        _todayEventsCount.value = GetEventsUseCase.forDay(monthEvents, LocalDate.now()).size
        _todoPendingCount.value = getTodoItems(uid).count { !it.isDone }
    }

    fun onLogout(onDone: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onDone()
        }
    }

    class Factory(
        private val logoutUseCase: LogoutUseCase,
        private val userRepository: UserRepository,
        private val sessionRepository: SessionRepository,
        private val getEventsUseCase: GetEventsUseCase,
        private val observeShoppingItems: ObserveShoppingItemsUseCase,
        private val getTodoItems: GetTodoItemsUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(
                logoutUseCase = logoutUseCase,
                userRepository = userRepository,
                sessionRepository = sessionRepository,
                getEventsUseCase = getEventsUseCase,
                observeShoppingItems = observeShoppingItems,
                getTodoItems = getTodoItems,
            ) as T
    }
}
