package com.dgero.homly.shopping.domain.usecase

import com.dgero.homly.shopping.domain.FakeShoppingRepository
import com.dgero.homly.shopping.domain.model.ShoppingError
import com.dgero.homly.shopping.domain.model.ShoppingLimits
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddShoppingItemUseCaseTest {

    private val userId = 1L

    private fun useCase(repository: FakeShoppingRepository = FakeShoppingRepository()) =
        AddShoppingItemUseCase(repository)

    @Test
    fun `empty name fails with EmptyName`() = runTest {
        val result = useCase()(userId, "")
        assertEquals(ShoppingError.EmptyName, result.exceptionOrNull())
    }

    @Test
    fun `blank name fails with EmptyName`() = runTest {
        val result = useCase()(userId, "   ")
        assertEquals(ShoppingError.EmptyName, result.exceptionOrNull())
    }

    @Test
    fun `name longer than limit fails with NameTooLong`() = runTest {
        val tooLong = "a".repeat(ShoppingLimits.MAX_NAME_LENGTH + 1)
        val result = useCase()(userId, tooLong)
        assertEquals(ShoppingError.NameTooLong, result.exceptionOrNull())
    }

    @Test
    fun `name at max length succeeds`() = runTest {
        val maxName = "a".repeat(ShoppingLimits.MAX_NAME_LENGTH)
        val result = useCase()(userId, maxName)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `reaching the item limit fails with LimitReached`() = runTest {
        val repository = FakeShoppingRepository()
        val add = useCase(repository)
        repeat(ShoppingLimits.MAX_ITEMS) { i ->
            assertTrue(add(userId, "item $i").isSuccess)
        }
        val result = add(userId, "overflow")
        assertEquals(ShoppingError.LimitReached, result.exceptionOrNull())
    }

    @Test
    fun `valid name is trimmed and stored`() = runTest {
        val repository = FakeShoppingRepository()
        val result = useCase(repository)(userId, "  Milk  ")
        assertTrue(result.isSuccess)
        assertEquals("Milk", result.getOrNull()?.name)
    }
}
