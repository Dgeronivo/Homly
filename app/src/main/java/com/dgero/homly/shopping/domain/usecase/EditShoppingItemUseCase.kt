package com.dgero.homly.shopping.domain.usecase

import com.dgero.homly.shopping.domain.model.ShoppingError
import com.dgero.homly.shopping.domain.model.ShoppingLimits
import com.dgero.homly.shopping.domain.repository.ShoppingRepository

class EditShoppingItemUseCase(private val repository: ShoppingRepository) {
    /** Renames an item; never touches its `createdAt` or `isBought`. */
    suspend operator fun invoke(id: Long, name: String): Result<Unit> {
        val trimmed = name.trim()
        validate(trimmed)?.let { return Result.failure(it) }
        return repository.editName(id, trimmed)
    }

    private fun validate(trimmed: String): ShoppingError? = when {
        trimmed.isEmpty() -> ShoppingError.EmptyName
        trimmed.length > ShoppingLimits.MAX_NAME_LENGTH -> ShoppingError.NameTooLong
        else -> null
    }
}
