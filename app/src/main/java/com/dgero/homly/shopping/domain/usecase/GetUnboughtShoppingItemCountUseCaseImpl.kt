package com.dgero.homly.shopping.domain.usecase

import com.dgero.homly.shopping.domain.repository.ShoppingRepository
import com.dgero.homly.shopping.domain.usecase.port.GetUnboughtShoppingItemCountUseCase

class GetUnboughtShoppingItemCountUseCaseImpl(
    private val repository: ShoppingRepository,
) : GetUnboughtShoppingItemCountUseCase {
    override suspend fun invoke(userId: Long): Int = repository.countNotBought(userId)
}
