package com.hayesenterprise.the93seven

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Pulled from the 93SEVEN logo: warm gold on dark charcoal.
val Gold = Color(0xFFE9C76B)
val GoldDeep = Color(0xFFF0A950)
val Charcoal = Color(0xFF1C1F1E)
val CharcoalLight = Color(0xFF2A2E2D)
val Over = Color(0xFFFF7A6B)

private val Scheme = darkColorScheme(
    primary = Gold,
    onPrimary = Color(0xFF1C1F1E),
    secondary = GoldDeep,
    onSecondary = Color(0xFF1C1F1E),
    background = Charcoal,
    onBackground = Color(0xFFF2F2EE),
    surface = CharcoalLight,
    onSurface = Color(0xFFF2F2EE),
    surfaceVariant = Color(0xFF353A38),
    onSurfaceVariant = Color(0xFFB9BDB9),
    error = Over
)

@Composable
fun NinetySevenTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
