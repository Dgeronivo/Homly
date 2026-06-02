package com.dgero.homly.shopping.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    /** Unsorted; ordering is applied in the domain layer. */
    @Query("SELECT * FROM shopping_items WHERE userId = :userId")
    fun observeByUser(userId: Long): Flow<List<ShoppingItemEntity>>

    @Query("SELECT COUNT(*) FROM shopping_items WHERE userId = :userId")
    suspend fun countByUser(userId: Long): Int

    @Insert
    suspend fun insert(item: ShoppingItemEntity): Long

    @Query("UPDATE shopping_items SET name = :name WHERE id = :id")
    suspend fun updateName(id: Long, name: String)

    @Query("UPDATE shopping_items SET isBought = :isBought WHERE id = :id")
    suspend fun updateBought(id: Long, isBought: Boolean)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
