package com.dgero.homly.home.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        onOpenShoppingList = onOpenShoppingList,
        onOpenTodoList = onOpenTodoList,
        onOpenCalendar = onOpenCalendar,
        onLogout = { viewModel.onLogout(onLogout) },
    )
}

@Composable
private fun HomeContent(
    todayEventsCount: Int,
    shoppingActiveCount: Int,
    todoPendingCount: Int,
    onOpenShoppingList: () -> Unit,
    onOpenTodoList: () -> Unit,
    onOpenCalendar: () -> Unit,
    onLogout: () -> Unit,
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        FeatureCard(
            title = "Shopping list",
            summary = if (shoppingActiveCount > 0) "$shoppingActiveCount items left to buy" else "Shopping list is empty",
            onClick = onOpenShoppingList,
        )
        Spacer(Modifier.height(8.dp))
        FeatureCard(
            title = "Todo list",
            summary = if (todoPendingCount > 0) "$todoPendingCount tasks pending" else "All tasks done",
            onClick = onOpenTodoList,
        )
        Spacer(Modifier.height(8.dp))
        FeatureCard(
            title = "Calendar",
            summary = if (todayEventsCount > 0) "Today: $todayEventsCount events" else "No events today",
            onClick = onOpenCalendar,
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { showLogoutConfirm = true }) {
            Text("Log out")
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

@Composable
private fun FeatureCard(
    title: String,
    summary: String,
    onClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(summary, style = MaterialTheme.typography.bodyMedium)
        }
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
            onOpenShoppingList = {},
            onOpenTodoList = {},
            onOpenCalendar = {},
            onLogout = {},
        )
    }
}
