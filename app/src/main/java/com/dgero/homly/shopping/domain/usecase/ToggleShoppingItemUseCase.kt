package com.dgero.homly.shopping.domain.usecase

import com.dgero.homly.shopping.domain.repository.ShoppingRepository

class ToggleShoppingItemUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(id: Long, isBought: Boolean): Result<Unit> =
        repository.toggleBought(id, isBought)
}
