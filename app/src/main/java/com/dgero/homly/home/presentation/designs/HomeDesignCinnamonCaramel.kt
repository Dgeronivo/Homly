package com.dgero.homly.home.presentation.designs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design 4 — "Кориця і карамель": глибокі теплі коричневі тони, наче шкіряний бірка,
 * асиметрична форма hero-картки. Структура екрана незмінна.
 */

private val CinnamonCaramelColors = lightColorScheme(
    background = Color(0xFFF7EDE2),
    surface = Color(0xFFF7EDE2),
    onBackground = Color(0xFF3A2415),
    onSurface = Color(0xFF3A2415),
    primaryContainer = Color(0xFF7A3B1E),
    onPrimaryContainer = Color(0xFFFCEFE2),
    secondaryContainer = Color(0xFFD9A566),
    onSecondaryContainer = Color(0xFF3A2415),
    surfaceVariant = Color(0xFFD9A566),
    onSurfaceVariant = Color(0xFF3A2415),
)

private val CinnamonCaramelTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
        lineHeight = 31.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
    ),
)

private val CinnamonCaramelShapes = Shapes(
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(topStart = 6.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 6.dp),
)

@Composable
private fun CinnamonCaramelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CinnamonCaramelColors,
        typography = CinnamonCaramelTypography,
        shapes = CinnamonCaramelShapes,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CinnamonCaramelHomeContent(
    todayEventsCount: Int,
    shoppingActiveCount: Int,
    todoPendingCount: Int,
    todoTotalCount: Int,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Home", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
        ) {
            CinnamonHeroCard(
                icon = Icons.Default.DateRange,
                title = "Calendar",
                summary = if (todayEventsCount > 0) "Today: $todayEventsCount events" else "No events today",
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CinnamonGridCard(
                    icon = Icons.Default.ShoppingCart,
                    title = "Shopping list",
                    summary = if (shoppingActiveCount > 0) "$shoppingActiveCount left" else "Empty",
                    modifier = Modifier.weight(1f),
                )
                CinnamonGridCard(
                    icon = Icons.Default.CheckCircle,
                    title = "Todo list",
                    summary = when {
                        todoPendingCount > 0 -> "$todoPendingCount pending"
                        todoTotalCount > 0 -> "All done"
                        else -> "Empty"
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CinnamonHeroCard(icon: ImageVector, title: String, summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable {},
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun CinnamonGridCard(icon: ImageVector, title: String, summary: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable {},
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CinnamonCaramelHomePreview() {
    CinnamonCaramelTheme {
        CinnamonCaramelHomeContent(
            todayEventsCount = 2,
            shoppingActiveCount = 5,
            todoPendingCount = 3,
            todoTotalCount = 5,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CinnamonCaramelHomeEmptyPreview() {
    CinnamonCaramelTheme {
        CinnamonCaramelHomeContent(
            todayEventsCount = 0,
            shoppingActiveCount = 0,
            todoPendingCount = 0,
            todoTotalCount = 0,
        )
    }
}
