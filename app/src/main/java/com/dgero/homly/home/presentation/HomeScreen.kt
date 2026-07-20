package com.dgero.homly.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dgero.homly.ui.theme.HomlyGridCard
import com.dgero.homly.ui.theme.HomlyHeroCard
import com.dgero.homly.ui.theme.HomlyTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenShoppingList: () -> Unit,
    onOpenTodoList: () -> Unit,
    onOpenCalendar: () -> Unit,
    onLogout: () -> Unit,
) {
    val todayEventsCount by viewModel.todayEventsCount.collectAsStateWithLifecycle()
    val shoppingActiveCount by viewModel.shoppingActiveCount.collectAsStateWithLifecycle()
    val todoPendingCount by viewModel.todoPendingCount.collectAsStateWithLifecycle()
    val todoTotalCount by viewModel.todoTotalCount.collectAsStateWithLifecycle()

    // Re-fetches the suspend-sourced summary counts whenever this screen resumes — e.g.
    // returning from Calendar/Shopping/Todo after changes — since HomeViewModel survives that
    // round-trip and would otherwise keep showing stale data.
    val lifecycleOwner = LocalLifecycleOwner.current
    val onResume by rememberUpdatedState(viewModel::refresh)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    HomeContent(
        todayEventsCount = todayEventsCount,
        shoppingActiveCount = shoppingActiveCount,
        todoPendingCount = todoPendingCount,
        todoTotalCount = todoTotalCount,
        onOpenShoppingList = onOpenShoppingList,
        onOpenTodoList = onOpenTodoList,
        onOpenCalendar = onOpenCalendar,
        onLogout = { viewModel.onLogout(onLogout) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    todayEventsCount: Int,
    shoppingActiveCount: Int,
    todoPendingCount: Int,
    todoTotalCount: Int,
    onOpenShoppingList: () -> Unit,
    onOpenTodoList: () -> Unit,
    onOpenCalendar: () -> Unit,
    onLogout: () -> Unit,
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    IconButton(onClick = { showLogoutConfirm = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log out")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
        ) {
            HomlyHeroCard(
                icon = Icons.Default.DateRange,
                title = "Calendar",
                summary = if (todayEventsCount > 0) "Today: $todayEventsCount events" else "No events today",
                onClick = onOpenCalendar,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HomlyGridCard(
                    icon = Icons.Default.ShoppingCart,
                    title = "Shopping list",
                    summary = if (shoppingActiveCount > 0) "$shoppingActiveCount left" else "Empty",
                    onClick = onOpenShoppingList,
                    modifier = Modifier.weight(1f),
                )
                HomlyGridCard(
                    icon = Icons.Default.CheckCircle,
                    title = "Todo list",
                    summary = when {
                        todoPendingCount > 0 -> "$todoPendingCount pending"
                        todoTotalCount > 0 -> "All done"
                        else -> "Empty"
                    },
                    onClick = onOpenTodoList,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                ) {
                    Text("Log out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    HomlyTheme {
        HomeContent(
            todayEventsCount = 2,
            shoppingActiveCount = 5,
            todoPendingCount = 3,
            todoTotalCount = 5,
            onOpenShoppingList = {},
            onOpenTodoList = {},
            onOpenCalendar = {},
            onLogout = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentAllDonePreview() {
    HomlyTheme {
        HomeContent(
            todayEventsCount = 0,
            shoppingActiveCount = 0,
            todoPendingCount = 0,
            todoTotalCount = 4,
            onOpenShoppingList = {},
            onOpenTodoList = {},
            onOpenCalendar = {},
            onLogout = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentEmptyPreview() {
    HomlyTheme {
        HomeContent(
            todayEventsCount = 0,
            shoppingActiveCount = 0,
            todoPendingCount = 0,
            todoTotalCount = 0,
            onOpenShoppingList = {},
            onOpenTodoList = {},
            onOpenCalendar = {},
            onLogout = {},
        )
    }
}
