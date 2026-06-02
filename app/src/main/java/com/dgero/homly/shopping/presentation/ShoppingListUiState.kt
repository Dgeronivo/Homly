package com.dgero.homly.shopping.presentation

import com.dgero.homly.shopping.domain.model.ShoppingItem
import com.dgero.homly.shopping.domain.model.ShoppingSortOrder

data class ShoppingListUiState(
    val items: List<ShoppingItem> = emptyList(),
    val sortOrder: ShoppingSortOrder = ShoppingSortOrder.DATE_DESC,
    val newItemText: String = "",
    val isLimitReached: Boolean = false,
    val errorMessage: String? = null,
)
