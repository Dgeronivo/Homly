package com.dgero.homly.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Terracotta Honey — the app's single brand palette, used regardless of system dark mode
// or Material You dynamic color, so the design stays consistent across devices.
private val HomlyColorScheme = lightColorScheme(
    background = TerracottaHoneyBackground,
    surface = TerracottaHoneySurface,
    onBackground = TerracottaHoneyOnBackground,
    onSurface = TerracottaHoneyOnSurface,
    primaryContainer = TerracottaHoneyPrimaryContainer,
    onPrimaryContainer = TerracottaHoneyOnPrimaryContainer,
    secondaryContainer = TerracottaHoneySecondaryContainer,
    onSecondaryContainer = TerracottaHoneyOnSecondaryContainer,
    surfaceVariant = TerracottaHoneySurfaceVariant,
    onSurfaceVariant = TerracottaHoneyOnSurfaceVariant,
)

@Composable
fun HomlyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HomlyColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}
