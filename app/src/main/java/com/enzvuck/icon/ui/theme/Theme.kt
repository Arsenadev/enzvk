package com.enzvuck.icon.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentLime,
    onPrimary = TextOnAccent,
    primaryContainer = AccentIndigo,
    onPrimaryContainer = Color.White,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    outlineVariant = BorderLight,
    error = AccentCoral,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AccentIndigo,
    onPrimary = Color.White,
    primaryContainer = AccentLime,
    onPrimaryContainer = TextOnAccent,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    background = BackgroundLight,
    onBackground = Color(0xFF10141D),
    surface = SurfaceLight,
    onSurface = Color(0xFF10141D),
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Color(0xFF4A5568),
    outline = BorderThemeLight,
    outlineVariant = Color(0xFFCBD5E1),
    error = AccentCoral,
    onError = Color.White
)

@Composable
fun EnzvuckTheme(
    darkTheme: Boolean = true, // Default to dark-first as specified
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
