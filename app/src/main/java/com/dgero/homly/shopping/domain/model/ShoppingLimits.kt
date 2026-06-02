package com.dgero.homly.shopping.domain.model

/** Single source of truth for shopping-list business constraints. */
object ShoppingLimits {
    /** Max items per user (bought + active combined). */
    const val MAX_ITEMS = 50

    /** Max length of an item name after trimming. */
    const val MAX_NAME_LENGTH = 100
}
