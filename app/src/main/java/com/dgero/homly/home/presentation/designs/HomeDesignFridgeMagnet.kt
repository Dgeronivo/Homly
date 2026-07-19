package com.dgero.homly.home.presentation.designs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design 6 (експериментальний) — "Магнітики на холодильнику": темна крейдяна панель,
 * кожна картка — окрема кольорова наліпка з легким нахилом, як записки на дверцятах холодильника.
 * Порядок і розташування елементів як в оригіналі (hero + рядок з двома картками),
 * але результат навмисно несподіваний.
 */

private val FridgeMagnetColors = lightColorScheme(
    background = Color(0xFF2E2621),
    surface = Color(0xFF2E2621),
    onBackground = Color(0xFFF3E9DC),
    onSurface = Color(0xFFF3E9DC),
    primaryContainer = Color(0xFFE8B84B),
    onPrimaryContainer = Color(0xFF2E2621),
    secondaryContainer = Color(0xFFDD6B4C),
    onSecondaryContainer = Color(0xFFFFF6EF),
    surfaceVariant = Color(0xFF8FA26B),
    onSurfaceVariant = Color(0xFF23301A),
)

private val FridgeMagnetTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
    ),
)

private val FridgeMagnetShapes = Shapes(
    medium = CutCornerShape(bottomEnd = 22.dp),
    large = RoundedCornerShape(10.dp),
)

@Composable
private fun FridgeMagnetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FridgeMagnetColors,
        typography = FridgeMagnetTypography,
        shapes = FridgeMagnetShapes,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FridgeMagnetHomeContent(
    todayEventsCount: Int,
    shoppingActiveCount: Int,
    todoPendingCount: Int,
    todoTotalCount: Int,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("HOME", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
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
            MagnetCard(
                icon = Icons.Default.DateRange,
                title = "Calendar",
                summary = if (todayEventsCount > 0) "Today: $todayEventsCount events" else "No events today",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                rotationDegrees = -3f,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MagnetCard(
                    icon = Icons.Default.ShoppingCart,
                    title = "Shopping list",
                    summary = if (shoppingActiveCount > 0) "$shoppingActiveCount left" else "Empty",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    rotationDegrees = 2.5f,
                    modifier = Modifier.weight(1f),
                )
                MagnetCard(
                    icon = Icons.Default.CheckCircle,
                    title = "Todo list",
                    summary = when {
                        todoPendingCount > 0 -> "$todoPendingCount pending"
                        todoTotalCount > 0 -> "All done"
                        else -> "Empty"
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    onContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    rotationDegrees = -2f,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MagnetCard(
    icon: ImageVector,
    title: String,
    summary: String,
    containerColor: Color,
    onContainerColor: Color,
    rotationDegrees: Float,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .graphicsLayer(rotationZ = rotationDegrees)
            .clickable {},
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(3.dp, onContainerColor.copy(alpha = 0.25f)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = onContainerColor)
            Spacer(Modifier.height(10.dp))
            Text(title.uppercase(), style = MaterialTheme.typography.titleMedium, color = onContainerColor)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = onContainerColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FridgeMagnetHomePreview() {
    FridgeMagnetTheme {
        FridgeMagnetHomeContent(
            todayEventsCount = 2,
            shoppingActiveCount = 5,
            todoPendingCount = 3,
            todoTotalCount = 5,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FridgeMagnetHomeEmptyPreview() {
    FridgeMagnetTheme {
        FridgeMagnetHomeContent(
            todayEventsCount = 0,
            shoppingActiveCount = 0,
            todoPendingCount = 0,
            todoTotalCount = 0,
        )
    }
}
