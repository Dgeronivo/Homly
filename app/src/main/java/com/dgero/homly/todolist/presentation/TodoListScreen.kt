package com.dgero.homly.todolist.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dgero.homly.R
import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.todolist.domain.model.TodoLimits
import com.dgero.homly.ui.theme.HomlyTheme

@Composable
fun TodoListScreen(
    viewModel: TodoListViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TodoListContent(
        uiState = uiState,
        onBack = onBack,
        onNewItemTitleChange = viewModel::onNewItemTitleChange,
        onAdd = viewModel::onAdd,
        onToggle = viewModel::onToggle,
        onEdit = viewModel::onEdit,
        onDelete = viewModel::onDelete,
        onToggleActiveOnly = viewModel::onToggleActiveOnly,
        onClearCompleted = viewModel::onClearCompleted,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoListContent(
    uiState: TodoListUiState,
    onBack: () -> Unit,
    onNewItemTitleChange: (String) -> Unit,
    onAdd: () -> Unit,
    onToggle: (TodoItem) -> Unit,
    onEdit: (Long, String) -> Unit,
    onDelete: (Long) -> Unit,
    onToggleActiveOnly: () -> Unit,
    onClearCompleted: () -> Unit,
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.todo_list)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            AddItemRow(
                title = uiState.newItemTitle,
                isLimitReached = uiState.isLimitReached,
                errorMessage = uiState.titleError,
                onTitleChange = onNewItemTitleChange,
                onAdd = onAdd,
            )
            TodoListControls(
                showActiveOnly = uiState.showActiveOnly,
                isClearEnabled = uiState.completedCount > 0,
                onToggleActiveOnly = onToggleActiveOnly,
                onClearCompletedClick = { showClearConfirm = true },
            )
            if (uiState.items.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.items, key = { it.id }) { item ->
                        TodoItemRow(
                            item = item,
                            onToggle = { onToggle(item) },
                            onEdit = onEdit,
                            onDelete = { onDelete(item.id) },
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        ClearCompletedConfirmDialog(
            completedCount = uiState.completedCount,
            onConfirm = {
                onClearCompleted()
                showClearConfirm = false
            },
            onDismiss = { showClearConfirm = false },
        )
    }
}

@Composable
private fun TodoListControls(
    showActiveOnly: Boolean,
    isClearEnabled: Boolean,
    onToggleActiveOnly: () -> Unit,
    onClearCompletedClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = showActiveOnly,
            onClick = onToggleActiveOnly,
            label = { Text(stringResource(R.string.active_only)) },
        )
        Button(
            onClick = onClearCompletedClick,
            enabled = isClearEnabled,
        ) {
            Text(stringResource(R.string.clear_completed))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoListControlsPreview() {
    HomlyTheme {
        TodoListControls(
            showActiveOnly = false,
            isClearEnabled = true,
            onToggleActiveOnly = {},
            onClearCompletedClick = {},
        )
    }
}

@Composable
private fun ClearCompletedConfirmDialog(
    completedCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_completed_tasks, completedCount)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun ClearCompletedConfirmDialogPreview() {
    HomlyTheme {
        ClearCompletedConfirmDialog(
            completedCount = 3,
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Composable
private fun AddItemRow(
    title: String,
    isLimitReached: Boolean,
    errorMessage: String?,
    onTitleChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val canAdd = title.isNotBlank() && !isLimitReached

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.add_item)) },
            singleLine = true,
            enabled = !isLimitReached,
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (canAdd) onAdd() }),
            supportingText = {
                when {
                    isLimitReached -> Text(stringResource(R.string.todo_list_full, TodoLimits.MAX_ITEMS))
                    errorMessage != null -> Text(errorMessage)
                }
            },
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Button(
            onClick = onAdd,
            enabled = canAdd,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(stringResource(R.string.add))
        }
    }
}

@Composable
private fun TodoItemRow(
    item: TodoItem,
    onToggle: () -> Unit,
    onEdit: (Long, String) -> Unit,
    onDelete: () -> Unit,
) {
    var isEditing by rememberSaveable(item.id) { mutableStateOf(false) }
    var editText by rememberSaveable(item.id) { mutableStateOf(item.title) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = item.isDone, onCheckedChange = { onToggle() })

        if (isEditing) {
            OutlinedTextField(
                value = editText,
                onValueChange = { editText = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onEdit(item.id, editText)
                        isEditing = false
                    },
                ),
                modifier = Modifier.weight(1f),
            )
        } else {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        editText = item.title
                        isEditing = true
                    }
                    .padding(vertical = 12.dp),
            )
        }

        IconButton(onClick = onDelete) {
            Text("✕", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.your_todo_list_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoListContentPreview() {
    HomlyTheme {
        TodoListContent(
            uiState = TodoListUiState(
                items = listOf(
                    TodoItem(id = 1, title = "Buy milk", isDone = false, createdAt = 2),
                    TodoItem(id = 2, title = "Call doctor", isDone = true, createdAt = 1),
                ),
                completedCount = 1,
            ),
            onBack = {},
            onNewItemTitleChange = {},
            onAdd = {},
            onToggle = {},
            onEdit = { _, _ -> },
            onDelete = {},
            onToggleActiveOnly = {},
            onClearCompleted = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoListEmptyPreview() {
    HomlyTheme {
        TodoListContent(
            uiState = TodoListUiState(),
            onBack = {},
            onNewItemTitleChange = {},
            onAdd = {},
            onToggle = {},
            onEdit = { _, _ -> },
            onDelete = {},
            onToggleActiveOnly = {},
            onClearCompleted = {},
        )
    }
}
