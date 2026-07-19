package com.dgero.homly.todolist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.todolist.domain.error.TodoError
import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.domain.model.TodoLimits
import com.dgero.homly.todolist.domain.usecase.AddTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.DeleteCompletedTodoItemsUseCase
import com.dgero.homly.todolist.domain.usecase.DeleteTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.EditTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.GetTodoItemsUseCase
import com.dgero.homly.todolist.domain.usecase.ToggleTodoItemUseCase
import com.dgero.homly.todolist.domain.validation.TodoTitleValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoListViewModel(
    private val getItems: GetTodoItemsUseCase,
    private val addItem: AddTodoItemUseCase,
    private val editItem: EditTodoItemUseCase,
    private val toggleItem: ToggleTodoItemUseCase,
    private val deleteItem: DeleteTodoItemUseCase,
    private val deleteCompletedItems: DeleteCompletedTodoItemsUseCase,
    private val validator: TodoTitleValidator,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val newItemTitle = MutableStateFlow("")
    private val titleError = MutableStateFlow<String?>(null)
    private val formError = MutableStateFlow<String?>(null)
    private val _items = MutableStateFlow<List<TodoItem>>(emptyList())
    private val _showActiveOnly = MutableStateFlow(false)

    private var userId: Long? = null

    init {
        viewModelScope.launch {
            val uid = sessionRepository.currentUserId.filterNotNull().first()
            userId = uid
            _items.value = getItems(uid)
        }
    }

    val uiState: StateFlow<TodoListUiState> = combine(
        _items, newItemTitle, titleError, formError, _showActiveOnly,
    ) { currentItems, title, tError, fError, showActiveOnly ->
        TodoListUiState(
            items = if (showActiveOnly) currentItems.filterNot { it.isDone } else currentItems,
            newItemTitle = title,
            isLimitReached = currentItems.size >= TodoLimits.MAX_ITEMS,
            titleError = tError,
            formError = fError,
            showActiveOnly = showActiveOnly,
            completedCount = currentItems.count { it.isDone },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodoListUiState())

    fun onNewItemTitleChange(value: String) {
        newItemTitle.value = value
        when {
            validator.validateMaxLength(value) != null ->
                titleError.value = "Name is too long (max 100 characters)"
            titleError.value == "Name is too long (max 100 characters)" ->
                titleError.value = null
        }
    }

    fun onAdd() {
        val uid = userId ?: return
        viewModelScope.launch {
            addItem(uid, newItemTitle.value).fold(
                onSuccess = { item ->
                    _items.value = listOf(item) + _items.value
                    newItemTitle.value = ""
                    titleError.value = null
                },
                onFailure = { e ->
                    when (e) {
                        is TodoError.EmptyTitle -> titleError.value = "Name cannot be empty"
                        is TodoError.TitleTooLong -> titleError.value = "Name is too long (max 100 characters)"
                        is TodoError.LimitReached -> formError.value = "List is full (max 50 items)"
                        else -> formError.value = "Something went wrong"
                    }
                },
            )
        }
    }

    fun onToggle(item: TodoItem) {
        val uid = userId ?: return
        viewModelScope.launch {
            toggleItem(item.id, uid, !item.isDone).onSuccess {
                _items.value = _items.value.map {
                    if (it.id == item.id) it.copy(isDone = !item.isDone) else it
                }
            }
        }
    }

    fun onEdit(id: Long, newTitle: String) {
        val uid = userId ?: return
        viewModelScope.launch {
            editItem(id, uid, newTitle).fold(
                onSuccess = {
                    _items.value = _items.value.map {
                        if (it.id == id) it.copy(title = newTitle) else it
                    }
                },
                onFailure = { e ->
                    when (e) {
                        is TodoError.Unauthorized -> {}
                        is TodoError.EmptyTitle -> titleError.value = "Name cannot be empty"
                        is TodoError.TitleTooLong -> titleError.value = "Name is too long (max 100 characters)"
                        else -> formError.value = "Something went wrong"
                    }
                },
            )
        }
    }

    fun onDelete(id: Long) {
        val uid = userId ?: return
        viewModelScope.launch {
            deleteItem(id, uid).onSuccess {
                _items.value = _items.value.filter { it.id != id }
            }
        }
    }

    fun onToggleActiveOnly() {
        _showActiveOnly.value = !_showActiveOnly.value
    }

    fun onClearCompleted() {
        val uid = userId ?: return
        viewModelScope.launch {
            deleteCompletedItems(uid).onSuccess {
                _items.value = _items.value.filterNot { it.isDone }
            }
        }
    }

    class Factory(
        private val getItems: GetTodoItemsUseCase,
        private val addItem: AddTodoItemUseCase,
        private val editItem: EditTodoItemUseCase,
        private val toggleItem: ToggleTodoItemUseCase,
        private val deleteItem: DeleteTodoItemUseCase,
        private val deleteCompletedItems: DeleteCompletedTodoItemsUseCase,
        private val validator: TodoTitleValidator,
        private val sessionRepository: SessionRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TodoListViewModel(
                getItems, addItem, editItem, toggleItem, deleteItem, deleteCompletedItems,
                validator, sessionRepository,
            ) as T
    }
}
