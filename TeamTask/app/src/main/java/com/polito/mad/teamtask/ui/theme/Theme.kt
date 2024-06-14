package com.polito.mad.teamtask.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MaizeDarkMode,                    // Primary
    primaryContainer = LightYellowDarkMode,   // Message back
    secondary = CaribbeanCurrent,       // Secondary
    tertiary = Amethyst,                // Tertiary
    surface = MyBlack,                   // Background
    surfaceVariant = DarkGreyDarkMode,  // Card back
    onSurface = WhiteSmoke,             // Text
    onSurfaceVariant = LightGrey,        // Button border
    error = Red,                        // Error
    inverseSurface = Orange,            // Scheduled
    inverseOnSurface = LightBlue,       // Blue
    inversePrimary = Green              // Success
)

private val LightColorScheme = lightColorScheme(
    primary = Maize,                    // Primary
    primaryContainer = LightYellow,     // Message back
    secondary = CaribbeanCurrent,       // Secondary
    tertiary = Amethyst,                // Tertiary
    surface = WhiteSmoke,               // Background
    surfaceVariant = LightGrey,         // Card back
    onSurface = Jet,                    // Text
    onSurfaceVariant = DarkGrey,        // Button border
    error = Red,                        // Error
    inverseSurface = Orange,            // Scheduled
    inverseOnSurface = LightBlue,       // Blue
    inversePrimary = Green              // Success
)

@Composable
fun TeamTaskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
