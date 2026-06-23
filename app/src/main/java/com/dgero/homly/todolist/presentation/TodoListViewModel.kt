package com.dgero.homly.todolist.presentation

import androidx.lifecycle.ViewModel
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.domain.usecase.AddTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.DeleteTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.EditTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.ObserveTodoItemsUseCase
import com.dgero.homly.todolist.domain.usecase.ToggleTodoItemUseCase
import com.dgero.homly.todolist.domain.validation.TodoTitleValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TodoListViewModel(
    private val observeItems: ObserveTodoItemsUseCase,
    private val addItem: AddTodoItemUseCase,
    private val editItem: EditTodoItemUseCase,
    private val toggleItem: ToggleTodoItemUseCase,
    private val deleteItem: DeleteTodoItemUseCase,
    private val validator: TodoTitleValidator,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    val uiState: StateFlow<TodoListUiState> = MutableStateFlow(TodoListUiState())

    fun onNewItemTitleChange(value: String) {}
    fun onAdd() {}
    fun onToggle(item: TodoItem) {}
    fun onEdit(id: Long, newTitle: String) {}
    fun onDelete(id: Long) {}
}
