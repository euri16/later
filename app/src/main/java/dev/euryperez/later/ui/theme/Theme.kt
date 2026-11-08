package dev.euryperez.later.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Indigo = Color(0xFF5B4CDB)
val IndigoDark = Color(0xFF3B2FA8)
val Lavender = Color(0xFFEDEAFF)
val Coral = Color(0xFFFF6B5B)
val Ink = Color(0xFF1C1B29)
val Fog = Color(0xFFB4B2C2)
val Overdue = Color(0xFFE5484D)

private val LaterColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Lavender,
    onPrimaryContainer = IndigoDark,
    secondary = Coral,
    onSecondary = Color.White,
    background = Color(0xFFF8F7FC),
    surface = Color.White,
    onSurface = Ink,
    onBackground = Ink,
)

@Composable
fun LaterTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LaterColors, content = content)
}
