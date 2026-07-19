package com.dgero.homly.shopping.domain.repository

import com.dgero.homly.shopping.domain.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

interface ShoppingRepository {
    /** Items of a single user, unsorted (sorting is a use-case concern). */
    fun observeItems(userId: Long): Flow<List<ShoppingItem>>

    suspend fun countNotBought(userId: Long): Int

    /** Adds a trimmed, validated name; enforces the per-user limit atomically. */
    suspend fun add(userId: Long, name: String): Result<ShoppingItem>

    suspend fun editName(id: Long, name: String): Result<Unit>

    suspend fun toggleBought(id: Long, isBought: Boolean): Result<Unit>

    suspend fun delete(id: Long): Result<Unit>
}
