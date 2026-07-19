package com.dgero.homly.shopping.domain

import com.dgero.homly.shopping.domain.model.ShoppingError
import com.dgero.homly.shopping.domain.model.ShoppingItem
import com.dgero.homly.shopping.domain.model.ShoppingLimits
import com.dgero.homly.shopping.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [ShoppingRepository] for domain unit tests. Mirrors the contract of
 * the real data-layer implementation: items are scoped per user, the limit is
 * enforced on [add], and [editName] never touches `createdAt` / `isBought`.
 */
class FakeShoppingRepository(seed: List<ShoppingItem> = emptyList()) : ShoppingRepository {

    private val state = MutableStateFlow(seed.associateBy { it.id })
    private var nextId: Long = (seed.maxOfOrNull { it.id } ?: 0L) + 1

    /** Items keyed by user so the fake can model per-user scoping in tests. */
    private val owners = HashMap<Long, Long>() // itemId -> userId

    init {
        // Seeded items default to user 1 unless re-seeded via add().
        seed.forEach { owners[it.id] = 1L }
    }

    override fun observeItems(userId: Long): Flow<List<ShoppingItem>> =
        state.map { items -> items.values.filter { owners[it.id] == userId } }

    override suspend fun countNotBought(userId: Long): Int =
        state.value.values.count { owners[it.id] == userId && !it.isBought }

    override suspend fun add(userId: Long, name: String): Result<ShoppingItem> {
        val countForUser = state.value.values.count { owners[it.id] == userId }
        if (countForUser >= ShoppingLimits.MAX_ITEMS) return Result.failure(ShoppingError.LimitReached)
        val item = ShoppingItem(id = nextId++, name = name, isBought = false, createdAt = nextId)
        owners[item.id] = userId
        state.value = state.value + (item.id to item)
        return Result.success(item)
    }

    override suspend fun editName(id: Long, name: String): Result<Unit> {
        val current = state.value[id] ?: return Result.success(Unit)
        state.value = state.value + (id to current.copy(name = name))
        return Result.success(Unit)
    }

    override suspend fun toggleBought(id: Long, isBought: Boolean): Result<Unit> {
        val current = state.value[id] ?: return Result.success(Unit)
        state.value = state.value + (id to current.copy(isBought = isBought))
        return Result.success(Unit)
    }

    override suspend fun delete(id: Long): Result<Unit> {
        state.value = state.value - id
        owners.remove(id)
        return Result.success(Unit)
    }

    fun itemById(id: Long): ShoppingItem? = state.value[id]
}
