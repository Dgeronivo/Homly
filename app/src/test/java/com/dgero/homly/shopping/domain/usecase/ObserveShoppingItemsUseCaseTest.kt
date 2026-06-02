package com.dgero.homly.shopping.domain.usecase

import com.dgero.homly.shopping.domain.FakeShoppingRepository
import com.dgero.homly.shopping.domain.model.ShoppingItem
import com.dgero.homly.shopping.domain.model.ShoppingSortOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveShoppingItemsUseCaseTest {

    private val userId = 1L

    private val items = listOf(
        ShoppingItem(id = 1, name = "banana", isBought = false, createdAt = 100L),
        ShoppingItem(id = 2, name = "Apple", isBought = false, createdAt = 200L),
        // Same createdAt as id=2 to exercise the id tie-breaker.
        ShoppingItem(id = 3, name = "cherry", isBought = false, createdAt = 200L),
    )

    private fun useCase() = ObserveShoppingItemsUseCase(FakeShoppingRepository(items))

    @Test
    fun `date desc orders newest first with id tie-break descending`() = runTest {
        val result = useCase()(userId, ShoppingSortOrder.DATE_DESC).first()
        // createdAt: 200 (id3), 200 (id2), 100 (id1) -> tie broken by id desc.
        assertEquals(listOf(3L, 2L, 1L), result.map { it.id })
    }

    @Test
    fun `alphabetical is case insensitive`() = runTest {
        val result = useCase()(userId, ShoppingSortOrder.ALPHABETICAL).first()
        // Apple, banana, cherry regardless of case.
        assertEquals(listOf("Apple", "banana", "cherry"), result.map { it.name })
    }
}
