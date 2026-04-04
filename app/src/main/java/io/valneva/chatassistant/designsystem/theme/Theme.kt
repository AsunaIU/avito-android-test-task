package io.valneva.chatassistant.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3D63F3),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF5D6472),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFF0F1F5),
    onSurfaceVariant = Color(0xFF5D6472),
    outline = Color(0xFFD8DCE4),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8DA2FF),
    onPrimary = Color(0xFF111318),
    secondary = Color(0xFFB3B8C7),
    onSecondary = Color(0xFF0E0F12),
    background = Color(0xFF0E0F12),
    onBackground = Color(0xFFF5F6FA),
    surface = Color(0xFF121317),
    onSurface = Color(0xFFF5F6FA),
    surfaceVariant = Color(0xFF1C1F26),
    onSurfaceVariant = Color(0xFFB3B8C7),
    outline = Color(0xFF31343D),
)

@Composable
fun ChatAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ChatAssistantTypography,
        content = content,
    )
}