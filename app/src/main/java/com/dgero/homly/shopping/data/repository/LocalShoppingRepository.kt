package com.dgero.homly.shopping.data.repository

import com.dgero.homly.auth.data.repository.TransactionRunner
import com.dgero.homly.shopping.data.local.ShoppingItemDao
import com.dgero.homly.shopping.data.local.ShoppingItemEntity
import com.dgero.homly.shopping.domain.model.ShoppingError
import com.dgero.homly.shopping.domain.model.ShoppingItem
import com.dgero.homly.shopping.domain.model.ShoppingLimits
import com.dgero.homly.shopping.domain.repository.ShoppingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalShoppingRepository(
    private val dao: ShoppingItemDao,
    private val runTransaction: TransactionRunner,
) : ShoppingRepository {

    override fun observeItems(userId: Long): Flow<List<ShoppingItem>> =
        dao.observeByUser(userId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun add(userId: Long, name: String): Result<ShoppingItem> = try {
        val item = runTransaction {
            if (dao.countByUser(userId) >= ShoppingLimits.MAX_ITEMS) throw ShoppingError.LimitReached
            val entity = ShoppingItemEntity(userId = userId, name = name)
            val id = dao.insert(entity)
            entity.copy(id = id).toDomain()
        }
        Result.success(item)
    } catch (e: ShoppingError) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(ShoppingError.Unknown(e))
    }

    override suspend fun editName(id: Long, name: String): Result<Unit> =
        runMutation { dao.updateName(id, name) }

    override suspend fun toggleBought(id: Long, isBought: Boolean): Result<Unit> =
        runMutation { dao.updateBought(id, isBought) }

    override suspend fun delete(id: Long): Result<Unit> =
        runMutation { dao.deleteById(id) }

    private suspend fun runMutation(block: suspend () -> Unit): Result<Unit> = try {
        block()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(ShoppingError.Unknown(e))
    }

    private fun ShoppingItemEntity.toDomain() = ShoppingItem(
        id = id,
        name = name,
        isBought = isBought,
        createdAt = createdAt,
    )
}
