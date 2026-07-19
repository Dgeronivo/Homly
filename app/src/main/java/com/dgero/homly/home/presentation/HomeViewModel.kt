package com.dgero.homly.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.auth.domain.usecase.LogoutUseCase
import com.dgero.homly.calendar.domain.usecase.port.GetTodayEventsCountUseCase
import com.dgero.homly.shopping.domain.usecase.port.GetUnboughtShoppingItemCountUseCase
import com.dgero.homly.todolist.domain.usecase.port.GetPendingTodoCountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val sessionRepository: SessionRepository,
    private val getTodayEventsCount: GetTodayEventsCountUseCase,
    private val getUnboughtShoppingItemCount: GetUnboughtShoppingItemCountUseCase,
    private val getPendingTodoCount: GetPendingTodoCountUseCase,
) : ViewModel() {

    private var userId: Long? = null

    private val _todayEventsCount = MutableStateFlow(0)
    val todayEventsCount: StateFlow<Int> = _todayEventsCount

    private val _todoPendingCount = MutableStateFlow(0)
    val todoPendingCount: StateFlow<Int> = _todoPendingCount

    private val _shoppingActiveCount = MutableStateFlow(0)
    val shoppingActiveCount: StateFlow<Int> = _shoppingActiveCount

    init {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId.filterNotNull().first()
            userId = uid
            loadCounts(uid)
        }
    }

    /**
     * Re-fetches all summary counts — called when [HomeScreen] resumes (e.g. returning from
     * Calendar/Shopping/Todo after changes), since this ViewModel instance survives that
     * round-trip and would otherwise keep showing stale data.
     */
    fun refresh() {
        val uid = userId ?: return
        viewModelScope.launch { loadCounts(uid) }
    }

    private suspend fun loadCounts(uid: Long) {
        _todayEventsCount.value = getTodayEventsCount(uid)
        _shoppingActiveCount.value = getUnboughtShoppingItemCount(uid)
        _todoPendingCount.value = getPendingTodoCount(uid)
    }

    fun onLogout(onDone: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onDone()
        }
    }

    class Factory(
        private val logoutUseCase: LogoutUseCase,
        private val sessionRepository: SessionRepository,
        private val getTodayEventsCount: GetTodayEventsCountUseCase,
        private val getUnboughtShoppingItemCount: GetUnboughtShoppingItemCountUseCase,
        private val getPendingTodoCount: GetPendingTodoCountUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(
                logoutUseCase = logoutUseCase,
                sessionRepository = sessionRepository,
                getTodayEventsCount = getTodayEventsCount,
                getUnboughtShoppingItemCount = getUnboughtShoppingItemCount,
                getPendingTodoCount = getPendingTodoCount,
            ) as T
    }
}
