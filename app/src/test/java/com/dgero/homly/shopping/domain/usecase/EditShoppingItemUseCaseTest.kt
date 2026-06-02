package com.dgero.homly.shopping.domain.usecase

import com.dgero.homly.shopping.domain.FakeShoppingRepository
import com.dgero.homly.shopping.domain.model.ShoppingError
import com.dgero.homly.shopping.domain.model.ShoppingItem
import com.dgero.homly.shopping.domain.model.ShoppingLimits
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditShoppingItemUseCaseTest {

    private fun repositoryWithItem() = FakeShoppingRepository(
        seed = listOf(ShoppingItem(id = 1, name = "Milk", isBought = true, createdAt = 100L)),
    )

    @Test
    fun `empty name fails with EmptyName`() = runTest {
        val result = EditShoppingItemUseCase(repositoryWithItem())(1, "   ")
        assertEquals(ShoppingError.EmptyName, result.exceptionOrNull())
    }

    @Test
    fun `name longer than limit fails with NameTooLong`() = runTest {
        val tooLong = "a".repeat(ShoppingLimits.MAX_NAME_LENGTH + 1)
        val result = EditShoppingItemUseCase(repositoryWithItem())(1, tooLong)
        assertEquals(ShoppingError.NameTooLong, result.exceptionOrNull())
    }

    @Test
    fun `valid edit trims name and keeps createdAt and isBought`() = runTest {
        val repository = repositoryWithItem()
        val result = EditShoppingItemUseCase(repository)(1, "  Bread  ")
        assertTrue(result.isSuccess)
        val item = repository.itemById(1)!!
        assertEquals("Bread", item.name)
        assertEquals(100L, item.createdAt)
        assertEquals(true, item.isBought)
    }
}
