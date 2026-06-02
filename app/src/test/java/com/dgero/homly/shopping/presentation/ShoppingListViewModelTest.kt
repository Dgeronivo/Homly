package com.dgero.homly.shopping.presentation

import com.dgero.homly.auth.domain.repository.SessionRepository
import com.dgero.homly.shopping.domain.FakeShoppingRepository
import com.dgero.homly.shopping.domain.model.ShoppingItem
import com.dgero.homly.shopping.domain.model.ShoppingSortOrder
import com.dgero.homly.shopping.domain.usecase.AddShoppingItemUseCase
import com.dgero.homly.shopping.domain.usecase.DeleteShoppingItemUseCase
import com.dgero.homly.shopping.domain.usecase.EditShoppingItemUseCase
import com.dgero.homly.shopping.domain.usecase.ObserveShoppingItemsUseCase
import com.dgero.homly.shopping.domain.usecase.ToggleShoppingItemUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeSessionRepository(initial: Long?) : SessionRepository {
        val userIdFlow = MutableStateFlow(initial)
        override val currentUserId: Flow<Long?> = userIdFlow
        override suspend fun setSession(userId: Long) {
            userIdFlow.value = userId
        }

        override suspend fun clear() {
            userIdFlow.value = null
        }
    }

    private fun viewModel(
        repository: FakeShoppingRepository,
        session: FakeSessionRepository,
    ) = ShoppingListViewModel(
        observeShoppingItems = ObserveShoppingItemsUseCase(repository),
        addShoppingItem = AddShoppingItemUseCase(repository),
        editShoppingItem = EditShoppingItemUseCase(repository),
        toggleShoppingItem = ToggleShoppingItemUseCase(repository),
        deleteShoppingItem = DeleteShoppingItemUseCase(repository),
        sessionRepository = session,
    )

    @Test
    fun `default sort order is DATE_DESC`() = runTest {
        val vm = viewModel(FakeShoppingRepository(), FakeSessionRepository(initial = 1L))
        backgroundScope.launchCollect(vm)
        advanceUntilIdle()

        assertEquals(ShoppingSortOrder.DATE_DESC, vm.uiState.value.sortOrder)
    }

    @Test
    fun `switching sort order reorders items`() = runTest {
        val repository = FakeShoppingRepository(
            seed = listOf(
                ShoppingItem(id = 1, name = "banana", isBought = false, createdAt = 100L),
                ShoppingItem(id = 2, name = "apple", isBought = false, createdAt = 200L),
            ),
        )
        val vm = viewModel(repository, FakeSessionRepository(initial = 1L))
        backgroundScope.launchCollect(vm)
        advanceUntilIdle()

        // DATE_DESC: newest (createdAt 200 -> apple) first.
        assertEquals(listOf("apple", "banana"), vm.uiState.value.items.map { it.name })

        vm.onSortChange(ShoppingSortOrder.ALPHABETICAL)
        advanceUntilIdle()

        assertEquals(ShoppingSortOrder.ALPHABETICAL, vm.uiState.value.sortOrder)
        assertEquals(listOf("apple", "banana"), vm.uiState.value.items.map { it.name })
    }

    @Test
    fun `isLimitReached is true when at the item limit`() = runTest {
        val seed = (1..50).map {
            ShoppingItem(id = it.toLong(), name = "item $it", isBought = false, createdAt = it.toLong())
        }
        val vm = viewModel(FakeShoppingRepository(seed), FakeSessionRepository(initial = 1L))
        backgroundScope.launchCollect(vm)
        advanceUntilIdle()

        assertEquals(50, vm.uiState.value.items.size)
        assertTrue(vm.uiState.value.isLimitReached)
    }

    @Test
    fun `items react to current user change without leaking`() = runTest {
        val repository = FakeShoppingRepository()
        val session = FakeSessionRepository(initial = 1L)
        repository.add(1, "Milk")
        repository.add(2, "Bread")

        val vm = viewModel(repository, session)
        backgroundScope.launchCollect(vm)
        advanceUntilIdle()

        assertEquals(listOf("Milk"), vm.uiState.value.items.map { it.name })

        session.setSession(2L)
        advanceUntilIdle()

        assertEquals(listOf("Bread"), vm.uiState.value.items.map { it.name })
    }

    @Test
    fun `empty list is not limit reached`() = runTest {
        val vm = viewModel(FakeShoppingRepository(), FakeSessionRepository(initial = 1L))
        backgroundScope.launchCollect(vm)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.items.isEmpty())
        assertFalse(vm.uiState.value.isLimitReached)
    }

    private fun CoroutineScope.launchCollect(vm: ShoppingListViewModel) {
        launch { vm.uiState.collect {} }
    }
}
