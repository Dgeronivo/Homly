package com.dgero.homly.shopping.domain.usecase.port

interface GetUnboughtShoppingItemCountUseCase {
    suspend operator fun invoke(userId: Long): Int
}
