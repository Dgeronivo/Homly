package com.dgero.homly.shopping.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.shopping.domain.model.ShoppingError
import com.dgero.homly.shopping.domain.model.ShoppingItem
import com.dgero.homly.shopping.domain.model.ShoppingLimits
import com.dgero.homly.shopping.domain.model.ShoppingSortOrder
import com.dgero.homly.shopping.domain.usecase.AddShoppingItemUseCase
import com.dgero.homly.shopping.domain.usecase.DeleteShoppingItemUseCase
import com.dgero.homly.shopping.domain.usecase.EditShoppingItemUseCase
import com.dgero.homly.shopping.domain.usecase.ObserveShoppingItemsUseCase
import com.dgero.homly.shopping.domain.usecase.ToggleShoppingItemUseCase
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
class ShoppingListViewModel(
    private val observeShoppingItems: ObserveShoppingItemsUseCase,
    private val addShoppingItem: AddShoppingItemUseCase,
    private val editShoppingItem: EditShoppingItemUseCase,
    private val toggleShoppingItem: ToggleShoppingItemUseCase,
    private val deleteShoppingItem: DeleteShoppingItemUseCase,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val sortOrder = MutableStateFlow(ShoppingSortOrder.DATE_DESC)
    private val newItemText = MutableStateFlow("")
    private val errorMessage = MutableStateFlow<String?>(null)

    private val currentUserId: StateFlow<Long?> = sessionRepository.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val items: Flow<List<ShoppingItem>> =
        combine(currentUserId, sortOrder) { userId, order -> userId to order }
            .flatMapLatest { (userId, order) ->
                if (userId == null) flowOf(emptyList())
                else observeShoppingItems(userId, order)
            }

    val uiState: StateFlow<ShoppingListUiState> = combine(
        items, sortOrder, newItemText, errorMessage,
    ) { currentItems, order, text, error ->
        ShoppingListUiState(
            items = currentItems,
            sortOrder = order,
            newItemText = text,
            isLimitReached = currentItems.size >= ShoppingLimits.MAX_ITEMS,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingListUiState())

    fun onNewItemTextChange(value: String) {
        newItemText.value = value
        errorMessage.value = null
    }

    fun onSortChange(order: ShoppingSortOrder) {
        sortOrder.value = order
    }

    fun onAdd() {
        val userId = currentUserId.value ?: return
        viewModelScope.launch {
            addShoppingItem(userId, newItemText.value).fold(
                onSuccess = {
                    newItemText.value = ""
                    errorMessage.value = null
                },
                onFailure = { errorMessage.value = shoppingErrorMessage(it) },
            )
        }
    }

    fun onToggle(item: ShoppingItem) {
        viewModelScope.launch { toggleShoppingItem(item.id, !item.isBought) }
    }

    fun onEdit(id: Long, newName: String) {
        viewModelScope.launch {
            editShoppingItem(id, newName).onFailure { errorMessage.value = shoppingErrorMessage(it) }
        }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch { deleteShoppingItem(id) }
    }

    class Factory(
        private val observeShoppingItems: ObserveShoppingItemsUseCase,
        private val addShoppingItem: AddShoppingItemUseCase,
        private val editShoppingItem: EditShoppingItemUseCase,
        private val toggleShoppingItem: ToggleShoppingItemUseCase,
        private val deleteShoppingItem: DeleteShoppingItemUseCase,
        private val sessionRepository: SessionRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ShoppingListViewModel(
                observeShoppingItems,
                addShoppingItem,
                editShoppingItem,
                toggleShoppingItem,
                deleteShoppingItem,
                sessionRepository,
            ) as T
    }
}

internal fun shoppingErrorMessage(e: Throwable): String = when (e) {
    is ShoppingError.EmptyName -> "Name cannot be empty"
    is ShoppingError.NameTooLong -> "Name is too long (max ${ShoppingLimits.MAX_NAME_LENGTH} characters)"
    is ShoppingError.LimitReached -> "List is full (max ${ShoppingLimits.MAX_ITEMS} items)"
    else -> "Something went wrong"
}
