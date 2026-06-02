package com.dgero.homly.shopping.domain.usecase

import com.dgero.homly.shopping.domain.model.ShoppingError
import com.dgero.homly.shopping.domain.model.ShoppingItem
import com.dgero.homly.shopping.domain.model.ShoppingLimits
import com.dgero.homly.shopping.domain.repository.ShoppingRepository

class AddShoppingItemUseCase(private val repository: ShoppingRepository) {
    /**
     * Trims and validates the name, then delegates to the repository, which
     * enforces the per-user limit atomically and may fail with [ShoppingError.LimitReached].
     */
    suspend operator fun invoke(userId: Long, name: String): Result<ShoppingItem> {
        val trimmed = name.trim()
        validate(trimmed)?.let { return Result.failure(it) }
        return repository.add(userId, trimmed)
    }

    private fun validate(trimmed: String): ShoppingError? = when {
        trimmed.isEmpty() -> ShoppingError.EmptyName
        trimmed.length > ShoppingLimits.MAX_NAME_LENGTH -> ShoppingError.NameTooLong
        else -> null
    }
}
