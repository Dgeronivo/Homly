package com.dgero.homly.shopping.presentation.designs

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dgero.homly.shopping.domain.model.ShoppingItem
import com.dgero.homly.shopping.domain.model.ShoppingSortOrder
import com.dgero.homly.ui.theme.HomlyTheme

/**
 * Design A — "Classic Cards": mirrors the todolist Classic Cards design — a compact
 * progress banner on top, every item as its own card, and an add-item field styled to
 * the Terracotta Honey palette instead of Material's default primary color.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassicCardsShoppingContent(
    items: List<ShoppingItem>,
    boughtCount: Int,
    newItemText: String,
    sortOrder: ShoppingSortOrder,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping list") },
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
                boughtCount = boughtCount,
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
                FilterChip(
                    selected = sortOrder == ShoppingSortOrder.DATE_DESC,
                    onClick = {},
                    label = { Text("Date") },
                )
                FilterChip(
                    selected = sortOrder == ShoppingSortOrder.ALPHABETICAL,
                    onClick = {},
                    label = { Text("A–Z") },
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = {},
                    placeholder = { Text("Add item") },
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
                    Text("Add")
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
                        ClassicShoppingItemCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactProgressBanner(boughtCount: Int, totalCount: Int, modifier: Modifier = Modifier) {
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
                Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "In cart: $boughtCount of $totalCount",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun ClassicShoppingItemCard(item: ShoppingItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (item.isBought) {
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
            Checkbox(checked = item.isBought, onCheckedChange = {})
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isBought) TextDecoration.LineThrough else null,
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
        text = "Your shopping list is empty",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 32.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun ClassicCardsShoppingPreview() {
    HomlyTheme {
        ClassicCardsShoppingContent(
            items = listOf(
                ShoppingItem(id = 1, name = "Milk", isBought = false, createdAt = 3),
                ShoppingItem(id = 2, name = "Eggs", isBought = false, createdAt = 2),
                ShoppingItem(id = 3, name = "Bread", isBought = true, createdAt = 1),
            ),
            boughtCount = 1,
            newItemText = "",
            sortOrder = ShoppingSortOrder.DATE_DESC,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ClassicCardsShoppingEmptyPreview() {
    HomlyTheme {
        ClassicCardsShoppingContent(
            items = emptyList(),
            boughtCount = 0,
            newItemText = "",
            sortOrder = ShoppingSortOrder.DATE_DESC,
        )
    }
}
