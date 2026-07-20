package com.dgero.homly.shopping.presentation

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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.dgero.homly.shopping.domain.model.ShoppingItem
import com.dgero.homly.shopping.domain.model.ShoppingLimits
import com.dgero.homly.shopping.domain.model.ShoppingSortOrder
import com.dgero.homly.ui.theme.HomlyTheme

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ShoppingListContent(
        uiState = uiState,
        onBack = onBack,
        onNewItemTextChange = viewModel::onNewItemTextChange,
        onAdd = viewModel::onAdd,
        onSortChange = viewModel::onSortChange,
        onToggle = viewModel::onToggle,
        onEdit = viewModel::onEdit,
        onDelete = viewModel::onDelete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingListContent(
    uiState: ShoppingListUiState,
    onBack: () -> Unit,
    onNewItemTextChange: (String) -> Unit,
    onAdd: () -> Unit,
    onSortChange: (ShoppingSortOrder) -> Unit,
    onToggle: (ShoppingItem) -> Unit,
    onEdit: (Long, String) -> Unit,
    onDelete: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shopping_list)) },
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
                text = uiState.newItemText,
                isLimitReached = uiState.isLimitReached,
                errorMessage = uiState.errorMessage,
                onTextChange = onNewItemTextChange,
                onAdd = onAdd,
            )
            SortSelector(sortOrder = uiState.sortOrder, onSortChange = onSortChange)

            if (uiState.items.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.items, key = { it.id }) { item ->
                        ShoppingItemRow(
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
}

@Composable
private fun AddItemRow(
    text: String,
    isLimitReached: Boolean,
    errorMessage: String?,
    onTextChange: (String) -> Unit,
    onAdd: () -> Unit,
) {
    val canAdd = text.isNotBlank() && !isLimitReached

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text(stringResource(R.string.add_item)) },
            singleLine = true,
            enabled = !isLimitReached,
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (canAdd) onAdd() }),
            supportingText = {
                when {
                    isLimitReached -> Text(stringResource(R.string.shopping_list_full, ShoppingLimits.MAX_ITEMS))
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
private fun SortSelector(
    sortOrder: ShoppingSortOrder,
    onSortChange: (ShoppingSortOrder) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = sortOrder == ShoppingSortOrder.DATE_DESC,
            onClick = { onSortChange(ShoppingSortOrder.DATE_DESC) },
            label = { Text(stringResource(R.string.date)) },
        )
        FilterChip(
            selected = sortOrder == ShoppingSortOrder.ALPHABETICAL,
            onClick = { onSortChange(ShoppingSortOrder.ALPHABETICAL) },
            label = { Text(stringResource(R.string.alphabetical)) },
        )
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onEdit: (Long, String) -> Unit,
    onDelete: () -> Unit,
) {
    var isEditing by rememberSaveable(item.id) { mutableStateOf(false) }
    var editText by rememberSaveable(item.id) { mutableStateOf(item.name) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = item.isBought, onCheckedChange = { onToggle() })

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
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isBought) TextDecoration.LineThrough else null,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        editText = item.name
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
            text = stringResource(R.string.your_shopping_list_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, locale = "uk")
@Composable
private fun ShoppingListContentPreview() {
    HomlyTheme {
        ShoppingListContent(
            uiState = ShoppingListUiState(
                items = listOf(
                    ShoppingItem(id = 1, name = "Milk", isBought = false, createdAt = 2),
                    ShoppingItem(id = 2, name = "Bread", isBought = true, createdAt = 1),
                ),
            ),
            onBack = {},
            onNewItemTextChange = {},
            onAdd = {},
            onSortChange = {},
            onToggle = {},
            onEdit = { _, _ -> },
            onDelete = {},
        )
    }
}

@Preview(showBackground = true, locale = "uk")
@Composable
private fun ShoppingListEmptyPreview() {
    HomlyTheme {
        ShoppingListContent(
            uiState = ShoppingListUiState(),
            onBack = {},
            onNewItemTextChange = {},
            onAdd = {},
            onSortChange = {},
            onToggle = {},
            onEdit = { _, _ -> },
            onDelete = {},
        )
    }
}
