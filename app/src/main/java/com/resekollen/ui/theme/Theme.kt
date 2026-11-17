package com.resekollen.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.resekollen.data.WeatherLook

val Ocean = Color(0xFF0B6E99)
val Sunset = Color(0xFFF29E4C)
val Lagoon = Color(0xFF2A9D8F)

private val LightColors = lightColorScheme(
    primary = Ocean,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDE8F5),
    onPrimaryContainer = Color(0xFF00344A),
    secondary = Sunset,
    onSecondary = Color(0xFF3B1E00),
    secondaryContainer = Color(0xFFFFE2C7),
    onSecondaryContainer = Color(0xFF4A2600),
    tertiary = Lagoon,
    tertiaryContainer = Color(0xFFCFF1EC),
    background = Color(0xFFF4F7FA),
    surface = Color(0xFFF4F7FA),
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFEAF0F5),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7CCBEF),
    onPrimary = Color(0xFF00344A),
    primaryContainer = Color(0xFF004D6A),
    onPrimaryContainer = Color(0xFFCDE8F5),
    secondary = Color(0xFFFFB877),
    secondaryContainer = Color(0xFF6A3A00),
    onSecondaryContainer = Color(0xFFFFE2C7),
    tertiary = Color(0xFF7FD8CB),
    tertiaryContainer = Color(0xFF00504A),
    background = Color(0xFF0F1418),
    surface = Color(0xFF0F1418),
    surfaceContainerLow = Color(0xFF172027),
    surfaceContainer = Color(0xFF1C262E),
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
)

@Composable
fun ReseKollenTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = AppShapes,
        content = content,
    )
}

/** Bakgrundsgradient för väderkortet beroende på väder och dag/natt. */
fun weatherGradient(kind: WeatherLook.Kind, isDay: Boolean): Brush {
    val colors = when {
        !isDay -> listOf(Color(0xFF1B2550), Color(0xFF4A3F8C))
        kind == WeatherLook.Kind.CLEAR -> listOf(Color(0xFF2E8FD8), Color(0xFFF2B05E))
        kind == WeatherLook.Kind.RAIN || kind == WeatherLook.Kind.STORM -> listOf(Color(0xFF3D5A73), Color(0xFF6E8CA0))
        kind == WeatherLook.Kind.SNOW -> listOf(Color(0xFF6C9BC2), Color(0xFFB7CFE3))
        else -> listOf(Color(0xFF3F7EA6), Color(0xFF8BB3CC))
    }
    return Brush.linearGradient(colors)
}
