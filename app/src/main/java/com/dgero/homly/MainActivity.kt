package com.dgero.homly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dgero.homly.auth.domain.usecase.LogoutUseCase
import com.dgero.homly.auth.presentation.authGraph
import com.dgero.homly.home.presentation.HomeScreen
import com.dgero.homly.home.presentation.HomeViewModel
import com.dgero.homly.shopping.domain.usecase.AddShoppingItemUseCase
import com.dgero.homly.shopping.domain.usecase.DeleteShoppingItemUseCase
import com.dgero.homly.shopping.domain.usecase.EditShoppingItemUseCase
import com.dgero.homly.shopping.domain.usecase.ObserveShoppingItemsUseCase
import com.dgero.homly.shopping.domain.usecase.ToggleShoppingItemUseCase
import com.dgero.homly.shopping.presentation.ShoppingListScreen
import com.dgero.homly.shopping.presentation.ShoppingListViewModel
import com.dgero.homly.todolist.domain.usecase.AddTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.DeleteTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.EditTodoItemUseCase
import com.dgero.homly.todolist.domain.usecase.ObserveTodoItemsUseCase
import com.dgero.homly.todolist.domain.usecase.ToggleTodoItemUseCase
import com.dgero.homly.todolist.domain.validation.TodoTitleValidator
import com.dgero.homly.todolist.presentation.TodoListScreen
import com.dgero.homly.todolist.presentation.TodoListViewModel
import com.dgero.homly.ui.theme.HomlyTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as HomlyApplication).container
        setContent {
            HomlyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuthGate(container = container)
                }
            }
        }
    }
}

private sealed interface SessionLoadState {
    object Loading : SessionLoadState
    data class Ready(val userId: Long?) : SessionLoadState
}

@Composable
private fun AuthGate(container: AppContainer) {
    var sessionState by remember { mutableStateOf<SessionLoadState>(SessionLoadState.Loading) }

    LaunchedEffect(Unit) {
        val userId = container.sessionRepository.currentUserId.first()
        sessionState = SessionLoadState.Ready(userId)
    }

    when (val state = sessionState) {
        is SessionLoadState.Loading -> Unit
        is SessionLoadState.Ready -> {
            val startDestination = if (state.userId != null) "home" else "auth"
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = startDestination) {
                authGraph(
                    navController = navController,
                    userRepository = container.userRepository,
                    sessionRepository = container.sessionRepository,
                )
                composable("home") {
                    val vm: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(
                            logoutUseCase = LogoutUseCase(container.sessionRepository),
                            userRepository = container.userRepository,
                            sessionRepository = container.sessionRepository,
                        )
                    )
                    HomeScreen(
                        viewModel = vm,
                        onOpenShoppingList = { navController.navigate("shopping") },
                        onOpenTodoList = { navController.navigate("todo-list") },
                        onLogout = {
                            navController.navigate("auth") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                    )
                }
                composable("todo-list") {
                    val vm: TodoListViewModel = viewModel(
                        factory = TodoListViewModel.Factory(
                            observeItems = ObserveTodoItemsUseCase(container.todoRepository),
                            addItem = AddTodoItemUseCase(container.todoRepository, TodoTitleValidator),
                            editItem = EditTodoItemUseCase(container.todoRepository, TodoTitleValidator),
                            toggleItem = ToggleTodoItemUseCase(container.todoRepository),
                            deleteItem = DeleteTodoItemUseCase(container.todoRepository),
                            validator = TodoTitleValidator,
                            sessionRepository = container.sessionRepository,
                        )
                    )
                    TodoListScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                    )
                }
                composable("shopping") {
                    val vm: ShoppingListViewModel = viewModel(
                        factory = ShoppingListViewModel.Factory(
                            observeShoppingItems = ObserveShoppingItemsUseCase(container.shoppingRepository),
                            addShoppingItem = AddShoppingItemUseCase(container.shoppingRepository),
                            editShoppingItem = EditShoppingItemUseCase(container.shoppingRepository),
                            toggleShoppingItem = ToggleShoppingItemUseCase(container.shoppingRepository),
                            deleteShoppingItem = DeleteShoppingItemUseCase(container.shoppingRepository),
                            sessionRepository = container.sessionRepository,
                        )
                    )
                    ShoppingListScreen(
                        viewModel = vm,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
