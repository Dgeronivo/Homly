package com.dgero.homly.shopping.domain.model

data class ShoppingItem(
    val id: Long,
    val name: String,
    val isBought: Boolean,
    val createdAt: Long,
)
