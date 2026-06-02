package com.dgero.homly.shopping.domain.usecase

import com.dgero.homly.shopping.domain.model.ShoppingItem
import com.dgero.homly.shopping.domain.model.ShoppingSortOrder
import com.dgero.homly.shopping.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveShoppingItemsUseCase(private val repository: ShoppingRepository) {
    operator fun invoke(userId: Long, sortOrder: ShoppingSortOrder): Flow<List<ShoppingItem>> =
        repository.observeItems(userId).map { items -> items.sortedWith(comparatorFor(sortOrder)) }

    private fun comparatorFor(sortOrder: ShoppingSortOrder): Comparator<ShoppingItem> = when (sortOrder) {
        ShoppingSortOrder.DATE_DESC ->
            compareByDescending<ShoppingItem> { it.createdAt }.thenByDescending { it.id }
        ShoppingSortOrder.ALPHABETICAL ->
            compareBy<ShoppingItem> { it.name.lowercase() }.thenBy { it.id }
    }
}
