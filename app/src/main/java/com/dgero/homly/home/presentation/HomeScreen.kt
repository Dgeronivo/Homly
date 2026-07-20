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
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dgero.homly.R
import com.dgero.homly.ui.theme.HomlyGridCard
import com.dgero.homly.ui.theme.HomlyHeroCard
import com.dgero.homly.ui.theme.HomlyTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    val locale = LocalLocale.current.platformLocale
    val todayLabel = remember(locale) {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)
        LocalDate.now().format(formatter).replaceFirstChar { it.titlecase(locale) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(todayLabel) },
                actions = {
                    IconButton(onClick = { showLogoutConfirm = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = stringResource(R.string.log_out))
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
                title = stringResource(R.string.calendar),
                summary = if (todayEventsCount > 0) stringResource(R.string.today_events, todayEventsCount) else stringResource(R.string.no_events_today),
                onClick = onOpenCalendar,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HomlyGridCard(
                    icon = Icons.Default.ShoppingCart,
                    title = stringResource(R.string.shopping_list),
                    summary = if (shoppingActiveCount > 0) stringResource(R.string.shopping_left, shoppingActiveCount) else stringResource(R.string.empty),
                    onClick = onOpenShoppingList,
                    modifier = Modifier.weight(1f),
                )
                HomlyGridCard(
                    icon = Icons.Default.CheckCircle,
                    title = stringResource(R.string.todo_list),
                    summary = when {
                        todoPendingCount > 0 -> stringResource(R.string.todo_pending, todoPendingCount)
                        todoTotalCount > 0 -> stringResource(R.string.all_done)
                        else -> stringResource(R.string.empty)
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
            title = { Text(stringResource(R.string.log_out_question)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                ) {
                    Text(stringResource(R.string.log_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Preview(showBackground = true, locale = "uk")
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

@Preview(showBackground = true, locale = "uk")
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

@Preview(showBackground = true, locale = "uk")
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
