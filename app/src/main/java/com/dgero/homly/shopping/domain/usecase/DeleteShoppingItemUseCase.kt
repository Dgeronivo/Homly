package com.dgero.homly.shopping.domain.usecase

import com.dgero.homly.shopping.domain.repository.ShoppingRepository

class DeleteShoppingItemUseCase(private val repository: ShoppingRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> = repository.delete(id)
}
