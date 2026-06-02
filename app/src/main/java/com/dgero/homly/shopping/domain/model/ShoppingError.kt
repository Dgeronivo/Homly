package com.dgero.homly.shopping.domain.model

sealed class ShoppingError : Exception() {
    object EmptyName : ShoppingError()
    object NameTooLong : ShoppingError()
    object LimitReached : ShoppingError()
    data class Unknown(override val cause: Throwable) : ShoppingError()
}
