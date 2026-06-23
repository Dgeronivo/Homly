package com.dgero.homly.todolist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.todolist.domain.error.TodoError
import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.domain.model.TodoLimits
import com.dgero.homly.todolist.domain.usecase.AddTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.DeleteTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.EditTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.ObserveTodoItemsUseCase
import com.dgero.homly.todolist.domain.usecase.ToggleTodoItemUseCase
import com.dgero.homly.todolist.domain.validation.TodoTitleValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModel(
    private val observeItems: ObserveTodoItemsUseCase,
    private val addItem: AddTodoItemUseCase,
    private val editItem: EditTodoItemUseCase,
    private val toggleItem: ToggleTodoItemUseCase,
    private val deleteItem: DeleteTodoItemUseCase,
    private val validator: TodoTitleValidator,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val newItemTitle = MutableStateFlow("")
    private val titleError = MutableStateFlow<String?>(null)
    private val formError = MutableStateFlow<String?>(null)

    private val currentUserId: StateFlow<Long?> = sessionRepository.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val items: Flow<List<TodoItem>> = currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList())
        else observeItems(userId)
    }

    val uiState: StateFlow<TodoListUiState> = combine(
        items, newItemTitle, titleError, formError,
    ) { currentItems, title, tError, fError ->
        TodoListUiState(
            items = currentItems,
            newItemTitle = title,
            isLimitReached = currentItems.size >= TodoLimits.MAX_ITEMS,
            titleError = tError,
            formError = fError,
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
        val userId = currentUserId.value ?: return
        viewModelScope.launch {
            addItem(userId, newItemTitle.value).fold(
                onSuccess = {
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
        val userId = currentUserId.value ?: return
        viewModelScope.launch { toggleItem(item.id, userId, !item.isDone) }
    }

    fun onEdit(id: Long, newTitle: String) {
        val userId = currentUserId.value ?: return
        viewModelScope.launch {
            editItem(id, userId, newTitle).onFailure { e ->
                when (e) {
                    is TodoError.Unauthorized -> {}
                    is TodoError.EmptyTitle -> titleError.value = "Name cannot be empty"
                    is TodoError.TitleTooLong -> titleError.value = "Name is too long (max 100 characters)"
                    else -> formError.value = "Something went wrong"
                }
            }
        }
    }

    fun onDelete(id: Long) {
        val userId = currentUserId.value ?: return
        viewModelScope.launch { deleteItem(id, userId) }
    }

    class Factory(
        private val observeItems: ObserveTodoItemsUseCase,
        private val addItem: AddTodoItemUseCase,
        private val editItem: EditTodoItemUseCase,
        private val toggleItem: ToggleTodoItemUseCase,
        private val deleteItem: DeleteTodoItemUseCase,
        private val validator: TodoTitleValidator,
        private val sessionRepository: SessionRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TodoListViewModel(
                observeItems, addItem, editItem, toggleItem, deleteItem, validator, sessionRepository,
            ) as T
    }
}
