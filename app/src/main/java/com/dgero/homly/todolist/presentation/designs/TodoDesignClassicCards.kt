package com.dgero.homly.todolist.presentation.designs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dgero.homly.R
import com.dgero.homly.todolist.domain.model.TodoItem
import com.dgero.homly.ui.theme.HomlyTheme

// A desaturated take on Material's default primary purple — keeps the accent color but
// stops it from outshining the "Active only" filter chip next to it.
private val MutedPurple = Color(0xFF9A8FB5)
private val MutedPurpleOn = Color(0xFFFFFFFF)

// Shared shape for "Active only" and "Clear completed" so the two controls match —
// squarer than the default pill button / rounded chip.
private val ControlShape = RoundedCornerShape(8.dp)

/**
 * Design A — "Classic Cards": current list structure, restyled so every item is its own
 * card, with a compact progress banner on top and an add-item field styled to the
 * Terracotta Honey palette instead of Material's default (unthemed) primary color.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassicCardsTodoContent(
    items: List<TodoItem>,
    completedCount: Int,
    newItemTitle: String,
    showActiveOnly: Boolean,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.todo_list)) },
                navigationIcon = {
                    IconButton(onClick = {}) {
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
            CompactProgressBanner(
                completedCount = completedCount,
                totalCount = items.size,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {},
                    shape = ControlShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showActiveOnly) MutedPurple else MutedPurple.copy(alpha = 0.5f),
                        contentColor = MutedPurpleOn,
                    ),
                ) {
                    Text(stringResource(R.string.active_only))
                }
                Button(
                    onClick = {},
                    enabled = completedCount > 0,
                    shape = ControlShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MutedPurple,
                        contentColor = MutedPurpleOn,
                        disabledContainerColor = MutedPurple.copy(alpha = 0.5f),
                        disabledContentColor = MutedPurpleOn,
                    ),
                ) {
                    Text(stringResource(R.string.clear_completed))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = newItemTitle,
                    onValueChange = {},
                    placeholder = { Text(stringResource(R.string.add_item)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Text(stringResource(R.string.add))
                }
            }
            if (items.isEmpty()) {
                ClassicCardsEmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        ClassicTodoItemCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactProgressBanner(completedCount: Int, totalCount: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.progress, completedCount, totalCount),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun ClassicTodoItemCard(item: TodoItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (item.isDone) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = item.isDone, onCheckedChange = {})
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
            )
            IconButton(onClick = {}) {
                Text("✕", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ClassicCardsEmptyState() {
    Text(
        text = stringResource(R.string.your_todo_list_empty),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 32.dp),
    )
}

@Preview(showBackground = true, locale = "uk")
@Composable
private fun ClassicCardsTodoPreview() {
    HomlyTheme {
        ClassicCardsTodoContent(
            items = listOf(
                TodoItem(id = 1, title = "Buy milk", isDone = false, createdAt = 3),
                TodoItem(id = 2, title = "Call doctor", isDone = false, createdAt = 2),
                TodoItem(id = 3, title = "Water the plants", isDone = true, createdAt = 1),
            ),
            completedCount = 1,
            newItemTitle = "",
            showActiveOnly = false,
        )
    }
}

@Preview(showBackground = true, locale = "uk")
@Composable
private fun ClassicCardsTodoActiveFilterPreview() {
    HomlyTheme {
        ClassicCardsTodoContent(
            items = listOf(
                TodoItem(id = 1, title = "Buy milk", isDone = false, createdAt = 3),
                TodoItem(id = 2, title = "Call doctor", isDone = false, createdAt = 2),
                TodoItem(id = 3, title = "Water the plants", isDone = true, createdAt = 1),
            ),
            completedCount = 1,
            newItemTitle = "",
            showActiveOnly = true,
        )
    }
}

@Preview(showBackground = true, locale = "uk")
@Composable
private fun ClassicCardsTodoEmptyPreview() {
    HomlyTheme {
        ClassicCardsTodoContent(
            items = emptyList(),
            completedCount = 0,
            newItemTitle = "",
            showActiveOnly = false,
        )
    }
}
