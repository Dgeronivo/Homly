package com.dgero.homly.shopping.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items", indices = [Index("userId")])
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val isBought: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
